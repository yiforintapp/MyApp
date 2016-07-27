package com.zlf.appmaster.cloud.crypto;

import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Jasper on 2015/12/10.
 */
public class ImageUploadOutputStream extends FilterOutputStream {

    /**
     * Constructs a new {@code FilterOutputStream} with {@code out} as its
     * target stream.
     *
     * @param out the target stream that this stream writes to.
     */
    public ImageUploadOutputStream(OutputStream out, String path) {
        super(out);

        if (TextUtils.isEmpty(path)) {
            throw new RuntimeException("path must not be null.");
        }

        File file = new File(path);
        if (!file.exists()) {
            throw new RuntimeException("file not exist.");
        }

        try {
            new FileInputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void write(byte[] buffer, int offset, int length) throws IOException {

        super.write(buffer, offset, length);
    }

    @Override
    public void write(int oneByte) throws IOException {
        super.write(oneByte);
    }
}
