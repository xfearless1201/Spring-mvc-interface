package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayConstant;
import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;

import net.sf.json.JSONObject;

public class HFPayServiceImpl implements PayService{
	
	private final static Logger logger = LoggerFactory.getLogger(HFPayServiceImpl.class);
	
    private String merchant_code;//商户号
    private String key;//结算类型
    private String payUrl;//支付地址
    private String notifyUrl;//异步通知地址
    
    public HFPayServiceImpl(Map<String,String> data){
    	if(data != null && !data.isEmpty()){
            if(data.containsKey("merchant_code")){
                this.merchant_code = data.get("merchant_code");
            }
            if(data.containsKey("key")){
                this.key = data.get("key");
            }
            if(data.containsKey("payUrl")){
                this.payUrl = data.get("payUrl");
            }
            if(data.containsKey("notifyUrl")){
                this.notifyUrl = data.get("notifyUrl");
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
		logger.info("[HFPay]汇丰支付扫码支付开始---------------START---------------");
		try {
            //获取支付请求参数
            Map<String,String> data = sealRequest(payEntity,2);
            //生成签名串
            String sign = generatorSign(data);
            data.put("sign",sign);//验签字段 是   MD5加密
            String reqParams = formatMapToString(data);
            logger.info("[HFPay]汇丰支付请求报文:"+reqParams);
            //发起HTTP-POST请求
            String response = HttpUtils.toPostJson(reqParams, payUrl);
            if(StringUtils.isBlank(response)){
                logger.error("[HFPay]汇丰支付失败,请求无响应结果!");
                return PayUtil.returnPayJson("error", "2", "下单失败,发起HTTP请求无响应结果!", "", 0, "", "展示请求响应结果:"+response);
            }
            
            //解析响应结果
            JSONObject jsonObject = JSONObject.fromObject(response);
            if(jsonObject.containsKey("flag") && jsonObject.getString("flag").equalsIgnoreCase("00")){
                //flag 00成功 01失败
                //下单成功
                String qrCodeURL = jsonObject.getString("qrCodeUrl");//
                if(StringUtils.isBlank(payEntity.getMobile())){
                    //PC端
                    return PayUtil.returnPayJson("success", "2", "下单成功!", payEntity.getUsername(),
                    		payEntity.getAmount(), payEntity.getOrderNo(), qrCodeURL);
                }
                //移动端
                if(PayConstant.CHANEL_ALI.equals(payEntity.getPayType())){
                    String formStr = HttpUtils.generatorForm(null, qrCodeURL.toLowerCase());
                    return PayResponse.sm_form(payEntity, formStr, "下单成功");
                }
                return PayUtil.returnPayJson("success", "4", "下单成功!", payEntity.getUsername(),
                		payEntity.getAmount(), payEntity.getOrderNo(), qrCodeURL.toLowerCase());   //当 pay_type=20201 时为手机WAP页面 可以直接跳转 qrCodeUrl（需把qrCodeUrl转换为小写）唤醒支付宝进行支付
            }
            //下单失败
            String respMsg = jsonObject.getString("msg");
            return PayUtil.returnPayJson("error", "2", "下单失败:"+respMsg, payEntity.getUsername(), payEntity.getAmount(), payEntity.getOrderNo(),response);
            
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[HFPay]汇丰支付扫码支付异常:"+e.getMessage());
            return PayUtil.returnPayJson("error", "2", "[HFPay]汇丰扫码支付异常!", "",0, "",e.getMessage());
        }
	}

	@Override
	public String callback(Map<String, String> data) {
		// TODO Auto-generated method stub
		logger.info("[HF]汇丰支付回调验签开始===================START===================");
        try {
            //获取验签原签名串
            String sourceSign = data.remove("sign");
            
            logger.info("[HF]汇丰支付验签原签名串:{}", sourceSign);
            
            //生成验签签名
            String sign = generatorSign(data);
            logger.info("[HF]汇丰支付验签生成签名串:{}", sign);
            if(sourceSign.equalsIgnoreCase(sign))
            	return "success";
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[HF]汇丰支付回调验签异常:{}", e.getMessage());
        }
        return "fail";
	}
	
	public String query(Map<String,String> data) {
//		logger.info("[JY]九域支付查询开始===================START=====================");
//		try {
//			Map<String,String> queryMap = new HashMap<>();
//			queryMap.put("pay_memberid", memberid);
//			queryMap.put("pay_orderid",data.get("order_no"));
//			//生成签名串
//            String sign = generatorSign(data);
//            queryMap.put("pay_md5sign", sign);
//            String response = HttpUtils.post(data, queryUrl);
//            if(StringUtils.isBlank(response)){
//                logger.error("[HFPay]汇丰支付失败,请求无响应结果!");
//                return "failed";
//            }
//            
//            //解析响应结果
//            JSONObject jsonObject = JSONObject.fromObject(response);
//            if(jsonObject.containsKey("flag") && jsonObject.getString("flag").equalsIgnoreCase("00")){
//                //flag 00成功 01失败
//                //下单成功
//                return "success";
//
//            }
//		} catch (Exception e) {
//            e.printStackTrace();
//            logger.error("[JY]九域支付回调验签异常:{}", e.getMessage());
//        }
        return null;
	}
	
	 /**
     * 
     * @Description 封装支付请求参数
     * @param entity
     * @param type 1 网银 
     * @return
     * @throws Exception
     */
    private Map<String,String> sealRequest(PayEntity entity,Integer type) throws Exception{
        logger.info("[HF]汇丰支付封装请求参数开始=====================START==================");
        try {
            //创建支付请求参数存储对象
            Map<String,String> data = new HashMap<>();
            //订单时间
            Date orderTime = new Date();
            //订单金额
            String amount = new DecimalFormat("##").format(entity.getAmount());//交易1金额 是   分为单位
            data.put("merchant_code",merchant_code);//商户号    是   171107105912001
            if(type == 2){
                data.put("pay_type",entity.getPayCode());  //10101:微信     10201:支付宝     10102:微信公众号     20201：H5支付宝
            }
            data.put("order_no",entity.getOrderNo());//订单号    是   20位长度唯一订单标识
            data.put("order_time",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(orderTime));
            data.put("order_amount",amount);//交易1金额 是   元为单位
            data.put("customer_ip",entity.getIp());//消费者ip
            Random rd = new Random();
            data.put("random_char",String.valueOf(rd.nextInt(9999)));//随机串
            data.put("notify_url",notifyUrl);//后台回调通知地址 是   后台回调通知地址
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[HF]汇丰支付封装请求参数异常:"+e.getMessage());
            throw new Exception("封装支付请求参数异常!");
        }
    }
    
    /**
     * 
     * @Description 生成支付签名串
     * @param data
     * @return
     * @throws Exception
     */
    public String generatorSign(Map<String,String> data) throws Exception{
        logger.info("[HF]汇丰支付生成支付签名串开始==================START========================");
        try {
             //签名规则:
            //把除签名字段和集合字段以外的所有字段（不包括值为null的）内容按照报文字段字典顺序，
            //依次按照“字段名=字段值”的方式用“&”符号连接，最后加上机构工作密钥，使用MD5算法计算数字签名，填入签名字段。接受方应按响应步骤验证签名。
            Map<String,String> treemap = new TreeMap<>(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }
            });
            treemap.putAll(data);
            
            //生成待签名串
            StringBuffer sb = new StringBuffer();
            Iterator<String> iterator = treemap.keySet().iterator();
            while(iterator.hasNext()){
                String key = iterator.next();
                String val = treemap.get(key);
                
                if(StringUtils.isBlank(val) || key.equalsIgnoreCase("sign") || key.equalsIgnoreCase("signature") || key.equalsIgnoreCase("pay_bankid") 
                		|| key.equalsIgnoreCase("pay_productname") || key.equalsIgnoreCase("return_params")) 
                	continue;
                sb.append(key).append("=").append(val).append("&");
            }
            
            //生成待签名串
            String signStr = sb.toString()+"key="+key;
            logger.info("[HF]汇丰支付生成待签名串:"+signStr);
            String sign = MD5Utils.md5toUpCase_32Bit(signStr);
            logger.info("[HF]汇丰支付生成加密签名串:"+sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[HF]汇丰支付生成支付签名串异常:"+e.getMessage());
            throw new Exception("生成支付签名串异常!");
        }
    }
    
    
    /**
     * 
     * @Description 格式化map参数
     * @param data
     * @return
     * @throws Exception
     */
    public String formatMapToString(Map<String,String> data) throws Exception{
        logger.info("[HF]汇丰支付请求参数类型转换开始====================START====================");
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("{");
            Iterator<String> iterator = data.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                String val = data.get(key);
                sb.append('"').append(key).append('"').append(":");
                if(StringUtils.isNoneBlank(val)){
                    sb.append('"').append(val).append('"');
                }else{
                    sb.append('"').append('"');
                }
                if(iterator.hasNext()){
                    sb.append(",");
                }
            }
            sb.append("}");
            String reqParams = sb.toString();
            return reqParams;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[HF]汇丰支付请求参数类型转换异常:"+e.getMessage());
            throw new Exception("请求参数转换类型异常!");
        }
    }

}
