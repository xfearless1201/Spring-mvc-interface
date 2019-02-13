package com.cn.tianxia.pay.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.gst.util.StringUtils;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.yjh.util.ApiResult;
import com.cn.tianxia.pay.yjh.util.HttpUtil;
import com.cn.tianxia.pay.yjh.util.HttpUtil.HttpResult;
import com.cn.tianxia.pay.yjh.util.Util;

import net.sf.json.JSONObject;

public class YJHPayServiceImpl implements PayService {
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

	/** 签名参数 */
	private static final String SIGN_PARA = "sign";

	/** 商品描述 **/
	private String goodsDesc = "TXWL";
	/** 异步通知地址 **/
	private String notifyUrl = "http://182.16.110.186:8080/XPJ/Notify/YJHNotify.do";
	/** 商户号 **/
	private String merchantId = "10022412";
	/** 密钥 **/
	private String KEY = "C260913EAF564E3894CB08556BD2941F";
	/** 接口地址 **/
	private String SERVICE_URL = "http://123.207.78.61";
	/** 银行卡类型: 1-借记卡，2-贷记卡 **/
	private String cardType = "1";
	/** 用户类型：1-个人，2-企业 **/
	private String userType = "1";

	private final static Logger logger = LoggerFactory.getLogger(YJHPayServiceImpl.class);

	public YJHPayServiceImpl(Map<String, String> pmap) {
		JSONObject jo = JSONObject.fromObject(pmap);
		if (null != pmap) {
			goodsDesc = jo.get("goodsDesc").toString();
			notifyUrl = jo.get("notifyUrl").toString();
			merchantId = jo.get("merchantId").toString();
			KEY = jo.get("KEY").toString();
			SERVICE_URL = jo.get("SERVICE_URL").toString();
			cardType = jo.get("cardType").toString();
			userType = jo.get("userType").toString();
		}
	}

	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		Double amount = payEntity.getAmount();
		String order_no = payEntity.getOrderNo();
		String refereUrl = payEntity.getRefererUrl();
		String pay_code = payEntity.getPayCode();
		String pay_url = payEntity.getPayUrl();
		String mobile = payEntity.getMobile();
		String ip = payEntity.getIp();

		Map<String, String> params = new HashMap<String, String>();
		params.put("outTradeNo", order_no);

		int int_amount = (int) (amount * 100);
		/** 支付金额(分) **/
		params.put("payMoney", String.valueOf(int_amount));
		params.put("bankNo", pay_code);
		params.put("returnUrl", refereUrl);
		params.put("channel", "1");
		// 手机端
		if (!StringUtils.isNullOrEmpty(mobile)) {
			params.put("channel", "2");
		}

		/** 终端ip **/
		params.put("ip", ip);

