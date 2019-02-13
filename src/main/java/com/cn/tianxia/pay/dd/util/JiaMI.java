package com.cn.tianxia.pay.dd.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class JiaMI {
	
	private static final String DEFAULT_PUBLIC_KEY= "";
	
	private static final String DEFAULT_PRIVATE_KEY="";
	
	private static String encryptedString = "<REQ><merc_ord_no><![CDATA[1132154545]]></merc_ord_no></REQ>";
	
	public void encrypt(){
		try {
			byte[] raw = "jia mi".getBytes("utf-8");
			SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
			byte[] encrypted = cipher.doFinal(encryptedString.getBytes("utf-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	public static void main(String[] args) {
		try {
			byte[] raw = "60D02AF787CB74FB".getBytes("utf-8");
			SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
			byte[] encrypted = cipher.doFinal(encryptedString.getBytes("utf-8"));
			System.out.println(new String(encrypted));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
