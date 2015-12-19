package com.leo.appmaster.cloud.crypto;

import android.text.TextUtils;
import android.util.Base64;

import com.leo.appmaster.AppMasterApplication;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
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

        byte[] keyByte = Base64.decode(keys[0], Base64.NO_WRAP);
        SecretKey aesKey = new SecretKeySpec(keyByte, "AES");

        byte[] ivByte = Base64.decode(keys[1], Base64.NO_WRAP);
        IvParameterSpec ivKey = new IvParameterSpec(ivByte);

        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(cipher.ENCRYPT_MODE, aesKey, ivKey);

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

        byte[] keyByte = Base64.decode(keys[0], Base64.NO_WRAP);
        SecretKey aesKey = new SecretKeySpec(keyByte, "AES");

        byte[] ivByte = Base64.decode(keys[1], Base64.NO_WRAP);
        IvParameterSpec ivKey = new IvParameterSpec(ivByte);

        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, aesKey, ivKey);

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

        byte[] keyByte = Base64.decode(keys[0], Base64.NO_WRAP);
        SecretKey aesKey = new SecretKeySpec(keyByte, "AES");

        byte[] ivByte = Base64.decode(keys[1], Base64.NO_WRAP);
        IvParameterSpec ivKey = new IvParameterSpec(ivByte);

        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, aesKey, ivKey);

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
}
