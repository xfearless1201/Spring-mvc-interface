package com.cn.tianxia.pay.impl;

import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.MapUtils;
import com.cn.tianxia.pay.utils.YEEUtils;

import net.sf.json.JSONObject;

/**
 * @ClassName YEEPayServiceImpl
 * @Description 易宝支付
 * @author Hardy
 * @Date 2018年12月22日 下午5:01:13
 * @version 1.0.0
 */
public class YEEPayServiceImpl implements PayService {

    private final static Logger logger = LoggerFactory.getLogger(YEEPayServiceImpl.class);

    private String merno;// 商户号

    private String payUrl;// 支付地址

    private String notifyUrl;// 回调地址

    private String secret;// 秘钥

    private String priKey;// 私钥

    private String pubKey;// 公钥

    // 构造器,初始化参数
    public YEEPayServiceImpl(Map<String, String> data) {
        if (MapUtils.isNotEntity(data)) {
            if (data.containsKey("merno")) {
                this.merno = data.get("merno");
            }
            if (data.containsKey("payUrl")) {
                this.payUrl = data.get("payUrl");
            }
            if (data.containsKey("notifyUrl")) {
                this.notifyUrl = data.get("notifyUrl");
            }
            if (data.containsKey("secret")) {
                this.secret = data.get("secret");
            }
            if (data.containsKey("priKey")) {
                this.priKey = data.get("priKey");
            }
            if (data.containsKey("pubKey")) {
                this.pubKey = data.get("pubKey");
            }
        }
    }

