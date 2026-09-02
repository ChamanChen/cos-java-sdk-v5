package com.qcloud.cos.demo.rapid;

import java.util.ArrayList;
import java.util.List;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.auth.RapidCOSCredentialProvider;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.exception.MultiObjectDeleteException;
import com.qcloud.cos.model.DeleteObjectsRequest;
import com.qcloud.cos.model.DeleteObjectsRequest.KeyVersion;
import com.qcloud.cos.model.DeleteObjectsResult;
import com.qcloud.cos.model.DeleteObjectsResult.DeletedObject;
import com.qcloud.cos.region.Region;

/**
 * 高性能桶删除对象示例
 */
public class RapidDeleteObjectDemo {
    private static String secretId = "AKIDXXXXXXXX";
    private static String secretKey = "1A2Z3YYYYYYYYYY";
    private static String bucketName = "examplebucket-x--12500000000";
    private static String region = "ap-guangzhou";
    private static COSClient cosClient = createClient();

    public static void main(String[] args) {
        try {
            deleteObjectDemo();
            // batchDeleteDemo();
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

    private static void deleteObjectDemo() {
        String key = "aaa/bbb.txt";
        try {
            cosClient.deleteObject(bucketName, key);
            System.out.println("delete object succeed");
        } catch (CosServiceException e) {
            e.printStackTrace();
        } catch (CosClientException e) {
            e.printStackTrace();
        }
    }

    private static void batchDeleteDemo() {
        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucketName);
        ArrayList<KeyVersion> keyList = new ArrayList<>();
        keyList.add(new KeyVersion("aaa.txt"));
        keyList.add(new KeyVersion("bbb.txt"));
        keyList.add(new KeyVersion("ccc/ddd.txt"));
        deleteObjectsRequest.setKeys(keyList);

        try {
            DeleteObjectsResult result = cosClient.deleteObjects(deleteObjectsRequest);
            List<DeletedObject> deletedObjects = result.getDeletedObjects();
            for (DeletedObject obj : deletedObjects) {
                System.out.println("deleted: " + obj.getKey());
            }
        } catch (MultiObjectDeleteException e) {
            e.printStackTrace();
        } catch (CosServiceException e) {
            e.printStackTrace();
        } catch (CosClientException e) {
            e.printStackTrace();
        }
    }
}
