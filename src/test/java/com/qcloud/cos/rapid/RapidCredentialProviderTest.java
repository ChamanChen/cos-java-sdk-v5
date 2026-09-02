package com.qcloud.cos.rapid;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.*;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.region.Region;
import org.junit.Test;

import static org.junit.Assert.*;

public class RapidCredentialProviderTest {

    // --- RapidCOSCredentialProvider 构造 ---

    @Test(expected = CosClientException.class)
    public void testConstructWithNullProvider() {
        COSCredentialsProvider nullProvider = null;
        new RapidCOSCredentialProvider(nullProvider);
    }

    @Test(expected = CosClientException.class)
    public void testConstructWithNestedRapidProvider() {
        COSCredentials cred = new BasicCOSCredentials("testAK", "testSK");
        RapidCOSCredentialProvider inner = new RapidCOSCredentialProvider(cred);
        new RapidCOSCredentialProvider(inner);
    }

    @Test
    public void testConstructWithStaticCredentials() {
        COSCredentials cred = new BasicCOSCredentials("testAK", "testSK");
        RapidCOSCredentialProvider provider = new RapidCOSCredentialProvider(cred);
        assertNotNull(provider.getBaseCredProvider());
    }

    // --- getCredentials 路由 ---

    @Test
    public void testGetCredentialsNonRapidReturnsBase() {
        COSCredentials cred = new BasicCOSCredentials("testAK", "testSK");
        RapidCOSCredentialProvider provider = new RapidCOSCredentialProvider(cred);
        // useRapidSession=false 应返回基础凭证
        COSCredentials result = provider.getCredentials("endpoint", "https", false, "bucket-125");
        assertEquals("testAK", result.getCOSAccessKeyId());
        assertEquals("testSK", result.getCOSSecretKey());
    }

    @Test
    public void testGetCredentialsBaseMethod() {
        COSCredentials cred = new BasicCOSCredentials("testAK", "testSK");
        RapidCOSCredentialProvider provider = new RapidCOSCredentialProvider(cred);
        COSCredentials result = provider.getCredentials();
        assertEquals("testAK", result.getCOSAccessKeyId());
    }

    @Test(expected = CosClientException.class)
    public void testGetCredentialsRapidWithNullBucket() {
        COSCredentials cred = new BasicCOSCredentials("testAK", "testSK");
        RapidCOSCredentialProvider provider = new RapidCOSCredentialProvider(cred);
        provider.getCredentials("endpoint", "https", true, null);
    }

    // --- RapidSessionCredentials ---

    @Test
    public void testIsAboutToExpireTrue() {
        long nowSec = System.currentTimeMillis() / 1000L;
        RapidSessionCredentials cred = new RapidSessionCredentials(
                "ak", "sk", "token", nowSec + 30, "baseAK");
        assertTrue(cred.isAboutToExpire(60));
    }

    @Test
    public void testIsAboutToExpireFalse() {
        long nowSec = System.currentTimeMillis() / 1000L;
        RapidSessionCredentials cred = new RapidSessionCredentials(
                "ak", "sk", "token", nowSec + 300, "baseAK");
        assertFalse(cred.isAboutToExpire(60));
    }

    @Test
    public void testIsBaseAKIDChanged() {
        RapidSessionCredentials cred = new RapidSessionCredentials(
                "ak", "sk", "token", 999999999L, "originalAK");
        assertFalse(cred.isBaseAKIDChanged("originalAK"));
        assertTrue(cred.isBaseAKIDChanged("differentAK"));
    }

    // --- COSClient 构造不自动包裹 + resolveCredentials 校验 ---

    @Test
    public void testCOSClientWithNonRapidProviderOnRapidBucket() {
        COSCredentials cred = new BasicCOSCredentials("testAK", "testSK");
        ClientConfig config = new ClientConfig(new Region("ap-nanjing"));
        COSClient client = new COSClient(cred, config);
        try {
            // 对 rapid bucket 发起请求应抛异常（credProvider 不是 RapidCOSCredentialProvider）
            client.doesObjectExist("mybucket-x--1250000000", "test.txt");
            fail("Expected CosClientException for non-RapidCOSCredentialProvider on rapid bucket");
        } catch (CosClientException e) {
            assertTrue(e.getMessage().contains("RapidCOSCredentialProvider"));
        } finally {
            client.shutdown();
        }
    }

    @Test
    public void testCOSClientWithRapidProviderOnNormalBucket() {
        COSCredentials cred = new BasicCOSCredentials("testAK", "testSK");
        RapidCOSCredentialProvider rapidProvider = new RapidCOSCredentialProvider(cred);
        ClientConfig config = new ClientConfig(new Region("ap-nanjing"));
        COSClient client = new COSClient(rapidProvider, config);
        // 对常规桶不应抛异常（回退到基础凭证），但会因为网络不通而失败
        // 这里只验证不会抛 RapidCOSCredentialProvider 相关异常
        try {
            client.doesObjectExist("mybucket-1250000000", "test.txt");
        } catch (CosClientException e) {
            assertFalse("Should not be a RapidCOSCredentialProvider error",
                    e.getMessage().contains("RapidCOSCredentialProvider"));
        } finally {
            client.shutdown();
        }
    }
}
