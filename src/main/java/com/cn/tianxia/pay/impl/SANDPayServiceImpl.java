package com.cn.tianxia.pay.impl;

import java.io.IOException;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.pay.sand.util.CertUtil;
import com.cn.tianxia.pay.sand.util.CryptoUtil;
import com.cn.tianxia.pay.sand.util.HttpClient;
import com.cn.tianxia.pay.sand.util.SDKConfig;
import com.cn.tianxia.pay.sand.util.SDKUtil;
import com.cn.tianxia.pay.service.PayService;

import net.sf.json.JSONObject;

public class SANDPayServiceImpl implements PayService {

    public static  Logger logger = LoggerFactory.getLogger(SANDPayServiceImpl.class);
    private  String version ;  // 店员通 智能云支付地址
    private  String mid ;     // 商户id
    private  String plMid ;
    private  String productId ; // 
    private  String subject ;
    private  String bodyMsg ;//
    private  String notifyUrl ;
    
    private  String gateway_url ;
    private  String storeId ;
    private  String terminalId ;
    private  String operatorId ;
    
    
    public SANDPayServiceImpl(Map<String, String> pmap) {
        if (pmap != null) {
            if (pmap.containsKey("version")) {
                version = pmap.get("version");
            }
            if (pmap.containsKey("mid")) {
                mid = pmap.get("mid");
            }
            if (pmap.containsKey("plMid")) {
                plMid = pmap.get("plMid");
            }
            if (pmap.containsKey("productId")) {
                productId = pmap.get("productId");
            }
            if (pmap.containsKey("subject")) {
                subject = pmap.get("subject");
            }
            if (pmap.containsKey("bodyMsg")) {
                bodyMsg = pmap.get("bodyMsg");
            }
            if (pmap.containsKey("notifyUrl")) {
                notifyUrl = pmap.get("notifyUrl");
            }
            if (pmap.containsKey("gateway_url")) {
                gateway_url = pmap.get("gateway_url");
            }
            if (pmap.containsKey("storeId")) {
                storeId = pmap.get("storeId");
            }
            if (pmap.containsKey("gateway_url")) {
                gateway_url = pmap.get("gateway_url");
            }
            if (pmap.containsKey("storeId")) {
                storeId = pmap.get("storeId");
            }
            if (pmap.containsKey("terminalId")) {
                terminalId = pmap.get("terminalId");
            }
            if (pmap.containsKey("operatorId")) {
                operatorId = pmap.get("operatorId");
            } 
        }
    }
    public static void main(String[] args) {
         HashMap<String, String> pmap = new HashMap<String, String>();
         pmap.put("version", "1.0");
         pmap.put("mid", "90000061");  
         pmap.put("plMid", "");      // 平台商户ID
         pmap.put("productId", "00000007");  // 产品编码,详见《杉德线上支付接口规范》 附录
         pmap.put("subject", "话费充值");    
         pmap.put("bodyMsg", "用户购买话费0.01");  // 订单描述
         pmap.put("notifyUrl", "http://127.0.0.1/Notify/SANDNotify.do");
         pmap.put("gateway_url", "http://61.129.71.103:8003/gateway/api/order/pay");
         pmap.put("storeId", ""); // 商户门店编号
         pmap.put("terminalId", "");// 商户终端编号
         pmap.put("operatorId", "");// 操作员编号
         System.out.println(JSONObject.fromObject(pmap).toString());
    }
    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        String payCode = payEntity.getPayCode();
        
