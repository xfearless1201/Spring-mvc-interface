package com.cn.tianxia.pay.sl.util;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * 
 * @ClassName:  SecurityUtil   
 * @Description:随乐支付签名工具类 
 * @author: Hardy
 * @date:   2018年8月22日 下午6:08:35   
 *     
 * @Copyright: 天下科技 
 *
 */
public class SecurityUtil {
	/**
	 * 签名
	 * @param sourceData 签名源数据
	 * @return 签名数据
	 * @throws Exception
	 */
	public static byte[] sign(byte sourceData[],String priKey) throws Exception {
		byte signMessage[] = null;
		try {
			//实例化系统签名工具类
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(hexString2ByteArr(priKey));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
			Signature signature = Signature.getInstance("SHA1withRSA");
			//私钥签名
			signature.initSign(privateKey);
			signature.update(sourceData);
			//签名
			signMessage = signature.sign();
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("签名失败");
		}
		return signMessage;
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
	
	
	/**
	 * 验签
	 * @param sourceData 签名源数据
	 * @param signMessage 签名数据
	 * @return 验签结果
	 * @throws Exception
	 */
	public static boolean verify(String sourceData,String signMessage,String pubKey) throws Exception {
		//参数
		byte data[] =  MD5.getDigest(sourceData.getBytes("UTF-8"));
		//签名
		byte sign[] = Base64.decode(signMessage.getBytes("UTF-8"));
		boolean verifySuccessed = false;
		try {
			Signature signature = Signature.getInstance("SHA1withRSA");
            KeyFactory keyFac = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFac.generatePublic(new X509EncodedKeySpec(hexString2ByteArr(pubKey)));
			signature.initVerify(publicKey);
			signature.update(data);
			verifySuccessed = signature.verify(sign);
		} catch (Exception e) {
			throw new Exception("签名不合法");
		}
		return verifySuccessed;
	}
}