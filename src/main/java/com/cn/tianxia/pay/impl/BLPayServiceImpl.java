/****************************************************************** 
 *
 * Powered By tianxia-online. 
 *
 * Copyright (c) 2018-2020 Digital Telemedia 天下网络 
 * http://www.d-telemedia.com/ 
 *
 * Package: com.cn.tianxia.pay.impl 
 *
 * Filename: CORALPayServiceImpl.java
 *
 * Description: BL宝来支付对接
 *
 * Copyright: Copyright (c) 2018-2020 
 *
 * Company: 天下网络科技 
 *
 * @author: Kay
 *
 * @version: 1.0.0
 *
 * Create at: 2018年10月11日 20:51
 *
 * Revision: 
 *
 * 2018/10/11 20:51
 * - first revision 
 *
 *****************************************************************/
package com.cn.tianxia.pay.impl;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;

import net.sf.json.JSONObject;
/**
 * @ClassName BLPayServiceImpl
 * @Description BL宝来支付对接
 * @Author kay
 * @Date 2018年10月11日 20:51
 * @Version 1.0.0
 **/
public class BLPayServiceImpl implements PayService {
	private final static org.slf4j.Logger logger = LoggerFactory.getLogger(BLPayServiceImpl.class);
	private String payUrl ;//支付地址
    private String mer_id;//商户编号
    private String notify_url;//异步回调地址
    private String key;//秘钥
    private String version;//支付商版本号
    private String orderQuery;//查询订单接口
    
