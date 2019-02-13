package com.cn.tianxia.pay.lxzf.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.DSAPrivateKeySpec;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

import javax.crypto.Cipher;

public class RsaUtil{
	
    private static final String ALG_RSA = "RSA";
    private static final String ALG_DSA = "DSA";
    
    public static Map<String, String> genKeyPair()
    {
        Map<String, String> map = null;
        try {
            map = new HashMap<String, String>();
            KeyPairGenerator keyPair = KeyPairGenerator.getInstance("RSA");
            keyPair.initialize(1024);
            KeyPair kp = keyPair.generateKeyPair();
            String pubKeyStr = byteArr2HexString(kp.getPublic().getEncoded());
            String priKeyStr = byteArr2HexString(kp.getPrivate().getEncoded());

            map.put("publicKey", pubKeyStr);
            map.put("privateKey", priKeyStr);
        } catch (Exception e) {
            e.printStackTrace();
            map = null;
        }
        return map;
    }

    public static String sign(String data, String privateKey)
    {
        String sign = null;
        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(hexString2ByteArr(privateKey));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey priKey = keyFactory.generatePrivate(keySpec);
            Signature si = Signature.getInstance("SHA1WithRSA");
            si.initSign(priKey);
            si.update(data.getBytes("UTF-8"));
            byte[] dataSign = si.sign();
            sign = byteArr2HexString(dataSign);
        } catch (Exception e) {
            e.printStackTrace();
            sign = null;
        }
        return sign;
    }

    public static boolean verify(String data, String sign, String publicKey)
    {
        boolean succ = false;
        try {
            Signature verf = Signature.getInstance("SHA1WithRSA");
            KeyFactory keyFac = KeyFactory.getInstance("RSA");
            PublicKey puk = keyFac.generatePublic(new X509EncodedKeySpec(hexString2ByteArr(publicKey)));
            verf.initVerify(puk);
            verf.update(data.getBytes("UTF-8"));
            succ = verf.verify(hexString2ByteArr(sign));
        } catch (Exception e) {
            e.printStackTrace();
            succ = false;
        }
        return succ;
    }

    public static String encrypt(String data, String publicKey)
    {
        String encryptData = null;
        try {
            KeyFactory keyFac = KeyFactory.getInstance("RSA");
            PublicKey pubKey = keyFac.generatePublic(new X509EncodedKeySpec(hexString2ByteArr(publicKey)));

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);
            byte[] result = cipher(data.getBytes("UTF-8"), cipher, getBlockSize(pubKey) - 11);
            encryptData = byteArr2HexString(result);
        } catch (Exception e) {
            e.printStackTrace();
            encryptData = null;
        }
        return encryptData;
    }

    public static String decrypt(String encryptedData, String privateKey)
    {
        String decryptData = null;
        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(hexString2ByteArr(privateKey));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey priKey = keyFactory.generatePrivate(keySpec);

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, priKey);
            byte[] result = cipher(hexString2ByteArr(encryptedData), cipher, getBlockSize(priKey));
            decryptData = new String(result, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            decryptData = null;
        }
        return decryptData;
    }

    public static String byteArr2HexString(byte[] bytearr)
    {
        if (bytearr == null) {
            return "null";
        }
        StringBuffer sb = new StringBuffer();

        for (int k = 0; k < bytearr.length; k++) {
            if ((bytearr[k] & 0xFF) < 16) {
                sb.append("0");
            }
            sb.append(Integer.toString(bytearr[k] & 0xFF, 16));
        }
        return sb.toString();
    }

    public static byte[] hexString2ByteArr(String hexString)
    {
        if ((hexString == null) || (hexString.length() % 2 != 0)) {
            return new byte[0];
        }

        byte[] dest = new byte[hexString.length() / 2];

        for (int i = 0; i < dest.length; i++) {
            String val = hexString.substring(2 * i, 2 * i + 2);
            dest[i] = ((byte)Integer.parseInt(val, 16));
        }
        return dest;
    }
	
	    private static byte[] cipher(byte[] data, Cipher cipher, int blockSize) throws Exception {
        final ByteArrayInputStream in = new ByteArrayInputStream(data);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final byte[] cache = new byte[blockSize];
        while (true) {
            final int r = in.read(cache);
            if (r < 0) {
                break;
            }
            final byte[] temp = cipher.doFinal(cache, 0, r);
            out.write(temp, 0, temp.length);
        }
        return out.toByteArray();
    }

    private static int getBlockSize(final Key key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        final String alg = key.getAlgorithm();
        final KeyFactory keyFactory = KeyFactory.getInstance(alg);
        if (key instanceof PublicKey) {
            final BigInteger prime;
            if (ALG_RSA.equals(alg)) {
                prime = keyFactory.getKeySpec(key, RSAPublicKeySpec.class).getModulus();
            } else if (ALG_DSA.equals(alg)) {
                prime = keyFactory.getKeySpec(key, DSAPublicKeySpec.class).getP();
            } else {
                throw new NoSuchAlgorithmException("不支持的解密算法：" + alg);
            }
            return prime.toString(2).length() / 8;
        } else if (key instanceof PrivateKey) {
            final BigInteger prime;
            if (ALG_RSA.equals(alg)) {
                prime = keyFactory.getKeySpec(key, RSAPrivateKeySpec.class).getModulus();
            } else if (ALG_DSA.equals(alg)) {
                prime = keyFactory.getKeySpec(key, DSAPrivateKeySpec.class).getP();
            } else {
                throw new NoSuchAlgorithmException("不支持的解密算法：" + alg);
            }
            return prime.toString(2).length() / 8;
        } else {
            throw new RuntimeException("不支持的密钥类型：" + key.getClass());
        }
    }

    public static void main(String[] args){
        Map<String,String> map = genKeyPair();
        String publicKey = map.get("publicKey");
        String privateKey = map.get("privateKey");
        System.out.println("公钥：");
        System.out.println(publicKey);
        System.out.println("私钥：");
        System.out.println(privateKey);
        String signResult = sign("key1=value1&key2=value2",privateKey);
        System.out.println("签名结果："+signResult);
        System.out.println("验签结果："+verify("key1=value1&key2=value2",signResult,publicKey));
        String encStr = encrypt("key1=value1&key2=value2",publicKey);
        System.out.println("加密结果："+encStr);
        System.out.println("解密结果："+decrypt(encStr,privateKey));
    }
}