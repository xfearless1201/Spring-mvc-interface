package com.cn.tianxia.pay.dd.util;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * Java版3DES加密解密，适用于PHP版3DES加密解密(PHP语言开发的MCRYPT_3DES算法、MCRYPT_MODE_ECB模式、
 * PKCS7填充方式)
 * 
 * @author G007N
 */
public class DesBase64Tool {
	
private static final String DEFAULT_PUBLIC_KEY= "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDYL0VZYb2SikrlYvYD5r0UZkk19vluq+ImpR3FzPm59hditqVFGlRMEX1makJ2u3ZSFOH+BeUEQutlWrIz83opiw7PNL7Aju6xPehmbEXis1iynxrTyaGIelKM01B1TNMG8pksWi4ESgDWXZf2CipDBa0CHIyuURFJ/e1fT7GIBwIDAQAB";
	
	private static final String DEFAULT_PRIVATE_KEY="MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCtbHpyd9z5SB27"+ "\r" +
			  "sJbkK0I555xmoEJSihAO8fGLELEyHlPKODUdOQAdb31a707CjiZngv4KP/ClBgNO"+ "\r" +
			  "jJQ1car46++GcdIfnTFz19K0yq7hKLNucsqvSPoNZaw41gr1Wy3WKUFyuKRkcFDz"+ "\r" +
				"zaqAROvczSKm8AmDr7i1e/k/Nr9rhb6f4mZh9FkOByV2gcOPgI455lk0/xEPjUFn"+ "\r" +
				"HSQBav0zG3fvGi6MGdCJGVZXRnOnLsNudUTpNCFLZBPrDYKO3QQX0rDoFwgHEVh1"+ "\r" +
				"2VJo3YuHsdVG5WdVWuMLZFRRpCaYpB32bsflUcOhgcCtwmNZUxityuBiUGQiUHBT"+ "\r" +
				"H9iOE3P/AgMBAAECggEAKFwakxE6J/mUkd2jAC7hFsDEMU4SoUroOdIsuISF+Hf+"+ "\r" +
				"YA18rvRZBoCv173hwn16ipR9srz2sDEaC+nDhDyT4aJm+A79K8ZVf/Q2YUTaNiRW"+ "\r" +
				"vdmROgHtz7hdwExv+HHj1Pd898no2EcbT+IJUBi6G4TSD6/FRxRaFEJp20BOhCMW"+ "\r" +
				"CWCtfarxR4ViKZyjI+qHLBKAy6pcO6JS1VlhqpKxJa61RhYkcx6O/JblnhONMcM7"+ "\r" +
				"EmtVye5D9acrnnPHImVTV1L9+52ZtdMgj1zJ1P37ym8CQiJVeKlJbzBaOupjmj99"+ "\r" +
				"ARjLYKWXhbcMs+tR/ea9ojDwFbBZZ4BQmMPsw/Tn4QKBgQDVPyos8EzntuoJt9ay"+ "\r" +
				"rgza/cCuU73dL0LMmTCLeR0UjmrCR88YqlWL149mwyx7tvIRwZP20gxlkmc4bulX"+ "\r" +
				"WwQFTJyoK4Df9RsVr6ChrRCMgy0kPiHGY9wTvD4PE9jAOnLbjqSvpCL5JFkXS5d3"+ "\r" +
				"H4Pep+axS4pWKvibBhMNJRQe3QKBgQDQMWiMuUipF1q2ppXnlMBjS9RZEYqndJ/h"+ "\r" +
				"6Qtt1T1DV2p//w2tmCHTCrwNK50reppWrLSWT3vTjugF463pKy3UWeirGfvIRdic"+ "\r" +
				"1Su2aRbEu/Yk9rGkYmBcXBPIk7jCmxo+TNYr+B6bkhAdjQB1gQgIqQ/8wgmLURQn"+ "\r" +
				"3Q3GpTFaiwKBgA8CjDBvlao1uNGITCd1ktgTRm2+3T1lZzlJix42HzLkN2WHcm9V"+ "\r" +
				"BgJ64NAl6sqmfPF9A7I65L59iKBzRh9s+6J32wsHTOH2ubpUC2V17hVF4naLnQr+"+ "\r" +
				"2m3VMTUUe4xEUPNdXgAy+Hc69L5sBrRO1pRkkxtlqs4X5jnbOpoRQxJNAoGAO8oE"+ "\r" +
				"sW1Ajbb0SUIcCM6yVZHgYNflzuNySwmHS7qShnVjU3Dm97SZ+ia6DLo6v9LM4hll"+ "\r" +
				"H0miUQZYYeCaAD1zQumzJPgoU8KjS574l8EkVcl5DfJ+36uU6tFSqSarR9ANQFkG"+ "\r" +
				"pMfz2k+fKy1kNVqh+QK8YaVD4mudczlLjIHCY7MCgYEAoSH/GrX/+mav6n2Plb3K"+ "\r" +
				"kp3ZUjcIbj25kPbBmXShmF5I3xvxln3iiF0t/JNXJuPnizKDbjiiHJBtYtRwRLk1"+ "\r" +
				"iHCP+6duAmCttP8NYd3iUGnOudaRaD31AHFp9e62RJrD1z4MVqj4r5a1vVSjQonh"+ "\r" +
				"i+SuKuMBAsZYDAkcStIcHwo="+ "\r";
	/**
	 * 私钥
	 */
	private static RSAPrivateKey privateKey;