    public BLPayServiceImpl(Map<String,String> data) {
        if(data != null && !data.isEmpty()){
            if(data.containsKey("payUrl")){
                this.payUrl = data.get("payUrl");
            }
            if(data.containsKey("mer_id")){
                this.mer_id = data.get("mer_id");
            }
            if(data.containsKey("notify_url")){
                this.notify_url = data.get("notify_url");
            }
            if(data.containsKey("key")){
                this.key = data.get("key");
            }
            if(data.containsKey("version")){
            	this.version = data.get("version");
            }
            if(data.containsKey("orderQuery")){
            	this.orderQuery = data.get("orderQuery");
            }
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
		logger.info("[BL]宝来支付扫码支付开始===================START=======================");
        try {
            
            String username = payEntity.getUsername();
            String order_no = payEntity.getOrderNo();
            double amount = payEntity.getAmount();
            //获取支付请求参数
            Map<String,String> data = sealRequest(payEntity, 0);
            //生成签名串
            String sign = generatorSign(data);
            data.put("sign",sign);//签名，详见 签名生成算法
            logger.info("[BL]宝来支付请求报文:"+JSONObject.fromObject(data).toString());
            Map<String,String> maps=new HashMap<String,String>();
            maps.put("amount", data.get("amount"));
            maps.put("callback_url", data.get("callback_url"));
            maps.put("mer_id", data.get("mer_id"));
            maps.put("notify_url", data.get("notify_url"));
            maps.put("out_trade_no", data.get("out_trade_no"));
            maps.put("pay_type", data.get("pay_type"));
            maps.put("version", version);
            maps.put("key", key);
            maps.put("sign", data.get("sign"));
            String response = HttpUtils.generatorForm(maps, payUrl);
            logger.info("[BL]宝来支付扫码支付完成"+response);
            return PayUtil.returnPayJson("success", "1", "success", username, amount, order_no, response);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[BL]宝来支付扫码支付异常:"+e.getMessage());
            return PayUtil.returnPayJson("error","2","下单失败!","",0,"",e.getMessage());
        }
	}

	/**
	 * 异步回调接口
	 */
	@Override
	public String callback(Map<String, String> data) {
		logger.info("[BL]宝来支付回调验签开始===================START==============");
        try {
            //获取验签原串
            String sourceSign = data.get("sign");
            //生成待签名串
            String sign = generatorSign(data);;
            logger.info("[BL]宝来支付回调验签,服务器签名:["+sourceSign+"],本地签名:["+sign+"]");
            //验签
            if(sourceSign.equalsIgnoreCase(sign)) return "success";
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[BL]宝来支付回调验签异常:"+e.getMessage());
        }
        return "faild";
	}
	
	
	/**
     * 
     * @Description 封装支付请求参数
     * @param entity
     * @param type
     * @return
     * @throws Exception
     */
    private Map<String,String> sealRequest(PayEntity entity,Integer type) throws Exception{
        logger.info("[BL]宝来支付组装支付请求参数开始===================START==================");
        try {
            //创建存储支付请求参数对象
            Map<String,String> data = new HashMap<>();
            data.put("version", version);//版本号
            data.put("mer_id", mer_id);//商户号
            data.put("out_trade_no",entity.getOrderNo());//订单号，20位
            String amount = new DecimalFormat("0").format(entity.getAmount());
            data.put("amount",amount);//订单金额  
            data.put("pay_type",entity.getPayCode());//支付编号
            data.put("notify_url", notify_url);//异步通知URL,不能带有任何参数
            data.put("callback_url", entity.getRefererUrl());//同步跳转URL,不能带有任何参数
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[BL]宝来支付组装支付请求参数异常:"+e.getMessage());
            throw new Exception("组装支付请求参数异常!");
        }
    }
    
    /**
     * 查询订单状态
     * @param outTradeNo
     * @return
     * @throws Exception 
     */
    public JSONObject orderQuery(String outTradeNo) throws Exception {

		JSONObject json = new JSONObject();
		String result=HttpUtils.toPostForm(outTradeNo, payUrl);
		// 判断是否成功
		if (result.contains("success")) {
			json = JSONObject.fromObject(result);
			json.put("bl", "success");
			logger.info("订单查询成功" + json);
		} else {
			// 发生错误
			json.put("bl", "fail");
			json.put("msg", "error: " + result );
			logger.info("订单查询失败:" + json);
		}
		return json;
	}
    
    /**
     * 
     * @Description 生成签名串
     * @param data
     * @return
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
	private String generatorSign(Map<String,String> data) throws Exception{
        logger.info("[BL]宝来支付生成签名串开始================START=================");
        try {
        	SortedMap<Object,Object> parameters = new TreeMap<Object,Object>();
        	parameters.put("mer_id", data.get("mer_id"));
        	parameters.put("out_trade_no", data.get("out_trade_no"));
        	parameters.put("pay_type", data.get("pay_type"));
        	parameters.put("amount", data.get("amount"));
        	if(data.get("callback_url")!=null){
        		parameters.put("callback_url", data.get("callback_url"));
        		parameters.put("notify_url", notify_url);
        	}else{
        		parameters.put("real_fee",data.get("real_fee"));
        		parameters.put("paydate",data.get("paydate"));
        		parameters.put("paytime",data.get("paytime"));
        		parameters.put("plat_trade_no",data.get("plat_trade_no"));
        	}
        	parameters.put("version", data.get("version"));
        	StringBuffer sb = new StringBuffer();
        	Set es = parameters.entrySet();  //所有参与传参的参数按照accsii排序（升序）
    	    Iterator it = es.iterator();
    	    while(it.hasNext()) {
    	        Map.Entry entry = (Map.Entry)it.next();
    	        String k = (String)entry.getKey();
    	        Object v = entry.getValue();
    	        //空值不传递，不参与签名组串
    	        if(null != v && !"".equals(v)) {
    	            sb.append(k + "=" + v + "&");
    	        }
    	    }
    	    sb.append("key=").append(key);
            logger.info("[BL]宝来支付生成待签名串:"+sb);
            String sign = md5To32(sb.toString()).toLowerCase();
            logger.info("[BL]宝来支付生成加密签名串:"+sign);
//            
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[BL]宝来支付生成签名串异常:"+e.getMessage());
            throw new Exception("生成签名串失败!");
        }
    }
    
    /**
     * 生成32位小号md5码
     * @param str
     * @return
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public static String md5To32(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException{
    	MessageDigest md5 = MessageDigest.getInstance("MD5");
    	md5.update((str).getBytes("UTF-8"));
    	byte b[] = md5.digest();
    	 
    	int i;
    	StringBuffer buf = new StringBuffer("");
    	 
    	for(int offset=0; offset<b.length; offset++){
    		i = b[offset];
    		if(i<0){
    			i+=256;
    		}
    		if(i<16){
    			buf.append("0");
    		}
    		buf.append(Integer.toHexString(i));
    	}
    	return buf.toString();
    }
    
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException {
    	SortedMap<Object,Object> parameters = new TreeMap<Object,Object>();
    	parameters.put("mer_id", "88533001");
    	parameters.put("out_trade_no", "BLbl1201810141201531201531613");
    	parameters.put("pay_type", "alipay");
    	parameters.put("amount", "10");
    	parameters.put("real_fee","9.98");
    	parameters.put("paydate","2018-10-14");
    	parameters.put("paytime","12:2:0");
    	parameters.put("plat_trade_no", "BL201810141202013193");
    	parameters.put("version", "2.0.1");
    	StringBuffer sb = new StringBuffer();
    	Set es = parameters.entrySet();  //所有参与传参的参数按照accsii排序（升序）
	    Iterator it = es.iterator();
	    while(it.hasNext()) {
	        Map.Entry entry = (Map.Entry)it.next();
	        String k = (String)entry.getKey();
	        Object v = entry.getValue();
	        //空值不传递，不参与签名组串
	        if(null != v && !"".equals(v)) {
	            sb.append(k + "=" + v + "&");
	        }
	    }
	    sb.append("key=").append("2512fb893ded5c70d5b4d5fb72b072d9");
        logger.info("[BL]宝来支付生成待签名串:"+sb);
        String sign = md5To32(sb.toString()).toLowerCase();
        logger.info("[BL]宝来支付生成加密签名串:"+sign);
        System.out.println(sign);
	}
    
    
    
}
