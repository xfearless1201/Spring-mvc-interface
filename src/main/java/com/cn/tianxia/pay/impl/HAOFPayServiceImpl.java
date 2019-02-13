package com.cn.tianxia.pay.impl;

import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import com.cn.tianxia.pay.utils.MapUtils;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName HAOFPayServiceImpl
 * @Description 好富支付
 * @author Hardy
 * @Date 2019年1月9日 下午5:50:21
 * @version 1.0.0
 */
public class HAOFPayServiceImpl implements PayService {

    //日志
    private static final Logger logger = LoggerFactory.getLogger(HAOFPayServiceImpl.class);
    
    private String merid;//商户号
    
    private String wyPayUrl;//网银支付请求地址
    
    private String smPayUrl;//扫码支付请求地址
    
    private String notifyUrl;//回调地址
    
    private String md5Key;//签名秘钥

    //构造器,出事参数
    public HAOFPayServiceImpl(Map<String,String> data) {
        if(MapUtils.isNotEntity(data)){
            this.merid = StringUtils.isNotBlank(data.get("merid"))?data.get("merid"):null;
            this.wyPayUrl = StringUtils.isNotBlank(data.get("wyPayUrl"))?data.get("wyPayUrl"):null;
            this.smPayUrl = StringUtils.isNotBlank(data.get("smPayUrl"))?data.get("smPayUrl"):null;
            this.notifyUrl = StringUtils.isNotBlank(data.get("notifyUrl"))?data.get("notifyUrl"):null;
            this.md5Key = StringUtils.isNotBlank(data.get("md5Key"))?data.get("md5Key"):null;
        }
    }

    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JSONObject smPay(PayEntity payEntity) {
        logger.info("[HAOF]好富支付扫码支付开始===============START================");
        try {
            //获取支付请求参数
            Map<String,String> data = sealRequest(payEntity, 0);
            String jsonData = JSONObject.fromObject(data).toString();
            //生成签名串
            String sign = generatorSign(data);
            logger.info("[HAOF]好富支付扫码支付请求参数明文报文:{}",jsonData);
            //请求参数进行base64编码
            String reqParams = new String(Base64.encodeBase64(jsonData.toString().getBytes()));
            logger.info("[HAOF]好富支付扫码支付Base64编码后的请求参数:{}",reqParams);
            Map<String,String> reqMap = new HashMap<>();
            reqMap.put("req", reqParams);
            reqMap.put("sign", sign);
            
            logger.info("[HAOF]好富支付扫码支付请求参数报文:{}",JSONObject.fromObject(reqMap).toString());
            if(StringUtils.isBlank(payEntity.getMobile())){
                //PC端，扫码POST请求
                String response = HttpUtils.toPostForm(reqMap,smPayUrl);
                if(StringUtils.isBlank(response)){
                    logger.info("[HAOF]好富支付扫码支付发起HTTP请求无响应结果,请求参数:{},请求地址:{}",reqMap.toString(),smPayUrl);
                    return PayResponse.error("[HAOF]好富支付扫码支付发起HTTP请求无响应结果");
                }
                //解析响应结果
                JSONObject jsonObject = JSONObject.fromObject(response);
                String resp = new String(Base64.decodeBase64(jsonObject.getString("resp")));
                logger.info("[HAOF]好富支付扫码支付发起HTTP请求响应结果:{}",response);
                jsonObject = JSONObject.fromObject(resp);
                if(jsonObject.containsKey("respcode") && "00".equals(jsonObject.getString("respcode"))){
                    //发起支付成功
                    String formaction = jsonObject.getString("formaction");
                    return PayResponse.sm_link(payEntity, formaction, "下单成功");
                }
                return PayResponse.error("下单失败:"+resp);
            }else {
                //直接跳转
                String params = wyPayUrl+"?req=" + reqParams + "&sign=" + sign;//请求参数
                return PayResponse.sm_link(payEntity, params, "下单成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[HAOF]好富支付扫码支付异常:{}",e.getMessage());
            return PayResponse.error("[HAOF]好富支付扫码支付异常");
        }
    }

    @Override
    public String callback(Map<String, String> data) {
       return null;
    }
    
    
    
    /**
     * 
     * @Description 封装支付请求参数
     * @param entity
     * @param type
     * @return
     * @throws Exception
     */
    private Map<String,String> sealRequest(PayEntity entity,int type) throws Exception{
        logger.info("[HAOF]好富支付组装支付请求参数开始================START==============");
        try {
            Map<String,String> data = new HashMap<>();
            String amount = new DecimalFormat("##").format(entity.getAmount() * 100);//订单金额，单位为分
            if(type == 1){
                //网银支付
                data.put("action","Bank");//请求方式    微 信 ：WxCode/WxSao/WxJsApi/WxH5 支付宝：AliCode/AliSao/AliWap
            }else{
                data.put("action",entity.getPayCode());//请求方式    微 信 ：WxCode/WxSao/WxJsApi/WxH5 支付宝：AliCode/AliSao/AliWap
            }
            data.put("txnamt",amount);//交易金额    订单金额，单位为分
            data.put("merid",merid);//商户号  商户号，接入手机支付平台时分配
            data.put("orderid",entity.getOrderNo());//商户订单号  由商户生成，必需唯一，长度 8-32 位，由字母和数字组成
//            data.put("code","");//交易二维码 反扫时的码，App 扫描微信/支付宝生成的码
//            data.put("openid","");//openid  微信公众号支付时的 openid
            data.put("ip",entity.getIp());//用户 IP   WxH5 时需上送，用户真实 IP，错误将无法交易
            data.put("backurl",notifyUrl);//通知 URL 商户系统的地址，支付结束后，通过该 url 通知商户交易结果，POST 返回参数参考 3.4
            data.put("fronturl",entity.getRefererUrl());//前台 URL    仅 WxJsApi 支持，丌填时使用默讣页面，GET 返回
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[HAOF]好富支付组装支付请求参数异常:{}",e.getMessage());
            throw new Exception("[HAOF]好富支付组装支付请求参数异常");
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
        logger.info("[HAOF]好富生成签名串开始==================START==================");
        try {
            JSONObject jsonObject = JSONObject.fromObject(data);
            if(jsonObject.containsKey("ip")) jsonObject.remove("ip");
            logger.info("[HAOF]好富支付生成待签名串明文:{}",jsonObject.toString());
            byte[] signByte = Base64.encodeBase64(jsonObject.toString().getBytes());
            String signStr = new String(signByte)+md5Key;
            logger.info("[HAOF]好富支付生成待签名串:{}",signStr);
            String sign = MD5Utils.md5toUpCase_32Bit(signStr).toLowerCase();
            logger.info("[HAOF]好富支付生成加密签名串:{}",sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[HAOF]好富生成签名串异常:{}",e.getMessage());
            throw new Exception("[HAOF]好富生成签名串异常");
        }
    }

}
