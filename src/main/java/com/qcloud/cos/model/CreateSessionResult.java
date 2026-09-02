package com.qcloud.cos.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * cos-rapid 高性能桶 CreateSession 接口的返回结果。
 * <p>
 * 对应返回的 XML：
 * <pre>
 * &lt;CreateSessionResult&gt;
 *   &lt;Credentials&gt;
 *     &lt;AccessKeyId&gt;...&lt;/AccessKeyId&gt;
 *     &lt;SecretAccessKey&gt;...&lt;/SecretAccessKey&gt;
 *     &lt;SessionToken&gt;...&lt;/SessionToken&gt;
 *     &lt;Expiration&gt;2026-07-24T12:00:00Z&lt;/Expiration&gt;
 *   &lt;/Credentials&gt;
 * &lt;/CreateSessionResult&gt;
 * </pre>
 * </p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateSessionResult implements Serializable {

    @JsonProperty("Credentials")
    private Credentials credentials;

    /** 凭证绝对过期时间戳（秒，Unix epoch） */
    @JsonProperty("ExpiredTime")
    private long expiredTime;

    /** 凭证过期的 ISO8601 时间字符串 */
    @JsonProperty("Expiration")
    private String expiration;

    @JsonProperty("RequestId")
    private String requestId;

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public long getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(long expiredTime) {
        this.expiredTime = expiredTime;
    }

    public String getExpiration() {
        return expiration;
    }

    public void setExpiration(String expiration) {
        this.expiration = expiration;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * 临时数据密钥明细。
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Credentials implements Serializable {

        private String tmpSecretId;
        private String tmpSecretKey;
        private String sessionToken;

        public String getTmpSecretId() {
            return tmpSecretId;
        }

        public void setTmpSecretId(String tmpSecretId) {
            this.tmpSecretId = tmpSecretId;
        }

        public String getTmpSecretKey() {
            return tmpSecretKey;
        }

        public void setTmpSecretKey(String tmpSecretKey) {
            this.tmpSecretKey = tmpSecretKey;
        }

        public String getSessionToken() {
            return sessionToken;
        }

        public void setSessionToken(String sessionToken) {
            this.sessionToken = sessionToken;
        }

        /** 兼容别名 */
        public String getToken() {
            return sessionToken;
        }
    }
}
