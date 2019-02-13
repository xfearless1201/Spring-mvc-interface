package com.cn.tianxia.pay.daqiang.util;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.digest.DigestUtils;

public class SignUtils {
	/**
	 * signType=1 (MD5签名方法)
	 * 
	 * @param message
	 * @return message
	 * @throws UnsupportedEncodingException
	 */
	public static String md5(String message) throws UnsupportedEncodingException {
		return DigestUtils.md5Hex(message.getBytes("UTF-8"));
	}
}
