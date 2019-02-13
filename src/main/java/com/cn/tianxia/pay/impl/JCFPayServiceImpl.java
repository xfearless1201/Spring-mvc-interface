package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.gst.util.StringUtils;
import com.cn.tianxia.pay.jh.util.MerchantApiUtil;
import com.cn.tianxia.pay.jh.util.SimpleHttpUtils;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.tjf.utils.Config;

import net.sf.json.JSONObject;

public class JCFPayServiceImpl implements PayService {

	private String PAY_KEY; // 商户key
	private String PAY_SECRET; // 支付密钥
	private String h5_url;
	private String scan_url;
	private String bank_url;
	/** 支付产品名称 **/
	private String productName;
	/** 支付银行帐户类型 **/
	private String bankAccountType;
	/** 异步通知地址 **/
	private String notifyUrl;

	private final static Logger logger = LoggerFactory.getLogger(JCFPayServiceImpl.class);

	public JCFPayServiceImpl(Map<String, String> pmap) {
		JSONObject jo = JSONObject.fromObject(pmap);
		if (null != pmap) {
			PAY_KEY = jo.get("PAY_KEY").toString();// key
			PAY_SECRET = jo.get("PAY_SECRET").toString();// 商户密钥
			h5_url = jo.get("h5_url").toString();// h5地址
			scan_url = jo.get("scan_url").toString();// 扫码地址
			bank_url = jo.get("bank_url").toString();// 支付网关
			productName = jo.get("productName").toString();// 商品名称
			bankAccountType = jo.get("bankAccountType").toString();// 支付银行账户类型
			notifyUrl = jo.get("notifyUrl").toString();// 异步通知地址
		}
	}

	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		Double amount = payEntity.getAmount();
		String order_no = payEntity.getOrderNo();
		String refereUrl = payEntity.getRefererUrl();
		String pay_code = payEntity.getPayCode();
		String ip = payEntity.getIp();
		Map<String, String> bankMap = new HashMap<>();
		bankMap.put("orderPrice", String.valueOf(amount));// 订单金额
		bankMap.put("outTradeNo", order_no);// 支付订单号(唯一)
		bankMap.put("productType", "50000103");// B2C 网银 T0 支付
		bankMap.put("orderIp", ip);// 终端IP地址，传自己本地的IP公网地址
		bankMap.put("returnUrl", refereUrl);// 同步通知地址
		bankMap.put("bankCode", pay_code);// 银行编码

		String html = bankPay(bankMap);

