package com.cn.tianxia.pay.qyf.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import org.apache.commons.codec.binary.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public class ToolKit {
	static final char HEX_DIGITS[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
	// 支付公钥
//	public static final String PAY_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDBteAp6tB90/OG6C6M5RhgKHbGajBZSumfk1std7oihCo+ZOL70GNdyysjduo5jyMy11Sc2cVrl2xmw4oN6IPq2GvP3hSTFpOmo8wnIBcsBecQFcWI9bUOsJMXSyBYsHJrLHGzG+UAcFn/ZAVTWaI/7RRrhpfnWPo65dXpdFmsEQIDAQAB";
	// 代付公钥
	public static final String REMIT_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCS04Bl4sW5o+xSh62l86FLbJxY8Gf7ImT9117R"
												+ "Y3TeUlICjiBTkUUJn/q/jQhSppja1jCU02zoS4g5Jq7ZeFaxdDeNkWqvfF76YC7U7lE+S6b9Wv/k"
												+ "6w9UmvZYafTNbltszdMFY4coDHaL44grLRHMonMEa+gX1rt1WYORiaJLJwIDAQAB";
	// 商户私钥
//	public static final String PRIVATE_KEY = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAI571YkVYHVb4TvTtJxcVVByWRHF"
//										   + "5se8xDDZZvPA1HlU0tj7bYSdzZ4iluXtj3FKQFQjU4tNgbBaXQMHJQOKRCbOQUhODth1K0FHZtrT"
//										   + "01eVcXfEcsd0m608vhLjx87Rr6wzJjR+gpq4DT0mAQGxf4wHEPBu5GBZ5aIbwak5ODXBAgMBAAEC"
//										   + "gYAbvUIiURYZYwzjj+DOvC8j3U835ZZ7dmWfuQORGw6CnJ/7/F8i/XHlgohsNSbDAJiriMEgErPX"
//										   + "+I+5Ii/zk3yW4xEoqkHrHRZGJTGNP2VgwnF25Nr1mDfslI71DJqdZFnl7ZUQcEP3n/IzzvxNYFQ9"
//										   + "yhYAmxV849QycaNgunZ1AQJBAMXye/QX0aezBdvg0zTzNxTA6SHhfUzvGpVIL+2GVTDlmYsU46d2"
//										   + "nKOVwOEE20QYLfYiLBZMAqwmi/t8Mt9TYNECQQC4RUDGqVSNMi7e1seTljol/qP6HGLe3tujZY/n"
//										   + "mHQr2VYQFLnuF7EyBl41nQ7rX/s+OM9wnDgC/21UJBuvFUHxAkAaBIMyVCckaa1tdyGLpiQpQCnk"
//										   + "YCT+Bbdyw6g5Ch0MbkE+PKKnkjmIbtiJOwAu9RalcVxmGduIERD5Hxv4qpbhAkEAn/GUkRtnRYt6"
//										   + "fXfmEWfDHzmQsUa0VwkPkhtUtkxxAaKK/jhPTqeH6Yj3ewfRbGKKXG7JN9CRGaEGD5Or5+PGsQJB"
//										   + "AJnaN/AJdu+G09DnjsgU1uT8cfwqjztAWaV0ctRWkylOonCgo14/iBqEkDfeDNsda8oEknl42ZX/"
//										   + "UJgJMtPwx/g=";

	public static int blockSize = 128;
	// 非对称密钥算法
	public static final String KEY_ALGORITHM = "RSA";
	public final static String CHARSET = "UTF-8";

	/**
	 * 获取响应报文
	 * 
	 * @param in
	 * @return
	 */
	private static String getResponseBodyAsString(InputStream in) {
		try {
			BufferedInputStream buf = new BufferedInputStream(in);
			byte[] buffer = new byte[1024];
			StringBuffer data = new StringBuffer();
			int readDataLen;
			while ((readDataLen = buf.read(buffer)) != -1) {
				data.append(new String(buffer, 0, readDataLen, CHARSET));
			}
			System.out.println("响应报文=" + data);
			return data.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 提交请求
	 * 
	 * @param url
	 * @param params
	 * @return
	 */
	public static String request(String url, String params) {
		try {
			System.out.println("请求报文:" + url+"?"+params);
			URL urlObj = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setConnectTimeout(1000 * 5);
			conn.setRequestProperty("Charset", CHARSET);
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("Content-Length", String.valueOf(params.length()));
			OutputStream outStream = conn.getOutputStream();
			outStream.write(params.toString().getBytes(CHARSET));
			outStream.flush();
			outStream.close();
			return getResponseBodyAsString(conn.getInputStream());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * MD5加密
	 * 
	 * @param s
	 * @param encoding
	 * @return
	 */
	public final static String MD5(String s, String encoding) {
		try {
			byte[] btInput = s.getBytes(encoding);
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			mdInst.update(btInput);
			byte[] md = mdInst.digest();
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = HEX_DIGITS[byte0 >>> 4 & 0xf];
				str[k++] = HEX_DIGITS[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 转换成Json格式
	 * 
	 * @param map
	 * @return
	 */
	public static String mapToJson(Map<String, String> map) {
		Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
		StringBuffer json = new StringBuffer();
		json.append("{");
		while (it.hasNext()) {
			Map.Entry<String, String> entry = it.next();
			String key = entry.getKey();
			String value = entry.getValue();
			json.append("\"").append(key).append("\"");
			json.append(":");
			json.append("\"").append(value).append("\"");
			if (it.hasNext()) {
				json.append(",");
			}
		}
		json.append("}");
		System.out.println("mapToJson=" + json.toString());
		return json.toString();
	}

	/**
	 * 生成随机字符
	 * 
	 * @param num
	 * @return
	 */
	public static String randomStr(int num) {
		char[] randomMetaData = new char[] { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
				'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
				'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4',
				'5', '6', '7', '8', '9' };
		Random random = new Random();
		String tNonceStr = "";
		for (int i = 0; i < num; i++) {
			tNonceStr += (randomMetaData[random.nextInt(randomMetaData.length - 1)]);
		}
		return tNonceStr;
	}

	/**
	 * 公钥加密
	 * 
	 * @param data待加密数据
	 * @param key
	 *            密钥
	 * @return byte[] 加密数据
	 * @throws IOException 
	 */
	public static byte[] encryptByPublicKey(byte[] data, String publicKey) throws IOException {
//		byte[] key = Base64.getDecoder().decode(publicKey);
		byte[] key  =Base64.decodeBase64(publicKey);
		
		// 实例化密钥工厂
		KeyFactory keyFactory;
		try {
			keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
			// 密钥材料转换
			X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(key);
			// 产生公钥
			PublicKey pubKey = keyFactory.generatePublic(x509KeySpec);
			// 数据加密
			Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
			cipher.init(Cipher.ENCRYPT_MODE, pubKey);
			blockSize = cipher.getOutputSize(data.length) - 11;
			return doFinal(data, cipher);

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 私钥解密
	 * 
	 * @param data
	 *            待解密数据
	 * @param key
	 *            密钥
	 * @return byte[] 解密数据
	 * @throws IOException 
	 */
	public static byte[] decryptByPrivateKey(byte[] data, String privateKeyValue) throws IOException {
		
//		byte[] key = Base64.getDecoder().decode(privateKeyValue);
		byte[] key  =Base64.decodeBase64(privateKeyValue);
		try {
			// 取得私钥
			PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(key);
			KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
			// 生成私钥
			PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
			// 数据解密
			Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			blockSize = cipher.getOutputSize(data.length);
			return doFinal(data, cipher);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 加密解密共用核心代码，分段加密解密
	 * 
	 * @param decryptData
	 * @param cipher
	 * @return
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws IOException
	 */
	public static byte[] doFinal(byte[] decryptData, Cipher cipher)
			throws IllegalBlockSizeException, BadPaddingException, IOException {
		int offSet = 0;
		byte[] cache = null;
		int i = 0;
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		while (decryptData.length - offSet > 0) {
			if (decryptData.length - offSet > blockSize) {
				cache = cipher.doFinal(decryptData, offSet, blockSize);
			} else {
				cache = cipher.doFinal(decryptData, offSet, decryptData.length - offSet);
			}
			out.write(cache, 0, cache.length);
			i++;
			offSet = i * blockSize;
		}
		byte[] encryptedData = out.toByteArray();
		out.close();
		return encryptedData;
	}

	public static void main(String[] args) throws Exception {
	}
}
