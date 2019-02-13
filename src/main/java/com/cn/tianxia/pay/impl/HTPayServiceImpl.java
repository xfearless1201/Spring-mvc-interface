package com.cn.tianxia.pay.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.gst.util.StringUtils;
import com.cn.tianxia.pay.ht.util.DateUtils;
import com.cn.tianxia.pay.ht.util.HttpRequestUtil;
import com.cn.tianxia.pay.ht.util.Utils;
import com.cn.tianxia.pay.mkt.util.HttpUtil;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.util.IPTools;

import aj.org.objectweb.asm.Type;
import net.sf.json.JSONObject;

public class HTPayServiceImpl implements PayService {

    // private static String orderId=System.;
    private final static Logger logger = LoggerFactory.getLogger(HTPayServiceImpl.class);
    private String merchant_code;
    private String key;
    private String urlHome;
    private String notify_url;
    // private static String merchant_code;
    private String req_referer;

    private String wx_merchant;
    private String wx_key;

    private String jd_merchant;
    private String jd_key;

    private String yl_merchant;
    private String yl_key;

    private String kj_merchant;
    private String kj_key;

    public HTPayServiceImpl(Map<String, String> pmap) {
        net.sf.json.JSONObject jo = new net.sf.json.JSONObject().fromObject(pmap);
        if (null != pmap) {
            // user_Id = jo.get("user_Id").toString();
            key = jo.get("key").toString();
            urlHome = jo.get("urlHome").toString();
            notify_url = jo.get("notify_url").toString();
            merchant_code = jo.get("merchant_code").toString();
            req_referer = jo.get("req_referer").toString();
            if (jo.containsKey("wx_merchant")) {
                wx_merchant = jo.get("wx_merchant").toString();
            }
            if (jo.containsKey("wx_key")) {
                wx_key = jo.get("wx_key").toString();
            }

            if (jo.containsKey("yl_merchant")) {
                yl_merchant = jo.get("yl_merchant").toString();
            }
            if (jo.containsKey("yl_key")) {
                yl_key = jo.get("yl_key").toString();
            }

            if (jo.containsKey("jd_merchant")) {
                jd_merchant = jo.get("jd_merchant").toString();
            }
            if (jo.containsKey("jd_key")) {
                jd_key = jo.get("jd_key").toString();
            }

            if (jo.containsKey("kj_merchant")) {
                kj_merchant = jo.get("kj_merchant").toString();
            }
            if (jo.containsKey("kj_key")) {
                kj_key = jo.get("kj_key").toString();
            }

        }
    }

    /**
     * @Description (网银接口)
     * @param bankMap
     * @param merchant_code
     * @param key
     * @return
     */
    public String bankPay(Map<String, String> bankMap, String merchant_code, String key) {
        Map<String, String> map = new HashMap<>();
        map.put("notify_url", notify_url);
        map.put("return_url", bankMap.get("return_url"));
        map.put("pay_type", bankMap.get("pay_type"));
        map.put("bank_code", bankMap.get("bank_code"));
        map.put("merchant_code", merchant_code);
        map.put("order_no", bankMap.get("order_no"));
        map.put("order_amount", bankMap.get("order_amount"));
        map.put("order_time", DateUtils.format(new Date()));
        map.put("customer_ip", bankMap.get("customer_ip"));
        map.put("req_referer", req_referer);
        String html = "";
        String url = urlHome + "pay.html";

        try {
            html = post(map, url, key);
        } catch (Exception e) {
            logger.info("汇通支付网银生成from表单异常！");
            e.printStackTrace();
        }
        logger.info("汇通支付网银表单:" + html);
        return html;
    }

