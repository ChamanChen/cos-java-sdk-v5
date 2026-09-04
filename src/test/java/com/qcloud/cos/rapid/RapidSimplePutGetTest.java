package com.qcloud.cos.rapid;

import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.utils.Md5Utils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class RapidSimplePutGetTest extends AbstractRapidCOSClientTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        AbstractRapidCOSClientTest.initRapidCosClient();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        AbstractRapidCOSClientTest.destroyRapidCosClient();
    }

    @Test
    public void testSimplePutAndGet() throws IOException {
        skipIfNotRapid();
        if (!judgeUserInfoValid()) {
            return;
        }
        File localFile = buildTestFile(1024 * 1024L);
        String key = "ut/rapid_simple_put_get_" + localFile.getName();
        File downFile = new File(localFile.getAbsolutePath() + ".down");
        try {
            // put
            PutObjectResult putResult = cosclient.putObject(new PutObjectRequest(bucket, key, localFile));
            String expectedMd5 = Md5Utils.md5Hex(localFile);
            assertEquals(expectedMd5, putResult.getETag());
            assertNotNull(putResult.getRequestId());
            assertNotNull(putResult.getDateStr());

            // get
            ObjectMetadata meta = cosclient.getObject(new GetObjectRequest(bucket, key), downFile);
            assertEquals(1024 * 1024L, downFile.length());
            assertEquals(expectedMd5, Md5Utils.md5Hex(downFile));
        } finally {
            cosclient.deleteObject(bucket, key);
            if (localFile.exists()) assertTrue(localFile.delete());
            if (downFile.exists()) assertTrue(downFile.delete());
        }
    }
}
