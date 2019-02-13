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
 * @ClassName UYZFPayServiceImpl
 * @Description 优银支付
 * @author Hardy
 * @Date 2019年1月1日 下午4:27:43
 * @version 1.0.0
 */
public class UYZFPayServiceImpl implements PayService {

    // 日志
    private static final Logger logger = LoggerFactory.getLogger(UYZFPayServiceImpl.class);

    private String merId;// 商户号

    private String payUrl;// 支付地址

    private String notifyUrl;// 回调通知地址

    private String md5Key;// 秘钥

    // 构造器,初始化参数
    public UYZFPayServiceImpl(Map<String,String> data) {
        if(MapUtils.isNotEntity(data)){
            if(data.containsKey("merId")){
                this.merId = data.get("merId");
            }
            if(data.containsKey("payUrl")){
                this.payUrl = data.get("payUrl");
            }
            if(data.containsKey("notifyUrl")){
                this.notifyUrl = data.get("notifyUrl");
            }
            if(data.containsKey("md5Key")){
                this.md5Key = data.get("md5Key");
            }
        }
     }

    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        logger.info("[UYZF]优银支付网银支付开始===============START============");
        try {
            //获取支付请求参数
            Map<String,String> data = sealRequest(payEntity, 0);
            //生成签名串
            String sign = generatorSign(data, 1);
            data.put("sign", sign);
            logger.info("[UYZF]优银支付网银支付请求参数报文:{}",JSONObject.fromObject(data).toString());
            //生成form表单请求
            String formStr = HttpUtils.generatorForm(data, payUrl);
            logger.info("[UYZF]优银支付网银支付生成form表单请求结果:{}",formStr);
            return PayResponse.wy_form(payEntity.getPayUrl(),formStr);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[UYZF]优银支付网银支付异常{}",e.getMessage());
            return PayResponse.wy_write("[UYZF]优银支付网银支付异常");
        }
    }

    @Override
    public JSONObject smPay(PayEntity payEntity) {
        logger.info("[UYZF]优银支付扫码支付开始===============START============");
        try {
            //获取支付请求参数
            Map<String,String> data = sealRequest(payEntity, 0);
            //生成签名串
            String sign = generatorSign(data, 1);
            data.put("sign", sign);
            logger.info("[UYZF]优银支付扫码支付请求参数报文:{}",JSONObject.fromObject(data).toString());
            //生成form表单请求
            String formStr = HttpUtils.generatorForm(data, payUrl);
            logger.info("[UYZF]优银支付扫码支付生成form表单请求结果:{}",formStr);
            return PayResponse.sm_form(payEntity, formStr, "下单成功");
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[UYZF]优银支付扫码支付异常{}",e.getMessage());
            return PayResponse.error("[UYZF]优银支付扫码支付异常");
        }
    }

    @Override
    public String callback(Map<String, String> data) {
        logger.info("[UYZF]优银支付回调验签开始==============START=============");
        try {
            String sourceSign = data.get("sign");
            logger.info("[UYZF]优银支付回调验签获取原签名串:{}",sourceSign);
            String sign = generatorSign(data, 0);
            logger.info("[UYZF]优银支付回调验签生成签名串:{}",sign);
            if(sourceSign.equalsIgnoreCase(sign)) return "success";
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[UYZF]优银支付回调验签异常:{}",e.getMessage());
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
    private Map<String,String> sealRequest(PayEntity entity,int type) throws Exception{
        logger.info("[UYZF]优银支付组装支付请求参数开始================START================");
        try {
            Map<String,String> data = new HashMap<>();
            
            String amount = new DecimalFormat("0.00").format(entity.getAmount());
            
            data.put("version","1.0");//版本号    默认1.0
            data.put("customerid",merId);//商户编号    商户后台获取
            data.put("sdorderno",entity.getOrderNo());//商户订单号,20位
            data.put("total_fee",amount);//订单金额 精确到小数点后两位，例如10.24
            if(type == 1){
                //网关支付
                data.put("paytype","bank");//支付编号   详见附录1
                data.put("bankcode",entity.getPayCode());//支付编号  网关必填详见附录2
            }else{
                data.put("paytype",entity.getPayCode());//支付编号   详见附录1
            }
            data.put("notifyurl",notifyUrl);//异步通知URL  不能带有任何参数
            data.put("returnurl",entity.getRefererUrl());//同步跳转URL  不能带有任何参数
            data.put("sendip",entity.getIp());//终端IP    客户发起支付终端IP
            data.put("remark","TOP-UP");//订单备注说明  可为空
            data.put("sign","");//md5签名串    参照md5签名说明
//            data.put("get_code","");//二维码URL    获取URL地址传：1（仅扫码支付有效）
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[UYZF]优银支付组装支付请求参数异常:{}",e.getMessage());
            throw new Exception("[UYZF]优银支付组装支付请求参数异常");
        }
    }
    
    /**
     * 
     * @Description 生成签名串
     * @param data
     * @return
     * @throws Exception
     */
    private String generatorSign(Map<String,String> data,int type) throws Exception{
        logger.info("[UYZF]优银支付生成签名串开始==================START===================");
        try {
            StringBuffer sb = new StringBuffer();
            if(type == 1){
                //支付签名规则:version={value}&customerid={value}&total_fee={value}&sdorderno={value}&notifyurl={value}&returnurl={value}&{apikey}
                sb.append("version=").append(data.get("version")).append("&");
                sb.append("customerid=").append(data.get("customerid")).append("&");
                sb.append("total_fee=").append(data.get("total_fee")).append("&");
                sb.append("sdorderno=").append(data.get("sdorderno")).append("&");
                sb.append("notifyurl=").append(data.get("notifyurl")).append("&");
                sb.append("returnurl=").append(data.get("returnurl")).append("&");
            }else{
                //回调验签规则:customerid={value}&status={value}&sdpayno={value}&sdorderno={value}&total_fee={value}&paytype={value}&{apikey}
                sb.append("customerid=").append(merId).append("&");
                sb.append("status=").append(data.get("status")).append("&");
                sb.append("sdpayno=").append(data.get("sdpayno")).append("&");
                sb.append("sdorderno=").append(data.get("sdorderno")).append("&");
                sb.append("total_fee=").append(data.get("total_fee")).append("&");
                sb.append("paytype=").append(data.get("paytype")).append("&");
            }
            
            String signStr = sb.append(md5Key).toString();
            logger.info("[UYZF]优银支付生成待加密签名串:{}",signStr);
            String sign = MD5Utils.md5toUpCase_32Bit(signStr).toLowerCase();
            logger.info("[UYZF]优银支付生成加密串:{}",sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[UYZF]优银支付生成签名串异常:{}",e.getMessage());
            throw new Exception("[UYZF]优银支付生成签名串异常");
        }
    }

}
