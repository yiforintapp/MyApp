package com.zlf.appmaster.cloud;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

/**
 * Created by Jasper on 2015/12/16.
 */
public class DownloadService extends com.zlf.appmaster.cloud.CloudService {

    DownloadService() {
        super();
    }

    @Override
    protected void performRequest(com.zlf.appmaster.cloud.CloudRequest request) {

    }

    @Override
    protected HttpURLConnection openConnection(com.zlf.appmaster.cloud.CloudRequest request) throws MalformedURLException, IOException {
        return null;
    }

}
