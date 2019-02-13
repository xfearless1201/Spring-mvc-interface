package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.json.JSONUtils;
import com.cn.tianxia.pay.gst.util.StringUtils;
import com.cn.tianxia.pay.jh.util.MerchantApiUtil;
import com.cn.tianxia.pay.jh.util.SimpleHttpUtils;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.ys.util.DateUtil;

import net.sf.json.JSONObject;

public class JHPayServiceImpl implements PayService {
	private final static Logger logger = LoggerFactory.getLogger(JHPayServiceImpl.class);

	private static String payKey;// 商户支付Key
	private static String zfbproductType;// 产品类型
	private static String zfbwapproductType;// 产品类型
	private static String wxproductType;// 产品类型
	private static String wxwapproductType;// 产品类型
	private static String qqproductType;// 产品类型
	private static String wyproductType;// 产品类型
	private static String ylproductType;// 产品类型
	private static String kjproductType;// 产品类型
	private static String productName;// 支付产品名称
	private static String returnUrl;// 页面通知地址
	private static String notifyUrl;// 后台异步通知地址
	private static String serverUrl;// 扫码请求地址
	private static String paySecret;//
	private static String quickPayGateWay;// 网银支付地址

	private String checkNumber;// 值为1 需要验证金额 值为0不需要验证金额

	public JHPayServiceImpl(Map<String, String> pmap) {
		JSONObject jo = new JSONObject().fromObject(pmap);
		if (null != pmap) {
			payKey = jo.get("payKey").toString();
			zfbproductType = jo.get("zfbproductType").toString();
			wxproductType = jo.get("wxproductType").toString();
			qqproductType = jo.get("qqproductType").toString();
			zfbwapproductType = jo.get("zfbwapproductType").toString();
			wxwapproductType = jo.get("wxwapproductType").toString();
			wyproductType = jo.get("wyproductType").toString();
			boolean dd = jo.get("ylproductType") == null ? false : true;
			if (dd) {
				ylproductType = jo.get("ylproductType").toString();
			}
			boolean ff = jo.get("kjproductType") == null ? false : true;
			if (ff) {
				kjproductType = jo.get("kjproductType").toString();
			}
			productName = jo.get("productName").toString();
			returnUrl = jo.get("returnUrl").toString();
			notifyUrl = jo.get("notifyUrl").toString();
			serverUrl = jo.get("serverUrl").toString();
			paySecret = jo.get("paySecret").toString();
			quickPayGateWay = jo.get("quickPayGateWay").toString();
		}

		// 支付宝二维码收款
		if (jo.containsKey("checkNumber")) {
			checkNumber = "1";
		} else {
			checkNumber = "0";
		}

	}

