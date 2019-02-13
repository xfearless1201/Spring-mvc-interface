package com.cn.tianxia.pay.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

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

import net.sf.json.JSONObject;

public class LYGPayServiceImpl implements PayService {
	/**
	 * 
	 */
	// 商户支付Key
	private  String payKey;
	// 回调地址
	private  String notifyUrl;
	// paySecret
	private  String paySecret;
	// 网银接口地址
	private  String b2cPayUrl;
	// 交易内容
	private  String productName;
	// 网银类型
	private  String wyproductType;
	// 银行编码
	private  String bankAccountType;
	// 扫码地址
	private  String cnpPayUrl;
	// wx类型
    private  String wxproductType;
    // zfb类型
    private  String zfbproductType;
    // qq类型
    private  String qqproductType;
    // zfb手机端类型
    private  String zfbwapproductType;
    // kl类型
    private  String kjproductType;
    // kl手机端类型
    private  String kjwapproductType;
    // jd类型
    private  String jdproductType;
    // yl类型
    private  String ylproductType;
    //快捷支付地址
    private String quickGateWayPayUrl;
    
	private final static Logger logger = LoggerFactory.getLogger(LYGPayServiceImpl.class);

	public LYGPayServiceImpl(Map<String, String> pmap) {
		JSONObject jo = JSONObject.fromObject(pmap);
		if (null != pmap) {
		    payKey = jo.getString("payKey");
		    notifyUrl = jo.getString("notifyUrl");
		    paySecret = jo.getString("paySecret");
		    b2cPayUrl = jo.getString("b2cPayUrl");
		    productName = jo.getString("productName");
		    wyproductType = jo.getString("wyproductType");
		    bankAccountType = jo.getString("bankAccountType");
		    cnpPayUrl = jo.getString("cnpPayUrl");
		    wxproductType = jo.getString("wxproductType");
		    zfbproductType = jo.getString("zfbproductType");
		    qqproductType = jo.getString("qqproductType");
		    zfbwapproductType = jo.getString("zfbwapproductType");
		    jdproductType = jo.getString("jdproductType");
		    ylproductType = jo.getString("ylproductType");
		    kjproductType = jo.getString("kjproductType");
		    kjwapproductType = jo.getString("kjwapproductType");
		    quickGateWayPayUrl = jo.getString("quickGateWayPayUrl");
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

		Map<String, String> bankMap = new HashMap<>();
		bankMap.put("outTradeNo", order_no);// 订单号
		bankMap.put("orderPrice", String.valueOf(amount));// 金额
		bankMap.put("returnUrl", refereUrl);
		bankMap.put("orderIp", ip);
		bankMap.put("bankCode", pay_code);
		String html = bankPay(bankMap);
		return PayUtil.returnWYPayJson("success", "form", html, pay_url, "");
	}
	
	public String bankPay(Map<String, String> bankMap) {

	    Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("payKey", payKey);// 商户支付Key
        paramMap.put("orderPrice", bankMap.get("orderPrice")); //支付金额    
        paramMap.put("outTradeNo", bankMap.get("outTradeNo"));
        paramMap.put("productType",getWYProductType(wyproductType));//B2C T0支付
        Date orderTime = new Date();// 订单时间
        String orderTimeStr = new SimpleDateFormat("yyyyMMddHHmmss").format(orderTime);// 订单时间
        paramMap.put("orderTime", orderTimeStr);
        paramMap.put("productName", productName);// 商品名称
        paramMap.put("orderIp", bankMap.get("orderIp")); 
        paramMap.put("bankCode", bankMap.get("bankCode"));//银行编码
        paramMap.put("bankAccountType", bankAccountType);//银行编码
        paramMap.put("returnUrl", bankMap.get("returnUrl"));
        paramMap.put("notifyUrl", notifyUrl); 
        paramMap.put("remark", "");
        ///// 签名及生成请求API的方法///  
        String sign = MerchantApiUtil.getSign(paramMap, paySecret);
        paramMap.put("sign", sign);
        System.out.println(b2cPayUrl);
        System.out.println(paramMap);
//        String payResult = SimpleHttpUtils.httpPost(b2cPayUrl, paramMap);
//        System.out.println(payResult);
        String FormString = "<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form  id=\"actform\" name=\"actform\" method=\"post\" action=\"" 
                + b2cPayUrl + "\">"; 
        for (String key : paramMap.keySet()) { 
            FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" + paramMap.get(key) + "'>\r\n"; 
        } 
        FormString += "</form></body>"; 
        System.out.println(FormString);
        return FormString;
    }
	
	private Object getWYProductType(String productType) {
        if ("T1".equals(productType))
            return 50000101;
        if ("T0".equals(productType))
            return 50000103;
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

		Map<String, String> scanMap = new HashMap<>();
		scanMap.put("outTradeNo", order_no);// 订单号
        scanMap.put("orderPrice", String.valueOf(amount));// 金额
        scanMap.put("returnUrl", refereUrl);
        scanMap.put("orderIp", ip);
        scanMap.put("type", pay_code);
        scanMap.put("mobile", mobile);
        if (pay_code.equals("KJ")) {
        	scanMap.put("bankCardNo", (String)payEntity.getExtendMap().get("bankCardNo"));
        	
            String qrcode = scanKJPay(scanMap);
            return PayUtil.returnPayJson("success", "1", "支付接口请求成功!", userName, amount, order_no, qrcode);
        }
		JSONObject json = scanPay(scanMap);
		if ("success".equals(json.getString("status"))) {
		    if (StringUtils.isNullOrEmpty(mobile)) {
		        return PayUtil.returnPayJson("success", "2", "支付接口请求成功!", userName, amount, order_no,
		                json.getString("qrCode"));
	        } else {
	            return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, amount, order_no,
	                    json.getString("qrCode"));
	        }
        } else {
            return PayUtil.returnPayJson("error", "4", json.getString("msg"), userName, amount, order_no, "");
        }
	}
	
