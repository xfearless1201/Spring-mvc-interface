package com.cn.tianxia.pay.ddzf.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.TreeMap;
import org.apache.commons.lang.StringUtils;
import org.apache.http.message.BasicNameValuePair;

import com.alibaba.fastjson.JSONObject;

/**
 * 
 * 数字签名工具
 * 
 *
 */
public class SignUtils {

	/**
	 * 根据请求参数计算签名
	 * 
	 * @param nvps
	 *            请求参数集合
	 * @return
	 * @throws Exception
	 */
	public static String signData(List<BasicNameValuePair> nvps , String privateKey) throws Exception {
		TreeMap<String, String> tempMap = new TreeMap<String, String>();
		for (BasicNameValuePair pair : nvps) {
			if (StringUtils.isNotBlank(pair.getValue())) {
				tempMap.put(pair.getName(), pair.getValue());
			}
		}
		StringBuffer buf = new StringBuffer();
		for (String key : tempMap.keySet()) {
			buf.append(key).append("=").append((String) tempMap.get(key)).append("&");
		}
		String signatureStr = buf.substring(0, buf.length() - 1);
		// RSA1生产摘要
		String messageDigest = SHA1(signatureStr);
		String signData = RSAUtil.signByPrivate(messageDigest, privateKey, "UTF-8");
		System.out.println("请求数据：" + signatureStr + "&signature=" + signData);
		return signData;
	}

	/**
	 * 验证签名是否正确
	 * 
	 * @param str
	 *            响应数据，形如:key-value&key-value&signature=sign
	 * @return
	 */
	public static boolean verifySign(String str,String publicKey) {
		System.out.println("响应数据：" + str);
		JSONObject result = JSONObject.parseObject(str);
		StringBuffer buf = new StringBuffer();
		String signature = "";
		TreeMap<String, String> params = new TreeMap<String, String>();
		for(String key : result.keySet()){
			params.put(key, result.getString(key));
		}
		for(String key : params.keySet()){
			if(StringUtils.isEmpty(params.get(key))){
				continue;
			}
			if ("signature".equals(key)) {
				signature = params.get("signature");
			} else {
				buf.append(key).append("=").append(params.get(key)).append("&");
			}
		}
		String signatureStr = buf.substring(0, buf.length() - 1);
		System.out.println("验签数据：" + signatureStr);
		String messageDigest = SHA1(signatureStr);
		return RSAUtil.verifyByKeyPath(messageDigest, signature, publicKey, "UTF-8");
	}

	/**
	 * 计算MD5签名
	 * 
	 * @param nvps
	 * @return
	 */
	public static String signMD5(List<BasicNameValuePair> nvps) {
		TreeMap<String, String> tempMap = new TreeMap<String, String>();
		for (BasicNameValuePair pair : nvps) {
			if (StringUtils.isNotBlank(pair.getValue())) {
				tempMap.put(pair.getName(), pair.getValue());
			}
		}
		StringBuffer buf = new StringBuffer();
		for (String key : tempMap.keySet()) {
			buf.append(key).append("=").append((String) tempMap.get(key)).append("&");
		}
		String signatureStr = buf.substring(0, buf.length() - 1);
		return MD5Util.getMd5(signatureStr);
	}

	public static String SHA1(String decript) {
		try {
			MessageDigest digest = java.security.MessageDigest.getInstance("SHA-1");
			digest.update(decript.getBytes("UTF-8"));
			byte messageDigest[] = digest.digest();
			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			// 字节数组转换为 十六进制 数
			for (int i = 0; i < messageDigest.length; i++) {
				String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
				if (shaHex.length() < 2) {
					hexString.append(0);
				}
				hexString.append(shaHex);
			}
			return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
}
