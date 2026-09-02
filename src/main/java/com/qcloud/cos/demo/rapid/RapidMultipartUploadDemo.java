package com.qcloud.cos.demo.rapid;

import java.io.ByteArrayInputStream;
import java.util.LinkedList;
import java.util.List;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.auth.RapidCOSCredentialProvider;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.AbortMultipartUploadRequest;
import com.qcloud.cos.model.CompleteMultipartUploadRequest;
import com.qcloud.cos.model.CompleteMultipartUploadResult;
import com.qcloud.cos.model.InitiateMultipartUploadRequest;
import com.qcloud.cos.model.InitiateMultipartUploadResult;
import com.qcloud.cos.model.ListPartsRequest;
import com.qcloud.cos.model.PartETag;
import com.qcloud.cos.model.PartListing;
import com.qcloud.cos.model.PartSummary;
import com.qcloud.cos.model.UploadPartRequest;
import com.qcloud.cos.model.UploadPartResult;
import com.qcloud.cos.region.Region;

/**
 * 高性能桶分块上传示例
 */
public class RapidMultipartUploadDemo {
    private static String secretId = "AKIDXXXXXXXX";
    private static String secretKey = "1A2Z3YYYYYYYYYY";
    private static String bucketName = "examplebucket-x--12500000000";
    private static String region = "ap-guangzhou";
    private static String key = "aaa/bbb.txt";
    private static COSClient cosClient = createClient();

    public static void main(String[] args) {
        try {
            multipartUploadDemo();
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

    private static void multipartUploadDemo() {
        String uploadId = initMultipartUpload();
        List<PartETag> partETags = uploadParts(uploadId);
        completeMultipartUpload(uploadId, partETags);
    }

    private static String initMultipartUpload() {
        InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, key);
        try {
            InitiateMultipartUploadResult result = cosClient.initiateMultipartUpload(request);
            String uploadId = result.getUploadId();
            System.out.println("init multipart upload, uploadId: " + uploadId);
            return uploadId;
        } catch (CosServiceException e) {
            throw e;
        } catch (CosClientException e) {
            throw e;
        }
    }

    private static List<PartETag> uploadParts(String uploadId) {
        List<PartETag> partETags = new LinkedList<>();
        // 上传 10 个 1MB 的分块
        for (int i = 0; i < 10; i++) {
            byte[] data = new byte[1024 * 1024];
            UploadPartRequest uploadPartRequest = new UploadPartRequest();
            uploadPartRequest.setBucketName(bucketName);
            uploadPartRequest.setKey(key);
            uploadPartRequest.setUploadId(uploadId);
            uploadPartRequest.setInputStream(new ByteArrayInputStream(data));
            uploadPartRequest.setPartSize(data.length);
            uploadPartRequest.setPartNumber(i + 1);

            try {
                UploadPartResult result = cosClient.uploadPart(uploadPartRequest);
                partETags.add(result.getPartETag());
                System.out.println("upload part " + (i + 1) + " succeed");
            } catch (CosServiceException e) {
                throw e;
            } catch (CosClientException e) {
                throw e;
            }
        }
        return partETags;
    }

    private static void listParts(String uploadId) {
        ListPartsRequest listPartsRequest = new ListPartsRequest(bucketName, key, uploadId);
        PartListing partListing;
        do {
            partListing = cosClient.listParts(listPartsRequest);
            for (PartSummary part : partListing.getParts()) {
                System.out.println("partNumber: " + part.getPartNumber() + ", etag: " + part.getETag());
            }
            listPartsRequest.setPartNumberMarker(partListing.getNextPartNumberMarker());
        } while (partListing.isTruncated());
    }

    private static void completeMultipartUpload(String uploadId, List<PartETag> partETags) {
        CompleteMultipartUploadRequest request =
                new CompleteMultipartUploadRequest(bucketName, key, uploadId, partETags);
        try {
            CompleteMultipartUploadResult result = cosClient.completeMultipartUpload(request);
            System.out.println("complete multipart upload succeed");
        } catch (CosServiceException e) {
            throw e;
        } catch (CosClientException e) {
            throw e;
        }
    }

    private static void abortMultipartUpload(String uploadId) {
        AbortMultipartUploadRequest request = new AbortMultipartUploadRequest(bucketName, key, uploadId);
        try {
            cosClient.abortMultipartUpload(request);
            System.out.println("abort multipart upload succeed, uploadId: " + uploadId);
        } catch (CosServiceException e) {
            e.printStackTrace();
        } catch (CosClientException e) {
            e.printStackTrace();
        }
    }
}
