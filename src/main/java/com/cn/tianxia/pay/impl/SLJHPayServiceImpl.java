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

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName SLJHPayServiceImpl
 * @Description 顺利聚合支付
 * @author Hardy
 * @Date 2018年11月8日 上午11:57:23
 * @version 1.0.0
 */
public class SLJHPayServiceImpl implements PayService{

    //日志 
    private static final Logger logger = LoggerFactory.getLogger(SLJHPayServiceImpl.class);
    
    private String memberid;//商户号
    
    private String payUrl;//支付请求地址
    
    private String notifyUrl;//回调请求地址
    
    private String secret;//秘钥
    
    //构造器,初始化参数
    public SLJHPayServiceImpl(Map<String,String> data) {
        if(data != null && !data.isEmpty()){
            if(data.containsKey("memberid")){
                this.memberid = data.get("memberid");
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
    
    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JSONObject smPay(PayEntity payEntity) {
        logger.info("[SLJH]顺利聚合支付扫码支付开始=====================START==================");
        try {
            //获取支付请求报文
            Map<String,String> data = sealRequest(payEntity);
            //生成签名串
            String sign = generatorSign(data);
            data.put("pay_md5sign",sign);
            logger.info("[SLJH]顺利聚合支付扫码支付请求报文:{}",JSONObject.fromObject(data).toString());
            //发起支付请求
            String formStr = HttpUtils.generatorForm(data, payUrl);
            logger.info("[SLJH]顺利聚合支付扫码支付生成请求form表单:{}",formStr);
            return PayResponse.sm_form(payEntity, formStr, "下单成功!");
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[SLJH]顺利聚合支付扫码支付异常:{}",e.getMessage());
            return PayResponse.error("[SLJH]顺利聚合支付扫码支付异常");
        }
    }

    @Override
    public String callback(Map<String, String> data) {
        logger.info("[SLJH]顺利聚合支付回调验签开始=======================START=======================");
        try {
            //获取验签原签名串
            String sourceSign = data.get("sign");
            logger.info("[SLJH]顺利聚合支付回调验签服务器原签名串:{}",sourceSign);
            String sign = generatorSign(data);
            logger.info("[SLJH]顺利聚合支付回调验签加密签名串:{}",sign);
            if(sourceSign.equalsIgnoreCase(sign)) return "success";
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[SLJH]顺利聚合支付回调验签异常:{}",e.getMessage());
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
        logger.info("[SLJH]顺利聚合支付组装支付请求参数开始=====================START=======================");
        try {
            //创建支付请求参数存储对象
            Map<String,String> data = new HashMap<>();
            String amount = new DecimalFormat("0.00").format(entity.getAmount());//订单金额
            String applydata = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            data.put("pay_memberid",memberid);//商户号
            data.put("pay_orderid",entity.getOrderNo());//订单号,20位
            data.put("pay_applydate",applydata);//提交时间 yyyy-MM-dd HH:mm:ss
            data.put("pay_bankcode",entity.getPayCode());//银行编码
            data.put("pay_notifyurl",notifyUrl);//服务端通知
            data.put("pay_callbackurl",entity.getRefererUrl());//页面跳转通知
            data.put("pay_amount",amount);//订单金额
//            data.put("pay_md5sign","");//MD5签名
//            data.put("pay_attach","TOP-UP");//附加字段
            data.put("pay_productname","TOP-UP");//商品名称
//            data.put("pay_productnum","");//商户品数量
//            data.put("pay_productdesc","");//商品描述
//            data.put("pay_producturl","");//商户链接地址
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[SLJH]顺利聚合支付组装支付请求参数异常:{}",e.getMessage());
            throw new Exception("[SLJH]顺利聚合支付组装支付请求参数异常");
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
        logger.info("[SLJH]顺利聚合支付生成签名串开始===================START=====================");
        try {
            //签名规则:
            //第一步，设所有发送或者接收到的数据为集合M，将集合M内非空参数值的参数按照参数名ASCII码从小到大排序（字典序），
            //使用URL键值对的格式（即key1=value1&key2=value2…）拼接成字符串stringA。
            //如果参数的值为空不参与签名；
            //第二步，在stringA最后拼接上key得到stringSignTemp字符串，并对stringSignTemp进行MD5运算，
            //再将得到的字符串所有字符转换为大写，得到sign值signValue。
            Map<String,String> sortmap = MapUtils.sortByKeys(data);
            StringBuffer sb = new StringBuffer();
            Iterator<String> iterator = sortmap.keySet().iterator();
            while(iterator.hasNext()){
                String key = iterator.next();
                String val = sortmap.get(key);
                if(StringUtils.isBlank(val) || key.equalsIgnoreCase("pay_md5sign") || 
                        key.equalsIgnoreCase("sign") || key.equalsIgnoreCase("attach") || key.equalsIgnoreCase("pay_productname")) continue;
                
                sb.append(key).append("=").append(val).append("&");
            }
            sb.append("key=").append(secret);
            //待加密签名串
            String signStr = sb.toString();
            logger.info("[SLJH]顺利聚合支付生成待签名串:{}",signStr);
            String sign = MD5Utils.md5toUpCase_32Bit(signStr);
            logger.info("[SLJH]顺利聚合支付生成加密签名串:{}",sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[SLJH]顺利聚合支付生成签名串异常:{}",e.getMessage());
            throw new Exception("[SLJH]顺利聚合支付生成签名串异常");
        }
    }

}