        JSONObject header = new JSONObject();
        header.put("version", version);         //版本号
        header.put("method", "sandpay.trade.pay");          //接口名称:统一下单
        header.put("mid", mid); //商户ID
        //平台商户ID
        if(plMid!=null && StringUtils.isNotEmpty(plMid)) {  //平台商户存在时接入
            header.put("accessType", "2");                  //接入类型设置为平台商户接入
            header.put("plMid", plMid);
        }else {
            header.put("accessType", "1");                  //接入类型设置为平台商户接入                                             //接入类型设置为普通商户接入
        }       
        header.put("channelType", "07");                    //渠道类型：07-互联网   08-移动端
        header.put("reqTime", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));   //请求时间
        String productId="00000007";
        
        header.put("productId", productId);//产品编码,详见《杉德线上支付接口规范》 附录
        
        JSONObject body=new JSONObject();
        body.put("orderCode", payEntity.getOrderNo());                           //商户订单号
        
        DecimalFormat df=new DecimalFormat("000000000000");
        String totalAmount=df.format(payEntity.getAmount()*100);
        
        body.put("totalAmount",totalAmount);                                  //订单金额
        body.put("subject", subject);                                             //订单标题
        body.put("body", bodyMsg);                                         //订单描述 
        body.put("txnTimeOut", getNextDayTime());                        //订单超时时间
        body.put("clientIp", payEntity.getIp());                                    //客户端IP
        body.put("notifyUrl", notifyUrl);                         //异步通知地址  
        body.put("frontUrl", payEntity.getRefererUrl());                     //前台通知地址
        body.put("storeId", "");                                                  //商户门店编号
        body.put("terminalId", "");                                               //商户终端编号
        body.put("operatorId", "");                                               //操作员编号
        body.put("clearCycle", "");                                               //清算模式
        body.put("royaltyInfo", "");                                              //分账信息
        body.put("riskRateInfo", "");                                             //风控信息域
        body.put("bizExtendParams", "");                                          //业务扩展参数
        body.put("merchExtendParams", "");                                        //商户扩展参数
        body.put("extend", "");                                                   //扩展域
        
        String payMode="bank_pc";
        JSONObject payExtra=new JSONObject();
        payExtra.put("payType", "1");               //支付类型  1-借记卡  2-贷记卡  3-借/贷记卡
        //payExtra.put("bankCode", payEntity.getPayCode());     //银行编码,具体编码见《杉德线上支付接口规范》 附录
        payExtra.put("bankCode", "01020000");   
        body.put("payMode", payMode);                   //支付模式
        body.put("payExtra", payExtra.toString());  //支付扩展域
        
        JSONObject resp= requestServer(header, body);
        JSONObject params = new JSONObject();
        if ("000000".equals(resp.getJSONObject("head").get("respCode"))) {
            String credential = resp.getJSONObject("body").getJSONObject("credential").toString();
            logger.info("生成的支付凭证为："+credential);
            params.put("credential", credential);
            params.put("status", "success");
            //String jump = params.getString("backUrl");
            return params;
        }else{
            params.put("status", "error");
            return params;
        }
        
    }

    public JSONObject requestServer(JSONObject header,JSONObject body) {
        
        Map<String, String> reqMap = new HashMap<String, String>();
        JSONObject reqJson=new JSONObject();
        reqJson.put("head", header);
        reqJson.put("body", body);
        String reqStr=reqJson.toString();
        String reqSign;
        //加载配置文件
        SDKConfig.getConfig().loadPropertiesFromSrc();
        //加载证书
        try {
            CertUtil.init(SDKConfig.getConfig().getSandCertPath(), SDKConfig.getConfig().getSignCertPath(), SDKConfig.getConfig().getSignCertPwd());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 签名
        try {
            reqSign = new String(Base64.encodeBase64(CryptoUtil.digitalSign(reqStr.getBytes("UTF-8"), CertUtil.getPrivateKey(), "SHA1WithRSA")));
            
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
        //整体报文格式
        reqMap.put("charset", "UTF-8");
        reqMap.put("data", reqStr);
        reqMap.put("signType", "01");
        reqMap.put("sign", reqSign);
        reqMap.put("extend", "");
        
        String result;
        try {
            logger.info("请求报文：\n"+reqJson.toString());  
            result = HttpClient.doPost(gateway_url, reqMap, 20000, 20000);
            logger.info("响应报文：\n" + result);
            result = URLDecoder.decode(result, "UTF-8");
        } catch (IOException e) {
            logger.error(e.getMessage());
            return null;
        }
        Map<String, String> respMap = SDKUtil.convertResultStringToMap(result);     
        String respData = respMap.get("data");
        String respSign = respMap.get("sign");
        
        // 验证签名
        boolean valid;
        try {
            valid = CryptoUtil.verifyDigitalSign(respData.getBytes("UTF-8"), Base64.decodeBase64(respSign), CertUtil.getPublicKey(),"SHA1WithRSA");
            if(!valid) {
                logger.error("verify sign fail.");
                return null;
            }           
            logger.info("verify sign success");
            JSONObject respJson=JSONObject.fromObject(respData);
            if(respJson!=null) {
                logger.info("响应码：["+respJson.getJSONObject("head").getString("respCode")+"]");  
                logger.info("响应描述：["+respJson.getJSONObject("head").getString("respMsg")+"]");  
                logger.info("响应报文：\n"+respJson.toString());             
            }else {
                logger.error("服务器请求异常！！！"); 
            }           
            return respJson;
            
        }  catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }
    

    @Override
    public JSONObject smPay(PayEntity payEntity) {
        // TODO Auto-generated method stub
        return null;
    }

    //获取当前时间24小时后的时间
    public String getNextDayTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        return new SimpleDateFormat("yyyyMMddHHmmss").format(calendar.getTime());
    }
    
    @Override
    public String callback(Map<String, String> infoMap) {
        return "success";
    }
}
