package com.cn.tianxia.pay.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.qyf.util.ToolKit;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;

import net.sf.json.JSONObject;

public class YDPayServiceImpl implements PayService {
	private final static Logger logger = LoggerFactory.getLogger(YDPayServiceImpl.class); 
	/**支付地址*/
	private String payUrl;
	/**商户编号*/
	private String payMemberid;
	/**密钥*/
	private String md5Key;
	/**异步通知URL*/
	private String payNotifyUrl;
	public YDPayServiceImpl(Map<String,String> data) {
		if(data.containsKey("payUrl")){
			this.payUrl = data.get("payUrl");
		}
		if(data.containsKey("payMemberid")){
			this.payMemberid = data.get("payMemberid");
		}
		if(data.containsKey("md5Key")){
			this.md5Key = data.get("md5Key");
		}
		if(data.containsKey("payNotifyUrl")){
			this.payNotifyUrl = data.get("payNotifyUrl");
		}
	}
	/**
	 * 网银支付
	 */
	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		return null;
	}
    /**
     * 扫码支付
     */
	@Override
	public JSONObject smPay(PayEntity payEntity) {
		logger.info("云端支付网银支付开始======================START==================");
		try {
			//封装请求参数
			Map<String, String> data = sealRequest(payEntity, 1);
			//生成签名串
			String sign = generatorSign(data);
			data.put("pay_md5sign", sign);
			logger.info("云端支付请求参数:"+JSONObject.fromObject(data).toString());
			//生成请求表单
			String resStr = HttpUtils.toPostForm(data, payUrl);
			logger.info("云端支付生成form表单结果:"+resStr);
			if(StringUtils.isBlank(resStr)){
				logger.info("[YD]云端支付扫码支付发起HTTP请求无响应结果");
				return PayResponse.error("[YD]云端支付扫码支付发起HTTP请求无响应结果");
			}
			JSONObject resObj = JSONObject.fromObject(resStr);
			if(resObj.containsKey("pay_url")&&resObj.getString("pay_url")!=""){
				return PayResponse.sm_qrcode(payEntity, resObj.getString("pay_url"), "下单成功");
			}
			return PayResponse.error("下单失败"+resStr);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("云端支付生成异常:"+e.getMessage());
			return PayUtil.returnWYPayJson("error", "form", "", "", "");
		}
	}

	@Override
    public String callback(Map<String,String> data) {
        try {
        	String sourceSign =  data.get("sign");
        	StringBuilder sb = new StringBuilder();
            sb.append("amount="+data.get("amount")).append("&");
            sb.append("datetime="+data.get("datetime")).append("&");
            sb.append("memberid="+data.get("memberid")).append("&");
            sb.append("orderid="+data.get("orderid")).append("&");
            sb.append("returncode="+data.get("returncode")).append("&");
            sb.append("transaction_id="+data.get("transaction_id")).append("&");
            sb.append("key="+md5Key);
            logger.info("[YD]支付回调生成待签名串:"+sb.toString());
            String sign = ToolKit.MD5(sb.toString(), "UTF-8").toUpperCase();
            logger.info("[YD]支付回调生成签名串:"+sign);
            if (sign.equals(sourceSign)) return "success";
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[YD]云端支付回调验签异常:"+e.getMessage());
        }
        return "fail";
    }
	
	 
	/**
     * 
     * @Description 封装支付请求参数
     * @param entity
     * @param type 支付类型  1 网银支付   2 扫码支付
     * @return
     * @throws Exception
     */
	public Map<String, String> sealRequest(PayEntity payEntity,Integer type){
		SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-DD HH:mm:ss");
		String toDate = dateFormat.format(new Date());
		Map<String,String> data = new HashMap<>();
		data.put("pay_memberid", payMemberid);
		data.put("pay_orderid", payEntity.getOrderNo());
		data.put("pay_amount", String.valueOf(payEntity.getAmount()));
		data.put("pay_applydate", toDate);
		data.put("pay_bankcode", payEntity.getPayCode());
		data.put("pay_notifyurl", payNotifyUrl);
		data.put("pay_callbackurl", payEntity.getRefererUrl());
		data.put("pay_attach", "YDPay");
		data.put("pay_productname", "pay");
		return data;
	}

	/**
     * 
     * @Description 生成签名串
     * @param data
     * @return
     * @throws Exception
     */
    private String generatorSign(Map<String,String> data){
    	StringBuilder sb = new StringBuilder();
        sb.append("pay_amount="+data.get("pay_amount")).append("&");
        sb.append("pay_applydate="+data.get("pay_applydate")).append("&");
        sb.append("pay_bankcode="+data.get("pay_bankcode")).append("&");
        sb.append("pay_callbackurl="+data.get("pay_callbackurl")).append("&");
        sb.append("pay_memberid="+data.get("pay_memberid")).append("&");
        sb.append("pay_notifyurl="+data.get("pay_notifyurl")).append("&");
        sb.append("pay_orderid="+data.get("pay_orderid")).append("&");
        sb.append("key="+md5Key);
        String sign = ToolKit.MD5(sb.toString(), "UTF-8").toUpperCase();
    	return sign;
    }
   /* public static void main(String[] args) {
    	Map<String, String> data = new HashMap<>();
		YDPayServiceImpl ydPayServiceImpl = new YDPayServiceImpl(data);
		
		PayEntity payEntity = new PayEntity();
		payEntity.setOrderNo("1234567890");
		payEntity.setAmount(100.88);
		JSONObject smPay = ydPayServiceImpl.smPay(payEntity);
		System.out.println("响应数据："+smPay);
		HashMap<String, String> params = new HashMap<>();
        params.put("amount", "50.00");
		params.put("datetime", "20190103133128");
		params.put("memberid", "10025");
		params.put("orderid", "YDtas201901031331021331025504");
		params.put("returncode", "00");
		params.put("transaction_id", "190103133103555652-=");
		params.put("attach", "YDPay");
		params.put("sign", "BD2F5ADF741E75075DD21B2E5E237B58");
		ydPayServiceImpl.callback(params);
	   System.out.println(params.get("sign")+"===========");
	}*/
    
}
