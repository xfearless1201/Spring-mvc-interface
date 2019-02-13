package com.cn.tianxia.pay.xfzf.util;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import com.cn.tianxia.pay.rczf.util.Base64Utils;

import sun.misc.BASE64Decoder;

public class RSAUtil {

	// 用md5生成内容摘要，再用RSA的私钥加密，进而生成数字签名
	public static String getMd5Sign(String content, String privateKeyStr) throws Exception {
		PrivateKey privateKey = getPrivateKey(privateKeyStr);
		
		byte[] contentBytes = content.getBytes("utf-8");
		Signature signature = Signature.getInstance("MD5withRSA");
		signature.initSign(privateKey);
		signature.update(contentBytes);
		byte[] signs = signature.sign();
		return Base64.encode(signs);
	}

	private static  PrivateKey getPrivateKey(String key) throws Exception {
		byte[] keyBytes;
		keyBytes = (new BASE64Decoder()).decodeBuffer(key);
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
		return privateKey;
	}
	
	/** 
     * RSA validate signature
     * @param content: Signature data to be signed 
     * @param sign: Signature value
     * @param publicKey: merchant's public key
     * @param encode: Character set coding
     * @return boolean
     */  
     public static boolean validataSign(String content, String sign, String publicKey)  
     {  
         try   
         {  
             KeyFactory keyFactory = KeyFactory.getInstance("RSA");  
             byte[] encodedKey = Base64Utils.decode(publicKey);  
             PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));  
             java.security.Signature signature = java.security.Signature.getInstance("MD5withRSA");  
             signature.initVerify(pubKey);  
             signature.update( content.getBytes() );  
             boolean bverify = signature.verify( Base64Utils.decode(sign) );  
             return bverify;  
         }   
         catch (Exception e)   
         {  
             e.printStackTrace();  
         }  
           
         return false;  
     } 
	
	
	 //对用md5和RSA私钥生成的数字签名进行验证
    public static boolean verifySign(String content, String sign, String publicKeyStr) throws Exception {
    	PublicKey publicKey = getPublicKey(publicKeyStr);
        byte[] contentBytes = content.getBytes("utf-8");
        Signature signature = Signature.getInstance("MD5withRSA");
        signature.initVerify(publicKey);
        signature.update(contentBytes);
        return signature.verify(Base64.decode(sign));
    }
    
    private static PublicKey getPublicKey(String key) throws Exception {  
        byte[] keyBytes;  
        keyBytes = (new BASE64Decoder()).decodeBuffer(key);  
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);  
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");  
        PublicKey publicKey = keyFactory.generatePublic(keySpec);  
        return publicKey;  
  }  
}