    /**
     * 网银支付
     */
    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        logger.info("[YEE]易宝支付网银支付开始===================START==========================");
        try {
            // 获取支付请求参数
            Map<String, String> data = sealRequest(payEntity);
            // 生成MD5加密串
            String sign = generatorSgin(data);
            data.put("sig", sign);
            logger.info("[YEE]易宝支付网银支付生成MD5加密后的请求报文:{}", JSONObject.fromObject(data).toString());
            // 进行请求参数公钥加密
            // 公钥加密、BASE64位加密、URL编码加密并拼接商户号和版本号
            byte[] dataStr = YEEUtils.encryptByPublicKey(YEEUtils.mapToJson(data).getBytes(YEEUtils.CHARSET), pubKey);
            String param = new Base64().encodeToString(dataStr);
            String reqParam = "data=" + URLEncoder.encode(param, YEEUtils.CHARSET) + "&account=" + data.get("account")
                    + "&version=" + data.get("version");
            logger.info("[YEE]易宝支付网银支付请求参数报文:{}", reqParam);
            // 发起HTTP支付请求
            String response = YEEUtils.request(payUrl, reqParam);
            if (StringUtils.isBlank(response)) {
                logger.info("[YEE]易宝支付网银支付发起HTTP请求无响应结果");
                return PayResponse.wy_write("[YEE]易宝支付网银支付发起HTTP请求无响应结果");
            }
            logger.info("[YEE]易宝支付网银支付发起HTTP请求响应结果:{}", response);

            // 解析响应结果
            JSONObject jsonObject = JSONObject.fromObject(response);
            if (jsonObject.containsKey("flag") && "00".equals(jsonObject.getString("flag"))) {
                // 创建订单成功后验签
                String sSign = jsonObject.getString("sig");
                jsonObject.remove("sig");
                // 生成响应结果签名
                String rSign = YEEUtils.MD5(jsonObject.toString() + secret, YEEUtils.CHARSET);
                if (sSign.equals(rSign)) {
                    // 支付成功
                    String payUri = jsonObject.getString("payUri");
                    String html = generatorHtml(payUri);
                    return PayResponse.wy_write(html);
                }
            }
            return PayResponse.wy_write("下单失败:" + response);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[YEE]易宝支付网银支付异常:{}", e.getMessage());
            return PayResponse.wy_write("[YEE]易宝支付网银支付异常");
        }
    }

    @Override
    public JSONObject smPay(PayEntity payEntity) {
        logger.info("[YEE]易宝支付扫码支付开始===================START==========================");
        try {
            // 获取支付请求参数
            Map<String, String> data = sealRequest(payEntity);
            // 生成MD5加密串
            String sign = generatorSgin(data);
            data.put("sig", sign);
            logger.info("[YEE]易宝支付扫码支付生成MD5加密后的请求报文:{}", JSONObject.fromObject(data).toString());
            // 进行请求参数公钥加密
            // 公钥加密、BASE64位加密、URL编码加密并拼接商户号和版本号
            byte[] dataStr = YEEUtils.encryptByPublicKey(YEEUtils.mapToJson(data).getBytes(YEEUtils.CHARSET), pubKey);
            String param = new Base64().encodeToString(dataStr);
            String reqParam = "data=" + URLEncoder.encode(param, YEEUtils.CHARSET) + "&account=" + data.get("account")
                    + "&version=" + data.get("version");
            logger.info("[YEE]易宝支付扫码支付请求参数报文:{}", reqParam);
            // 发起HTTP支付请求
            String response = YEEUtils.request(payUrl, reqParam);
            if (StringUtils.isBlank(response)) {
                logger.info("[YEE]易宝支付扫码支付发起HTTP请求无响应结果");
                return PayResponse.error("[YEE]易宝支付扫码支付发起HTTP请求无响应结果");
            }
            logger.info("[YEE]易宝支付扫码支付发起HTTP请求响应结果:{}", response);

            // 解析响应结果
            JSONObject jsonObject = JSONObject.fromObject(response);
            if (jsonObject.containsKey("flag") && "00".equals(jsonObject.getString("flag"))) {
                // 创建订单成功后验签
                String sSign = jsonObject.getString("sig");
                jsonObject.remove("sig");
                // 生成响应结果签名
                String rSign = YEEUtils.MD5(jsonObject.toString() + secret, YEEUtils.CHARSET);
                if (sSign.equals(rSign)) {
                    // 支付成功
                    String payUri = jsonObject.getString("payUri");
                    return PayResponse.sm_link(payEntity, payUri, "下单成功");
                }
            }
            return PayResponse.error("下单失败:" + response);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[YEE]易宝支付扫码支付异常:{}", e.getMessage());
            return PayResponse.error("[YEE]易宝支付支付异常");
        }
    }

    @Override
    public String callback(Map<String, String> data) {
        logger.info("[YEE]易宝支付回调验签开始===============START=====================");
        try {
            String sourceSign = data.get("sig");
            logger.info("[YEE]易宝支付回调原签名串:{}", sourceSign);
            String signStr = YEEUtils.mapToJson(data);
            String sign = YEEUtils.MD5(signStr + secret, YEEUtils.CHARSET);
            if (sourceSign.equals(sign))
                return "success";
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[YEE]易宝支付回调验签异常:{}", e.getMessage());
        }
        return "faild";
    }

    /**
     * @Description 组装支付请求参数
     * @param entity
     * @return
     * @throws Exception
     */
    private Map<String, String> sealRequest(PayEntity entity) throws Exception {
        logger.info("[YEE]易宝支付组装支付请求参数开始=================START=================");
        try {
            Map<String, String> data = new HashMap<>();
            String amount = new DecimalFormat("##").format(entity.getAmount() * 100);
            data.put("account", merno);// 商户账号,下发的商户账号
            data.put("amount", amount);// 金额（分）,100=1元人民币
            data.put("backUri", notifyUrl);// 回调uri
            data.put("skipUri", entity.getRefererUrl());// 跳显uri
            data.put("charset", "UTF-8");// 字符编码UTF-8
            data.put("orderId", entity.getOrderNo());// 订单号
            data.put("random", YEEUtils.randomStr(3));// 随机参数
            data.put("trade", "TOP-UP");// 交易名称
            data.put("type", entity.getPayCode());// 支付类型
            data.put("version", "V1.1");// 版本号
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[YEE]易宝支付组装支付请求参数异常:{}", e.getMessage());
            throw new Exception("[YEE]易宝支付组装支付请求参数异常");
        }
    }

    /**
     * @Description 生成签名串
     * @param data
     * @return
     * @throws Exception
     */
    private String generatorSgin(Map<String, String> data) throws Exception {
        logger.info("[YEE]易宝支付生成签名串开始===================START================");
        try {
            // 排序
            Map<String, String> sortmap = MapUtils.sortByKeys(data);
            // 生成JSON串
            String signStr = YEEUtils.mapToJson(sortmap);
            logger.info("[YEE]易宝支付生成待加密串:{}", signStr);
            String sign = YEEUtils.MD5(signStr + secret, YEEUtils.CHARSET);
            logger.info("[YEE]易宝支付生成加密串:{}", sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[YEE]易宝支付生成签名串异常:{}", e.getMessage());
            throw new Exception("[YEE]易宝支付生成签名串异常");
        }
    }

    /**
     * @Description 生成html
     * @param data
     * @return
     * @throws Exception
     */
    public static String generatorHtml(String data) throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append("<!DOCTYPE html>");
        sb.append("<html lang=\"en\">");
        sb.append("<head>");
        sb.append("<meta charset=\"UTF-8\">");
        sb.append("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">");
        sb.append("<title>天下支付</title>");
        sb.append("</head>");
        sb.append("<body>");
        sb.append("<a id=\"pay\" href=\"");
        sb.append(data);
        sb.append("\"target=\"_self\" rel=\"noreferrer\">");
        sb.append("</a>");
        // 写入js
        sb.append("<script type=\"text/javascript\">");
        sb.append("function run(){");
        sb.append("document.getElementById(\"pay\").click();");
        sb.append("} run();");
        sb.append("</script>");
        sb.append("</body>");
        sb.append("</html>");
        return sb.toString();
    }

}
