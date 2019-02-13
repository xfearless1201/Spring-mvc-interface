package com.cn.tianxia.pay.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.ly.util.HttpMethod;
import com.cn.tianxia.pay.ly.util.HttpSendModel;
import com.cn.tianxia.pay.ly.util.SimpleHttpResponse;
import com.cn.tianxia.pay.qgdl.util.HttpUtil;
import com.cn.tianxia.pay.qgdl.util.MD5Util;
import com.cn.tianxia.pay.service.PayService;

import net.sf.json.JSONObject;

/**
 * 全谷迪聊支付
 * 
 * @author hb
 * @date 2018-06-14
 */
public class QGDLPayServiceImpl implements PayService {

    private final static Logger logger = LoggerFactory.getLogger(QGDLPayServiceImpl.class);

    /** 支付地址 */
    private String payUrl;// ="http://www.payapibest.com/pay/gateway";
    /** 密钥 */
    private String md5Key;// = "61u7imtk0938aebzgfpvcw5l2nsqjdh4";
    /** 回调地址 */
    private String notify_url;// = "http://www.baidu.com";//回调地址
    /** 商户号 */
    private String mch_id;// = "2018061313";//商户id
    /** 订单主题 */
    private String subject;// = "pay";//订单主题

    public QGDLPayServiceImpl() {

    }

    public QGDLPayServiceImpl(Map<String, String> pmap) {
        JSONObject json = JSONObject.fromObject(pmap);
        this.payUrl = json.getString("payUrl");
        this.md5Key = json.getString("md5Key");
        this.notify_url = json.getString("notify_url");
        this.mch_id = json.getString("mch_id");
        this.subject = json.getString("subject");
    }

    public static void main(String[] args) {
        /**
         * {charset=UTF-8, mch_id=2018061313, nonce_str=c244d163a8e2420f94a7ff53cec84ff6,
         * out_trade_no=QGDL20180616163108716, result_code=0, service=PAY_UNION_QUICK,
         * sign=E8BE6FDADA3C9382630E315D9BA13AF6, sign_type=MD5, status=0, total_fee=2100, version=1.0}
         */
        Map<String, String> params = new TreeMap<>();
        params.put("charset", "UTF-8");
        params.put("mch_id", "2018061313");
        params.put("nonce_str", "c244d163a8e2420f94a7ff53cec84ff6");
        params.put("out_trade_no", "QGDL20180616163108716");
        params.put("result_code", "0");
        params.put("service", "PAY_UNION_QUICK");
        params.put("sign", "E8BE6FDADA3C9382630E315D9BA13AF6");
        params.put("sign_type", "MD5");
        params.put("status", "0");
        params.put("total_fee", "2100");
        params.put("version", "1.0");

        QGDLPayServiceImpl service = new QGDLPayServiceImpl();
        service.md5Key = "61u7imtk0938aebzgfpvcw5l2nsqjdh4";

        service.callback(params);
    }

    @Override
    public JSONObject smPay(PayEntity payEntity) {
        String out_trade_no = payEntity.getOrderNo();// 商户订单号
        double total_fee = payEntity.getAmount();// 123;//"12300";//支付金额，分
        String userName = payEntity.getUsername();
        String mobile = payEntity.getMobile();
        // 微信渠道
        if ("PAY_WECHAT_NATIVE".equals(payEntity.getPayCode())) {
            JSONObject r_json = wxPay(payEntity);
            if ("success".equals(r_json.getString("status"))) {
                // pc端
                if (org.apache.commons.lang3.StringUtils.isBlank(mobile)) {
                    return PayUtil.returnPayJson("success", "2", "支付接口请求成功!", userName, total_fee,
                            payEntity.getOrderNo(), r_json.getString("qrCode"));
                } else {
                    // 手机端
                    return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, total_fee,
                            payEntity.getOrderNo(), r_json.getString("qrCode"));
                }
            } else {
                return PayUtil.returnPayJson("error", "2", r_json.getString("msg"), userName, total_fee,
                        payEntity.getOrderNo(), "");
            }
        }

