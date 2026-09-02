package com.qcloud.cos.rapid;

import com.qcloud.cos.endpoint.RegionEndpointBuilder;
import com.qcloud.cos.model.*;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.utils.Md5Utils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class RapidCopyObjectTest extends AbstractRapidCOSClientTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        AbstractRapidCOSClientTest.initRapidCosClient();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        AbstractRapidCOSClientTest.destroyRapidCosClient();
    }

    @Test
    public void testCopySameRegion() throws IOException {
        skipIfNotRapid();
        if (!judgeUserInfoValid()) {
            return;
        }

        long fileSize = 1024 * 1024L;
        File localFile = buildTestFile(fileSize);
        String srcKey = "ut/rapid_copy_src.txt";
        String destKey = "ut/rapid_copy_dest.txt";
        try {
            putObjectFromLocalFile(localFile, srcKey);
            CopyObjectRequest copyReq = new CopyObjectRequest(bucket, srcKey, bucket, destKey);
            copyReq.setSourceBucketRegion(new Region(region));
            copyReq.setSourceEndpointBuilder(new RegionEndpointBuilder(new Region(region)));
            copyReq.setSourceAppid(appid);
            CopyObjectResult copyResult = cosclient.copyObject(copyReq);
            assertNotNull(copyResult.getRequestId());
            headSimpleObject(destKey, fileSize, Md5Utils.md5Hex(localFile));
        } finally {
            clearObject(srcKey);
            clearObject(destKey);
            if (localFile.exists()) {
                assertTrue(localFile.delete());
            }
        }
    }

    @Test
    public void testCopyPart() throws IOException {
        skipIfNotRapid();
        if (!judgeUserInfoValid()) {
            return;
        }

        long fileSize = 10 * 1024 * 1024L;
        File localFile = buildTestFile(fileSize);
        String srcKey = "ut/rapid_copypart_src.txt";
        String destKey = "ut/rapid_copypart_dest.txt";
        try {
            putObjectFromLocalFile(localFile, srcKey);

            InitiateMultipartUploadRequest initReq = new InitiateMultipartUploadRequest(bucket, destKey);
            String uploadId = cosclient.initiateMultipartUpload(initReq).getUploadId();

            CopyPartRequest copyPartReq = new CopyPartRequest();
            copyPartReq.setSourceBucketRegion(new Region(region));
            copyPartReq.setSourceBucketName(bucket);
            copyPartReq.setSourceKey(srcKey);
            copyPartReq.setSourceAppid(appid);
            copyPartReq.setFirstByte(0L);
            copyPartReq.setLastByte(fileSize - 1);
            copyPartReq.setDestinationBucketName(bucket);
            copyPartReq.setDestinationKey(destKey);
            copyPartReq.setPartNumber(1);
            copyPartReq.setUploadId(uploadId);

            CopyPartResult copyPartResult = cosclient.copyPart(copyPartReq);
            assertNotNull(copyPartResult.getETag());

            List<PartETag> partETags = new ArrayList<>();
            partETags.add(copyPartResult.getPartETag());
            CompleteMultipartUploadRequest completeReq =
                    new CompleteMultipartUploadRequest(bucket, destKey, uploadId, partETags);
            cosclient.completeMultipartUpload(completeReq);

            headMultiPartObject(destKey, fileSize, 1);
        } finally {
            clearObject(srcKey);
            clearObject(destKey);
            if (localFile.exists()) {
                assertTrue(localFile.delete());
            }
        }
    }
}
