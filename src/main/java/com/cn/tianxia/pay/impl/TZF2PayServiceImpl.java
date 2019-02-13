package com.cn.tianxia.pay.impl;

import com.cn.tianxia.common.PayConstant;
import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ClassName: TZF2PayServiceImpl
 * @Description: 通支付2
 * @Author: Zed
 * @Date: 2018-12-30 09:51
 * @Version:1.0.0
 **/

public class TZF2PayServiceImpl implements PayService {
    private final static Logger logger = LoggerFactory.getLogger(TZF2PayServiceImpl.class);

    /**
     * 支付地址
     **/
    private String api_url = "http://39.108.15.221:8188/cloud/cloudplatform/api/trade.html";

    /**
     * 商户号
     **/
    private String merchantNo = "00001006012";

    /**
     * md5key
     **/
    private String md5Key = "b33443ad68a745b69f1a055b6ea7e9f";

    /**
     * notifyUrl
     **/
    private String notifyUrl = "http://txw.tx8899.com/QUC/Notify/TZF2Notify.do";

    public TZF2PayServiceImpl(Map<String, String> pmap) {
        if (pmap != null) {
            if (pmap.containsKey("api_url")) {
                this.api_url = pmap.get("api_url");
            }
            if (pmap.containsKey("merchantNo")) {
                this.merchantNo = pmap.get("merchantNo");
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
        try {
            Map<String, String> paramsMap = sealRequest(payEntity);
            String sign = generatorSign(paramsMap);
            paramsMap.put("sign", sign);
            logger.info("[TZF2]通支付2请求参数:" + JSONObject.fromObject(paramsMap).toString());
            String res = HttpUtils.toPostForm(paramsMap, api_url);

            if (StringUtils.isBlank(res)) {
                logger.error("[TZF2]通支付2扫码支付请求异常,返回结果为空!");
                return PayResponse.error("[TZF2]通支付2扫码支付请求异常,返回结果为空!");
            }
            logger.info("[TZF2]通支付2响应参数字符:" + res);
            JSONObject resposeJson = JSONObject.fromObject(res);

            if (resposeJson.containsKey("resultCode") && "0".equals(resposeJson.getString("resultCode"))) {

                String qrCodeUrl = resposeJson.getString("payUrl");
                if (StringUtils.isNotBlank(payEntity.getMobile())) {
                    return PayResponse.sm_link(payEntity, qrCodeUrl, "下单成功");
                }
                return PayResponse.sm_qrcode(payEntity, qrCodeUrl, "下单成功");
            }
            return PayResponse.error("[TZF2]通支付2扫码支付下单失败:" + resposeJson.getString("returnMsg"));

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[TZF2]通支付2扫码支付下单失败:{}", e.getMessage());
            return PayResponse.error("[TZF2]通支付2扫码支付下单失败:" + e.getMessage());
        }
    }

    @Override
    public String callback(Map<String, String> data) {
        String sourceSign = data.remove("sign");
        if (StringUtils.isBlank(sourceSign)) {
            logger.info("[TZF2]通支付2回调验签失败：回调签名为空！");
            return "fail";
        }
        String localSign;
        try {
            localSign = generatorSign(data);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[TZF2]通支付2签名异常：" + e.getMessage());
            return "fail";
        }

        logger.info("本地签名:" + localSign + "      服务器签名:" + sourceSign);
        if (sourceSign.equalsIgnoreCase(localSign)) {
            return "success";
        }
        return "fail";
    }

    /**
     * @param payEntity
     * @return
     */
    private Map<String, String> sealRequest(PayEntity payEntity) throws Exception {
        logger.info("[TZF2]通支付2封装请求参数开始=====================START==================");
        try {
            //创建支付请求参数存储对象
            Map<String, String> data = new HashMap<>();
            //订单金额
            String amount = new DecimalFormat("0.00").format(payEntity.getAmount());//交易金额 分为单位

            data.put("tradeType","cs.pay.submit");
            data.put("version",	"1.5");
            data.put("channel",	payEntity.getPayCode());//	支付使用的第三方支付类型，见附件“7.1支付类型”
            data.put("mchId",merchantNo);//	是	String(32)	由平台分配的商户号
            data.put("body","top_up"); //	是	String(128)	商品或支付单简要描述
            data.put("outTradeNo",payEntity.getOrderNo());//	是	String(32)	商户系统内部的订单号,32个字符内、可包含字母, 确保在商户系统唯一
            data.put("amount",amount);//	是	Number

            data.put("timePaid",new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));//	否	String(14)	订单创建时间，格式为yyyyMMddHHmmss，如2009年12月25日9点10分10秒表示为20091225091010
            if (PayConstant.CHANEL_WX.equals(payEntity.getPayType()) && StringUtils.isNotBlank(payEntity.getMobile())) {
                //wxH5
                data.put("mwebType","");//	否	String	应用场景类型；支付类型wxH5该参数有效且必填;取值范围：IOS/Android/WAP
                data.put("mwebName","");//	否	String	支付类型wxH5该参数有效且必填;
//            取值规则如下：
//            IOS：应用在App Store中唯一应用名；
//            Android：应用在一台设备上的唯一标识在manifest文件里面的声明
//            WAP：WAP网站名
                data.put("mwebId","");//	否	String	支付类型wxH5该参数有效且必填;
//            IOS：IOS应用唯一标识
//            Android：应用在安卓分发市场中的应用名
//            WAP：WAP网站的首页URL
                data.put("callbackUrl",payEntity.getRefererUrl());//	否	String(500)	支付成功跳转路径；form表单形式提交商户后台；参数参考交易详细查询;
            }
            if (PayConstant.CHANEL_KJ.equals(payEntity.getPayType())) {
                data.put("cardInfo","");//	 否	String	支付类型qpay该参数有效且必填;
                data.put("callbackUrl",payEntity.getRefererUrl());
                data.put("bankType","");//	否	String 支付类型gateway、qpay该参数有效且必填;银行类型 参见 “7.4银行类型”
            }
            data.put("notifyUrl",notifyUrl);//	否	String(500)	支付完成后结果通知url；参数参考交易详细查询;

            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[TZF2]通支付2封装请求参数异常:", e.getMessage());
            throw new Exception("封装支付请求参数异常!");
        }
    }

    /**
     * @param data
     * @return
     * @throws Exception
     * @Description 生成支付签名串
     */
    public String generatorSign(Map<String, String> data) throws Exception {
        logger.info("[TZF2]通支付2生成支付签名串开始==================START========================");
        try {
            //不为验签的字段为:orderCode,sign,pay_number,remark,channel_code
            StringBuilder sb = new StringBuilder();
            SortedMap<String, String> sortedMap = new TreeMap<>(data);
            for (String key : sortedMap.keySet()) {
                if (StringUtils.isBlank(sortedMap.get(key)) || "sign".equalsIgnoreCase(key)) {
                    continue;
                }
                sb.append(key).append("=").append(sortedMap.get(key)).append("&");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append(md5Key);
            logger.info("[TZF2]通支付2待签名字符:" + sb.toString());
            String sign = MD5Utils.md5toUpCase_32Bit(sb.toString());
            if (StringUtils.isBlank(sign)) {
                logger.error("[TZF2]通支付2生成签名串为空！");
                return null;
            }
            logger.info("[TZF2]通支付2生成加密签名串:" + sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[TZF2]通支付2生成支付签名串异常:" + e.getMessage());
            throw new Exception("生成支付签名串异常!");
        }
    }

    public static void main(String[] args) {
        PayEntity entity = new PayEntity();
        entity.setPayCode("alipayQR");
        entity.setAmount(100);
        entity.setRefererUrl("http://localhost:85/JJF");
        entity.setOrderNo("TZF2bl1123450023");
        TZF2PayServiceImpl service = new TZF2PayServiceImpl(null);
        service.smPay(entity);
    }
}