	public String ScanPay(Map<String, String> scanMap) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("payKey", payKey); // 商户支付Key
		paramMap.put("orderPrice", scanMap.get("orderPrice")); // 金额
		paramMap.put("outTradeNo", scanMap.get("outTradeNo"));// 订单号
		if ("true".equals(scanMap.get("mobile"))) {
			if ("WX".equals(scanMap.get("type")))
				paramMap.put("productType", getWAPProductType(wxwapproductType, scanMap.get("type")));
			if ("ZFB".equals(scanMap.get("type")))
				paramMap.put("productType", getWAPProductType(zfbwapproductType, scanMap.get("type")));
			if ("QQ".equals(scanMap.get("type")))
				paramMap.put("productType", getProductType(qqproductType, scanMap.get("type")));
			if ("KJ".equals(scanMap.get("type")))
				paramMap.put("productType", getProductType(kjproductType, scanMap.get("type")));
		} else {
			if ("WX".equals(scanMap.get("type")))
				paramMap.put("productType", getProductType(wxproductType, scanMap.get("type")));
			if ("ZFB".equals(scanMap.get("type")))
				paramMap.put("productType", getProductType(zfbproductType, scanMap.get("type")));
			if ("QQ".equals(scanMap.get("type")))
				paramMap.put("productType", getProductType(qqproductType, scanMap.get("type")));
			if ("YL".equals(scanMap.get("type")))
				paramMap.put("productType", getProductType(ylproductType, scanMap.get("type")));
			if ("KJ".equals(scanMap.get("type")))
				paramMap.put("productType", getProductType(kjproductType, scanMap.get("type")));
		}
		paramMap.put("orderTime", scanMap.get("orderTime"));// 时间
		paramMap.put("productName", productName);// 商品名称
		paramMap.put("orderIp", scanMap.get("orderIp"));// IP地址
		paramMap.put("returnUrl", scanMap.get("pageUrl"));// 返回页面路径
		paramMap.put("notifyUrl", notifyUrl);// 回调地址
		paramMap.put("remark", "remark");
		///// 签名及生成请求API的方法///
		String sign = MerchantApiUtil.getSign(paramMap, paySecret.trim());
		paramMap.put("sign", sign);
		System.out.println("请求参数：" + paramMap);
		String payResult = SimpleHttpUtils.httpPost(serverUrl.trim(), paramMap);
		System.out.println("响应结果：" + payResult);
		com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(payResult);
		Object resultCode = jsonObject.get("resultCode");// 返回码
		Object payMessage = jsonObject.get("payMessage");// 请求结果(请求成功时)
		Object errMsg = jsonObject.get("errMsg");// 错误信息(请求失败时)
		if ("0000".equals(resultCode.toString())) {// 请求成功
			if ((payMessage.toString()).toLowerCase().indexOf("html") > -1) {
				int a = (payMessage.toString()).toLowerCase().indexOf("http".toLowerCase());
				int b = (payMessage.toString()).toLowerCase().indexOf("method".toLowerCase());
				if (a > -1 && b > -1) {
					payMessage = payMessage.toString().substring(a, b - 2);
				} else {
					logger.info("错误信息地址为空" + payMessage);
					return null;
				}
			}
			logger.info("付款信息：\n" + payMessage);
			return payMessage.toString();
		} else {// 请求失败
			logger.info("错误信息：" + errMsg);
			return null;
		}
	}

	private Object getWAPProductType(String productType, String type) {
		if ("D0".equals(productType)) {
			if ("WX".equals(type))
				return 10000202;
			if ("ZFB".equals(type))
				return 20000202;
		} else if ("T0".equals(productType)) {
			if ("WX".equals(type))
				return 10000203;
			if ("ZFB".equals(type))
				return 20000203;
		} else if ("T1".equals(productType)) {
			if ("WX".equals(type))
				return 10000201;
			if ("ZFB".equals(type))
				return 20000201;
		}
		return null;
	}

	private Object getProductType(String productType, String type) {
		if ("D0".equals(productType)) {
			if ("WX".equals(type))
				return 10000102;
			if ("ZFB".equals(type))
				return 20000302;
			if ("QQ".equals(type))
				return 70000202;
			if ("YL".equals(type))
				return 60000103;
			if ("KJ".equals(type))
				return 40000502;
		} else if ("T0".equals(productType)) {
			if ("WX".equals(type))
				return 10000103;
			if ("ZFB".equals(type))
				return 20000303;
			if ("QQ".equals(type))
				return 70000203;
			if ("YL".equals(type))
				return 60000103;
			if ("KJ".equals(type))
				return 40000503;
		} else if ("T1".equals(productType)) {
			if ("WX".equals(type))
				return 10000101;
			if ("ZFB".equals(type))
				return 20000301;
			if ("QQ".equals(type))
				return 70000201;
			if ("YL".equals(type))
				return 60000101;
			if ("KJ".equals(type))
				return 40000501;
		}
		return null;
	}

	private Object getWYProductType(String productType) {
		if ("T1".equals(productType))
			return 50000101;
		if ("T0".equals(productType))
			return 50000103;
		if ("D0".equals(productType))
			return 50000102;
		return null;
	}

	private Object getKJProductType(String productType) {
		if ("T1".equals(productType))
			return 40000501;
		if ("T0".equals(productType))
			return 40000503;
		if ("D0".equals(productType))
			return 40000502;
		return null;
	}

	public String callback(Map<String, String> infoMap, HttpServletRequest request, HttpServletResponse response) {
		String localsign = infoMap.remove("sign");
		Map<String, Object> map = JSONUtils.toHashMap(infoMap);
		// 制作签名
		String sign = MerchantApiUtil.getSign(map, paySecret);
		if (sign.equals(localsign)) {
			return "success";
		}
		return null;
	}

	public String bankPay(Map<String, String> bankMap) {

		Map<String, Object> paramMap = new HashMap<String, Object>();
		if ("4".equals(bankMap.get("payMethod"))) {
			paramMap.put("productType", getWYProductType(wyproductType));
		} else {
			paramMap.put("productType", getKJProductType(kjproductType));
		}
		paramMap.put("payKey", payKey);// 商户支付Key
		paramMap.put("orderPrice", bankMap.get("orderPrice"));
		paramMap.put("outTradeNo", bankMap.get("outTradeNo"));
		paramMap.put("productName", productName);// 商品名称
		paramMap.put("orderIp", bankMap.get("orderIp"));// 下单IP
		paramMap.put("orderTime", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));// 订单时间
		paramMap.put("returnUrl", bankMap.get("pageUrl"));// 页面通知返回url
		paramMap.put("notifyUrl", notifyUrl); // 后台消息通知Url
		paramMap.put("remark", "remark");
		paramMap.put("sign", MerchantApiUtil.getSign(paramMap, paySecret));
		String payResult = SimpleHttpUtils.httpPost(quickPayGateWay, paramMap);
		System.out.println(payResult);
		logger.info("网银支付返回回来参数:" + payResult);
		return payResult;
	}

	public static void main(String[] args) {
		// Map<String, Object> paramMap = new HashMap<String, Object>();
		// paramMap.put("productType", 60000101);
		// paramMap.put("payKey", "08e5af2b475c4f39be1eba62a4d67f33");// 商户支付Key
		// paramMap.put("orderPrice", 100);
		// String orderNo = String.valueOf(System.currentTimeMillis());
		// paramMap.put("outTradeNo",orderNo);
		// paramMap.put("productName", "纸巾");// 商品名称
		// paramMap.put("orderIp", "127.0.0.1");// 下单IP
		// paramMap.put("orderTime", new
		// SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));// 订单时间
		// paramMap.put("returnUrl", "returnUrl");// 页面通知返回url
		// paramMap.put("notifyUrl", "returnUrl"); // 后台消息通知Url
		// paramMap.put("remark", "remark");
		// paramMap.put("sign", MerchantApiUtil.getSign(paramMap,
		// "20dbc3e2e8094e2b845eea52104ba4df"));
		// String payResult =
		// SimpleHttpUtils.httpPost("https://gateway.iexbuy.com/cnpPay/initPay",
		// paramMap);
		// System.out.println(payResult);
		// logger.info("网银支付返回回来参数:" + payResult);

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
		// int int_amount = (int) (amount);
		// 订单明细金额 分为单位
		bankMap.put("pageUrl", refereUrl);
		bankMap.put("orderPrice", String.valueOf(amount));
		bankMap.put("outTradeNo", order_no);// 订单号
		bankMap.put("payMethod", pay_code);// 网银类型支付
		bankMap.put("orderIp", ip);
		bankMap.put("orderTime", DateUtil.getCurrentDate("yyyyMMddHHmmss"));

		String html = bankPay(bankMap);
		return PayUtil.returnWYPayJson("success", "jsp", html, pay_url, "payhtml");
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
		// 订单明细金额 分为单位
		scanMap.put("pageUrl", refereUrl);// 页面返回地址
		scanMap.put("orderPrice", String.valueOf(amount));
		scanMap.put("outTradeNo", order_no);// 订单号
		scanMap.put("type", pay_code);// 网银类型支付
		scanMap.put("orderIp", ip);// 网银类型支付
		scanMap.put("orderTime", DateUtil.getCurrentDate("yyyyMMddHHmmss"));
		scanMap.put("mobile", mobile != null ? "true" : "false");// 是否WAP

		if ("kj".equals(pay_code)) {
			scanMap.put("payMethod", "");// 快捷类型

			String html = bankPay(scanMap);
			int prefix = html.indexOf("action=\"");
			String url = html.substring(prefix).split("\"")[1];
			
			//判断获取出来的是否是连接格式
			if(url.indexOf("http")>=0){
				return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, amount, order_no, url);
			}else{
				return PayUtil.returnPayJson("error", "4", "支付接口请求成功!", userName, amount, order_no, "");
			}
		}

		// 支付宝二维码收款
		DecimalFormat decimalFormat = new DecimalFormat("#########.##");
		if (pay_code.equals("ZFB") && !StringUtils.isNullOrEmpty(mobile) && checkNumber.equals("1")
				&& !check(decimalFormat.format(amount))) {
			return PayUtil.returnPayJson("error", "1", "输入金额有误，不能识别,只支持105,255,350,505,980,1505,2580,3500,4980",
					userName, amount, order_no, "");
		}

		String qrcode = ScanPay(scanMap);
		if (StringUtils.isNullOrEmpty(qrcode)) {
			logger.info("聚合扫码接口获取二维码异常！");
			return PayUtil.returnPayJson("error", "2", "支付接口获取二维码失败！", userName, amount, order_no, "");
		}
		if (StringUtils.isNullOrEmpty(mobile)) {
			return PayUtil.returnPayJson("success", "2", "支付接口请求成功!", userName, amount, order_no, qrcode);
		} else {
			return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, amount, order_no, qrcode);
		}
	}

	/**
	 * 验证金额
	 * 
	 * @param int_amount
	 * @return
	 */
	private boolean check(String int_amount) {
		String am = "55,105,255,505,980,1505,2580,3580,4980,5990,7990,9990";
		String amStr[] = am.split(",");
		for (int i = 0; i < amStr.length; i++) {
			if (int_amount.equals(amStr[i])) {
				return true;
			}
		}
		return false;
	}

    @Override
    public String callback(Map<String, String> data) {
        // TODO Auto-generated method stub
        return null;
    }

}
