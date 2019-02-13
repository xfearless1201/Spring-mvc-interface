package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.entity.QueryOrderVO;
import com.cn.tianxia.pay.po.OrderResponse;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.tx.util.MD5Utils;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MapUtils;

import net.sf.json.JSONObject;

/**
 * 百姓支付
 * @author TX
 */
public class BXPayServiceImpl implements PayService{
	private final static Logger logger = LoggerFactory.getLogger(BXPayServiceImpl.class);
	private String payUrl;//请求支付路径
	private String secret;//密钥
	private String amchid;//商户号
	private String notify_url;
	
	public BXPayServiceImpl(Map<String,String> map){
		if(map.containsKey("payUrl")){
			this.payUrl = map.get("payUrl");
		}
		if(map.containsKey("amchid")){
			this.amchid = map.get("amchid");
		}
		if(map.containsKey("secret")){
			this.secret = map.get("secret");
		}
		if(map.containsKey("notify_url")){
			this.notify_url = map.get("notify_url");
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
		logger.info("[百姓支付] 扫码支付开始....");
	
		//组装请求参数
		try {
			Map<String,String> map = parameter(payEntity);
			String sign = generatorSign(map,1);
			logger.info("[百姓支付] 扫码支付添加 sign...");
			map.put("sign", sign);
		
			Map<String,String> sortMap = MapUtils.sortByKeys(map);
			logger.info("[百姓支付] 扫码支付请求参数:{}",sortMap);
			String result = HttpUtils.post(sortMap, payUrl);
			logger.info("[百姓支付] 扫码支付返回请求的结果:{}",result);
			
			if(StringUtils.isBlank(result)){
				return PayResponse.error("[百姓支付] 请求无返回值,请联系第三方支付");
			}
			JSONObject jsonObject = JSONObject.fromObject(result);
			if(jsonObject.containsKey("state") && "1".equals(jsonObject.getString("state"))){
				String data = jsonObject.getString("data");
				logger.info("[百姓支付]扫码支付 返回支付路径:{}",data);
				return PayResponse.sm_link(payEntity, data, "百姓支付成功");
			}
			
			return PayResponse.error("[百姓支付]扫码支付返回结果:" + result);
		} catch (Exception e) {
			e.printStackTrace();
			return PayResponse.error("[百姓支付]扫码支付出现错误");
		}
	}

	/**
	 * 回调函数
	 */
	@Override
	public String callback(Map<String, String> data) {
		try{
			String sourceSign = data.get("sign");
			logger.info("[百姓支付] ");
			String sign = generatorSign(data,0);
			
			if(sign.equals(sourceSign)){
				return "ok";
			}
		}catch(Exception e){
			e.printStackTrace();
			logger.info("[百姓支付] ");
		}
		return "fail";
	}
	
	public JSONObject query(QueryOrderVO vo){
		logger.info("[百姓支付] 查询订单状态方法......");
		try{
			Map<String,String> map = new HashMap<String,String>();
			map.put("amchid", amchid);
			map.put("border", vo.getOrderNo());
			
			String sign = generatorSign(map,1);
			map.put("sign", sign);//签名
			logger.info("[百姓支付] 查询订单状态参数:{}",map);
			String result = HttpUtils.post(map, payUrl);
			
			if(StringUtils.isBlank(result)){
				logger.info("[百姓支付] 查询订单状态 返回值为空，请联系第三方支付");
				return OrderResponse.error("[百姓支付]查询订单状态 返回值为空!");
			}
			JSONObject object = JSONObject.fromObject(result);
			if(object.containsKey("state") && "2".equals(object.getString("state"))){
				logger.info("[百姓支付]查询订单状态返回状态成功");
				return OrderResponse.success("支付订单成功", result);
			}
			
			return OrderResponse.error("[百姓支付]查询订单 支付失败，订单号为:"+vo.getOrderNo());
		}catch(Exception e){
			e.printStackTrace();
			logger.info("[百姓支付] 查询订单状态发生错误......");
			return OrderResponse.error("[百姓支付] 查询订单出现错误!");
		}
	}
	
	/**
	 * 组装请求参数
	 * @param payEntity
	 * @return
	 */
	private Map<String,String> parameter(PayEntity payEntity){
		logger.info("[百姓支付] 扫码支付开始组装请求参数......");
		String amount = new DecimalFormat("#.##").format(payEntity.getAmount());
		Map<String,String> map = new HashMap<String,String>();
		map.put("amchid", amchid);
		map.put("border", payEntity.getOrderNo());
		map.put("cpacc", "TOP-UP");
		map.put("dmoney", amount);
		map.put("enotifyurl", notify_url);
		map.put("freturl", "DOWN");
		map.put("gpaytype", payEntity.getPayCode());
		map.put("hbcode", "");
		map.put("iclientip", payEntity.getIp()); //58.64.40.26
		logger.info("[百姓支付] 扫码支付请求参数:{}",map);
		return map;
	}
	
	/**
	 * 百姓支付签名
	 * @param map
	 * @return
	 * @throws Exception 
	 */
	private String generatorSign(Map<String,String> map,int type) throws Exception{
		logger.info("[百姓支付] 扫码支付签名开始....参数值:{},type={}",map,type);
		StringBuilder sb = new StringBuilder();
		Map<String,String> sortMap = MapUtils.sortByKeys(map);
		String flag = "";
		if(type == 1){
			////支付签名是 key=value
			for(Entry<String,String> entry:sortMap.entrySet()){
				if("sign".equals(entry.getKey())){
					continue;
				}
				
				sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
			}
			flag = sb.substring(0,sb.length()-1);
		}else{
			//回调 签名 value+value
			for(Entry<String,String> entry:sortMap.entrySet()){
				if("sign".equals(entry.getKey())){
					continue;
				}
				sb.append(entry.getValue());
			}
			flag = sb.toString();
		}
		
		flag = flag + secret;
		logger.info("[百姓支付] 扫码支付签名前参数:{}", flag);
		String md5Result = MD5Utils.md5(flag);
		logger.info("[百姓支付] 扫码支付签名 sign:{}", md5Result);
		
		return md5Result;
	}
}