	/**
	 * 公钥
	 */
	private static RSAPublicKey publicKey;
	
	/**
	 * 获取私钥
	 * @return 当前的私钥对象
	 */
	public static RSAPrivateKey getPrivateKey() {
		return privateKey;
	}

	/**
	 * 获取公钥
	 * @return 当前的公钥对象
	 */
	public static RSAPublicKey getPublicKey() {
		return publicKey;
	}
	
	private static SecretKey secretKey = null;// key对象
	private static Cipher cipher = null; // 私鈅加密对象Cipher
	/*密钥为16的倍数*/
	private static String keyString = "AKlMU89D3FchIkhK";//密钥
	static {
		try {
			/*AES算法*/
			secretKey = new SecretKeySpec(keyString.getBytes(), "AES");//获得密钥
			System.out.println(secretKey);
			/*获得一个私鈅加密类Cipher，DESede-》AES算法，ECB是加密模式，PKCS5Padding是填充方式*/
			cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * 随机生成密钥对
	 */
	public void genKeyPair(){
		KeyPairGenerator keyPairGen= null;
		try {
			keyPairGen= KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		keyPairGen.initialize(1024, new SecureRandom());
		KeyPair keyPair= keyPairGen.generateKeyPair();
		this.privateKey= (RSAPrivateKey) keyPair.getPrivate();
		this.publicKey= (RSAPublicKey) keyPair.getPublic();
	}
	
	/**
	 * 加密
	 * 
	 * @param message
	 * @return
	 */
	public static String desEncrypt(String message) {
		String result = ""; // DES加密字符串
		String newResult = "";// 去掉换行符后的加密字符串
		try {
			cipher.init(Cipher.ENCRYPT_MODE, DesBase64Tool.getPublicKey()); // 设置工作模式为加密模式，给出密钥
			byte[] resultBytes = cipher.doFinal(message.getBytes("UTF-8")); // 正式执行加密操作
			BASE64Encoder enc = new BASE64Encoder();
			result = enc.encode(resultBytes);// 进行BASE64编码
			newResult = filter(result); // 去掉加密串中的换行符
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return newResult;
	}

	/**
	 * 解密
	 * 
	 * @param message
	 * @return
	 * @throws Exception
	 */
	public static String desDecrypt(String message) throws Exception {
		String result = "";
		try {
			BASE64Decoder dec = new BASE64Decoder();
			byte[] messageBytes = dec.decodeBuffer(message); // 进行BASE64编码
			System.out.println(DesBase64Tool.getPrivateKey());
			cipher.init(Cipher.DECRYPT_MODE, DesBase64Tool.getPrivateKey()); // 设置工作模式为解密模式，给出密钥
			byte[] resultBytes = cipher.doFinal(messageBytes);// 正式执行解密操作
			result = new String(resultBytes, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 去掉加密字符串换行符
	 * 
	 * @param str
	 * @return
	 */
	public static String filter(String str) {
		String output = "";
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < str.length(); i++) {
			int asc = str.charAt(i);
			if (asc != 10 && asc != 13) {
				sb.append(str.subSequence(i, i + 1));
			}
		}
		output = new String(sb);
		return output;
	}

	/**
	 * 加密解密测试
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		DesBase64Tool a = new DesBase64Tool();
		a.genKeyPair();
		try {
			String strText = "Hello world!";
			String deseResult = desEncrypt(strText);// 加密
			System.out.println("加密结果：" + deseResult);
			String desdResult = desDecrypt(deseResult);// 解密
			System.out.println("解密结果：" + desdResult);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
