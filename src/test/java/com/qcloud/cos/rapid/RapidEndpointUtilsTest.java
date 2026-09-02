package com.qcloud.cos.rapid;

import com.qcloud.cos.endpoint.RegionEndpointBuilder;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.utils.EndpointUtils;
import org.junit.Test;

import static org.junit.Assert.*;

public class RapidEndpointUtilsTest {

    // --- EndpointUtils.isRapidBucket ---

    @Test
    public void testIsRapidBucketTrue() {
        assertTrue(EndpointUtils.isRapidBucket("mybucket-x--1250000000"));
        assertTrue(EndpointUtils.isRapidBucket("a-x--1"));
        assertTrue(EndpointUtils.isRapidBucket("test-bucket-x--1253960454"));
    }

    @Test
    public void testIsRapidBucketFalse() {
        assertFalse(EndpointUtils.isRapidBucket("mybucket-1250000000"));
        assertFalse(EndpointUtils.isRapidBucket("mybucket-x-1250000000"));
        assertFalse(EndpointUtils.isRapidBucket("mybucket-x---1250000000"));
    }

    @Test
    public void testIsRapidBucketNull() {
        assertFalse(EndpointUtils.isRapidBucket(null));
    }

    @Test
    public void testIsRapidBucketEmpty() {
        assertFalse(EndpointUtils.isRapidBucket(""));
    }

    @Test
    public void testIsRapidBucketUpperCase() {
        assertFalse(EndpointUtils.isRapidBucket("MYBUCKET-x--1250000000"));
    }

    // --- RegionEndpointBuilder.buildGeneralApiEndpoint for rapid bucket ---

    @Test
    public void testBuildGeneralApiEndpointRapidBucket() {
        RegionEndpointBuilder builder = new RegionEndpointBuilder(new Region("ap-nanjing"));
        String endpoint = builder.buildGeneralApiEndpoint("mybucket-x--1250000000");
        assertEquals("mybucket-x--1250000000.cosrapid.ap-nanjing.myqcloud.com", endpoint);
    }

    @Test
    public void testBuildGeneralApiEndpointNormalBucket() {
        RegionEndpointBuilder builder = new RegionEndpointBuilder(new Region("ap-nanjing"));
        String endpoint = builder.buildGeneralApiEndpoint("mybucket-1250000000");
        assertEquals("mybucket-1250000000.cos.ap-nanjing.myqcloud.com", endpoint);
    }

    // --- RegionEndpointBuilder.buildGetServiceApiEndpoint(bucket) ---

    @Test
    public void testBuildGetServiceApiEndpointRapidBucket() {
        RegionEndpointBuilder builder = new RegionEndpointBuilder(new Region("ap-nanjing"));
        String endpoint = builder.buildGetServiceApiEndpoint("mybucket-x--1250000000");
        assertEquals("service.cosrapid.ap-nanjing.myqcloud.com", endpoint);
    }

    @Test
    public void testBuildGetServiceApiEndpointNormalBucket() {
        RegionEndpointBuilder builder = new RegionEndpointBuilder(new Region("ap-nanjing"));
        String endpoint = builder.buildGetServiceApiEndpoint("mybucket-1250000000");
        assertEquals("service.cos.myqcloud.com", endpoint);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildGetServiceApiEndpointNullRegion() {
        RegionEndpointBuilder builder = new RegionEndpointBuilder(null);
        builder.buildGetServiceApiEndpoint("mybucket-x--1250000000");
    }

    // --- EndpointUtils.extractEndpointSuffix ---

    @Test
    public void testExtractEndpointSuffix() {
        assertEquals("cosrapid.ap-nanjing.myqcloud.com",
                EndpointUtils.extractEndpointSuffix("mybucket-x--125.cosrapid.ap-nanjing.myqcloud.com"));
    }

    @Test
    public void testExtractEndpointSuffixNormal() {
        assertEquals("cos.ap-nanjing.myqcloud.com",
                EndpointUtils.extractEndpointSuffix("mybucket-125.cos.ap-nanjing.myqcloud.com"));
    }

    @Test
    public void testExtractEndpointSuffixNull() {
        assertNull(EndpointUtils.extractEndpointSuffix(null));
    }

    @Test
    public void testExtractEndpointSuffixNoDot() {
        assertNull(EndpointUtils.extractEndpointSuffix("nodothere"));
    }

    @Test
    public void testExtractEndpointSuffixTrailingDot() {
        assertNull(EndpointUtils.extractEndpointSuffix("bucket."));
    }
}
