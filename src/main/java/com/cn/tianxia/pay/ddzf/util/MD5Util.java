package com.cn.tianxia.pay.ddzf.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class MD5Util {

	private static MessageDigest MD5;
	static {
		try {
			MD5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public static synchronized String getMd5(String msg) {
		return getMd5(msg.getBytes());
	}

	public static String getMd5(String source, String key){
		byte[] k_ipad = new byte[64];
		byte[] k_opad = new byte[64];
		byte[] keyb = null;
		byte[] value = null;
		try {
			keyb = key.getBytes("UTF8");
			value = source.getBytes("UTF8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		
		
		Arrays.fill(k_ipad, keyb.length, 64, (byte)0x36);
		Arrays.fill(k_opad, keyb.length, 64, (byte)0x5c);
		
		for (int i = 0; i < keyb.length; i++) {
			k_ipad[i] = (byte) (keyb[i] ^ 0x36);
			k_opad[i] = (byte) (keyb[i] ^ 0x5C);
		}
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
		md.update(k_ipad);
		md.update(value);
		byte[] dg = md.digest();
		md.reset();
		md.update(k_opad);
		md.update(dg, 0, 16);
		dg = md.digest();
		return CodingUtil.bytesToHexString(dg);
	}

	public static synchronized byte[] getMd5Byte(String msg) {
		return getMd5Byte(msg.getBytes());
	}

	public static synchronized byte[] getMd5Byte(byte[] msg) {
		return MD5.digest(msg);
	}

	public static synchronized String getMd5(byte[] msg) {
		MD5.update(msg);
		return CodingUtil.bytesToHexString(MD5.digest());
	}
}
