package com.qcloud.cos.rapid;

import com.qcloud.cos.Headers;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.GeneratePresignedUrlRequest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;

import static org.junit.Assert.*;

public class RapidPresignedUrlTest extends AbstractRapidCOSClientTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        AbstractRapidCOSClientTest.initRapidCosClient();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        AbstractRapidCOSClientTest.destroyRapidCosClient();
    }

    @Test
    public void testPresignedUrlPutAndGet() throws IOException {
        skipIfNotRapid();
        if (!judgeUserInfoValid()) {
            return;
        }

        File localFile = buildTestFile(1024L);
        String key = "ut/rapid_presigned_" + localFile.getName();
        try {
            putObjectFromLocalFile(localFile, key);

            Date expiration = new Date(System.currentTimeMillis() + 3 * 60 * 1000);
            GeneratePresignedUrlRequest req =
                    new GeneratePresignedUrlRequest(bucket, key, HttpMethodName.GET);
            req.setExpiration(expiration);
            URL getUrl = cosclient.generatePresignedUrl(req);

            assertNotNull(getUrl);
            String urlStr = getUrl.toString();
            // rapid 桶预签名应包含 security-token（Session 临时密钥）
            assertTrue("URL should contain security token",
                    urlStr.contains(Headers.SECURITY_TOKEN) || urlStr.contains("x-cos-security-token"));
            assertTrue("URL should contain signature", urlStr.contains("q-signature="));

            // 验证 URL 可访问
            HttpURLConnection conn = (HttpURLConnection) getUrl.openConnection();
            conn.setRequestMethod("GET");
            assertEquals(200, conn.getResponseCode());
        } finally {
            clearObject(key);
            if (localFile.exists()) {
                assertTrue(localFile.delete());
            }
        }
    }

    @Test
    public void testPresignedUrlWithMethods() throws IOException {
        skipIfNotRapid();
        if (!judgeUserInfoValid()) {
            return;
        }

        String key = "ut/rapid_presigned_methods_test.txt";
        Date expiration = new Date(System.currentTimeMillis() + 3 * 60 * 1000);

        URL putUrl = cosclient.generatePresignedUrl(bucket, key, expiration,
                HttpMethodName.PUT, new HashMap<>(), new HashMap<>());
        URL getUrl = cosclient.generatePresignedUrl(bucket, key, expiration,
                HttpMethodName.GET, new HashMap<>(), new HashMap<>());
        URL delUrl = cosclient.generatePresignedUrl(bucket, key, expiration,
                HttpMethodName.DELETE, new HashMap<>(), new HashMap<>());

        assertNotNull(putUrl);
        assertNotNull(getUrl);
        assertNotNull(delUrl);
        // 所有 URL 都应包含 security token
        for (URL url : new URL[]{putUrl, getUrl, delUrl}) {
            String urlStr = url.toString();
            assertTrue(urlStr.contains(Headers.SECURITY_TOKEN) || urlStr.contains("x-cos-security-token"));
        }
    }

    @Test
    public void testPresignedUrlExceedMaxDuration() {
        skipIfNotRapid();
        if (!judgeUserInfoValid()) {
            return;
        }

        String key = "ut/rapid_presigned_exceed_duration.txt";
        // 预签名有效期 10 分钟，超过 rapid session 最大时长 5 分钟，必须抛异常
        Date expiration = new Date(System.currentTimeMillis() + 10 * 60 * 1000);
        GeneratePresignedUrlRequest req =
                new GeneratePresignedUrlRequest(bucket, key, HttpMethodName.GET);
        req.setExpiration(expiration);

        try {
            cosclient.generatePresignedUrl(req);
            fail("Should throw CosClientException when expiration exceeds 5 minutes");
        } catch (CosClientException e) {
            assertTrue(e.getMessage().contains("exceeds the maximum session token duration"));
        }
    }
}
