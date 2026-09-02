package com.qcloud.cos.model;

import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.http.CosHttpResponse;
import com.qcloud.cos.internal.AbstractCosResponseHandler;
import com.qcloud.cos.internal.CosServiceResponse;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;

/**
 * 解析 CreateSession 返回的 XML 响应。
 */
public class CreateSessionResultHandler extends AbstractCosResponseHandler<CreateSessionResult> {

    private static final XMLInputFactory xmlInputFactory;
    static {
        xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
    }

    @Override
    public CosServiceResponse<CreateSessionResult> handle(CosHttpResponse response) throws Exception {
        String requestId = response.getHeaders().get("x-cos-request-id");

        CreateSessionResult result = new CreateSessionResult();
        result.setRequestId(requestId);

        InputStream content = response.getContent();
        if (content != null) {
            parseXml(content, result);
        }

        CosServiceResponse<CreateSessionResult> cosResponse = new CosServiceResponse<>();
        cosResponse.setResult(result);
        return cosResponse;
    }

    private void parseXml(InputStream in, CreateSessionResult result) {
        try {
            XMLStreamReader reader;
            synchronized (xmlInputFactory) {
                reader = xmlInputFactory.createXMLStreamReader(in);
            }

            CreateSessionResult.Credentials credentials = null;
            String currentTag = null;

            while (reader.hasNext()) {
                int event = reader.next();
                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        currentTag = reader.getLocalName();
                        if ("Credentials".equals(currentTag)) {
                            credentials = new CreateSessionResult.Credentials();
                        }
                        break;
                    case XMLStreamConstants.CHARACTERS:
                        String text = reader.getText();
                        if (text == null || currentTag == null) break;
                        text = text.trim();
                        if (text.isEmpty()) break;

                        if (credentials != null) {
                            switch (currentTag) {
                                case "AccessKeyId": credentials.setTmpSecretId(text); break;
                                case "SecretAccessKey": credentials.setTmpSecretKey(text); break;
                                case "SessionToken": credentials.setSessionToken(text); break;
                                case "Expiration": result.setExpiration(text); break;
                            }
                        }
                        switch (currentTag) {
                            case "ExpiredTime": result.setExpiredTime(Long.parseLong(text)); break;
                            case "RequestId": result.setRequestId(text); break;
                        }
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        String endTag = reader.getLocalName();
                        if ("Credentials".equals(endTag) && credentials != null) {
                            result.setCredentials(credentials);
                        }
                        currentTag = null;
                        break;
                }
            }
            reader.close();
        } catch (Exception e) {
            throw new CosClientException("Failed to parse CreateSession XML response", e);
        }
    }
}
