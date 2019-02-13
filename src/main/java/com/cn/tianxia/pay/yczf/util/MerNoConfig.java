package com.cn.tianxia.pay.yczf.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 商户配置信息
 */
public class MerNoConfig {
	private static Logger logger = LoggerFactory.getLogger(MerNoConfig.class);

	// 参数分隔符
//	public static final String URL_PARAM_CONNECT_FLAG = "&";
	// 字符编码
//	public static final String CHARSET = "UTF-8";
	// 签名类型
//	public static final String SIGNMETHOD = "MD5";
	// 需做Base64加密
//	public static final String[] base64Keys = new String[] { "CodeUrl", "ImgUrl", "Token_Id", "PayInfo", "sPayUrl", "PayUrl", "NotifyUrl", "ReturnUrl" };
	// 密钥
//	public static final String MD5KEY = "c63422a3ce4b5f0d4fa58e1427ff3f3d";// 此为测试商户的秘钥,请自行替换为自己的秘钥
	// 商户号
//	public static final String MERNO = "70815929";// 此为测试商户号,请自行替换为自己的商户号
	//请求地址
	

		
	/**
	 * 设置签名
	 * 
	 * @param paramMap
	 */
	/*public static void setSignature(Map<String, String> paramMap) {
		String signMethod = paramMap.get("SignMethod");
		Set<String> removeKey = new HashSet<String>();
		removeKey.add("SignMethod");
		removeKey.add("Signature");
		String signMsg = SignUtil.getSignMsg(paramMap, MerNoConfig.URL_PARAM_CONNECT_FLAG, removeKey);
		logger.info("需签名报文末尾未加密钥串: {}", signMsg);
		String signature = SignUtil.sign(signMethod, signMsg, MerNoConfig.MD5KEY, MerNoConfig.CHARSET);
		logger.info("生成的签名: {}", signature);
		paramMap.put("Signature", signature);
	}*/

	/**
	 * 往渠道发送数据
	 * 
	 * @param url
	 *            通讯地址
	 * @param paramMap
	 *            发送参数
	 * @return 应答消息
	 */
	/*public static String sendMsg(String url, Map<String, String> paramMap) {
		try {
			// 转换参数格式
			String requestMsg = SignUtil.getWebForm(paramMap, MerNoConfig.base64Keys, MerNoConfig.CHARSET, MerNoConfig.URL_PARAM_CONNECT_FLAG);

			logger.info("通过URLEncode处理后发送的报文: {}", requestMsg);

			String rspMsg = SendMsgUtil.post(url, requestMsg);

			logger.info("返回数据: {}", rspMsg);

			return rspMsg;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}*/

	/**
	 * 验证返回数据签名
	 * 
	 * @param paramMap
	 * @return
	 */
/*	public static boolean verifySign(Map<String, String> paramMap) {
		// 计算签名
		Set<String> removeKey = new HashSet<String>();
		removeKey.add("SignMethod");
		removeKey.add("Signature");
		String signedMsg = SignUtil.getSignMsg(paramMap, MerNoConfig.URL_PARAM_CONNECT_FLAG, removeKey);
		String signMethod = (String) paramMap.get("SignMethod");
		String signature = (String) paramMap.get("Signature");
		logger.info("签名字符串: {}", signedMsg);
		return SignUtil.verifySign(signMethod, signedMsg, signature, MerNoConfig.MD5KEY, MerNoConfig.CHARSET);
	}*/

	/**
	 * 验签
	 * 
	 * @param paramMap
	 * @return
	 */
/*	public static boolean verifyAsynNotifySign(Map<String, String> paramMap) {
		// 计算签名
		Set<String> removeKey = new HashSet<String>();
		removeKey.add("SignMethod");
		removeKey.add("Signature");

		String signedMsg = SignUtil.getAsynNotifySignMsg(paramMap, MerNoConfig.URL_PARAM_CONNECT_FLAG, removeKey);
		String signMethod = (String) paramMap.get("SignMethod");
		String signature = (String) paramMap.get("Signature");
		logger.info("签名字符串: {}", signedMsg);
		return SignUtil.verifySign(signMethod, signedMsg, signature, MerNoConfig.MD5KEY, MerNoConfig.CHARSET);
	}*/
}