package com.cn.tianxia.pay.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.dc.util.HttpClientUtil;
import com.cn.tianxia.pay.gst.util.StringUtils;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.yx.util.CharSet;
import com.cn.tianxia.pay.yx.util.SHAUtils;
import com.cn.tianxia.pay.yx.util.UtilSign;

import net.sf.json.JSONObject;

/**
 * XIN支付
 * 
 * @author AKON
 *         {"body":"tianxia","charset":"UTF-8","merchantId":"100000000002350",
 *         "notifyUrl":"192.168.0.228:8080/JJF/PlatformPay/YXNotify.do",
 *         "paymentType":"1","service":"online_pay","title":"tianxia","signType"
 *         :"SHA","secret":
 *         "4b4aad5aaa15bfad568c68gg3gc850f6ed46740635715beb12d38ae4494d2e4e",
 *         "payUrl":"https://ebank.ztpo.cn"}
 * @TIME 2018年3月9日-下午9:33:52
 *
 * @DESCRIPYION
 */
public class YXPayServiceImpl implements PayService{
	private final static Logger logger = LoggerFactory.getLogger(YXPayServiceImpl.class);
	private String body;//商品描述
	private String charset;//编码方式
//	private String defaultbank;//网银代码，当支付方式为bankPay时，该值为空；支付方式为directPay时该值必传
//	private String isApp;//当该值传“app”时，表示app接入，返回二维码地址，需商户自行生成二维码；值为“web”时，表示web接入，直接在收银台页面上显示二维码
	private String merchantId;//
	private String notifyUrl;//
//	private String orderNo;//
	private String paymentType;//支付类型，固定值为1
//	private String paymethod;//支付方式，directPay：直连模式；bankPay：收银台模式
//	private String returnUrl;//
//	private String riskItem;//
	private String service;//固定值online_pay，表示网上支付
	private String title;//
//	private String totalFee;//
	private String signType;//签名方式 ：SHA
//	private String sign;//
	private String secret;//支付密钥
	private String payUrl;//支付地址https://ebank.ztpo.cn    +/payment/v1/order/{merchantId}-{orderNo}
	
	
	public YXPayServiceImpl(Map<String, String> pmap) {
		JSONObject jo = JSONObject.fromObject(pmap);
		if (null != pmap) {
			body = jo.get("body").toString();
			charset = jo.get("charset").toString();
			merchantId = jo.get("merchantId").toString();
			notifyUrl = jo.get("notifyUrl").toString();
			paymentType = jo.get("paymentType").toString();
			service = jo.get("service").toString();
			title = jo.get("title").toString();
			signType = jo.get("signType").toString();
			secret = jo.get("secret").toString();
			payUrl = jo.get("payUrl").toString();
		}
	}

	
	/**
	 * 扫码支付
	 * 
	 * @return
	 */
	public JSONObject scanPay(Map<String, String> scanMap) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("body", body);
		map.put("charset", charset);
		map.put("defaultbank", scanMap.get("defaultbank"));
		map.put("isApp", scanMap.get("isApp"));
		map.put("merchantId", merchantId);
		map.put("notifyUrl", notifyUrl);
		map.put("orderNo", scanMap.get("orderNo"));
		map.put("paymentType", paymentType);
		map.put("paymethod",scanMap.get("paymethod"));
		map.put("returnUrl", scanMap.get("returnUrl"));
		map.put("service", service);
		map.put("signType", signType);
		map.put("title", title);
		map.put("totalFee", scanMap.get("totalFee"));
		
		String signStr = getSign(map, secret);
		String sign = SHAUtils.sign(signStr,CharSet.UTF8);
		
