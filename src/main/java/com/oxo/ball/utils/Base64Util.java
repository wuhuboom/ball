package com.oxo.ball.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

public class Base64Util {
    private static char[] key = new char[]{'4','4','2','7','6','5','1','8','0','9','3'};

    public static String byteToBase64(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    public static String base64ToString(String source) {
        byte[] decode = Base64.getDecoder().decode(source);
        try {
            return new String(decode, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * @param sourceString
     * @return 密文
     */

    public static String encrypt(String sourceString) {
        int n = key.length; // 密码长度
        char[] c = sourceString.toCharArray();
        int m = c.length; // 字符串长度
        for (int k = 0; k < m; k++) {
            int mima = c[k] + key[k % n]; // 加密
            c[k] = (char) mima;
        }
        return new String(c);
    }

    /**
     * @param sourceString 2022524183151
     * @return 明文
     */
    public static String decrypt(String sourceString) {
        int n = key.length; // 密码长度
        char[] c = sourceString.toCharArray();
        int m = c.length; // 字符串长度
        for (int k = 0; k < m; k++) {
            int mima = c[k] - key[k % n]; // 解密
            c[k] = (char) mima;
        }
        return new String(c);
    }
}