	private String scanKJPay(Map<String, String> scanMap) {
	    Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("payKey", payKey);// 商户支付key
        paramMap.put("orderPrice", scanMap.get("orderPrice")); //订单金额
        paramMap.put("outTradeNo", scanMap.get("outTradeNo"));// 商户支付订单号
        if ("mobile".equals(scanMap.get("mobile"))) {
            if ("KJ".equals(scanMap.get("type")))
                paramMap.put("productType", getWAPProductType(kjwapproductType, scanMap.get("type")));
        } else {
            if ("KJ".equals(scanMap.get("type")))
                paramMap.put("productType", getProductType(kjproductType, scanMap.get("type")));
        }
        Date orderTime = new Date();// 订单时间
        String orderTimeStr = new SimpleDateFormat("yyyyMMddHHmmss").format(orderTime);//订单时间
        paramMap.put("orderTime", orderTimeStr);
        paramMap.put("payBankAccountNo", scanMap.get("bankCardNo"));// 银行卡号
        paramMap.put("productName",productName);// 支付产品
        paramMap.put("orderIp",  scanMap.get("orderIp"));
        paramMap.put("returnUrl", scanMap.get("returnUrl"));
        paramMap.put("notifyUrl", notifyUrl);
        paramMap.put("remark", "");  
        SortedMap<String, Object> smap = new TreeMap<String, Object>(paramMap);
        StringBuilder sb = new StringBuilder(); 
        for (Map.Entry<String, Object> m : smap.entrySet()) {
            Object value = m.getValue();
            if (value != null && !StringUtils.isNullOrEmpty(String.valueOf(value))) {
                sb.append(m.getKey()).append("=").append(value).append("&");
            }
        }
        ///// 签名及生成请求API的方法///
        String sign = MerchantApiUtil.getSign(paramMap, paySecret);
        paramMap.put("sign", sign);
        System.out.println(quickGateWayPayUrl);
        System.out.println(paramMap);
        String FormString = "<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form  id=\"actform\" name=\"actform\" method=\"post\" action=\"" 
                + quickGateWayPayUrl + "\">"; 
        for (String key : paramMap.keySet()) { 
            FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" + paramMap.get(key) + "'>\r\n"; 
        } 
        FormString += "</form></body>"; 
        System.out.println(FormString);
        return FormString;
    }

