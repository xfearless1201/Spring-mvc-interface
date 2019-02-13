package com.cn.tianxia.pay.impl;

import com.cn.tianxia.common.CryptoUtil;
import com.cn.tianxia.common.HttpClient;
import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.service.PayService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.applet.Main;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ZNYPayServiceImpl implements PayService {
    private final static Logger logger = LoggerFactory.getLogger(ZNYPayServiceImpl.class);

    private String uid;// 商户编号
    private String token;// 商户token
    private String goodsname;//  订单名称
    private String orderuid;//  商品详情


    private String payUrl;//  下单地址
    private String notify_url;// 异步通知地址


    public ZNYPayServiceImpl(Map<String, String> pmap) {
        if (pmap != null) {
            if (pmap.containsKey("uid")) {
                uid = pmap.get("uid");
            }
            if (pmap.containsKey("goodsname")) {
                goodsname = pmap.get("goodsname");
            }
            if (pmap.containsKey("orderuid")) {
                orderuid = pmap.get("orderuid");
            }
            if (pmap.containsKey("notify_url")) {
                notify_url = pmap.get("notify_url");
            }
            if (pmap.containsKey("payUrl")) {
                payUrl = pmap.get("payUrl");
            }
            if (pmap.containsKey("token")) {
                token = pmap.get("token");
            }

        }
    }

    public static void main(String[] args) {
        Map<String, String> map = new HashMap<>();
        map.put("uid", "26380815968");  // 商户编号
        map.put("token", "0a15055c36892b770805b8eb1f60b829");  // 商户密钥
        map.put("goodsname", "charge");  // 订单名称
        map.put("orderuid", "game charge");  // 商品详情
        map.put("payUrl", "http://api.068063.cn:88/pay/action");  //  下单地址
        map.put("notify_url", "http://182.16.110.186:8080/XPJ/Notify/ZNYNotify.do");  //  异步通知地址
        System.out.println(JSONObject.fromObject(map).toString());
    }
    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        return null;
    }

    @Override
    public JSONObject smPay(PayEntity payEntity) {

        String mobile = payEntity.getMobile();
        String payCode = payEntity.getPayCode();
        String orderNo = payEntity.getOrderNo();
        // 订单金额，单位元，小数位最末位不能是0
        DecimalFormat df = new DecimalFormat("0.##");
        String price = String.valueOf(df.format(payEntity.getAmount())) ;

        Map<String, String> paramMap = new TreeMap<>();
        paramMap.put("uid", uid);    // 商户编号
        paramMap.put("orderid", orderNo);    // 商户订单号
        paramMap.put("istype", payCode);    // 付款方式编号，10001 支付宝 20001 微信
        paramMap.put("price", price);    // 订单金额，单位元，小数位最末位不能是0；
        paramMap.put("goodsname", goodsname);    // 订单名称（描述）
        paramMap.put("orderuid", orderuid);    // 商品详情
        paramMap.put("notify_url", notify_url);    //  异步通知地址
        paramMap.put("return_url", payEntity.getRefererUrl());    //  支付结果跳转地址
        paramMap.put("format", "json");    //  web 跳转到我们支付页，json（默认) 获取 json 支付页信息，可自定义支付页 return_url 不在起作用

        paramMap.put("key", getSign(paramMap));  // sign
        logger.info("请求支付中心下单接口,请求数据:" + paramMap,toString());

        JSONObject r_json = null;
        try {
            String js = HttpClient.doPost(payUrl, paramMap, "UTF-8", 20000, 20000);
            logger.info("post支付请求返回结果" + js);
            r_json = JSONObject.fromObject(js);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (StringUtils.isBlank(mobile)) {
            // pc端
            if ("200".equals(r_json.getString("code"))) {
            	JSONObject data = r_json.getJSONObject("data");
                String realprice = data.getString("realprice");
                double d = Double.parseDouble(realprice);
                return PayUtil.returnPayJson("success", "2", "支付接口请求成功!", payEntity.getUsername(), d, orderNo,
                		data.getString("qrcode"));
            } else {
                return PayUtil.returnPayJson("error", "2", r_json.getString("msg"), payEntity.getUsername(), payEntity.getAmount(), orderNo, "");
            }
        } else {
            // 手机端
            if ("200".equals(r_json.getString("code"))) {
            	JSONObject data = r_json.getJSONObject("data");
                String realprice = data.getString("realprice");
                double d = Double.parseDouble(realprice);
                return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", payEntity.getUsername(), d, orderNo,
                		data.getString("qrcode"));
            } else {
                return PayUtil.returnPayJson("error", "4", r_json.getString("msg"), payEntity.getUsername(), payEntity.getAmount(), orderNo, "");
            }
        }
    }

    private String getSign(Map<String,String> paramMap) {
        StringBuilder sb = new StringBuilder();
        String goodsname = paramMap.get("goodsname");
        String istype = paramMap.get("istype");
        String notify_url = paramMap.get("notify_url");
        String orderid = paramMap.get("orderid");
        String orderuid = paramMap.get("orderuid");
        String price = paramMap.get("price");
        String return_url = paramMap.get("return_url");
        String token = this.token;
        String uid = paramMap.get("uid");

        sb.append(goodsname).append(istype).append(notify_url).append(orderid).append(orderuid).append(price).append(return_url).append(token).append(uid);
        logger.info("验签内容signatureStr = " + sb.toString());

        String signature = null;
        try {
            signature = CryptoUtil.cryptMD5(sb.toString());
            logger.info("生成签名串：" + signature);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("签名生成失败");
        }
        return signature;
    }

    @Override
    public String callback(Map<String,String> infoMap) {
    	
    	 StringBuilder sb = new StringBuilder();
         String orderid = infoMap.get("orderid");
         String orderuid = infoMap.get("orderuid");
         String ordno = infoMap.get("ordno");
         String price = infoMap.get("price");
         String realprice = infoMap.get("realprice");
         String token = this.token;
         String key = infoMap.get("key");
         
         sb.append(orderid).append(orderuid).append(ordno).append(price).append(realprice).append(token);
         logger.info("验签内容signatureStr = " + sb.toString());
         String signStr = CryptoUtil.cryptMD5(sb.toString());
         logger.info("生成签名串：" + signStr);
         
         if (key.equals(signStr.toLowerCase())) {
        	 logger.info("验签成功");
        	 return "success";
		 }else {
			logger.info("验签失败");
			return "fail";
		}
    }
}
