package com.cn.tianxia.pay.yjh.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;

/**
 * 工具类
 * 
 * @author devin <br/>
 *         2017年8月26日
 */
public class Util {
	/** 日期格式 */
	public static final SimpleDateFormat date_fmt = new SimpleDateFormat("yyMMddHHmmssSSS");

	/** random */
	private static final Random random = new Random();

	/**
	 * 判断字符串是否为空
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isBlank(String s) {
		return StringUtils.isBlank(s);
	}

	/**
	 * 判断map非空
	 * 
	 * @param m
	 * @return
	 */
	public static boolean isEmpty(Map<?, ?> m) {
		return null == m || 0 == m.size();
	}

	/**
	 * 计算md5值
	 * 
	 * @param txt
	 * @param charset
	 * @return
	 */
	public static String md5(String txt, String charset) {
		try {
			return isBlank(txt) ? txt : DigestUtils.md5Hex(txt.getBytes(charset));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String md5(String txt) {
		return md5(txt, Config.CHARSET);
	}

	/** service */
	// 移除参数map中的空值和签名参数
	public static Map<String, String> paraFilter(Map<String, String> paraMap) {
		Map<String, String> retMap = new HashMap<String, String>();
		if (isEmpty(paraMap)) {
			return retMap;
		}

		String value = null;
		for (String key : paraMap.keySet()) {
			value = paraMap.get(key);
			if (StringUtils.isBlank(value) || key.equalsIgnoreCase("sign")) {
				continue;
			}

			retMap.put(key, value);
		}

		return retMap;
	}

	// 生成签名
	public static String getSign(Map<String, String> map, String md5key) {
		if (isEmpty(map)) {
			throw new IllegalArgumentException("参数map为空");
		}

		List<String> keys = new ArrayList<String>(map.keySet());
		Collections.sort(keys);
		StringBuilder sb = new StringBuilder();
		String key = "", value = "";
		for (int i = 0; i < keys.size(); i++) {
			key = keys.get(i);
			value = map.get(key);
			if (key.equals("sign") || isBlank(value)) { // 忽略sign和空值
				continue;
			}
			sb.append(key).append("=").append(map.get(key)).append("&");
		}

		sb.setLength(sb.length() - 1); // remove '&'
		String querystr = sb.toString();
		return md5(querystr + md5key, Config.CHARSET).toUpperCase();
	}

	/**
	 * 从http请求中提取参数列表
	 * 
	 * @return
	 */
	public static Map<String, String> getRequestParams(HttpServletRequest request) {
		Map<String, String> params = new HashMap<String, String>();
		Map<?, ?> requestParams = request.getParameterMap();
		for (Iterator<?> iter = requestParams.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
			}

			params.put(name, valueStr);
		}

		return params;
	}

	/**
	 * 生成随机的订单号
	 */
	public static String createOrderId() {
		return String.format("test_%s%03d", date_fmt.format(new Date()), random.nextInt(999));
	}

	/**
	 * 打印结果到页面
	 * 
	 * @param ret
	 *            返回结果
	 * @param out
	 *            JspWriter
	 * @throws IOException
	 */
	public static void write(ApiResult ret, JspWriter out) throws IOException {
		out.println("平台返回结果：" + ret);
		out.println("<br/>是否成功：" + (ret.isSuccess() ? "是" : "否"));
	}

	public static void main(String args[]) {
		String s = "buyer_id=20151019135246420106278&charset_name=UTF-8&dt_order=2016-06-27 10:30:51&id_no=510105199501151017&id_type=身份证&info_order=iMac一部&is_import=1&money_order=3&name_goods=iMac&no_order=test146598003273&notify_url=http://192.168.1.106:8080/sdk_java_utf8/notify.jsp&partner_id=20151019135246420106278&pay_type=&platform=&sign_type=MD5&timestamp=1466994651&url_order=http://your.website.com/xxx&url_return=http://192.168.1.106:8080/sdk_java_utf8/return.jsp&user_name=test&valid_order=36000&version=2";
		System.out.println(md5(s + Config.KEY));
		System.out.println(md5(s + Config.KEY, Config.CHARSET));
		// System.out.println(base64Decode(base64Encode(s)));
	}
}