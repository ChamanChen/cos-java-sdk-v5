package com.qcloud.cos.utils;

import java.util.regex.Pattern;

public class EndpointUtils {

    private EndpointUtils() {
        // 工具类，禁止实例化
    }

    private static final Pattern RAPID_BUCKET_PATTERN =
            Pattern.compile("^[a-z0-9][a-z0-9-]*-x--[0-9]+$");

    /**
     * 高性能桶桶名正则：^[a-z0-9][a-z0-9-]*-x--[0-9]+$
     * 例：bucket-x--1253960454
     */
    public static boolean isRapidBucket(String bucketName) {
        return bucketName != null && RAPID_BUCKET_PATTERN.matcher(bucketName).matches();
    }

    /** 提取 endpoint 中桶名后的 suffix。如 bucket.cosrapid.ap-nanjing.myqcloud.com → cosrapid.ap-nanjing.myqcloud.com */
    public static String extractEndpointSuffix(String endpoint) {
        if (endpoint == null) {
            return null;
        }
        int dotIndex = endpoint.indexOf('.');
        if (dotIndex < 0 || dotIndex == endpoint.length() - 1) {
            return null;
        }
        return endpoint.substring(dotIndex + 1);
    }

    /**
     * 从 suffix 中提取 region。如 cosrapid.ap-guangzhou.myqcloud.com → ap-guangzhou
     * 格式：{service}.{region}.myqcloud.com，取第一个点和第二个点之间的部分。
     */
    public static String extractRegionFromSuffix(String suffix) {
        if (suffix == null) {
            return null;
        }
        int firstDot = suffix.indexOf('.');
        if (firstDot < 0 || firstDot == suffix.length() - 1) {
            return null;
        }
        int secondDot = suffix.indexOf('.', firstDot + 1);
        if (secondDot < 0) {
            return null;
        }
        return suffix.substring(firstDot + 1, secondDot);
    }

}
