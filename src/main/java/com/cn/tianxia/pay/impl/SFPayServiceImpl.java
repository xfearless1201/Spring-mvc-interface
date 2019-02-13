package com.cn.tianxia.pay.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.gst.util.StringUtils;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.sf.util.DateUtils;
import com.cn.tianxia.pay.sf.util.HttpRequestUtil;
import com.cn.tianxia.pay.sf.util.Utils;

import net.sf.json.JSONObject;

/**
 * @Description:TODO
 * @author:zouwei
 * @time:2017年7月6日 下午7:22:39
 */
public class SFPayServiceImpl implements PayService {

    private String merchant_code;// 商户id
    private String notify_url; // 回调通知URL
    private String url;// 请求地址
    private static String merchantKey;// 加密key
    private String pay_url;

    private final static Logger logger = LoggerFactory.getLogger(SFPayServiceImpl.class);

    public SFPayServiceImpl(Map<String, String> pmap, String type) {
        JSONObject jo = JSONObject.fromObject(pmap);
        if (null != pmap) {
            if ("bank".equals(type)) {
                JSONObject bankJson = JSONObject.fromObject(jo.get("bank"));
                merchant_code = bankJson.get("merchant_code").toString();
                notify_url = bankJson.get("notify_url").toString();
                url = bankJson.get("url").toString();
                merchantKey = bankJson.get("merchantKey").toString();
            }
            if ("wx".equals(type)) {
                JSONObject wxJson = JSONObject.fromObject(jo.get("wx"));
                merchant_code = wxJson.get("merchant_code").toString();
                notify_url = wxJson.get("notify_url").toString();
                url = wxJson.get("url").toString();
                merchantKey = wxJson.get("merchantKey").toString();
            }
            if ("ali".equals(type)) {
                JSONObject aliJson = JSONObject.fromObject(jo.get("ali"));
                merchant_code = aliJson.get("merchant_code").toString();
                notify_url = aliJson.get("notify_url").toString();
                url = aliJson.get("url").toString();
                merchantKey = aliJson.get("merchantKey").toString();
            }
            if ("cft".equals(type)) {
                JSONObject cftJson = JSONObject.fromObject(jo.get("cft"));
                merchant_code = cftJson.get("merchant_code").toString();
                notify_url = cftJson.get("notify_url").toString();
                url = cftJson.get("url").toString();
                merchantKey = cftJson.get("merchantKey").toString();
            }
            if ("jd".equals(type)) {
                JSONObject jdJson = JSONObject.fromObject(jo.get("jd"));
                merchant_code = jdJson.get("merchant_code").toString();
                notify_url = jdJson.get("notify_url").toString();
                url = jdJson.get("url").toString();
                merchantKey = jdJson.get("merchantKey").toString();
            }
            if ("yl".equals(type)) {
                JSONObject ylJson = JSONObject.fromObject(jo.get("yl"));
                merchant_code = ylJson.get("merchant_code").toString();
                notify_url = ylJson.get("notify_url").toString();
                url = ylJson.get("url").toString();
                merchantKey = ylJson.get("merchantKey").toString();
            }
            if ("wxtm".equals(type)) {
                JSONObject ylJson = JSONObject.fromObject(jo.get("wxtm"));
                merchant_code = ylJson.get("merchant_code").toString();
                notify_url = ylJson.get("notify_url").toString();
                url = ylJson.get("url").toString();
                merchantKey = ylJson.get("merchantKey").toString();
            }
            if (jo.containsKey("pay_url")) {
                pay_url = jo.get("pay_url").toString();
            }
        }
    }

    /**
     * 接口返回结果
     * 
     * @param link
     * @param linkType
     *            二种形式:1.qrcode 生成二维码 2.qrcode_url 支持跳转的url
     * @param msg
     * @param status
     * @return
     */
    private JSONObject retJSON(String link, String linkType, String msg, String status) {
        JSONObject json = new JSONObject();
        json.put("status", status);
        json.put("type", linkType);
        json.put("link", link);
        json.put("msg", msg);
        return json;
    }

