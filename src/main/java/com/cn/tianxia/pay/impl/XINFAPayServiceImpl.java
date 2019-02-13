/****************************************************************** 
 *
 *    Powered By tianxia-online. 
 *
 *    Copyright (c) 2018-2020 Digital Telemedia 天下网络 
 *    http://www.d-telemedia.com/ 
 *
 *    Package:     com.cn.tianxia.pay.impl 
 *
 *    Filename:    XINFAPayServiceImpl.java 
 *
 *    Description: TYC太阳城新接入支付鑫发支付
 *
 *    Copyright:   Copyright (c) 2018-2020 
 *
 *    Company:     天下网络科技 
 *
 *    @author: Elephone
 *
 *    @version: 1.0.0
 *
 *    Create at:   2018年08月17日 19:59 
 *
 *    Revision: 
 *
 *    2018/8/17 19:59 
 *        - first revision 
 *
 *****************************************************************/
package com.cn.tianxia.pay.impl;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.MapUtils;
import com.cn.tianxia.pay.xinfa.util.ToolKit;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Encoder;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @ClassName XINFAPayServiceImpl
 * @Description TYC太阳城新接入支付鑫发支付
 * @Author Elephone
 * @Date 2018年08月17日 19:59
 * @Version 1.0.0
 **/
public class XINFAPayServiceImpl implements PayService {
    private final static Logger logger = LoggerFactory.getLogger(XINFAPayServiceImpl.class);
    static String merchNo ;// 商户号
    static String key ;   // 签名MD5密钥,24位
    static String reqUrl ;// 支付地址
    static String version;
    static String goodsName;
    static String notifyUrl;
    static String PAY_PUBLIC_KEY;// 支付公钥
    public static String MECHA_PRIVATE_KEY;// 商家支付私钥

