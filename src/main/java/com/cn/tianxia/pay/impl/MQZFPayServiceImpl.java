package com.cn.tianxia.pay.impl;

import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.mqzf.util.HttpUtil;
import com.cn.tianxia.pay.mqzf.util.MD5;
import com.cn.tianxia.pay.service.PayService;

import net.sf.json.JSONObject;

/**
 * 免签支付
 * 
 * @author tx001
 * @date 2018-06-25
 */
public class MQZFPayServiceImpl implements PayService {
	private final static Logger logger = LoggerFactory.getLogger(KJFPayServiceImpl.class);

	private String pay_url ;//= "https://www.uspays.com/Pay_Index.html";//支付地址
	private String pay_memberid ;//= "10005";// 商户号
	private String md5_key ;//= "fp9kzfhchbxidy3bxbvptx8ylpd6xiv6"; // 密钥
	private String pay_notifyurl ;//= "http://www.baidu.com/";// 异步回调地址
	private String pay_productname ;//= "pay";// 商品名称

	public MQZFPayServiceImpl() {

	}

	public MQZFPayServiceImpl(Map<String, String> pmap) {
		JSONObject json = JSONObject.fromObject(pmap);
		this.pay_url = json.getString("pay_url");
		this.pay_memberid = json.getString("pay_memberid");
		this.md5_key = json.getString("md5_key");
		this.pay_notifyurl = json.getString("pay_notifyurl");
		this.pay_productname = json.getString("pay_productname");
		
	}

	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject smPay(PayEntity payEntity) {
		String userName = payEntity.getUsername();
		String pay_orderid = payEntity.getOrderNo();//"MQ" + new SimpleDateFormat("yyMMddHHmmssSSS").format(new Date());// 订单号
		String pay_applydate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());// 提交时间
		String pay_bankcode = payEntity.getPayCode();// 支付方式
		String mobile = payEntity.getMobile();
		
		String pay_callbackurl = payEntity.getRefererUrl();// 页面通知地址
		double pay_amount = payEntity.getAmount();// 订单金额
		String pay_attach = "nothing";// 附加字段
		
		Map<String, String> params = new TreeMap<>();
		params.put("pay_memberid", pay_memberid);
		params.put("pay_orderid", pay_orderid);
		params.put("pay_applydate", pay_applydate);
		params.put("pay_bankcode", pay_bankcode);
		params.put("pay_notifyurl", pay_notifyurl);
		params.put("pay_callbackurl", pay_callbackurl);
		params.put("pay_amount", String.valueOf(pay_amount));
		params.put("pay_attach", pay_attach);
		params.put("pay_productname", pay_productname);
		
		String result = null;
		
		try {
			String pay_md5sign = generateMd5(params);// MD5签名
			params.put("pay_md5sign", pay_md5sign);
			//请求支付接口
			result = HttpUtil.post(pay_url, params);
			logger.info("免签支付请求结果:"+result);
			JSONObject data = JSONObject.fromObject(result);
			if(data.get("status").toString().toUpperCase().equals("SUCCESS")){
				//二维码
				String qrcode = JSONObject.fromObject(data.getString("data")).getString("qrcode");
				
				if(StringUtils.isEmpty(mobile)){
					return PayUtil.returnPayJson("success", "2", "支付接口请求成功!", userName, pay_amount, pay_orderid, qrcode);
				}
				return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, pay_amount, pay_orderid, qrcode);
			}
			
//			if(!StringUtils.isEmpty(mobile)) {//手机端支付
//				this.pay_url = this.pay_url+"?format=js";
//			}
//			
//			String formStr = buildForm(params, pay_url);
//			logger.info("支付form表单：" + formStr);
//			return PayUtil.returnPayJson("success", "1", "支付接口请求成功!", userName, pay_amount, pay_orderid, formStr);
			
		} catch (Exception e) {
			logger.info("免签支付异常:"+e.getMessage());
			e.printStackTrace();
		}
		return PayUtil.returnPayJson("error", "2", "支付接口请求失败!", userName, pay_amount, pay_orderid, result);
	}

	private String buildForm(Map<String, String> paramMap, String payUrl) {
		// 待请求参数数组
		String FormString = "<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form  id=\"actform\" name=\"actform\" method=\"post\" action=\""
				+ payUrl + "\">";
		for (String key : paramMap.keySet()) {
			FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" + paramMap.get(key) + "'>\r\n";
		}
		FormString += "</form></body>";

		return FormString;
	}

	private String generateMd5(Map<String, String> params) {

		StringBuilder builder = new StringBuilder();
		for (String key : params.keySet()) {
			String value = params.get(key);
			if (StringUtils.isEmpty(value) || "pay_productname".equalsIgnoreCase(key)
					|| "pay_attach".equalsIgnoreCase(key)) {
				continue;
			}
			builder.append(key + "=" + value + "&");
		}
		builder.append("key=" + this.md5_key);

		String signatureStr = builder.toString();
		logger.info("签名字符串 = " + signatureStr);

		String sign = null;
		try {
			sign = MD5.md5(signatureStr);
			logger.info("签名sign = " + sign);
		} catch (NoSuchAlgorithmException e) {
		}

		return sign;
	}
	
	
	public static void main(String[] args) throws NoSuchAlgorithmException {
		/*
		 * {amount=100.00, 
		 * attach=nothing, 
		 * datetime=20180625231803, 
		 * memberid=10005, 
		 * orderid=MQ180625231054241, 
		 * returncode=00, 
		 * sign=4E425FB43AF8A5DCA88F33221708062C, 
		 * transaction_id=20180625231035984997}
		 */
		MQZFPayServiceImpl service = new MQZFPayServiceImpl();
		service.md5_key="fp9kzfhchbxidy3bxbvptx8ylpd6xiv6";
		
		Map<String, String> params = new TreeMap<>();
		params.put("amount", "100.00");
		params.put("attach", "nothing");
		params.put("datetime", "20180625231803");
		params.put("memberid", "10005");
		params.put("orderid", "MQ180625231054241");
		params.put("returncode", "00");
		params.put("sign", "4E425FB43AF8A5DCA88F33221708062C");
		params.put("transaction_id", "20180625231035984997");
		
		service.callback(params);
		
	}

	@Override
	public String callback(Map<String, String> params){
		try {
		    StringBuilder sb = new StringBuilder();
	        for(String key : params.keySet()) {
	            if("sign".equalsIgnoreCase(key) ||"attach".equalsIgnoreCase(key)) {
	                continue;
	            }
	            String value = params.get(key);
	            sb.append(key+"="+value+"&");
	        }
	        sb.append("key="+this.md5_key);
	        logger.info("签名字符串："+sb.toString());
	        
	        String localSign = MD5.md5(sb.toString());
	        String remoteSign = params.get("sign");
	        if(localSign.equalsIgnoreCase(remoteSign)) {
	            logger.info("验签成功");
	            return "success";
	        }
        } catch (Exception e) {
            e.printStackTrace();
        }
		logger.info("验签失败");
		return "fail";
	}

}
