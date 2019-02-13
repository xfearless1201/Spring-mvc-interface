package com.cn.tianxia.pay.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.DigestUtils;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;

import net.sf.json.JSONObject;

public class WOWPayServiceImpl implements PayService {
	private final static Logger logger = LoggerFactory.getLogger(WOWPayServiceImpl.class);
	/**支付地址*/
	private String payUrl;
	/**商户编号*/
	private String merchantNo;
	/**商户接收支付成功数据的地址*/
	private String notifyUrl;
	/**商户密钥*/
	private String md5Key;
	
	public WOWPayServiceImpl(Map<String,String> data) {
		if(data!=null){
			if(data.containsKey("payUrl")){
				this.payUrl = data.get("payUrl");
			}
			if(data.containsKey("merchantNo")){
				this.merchantNo = data.get("merchantNo");
			}
			if(data.containsKey("notifyUrl")){
				this.notifyUrl = data.get("notifyUrl");
			}
			if(data.containsKey("md5Key")){
				this.md5Key = data.get("md5Key");
			}
		}
	}
	public JSONObject wyPay(PayEntity payEntity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject smPay(PayEntity payEntity) {
		logger.info("Wow支付扫码支付开始======================START==================");
		try {
			//封装请求参数
			Map<String, String> data = sealRequest(payEntity);
			//生成签名串
			String sign = generatorSign(data);
			data.put("sign", sign);
			logger.info("[WOW]Wow支付请求参数:"+JSONObject.fromObject(data).toString());
			//生成请求表单
			String resStr = HttpUtils.generatorFormGet(data, payUrl);
			logger.info("[WOW]Wow支付响应信息:"+resStr);
			if(StringUtils.isBlank(resStr)){
				logger.info("[WOW]Wow支付扫码支付发起HTTP请求无响应结果");
				return PayResponse.error("[WOW]Wow支付扫码支付发起HTTP请求无响应结果");
			}
			return PayResponse.sm_form(payEntity, resStr, "下单成功");
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("[WOW]Wow支付生成异常:"+e.getMessage());
			return PayUtil.returnWYPayJson("error", "form", "", "", "");
		}
	}

	@Override
	public String callback(Map<String, String> data){
		try {
			String sourceSign = data.remove("sign");
			String sign = generatorSign(data);
			logger.info("[WOW]Wow支付回调生成签名串:"+sign);
			if (sign.equals(sourceSign)) return "success";
		} catch (Exception e) {
			 e.printStackTrace();
	         logger.error("[WOW]Wow支付支付回调验签异常:"+e.getMessage());
		}
		return "fail";
	}
	/**
     * 
     * @Description 封装支付请求参数
     * @param entity
     * @param 
     * @return
     * @throws Exception
     */
	public Map<String, String> sealRequest(PayEntity payEntity){
		SimpleDateFormat dateFormat = new SimpleDateFormat("MMddHHmmssSSS");
		String toDate = dateFormat.format(new Date());
		Map<String,String> data = new HashMap<>();
		data.put("merchant", merchantNo);
		data.put("merchant_order_id", payEntity.getOrderNo());
		data.put("amount", String.valueOf(payEntity.getAmount()));
		data.put("notify_url", notifyUrl);
		data.put("nonce", payEntity.getOrderNo());
		data.put("timestamp", toDate);
		return data;
	}
	/**
     * 
     * @Description 生成签名串
     * @param data
     * @return
	 * @throws Exception
     */
    public String generatorSign(Map<String,String> data) throws Exception{
    	List<String> keys = new ArrayList<>(data.keySet());
        Collections.sort(keys);
        String sign = "";
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = null;
            try {
                if (StringUtils.isNotBlank(data.get(key))) {
                    value = URLEncoder.encode(data.get(key), "UTF-8");
                }
                key = URLEncoder.encode(key, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            sign = sign + key + "=" + value + "&";
        }
        sign += "secret_key=" + md5Key;
        sign = DigestUtils.md5DigestAsHex(sign.getBytes()).toUpperCase();
        return sign;
    }
	/*public static void main(String[] args) throws UnsupportedEncodingException {
		String str = "http://txw.tx8899.com/JJF/Notify/WOWNotify.do";
		String encode = URLEncoder.encode(str,"UTF-8");
		Map<String, String> map = new HashMap<>();
		map.put("a", "1");
		map.put("b", "2");
		System.out.println(map.keySet());
		String b = map.remove("b");
		System.out.println(map+":"+b);
	}*/
}
