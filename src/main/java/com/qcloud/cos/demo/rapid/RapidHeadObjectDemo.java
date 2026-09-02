package com.qcloud.cos.demo.rapid;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.auth.RapidCOSCredentialProvider;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.region.Region;

/**
 * 高性能桶查询对象元信息示例
 */
public class RapidHeadObjectDemo {
    private static String secretId = "AKIDXXXXXXXX";
    private static String secretKey = "1A2Z3YYYYYYYYYY";
    private static String bucketName = "examplebucket-x--12500000000";
    private static String region = "ap-guangzhou";
    private static COSClient cosClient = createClient();

    public static void main(String[] args) {
        try {
            doesObjectExistDemo();
            headObjectDemo();
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

    private static void doesObjectExistDemo() {
        String key = "aaa/bbb.txt";
        boolean isExist = cosClient.doesObjectExist(bucketName, key);
        System.out.println("object exist: " + isExist);
    }

    private static void headObjectDemo() {
        String key = "aaa/bbb.txt";
        ObjectMetadata metadata = cosClient.getObjectMetadata(bucketName, key);
        System.out.println("content-length: " + metadata.getContentLength());
        System.out.println("content-type: " + metadata.getContentType());
    }
}
