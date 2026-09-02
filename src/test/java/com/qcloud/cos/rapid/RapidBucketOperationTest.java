package com.qcloud.cos.rapid;

import com.qcloud.cos.model.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class RapidBucketOperationTest extends AbstractRapidCOSClientTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // 禁止测试删除bucket
        AbstractRapidCOSClientTest.initRapidCosClient();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        AbstractRapidCOSClientTest.destroyRapidCosClient();
    }

    @Test
    public void testListObjects() throws IOException {
        skipIfNotRapid();
        if (!judgeUserInfoValid()) {
            return;
        }

        String key = "ut/rapid_list_test_" + System.currentTimeMillis();
        try {
            cosclient.putObject(new PutObjectRequest(bucket, key,
                    new ByteArrayInputStream("test".getBytes()), new ObjectMetadata()));

            ObjectListing listing = cosclient.listObjects(bucket, "ut/rapid_list_test_");
            assertNotNull(listing);
            assertTrue(listing.getObjectSummaries().size() > 0);
        } finally {
            clearObject(key);
        }
    }

    @Test
    public void testHeadBucket() {
        skipIfNotRapid();
        if (!judgeUserInfoValid()) {
            return;
        }
        HeadBucketRequest headReq = new HeadBucketRequest(bucket);
        cosclient.headBucket(headReq);
    }

    @Test
    public void testDoesBucketExist() {
        skipIfNotRapid();
        if (!judgeUserInfoValid()) {
            return;
        }
        assertTrue(cosclient.doesBucketExist(bucket));
    }

    @Test
    public void testCreateSession() {
        skipIfNotRapid();
        if (!judgeUserInfoValid()) {
            return;
        }
        CreateSessionRequest req = new CreateSessionRequest(bucket);
        CreateSessionResult result = cosclient.createSession(req);
        assertNotNull(result);
        assertNotNull(result.getCredentials());
        assertNotNull(result.getCredentials().getTmpSecretId());
        assertNotNull(result.getCredentials().getTmpSecretKey());
        assertNotNull(result.getCredentials().getSessionToken());
        assertTrue(result.getExpiredTime() > 0);
    }

    @Test
    public void testBucketPolicy() throws InterruptedException {
        skipIfNotRapid();
        if (!judgeUserInfoValid()) {
            return;
        }

        String policyText = String.format(
                "{\"Statement\":[{\"Principal\":{\"qcs\":[\"qcs::cam::anyone:anyone\"]},"
                + "\"Effect\":\"allow\",\"Action\":[\"cos:GetObject\"],"
                + "\"Resource\":[\"qcs::cos:%s:uid/%s:%s/*\"]}],\"Version\":\"2.0\"}",
                region, appid, bucket);

        try {
            cosclient.setBucketPolicy(bucket, policyText);
            Thread.sleep(5000);

            BucketPolicy policy = cosclient.getBucketPolicy(bucket);
            assertNotNull(policy.getPolicyText());
            assertFalse(policy.getPolicyText().isEmpty());
        } finally {
            try { cosclient.deleteBucketPolicy(bucket); } catch (Exception ignored) {}
        }
    }

    @Test
    public void testListBucketsForRapidBucket() {
        skipIfNotRapid();
        if (!judgeUserInfoValid()) {
            return;
        }
        // 验证 buildUrlAndHost 中 isServiceRequest 分支走 buildGetServiceApiEndpoint(bucket)
        List<Bucket> buckets = cosclient.listBuckets();
        assertNotNull(buckets);
        boolean found = false;
        for (Bucket b : buckets) {
            if (b.getName().equals(bucket)) {
                found = true;
                break;
            }
        }
        assertTrue("ListBuckets should contain the rapid bucket", found);
    }
}
