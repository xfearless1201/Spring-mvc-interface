package com.cn.tianxia.pay.ht.util;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author 易付
 * @time 2016-08-01
 */
public class AES {
	private static final String CipherMode = "AES/ECB/PKCS5Padding";

	/**
	 * 生成一个AES密钥对象
	 * 
	 * @return
	 */
	public static SecretKeySpec generateKey() throws Exception {
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		kgen.init(128, new SecureRandom());
		SecretKey secretKey = kgen.generateKey();
		byte[] enCodeFormat = secretKey.getEncoded();
		SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
		return key;
	}

	/**
	 * 生成一个AES密钥字符串
	 * 
	 * @return
	 */
	public static String generateKeyString() throws Exception {
		return byte2hex(generateKey().getEncoded());
	}

	/**
	 * 加密字节数据
	 * 
	 * @param content
	 * @param key
	 * @return
	 */
	public static byte[] encrypt(byte[] content, byte[] key) throws Exception {
		Cipher cipher = Cipher.getInstance(CipherMode);
		cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));
		byte[] result = cipher.doFinal(content);
		return result;
	}

	/**
	 * 通过byte[]类型的密钥加密String
	 * 
	 * @param content
	 * @param key
	 * @return 16进制密文字符串
	 */
	public static String encrypt(String content, byte[] key) throws Exception {
		Cipher cipher = Cipher.getInstance(CipherMode);
		cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));
		byte[] data = cipher.doFinal(content.getBytes("UTF-8"));
		String result = byte2hex(data);
		return result;
	}

	/**
	 * 通过String类型的密钥加密String
	 * 
	 * @param content
	 * @param key
	 * @return 16进制密文字符串
	 */
	public static String encrypt(String content, String key) throws Exception {
		byte[] data = null;
		try {
			data = content.getBytes("UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		data = encrypt(data, new SecretKeySpec(hex2byte(key), "AES").getEncoded());
		String result = byte2hex(data);
		return result;
	}

	/**
	 * 通过byte[]类型的密钥解密byte[]
	 * 
	 * @param content
	 * @param key
	 * @return
	 */
	public static byte[] decrypt(byte[] content, byte[] key) throws Exception {
		Cipher cipher = Cipher.getInstance(CipherMode);
		cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"));
		byte[] result = cipher.doFinal(content);
		return result;
	}

	/**
	 * 通过String类型的密钥 解密String类型的密文
	 * 
	 * @param content
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static String decrypt(String content, String key) throws Exception {
		byte[] data = null;
		data = hex2byte(content);
		data = decrypt(data, hex2byte(key));
		if (data == null)
			return null;
		String result = null;
		result = new String(data, "UTF-8");
		return result;
	}

	/**
	 * 通过byte[]类型的密钥 解密String类型的密文
	 * 
	 * @param content
	 * @param key
	 * @return
	 */
	public static String decrypt(String content, byte[] key) throws Exception {
		Cipher cipher = Cipher.getInstance(CipherMode);
		cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"));
		byte[] data = cipher.doFinal(hex2byte(content));
		return new String(data, "UTF-8");
	}

	/**
	 * 字节数组转成16进制字符串
	 * 
	 * @param b
	 * @return
	 */
	public static String byte2hex(byte[] b) { // 一个字节的数，
		StringBuffer sb = new StringBuffer(b.length * 2);
		String tmp;
		for (int n = 0; n < b.length; n++) {
			// 整数转成十六进制表示
			tmp = (Integer.toHexString(b[n] & 0XFF));
			if (tmp.length() == 1) {
				sb.append("0");
			}
			sb.append(tmp);
		}
		return sb.toString().toUpperCase(); // 转成大写
	}

	/**
	 * 将hex字符串转换成字节数组
	 * 
	 * @param inputString
	 * @return
	 */
	private static byte[] hex2byte(String inputString) {
		if (inputString == null || inputString.length() < 2) {
			return new byte[0];
		}
		inputString = inputString.toLowerCase();
		int l = inputString.length() / 2;
		byte[] result = new byte[l];
		for (int i = 0; i < l; ++i) {
			String tmp = inputString.substring(2 * i, 2 * i + 2);
			result[i] = (byte) (Integer.parseInt(tmp, 16) & 0xFF);
		}
		return result;
	}

	public static void main(String[] args) {
		try {

			// 生成AES密钥
			String key = "4d4cd0e76aecc5eca4dc322eaad3448b";
			// String key = "1234567890123456";
			System.out.println("AES-KEY:" + key);

			// 内容
			String content = "50000.00";

			// 用AES加密内容
			String miContent = AES.encrypt(content, key);
			System.out.println("AES加密后的内容:" + miContent);
			// 用AES解密内容
			String mingContent = AES.decrypt(miContent, key);
			System.out.println("AES解密后的内容:" + mingContent);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}