package com.cn.tianxia.pay.gst.util;

import java.util.Date;

public class Test {
	
	
	public static void main(String[] args) {
		
		String url="http://pay.gstpay.vip/gstpay/gateway/pay.html";
		String input_charset="UTF-8";
		KeyValues kvs = new KeyValues();
		kvs.add(new KeyValue("input_charset", input_charset));
		String amount= AES.encrypt("1.00", "1b4727d1d81d3c9e83af9bf61c539cbe");
		kvs.add(new KeyValue("amount", "1.00"));
		kvs.add(new KeyValue("transid", "20161208102420106741"));
		kvs.add(new KeyValue("bitch_no", "201612081022221177706961"));
		kvs.add(new KeyValue("currentDate","2016-12-05 21:45:11"));
		kvs.add(new KeyValue("bank_name", ""));
		kvs.add(new KeyValue("account_name", ""));
		kvs.add(new KeyValue("account_number", ""));
		kvs.add(new KeyValue("remark", "1485"));
		//商户号
		kvs.add(new KeyValue("merchant_code", "1485"));
		String param=kvs.getSortUrl(input_charset);
		//填key
		String key="1b4727d1d81d3c9e83af9bf61c539cbe";
		System.out.println(param);
		String sign = kvs.sign(key, "UTF-8").toUpperCase();
		for(int i=0;i<2;i++){
			sign=MD5Encoder.encode(sign, input_charset);
		}
//		param=param+"&sign="+sign;
//		String result=HttpRequestUtil.sendPost(url, param);
//		System.out.println(result);
		
			String orderNo="GFT"+ System.currentTimeMillis();
			   String currentDate = DateUtils.format(new Date());
		   KeyValues kvs1 = new KeyValues();
		   kvs1.add(new KeyValue(AppConstants.INPUT_CHARSET, input_charset));
		   kvs1.add(new KeyValue(AppConstants.NOTIFY_URL, "www.baidu.com"));
		   kvs1.add(new KeyValue(AppConstants.RETURN_URL, "www.baidu.com"));
		   kvs1.add(new KeyValue(AppConstants.PAY_TYPE, "2"));
		   kvs1.add(new KeyValue(AppConstants.BANK_CODE, ""));
		   kvs1.add(new KeyValue(AppConstants.MERCHANT_CODE, "1485"));
		   kvs1.add(new KeyValue(AppConstants.ORDER_NO, orderNo));
		   kvs1.add(new KeyValue(AppConstants.ORDER_AMOUNT, amount));
		   kvs1.add(new KeyValue(AppConstants.ORDER_TIME, currentDate));
		   kvs1.add(new KeyValue(AppConstants.REQ_REFERER, "116.226.209.138"));
		   kvs1.add(new KeyValue(AppConstants.CUSTOMER_IP, "116.226.209.138"));
		   kvs1.add(new KeyValue(AppConstants.RETURN_PARAMS, "0|A|th|pay"));
	        String sign1 = kvs1.sign(key, input_charset);
		
			StringBuilder sb = new StringBuilder();
	        sb.append(url);
	        URLUtils.appendParam(sb, AppConstants.INPUT_CHARSET, input_charset, false);
	        URLUtils.appendParam(sb, AppConstants.RETURN_URL, "www.baidu.com", input_charset);
	        URLUtils.appendParam(sb, AppConstants.NOTIFY_URL, "www.baidu.com", input_charset);
	        URLUtils.appendParam(sb, AppConstants.PAY_TYPE, "2");
	        URLUtils.appendParam(sb, AppConstants.BANK_CODE, "");
	        URLUtils.appendParam(sb, AppConstants.MERCHANT_CODE, "1485");
	    
	        URLUtils.appendParam(sb, AppConstants.ORDER_NO, orderNo);
	        URLUtils.appendParam(sb, AppConstants.ORDER_AMOUNT, amount);
	        URLUtils.appendParam(sb, AppConstants.ORDER_TIME, currentDate);
	        URLUtils.appendParam(sb, AppConstants.REQ_REFERER, "116.226.209.138", input_charset);
	        URLUtils.appendParam(sb, AppConstants.CUSTOMER_IP, "116.226.209.138");
	        URLUtils.appendParam(sb, AppConstants.RETURN_PARAMS, "0|A|th|pay", input_charset);
	        URLUtils.appendParam(sb, AppConstants.SIGN, sign1);
	        System.out.println(sb.toString());
	}
}
