package com.cn.tianxia.pay.impl;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.HttpClient;
import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.service.PayService;

import net.sf.json.JSONObject;

public class DYTPayServiceImpl implements PayService {
	private final static Logger logger = LoggerFactory.getLogger(DYTPayServiceImpl.class);
	private  String payUrl ;  // 店员通 智能云支付地址
	private  String mchId ;     // 商户id
	private  String appId ;
	private  String subject ; // 
	private  String body ;
	private  String notifyUrl ;//
	private  String productId ;
	
	private  String reqKey;
	private  String resKey;

	public DYTPayServiceImpl(Map<String, String> pmap) {
		if (pmap != null) {
			if (pmap.containsKey("payUrl")) {
				payUrl = pmap.get("payUrl");
			}
			if (pmap.containsKey("mchId")) {
				mchId = pmap.get("mchId");
			}
			if (pmap.containsKey("appId")) {
				appId = pmap.get("appId");
			}
			if (pmap.containsKey("subject")) {
				subject = pmap.get("subject"); // 
			}
			if (pmap.containsKey("body")) {
				body = pmap.get("body"); // 
			}
			if (pmap.containsKey("notifyUrl")) {
				notifyUrl = pmap.get("notifyUrl"); // 
			}
			if (pmap.containsKey("reqKey")) {
				reqKey = pmap.get("reqKey"); // 
			}
			if (pmap.containsKey("resKey")) {
				resKey = pmap.get("resKey"); // 
			}
			if (pmap.containsKey("productId")) {
				productId = pmap.get("productId"); // 
			}
		}
	}
	
	public static void main(String[] args) {
		 HashMap<String, String> pmap = new HashMap<String, String>();
	     pmap.put("payUrl", "http://47.75.36.94:3020/api/pay/create_order");
	     pmap.put("mchId", "20000000");  // 商户ID
	     pmap.put("appId", "918ea8a3abc545868d43d193bbbf051d");
	     pmap.put("subject", "lottery");
	     pmap.put("body", "nobody knows what this is");
	     pmap.put("notifyUrl", "http://txw.tx8899.com/WNS/Notify/DYTNotify.do"); 
	     pmap.put("reqKey", "F5Owd7m2ILuQF4TwvSXLvYFmYaHvJwcwc0zeZY8EFfvHqUti");
	     pmap.put("resKey", "JZ9LVj35CPckkpouLqL6BxOL09MtOaD8pwSf6Kpt3FouRMJh");
	     pmap.put("productId", "");
	     System.out.println(JSONObject.fromObject(pmap).toString());
	}
	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		String payCode = payEntity.getPayCode();
		JSONObject paramMap = new JSONObject();
        paramMap.put("mchId", mchId);                       // 商户ID
        paramMap.put("appId", appId);
        paramMap.put("mchOrderNo", payEntity.getOrderNo());           // 商户订单号
        paramMap.put("passageId", "1");   // 通道ID，当商户类型为平台账户时该参数必填
        
        paramMap.put("channelId", "gomepay_b2c2"); // 支付渠道ID, WX_NATIVE,ALIPAY_WAP
        // 不能带小数
        DecimalFormat df = new DecimalFormat("#");
        String price = String.valueOf(df.format(payEntity.getAmount()*100)) ;
        paramMap.put("amount", price);      // 支付金额,单位分
        paramMap.put("currency", "cny");                    // 币种, cny-人民币
        paramMap.put("clientIp", payEntity.getIp());        // 用户地址,IP或手机号
        paramMap.put("device", "WEB");                      // 设备
        paramMap.put("subject", subject);  
        paramMap.put("body", body);
        paramMap.put("notifyUrl", notifyUrl);         // 回调URL
        paramMap.put("param1", "");                         // 扩展参数1
        paramMap.put("param2", "");                         // 扩展参数2
        JSONObject extra = new JSONObject();
        extra.put("bankId", payCode);
        paramMap.put("extra", extra.toString());  // 附加参数

        String reqSign = getSign(paramMap);
        paramMap.put("sign", reqSign);   // 签名
        String reqData = "params=" + paramMap.toString();
        logger.info("请求支付中心下单接口,请求数据:" + paramMap.toString());
        
//        String formStr = buildForm(paramMap, this.payUrl);// HttpUtil.RequestForm(payUrl, params);
//        System.out.println(formStr);
//        logger.info("支付form表单：" + formStr);
        //reqData = "params=" + paramMap.toString();
        Map<String, String> pp = new HashMap();
        pp.put("params", paramMap.toString());
        
