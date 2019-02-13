package com.cn.tianxia.pay.impl;

import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.jh.util.MD5Util;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.util.IPTools;

import net.sf.json.JSONObject;

public class RJPayServiceImpl implements PayService {
	private String version;
	private String merchantid;
	private String notifyurl;
	private String attach;
	private String sign;
	private String payurl;

	private final static Logger logger = LoggerFactory.getLogger(RJPayServiceImpl.class);

	public RJPayServiceImpl(Map<String, String> pmap) {
		JSONObject jo = JSONObject.fromObject(pmap);
		if (null != pmap) {
			version = jo.get("version").toString();
			merchantid = jo.get("merchantid").toString();
			notifyurl = jo.get("notifyurl").toString();
			attach = jo.get("attach").toString();
			sign = jo.get("sign").toString();
			payurl = jo.get("payurl").toString();
		}
	}

	/**
	 * 网银接口
	 * 
	 * @param bankMap
	 * @return
	 */
	public String bankPay(Map<String, String> bankMap) {
		// 同步URL
		String returnurl = bankMap.get("returnurl");
		// 订单号
		String merordernum = bankMap.get("merordernum");
		// 订单金额
		Double tranamt = Double.parseDouble(bankMap.get("tranamt"));
		// 精确到小数点后两位，例如 10.24
		DecimalFormat df = new DecimalFormat("#.00");
		String dd = df.format(tranamt);
		// 业务代码
		String bussid = bankMap.get("bussid");
		// 银行编号
		String bankid = bankMap.get("bankid");

		Map<String, String> paramMap = new LinkedHashMap<String, String>();
		paramMap.put("version", version);
		paramMap.put("merchantid", merchantid);
		paramMap.put("tranamt", dd + "");
		paramMap.put("merordernum", merordernum);
		paramMap.put("notifyurl", notifyurl);
		paramMap.put("returnurl", returnurl);
		String md5_sign = getSign(paramMap, sign);
		paramMap.put("sign", md5_sign);
		paramMap.put("bussid", bussid);
		paramMap.put("bankid", bankid);
		paramMap.put("attach", attach);

		String FormString = "<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form  id=\"actform\" name=\"actform\" method=\"post\" action=\""
				+ payurl + "\">";
		for (String key : paramMap.keySet()) {
			FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" + paramMap.get(key) + "'>\r\n";
		}
		return FormString += "</form></body>";
	}

	/**
	 * 扫码接口
	 * 
	 * @param scanMap
	 * @return
	 */
	public String scanPay(Map<String, String> scanMap) {
		// 同步URL
		String returnurl = scanMap.get("returnurl");
		// 订单号
		String merordernum = scanMap.get("merordernum");
		// 订单金额
		Double tranamt = Double.parseDouble(scanMap.get("tranamt"));
		// 精确到小数点后两位，例如 10.24
		DecimalFormat df = new DecimalFormat("#.00");
		String dd = df.format(tranamt);

		// 业务代码
		String bussid = scanMap.get("bussid");
		// 银行编号
		// String bankid = scanMap.get("bankid");

		Map<String, String> paramMap = new LinkedHashMap<String, String>();
		paramMap.put("version", version);
		paramMap.put("merchantid", merchantid);
		paramMap.put("tranamt", dd + "");
		paramMap.put("merordernum", merordernum);
		paramMap.put("notifyurl", notifyurl);
		paramMap.put("returnurl", returnurl);
		String md5_sign = getSign(paramMap, sign);
		paramMap.put("sign", md5_sign);
		paramMap.put("bussid", bussid);
		paramMap.put("attach", attach);

		String FormString = "<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form  id=\"actform\" name=\"actform\" method=\"post\" action=\""
				+ payurl + "\">";
		for (String key : paramMap.keySet()) {
			FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" + paramMap.get(key) + "'>\r\n";
		}
		logger.info("润京支付表单:" + FormString + "</form></body>");
		return FormString += "</form></body>";
	}

	/**
	 * 回调方法
	 * 
	 * @param infoMap
	 * @return
	 */
	@Override
	public String callback(Map<String, String> infoMap) {
		Map<String, String> paramMap = new LinkedHashMap<String, String>();
		paramMap.put("merchantid", infoMap.get("merchantid"));
		paramMap.put("respcode", infoMap.get("respcode"));
		paramMap.put("ordernum", infoMap.get("ordernum"));
		paramMap.put("merordernum", infoMap.get("merordernum"));
		paramMap.put("tranamt", infoMap.get("tranamt"));
		paramMap.put("bussid", infoMap.get("bussid"));
		String serverSign = infoMap.get("sign");

		String md5_sign = getSign(paramMap, sign);

		if (md5_sign.equals(serverSign)) {
			logger.info("润京支付签名成功！");
			return "success";
		}
		logger.info("润京支付签名失败！");
		return "";
	}

	/**
	 * md5
	 * 
	 * @param paramMap
	 * @param sign
	 * @return
	 */
	private static String getSign(Map<String, String> paramMap, String sign) {
		LinkedHashMap<String, Object> smap = new LinkedHashMap<String, Object>(paramMap);
		StringBuffer stringBuffer = new StringBuffer();
		for (Map.Entry<String, Object> m : smap.entrySet()) {
			Object value = m.getValue();
			if (value != null && StringUtils.isNotBlank(String.valueOf(value))) {
				stringBuffer.append(m.getKey()).append("=").append(m.getValue()).append("&");
			}
		}
		stringBuffer.delete(stringBuffer.length() - 1, stringBuffer.length());

		String argPreSign = stringBuffer.append("&").append(sign).toString();
		logger.info("润京字符原串:" + argPreSign);
		String signStr = MD5Util.encode(argPreSign).toLowerCase();
		return signStr;
	}

	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		Double amount = payEntity.getAmount();
		String order_no = payEntity.getOrderNo();
		String refereUrl = payEntity.getRefererUrl();
		String pay_code = payEntity.getPayCode();
//		String userName = payEntity.getUsername();
		String pay_url = payEntity.getPayUrl();

		Map<String, String> bankMap = new HashMap<>();
		bankMap.put("returnurl", refereUrl);
		bankMap.put("merordernum", order_no);// 订单号
		bankMap.put("tranamt", String.valueOf(amount));// 金额
		bankMap.put("bussid", "bank");
		bankMap.put("bankid", pay_code);
		String html = bankPay(bankMap);

		if (com.amazonaws.util.StringUtils.isNullOrEmpty(html)) {
			logger.info("信汇支付表单生成异常！");
			return PayUtil.returnWYPayJson("error", "", "", "", "");
		}
		return PayUtil.returnWYPayJson("success", "form", html, pay_url, "");
	}

	@Override
	public JSONObject smPay(PayEntity payEntity) {
		Double amount = payEntity.getAmount();
		String order_no = payEntity.getOrderNo();
		String refereUrl = payEntity.getRefererUrl();
		String pay_code = payEntity.getPayCode();
		String userName = payEntity.getUsername();

		Map<String, String> scanMap = new HashMap<>();
		scanMap.put("returnurl", refereUrl);
		scanMap.put("merordernum", order_no);// 订单号
		scanMap.put("tranamt", String.valueOf(amount));// 金额
		scanMap.put("bussid", pay_code);

		String form = scanPay(scanMap);

		if (com.cn.tianxia.pay.gst.util.StringUtils.isNullOrEmpty(form)) {
			return PayUtil.returnPayJson("error", "1", "扫码表单生成异常！", userName, amount, order_no, "");
		}
		return PayUtil.returnPayJson("success", "1", "支付接口请求成功!", userName, amount, order_no, form);
	}

}
