package com.qcloud.cos.demo.rapid;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.auth.RapidCOSCredentialProvider;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.RenameRequest;
import com.qcloud.cos.region.Region;

/**
 * 高性能桶原子重命名对象示例（高性能桶专用接口）
 */
public class RapidRenameObjectDemo {
    private static String secretId = "AKIDXXXXXXXX";
    private static String secretKey = "1A2Z3YYYYYYYYYY";
    private static String bucketName = "examplebucket-x--12500000000";
    private static String region = "ap-guangzhou";
    private static COSClient cosClient = createClient();

    public static void main(String[] args) {
        try {
            renameObjectDemo();
            // renameObjectForbidOverwriteDemo();
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

    private static void renameObjectDemo() {
        String srcKey = "aaa/old_name.txt";
        String dstKey = "aaa/new_name.txt";
        RenameRequest renameRequest = new RenameRequest(bucketName, srcKey, dstKey);
        try {
            cosClient.rename(renameRequest);
            System.out.println("rename succeed");
        } catch (CosServiceException e) {
            e.printStackTrace();
        } catch (CosClientException e) {
            e.printStackTrace();
        }
    }

    // 设置 forbidOverwrite=true，目标已存在同名文件时返回 409
    private static void renameObjectForbidOverwriteDemo() {
        String srcKey = "aaa/old_name.txt";
        String dstKey = "aaa/new_name.txt";
        RenameRequest renameRequest = new RenameRequest(bucketName, srcKey, dstKey);
        renameRequest.setForbidOverwrite(true);
        try {
            cosClient.rename(renameRequest);
            System.out.println("rename succeed");
        } catch (CosServiceException e) {
            // 目标已存在时返回 409 FileAlreadyExists
            e.printStackTrace();
        } catch (CosClientException e) {
            e.printStackTrace();
        }
    }
}
