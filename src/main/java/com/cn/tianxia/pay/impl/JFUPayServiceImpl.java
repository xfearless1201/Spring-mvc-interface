package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import com.cn.tianxia.pay.utils.MapUtils;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName JFUPayServiceImpl
 * @Description 极付支付
 * @author Hardy
 * @Date 2018年10月26日 上午11:03:50
 * @version 1.0.0
 */
public class JFUPayServiceImpl implements PayService{
    
    //日志
    private static final Logger logger = LoggerFactory.getLogger(JFUPayServiceImpl.class);
    
    private String apiCode;//商户号
    
    private String payUrl;//支付请求地址
    
    private String notifyUrl;//服务器回调地址
    
    private String secret;//签名秘钥

    private String returnType;//返回数据类型
    
    //构造器,初始化数据
    public JFUPayServiceImpl(Map<String,String> data) {
        if(data != null && !data.isEmpty()){
            if(data.containsKey("apiCode")){
                this.apiCode = data.get("apiCode");
            }
            if(data.containsKey("payUrl")){
                this.payUrl = data.get("payUrl");
            }
            if(data.containsKey("notifyUrl")){
                this.notifyUrl = data.get("notifyUrl");
            }
            if(data.containsKey("secret")){
                this.secret = data.get("secret");
            }
            if(data.containsKey("returnType")){
                this.returnType = data.get("returnType");
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
        logger.info("[JFU]极付支付扫码支付开始=========================START======================");
        try {
            //获取支付请求报文
            Map<String,String> data = sealRequest(payEntity);
            //生成签名串
            String sign = generatorSign(data);
            data.put("sign", sign);
            logger.info("[JFU]极付扫码支付请求报文:"+JSONObject.fromObject(data).toString());
            if(returnType.equalsIgnoreCase("html")){
                String formStr = HttpUtils.generatorForm(data, payUrl);
                logger.info("[JFU]极付支付扫码生成form表单结果:"+formStr);
                return PayUtil.returnPayJson("success","1","form表单创建成功!",payEntity.getUsername(),payEntity.getAmount(),payEntity.getOrderNo(),formStr);
            }
            //发起请求
            String response = HttpUtils.toPostForm(data, payUrl);
            if(StringUtils.isBlank(response)){
                logger.info("[JFU]极付支付扫码支付发起HTTP请求无响应结果![请求报文:"+JSONObject.fromObject(data).toString()+"],[支付 请求地址:"+payUrl+"]");
                return PayUtil.returnPayJson("error","2","[JFU]极付扫码支付发起HTTP请求无响应结果!","",0,"",response);
            }
            logger.info("[JFU]极付扫码支付发起HTTP请求响应结果:"+response);
            //解析响应结果
            JSONObject jsonObject = JSONObject.fromObject(response);
            if(jsonObject.containsKey("messages") && StringUtils.isNoneBlank(jsonObject.getString("messages"))){
                JSONObject resultJson = JSONObject.fromObject(jsonObject.getString("messages"));
                if(resultJson.containsKey("returncode") && resultJson.getString("returncode").equalsIgnoreCase("SUCCESS")){
                    String qrurl = jsonObject.getString("payurl").replace("\\", "");
                    if(StringUtils.isBlank(payEntity.getMobile())){
                        return PayUtil.returnPayJson("success","2","下单成功",payEntity.getUsername(),payEntity.getAmount(),payEntity.getOrderNo(),qrurl);
                    }
                    return PayUtil.returnPayJson("success","4","下单成功",payEntity.getUsername(),payEntity.getAmount(),payEntity.getOrderNo(),qrurl);
                }
            }
            return PayUtil.returnPayJson("error","2","下单失败!","",0,"","请求响应结果:"+response);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[JFU]极付支付扫码支付异常:"+e.getMessage());
            return PayUtil.returnPayJson("error","2","[JFU]极付扫码支付异常:"+e.getMessage(),"",0,"", e.getMessage());
        }
    }

    /**
     * 回调验签
     */
    @Override
    public String callback(Map<String, String> data) {
        logger.info("[JFU]极付支付回调验签开始=======================start=======================");
        try {
            //获取回调原签名串
            String sourceSign = data.get("sign");
            logger.info("[JFU]极付支付回调原签名串:{}",sourceSign);
            //加入api_code
            data.put("api_code",apiCode);
            String sign = generatorSign(data);
            logger.info("[JFU]极付支付生成回调验签加密签名串:{}",sign);
            
            if(sourceSign.equalsIgnoreCase(sign)) return "success";
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[JFU]极付支付回调验签异常:{}",e.getMessage());
        }
        return "faild";
    }

    
    /**
     * 
     * @Description 组装支付请求参数
     * @param entity
     * @return
     * @throws Exception
     */
    private Map<String,String> sealRequest(PayEntity entity) throws Exception{
        logger.info("[JFU]极付支付组装支付请求参数开始====================START=====================");
        try {
            //创建支付请求报文存储对象
            Map<String,String> data = new HashMap<>();
            String amount = new DecimalFormat("0.00").format(entity.getAmount());
            data.put("return_type",returnType);//返回数据类型,必填参数json， html（详情请看，返回说明）
            data.put("api_code",apiCode);//商户号
            data.put("is_type",entity.getPayCode());//支付类型
            data.put("price",amount);//订单定价,留2位小数，不能传0
            data.put("order_id",entity.getOrderNo());//您的自定义单号
            data.put("time",System.currentTimeMillis()+"");//发起时间,时间戳
            data.put("mark","TOP-UP");//描述
            data.put("return_url",entity.getRefererUrl());//成功后网页跳转地址
            data.put("notify_url",notifyUrl);//通知状态异步回调接收地址
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[JFU]极付支付组装支付请求参数异常:"+e.getMessage());
            throw new Exception("[JFU]极付支付组装支付请求参数异常");
        }
    }
    
    
    /**
     * 
     * @Description 生成签名串
     * @param data
     * @return
     * @throws Exception
     */
    private String generatorSign(Map<String,String> data) throws Exception{
        logger.info("[JFU]极付支付生成支付签名串开始=======================START=====================");
        try {
            //参数排序
            Map<String,String> sortmap = MapUtils.sortByKeys(data);
            //拼接参数，组装待签名串
            StringBuffer sb = new StringBuffer();
            Iterator<String> iterator = sortmap.keySet().iterator();
            while(iterator.hasNext()){
                String key = iterator.next();
                String val = sortmap.get(key);
                
                if(StringUtils.isBlank(val) || key.equalsIgnoreCase("sign") || 
                        key.equalsIgnoreCase("api_key") || key.equalsIgnoreCase("messages")) continue;
                sb.append(key).append("=").append(val).append("&");
            }
            sb.append("key=").append(secret);
            //生成待签名串
            String signStr = sb.toString();
            logger.info("[JFU]极付支付生成待签名串:"+signStr);
            //进行MD5签名，并结果转换为大写
            String sign = MD5Utils.md5toUpCase_32Bit(signStr);
            logger.info("[JFU]极付支付生成加密签名串:"+sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[JFU]极付支付生成支付签名串异常:"+e.getMessage());
            throw new Exception("[JFU]极付支付生成支付签名串异常!");
        }
    }
}
