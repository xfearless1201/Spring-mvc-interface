package com.cn.tianxia.pay.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.util.IPTools;

import net.sf.json.JSONObject;

public class YYBPayServiceImpl implements PayService {

	private final static Logger logger = LoggerFactory.getLogger(YYBPayServiceImpl.class);
	private static String pid;
	private static String url;
	private static String key;
	private static String serverUrl;
	private static String m;
	private static String bk;

	public YYBPayServiceImpl(Map<String, String> pmap) {
		JSONObject jo = new JSONObject().fromObject(pmap);
		if (null != pmap) {
			// 商户APPID
			pid = jo.get("pid").toString();
			// 支付成功后跳回网址
			url = jo.get("url").toString();
			// key值
			key = jo.get("key").toString();
			// 网关默认模板金额横排1竖排0
			m = jo.get("m").toString();
			// 网关支付模板边框是否显示1不显示
			bk = jo.get("bk").toString();
			// 请求地址
			serverUrl = jo.get("serverUrl").toString();
		}
	}

	public String scanPay(Map<String, String> scanMap) {
		// TODO Auto-generated method stub
		String data = scanMap.get("data").trim();
		String money = scanMap.get("money").trim();
		String lb = scanMap.get("lb").trim();
		String asynURL = scanMap.get("asynURL").trim();
		// 构建请求参数
		Map<String, String> resquestMap = new LinkedHashMap<>();
		resquestMap.put("pid", pid);
		resquestMap.put("money", money);
		resquestMap.put("data", data);
		resquestMap.put("url", asynURL);
		resquestMap.put("lb", lb);
		resquestMap.put("m", m);
		resquestMap.put("bk", bk);
		// form表单请求到接口地址
		List<String> keys = new ArrayList<String>(resquestMap.keySet());
		for (int i = 0; i < keys.size(); i++) {
			String name = (String) keys.get(i);
			String value = (String) resquestMap.get(name);
			if (com.cn.tianxia.pay.gst.util.StringUtils.isNullOrEmpty(value)) {
				logger.info("删除:" + name);
				resquestMap.remove(name);
			}
		}

		String FormString = "<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form  id=\"actform\" name=\"actform\" method=\"post\" action=\""
				+ serverUrl + "\">";
		for (String key : resquestMap.keySet()) {
			FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" + resquestMap.get(key) + "'>\r\n";
		}
		FormString += "</form></body>";

		String html = FormString;
		logger.info("优云宝支付表单:" + html);
		return html;
	}

	/**
	 * formatString() : 字符串格式化方法
	 */
	public static String formatString(String text) {
		return (text == null) ? "" : text.trim();
	}

	public boolean callback(HttpServletRequest request) {
		// TODO Auto-generated method stub
		String returnKey = request.getParameter("key");
		if (key.equals(returnKey)) {
			logger.info("回调key值校验正确");
			return true;

		} else {
			logger.info("回调key值校验失败");
			return false;
		}

	}

	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		// TODO Auto-generated method stub
		return null;
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
		scanMap.put("data", order_no);// 订单号
		scanMap.put("lb", pay_code);
		scanMap.put("asynURL", refereUrl);// 同步地址
		String html = scanPay(scanMap);

		return PayUtil.returnPayJson("success", "1", "支付接口请求成功!", userName, amount, order_no, html);
	}

    @Override
    public String callback(Map<String, String> data) {
        // TODO Auto-generated method stub
        return null;
    }
}
