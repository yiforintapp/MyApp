package com.leo.appmaster.encrypt;

/**
 * Created by Jasper on 2015/12/2.
 */
public class ImageEncryptor {
    public static final int TYPE_BLOCK = 1;
    public static final int TYPE_ALL = 0;

    static {
        try {
            System.loadLibrary("LeoImage");
            nativeInit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 此变量由native层持有
    private long mNativeContext;

    public ImageEncryptor() {
    }

    private native static void nativeInit(int debugable);

    /**
     * 加密
     * @param filePath
     * @return
     */
    public native int encrypt(String filePath);

    /**
     * 解密
     * @param filePath
     * @return
     */
    public native int decrypt(String filePath);

    /**
     * 是否已加密
     * @param filePath
     * @return
     */
    public native boolean isEncrypt(String filePath);

    /**
     * 读取文件内容
     * @param byteOffset
     * @param byteCount
     * @return
     */
    public native int read(byte[] buffer, int byteOffset, int byteCount);

    /**
     * 打开文件
     * @param filePath
     */
    public native void open(String filePath);

    /**
     * 关闭文件
     * @param filePath
     */
    public native void close(String filePath);
}
