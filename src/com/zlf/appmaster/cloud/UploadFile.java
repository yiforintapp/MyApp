package com.zlf.appmaster.cloud;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by Jasper on 2015/12/16.
 */
class UploadFile {

    protected final File file;

    UploadFile(String path) throws FileNotFoundException, IllegalArgumentException {
        if (path == null || "".equals(path)) {
            throw new IllegalArgumentException("Please specify a file path! Passed path value is: " + path);
        }
        File file = new File(path);
        if (!file.exists()) throw new FileNotFoundException("Could not find file at path: " + path);
        this.file = file;
    }

    public long length() {
        return file.length();
    }

    public final InputStream getStream() throws FileNotFoundException {
        return new FileInputStream(file);
    }

}
