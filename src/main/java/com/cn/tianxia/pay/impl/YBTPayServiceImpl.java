package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import com.cn.tianxia.pay.utils.RandomUtils;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName YBTPayServiceImpl
 * @Description 中富通支付
 * @author Hardy
 * @Date 2018年10月27日 上午9:14:44
 * @version 1.0.0
 */
public class YBTPayServiceImpl implements PayService{
    
    //日志
    private static final Logger logger = LoggerFactory.getLogger(YBTPayServiceImpl.class);
    
    private String merchantNum;//商户号
    
    private String merMark;//商户标识符
    
    private String payUrl;//支付请求地址
    
    private String notifyUrl;//回调请求地址
    
    private String secret;//签名秘钥

    //构造器，初始化参数
    public YBTPayServiceImpl(Map<String,String> data) {
        if(data != null && !data.isEmpty()){
            if(data.containsKey("merchantNum")){
                this.merchantNum = data.get("merchantNum");
            }
            if(data.containsKey("merMark")){
                this.merMark = data.get("merMark");
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
        logger.info("[YBT]中富通支付网银支付开始======================START========================");
        try {
            //获取支付请求参数
            Map<String,String> data = sealRequest(payEntity, 1);
            //生成签名串
            String sign = generatorSign(data,1);
            data.put("sign", sign);
            logger.info("[YBT]中富通支付网银支付请求报文:{}",JSONObject.fromObject(data).toString());
            String formStr = HttpUtils.generatorForm(data, payUrl);
            logger.info("[YBT]中富通支付组装form表单结果:{}",formStr);
            return PayUtil.returnWYPayJson("success","form",formStr,payEntity.getPayUrl(),"pay");
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[YBT]中富通支付网银支付异常:{}",e.getMessage());
            return PayUtil.returnWYPayJson("error","form","",payEntity.getPayUrl(),"pay");
        }
    }

    /**
     * 扫码支付
     */
    @Override
    public JSONObject smPay(PayEntity payEntity) {
        logger.info("[YBT]中富通支付扫码支付开始======================START========================");
        try {
            //获取支付请求参数
            Map<String,String> data = sealRequest(payEntity, 0);
            //生成签名串
            String sign = generatorSign(data,1);
            data.put("sign", sign);
            logger.info("[YBT]中富通支付扫码支付请求报文:{}",JSONObject.fromObject(data).toString());
            if(payEntity.getPayCode().equalsIgnoreCase("kuaijie") || StringUtils.isNotBlank(payEntity.getMobile())){
                //快捷支付
                String formStr = HttpUtils.generatorForm(data, payUrl);
                logger.info("[YBT]中富通支付组装form表单结果:{}",formStr);
                return PayUtil.returnPayJson("success","1","下单成功",payEntity.getUsername(),payEntity.getAmount(),payEntity.getOrderNo(),formStr);
            }
            
            //发起支付,采用form表单的形式提交
            String response = HttpUtils.toPostForm(data, payUrl);
            logger.info("[YBT]中富通支付扫码支付请求报文:{},[YBT]中富通支付扫码支付请求地址:{}",JSONObject.fromObject(data).toString(),payUrl);
            if(StringUtils.isBlank(response)){
                logger.info("[YBT]中富通支付发起HTTP请求无响应结果!");
                return PayUtil.returnPayJson("error","","[YBT]扫码支付失败,发起HTTP请求无响应结果!","",0,"",response);
            }
            logger.info("[YBT]中富通支付扫码支付发起HTTP请求响应结果:{}",response);
            //解析响应结果
            JSONObject jsonObject = JSONObject.fromObject(response);
            if(jsonObject.containsKey("orderStatus") && jsonObject.getString("orderStatus").equalsIgnoreCase("SUCCESS")){
                //订单请求状态，“SUCCESS”为请求成功，其他失败
                String qyCode = jsonObject.getString("qyCode");
                return PayUtil.returnPayJson("success","2","下单成功",payEntity.getUsername(),payEntity.getAmount(),payEntity.getOrderNo(),qyCode);
            }
            String resp_ErrMsg = jsonObject.getString("resp_ErrMsg");
            return PayUtil.returnPayJson("error","2","[YBT]中富通支付扫码支付失败:"+resp_ErrMsg,"",0,"",response);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[YBT]中富通支付扫码支付异常:{}",e.getMessage());
            return PayUtil.returnPayJson("error", "2", "[YBT]中富通支付扫码支付失败!","",0,"","[YBT]支付异常:"+e.getMessage());
        }
    }

    /**
     * 回调
     */
    @Override
    public String callback(Map<String, String> data) {
        logger.info("[YBT]中富通支付回调验签开始========================START======================");
        try {
            //获取回调原签名串
            String sourceSign = data.get("sign");
            //生成验签签名串
            String sign = generatorSign(data, 0);
            logger.info("[YBT]中富通支付回调验签原签名串:{},[YBT]中富通支付回调验签生成加密签名串:{}",sourceSign,sign);
            if(sourceSign.equalsIgnoreCase(sign)) return "success";
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[YBT]中富通支付回调验签异常:{}",e.getMessage());
        }
        return "faild";
    }
    
    
    /**
     * 
     * @Description 组装支付请求参数
     * @param entity
     * @param type 1 网银支付 0 扫码支付
     * @return
     * @throws Exception
     */
    private Map<String,String> sealRequest(PayEntity entity,Integer type) throws Exception{
        logger.info("[YBT]中富通支付组装支付请求参数开始=====================START======================");
        try {
            //创建支付请求报文存储对象
            Map<String,String> data = new HashMap<>();
            
            //订单金额
            String amount = new DecimalFormat("##").format(entity.getAmount()*100);//单位为分
            //订单时间
            String orderTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            //订单号,订单号必须加上商户标识
            String orderNo = merMark + entity.getOrderNo();
            entity.setOrderNo(orderNo);
            
            data.put("version","V1.0");//当前接口版本 V1.0
            data.put("merchantNum",merchantNum);//分配给商家的商户号
            data.put("nonce_str",RandomUtils.generateString(8));//随机字符串
            data.put("merMark",merMark);//分配给商家的商户标识
            data.put("client_ip",entity.getIp());//客户端ip，如127.0.0.1
            data.put("orderTime",orderTime);//订单时间（格式: yyyy-MM-dd HH:mm:ss）
            //支付类型（QQCode:QQ扫码，QQH5：QQH5，wechatCode：微信扫码 wechatH5：微信H5，aliCode：支付宝扫码，aliH5：支付宝H5，B2C：网银支付，kuaijie：快捷支付）
            if(type == 1){
                //网银支付
                data.put("payType","B2C");//支付类型
                data.put("bank_code",entity.getPayCode());//payType为B2C时,参照附录中的银行代码对照表
            }else{
                data.put("payType",entity.getPayCode());//支付类型
            }
            data.put("orderNum",entity.getOrderNo());//商户订单号（此订单号必须在自定义订单号前拼接商户标识，示例：ABC10000,ABC为商户标识）
            data.put("amount",amount);//订单金额，单位（分）
            data.put("body","TOP-UP");//订单描述
            data.put("signType","MD5");//签名类型（MD5）不参与签名
            data.put("notifyUrl",notifyUrl);//后台通知地址，如不填则不发送通知
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[YBT]中富通支付组装支付请求参数异常:"+e.getMessage());
            throw new Exception("[YBT]中富通支付组装支付请求参数异常");
        }
    }
    
    /**
     * 
     * @Description 生成签名秘钥
     * @param data
     * @param type 1 支付  0回调
     * @return
     * @throws Exception
     */
    private String generatorSign(Map<String,String> data,Integer type) throws Exception{
        logger.info("[YBT]中富通支付生成支付签名串开始=========================START=========================");
        try {
            StringBuffer sb = new StringBuffer();
            if(type == 1){
                //生成支付签名串
                //支付签名规则:参数签名顺序（必须按照此顺序组织签名）说明及示例
                //sign=md5（version=版本号&merchantNum=商户号&nonce_str=随机字符串&merMark=商户标识&client_ip=客户端IP
                //&payType=支付类型&orderNum=交易订单号&amount=交易金额&body=订单描述&key=商户密钥)
                sb.append("version=").append(data.get("version")).append("&");
                sb.append("merchantNum=").append(merchantNum).append("&");
                sb.append("nonce_str=").append(data.get("nonce_str")).append("&");
                sb.append("merMark=").append(merMark).append("&");
                sb.append("client_ip=").append(data.get("client_ip")).append("&");
                sb.append("payType=").append(data.get("payType")).append("&");
                sb.append("orderNum=").append(data.get("orderNum")).append("&");
                sb.append("amount=").append(data.get("amount")).append("&");
                sb.append("body=").append(data.get("body")).append("&");
            }else{
                //生成回调签名串
                //sign=md5（merchantNum=商户号&orderNum=商户订单号&amount=交易金额&nonce_str=随机字符串&orderStatus=订单状态&key=商户密钥)
                sb.append("merchantNum=").append(merchantNum).append("&");
                sb.append("orderNum=").append(data.get("orderNum")).append("&");
                sb.append("amount=").append(data.get("amount")).append("&");
                sb.append("nonce_str=").append(data.get("nonce_str")).append("&");
                sb.append("orderStatus=").append(data.get("orderStatus")).append("&");
            }
            sb.append("key=").append(secret);
            //生成待签名串
            String signStr = sb.toString();
            //签名
            String sign = MD5Utils.md5toUpCase_32Bit(signStr);
            logger.info("[YBT]中富通支付生成待签名串:{},[YBT]中富通支付生成加密签名串:{}",signStr,sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[YBT]中富通支付生成支付签名串异常:"+e.getMessage());
            throw new Exception("[YBT]中富通支付生成支付签名串异常!");
        }
    }
}