		if (StringUtils.isNullOrEmpty(html)) {
			logger.info("聚成付网银获取表单异常！");
			return PayUtil.returnWYPayJson("error", "link", html, "", "");
		}
		return PayUtil.returnWYPayJson("success", "link", html, "", "");
	}

	@Override
	public JSONObject smPay(PayEntity payEntity) {
		Double amount = payEntity.getAmount();
		String order_no = payEntity.getOrderNo();
		String refereUrl = payEntity.getRefererUrl();
		String pay_code = payEntity.getPayCode();
		String userName = payEntity.getUsername();
		String ip = payEntity.getIp();
		String mobile = payEntity.getMobile();

		Map<String, String> scanMap = new HashMap<>();
		scanMap.put("orderPrice", String.valueOf(amount));// 订单金额
		scanMap.put("outTradeNo", order_no);// 支付订单号(唯一)
		scanMap.put("productType", pay_code);// QQ T0 QQ钱包扫码支付
		scanMap.put("orderIp", ip);// 终端IP地址，传自己本地的IP公网地址
		scanMap.put("returnUrl", refereUrl);// 同步通知地址
		JSONObject rjson = null;
		if (StringUtils.isNullOrEmpty(mobile)) {
			rjson = scanPay(scanMap);
		} else {
			// 手机端 只支持微信h5
			rjson = h5Pay(scanMap);
		}

		if ("success".equals(rjson.getString("status"))) {
			/** pc端 **/
			if (StringUtils.isNullOrEmpty(mobile)) {
				return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, amount, order_no,
						rjson.getString("qrCode"));
			} else {
				/** 手机端 **/
				return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, amount, order_no,
						rjson.getString("qrCode"));
			}
		} else {
			return PayUtil.returnPayJson("error", "4", rjson.getString("msg"), userName, amount, order_no, "");
		}

	}

	private String bankPay(Map<String, String> bankMap) {

		DecimalFormat decimalFormat = new DecimalFormat("######.00");
		String orderPrice = String.valueOf(decimalFormat.format(Double.parseDouble(bankMap.get("orderPrice"))));// 订单金额
		String outTradeNo = bankMap.get("outTradeNo");// 支付订单号(唯一)
		String productType = bankMap.get("productType");// B2C T0网银支付
		String orderTimeStr = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());// 订单时间(格式：yyyyMMddHHmmss)
		// String productName = "goods";// 商品名称
		String orderIp = bankMap.get("orderIp");// 下单IP。正式应用时，请配置成获取终端IP地址
		String bankCode = bankMap.get("bankCode");// 银行编码
		// String bankAccountType = "PRIVATE_DEBIT_ACCOUNT";// 支付银行卡类型
		String returnUrl = bankMap.get("returnUrl");// 同步通知地址。正式应用时，请配置成你们自己的公网IP地址或者域名下的页面
		// String notifyUrl = "http://wy.mjfhb.top/mpay/yeeBankPayNotify.jsp";//
		// 异步通知地址。正式应用时，请配置成你们自己的公网IP地址或者域名下的页面
		String sign;// 签名

		Map<String, Object> paramMap = new TreeMap<>();// 按ASCII顺序进行排序
		paramMap.put("bankAccountType", bankAccountType);
		paramMap.put("bankCode", bankCode);
		paramMap.put("notifyUrl", notifyUrl);
		paramMap.put("orderIp", orderIp);
		paramMap.put("orderPrice", orderPrice);
		paramMap.put("orderTime", orderTimeStr);
		paramMap.put("outTradeNo", outTradeNo);
		paramMap.put("payKey", PAY_KEY);
		paramMap.put("productName", productName);
		paramMap.put("productType", productType);
		paramMap.put("returnUrl", returnUrl);
		sign = MerchantApiUtil.getSign(paramMap, PAY_SECRET);// 进行签名
		paramMap.put("sign", sign);

		logger.info("请求参数：" + paramMap);
		logger.info("订单号 outTradeNo:" + outTradeNo);
		String payResult = SimpleHttpUtils.httpPost(bank_url, paramMap);// 发送请求,POST请求，文档get请求是演示参数
		logger.info("请求成功，响应数据:" + payResult);

		JSONObject jsonObject = JSONObject.fromObject(payResult);
