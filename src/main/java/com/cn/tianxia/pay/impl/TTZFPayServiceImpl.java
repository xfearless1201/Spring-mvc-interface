package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
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

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName TTZFPayServiceImpl
 * @Description (废弃)
 * @author Hardy
 * @Date 2018年12月29日 下午5:20:48
 * @version 1.0.0
 */
public class TTZFPayServiceImpl implements PayService {

    //日志
    private static final Logger logger = LoggerFactory.getLogger(TTZFPayServiceImpl.class);
    
    private String pid;//商户号
    
    private String payUrl;//支付地址
    
    private String notifyUrl;//回调地址
    
    private String secret;//秘钥
    
    private String settleCycle;//结算周期
    
    //构造器,初始化参数
    public TTZFPayServiceImpl(Map<String,String> data) {
        if(MapUtils.isNotEntity(data)){
            if(data.containsKey("pid")){
                this.pid = data.get("pid");
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
            if(data.containsKey("settleCycle")){
                this.settleCycle = data.get("settleCycle");
            }
        }
    }

    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        logger.info("[TTZF]通支付2网银支付开始===============start=============");
        try {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[TTZF]通支付2网银支付异常:{}",e.getMessage());
            return PayResponse.wy_write("[TTZF]通支付2网银支付");
        }
    }

    @Override
    public JSONObject smPay(PayEntity payEntity) {
        logger.info("[TTZF]通支付2扫码支付开始===============start=============");
        try {
            
            //获取支付请求参数
            Map<String,String> data = sealRequest(payEntity);
            //生成签名串
            String sign = generatorSign(data);
            //put签名串到请求参数集合中
            data.put("sign", sign);
            logger.info("[TTZF]通支付2扫码支付请求参数报文:{}",JSONObject.fromObject(data).toString());
            
            //发起HTTP请求
            String response = HttpUtils.toPostJsonStr(JSONObject.fromObject(data), payUrl);
            
            if(StringUtils.isBlank(response)){
                logger.info("[TTZF]通支付2扫码支付发起HTTP请求无响应结果");
                return PayResponse.error("[TTZF]通支付2扫码支付发起HTTP请求无响应结果");
            }
            
            logger.info("[TTZF]通支付2扫码支付发起HTTP请求响应结果:{}",response);
            
            //解析响应结果
            JSONObject jsonObject = JSONObject.fromObject(response);
            if(jsonObject.containsKey("resultCode") && "0".equals(jsonObject.getString("resultCode"))){
                if(StringUtils.isBlank(payEntity.getMobile())){
                    //PC端
                    String codeUrl = jsonObject.getString("codeUrl");
                    return PayResponse.sm_qrcode(payEntity, codeUrl, "扫码支付下单成功");
                }else{
                    String payCode = jsonObject.getString("payCode");
                    return PayResponse.sm_link(payEntity, payCode, "H5支付下单成功");
                }
            }
            
            return PayResponse.error("下单失败:" + response);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[TTZF]通支付2扫码支付异常:{}",e.getMessage());
            return PayResponse.error("[TTZF]通支付2扫码支付异常");
        }
    }

