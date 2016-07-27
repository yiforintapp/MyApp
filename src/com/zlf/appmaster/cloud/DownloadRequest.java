package com.zlf.appmaster.cloud;

import com.zlf.appmaster.cloud.DownloadService;

import java.net.MalformedURLException;

/**
 * Created by Jasper on 2015/12/16.
 */
public class DownloadRequest extends com.zlf.appmaster.cloud.CloudRequest {

    public static final String TAG = DownloadRequest.class.getSimpleName();

    public DownloadRequest(String uploadId, String serverUrl) {
        super(uploadId, serverUrl);
    }

    public void startDownload() throws IllegalArgumentException, MalformedURLException {
        this.validate();

        DownloadService service = (DownloadService) com.zlf.appmaster.cloud.CloudService.createService(DownloadRequest.class);
        service.addRequest(this);
    }
}
