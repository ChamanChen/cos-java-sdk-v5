package com.qcloud.cos.demo.rapid;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.auth.RapidCOSCredentialProvider;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.CopyObjectRequest;
import com.qcloud.cos.model.CopyObjectResult;
import com.qcloud.cos.region.Region;

/**
 * 高性能桶复制对象示例
 */
public class RapidCopyObjectDemo {
    private static String secretId = "AKIDXXXXXXXX";
    private static String secretKey = "1A2Z3YYYYYYYYYY";
    private static String srcBucketName = "srcbucket-x--12500000000";
    private static String destBucketName = "destbucket-x--12500000000";
    private static String regionName = "ap-guangzhou";
    private static COSClient cosClient = createClient();

    public static void main(String[] args) {
        try {
            copyObjectDemo();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cosClient.shutdown();
        }
    }

    private static COSClient createClient() {
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        RapidCOSCredentialProvider credProvider = new RapidCOSCredentialProvider(cred);
        ClientConfig clientConfig = new ClientConfig(new Region(regionName));
        return new COSClient(credProvider, clientConfig);
    }

    // copyObject 最大支持 5G 文件的 copy
    private static void copyObjectDemo() {
        String srcKey = "aaa/bbb.txt";
        String destKey = "ccc/ddd.txt";
        Region srcBucketRegion = new Region(regionName);
        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(
                srcBucketRegion, srcBucketName, srcKey, destBucketName, destKey);
        try {
            CopyObjectResult result = cosClient.copyObject(copyObjectRequest);
            System.out.println("copy succeed, crc64: " + result.getCrc64Ecma());
        } catch (CosServiceException e) {
            e.printStackTrace();
        } catch (CosClientException e) {
            e.printStackTrace();
        }
    }
}