        String toPayUrl = assembleToPayUrl(payEntity, false);
        return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, total_fee, out_trade_no, toPayUrl);
    }

    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        String toPayUrl = assembleToPayUrl(payEntity, true);
        return PayUtil.returnWYPayJson("success", "link", toPayUrl, "", "");
    }

    public JSONObject wxPay(PayEntity payEntity) {
        String out_trade_no = payEntity.getOrderNo();// cagent+new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new
                                                     // Date());//
        double total_fee = payEntity.getAmount();// 123;//"12300";//支付金额，分
        String service = payEntity.getPayCode();// "PAY_ALIPAY_QRCODE";//"PAY_QQ_QR";//支付通道
        String return_url = payEntity.getRefererUrl();// "http://www.baidu.com";//回显地址
        String mch_create_ip = payEntity.getIp();// "127.0.0.1";//商户ip地址
        String nonce_str = UUID.randomUUID().toString().replaceAll("-", "");// 随机字符串,最大32位

        Map<String, String> params = new TreeMap<>();
        params.put("out_trade_no", out_trade_no);
        params.put("mch_id", this.mch_id);
        params.put("total_fee", String.valueOf((int) (100 * total_fee)));
        params.put("service", service);
        params.put("notify_url", this.notify_url);
        params.put("return_url", return_url);
        params.put("subject", this.subject);
        params.put("mch_create_ip", mch_create_ip);
        params.put("nonce_str", nonce_str);

        // 生成签名
        StringBuilder sb = new StringBuilder();
        for (String key : params.keySet()) {
            String value = params.get(key);
            sb.append(key + "=" + value + "&");
        }
        sb.append("key=" + this.md5Key);
        logger.info("签名字符串:" + sb.toString());
        params.put("sign", MD5Util.encode(sb.toString()).toUpperCase());

        String responseStr = "";

        try {
            responseStr = HttpUtil.RequestForm(payUrl, params, HttpMethod.GET);
            logger.info("微信接口响应:" + responseStr);
            JSONObject resJson = JSONObject.fromObject(responseStr);
            if (resJson.containsKey("status") && "0".equals(resJson.getString("status"))
                    && resJson.containsKey("result_code") && "0".equals(resJson.getString("result_code"))) {
                String code_url = resJson.getString("code_url");
                return getReturnJson("success", code_url, "二维码连接获取成功！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return getReturnJson("error", "", responseStr);
        }
        return getReturnJson("error", "", responseStr);
    }

    private String assembleToPayUrl(PayEntity payEntity, boolean isWy) {

        String out_trade_no = payEntity.getOrderNo();// cagent+new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new
                                                     // Date());//
        double total_fee = payEntity.getAmount();// 123;//"12300";//支付金额，分
        String service = payEntity.getPayCode();// "PAY_ALIPAY_QRCODE";//"PAY_QQ_QR";//支付通道
        String return_url = payEntity.getRefererUrl();// "http://www.baidu.com";//回显地址
        String mch_create_ip = payEntity.getIp();// "127.0.0.1";//商户ip地址
        String nonce_str = UUID.randomUUID().toString().replaceAll("-", "");// 随机字符串,最大32位

        Map<String, String> params = new TreeMap<>();
        params.put("out_trade_no", out_trade_no);
        params.put("mch_id", this.mch_id);
        params.put("total_fee", String.valueOf((int) (100 * total_fee)));
        params.put("service", service);
        params.put("notify_url", this.notify_url);
        params.put("return_url", return_url);
        params.put("subject", this.subject);
        params.put("mch_create_ip", mch_create_ip);
        params.put("nonce_str", nonce_str);

        // 生成签名
        StringBuilder sb = new StringBuilder();
        for (String key : params.keySet()) {
            String value = params.get(key);
            sb.append(key + "=" + value + "&");
        }
        sb.append("key=" + this.md5Key);
        logger.info("签名字符串:" + sb.toString());
        params.put("sign", MD5Util.encode(sb.toString()).toUpperCase());

        // 拼接下单参数
        sb = new StringBuilder();
        for (String key : params.keySet()) {
            String value = params.get(key);
            sb.append(key + "=" + value + "&");
        }
        String paramStr = sb.substring(0, sb.length() - 1);
        String toPayUrl = this.payUrl + "?" + paramStr;
        logger.info("成功获取支付地址：" + toPayUrl);

        return toPayUrl;
    }

    /**
     * 结果返回
     * 
     * @param status
     * @param qrCode
     * @param msg
     * @return
     */
    private JSONObject getReturnJson(String status, String qrCode, String msg) {
        JSONObject json = new JSONObject();
        json.put("status", status);
        json.put("qrCode", qrCode);
        json.put("msg", msg);
        return json;
    }

    /**
     * 回调验签
     * 
     * @param infoMap
     * @return
     */
    @Override
    public String callback(Map<String, String> params) {

        String signRemote = params.get("sign");
        params.remove("sign");

        StringBuilder sb = new StringBuilder();
        for (String key : params.keySet()) {
            String value = params.get(key);
            sb.append(key + "=" + value + "&");
        }
        sb.append("key=" + this.md5Key);

        String signLocal = MD5Util.encode(sb.toString()).toUpperCase();
        if (signLocal.equalsIgnoreCase(signRemote)) {
            logger.info("回调验签成功");
            return "success";
        }

        logger.info("回调验签失败");
        return "fail";
    }
}
