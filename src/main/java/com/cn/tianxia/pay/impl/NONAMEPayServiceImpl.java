package com.cn.tianxia.pay.impl;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.CryptoUtil;
import com.cn.tianxia.common.HttpClient;
import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.service.PayService;

import net.sf.json.JSONObject;

public class NONAMEPayServiceImpl implements PayService {
	private final static Logger logger = LoggerFactory.getLogger(NONAMEPayServiceImpl.class);
	
	private String merchantCode;// 商户id
	private String md5Key;      // 扫码密钥
	private String model;    // 模块名
	private String deviceNo;   // 设备号,门店号或收银设备ID
	private String goodsName;  // 商品名称,最大20个字符
	private String goodsExplain; // 商品描述
	private String ext; // 商户任意输入，将在异步通知中原样返回
	private String noticeUrl; // 异步通知地址
	private String scanUrl; // 扫码支付地址
	private String goodsMark; // 商品标记
	
	public NONAMEPayServiceImpl(Map<String, String> pmap) {
		if (pmap != null) {
			if (pmap.containsKey("merchantCode")) {
				merchantCode = pmap.get("merchantCode");
			}
			if (pmap.containsKey("md5Key")) {
				md5Key = pmap.get("md5Key");
			}
			if (pmap.containsKey("model")) {
				model = pmap.get("model");
			}
			if (pmap.containsKey("deviceNo")) {
				deviceNo = pmap.get("deviceNo"); // 
			}
			if (pmap.containsKey("goodsName")) {
				goodsName = pmap.get("goodsName"); // 
			}
			if (pmap.containsKey("goodsExplain")) {
				goodsExplain = pmap.get("goodsExplain"); // 
			}
			if (pmap.containsKey("ext")) {
				ext = pmap.get("ext"); // 
			}
			if (pmap.containsKey("noticeUrl")) {
				noticeUrl = pmap.get("noticeUrl"); // 
			}
			if (pmap.containsKey("scanUrl")) {
				scanUrl = pmap.get("scanUrl"); // 
			}
			if (pmap.containsKey("goodsMark")) {
				goodsMark = pmap.get("goodsMark"); // 
			}
		}
	}
	public static void main(String[] args) {
		 Map<String, String> map = new HashMap<>();
		 //*********扫码支付*************************
	     map.put("merchantCode", "1000001731");  // 商户id
	     map.put("md5Key", "SrR9jwvEI96k20o9");  // 扫码密钥
	     map.put("model", "QR_CODE");    // 模块名
	     map.put("deviceNo", "tianxia88");   // 设备号,门店号或收银设备ID
	     map.put("goodsName", "充值"); // 商品名称,最大20个字符
	     map.put("goodsExplain", "游戏充值"); // 商品描述
	     map.put("ext", "我是谁，我的支付商名字是啥"); // 商户任意输入，将在异步通知中原样返回
	     
	     map.put("scanUrl", "http://120.78.142.240:10000/payment-pre-interface/scan/pay.do"); 
	     map.put("noticeUrl", "http://txw.tx8899.com/WNS/Notify/NONAMENotify.do"); // 异步通知地址
	     map.put("goodsMark", "without mark"); // 商品标记
	     
	     System.out.println(JSONObject.fromObject(map).toString());
	}

	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject smPay(PayEntity payEntity) {
		String mobile = payEntity.getMobile();
		String payCode = payEntity.getPayCode();
		String orderNo = payEntity.getOrderNo();
		Map<String, String> paramMap = new HashMap();
        paramMap.put("model", model);    // 模块名
        paramMap.put("merchantCode", merchantCode);   // 商户号
        paramMap.put("outOrderId", orderNo);  // 商户订单号
        paramMap.put("deviceNo", deviceNo);   // 设备号
        
        // 正整数
        DecimalFormat df = new DecimalFormat("#");
        String price = String.valueOf(df.format(payEntity.getAmount()*100)) ;
        paramMap.put("amount", price);  // 支付金额	Long单位分,只能为正整数，最小为1
        paramMap.put("goodsName", goodsName); // 商品名称,最大20个字符
        paramMap.put("goodsExplain", goodsExplain); // 商品描述
        paramMap.put("ext", ext); // 商户任意输入，将在异步通知中原样返回
        paramMap.put("orderCreateTime", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));// 格式为yyyyMMddHHmmss
        
