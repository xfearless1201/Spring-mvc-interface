package com.cn.tianxia.pay.xzx.util;

import java.security.MessageDigest;

public class SHAUtil {
	public static String sha256(String orig) {
		String strDes = null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(orig.getBytes("UTF-8"));
			strDes = bytes2Hex(md.digest()); // to HexString
			System.out.println(strDes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strDes;
	}

	public static String bytes2Hex(byte[] bts) {
		String des = "";
		String tmp = null;
		for (int i = 0; i < bts.length; i++) {
			tmp = (Integer.toHexString(bts[i] & 0xFF));
			if (tmp.length() == 1) {
				des += "0";
			}
			des += tmp;
		}
		return des;
	}
}
