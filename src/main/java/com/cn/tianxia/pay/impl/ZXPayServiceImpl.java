package com.cn.tianxia.pay.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.dc.util.MD5;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.util.IPTools;

import net.sf.json.JSONObject;

public class ZXPayServiceImpl implements PayService {

	private final static Logger logger = LoggerFactory.getLogger(ZXPayServiceImpl.class);
	// 网银支付网关
	private static String bankUrl;
	// 扫码支付网关
	private static String scanUrl;
	// 商户号
	private static String platformid;
	// 用户名称
	private static String username;
	// 异步通知地址
	private static String notify_url;
	// 密钥
	static String key_value;

	public ZXPayServiceImpl(Map<String, String> pmap) {
		JSONObject jo = new JSONObject().fromObject(pmap);
		if (null != pmap) {
			bankUrl = jo.get("bankUrl").toString();
			scanUrl = jo.get("scanUrl").toString();
			platformid = jo.get("platformid").toString();
			username = jo.get("username").toString();
			notify_url = jo.get("notify_url").toString();
			key_value = jo.get("key_value").toString();
		}
	}

	/**
	 * 扫码支付
	 * 
	 * @param scanMap
	 * @return
	 */
	public String scanPay(Map<String, String> scanMap) {
		String paytype = scanMap.get("paytype");
		String money = scanMap.get("money"); // 充值金额
		String time = String.valueOf(System.currentTimeMillis());
		String number = scanMap.get("number");// 订单号

		String sign = MD5.md5(platformid + username + paytype + number + money + time + key_value);

		Map<String, String> map = new LinkedHashMap<>();
		map.put("platformid", platformid);
		map.put("username", username);
		map.put("paytype", paytype);
		map.put("number", number);
		map.put("money", money);
		map.put("sign", sign.toLowerCase());
		map.put("time", time);
		map.put("notify_url", notify_url);

		String FormString = "<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form  id=\"actform\" name=\"actform\" method=\"post\" action=\""
				+ scanUrl + "\">";
		for (String key : map.keySet()) {
			FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" + map.get(key) + "'>\r\n";
		}
		FormString += "</form></body>";

		logger.info("棕熊支付网银表单:" + FormString);
		return FormString;
	}

	/**
	 * 网银支付
	 * 
	 * @param bankMap
	 * @return
	 */
	public String bankPay(Map<String, String> bankMap) {
		String timestamp = String.valueOf(System.currentTimeMillis());
		String paytype = bankMap.get("paytype");
		String money = bankMap.get("money"); // 充值金额
		String number = bankMap.get("number");// 订单号
		String sign = MD5.md5(platformid + paytype + number + money + key_value);
		Map<String, String> map = new LinkedHashMap<>();
		map.put("platformid", platformid);
		map.put("paytype", paytype);
		map.put("number", number);
		map.put("money", money);
		map.put("sign", sign.toLowerCase());
		map.put("notify_url", notify_url);
		map.put("timestamp", timestamp);

		String FormString = "<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form  id=\"actform\" name=\"actform\" method=\"post\" action=\""
				+ bankUrl + "\">";
		for (String key : map.keySet()) {
			FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" + map.get(key) + "'>\r\n";
		}
		FormString += "</form></body>";

		logger.info("棕熊支付扫码表单:" + FormString);
		return FormString;
	}

	/**
	 * formatString() : 字符串格式化方法
	 */
	public static String formatString(String text) {
		return (text == null) ? "" : text.trim();
	}

	/**
	 * 棕熊支付回调
	 * 
	 * @param request
	 * @param type
	 * @return
	 */
	public boolean callback(HttpServletRequest request, String type) {
		// String[] params = { "status", "number", "money", "tradetime", "sign",
		// "paysn" };
		String status = formatString(request.getParameter("status"));
		String number = formatString(request.getParameter("number"));
		String money = formatString(request.getParameter("money"));
		String tradetime = formatString(request.getParameter("tradetime"));
		String sign = formatString(request.getParameter("sign"));
		String paysn = formatString(request.getParameter("paysn"));

		Map<String, String> paMap = new HashMap<>();
		paMap.put("status", status);
		paMap.put("number", number);
		paMap.put("money", money);
		paMap.put("tradetime", tradetime);
		paMap.put("sign", sign);
		paMap.put("paysn", paysn);
		logger.info("棕熊回调参数:" + paMap.toString());
		String localSign = MD5.md5(platformid + number + money + tradetime + key_value);
		logger.info("棕熊支付回调本地sign:" + localSign + "   服务器sign:" + sign);
		if (localSign.toLowerCase().equals(sign) && "1".equals(status)) {
			logger.info("棕熊支付回调成功！");
			return true;
		}
		logger.info("棕熊支付回调失败！");
		return false;
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

		Map<String, String> bankMap = new HashMap<String, String>();
		bankMap.put("money", String.valueOf(amount));// 订单明细金额
		bankMap.put("number", order_no);// 订单号
		bankMap.put("paytype", pay_code);// 银行代码固定值:3 跳转棕熊收银台
		String html = bankPay(bankMap);
		return PayUtil.returnWYPayJson("success", "form", html, pay_url, "");
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

		Map<String, String> scanMap = new HashMap<String, String>();
		scanMap.put("money", String.valueOf(amount));// 订单明细金额
		scanMap.put("number", order_no);// 订单号
		scanMap.put("paytype", pay_code);// 银行代码固定值:3 跳转棕熊收银台

		String html = scanPay(scanMap);

		return PayUtil.returnPayJson("success", "1", userName, userName, amount, order_no, html);
	}

    @Override
    public String callback(Map<String, String> data) {
        // TODO Auto-generated method stub
        return null;
    }

}
