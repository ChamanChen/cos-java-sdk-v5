package com.qcloud.cos.rapid;

import com.qcloud.cos.AbstractCOSClientTest;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.auth.RapidCOSCredentialProvider;
import com.qcloud.cos.endpoint.UserSpecifiedEndpointBuilder;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.utils.EndpointUtils;
import org.junit.Assume;

import java.io.File;

public class AbstractRapidCOSClientTest extends AbstractCOSClientTest {

    protected static boolean rapidBucketAvailable = false;

    public static void initRapidCosClient() throws Exception {
        if (!initConfig()) {
            return;
        }

        // rapid 桶是预先创建的，桶名不能拼接随机数和 appid，需要用原始环境变量覆盖
        String envBucket = System.getenv("bucket");
        if (envBucket != null && !envBucket.isEmpty()) {
            bucket = envBucket;
        }

        rapidBucketAvailable = EndpointUtils.isRapidBucket(bucket);
        if (!rapidBucketAvailable) {
            return;
        }

        COSCredentials baseCred = new BasicCOSCredentials(secretId, secretKey);
        RapidCOSCredentialProvider rapidProvider = new RapidCOSCredentialProvider(baseCred);
        clientConfig = new ClientConfig(new Region(region));
        String scheme = System.getenv("scheme");
        if ("http".equalsIgnoreCase(scheme)) {
            clientConfig.setHttpProtocol(HttpProtocol.http);
        }
        if (generalApiEndpoint != null && generalApiEndpoint.trim().length() > 0 &&
                serviceApiEndpoint != null && serviceApiEndpoint.trim().length() > 0) {
            clientConfig.setEndpointBuilder(
                    new UserSpecifiedEndpointBuilder(generalApiEndpoint, serviceApiEndpoint));
        }
        cosclient = new COSClient(rapidProvider, clientConfig);

        tmpDir = new File("ut_test_tmp_data");
        if (!tmpDir.exists()) {
            tmpDir.mkdirs();
        }
    }

    public static void destroyRapidCosClient() throws Exception {
        if (cosclient != null) {
            cosclient.shutdown();
        }
        if (tmpDir != null) {
            deleteDir(tmpDir);
        }
    }

    protected static void skipIfNotRapid() {
        Assume.assumeTrue("Skipping: configured bucket is not a rapid bucket", rapidBucketAvailable);
    }
}
