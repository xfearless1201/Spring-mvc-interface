package com.cn.tianxia.pay.ddzf.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Enumeration;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * 
 * RSA算法工具类
 * 
 *
 */
public class RSAUtil {
	/**
	 * 算法类型
	 */
	public static final String SIGN_ALGORITHMS = "SHA1WithRSA";

	/**
	 * 计算签名�?
	 * 
	 * @param content
	 *            待签名数据
	 * @param input_charset
	 *            编码格式
	 * @param key
	 *            秘钥
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws Exception
	 */
	public static String sign(String content, String input_charset, Key key)
			throws UnsupportedEncodingException, Exception {
		Cipher cipher;
		try {
			Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
			cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] output = cipher.doFinal(content.getBytes(input_charset));
			return Base64.encode(output);
		} catch (NoSuchAlgorithmException e) {
			throw new Exception("无此加密算法");
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
			return null;
		} catch (InvalidKeyException e) {
			throw new Exception("加密公钥非法,请检查");
		} catch (IllegalBlockSizeException e) {
			throw new Exception("明文长度非法");
		} catch (BadPaddingException e) {
			throw new Exception("明文数据已损坏");
		}
	}

	/**
	 * 读取秘钥文件中的秘钥信息
	 * 
	 * @param filePath
	 *            秘钥文件路径
	 * @param charSet
	 *            编码格式
	 * @return
	 * @throws Exception
	 */
	public static String readFile(String filePath, String charSet) throws Exception {
		FileInputStream fileInputStream = new FileInputStream(filePath);
		try {
			FileChannel fileChannel = fileInputStream.getChannel();
			ByteBuffer byteBuffer = ByteBuffer.allocate((int) fileChannel.size());
			fileChannel.read(byteBuffer);
			byteBuffer.flip();
			return new String(byteBuffer.array(), charSet);
		} finally {
			fileInputStream.close();
		}

	}

	/**
	 * 获取秘钥
	 * 
	 * @param keyFilePath
	 *            秘钥文件路径
	 * @return
	 * @throws Exception
	 */
	public static String getKey(String keyFilePath) throws Exception {
		String content = readFile(keyFilePath, "UTF8");
		return content.replaceAll("\\-{5}[\\w\\s]+\\-{5}[\\r\\n|\\n]", "");
	}


	

	/**
	 * 得到私钥
	 * 
	 * @param key
	 *            密钥字符串（经过base64编码）
	 * @throws Exception
	 */
	public static PrivateKey getPrivateKey(String key) throws Exception {
		byte[] keyBytes = buildPKCS8Key(key);
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
		return privateKey;
	}

	/**
	 * 构建PKC秘钥
	 * 
	 * @param privateKey
	 * @return
	 * @throws IOException
	 */
	private static byte[] buildPKCS8Key(String privateKey) throws IOException {
		if (privateKey.contains("-----BEGIN PRIVATE KEY-----")) {
			return Base64.decode(privateKey.replaceAll("-----\\w+ PRIVATE KEY-----", ""));
		} else if (privateKey.contains("-----BEGIN RSA PRIVATE KEY-----")) {
			final byte[] innerKey = Base64.decode(privateKey.replaceAll("-----\\w+ RSA PRIVATE KEY-----", ""));
			final byte[] result = new byte[innerKey.length + 26];
			System.arraycopy(Base64.decode("MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKY="), 0, result, 0, 26);
			System.arraycopy(BigInteger.valueOf(result.length - 4).toByteArray(), 0, result, 2, 2);
			System.arraycopy(BigInteger.valueOf(innerKey.length).toByteArray(), 0, result, 24, 2);
			System.arraycopy(innerKey, 0, result, 26, innerKey.length);
			return result;
		} else {
			return Base64.decode(privateKey);
		}
	}

	/**
	 * 读取证书文件中的秘钥信息
	 * 
	 * @param pfxPath
	 *            证书文件地址
	 * @param password
	 *            密码
	 * @return
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws IOException
	 * @throws UnrecoverableKeyException
	 */
	public static KeyInfo getPFXPrivateKey(String pfxPath, String password) throws KeyStoreException,
			NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException {
		FileInputStream fis = new FileInputStream(pfxPath);
		KeyStore ks = KeyStore.getInstance("PKCS12");
		ks.load(fis, password.toCharArray());
		fis.close();
		Enumeration<String> enumas = ks.aliases();
		String keyAlias = null;
		if (enumas.hasMoreElements()) {
			keyAlias = enumas.nextElement();
		}
		KeyInfo keyInfo = new KeyInfo();
		PrivateKey prikey = (PrivateKey) ks.getKey(keyAlias, password.toCharArray());
		Certificate cert = ks.getCertificate(keyAlias);
		PublicKey pubkey = cert.getPublicKey();
		keyInfo.privateKey = prikey;
		keyInfo.publicKey = pubkey;
		return keyInfo;
	}

	/**
	 * 秘钥信息对象
	 * 
	 */
	public static class KeyInfo {
		PublicKey publicKey;
		PrivateKey privateKey;

		public PublicKey getPublicKey() {
			return publicKey;
		}

		public PrivateKey getPrivateKey() {
			return privateKey;
		}
	}

	/**
	 * 获取公钥对象
	 * 
	 * @param key
	 *            公钥�?
	 * @return
	 * @throws Exception
	 */
	public static PublicKey getPublicKey(String key) throws Exception {
		if (key == null) {
			throw new Exception("加密公钥为空, 请设置");
		}
		byte[] buffer = Base64.decode(key);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
		return keyFactory.generatePublic(keySpec);
	}
	
	/**
	 * 私钥计算RSA签名
	 * 
	 * @param content
	 *            签名数据
	 * @param privateKey
	 *            私钥
	 * @param input_charset
	 *            编码类型
	 * @return
	 * @throws Exception
	 */
	public static String signByPrivate(String content, PrivateKey privateKey, String input_charset) throws Exception {
		if (privateKey == null) {
			throw new Exception("加密私钥为空, 请设置");
		}
		java.security.Signature signature = java.security.Signature.getInstance(SIGN_ALGORITHMS);
		signature.initSign(privateKey);
		signature.update(content.getBytes(input_charset));
		return Base64.encode(signature.sign());
	}

	/**
	 * 私钥计算RSA签名
	 * 
	 * @param content
	 *            签名数据
	 * @param privateKey
	 *            私钥值
	 * @param input_charset
	 *            编码类型
	 * @return
	 * @throws Exception
	 */
	public static String signByPrivate(String content, String privateKey, String input_charset) throws Exception {
		if (privateKey == null) {
			throw new Exception("加密私钥为空, 请设置");
		}
		PrivateKey privateKeyInfo = getPrivateKey(privateKey);
		return signByPrivate(content, privateKeyInfo, input_charset);
	}

	/**
	 * 根据指定秘钥路径，验证签名是否正确
	 * 
	 * @param content
	 *            签名数据
	 * @param sign
	 *            签名值
	 * @param publicKeyPath
	 *            秘钥文件路径
	 * @param input_charset
	 *            编码类型
	 * @return
	 */
	public static boolean verifyByKeyPath(String content, String sign, String publicKey, String input_charset) {
		try {
			return verify(content, sign, publicKey/*getKey(publicKeyPath)*/, input_charset);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * RSA验签名检查
	 * 
	 * @param content
	 *            待签名数据
	 * @param sign
	 *            签名值
	 * @param publicKey
	 *            公钥
	 * @param input_charset
	 *            编码格式
	 * @return 验证成功，返回true，否则返回false
	 */
	public static boolean verify(String content, String sign, String publicKey, String input_charset) {
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			byte[] encodedKey = Base64.decode(publicKey);
			PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
			return verify(content, sign, pubKey, input_charset);
		} catch (Exception e) {
		}
		return false;

	}

	/**
	 * 验证签名是否正确
	 * 
	 * @param content
	 *            签名数据
	 * @param sign
	 *            签名值
	 * @param publicKey
	 *            公钥
	 * @param inputCharset
	 *            编码格式
	 * @return 验证成功，返回true，否则返回false
	 */
	public static boolean verify(String content, String sign, PublicKey publicKey, String inputCharset) {
		try {
			java.security.Signature signature = java.security.Signature.getInstance(SIGN_ALGORITHMS);
			signature.initVerify(publicKey);
			signature.update(content.getBytes(inputCharset));
			boolean bverify = signature.verify(Base64.decode(sign));
			return bverify;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
