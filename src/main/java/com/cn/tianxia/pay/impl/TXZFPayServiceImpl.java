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
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.json.JSONUtils;
import com.cn.tianxia.pay.jh.util.MerchantApiUtil;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import com.cn.tianxia.pay.utils.MapUtils;

import net.sf.json.JSONObject;

public class TXZFPayServiceImpl implements PayService {

    private final static Logger logger = LoggerFactory.getLogger(TXZFPayServiceImpl.class);

    private String payKey;
    private String paySecret;
    // private String productName;
    private String notifyUrl;

    private String payUrl;
    private String scanUrl;

    // wx类型
    private String wxproductType;
    // zfb类型
    private String zfbproductType;
    // qq类型
    private String qqproductType;
    // zfb手机端类型
    private String zfbwapproductType;
    // kl类型
    private String kjproductType;
    // kl手机端类型
    private String kjwapproductType;
    // jd类型
    private String jdproductType;
    // yl类型
    private String ylproductType;
    private String qqwapproductType;
    private String wxwapproductType;

    //
    private String html = "no";

    public TXZFPayServiceImpl(Map<String, String> pmap) {
        if (pmap != null) {
            if (pmap.containsKey("payKey")) {
                payKey = pmap.get("payKey");
            }
            if (pmap.containsKey("notifyUrl")) {
                notifyUrl = pmap.get("notifyUrl");
            }
            if (pmap.containsKey("payUrl")) {
                payUrl = pmap.get("payUrl");
            }
            if (pmap.containsKey("scanUrl")) {
                scanUrl = pmap.get("scanUrl");
            }
            if (pmap.containsKey("paySecret")) {
                paySecret = pmap.get("paySecret");
            }
            if (pmap.containsKey("wxproductType")) {
                wxproductType = pmap.get("wxproductType");
            }
            if (pmap.containsKey("wxwapproductType")) {
                wxwapproductType = pmap.get("wxwapproductType");
            }
            if (pmap.containsKey("zfbproductType")) {
                zfbproductType = pmap.get("zfbproductType");
            }
            if (pmap.containsKey("qqproductType")) {
                qqproductType = pmap.get("qqproductType");
            }
            if (pmap.containsKey("qqwapproductType")) {
                qqwapproductType = pmap.get("qqwapproductType");
            }
            if (pmap.containsKey("zfbwapproductType")) {
                zfbwapproductType = pmap.get("zfbwapproductType");
            }
            if (pmap.containsKey("jdproductType")) {
                jdproductType = pmap.get("jdproductType");
            }
            if (pmap.containsKey("ylproductType")) {
                ylproductType = pmap.get("ylproductType");
            }
            if (pmap.containsKey("kjproductType")) {
                kjproductType = pmap.get("kjproductType");
            }
            if (pmap.containsKey("kjwapproductType")) {
                kjwapproductType = pmap.get("kjwapproductType");
            }
            if (pmap.containsKey("html")) {
                html = pmap.get("html");
            }

        }
    }

