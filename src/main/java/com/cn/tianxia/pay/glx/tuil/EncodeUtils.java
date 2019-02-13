package com.cn.tianxia.pay.glx.tuil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringEscapeUtils;

public class EncodeUtils {
	private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	private static final String DEFAULT_URL_ENCODING = "UTF-8";

	/**
	 * 哈希编码
	 * @param input
	 * @return
	 */
	public static String hexEncode(byte[] input) {
		return new String(Hex.encodeHex(input));
	}

	/**
	 * 哈希解码
	 * @param input
	 * @return
	 */
	public static byte[] hexDecode(String input) {
		try {
			return Hex.decodeHex(input.toCharArray());
		} catch (DecoderException e) {
			throw new IllegalStateException("Hex Decoder exception", e);
		}
	}

	/**
	 * Base64编码, byte[]->String
	 * @param input
	 * @return
	 */
	public static String base64Encode(byte[] input) {
		return new String(Base64.encodeBase64(input));
	}

	/**
	 * Base64编码, URL安全(将Base64中的URL非法字符'+'和'/'转为'-'和'_', 见RFC3548).  
	 * @param input
	 * @return
	 */
	public static String base64UrlSafeEncode(byte[] input) {
		return "";
	}

	/**
	 * Base64解码, String->byte[].  
	 * @param input
	 * @return
	 */
	public static byte[] base64Decode(String input) {
		return Base64.decodeBase64(input.getBytes());
	}

	/**
	 * Base36(0_9A_Z)编码, long->String.
	 * @param num
	 * @return
	 */
	public static String base36Encode(long num) {
		return alphabetEncode(num, 36);
	}

	/**
	 * Base36(0_9A_Z)解码, String->long.
	 * @param str
	 * @return
	 */
	public static long base36Decode(String str) {
		return alphabetDecode(str, 36);
	}

	/**
	 * Base62(0_9A_Za_z)编码, long->String.
	 * @param num
	 * @return
	 */
	public static String base62Encode(long num) {
		return alphabetEncode(num, 62);
	}

	/**
	 * Base62(0_9A_Za_z)解码, String->long.
	 * @param str
	 * @return
	 */
	public static long base62Decode(String str) {
		return alphabetDecode(str, 62);
	}

	private static String alphabetEncode(long num, int base) {
		StringBuilder sb = new StringBuilder();
		for (; num > 0L; num /= base) {
			sb.append("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".charAt((int) (num % base)));
		}

		return sb.toString();
	}

	private static long alphabetDecode(String str, int base) {
		long result = 0L;
		for (int i = 0; i < str.length(); i++) {
			result += "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".indexOf(str.charAt(i))
					* Math.pow(base, i);
		}

		return result;
	}

	/**
	 * URL 编码, Encode默认为UTF-8.
	 * @param input
	 * @return
	 */
	public static String urlEncode(String input) {
		try {
			return URLEncoder.encode(input, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException("Unsupported Encoding Exception", e);
		}
	}
	
	public static String urlEncode(String input, String charset) {
		try {
			return URLEncoder.encode(input, charset);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException("Unsupported Encoding Exception", e);
		}
	}

	/**
	 * URL 解码, Encode默认为UTF-8.
	 * @param input
	 * @return
	 */
	public static String urlDecode(String input) {
		try {
			return URLDecoder.decode(input, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException("Unsupported Encoding Exception", e);
		}
	}

}
