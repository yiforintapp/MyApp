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

        try {
            initAesKeyIfNeeded();
        } catch (RuntimeException e) {
            return message;
        }
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

        try {
            initAesKeyIfNeeded();
        } catch (RuntimeException e) {
            return message;
        }
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

        try {
            initAesKeyIfNeeded();
        } catch (RuntimeException e) {
            return data;
        }
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
            InvalidKeyException, InvalidAlgorithmParameterException, RuntimeException {
        initAesKeyIfNeeded();
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.ENCRYPT_MODE, sAesKey, sAesIv);
        return new CipherOutputStream(os, c);
    }

    public static CipherInputStream newInputStream(InputStream in)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, InvalidAlgorithmParameterException, RuntimeException {
        initAesKeyIfNeeded();
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.DECRYPT_MODE, sAesKey, sAesIv);
        return new CipherInputStream(in, c);
    }

    private static void initAesKeyIfNeeded() throws RuntimeException {
        if (sAesKey != null && sAesIv != null) {
            return;
        }
        synchronized (LOCK) {
            AppMasterApplication application = AppMasterApplication.getInstance();
            String[] keys = application.getKeyArray();
            if (keys == null || keys.length <= 0) {
                throw new RuntimeException("key is null.");
            }
            byte[] keyByte = HexStringToByteArray(keys[0]);
            sAesKey = new SecretKeySpec(keyByte, "AES");

            byte[] ivByte = HexStringToByteArray(keys[1]);
            sAesIv = new IvParameterSpec(ivByte);
        }
    }

    private static byte[] HexStringToByteArray(String s) throws IllegalArgumentException {
        int len = s.length();
        if (len % 2 == 1) {
            throw new IllegalArgumentException("Hex string must have even number of characters");
        }
        byte[] data = new byte[len / 2]; // Allocate 1 byte per 2 hex characters
        for (int i = 0; i < len; i += 2) {
            // Convert each character into a integer (base-16), then bit-shift into place
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

}
