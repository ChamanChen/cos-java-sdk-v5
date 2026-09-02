package com.qcloud.cos.auth;

import java.util.Objects;

/**
 * rapid 高性能桶的临时数据密钥凭证。
 */
public class RapidSessionCredentials extends BasicSessionCredentials {

    private static final long serialVersionUID = 1L;

    /**
     * 凭证的绝对过期时间戳（秒，Unix epoch）。
     */
    private final long expiredTimeSec;

    /**
     * 获取该临时凭证时使用的长期 AKID。
     */
    private final String baseAKID;

    public RapidSessionCredentials(String accessKey, String secretKey, String sessionToken,
            long expiredTimeSec, String baseAKID) {
        super(accessKey, secretKey, sessionToken);
        this.expiredTimeSec = expiredTimeSec;
        this.baseAKID = baseAKID;
    }

    /**
     * @return 凭证的绝对过期时间戳（秒，Unix epoch）。
     */
    public long getExpiredTimeSec() {
        return expiredTimeSec;
    }

    /**
     * @return 获取该临时凭证时使用的长期 AKID。
     */
    public String getBaseAKID() {
        return baseAKID;
    }

    /**
     * 判断凭证是否即将过期。
     */
    public boolean isAboutToExpire(int safetyMarginSec) {
        long nowSec = System.currentTimeMillis() / 1000L;
        return (expiredTimeSec - nowSec) <= safetyMarginSec;
    }

    /**
     * 判断上游 baseCredProvider 的 AKID 是否已发生变化（与获取本凭证时的 AKID 不一致）。
     */
    public boolean isBaseAKIDChanged(String currentBaseAKID) {
        return !Objects.equals(baseAKID, currentBaseAKID);
    }
}
