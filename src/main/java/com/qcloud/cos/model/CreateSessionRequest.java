package com.qcloud.cos.model;

import java.io.Serializable;

import com.qcloud.cos.internal.CosServiceRequest;

/**
 * cos-rapid 高性能桶换取临时数据密钥（session）的请求。
 * <p>
 * 该请求本身使用用户的长期凭证（固定密钥 / STS / CVM Role）加签，返回的对象级 / 分块级接口专用临时密钥。
 * 请求不区分 ReadOnly / ReadWrite 模式，服务端按桶返回统一可用的临时数据密钥。
 * </p>
 */
public class CreateSessionRequest extends CosServiceRequest implements Serializable {

    /** 桶名 */
    private String bucketName;

    public CreateSessionRequest() {
    }

    public CreateSessionRequest(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public CreateSessionRequest withBucketName(String bucketName) {
        setBucketName(bucketName);
        return this;
    }
}
