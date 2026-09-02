package com.qcloud.cos.model;

import com.qcloud.cos.internal.CosServiceRequest;

import java.io.Serializable;

// this rename request only used in merge bucket
public class RenameRequest extends CosServiceRequest implements Serializable {
    // src object name
    private String srcObject;
    // dst object name
    private String dstObject;
    // bucket name
    private String bucketName;

    /**
     * 是否禁止覆盖目标路径上的同名文件对象（高性能桶专用，对应 x-cos-forbid-overwrite 请求头）。
     * <ul>
     *   <li>{@code true}：禁止覆盖，目标已存在同名文件对象时返回 409 FileAlreadyExists；</li>
     *   <li>{@code false} 或不设置（默认）：覆盖目标路径上的同名文件对象。</li>
     * </ul>
     */
    private boolean forbidOverwrite = false;

    public RenameRequest(String bucketName, String srcObject, String dstObject) {
        this.bucketName = bucketName;
        this.srcObject = srcObject;
        this.dstObject = dstObject;
    }

    public String getSrcObject() {
        return srcObject;
    }

    public String getDstObject() {
        return dstObject;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setSrcObject(String srcObject) {
        this.srcObject = srcObject;
    }

    public void setDstObject(String dstObject) {
        this.dstObject = dstObject;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public boolean isForbidOverwrite() {
        return forbidOverwrite;
    }

    public void setForbidOverwrite(boolean forbidOverwrite) {
        this.forbidOverwrite = forbidOverwrite;
    }
}
