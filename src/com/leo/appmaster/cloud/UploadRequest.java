package com.leo.appmaster.cloud;

import java.net.MalformedURLException;

/**
 * Created by Jasper on 2015/12/16.
 */
public class UploadRequest extends CloudRequest {
    public static final String TAG = UploadRequest.class.getSimpleName();

    public UploadRequest(String uploadId, String serverUrl) {
        super(uploadId, serverUrl);
    }

    public void startUpload() throws IllegalArgumentException, MalformedURLException {
        this.validate();

        UploadService service = (UploadService) CloudService.createService(UploadRequest.class);
        service.addRequest(this);
    }
}