		Map<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("body", map.get("body"));
		hashMap.put("charset", map.get("charset"));
		hashMap.put("defaultbank", map.get("defaultbank"));
		hashMap.put("isApp", map.get("isApp"));
		hashMap.put("merchantId", map.get("merchantId"));
		hashMap.put("notifyUrl", map.get("notifyUrl"));
		hashMap.put("orderNo", map.get("orderNo"));
		hashMap.put("paymentType", map.get("paymentType"));
		hashMap.put("paymethod", map.get("paymethod"));
		hashMap.put("returnUrl", map.get("returnUrl"));
		hashMap.put("service", map.get("service"));
		hashMap.put("signType", map.get("signType"));
		hashMap.put("title", map.get("title"));
		hashMap.put("totalFee", map.get("totalFee"));
		hashMap.put("sign", sign);
		JSONObject jsonObject = JSONObject.fromObject(hashMap);
		System.out.println(jsonObject.toString());
		String res = HttpClientUtil.doPost(payUrl+"/payment/v1/order/"+hashMap.get("merchantId")+"-"+hashMap.get("orderNo"), jsonObject.toString(), "UTF-8", "application/json");
		System.out.println("响应参数：" + res);
		JSONObject json = JSONObject.fromObject(res);
		if ("S0001".equals(json.get("respCode"))) {
			logger.info("启付通接口调用成功");
			
			return getReturnJson("success", json.get("codeUrl").toString(), "二维码获取成功！");
		} else {	
			return getReturnJson("error", "", json.get("respMessage").toString());
		}
		
	}
	
	public String bankPay(Map<String, String> bankMap) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("body", body);
		map.put("charset", charset);
		map.put("defaultbank", bankMap.get("defaultbank"));
		map.put("isApp", bankMap.get("isApp"));
		map.put("merchantId", merchantId);
		map.put("notifyUrl", notifyUrl);
		map.put("orderNo", bankMap.get("orderNo"));
		map.put("paymentType", paymentType);
		map.put("paymethod",bankMap.get("paymethod"));
		map.put("returnUrl", bankMap.get("returnUrl"));
		map.put("service", service);
//		map.put("signType", signType);
		map.put("title", title);
		map.put("totalFee", bankMap.get("totalFee"));
		
//		String signStr = getSign(map, secret);
		String sign = UtilSign.GetSHAstr(map, secret);
