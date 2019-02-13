package com.cn.tianxia.pay.as.util;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import com.cn.tianxia.pay.tx.util.Base64Utils;

public class Rsaencrypt {
	 /**
     * 加密算法RSA
     */
    public static final String KEY_ALGORITHM = "RSA";

    /**
     * 签名算法
     */
    public static final String SIGNATURE_ALGORITHM = "SHA1withRSA";

    /**
     * 获取公钥的key
     */
    private static final String PUBLIC_KEY = "RSAPublicKey";

    /**
     * 获取私钥的key
     */
    private static final String PRIVATE_KEY = "RSAPrivateKey";

    /**
     * RSA最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 117;

    /**
     * RSA最大解密密文大小
     */
    private static final int MAX_DECRYPT_BLOCK = 128;

    private static Signature signature;
    //encryptByPublicKey
    public static Signature getSignature() {
    	
		try {
			signature = Signature.getInstance(SIGNATURE_ALGORITHM);
			return signature;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
        return null;
    }

    /**
     * 设置私钥
     * @param privateKey
     * @throws Exception
     */
    public static void setSignature(String privateKey) throws Exception {
        byte[] keyBytes;
        keyBytes = Base64Utils.decode(privateKey);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PrivateKey privateK = keyFactory.generatePrivate(pkcs8KeySpec);
        Signature sign = Signature.getInstance(SIGNATURE_ALGORITHM);
        sign.initSign(privateK);
        signature = sign;
    }

    /**
     * 使用私钥进行签名
     * @param signStr
     * @param privateKey
     * @return
     * @throws Exception
     */
    public static String sign(String signStr) throws Exception {
    	System.out.println("signStr: " + signStr);
    	signature = getSignature();
        signature.update(signStr.getBytes());
        return Base64Utils.encode(signature.sign());
    }

    public static String signByMap(TreeMap<String, Object> map) {
        try {
            List<String> keys = new ArrayList<String>(map.keySet());
            StringBuilder prestrSB = new StringBuilder();
            for (int i = 0; i < keys.size(); i++) {
                String key = keys.get(i);
                if (i == keys.size() - 1) {// 拼接时，不包括最后一个&字符
                    prestrSB.append(key).append("=").append(map.get(key));
                } else {
                    prestrSB.append(key).append("=").append(map.get(key)).append("&");
                }
            }
            String signData = sign(prestrSB.toString().trim());
            return signData;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
