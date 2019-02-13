package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
 * @ClassName XMQPPayServiceImpl
 * @Description 新免签支付
 * @author Hardy
 * @Date 2018年12月17日 下午9:59:59
 * @version 1.0.0
 */
public class XMQPPayServiceImpl implements PayService {
    
    //日志
    private static final Logger logger = LoggerFactory.getLogger(XMQPPayServiceImpl.class);
    
    private String secret;//秘钥 
    private String payUrl;//支付地址
    private String notifyUrl;//回调地址
    private String merno;//商户号
    
    //构造器,初始化参数
    public XMQPPayServiceImpl(Map<String,String> data) {
        if(MapUtils.isNotEntity(data)){
            if(data.containsKey("secret")){
                this.secret = data.get("secret");
            }
            if(data.containsKey("payUrl")){
                this.payUrl = data.get("payUrl");
            }
            if(data.containsKey("notifyUrl")){
                this.notifyUrl = data.get("notifyUrl");
            }
            if(data.containsKey("merno")){
                this.merno = data.get("merno");
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
        logger.info("[XMQ]新免签支付扫码支付开始=================START====================");
        try {
            //获取支付请求参数
            Map<String,String> data = sealRequest(payEntity);
            //进行签名
            String sign = generatorSign(data,1);
            data.put("sign",sign);//签名    将参数1至6按顺序连Token一起，做md5-32位加密，取字符串小写。网址类型的参数值不要urlencode（例：uid + price + paytype + notify_url + return_url + user_order_no + token）
            logger.info("[XMQ]新免签支付扫码支付请求参数报文:{}",JSONObject.fromObject(data).toString());
            //发起支付请求
            String formStr = HttpUtils.generatorForm(data, payUrl);
            logger.info("[XMQ]新免签支付扫码支付生成form表单请求报文:{}",formStr);
            return PayResponse.sm_form(payEntity, formStr, "下单成功");
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[XMQ]新免签支付扫码支付异常:{}",e.getMessage());
            return PayResponse.error("[XMQ]新免签支付扫码支付异常");
        }
    }

    @Override
    public String callback(Map<String, String> data) {
        logger.info("[XMQ]新免签支付回调验签开始=================START===================");
        try {
            //获取原签名串
            String sourceSign = data.get("sign");
            logger.info("[XMQ]新免签支付获取原签名串:{}",sourceSign);
            //进行签名
            String sign = generatorSign(data,0);
            logger.info("[XMQ]新免签支付回调生成签名串:{}",sign);
            if(sourceSign.equalsIgnoreCase(sign)) return "success";
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[XMQ]新免签支付回调验签异常:{}",e.getMessage());
        }
        return "faild";
    }
    
    /**
     * 
     * @Description 封装支付请求参数
     * @param entity
     * @return
     * @throws Exception
     */
    private Map<String,String> sealRequest(PayEntity entity) throws Exception{
        logger.info("[XMQ]新免签支付组装支付请求参数开始===================START================");
        try {
            Map<String,String> data = new HashMap<>();
            String amount = new DecimalFormat("0.00").format(entity.getAmount());
            data.put("uid",merno);//商户ID   您的商户唯一标识，注册后在基本资料里获得
            data.put("price",amount);//金额   单位：元。精确小数点后2位
            data.put("paytype",entity.getPayCode());//支付渠道   1：支付宝；2：微信支付
            data.put("notify_url",notifyUrl);//异步回调地址  用户支付成功后，我们服务器会主动发送一个post消息到这个网址。由您自定义。不要urlencode并且不带任何参数。例：http://www.xxx.com/notify_url
            data.put("return_url",entity.getRefererUrl());//同步跳转地址  用户支付成功后，我们会让用户浏览器自动跳转到这个网址。由您自定义。不要urlencode并且不带任何参数。例：http://www.xxx.com/return_url
            data.put("user_order_no",entity.getOrderNo());//商户自定义订单号 我们会据此判别是同一笔订单还是新订单。我们回调时，会带上这个参数。例：201010101041
            data.put("note","TOP-UP");//附加内容回调时将会根据传入内容原样返回（为防止乱码情况，请尽量不填写中文）
            data.put("cuid",entity.getuId());//商户自定义用户唯一标识   我们会显示在您后台的订单列表中，方便您看到是哪个用户的付款，方便后台对账。强烈建议填写。可以填用户名、邮箱、主键
            data.put("tm",new SimpleDateFormat("yyyy-mm-dd hh:mi:ss").format(new Date()));//日期时间请求时间yyyy-mm-dd hh:mi:ss
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[XMQ]新免签支付组装支付请求参数异常:{}",e.getMessage());
            throw new Exception("[XMQ]新免签支付组装支付请求参数异常");
        }
    }
    
    /**
     * 
     * @Description 生成签名串
     * @param data
     * @param type 1 支付 其他 回调
     * @return
     * @throws Exception
     */
    private String generatorSign(Map<String,String> data,int type) throws Exception{
        logger.info("[XMQ]新免签支付生成签名串开始=====================START=====================");
        try {
            //支付签名规则:将参数1至6按顺序连Token一起，做md5-32位加密，取字符串小写。
            //网址类型的参数值不要urlencode（例：uid + price + paytype + notify_url + return_url + user_order_no + token）
            StringBuffer sb = new StringBuffer();
            if(type == 1){
                sb.append(data.get("uid")).append(data.get("price")).append(data.get("paytype"));
                sb.append(data.get("notify_url")).append(data.get("return_url"));
                sb.append(data.get("user_order_no")).append(secret);
            }else{
                //回调签名规则:将参数1至5按顺序连Token一起，做md5-32位加密，取字符串小写。您需要在您的服务端按照同样的算法，自己验证此sign是否正确。
                //只在正确时，执行您自己逻辑中支付成功代码。（拼接顺序：user_order_no + orderno + tradeno + price + realprice + token）
                sb.append(data.get("user_order_no")).append(data.get("orderno")).append(data.get("tradeno"));
                sb.append(data.get("price")).append(data.get("realprice")).append(secret);
            }
            String signStr = sb.toString();
            logger.info("[XMQ]新免签支付生成待签名串:{}",signStr);
            String sign = MD5Utils.md5toUpCase_32Bit(signStr).toLowerCase();
            logger.info("[XMQ]新免签支付生成加密串:{}",sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[XMQ]新免签支付生成签名串异常:{}",e.getMessage());
            throw new Exception("[XMQ]新免签支付生成签名串异常");
        }
    }

}