//		String resultCode = (String) jsonObject.get("resultCode");
		String resultCode = (String) jsonObject.get("returnCode");

		if ("0000".equals(resultCode)) {// 请求成功
			return jsonObject.getString("url");

		} else {// 请求失败
			return "";
		}
	}

	/**
	 * H5 支付 某些字段可为空
	 */
	private JSONObject h5Pay(Map<String, String> h5Map) {

		DecimalFormat decimalFormat = new DecimalFormat("######.00");
		String orderPrice = String.valueOf(decimalFormat.format(Double.parseDouble(h5Map.get("orderPrice"))));// 订单金额
		String outTradeNo = h5Map.get("outTradeNo");// 支付订单号(唯一)
		String productType = h5Map.get("productType");// QQ T0 QQ钱包扫码支付
		String orderTimeStr = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());// 订单时间(格式：yyyyMMddHHmmss)
		// String productName = "goods";// 商品名称 建议传英文产品名称
		String orderIp = h5Map.get("orderIp");// 下单IP。正式应用时，请配置成获取终端IP地址
		String returnUrl = h5Map.get("returnUrl");// 同步通知地址。正式应用时，请配置成你们自己的公网IP地址或者域名下的页面
		// String notifyUrl = "http://wy.mjfhb.top/mpay/yeeBankPayNotify.jsp";//
		// 异步通知地址。正式应用时，请配置成你们自己的公网IP地址或者域名下的页面
		String sign;// 签名

		Map<String, Object> paramMap = new TreeMap<>();// 按ASCII顺序进行排序
		paramMap.put("notifyUrl", notifyUrl);
		paramMap.put("orderPrice", orderPrice);
		paramMap.put("orderTime", orderTimeStr);
		paramMap.put("outTradeNo", outTradeNo);
		paramMap.put("payKey", PAY_KEY);
		paramMap.put("productName", productName);
		paramMap.put("orderIp", orderIp);
		paramMap.put("productType", productType);
		paramMap.put("returnUrl", returnUrl);
		sign = MerchantApiUtil.getSign(paramMap, PAY_SECRET);// 进行签名
		paramMap.put("sign", sign);

		logger.info("请求参数：" + paramMap);
		logger.info("订单号 outTradeNo:" + outTradeNo);
		String payResult = SimpleHttpUtils.httpPost(h5_url, paramMap);// 发送请求,POST请求，文档get请求是演示参数

		/**
		 * 响应
		 */
		JSONObject jsonObject = JSONObject.fromObject(payResult);
		String resultCode = (String) jsonObject.get("resultCode");

		if ("0000".equals(resultCode)) {// 请求成功
			logger.info("请求成功，响应数据:" + payResult);
			return getReturnJson("success", jsonObject.getString("payMessage"), "接口请求成功！");
		} else {// 请求失败
			logger.info("请求失败 响应数据:" + payResult);
			return getReturnJson("error", "", payResult);
		}
	}

	/**
	 * 二维码接口
	 */
	private JSONObject scanPay(Map<String, String> scanMap) {
		DecimalFormat decimalFormat = new DecimalFormat("######.00");
		String orderPrice = String.valueOf(decimalFormat.format(Double.parseDouble(scanMap.get("orderPrice"))));// 订单金额
		String outTradeNo = scanMap.get("outTradeNo");// 支付订单号(唯一)
		String productType = scanMap.get("productType");// QQ T0 QQ钱包扫码支付
		String orderTimeStr = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());// 订单时间(格式：yyyyMMddHHmmss)
		// String productName = "txwl";// 商品名称 建议传英文产品名称
		String orderIp = scanMap.get("orderIp");// 终端IP地址，传自己本地的IP公网地址
		String returnUrl = scanMap.get("returnUrl");// 同步通知地址
		// String notifyUrl = "http://wy.mjfhb.top/mpay/yeeBankPayNotify.jsp";//
		// 异步通知地址
		String sign;// 签名

		Map<String, Object> paramMap = new TreeMap<>();// 按ASCII顺序进行排序
		paramMap.put("notifyUrl", notifyUrl);
		paramMap.put("orderPrice", orderPrice);
		paramMap.put("orderTime", orderTimeStr);
		paramMap.put("outTradeNo", outTradeNo);
		paramMap.put("payKey", PAY_KEY);
		paramMap.put("productName", productName);
		paramMap.put("orderIp", orderIp);
		paramMap.put("productType", productType);
		paramMap.put("returnUrl", returnUrl);
		paramMap.put("title", "TXWL码扫码支付"); // 添加title
		sign = MerchantApiUtil.getSign(paramMap, PAY_SECRET);// 进行签名
		paramMap.put("sign", sign);

		logger.info("请求参数：" + paramMap);
		logger.info("订单号 outTradeNo:" + outTradeNo);
		String payResult = SimpleHttpUtils.httpPost(scan_url, paramMap);// 发送请求,POST请求，文档get请求是演示参数

		/**
		 * 响应
		 */
		JSONObject jsonObject = JSONObject.fromObject(payResult);
		String resultCode = (String) jsonObject.get("resultCode");

		if ("0000".equals(resultCode)) {// 请求成功
			logger.info("请求成功，响应数据:" + payResult);
			return getReturnJson("success", jsonObject.getString("payMessage"), "接口请求成功！");
		} else {// 请求失败
			logger.info("请求失败 响应数据:" + payResult);
			return getReturnJson("error", "", payResult);
		}
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
	 * 异步回调验签方法
	 * 
	 * @param request
	 * @return
	 */
	@Override
	public String callback(Map<String, String> request) {
		String productName = request.get("productName");
		String orderPrice = request.get("orderPrice");
		String tradeStatus = request.get("tradeStatus");
		String outTradeNo = request.get("outTradeNo");
		String productType = request.get("productType");
		String payKey = request.get("payKey");
		String trxNo = request.get("trxNo");
		String remark = request.get("remark");
		String successTime = request.get("successTime");
		String orderTime = request.get("orderTime");
		String sign = request.get("sign");

		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("productName", productName);
		paramMap.put("orderPrice", orderPrice);
		paramMap.put("tradeStatus", tradeStatus);
		paramMap.put("outTradeNo", outTradeNo);
		paramMap.put("productType", productType);
		paramMap.put("payKey", payKey);
		paramMap.put("trxNo", trxNo);
		paramMap.put("remark", remark);
		paramMap.put("successTime", successTime);
		paramMap.put("orderTime", orderTime);

		String signStr = MerchantApiUtil.getSign(paramMap, PAY_SECRET);
		if (sign.equals(signStr)) {
			logger.info("聚成付验签成功!");
			return "success";
		}

		logger.info("聚成付验签失败!");
		return "";
	}
}
