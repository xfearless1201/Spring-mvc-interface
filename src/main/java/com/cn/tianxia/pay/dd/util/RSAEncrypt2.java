package com.cn.tianxia.pay.dd.util;

import java.io.BufferedReader; 

import java.io.IOException; 

import java.io.InputStream; 

import java.io.InputStreamReader; 

import java.security.InvalidKeyException; 

import java.security.Key;

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

import sun.misc.BASE64Decoder; 

import sun.misc.BASE64Encoder;

 

public class RSAEncrypt2 { 

    

   private static final String DEFAULT_PUBLIC_KEY= "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDYL0VZYb2SikrlYvYD5r0UZkk19vluq+ImpR3FzPm59hditqVFGlRMEX1makJ2u3ZSFOH+BeUEQutlWrIz83opiw7PNL7Aju6xPehmbEXis1iynxrTyaGIelKM01B1TNMG8pksWi4ESgDWXZf2CipDBa0CHIyuURFJ/e1fT7GIBwIDAQAB";

    

   private static final String DEFAULT_PRIVATE_KEY= "MIICxjBABgkqhkiG9w0BBQ0wMzAbBgkqhkiG9w0BBQwwDgQIpssZIAed/hcCAggA"+ "\r" +
			"MBQGCCqGSIb3DQMHBAhXlAP8prj0BQSCAoA3dxtyW1DrVx7D5msaE2TW9RAjFfge"+ "\r" +
			"E6MgSgbH+EWQBtq5LUFTUCimXSfcnNwsxVt/aCqccEXD9QnwBD3bzCoVpFXyoziC"+ "\r" +
			"99lliQrAPqV+F+6kNC35d90QgNvlHewtkUvHi3AsxaN8t2Ml3Hf2j7fgKNL/zD9S"+ "\r" +
			"82OiXTIpqNQTLAIRCzBPw6kBD7eEZoFP6gk+G/v6si/QrkPko82hlgey27HClK9C"+ "\r" +
			"5RMZkkls7RAxc6SFGJGiM1AsFVgYgSNEQMrIrHJqEWFEUQkRENBY0Md86QTh1iqJ"+ "\r" +
			"+wcMPyRkLBXm9yceFaQZKUDUYzLz3ZaSVeMsvgQveKeqdlXDHqCTtXwp1MyGR/La"+ "\r" +
			"phoCkmHFp93d7SwBHKZxZUQsEOFbQOOh9b7SLnjutJkE3T+ReE1rjueDv2zhXwEn"+ "\r" +
			"K5m7L0qXjsZrh7OfUkrH7FLwqzBEf0Qb4IXYW2mj9zIkyRI5rs7z/t+b60uMZ6b9"+ "\r" +
			"yF+dxTWhpB27B9cwxxIJo5lUPMtdU01IROr9tB51MwGpIuyZYwLoCKfrXWFJv22r"+ "\r" +
			"4XfDQ5wFL6rXRYfB4H3KBGjbrft2RwC71XLwWCsr1CK9qb7t48wwLaiavu1fbZ+j"+ "\r" +
			"Gu3NnOoS7xBJCMd41qWnhOg4/5e98FFGJOvqJPlmygAFhXwS5LJISB06FaDytrRQ"+ "\r" +
			"ZTlgf/xhslQvgSbAuPPTLLqwasE3nordxjy7eGSNwZYfLbfNKlAG3qrmldd5vH9P"+ "\r" +
			"B0/KnB3IVbb6/szX8S7qYppUqxkB6Luox7iq1+F7Cgy03/d3QVJrhM3oKL1Qyh53"+ "\r" +
			"MyfVQeV3CiH1tE9NdZvvpHBJ7cmRwIYUnzRALYhUvP8qRtjRk2S66Atb";
 