    public JSONObject scanPay(Map<String, String> scanMap) {
	    Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("payKey", payKey);// 商户支付Key
        paramMap.put("orderPrice", scanMap.get("orderPrice")); //订单金额
        paramMap.put("outTradeNo", scanMap.get("outTradeNo"));//商户支付订单编号
        if ("mobile".equals(scanMap.get("mobile"))) {
            if ("ZFB".equals(scanMap.get("type")))
                paramMap.put("productType", getWAPProductType(zfbwapproductType, scanMap.get("type")));
        } else {
            if ("WX".equals(scanMap.get("type")))
                paramMap.put("productType", getProductType(wxproductType, scanMap.get("type")));
            if ("ZFB".equals(scanMap.get("type")))
                paramMap.put("productType", getProductType(zfbproductType, scanMap.get("type")));
            if ("QQ".equals(scanMap.get("type")))
                paramMap.put("productType", getProductType(qqproductType, scanMap.get("type")));
            if ("YL".equals(scanMap.get("type")))
                paramMap.put("productType", getProductType(ylproductType, scanMap.get("type")));
            if ("JD".equals(scanMap.get("type")))
                paramMap.put("productType", getProductType(jdproductType, scanMap.get("type")));
        }
        Date orderTime = new Date();// 订单时间
        String orderTimeStr = new SimpleDateFormat("yyyyMMddHHmmss").format(orderTime);// 订单时间
        paramMap.put("orderTime", orderTimeStr);
        paramMap.put("productName", productName);//支付产品名称
        System.out.println(orderTime);
        paramMap.put("orderIp", scanMap.get("orderIp"));
        paramMap.put("returnUrl", scanMap.get("returnUrl"));
        paramMap.put("notifyUrl", notifyUrl);
        paramMap.put("remark", "");
        ///// 签名及生成请求API的方法////  
        String sign = MerchantApiUtil.getSign(paramMap, paySecret);
        paramMap.put("sign", sign);  
        System.out.println(cnpPayUrl);
        System.out.println(paramMap); 
        String payResult = SimpleHttpUtils.httpPost(cnpPayUrl, paramMap);
        System.out.println(payResult);
        com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(payResult);
        if("0000".equals(jsonObject.get("resultCode"))){
            String payMessage = jsonObject.get("payMessage").toString();//返回码
            return getReturnJson("success", payMessage, "获取链接成功");
        }else{
            return getReturnJson("error", "", jsonObject.get("errMsg").toString());
        }
    }
    
    public JSONObject getReturnJson(String status, String qrCode, String msg) {
        JSONObject json = new JSONObject();
        json.put("status", status);
        json.put("qrCode", qrCode);
        if (StringUtils.isNullOrEmpty(msg)) {
            json.put("msg", "");
        } else {
            json.put("msg", msg);
        }
        return json;
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
                return 60000102;
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
	private Object getWAPProductType(String productType, String type) {
        if ("D0".equals(productType)) {
            if ("KJ".equals(type))
                return 40000702;
            if ("ZFB".equals(type))
                return 20000202;
        } else if ("T0".equals(productType)) {
            if ("KJ".equals(type))
                return 40000703;
            if ("ZFB".equals(type))
                return 20000203;
        } else if ("T1".equals(productType)) {
            if ("KJ".equals(type))
                return 40000701;
            if ("ZFB".equals(type))
                return 20000201;
        }
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

    @Override
    public String callback(Map<String, String> data) {
        // TODO Auto-generated method stub
        return null;
    }
}