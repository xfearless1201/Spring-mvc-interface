package com.cn.tianxia.pay.glzf.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Util {

	private static final char HEX_DIGITS[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	public static String md5(String s, String charset) {
		try {

			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			digest.update((s).getBytes(charset));
			byte messageDigest[] = digest.digest();

			return toHexString(messageDigest);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return "";
	}

	public static String toHexString(byte[] b) {
		StringBuilder sb = new StringBuilder(b.length * 2);
		for (int i = 0; i < b.length; i++) {
			sb.append(HEX_DIGITS[(b[i] & 0xf0) >>> 4]);
			sb.append(HEX_DIGITS[b[i] & 0x0f]);
		}
		return sb.toString();
	}
	
	public static void main(String[] str) {
		System.out.println(md5("Amount=1000&MerNo=88570021&NotifyUrl=http://XXX.XXX.com/asyncNotifyProcess&PdtName=测试&ProductId=0612&Remark=测试&ReqTime=20180326194355&ReturnUrl=http://XXX.XXX.com/return&TxCode=210112&TxSN=20180326194355aeac69bb25d9b1a87754b32701f4ff77","utf-8"));
	}
}
