package com.cn.tianxia.pay.impl;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.pay.dc.util.HttpClientUtil;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.qyf.util.ToolKit;
import com.cn.tianxia.pay.service.PayService;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.*;

/**
 * @ClassName: TITIPayServiceImpl
 * @Description: TT支付
 * @Author: Zed
 * @Date: 2018-12-16 17:51
 * @Version:1.0.0
 **/

public class TITIPayServiceImpl implements PayService {
    private final static Logger logger = LoggerFactory.getLogger(TITIPayServiceImpl.class);

    /** 支付地址 **/
    private String api_url = "http://39.108.238.56:8080/qcms/api/qrcode";

    /** 商户号 **/
    private String clientId = "vbkh1";

    /** md5key **/
    private String md5Key = "nlI4mYlo0dLNzamXMZya";

    public TITIPayServiceImpl(Map<String, String> pmap) {
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
        DecimalFormat df = new DecimalFormat("0.00");
        String orderAmount = df.format(payEntity.getAmount());// 分为单位

        String payType = payEntity.getPayCode();// 支付方式

        String tradeNo = payEntity.getOrderNo();// 商户订单号

        Map<String, String> paramsMap = new HashMap<>();

        String sign = "";

        paramsMap.put("money", orderAmount);
        paramsMap.put("tradeNo", tradeNo);
        paramsMap.put("clientId", clientId);
        paramsMap.put("type", payType);
        paramsMap.put("urlType", "0");  //码类型：0-支付码，1-页面码；默认为支付码
        // 签名
        sign = ToolKit.MD5(buildPrePayParams(paramsMap, md5Key), "UTF-8");
        if (StringUtils.isBlank(sign)) {
            logger.error("[TTZF]踢踢支付扫码支付签名异常");
            return PayResponse.error("[TTZF]踢踢支付扫码支付签名异常");
        }
        paramsMap.put("sign", sign.toLowerCase());

        String res = "";
        try {
            String postJson = JSONObject.fromObject(paramsMap).toString();
            logger.info("[TTZF]踢踢支付请求参数:" + postJson);
            res = HttpClientUtil.doPost(api_url, postJson, "UTF-8", "application/json");

            logger.info("[TTZF]踢踢支付响应参数字符:" + res);
            JSONObject resposeJson = JSONObject.fromObject(res);

            if (resposeJson.containsKey("code") && "0".equals(resposeJson.getString("code"))) {
                String qrCodeUrl = resposeJson.getString("msg");
                if (StringUtils.isNotBlank(payEntity.getMobile())) {
                    return PayResponse.sm_link(payEntity,qrCodeUrl,"下单成功");
                }
                return PayResponse.sm_qrcode(payEntity,qrCodeUrl,"下单成功");
            }
            return PayResponse.error("[TTZF]踢踢扫码支付下单失败:"+ resposeJson.getString("msg"));
        } catch (Exception e) {
            e.printStackTrace();
            return PayResponse.error("[TTZF]踢踢扫码支付下单失败"+e.getMessage());
        }
    }

    /**
     * @Description (TODO这里用一句话描述这个方法的作用)
     * @param payParams
     * @param md5Key
     */
    public static String buildPrePayParams(Map<String, String> payParams, String md5Key) {
        StringBuilder sb = new StringBuilder();
        sb.append(payParams.get("clientId")).append(payParams.get("money")).append(payParams.get("type"))
                .append(payParams.get("tradeNo")).append(md5Key);
        logger.info("[TTZF]踢踢待签名字符:" + sb.toString());
        return sb.toString();
    }

    @Override
    public String callback(Map<String, String> map) {
        String serverSign = map.remove("sign");

        String localSign = callbackSign(map);

        logger.info("本地签名:" + localSign + "      服务器签名:" + serverSign);
        if (serverSign.equalsIgnoreCase(localSign)) {
            return "success";
        }
        return "";
    }

    public String callbackSign(Map<String,String> map) {
        StringBuffer sb = new StringBuffer();
        sb.append("type=").append(map.get("type"))
                .append("&money=").append(map.get("money"))
                .append("&tradeNo=").append(map.get("tradeNo"))
                .append("&dt=").append(map.get("dt"))
                .append("&clientId=").append(map.get("clientId"))
                .append("&token=").append(md5Key);
        return ToolKit.MD5(sb.toString(),"UTF-8");
    }

    public static void main(String[] args) {
        PayEntity testPay = new PayEntity();
        testPay.setAmount(0.01);
        testPay.setOrderNo("mk000000004567");
        testPay.setPayCode("1");
        testPay.setMobile("mobile");
        TITIPayServiceImpl service = new TITIPayServiceImpl(null);
        service.smPay(testPay);
    }
}
