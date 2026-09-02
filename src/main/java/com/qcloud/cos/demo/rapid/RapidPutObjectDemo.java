package com.qcloud.cos.demo.rapid;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.auth.RapidCOSCredentialProvider;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;

/**
 * 高性能桶上传对象示例
 */
public class RapidPutObjectDemo {
    private static String secretId = "AKIDXXXXXXXX";
    private static String secretKey = "1A2Z3YYYYYYYYYY";
    // 高性能桶名格式: <bucket>-x--<appid>
    private static String bucketName = "examplebucket-x--12500000000";
    private static String region = "ap-guangzhou";
    private static COSClient cosClient = createClient();

    public static void main(String[] args) {
        try {
            putLocalFileDemo();
            // putInputStreamDemo();
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

    private static void putLocalFileDemo() {
        String key = "abc/abc.txt";
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, new File("abc.txt"));
        try {
            PutObjectResult result = cosClient.putObject(putObjectRequest);
            System.out.println(result.getRequestId());
        } catch (CosServiceException e) {
            e.printStackTrace();
        } catch (CosClientException e) {
            e.printStackTrace();
        }
    }

    private static void putInputStreamDemo() {
        String key = "abc/def.txt";
        int inputStreamLength = 1024 * 1024;
        byte[] data = new byte[inputStreamLength];
        InputStream inputStream = new ByteArrayInputStream(data);

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(inputStreamLength);

        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, inputStream, objectMetadata);
        try {
            PutObjectResult result = cosClient.putObject(putObjectRequest);
            System.out.println(result.getRequestId());
        } catch (CosServiceException e) {
            e.printStackTrace();
        } catch (CosClientException e) {
            e.printStackTrace();
        }
    }
}