    @Override
    public String callback(Map<String, String> request) {
        String sign = request.get("sign").toString();
        String signStr = Utils.getSign(request, merchantKey);
        if (sign.equals(signStr)) {
            return "success";
        }
        return null;
    }

    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        String pay_url = payEntity.getPayUrl();
        Map<String, String> scanMap = new HashMap<>();
        scanMap.put("order_no", payEntity.getOrderNo());// 订单号
        scanMap.put("order_amount", String.valueOf(payEntity.getAmount()));// 金额
        scanMap.put("customer_ip", payEntity.getIp());//
        scanMap.put("req_referer", payEntity.getRefererUrl());// "110.164.197.124"
        scanMap.put("bank_code", payEntity.getPayCode());
        String html = bankPay(scanMap);
        return PayUtil.returnWYPayJson("success", "form", html, pay_url, "");
    }

    private String bankPay(Map<String, String> scanMap) {
        Map<String, String> map = new HashMap<>();
        map.put("notify_url", notify_url);
        map.put("pay_type", "1");
        map.put("bank_code", scanMap.get("bank_code"));
        map.put("merchant_code", merchant_code);
        map.put("order_no", scanMap.get("order_no"));
        map.put("order_amount", scanMap.get("order_amount"));
        map.put("order_time", DateUtils.format(new Date()));
        map.put("customer_ip", scanMap.get("customer_ip"));
        map.put("req_referer", scanMap.get("req_referer"));
        map.put("sign", Utils.getSign(map, merchantKey));
        if (map.containsKey("account_name")) {
            try {
                map.put("account_name", URLEncoder.encode(map.get("account_name"), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        String FormString = "<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form  id=\"actform\" name=\"actform\" method=\"post\" action=\""
                + url + "\">";
        for (String key : map.keySet()) {
            FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" + map.get(key) + "'>\r\n";
        }
        FormString += "</form></body>";

        String html = FormString;
        logger.info("速付支付网银支付表单:" + html);
        return html;
    }

    @Override
    public JSONObject smPay(PayEntity payEntity) {
        String pay_code = payEntity.getPayCode();
        Double amount = payEntity.getAmount();
        String order_no = payEntity.getOrderNo();
        String userName = payEntity.getUsername();
        String mobile = payEntity.getMobile();
        Map<String, String> scanMap = new HashMap<String, String>();
        Map<String, Object> json = new HashMap<String, Object>();
        scanMap.put("order_no", payEntity.getOrderNo());// 订单号
        scanMap.put("order_amount", String.valueOf(payEntity.getAmount()));// 金额
        scanMap.put("customer_ip", payEntity.getIp());//
        scanMap.put("req_referer", payEntity.getRefererUrl());// "110.164.197.124"
        scanMap.put("pay_type", pay_code);
        // 京东扫码使用pay接口
        if ("6".equals(pay_code)) {
            scanMap.put("bank_code", "qrcode");
            String html = JDPay(scanMap);
            return PayUtil.returnPayJson("success", "1", "支付接口请求失败!", userName, amount, order_no, html);
        }
        JSONObject retJson;
        scanMap.put("mobile", mobile);
        // String mobile = payEntity.getMobile();
        retJson = scanPay(scanMap);

        if (retJson.getString("status").equals("success")) {

            // 微信条码
            if ("2".equals(pay_code)) {
                return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, amount, order_no,
                        retJson.getString("link"));
            }

            if (StringUtils.isNullOrEmpty(mobile)) {
                return PayUtil.returnPayJson("success", "2", "支付接口请求成功!", userName, amount, order_no,
                        retJson.getString("link"));
            } else {
                return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, amount, order_no,
                        retJson.getString("link"));
            }
        }
        return PayUtil.returnPayJson("error", "1", "支付接口请求失败!", userName, amount, order_no, "支付接口请求失败!");
    }

    private JSONObject scanPay(Map<String, String> scanMap) {
        Map<String, String> map = new HashMap<>();
        map.put("notify_url", notify_url);
        map.put("pay_type", scanMap.get("pay_type"));
        map.put("bank_code", "qrcode");
        map.put("merchant_code", merchant_code);
        map.put("order_no", scanMap.get("order_no"));
        map.put("order_amount", scanMap.get("order_amount"));
        map.put("order_time", DateUtils.format(new Date()));
        map.put("customer_ip", scanMap.get("customer_ip"));
        map.put("req_referer", scanMap.get("req_referer"));
        try {
            String html = post(map, url);
            logger.info("返回信息为：" + html);
            JSONObject json = JSONObject.fromObject(html);
            if ("00".equals(json.get("flag"))) {
                if (StringUtils.isNullOrEmpty(scanMap.get("mobile"))) {
                    return retJSON(json.get("qrCodeUrl").toString(), "qrcode", "二维码图片生成", "success");
                } else {
                    return retJSON(json.get("qrCodeUrl").toString(), "qrcode_url", "二维码图片连接", "success");
                }
            } else {
                return retJSON("", "", json.toString(), "error");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retJSON("", "", "扫码获取失败", "error");
    }

    public static String post(Map<String, String> map, String url) throws Exception {
        map.put("sign", Utils.getSign(map, merchantKey));
        if (map.containsKey("account_name")) {
            map.put("account_name", URLEncoder.encode(map.get("account_name"), "UTF-8"));
        }
        String result = HttpRequestUtil.sendPost(url, map);
        return result;
    }

    public static void main(String[] args) {
        String url = "http://pay.sufupay.vip/order.html";
        String pay_url = "http://pay.sufupay.vip/pay.html";
        Map<String, String> map = new HashMap<>();
        map.put("notify_url", "http://127.0.0.1:8080/gateway/pay_notify.html");
        map.put("pay_type", "6");
        map.put("bank_code", "qrcode");
        map.put("merchant_code", "6199216");
        map.put("order_no", "SF" + System.currentTimeMillis());
        map.put("order_amount", "10.00");
        map.put("order_time", DateUtils.format(new Date()));
        map.put("customer_ip", "127.0.0.1");
        map.put("req_referer", "111");
        map.put("sign", Utils.getSign(map, merchantKey));
        // try {
        // String result = post(map, url);
        // System.out.println("返回结果为:" + result);
        // } catch (Exception e) {
        // e.printStackTrace();
        // }

        // if (map.containsKey("account_name")) {
        // try {
        // map.put("account_name", URLEncoder.encode(map.get("account_name"), "UTF-8"));
        // } catch (UnsupportedEncodingException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // }
        // String FormString = "<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form
        // id=\"actform\" name=\"actform\" method=\"post\" action=\""
        // + pay_url + "\">";
        // for (String key : map.keySet()) {
        // FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" + map.get(key) + "'>\r\n";
        // }
        // FormString += "</form></body>";
        //
        // String html = FormString;
        // // String html = HttpUtil.HtmlFrom(url, resquestMap);
        // logger.info("速付支付网银支付表单:" + html);
    }

    /**
     * @Description 京东使用pay方法请求
     * @param scanMap
     * @return
     */
    private String JDPay(Map<String, String> scanMap) {
        Map<String, String> map = new HashMap<>();
        map.put("notify_url", notify_url);
        map.put("pay_type", scanMap.get("pay_type"));
        map.put("bank_code", scanMap.get("bank_code"));
        map.put("merchant_code", merchant_code);
        map.put("order_no", scanMap.get("order_no"));
        map.put("order_amount", scanMap.get("order_amount"));
        map.put("order_time", DateUtils.format(new Date()));
        map.put("customer_ip", scanMap.get("customer_ip"));
        map.put("req_referer", scanMap.get("req_referer"));
        map.put("sign", Utils.getSign(map, merchantKey));
        if (map.containsKey("account_name")) {
            try {
                map.put("account_name", URLEncoder.encode(map.get("account_name"), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        String FormString = "<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form  id=\"actform\" name=\"actform\" method=\"post\" action=\""
                + pay_url + "\">";
        for (String key : map.keySet()) {
            FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" + map.get(key) + "'>\r\n";
        }
        FormString += "</form></body>";

        logger.info("速付京东表单:" + FormString);
        return FormString;
    }

}
