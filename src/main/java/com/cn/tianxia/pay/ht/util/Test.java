package com.cn.tianxia.pay.ht.util;

import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import com.cn.tianxia.pay.mkt.util.HttpUtil;

/**
 * @author 汇通
 */
public class Test {
	String orderId;
	String money;
	static String user_Id = "11787646";
	static String key = "8d52e31683e0bc1f1ab654d9983b8d32";
	static String urlHome = "https://api.huitongvip.com/";

	public Test(String orderId, String money) {
		super();
		this.orderId = orderId;
		this.money = money;
	}

	public static void main(String[] args) {
		Test test = new Test(getNotSimple(8) + "", "1.00");
		/**
		 * 支付
		 */
		test.pay();
		/**
		 * 订单查询
		 */
//		test.query();
		/**
		 * 代付查询
		 */
//		test.df_query();
		/**
		 * 余额查询
		 */
//		test.queryMoney();
		/**
		 * 下单
		 */
//		test.order();
		/**
		 * 代付
		 */
//		test.dfm();
	}

	/**
	 * 代付
	 */
	public void dfm() {
		try {
			String url = urlHome + "remit.html";
			Map<String, String> map = new HashMap<>();
			map.put("merchant_code", user_Id);
			map.put("order_amount", "1.00");
			map.put("trade_no", orderId);
			map.put("order_time", DateUtils.format(new Date()));
			map.put("bank_code", "ICBC");
			map.put("account_name", "张三");
			map.put("account_number", "6212261911001799979");
			map.put("notify_url", "http://127.0.0.1:8080/gateway/pay_notify.html");
			System.out.println(post(map, url));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 支付
	 */
	public void pay() {
		try {
			String url = urlHome + "pay.html";
			Map<String, String> map = new HashMap<>();
			map.put("notify_url", "http://127.0.0.1:8080/gateway/pay_notify.html");
			map.put("return_url", "http://127.0.0.1/gateway/pay_notify.html");
			map.put("pay_type", "1");
			map.put("bank_code", "ICBC");
			map.put("merchant_code", user_Id);
			map.put("order_no", orderId);
			map.put("order_amount", "1.00");
			map.put("order_time", DateUtils.format(new Date()));
			map.put("customer_ip", "127.0.0.1");
			map.put("req_referer", "111");
			System.out.println(post(map, url));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 下单
	 */
	public void order() {
		try {
			String url = urlHome + "order.html";
			Map<String, String> map = new HashMap<>();
			map.put("notify_url", "http://127.0.0.1:8080/gateway/pay_notify.html");
			map.put("return_url", "http://127.0.0.1/gateway/pay_notify.html");
			map.put("pay_type", "1");
			map.put("bank_code", "ICBC");
			map.put("merchant_code", user_Id);
			map.put("order_no", orderId);
			map.put("order_amount", "1.00");
			map.put("order_time", DateUtils.format(new Date()));
			map.put("customer_ip", "127.0.0.1");
			map.put("req_referer", "111");
			System.out.println(post(map, url));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 订单查询接口
	 */
	public void query() {
		try {
			String url = urlHome + "query.html";
			Map<String, String> map = new HashMap<>();
			map.put("merchant_code", user_Id);
			map.put("order_no", orderId);
			map.put("now_date", DateUtils.format(new Date()));
			System.out.println(post(map, url));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 代付查询
	 */
	public void df_query() {
		try {
			String url = urlHome + "remit_query.html";
			Map<String, String> map = new HashMap<>();
			map.put("merchant_code", user_Id);
			map.put("trade_no", "2017042223324217612");
			// map.put("order_no", "17060514451503709156");
			map.put("now_date", DateUtils.format(new Date()));
			System.out.println(post(map, url));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 余额查询
	 */
	public void queryMoney() {
		try {
			String url = urlHome + "balance.html";
			Map<String, String> map = new HashMap<>();
			map.put("merchant_code", user_Id);
			map.put("query_time", DateUtils.format(new Date()));
			System.out.println(post(map, url));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String post(Map<String, String> map, String url) throws Exception {
		map.put("sign", Utils.getSign(map, key));
		if (map.containsKey("account_name")) {
			map.put("account_name", URLEncoder.encode(map.get("account_name"), "UTF-8"));
		}
//		String result = HttpRequestUtil.sendPost(url, map);
//		Map <String,Object> map2=new HashMap<>();
//		for (String name : map.keySet()) {  
//			map2.put(name, map.get(name));
//        }  
//		
//		String result =Https.doPostSSL(url, map2);
		
		String result =HttpUtil.HtmlFrom(url, map);
		return result;
	}

	/**
	 * 每次生成的len位数都不相同
	 * 
	 * @param param
	 * @return 定长的数字
	 */
	public static int getNotSimple(int len) {
		int[] param = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		Random rand = new Random();
		for (int i = param.length; i > 1; i--) {
			int index = rand.nextInt(i);
			int tmp = param[index];
			param[index] = param[i - 1];
			param[i - 1] = tmp;
		}
		int result = 0;
		for (int i = 0; i < len; i++) {
			result = result * 10 + param[i];
		}
		return result;
	}

	public static String getUrl(HttpServletRequest request) {
		try {
			String url = urlHome + "pay.html";
			Map<String, String> map = new HashMap<>();
			map.put("notify_url", "http://127.0.0.1:8080/gateway/pay_notify.html");
			map.put("return_url", "http://127.0.0.1/gateway/pay_notify.html");
			map.put("pay_type", "2");
			map.put("bank_code", request.getParameter("bank_code"));
			map.put("merchant_code", user_Id);
			map.put("order_no", getNotSimple(8) + "");
			map.put("order_amount", request.getParameter("order_amount"));
			map.put("order_time", DateUtils.format(new Date()));
			map.put("customer_ip", "127.0.0.1");
			map.put("req_referer", "111");
			url += "?";
			String sign = Utils.getSign(map, key);
			for (String key : map.keySet()) {
				url += key + "=" + map.get(key) + "&";
			}
			return url+"sign="+sign;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
}