    @Override
    public String callback(Map<String, String> data) {
        logger.info("[TTZF]通支付2支付回调验签开始===============start=============");
        try {
            
            //生成原签名串
            String sourceSign = data.get("sign");
            logger.info("[TTZF]通支付2支付回调验签获取原签名串:{}",sourceSign);
            String sign = generatorSign(data);
            logger.info("[TTZF]通支付2支付回调验签生成加密签名串：{}",sign);
            
            if(sourceSign.equalsIgnoreCase(sign)) return "success";
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[TTZF]通支付2支付回调验签异常:{}",e.getMessage());
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
        logger.info("[TTZF]通支付2组装支付请求参数开始===============start=============");
        try {
            Map<String,String> data = new HashMap<>();
            
            String amount = new DecimalFormat("0.00").format(entity.getAmount());
            
            data.put("tradeType","cs.pay.submit");//交易类型String(32)cs.pay.submit
            data.put("version","1.5");//版本String(8)版本号，1.5
            data.put("channel","");//支付类型String(24)支付使用的第三方支付类型，见附件“7.1支付类型”
            data.put("mchId",pid);//商户号String(32)由平台分配的商户号
            data.put("sign","");//签名String(32)签名，详见签名生成算法MD5
            data.put("body","TOP-UP");//商品描述String(128)商品或支付单简要描述
            data.put("outTradeNo",entity.getOrderNo());//商户订单号String(32)商户系统内部的订单号,32个字符内、可包含字母, 确保在商户系统唯一
            data.put("amount",amount);//交易金额Number单位为元，小数两位
//            data.put("timePaid","");//订单创建时间String(14)订单创建时间，格式为yyyyMMddHHmmss，如2009年12月25日9点10分10秒表示为20091225091010
//            data.put("timeExpire","");//订单失效时间String(14)订单失效时间，格式同上,不传默认是上游的渠道失效时间，传失效时间不能低于创建时间5分钟以下
            if("T1".equals(settleCycle)){
                data.put("settleCycle","1");//结算周期Number0(D0) 1(T1)默认结算周期是T1必填：
            }
//            data.put("authCode","");//支付授权码String(50)被扫条码编号；以下支付类型该参数有效且必填：1.wxMicro 2.alipayMicro 3.jdMicro 4. qqMicro
//            data.put("limitPay","");//限定支付方式String(500)指定不能使用信用卡支付可设置为no_credit；以下支付类型该参数有效：1.wxPub 2.wxPubQR 3.wxMicro 4.wxApp 5.qqQr 6.gateway
//            data.put("mobileAppId","");//移动APPIDString微信开放平台上创建应用所生成的AppID；支付类型wxApp该参数有效且必填
            if(StringUtils.isNoneBlank(entity.getMobile())){
                //移动端
                data.put("mwebType","WAP");//应用场景类型String应用场景类型；支付类型wxH5该参数有效且必填;取值范围：IOS/Android/WAP
                data.put("mwebName","TX");//应用场景名称String支付类型wxH5该参数有效且必填;取值规则如下：IOS：应用在App Store中唯一应用名； Android：应用在一台设备上的唯一标识在manifest文件里面的声明 WAP：WAP网站名
                data.put("mwebId",entity.getRefererUrl());//应用场景IDString支付类型wxH5该参数有效且必填;IOS：IOS应用唯一标识 Android：应用在安卓分发市场中的应用名 WAP：WAP网站的首页URL
            }
            data.put("notifyUrl",notifyUrl);//后台异步回调地址String(500)支付完成后结果通知url；参数参考交易详细查询;
            data.put("callbackUrl",entity.getRefererUrl());//前台同步回调地址String(500)支付成功跳转路径；form表单形式提交商户后台；参数参考交易详细查询;以下支付类型该参数有效：1.wxPub 2.wxH5 3.qpay 4.jdPay 5.jdGateway
//            data.put("bankType","");//银行类型 String应用场景类型；支付类型gateway、qpay该参数有效且必填;银行类型 参见 “7.4银行类型”
//            data.put("accountType","");//用户类型String应用场景类型；支付类型gateway该参数有效且必填;帐户类型;1：个人；2：企业
//            data.put("openId","");//公众号openIdString应用场景类型；支付类型wxPub该参数有效且必填;
//            data.put("appId","");//公众号appIdString应用场景类型；支付类型wxPub该参数有效且必填;
            if("alipayH5".equals(entity.getPayCode())){
                data.put("userId",entity.getuId());//支付宝userIdString应用场景类型；支付类型alipayH5该参数有效且必填;
            }
//            data.put("cardInfo","");//卡信息String支付类型qpay该参数有效且必填;
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[TTZF]通支付2组装支付请求参数异常:{}",e.getMessage());
            throw new Exception("[TTZF]通支付2组装支付请求参数异常");
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
        logger.info("[TTZF]通支付2生成签名串开始===============start=============");
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
            
            String signStr = sb.append("key=").append(secret).toString();
            logger.info("[TTZF]通支付2生成待加密签名串:{}",signStr);
            String sign = MD5Utils.md5toUpCase_32Bit(signStr);
            logger.info("[TTZF]通支付2生成加密签名串:{}",sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[TTZF]通支付2生成签名串异常:{}",e.getMessage());
            throw new Exception("[TTZF]通支付2生成签名串异常");
        }
    }
    
}