		String html = bankPay(params);
		return PayUtil.returnWYPayJson("success", "form", html, pay_url, "");
	}

	@Override
	public JSONObject smPay(PayEntity payEntity) {
		Double amount = payEntity.getAmount();
		String order_no = payEntity.getOrderNo();
		String refereUrl = payEntity.getRefererUrl();
		String pay_code = payEntity.getPayCode();
		String userName = payEntity.getUsername();
		String mobile = payEntity.getMobile();
		String ip = payEntity.getIp();

		JSONObject r_json = null;

		Map<String, String> params = new HashMap<String, String>();
		params.put("outTradeNo", order_no);
		int int_amount = (int) (amount * 100);
		/** 支付金额(分) **/
		params.put("payMoney", String.valueOf(int_amount));
		params.put("payType", pay_code);
		params.put("returnUrl", refereUrl);
		params.put("ip", ip);
		// 手机端
		if (!StringUtils.isNullOrEmpty(mobile)) {
			// params.put("returnUrl", refereUrl);
			r_json = h5Pay(params);
		} else {
			r_json = scanPay(params);
		}

		if ("success".equals(r_json.getString("status"))) {
			if (!StringUtils.isNullOrEmpty(mobile)) {
				return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, amount, order_no,
						r_json.getString("qrCode"));
			} else {
				String type = "2";
				return PayUtil.returnPayJson("success", type, "支付接口请求成功!", userName, amount, order_no,
						r_json.getString("qrCode"));
			}

		} else {
			return PayUtil.returnPayJson("error", "4", r_json.getString("msg"), userName, amount, order_no, "");
		}
	}

	/**
	 * 网银支付接口
	 * 
	 * @param scanMap
	 * @return
	 */
	public String bankPay(Map<String, String> scanMap) {

		Map<String, String> params = new HashMap<String, String>();

		/** 订单号 **/
		String outTradeNo = scanMap.get("outTradeNo");

		/** 银行代号 **/
		String bankNo = scanMap.get("bankNo");

		/** 支付金额(分) **/
		String payMoney = scanMap.get("payMoney");

		/** 终端ip **/
		String ip = scanMap.get("ip");

		String channel = scanMap.get("channel");

		String returnUrl = scanMap.get("returnUrl");

		params.put("merchantId", merchantId); // 商户号

		params.put("outTradeNo", outTradeNo);

		params.put("cardType", cardType);

		params.put("userType", userType);

		params.put("bankNo", bankNo);

		params.put("payMoney", payMoney);

		params.put("goodsDesc", goodsDesc);

		params.put("ip", ip);

		params.put("notifyUrl", notifyUrl); // 异步通知地址

		params.put("returnUrl", returnUrl);

		params.put("channel", channel);

		String sign = Util.getSign(params, KEY);
		params.put("sign", sign);

		String FormString = "<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form  id=\"actform\" name=\"actform\" method=\"post\" action=\""
				+ SERVICE_URL + API_PAY_EBANK + "\">";
		for (String key : params.keySet()) {
			FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" + params.get(key) + "'>\r\n";
		}
		FormString += "</form></body>";
		logger.info("云聚合支付表单:" + FormString);
		return FormString;
	}

	/**
	 * 二维码支付接口
	 * 
	 * @param scanMap
	 * @return
	 */
	public JSONObject scanPay(Map<String, String> scanMap) {

		Map<String, String> params = new HashMap<String, String>();

		/** 订单号 **/
		String outTradeNo = scanMap.get("outTradeNo");

		/** 支付方式 **/
		String payType = scanMap.get("payType");

		/** 支付金额(分) **/
		String payMoney = scanMap.get("payMoney");

		/** 终端ip **/
		String ip = scanMap.get("ip");

		params.put("merchantId", merchantId); // 商户号

		params.put("outTradeNo", outTradeNo);

		params.put("payType", payType);

		params.put("payMoney", payMoney);

		params.put("goodsDesc", goodsDesc);

		params.put("ip", ip);

		params.put("notifyUrl", notifyUrl); // 异步通知地址

		String sign = Util.getSign(params, KEY);
		params.put("sign", sign);

		ApiResult ret = null;
		try {
			HttpResult httpRet = HttpUtil.doPost(SERVICE_URL + API_PAY_QR, params);

			if (!httpRet.isSuccess()) {
				throw new RuntimeException("http请求失败: " + httpRet);
			}

			ret = new ApiResult(httpRet.getContent());

			logger.info("平台返回结果：" + ret);
			logger.info("<br/>是否成功：" + (ret.isSuccess() ? "是" : "否"));

			if (ret.isSuccess()) {
				return getReturnJson("success", ret.get("qrCodeUrl"), ret.getRetMsg());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return getReturnJson("error", "", ret.toString());
		}
		return getReturnJson("error", "", ret.toString());
	}

	public JSONObject h5Pay(Map<String, String> scanMap) {

		Map<String, String> params = new HashMap<String, String>();

		/** 订单号 **/
		String outTradeNo = scanMap.get("outTradeNo");

		/** 支付方式 **/
		String payType = scanMap.get("payType");

		/** 支付金额(分) **/
		String payMoney = scanMap.get("payMoney");

		/** 终端ip **/
		String ip = scanMap.get("ip");

		/** 同步跳转通知的url **/
		String returnUrl = scanMap.get("returnUrl");

		params.put("merchantId", merchantId); // 商户号

		params.put("outTradeNo", outTradeNo);

		params.put("payType", payType);

		params.put("payMoney", payMoney);

		params.put("goodsDesc", goodsDesc);

		params.put("ip", ip);

		params.put("notifyUrl", notifyUrl); // 异步通知地址

		params.put("returnUrl", returnUrl);

		String sign = Util.getSign(params, KEY);
		params.put("sign", sign);

		ApiResult ret = null;
		try {
			HttpResult httpRet = HttpUtil.doPost(SERVICE_URL + API_PAY_H5, params);

			if (!httpRet.isSuccess()) {
				throw new RuntimeException("http请求失败: " + httpRet);
			}

			ret = new ApiResult(httpRet.getContent());

			logger.info("平台返回结果：" + ret);
			logger.info("<br/>是否成功：" + (ret.isSuccess() ? "是" : "否"));

			if (ret.isSuccess()) {
				return getReturnJson("success", ret.get("prepayUrl"), ret.getRetMsg());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return getReturnJson("error", "", ret.toString());
		}
		return getReturnJson("error", "", ret.toString());
	}

	@Override
	public String callback(Map<String, String> params) {
		logger.info(".........进入验签方法....开始签名验证！");
		// 提取签名
		String sign = params.remove(SIGN_PARA);

		String localSign = Util.getSign(params, KEY);
		logger.info("本地签名:" + localSign + "       支付商签名:" + sign);

		// 本地生成签名
		if (localSign.equals(sign)) {
			logger.info(".........验签方法....验签成功！");
			return "success";
		}
		return "";
	}

	/**
	 * 结果返回
	 * 
	 * @param status
	 * @param qrCode
	 * @param msg
	 * @return
	 */
	private static JSONObject getReturnJson(String status, String qrCode, String msg) {
		JSONObject json = new JSONObject();
		json.put("status", status);
		json.put("qrCode", qrCode);
		json.put("msg", msg);
		return json;
	}
}
