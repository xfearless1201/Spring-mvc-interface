package com.cn.tianxia.pay.ys.util;


import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;


/**
 * 注意该类是平台外给商户的des加密类和平台解密商户信息的方法 平台内部用 DesUtil工具类
 * 
 * @author chenliang
 * 
 */
public class SrcDesUtil {

	/**
	 * 加密扩展数据
	 * 
	 * @param extraData
	 * @return
	 */
	public static String encryptExtraData(String src, String extraData)
			throws  Exception{
		src = String.format("%8.8s", src);
		String out = "";
		if (StringUtils.isNotBlank(extraData)) {
			JDES jd = new JDES();
			jd.SetKey(src.getBytes());
			try {
				out = new String(Base64.encodeBase64(jd.doECBEncrypt(
						extraData.getBytes("GBK"),
						extraData.getBytes("GBK").length)));
			} catch (Exception e) {
				throw new RuntimeException("加密错误"+e.getMessage());
			}
		} else {
			out = extraData;
		}
		return out;
	}

	/**
	 * 解密扩展数据
	 * 
	 * @param extraData
	 * @return
	 */
	public static String decryptExtraData(String src, String encryptExtraData)
			throws Exception {
		src = String.format("%8.8s", src);
		String out = "";
		if (StringUtils.isNotBlank(encryptExtraData)) {
			JDES jd = new JDES();
			jd.SetKey(src.getBytes());
			try {
				byte[] encryptByte = Base64.decodeBase64(encryptExtraData);
				out = new String(jd.doECBDecrypt(encryptByte,
						encryptByte.length), "GBK");
			} catch (Exception e) {
				throw new RuntimeException("解密错误"+e.getMessage());
			}
		} else {
			out = encryptExtraData;
		}
		return out;
	}

	/**
	 * DES加密
	 * 
	 * @param
	 * @return
	 */
	public static String encryptData(String key, String data)
			throws Exception {
		key = String.format("%8.8s", key);
		String out = "";
		if (StringUtils.isNotBlank(data)) {
			JDES jd = new JDES();
			jd.SetKey(key.getBytes());
			try {
				out = new String(Base64.encodeBase64(jd.doECBEncrypt(
						data.getBytes("GBK"), data.getBytes("GBK").length)));
			} catch (Exception e) {
				throw new RuntimeException("加密错误"+e.getMessage());
			}
		} else {
			out = data;
		}
		return out;
	}

	/**
	 * DES解密
	 * 
	 * @param
	 * @return
	 */
	public static String decryptData(String key, String data)
			throws Exception {
		key = String.format("%8.8s", key);
		String out = "";
		if (StringUtils.isNotBlank(data)) {
			JDES jd = new JDES();
			jd.SetKey(key.getBytes());
			try {
				byte[] encryptByte = Base64.decodeBase64(data);
				out = new String(jd.doECBDecrypt(encryptByte,
						encryptByte.length), "GBK");
			} catch (Exception e) {
				throw new Exception("解密错误"+e.getMessage());
			}
		} else {
			out = data;
		}
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		//System.out.println(decryptExtraData("YS_test", "37vQSZaucnPpqinXg9fp9cjtzFpaYDoR="));
		
	}
}