        String  res = "";
        try {
		    res= HttpClient.doGet(payUrl, pp, "UTF-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        //String requestForm2 = com.cn.tianxia.pay.mob.util.HttpUtil.RequestForm(payUrl, pp);
        JSONObject r_json = JSONObject.fromObject(res);
        if ("SUCCESS".equals(r_json.get("retCode"))) {
        	return PayUtil.returnWYPayJson("success", "jsp",r_json.getString("payUrl"), this.payUrl, "payhtml");
		}else{
			return PayUtil.returnWYPayJson("error", "jsp","", this.payUrl, "payhtml");
		}
		
	}

	private String getSign(Map<String, String> infoMap) {
		Map<String, String> sortMap = new TreeMap<String, String>(new Comparator<String>() {
			@Override
			public int compare(String str1, String str2) {
				return str1.compareTo(str2);
			}
		});
	   sortMap.putAll(infoMap);
	   StringBuilder sb = new StringBuilder();
        for (String key : sortMap.keySet()) {
            if ("sign".equalsIgnoreCase(key)) {
                continue;
            }
            String value = String.valueOf(infoMap.get(key));
            if (StringUtils.isBlank(value)) {
                continue;
            }
            sb.append(key + "=" + value + "&");
        }
        sb.append("key=").append(this.reqKey);
        logger.info("验签内容signatureStr = " + sb.toString());
        
        String signature = null;
        try {
            signature = cryptMD5(sb.toString());
            // logger.info("生成签名串：" + signature);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("签名生成失败");
        }
        return signature.toUpperCase();
	}

	@Override
	public JSONObject smPay(PayEntity payEntity) {
		String mobile = payEntity.getMobile();
		String payCode = payEntity.getPayCode();
		String orderNo = payEntity.getOrderNo();
		
		Map<String, String> paramMap = new HashMap();
        paramMap.put("mchId", mchId);                       // 商户ID
        paramMap.put("appId", appId);
        paramMap.put("mchOrderNo", payEntity.getOrderNo());           // 商户订单号
        // paramMap.put("passageId", "");   // 通道ID，当商户类型为平台账户时该参数必填
        
        paramMap.put("channelId", payCode); // 支付渠道ID, WX_NATIVE,ALIPAY_WAP
        // 不能带小数
        DecimalFormat df = new DecimalFormat("#");
        String price = String.valueOf(df.format(payEntity.getAmount()*100)) ;
        paramMap.put("amount", price);      // 支付金额,单位分
        paramMap.put("currency", "cny");                    // 币种, cny-人民币
        paramMap.put("clientIp", payEntity.getIp());        // 用户地址,IP或手机号
        paramMap.put("device", "WEB");                      // 设备
        paramMap.put("subject", subject);  
        paramMap.put("body", body);
        paramMap.put("notifyUrl", notifyUrl);         // 回调URL
        paramMap.put("param1", "");                         // 扩展参数1
        paramMap.put("param2", "");                         // 扩展参数2

        JSONObject extra = new JSONObject();
        if("WX_NATIVE".equals(payCode)){
        	extra.put("productId", productId);  
        	paramMap.put("extra", extra.toString());  // 附加参数
        }
//      extra.put("banksn", "622298703087324");   // 银行卡号
//	    extra.put("biztype", "01");
//	    extra.put("buyername", "张三");
//	    extra.put("buyerlinkinfo", "");
       
        String reqSign = getSign(paramMap);
        paramMap.put("sign", reqSign);   // 签名
        String reqData = "params=" + paramMap.toString();
        logger.info("请求支付中心下单接口,请求数据:" + reqData);
	
        JSONObject r_json = null;
		try {
			String js = HttpClient.doGet(payUrl+"?"+reqData,null, "UTF-8");
			r_json = JSONObject.fromObject(js);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        if (StringUtils.isBlank(mobile)) {
        	// pc端
            if ("SUCCESS".equals(r_json.getString("retCode"))) {
                return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", payEntity.getUsername(), payEntity.getAmount(), orderNo,
                        r_json.getString("payUrl"));
            } else {
                return PayUtil.returnPayJson("error", "4", r_json.getString("msg"), payEntity.getUsername(), payEntity.getAmount(), orderNo, "");
            }
        } else {
            // 手机端
        	if ("SUCCESS".equals(r_json.getString("retCode"))) {
                return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", payEntity.getUsername(), payEntity.getAmount(), orderNo,
                        r_json.getString("payUrl"));
            } else {
                return PayUtil.returnPayJson("error", "4", r_json.getString("msg"), payEntity.getUsername(), payEntity.getAmount(), orderNo, "");
            }
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

    @Override
	public String callback(Map<String, String> infoMap) {
            return "success";
    }
    
	 public String buildForm(Map<String, String> paramMap, String payUrl) {
	        // 待请求参数数组
	        String FormString = "<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form  id=\"actform\" name=\"actform\" method=\"post\" action=\""
	                + payUrl + "\">";
	        for (String key : paramMap.keySet()) {
	            FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" + paramMap.get(key) + "'>\r\n";
	        }
	        FormString += "</form></body>";

	        return FormString;
	}
    
}
