package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.json.JSONUtils;
import com.cn.tianxia.pay.dd.util.HttpClientUtil;
import com.cn.tianxia.pay.dd.util.JsonUtil;
import com.cn.tianxia.pay.dd.util.RSA;
import com.cn.tianxia.pay.qyf.util.ToolKit;
import com.cn.tianxia.pay.service.PayService;

import net.sf.json.JSONObject;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

@SuppressWarnings("restriction")
public class DDPayServiceImpl implements PayService {
	/** 扫码接口 **/
	private String scan_method;
	/** wap接口 **/
	private String wap_method;
	/** 网银接口 **/
	private String bank_method;
	/** 请求地址 **/
	private String requrl;
	/** md5密钥 **/
	private String key;
	/** 商户号 **/
	private String merchant_no;

	/** 商户签约pin **/
	private String pid;

	/*** 货币类型 **/
	private String ord_currency;

	/** 扫码场景 用户扫商户二维码 **/
	private String scan_type;

	/** 交易描述 **/
	private String desc;
	/** 网银异步回调地址 **/
	private String wy_notify_method;
	/** 扫码异步回调地址 **/
	private String sm_notify_method;
	/** 扫码异步回调地址 **/
	private String wap_notify_method;
	/** 扫码地址 **/
	private String notify_url;

	/** rsa公钥 **/
	private String publicKey;

	/** rsa私钥 **/
	private String privateKey;

	private final static Logger logger = LoggerFactory.getLogger(DDPayServiceImpl.class);

	public DDPayServiceImpl(Map<String, String> pmap) {
		JSONObject jo = JSONObject.fromObject(pmap);
		if (null != pmap) {
			scan_method = jo.getString("scan_method");
			wap_method = jo.getString("wap_method");
			bank_method = jo.getString("bank_method");
			requrl = jo.getString("requrl");
			key = jo.getString("key");
			merchant_no = jo.getString("merchant_no");
			pid = jo.getString("pid");
			ord_currency = jo.getString("ord_currency");
			scan_type = jo.getString("scan_type");
			desc = jo.getString("desc");
			notify_url = jo.getString("notify_url");
			wy_notify_method = jo.getString("notify_url") + "/DDWYNotify.do";
			sm_notify_method = jo.getString("notify_url") + "/DDSMNotify.do";
			wap_notify_method = jo.getString("notify_url") + "/DDWAPNotify.do";
			publicKey = jo.getString("publicKey");
			privateKey = jo.getString("privateKey");
		}
	}

	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		Double amount = payEntity.getAmount();
		String order_no = payEntity.getOrderNo();
		String refereUrl = payEntity.getRefererUrl();
		String pay_code = payEntity.getPayCode();
		String userName = payEntity.getUsername();
		String pay_url = payEntity.getPayUrl();
		String mobile = payEntity.getMobile();
		String ip = payEntity.getIp();
		String topay = payEntity.getTopay();

		// 精确到小数点后两位，例如 10.24
		double amount1 = (amount * 100);// 分为单位
		DecimalFormat df = new DecimalFormat("#########");
		String pay_amount = df.format(amount1);

		Map<String, String> bankMap = new HashMap<>();
		bankMap.put("order_sn", order_no);
		bankMap.put("bank_code", pay_code);// 订单号
		bankMap.put("pay_amount", pay_amount);// 金额
		bankMap.put("return_url", refereUrl);
		JSONObject json = bankPay(bankMap);

