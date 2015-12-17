package com.leo.appmaster.cloud;

import java.net.HttpURLConnection;

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
    protected HttpURLConnection openConnection() {
        return null;
    }
}
