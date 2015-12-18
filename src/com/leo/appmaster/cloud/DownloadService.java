package com.leo.appmaster.cloud;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

/**
 * Created by Jasper on 2015/12/16.
 */
public class DownloadService extends CloudService {

    DownloadService() {
        super();
    }

    @Override
    protected void performRequest(CloudRequest request) {

    }

    @Override
    protected HttpURLConnection openConnection(CloudRequest request) throws MalformedURLException, IOException {
        return null;
    }

    protected HttpURLConnection openConnection() {
        return null;
    }
}
