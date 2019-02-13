package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import com.cn.tianxia.pay.utils.MapUtils;
import com.cn.tianxia.pay.utils.RandomUtils;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName FYZFPayServiceImpl
 * @Description 飞鹰支付
 * @author Hardy
 * @Date 2019年1月23日 下午6:58:58
 * @version 1.0.0
 */
public class FYZFPayServiceImpl implements PayService {

    //日志
    private static final Logger logger = LoggerFactory.getLogger(FYZFPayServiceImpl.class);
    
    private String appid;//应用ID
    
    private String wyPayUrl;//网银支付地址
    
    private String kjPayUrl;//快捷支付地址

    private String scanPayUrl;//扫码支付地址
    
    private String h5PayUrl;//H5支付地址
    
    private String notifyUrl;//回调地址
    
    private String md5Key;//md5秘钥
    
    private String bankProductType;//网银产品类型
    
    private String bankAccountType;//支付银行卡类型 

    //构造器,初始化参数
    public FYZFPayServiceImpl(Map<String,String> data) {
        if(MapUtils.isNotEntity(data)){
            if(data.containsKey("appid")){
                this.appid = data.get("appid");
            }
            if(data.containsKey("wyPayUrl")){
                this.wyPayUrl = data.get("wyPayUrl");
            }
            if(data.containsKey("kjPayUrl")){
                this.kjPayUrl = data.get("kjPayUrl");
            }
            if(data.containsKey("scanPayUrl")){
                this.scanPayUrl = data.get("scanPayUrl");
            }
            if(data.containsKey("h5PayUrl")){
                this.h5PayUrl = data.get("h5PayUrl");
            }
            if(data.containsKey("notifyUrl")){
                this.notifyUrl = data.get("notifyUrl");
            }
            if(data.containsKey("md5Key")){
                this.md5Key = data.get("md5Key");
            }
            if(data.containsKey("bankProductType")){
                this.bankProductType = data.get("bankProductType");
            }
            if(data.containsKey("bankAccountType")){
                this.bankAccountType = data.get("bankAccountType");
            }
        }
    }

    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        logger.info("[FYZF]飞鹰支付网银支付开始==============start============");
        try {
            
            //获取支付请求参数
            Map<String,String> data = sealRequest(payEntity, 2);
            //生成签名串
            String sign = generatorSign(data);
            
            //put签名到请求参数集合中
            data.put("sign", sign);
            
            logger.info("[FYZF]飞鹰支付网银支付请求参数报文:{}",JSONObject.fromObject(data).toString());
            
            //发起form表单请求
            String formStr = HttpUtils.generatorForm(data, wyPayUrl);
            logger.info("[FYZF]飞鹰支付网银支付生成form表单结果:{}",formStr);
            return PayResponse.wy_form(payEntity.getPayUrl(), formStr);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[FYZF]飞鹰支付网银支付异常:{}",e.getMessage());
            return PayResponse.error("[FYZF]飞鹰支付网银支付异常");
        }
    }

    @Override
    public JSONObject smPay(PayEntity payEntity) {
        logger.info("[FYZF]飞鹰支付扫码支付开始==============start============");
        try {
            
            //获取支付请求参数
            Map<String,String> data = sealRequest(payEntity, 2);
            //生成签名串
            String sign = generatorSign(data);
            
            //put签名到请求参数集合中
            data.put("sign", sign);
            
            logger.info("[FYZF]飞鹰支付扫码支付请求参数报文:{}",JSONObject.fromObject(data).toString());
            
            String payUrl = "";
            
            if("GQ0001".equals(payEntity.getPayCode()) || "GQ0002".equals(payEntity.getPayCode())
                    || "GQ0003".equals(payEntity.getPayCode()) || "GQ0004".equals(payEntity.getPayCode())){
                payUrl = this.kjPayUrl;
                
                //form表单提交
                String formStr = HttpUtils.generatorForm(data, payUrl);
                logger.info("[FYZF]飞鹰支付快捷支付生成form表单结果:{}",formStr);
                return PayResponse.sm_form(payEntity, formStr, "[FYZF]飞鹰支付快捷支付下单成功");
            }else{
                if(StringUtils.isBlank(payEntity.getMobile())){
                    //PC端
                    payUrl = this.scanPayUrl;
                }else{
                    payUrl = this.h5PayUrl;
                }
            }
            //发起HTTP请求
            String response = HttpUtils.toPostForm(data, payUrl);
            if(StringUtils.isBlank(response)){
                logger.info("[FYZF]飞鹰支付扫码支付发起HTTP请求无响应结果");
                return PayResponse.error("[FYZF]飞鹰支付扫码支付发起HTTP请求无响应结果");
            }
            logger.info("[FYZF]飞鹰支付扫码支付发起HTTP请求响应结果:{}",response);
            //解析响应结果
            JSONObject jsonObject = JSONObject.fromObject(response);
            if(jsonObject.containsKey("resultCode") && "0000".equals(jsonObject.getString("resultCode"))){
                //发起下单请求成功
                String payMessage = jsonObject.getString("payMessage");
                if(StringUtils.isBlank(payEntity.getMobile())){
                    //PC端
                    return PayResponse.sm_qrcode(payEntity, payMessage, "扫码支付下单成功");
                }
                return PayResponse.sm_link(payEntity, payMessage, "H5支付下单成功");
            }
            return PayResponse.error("扫码支付下单失败:" + response);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[FYZF]飞鹰支付扫码支付异常:{}",e.getMessage());
            return PayResponse.error("[FYZF]飞鹰支付扫码支付异常");
        }
    }

    @Override
    public String callback(Map<String, String> data) {
        logger.info("[FYZF]飞鹰支付回调验签开始=============START=============");
        try {
            
            //获取原签名串
            String sourceSign = data.get("sign");
            logger.info("[FYZF]飞鹰支付回调验签获取原签名串:{}",sourceSign);
            String sign = generatorSign(data);
            logger.info("[FYZF]飞鹰支付回调验签生成加密签名串:{}",sign);
            if(sourceSign.equalsIgnoreCase(sign)) return "success";
            
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[FYZF]飞鹰支付回调验签开始异常:{}",e.getMessage());
        }
        return "faild";
    }
    
    
    /**
     * 
     * @Description 组装支付请求参数
     * @param entity
     * @param type 1 网银 2 扫码
     * @return
     * @throws Exception
     */
    private Map<String,String> sealRequest(PayEntity entity,int type) throws Exception{
        logger.info("[FYZF]飞鹰支付组装支付请求参数开始==============start============");
        try {
            Map<String,String> data = new HashMap<>();
            
            String amount = new DecimalFormat("0.00").format(entity.getAmount());
            String orderTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            data.put("appid",appid);//应用IDString(32)否
            data.put("orderAmount",amount);//订单金额，单位：元，保留小数点后两位String(12)否
            data.put("outTradeNo",entity.getOrderNo());//商户支付订单号String(30)否
            if(type == 1){
                data.put("pType",bankPayType(bankProductType));//产品类型（参照2.1.3产品类型编码）String(6)否
                data.put("bankCode",entity.getPayCode());//银行编码，详见3.1银行编码String(10)否
                data.put("bankAccountType",bankAccountType);//支付银行卡类型,PRIVATE_DEBIT_ACCOUNT（对私借记卡,PRIVATE_CREDIT_ACCOUNT（对私贷记卡String(10)否
            }else{
                data.put("pType",entity.getPayCode());//产品类型（参照2.1.3产品类型编码）String(6)否
            }
            data.put("orderTime",orderTime);//下单时间，格式yyyyMMddHHmmssString(14)否
//            data.put("storeInfo","");//商品描述String(200)是
            data.put("orderIp",entity.getIp());//下单IPString(15)否
            data.put("returnUrl",entity.getRefererUrl());//页面通知地址String(300)否
            data.put("notifyUrl",notifyUrl);//后台异步通知地址String(300)否
//            data.put("remark","TOP-UP");//备注String(200)是
            if(StringUtils.isNotBlank(entity.getMobile())){
                //移动端
                data.put("mobile","1");//移动端（当为手机端时此参数不为空值为1）String(13)是
            }
            data.put("randomStr",RandomUtils.generateString(8));//随机字符串，每次请求都是唯一字符串String(32)否
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[FYZF]飞鹰支付组装支付请求参数异常:{}",e.getMessage());
            throw new Exception("[FYZF]飞鹰支付组装支付请求参数异常");
        }
    }
    
    /**
     * 
     * @Description 生成签名串
     * @param data
     * @return
     * @throws Exception
     */
    public String generatorSign(Map<String,String> data) throws Exception{
        logger.info("[FYZF]飞鹰支付生成签名串开始==============start============");
        try {
            StringBuffer sb = new StringBuffer();
            //排序
            Map<String,String> map = MapUtils.sortByKeys(data);
            Iterator<String> iterator = map.keySet().iterator();
            while(iterator.hasNext()){
                String key = iterator.next();
                String val = map.get(key);
                if(StringUtils.isBlank(val) || "sign".equalsIgnoreCase(key)) continue;
                
                sb.append(key).append("=").append(val).append("&");
            }
            
            String signStr = sb.append("key=").append(md5Key).toString();
            logger.info("[FYZF]飞鹰支付生成待签名串:{}",signStr);
            
            String sign = MD5Utils.md5toUpCase_32Bit(signStr);
            logger.info("[FYZF]飞鹰支付生成加密签名串:{}",sign);
            
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[FYZF]飞鹰支付生成签名串异常:{}",e.getMessage());
            throw new Exception("[FYZF]飞鹰支付生成签名串异常");
        }
    }
    
    /**
     * 
     * @Description 获取网银支付通道
     * @param productType
     * @return
     */
    private String bankPayType(String productType){
        String payType = "";
        if(productType.equals("D0")){
            payType = "B2C001";
        }else if(productType.equals("Z1")){
            payType = "B2C002";
        }
        return payType;
    }
}
