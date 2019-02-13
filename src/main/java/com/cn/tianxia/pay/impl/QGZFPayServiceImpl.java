package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
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

public class QGZFPayServiceImpl implements PayService{
	private final static Logger logger = LoggerFactory.getLogger(QGZFPayServiceImpl.class);
	/**商户号*/
	private String payMemberid;
	/**支付地址*/
	private String payUrl;
	/**密钥*/
	private String md5Key;
	/**回调地址*/
	private String payNotifyUrl;
	
	public QGZFPayServiceImpl(Map<String,String> data) {
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
		logger.info("[QGZF]钱柜支付扫码支付开始======================START==================");
		try {
			//封装请求参数
			Map<String, String> data = sealRequest(payEntity);
			//生成签名串
			String sign = generatorSign(data);
			data.put("sign", sign);
			logger.info("[QGZF]钱柜支付请求参数:"+JSONObject.fromObject(data).toString());
			//生成请求表单
			String resStr = HttpUtils.toPostForm(data, payUrl);
			logger.info("[QGZF]钱柜支付响应信息:"+resStr);
			if(StringUtils.isBlank(resStr)){
				logger.info("[QGZF]钱柜支付扫码支付发起HTTP请求无响应结果");
				return PayResponse.error("[QGZF]钱柜支付扫码支付发起HTTP请求无响应结果");
			}
			JSONObject resObj = JSONObject.fromObject(resStr);
			if(resObj.containsKey("code")&&resObj.getInt("code")==200){
				resObj = JSONObject.fromObject(resObj.getString("data"));
				if (StringUtils.isNotBlank(payEntity.getMobile())) {
					return PayResponse.sm_link(payEntity, resObj.getString("pay_url"), "下单成功");
				}
				return PayResponse.sm_qrcode(payEntity, resObj.getString("pay_url"), "下单成功");
			}
			return PayResponse.error("下单失败"+resStr);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("[QGZF]钱柜支付生成异常:"+e.getMessage());
			return PayUtil.returnWYPayJson("error", "form", "", "", "");
		}
	}
	@Override
	public String callback(Map<String, String> data) {
		try {
            String sourceSign = data.remove("sign");
            logger.info("[QGZF]钱柜支付回调原签名串:"+sourceSign);
            String sign = generatorSign(data);
            logger.info("[QGZF]钱柜支付回调:本地签名:" + sign + "      服务器签名:" + sourceSign);
            if(sign.equals(sourceSign)) return "success";
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[QGZF]钱柜支付回调验签异常:"+e.getMessage());
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
		DecimalFormat df = new DecimalFormat("0.00");
		Map<String,String> data = new HashMap<>();
		data.put("merchant_code", payMemberid);
		data.put("pay_money", df.format(payEntity.getAmount()));
		data.put("pay_code", payEntity.getPayCode());
		data.put("out_trade_no", payEntity.getOrderNo());
		data.put("order_time", toDate);
		data.put("order_ip", payEntity.getIp());
		data.put("notify_url", payNotifyUrl);
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
        StringBuffer sb1 = new StringBuffer();
        StringBuffer sb2 = new StringBuffer();
        Iterator<String> iterator = sortmap.keySet().iterator();
        while(iterator.hasNext()){
            String key = iterator.next();
            String val = sortmap.get(key);
            if(StringUtils.isBlank(val) || key.equalsIgnoreCase("sign")) continue;
            sb1.append(key).append(val);
        }
        //生成待签名串
        String signStr = sb2.append(md5Key).append(sb1.toString()).toString();
        logger.info("[QGZF]钱柜支付生成待签名串:{}",signStr);
        String sign = MD5Utils.md5toUpCase_32Bit(MD5Utils.md5(signStr.getBytes())+sb1.toString());
        logger.info("[QGZF]钱柜支付生成加密签名串:{}",sign);
        return sign;
    }
   /* public static void main(String[] args) {
    	HashMap<String, String> params = new HashMap<>();
        params.put("merchant_code","VP2019010702");
        params.put("pay_code","alipayh5");
        params.put("pay_money","100.00");
        params.put("out_trade_no","QGZFbl1201901081016311016319226");
        params.put("system_order_sn","SN201901081021015575");
        params.put("status","2");
        params.put("sign","E92FE14487563FE7A2253CC99BC0AC63");
        params.put("md5Key","njObNkUNspnM7m_rpyzHj4pHTRclRb1a");
        QGZFPayServiceImpl apayPayServiceImpl = new QGZFPayServiceImpl(params);
        String callback = apayPayServiceImpl.callback(params);
        System.out.println(callback);
	}*/
}
