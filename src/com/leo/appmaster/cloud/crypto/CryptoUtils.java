package com.leo.appmaster.cloud.crypto;

import android.text.TextUtils;
import android.util.Base64;

import com.leo.appmaster.AppMasterApplication;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 加密工具类
 * Created by Jasper on 2015/12/19.
 */
public class CryptoUtils {
    private static final byte[] LOCK = new byte[1];
    private static SecretKey sAesKey;
    private static AlgorithmParameterSpec sAesIv;

    /**
     * 加密
     * @param message
     * @return
     */
    public static String encrypt(String message) {
        if (TextUtils.isEmpty(message)) {
            return message;
        }
        AppMasterApplication application = AppMasterApplication.getInstance();
        String[] keys = application.getKeyArray();
        if (keys == null || keys.length <= 0) {
            return message;
        }

        initAesKeyIfNeeded();
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(cipher.ENCRYPT_MODE, sAesKey, sAesIv);

            byte[] msgByte = message.getBytes("UTF-8");
            byte[] cryptoByte = cipher.doFinal(msgByte);

            return Base64.encodeToString(cryptoByte, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return message;
    }

    /**
     * 解密
     * @param message
     * @return
     */
    public static String decrypt(String message) {
        if (TextUtils.isEmpty(message)) {
            return message;
        }
        AppMasterApplication application = AppMasterApplication.getInstance();
        String[] keys = application.getKeyArray();
        if (keys == null || keys.length <= 0) {
            return message;
        }

        initAesKeyIfNeeded();
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, sAesKey, sAesIv);

            byte[] msgByte = Base64.decode(message, Base64.NO_WRAP);
            byte[] cryptoByte = cipher.doFinal(msgByte);

            return new String(cryptoByte, "UTF-8");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return message;
    }

    public static byte[] encrypt(byte[] data) {
        if (data == null || data.length <= 0) {
            return data;
        }
        AppMasterApplication application = AppMasterApplication.getInstance();
        String[] keys = application.getKeyArray();
        if (keys == null || keys.length <= 0) {
            return data;
        }

        initAesKeyIfNeeded();
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, sAesKey, sAesIv);

            byte[] cryptoByte = cipher.doFinal(data);
            return cryptoByte;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        return data;
    }

    public static CipherOutputStream newOutputStream(OutputStream os)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, InvalidAlgorithmParameterException {
        initAesKeyIfNeeded();
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.ENCRYPT_MODE, sAesKey, sAesIv);
        return new CipherOutputStream(os, c);
    }

    public static CipherInputStream newInputStream(InputStream in)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, InvalidAlgorithmParameterException {
        initAesKeyIfNeeded();
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.DECRYPT_MODE, sAesKey, sAesIv);
        return new CipherInputStream(in, c);
    }

    private static void initAesKeyIfNeeded() {
        if (sAesKey != null && sAesIv != null) {
            return;
        }
        synchronized (LOCK) {
            AppMasterApplication application = AppMasterApplication.getInstance();
            String[] keys = application.getKeyArray();
            if (keys == null || keys.length <= 0) {
                return;
            }

            byte[] keyByte = Base64.decode(keys[0], Base64.NO_WRAP);
            sAesKey = new SecretKeySpec(keyByte, "AES");

            byte[] ivByte = Base64.decode(keys[1], Base64.NO_WRAP);
            sAesIv = new IvParameterSpec(ivByte);
        }
    }
}
