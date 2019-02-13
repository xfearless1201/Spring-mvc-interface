package com.cn.tianxia.pay.impl;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.common.HttpClient;

import net.sf.json.JSONObject;

public class ZFZFPayServiceImpl implements PayService {
	private final static Logger logger = LoggerFactory.getLogger(ZFZFPayServiceImpl.class);
	private String payUrl ;// 掌付支付地址
	private String wx_payUrl ;// 微信公众号支付提交地址
	private String appid ;
	private String subject;
	private String body ;
	/** 通知地址 */
	private String tongbu_url ;
	 
    private String appname;
    private String appbs;
    private String appkey;
	
	public static void main(String[] args) {
		 HashMap<String, String> pmap = new HashMap<String, String>();
	     pmap.put("payUrl", "http://sanfang.yp178.com/dealpay.php");
	     pmap.put("wx_payUrl", "http://sanfang.yp178.com/dealpay_wx.php");
	     pmap.put("appid", "10280");      // 应用ID,在网站创建APP应用后获得
	     pmap.put("key","98b1396139f7b656a66d483eff3957a4");
	     pmap.put("subject", "");
	     pmap.put("body", "");
	     pmap.put("tongbu_url", "http://localhost:8087/JJF/Notify/ZFZFNotify.do");
	     
	     pmap.put("appname", "");    // 选择app时必填，如：王者荣耀、支付宝等
	     pmap.put("appbs", ""); // 应用唯一标识,选择app时必填如：com.chengzhen.YPTest
	     pmap.put("appkey", ""); 
	     System.out.println(JSONObject.fromObject(pmap).toString());
	}
	
