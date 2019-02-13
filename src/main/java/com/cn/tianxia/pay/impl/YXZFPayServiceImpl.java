package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.enums.PayTypeEnum;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import com.cn.tianxia.pay.utils.MapUtils;
import com.google.gson.Gson;

import net.sf.json.JSONObject;

public class YXZFPayServiceImpl implements PayService {
	private final static Logger logger = LoggerFactory.getLogger(YXZFPayServiceImpl.class);
	/**支付地址*/
	private String payUrl;
	/**商户编号*/
	private String payMemberid;
	/**商户接收支付成功数据的地址*/
	private String payNotifyUrl;
	/**商户密钥*/
	private String md5Key;
	
	public YXZFPayServiceImpl(Map<String,String> data) {
		if(data!=null){
			if(data.containsKey("payUrl")){
				this.payUrl = data.get("payUrl");
			}
			if(data.containsKey("payMemberid")){
				this.payMemberid = data.get("payMemberid");
			}
			if(data.containsKey("payNotifyUrl")){
				this.payNotifyUrl = data.get("payNotifyUrl");
			}
			if(data.containsKey("md5Key")){
				this.md5Key = data.get("md5Key");
			}
		}
	}
	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject smPay(PayEntity payEntity) {
		logger.info("[YXZF]云悉支付扫码支付开始======================START==================");
		try {
			//封装请求参数
			JSONObject data = sealRequest(payEntity);
			//生成签名串
			String sign = generatorSign(data);
			data.put("sign", sign);
			logger.info("[YXZF]云悉支付请求参数:"+data.toString());
			//生成请求表单
			String resStr = HttpUtils.toPostJsonStr(data, payUrl);
			logger.info("[YXZF]云悉支付响应信息:"+resStr);
			if(StringUtils.isBlank(resStr)){
				logger.info("[YXZF]云悉扫码支付发起HTTP请求无响应结果");
				return PayResponse.error("[YXZF]云悉扫码支付发起HTTP请求无响应结果");
			}
		    return PayResponse.sm_form(payEntity, resStr, "下单成功");
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("[YXZF]云悉支付生成异常:"+e.getMessage());
			return PayResponse.error("[YXZF]云悉支付下单失败");
		}
	}

	@Override
	public String callback(Map<String, String> data) {
		try {
			String sourceSign = data.remove("sign");
			JSONObject callbackJson = JSONObject.fromObject(data);
			data.remove("attach");
			String sign = generatorSign(callbackJson);
			logger.info("[YXZF]云悉支付回调生成签名串"+sign);
			if(sign.equals(sourceSign)) return "success";
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("[YXZF]云悉支付回调生成签名串异常"+e.getMessage());
		}
		return null;
	}
	/**
     * 
     * @Description 封装支付请求参数
     * @param entity
     * @param 
     * @return
     * @throws Exception
     */
	public JSONObject sealRequest(PayEntity payEntity){
		String payCode = PayTypeEnum.getPayCode(payEntity.getPayType());
		JSONObject outJsonObj = new JSONObject();
		JSONObject inJsonObj = new JSONObject();
		outJsonObj.put("appid", payMemberid);//商户号
		if("wx".equals(payCode)){
			if(StringUtils.isBlank(payEntity.getMobile())){
				outJsonObj.put("method", "wx_native");
			}
			outJsonObj.put("method", "wx_jsapi");
		}
		if("ali".equals(payCode)){
			if(StringUtils.isBlank(payEntity.getMobile())){
				outJsonObj.put("method", "ali_native");
			}
			outJsonObj.put("method", "ali_jsapi");
		}
		String uid = UUID.randomUUID().toString();
		DecimalFormat df = new DecimalFormat("0");
		inJsonObj.put("store_id", "");//商户号
		inJsonObj.put("total", df.format(payEntity.getAmount()*100));//金额 单位：分
		inJsonObj.put("nonce_str", uid.replace("-", ""));
		inJsonObj.put("out_trade_no", payEntity.getOrderNo());//订单号
		inJsonObj.put("body", "");//商品名称
		outJsonObj.put("data", inJsonObj);
		return outJsonObj;
	}
	/**
     * 
     * @Description 生成签名串
     * @param data
     * @return
	 * @throws Exception
     */
    public String generatorSign(JSONObject data) throws Exception{
    	String dataStr = data.getString("data");
    	Gson gson = new Gson();
    	Map<String, String> value = gson.fromJson(dataStr, Map.class);
    	Map<String,String> sortmap = MapUtils.sortByKeys(value);
        StringBuffer sb = new StringBuffer();
        Iterator<String> iterator = sortmap.keySet().iterator();
        while(iterator.hasNext()){
            String key = iterator.next();
            String val = sortmap.get(key);
            if(StringUtils.isBlank(val) || key.equalsIgnoreCase("sign")) continue;
            sb.append(key).append("=").append(val).append("&");
        }
        sb.append("key=").append(md5Key);
    	//生成待签名串
    	String signStr = sb.toString();
    	logger.info("[YXZF]云悉支付生成待签名串:{}",signStr);
    	String sign = MD5Utils.md5toUpCase_32Bit(signStr);
    	logger.info("[YXZF]云悉支付生成加密签名串:{}",sign);
    	return sign;
    }
}