    /**
     * 网银支付
     */
    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        logger.info("[TTZF]天天支付网银支付开始================START======================");
        try {
            //获取支付请求参数
            Map<String,String> data = sealRequest(payEntity, 1);
            
            //生成签名串
            String sign = generatorSign(data);
            
            data.put("sign", sign);
            
            logger.info("[TTZF]天天支付生成网银支付请求报文:{}",JSONObject.fromObject(data).toString());
            
            //生成请求表单
            String formStr = HttpUtils.generatorForm(data, payUrl);
            
            return PayUtil.returnWYPayJson("success","form",formStr,payEntity.getPayUrl(),"pay");
            
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[TTZF]天天支付网银支付异常:{}",e.getMessage());
            return PayUtil.returnWYPayJson("error","网银支付下单失败:"+e.getMessage(),"","","");
        }
    }

    
    /**
     * 扫码支付
     */
    @Override
    public JSONObject smPay(PayEntity payEntity) {
        logger.info("[TTZF]天天支付扫码支付开始=========================START===============================");
        try {
            //获取支付请求参数
            Map<String,String> data = sealRequest(payEntity,0);
            //生成签名串
            String sign = generatorSign(data);
            
            data.put("sign", sign);
            
            logger.info("[TTZF]天天支付生成扫码支付请求报文:{}",JSONObject.fromObject(data).toString());
            
            // 快捷支付
            if ("KJ".equals(payEntity.getPayCode())) {
                String formStr = HttpUtils.generatorForm(data, payUrl);
                logger.info("[TTZF]天天支付生成form表单请求结果:{}", formStr);
                return PayUtil.returnPayJson("success", "1", "支付接口请求成功!", payEntity.getUsername(),
                        payEntity.getAmount(), payEntity.getOrderNo(), formStr);
            }
            String response = HttpUtils.toPostForm(data, scanUrl);
            if (StringUtils.isBlank(response)) {
                logger.error("[TTZF]天天支付发起HTTP请求无响应结果!");
                return PayUtil.returnPayJson("error", "2", "[TTZF]天天支付发起HTTP无响应结果!", "", 0, "", response);
            }
            logger.info("[TTZF]天天支付发起HTTP响应结果:" + response);
            JSONObject jsonObject = JSONObject.fromObject(response);
            if(jsonObject.containsKey("resultCode") && jsonObject.getString("resultCode").equals("0000")){
              String payMessage = jsonObject.getString("payMessage");
              if(StringUtils.isBlank(payEntity.getMobile())){
                  return PayUtil.returnPayJson("success","2","下单成功",payEntity.getUsername(),payEntity.getAmount(),payEntity.getOrderNo(),payMessage);
              }
              return PayUtil.returnPayJson("success", "4", "下单成功","",0,"",payMessage);
          }
          return PayUtil.returnPayJson("error", "2", "支付失败",payEntity.getUsername(),payEntity.getAmount(),payEntity.getOrderNo(),"支付失败原因:[" + response + "]");
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[TTZF]天天支付扫码支付异常:{}",e.getMessage());
            return PayUtil.returnPayJson("error","4","[TTZF]天天支付扫码支付异常:"+e.getMessage(),"",0,"", "");
        }

    }
    
    /**
     * 回调
     */
    @Override
    public String callback(Map<String, String> infoMap) {
        String serviSign = infoMap.remove("sign");
        Map<String, Object> map = JSONUtils.toHashMap(infoMap);
        // 制作签名
        String localSign = MerchantApiUtil.getSign(map, paySecret);

        logger.info("本地签名:" + localSign + "      服务器签名:" + serviSign);
        if (serviSign.equalsIgnoreCase(localSign)) {
            return "success";
        }

        return "";
    }
    
    
    /**
     * 
     * @Description 组装支付请求参数
     * @param entity
     * @param type 1 网银 0 扫码
     * @return
     * @throws Exception
     */
    private Map<String,String> sealRequest(PayEntity entity,Integer type) throws Exception{
        logger.info("[TTZF]天天支付组装支付请求参数开始=======================START==========================");
        try {
            //创建支付请求参数存储对象
            Map<String,String> data = new HashMap<>();
            String amount = new DecimalFormat("0.00").format(entity.getAmount());//订单金额,单位为元!
            String orderTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());//订单时间
            data.put("payKey", payKey); // 商户支付Key
            data.put("orderPrice", amount);// 订单金额，单位：元保留小数点后两位
            data.put("outTradeNo", entity.getOrderNo());// 商户支付订单号
            if(type == 1){
                //网银支付
                data.put("productType", "50000103"); // D0网银支付 50000102
                data.put("payBankAccountNo", entity.getPayCode());//银行编码
            }else{
                String productType = null;
                if (StringUtils.isNoneEmpty(entity.getMobile())) {
                  if ("ZFB".equals(entity.getPayCode()))
                      productType = zfbwapproductType;
                  if ("KJ".equals(entity.getPayCode()))
                      productType = kjwapproductType;
                  if ("QQ".equals(entity.getPayCode()))
                      productType = qqwapproductType;
                  if ("WX".equals(entity.getPayCode()))
                      productType = wxwapproductType;
              } else {
                  // pc端产品
                  if ("WX".equals(entity.getPayCode()))
                      productType = wxproductType;
                  if ("ZFB".equals(entity.getPayCode()))
                      productType = zfbproductType;
                  if ("QQ".equals(entity.getPayCode()))
                      productType = qqproductType;
                  if ("YL".equals(entity.getPayCode()))
                      productType = ylproductType;
                  if ("JD".equals(entity.getPayCode()))
                      productType = jdproductType;
                  if ("KJ".equals(entity.getPayCode()))
                      productType = kjproductType;
              }
                String productCode = getScanProductType(entity.getMobile(),productType,entity.getPayCode());
                data.put("productType",productCode); // D0网银支付 50000102
                data.put("field1", "url");//返回payMessage格式为链接
            }            
            data.put("orderTime", orderTime); // 下单时间，格式：yyyyMMddHHmmss
            data.put("productName", "TOP-UP");// 支付产品名称
            data.put("orderIp",entity.getIp()); // orderIp
            data.put("returnUrl",entity.getRefererUrl()); // 页面通知地址
            data.put("notifyUrl", notifyUrl);// 后台异步通知地址
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[TTZF]天天支付组装支付请求参数异常:{}",e.getMessage());
            throw new Exception("[TTZF]天天支付组装支付请求参数异常!");
        }
    }
    
    /**
     * 
     * @Description 生成签名
     * @param data
     * @return
     * @throws Exception
     */
    private String generatorSign(Map<String,String> data) throws Exception{
        logger.info("[TTZF]天天支付生成签名串开始==================START=====================");
        try {
            //参数排序
            Map<String,String> sortmap = MapUtils.sortByKeys(data);
            //组装签名串
            StringBuffer sb = new StringBuffer();
            Iterator<String> iterator = sortmap.keySet().iterator();
            while(iterator.hasNext()){
                String key = iterator.next();
                String val = sortmap.get(key);
                if(StringUtils.isBlank(val) || key.equalsIgnoreCase("sign")) continue;
                sb.append(key).append("=").append(val).append("&");
            }
            sb.append("paySecret=").append(paySecret);
            
            String signStr = sb.toString();
            logger.info("[TTZF]天天支付生成待签名串:{}",signStr);
            String sign = MD5Utils.md5toUpCase_32Bit(signStr);//签名串 大写
            logger.info("[TTZF]天天支付生成加密签名串:{}",sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[TTZF]天天支付生成签名串异常:{}",e.getMessage());
            throw new Exception("[TTZF]天天支付生成签名串异常");
        }
    }
    
    /**
     * 
     * @Description 获取支付类型
     * @param mobile
     * @param productType
     * @param type 支付渠道类型 
     * @return
     * @throws Exception
     */
    private String getScanProductType(String mobile,String productType,String type){
        logger.info("[TTZF]天天支付获取扫码支付产品类型开始========================START=========================");
        String productCode = "";
        if(type.equalsIgnoreCase("WX")){
            if(productType.equalsIgnoreCase("T0") || productType.equalsIgnoreCase("D0")){
                if(StringUtils.isBlank(mobile)){
                    //PC端
                    productCode = "10000103";
                }else{
                    productCode = "10000203";
                }
            }else if(productType.equalsIgnoreCase("T1")){
                if(StringUtils.isBlank(mobile)){
                    productCode = "10000101";
                }else{
                    productCode = "10000201";
                }
            }
        }else if(type.equalsIgnoreCase("ZFB")){
            if(productType.equalsIgnoreCase("T0") || productType.equalsIgnoreCase("D0")){
                if(StringUtils.isBlank(mobile)){
                    //PC端
                    productCode = "20000203";
                }else{
                    productCode = "20000203";
                }
            }else if(productType.equalsIgnoreCase("T1")){
                if(StringUtils.isBlank(mobile)){
                    productCode = "20000301";
                }else{
                    productCode = "20000201";
                }
            }
        }else if(type.equalsIgnoreCase("QQ")){
            if(productType.equalsIgnoreCase("T0") || productType.equalsIgnoreCase("D0")){
                productCode = "70000203";
            }else if(productType.equalsIgnoreCase("T1")){
                productCode = "70000201";
            }
        }else if(type.equalsIgnoreCase("YL")){
            if(productType.equalsIgnoreCase("T0") || productType.equalsIgnoreCase("D0")){
                productCode = "60000103";
            }else if(productType.equalsIgnoreCase("T1")){
                productCode = "60000101";
            }
        }
        
        return productCode;
    }

}
