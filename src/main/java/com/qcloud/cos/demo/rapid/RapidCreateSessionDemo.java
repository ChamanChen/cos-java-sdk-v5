package com.qcloud.cos.demo.rapid;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.auth.RapidCOSCredentialProvider;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.CreateSessionRequest;
import com.qcloud.cos.model.CreateSessionResult;
import com.qcloud.cos.region.Region;

/**
 * 高性能桶手动换取 Session 临时凭证示例（高性能桶专用接口）
 *
 * 通常无需手动调用此接口，RapidCOSCredentialProvider 会自动管理 Session 的获取和缓存。
 * 此 demo 仅用于演示如何手动获取 Session 临时凭证。
 */
public class RapidCreateSessionDemo {
    private static String secretId = "AKIDXXXXXXXX";
    private static String secretKey = "1A2Z3YYYYYYYYYY";
    private static String bucketName = "examplebucket-x--12500000000";
    private static String region = "ap-guangzhou";
    private static COSClient cosClient = createClient();

    public static void main(String[] args) {
        try {
            createSessionDemo();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cosClient.shutdown();
        }
    }

    private static COSClient createClient() {
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        RapidCOSCredentialProvider credProvider = new RapidCOSCredentialProvider(cred);
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        return new COSClient(credProvider, clientConfig);
    }

    private static void createSessionDemo() {
        CreateSessionRequest request = new CreateSessionRequest(bucketName);
        try {
            CreateSessionResult result = cosClient.createSession(request);
            CreateSessionResult.Credentials cred = result.getCredentials();
            System.out.println("sessionToken: " + cred.getSessionToken());
            System.out.println("tmpSecretId: " + cred.getTmpSecretId());
            System.out.println("tmpSecretKey: " + cred.getTmpSecretKey());
            System.out.println("expiration: " + result.getExpiration());
        } catch (CosServiceException e) {
            e.printStackTrace();
        } catch (CosClientException e) {
            e.printStackTrace();
        }
    }
}
