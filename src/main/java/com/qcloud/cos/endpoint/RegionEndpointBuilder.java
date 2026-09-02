/*
 * Copyright 2010-2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.

 * According to cos feature, we modify some class，comment, field name, etc.
 */


package com.qcloud.cos.endpoint;

import com.qcloud.cos.internal.BucketNameUtils;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.utils.EndpointUtils;

public class RegionEndpointBuilder implements EndpointBuilder {
    private Region region;

    public RegionEndpointBuilder(Region region) {
        super();
        this.region = region;
    }

    @Override
    public String buildGeneralApiEndpoint(String bucketName) {
        if (this.region == null) {
            throw new IllegalArgumentException("region is null");
        }
        BucketNameUtils.validateBucketName(bucketName);
        if (EndpointUtils.isRapidBucket(bucketName)) {
            return String.format("%s.%s.myqcloud.com", bucketName, Region.formatRapidRegion(this.region));
        }
        return String.format("%s.%s.myqcloud.com", bucketName, Region.formatRegion(this.region));
    }

    @Override
    public String buildGetServiceApiEndpoint() {
        return "service.cos.myqcloud.com";
    }

    /**
     * 带桶名的 GetService 端点构建。高性能桶使用 cos-rapid 域名。
     */
    public String buildGetServiceApiEndpoint(String bucketName) {
        if (EndpointUtils.isRapidBucket(bucketName)) {
            if (this.region == null) {
                throw new IllegalArgumentException("region is null");
            }
            return String.format("service.%s.myqcloud.com", Region.formatRapidRegion(this.region));
        }
        return buildGetServiceApiEndpoint();
    }
}
