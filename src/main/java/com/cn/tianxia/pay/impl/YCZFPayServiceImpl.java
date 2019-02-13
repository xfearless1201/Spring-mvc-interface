package com.cn.tianxia.pay.impl;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.util.StringUtils;
import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.yczf.util.SendMsgUtil;
import com.cn.tianxia.pay.yczf.util.SignUtil;

import net.sf.json.JSONObject;

/**
 * 元潮支付
 * 
 * @author hb
 * @date 2018-06-05
 */
public class YCZFPayServiceImpl implements PayService {
	private final static Logger logger = LoggerFactory.getLogger(YCZFPayServiceImpl.class);

	// 参数分隔符
	private static final String URL_PARAM_CONNECT_FLAG = "&";
	// 需做Base64加密
	private static final String[] base64Keys = new String[] { "CodeUrl", "ImgUrl", "Token_Id", "PayInfo", "sPayUrl", "PayUrl", "NotifyUrl", "ReturnUrl" };
	// 字符编码
	private static final String CHARSET = "UTF-8";

	/** 请求地址 */
	private String payUrl ;//= "http://47.106.173.4:8990/mer/api";
	/** 收银台地址 */
	private String payUrlShow ;//= "http://47.106.173.4:8990/payshow/index";
	/** 签名方式 */
	private String SignMethod ;//= "MD5";
	/** 版本号 */
	private String Version ;//= "1.0";
	/** 交易编码 */
	private String TxCode ;//= "setupord";
	/** 商户号 */
	private String MerNo ;//= "70815929";
	/** 商户密钥 */
	private String Md5Key ;//= "c63422a3ce4b5f0d4fa58e1427ff3f3d";
	/** 商品名称 */
	private String PdtName ;//= "pay";
	/** 回调地址 */
	private String NotifyUrl ;//= "http://182.16.110.186:8080/XPJ/Notify/YczfNotify.do";

	public YCZFPayServiceImpl() {
	}

