package com.cn.tianxia.pay.dd.util;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5 {

    /**
     * 返回 md5摘要字节
     *
     * @param input
     * @return
     */
    private static byte[] digest(String input) {
        return digest(input, Charset.forName("UTF-8"));
    }

    private static byte[] digest(String input, Charset charset) {
        try {
            // 拿到一个MD5转换器（如果想要SHA1参数换成”SHA1”）
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            // 输入的字符串转换成字节数组
            byte[] inputByteArray = input.getBytes(charset);
            // inputByteArray是输入字符串转换得到的字节数组
            messageDigest.update(inputByteArray);
            // 转换并返回结果，也是字节数组，包含16个元素
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    /**
     * @param input
     * @return
     * @Description: 32位小写MD5
     */
    public static String digest32Lower(String input) {
        String digestVal = null;
        byte[] bytes = digest(input);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            int bt = b & 0xff;
            if (bt < 16) {
                sb.append(0);
            }
            sb.append(Integer.toHexString(bt));
        }
        digestVal = sb.toString();

        return digestVal;
    }

    /**
     * @param input
     * @return
     * @Description: 32位大写MD5
     */
    public static String digest32Upper(String input) {
        String reStr = digest32Lower(input);
        if (reStr != null) {
            reStr = reStr.toUpperCase();
        }
        return reStr;
    }

    /**
     * @param input
     * @return
     * @Description: 16位小写MD5
     */
    public static String digest16Lower(String input) {
        String reStr = digest32Lower(input);
        if (reStr != null) {
            reStr = reStr.toUpperCase().substring(8, 24);
        }
        return reStr;
    }

    /**
     * @param input
     * @return
     * @Description: 16位大写MD5
     */
    public static String digest16Upper(String input) {
        String reStr = digest32Upper(input);
        if (reStr != null) {
            reStr = reStr.substring(8, 24);
        }
        return reStr;
    }
}
