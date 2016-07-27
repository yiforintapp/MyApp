package com.zlf.appmaster.cloud;

import com.zlf.appmaster.cloud.UploadService;

import java.net.MalformedURLException;

/**
 * Created by Jasper on 2015/12/16.
 */
public class UploadRequest extends com.zlf.appmaster.cloud.CloudRequest {
    public static final String TAG = UploadRequest.class.getSimpleName();

    public UploadRequest(String uploadId, String serverUrl) {
        super(uploadId, serverUrl);
    }

    public void startUpload() throws IllegalArgumentException, MalformedURLException {
        this.validate();

        UploadService service = (UploadService) com.zlf.appmaster.cloud.CloudService.createService(UploadRequest.class);
        service.addRequest(this);
    }
}
