package com.cn.tianxia.pay.ddzf.util;
import org.apache.commons.lang.StringUtils;

/**
 * @author Administrator
 */
public class CodingUtil {

	public static byte[] base64Decode(String str) {
		return Base64.decode(str);
	}

	public static String base64Encode(byte[] data) {
		return Base64.encode(data);
	}

	private static final int BIT_SIZE = 0x10;
	private static final int BIZ_ZERO = 0X00;

	private static char[][] charArrays = new char[256][];

	static {
		int v;
		char[] ds;
		String temp;
		for (int i = 0; i < charArrays.length; i++) {
			ds = new char[2];
			v = i & 0xFF;
			temp = Integer.toHexString(v);
			if (v < BIT_SIZE) {
				ds[0] = '0';
				ds[1] = temp.charAt(0);
			} else {
				ds[0] = temp.charAt(0);
				ds[1] = temp.charAt(1);
			}
			charArrays[i] = ds;
		}
	}

	public static String bytesToHexString(byte[] src) {
		HexAppender helper = new HexAppender(src.length * 2);
		if (src == null || src.length <= BIZ_ZERO) {
			return null;
		}
		int v;
		char[] temp;
		for (int i = 0; i < src.length; i++) {
			v = src[i] & 0xFF;
			temp = charArrays[v];
			helper.append(temp[0], temp[1]);
		}
		return helper.toString();
	}

	public static String bytesToHexStringSub(byte[] src, int length) {
		HexAppender helper = new HexAppender(src.length * 2);
		if (src == null || src.length <= BIZ_ZERO) {
			return null;
		}
		int v;
		char[] temp;
		for (int i = 0; i < length; i++) {
			v = src[i] & 0xFF;
			temp = charArrays[v];
			helper.append(temp[0], temp[1]);
		}
		return helper.toString();
	}

	/**
	 * Convert hex string to byte[]
	 * 
	 * @param hexString
	 *            the hex string
	 * @return byte[]
	 */
	public static byte[] hexStringToBytes(String hexString) {
		if (StringUtils.isEmpty(hexString)) {
			return null;
		}
		int length = hexString.length() / 2;
		byte[] d = new byte[length];
		int pos;
		for (int i = 0; i < length; i++) {
			pos = i * 2;
			d[i] = (byte) (charToByte(hexString.charAt(pos)) << 4 | charToByte(hexString.charAt(pos + 1)));
		}
		return d;
	}

	/**
	 * Convert char to byte
	 * 
	 * @param c
	 *            char
	 * @return byte
	 */
	private static byte charToByte(char c) {
		return (byte) (c < 58 ? c - 48 : c < 71 ? c - 55 : c - 87);
	}

	private static class HexAppender {

		private int offerSet = 0;
		private char[] charData;

		public HexAppender(int size) {
			charData = new char[size];
		}

		public void append(char a, char b) {
			charData[offerSet++] = a;
			charData[offerSet++] = b;
		}

		@Override
		public String toString() {
			return new String(charData, 0, offerSet);
		}
	}

	public static String bytesToHexString(byte[] src, int startWith) {
		HexAppender helper = new HexAppender((src.length - startWith) * 2);
		if (src == null || src.length <= BIZ_ZERO) {
			return null;
		}
		int v;
		char[] temp;
		for (int i = startWith; i < src.length; i++) {
			v = src[i] & 0xFF;
			temp = charArrays[v];
			helper.append(temp[0], temp[1]);
		}
		return helper.toString();
	}

	public static byte[] hex2byte(byte[] b) {
		if (b.length % 2 != 0)
			throw new IllegalArgumentException();
		byte[] b2 = new byte[b.length / 2];
		for (int n = 0; n < b.length; n += 2) {
			String item = new String(b, n, 2);
			b2[(n / 2)] = ((byte) Integer.parseInt(item, 16));
		}
		return b2;
	}
}
