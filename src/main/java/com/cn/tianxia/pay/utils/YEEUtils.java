package com.cn.tianxia.pay.utils;

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
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.lang.StringUtils;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class YEEUtils {
    static final char HEX_DIGITS[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    // RSA非对称密钥算法
    public static final String KEY_ALGORITHM = "RSA";
    
    public static final String PRIVATE_KEY = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAKdY9jjC9mQnnHvYLDcPcA//ej19BMZpwZjkTrdS37DZu8OfNJAK1+u9ChD1DrzQvIjMIsO9+Ici1hx1syses9l1araelr3cl7o5nJZ8k8wzL/9yHbHugyVryt8PlBQccI2jM/ASsc7JXK4kgQCgCiMn1Z5KbOIpsJXoF6XXCo9pAgMBAAECgYEAl6DWsAWO5J6zfc8X+Oem/QfZxZ7iVbj7LkInRJn1jrMqGwzEvoLks3d8iHLSYKitOzHjigI2DpO09uJ6beaKw1XH7MCIAaktfyxkFiy+fIGSm8SDyaMO2R/OpMXG2OqEtksvMeTCfaR8aRa0qT8vBqRaSCf/2Q+g/jo+uMrUtxUCQQDZOzfWez/PVCGjhlSJ1SFmPgymRwW7NVx32DMNoFpv8usoEU1MTc6mAp6o2pZIHOOYBUvDcJ7WuoqjnjRrmWBDAkEAxTarxm1VlRKwXNr7M/24jG2o1opt72sia1QsrjWxAKQ95e3H6DOGOS7mLody28/j4R4xPnnshphutisHCjO84wJBAL7bVJfQ+aNDG7r8jtH6u7mE0sBUrihpHbD99v0F4e9x0kIF0ZA03Fbm/lcElf0NxLkQDhzMmgG3K4/Ns+jFfNkCQCPvkGF57sxZOXXSGVAh/YzpNojRdLLZzM3N6s5bzkMVqonJSFIRgXXOC/eSiuVu/lE5FTQTN3xXLmVXddLwFo0CQG5ienS/KWVx+dC29dp/Hlcqv5aiw32oYJ8HGmdZ/gtB0C36K3vcB2tGIsCUBm7XhPBVBzk9r9ZdAV0nhws7DpE=";

    public final static String CHARSET = "UTF-8";
    
    public static final String SIGNATURE_ALGORITHM_SHA1WITHRSA = "SHA1WithRSA";//SHA1WithRSA 或者 MD5withRSA

    /**
     * 获取响应报文
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
     */
    public static String request(String url, String params) {
        try {
            System.out.println("请求报文:" + params);
            URL urlObj = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setConnectTimeout(1000 * 10);
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
     * 生成数字字母随机字符串
     * @param num 随机字符串长度
     * @return 生成的随机字符串
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
     * @param data待加密数据
     * @param key 密钥
     * @return byte[] 加密数据
     * @throws IOException 
     */
    public static byte[] encryptByPublicKey(byte[] data, String publicKey) throws IOException {
        // 解码支付公钥
        byte[] key = new BASE64Decoder().decodeBuffer(publicKey);
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
            int blockSize = cipher.getOutputSize(data.length) - 11;
            return doFinal(data, cipher, blockSize);
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
     * @param data 待解密数据
     * @param key 密钥
     * @return byte[] 解密数据
     * @throws IOException 
     */
    public static byte[] decryptByPrivateKey(byte[] data, String privateKeyValue) throws IOException {
        byte[] key = new BASE64Decoder().decodeBuffer(privateKeyValue);
        try {
            // 取得私钥
            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(key);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            // 生成私钥
            PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
            // 数据解密
            Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            int blockSize = cipher.getOutputSize(data.length);
            return doFinal(data, cipher, blockSize);
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
     * @param decryptData 要加密的数据
     * @param cipher
     * @return
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws IOException
     */
    public static byte[] doFinal(byte[] decryptData, Cipher cipher, int blockSize)
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
    
    /**
     * 
     * @author ahao
     * @param mapParam
     *            参数map
     * @param kvMark
     *            key和value连接标识
     * @param domainMark
     *            域连接标识
     * @param isJoinKey
     *            是否连接key
     * @param isJoinEmptyValue
     *            是否连接空值
     * @return
     * @date 2017年9月30日 下午3:10:15
     */
    public static String mapToParam(Map<String, String> mapParam, String kvMark, String domainMark, boolean isJoinKey,
            boolean isJoinEmptyValue) {
        StringBuffer params = new StringBuffer();
        for (Map.Entry<String, String> p : mapParam.entrySet()) {
            String value = (String) p.getValue();
            boolean isEmptyValue = StringUtils.isBlank(value);
            boolean isJoinValue = isJoinEmptyValue ? true : !isEmptyValue;
            if (!isJoinValue) {
                continue;
            }
            if (isJoinKey) {
                params.append(p.getKey());
                params.append(kvMark);
            }
            if (isJoinValue) {
                params.append(p.getValue());
                params.append(domainMark);
            }
        }
        if (StringUtils.isNotBlank(domainMark) && params.length() > 0) {
            return params.substring(0, params.lastIndexOf(domainMark));
        }
        return params.toString();
    }
    
    /**
     * 用私钥对信息生成数字签名
     *
     * @param data       加密数据
     * @param privateKey 私钥
     * @return
     * @throws Exception
     */
    public static String RSA_SHA1(byte[] data, String privateKey) throws Exception {
        // 解密由base64编码的私钥
        byte[] keyBytes = new BASE64Decoder().decodeBuffer(privateKey);

        // 构造PKCS8EncodedKeySpec对象
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);

        // KEY_ALGORITHM 指定的加密算法
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);

        // 取私钥匙对象
        PrivateKey priKey = keyFactory.generatePrivate(pkcs8KeySpec);

        // 用私钥对信息生成数字签名
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM_SHA1WITHRSA);
        signature.initSign(priKey);
        signature.update(data);

        return new BASE64Encoder().encodeBuffer(signature.sign());
    }
}