package com.cn.tianxia.pay.impl;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.pay.dc.util.HttpClientUtil;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.qyf.util.ToolKit;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: TT2PayServiceImpl
 * @Description: 踢踢支付2
 * @Author: Zed
 * @Date: 2018-12-19 11:19
 * @Version:1.0.0
 **/

public class TT2PayServiceImpl implements PayService {

    private final static Logger logger = LoggerFactory.getLogger(TT2PayServiceImpl.class);

    /** 支付地址 **/
    private String api_url = "http://112.74.179.226:8080/qcms/api/qrcode";

    /** 商户号 **/
    private String clientId = "vbkh1";

    /** md5key **/
    private String md5Key = "HIlnu3YPLZJNO3fmvrJi";

    /** notifyUrl **/
    private String notifyUrl = "http://txw8899.com/XJC/Notify/TT2Notify.do";

    public TT2PayServiceImpl(Map<String, String> pmap) {
        if (pmap != null) {
            if (pmap.containsKey("api_url")) {
                this.api_url = pmap.get("api_url");
            }
            if (pmap.containsKey("clientId")) {
                this.clientId = pmap.get("clientId");
            }
            if (pmap.containsKey("md5Key")) {
                this.md5Key = pmap.get("md5Key");
            }
            if (pmap.containsKey("notifyUrl")) {
                this.notifyUrl = pmap.get("notifyUrl");
            }
        }
    }

    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        return null;
    }

    @Override
    public JSONObject smPay(PayEntity payEntity) {
        JSONObject r_json = Pay(payEntity);
        return r_json;
    }

    /**
     * @Description 二维码支付接口
     * @param payEntity
     * @return
     */
    public JSONObject Pay(PayEntity payEntity) {
        try {
            Map<String,String> paramsMap = sealRequest(payEntity);
            String sign  = generatorSign(paramsMap);
            paramsMap.put("sign",sign);
            String postJson = JSONObject.fromObject(paramsMap).toString();
            logger.info("[TT2]踢踢支付2请求参数:" + postJson);
            String res = HttpUtils.toPostJson(postJson,api_url);

            if (StringUtils.isBlank(res)) {
                logger.error("[TT2]踢踢支付2扫码支付请求异常,返回结果为空!");
                return PayResponse.error("[TT2]踢踢支付2扫码支付请求异常,返回结果为空!");
            }
            logger.info("[TT2]踢踢支付响应参数字符:" + res);
            JSONObject resposeJson = JSONObject.fromObject(res);

            if (resposeJson.containsKey("code") && "0".equals(resposeJson.getString("code"))) {
                String qrCodeUrl = resposeJson.getString("msg");
                if (StringUtils.isNotBlank(payEntity.getMobile())) {
                    return PayResponse.sm_link(payEntity,qrCodeUrl,"下单成功");
                }
                return PayResponse.sm_qrcode(payEntity,qrCodeUrl,"下单成功");
            }
            return PayResponse.error("[TT2]踢踢支付2扫码支付下单失败:"+ resposeJson.getString("msg"));
        } catch (Exception e) {
            e.printStackTrace();
            return PayResponse.error("[TT2]踢踢支付2扫码支付下单失败"+e.getMessage());
        }
    }

    /**
     *
     * @param payEntity
     * @return
     */
    private Map<String,String> sealRequest(PayEntity payEntity) throws Exception {
        logger.info("[TT2]踢踢支付2封装请求参数开始=====================START==================");
        try {
            //创建支付请求参数存储对象
            Map<String,String> data = new HashMap<>();
            //订单金额
            String amount = new DecimalFormat("0.00").format(payEntity.getAmount());//交易金额 分为单位
            data.put("money", amount);
            data.put("tradeNo", payEntity.getOrderNo());
            data.put("clientId", clientId);
            data.put("type", payEntity.getPayCode());
            data.put("pushUrl", notifyUrl);

            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[TT2]踢踢支付2封装请求参数异常:"+e.getMessage());
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
        logger.info("[HYZF]虎云支付生成支付签名串开始==================START========================");
        try {

            StringBuilder sb = new StringBuilder();
            sb.append(data.get("clientId")).append(data.get("money")).append(data.get("type"))
                    .append(data.get("tradeNo")).append(md5Key);
            logger.info("[TT2ZF]踢踢支付2待签名字符:" + sb.toString());
            String sign = MD5Utils.md5toUpCase_32Bit(sb.toString());
            if (StringUtils.isBlank(sign)) {
                logger.error("[HYZF]虎云支付生成签名串为空！");
                return null;
            }
            logger.info("[HYZF]虎云支付生成加密签名串:"+sign.toLowerCase());
            return sign.toLowerCase();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[HYZF]虎云支付生成支付签名串异常:"+e.getMessage());
            throw new Exception("生成支付签名串异常!");
        }
    }

    @Override
    public String callback(Map<String, String> map) {

        String sourceSign = map.remove("sign");
        if (StringUtils.isBlank(sourceSign)) {
            logger.info("[TT2]踢踢支付2回调验签失败：回调签名为空！");
            return "fail";
        }
        String localSign = callbackSign(map);

        logger.info("本地签名:" + localSign + "      服务器签名:" + sourceSign);
        if (sourceSign.equalsIgnoreCase(localSign)) {
            return "success";
        }
        return "fail";
    }

    public String callbackSign(Map<String,String> map) {
        StringBuffer sb = new StringBuffer();
        sb.append("type=").append(map.get("type"))
                .append("&money=").append(map.get("money"))
                .append("&tradeNo=").append(map.get("tradeNo"))
                .append("&dt=").append(map.get("dt"))
                .append("&clientId=").append(map.get("clientId"))
                .append("&token=").append(md5Key);
        try {
            logger.info("[TT2]踢踢支付2回调验签，待签名串:{}",sb.toString());
            return MD5Utils.md5toUpCase_32Bit(sb.toString());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        PayEntity testPay = new PayEntity();
        testPay.setAmount(0.01);
        testPay.setOrderNo("mk0000000056465");
        testPay.setPayCode("1");
        testPay.setMobile("mobile");
        TT2PayServiceImpl service = new TT2PayServiceImpl(null);
        service.smPay(testPay);
    }

}
