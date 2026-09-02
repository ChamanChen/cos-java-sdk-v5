package com.qcloud.cos.rapid;

import com.qcloud.cos.model.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class RapidMultipartUploadTest extends AbstractRapidCOSClientTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        AbstractRapidCOSClientTest.initRapidCosClient();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        AbstractRapidCOSClientTest.destroyRapidCosClient();
    }

    @Test
    public void testMultipartUpload4MPart1M() throws IOException {
        skipIfNotRapid();
        testMultiPartUploadObject(4 * 1024 * 1024L, 1 * 1024 * 1024L);
    }

    @Test
    public void testListMultipartUploads() {
        skipIfNotRapid();
        if (!judgeUserInfoValid()) {
            return;
        }

        String key = "ut/rapid_list_multipart.txt";
        InitiateMultipartUploadRequest initReq = new InitiateMultipartUploadRequest(bucket, key);
        String uploadId = testInitMultipart(initReq);
        assertNotNull(uploadId);

        try {
            ListMultipartUploadsRequest listReq = new ListMultipartUploadsRequest(bucket);
            listReq.setPrefix("ut/rapid_list_multipart");
            MultipartUploadListing listing = cosclient.listMultipartUploads(listReq);
            List<MultipartUpload> uploads = listing.getMultipartUploads();
            boolean found = false;
            for (MultipartUpload upload : uploads) {
                if (upload.getUploadId().equals(uploadId)) {
                    found = true;
                    break;
                }
            }
            assertTrue("Should find the initiated upload", found);
        } finally {
            cosclient.abortMultipartUpload(new AbortMultipartUploadRequest(bucket, key, uploadId));
        }
    }

    @Test
    public void testAbortMultipartUpload() {
        skipIfNotRapid();
        if (!judgeUserInfoValid()) {
            return;
        }

        String key = "ut/rapid_abort_multipart.txt";
        InitiateMultipartUploadRequest initReq = new InitiateMultipartUploadRequest(bucket, key);
        String uploadId = testInitMultipart(initReq);
        cosclient.abortMultipartUpload(new AbortMultipartUploadRequest(bucket, key, uploadId));
    }
}
