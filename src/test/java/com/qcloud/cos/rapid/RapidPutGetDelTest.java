package com.qcloud.cos.rapid;

import com.qcloud.cos.exception.CosServiceException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class RapidPutGetDelTest extends AbstractRapidCOSClientTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        AbstractRapidCOSClientTest.initRapidCosClient();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        AbstractRapidCOSClientTest.destroyRapidCosClient();
    }

    @Test
    public void testPutGetDelObjectEmpty() throws CosServiceException, IOException {
        skipIfNotRapid();
        testPutGetDelObjectDiffSize(0L);
    }

    @Test
    public void testPutGetDelObject256k() throws CosServiceException, IOException {
        skipIfNotRapid();
        testPutGetDelObjectDiffSize(256 * 1024L);
    }

    @Test
    public void testPutGetDelObject1M() throws CosServiceException, IOException {
        skipIfNotRapid();
        testPutGetDelObjectDiffSize(1024 * 1024L);
    }

    @Test
    public void testPutGetDelObject4M() throws CosServiceException, IOException {
        skipIfNotRapid();
        testPutGetDelObjectDiffSize(4 * 1024 * 1024L);
    }

    @Test
    public void testDoesObjectExist() throws IOException {
        skipIfNotRapid();
        if (!judgeUserInfoValid()) {
            return;
        }
        File localFile = buildTestFile(1024L);
        String key = "ut/rapid_exist_test_" + localFile.getName();
        try {
            putObjectFromLocalFile(localFile, key);
            assertTrue(cosclient.doesObjectExist(bucket, key));
        } finally {
            clearObject(key);
            if (localFile.exists()) {
                assertTrue(localFile.delete());
            }
        }
    }
}