        paramMap.put("noticeUrl", noticeUrl); // 异步通知地址
        paramMap.put("goodsMark", goodsMark); // 商品标记
        
        paramMap.put("payChannel", payCode);// 渠道编码 ,21-微信，30-支付宝，31-QQ钱包，32-银联扫码，33-京东扫码
        paramMap.put("ip", payEntity.getIp()); // app和网页支付提交用户端ip
        paramMap.put("sign", getSign(paramMap)); // 详情见签名机制

        logger.info("请求支付中心下单接口,请求数据:" + paramMap,toString());
		
        JSONObject r_json = null;
		try {
			String js = HttpClient.doPost(scanUrl, paramMap, "UTF-8", 20000, 20000);
			r_json = JSONObject.fromObject(js);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	   if (StringUtils.isBlank(mobile)) {
        	// pc端
            if ("00".equals(r_json.getString("code"))) {
            	JSONObject data = r_json.getJSONObject("data");
                return PayUtil.returnPayJson("success", "2", "支付接口请求成功!", payEntity.getUsername(), payEntity.getAmount(), orderNo,
                		data.getString("url"));
            } else {
                return PayUtil.returnPayJson("error", "2", r_json.getString("msg"), payEntity.getUsername(), payEntity.getAmount(), orderNo, "");
            }
        } else {
            // 手机端
        	if ("00".equals(r_json.getString("code"))) {
        		JSONObject data = r_json.getJSONObject("data");
        		return PayUtil.returnPayJson("success", "3", "支付接口请求成功!", payEntity.getUsername(), payEntity.getAmount(), orderNo,
                		data.getString("url"));
            } else {
                return PayUtil.returnPayJson("error", "3", r_json.getString("msg"), payEntity.getUsername(), payEntity.getAmount(), orderNo, "");
            }
        }
	}

	
	private String getSign(Map<String, String> infoMap) {
	   StringBuilder sb = new StringBuilder();
	   sb.append("amount" + "=" + String.valueOf(infoMap.get("amount")) + "&");
	   sb.append("merchantCode" + "=" + String.valueOf(infoMap.get("merchantCode")) + "&");
	   sb.append("noticeUrl" + "=" + String.valueOf(infoMap.get("noticeUrl")) + "&");
	   sb.append("orderCreateTime" + "=" + String.valueOf(infoMap.get("orderCreateTime")) + "&");
	   sb.append("outOrderId" + "=" + String.valueOf(infoMap.get("outOrderId")) + "&");
	   sb.append("payChannel" + "=" + String.valueOf(infoMap.get("payChannel")) + "&");
	   
       sb.append("KEY=").append(md5Key);
       logger.info("验签内容signatureStr = " + sb.toString());
        
       String signature = null;
       try {
           signature = CryptoUtil.cryptMD5(sb.toString());
           logger.info("生成签名串：" + signature);
       } catch (Exception e) {
            e.printStackTrace();
            logger.error("签名生成失败");
       }
       return signature.toUpperCase();
	}


	@Override
	public String callback(Map<String, String> infoMap) {
	   StringBuilder sb = new StringBuilder();
	   sb.append("instructCode" + "=" + String.valueOf(infoMap.get("instructCode")) + "&");
	   sb.append("merchantCode" + "=" + String.valueOf(infoMap.get("merchantCode")) + "&");
	   sb.append("outOrderId" + "=" + String.valueOf(infoMap.get("outOrderId")) + "&");
	   sb.append("totalAmount" + "=" + String.valueOf(infoMap.get("totalAmount")) + "&");
	   sb.append("transTime" + "=" + String.valueOf(infoMap.get("transTime")) + "&");
	   
	   sb.append("KEY=").append(md5Key);
	   String signatureStr = sb.toString();
	   logger.info("验签内容 = " + signatureStr);
	      
       String result = "";
        try {
            result = CryptoUtil.cryptMD5(signatureStr);
            logger.info("生成签名串：" + result);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("签名生成失败");
        }

        String sign = infoMap.get("sign");
        if (sign.equals(result)) {
            logger.info("验签成功");
            return "success";
        }
        logger.info("验签失败");
        return "fail";
	}
}
