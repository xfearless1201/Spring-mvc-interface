package com.cn.tianxia.pay.yjh.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.cn.tianxia.pay.yjh.util.HttpUtil.HttpResult;

/**
 * 支付api，仅供参考
 * 
 * @author devin <br/>
 *         2017年8月26日
 */
public class PayApi {

	/** 签名参数 */
	private static final String SIGN_PARA = "sign";

	/** 接口地址 */
	private static final String API_PAY = "/pay";
	/** 获取二维码 */
	private static final String API_PAY_QR = API_PAY + "/qr.do";
	/** 获取二维码 */
	private static final String API_PAY_H5 = API_PAY + "/h5.do";
	/** 网银支付(注意这里是.action结尾) */
	private static final String API_PAY_EBANK = API_PAY + "/ebank.action";
	/** 查询订单状态 */
	private static final String API_PAY_STATUS = API_PAY + "/status.do";
	/** 通知校验 */
	private static final String API_PAY_VERIFY = API_PAY + "/verify.do";
	/** 退款 */
	private static final String API_PAY_REFUND = API_PAY + "/refund.do";
	/** 代付 */
	private static final String API_PAY_WITHDRAW = API_PAY + "/withdraw.do";
	/** 查询账户余额 */
	private static final String API_PAY_BALANCE = API_PAY + "/balance.do";

	/**
	 * 扫码支付获取二维码
	 * 
	 * @param params
	 *            请求参数
	 * @return
	 */
	public static ApiResult getQrCodeUrl(Map<String, String> params) {
		params.put("notifyUrl", Config.NOTIFY_URL); // 异步通知地址
		return post(API_PAY_QR, params);
	}

	/**
	 * 扫码h5预支付url
	 * 
	 * @param params
	 *            请求参数
	 * @return
	 */
	public static ApiResult getH5PrepayUrl(Map<String, String> params) {
		params.put("returnUrl", Config.RETURN_URL); // 页面同步跳转通知地址
		params.put("notifyUrl", Config.NOTIFY_URL); // 异步通知地址
		return post(API_PAY_H5, params);
	}

	/**
	 * 网银支付
	 * 
	 * @param params
	 * @return
	 */
	public static String createEbankRedirectForm(Map<String, String> params) {
		params.put("returnUrl", Config.RETURN_URL); // 页面同步跳转通知地址
		params.put("notifyUrl", Config.NOTIFY_URL); // 异步通知地址

		// 添加公共参数和签名
		addBaseParams(params);
		sign(params);

		// 拼接表单
		StringBuffer sbHtml = new StringBuffer("<form id=\"subForm\"").append(" name=\"alipaysubmit\"")
				.append(" action=\"" + Config.SERVICE_URL + API_PAY_EBANK + "\"").append(" method=\"POST\">");
		Entry<String, String> entry = null;
		for (Iterator<Entry<String, String>> itr = params.entrySet().iterator(); itr.hasNext();) {
			entry = itr.next();
			sbHtml.append(
					"<input type=\"hidden\" name=\"" + entry.getKey() + "\" value=\"" + entry.getValue() + "\"/>");
		}

		// submit按钮控件请不要含有name属性
		sbHtml.append("<input type=\"submit\" value=\"确认\" style=\"display:none;\"></form>");
		sbHtml.append("<script>document.forms['subForm'].submit();</script>");
		return sbHtml.toString();
	}

	/**
	 * 查询订单的支付状态
	 * 
	 * @param params
	 *            请求参数
	 * @return
	 */
	public static ApiResult queryOrder(Map<String, String> params) {
		return post(API_PAY_STATUS, params);
	}

	/**
	 * 退款
	 * 
	 * @param params
	 *            请求参数
	 * @return
	 */
	public static ApiResult refund(Map<String, String> params) {
		return post(API_PAY_REFUND, params);
	}

	/**
	 * 代付（资金提现）
	 * 
	 * @param params
	 *            请求参数
	 * @return
	 */
	public static ApiResult withdraw(Map<String, String> params) {
		return post(API_PAY_WITHDRAW, params);
	}

	/**
	 * 查询账户余额
	 * 
	 * @return
	 */
	public static ApiResult queryBalance() {
		return post(API_PAY_BALANCE, new HashMap<String, String>());
	}

	// 在参数中添加签名
	private static void sign(Map<String, String> params) {
		params.put("sign", Util.getSign(params, ""));
	}

	/**
	 * 验证签名是否正确
	 * 
	 * @param params
	 *            含有签名和签名类型的订单信息集合
	 * @return true:签名验证通过；false：签名验证失败
	 */
	public static boolean validSign(Map<String, String> params) {
		// 提取签名
		String sign = params.remove(SIGN_PARA);

		// 本地生成签名
		return Util.getSign(params, "").equals(sign);
	}

	/**
	 * 到支付平台验证通知消息是否真实
	 * 
	 * @param merchantId
	 *            商户号
	 * @param notifyId
	 *            通知id
	 * @return
	 * @throws Exception
	 */
	public static ApiResult validSource(String merchantId, String notifyId) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put("notifyId", notifyId);
		return get(API_PAY_VERIFY, params);
	}

	/**
	 * 发送请求
	 * 
	 * @param api
	 *            接口path（不包含host）
	 * @param params
	 *            参数
	 * @param isPost
	 *            是否post
	 * @return
	 */
	private static ApiResult sendReq(String api, Map<String, String> params, boolean isPost) {
		// 添加公共参数和签名
		addBaseParams(params);
		sign(params);

		// 发请求
		String url = Config.SERVICE_URL + api;
		HttpResult httpRet = isPost ? HttpUtil.doPost(url, params) : HttpUtil.doGet(url, params);
		if (!httpRet.isSuccess()) {
			throw new RuntimeException("http请求失败: " + httpRet);
		}

		return new ApiResult(httpRet.getContent());
	}

	/**
	 * 添加公共参数
	 * 
	 * @param params
	 */
	private static void addBaseParams(Map<String, String> params) {
		params.put("merchantId", Config.MERCHANT_ID); // 商户号
	}

	/**
	 * 发送post请求
	 * 
	 * @param api
	 *            接口path（不包含host）
	 * @param params
	 *            参数
	 * @return
	 */
	public static ApiResult post(String api, Map<String, String> params) {
		return sendReq(api, params, true);
	}

	/**
	 * 发送get请求
	 * 
	 * @param api
	 *            接口path（不包含host）
	 * @param params
	 *            参数
	 * @return
	 */
	public static ApiResult get(String api, Map<String, String> params) {
		return sendReq(api, params, false);
	}

	/**
	 * 测试余额查询接口
	 * 
	 * @return
	 * @throws Exception
	 */
	public static void main(String[] args) {
		ApiResult ret = queryBalance();
		System.out.println(ret);
	}
}
