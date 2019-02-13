package com.cn.tianxia.pay.ys.util;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @类名称：JDES
 * @类描述：DES加密工具类。支持对文件的加密
 * @作者：zhangy
 * @日期：2014-6-4 java的默认模式为ECB，默认填充方式为PKCS5Padding C#的默认模式为CBC,默认填充方式为PKCS7
 */
public class JDES {
    private static JDES instance;
    private byte[] m_desKey;

    public static JDES getInstanse() {
        if (instance == null) {
            instance = new JDES();
        }
        return instance;
    }

    /**
     * @return DES算法密钥
     */
    public static byte[] generateKey() {
        try {
            // DES算法要求有一个可信任的随机数源
            SecureRandom sr = new SecureRandom();
            // 生成一个DES算法的KeyGenerator对象
            KeyGenerator kg = KeyGenerator.getInstance("DES");
            kg.init(sr);
            // 生成密钥
            SecretKey secretKey = kg.generateKey();
            // 获取密钥数据
            byte[] key = secretKey.getEncoded();
            return key;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void SetKey(byte[] desKey) {
        this.m_desKey = desKey;
    }

    public void doECBEncrypt_File(String inFile, String outFile) throws Exception {
        Cipher cipher = getCipher("DES", "DES/ECB/PKCS5Padding", Cipher.ENCRYPT_MODE, null);
        // 打开需文件作为文件输入流
        FileInputStream fin = new FileInputStream(inFile);
        // 建立文件输出流
        FileOutputStream fout = new FileOutputStream(outFile);
        byte[] buf = new byte[1024];
        int nStart, num;
        byte[] decryptText;
        nStart = 0;
        while ((num = fin.read(buf, 0, buf.length)) != -1) {
            decryptText = cipher.update(buf, 0, num);
            fout.write(decryptText);
            nStart += num;
        }
        decryptText = cipher.doFinal();
        // 正式执行加密操作
        fout.write(decryptText);
        fout.close();
        fin.close();
    }

    public void doECBDecrypt_File(String inFile, String outFile) throws Exception {
        Cipher cipher = getCipher("DES", "DES/ECB/PKCS5Padding", Cipher.DECRYPT_MODE, null);
        // 打开需文件作为文件输入流
        FileInputStream fin = new FileInputStream(inFile);
        // 建立文件输出流
        FileOutputStream fout = new FileOutputStream(outFile);
        byte[] buf = new byte[1024];
        int nStart, num;
        byte[] decryptText;
        nStart = 0;
        while ((num = fin.read(buf, 0, buf.length)) != -1) {
            decryptText = cipher.update(buf, 0, num);
            fout.write(decryptText);
            nStart += num;
        }
        decryptText = cipher.doFinal();
        // 正式执行加密操作
        fout.write(decryptText);
        fout.close();
        fin.close();
    }

    // 一定要传入长度
    public byte[] doECBEncrypt(byte[] plainText, int len) throws Exception {
        Cipher cipher = getCipher("DES", "DES/ECB/PKCS5Padding", Cipher.ENCRYPT_MODE, null);
        // 现在，获取数据并加密
        byte data[] = plainText;/* 用某种方法获取数据 */
        // 正式执行加密操作
        byte encryptedData[] = cipher.doFinal(data, 0, len);
        return encryptedData;
    }

    public byte[] doECBEncryptZeroPadding(byte[] plainText, int len) throws Exception {
        Cipher cipher = getCipher("DES", "DES/ECB/NoPadding", Cipher.ENCRYPT_MODE, null);
        // 正式执行加密操作
        byte encryptedData[];
        byte data[];
        int nMod = len % 8;
        if (nMod > 0) {
            data = new byte[len + 8 - nMod];
            System.arraycopy(plainText, 0, data, 0, len);
        } else
            data = plainText;/* 用某种方法获取数据 */
        encryptedData = cipher.doFinal(data, 0, data.length);

        return encryptedData;
    }

    public byte[] doECBDecrypt(byte[] encryptText, int len) throws Exception {
        Cipher cipher = getCipher("DES", "DES/ECB/PKCS5Padding", Cipher.DECRYPT_MODE, null);

        // 现在，获取数据并解密
        byte encryptedData[] = encryptText;/* 获得经过加密的数据 */
        // 正式执行解密操作
        byte decryptedData[] = cipher.doFinal(encryptedData, 0, len);
        return decryptedData;
    }

    // cipherName = "DES/ECB/PKCS5Padding"
    public byte[] doECBDecryptZeroPadding(byte[] encryptText, int len) throws Exception {
        Cipher cipher = getCipher("DES", "DES/ECB/NoPadding", Cipher.DECRYPT_MODE, null);
        // 现在，获取数据并解密
        byte encryptedData[] = encryptText;/* 获得经过加密的数据 */
        // 正式执行解密操作
        byte decryptedData[] = cipher.doFinal(encryptedData, 0, len);
        return decryptedData;
    }

    // "DES" "DES/ECB/PKCS5Padding
    // cryptMode取值 加密Cipher.ENCRYPT_MODE,解密Cipher.DECRYPT_MODE
    protected Cipher getCipher(String factory, String cipherName, int cryptMode, byte[] iv)
            throws Exception {
        // DES算法要求有一个可信任的随机数源
        SecureRandom sr = new SecureRandom();
        byte rawKeyData[] = m_desKey; /* 用某种方法获取原始密匙数据 */
        // 从原始密匙数据创建一个DESKeySpec对象
        // DESKeySpec dks = new DESKeySpec(rawKeyData);//只能是单DES
        // DESedeKeySpec dks = new DESedeKeySpec(rawKeyData);//3DES
        // 创建一个密匙工厂，然后用它把DESKeySpec对象转换成
        // 一个SecretKey对象
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(factory);
        // SecretKey key = keyFactory.generateSecret(dks);
        SecretKey key = new SecretKeySpec(m_desKey, factory);
        // Cipher对象实际完成解密操作
        // using DES in ECB mode
        Cipher cipher = Cipher.getInstance(cipherName);
        // 用密匙初始化Cipher对象
        if (iv != null) {
            IvParameterSpec ips = new IvParameterSpec(iv);
            cipher.init(cryptMode, key, ips, sr);
        } else
            cipher.init(cryptMode, key, sr);

        return cipher;

    }

    // 一定要传入长度
    public byte[] do3DES_CBCEncrypt(byte[] plainText, int len, byte[] iv) throws Exception {
        Cipher cipher = getCipher("DESede", "DESede/CBC/PKCS5Padding", Cipher.ENCRYPT_MODE, iv);
        byte encryptedData[] = cipher.doFinal(plainText, 0, len);
        return encryptedData;
    }

    public byte[] do3DES_CBCDecrypt(byte[] encryptText, int len, byte[] iv) throws Exception {
        Cipher cipher = getCipher("DESede", "DESede/CBC/PKCS5Padding", Cipher.DECRYPT_MODE, iv);
        byte plainText[] = cipher.doFinal(encryptText, 0, len);
        return plainText;
    }

    public byte[] do3DES_ECBEncrypt(byte[] plainText, int len) throws Exception {
        Cipher cipher = getCipher("DESede", "DESede/ECB/PKCS5Padding", Cipher.ENCRYPT_MODE, null);
        byte encryptedData[] = cipher.doFinal(plainText, 0, len);
        return encryptedData;
    }

    public byte[] do3DES_ECBDecrypt(byte[] encryptText, int len) throws Exception {
        Cipher cipher = getCipher("DESede", "DESede/ECB/PKCS5Padding", Cipher.DECRYPT_MODE, null);
        byte plainText[] = cipher.doFinal(encryptText, 0, len);
        return plainText;

    }

    /**
     * 加密函数
     * 
     * @param data 加密数据
     * @param key 密钥
     * @return 返回加密后的数据
     */
    public byte[] CBCEncrypt(byte[] data, byte[] iv) {
        try {
            // 从原始密钥数据创建DESKeySpec对象
            DESKeySpec dks = new DESKeySpec(m_desKey);
            // 创建一个密匙工厂，然后用它把DESKeySpec转换成
            // 一个SecretKey对象
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(dks);
            // Cipher对象实际完成加密操作
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            // 若采用NoPadding模式，data长度必须是8的倍数
            // Cipher cipher = Cipher.getInstance("DES/CBC/NoPadding");
            // 用密匙初始化Cipher对象
            IvParameterSpec param = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, param);
            // 执行加密操作
            byte encryptedData[] = cipher.doFinal(data);
            return encryptedData;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解密函数
     * 
     * @param data 解密数据
     * @param key 密钥
     * @return 返回解密后的数据
     */
    public byte[] CBCDecrypt(byte[] data, byte[] iv) {
        try {
            // 从原始密匙数据创建一个DESKeySpec对象
            DESKeySpec dks = new DESKeySpec(m_desKey);
            // 创建一个密匙工厂，然后用它把DESKeySpec对象转换成
            // 一个SecretKey对象
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(dks);
            // using DES in CBC mode
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            // 若采用NoPadding模式，data长度必须是8的倍数
            // Cipher cipher = Cipher.getInstance("DES/CBC/NoPadding");
            // 用密匙初始化Cipher对象
            IvParameterSpec param = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, param);
            // 正式执行解密操作
            byte decryptedData[] = cipher.doFinal(data);
            return decryptedData;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] do3DES_ECBEncryptZeroPadding(byte[] plainText, int len) throws Exception {
        // 正式执行加密操作
        byte encryptedData[];
        byte data[];
        int nMod = len % 8;
        if (nMod > 0) {
            data = new byte[len + 8 - nMod];
            System.arraycopy(plainText, 0, data, 0, len);
        } else {
            data = plainText;/* 用某种方法获取数据 */
        }

        Cipher cipher = getCipher("DESede", "DESede/ECB/NoPadding", Cipher.ENCRYPT_MODE, null);
        encryptedData = cipher.doFinal(data, 0, data.length);
        return encryptedData;
    }

    public byte[] do3DES_ECBDecryptZeroPadding(byte[] encryptText, int len) throws Exception {
        Cipher cipher = getCipher("DESede", "DESede/ECB/NoPadding", Cipher.DECRYPT_MODE, null);
        byte plainText[] = cipher.doFinal(encryptText, 0, len);
        return plainText;
    }
}
