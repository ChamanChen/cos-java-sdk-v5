package com.qcloud.cos.auth;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.CreateSessionRequest;
import com.qcloud.cos.model.CreateSessionResult;

import com.qcloud.cos.utils.EndpointUtils;

/**
 * cos-rapid 高性能桶 Session 管理器。
 * per-bucket 缓存 CreateSession 临时密钥，剩余有效期 ≤ 60s 时惰性刷新。
 */
public class RapidSessionManager {

    private static final Logger log = LoggerFactory.getLogger(RapidSessionManager.class);

    public static final int MAX_SESSION_DURATION_SEC = 300;
    public static final int REFRESH_THRESHOLD_SEC = 60;

    private final ConcurrentMap<String, RapidSessionCredentials> bucketToRapidCredMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Object> lockMap = new ConcurrentHashMap<>();
    private final RapidCOSCredentialProvider owner;

    public RapidSessionManager(RapidCOSCredentialProvider owner) {
        if (owner == null) {
            throw new CosClientException("owner must not be null for RapidSessionManager");
        }
        this.owner = owner;
    }

    /** 获取指定桶的临时密钥，必要时惰性刷新。 */
    public RapidSessionCredentials getActiveSession(String bucketName, String endpoint, String scheme) {
        return getActiveSession(bucketName, endpoint, scheme, 0);
    }

    private RapidSessionCredentials getActiveSession(
        String bucketName, String endpoint, String scheme, long requiredRemainingSec) throws CosClientException {
        RapidSessionCredentials cred = bucketToRapidCredMap.get(bucketName);
        // String currentBaseAKID = resolveCurrentBaseAKID();

        // 不检查 baseAKID 变化：AssumeRole 场景下 baseAK 按分钟级轮换，
        // 但服务端颁发的 Session 临时密钥在有效期内始终可用，无需跟随 baseAK 刷新。
        int refreshThresholdSec = (int) Math.max(requiredRemainingSec, REFRESH_THRESHOLD_SEC);
        if (cred != null && !cred.isAboutToExpire(refreshThresholdSec)) {
            return cred;
        }

        synchronized (lockFor(bucketName)) {
            cred = bucketToRapidCredMap.get(bucketName);
            // 不检查 && !cred.isBaseAKChanged(currentBaseAKID)
            if (cred != null && !cred.isAboutToExpire(refreshThresholdSec)) {
                return cred;
            }

            String currentBaseAKID = resolveCurrentBaseAKID();
            RapidSessionCredentials refreshed = doRefresh(bucketName, endpoint, scheme, currentBaseAKID);
            bucketToRapidCredMap.put(bucketName, refreshed);
            return refreshed;
        }
    }

    private String resolveCurrentBaseAKID() throws CosClientException {
        COSCredentials baseCred = owner.getBaseCredProvider().getCredentials();
        if (baseCred == null) {
            throw new CosClientException(
                    "base credentials from Provider is null. please check your base credentials provider");
        }
        return baseCred.getCOSAccessKeyId();
    }

    private RapidSessionCredentials doRefresh(String bucketName, String endpoint, String scheme, String baseAKID) {
        COSClient tempClient = getOrCreateTempClient(endpoint, scheme);
        CreateSessionRequest request = new CreateSessionRequest(bucketName);
        request.setFixedEndpointAddr(endpoint);

        CreateSessionResult result = tempClient.createSession(request);
        CreateSessionResult.Credentials c = result.getCredentials();
        if (c == null || c.getTmpSecretId() == null || c.getTmpSecretKey() == null
            || c.getSessionToken() == null) {
            throw new CosClientException(
                "CreateSession returned invalid credentials for bucket: " + bucketName
                + ". credentials=" + (c == null ? "null"
                    : "{tmpSecretId=" + (c.getTmpSecretId() == null ? "null" : "set")
                    + ", tmpSecretKey=" + (c.getTmpSecretKey() == null ? "null" : "set")
                    + ", sessionToken=" + (c.getSessionToken() == null ? "null" : "set") + "}")
                + ", expiration=" + result.getExpiration()
                + ", requestId=" + result.getRequestId());
        }

        // 优先使用 ExpiredTime 时间戳，若服务端未返回则从 Expiration ISO8601 字符串解析
        long expiredTimeSec = result.getExpiredTime();
        if (expiredTimeSec <= 0 && result.getExpiration() != null) {
            expiredTimeSec = java.time.Instant.parse(result.getExpiration()).getEpochSecond();
        }
        if (expiredTimeSec <= 0) {
            throw new CosClientException(
                "CreateSession returned no valid expiration for bucket: " + bucketName
                + ", expiredTime=" + result.getExpiredTime()
                + ", expiration=" + result.getExpiration()
                + ", requestId=" + result.getRequestId());
        }

        log.debug("CreateSession success for bucket: {}, requestId: {}, expiredTime: {}",
                bucketName, result.getRequestId(), expiredTimeSec);
        return new RapidSessionCredentials(c.getTmpSecretId(), c.getTmpSecretKey(), c.getSessionToken(), expiredTimeSec, baseAKID);
    }

    /** 构造临时 COSClient 发起 CreateSession，用完即弃。 */
    private COSClient getOrCreateTempClient(String endpoint, String scheme) {
        if (endpoint == null) {
            throw new CosClientException(
                    "endpoint is required to create a temporary COSClient for CreateSession");
        }
        String suffix = EndpointUtils.extractEndpointSuffix(endpoint);
        if (suffix == null) {
            throw new CosClientException(
                "failed to extract endpoint suffix from endpoint: " + endpoint
                    + ". Expected format like: bucket.cosrapid.<region>.myqcloud.com");
        }
        // 从 suffix（如 cosrapid.ap-guangzhou.myqcloud.com）中提取 region
        String regionName = EndpointUtils.extractRegionFromSuffix(suffix);
        ClientConfig tempConfig = new ClientConfig();
        if (regionName != null) {
            tempConfig.setRegion(new com.qcloud.cos.region.Region(regionName));
        }
        tempConfig.setEndPointSuffix(suffix);
        if (scheme != null && !scheme.isEmpty()) {
            tempConfig.setHttpProtocol(HttpProtocol.valueOf(scheme.toLowerCase()));
        }
        return new COSClient(owner, tempConfig);
    }

    /** 获取临时密钥并确保剩余有效期满足预签名需求。 */
    public RapidSessionCredentials getActiveSessionForPresign(String bucketName, String endpoint,
            String scheme, long requiredRemainingSec) {
        if (requiredRemainingSec > MAX_SESSION_DURATION_SEC) {
            throw new CosClientException(
                "The pre-signed URL expiration (" + requiredRemainingSec + "s) exceeds the maximum "
                    + "session token duration (" + MAX_SESSION_DURATION_SEC + "s). "
                    + "Rapid bucket pre-signed URLs cannot exceed " + MAX_SESSION_DURATION_SEC + " seconds.");
        }

        RapidSessionCredentials cred = getActiveSession(bucketName, endpoint, scheme, requiredRemainingSec);

        long refreshedRemaining = cred.getExpiredTimeSec() - (System.currentTimeMillis() / 1000L);
        if (refreshedRemaining < requiredRemainingSec) {
            throw new CosClientException(
                "After refreshing session token, the remaining validity (" + refreshedRemaining
                    + "s) is still less than the required pre-signed URL duration ("
                    + requiredRemainingSec + "s). "
                    + "Please reduce the pre-signed URL expiration time.");
        }
        return cred;
    }

    private Object lockFor(String bucketName) {
        return lockMap.computeIfAbsent(bucketName, k -> new Object());
    }
}
