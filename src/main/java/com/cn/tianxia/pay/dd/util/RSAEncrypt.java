package com.cn.tianxia.pay.dd.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import sun.misc.BASE64Decoder;

public class RSAEncrypt {
	
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
	
//	private static final String DEFAULT_PUBLIC_KEY= "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCSCv7DeCqNShbibrMYSD5qoKyRhh4NRgoflSAskJLZt+epS/+QDWPEAxOGKXg4EN0Vh5SEzPBxQT4MlG3aX/i6aWc8B1+BeJJtW+mrCmNgCOPQ+rab2rjHOAYeKQAb6nxq5tVIwAZxHfQ8PvYD5ViIG7WnHEh4i0mV4jKADGva+wIDAQAB";
//	
//	private static final String DEFAULT_PRIVATE_KEY="MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAJIK/sN4Ko1KFuJusxhIPmqgrJGG"+ "\r"+
//														"Hg1GCh+VICyQktm356lL/5ANY8QDE4YpeDgQ3RWHlITM8HFBPgyUbdpf+LppZzwHX4F4km1b6asK"+ "\r"+
//														"Y2AI49D6tpvauMc4Bh4pABvqfGrm1UjABnEd9Dw+9gPlWIgbtaccSHiLSZXiMoAMa9r7AgMBAAEC"+ "\r"+
//														"gYA/ONlPH280WRWNEqUfZu72U/ZAzVGnJ2HsiC80e6XG8LEt/gN1hgZhbX9/PeaYRtyenGH0F5Aj"+ "\r"+
//														"Rz4oTf08Yv2WdbbV9ndx1UhsyILrkslJxbOou8r7JG2rx57kpTDaHWkO/MtJYbeIP3zJm1VhQy7U"+ "\r"+
//														"AeJ+bWcvjQBK8VFuvOn8iQJBAMuOLABU1sZ6ayFpk+10FZ2o8FITYSK+A2QPUOQSZg2G7dHzyKUY"+ "\r"+
//														"RTTIQ+qK07s0SOXcFBFetAWMnmYRwQiis28CQQC3q390F/SUtisht02W3ijQ8hwZo5Klownfiz3h"+ "\r"+
//														"Fx2VZMKkvKEvQGkB5TDAWLP7HIDSQ6ZDYBvP5uzy/QpbMBs1AkEAoGK3PNOcHusaOktr6S8MURtp"+ "\r"+
//														"r+HhKXS6Sz7eJ7Zvfr0P6dvB/oNvFDWvfcBBMEH0JlkP3tfV1IGF1Tqr9FfuNQJAKVqv48/Q/frW"+ "\r"+
//														"U00WZzW2MvWQZiVyS2EeQ/rx/9BYlN6PBNCD6kOyYP7drzFJtOyCBNW+hcUJ/hGcoElhH48BUQJA"+ "\r"+
//														"OfNifEbr47yS19fgTCdUVxg2ycvrLMKjOIP456r4MPCJDR71Xs7vsuvK1719GjpFywJ8JQGJFwdg"+ "\r"+
//														"348SLLgSmQ=="+ "\r";

	private static String key2 = "60D02AF787CB74FB";
	
	/**
	 * 私钥
	 */
	private RSAPrivateKey privateKey;

	/**
	 * 公钥
	 */
	private RSAPublicKey publicKey;
	
