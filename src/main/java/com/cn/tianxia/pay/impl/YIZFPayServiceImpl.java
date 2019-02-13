package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

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
 * @ClassName YIZFPayServiceImpl
 * @Description 易支付
 * @author Hardy
 * @Date 2018年10月7日 下午2:50:56
 * @version 1.0.0
 */
public class YIZFPayServiceImpl implements PayService{
    
    /**
     * 日志
     */
    private static final Logger logger = LoggerFactory.getLogger(YIZFPayServiceImpl.class);
    
    private String payUrl;//支付请求地址
    
    private String uid;//商户号
    
    private String notifyUrl;//回调地址
    
    private String secret;//秘钥

    //构造器，初始化基本信息
    public YIZFPayServiceImpl(Map<String,String> data) {
        if(data != null && !data.isEmpty()){
            if(data.containsKey("payUrl")){
                this.payUrl = data.get("payUrl");
            }
            if(data.containsKey("uid")){
                this.uid = data.get("uid");
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
        return null;
    }

    /**
     * 扫码支付
     */
    @Override
    public JSONObject smPay(PayEntity payEntity) {
        logger.info("[YIZF]易支付扫码支付开始======================START=================");
        try {
            //获取支付请求参数
            Map<String,String> data = sealRequest(payEntity);
            //生成钱明
            String key = generatorSign(data);
            data.put("key", key);//秘钥
            data.remove("token");
            logger.info("[YIZF]易支付扫码支付,请求参数:["+JSONObject.fromObject(data).toString()+"],支付请求地址:["+payUrl+"]");
            
            //发起支付
            String response = HttpUtils.generatorForm(data, payUrl);
            logger.info("[YIZF]易支付请求报文:"+response);
            return PayUtil.returnPayJson("success", "1", "下单成功", "", 0, "", response);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[YIZF]易支付扫码支付异常:"+e.getMessage());
            return PayUtil.returnPayJson("error", "2", "下单失败!", "", 0, "", "");
        }
    }

    /**
     * 回调
     */
    @Override
    public String callback(Map<String, String> data) {
        logger.info("[YIZF]易支付回调验签开始==================START=================");
        try {
            //原签名串
            String sourceSign = data.get("sign");
            //生成回调签名串 
            StringBuffer sb = new StringBuffer();
            sb.append(data.get("order_id")).append(data.get("price")).append(secret);
            String signStr = sb.toString();
            logger.info("[YIZF]易支付回调待签名串:"+signStr);
            String sign = MD5Utils.md5toUpCase_32Bit(signStr).toLowerCase();
            logger.info("[YIZF]易支付回调验签,服务器签名串:["+sourceSign+"],本地签名串:["+sign+"]");
            if(sign.equalsIgnoreCase(sourceSign)) return "success";
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[YIZF]易支付回调验签异常:"+e.getMessage());
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
        logger.info("[YIZF]易支付组装支付请求参数开始===============START===================");
        try {
            String amount = new DecimalFormat("0.00").format(entity.getAmount());
            //创建存储参数对象
            Map<String,String> data = new HashMap<>();
            data.put("uid", uid);//商户uid
            data.put("price", amount);//订单金额，单位：元。精确小数点后2位
            data.put("type", entity.getPayCode());//支付渠道 1：微信支付；2：支付宝
            data.put("notify_url",notifyUrl);//回调地址
            data.put("return_url", entity.getRefererUrl());//跳转网址
            data.put("order_id", entity.getOrderNo());//订单号
            data.put("order_uid", entity.getuId());//用户ID
            data.put("order_name", "TOP-UP");//商品名称
            data.put("token", secret);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[YIZF]易支付组装支付请求参数异常:"+e.getMessage());
            throw new Exception("组装支付请求参数异常!");
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
        logger.info("[YIZF]易支付生成签名开始================START=====================");
        try {
            //签名规则:key的拼接顺序：如用到了所有参数，就按这个顺序拼接：notify_url + order_id + order_name + price + return_url + token + type + uid
            //把使用到的所有参数，连Token一起，按参数名字母升序排序。把参数值拼接在一起。做md5-32位加密，取字符串小写。得到key。网址类型的参数值不要urlencode
            Map<String,String> treemap = new TreeMap<>(new Comparator<String>() {
                //排序
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }
            });
        
            treemap.putAll(data);
            
            StringBuffer sb = new StringBuffer();
            Iterator<String> iterator = treemap.keySet().iterator();
            while(iterator.hasNext()){
                String key = iterator.next();
                String val = treemap.get(key);
                
                if(StringUtils.isBlank(val) || key.equalsIgnoreCase("key") 
                        || key.equalsIgnoreCase("sign") || key.equalsIgnoreCase("order_uid")) continue;
                
                sb.append(val);
            }
            String signStr = sb.toString();
            logger.info("[YIZF]易支付生成待签名串:"+signStr);
            String sign = MD5Utils.md5toUpCase_32Bit(signStr).toLowerCase();
            logger.info("[YIZF]易支付生成加密签名串:"+sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[YIZF]易支付生成签名异常:"+e.getMessage());
            throw new Exception("生成签名异常!");
        }
    }

}
