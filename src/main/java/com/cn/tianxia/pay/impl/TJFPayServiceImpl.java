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
import com.cn.tianxia.pay.tjf.utils.Config;
import com.cn.tianxia.pay.tjf.utils.DateUtil;
import com.cn.tianxia.pay.tjf.utils.Merchant;
import com.cn.tianxia.pay.tjf.utils.RefundResponseEntity;
import com.cn.tianxia.pay.tjf.utils.SignUtil;

import sun.misc.BASE64Decoder;

import net.sf.json.JSONObject;

/**
 * 天机付
 * 
 * @author AKON
 *
 * @TIME 2018年3月10日-下午2:43:33
 *
 * @DESCRIPYION 配置json { "sign_type":"MD5", "key":"", "merchant_id":"MD5",
 *              "notify_url":
 *              "http://192.168.0.228:8080/JJF/PlatformPay/TJFNotify.do",
 *              "gateway_url":
 *              "http://gate.iceuptrade.com/cooperate/gateway.cgi",
 *              "api_version":"1.0.0.0", "summary":"tianxia"
 * 
 *              }
 */
public class TJFPayServiceImpl implements PayService {

	private final static Logger logger = LoggerFactory.getLogger(TJFPayServiceImpl.class);

	public TJFPayServiceImpl(Map<String, String> pmap) {
		JSONObject jo = JSONObject.fromObject(pmap);
		if (null != pmap) {
			Config.SIGN_TYPE = jo.get("sign_type").toString();// MD5
			Config.KEY = jo.get("key").toString();// 商户密钥
			Config.MERCHANT_ID = jo.get("merchant_id").toString();// 商户号
			Config.MERCHANT_NOTIFY_URL = jo.get("notify_url").toString();// 通知地址
			Config.GATEWAY_URL = jo.get("gateway_url").toString();// 支付网关
			Config.API_VERSION = jo.get("api_version").toString();// api版本
			Config.SUMMARY = jo.get("summary").toString();// 商品名称
		}
	}

	/**
	 * 网银
	 * 
	 * @param bankMap
	 * @return
	 */
	public String bankPay(Map<String, String> bankMap) {
		Map<String, String> paramsMap = new HashMap<String, String>();
		// StringBuffer sbHtml = new StringBuffer();

		String html = "";
		try {
			paramsMap.put("service", Config.APINAME_PAY);
			paramsMap.put("version", Config.API_VERSION);
			paramsMap.put("merId", Config.MERCHANT_ID);
			paramsMap.put("tradeNo", bankMap.get("orderNo"));
			paramsMap.put("tradeDate", bankMap.get("tradeDate"));
			paramsMap.put("amount", bankMap.get("amount"));
			paramsMap.put("notifyUrl", Config.MERCHANT_NOTIFY_URL);
			paramsMap.put("extra", null);
			paramsMap.put("summary", Config.SUMMARY);
			paramsMap.put("clientIp", bankMap.get("clientIp"));
			paramsMap.put("bankId", bankMap.get("bankId"));
			paramsMap.put("expireTime", "");

			String paramsStr = Merchant.generatePayRequest(paramsMap);
			String signMsg = SignUtil.signData(paramsStr);
			logger.info("签名: " + signMsg);

			String payGateUrl = Config.GATEWAY_URL;
			paramsMap.put("sign", signMsg);

			String FormString = "<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form  id=\"actform\" name=\"actform\" method=\"post\" action=\""
					+ payGateUrl + "\">";
			for (String key : paramsMap.keySet()) {
				FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" + paramsMap.get(key) + "'>\r\n";
			}
			FormString += "</form></body>";
			html = FormString;

		} catch (Exception e) {
			logger.info("异常信息: " + e.getMessage());
			e.printStackTrace();
		}

		return html;

	}

	/*
	 * h5
	 */
	public String scanPayH5(Map<String, String> h5Map) {
		// StringBuffer sbHtml = new StringBuffer();
		String html = "";
		try {
			// 组织请求数据
			Map<String, String> paramsMap = new HashMap<String, String>();
			paramsMap.put("service", Config.APINAME_H5PAY);
			paramsMap.put("version", Config.API_VERSION);
			paramsMap.put("merId", Config.MERCHANT_ID);
			paramsMap.put("typeId", h5Map.get("typeId"));// 1；支付宝2；微信3；QQ 钱包
			paramsMap.put("tradeNo", h5Map.get("tradeNo"));
			paramsMap.put("tradeDate", h5Map.get("tradeDate"));
			paramsMap.put("amount", h5Map.get("amount"));
			paramsMap.put("notifyUrl", Config.MERCHANT_NOTIFY_URL);
			paramsMap.put("extra", "");
			paramsMap.put("summary", Config.SUMMARY);
			paramsMap.put("clientIp", h5Map.get("clientIp"));
			paramsMap.put("expireTime", "");

			String paramsStr = Merchant.generateAlspQueryRequestH5(paramsMap);
			logger.info("收銀台模式" + paramsStr);
			String signMsg = SignUtil.signData(paramsStr);
			logger.info("签名: " + signMsg);

			String payGateUrl = Config.GATEWAY_URL;
			paramsMap.put("sign", signMsg);

			String FormString = "<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form  id=\"actform\" name=\"actform\" method=\"post\" action=\""
					+ payGateUrl + "\">";
			for (String key : paramsMap.keySet()) {
				FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" + paramsMap.get(key) + "'>\r\n";
			}
			FormString += "</form></body>";
			html = FormString;
			logger.info("天机付h5表单: " + html);
		} catch (Exception e) {
			logger.info("异常信息: " + e.getMessage());
			e.printStackTrace();
		}
		return html;
	}

