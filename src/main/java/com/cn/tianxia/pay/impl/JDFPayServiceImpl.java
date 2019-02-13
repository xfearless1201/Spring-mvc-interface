package com.cn.tianxia.pay.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import com.cn.tianxia.pay.utils.MapUtils;

import net.sf.json.JSONObject;

public class JDFPayServiceImpl implements PayService{
	private final static Logger logger = LoggerFactory.getLogger(JDFPayServiceImpl.class);
	/**支付地址*/
	private String payUrl;
	/**商户编号*/
	private String merchantCode;
	/**加密类型*/
	private String signType;
	/**商户请求参数的签名串*/
	private String sign;
	/**商户名称*/
	private String productName;
	/**异步通知地址*/
	private String notifyUrl;
	
	
	public JDFPayServiceImpl(Map<String,String> data) {
		if(data.containsKey("payUrl")){
			this.payUrl = data.get("payUrl");
		}
		if(data.containsKey("merchantCode")){
			this.merchantCode = data.get("merchantCode");
		}
		if(data.containsKey("signType")){
			this.signType = data.get("signType");
		}
		if(data.containsKey("sign")){
			this.sign = data.get("sign");
		}
		if(data.containsKey("productName")){
			this.productName = data.get("productName");
		}
		if(data.containsKey("notifyUrl")){
			this.notifyUrl = data.get("notifyUrl");
		}
	}
	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject smPay(PayEntity payEntity) {
		logger.info("简单支付扫码支付开始======================START==================");
		try {
			//封装请求参数
			Map<String, String> data = sealRequest(payEntity);
			//生成签名串
			String sign = generatorSign(data);
			data.put("sign", sign);
			logger.info("简单支付请求参数:"+JSONObject.fromObject(data).toString());
			//生成请求表单
			String resStr = HttpUtils.toPostForm(data, payUrl);
			logger.info("简单支付响应信息:"+resStr);
			JSONObject resObj = JSONObject.fromObject(resStr);
			return PayResponse.sm_form(payEntity, resStr, resObj.getString("platRespMessage"));
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("简单支付生成异常:"+e.getMessage());
			return PayUtil.returnWYPayJson("error", "form", "", "", "");
		}
	}

	@Override
	public String callback(Map<String, String> data) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
     * 
     * @Description 封装支付请求参数
     * @param entity
     * @param type 支付类型  1 网银支付   2 扫码支付
     * @return
     * @throws Exception
     */
	public Map<String, String> sealRequest(PayEntity payEntity){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		String toDate = dateFormat.format(new Date());
		Map<String,String> data = new HashMap<>();
		data.put("merchantCode", merchantCode);
		data.put("signType", signType);
		data.put("productName", productName);
		data.put("orderNum", payEntity.getOrderNo());
		data.put("payMoney", String.valueOf(payEntity.getAmount()));
		data.put("dateTime", toDate);
		data.put("method", payEntity.getPayCode());
		data.put("notifyUrl", notifyUrl);
		data.put("spbillCreateIp", payEntity.getIp());
		return data;
	}
	/**
     * 
     * @Description 生成签名串
     * @param data
     * @return
	 * @throws Exception
     */
    private String generatorSign(Map<String,String> data) throws Exception{
    	StringBuffer sb = new StringBuffer();
    	Map<String,String> sortmap = MapUtils.sortByKeys(data);
        Iterator<String> iterator = sortmap.keySet().iterator();
        while(iterator.hasNext()){
            String key = iterator.next();
            String val = sortmap.get(key);
            if (StringUtils.isBlank(val) || "sign".equalsIgnoreCase(key)) continue;
            sb.append(val);
        }
        sb.append(sign);
        String signStr = sb.toString();
        logger.info("[JDZF]简单支付生成待签名串:{}",signStr);
        String sign = MD5Utils.md5toUpCase_32Bit(signStr).toLowerCase();
        logger.info("[JDZF]简单支付生成加密签名串:{}",sign);
        return sign;
    }

}
