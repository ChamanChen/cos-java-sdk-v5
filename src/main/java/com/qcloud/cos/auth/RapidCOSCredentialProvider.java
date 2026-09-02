package com.qcloud.cos.auth;

import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.CreateSessionRequest;
import com.qcloud.cos.model.CreateSessionResult;

/**
 * cos-rapid 高性能桶透明凭证 provider。
 * 根据 useRapidSession 参数自动路由：桶级请求返回基础凭证，对象级请求返回 Session 临时密钥。
 */
public class RapidCOSCredentialProvider implements COSCredentialsProvider {

    private final COSCredentialsProvider baseCredProvider;
    private final RapidSessionManager rapidSessionManager;

    public RapidCOSCredentialProvider(COSCredentials cred) {
        this(new COSStaticCredentialsProvider(cred));
    }

    public RapidCOSCredentialProvider(COSCredentialsProvider baseCredProvider) {
        if (baseCredProvider == null) {
            throw new CosClientException("baseCredProvider must not be null for RapidCOSCredentialProvider");
        }
        if (baseCredProvider instanceof RapidCOSCredentialProvider) {
            throw new CosClientException("baseCredProvider must not be another RapidCOSCredentialProvider");
        }
        this.baseCredProvider = baseCredProvider;
        this.rapidSessionManager = new RapidSessionManager(this);
    }

    public COSCredentialsProvider getBaseCredProvider() {
        return baseCredProvider;
    }

    /** 返回基础凭证（桶级请求默认行为）。 */
    @Override
    public COSCredentials getCredentials() {
        return baseCredProvider.getCredentials();
    }

    /** 按路由参数返回凭证：useRapidSession=true 时返回 Session 临时密钥，否则返回基础凭证。 */
    @Override
    public COSCredentials getCredentials(
        String endpoint, String scheme, boolean useRapidSession, String bucketName) throws CosClientException {
        if (!useRapidSession) {
            return getCredentials();
        }
        if (bucketName == null || bucketName.isEmpty()) {
            throw new CosClientException("bucketName is required for getCredentials");
        }
        return rapidSessionManager.getActiveSession(bucketName, endpoint, scheme);
    }

    /** 为预签名获取凭证，确保 Session 剩余有效期 ≥ requiredRemainingSec。 */
    public COSCredentials getCredentialsForPresign(String endpoint, String scheme,
                                                   String bucketName, long requiredRemainingSec) throws CosClientException {
        if (bucketName == null || bucketName.isEmpty()) {
            throw new CosClientException("bucketName is required for getCredentialsForPresign");
        }
        return rapidSessionManager.getActiveSessionForPresign(bucketName, endpoint, scheme, requiredRemainingSec);
    }

    @Override
    public void refresh() {
        baseCredProvider.refresh();
    }
}
