package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.MD5Util;
import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.gst.util.StringUtils;
import com.cn.tianxia.pay.qianying.util.HttpUtil;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.util.main;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName XFBPayServiceImpl
 * @Description 信付宝支付
 * @author Hardy
 * @Date 2018年9月13日 下午9:12:22
 * @version 1.0.0
 */
public class XFBPayServiceImpl implements PayService{
    
    /**
     * 日志
     */
    private static final Logger logger = LoggerFactory.getLogger(XFBPayServiceImpl.class);
    
    private String fxid;
    
    private String fxnotifyurl;
    
    private String payUrl;
    
    private String sercet;

    public XFBPayServiceImpl(Map<String,String> data) {
        if(data != null && !data.isEmpty()){
            if(data.containsKey("fxid")){
                this.fxid = data.get("fxid");
            }
            if(data.containsKey("fxnotifyurl")){
                this.fxnotifyurl = data.get("fxnotifyurl");
            }
            if(data.containsKey("payUrl")){
                this.payUrl = data.get("payUrl");
            }
            if(data.containsKey("sercet")){
                this.sercet = data.get("sercet");
            }
        }
    }

    /**
     * 网银支付
     */
    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * 扫码支付
     */
    @Override
    public JSONObject smPay(PayEntity payEntity) {
        logger.info("[XFB]信付宝支付开始=================START=============");
        //初始化基本数据
        String username = payEntity.getUsername();
        double amount = payEntity.getAmount();
        String order_no = payEntity.getOrderNo();
        try {
            //组装支付请求参数
            Map<String,String> data = sealRequest(payEntity);
            //发起下单请求
            String response = initPay(data);
            //解析响应结果
            JSONObject jsonObject = JSONObject.fromObject(response);
            String status = jsonObject.getString("status");//交易结果:状态【1代表正常】【0代表错误】
            if(status.equals("1")){
                //交易成功
                String payurl = jsonObject.getString("payurl");//正常状态下返回支付跳转路径，跳转到该路径即可支付
                
                return PayUtil.returnPayJson("success", "4", "生成跳转链接地址", username, amount, order_no, payurl);
            }
            String error = jsonObject.getString("error");//错误状态下返回错误信息utf-8编码数据
            return PayUtil.returnPayJson("faild", "2", "支付交易失败", username, amount, order_no, error);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[XFB]信付宝支付异常:"+e.getMessage());
            return PayUtil.returnPayJson("error", "2", "支付异常", username, amount, order_no,e.getMessage());
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
        logger.info("[XFB]信付宝支付组装请求参数开始=================start=================");
        try {
            //创建存储参数对象
            Map<String,String> data = new HashMap<>();
            String amount = new DecimalFormat("0.00").format(entity.getAmount());//订单金额
            data.put("fxid", fxid);//商务号 是   唯一号，由信付宝提供
            data.put("fxddh", entity.getOrderNo());//商户订单号   是   仅允许字母或数字类型,不超过22个字符，不要有中文
            data.put("fxdesc", "TOP-UP");//商品名称    是   utf-8编码
            data.put("fxfee", amount);//支付金额    是   请求的价格(单位：元) 可以0.01元
            data.put("fxattch", "TOP-UP");//附加信息    否   原样返回，utf-8编码
            data.put("fxnotifyurl", fxnotifyurl);//异步通知地址  是   异步接收支付结果通知的回调地址，通知url必须为外网可访问的url，不能携带参数。
            data.put("fxbackurl", entity.getRefererUrl());//同步通知地址  是   支付成功后跳转到的地址，不参与签名。
            data.put("fxpay", entity.getPayCode());//请求类型 【微信扫码：wxewm】【支付宝H5：zfbewm】 是   请求支付的接口类型。
            data.put("fxip", entity.getIp());//支付用户IP地址    是   用户支付时设备的IP地址
            //生成待签名串
            StringBuffer sb = new StringBuffer();
            sb.append(fxid).append(entity.getOrderNo()).append(amount).append(fxnotifyurl).append(sercet);
            String signStr = sb.toString();
            logger.info("[XFB]信付宝生成待签名串:"+signStr);
            String sign = MD5Util.encode(signStr);
            logger.info("[XFB]信付宝生成MD5加密签名串:"+sign);
            data.put("fxsign", sign);//签名【md5(商务号+商户订单号+支付金额+异步通知地址+商户秘钥)】 是   通过签名算法计算得出的签名值。
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[XFB]信付宝支付组装请求参数异常:"+e.getMessage());
            throw new Exception("[信付宝组装支付请求参数异常]");
        }
    }
    
    /**
     * 
     * @Description 发起第三方下单请求
     * @param data
     * @return
     * @throws Exception
     */
    private String initPay(Map<String,String> data) throws Exception{
        logger.info("[XFB]信付宝支付调用第三方接口开始=================START=============");
        try {
            String response = HttpUtil.toPostForm(data, payUrl);
            if(StringUtils.isNullOrEmpty(response)){
                logger.error("[XFB]信付宝支付调用第三方接口无响应结果!");
                throw new Exception("调用信付宝支付无响应结果,请重试....");
            }
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[XFB]信付宝支付发起第三方请求异常:"+e.getMessage());
            throw new Exception("调用第三方接口异常!");
        }
    }
    
    //回调验签
    @Override
    public String callback(Map<String,String> data) {
        logger.info("[XFB]信付宝支付回调验签开始=============START==================");
        try {
            String sourceSign = data.get("fxsign");
            logger.info("[XFB]信付宝支付回调签名原串:"+sourceSign);
            //组装待签名参数
            StringBuffer sb = new StringBuffer();
            sb.append(data.get("fxstatus")).append(fxid);
            sb.append(data.get("fxddh")).append(data.get("fxfee"));
            sb.append(sercet);
            String signStr = sb.toString();
            logger.info("[XFB]信付宝支付回调生成待签名串:"+signStr);
            //生成MD5加密签名串
            String sign = MD5Util.encode(signStr);
            logger.info("[XFB]信付宝支付回调生成MD5加密签名串:"+sign);
            if(sign.equals(sourceSign)) return "success";
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[XFB]信付宝支付回调验签异常:"+e.getMessage());
        }
        return "";
    }
    
    public static void main(String[] args) {
        JSONObject data = new JSONObject();
        data.put("fxid", "2018159");
        data.put("fxnotifyurl", "http://www.baidu.com");
        data.put("payUrl", "http://api.ds-pay.com/Pay");
        data.put("sercet", "ExiKEMiHyOsJXzyvpNKQVleRbTFXJAro");
        System.err.println(data.toString());
    }

}
