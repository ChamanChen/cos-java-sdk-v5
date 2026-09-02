# cos-java-sdk-v5  ![Build Status](https://api.travis-ci.org/tencentyun/cos-java-sdk-v5.svg?branch=master)



## maven坐标

```xml
<dependency>
    <groupId>com.qcloud</groupId>
    <artifactId>cos_api</artifactId>
    <version>5.6.261</version>
</dependency>
```

cos-java-sdk-v5 适用于COS XML API https://www.qcloud.com/document/product/436/7751, 

JSON API 请参照 https://github.com/tencentyun/cos-java-sdk-v4

示例程序 demo 请参照 https://github.com/tencentyun/cos-java-sdk-v5/blob/master/src/main/java/com/qcloud/cos/demo
下的示例代码

## 高性能桶（COS Rapid Bucket）支持

高性能桶（COS Rapid Bucket）使用 session 临时数据密钥鉴权。开启方式：在构建 `COSClient` 前调用
`ClientConfig.setRapidBucket(true)`，SDK 内部的 `RapidSessionManager` 会自动完成 `CreateSession`、
按桶缓存并复用 session 凭证，底层基础凭证（SecretId/SecretKey、STS、CVM Role 等）变更后会自动重签。

特性要点：
* Rename （高性能桶支持，`COSClient.rename`，同桶内重命名对象）
* Session 预签名 URL（高性能桶自动支持，调用 `COSClient.generatePresignedUrl` 时 SDK 通过桶名自动识别高性能桶并使用 session 临时凭证加签）
* 高性能桶 session 自动鉴权（高性能桶专用，`ClientConfig.setRapidBucket(true)` 开启后，所有对象/分块接口自动使用 session 临时凭证加签）或 自动通过 cos-rapid.<region>.tencentcos.com 的endpoint识别为Rapid Bucket。

示例程序请参照 [demo 目录](https://github.com/tencentyun/cos-java-sdk-v5/blob/master/src/main/java/com/qcloud/cos/demo) 下的示例代码。

### 高性能桶支持 API 汇总

调用 `ClientConfig.setRapidBucket(true)` 后，即可用下列 API 操作高性能桶：

| 类别 | API（SDK 方法） | 说明 |
|---|---|---|
| 对象 | `COSClient.putObject` | 上传对象 |
| 对象 | `COSClient.getObject` | 下载对象 |
| 对象 | `COSClient.getObjectMetadata` | 查询对象元信息 |
| 对象 | `COSClient.doesObjectExist` | 查询对象是否存在 |
| 对象 | `COSClient.deleteObject` / `deleteObjects` | 删除单个 / 批量对象 |
| 对象 | `COSClient.copyObject` | 复制对象 |
| 对象 | `COSClient.rename` | 同桶内原子重命名对象（高性能桶支持） |
| 对象 | `COSClient.generatePresignedUrl` | 获取预签名 URL（高性能桶自动使用 session 凭证加签） |
| 分块上传 | `COSClient.initiateMultipartUpload` / `uploadPart` / `completeMultipartUpload` / `abortMultipartUpload` / `listParts` | 分块上传全流程 |
| 分块上传 | `COSClient.copyPart` | 分块复制 |
| 分块上传 | `COSClient.listMultipartUploads` | 列出进行中的分块上传 |
| 存储桶 | `COSClient.listObjects` | 列举对象 |
| 存储桶 | `COSClient.createBucket` | 创建存储桶 |
| 存储桶 | `COSClient.deleteBucket` / `headBucket` / `doesBucketExist` | 删除 / 查询存储桶 |
| 存储桶 | `COSClient.createSession` | 换取 session 临时凭证（高性能桶专用） |
| 存储桶 | `COSClient.setBucketPolicy` / `getBucketPolicy` / `deleteBucketPolicy` | 存储桶权限策略 |


## 常见问题:
请参考[FAQ](https://cloud.tencent.com/document/product/436/12263#faq)
