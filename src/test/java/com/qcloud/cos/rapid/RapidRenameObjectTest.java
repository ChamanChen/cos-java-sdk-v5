package com.qcloud.cos.rapid;

import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.RenameRequest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.*;

public class RapidRenameObjectTest extends AbstractRapidCOSClientTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        AbstractRapidCOSClientTest.initRapidCosClient();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        AbstractRapidCOSClientTest.destroyRapidCosClient();
    }

    @Test
    public void testRenameObject() {
        skipIfNotRapid();
        if (!judgeUserInfoValid()) {
            return;
        }

        String srcKey = "ut/rapid_rename_src_" + System.currentTimeMillis();
        String dstKey = "ut/rapid_rename_dst_" + System.currentTimeMillis();
        byte[] content = "hello rapid cos".getBytes();

        try {
            PutObjectRequest putReq = new PutObjectRequest(bucket, srcKey,
                    new ByteArrayInputStream(content), new ObjectMetadata());
            cosclient.putObject(putReq);

            RenameRequest renameReq = new RenameRequest(bucket, srcKey, dstKey);
            cosclient.rename(renameReq);

            assertTrue(cosclient.doesObjectExist(bucket, dstKey));
            assertFalse(cosclient.doesObjectExist(bucket, srcKey));
        } finally {
            try { cosclient.deleteObject(bucket, srcKey); } catch (Exception ignored) {}
            try { cosclient.deleteObject(bucket, dstKey); } catch (Exception ignored) {}
        }
    }

    @Test
    public void testRenameObjectWithForbidOverwrite() {
        skipIfNotRapid();
        if (!judgeUserInfoValid()) {
            return;
        }

        String srcKey = "ut/rapid_rename_forbid_src_" + System.currentTimeMillis();
        String dstKey = "ut/rapid_rename_forbid_dst_" + System.currentTimeMillis();
        byte[] content = "hello rapid cos".getBytes();

        try {
            cosclient.putObject(new PutObjectRequest(bucket, srcKey,
                    new ByteArrayInputStream(content), new ObjectMetadata()));
            cosclient.putObject(new PutObjectRequest(bucket, dstKey,
                    new ByteArrayInputStream(content), new ObjectMetadata()));

            RenameRequest renameReq = new RenameRequest(bucket, srcKey, dstKey);
            renameReq.setForbidOverwrite(true);
            try {
                cosclient.rename(renameReq);
                fail("Expected 409 FileAlreadyExists");
            } catch (com.qcloud.cos.exception.CosServiceException e) {
                assertEquals(409, e.getStatusCode());
            }
        } finally {
            try { cosclient.deleteObject(bucket, srcKey); } catch (Exception ignored) {}
            try { cosclient.deleteObject(bucket, dstKey); } catch (Exception ignored) {}
        }
    }
}
