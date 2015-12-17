package com.leo.appmaster.cloud;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

/**
 * Created by Jasper on 2015/12/16.
 */
class MultipartUploadFile extends UploadFile {

    private static final String NEW_LINE = "\r\n";

    protected final String paramName;
    protected final String fileName;
    protected String contentType;

    public MultipartUploadFile(final String path, final String parameterName,
                               final String fileName, final String contentType)
        throws FileNotFoundException, IllegalArgumentException {

        super(path);

        if (parameterName == null || "".equals(parameterName)) {
            throw new IllegalArgumentException("Please specify parameterName value for file: " + path);
        }

        this.paramName = parameterName;
        this.contentType = contentType;

        if (fileName == null || "".equals(fileName)) {
            this.fileName = this.file.getName();
        } else {
            this.fileName = fileName;
        }
    }

    public byte[] getMultipartHeader() throws UnsupportedEncodingException {
        StringBuilder builder = new StringBuilder();

        builder.append("Content-Disposition: form-data; name=\"")
               .append(paramName).append("\"; filename=\"")
               .append(fileName).append("\"").append(NEW_LINE);

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        builder.append("Content-Type: ").append(contentType).append(NEW_LINE).append(NEW_LINE);

        return builder.toString().getBytes("US-ASCII");
    }

    public long getTotalMultipartBytes(long boundaryBytesLength) throws UnsupportedEncodingException {
        return boundaryBytesLength + getMultipartHeader().length + file.length();
    }

}