		return PayUtil.returnWYPayJson("success", "link", json.getString("qrCode"), pay_url, "");
	}

	@Override
	public JSONObject smPay(PayEntity payEntity) {
		Double amount = payEntity.getAmount();
		String order_no = payEntity.getOrderNo();
		String refereUrl = payEntity.getRefererUrl();
		String pay_code = payEntity.getPayCode();
		String userName = payEntity.getUsername();
		String pay_url = payEntity.getPayUrl();
		String mobile = payEntity.getMobile();
		String ip = payEntity.getIp();
		String topay = payEntity.getTopay();

		// 精确到小数点后两位，例如 10.24
		double amount1 = (amount * 100);// 分为单位
		DecimalFormat df = new DecimalFormat("#########");
		String pay_amount = df.format(amount1);

		JSONObject r_json = null;

		// 手机端
		if (StringUtils.isNotEmpty(mobile)) {
			Map<String, String> wapMap = new HashMap<>();
			wapMap.put("out_trade_no", order_no);
			wapMap.put("pay_type", pay_code);// 订单号
			wapMap.put("trade_amount", pay_amount);// 金额
			wapMap.put("sync_url", refereUrl);
			wapMap.put("client_ip", ip);
			r_json = wapPay(wapMap);
		} else {
			// pc端
			Map<String, String> scanMap = new HashMap<>();
			scanMap.put("order_sn", order_no);
			scanMap.put("pmt_tag", pay_code);// 订单号
			scanMap.put("pay_amount", pay_amount);// 金额
			r_json = scanPay(scanMap);
		}

		if ("success".equals(r_json.getString("status"))) {
			if (StringUtils.isNotEmpty(mobile)) {
				return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, amount, order_no,
						r_json.getString("qrCode"));
			} else {
				return PayUtil.returnPayJson("success", "2", "支付接口请求成功!", userName, amount, order_no,
						r_json.getString("qrCode"));
			}

		} else {
			return PayUtil.returnPayJson("error", "4", r_json.getString("msg"), userName, amount, order_no, "");
		}
	}

	public JSONObject scanPay(Map<String, String> scanMap) {
		String responseMsg = "";
		try {
			// RSAEncrypt2 rsaEncrypt = getRSA(publicKey);
			BASE64Encoder encoder = new BASE64Encoder();
			// 调用接口
			String timestamp = new Date().getTime() + "";// 时间戳
			String randstr = ToolKit.randomStr(32);// 随机字符串

			String sign = "";// 签名
			String data = "";// 数据

			String order_sn = scanMap.get("order_sn");// 商户订单
			String pmt_tag = scanMap.get("pmt_tag");// 支付类型
			String pay_amount = scanMap.get("pay_amount");// 支付金额
			// String notify_url = "http://www.baidu.com";// 回调地址
			String order_desc = desc;// 订单描述
			String pay_type = scan_type;// 支付场景,swept(用户扫商户二维码)
			Map<String, Object> requestMap = new HashMap<String, Object>();
			requestMap.put("merchant_no", merchant_no);
			requestMap.put("order_sn", order_sn);
			requestMap.put("pmt_tag", pmt_tag);
			requestMap.put("pay_amount", pay_amount);
			requestMap.put("notify_url", sm_notify_method);
			requestMap.put("order_desc", order_desc);
			requestMap.put("pay_type", pay_type);
			data = JsonUtil.toJson(new TreeMap<String, Object>(requestMap)).replaceAll("\\\\/", "/");
			String md5Str = "{\"data\":" + data + ",\"method\":\"" + scan_method + "\",\"pid\":\"" + pid
					+ "\",\"randstr\":\"" + randstr + "\",\"timestamp\":\"" + timestamp + "\"}" + key;
			sign = DigestUtils.md5Hex(md5Str);
			logger.info(md5Str);
			logger.info("签名:" + sign);

			byte[] dataBytes = RSA.encryptByPublicKeyNew(data.getBytes(), publicKey);
			data = encoder.encode(dataBytes).replaceAll("\r\n", "");

			requrl += "?method=" + scan_method + "&pid=" + pid + "&randstr=" + randstr + "&sign=" + sign + "&timestamp="
					+ timestamp;
			Map<String, String> dataMap = new HashMap<String, String>();
			dataMap.put("data", data);
			logger.info(requrl);
			logger.info("data:" + data);
			responseMsg = new HttpClientUtil().doPost(requrl, dataMap, "utf-8");
			logger.info("豆豆扫码响应：" + responseMsg);

			/*** 数据解密 ***/
			// BASE64Decoder decoder = new BASE64Decoder();
			// String resultStr = new
			// String(RSA.decryptByPrivateKeyNew(decoder.decodeBuffer(responseMsg),
			// privateKey));
			// System.out.println("responseMsg.msg=" +
			// JsonUtil.json2Map(resultStr));
			/*** 数据解密 ***/

			JSONObject json = JSONObject.fromObject(responseMsg);

			if (json.containsKey("errcode") && "0".equals(json.getString("errcode"))) {
				JSONObject dataJson = JSONUtils.toJSONObject(json.get("data"));
				if (dataJson.containsKey("code_status") && "SUCCESS".equals(dataJson.getString("code_status"))) {
					return getReturnJson("success", dataJson.getString("code_url"), "二维码连接获取成功！");
				}

			}
			return getReturnJson("error", "", responseMsg);
		} catch (Exception e) {
			e.printStackTrace();
			return getReturnJson("error", "", responseMsg);
		}

	}

	public JSONObject wapPay(Map<String, String> wapMap) {
		String responseMsg = "";
		try {
			BASE64Encoder encoder = new BASE64Encoder();
			// 调用接口
			String timestamp = new Date().getTime() + "";// 时间戳
			String randstr = ToolKit.randomStr(32);// 随机字符串

			String sign = "";// 签名
			String data = "";// 数据

			String out_trade_no = wapMap.get("out_trade_no");// 商户订单
			String body = desc;// 交易描述
			String pay_type = wapMap.get("pay_type");// 支付方式
			String trade_amount = wapMap.get("trade_amount");// 支付金额
			String sync_url = wapMap.get("sync_url");
			String client_ip = wapMap.get("client_ip");
			Map<String, Object> requestMap = new HashMap<String, Object>();
			requestMap.put("out_trade_no", out_trade_no);
			requestMap.put("body", body);
			requestMap.put("pay_type", pay_type);
			requestMap.put("trade_amount", trade_amount);
			requestMap.put("notify_url", wap_notify_method);
			requestMap.put("sync_url", sync_url);
			requestMap.put("client_ip", client_ip);
			requestMap.put("merchant_no", merchant_no);
			data = JsonUtil.toJson(new TreeMap<String, Object>(requestMap)).replaceAll("\\\\/", "/");
			String md5Str = "{\"data\":" + data + ",\"method\":\"" + wap_method + "\",\"pid\":\"" + pid
					+ "\",\"randstr\":\"" + randstr + "\",\"timestamp\":\"" + timestamp + "\"}" + key;
			sign = DigestUtils.md5Hex(md5Str);
			logger.info(md5Str);
			logger.info("签名:" + sign);

			byte[] dataBytes = RSA.encryptByPublicKeyNew(data.getBytes(), publicKey);
			data = encoder.encode(dataBytes).replaceAll("\r\n", "");

			// data = encoder.encode(data3).replaceAll("\r\n", "");
			requrl += "?method=" + wap_method + "&pid=" + pid + "&randstr=" + randstr + "&sign=" + sign + "&timestamp="
					+ timestamp;
			Map<String, String> dataMap = new HashMap<String, String>();
			dataMap.put("data", data);
			logger.info(requrl);
			logger.info("data:" + data);
			responseMsg = new HttpClientUtil().doPost(requrl, dataMap, "utf-8");
			logger.info("豆豆扫码wap响应：" + responseMsg);

			JSONObject json = JSONObject.fromObject(responseMsg);
			if (json.containsKey("errcode") && "0".equals(json.getString("errcode"))) {
				JSONObject dataJson = JSONUtils.toJSONObject(json.get("data"));

				return getReturnJson("success", dataJson.getString("out_pay_url"), "二维码连接获取成功！");

			}
			return getReturnJson("error", "", responseMsg);
		} catch (Exception e) {
			e.printStackTrace();
			return getReturnJson("error", "", responseMsg);
		}
	}

	public JSONObject bankPay(Map<String, String> bankMap) {
		String responseMsg = "";
		try {
			BASE64Encoder encoder = new BASE64Encoder();
			// 调用接口
			String timestamp = new Date().getTime() + "";// 时间戳
			String randstr = ToolKit.randomStr(32);// 随机字符串

			String sign = "";// 签名
			String data = "";// 数据

			String order_sn = bankMap.get("order_sn");// 商户订单
			String ord_name = desc;// 交易描述
			String pay_amount = bankMap.get("pay_amount");// 支付金额
			String bank_code = bankMap.get("bank_code");// 银行编码
			String bank_card_type = "0";// 银行卡类型（0:借记卡，1:贷记卡） 贷记卡暂不支持
			String return_url = bankMap.get("return_url");
			// String order_desc = "xxx";// 订单描述
			Map<String, Object> requestMap = new HashMap<String, Object>();
			requestMap.put("merchant_no", merchant_no);
			requestMap.put("order_sn", order_sn);
			requestMap.put("ord_name", ord_name);
			requestMap.put("pay_amount", pay_amount);
			requestMap.put("ord_currency", ord_currency);
			requestMap.put("bank_code", bank_code);
			requestMap.put("bank_card_type", bank_card_type);
			requestMap.put("notify_url", wy_notify_method);
			requestMap.put("return_url", return_url);
			data = JsonUtil.toJson(new TreeMap<String, Object>(requestMap)).replaceAll("\\\\/", "/");
			String md5Str = "{\"data\":" + data + ",\"method\":\"" + bank_method + "\",\"pid\":\"" + pid
					+ "\",\"randstr\":\"" + randstr + "\",\"timestamp\":\"" + timestamp + "\"}" + key;
			sign = DigestUtils.md5Hex(md5Str);
			logger.info(md5Str);
			logger.info("签名:" + sign);

			byte[] dataBytes = RSA.encryptByPublicKeyNew(data.getBytes(), publicKey);
			data = encoder.encode(dataBytes).replaceAll("\r\n", "");

			requrl += "?method=" + bank_method + "&pid=" + pid + "&randstr=" + randstr + "&sign=" + sign + "&timestamp="
					+ timestamp;
			Map<String, String> dataMap = new HashMap<String, String>();
			dataMap.put("data", data);
			logger.info(requrl);
			logger.info("data:" + data);
			responseMsg = new HttpClientUtil().doPost(requrl, dataMap, "utf-8");
			logger.info(responseMsg);

			JSONObject json = JSONObject.fromObject(responseMsg);
			if (json.containsKey("errcode") && "0".equals(json.getString("errcode"))) {
				JSONObject dataJson = JSONUtils.toJSONObject(json.get("data"));
				return getReturnJson("success", dataJson.getString("url"), "二维码连接获取成功！");
			}
			return getReturnJson("error", "", responseMsg);
		} catch (Exception e) {
			e.printStackTrace();
			return getReturnJson("error", "", responseMsg);
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
	 * 回调验签
	 * 
	 * @param map
	 * @return
	 */
	@Override
	public String callback(Map<String, String> map) {
		String serviceSign = map.get("sign");

		String localSign = getSign(map, key);
		logger.info("服务器serviceSign:" + serviceSign + "              本地localSign:" + localSign);

		if (localSign.equals(serviceSign)) {
			return "success";
		}

		return "";
	}

	/**
	 * 
	 * @param paramMap
	 * @param paySecret
	 * @return
	 */
	public static String getSign(Map<String, String> paramMap, String paySecret) {
		SortedMap<String, Object> smap = new TreeMap<String, Object>(paramMap);
		StringBuffer stringBuffer = new StringBuffer();

		/**
		 * sign 参数不参与签名，值为空不参与签名，值为 0 不参与签名(如 bank_card_type=0 将不参与签名)
		 **/
		if (smap.containsKey("sign")) {
			smap.remove("sign");
		}
		if (smap.containsKey("bank_card_type") && "0".equals(smap.get("bank_card_type").toString())) {
			smap.remove("bank_card_type");
		}

		for (Map.Entry<String, Object> m : smap.entrySet()) {
			Object value = m.getValue();
			if (value != null && StringUtils.isNotBlank(String.valueOf(value))) {
				stringBuffer.append(m.getKey()).append("=").append(m.getValue()).append("&");
			}
		}
		stringBuffer.delete(stringBuffer.length() - 1, stringBuffer.length());

		String argPreSign = stringBuffer.append("&key=").append(paySecret).toString();
		logger.info("前面字符原串:" + argPreSign);
		String sign = DigestUtils.md5Hex(argPreSign);
		return sign;
	}

}