    public XINFAPayServiceImpl(Map<String, String> pmap) {
        if (pmap != null) {
            if (pmap.containsKey("merchNo")) {
                this.merchNo = pmap.get("merchNo");
            }
            if (pmap.containsKey("key")) {
                this.key = pmap.get("key");
            }
            if (pmap.containsKey("reqUrl")) {
                this.reqUrl = pmap.get("reqUrl");
            }
            if (pmap.containsKey("notifyUrl")) {
                this.notifyUrl = pmap.get("notifyUrl");
            }
            if (pmap.containsKey("version")) {
                this.version = pmap.get("version");
            }
            if (pmap.containsKey("goodsName")) {
                this.goodsName = pmap.get("goodsName");
            }
            if (pmap.containsKey("PAY_PUBLIC_KEY")) {
                this.PAY_PUBLIC_KEY = pmap.get("PAY_PUBLIC_KEY");
            }
            if (pmap.containsKey("MECHA_PRIVATE_KEY")) {
                this.MECHA_PRIVATE_KEY = pmap.get("MECHA_PRIVATE_KEY");
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
        logger.info("[XINFA]鑫发支付扫码支付开始=======================START=======================");
        try {
            //获取支付请求参数
            Map<String,String> metaSignMap = sealRequest(payEntity);
            //生成签名串
            String sign = generatorSign(metaSignMap);
            metaSignMap.put("sign", sign);
            String reqParam = getRequestParams(metaSignMap);
            String resultJsonStr = ToolKit.request(reqUrl, reqParam);
            if(org.apache.commons.lang3.StringUtils.isBlank(resultJsonStr)){
                logger.info("[XINFA]鑫发支付发起HTTP请求无响应结果");
                return PayResponse.error("鑫发支付发起HTTP请求无响应结果");
            }
            logger.info("[XINFA]鑫发支付发起HTTP请求响应报文:{}",resultJsonStr);
            //解析响应结果
            JSONObject r_json = JSONObject.fromObject(resultJsonStr);

            if(r_json.containsKey("stateCode") && "00".equals(r_json.getString("stateCode"))){
                logger.info("[XINFA]鑫发支付发起HTTP请求，下单成功!");
                String qrcodeUrl = r_json.getString("qrcodeUrl");
                if(org.apache.commons.lang3.StringUtils.isBlank(payEntity.getMobile())){
                    //PC端
                    return PayResponse.sm_qrcode(payEntity, qrcodeUrl, "[XINFA]鑫发支付下单请求成功!");
                }
                return PayResponse.sm_link(payEntity, qrcodeUrl, "[XINFA]鑫发支付下单请求成功!");
            }
            
            String msg = r_json.getString("msg");
            return PayResponse.error("[XINFA]鑫发支付下单请求失败:"+msg);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[XINFA]鑫发支付下单请求异常:{}",e.getMessage());
            return PayResponse.error(e.getMessage());
        }
    }

    /**
     * 回调
     */
    @Override
    public String callback(Map<String, String> metaMap) {
        String metaSign = metaMap.remove("sign");
        Map<String, String> infoMap = new TreeMap<String, String>();
        infoMap.putAll(metaMap);
        String jsonStr = ToolKit.mapToJson(infoMap);
        logger.info("验签内容signatureStr = " + jsonStr);
        String sign = ToolKit.MD5(jsonStr.toString() + key, ToolKit.CHARSET);
        if (!sign.equals(metaSign)) {
            logger.info("验签失败");
            return "fail";
        }
        logger.info("验签成功");
        return "success";
    }
    
    /**
     * 
     * @Description 封装支付请求参数
     * @param entity
     * @return
     * @throws Exception
     */
    private Map<String,String> sealRequest(PayEntity entity) throws Exception{
        logger.info("[XINFA]鑫发支付组装支付请求参数开始====================START========================");
        try {
            //创建存储对象
            Map<String,String> data = new TreeMap<String, String>();
            String price = new DecimalFormat("#").format(entity.getAmount()*100);//订单金额 ，单位为分
            data.put("orderNo", entity.getOrderNo());
            data.put("version", version);
            data.put("charsetCode", ToolKit.CHARSET);//
            data.put("randomNum", ToolKit.randomStr(4));// 4位随机数
            data.put("merchNo", merchNo);
            data.put("payType", entity.getPayCode());// WX:微信支付,ZFB:支付宝支付
            data.put("amount", price);// 单位:分
            data.put("goodsName", goodsName);// 商品名称：20位
            data.put("notifyUrl", notifyUrl);// 回调地址
            data.put("notifyViewUrl", entity.getRefererUrl());// 回显地址
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[XINFA]鑫发支付组装支付请求参数异常:{}",e.getMessage());
            throw new Exception("鑫发支付组装支付请求参数异常");
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
        logger.info("[XINFA]鑫发支付生成签名串开始=====================START========================");
        try {
            //生成待签名串
            String signStr = ToolKit.mapToJson(data);
            logger.info("[XINFA]鑫发支付生成待签名串:{}",signStr);
            //生成签名串
            String sign = ToolKit.MD5(signStr + key, ToolKit.CHARSET);// 32位 
            logger.info("[XINFA]鑫发支付生成加密签名串:{}",sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[XINFA]鑫发支付生成签名串异常:{}",e.getMessage());
            throw new Exception("鑫发支付生成签名串异常");
        }
    }
    
    /**
     * 
     * @Description 生成支付请求加密串
     * @param entity
     * @return
     * @throws Exception
     */
    private String getRequestParams(Map<String,String> data) throws Exception{
        logger.info("[XINFA]鑫发支付生成支付请求加密串开始=====================START=========================");
        try {
            byte[] dataStr = ToolKit.encryptByPublicKey(ToolKit.mapToJson(data).getBytes(ToolKit.CHARSET),
                    PAY_PUBLIC_KEY);
            String param = new BASE64Encoder().encode(dataStr);
            String reqParam = "data=" + URLEncoder.encode(param, ToolKit.CHARSET) + "&merchNo=" + data.get("merchNo");
            return reqParam;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[XINFA]鑫发支付生成支付请求加密串异常:{}",e.getMessage());
            throw new Exception("鑫发支付生成支付请求加密串异常");
        }
    }
}
