package com.cn.tianxia.pay.sl.util;

import java.security.MessageDigest;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

//import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

public class MD5 {
	public static byte[] getKeyedDigest(byte[] buffer, byte[] key) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(buffer);
            return md5.digest(key);
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return null;
    }
	public static byte[] getDigest(byte[] buffer) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(buffer);
            return md5.digest();
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return null;
    }
	public static String getKeyedDigest(String strSrc, String key) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(strSrc.getBytes("UTF8"));
            String result="";
            byte[] temp=md5.digest(key.getBytes("UTF8"));
    		for (int i=0; i<temp.length; i++){
    			result+=Integer.toHexString((0x000000ff & temp[i]) | 0xffffff00).substring(6);
    		}
    		return result;
        } catch(Exception e) {
        	e.printStackTrace();
        }
        return null;
    }
	public static String getDigest(String strSrc) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(strSrc.getBytes("UTF8"));
            String result="";
            byte[] temp=md5.digest();
    		for (int i=0; i<temp.length; i++){
    			result+=Integer.toHexString((0x000000ff & temp[i]) | 0xffffff00).substring(6);
    		}
    		return result;
        } catch(Exception e) {
        	e.printStackTrace();
        }
        return null;
    }
	public static void main(String[] args) {
		
	}
}
