package com.cn.tianxia.pay.xq.util;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author chaoyue
 *
 */
public class ClientSignature {

	static String initParam = "properties/gateway-keys.propertes";

	static Map paramsMap = new HashMap();
	static final String partnerKeyPath = "hnapay.partner.kayPath";
	static final String partnerStorepass = "hnapay.partner.storepass";
	static final String partnerAlias = "hnapay.partner.alias";
	static final String partnerPwd = "hnapay.partner.pwd";
	static final String hnapayGatewayPubKey = "hnapay.gateway.pubkey";
	static final String hnapayPartnerPubKey = "hnapay.partner.pubkey";
	static boolean flag = true;
	static String pkeyHeader = "&pkey=";

	/*static {
		Properties p = new Properties();
		try {
			p.load(ClientSignature.class.getClassLoader().getResourceAsStream(
					initParam));
			paramsMap.put("hnapay.partner.kayPath",
					p.get("hnapay.partner.kayPath"));
			paramsMap.put("hnapay.partner.storepass",
					p.get("hnapay.partner.storepass"));
			paramsMap
					.put("hnapay.partner.alias", p.get("hnapay.partner.alias"));
			paramsMap.put("hnapay.partner.pwd", p.get("hnapay.partner.pwd"));

			paramsMap.put("hnapay.gateway.pubkey",
					p.get("hnapay.gateway.pubkey"));
			paramsMap.put("hnapay.partner.pubkey",
					p.get("hnapay.partner.pubkey"));
		} catch (Exception e) {
			System.err.println("初始化配置信息异常：请检查配置文件路径以及内容");
			e.printStackTrace();
			flag = false;
		}
	}*/

	public static String genSignByMD5(String src, CharsetTypeEnum charsetType,String pkey)
			throws Exception {
		if (!flag) {
			System.err.println("初始化配置未成功,genSignByMD5无法执行");
			throw new Exception("初始化配置未成功,genSignByMD5无法执行");
		}

		if ((src == null) || ("".equals(src.trim()))) {
			System.err.println("src is empty,genSignByMD5无法执行");
			throw new Exception("src is empty,genSignByMD5无法执行");
		}

		src += pkeyHeader+pkey;
		String mac = null;
		try {
			
			System.out.println("加密前的串：" + src);
			mac = MD5BaseAlgorithms.getMD5Str(src);
		} catch (Exception e) {
			System.err.println("MD5 genSignByMD5 Exception");
			e.printStackTrace();
			throw new Exception(e);
		}

		return mac;
	}

	public static String genSignByRSA(String src, CharsetTypeEnum charsetType,
			InputStream in, String storepass, String keypass, String alias)
			throws Exception {
		if (!flag) {
			System.err.println("初始化配置未成功,genSignByRSA无法执行");
			throw new Exception("初始化配置未成功,genSignByRSA无法执行");
		}

		if ((src == null) || ("".equals(src.trim()))) {
			System.err.println("src is empty,genSignByRSA无法执行");
			throw new Exception("src is empty,genSignByRSA无法执行");
		}

		String mac = null;

		int hashCode = HashAlgorithms.PJWHash(src);
		mac = String.valueOf(hashCode);

		String signMsg = "";

		PrivateKey prikey = null;
		PublicKey pubKey = null;
		try {
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(in, storepass.toCharArray());

			Certificate c = ks.getCertificate(alias);
			pubKey = c.getPublicKey();

			prikey = (PrivateKey) ks.getKey(alias, keypass.toCharArray());

			RSAAlgorithms rsa = new RSAAlgorithms();

			signMsg = rsa.genSignature(
					mac.getBytes(charsetType.getDescription()), prikey);

			mac = signMsg;
		} catch (Exception e) {
			System.err.println("RSA genSignByRSA Exception");
			e.printStackTrace();
			throw new Exception(e);
		}
		return mac;
	}

