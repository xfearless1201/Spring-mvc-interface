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
 * @ClassName XDPayServiceImpl
 * @Description 兄弟支付
 * @author Hardy
 * @Date 2018年10月13日 下午1:50:29
 * @version 1.0.0
 */
public class XDPayServiceImpl implements PayService {

    // 日志
    private static final Logger logger = LoggerFactory.getLogger(XDPayServiceImpl.class);
    private String merchantNo;// 平台编号
    private String notifyUrl;// 回调地址
    private String payUrl;// PC端支付地址
    private String secret;// 秘钥

    // 构造器,初始化参数
    public XDPayServiceImpl(Map<String, String> data) {
        if (data != null && !data.isEmpty()) {
            if (data.containsKey("merchantNo")) {
                this.merchantNo = data.get("merchantNo");
            }
            if (data.containsKey("notifyUrl")) {
                this.notifyUrl = data.get("notifyUrl");
            }
            if (data.containsKey("payUrl")) {
                this.payUrl = data.get("payUrl");
            }
            if (data.containsKey("secret")) {
                this.secret = data.get("secret");
            }
        }
    }

    /**
     * 网银支付
     */
    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        logger.info("[XD]兄弟支付网银支付开始===============START==================");
        try {
            // 获取支付请求参数
            Map<String, String> data = sealRequest(payEntity,1);
            //支付请求参数生成规则:
            //paramsA:为所有参与支付的请求参数,按首字母由ASCII码从小到大排列拼接字符串
            //签名串:sign
            //reqParams = paramsA + sign
            //生成支付请求参数
//            String paramsA = formatMapToString(data);
            //生成签名串
            String sign = generatorSign(data);
            data.put("sign", sign);
            //最终支付请求参数
//            String reqParams = paramsA+sign;
            logger.info("[XD]兄弟支付请求报文:"+JSONObject.fromObject(data).toString());
            String response = HttpUtils.generatorForm(data, payUrl+"/gateway");
            return PayUtil.returnWYPayJson("success", "form",response, payEntity.getPayUrl(), "pay");
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[XD]兄弟支付网银支付异常:"+e.getMessage());
            return PayUtil.returnWYPayJson("error", "", "", "", "");
        }
    }

    /**
     * 扫码支付
     */
    @Override
    public JSONObject smPay(PayEntity payEntity) {
        logger.info("[XD]兄弟支付扫码支付开始===============START==================");
        try {
            //获取支付请求参数
            Map<String, String> data = sealRequest(payEntity,0);
            //支付请求参数生成规则:
            //paramsA:为所有参与支付的请求参数,按首字母由ASCII码从小到大排列拼接字符串
            //签名串:sign
//            reqParams = paramsA + sign
            //生成支付请求参数
            String paramsA = formatMapToString(data);
            //生成签名串
            String sign = generatorSign(data);
            data.put("sign", sign);
            
            if(payEntity.getPayCode().equalsIgnoreCase("quickpay")){
                String formStr = HttpUtils.generatorForm(data, payUrl+"/"+payEntity.getPayCode());
                return PayUtil.returnPayJson("success","1","下单成功!","",0,"",formStr);
            }
            //最终支付请求参数
            String reqParams = paramsA+"sign="+sign;
            logger.info("[XD]兄弟支付请求报文:"+reqParams);
            String response = HttpUtils.toPostIO(reqParams, payUrl+"/"+payEntity.getPayCode());
            if(StringUtils.isBlank(response)){
                logger.info("[XD]兄弟支付发起HTTP请求无响应结果!");
                return PayUtil.returnPayJson("error", "2", "下单失败:发起HTTP请求无响应结果!",payEntity.getUsername(),payEntity.getAmount(),payEntity.getOrderNo(),response);
            }
            logger.info("[XD]兄弟支付发起HTTP请求响应结果:"+response);
            //解析响应结果
            JSONObject jsonObject = JSONObject.fromObject(response);
            if(jsonObject.containsKey("code") && jsonObject.getString("code").equals("00000")){
                //下单成功
                if(jsonObject.containsKey("data") && StringUtils.isNotBlank(jsonObject.getString("data"))){
                    JSONObject jsonData = JSONObject.fromObject(jsonObject.getString("data"));
                    if(jsonData.containsKey("payUrl")){
                        String payUrl = jsonData.getString("payUrl").replace("\\\\", "");
                        if(StringUtils.isBlank(payEntity.getMobile())){
                            //PC端
                            return PayUtil.returnPayJson("success","2","下单成功",payEntity.getUsername(),payEntity.getAmount(),payEntity.getOrderNo(),payUrl);
                        }
                        
                        //移动端
                        return PayUtil.returnPayJson("success","4","下单成功",payEntity.getUsername(),payEntity.getAmount(),payEntity.getOrderNo(),payUrl);
                    }
                }
            }
            //解析响应结果
            return PayUtil.returnPayJson("error", "2", "下单失败", payEntity.getUsername(), payEntity.getAmount(), payEntity.getOrderNo(), response);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[XD]兄弟支付扫码支付异常:"+e.getMessage());
            return PayUtil.returnPayJson("error", "2", "扫码支付异常!", null, 0, null, null);
        }
    }

    /**
     * 回调
     */
    @Override
    public String callback(Map<String, String> data) {
        logger.info("[XD]兄弟支付回调验签开始==============START===================");
        try {
            //获取原串
            String sourceSign = data.get("sign");
            logger.info("[XD]兄弟支付回调验签原签名串:"+sourceSign);
            //生成回调验签签名串
            String sign = generatorSign(data);
            logger.info("[XD]兄弟支付回调验签生成签名串:"+sign);
            if(sourceSign.equalsIgnoreCase(sign)) return "success";
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[XD]兄弟支付回调验签异常:"+e.getMessage());
        }
        return "faild";
    }

    /**
     * @Description 封装支付请求参数
     * @param payEntity
     * @param type
     * @return
     * @throws Exception
     */
    private Map<String, String> sealRequest(PayEntity entity, Integer type) throws Exception {
        logger.info("[XD]兄弟支付组装请求参数开始==================START====================");
        try {
            // 创建存储参数对象
            Map<String, String> data = new HashMap<>();
            String amount = new DecimalFormat("0.00").format(entity.getAmount());
            data.put("merchantNo", merchantNo);// 商户id,由兄弟支付分配
            if(entity.getPayCode().equalsIgnoreCase("quickpay")){
                //支付宝支付
                data.put("userId", entity.getuId());// 商户的用户id，该值需要在商户系统内唯一
            }
            if(entity.getPayCode().equalsIgnoreCase("alipay")){
                //阿里支付
                data.put("body", "TOP-UP");//支付内容
                data.put("returnType", "2");//请求的返回方式：1-页面，2-Url
            }
            data.put("amount", amount);// 单位元（人民币）
            data.put("orderNo", entity.getOrderNo());// 商户订单号
            data.put("notifyUrl", notifyUrl);// 下行异步通知地址
            data.put("frontUrl", entity.getRefererUrl());// 下行同步通知地址
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[XD]兄弟支付组装请求参数异常:" + e.getMessage());
            throw new Exception("组装支付请求参数异常!");
        }
    }

    /**
     * @Descriptions 生成签名串
     * @param data
     * @return
     * @throws Exception
     */
    private String generatorSign(Map<String, String> data) throws Exception {
        logger.info("[XD]兄弟支付生成签名串开始=====================START===================");
        try {
            // 签名规则：待签名数据为表1 中所有加入签名的参数以及商户密钥，签名顺序为参数首字母由ASCII码从小到大排列，之后对字符串进行小写转换。具体MD5 签名源串及格式如下：
            Map<String, String> treemap = new TreeMap<>(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }
            });
            treemap.putAll(data);
            StringBuffer sb = new StringBuffer();
            Iterator<String> iterator = treemap.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                String val = treemap.get(key);
                if (StringUtils.isBlank(val) || key.equalsIgnoreCase("sign"))
                    continue;
                sb.append("&").append(key).append("=").append(val);
            }
            // 生成待签名串
            String lowercaseStr = sb.toString().replaceFirst("&", "").toLowerCase();
            String signStr = lowercaseStr + secret;
            logger.info("[XD]兄弟支付生成待签名串:" + signStr);
            String sign = MD5Utils.md5toUpCase_32Bit(signStr).toLowerCase();
            logger.info("[XD]兄弟支付生成加密签名串:" + sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[XD]兄弟支付生成签名串异常:" + e.getMessage());
            throw new Exception("生成签名串异常!");
        }
    }
    
    /**
     * 
     * @Description 生成请求参数
     * @param data
     * @return
     * @throws Exception
     */
    private String formatMapToString(Map<String,String> data) throws Exception{
        logger.info("[XD]兄弟支付Map类型参数转换成String类型参数开始=====================START========================");
        try {
            Map<String, String> treemap = new TreeMap<>(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }
            });
            treemap.putAll(data);
            StringBuffer sb = new StringBuffer();
            Iterator<String> iterator = treemap.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                String val = treemap.get(key);
                if (StringUtils.isBlank(val) || key.equalsIgnoreCase("sign"))
                    continue;
                sb.append(key).append("=").append(val).append("&");
            }
            return sb.toString();    
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[XD]兄弟支付Map类型参数转换成String类型参数异常:"+e.getMessage());
            throw new Exception("Map类型参数转换成String类型参数异常!");
        }
    }
    
}