	/**
	 * 网银
	 * 
	 * @param scanMap
	 * @return
	 */
	public JSONObject scanPay(Map<String, String> scanMap) {
		RefundResponseEntity entity = new RefundResponseEntity();
		try {
			// 组织请求数据
			Map<String, String> paramsMap = new HashMap<String, String>();
			paramsMap.put("service", Config.APINAME_SCANPAY);
			paramsMap.put("version", Config.API_VERSION);
			paramsMap.put("merId", Config.MERCHANT_ID);
			paramsMap.put("typeId", scanMap.get("typeId"));
			paramsMap.put("tradeNo", scanMap.get("tradeNo"));
			paramsMap.put("tradeDate", scanMap.get("tradeDate"));
			paramsMap.put("amount", scanMap.get("amount"));
			paramsMap.put("notifyUrl", Config.MERCHANT_NOTIFY_URL);
			paramsMap.put("extra", "");// 即使为null也不能省略
			paramsMap.put("summary", Config.SUMMARY);
			paramsMap.put("clientIp", scanMap.get("clientIp"));
			paramsMap.put("expireTime", "");

			String paramsStr = Merchant.generateAlspQueryRequest(paramsMap);
			logger.info("扫码" + paramsStr);
			String signMsg = SignUtil.signData(paramsStr);
			logger.info("签名: " + signMsg);
			paramsStr += "&sign=" + signMsg;

			String payGateUrl = Config.GATEWAY_URL;

			// 发送请求并接收返回
			System.out.println(paramsStr);
			String responseMsg = Merchant.transact(paramsStr, payGateUrl);
			System.out.println("===" + responseMsg);

			// 解析返回数据

			entity.parse(responseMsg);

		} catch (Exception e) {
			logger.info("异常信息：" + e.getMessage());
			e.printStackTrace();
		}

		String qrUrl = "";
		if (entity.getRespCode().equals("00")) {
			logger.info("天机付接口调用成功");
			BASE64Decoder decoder = new BASE64Decoder();
			try {
				qrUrl = new String(decoder.decodeBuffer(entity.getQrCode().toString()), "UTF-8");
			} catch (Exception e) {
				logger.info("异常信息：" + e.getMessage());
				e.printStackTrace();
				return getReturnJson("error", "", "BASE64Decoder解码异常");
			}
			return getReturnJson("success", qrUrl, "二维码获取成功！");
		} else {
			return getReturnJson("error", "", entity.getRespDesc().toString());
		}
	}

	/**
	 * 
	 */
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
		Map<String, String> scanMap = new HashMap<>();
		scanMap.put("tradeNo", order_no);
		scanMap.put("amount", String.valueOf(amount));
		scanMap.put("bankId", pay_code);
		scanMap.put("clientIp", ip);
		scanMap.put("tradeDate", new SimpleDateFormat("YYYYMMDD").format(new Date()));

