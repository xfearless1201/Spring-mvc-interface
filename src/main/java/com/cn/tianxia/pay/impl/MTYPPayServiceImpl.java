package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import com.cn.tianxia.pay.ys.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.pay.service.PayService;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName MTYPPayServiceImpl
 * @Description 迷特优支付
 * @author Hardy
 * @Date 2018年12月19日 下午12:18:33
 * @version 1.0.0
 */
public class MTYPPayServiceImpl implements PayService {

    //日志
    private static final Logger logger = LoggerFactory.getLogger(MTYPPayServiceImpl.class);


    private String payUrl = "http://47.107.247.113/gateway/transaction/request";
    //测试商户编号：
    private String merNo  = "88882018121610001148";
    //测试商户商户产品编号：
    private String merKey =  "e08891a18ff54420851c4966b3c5a512";
    //测试商户商户产品密钥：
    private String paySecret = "a5556bee977f4d99aa9831a2225f0bac";

    private String notifyUrl = "http://txw.tx8899.com/TYC/Notify/MTYPNotify.do";


    public MTYPPayServiceImpl(Map<String,String> map){
        if(map != null && !map.isEmpty()){
            if(map.containsKey("merNo")){
                this.merNo = map.get("merNo");
            }
            if(map.containsKey("notifyUrl")){
                this.notifyUrl = map.get("notifyUrl");
            }
            if(map.containsKey("merKey")){
                this.merKey = map.get("merKey");
            }
            if(map.containsKey("paySecret")){
                this.paySecret = map.get("paySecret");
            }
            if(map.containsKey("payUrl")){
                this.payUrl = map.get("payUrl");
            }
        }
    }

    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        return null;
    }

    @Override
    public JSONObject smPay(PayEntity payEntity) {
        try {
            Map<String,String> params = sealRequest(payEntity);
            String sign = generatorSign(params);
            params.put("sign",sign);
            logger.info("[MTYP]迷特优支付扫码支付请求参数:{}",params.toString());

            String response = HttpUtils.post(params,payUrl);

            if (StringUtils.isBlank(response)) {
                logger.info("[MTYP]迷特优支付扫码支付请求异常:请求结果为空！");
                return PayResponse.error("[MTYP]迷特优支付扫码支付请求异常:请求结果为空");
            }

            JSONObject jsonObject = JSONObject.fromObject(response);

            if (null != jsonObject && jsonObject.containsKey("respCode") && "0000".equals(jsonObject.getString("respCode"))) {
                logger.info("[MTYP]迷特优扫码支付成功状态值:{}",jsonObject.getString("respCode"));
                //支付充值成功
                String payurl = jsonObject.getString("authCode");
                if(StringUtils.isBlank(payEntity.getMobile())){
                    //PC端
                    return PayResponse.sm_qrcode(payEntity, payurl, "扫码下单成功");
                }
                return PayResponse.sm_link(payEntity, payurl, "H5下单成功");
            }

            return PayResponse.error("下单失败:"+response);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[MTYP]迷特优支付扫码支付异常:{}",e.getMessage());
            return PayResponse.error("[MTYP]迷特优支付扫码支付异常:"+e.getMessage());
        }

    }

    @Override
    public String callback(Map<String, String> data) {

        try {
            String sourceSign = data.remove("sign");
            logger.info("[MTYP]迷特优支付回调验签原签名串;",data.toString());
            logger.info("[MTYP]迷特优支付回调验签原签名字段：{}",sourceSign);
            String validSign = generatorSign(data);
            logger.info("[MTYP]迷特优支付回调验签生成签名：{}",validSign);
            if (sourceSign.equalsIgnoreCase(validSign)) {
                logger.info("[MTYP]迷特优支付回调验签成功");
                return "success";
            }
            return "fail";
        } catch (Exception e) {
            logger.error("[JFZF]迷特优支付回调验签异常:" + e.getMessage());
            return "fail";
        }
    }

    /**
     *
     * @Description 封装支付请求参数
     * @param entity
     * @return
     * @throws Exception
     */
    private Map<String,String> sealRequest(PayEntity entity) throws Exception{
        logger.info("[MTYP]迷特优支付封装请求参数开始=====================START==================");
        try {
            //创建支付请求参数存储对象
            Map<String,String> data = new HashMap<>();
            //订单金额
            String amount = new DecimalFormat("0.00").format(entity.getAmount());//交易金额
            Date date = new Date();
            data.put("transId",entity.getPayCode());   //  交易接口编号	N(30)	是	SMARTCLOUD_ALIPAY_H5_PAY支付宝
            data.put("serialNo",String.valueOf(System.currentTimeMillis()));                     //            serialNo	交易流水号	AN(20)	是	交易标识，每次请求必须唯一
            data.put("merNo",merNo);                     // 商户号	AN(20)	是	由平台提供
            data.put("merKey",merKey);                       // 商户交易KEY	AN(32)	是	由平台提供
            data.put("merIp",entity.getIp());                // 商户请求IP	NS(20)	是	Ip地址
            data.put("orderNo",entity.getOrderNo());         // 商户订单号	ANS(20)	是	商户系统生成的唯一订单号
            data.put("transAmt",amount);                     // 交易金额	NS(20)	是	单位：元，格式：12.34
            data.put("orderDesc","top_up");                  // 订单内容描述	ANS(99)	是	订单商品内容描述
            data.put("transDate",new SimpleDateFormat("yyyyMMdd").format(date));             // 交易日期	N(8)	是	格式：yyyyMMdd
            data.put("transTime",new SimpleDateFormat("yyyyMMddHHmmss").format(date));                // 交易时间	N(14)	是	格式: yyyyMMddHHmmss
            data.put("notifyUrl",notifyUrl); //异步返回地址	ANS(99)	是	商户异步接收交易结果地址

            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[MTYP]迷特优支付封装请求参数异常:"+e.getMessage());
            throw new Exception("封装支付请求参数异常!");
        }
    }

    /**
     *
     * @Description 生成支付签名串
     * @param data
     * @return
     * @throws Exception
     */
    public String generatorSign(Map<String,String> data) throws Exception{
        logger.info("[MTYP]迷特优支付生成支付签名串开始==================START========================");
        try {
            Map<String,String> treemap = new TreeMap<>();
            treemap.putAll(data);

            //生成待签名串
            StringBuffer sb = new StringBuffer();
            Iterator<String> iterator = treemap.keySet().iterator();
            while(iterator.hasNext()){
                String key = iterator.next();
                String val = treemap.get(key);
                if(StringUtils.isBlank(val) || key.equalsIgnoreCase("sign"))
                    continue;
                sb.append(key).append("=").append(val).append("&");

            }

            //生成待签名串
            String signStr = sb.toString()+"paySecret=" + paySecret;
            logger.info("[MTYP]迷特优支付生成待签名串:"+signStr);
            String sign = MD5Utils.md5toUpCase_32Bit(signStr);
            logger.info("[MTYP]迷特优支付生成加密签名串:"+sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[MTYP]迷特优支付生成支付签名串异常:"+e.getMessage());
            throw new Exception("生成支付签名串异常!");
        }
    }

    public static void main(String[] args) {
        PayEntity entity = new PayEntity();
        entity.setPayCode("SMARTCLOUD_ALIPAY_H5_PAY");
        entity.setAmount(100);
        entity.setOrderNo("MTYP00000066476");
        entity.setIp("8.8.8.8");
        MTYPPayServiceImpl MTYPPayService = new MTYPPayServiceImpl(null);
        MTYPPayService.smPay(entity);

    }

}
