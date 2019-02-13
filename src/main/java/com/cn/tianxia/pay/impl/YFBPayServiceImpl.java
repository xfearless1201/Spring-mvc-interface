package com.cn.tianxia.pay.impl;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.MD5Util;
import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MapUtils;

import net.sf.json.JSONObject;

/**
 * 云付宝支付
 * @author TX
 */
public class YFBPayServiceImpl implements PayService{
	private final static Logger logger = LoggerFactory.getLogger(YFBPayServiceImpl.class);
	private String accountId;//商家id
	private String notifyUrl;//回调路径
	private String secret;//key
	private String payUrl;//支付路径
	private String interfaceStr;//接口类型
	
	public YFBPayServiceImpl(Map<String,String> map){
		if(map.containsKey("accountId")){
			this.accountId = map.get("accountId");
		}
		if(map.containsKey("notifyUrl")){
			this.notifyUrl = map.get("notifyUrl");
		}
		if(map.containsKey("secret")){
			this.secret = map.get("secret");
		}
		if(map.containsKey("payUrl")){
			this.payUrl = map.get("payUrl");
		}
		if(map.containsKey("interfaceStr")){
			this.interfaceStr = map.get("interfaceStr");
		}
	}
	
	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		return null;
	}

	@Override
	public JSONObject smPay(PayEntity payEntity) {
		try {
			JSONObject json  = sealRequest(payEntity);//组装请求参数
			String sign = generatorSign(json);//签名
			json.put("sign", sign);
			
			logger.info("[云付宝支付]请求路径:{}",payUrl);
			String str = HttpUtils.toPostForm(json, payUrl);
			logger.info("[云付宝支付]请求返回的结果:{}", str);
			
			if(StringUtils.isEmpty(str)){
				PayResponse.error("[云付宝支付]请求无响应");
			}
			JSONObject result = JSONObject.fromObject(str);
			// status = 1 请求成功
			if(result.containsKey("status") && "1".equals(result.getString("status"))){
				JSONObject data = (JSONObject) result.get("data");
				logger.info("[云付宝支付]解析data 数据:{}",data);
				if(data.containsKey("code") && "10000".equals(data.getString("code"))){
					return PayResponse.sm_qrcode(payEntity, data.getString("qr_code"), result.toString());
				}
			}
			
			return PayResponse.error("[云付宝支付]请求返回失败,"+result);
		} catch (Exception e) {
			e.printStackTrace();
			
			return PayResponse.error("[云付宝支付]请求报错,"+e.getMessage());
		}
	}

	/**
	 *  云付宝回调函数
	 */
	@Override
	public String callback(Map<String, String> data) {
		
		try{
			logger.info("进入回调方法");
			String sourceSign = data.get("sign");
			logger.info("[YFB]云付宝支付验签原签名串:{}", sourceSign);
			JSONObject json = JSONObject.fromObject(data);
			String sign = generatorSign(json);
			logger.info("[YFB]云付宝支付验签生成签名串:{}", sign);
	        if(sourceSign.equalsIgnoreCase(sign)) return "success";
	        
		}catch(Exception e){
			e.printStackTrace();
			logger.info("云付宝回调函数出现错误:{}",e.getMessage());
		}
		
		return "fail";
	} 

	private JSONObject sealRequest(PayEntity payEntity){
		logger.info("[云付宝支付]开始组装参数......");
		JSONObject json = new JSONObject();
		json.put("accountId", accountId);
		json.put("payType", "1");//支付宝
		json.put("orderId", payEntity.getOrderNo());//订单编号
		json.put("commodity", "TOP-UP");
		json.put("interface", interfaceStr);//接口类型 支付宝:alipay_yard
		json.put("amount", String.valueOf(payEntity.getAmount()));
		json.put("notifyUrl", notifyUrl);
		logger.info("[云付宝支付] 组装请求参数值:{}",json);
		return json;
	}
	
	private String generatorSign(JSONObject json) throws Exception{
		logger.info("[云付宝支付] ");
		try {
			Map<String,Object> sortmap = MapUtils.sortMapByKeys(json);
			StringBuffer sb = new StringBuffer();
			
			for(Entry<String,Object> entry: sortmap.entrySet()){
				
				if(entry.getValue() == null || entry.getKey().equalsIgnoreCase("sign")){
					continue;
				}
				
				sb.append(entry.getKey()).append("=")
				.append(entry.getValue()).append("&");
				
			}
			sb.append("key=").append(secret);
			logger.info("[云付宝支付]签名之前参数:{}", sb);
			String md5Value = MD5Util.encodeToUpperCase(sb.toString());
			logger.info("[云付宝支付]加密之后:{}", md5Value);
			return md5Value;
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("[XYZ]信誉支付 生产签名串异常:{}",e.getCause());
			throw new Exception("[XYZ]信誉支付 生产签名串异常!");
		}
		
	}
}