   /*private static final StringDEFAULT_PUBLIC_KEY="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCbrbo/JaPJTJLl+6hfZm7uuLIr"+"\r"

         +"t/hivaLfot32wq/nSzoSsYkoNk27Yy+n10ODoZ75/91Y8QoJKeoWe0Ik1H1DmMuw"+"\r"

         +"Ef3eBoBCFn+eNjgZq6SIVBCNEnUaS0STmWqGPFKRFJ1Ujd4rJQ1tGFG3z3v9Cw2b"+"\r"

         +"Kq41AAYMD7ZqLv2zfQIDAQAB"+"\r";

private static final StringDEFAULT_PRIVATE_KEY="MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAJutuj8lo8lMkuX7"+"\r"

       +"qF9mbu64siu3+GK9ot+i3fbCr+dLOhKxiSg2TbtjL6fXQ4Ohnvn/3VjxCgkp6hZ7"+"\r"

       +"QiTUfUOYy7AR/d4GgEIWf542OBmrpIhUEI0SdRpLRJOZaoY8UpEUnVSN3islDW0Y"+"\r"

       +"UbfPe/0LDZsqrjUABgwPtmou/bN9AgMBAAECgYA7MOS9mbwaj4jS9Mph63qGPadQ"+"\r"

       +"xBQmxt+klDI83m0vTgT4kpQJvAv2EZJPCKPc6pidiVFJvewI6+zbO2Y8xqYRc6Ut"+"\r"

       +"RvZKIVD2ECmJ7mvsCt2yJLP44j7Gu3xD3NL12dgv8kyZoqdIfHfFWKlI2sPizFVo"+"\r"

       +"IsO/vuT7ngC07uJkKQJBAM5EL0HhSR+t9R23oWk6mFy0tsh6POXWgpvVt2jAuwYP"+"\r"

       +"VDvwkbNTSdkh5oFNMiEUuN1w5fA6p1dh+Fe2ZJEcgdMCQQDBNwJx7UUzzSZPhA6N"+"\r"

       +"2YBVo09qHUCOWK4kEgNc32+k/f6/Rcx/ghL2USJv/d6r6Ne6DS32ix2we4ghUpzk"+"\r"

       +"DlNvAkEAjaICNSGFvYwMmLhDzGbgwh+Qtv5AnK18B5drljFo+pNCCtp4oYMaXU9K"+"\r"

       +"4RRpzI9XFS71pMwRpg3uogPq8D174wJBAJrZp4//BTNa+5VG1RNCeeQomoEFKLPs"+"\r"

       +"xsQJ28AXIrctCXnqStb2PJREukDfShKk7iAiZ+/r1sOWfaeXOj5cbOkCQGKATKxy"+"\r"

       +"ad5K+z2PvzKFMoqLK+uRJFS4RHdefUaYshfCdMmoklyFjtk53RJ4WHAcY5TLBvbz"+"\r"

       +"56WtrXK/09rNqfI="+"\r";*/

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

    * @return当前的私钥对象

    */ 

   public RSAPrivateKey getPrivateKey() { 

       return privateKey; 

   } 

 

   /**

    * 获取公钥

    * @return当前的公钥对象

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

           keyPairGen=KeyPairGenerator.getInstance("RSA"); 

       } catch (NoSuchAlgorithmException e) { 

          e.printStackTrace(); 

       } 

      keyPairGen.initialize(1024, new SecureRandom()); 

       KeyPair keyPair=keyPairGen.generateKeyPair(); 

       this.privateKey= (RSAPrivateKey) keyPair.getPrivate(); 

       this.publicKey= (RSAPublicKey)keyPair.getPublic(); 

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

     * @return是否成功

     * @throws Exception 

     */ 

