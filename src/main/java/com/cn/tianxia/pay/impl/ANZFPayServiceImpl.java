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
 * @ClassName ANZFPayServiceImpl
 * @Description A9支付
 * @author Hardy
 * @Date 2018年11月7日 下午9:18:42
 * @version 1.0.0
 */
public class ANZFPayServiceImpl implements PayService {

    //日志 
    private static final Logger logger = LoggerFactory.getLogger(ANZFPayServiceImpl.class);
    
    private String MerchantId;//商户号
    
    private String payUrl;//支付请求地址
    
    private String notifyUrl;//回调请求地址
    
    private String secret;//秘钥
    
    //构造器,初始化参数
    public ANZFPayServiceImpl(Map<String,String> data) {
        if(data != null && !data.isEmpty()){
            if(data.containsKey("MerchantId")){
                this.MerchantId = data.get("MerchantId");
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
     * 网银
     */
    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        logger.info("[ANZF]A9支付网银支付开始======================START==================");
        try {
            //获取支付请求参数
            Map<String,String> data = sealRequest(payEntity, 1);
            //生成签名串
            String sign = generatorSign(data);
            data.put("sign", sign);
            logger.info("[ANZF]A9支付生成支付请求报文:{}",JSONObject.fromObject(data).toString());
            String formStr = HttpUtils.generatorForm(data, payUrl);
            logger.info("[ANZF]A9支付生成请求表单:{}",formStr);
            return PayResponse.wy_form(payEntity.getPayUrl(), formStr);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[ANZF]A9支付扫码支付异常:{}",e.getMessage());
            return PayResponse.error("[ANZF]A9支付扫码支付异常");
        }
    }

    /**
     * 扫码
     */
    @Override
    public JSONObject smPay(PayEntity payEntity) {
        logger.info("[ANZF]A9支付扫码支付开始======================START==================");
        try {
            //获取支付请求参数
            Map<String,String> data = sealRequest(payEntity, 2);
            //生成签名串
            String sign = generatorSign(data);
            data.put("sign", sign);
            logger.info("[ANZF]A9支付生成支付请求报文:{}",JSONObject.fromObject(data).toString());
            String formStr = HttpUtils.generatorForm(data, payUrl);
            logger.info("[ANZF]A9支付生成请求表单:{}",formStr);
            return PayResponse.sm_form(payEntity, formStr, "下单成功!");
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[ANZF]A9支付扫码支付异常:{}",e.getMessage());
            return PayResponse.error("[ANZF]A9支付扫码支付异常");
        }
    }

    /**
     * 回调验签
     */
    @Override
    public String callback(Map<String, String> data) {
        logger.info("[ANZF]A9支付回调验签开始===================START=========================");
        try {
            //获取原签名串
            String sourceSign = data.get("sign");
            logger.info("[ANZF]A9支付回调验签原签名串:{}",sourceSign);
            //生成回调验签签名串
            String sign = generatorSign(data);
            logger.info("[ANZF]A9支付回调验签生成加密签名串:{}",sign);
            
            if(sourceSign.equalsIgnoreCase(sign)) return "success";
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[ANZF]A9支付回调验签异常:{}",e.getMessage());
        }
        return "faild";
    }

    
    /**
     * 
     * @Description 封装支付请求参数
     * @param entity
     * @param type 支付类型  1 网银支付   2 扫码支付
     * @return
     * @throws Exception
     */
    private Map<String,String> sealRequest(PayEntity entity,Integer type) throws Exception{
        logger.info("[ANZF]A9支付封装支付请求参数开始========================START===================");
        try {
            
            //创建请求参数存储对象
            Map<String,String> data = new HashMap<>();
            String amount = new DecimalFormat("0.00").format(entity.getAmount());
            data.put("inputCharset","UTF-8");//字符集,UTF-8
            data.put("signType","MD5");//签名方式,MD5
            data.put("MerchantId",MerchantId);//商户编号,聚合商户编号
            data.put("out_trade_no",entity.getOrderNo());//商户订单号,商户订单号
            data.put("amount",amount);//订单金额,精确到分 0.00
            data.put("attach","TOP-UP");//回传参数,商户自定义返回参数
            if(type == 1){
                data.put("gateway","cyberbank");//支付类型,参数参照最底部图片
            }else{
                data.put("gateway", entity.getPayCode());
            }
            data.put("returnUrl",entity.getRefererUrl());//同步跳转,同步跳转地址（部分通道无同步跳转）
            data.put("notifyUrl",notifyUrl);//通知地址,商户通知地址
            data.put("sign","");//签名摘要
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[ANZF]A9支付封装支付请求参数异常:{}",e.getMessage());
            throw new Exception("[ANZF]A9支付封装支付请求参数异常");
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
        logger.info("[ANZF]A9支付封装支付生成加密签名串开始======================START===================");
        try {
            //签名规则:
            //第一步，设所有发送或者接收到的数据为集合M，将集合M内非空参数值的参数按照参数名ASCII码从小到大排序（字典序），
            //使用URL键值对的格式（即key1=value1&key2=value2…）拼接成字符串stringA。
            //如果参数的值为空不参与签名；
            //第二步，在stringA最后拼接上key得到stringSignTemp字符串，并对stringSignTemp进行MD5运算，
            //再将得到的字符串所有字符转换为大写，得到sign值signValue。
            StringBuffer sb = new StringBuffer();
            Map<String,String> sortmap = MapUtils.sortByKeys(data);
            Iterator<String> iterator = sortmap.keySet().iterator();
            while(iterator.hasNext()){
                String key = iterator.next();
                String val = sortmap.get(key);
                if(StringUtils.isBlank(val) || key.equalsIgnoreCase("sign")) continue;
                sb.append("&").append(key).append("=").append(val);
            }
            sb.append(secret);
            String signStr = sb.toString().replaceFirst("&", "");
            logger.info("[ANZF]A9支付生成待签名串:{}",signStr);
            String sign = MD5Utils.md5toUpCase_32Bit(signStr);
            logger.info("[ANZF]A9支付生成加密签名串:{}",sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[ANZF]A9支付封装支付生成加密签名串异常:{}",e.getMessage());
            throw new Exception("[ANZF]A9支付封装支付生成加密签名串异常");
        }
    }
    
}
