package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

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
 * @ClassName CFWPayServiceImpl
 * @Description 财富微支付
 * @author Hardy
 * @Date 2018年10月30日 下午3:55:37
 * @version 1.0.0
 */
public class CFWPayServiceImpl implements PayService{

  //日志
    private static final Logger logger = LoggerFactory.getLogger(CFWPayServiceImpl.class);
    
    private String shopId;//商户ID
    private String payUrl;//支付地址
    private String notifyUrl;//通知地址
    private String secret;//秘钥
    
    public CFWPayServiceImpl(Map<String,String> data) {
        if(data != null && !data.isEmpty()){
            if(data.containsKey("shopId")){
                this.shopId = data.get("shopId");
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
        }
    }

    /**
     * 网银支付
     */
    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        // TODO Auto-generated method stub
        return null;
    }
    
    /**
     * 扫码支付
     */
    @Override
    public JSONObject smPay(PayEntity payEntity) {
        logger.info("[CFW]财富微支付扫码支付开始======================START============================");
        try {
            //获取支付请求参数
            Map<String,String> data = sealRequest(payEntity);
            //验签字符串,MD5（shop_id + user_id + money + type +sign_key）；字符串相加再计算MD5一次，MD5为32位小写；shop_id 和sign_key登陆商家后台可以查看；
            String sign = generatorSign(data);
            data.put("sign",sign);
            //转换成json类型
            String reqParams = JSONObject.fromObject(data).toString();
            //发起HTTP请求
            String response = HttpUtils.toPostJson(reqParams, payUrl);
            if(StringUtils.isBlank(response)){
                logger.info("[CFW]财富微支付发起HTTP请求无响应结果,请联系第三方支付商资讯请求地址是否通畅!");
                return PayUtil.returnPayJson("error", "2", "下单失败:发起HTTP请求无响应结果,资讯第三方支付商!", "", 0, "", response);
            }
            logger.info("[CFW]财富微支付发起HTTP请求响应结果:"+response);
            //解析支付结果
            JSONObject jsonObject = JSONObject.fromObject(response);
            String qrcodeUrl = jsonObject.getString("qrcode_url");
//            if(StringUtils.isBlank(payEntity.getMobile())){
//                qrcodeUrl = jsonObject.getString("qrcode_url");
//            }else{
//                qrcodeUrl = jsonObject.getString("pay_url");
//            }
            if(StringUtils.isNotBlank(qrcodeUrl)){
                return PayUtil.returnPayJson("success","4","下单成功!",payEntity.getUsername(),payEntity.getAmount(),payEntity.getOrderNo(),qrcodeUrl);
            }
            return PayUtil.returnPayJson("error", "2", "下单失败",payEntity.getUsername(),payEntity.getAmount(),payEntity.getOrderNo(),response);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[CFW]财富微支付扫码支付异常!");
            return PayUtil.returnPayJson("error", "2", "下单失败!", "", 0, "", "");
        }
    }

    /**
     * 支付回调
     */
    @Override
    public String callback(Map<String, String> data) {
        logger.info("[CFW]财富微支付回调验签开始===================START===================");
        try {
            //获取验签原签名串
            //验签字符串,MD5（shop_id + user_id + order_no +sign_key+money+type）；字符串相加再计算MD5一次，32位小写；shop_id 和sign_key登陆商家后台可以查看；
            String sourceSign = data.get("sign");
            logger.info("[CFW]财富微支付验签原签名串:"+sourceSign);
            //生成验签签名
            StringBuffer sb = new StringBuffer();
            sb.append(shopId).append(data.get("user_id")).append(data.get("order_no"));
            sb.append(secret).append(data.get("money")).append(data.get("type"));
            String signStr = sb.toString();
            logger.info("[CFW]财富微支付生成回调验签待签名串:"+signStr);
            String sign = MD5Utils.md5toUpCase_32Bit(signStr).toLowerCase();
            logger.info("[CFW]财富微支付验签生成签名串:"+sign);
            if(sourceSign.equalsIgnoreCase(sign)) return "success";
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[CFW]财富微支付回调验签异常:"+e.getMessage());
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
        logger.info("[YIFA]易发支付组装支付请求参数开始=======================START====================");
        try {
            Map<String,String> data = new HashMap<>();
            String amount = new DecimalFormat("0.00").format(entity.getAmount());
            data.put("shop_id",shopId);//商家ID
            data.put("user_id",entity.getuId());//商家用户ID
            data.put("money",amount);//订单金额，单位元，如：0.01表示一分钱；
            data.put("type",entity.getPayCode());//微信：wechat，支付宝：alipay
            data.put("shop_no",entity.getOrderNo());//商家订单号，长度不超过40；
            data.put("notify_url",notifyUrl);//订单支付成功回调地址（具体参数详见接口2，如果为空，平台会调用商家在WEB端设置的订单回调地址；否则，平台会调用该地址，WEB端设置的地址不会被调用）；
            data.put("return_url",entity.getRefererUrl());//二维码扫码支付模式下：支付成功页面‘返回商家端’按钮点击后的跳转地址；如果商家采用自有界面，则可忽略该参数；
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[CFW]财富微支付组装支付请求参数异常:"+e.getMessage());
            throw new Exception("组装支付请求参数异常!");
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
        logger.info("[CFW]财富微支付生成签名串开始===========================START=========================");
        try {
            StringBuffer sb = new StringBuffer();
            sb.append(shopId).append(data.get("user_id"));
            sb.append(data.get("money")).append(data.get("type")).append(secret);
            String signStr = sb.toString();
            logger.info("[CFW]财富微支付生成支付待签名串:"+signStr);
            String sign = MD5Utils.md5toUpCase_32Bit(signStr).toLowerCase();
            logger.info("[CFW]财富微支付生成支付签名串:"+sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[CFW]财富微支付生成签名串异常:"+e.getMessage());
            throw new Exception("生成签名串异常!");
        }
    }

}
