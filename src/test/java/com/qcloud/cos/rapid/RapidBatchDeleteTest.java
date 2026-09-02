package com.qcloud.cos.rapid;

import com.qcloud.cos.model.DeleteObjectsRequest;
import com.qcloud.cos.model.DeleteObjectsRequest.KeyVersion;
import com.qcloud.cos.model.DeleteObjectsResult;
import com.qcloud.cos.model.PutObjectResult;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class RapidBatchDeleteTest extends AbstractRapidCOSClientTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        AbstractRapidCOSClientTest.initRapidCosClient();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        AbstractRapidCOSClientTest.destroyRapidCosClient();
    }

    @Test
    public void testBatchDeleteObjects() throws IOException {
        skipIfNotRapid();
        if (!judgeUserInfoValid()) {
            return;
        }

        int deleteFileCount = 5;
        ArrayList<KeyVersion> keyList = new ArrayList<>();
        for (int i = 0; i < deleteFileCount; i++) {
            File localFile = buildTestFile(i * 1024L);
            String key = "ut/rapid_batch_del_" + localFile.getName();
            PutObjectResult putResult = putObjectFromLocalFile(localFile, key);
            keyList.add(new KeyVersion(key));
            if (localFile.exists()) {
                localFile.delete();
            }
        }

        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucket);
        deleteObjectsRequest.setKeys(keyList);
        DeleteObjectsResult result = cosclient.deleteObjects(deleteObjectsRequest);
        assertEquals(deleteFileCount, result.getDeletedObjects().size());
    }
}
