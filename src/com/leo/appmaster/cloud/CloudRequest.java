package com.leo.appmaster.cloud;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Jasper on 2015/12/16.
 */
public class CloudRequest {

    private static final int TIMEOUT_CONNECT = 10000;
    private static final int TIMEOUT_SOCKET = 15000;

    protected String method = "POST";
    protected int maxRetries;
    protected final String uploadId;
    protected final String url;
    protected final ArrayList<NameValue> headers;

    protected boolean canceled = false;

    public CloudRequest(final String uploadId, final String serverUrl) {
        this.uploadId = uploadId;
        url = serverUrl;
        headers = new ArrayList<NameValue>();
        maxRetries = 0;
    }

    protected void validate() throws IllegalArgumentException, MalformedURLException {
        if (url == null || "".equals(url)) {
            throw new IllegalArgumentException("Request URL cannot be either null or empty");
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new IllegalArgumentException("Specify either http:// or https:// as protocol");
        }

        new URL(url);
    }

    public CloudRequest addHeader(final String headerName, final String headerValue) {
        headers.add(new NameValue(headerName, headerValue));
        return this;
    }

    public CloudRequest setMethod(final String method) {
        if (method != null && method.length() > 0)
            this.method = method;

        return this;
    }

    protected String getMethod() {
        return method;
    }

    protected String getUploadId() {
        return uploadId;
    }

    protected String getServerUrl() {
        return url;
    }

    protected ArrayList<NameValue> getHeaders() {
        return headers;
    }

    protected final int getMaxRetries() {
        return maxRetries;
    }

    public CloudRequest setMaxRetries(int maxRetries) {
        if (maxRetries < 0) {
            this.maxRetries = 0;
        } else {
            this.maxRetries = maxRetries;
        }

        return this;
    }

    public int getTimeoutConnect() {
        return TIMEOUT_CONNECT;
    }

    public int getTimeoutSocket() {
        return TIMEOUT_SOCKET;
    }

    public void cancel() {
        canceled = true;
    }

    public boolean isCanceled() {
        return canceled;
    }

}