    /**
     * @Description (扫码接口)
     * @param scanMap
     * @param merchant_code
     * @param key
     * @return
     */
    public String scanPay(Map<String, String> scanMap, String merchant_code, String key) {
        Map<String, String> map = new HashMap<>();
        map.put("notify_url", notify_url);
        map.put("return_url", scanMap.get("return_url"));
        map.put("pay_type", scanMap.get("pay_type"));
        // map.put("bank_code", bankMap.get("bank_code"));
        map.put("merchant_code", merchant_code);
        map.put("order_no", scanMap.get("order_no"));
        map.put("order_amount", scanMap.get("order_amount"));
        map.put("order_time", DateUtils.format(new Date()));
        map.put("customer_ip", scanMap.get("customer_ip"));
        map.put("req_referer", req_referer);
        // String html = "";

        logger.info("汇通扫码请求:" + JSONObject.fromObject(map));
        // map.put("sign", Utils.getSign(map, key));
        String url = urlHome + "order.html";
        // String result = HttpRequestUtil.sendPost(url, map);
        String result = "";
        try {
            result = HttpsPost(map, url, key);
            logger.info("汇通扫码响应:" + result);
        } catch (Exception e) {
            logger.info("汇通扫码响应异常:" + result);
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 表单提交
     * 
     * @param map
     * @param url
     * @return
     * @throws Exception
     */
    public String post(Map<String, String> map, String url, String key) throws Exception {
        map.put("sign", Utils.getSign(map, key));
        if (map.containsKey("account_name")) {
            map.put("account_name", URLEncoder.encode(map.get("account_name"), "UTF-8"));
        }
        // String result = HttpRequestUtil.sendPost(url, map);
        // Map <String,Object> map2=new HashMap<>();
        // for (String name : map.keySet()) {
        // map2.put(name, map.get(name));
        // }
        //
        // String result =Https.doPostSSL(url, map2);

        String result = HttpUtil.HtmlFrom(url, map);
        return result;
    }

    public String dfPost(Map<String, String> map, String url) {
        map.put("sign", Utils.getSign(map, key));
        if (map.containsKey("account_name")) {
            try {
                map.put("account_name", URLEncoder.encode(map.get("account_name"), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        // String result = HttpRequestUtil.sendPost(url, map);
        String result = HttpRequestUtil.sendPost(url, map);
        return result;
    }

    /**
     * https
     * 
     * @param map
     * @param url
     * @return
     * @throws Exception
     */
    public String HttpsPost(Map<String, String> map, String url, String key) throws Exception {
        map.put("sign", Utils.getSign(map, key));
        if (map.containsKey("account_name")) {
            map.put("account_name", URLEncoder.encode(map.get("account_name"), "UTF-8"));
        }
        String result = HttpRequestUtil.sendPost(url, map);
        return result;
    }

    public boolean HTNotify(HttpServletRequest request, String key) {
        boolean falg = false;

        String[] ParmList = new String[] {"merchant_code", "sign", "order_no", "order_amount", "order_time",
                "return_params", "trade_no", "trade_time", "trade_status", "notify_type" };

        Map<String, String> reqMap = new HashMap<String, String>();
        for (String Key : ParmList) {// 接收参数
            if (request.getParameterMap().containsKey(Key)) {
                reqMap.put(Key, request.getParameter(Key));
                if (request.getParameterMap().containsKey("trade_time")
                        || request.getParameterMap().containsKey("order_time")) {
                    reqMap.put(Key, URLDecoder.decode(request.getParameter(Key)));
                }
            }
        }

        if (reqMap.containsKey("notify_type") && "bank_page".equals(reqMap.get("notify_type").toString())) {
            logger.info("汇通支付页面跳转通知不处理！");
            return falg;
        }

        String servenSign = reqMap.remove("sign");
        String localSign = Utils.getSign(reqMap, key);

        logger.info("支付商签名:" + servenSign + " 本地签名:" + localSign);
        if (!StringUtils.isNullOrEmpty(servenSign) && servenSign.equals(localSign)) {
            logger.info("汇通支付签名成功！");
            return true;
        }

        logger.info("汇通支付签名失败！");
        return falg;
    }

    public static void TestHTNotify() {
        // 商户号merchant_code 签名sign
        // 商户唯一订单号 order_no
        // 商户订单总金额 order_amount
        // 商户订单时间 order_time
        // 回传参数 return_params
        // 支付平台订单号 trade_no
        // 支付平台订单时间 trade_time
        // 交易状态 trade_status
        // 通知类型 notify_type

        String merchant_code = "11688881";
        String order_no = "HTbl1201709131548091548091017";
        String order_amount = "10";
        String order_time = "2017-09-13 16:48:15";
        String return_params = "22";
        String trade_no = "333";
        String trade_time = "2017-09-13 16:48:15";
        String trade_status = "success";
        String notify_type = "back_notify";

        Map<String, String> params = new HashMap<>();
        params.put("merchant_code", merchant_code);
        params.put("order_no", order_no);
        params.put("order_amount", order_amount);
        params.put("order_time", order_time);
        params.put("return_params", return_params);
        params.put("trade_no", trade_no);
        params.put("trade_time", trade_time);
        params.put("trade_status", trade_status);
        params.put("notify_type", notify_type);

        String sign = Utils.getSign(params, "b2bbdc6884440be3f137caf11c0013fa");
        params.put("sign", sign);
        // http://182.16.110.186:8080/XPJ/PlatformPay/bankingNotify.do
        String ss = HttpUtil.RequestForm("http://localhost:8087/JJF/PlatformPay/bankingNotify.do", params);
        logger.info("回调后返回：" + ss);
    }

    public static void main(String[] args) {
        // String order_no = "TX" + System.currentTimeMillis();
        // Map<String, String> params = new HashMap<>();
        // // String user_Id = "11787646";
        // String key = "8d52e31683e0bc1f1ab654d9983b8d32";
        // String urlHome = "https://api.huitongvip.com/";
        // String notify_url = "http://127.0.0.1:8080/gateway/pay_notify.html";
        // String merchant_code = "11787646";
        // String req_referer = "222";
        // String customer_ip = "127.0.0.1";
        //
        // String bank_code = "ICBC";
        // String pay_type = "2";
        // String order_amount = "1";
        // params.put("key", key);
        // params.put("urlHome", urlHome);
        // params.put("notify_url", notify_url);
        // params.put("merchant_code", merchant_code);
        // params.put("req_referer", req_referer);
        //
        // logger.info("HTjson配置:" + JSONObject.fromObject(params));
        // HTPayServiceImpl ht = new HTPayServiceImpl(params);

        // System.out.println("****-----------网银测试-----------***");
        // Map<String, String> bankMap = new HashMap<>();
        // bankMap.put("return_url", req_referer);
        // bankMap.put("pay_type", pay_type);
        // bankMap.put("bank_code", bank_code);
        // bankMap.put("order_no", order_no);
        // bankMap.put("order_amount", order_amount);
        // bankMap.put("customer_ip", customer_ip);
        // ht.bankPay(bankMap);
        // System.out.println("****-----------网银测试结束-----------***");

        // System.out.println("****-----------扫码测试-----------***");
        // String orderUrl = urlHome + "order.html";
        // Map<String, String> scanMap = new HashMap<>();
        // scanMap.put("return_url",
        // "http://127.0.0.1/gateway/pay_notify.html");
        // scanMap.put("pay_type", pay_type);
        // scanMap.put("order_no", order_no);
        // scanMap.put("order_amount", order_amount);
        // scanMap.put("customer_ip", customer_ip);
        // ht.ScanPay(scanMap);
        // System.out.println("****-----------扫码测试结束-----------***");

        // Map<String, String> postMap=new HashMap<>();
        // postMap.put("type", "1");
        // String ss =
        // HttpUtil.RequestForm("http://192.168.0.103:69/TXK/alipayPpaymentScanCode/getQRCode.do",
        // postMap);
        // System.out.println(ss);
        // System.out.println("****-----------回调测试-----------***");
        // ht.TestHTNotify();
        TestHTNotify();
        // System.out.println("****-----------回调测试结束-----------***");

        // String trade_no = order_no;
        // Map<String, String> dfMap = new HashMap<>();
        // dfMap.put("merchant_code", merchant_code);
        // dfMap.put("order_amount", "1");
        // dfMap.put("trade_no", trade_no);
        // dfMap.put("bank_code", "ICBC");
        // dfMap.put("account_name", "xx");
        // dfMap.put("account_number", "xxxx");

        // System.out.println(ht.dfm(dfMap));

        // System.out.println(ht.df_query(dfMap));
    }

    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        Double amount = payEntity.getAmount();
        String order_no = payEntity.getOrderNo();
        String refereUrl = payEntity.getRefererUrl();
        String pay_code = payEntity.getPayCode();
        String userName = payEntity.getUsername();
        String pay_url = payEntity.getPayUrl();
        String mobile = payEntity.getMobile();
        String ip = payEntity.getIp();

        Map<String, String> bankMap = new HashMap<String, String>();
        // int int_amount = (int) (amount * 100);
        bankMap.put("order_amount", String.valueOf(amount));// 订单明细金额
        bankMap.put("customer_ip", ip);// 客户端ip
        bankMap.put("order_no", order_no);// 订单号
        bankMap.put("pay_type", "1");// 1为网银支 2为微信支付 3为支付宝支付 5为QQ钱包
        bankMap.put("bank_code", pay_code);// 目标资金机构代码
        bankMap.put("return_url", refereUrl);// 支付完成地址
        String html = bankPay(bankMap, merchant_code, key);

        return PayUtil.returnWYPayJson("success", "form", html, pay_url, "");
    }

    @Override
    public JSONObject smPay(PayEntity payEntity) {
        Double amount = payEntity.getAmount();
        String order_no = payEntity.getOrderNo();
        String refereUrl = payEntity.getRefererUrl();
        String pay_code = payEntity.getPayCode();
        String userName = payEntity.getUsername();
        String pay_url = payEntity.getPayUrl();
        String mobile = payEntity.getMobile();
        String ip = payEntity.getIp();

        Map<String, String> bankMap = new HashMap<String, String>();
        bankMap.put("order_amount", String.valueOf(amount));// 订单明细金额
        bankMap.put("customer_ip", ip);// 客户端ip
        bankMap.put("order_no", order_no);// 订单号
        bankMap.put("bank_code", pay_code);// 目标资金机构代码
        bankMap.put("return_url", refereUrl);// 支付完成地址

        bankMap.put("pay_type", pay_code);// 1为网银支 2为微信支付 3为支付宝支付 5为QQ钱包

        String b_key = "";
        String b_merchant = "";
        if ("2".equals(pay_code)) {
            b_key = wx_key;
            b_merchant = wx_merchant;
        } else if ("6".equals(pay_code)) {
            b_key = jd_key;
            b_merchant = jd_merchant;
        } else if ("7".equals(pay_code)) {
            b_key = yl_key;
            b_merchant = yl_merchant;
        } else if ("8".equals(pay_code)) {
            b_key = kj_key;
            b_merchant = kj_merchant;
        } else {
            return PayUtil.returnPayJson("error", "2", "获取配置信息错误！", userName, amount, order_no, "");
        }
        String html = bankPay(bankMap, b_merchant, b_key);

        return PayUtil.returnPayJson("success", "1", "支付接口请求成功!", userName, amount, order_no, html);
    }

    @Override
    public String callback(Map<String, String> data) {
        // TODO Auto-generated method stub
        return null;
    }
}