	/**
	 * 字节数据转字符串专用集合
	 */
	private static final char[] HEX_CHAR= {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
	

	/**
	 * 获取私钥
	 * @return 当前的私钥对象
	 */
	public RSAPrivateKey getPrivateKey() {
		return privateKey;
	}

	/**
	 * 获取公钥
	 * @return 当前的公钥对象
	 */
	public RSAPublicKey getPublicKey() {
		return publicKey;
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
	 * 从文件中输入流中加载公钥
	 * @param in 公钥输入流
	 * @throws Exception 加载公钥时产生的异常
	 */
	public void loadPublicKey(InputStream in) throws Exception{
		try {
			BufferedReader br= new BufferedReader(new InputStreamReader(in));
			String readLine= null;
			StringBuilder sb= new StringBuilder();
			while((readLine= br.readLine())!=null){
				if(readLine.charAt(0)=='-'){
					continue;
				}else{
					sb.append(readLine);
					sb.append('\r');
				}
			}
			loadPublicKey(sb.toString());
		} catch (IOException e) {
			throw new Exception("公钥数据流读取错误");
		} catch (NullPointerException e) {
			throw new Exception("公钥输入流为空");
		}
	}


	/**
	 * 从字符串中加载公钥
	 * @param publicKeyStr 公钥数据字符串
	 * @throws Exception 加载公钥时产生的异常
	 */
	public void loadPublicKey(String publicKeyStr) throws Exception{
		try {
			BASE64Decoder base64Decoder= new BASE64Decoder();
			byte[] buffer= base64Decoder.decodeBuffer(publicKeyStr);
			KeyFactory keyFactory= KeyFactory.getInstance("RSA");
			X509EncodedKeySpec keySpec= new X509EncodedKeySpec(buffer);
			this.publicKey= (RSAPublicKey) keyFactory.generatePublic(keySpec);
		} catch (NoSuchAlgorithmException e) {
			throw new Exception("无此算法");
		} catch (InvalidKeySpecException e) {
			throw new Exception("公钥非法");
		} catch (IOException e) {
			throw new Exception("公钥数据内容读取错误");
		} catch (NullPointerException e) {
			throw new Exception("公钥数据为空");
		}
	}

	/**
	 * 从文件中加载私钥
	 * @param keyFileName 私钥文件名
	 * @return 是否成功
	 * @throws Exception 
	 */
	public void loadPrivateKey(InputStream in) throws Exception{
		try {
			BufferedReader br= new BufferedReader(new InputStreamReader(in));
			String readLine= null;
			StringBuilder sb= new StringBuilder();
			while((readLine= br.readLine())!=null){
				if(readLine.charAt(0)=='-'){
					continue;
				}else{
					sb.append(readLine);
					sb.append('\r');
				}
			}
			loadPrivateKey(sb.toString());
		} catch (IOException e) {
			throw new Exception("私钥数据读取错误");
		} catch (NullPointerException e) {
			throw new Exception("私钥输入流为空");
		}
	}

	public void loadPrivateKey(String privateKeyStr) throws Exception{
		try {
			BASE64Decoder base64Decoder= new BASE64Decoder();
			byte[] buffer= base64Decoder.decodeBuffer(privateKeyStr);
			PKCS8EncodedKeySpec keySpec= new PKCS8EncodedKeySpec(buffer);
			KeyFactory keyFactory= KeyFactory.getInstance("RSA");
			this.privateKey= (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
		} catch (NoSuchAlgorithmException e) {
			throw new Exception("无此算法");
		} catch (InvalidKeySpecException e) {
			throw new Exception("私钥非法");
		} catch (IOException e) {
			throw new Exception("私钥数据内容读取错误");
		} catch (NullPointerException e) {
			throw new Exception("私钥数据为空");
		}
	}

	/**
	 * 加密过程
	 * @param publicKey 公钥
	 * @param plainTextData 明文数据
	 * @return
	 * @throws Exception 加密过程中的异常信息
	 */
	public byte[] encrypt( RSAPublicKey publicKey, byte[] plainTextData) throws Exception{
		if(publicKey== null){
			throw new Exception("加密私钥为空, 请设置");
		}
		Cipher cipher= null;
		try {
			byte[] raw = key2.getBytes("utf-8");
			SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
			cipher= Cipher.getInstance("RSA", new BouncyCastleProvider());
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
//			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
//			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
			byte[] output= cipher.doFinal(plainTextData);
			return output;
		} catch (NoSuchAlgorithmException e) {
			throw new Exception("无此加密算法");
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
			return null;
		}catch (InvalidKeyException e) {
			throw new Exception("加密公钥非法,请检查");
		} catch (IllegalBlockSizeException e) {
			throw new Exception("明文长度非法");
		} catch (BadPaddingException e) {
			throw new Exception("明文数据已损坏");
		}
	}

	/**
	 * 解密过程
	 * @param privateKey 私钥
	 * @param cipherData 密文数据
	 * @return 明文
	 * @throws Exception 解密过程中的异常信息
	 */
	public byte[] decrypt(RSAPrivateKey privateKey, byte[] cipherData) throws Exception{
		if (privateKey== null){
			throw new Exception("解密公钥为空, 请设置");
		}
		Cipher cipher= null;
		try {
			cipher= Cipher.getInstance("RSA", new BouncyCastleProvider());
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			byte[] output= cipher.doFinal(cipherData);
			return output;
		} catch (NoSuchAlgorithmException e) {
			throw new Exception("无此解密算法");
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
			return null;
		}catch (InvalidKeyException e) {
			throw new Exception("解密私钥非法,请检查");
		} catch (IllegalBlockSizeException e) {
			throw new Exception("密文长度非法");
		} catch (BadPaddingException e) {
			throw new Exception("密文数据已损坏");
		}		
	}

	
	/**
	 * 字节数据转十六进制字符串
	 * @param data 输入数据
	 * @return 十六进制内容
	 */
	public static String byteArrayToString(byte[] data){
		StringBuilder stringBuilder= new StringBuilder();
		for (int i=0; i<data.length; i++){
			//取出字节的高四位 作为索引得到相应的十六进制标识符 注意无符号右移
			stringBuilder.append(HEX_CHAR[(data[i] & 0xf0)>>> 4]);
			//取出字节的低四位 作为索引得到相应的十六进制标识符
			stringBuilder.append(HEX_CHAR[(data[i] & 0x0f)]);
			if (i<data.length-1){
				stringBuilder.append(' ');
			}
		}
		return stringBuilder.toString();
	}


	public static void main(String[] args){
		RSAEncrypt rsaEncrypt= new RSAEncrypt();
		rsaEncrypt.genKeyPair();

		//加载公钥
		try {
			rsaEncrypt.loadPublicKey(RSAEncrypt.DEFAULT_PUBLIC_KEY);
			System.out.println("加载公钥成功");
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.err.println("加载公钥失败");
		}

		//加载私钥
		try {
			rsaEncrypt.loadPrivateKey(RSAEncrypt.DEFAULT_PRIVATE_KEY);
			System.out.println("加载私钥成功");
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.err.println("加载私钥失败");
		}

		//测试字符串
		String encryptStr= "Test String chaijunkun";

		try {
			//加密
			byte[] cipher = rsaEncrypt.encrypt(rsaEncrypt.getPublicKey(), encryptStr.getBytes());
//			byte[] c=RSAEncrypt.byteArrayToString(cipher).substring(0, 117).getBytes();
//			System.out.println(c);
//			System.out.println(c.length);
			//解密
			byte[] plainText = rsaEncrypt.decrypt(rsaEncrypt.getPrivateKey(), cipher);
			System.out.println("密文长度:"+ new sun.misc.BASE64Encoder().encode(cipher));
			System.out.println(RSAEncrypt.byteArrayToString(cipher));
			System.out.println("明文长度:"+ plainText.length);
			System.out.println(RSAEncrypt.byteArrayToString(plainText));
			System.out.println(new String(plainText));
		System.out.println(new sun.misc.BASE64Encoder().encode(plainText));	 
		} catch (Exception e) {
			System.err.println(e.getMessage());//RSA块的数据太多
		}
	}
}