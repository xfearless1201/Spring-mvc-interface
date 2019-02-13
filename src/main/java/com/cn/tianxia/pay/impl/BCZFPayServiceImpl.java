/**  
 * 
 * @Title:  BCPayServiceImpl.java   
 * @Package com.cn.tianxia.pay.impl   
 * @Description:    TODO(用一句话描述该文件做什么)   
 * @author: seven
 * @date:   2018年12月4日 下午5:11:33   
 * @version V1.0 
 * 
 */
package com.cn.tianxia.pay.impl;

import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.tx.util.DateUtil;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;

import net.sf.json.JSONObject;

/**   
 * @ClassName:  BCZFPayServiceImpl   
 * @Description:TODO(保诚支付)   
 * @author: seven
 * @date:   2018年12月4日 下午5:11:33      
 */
public class BCZFPayServiceImpl implements PayService{
	/**
	 *商户id
	 */
	private String pay_memberid;
	/**
	 * 商户密钥
	 */
	private String key; 
	/**
	 * 回调函数
	 */
	private String pay_notifyurl;
	/**
	 * 支付地址
	 */
	private String pay_url;
	
	/**
	 * 手机是否打开 https 请求
	 */
	private String isOpen;
	
	private static final Logger logger = LoggerFactory.getLogger(BCZFPayServiceImpl.class);	

	public BCZFPayServiceImpl(Map<String, String> data){
		
		if(data.containsKey("pay_memberid")){
			this.pay_memberid = data.get("pay_memberid");
		}
		if(data.containsKey("pay_url")){
			this.pay_url = data.get("pay_url");
		}
		if(data.containsKey("pay_notifyurl")){
			this.pay_notifyurl = data.get("pay_notifyurl");
		}
		if(data.containsKey("key")){
			this.key = data.get("key");
		}
		if(data.containsKey("isOpen")){
			this.isOpen = data.get("isOpen");
		}
	}
	
	/**
	 * 网银支付
	 * 
	 * @param payEntity
	 * @return
	 */
	public JSONObject wyPay(PayEntity payEntity){
		logger.info("[BCZF]保诚支付网银支付======================START=======================");
		Map<String, String> url;
		try {
			url = getUrl(payEntity);
			
			String payUrl = "";
			if("1".equals(isOpen) && !StringUtils.isEmpty(payEntity.getMobile())){
				payUrl = pay_url.replace("http", "https");
			}else{
				payUrl = pay_url;
			}
			   
			logger.info("{}保诚支付请求路径:{}", payEntity.getOrderNo(), payUrl);
			String form = HttpUtils.generatorForm(url, payUrl);
			logger.info("[BCZF]保诚支付网银支付生成表单:{}",form);
			return PayUtil.returnWYPayJson("success", "form", form, payUrl, "");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return PayResponse.error("保诚支付错误");
	}

	/**
	 * 扫码支付
	 * 
	 * @param payEntity
	 * @return
	 */
	public JSONObject smPay(PayEntity payEntity){
		logger.info("[BCZF]保诚支付扫码支付======================START=======================");
		try {
			
			Map<String, String> url = getUrl(payEntity);
			logger.info("订单号:{},保诚支付请求参数:{}",payEntity.getOrderNo(),url);
			String payUrl = "";
			if("1".equals(isOpen) && !StringUtils.isEmpty(payEntity.getMobile())){
				payUrl = pay_url.replace("http", "https");
			}else{
				payUrl = pay_url;
			}
			
			logger.info("保诚支付订单号:{},请求路径:{}",payEntity.getOrderNo(),payUrl);
			String form = HttpUtils.generatorForm(url, payUrl);
			logger.info("[BCZF]保诚支付扫码支付生成表单:{}",form);
			return PayResponse.sm_form(payEntity, form, "下单成功!");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return PayResponse.error("保诚支付出现错误!");
	}
	
	/**
	 * 
	 * @Description 验签
	 * @param data
	 * @return
	 */
	public String callback(Map<String,String> data){
		logger.info("[BCZF]保诚支付回调参数:{}",data);
		String pay_md5sign ="";
		String sgin ="";
		try{
			 sgin =data.get("sign");
			 logger.info("保诚支付获取的sign:{}",sgin);
			 SortedMap<String,String> dataMapIn=new TreeMap<String, String>();
			 dataMapIn.put("amount", data.get("amount"));
			 dataMapIn.put("datetime", data.get("datetime"));
			 dataMapIn.put("memberid", data.get("memberid"));
			 dataMapIn.put("orderid", data.get("orderid"));
			 dataMapIn.put("returncode", data.get("returncode"));
			 dataMapIn.put("transaction_id", data.get("transaction_id"));
			 String localMd5=generatorSign(dataMapIn);
		    if(localMd5.equalsIgnoreCase(sgin)) {
		    	return "success";
		    }
	   } catch (Exception e) {
         e.printStackTrace();
         logger.error("[BCZF]宝城支付回调验签异常:"+e.getMessage());
      }
		 logger.error("[BCZF]宝城支付回调验签异常:sgin:"+sgin+",pay_md5sign="+pay_md5sign);
      return "faild";
	}
	/**
	 * @throws NoSuchAlgorithmException 
	 * @Title: getUrl   
	 * @Description: TODO(获取请求参数串)   
	 * @param: @param payEntity
	 * @param: @return      
	 * @return: String      
	 * @throws
	 */
    private Map<String, String> getUrl(PayEntity payEntity) throws NoSuchAlgorithmException{
    	logger.info("[BCZF]保诚支付组装参数开始...........");
    	SortedMap<String,String> dataMapIn=new TreeMap<String, String>();
    	dataMapIn.put("pay_amount", payEntity.getAmount()+"");
    	dataMapIn.put("pay_applydate", DateUtil.convertDateToString(new Date(), "yyyy-MM-dd HH:mm:ss"));
    	dataMapIn.put("pay_bankcode", payEntity.getPayCode());
    	dataMapIn.put("pay_clientip", payEntity.getIp());
    	dataMapIn.put("pay_memberid", pay_memberid);
    	dataMapIn.put("pay_notifyurl", pay_notifyurl);
    	dataMapIn.put("pay_orderid", payEntity.getOrderNo());
    	String localMd5=generatorSign(dataMapIn);
    	logger.info("[BCZF]保诚支付生成签名结果:"+localMd5);
    	dataMapIn.put("pay_md5sign", localMd5);
    	logger.info("[BCZF]保诚支付参数:{}",dataMapIn);
    	return dataMapIn;
    }
	
    
	/**
	 * @throws NoSuchAlgorithmException 
	 * 
	 * @Title: mapToString   
	 * @Description: TODO(map转换链接)   
	 * @param: @param params
	 * @param: @return      
	 * @return: String      
	 * @throws
	 */
	private  String generatorSign(Map<String, String> params) throws NoSuchAlgorithmException{
		logger.info("");
		StringBuffer sb =new StringBuffer();

		if (params == null || params.size() <= 0) {
			return "";
		}
		for (String key : params.keySet()) {
			String value = params.get(key);
			if (StringUtils.isEmpty(value) || "sign".equals(key)) {
				continue;
			}
			sb.append(key).append("=").append(value).append("&");
		}

		sb.append("key=").append(key);
		
		logger.info("保诚支付 加密前参数:{}", sb);
		String md5 = MD5Utils.md5toUpCase_32Bit(sb.toString());
		logger.info("保诚支付加密后:{}",md5);
		return md5;
	}
    
}
