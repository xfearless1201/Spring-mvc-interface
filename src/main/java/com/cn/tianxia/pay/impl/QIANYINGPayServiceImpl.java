package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.qianying.util.HttpUtil;
import com.cn.tianxia.pay.qianying.util.MD5Util;
import com.cn.tianxia.pay.service.PayService;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName QIANYINGPayServiceImpl
 * @Description 千应支付
 * @author Hardy
 * @Date 2018年8月31日 下午8:43:54
 * @version 1.0.0
 */
public class QIANYINGPayServiceImpl implements PayService{
    
    /**
     * 日志
     */
    private final static Logger logger = LoggerFactory.getLogger(QIANYINGPayServiceImpl.class);
    
    private final static String CHARSET_UTF_8="UTF-8";//编码类型

    private String uid;//商户ID，有千应支付分配

    private String callbackurl;//下行异步通知地址

    private String sercet;//签名key
    
    private String payUrl;//支付url
    

    public QIANYINGPayServiceImpl(Map<String,String> pmap) {

        if (pmap != null && !pmap.isEmpty()){
            
            if (pmap.containsKey("uid")){
                this.uid = pmap.get("uid");
            }

            if (pmap.containsKey("callbackurl")){
                this.callbackurl = pmap.get("callbackurl");
            }

            if (pmap.containsKey("sercet")){
                this.sercet = pmap.get("sercet");
            }
            
            if (pmap.containsKey("payUrl")){
                this.payUrl = pmap.get("payUrl");
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
        logger.info("{QIANYINGPay}千应扫码支付开始==============start==============");
        try {
            //封装支付请求参数
            Map<String,String> map = sealRequest(payEntity);
            String mobile = payEntity.getMobile();
            String username = payEntity.getUsername();
            Double amount = payEntity.getAmount();
            String orderNo = payEntity.getOrderNo();
            return sealResponse(map, mobile, username, amount, orderNo);
        } catch (Exception e) {
            logger.info("{QIANYINGPay}千应扫码支付异常:"+e.getMessage());
            return PayUtil.returnPayJson("error", "2", "请求失败", "", 0, "", "");
        }
    }
    
    /**
     * 
     * @Description 封装支付请求参数
     * @param payEntity
     * @return
     * @throws Exception
     */
    private Map<String,String> sealRequest(PayEntity payEntity) throws Exception{
        try {
          //订单金额:单位元（人民币），正整数（不能带小数，最小支付金额为1）
            String amount = new DecimalFormat("##").format(payEntity.getAmount());
            Map<String,String> map = new HashMap<>();
            map.put("uid",uid);
            map.put("type",payEntity.getPayCode());
            map.put("m",amount);
            map.put("orderid",payEntity.getOrderNo());
            map.put("callbackurl",callbackurl);
            //待签名串
            String signStr = "uid="+uid+"&type="+payEntity.getPayCode()+"&m="+amount+"&orderid="+payEntity.getOrderNo()+"&callbackurl="+callbackurl+sercet;
            logger.info("{QIANYINGPay}千应支付待签名串:"+signStr);
            //进行MD5加密签名,32位小写
            String sign = MD5Util.convert(signStr);
            logger.info("{QIANYINGPay}千应支付生成签名串:"+sign);
            map.put("uuid", payEntity.getuId());//系统用户ID
            map.put("sign", sign);//签名
            map.put("gofalse",payEntity.getRefererUrl());//同步回调地址
            map.put("gotrue",payEntity.getRefererUrl());//同步回调地址
            map.put("gofalse",callbackurl);//同步回调地址
            map.put("gotrue",callbackurl);//同步回调地址
            map.put("charset",CHARSET_UTF_8);
            map.put("token",System.currentTimeMillis()+"");//订单时间
            logger.info("{QIANYINGPay}千应支付支付请求参数:"+map.toString());
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("{QIANYINGPay}千应支付封装请求参数异常:"+e.getMessage());
            throw new Exception(e.getMessage());
        }
    }
    
    /**
     * 
     * @Description 封装支付返回结果
     * @param mobile
     * @return
     * @throws Exception
     */
    private JSONObject sealResponse(Map<String,String> data, String mobile,String username,Double amount,String orderNo) throws Exception{
        logger.info("{QIANYINGPay}千应支付发起第三方支付请求开始=============start======================");
        try {
            //封装请求参数
            StringBuffer sb = new StringBuffer();
            sb.append("uid=").append(data.get("uid"));
            sb.append("&type=").append(data.get("type"));
            sb.append("&m=").append(data.get("m"));
            sb.append("&orderid=").append(data.get("orderid"));
            sb.append("&callbackurl=").append(data.get("callbackurl"));
            sb.append("&sign=").append(data.get("sign"));
            sb.append("&gofalse=").append(data.get("gofalse"));
            sb.append("&gotrue=").append(data.get("gotrue"));
            sb.append("&=charset").append(data.get("charset"));
            sb.append("&token=").append(data.get("token"));
            sb.append("&uuid=").append(data.get("uuid"));
            
            String reqParams = sb.toString().trim();
            logger.info("发起支付请求参数字符串:"+reqParams);
            String response = HttpUtil.toPostForm(reqParams, payUrl);
            if(StringUtils.isBlank(response)){
                logger.info("{QIANYINGPay}调用第三方支付返回结果为空!");
                return PayUtil.returnPayJson("error", "2" , "支付请求失败!", username, amount, orderNo, "");
            }
            
            JSONObject json = JSONObject.fromObject(response);
            
            logger.info("{QIANYINGPay}解析调用第三方支付返回的结果串:"+json.toString());
            
            String status = json.getString("Status");//状态 0失败 1 成功
            
            if(status.equals("1")){
                //成功
                String QRTxt = json.getString("QRTxt");//二维码解析路径
                String QRImg = json.getString("QRImg");//二维码图片
                if(StringUtils.isBlank(mobile)){
                    return PayUtil.returnPayJson("success", "2","支付成功", username, amount, orderNo, QRImg);
                }
                //只有支付宝H5 没有微信H5 支付宝 101 微信 102
                if(data.get("type").equals("101")){
                    //支付宝
                    return PayUtil.returnPayJson("success", "4", "支付成功", username, amount, orderNo, QRTxt);
                }
            }
            return PayUtil.returnPayJson("error", "2", "支付请求失败", username, amount, orderNo, response);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("{QIANYINGPay}千应支付发起支付请求异常:"+e.getMessage());
            return PayUtil.returnPayJson("error", "2", "支付失败", username, amount, orderNo, "");
        }
    }
    
    public static void main(String[] args) {
        String src = "oid=QIANYwns201809221802361802364505&status=1&m=1.00372eec0992d64474a978936c1c407cfd";
        String signStr = MD5Util.convert(src).toUpperCase();
        String sourceSign = "0B4DDD21F1D2360A57A1B5E9BC965D4B";
        if(signStr.equals(sourceSign)){
           System.err.println("验签成功");
        }else{
            System.err.println("验签失败");
        }
    }
    
    /**
     * 回调
     */
    @Override
    public String callback(Map<String, String> data) {
        logger.info("[QIANYING]千应支付回调验签开始===========================START=====================");
        try {
            String sourceSign = data.get("sign").toUpperCase();
            logger.info("[QIANYING]千应支付回调原签名:"+sourceSign);
            //生成待签名串
            String signStr = "oid="+data.get("oid")+"&status="+data.get("status")+"&m="+data.get("m")+sercet;
            logger.info("[QIANYING]千应支付回调生成待签名串:"+signStr);
            String sign = MD5Util.convert(signStr).toUpperCase();
            logger.info("[QIANYING]千应支付回调验签MD5加密串:"+sign);
            if(sign.equals(sourceSign)) return "success";
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[QIANYING]千应支付回调验签异常:"+e.getMessage());
        }
        return "";
    }
}
