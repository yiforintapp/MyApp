package com.zlf.appmaster.cloud;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by Jasper on 2015/12/16.
 */
public class UploadService extends com.zlf.appmaster.cloud.CloudService {
    public static final String NAMESPACE = "com.leo.appmaster";

    protected static final String PARAM_ID = "id";
    protected static final String PARAM_URL = "url";
    protected static final String PARAM_METHOD = "method";
    protected static final String PARAM_FILES = "files";
    protected static final String PARAM_FILE = "file";
    protected static final String PARAM_TYPE = "uploadType";

    protected static final String PARAM_REQUEST_HEADERS = "requestHeaders";
    protected static final String PARAM_REQUEST_PARAMETERS = "requestParameters";
    protected static final String PARAM_CUSTOM_USER_AGENT = "customUserAgent";
    protected static final String PARAM_MAX_RETRIES = "maxRetries";

    UploadService() {
        super();
    }

    @Override
    protected void performRequest(com.zlf.appmaster.cloud.CloudRequest request) {
        if (!(request instanceof com.zlf.appmaster.cloud.UploadRequest)) {
            return;
        }

        while (true) {
            try {
                HttpURLConnection connection = openConnection(request);
                if (connection == null) {
                    return;
                }
                List<NameValue> headers = request.getHeaders();
                if (headers != null && headers.size() > 0) {
                    for (NameValue nameValue : headers) {
                        connection.setRequestProperty(nameValue.getName(), nameValue.getValue());
                    }
                }
                connection.getOutputStream();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    @Override
    protected HttpURLConnection openConnection(com.zlf.appmaster.cloud.CloudRequest request) throws MalformedURLException, IOException {
        URL url = new URL(request.getServerUrl());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setUseCaches(false);
        connection.setRequestMethod(request.getMethod());
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setConnectTimeout(request.getTimeoutConnect());
        connection.setReadTimeout(request.getTimeoutSocket());
        return connection;
    }

}
