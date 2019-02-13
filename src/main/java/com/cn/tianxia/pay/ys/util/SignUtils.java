package com.cn.tianxia.pay.ys.util;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
 

/**
 * 签名验签工具
 * @author chang
 *
 */
public class SignUtils {
	// static {
	// Security.addProvider(
	// new org.bouncycastle.jce.provider.BouncyCastleProvider());
	// }

	// 缓存公钥和私钥
	public static Map<String, Object> certMap = new java.util.concurrent.ConcurrentHashMap<String, Object>();

	/**
	 * 除去数组中的空值和签名参数
	 * 
	 * @param sArray
	 *            签名参数组
	 * @return 去掉空值与签名参数后的新签名参数组
	 */
	public static Map<String, String> paraFilter(Map<String, String> sArray) {

		Map<String, String> result = new HashMap<String, String>();

		if (sArray == null || sArray.size() <= 0) {
			return result;
		}

		for (String key : sArray.keySet()) {
			String value = sArray.get(key);
			if (value == null || StringUtils.isEmpty(value)
					|| key.equalsIgnoreCase("sign")) {
				continue;
			}
			result.put(key, value);
		}

		return result;
	}

	/**
	 * 遍历以及根据重新排序
	 * @param sortedParams
	 * @return
	 */
	public static String getSignContent(Map<String, String> sortedParams) {
		StringBuffer content = new StringBuffer();
		List<String> keys = new ArrayList<String>(sortedParams.keySet());
		Collections.sort(keys);
		int index = 0;
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			String value = sortedParams.get(key);
			if (StringUtils.areNotEmpty(key, value)) {
				content.append((index == 0 ? "" : "&") + key + "=" + value);
				index++;
			}
		}
		return content.toString();
	}
	
	/**
	 * 签名
	 * @param content
	 * @param charset
	 * @param pfxCertFileInputStream
	 * @return
	 * @throws Exception
	 */
	public static String rsaSign(String content, String charset,HttpSession session,
			InputStream pfxCertFileInputStream) throws Exception {
		YspayConfig ys=new YspayConfig("",session);
		try {
			System.out.println("进入签名方法：content[" + content + "], charset["
					+ charset + "]");
			PrivateKey priKey = getPrivateKeyFromPKCS12(
					ys.getPASSWORD_PARTNER_PKCS12(),
					pfxCertFileInputStream);

			java.security.Signature signature = java.security.Signature
					.getInstance(ys.getRSA_ALGORITHM());

			signature.initSign(priKey);

			if (StringUtils.isEmpty(charset)) {
				signature.update(content.getBytes());
			} else {
				signature.update(content.getBytes(charset));
			}

			byte[] signed = signature.sign();

			String sign = new String(Base64.encodeBase64(signed), charset);

			System.out.println("进入签名完：content[" + content + "], charset["
					+ charset + "], sign[" + sign + "]");

			return sign;
		} catch (Exception e) {
			System.out.println("签名失败：content[" + content + "], charset["
					+ charset + "]");
			throw new Exception("RSAcontent = " + content + "; charset = "
					+ charset, e);
		}
	}
	/**
	 * 把参数签名
	 * @param params
	 * @param charset
	 * @param pfxCertFileInputStream
	 * @return
	 * @throws Exception
	 */
	public static String rsaSign(Map<String, String> params, String charset,HttpSession session,
			InputStream pfxCertFileInputStream) throws Exception {
		String signContent = getSignContent(params);

		return rsaSign(signContent, charset,session,pfxCertFileInputStream);
	}

	public static boolean rsaCheckContent(
			InputStream publicCertFileInputStream, Map<String, String> params,HttpSession session,
			String sign, String charset) throws Exception {
		String content = StringUtils.createLinkString(SignUtils
				.paraFilter(params));

		return rsaCheckContent(publicCertFileInputStream, content, sign,session,
				charset);
	}

	
	/**
	 * 验签
	 * @param publicCertFileInputStream
	 * @param content
	 * @param sign
	 * @param charset
	 * @return
	 * @throws Exception
	 */
	public static boolean rsaCheckContent(
			InputStream publicCertFileInputStream, String content, String sign,HttpSession session,
			String charset) throws Exception {
		YspayConfig ys=new YspayConfig("",session);
		System.out.println("进入验证签名方法: content[" + content
				+ "], sign[" + sign + "], charset["
				+ charset + "]");
		boolean bFlag = false;
		try {
			java.security.Signature signetcheck = java.security.Signature
					.getInstance(ys.getRSA_ALGORITHM());
			signetcheck
					.initVerify(getPublicKeyFromCert(publicCertFileInputStream));
			signetcheck.update(content.getBytes(charset));
			if (signetcheck.verify(Base64.decodeBase64(sign.getBytes(charset)))) {
				//跑不进条件语句里面
				bFlag = true;
				System.out.println("解密成功");
				System.out.println("sign:"+Base64.decodeBase64(sign.getBytes(charset)).toString());
			}
			System.out.println("进入验证签名方法: content[" + content + "], sign["
					+ sign + "], charset[" + charset + "], result[" + bFlag
					+ "]");
		} catch (Exception e) {
			System.out.println("验证签名异常" + ": content[" + content + "], sign["
					+ sign + "], charset[" + charset + "]");
			throw new Exception("验证签名异常");
		}

		return bFlag;
	}

	/**
	 * 读取公钥，x509格式
	 * 
	 * @param password
	 * @param ins
	 * @return
	 * @throws Exception
	 * @see
	 */
	public static PublicKey getPublicKeyFromCert(InputStream ins)
			throws Exception {
		PublicKey pubKey = (PublicKey) certMap.get("PublicKey");
		if (pubKey != null) {
			return pubKey;
		}

		try {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			Certificate cac = cf.generateCertificate(ins);
			pubKey = cac.getPublicKey();
			certMap.put("PublicKey", pubKey);
		} catch (Exception e) {
			if (ins != null)
				ins.close();
			throw e;
		} finally {
			if (ins != null) {
				ins.close();
			}
		}

		return pubKey;
	}

	/**
	 * 读取PKCS12格式的key（私钥）pfx格式
	 * 
	 * @param password
	 * @param ins
	 * @return
	 * @throws Exception
	 * @see
	 */
	public static PrivateKey getPrivateKeyFromPKCS12(String password,
			InputStream ins) throws Exception {
		PrivateKey priKey = (PrivateKey) certMap.get("PrivateKey");
		if (priKey != null) {
			return priKey;
		}

		KeyStore keystoreCA = KeyStore.getInstance("PKCS12");
		try {
			// 读取CA根证书
			keystoreCA.load(ins, password.toCharArray());

			Enumeration<?> aliases = keystoreCA.aliases();
			String keyAlias = null;
			if (aliases != null) {
				while (aliases.hasMoreElements()) {
					keyAlias = (String) aliases.nextElement();
					// 获取CA私钥
					priKey = (PrivateKey) (keystoreCA.getKey(keyAlias,
							password.toCharArray()));
					if (priKey != null) {
						certMap.put("PrivateKey", priKey);
						break;
					}
				}
			}
		} catch (Exception e) {
			if (ins != null)
				ins.close();
			throw e;
		} finally {
			if (ins != null) {
				ins.close();
			}
		}

		return priKey;
	}

}