//		String sign = SHAUtils.sign(signStr,CharSet.UTF8);
		Map<String, Object> hashMap = new HashMap<String, Object>();
		hashMap.put("body", map.get("body"));
		hashMap.put("charset", map.get("charset"));
		hashMap.put("isApp", map.get("isApp"));
		hashMap.put("merchantId", map.get("merchantId"));
		hashMap.put("notifyUrl", map.get("notifyUrl"));
		hashMap.put("orderNo", map.get("orderNo"));
		hashMap.put("paymentType", map.get("paymentType"));
		hashMap.put("paymethod", map.get("paymethod"));
		hashMap.put("returnUrl", map.get("returnUrl"));
		hashMap.put("service", map.get("service"));
		hashMap.put("signType", map.get("signType"));
		hashMap.put("title", map.get("title"));
		hashMap.put("totalFee", Double.parseDouble(map.get("totalFee")));
		hashMap.put("sign", sign);
		String FormString = "<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form  id=\"actform\" name=\"actform\" method=\"post\" action=\""
				+ payUrl+"/payment/v1/order/"+hashMap.get("merchantId")+"-"+hashMap.get("orderNo")+"" + "\">";
		for (String key : hashMap.keySet()) {
			FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" + hashMap.get(key) + "'>\r\n";
		}
		FormString += "</form></body>";
		String html = FormString;
		System.out.println(html);
		return html;
	}
	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		Double totalFee = payEntity.getAmount();
		String orderNo = payEntity.getOrderNo();
		String returnUrl = payEntity.getRefererUrl();
		//*****************paymethod=directPay时候defaultbank不能为空***************************
		String defaultbank = payEntity.getPayCode();
		String paymethod = "directPay";
		//********************************************
		String isApp = "web";
		String pay_url = payEntity.getPayUrl();
		
		
		Map<String, String> bankMap = new HashMap<>();
		bankMap.put("orderNo", orderNo);
		bankMap.put("totalFee", String.valueOf(totalFee));
		bankMap.put("defaultbank", defaultbank);
		bankMap.put("returnUrl", returnUrl);
		bankMap.put("isApp", isApp);
		bankMap.put("paymethod", paymethod);
		
		String html = bankPay(bankMap);
		return PayUtil.returnWYPayJson("success", "form", html, pay_url, "");
	}

	@Override
	public JSONObject smPay(PayEntity payEntity) {
		Double totalFee = payEntity.getAmount();
		String orderNo = payEntity.getOrderNo();
		String returnUrl = payEntity.getRefererUrl();
		//*****************paymethod=directPay时候defaultbank不能为空***************************
		String defaultbank = payEntity.getPayCode();
		String paymethod = "directPay";
		//********************************************
		String isApp = "app";
		String pay_url = payEntity.getPayUrl();
		String userName = payEntity.getUsername();
		String mobile = payEntity.getMobile();
		
		Map<String, String> scanMap = new HashMap<>();
		scanMap.put("orderNo", orderNo);
		scanMap.put("totalFee", String.valueOf(totalFee));
		scanMap.put("defaultbank", defaultbank);
		scanMap.put("returnUrl", returnUrl);
		scanMap.put("isApp", isApp);
		scanMap.put("paymethod", paymethod);
		JSONObject rjson = null;
		String html = "";
		
		rjson = scanPay(scanMap);

		// 手机 or pc 返回类型
		if (!StringUtils.isNullOrEmpty(mobile)) {
			return PayUtil.returnPayJson("success", "1", "支付接口请求成功!", userName, totalFee, orderNo, html);
		} else {
			if (!"success".equals(rjson.getString("status"))) {
				return PayUtil.returnPayJson("error", "2", rjson.getString("msg"), userName, totalFee, orderNo, "");
			}
			String qrcode = rjson.getString("qrCode");
			if (rjson.containsKey("qrCode") && !"null".equals(qrcode) && !StringUtils.isNullOrEmpty(qrcode)) {
				return PayUtil.returnPayJson("success", "2", "支付接口请求成功!", userName, totalFee, orderNo, qrcode);
			} else {
				return PayUtil.returnPayJson("error", "2", rjson.toString(), userName, totalFee, orderNo, "");
			}

		}
	}
	public static String getSign(Map<String, String> map, String sign) {
		if(map.containsKey("signType")){
			map.remove("signType");
		}
		if(map.containsKey("key")){
			map.remove("key");
		}
		Set<String> set = new TreeSet<String>();
		StringBuffer sb = new StringBuffer();
		for (String s : map.keySet()) {
			set.add(s);
		}
		int i = 0;
		for (String s : set) {
			if (i < 1) {
				sb.append(s + "=" + map.get(s));
			} else {
				sb.append("&" + s + "=" + map.get(s));
			}
			i++;
		}
		sb.append(sign);
		System.out.println(sb.toString());
		return sb.toString();
	}
	public JSONObject getReturnJson(String status, String qrCode, String msg) {
		JSONObject json = new JSONObject();
		json.put("status", status);
		json.put("qrCode", qrCode);
		json.put("msg", msg);
		return json;
	}
	
	@Override
	public String callback(Map<String, String> infoMap) {
		String sign = infoMap.get("sign");
		infoMap.remove("sign");
		infoMap.remove("signType");
		String signStr = getSign(infoMap, secret);
		String sig =  SHAUtils.sign(signStr,CharSet.UTF8);
		if (sign.equals(sig)) {
			return "success";
		}
		return "";
	}
	public static void main(String[] args) {
		Map<String, String> pmap = new HashMap<>();
		pmap.put("body", "tianxia");
		pmap.put("charset", "UTF-8");
		pmap.put("merchantId", "100000000002350");
		pmap.put("notifyUrl", "http://192.168.0.228:8080/JJF/PlatformPay/YXNotify.do");
		pmap.put("paymentType", "1");
		pmap.put("service", "online_pay");
		pmap.put("title", "tianxia");
		pmap.put("signType", "SHA");
		pmap.put("secret", "4b4aad5aaa15bfad568c68gg3gc850f6ed46740635715beb12d38ae4494d2e4e");
		pmap.put("payUrl", "https://ebank.ztpo.cn");
		YXPayServiceImpl yx = new YXPayServiceImpl(pmap);
		Map<String, String> bankMap = new HashMap<>();
		bankMap.put("defaultbank", "CCB");
		bankMap.put("isApp", "web");
		bankMap.put("orderNo", "order"+new Date().getTime());
		bankMap.put("paymethod", "directPay");
		bankMap.put("returnUrl", "http://192.168.0.228:8080/JJF/PlatformPay/YXNotify.do");
		bankMap.put("totalFee", "0.05");
		String res = yx.bankPay(bankMap);
		
	}
}
