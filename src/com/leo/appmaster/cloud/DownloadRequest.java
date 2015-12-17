package com.leo.appmaster.cloud;

import android.content.Context;
import android.content.Intent;

import java.net.MalformedURLException;

/**
 * Created by Jasper on 2015/12/16.
 */
public class DownloadRequest extends CloudRequest {

    public static final String TAG = DownloadRequest.class.getSimpleName();

    public DownloadRequest(String uploadId, String serverUrl) {
        super(uploadId, serverUrl);
    }

    public void startDownload() throws IllegalArgumentException, MalformedURLException {
        this.validate();

        DownloadService service = (DownloadService) CloudService.createService(DownloadRequest.class);
        service.addRequest(this);
    }
}
