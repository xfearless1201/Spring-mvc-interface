package com.cn.tianxia.pay.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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

public class XHFPayServiceImpl implements PayService{
	private final static Logger logger = LoggerFactory.getLogger(XHFPayServiceImpl.class);
	/**商户号*/
	private String payMemberid;
	/**支付地址*/
	private String payUrl;
	/**密钥*/
	private String md5Key;
	/**回调地址*/
	private String payNotifyUrl;
	
	public XHFPayServiceImpl(Map<String,String> data) {
		if(data!=null){
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
	}

	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject smPay(PayEntity payEntity) {
		logger.info("新汇付支付扫码支付开始======================START==================");
		try {
			//封装请求参数
			Map<String, String> data = sealRequest(payEntity);
			//生成签名串
			String sign = generatorSign(data);
			data.put("pay_md5sign", sign);
			logger.info("新汇付支付请求参数:"+JSONObject.fromObject(data).toString());
			//生成请求表单
			String resStr = HttpUtils.toPostForm(data, payUrl);
			logger.info("[XHF]新汇支付响应信息:"+resStr);
			if(StringUtils.isBlank(resStr)){
				logger.info("[XHF]新汇支付扫码支付发起HTTP请求无响应结果");
				return PayResponse.error("[XHF]新汇支付扫码支付发起HTTP请求无响应结果");
			}
			JSONObject resObj = JSONObject.fromObject(resStr);
			if(resObj.containsKey("url")&&resObj.containsKey("msg")){
				return PayResponse.sm_qrcode(payEntity, resObj.getString("url"), resObj.getString("msg"));
			}
			return PayResponse.error("下单失败"+resStr);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("新汇付支付生成异常:"+e.getMessage());
			return PayUtil.returnWYPayJson("error", "form", "", "", "");
		}
	}
	@Override
	public String callback(Map<String, String> data) {
		try {
			data.remove("attach");
            String sourceSign = data.remove("sign");
            logger.info("[XHF]新汇支付回调原签名串:"+sourceSign);
            String sign = generatorSign(data);
            logger.info("[XHF]新汇支付回调:本地签名:" + sign + "      服务器签名:" + sourceSign);
            if(sign.equals(sourceSign)) return "success";
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[XHF]新汇支付回调验签异常:"+e.getMessage());
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
	public Map<String, String> sealRequest(PayEntity payEntity){
		SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-DD HH:mm:ss");
		String toDate = dateFormat.format(new Date());
		Map<String,String> data = new HashMap<>();
		data.put("pay_memberid", payMemberid);
		data.put("pay_orderid", payEntity.getOrderNo());
		data.put("pay_amount", String.valueOf(payEntity.getAmount()));
		data.put("pay_applydate", toDate);
		data.put("pay_bankcode", payEntity.getPayCode());
		data.put("pay_notifyurl", payNotifyUrl);
		data.put("pay_producturl", "");
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
    	Map<String,String> sortmap = MapUtils.sortByKeys(data);
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
        logger.info("[XHFZF]新汇付支付生成待签名串:{}",signStr);
        String sign = MD5Utils.md5toUpCase_32Bit(signStr);
        logger.info("[XHFZF]新汇付支付生成加密签名串:{}",sign);
        return sign;
    }
}