    public void loadPrivateKey(InputStream in) throws Exception{ 

        try { 

            BufferedReader br= new BufferedReader(new InputStreamReader(in)); 

            String readLine=null; 

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

    public byte[] encrypt(RSAPublicKey publicKey, byte[] plainTextData) throws Exception{ 

        if(publicKey== null){ 

            throw new Exception("加密公钥为空, 请设置"); 

        } 

        Cipher cipher= null; 

        try { 

            cipher= Cipher.getInstance("RSA/ECB/PKCS1Padding"); 

            cipher.init(Cipher.ENCRYPT_MODE, publicKey); 

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

     * @return明文

     * @throws Exception 解密过程中的异常信息

     */ 

    public byte[] decrypt(RSAPrivateKey privateKey, byte[] cipherData) throws Exception{ 

        if (privateKey== null){ 

            throw new Exception("解密私钥为空, 请设置"); 

        } 

        Cipher cipher= null; 

        try { 

            cipher= Cipher.getInstance("RSA/ECB/PKCS1Padding"); 

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

     * @return十六进制内容

     */ 

    public static String byteArrayToString(byte[] data){ 

        StringBuilder stringBuilder= new StringBuilder(); 

        for (int i=0; i<data.length; i++){ 

            //取出字节的高四位 作为索引得到相应的十六进制标识符 注意无符号右移 

            stringBuilder.append(HEX_CHAR[(data[i] & 0xf0)>>> 4]); 

            //取出字节的低四位 作为索引得到相应的十六进制标识符 

           stringBuilder.append(HEX_CHAR[(data[i] &0x0f)]); 

            /*if (i<data.length-1){ 

               stringBuilder.append(' '); 

            }*/ 

        } 

        return stringBuilder.toString(); 

    } 

   

    /**

     * 得到密钥字符串（经过base64编码）

     * 

     * @return

     */ 

    public static String getKeyString(Key key) throws Exception { 

        byte[] keyBytes = key.getEncoded(); 

        String s = (new BASE64Encoder()).encode(keyBytes); 

        return s; 

    }

 

 

    public static void main(String[] args){ 

        RSAEncrypt2 rsaEncrypt= new RSAEncrypt2(); 

        //rsaEncrypt.genKeyPair(); 

 

        //加载公钥 

        try { 

           rsaEncrypt.loadPublicKey(RSAEncrypt2.DEFAULT_PUBLIC_KEY); 

            System.out.println("加载公钥成功"); 

        } catch (Exception e) { 

            System.err.println(e.getMessage()); 

            System.err.println("加载公钥失败"); 

        } 

 

        //加载私钥 

        try { 

           rsaEncrypt.loadPrivateKey(RSAEncrypt2.DEFAULT_PRIVATE_KEY); 

            System.out.println("加载私钥成功"); 

        } catch (Exception e) { 

            System.err.println(e.getMessage()); 

            System.err.println("加载私钥失败"); 

        } 

 

        //测试字符串 

        String encryptStr= "duhuawei"; 

 

        try { 

           BASE64Encoder encoder = new BASE64Encoder();

           BASE64Decoder decoder = new BASE64Decoder();
           
           

            //加密 

            byte[] cipher = rsaEncrypt.encrypt(rsaEncrypt.getPublicKey(),encryptStr.getBytes());

           

            //解密

            //cipher="fF7DHU9MpBOS0WFrmb7Xplq1ctrsNAPCOHLxmwSd+X/w1cVMQLaQHH3bJYx7mrVRRYzxhCmwOcA+jMXe5sDbGmMU7YJrQGyqLCpbERfqDUbU6bSiVO/lxWDW2cciSBTiffSOHxbJPGkK0SSROs9+leBObh7W9FqNRvH83GdFIq8=".getBytes();

          

            System.out.println("密文长度:"+ cipher.length);

            System.out.println(RSAEncrypt.byteArrayToString(cipher));

            String des=encoder.encode(cipher);
            
            System.out.println("密文如下："+encoder.encode(cipher));

            byte[] plainText = rsaEncrypt.decrypt(rsaEncrypt.getPrivateKey(),decoder.decodeBuffer(des)); 

            //System.out.println("明文长度:"+plainText.length); 

            //System.out.println(RSAEncrypt.byteArrayToString(plainText)); 

            System.out.println("解密后结果："+new String(plainText)); 

        } catch (Exception e) { 

            System.err.println(e.getMessage()); 

        } 

    } 

} 