	public static String genSignByRSA(String src, CharsetTypeEnum charsetType)
			throws Exception {
		if (!flag) {
			System.err.println("初始化配置未成功,genSignByRSA无法执行");
			throw new Exception("初始化配置未成功,genSignByRSA无法执行");
		}

		if ((src == null) || ("".equals(src.trim()))) {
			System.err.println("src is empty,genSignByRSA无法执行");
			throw new Exception("src is empty,genSignByRSA无法执行");
		}

		String mac = null;

		int hashCode = HashAlgorithms.PJWHash(src);
		mac = String.valueOf(hashCode);

		String signMsg = "";

		PrivateKey prikey = null;
		PublicKey pubKey = null;
		try {
			InputStream in = ClientSignature.class.getClassLoader()
					.getResourceAsStream(
							(String) paramsMap.get("hnapay.partner.kayPath"));
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(in, ((String) paramsMap.get("hnapay.partner.storepass"))
					.toCharArray());

			Certificate c = ks.getCertificate((String) paramsMap
					.get("hnapay.partner.alias"));
			pubKey = c.getPublicKey();

			prikey = (PrivateKey) ks.getKey((String) paramsMap
					.get("hnapay.partner.alias"), ((String) paramsMap
					.get("hnapay.partner.pwd")).toCharArray());

			RSAAlgorithms rsa = new RSAAlgorithms();

			signMsg = rsa.genSignature(
					mac.getBytes(charsetType.getDescription()), prikey);

			mac = signMsg;
		} catch (Exception e) {
			System.err.println("RSA genSignByRSA Exception");
			e.printStackTrace();
			throw new Exception(e);
		}
		return mac;
	}

	public static boolean verifySignatureByMD5(String src, String dit,
			CharsetTypeEnum charsetType) throws Exception {
		if (!flag) {
			System.err.println("初始化配置未成功,verifySignatureByMD5无法执行");
			throw new Exception("初始化配置未成功,verifySignatureByMD5无法执行");
		}

		if ((src == null) || ("".equals(src.trim()))) {
			System.err.println("初始化配置未成功,verifySignatureByMD5无法执行");
			throw new Exception("src is empty ,verifySignatureByMD5无法执行");
		}

		if ((dit == null) || ("".equals(dit.trim()))) {
			System.err.println("初始化配置未成功,verifySignatureByMD5无法执行");
			throw new Exception("arg is empty ,verifySignatureByMD5无法执行");
		}

		String mac = null;
		try {
			mac = MD5BaseAlgorithms.getMD5Str(src);
		} catch (Exception e) {
			System.err.println("MD5 验签出现异常");
			e.printStackTrace();
			throw new Exception(e);
		}

		return dit.equals(mac);
	}

	public static boolean verifySignatureByRSA(String src, String dit,
			CharsetTypeEnum charsetType, String publicKey) throws Exception {
		if (!flag) {
			System.err.println("初始化配置未成功,verifySignatureByRSA无法执行");
			throw new Exception("初始化配置未成功,verifySignatureByRSA无法执行");
		}
		if ((src == null) || ("".equals(src.trim()))) {
			System.err.println("初始化配置未成功,verifySignatureByRSA无法执行");
			throw new Exception("src is empty ,verifySignatureByRSA无法执行");
		}
		if ((dit == null) || ("".equals(dit.trim()))) {
			System.err.println("初始化配置未成功,verifySignatureByRSA无法执行");
			throw new Exception("dit is empty ,verifySignatureByRSA无法执行");
		}

		boolean result = false;
		try {
			int hashCode = HashAlgorithms.PJWHash(src);
			String hashSrc = hashCode + "";
			RSAAlgorithms sign = new RSAAlgorithms();

			result = sign.verifySignature(
					hashSrc.getBytes(charsetType.getDescription()), dit,
					ByteArrayUtil.toByteArray(publicKey));
		} catch (Exception e) {
			System.err.println("验证签名出现异常：请检查输入参数");
			e.printStackTrace();
			throw new Exception(e);
		}

		return result;
	}

	public static boolean verifySignatureByRSA(String src, String dit,
			CharsetTypeEnum charsetType) throws Exception {
		if (!flag) {
			System.err.println("初始化配置未成功,verifySignatureByRSA无法执行");
			throw new Exception("初始化配置未成功,verifySignatureByRSA无法执行");
		}
		if ((src == null) || ("".equals(src.trim()))) {
			System.err.println("初始化配置未成功,verifySignatureByRSA无法执行");
			throw new Exception("src is empty ,verifySignatureByRSA无法执行");
		}
		if ((dit == null) || ("".equals(dit.trim()))) {
			System.err.println("初始化配置未成功,verifySignatureByRSA无法执行");
			throw new Exception("dit is empty ,verifySignatureByRSA无法执行");
		}

		boolean result = false;
		try {
			int hashCode = HashAlgorithms.PJWHash(src);
			String hashSrc = hashCode + "";
			RSAAlgorithms sign = new RSAAlgorithms();

			result = sign.verifySignature(hashSrc.getBytes(charsetType
					.getDescription()), dit, ByteArrayUtil
					.toByteArray((String) paramsMap
							.get("hnapay.gateway.pubkey")));
		} catch (Exception e) {
			System.err.println("验证签名出现异常：请检查输入参数");
			e.printStackTrace();
			throw new Exception(e);
		}

		return result;
	}
}