		String html = bankPay(scanMap);
		return PayUtil.returnWYPayJson("success", "form", html, pay_url, "");
	}

	/**
	 * 扫码
	 */
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

		Map<String, String> scanMap = new HashMap<>();
		scanMap.put("tradeNo", order_no);
		scanMap.put("amount", String.valueOf(amount));
		scanMap.put("typeId", pay_code);
		String payTime = new SimpleDateFormat("YYYYMMDD").format(new Date());
		scanMap.put("tradeDate", payTime);
		scanMap.put("clientIp", ip);
		JSONObject rjson = null;
		String html = "";
		// pc端
		if (StringUtils.isNullOrEmpty(mobile)) {
			rjson = scanPay(scanMap);
		} else {
			// 手机端 只支持微信h5

			html = scanPayH5(scanMap);
		}
		// 手机 or pc 返回类型
		if (!StringUtils.isNullOrEmpty(mobile)) {
			return PayUtil.returnPayJson("success", "1", "支付接口请求成功!", userName, amount, order_no, html);
		} else {
			if (!"success".equals(rjson.getString("status"))) {
				return PayUtil.returnPayJson("error", "2", rjson.getString("msg"), userName, amount, order_no, "");
			}
			String qrcode = rjson.getString("qrCode");
			if (rjson.containsKey("qrCode") && !"null".equals(qrcode) && !StringUtils.isNullOrEmpty(qrcode)) {
				return PayUtil.returnPayJson("success", "2", "支付接口请求成功!", userName, amount, order_no, qrcode);
			} else {
				return PayUtil.returnPayJson("error", "2", rjson.toString(), userName, amount, order_no, "");
			}

		}
	}

	public JSONObject getReturnJson(String status, String qrCode, String msg) {
		JSONObject json = new JSONObject();
		json.put("status", status);
		json.put("qrCode", qrCode);
		json.put("msg", msg);
		return json;
	}

	/**
	 * test
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Map<String, String> pmap = new HashMap<>();
		pmap.put("sign_type", "MD5");
		pmap.put("key", "70121a2b4749ae9d3bc4870abb1a279b");
		pmap.put("merchant_id", "2018031311010541");
		pmap.put("notify_url", "http://192.168.0.228:8080/JJF/PlatformPay/TJFNotify.do");
		pmap.put("gateway_url", "http://gate.iceuptrade.com/cooperate/gateway.cgi");
		pmap.put("api_version", "1.0.0.0");
		pmap.put("summary", "tianxia");
		System.out.println("天机付JSON配置:" + JSONObject.fromObject(pmap));
		TJFPayServiceImpl tj = new TJFPayServiceImpl(pmap);

		Map<String, String> paramsMap = new HashMap<>();
		paramsMap.put("service", Config.APINAME_PAY);
		paramsMap.put("version", Config.API_VERSION);
		paramsMap.put("merId", Config.MERCHANT_ID);
		paramsMap.put("tradeNo", "" + DateUtil.getOrderNum());
		paramsMap.put("tradeDate", "" + DateUtil.getDate());
		paramsMap.put("amount", "22.0");
		paramsMap.put("notifyUrl", Config.MERCHANT_NOTIFY_URL);
		paramsMap.put("summary", Config.SUMMARY);
		paramsMap.put("clientIp", "192.168.0.228");
		/**
		 * 网银支付用bankId
		 */
		// paramsMap.put("bankId", "ABC");
		// String res = tj.bankPay(paramsMap);
		/**
		 * H5支付用typeId
		 */
		// paramsMap.put("typeId", "1");// 1；支付宝2；微信3；QQ 钱包
		// String res = tj.scanPayH5(paramsMap);
		/**
		 * 扫码支付
		 */
		paramsMap.put("typeId", "3");// 1；支付宝2；微信3；QQ 钱包
		JSONObject res = tj.scanPay(paramsMap);
		System.out.println("***********************" + res.toString());

		// String service = "payservice";
		// String merId = "10101010101010";
		// String tradeNo = "order"+System.currentTimeMillis();
		// Object formatStr = String.format("service=%s&merId=%s&tradeNo=%s",
		// service,merId,tradeNo);
		// System.out.println(formatStr);
	}

	/**
	 * 回调
	 * 
	 * @param infoMap
	 * @return
	 */
	public String callBack(Map<String, String> infoMap) {
		String flag = "fail";
		try {
			// 获取请求参数，并将数据组织成前面验证源字符串

			String service = Config.APINAME_NOTIFY;
			String merId = infoMap.get("merId");
			String tradeNo = infoMap.get("tradeNo");
			String tradeDate = infoMap.get("tradeDate");
			String opeNo = infoMap.get("opeNo");
			String opeDate = infoMap.get("opeDate");
			String amount = infoMap.get("amount");
			String status = infoMap.get("status");
			String extra = infoMap.get("extra");
			String payTime = infoMap.get("payTime");
			String sign = infoMap.get("sign");
			sign.replaceAll(" ", "\\+");

			String srcMsg = String.format(
					"service=%s&merId=%s&tradeNo=%s&tradeDate=%s&opeNo=%s&opeDate=%s&amount=%s&status=%s&extra=%s&payTime=%s",
					service, merId, tradeNo, tradeDate, opeNo, opeDate, amount, status, extra, payTime);
			// 验证签名
			logger.info("===================开始接受通知");
			boolean verifyRst = SignUtil.verifyData(sign, srcMsg);

			if (verifyRst) {
				logger.info("===================验证通过");

				if (infoMap.get("notifyType").equals("1")) {
					logger.info("===================验证成功！");
					flag = "success";
				}
			}

		} catch (Exception ex) {
			logger.info(ex.getMessage());
			return flag;
		}
		logger.info("===================验证失败");
		return flag;
	}

    @Override
    public String callback(Map<String, String> data) {
        // TODO Auto-generated method stub
        return null;
    }

}
