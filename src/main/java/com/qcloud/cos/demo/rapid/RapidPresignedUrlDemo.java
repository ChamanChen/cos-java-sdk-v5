package com.qcloud.cos.demo.rapid;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.auth.RapidCOSCredentialProvider;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.GeneratePresignedUrlRequest;
import com.qcloud.cos.region.Region;

/**
 * 高性能桶预签名 URL 示例
 *
 * ⚠️ 重要限制：高性能桶预签名 URL 有效期不能超过 5 分钟（300 秒），
 * 超过会抛出 CosClientException。建议设置 3~4 分钟，预留网络传输时间。
 */
public class RapidPresignedUrlDemo {
    private static String secretId = "AKIDXXXXXXXX";
    private static String secretKey = "1A2Z3YYYYYYYYYY";
    private static String bucketName = "examplebucket-x--12500000000";
    private static String region = "ap-guangzhou";
    private static COSClient cosClient = createClient();

    public static void main(String[] args) {
        try {
            generatePresignedDownloadUrl();
            // generatePresignedUploadUrl();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cosClient.shutdown();
        }
    }

    private static COSClient createClient() {
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        RapidCOSCredentialProvider credProvider = new RapidCOSCredentialProvider(cred);
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        return new COSClient(credProvider, clientConfig);
    }

    private static void generatePresignedDownloadUrl() {
        String key = "aaa/bbb.txt";
        GeneratePresignedUrlRequest req =
                new GeneratePresignedUrlRequest(bucketName, key, HttpMethodName.GET);

        // ⚠️ 高性能桶预签名有效期不能超过 5 分钟（300 秒），这里设置 4 分钟
        Date expirationDate = new Date(System.currentTimeMillis() + 4 * 60 * 1000);
        req.setExpiration(expirationDate);

        URL url = cosClient.generatePresignedUrl(req);
        System.out.println("download url: " + url.toString());
    }

    private static void generatePresignedUploadUrl() {
        String key = "aaa/ccc.txt";

        // ⚠️ 高性能桶预签名有效期不能超过 5 分钟（300 秒），这里设置 3 分钟
        Date expirationTime = new Date(System.currentTimeMillis() + 3 * 60 * 1000);
        Map<String, String> headers = new HashMap<>();
        Map<String, String> params = new HashMap<>();

        URL url = cosClient.generatePresignedUrl(bucketName, key, expirationTime,
                HttpMethodName.PUT, headers, params);
        System.out.println("upload url: " + url.toString());

        // 使用预签名 URL 上传
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("PUT");
            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
            out.write("This text uploaded as object.");
            out.close();
            int responseCode = connection.getResponseCode();
            System.out.println("response code: " + responseCode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
