
package com.cn.tianxia.pay.impl;

import java.util.*;

import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.utils.MD5Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.tx.util.DateUtil;
import com.cn.tianxia.util.DESEncrypt;
import com.cn.tianxia.util.v2.HttpUtils;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName WSPayServiceImpl 
 * @Description TODO(WS支付) 
 * @Author seven
 * @Date 2018年12月5日 上午11:09:39 
 * @Version 1.0.0 
 *
 */
public class WSZFPayServiceImpl  implements PayService{

	private static final Logger logger = LoggerFactory.getLogger(WSZFPayServiceImpl.class);
	/**
	 * 扫码支付请求地址
	 */
	private  String scanPayUrl;
	/**
	 * h5支付请求地址
	 */
	private  String h5PayUrl;
	/**
	 * 商户号
	 */
	private String pay_memberid;
	/**
	 * 回调地址
	 */
	private String pay_notifyurl;
	/**
	 * 签名密钥
	 */
	private String key;
	
	public WSZFPayServiceImpl(Map<String, String> data){
		if(!CollectionUtils.isEmpty(data)){
			scanPayUrl = StringUtils.isEmpty(data.get("scanPayUrl"))?"":data.get("scanPayUrl");
			h5PayUrl = StringUtils.isEmpty(data.get("h5PayUrl"))?"":data.get("h5PayUrl");
			pay_memberid = StringUtils.isEmpty(data.get("pay_memberid"))?"":data.get("pay_memberid");
			pay_notifyurl = StringUtils.isEmpty(data.get("pay_notifyurl"))?"":data.get("pay_notifyurl");
			key = StringUtils.isEmpty(data.get("key"))?"":data.get("key");
		}
	}
	/**
	 * 网银支付
	 * 
	 * @param payEntity
	 * @return
	 */
	public JSONObject wyPay(PayEntity payEntity){
		return null;
	}
	

	/**
	 * 扫码支付
	 * 
	 * @param payEntity
	 * @return
	 */
	public JSONObject smPay(PayEntity payEntity){
		System.out.println("[WS]支付WX支付开始===================START=================");
		String res ="";
		try {
			SortedMap<String,String> dataMapIn=new TreeMap<String, String>();
			dataMapIn.put("pay_memberid", pay_memberid);//间连号
			dataMapIn.put("pay_orderid", payEntity.getOrderNo());
			dataMapIn.put("pay_amount", payEntity.getAmount()+"");
			dataMapIn.put("pay_applydate", DateUtil.convertDateToString(new Date(), "yyyyMMddhhmmss"));
			dataMapIn.put("pay_bankcode", payEntity.getPayCode());
			dataMapIn.put("pay_notifyurl", pay_notifyurl);
			dataMapIn.put("pay_callbackurl", payEntity.getRefererUrl());
			String localMd5=mapToString(dataMapIn)+"&key="+key;
			String sign = MD5Utils.md5toUpCase_32Bit(localMd5);
			dataMapIn.put("pay_md5sign", sign);
			String sendMsg= mapToString(dataMapIn).replace(">", "");

			logger.info("sengMsg:"+sendMsg);
			if (StringUtils.isEmpty(payEntity.getMobile())) {
				logger.info("[WS]支付WX支付请求链接:=========="+scanPayUrl);
				res = HttpUtils.doPost(scanPayUrl,sendMsg);
			} else {
				logger.info("[WS]支付WX支付请求链接:=========="+h5PayUrl);
				res = HttpUtils.doPost(h5PayUrl,sendMsg);
			}
			logger.info("[WS]支付WX支付请求响应结果:=========="+res);

			//解析响应结果
            JSONObject jsonObject = JSONObject.fromObject(res);
            if(jsonObject.containsKey("retCode") && jsonObject.getString("retCode").equals("10000")){
                 if(StringUtils.isEmpty(payEntity.getMobile())){
                     //PC端
					 return PayResponse.sm_qrcode(payEntity,jsonObject.getString("payurl"),"下单成功!");
                 }
                 return PayResponse.sm_link(payEntity,jsonObject.getString("payurl"),"下单成功!");
            }
            //下单失败
            String respMsg = jsonObject.getString("retMsg");
            return PayResponse.error("下单失败:"+respMsg);
		} catch (Exception e) {
			 e.printStackTrace();
	         logger.error("[WS]支付WX支付异常:"+e.getMessage());
	         return PayUtil.returnPayJson("error", "2", "下单失败:"+e.getMessage(), payEntity.getUsername(), payEntity.getAmount(), payEntity.getOrderNo(),res);
		}
	}
	
	/**
	 * 
	 * @Description 验签
	 * @param data
	 * @return
	 */
	public String callback(Map<String,String> data){
		logger.info("[WS]支付回调验签开始======================START=======================");
        try {
            //获取回调签名原串
            String signature = data.get("sign");
            SortedMap<String,String> dataMapIn=new TreeMap<>();
            dataMapIn.putAll(data);
        	String param=mapToString(dataMapIn)+"&key="+key;
            //生成回调签名
            String sign = DESEncrypt.getMd5(param);
            if(signature.equalsIgnoreCase(sign)) return "success";
            
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[WS]支付回调验签异常:"+e.getMessage());
        }
        return "faild";
	}
	
	/**
	 * 
	 * @Title: mapToString   
	 * @Description: TODO(map转换链接)   
	 * @param: @param params
	 * @param: @return      
	 * @return: String      
	 * @throws
	 */
	private  String mapToString (Map<String, String> params){

		StringBuffer sb =new StringBuffer();
		String result ="";

		if (params == null || params.size() <= 0) {
			return "";
		}
		for (String key : params.keySet()) {
			String value = params.get(key);
			if (value == null || value.equals("")) {
				continue;
			}
			sb.append(key+"=>"+value+"&");
		}

		result=sb.toString().substring(0,sb.length()-1);

		return result;
	}
	
	public static void main(String[] args) {
		Map<String,String> data = new HashMap<>();
		data.put("scanPayUrl","http://119.29.229.230:1043/Pay/WxSm/Pay");
		data.put("h5PayUrl","http://119.29.229.230:1043/Pay/Wxzf/Pay");
		data.put("pay_memberid","10154");
		data.put("notifyUrl","http://txw.tx8899.com/KFY/Notify/SWZFNotify.do");
		data.put("key","dnPleWnrQY7vmEc7VD1onyAMPwwCOU");
		WSZFPayServiceImpl  impl = new WSZFPayServiceImpl(data);
		PayEntity payEntity = new PayEntity();
		payEntity.setOrderNo("1234567652436");
		payEntity.setAmount(0.01);
		payEntity.setPayCode("WXPAY");
		System.out.println(impl.smPay(payEntity));
	}
}
