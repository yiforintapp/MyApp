package com.leo.appmaster.cloud.crypto;

import android.text.TextUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Created by Jasper on 2015/12/2.
 */
public class ImageEncryptInputStream extends InputStream {
    private ImageEncryptor mImageEncryptor;
    private String mFilePath;

    public ImageEncryptInputStream(String path) throws FileNotFoundException {
        this(path, new ImageEncryptor());
    }

    public ImageEncryptInputStream(String path, ImageEncryptor encryptor) throws FileNotFoundException {
        if (TextUtils.isEmpty(path)) {
            throw new RuntimeException("path must not be null.");
        }
        File file = new File(path);
        if (!file.exists()) {
            throw new FileNotFoundException(path);
        }
        mFilePath = path;
        encryptor.open(path);
        mImageEncryptor = encryptor;
    }

    @Override
    public int read() throws IOException {
        byte[] buffer = new byte[1];
        int result = read(buffer, 0, 1);
        return (result != -1) ? buffer[0] & 0xff : -1;
    }

    @Override
    public void close() throws IOException {
        super.close();
        mImageEncryptor.close(mFilePath);
    }

    @Override
    public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
        Arrays.fill(buffer, (byte) 0);
        int size = mImageEncryptor.read(buffer, byteOffset, byteCount);
        return size;
    }
}