	public YCZFPayServiceImpl(Map<String, String> pmap) {
		if (pmap != null) {
			if (pmap.containsKey("payUrl")) {
				this.payUrl = pmap.get("payUrl");
			}
			if (pmap.containsKey("payUrlShow")) {
				this.payUrlShow = pmap.get("payUrlShow");
			}
			if (pmap.containsKey("SignMethod")) {
				this.SignMethod = pmap.get("SignMethod");
			}
			if (pmap.containsKey("Version")) {
				this.Version = pmap.get("Version");
			}
			if (pmap.containsKey("TxCode")) {
				this.TxCode = pmap.get("TxCode");
			}
			if (pmap.containsKey("MerNo")) {
				this.MerNo = pmap.get("MerNo");
			}
			if (pmap.containsKey("Md5Key")) {
				this.Md5Key = pmap.get("Md5Key");
			}
			if (pmap.containsKey("PdtName")) {
				this.PdtName = pmap.get("PdtName");
			}
			if (pmap.containsKey("NotifyUrl")) {
				this.NotifyUrl = pmap.get("NotifyUrl");
			}
		}
	}

	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		
		String reqUrl = requestPayUrl(payEntity, true);
		if(StringUtils.isEmpty(reqUrl)) {
			return getReturnJson("error", "", "");
		}
		return PayUtil.returnWYPayJson("success", "link", reqUrl, null, null);
	}
	
	/**
	 * ====收银台URL 拼接==== 
	 * 商户自己定制收银台,用户选择支付后，直接跳转入支付工具，在不同的支付环境支持如下 ProductId 
	 * 
	 * 说明如下 手机端
	 * 0601 微信扫码 
	 * 0602 支付宝扫码
	 * 0614 快捷H5支付 
	 * 0621 H5微信APP支付 
	 * 0622 H5支付宝APP支付 
	 *  
	 *  PC端 
	 *网银：
	 * -0611 B2C网银跳银行 还需传入DirectBankId 参考文档列表 
	 * -0612 B2C网银跳收银台  
	 * 
	 *扫码：
	 *  0601 微信扫码
	 * -0602 支付宝扫码 
	 * -0614 快捷H5支付 
	 */
	public static void main(String[] args) {
		/*PayEntity payEntity = new PayEntity();
		payEntity.setUsername("txkj");
		payEntity.setRefererUrl("http://www.baidu.com");
		payEntity.setAmount(101);
		payEntity.setOrderNo(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
		payEntity.setPayCode("0602");
		new YCZFPayServiceImpl().smPay(payEntity);*/
		String SignMethod="MD5";
		String Version ="1.0";
		String MerNo="70815929";
		String TxSN="123";
		String Amount="";
		String PdtName="";
		String Remark ="";
		String Status ="";
		String PlatTxSN="";
		String TxTime="";
		String PlatTxMsg ="";
		
		String Signature="";
		
	}

	@Override
	public JSONObject smPay(PayEntity payEntity) {
		double Amount = payEntity.getAmount();
		String userName = payEntity.getUsername();
		String TxSN = payEntity.getOrderNo().substring(0,30);// 商户交易流水号(唯一)
		String reqUrl = requestPayUrl(payEntity, false);
		if(StringUtils.isEmpty(reqUrl)) {
			return getReturnJson("error", "", "");
		}
		
		return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, Amount, TxSN, reqUrl);
	}
	
	private String requestPayUrl(PayEntity payEntity ,boolean isWy) {
		
		String TxSN = payEntity.getOrderNo().substring(0,30);// 商户交易流水号(唯一)
		double Amount = payEntity.getAmount();
		String ReturnUrl = payEntity.getRefererUrl();
		String payCode = payEntity.getPayCode();

		Map<String, String> paramsMap = new TreeMap<>();
		paramsMap.put("SignMethod", this.SignMethod); // 签名类型
		paramsMap.put("Version", this.Version); // 版本号
		paramsMap.put("TxCode", this.TxCode); // 交易编码
		paramsMap.put("MerNo", this.MerNo); // 商户号
		paramsMap.put("TxSN", TxSN);
		paramsMap.put("Amount", String.valueOf((int) (Amount * 100)));// 金额:单位:分
		paramsMap.put("PdtName", this.PdtName);// 商品名称
		paramsMap.put("ReturnUrl", ReturnUrl);// 同步通知URL
		paramsMap.put("NotifyUrl", this.NotifyUrl);// 异步通知URL
		paramsMap.put("ReqTime", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));// 请求时间 格式:yyyyMMddHHmmss

		String reqUrl = null;// 收银台请求地址
		try {
			// 1. 获取token
			setSignature(paramsMap);// 生成签名
			String plain = SignUtil.getURLParam(paramsMap, URL_PARAM_CONNECT_FLAG, true, null);
			logger.info("请求原始报文: {}", plain);

			String requestMsg = SignUtil.getWebForm(paramsMap, base64Keys, CHARSET, URL_PARAM_CONNECT_FLAG);
			String responseStr = SendMsgUtil.post(payUrl, requestMsg);
			logger.info("返回数据: {}", responseStr);

			Map<String, String> resMap = SignUtil.parseResponse(responseStr, base64Keys, URL_PARAM_CONNECT_FLAG, CHARSET);
			logger.info("URLDecoder处理后返回数据: {}", SignUtil.getURLParam(resMap, URL_PARAM_CONNECT_FLAG, true, null));

			if (verifySign(resMap)) {
				logger.info("签名验证结果成功");
				if ("00000".equalsIgnoreCase(resMap.get("RspCod")) || "1".equalsIgnoreCase(resMap.get("Status"))
						|| resMap.get("Token") != null) {
					String token = resMap.get("Token");
					logger.info("支付token: {}", token);
					// 收银台URL 拼接
					if(isWy) {
						reqUrl = buildPayUrl(paramsMap, token, "0612", null);
					}else {
						reqUrl = buildPayUrl(paramsMap, token, payCode, null);
					}
					logger.info("支付reqUrl= " + reqUrl);
				}
			}

		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
		}
		return reqUrl;
	}
	
	private String buildPayUrl(Map<String, String> paramsMap, String token, String productId, String directBankId) {
		try {
			logger.info("==收银台支付urd拼接==");
			if (token == null) {
				logger.info("支付token 为空");
				return null;
			}
			paramsMap.clear();
			paramsMap.put("SignMethod", this.SignMethod); // 签名类型
			paramsMap.put("Version", this.Version); // 版本号
			paramsMap.put("MerNo", this.MerNo); // 商户号
			paramsMap.put("Token", token);
			// 操作收银台的有效时间，到期后不可操作 格式:yyyyMMddHHmmss 不传：默认:请求支付链接10分钟后关闭
			// 加5分钟
			Date nowTime = new Date();
			Calendar ca = Calendar.getInstance();
			ca.setTime(nowTime);
			ca.add(Calendar.MINUTE, 5);

			paramsMap.put("ExpireTime", new SimpleDateFormat("yyyyMMddHHmmss").format(ca.getTime())); // 操作收银台的有效时间，到期后不可操作格式:yyyyMMddHHmmss
			paramsMap.put("UserId", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())); // 用户标识,用户在商户系统唯一标识
																								// 请传真实用户ID
			paramsMap.put("UserIdType", "USEID"); // 用户标识类型:USEID:用户ID PHONE:用户手机号 ID_CARD:用户身份证号

			if (productId != null) {
				paramsMap.put("ProductId", productId);// 产品ID:传入该参数后直接调用支付工具,不进入收银台
				if ("0611".equalsIgnoreCase(productId)) {
					if (directBankId == null) {
						logger.info("网银跳银行 直连银行编码不能为空");
						return null;
					}
					paramsMap.put("DirectBankId", directBankId);// 但产品ID设置为B2C跳银行时必填，值参考银行编码
				}
			}
			// 设置签名
			setSignature(paramsMap);
			// 报文明文
			String plain = SignUtil.getURLParam(paramsMap, URL_PARAM_CONNECT_FLAG, true, null);
			logger.info("请求原始报文: {}", plain);
			String url = this.payUrlShow + "?" + plain;
			logger.info("支付url: {}", url);
			return url;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * 结果返回
	 * 
	 * @param status
	 * @param qrCode
	 * @param msg
	 * @return
	 */
	private JSONObject getReturnJson(String status, String qrCode, String msg) {
		JSONObject json = new JSONObject();
		json.put("status", status);
		json.put("qrCode", qrCode);
		json.put("msg", msg);
		return json;
	}

	/**
	 * 设置签名
	 * 
	 * @param paramMap
	 */
	private void setSignature(Map<String, String> paramMap) {
		String signMethod = paramMap.get("SignMethod");
		Set<String> removeKey = new HashSet<String>();
		removeKey.add("SignMethod");
		removeKey.add("Signature");
		String signMsg = SignUtil.getSignMsg(paramMap, URL_PARAM_CONNECT_FLAG, removeKey);
		logger.info("需签名报文末尾未加密钥串: {}", signMsg);
		String signature = SignUtil.sign(signMethod, signMsg, this.Md5Key, CHARSET);
		logger.info("生成的签名: {}", signature);
		paramMap.put("Signature", signature);
	}

	/**
	 * 验证返回数据签名
	 * 
	 * @param paramMap
	 * @return
	 */
	private boolean verifySign(Map<String, String> paramMap) {
		// 计算签名
		Set<String> removeKey = new HashSet<String>();
		removeKey.add("SignMethod");
		removeKey.add("Signature");
		String signedMsg = SignUtil.getSignMsg(paramMap, URL_PARAM_CONNECT_FLAG, removeKey);
		String signMethod = (String) paramMap.get("SignMethod");
		String signature = (String) paramMap.get("Signature");
		logger.info("签名字符串: {}", signedMsg);
		return SignUtil.verifySign(signMethod, signedMsg, signature, this.Md5Key, CHARSET);
	}

	/**
	 * 验签
	 * 
	 * @param paramMap
	 * @return
	 */
	@Override
	public String callback(Map<String, String> paramMap) {
		// 计算签名
		Set<String> removeKey = new HashSet<String>();
		removeKey.add("SignMethod");
		removeKey.add("Signature");

		String signedMsg = SignUtil.getAsynNotifySignMsg(paramMap, URL_PARAM_CONNECT_FLAG, removeKey);
		String signMethod = (String) paramMap.get("SignMethod");
		String signature = (String) paramMap.get("Signature");
		logger.info("签名字符串: {}", signedMsg);
		boolean isRight = SignUtil.verifySign(signMethod, signedMsg, signature, this.Md5Key, CHARSET);
		if(isRight) {
			logger.info("回调验签成功");
			return "success";
		}
		logger.info("回调验签失败");
		return "fail";
	}
}