	public ZFZFPayServiceImpl(Map<String, String> pmap) {
		if (pmap != null) {
			if (pmap.containsKey("payUrl")) {
				payUrl = pmap.get("payUrl");
			}
			if (pmap.containsKey("wx_payUrl")) {
				wx_payUrl = pmap.get("wx_payUrl");
			}
			if (pmap.containsKey("appid")) {
				appid = pmap.get("appid"); // 
			}
			if (pmap.containsKey("body")) {
				body = pmap.get("body"); // 
			}
			if (pmap.containsKey("subject")) {
				subject = pmap.get("subject"); // 
			}
			if (pmap.containsKey("tongbu_url")) {
				tongbu_url = pmap.get("tongbu_url"); // 异步回调地址
			}
			if (pmap.containsKey("appname")) {
				appname = pmap.get("appname");
			}
			if (pmap.containsKey("appbs")) {
				appbs = pmap.get("appbs");
			}
			if (pmap.containsKey("appkey")) {
				appkey = pmap.get("appkey");
			}
		}
	}
	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		return null;
	}

	@Override
	public JSONObject smPay(PayEntity payEntity) {
		String payCode = payEntity.getPayCode();
		double amt = payEntity.getAmount();// "8.02";// 订单金额
		DecimalFormat df=new DecimalFormat("##########");
		String amount = df.format(amt*100);
		String userName = payEntity.getUsername();
		String mobile = payEntity.getMobile();
		String orderNo = payEntity.getOrderNo();
		
		// 原始的
		Map<String, String> metaSignMap = new TreeMap<String, String>();
		
		metaSignMap.put("appid", appid);   // 应用ID,Int
		metaSignMap.put("orderid", orderNo); // 订单号  String(32)
		metaSignMap.put("subject", subject); // 商品名称  String(100)
		metaSignMap.put("body", body);    // 商品描述  String(200) 
		metaSignMap.put("fee",amount);     // 充值金额 fee money(100) 必填,单位：分
		metaSignMap.put("tongbu_url", tongbu_url); // 接收交易结果的通知地址,使用HTTP协议GET方式向此地址发送交易结果
		metaSignMap.put("cpparam", "");   // 透传参数 cpparam String(100) 选填,透传参数,向receiveurl发送交易结果时,将些参数原样返回. 
		metaSignMap.put("clientip", payEntity.getIp());       // 必填，请使用客户端真实IP地址
		metaSignMap.put("back_url",payEntity.getRefererUrl()); // 必填.支付结束后用户返回到的页面地址，不可带参数 (支付流程需要返回的时候有效).
		
		String sign = cryptMD5(appid+orderNo+amount+tongbu_url+appkey);
		metaSignMap.put("sign", sign);
		
		metaSignMap.put("paytype", payCode); // Int(1) 0或者空则跳转收银台,以下为直接调用支付的方式
		if (StringUtils.isBlank(mobile)) {
			return pcScanPay(metaSignMap,userName,amt,orderNo);
		}else{
			return wapScanPay(metaSignMap,userName,amt,orderNo);
		}
		
		
	}

    private JSONObject pcScanPay(Map<String, String> metaSignMap,String userName,double amount,String orderNo) {
		metaSignMap.put("sfrom", "pc"); //  app客户端请填写app，手机客户端请填写wap，pc客户端请填写pc
		String responseStr = "";
		try {
			responseStr = HttpClient.doGet(payUrl, metaSignMap, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
        logger.info("掌付支付扫码响应：" + responseStr);
        JSONObject resJson = JSONObject.fromObject(responseStr);
        if ("success".equals(resJson.getString("code"))) {
        	 return PayUtil.returnPayJson("success", "2", "支付接口请求成功!", userName, amount, orderNo,
        			 resJson.getString("msg"));
        }else{
        	return PayUtil.returnPayJson("error", "2", resJson.toString(), userName, amount, orderNo, "");
        }
	
	}

    private JSONObject wapScanPay(Map<String, String> metaSignMap,String userName,double amount,String orderNo) {
		metaSignMap.put("sfrom", "wap"); //  app客户端请填写app，手机客户端请填写wap，pc客户端请填写pc
		metaSignMap.put("mode", "");  // 选择app时必填，如：IOS，AND
		metaSignMap.put("appname", appname); // 选择app时必填，如：王者荣耀、支付宝等
		metaSignMap.put("appbs", appbs);  // 选择app时必填如：com.chengzhen.YPTest
		String responseStr = "";
		try {
			responseStr = HttpClient.doGet(payUrl, metaSignMap, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
        logger.info("掌付支付扫码响应：" + responseStr);
        JSONObject resJson = JSONObject.fromObject(responseStr);
        if ("success".equals(resJson.getString("code"))) {
        	 return PayUtil.returnPayJson("success", "3", "支付接口请求成功!", userName, amount, orderNo,
        			 resJson.getString("msg"));
        }else{
        	return PayUtil.returnPayJson("error", "3", resJson.toString(), userName, amount, orderNo, "");
        }
	}
	/**
     * MD5加密
     */
    public String cryptMD5(String str) {
        if (str == null || str.length() == 0) {
            throw new IllegalArgumentException("String to encript cannot be null or zero length");
        }
        StringBuffer hexString = new StringBuffer();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            byte[] hash = md.digest();
            for (int i = 0; i < hash.length; i++) {
                if ((0xff & hash[i]) < 0x10) {
                    hexString.append("0" + Integer.toHexString((0xFF & hash[i])));
                } else {
                    hexString.append(Integer.toHexString(0xFF & hash[i]));
                }
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hexString.toString();
    }
    
    private JSONObject getReturnJson(String status, String qrCode, String msg) {
        JSONObject json = new JSONObject();
        json.put("status", status);
        json.put("qrCode", qrCode);
        json.put("msg", msg);
        return json;
    }

    @Override
	public String callback(Map<String, String> infoMap) {
		String sign = infoMap.get("sign");
		infoMap.remove("paytype");
		infoMap.remove("cpparam");
		infoMap.remove("sign");
        StringBuilder sb = new StringBuilder();
        for (String key : infoMap.keySet()) {
            String value = String.valueOf(infoMap.get(key));
            sb.append(value);
        }
        sb.append(appkey);
        logger.info("验签内容signatureStr = " + sb.toString());
        String result = "";
        try {
            result = cryptMD5( sb.toString());
            logger.info("生成签名串：" + result);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("签名生成失败");
        }

        if (sign.equals(result)) {
            logger.info("验签成功");
            return "success";
        }
        logger.info("验签失败");
        return "fail";
    }
}
