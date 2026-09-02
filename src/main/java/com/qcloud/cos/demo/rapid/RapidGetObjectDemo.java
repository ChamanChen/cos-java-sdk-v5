package com.qcloud.cos.demo.rapid;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.auth.RapidCOSCredentialProvider;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.utils.IOUtils;

/**
 * 高性能桶下载对象示例
 */
public class RapidGetObjectDemo {
    private static String secretId = "AKIDXXXXXXXX";
    private static String secretKey = "1A2Z3YYYYYYYYYY";
    private static String bucketName = "examplebucket-x--12500000000";
    private static String region = "ap-guangzhou";
    private static COSClient cosClient = createClient();

    public static void main(String[] args) {
        try {
            getObjectToFileDemo();
            // getObjectInputStreamDemo();
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

    private static void getObjectToFileDemo() {
        String key = "test/my_test.json";
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, key);
        try {
            File localFile = new File("my_test.json");
            ObjectMetadata objectMetadata = cosClient.getObject(getObjectRequest, localFile);
            System.out.println(objectMetadata.getContentLength());
        } catch (CosServiceException e) {
            e.printStackTrace();
        } catch (CosClientException e) {
            e.printStackTrace();
        }
    }

    private static void getObjectInputStreamDemo() throws IOException {
        String key = "test/my_test.json";
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, key);
        InputStream cosObjectInput = null;
        try {
            COSObject cosObject = cosClient.getObject(getObjectRequest);
            cosObjectInput = cosObject.getObjectContent();
            byte[] bytes = IOUtils.toByteArray(cosObjectInput);
            System.out.println("object size: " + bytes.length);
        } catch (CosServiceException e) {
            e.printStackTrace();
        } catch (CosClientException e) {
            e.printStackTrace();
        } finally {
            if (cosObjectInput != null) {
                cosObjectInput.close();
            }
        }
    }
}
