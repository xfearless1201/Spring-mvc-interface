package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
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
 * @ClassName TDZFPayServiceImpl
 * @Description 天盾支付
 * @author Hardy
 * @Date 2018年12月17日 下午9:14:34
 * @version 1.0.0
 */
public class TDZFPayServiceImpl implements PayService{

    //日志
    private static final Logger logger = LoggerFactory.getLogger(TDZFPayServiceImpl.class);
    
    private String secret;//秘钥 
    private String payUrl;//支付地址
    private String notifyUrl;//回调地址
    private String merno;//商户号
    
    //构造器,初始化参数
    public TDZFPayServiceImpl(Map<String,String> data) {
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

    /**
     * 网银支付
     */
    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        logger.info("[TDZF]天盾支付网银支付开始=================START==================");
        try {
            //获取扫码支付请求参数
            Map<String,String> data = sealRequest(payEntity, 0);
            //生成签名串
            String sign = generatorSign(data,1);
            data.put("sign", sign);
            logger.info("[TDZF]天盾支付网银支付请求参数报文:{}",JSONObject.fromObject(data).toString());
            //发起网银支付
            String formStr = HttpUtils.generatorForm(data, payUrl);
            logger.info("[TDZF]天盾支付网银支付生成form表单请求结果:{}",formStr);
            return PayResponse.wy_form(payEntity.getPayUrl(), formStr);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[TDZF]天盾支付网银支付异常:{}",e.getMessage());
            return PayResponse.error("[TDZF]天盾支付网银支付异常");
        }
    }

    /**
     * 扫码支付
     */
    @Override
    public JSONObject smPay(PayEntity payEntity) {
        logger.info("[TDZF]天盾支付扫码支付开始=================START==================");
        try {
            //获取扫码支付请求参数
            Map<String,String> data = sealRequest(payEntity, 0);
            //生成签名串
            String sign = generatorSign(data,1);
            data.put("sign", sign);
            logger.info("[TDZF]天盾支付扫码支付请求参数报文:{}",JSONObject.fromObject(data).toString());
            String formStr = HttpUtils.generatorForm(data, payUrl);
            logger.info("[TDZF]天盾支付扫码支付生成form表单请求结果:{}",formStr);
            return PayResponse.sm_form(payEntity, formStr, "下单成功");
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[TDZF]天盾支付扫码支付异常:{}",e.getMessage());
            return PayResponse.error("[TDZF]天盾支付扫码支付异常");
        }
    }

    /**
     * 回调
     */
    @Override
    public String callback(Map<String, String> data) {
        logger.info("[TDZF]天盾支付回调验签开始==================START==================");
        try {
            //获取原签名串
            String sourceSign = data.get("sign");
            logger.info("[TDZF]天盾支付回调验签原签名串:{}",sourceSign);
            String sign = generatorSign(data,0);
            logger.info("[TDZF]天盾支付回调验签生成签名串:{}",sign);
            if(sourceSign.equalsIgnoreCase(sign)) return "success";
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[TDZF]天盾支付回调验签异常:{}",e.getMessage());
        }
        return "faild";
    }

    
    /**
     * 
     * @Description 组装支付请求参数
     * @param entity
     * @param type 1 网银支付 0扫码支付
     * @return
     * @throws Exception
     */
    private Map<String,String> sealRequest(PayEntity entity,int type) throws Exception{
        logger.info("[TDZF]天盾支付组装支付请求参数开始=====================START==================");
        try {
            Map<String,String> data = new HashMap<>();
            
            String amount = new DecimalFormat("0.00").format(entity.getAmount());
            
            data.put("version","1.0");//版本号        默认1.0
            data.put("customerid",merno);//商户编号        商户后台获取
            data.put("sdorderno",entity.getOrderNo());//商户订单号        
            data.put("total_fee",amount);//订单金额     精确到小数点后两位，例如10.24
            if(type == 1){
                data.put("paytype","bank");//支付编号       详见附录1
                data.put("bankcode",entity.getPayCode());//银行编号  网银直连不可为空，其他支付方式可为空  详见附录2
            }else{
                data.put("paytype",entity.getPayCode());//支付编号       详见附录1
            }
            data.put("notifyurl",notifyUrl);//异步通知URL      不能带有任何参数
            data.put("returnurl",entity.getRefererUrl());//同步跳转URL      不能带有任何参数
            data.put("remark","TOP-UP");//订单备注说明  Y   可为空
//            data.put("get_code","");//获取微信二维码   Y   如果只想获取被扫二维码，请设置get_code=1
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[TDZF]天盾支付组装支付请求参数异常:{}",e.getMessage());
            throw new Exception("[TDZF]天盾支付组装支付请求参数");
        }
    }
    
    
    /**
     * 
     * @Description 生成签名串
     * @param data
     * @param type 1 支付签名 其他 回调验签
     * @return
     * @throws Exception
     */
    private String generatorSign(Map<String,String> data,int type) throws Exception{
        logger.info("[TDZF]天盾支付生成签名串开始===================START=================");
        try {
            StringBuffer sb = new StringBuffer();
            if(type == 1){
                //支付签名规则:
                //version={value}&customerid={value}&total_fee={value}&sdorderno={value}
                //&notifyurl={value}&returnurl={value}&{apikey}
                sb.append("version=").append(data.get("version")).append("&");
                sb.append("customerid=").append(merno).append("&");
                sb.append("total_fee=").append(data.get("total_fee")).append("&");
                sb.append("sdorderno=").append(data.get("sdorderno")).append("&");
                sb.append("notifyurl=").append(notifyUrl).append("&");
                sb.append("returnurl=").append(data.get("returnurl")).append("&");
            }else{
                //回调签名规则:{value}要替换成接收到的值，{apikey}要替换成平台分配的接入密钥，可在商户后台获取
                //customerid={value}&status={value}&sdpayno={value}&sdorderno={value}&
                //total_fee={value}&paytype={value}&{apikey}
                //使用md5签名上面拼接的字符串即可生成小写的32位密文
                sb.append("customerid=").append(data.get("customerid")).append("&");
                sb.append("status=").append(data.get("status")).append("&");
                sb.append("sdpayno=").append(data.get("sdpayno")).append("&");
                sb.append("sdorderno=").append(data.get("sdorderno")).append("&");
                sb.append("total_fee=").append(data.get("total_fee")).append("&");
                sb.append("paytype=").append(data.get("paytype")).append("&");
            }
            sb.append(secret);
            String signStr = sb.toString();
            logger.info("[TDZF]天盾支付生成待签名串:{}",signStr);
            String sign = MD5Utils.md5toUpCase_32Bit(signStr).toLowerCase();
            logger.info("[TDZF]天盾支付生成加密前串:{}",sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[TDZF]天盾支付生成签名串异常:{}",e.getMessage());
            throw new Exception("[TDZF]天盾支付生成签名串异常");
        }
    }
}
