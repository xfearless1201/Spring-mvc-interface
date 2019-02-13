package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName BSPayServiceImpl
 * @Description 百盛支付
 * @author Hardy
 * @Date 2018年9月28日 下午2:20:43
 * @version 1.0.0
 */
public class BSPayServiceImpl implements PayService{
    
    //日志
    private static final Logger logger = LoggerFactory.getLogger(BSPayServiceImpl.class);
    
    private String MerchantId;//商户号
    
    private String NotifyUrl;//异步回调地址
    
    private String payUrl;//支付请求地址
    
    private String secret;//秘钥

    //构造器,初始化基本参数信息
    public BSPayServiceImpl(Map<String,String> data){
        if(data != null && !data.isEmpty()){
            if(data.containsKey("MerchantId")){
                this.MerchantId = data.get("MerchantId");
            }
            if(data.containsKey("NotifyUrl")){
                this.NotifyUrl = data.get("NotifyUrl");
            }
            if(data.containsKey("payUrl")){
                this.payUrl = data.get("payUrl");
            }
            if(data.containsKey("secret")){
                this.secret = data.get("secret");
            }
        }
    }
    
    /**
     * 
     * @Description 网银支付
     * @param payEntity
     * @return
     */
    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        logger.info("[BS]百盛支付网银支付开始==================START=========================");
        try {
          //获取支付请求参数
            Map<String,String> data = sealRequest(payEntity);
            //生成签名串
            String sign = generatorSign(data);
            data.put("sign", sign);
            logger.info("[BS]百盛支付发起请求最终参数:"+JSONObject.fromObject(data).toString());
            //发起支付请求,建议:扫码类，请使用curl或者httpclient获取，返回的是JSON格式数据；
            //WAP类，反扫类，网银快捷，请使用form表单的action跳转；
            //PC端:发起HTTP请求
            String response = HttpUtils.generatorForm(data, payUrl);
            return PayUtil.returnWYPayJson("success", "form", response, payEntity.getPayUrl(), "");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[BS]百盛支付网银支付异常:"+e.getMessage());
            return PayUtil.returnWYPayJson("error", "", "", "", "");
        }
    }
    
    /**
     * 
     * @Description 扫码支付
     * @param payEntity
     * @return
     */
    @Override
    public JSONObject smPay(PayEntity payEntity) {
        logger.info("[BS]百盛扫码支付开始===============start================");
        try {
            String mobile = payEntity.getMobile();
            String username = payEntity.getUsername();
            double amount = payEntity.getAmount();
            String orderNo = payEntity.getOrderNo();
            //获取支付请求参数
            Map<String,String> data = sealRequest(payEntity);
            //生成签名串
            String sign = generatorSign(data);
            data.put("sign", sign);
            logger.info("[BS]百盛支付发起请求最终参数:"+JSONObject.fromObject(data).toString());
            
            //发起支付请求,建议:扫码类，请使用curl或者httpclient获取，返回的是JSON格式数据；
            //WAP类，反扫类，网银快捷，请使用form表单的action跳转；
            //PC端:发起HTTP请求
            String response = HttpUtils.toPostForm(data, payUrl);
            if(StringUtils.isBlank(response)){
                logger.error("[BS]百盛支付发起HTTP请求无响应结果!");
                return PayUtil.returnPayJson("error", "2", "下单失败!",username,amount,orderNo, "发起HTTP请求无响应结果!");
            }
            logger.info("[BS]百盛支付发起HTTP请求响应结果:"+response);
            //解析响应结果
            JSONObject jsonObject = JSONObject.fromObject(response);
            String Code = jsonObject.getString("Code");//错误代码（Code=200时代表成功，其他为失败）
            if(Code.equals("200")){
                //下单成功
                String QrCodeUrl = jsonObject.getString("QrCodeUrl");//二维码链接
                if(StringUtils.isBlank(mobile)){
                    return PayUtil.returnPayJson("success", "2", "下单成功", username, amount, orderNo, QrCodeUrl);
                }
                return PayUtil.returnPayJson("success", "4", "下单成功", username, amount, orderNo, QrCodeUrl);
            }
            
            String Message = jsonObject.getString("Message");//错误消息（当Code!=200时，Message返回错误描述原因，Code=200时，不返回任何数据。）
            return PayUtil.returnPayJson("error", "2", "下单失败", username, amount, orderNo, "失败原因:"+Message);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[BS]百盛扫码支付异常:"+e.getMessage());
            return PayUtil.returnPayJson("error", "2", "下单失败", "", 0, "", "失败原因:"+e.getMessage());
        }
    } 
    
    
    /**
     * 
     * @Description 回调验签
     * @param data
     * @return
     */
    @Override
    public String callback(Map<String,String> data){
        logger.info("[BS]百盛支付回调验签开始==============START==============");
        try {
            //回调原串
            String sourceSign = data.get("Sign").toLowerCase();
            logger.info("[BS]百盛支付回调原签名串:"+sourceSign);
            //生产签名
            String sign = generatorSign(data).toLowerCase();
            logger.info("[BS]百盛支付回调生成签名串:"+sign);
            if(sign.equals(sourceSign)) return "success";
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[BS]百盛支付回调验签异常:"+e.getMessage());
        }
        return "";
    }
    
    /**
     * 
     * @Description 组装支付请求参数
     * @param entity
     * @return
     * @throws Exception
     */
    private Map<String,String> sealRequest(PayEntity entity) throws Exception{
        logger.info("[BS]百盛支付组装请求参数开始=====================START=================");
        try {
            //创建存储请求参数对象
            String amount = new DecimalFormat("##").format(entity.getAmount()*100);
            Map<String,String> data = new HashMap<>();
            data.put("MerchantId",MerchantId);//商户号
            data.put("Timestamp",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));//发送请求的时间，格式"yyyy-MM-dd HH:mm:ss"
            data.put("PaymentTypeCode",entity.getPayCode());//入款类型，具体请参考附录中的【入款类型】
            data.put("OutPaymentNo",entity.getOrderNo());//商户的入款流水号
            data.put("PaymentAmount",amount);//入款金额，单位为分，1元 = 100
            data.put("NotifyUrl",NotifyUrl);//入款成功异步通知URL
            data.put("PassbackParams",entity.getRefererUrl());//通知应答时，会按照商户在入款请求时上送的值进行原样返回
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[BS]百盛支付组装请求参数异常:"+e.getMessage());
            throw new Exception("百盛支付组装请求参数失败!");
        }
    }
    
    /**
     * 
     * @Description 生产签名串
     * @param data
     * @return
     * @throws Exception
     */
    private String generatorSign(Map<String,String> data) throws Exception{
        try {
           //签名规则:1、将请求参数按照ASCII码从小到大排序，并将其中参数值为空的剔除；
           //2、按照“key=value&key1=value1&key2=value2&……”的格式拼接起来；
           //3、将2中拼接的数据直接于商户交易密钥进行拼接，中间不含任何字符；
           //4、将3中拼接的数据以 MD5 方式加签，生成【Sign】参数。
            
            // 排序
            Map<String,String> treemap = new TreeMap<>(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }
            });
            treemap.putAll(data);
            
            //遍历参数，组装签名字符串
            StringBuffer sb = new StringBuffer();
            Iterator<String> keys = treemap.keySet().iterator();
            while(keys.hasNext()){
                String key = keys.next();
                String val = treemap.get(key);
                if(StringUtils.isBlank(val) || key.equals("sign") || key.equals("Sign")) continue;
                sb.append("&").append(key).append("=").append(val);
            }
            sb.append(secret);
            
            String signStr = sb.toString().replaceFirst("&", "");
            logger.info("[BS]百盛支付生成待签名串:"+signStr);
            String sign = MD5Utils.md5toUpCase_32Bit(signStr).toLowerCase();
            logger.info("[BS]百盛支付生成MD5加密签名串:"+sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[BS]百盛支付生产签名串异常:"+e.getMessage());
            throw new Exception("生产MD5签名串失败!");
        }
    }
    
    public static void main(String[] args) {
        JSONObject data = new JSONObject();
        data.put("MerchantId", "18030423114709");
        data.put("NotifyUrl", "http://www.baidu.com");
        data.put("payUrl", "https://dev.baishengpay.com/Payment/Gateway");
        data.put("secret", "9fdd579853eff32032f56f1017fb625a");
        System.err.println(data.toString());
    }
}
