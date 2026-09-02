package com.qcloud.cos.demo.rapid;

import java.util.List;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.auth.RapidCOSCredentialProvider;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.Bucket;
import com.qcloud.cos.model.BucketPolicy;
import com.qcloud.cos.model.COSObjectSummary;
import com.qcloud.cos.model.ListObjectsRequest;
import com.qcloud.cos.model.ObjectListing;
import com.qcloud.cos.region.Region;

/**
 * 高性能桶桶级操作示例（列举对象、查询桶、桶策略）
 */
public class RapidBucketOperationDemo {
    private static String secretId = "AKIDXXXXXXXX";
    private static String secretKey = "1A2Z3YYYYYYYYYY";
    private static String bucketName = "examplebucket-x--12500000000";
    private static String region = "ap-guangzhou";
    private static COSClient cosClient = createClient();

    public static void main(String[] args) {
        try {
            listObjectsDemo();
            listBucketsDemo();
            doesBucketExistDemo();
            // setBucketPolicyDemo();
            // getBucketPolicyDemo();
            // deleteBucketPolicyDemo();
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

    // listBuckets 走 service.cosrapid.<region>.myqcloud.com 域名
    private static void listBucketsDemo() {
        try {
            List<Bucket> buckets = cosClient.listBuckets();
            for (Bucket bucket : buckets) {
                System.out.println("bucket: " + bucket.getName() + ", region: " + bucket.getLocation());
            }
        } catch (CosServiceException e) {
            e.printStackTrace();
        } catch (CosClientException e) {
            e.printStackTrace();
        }
    }

    private static void listObjectsDemo() {
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
        listObjectsRequest.setBucketName(bucketName);
        listObjectsRequest.setPrefix("");
        listObjectsRequest.setMaxKeys(1000);

        try {
            ObjectListing objectListing = cosClient.listObjects(listObjectsRequest);
            List<COSObjectSummary> summaries = objectListing.getObjectSummaries();
            for (COSObjectSummary summary : summaries) {
                System.out.println("key: " + summary.getKey() + ", size: " + summary.getSize());
            }
        } catch (CosServiceException e) {
            e.printStackTrace();
        } catch (CosClientException e) {
            e.printStackTrace();
        }
    }

    private static void doesBucketExistDemo() {
        boolean isExist = cosClient.doesBucketExist(bucketName);
        System.out.println("bucket exist: " + isExist);
    }

    private static void setBucketPolicyDemo() {
        String bucketPolicyStr = "{" +
                "  \"Statement\": [" +
                "    {" +
                "      \"Principal\": {" +
                "        \"qcs\": [" +
                "          \"qcs::cam::uin/100000000001:uin/100000000011\"" +
                "        ]" +
                "      }," +
                "      \"Effect\": \"deny\"," +
                "      \"Action\": [" +
                "        \"name/cos:GetObject\"" +
                "      ]," +
                "      \"Resource\": [" +
                "        \"qcs::cos:ap-guangzhou:uid/12500000000:examplebucket-x--12500000000/test.txt\"" +
                "      ]" +
                "    }" +
                "  ]," +
                "  \"version\": \"2.0\"" +
                "}";
        cosClient.setBucketPolicy(bucketName, bucketPolicyStr);
        System.out.println("set bucket policy succeed");
    }

    private static void getBucketPolicyDemo() {
        BucketPolicy bucketPolicy = cosClient.getBucketPolicy(bucketName);
        System.out.println(bucketPolicy.getPolicyText());
    }

    private static void deleteBucketPolicyDemo() {
        cosClient.deleteBucketPolicy(bucketName);
        System.out.println("delete bucket policy succeed");
    }
}
