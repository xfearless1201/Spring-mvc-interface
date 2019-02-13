package com.cn.tianxia.pay.impl;

import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import com.cn.tianxia.dao.v2.XbbzfPaymentDao;
import com.cn.tianxia.jf.util.DESUtil;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.util.SpringContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.entity.v2.XbbzfPaymentEntity;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.tx.util.MD5Utils;
import com.cn.tianxia.pay.utils.RandomUtils;

import net.sf.json.JSONObject;


/**
 * 
 * @ClassName XBBZFPayServiceImpl
 * @Description 新币宝虚拟币 支付
 * @author Hardy
 * @Date 2019年1月15日 下午12:21:32
 * @version 1.0.0
 */
public class XBBZFPayServiceImpl implements PayService{
	private final static Logger logger = LoggerFactory.getLogger(XBBZFPayServiceImpl.class);
	
	/**商家编号**/
	private String MerCode;
	/**回调地址**/
	private String notify_url;
	/**获取地址**/
	private String getAddress;
	/**创建会员**/
	private String addUserUrl;
	/**login**/
	private String login;
	/**DES key**/
	private String DESKey;
	/**keyA**/
	private String keyA;
	/**keyB**/
	private String keyB;
	/**keyC**/
	private String keyC;
	/****/
	private String CoinCode;

	
	public XBBZFPayServiceImpl(Map<String,String> map){
		if(map.containsKey("MerCode")){
			this.MerCode = map.get("MerCode");
		}
		if(map.containsKey("notify_url")){
			this.notify_url = map.get("notify_url");
		}
		if(map.containsKey("addUserUrl")){
			this.addUserUrl = map.get("addUserUrl");
		}
		if(map.containsKey("getAddress")){
			this.getAddress = map.get("getAddress");
		}
		if(map.containsKey("login")){
			this.login = map.get("login");
		}
		if(map.containsKey("DESKey")){
			this.DESKey = map.get("DESKey");
		}
		if(map.containsKey("keyA")){
			this.keyA = map.get("keyA");
		}
		if(map.containsKey("keyB")){
			this.keyB = map.get("keyB");
		}
		if(map.containsKey("keyC")){
			this.keyC = map.get("keyC");
		}
		if(map.containsKey("CoinCode")){
			this.CoinCode = map.get("CoinCode");
		}
	}
	
	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		return null;
	}

	@Override
	public JSONObject smPay(PayEntity payEntity) {
		logger.info("新币宝支付开始扫描支付...............");
		
		String uid = payEntity.getuId();
		
		//查询会员是否存在
		if(checkUser(uid)){
			//创建会员
			JSONObject regist = registUser(payEntity);
			if (null != regist) {
				return regist;
			}
		}

		//开始支付
		Map<String,String> payMap = sealRequest(payEntity, 2);
		String payResponse;
		try {
			payResponse = HttpUtils.get(payMap,login);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("请求新币宝登录支付接口失败:{}",e.getMessage());
			return PayResponse.error("请求新币宝登录支付接口失败:"+e.getMessage());
		}
		logger.info("新币宝支付返回结果:{}", payResponse);
		if(StringUtils.isEmpty(payResponse)){
			return PayResponse.error("新币宝支付 请求登录支付无返回值");
		}
		
		JSONObject pay = JSONObject.fromObject(payResponse);
		if(pay.containsKey("Code") && "1".equals(pay.getString("Code"))){
			JSONObject url = pay.getJSONObject("Data");
			logger.info("新币宝支付 支付地址url= {}",url.getString("Url")+"/"+url.getString("Token"));
			return PayResponse.sm_link(payEntity, url.getString("Url")+"/"+url.getString("Token"), "新币宝支付成功");
		}
		
		return PayResponse.error("新币宝支付下单失败：" + pay.getString("Message"));
	}

	@Override
	public String callback(Map<String, String> data) {
		String sourceSign = data.remove("Sign");
		if (StringUtils.isBlank(sourceSign)) {
			logger.info("[XBBZF]新币宝支付回调验签失败：回调签名为空！");
			return "fail";
		}
		if(verifyCallback(sourceSign,data))
			return "success";
		return "fail";
	}

	private boolean verifyCallback(String sourceSign, Map<String, String> data) {

		StringBuffer sb = new StringBuffer();

		TreeMap<String,String> sortMap = new TreeMap<>(data);
		for (Map.Entry entry:sortMap.entrySet()) {
			if ("FinishTime".equals(entry.getKey()) || "Sign".equals(entry.getKey()))
				continue;
			sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append(keyB);
		String localSign;
		try {
			localSign = MD5Utils.md5(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("[XBBZF]新币宝支付生成支付签名串异常:"+ e.getMessage());
			return false;
		}
		return sourceSign.equalsIgnoreCase(localSign);
	}

	private JSONObject registUser(PayEntity entity) {
		Map<String,String> registUserParam = sealRequest(entity, 0);
		String response;
		try {
			response = HttpUtils.get(registUserParam,addUserUrl);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("请求新币宝注册用户接口失败:{}",e.getMessage());
			return PayResponse.error("请求新币宝注册用户接口失败:"+e.getMessage());
		}
		logger.info("新币宝支付新增会员 请求返回值:");
		if(StringUtils.isEmpty(response)) {
			return PayResponse.error("新币宝支付新增会员 请求无返回值,请联系第三方...");
		}

		JSONObject jsonObject = JSONObject.fromObject(response);
		if(jsonObject.containsKey("Code") && "1".equals(jsonObject.getString("Code"))){
			//获取会员支付地址
			Map<String,String> paramMap = sealRequest(entity, 1);
			String addressResponse;
			try {
				addressResponse = HttpUtils.get(paramMap, getAddress);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("请求新币宝获取用户钱包地址接口失败:{}",e.getMessage());
				return PayResponse.error("请求新币宝获取用户钱包地址接口失败:"+e.getMessage());
			}
			if(StringUtils.isEmpty(addressResponse)){
				return PayResponse.error("新币宝支付获取会员支付地址请求返回为空,请联系第三方");
			}

			JSONObject json = JSONObject.fromObject(addressResponse);

			if(json.containsKey("Code") && "1".equals(json.getString("Code"))){
				//插入到数据库
				JSONObject address = json.getJSONObject("Data");
				int i = insertXbbzfPaymentUser(entity.getuId(),address.getString("Address"));
				if(i > 0){
					logger.info("新币宝支付获取会员支付地址插入数据库成功");
				}else{
					return PayResponse.error("新币宝支付获取会员支付地址 插入数据库出现错误");
				}
			}
		} else {
			logger.error("新币宝支付新增会员失败:{}", jsonObject.getString("Message"));
			return PayResponse.error("新币宝支付新增会员失败:"+ jsonObject.getString("Message"));
		}
		return null;
	}

	/**
	 * 验证码(需全小写)，組成方式如下:Key=A+B+C(验证码組合方式)
	 * @param payEntity
	 * @return
	 */
	private Map<String,String> sealRequest(PayEntity payEntity,int type){
		String amount = new DecimalFormat("0.00").format(payEntity.getAmount());
		Map<String,String> map = new LinkedHashMap<>();
		map.put("MerCode", MerCode);
		map.put("Timestamp", System.currentTimeMillis()+"");
		map.put("UserName", payEntity.getUsername());
		//获取请求地址
		if(type == 1){
			map.put("UserType", "1");
			//币种 DC(钻石币)
			map.put("CoinCode", CoinCode);
		}else if(type == 2){
			map.put("Type", "1");
			map.put("Coin", "DC");
			map.put("Amount", amount);
			map.put("OrderNum", payEntity.getOrderNo());
			map.put("PayMethods", payEntity.getPayCode());
		}

		//将请求参数用des加密放在map中
		StringBuilder desSourceStr = new StringBuilder();
		for (Map.Entry entry:map.entrySet()) {
			desSourceStr.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
		}
		Map<String,String> param = new HashMap<>();
		DESUtil desUtil = new DESUtil(DESKey);
		String desStr = null;
		try {
			desStr = desUtil.encrypt(desSourceStr.deleteCharAt(desSourceStr.length()-1).toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		param.put("param",desStr);

		//key字段签名方法
		StringBuilder key = new StringBuilder(RandomUtils.generateLowerString(Integer.valueOf(keyA)));
		//创建会员
		if(type == 0){
			key.append(generatorSign(map,0));
		}else if(type == 1){
			key.append(generatorSign(map,1));
		}else if(type == 2){
			key.append(generatorSign(map,2));
		}
		key.append(RandomUtils.generateLowerString(Integer.valueOf(keyC)));
		param.put("key", key.toString());

		logger.info("新币宝支付 组装的参数:{}",map);

		return param;
	}
	
	/**
	 * MD5(MerCode + UserName + KeyB + YYYYMMDD)
	 * @param map
	 * @return
	 */
	private String generatorSign(Map<String,String> map,int type){
		logger.info("新币宝支付 开始生成 MD5值");
		StringBuilder sb = new StringBuilder();
		sb.append(map.get("MerCode"));
		logger.info("新币宝支付加密前参数:{}", sb);
		
		//新增会员
		if(type == 0){
			sb.append(map.get("UserName"));
		}else if(type == 1){//获取请求地址
			sb.append(map.get("UserType")).append(map.get("CoinCode"));
		}else if(type == 2){//支付
			sb.append(map.get("UserName")).append(map.get("Type")).append(map.get("OrderNum"));
		}

		sb.append(keyB).append(new SimpleDateFormat("YYYYMMDD").format(new Date()));
		logger.info("新币宝支付 加密前参数:{}", sb);
		String md5Value = MD5Utils.md5(sb.toString());
		logger.info("新币宝支付加密后值:{}",md5Value);
		
		return md5Value;
	}
	/**
	 * 查询会员是否存在
	 * @param payEntity
	 * @return
	 */
	private boolean checkUser(String uid){
		logger.info("查询新币宝会员是否存在");
		XbbzfPaymentDao xbbzfPaymentDao = (XbbzfPaymentDao) SpringContextUtils.getBeanByClass(XbbzfPaymentDao.class);
		XbbzfPaymentEntity entity = xbbzfPaymentDao.selectUserName(Integer.valueOf(uid));
		//查询没有，新增会员
		if(entity == null){
			logger.info("新币宝 会员uid = {} 暂时没有创建会员，需要创建会员，返回 true",uid);
			return true;
		}else{
			logger.info("新币宝 会员uid = {} 暂时已经创建会员，需要创建会员，返回 false",uid);
			return false;
		}
	}
	
	private int insertXbbzfPaymentUser(String uid,String address){
		XbbzfPaymentEntity entity = new XbbzfPaymentEntity();
		entity.setUid(uid);
		entity.setUsername(uid);
		entity.setAddress(address);
		XbbzfPaymentDao xbbzfPaymentDao = (XbbzfPaymentDao) SpringContextUtils.getBeanByClass(XbbzfPaymentDao.class);
		int i = xbbzfPaymentDao.insertXbbzfPaymentEntity(entity);
		if(i > 0){
			logger.info("新币宝 支付插入会员成功.....");
		}
		return i;
	}
}
