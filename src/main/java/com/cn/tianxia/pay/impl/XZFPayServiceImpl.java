package com.cn.tianxia.pay.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.gcc.util.TfcpayUtil;
import com.cn.tianxia.pay.gst.util.StringUtils;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.util.IPTools;
import com.cn.tianxia.util.SSLClient;

import net.sf.json.JSONObject;

public class XZFPayServiceImpl implements PayService {

    /** 测试商户号 **/
    private String appid;
    /** 测试密钥 ***/
    private String apikey;
    /** 支付地址 **/
    private String url;
    /** 回调地址 **/
    private String tongbu_url;
    /** 商品名称 **/
    private String subject;
    /** 商品描述 **/
    private String body;

    private final static Logger logger = LoggerFactory.getLogger(XZFPayServiceImpl.class);

    public XZFPayServiceImpl(Map<String, String> pmap) {
        net.sf.json.JSONObject jo = JSONObject.fromObject(pmap);
        if (null != pmap) {
            appid = jo.get("appid").toString();
            apikey = jo.get("apikey").toString();
            url = jo.get("url").toString();
            tongbu_url = jo.get("tongbu_url").toString();
            subject = jo.get("subject").toString();
            body = jo.get("body").toString();
        }
    }

    /**
     * 网银接口
     * 
     * @param banMap
     * @return
     */
    public String bankPay(Map<String, String> bankMap) {
        Map<String, String> data = new HashMap<String, String>();
        data.put("appid", appid);
        data.put("orderid", bankMap.get("orderid"));
        data.put("subject", subject);
        data.put("body", body);
        data.put("fee", bankMap.get("fee"));
        data.put("tongbu_url", tongbu_url);
        data.put("cpparam", "");
        data.put("clientip", bankMap.get("clientIp"));
        data.put("back_url", bankMap.get("returnUrl"));
        data.put("sfrom", bankMap.get("sfrom"));
        data.put("mode", "");
        data.put("appname", "");
        data.put("appbs", "");
        data.put("paytype", "0");
        String sign = md5(appid + bankMap.get("orderid") + bankMap.get("fee") + tongbu_url + apikey);
        data.put("sign", sign);
        if (data.isEmpty()) {
            return "参数不能为空！";
        }
        String FormString = "<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form  id=\"actform\" name=\"actform\" method=\"post\" action=\""
                + url + "\">";
        for (String key : data.keySet()) {
            FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" + data.get(key) + "'>\r\n";
        }

        FormString += "</form></body>";

        logger.info("掌付网银支付表单:\n" + FormString);
        return FormString;
    }

    /**
     * 扫码接口
     * 
     * @param scanMap
     * @return
     */
    public JSONObject scanPay(Map<String, String> scanMap) {
        Map<String, String> data = new HashMap<String, String>();
        data.put("appid", appid);// "10280"
        data.put("orderid", scanMap.get("orderid"));
        data.put("subject", subject);
        data.put("body", body);
        data.put("fee", scanMap.get("fee"));
        data.put("tongbu_url", tongbu_url);
        data.put("cpparam", "");
        data.put("clientip", scanMap.get("clientIp"));//
        data.put("back_url", scanMap.get("returnUrl"));
        data.put("sfrom", scanMap.get("sfrom"));
        data.put("mode", "");
        data.put("appname", "");
        data.put("appbs", "");
        data.put("paytype", scanMap.get("paytype"));
        String str = appid + scanMap.get("orderid") + scanMap.get("fee") + tongbu_url + apikey;
        System.out.println("加密值：" + str);
        String sign = md5(str);
        data.put("sign", sign);
        String sfrom = scanMap.get("sfrom").toString();
        if ("pc".equals(sfrom)) {
            data.put("user", "vip");
        }
        System.out.println("请求参数值:" + data.toString());
        String result = doPost(url, data, "utf-8");
        JSONObject json = JSONObject.fromObject(result);
        if ("success".equals(json.get("code"))) {
            return getReturnJson("success", json.get("msg").toString(), "获取二维码连接成功！");
        }
        return getReturnJson("error", "", json.get("msg").toString());
    }

    public static void main(String[] args) {
        String key = "98b1396139f7b656a66d483eff3957a4";
        String appid = "10280";
        String url = "http://sanfang.yp178.com/dealpay.php";
        Map<String, String> data = new HashMap<String, String>();
        data.put("appid", appid);
        String orderid = System.currentTimeMillis() + "";
        data.put("orderid", orderid);
        data.put("subject", "tianxianchongzhi");
        data.put("body", "tianxianchongzhi");
        String fee = "200";
        data.put("fee", "200");
        String tongbu_url = "http://www.1tx888.com/TXY/PlatformPay/XZFNotify.do";
        data.put("tongbu_url", "http://www.1tx888.com/TXY/PlatformPay/XZFNotify.do");
        data.put("cpparam", "");
        data.put("clientip", "110.164.197.124");
        data.put("back_url", "http://www.1tx888.com/");
        data.put("sfrom", "wap");
        data.put("mode", "");
        data.put("appname", "");
        data.put("appbs", "");
        data.put("paytype", "24");
        String str = appid + orderid + fee + tongbu_url + key;
        System.out.println("加密值：" + str);
        String sign = md5(str);
        System.out.println("sign值：" + sign);
        data.put("sign", sign);
        System.out.println("请求参数：" + data.toString());
        System.out.println("请求地址" + url);
        String result = doPost(url, data, "utf-8");
        System.out.println("返回结果值" + result.toString());
        // JSONObject json = JSONObject.fromObject(result);
        // if("success".equals(json.get("code"))){
        // return getReturnJson("success", json.get("msg").toString(),
        // "获取二维码连接成功！");
        // System.out.println(json.get("msg"));
        // }
        // return getReturnJson("error","",json.get("msg").toString());
    }

    /**
     * 回调验证签名
     * 
     * @param map
     * @return
     */
    @Override
    public String callback(Map<String, String> map) {
        String str = map.get("orderid") + map.get("result") + map.get("fee") + map.get("tradetime") + apikey;
        String sign = md5(str);
        if (sign.equals(map.get("sign"))) {
            logger.info("掌付回调sing校验成功");
            return "success";
        }
        logger.info("掌付回调sing校验失败");
        return "";
    }

    /**
     * 返回数据格式Json
     * 
     * @param status
     * @param qrCode
     * @param msg
     * @return
     */
    public JSONObject getReturnJson(String status, String qrCode, String msg) {
        JSONObject json = new JSONObject();
        json.put("status", status);
        json.put("qrCode", qrCode);
        json.put("msg", msg);
        return json;
    }

    /**
     * post 方法
     * 
     * @param url
     * @param map
     * @param charset
     * @return
     */
    public static String doPost(String url, Map<String, String> map, String charset) {
        HttpClient httpClient = null;
        HttpPost httpPost = null;
        String result = null;
        try {
            httpClient = new SSLClient();
            httpPost = new HttpPost(url);
            // ���ò���
            List<NameValuePair> list = new ArrayList<NameValuePair>();
            Iterator iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<String, String> elem = (Entry<String, String>) iterator.next();
                list.add(new BasicNameValuePair(elem.getKey(), elem.getValue()));
            }
            if (list.size() > 0) {
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list, charset);
                httpPost.setEntity(entity);
            }
            HttpResponse response = httpClient.execute(httpPost);
            if (response != null) {
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    result = EntityUtils.toString(resEntity, charset);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public static String TestdoPost(String url, Map<String, String> map, String charset) {
        HttpClient httpClient = null;
        HttpPost httpPost = null;
        String result = null;
        try {
            httpClient = new SSLClient();
            httpPost = new HttpPost(url);
            // ���ò���
            List<NameValuePair> list = new ArrayList<NameValuePair>();
            Iterator iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<String, String> elem = (Entry<String, String>) iterator.next();
                list.add(new BasicNameValuePair(elem.getKey(), elem.getValue()));
            }
            if (list.size() > 0) {
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list, charset);
                httpPost.setEntity(entity);
            }
            HttpResponse response = httpClient.execute(httpPost);
            if (response != null) {
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    result = EntityUtils.toString(resEntity, charset);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public static String md5(String strSrc) {
        String result = "";
        try {
            MessageDigest md5;
            try {
                md5 = MessageDigest.getInstance("MD5");
                md5.update((strSrc).getBytes("UTF-8"));
                byte b[] = md5.digest();

                int i;
                StringBuffer buf = new StringBuffer("");

                for (int offset = 0; offset < b.length; offset++) {
                    i = b[offset];
                    if (i < 0) {
                        i += 256;
                    }
                    if (i < 16) {
                        buf.append("0");
                    }
                    buf.append(Integer.toHexString(i));
                }
                result = buf.toString();
                return result;
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
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

        Map<String, String> bankMap = new HashMap<>();
        String orderNo = TfcpayUtil.nextUUID();
        bankMap.put("bankCode", pay_code);
        bankMap.put("orderid", orderNo);
        int int_amount = (int) (amount * 100);
        bankMap.put("fee", String.valueOf(int_amount));
        bankMap.put("returnUrl", refereUrl);
        bankMap.put("clientIp", ip);// ip
        if (StringUtils.isNullOrEmpty(mobile)) {
            bankMap.put("sfrom", "pc");
        } else {
            bankMap.put("sfrom", "wap");
        }

        String html = bankPay(bankMap);
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

        Map<String, String> scanMap = new HashMap<>();
        scanMap.put("orderid", order_no);
        int int_amount = (int) (amount * 100);
        scanMap.put("fee", String.valueOf(int_amount));
        scanMap.put("clientIp", ip);
        scanMap.put("returnUrl", refereUrl);
        scanMap.put("paytype", pay_code);
        JSONObject rjson = null;
        // pc端
        if (StringUtils.isNullOrEmpty(mobile)) {
            scanMap.put("sfrom", "pc");
        } else {
            scanMap.put("sfrom", "wap");
        }

        rjson = scanPay(scanMap);
        if (!"success".equals(rjson.getString("status"))) {
            return PayUtil.returnPayJson("error", "1", rjson.getString("msg"), userName, amount, order_no, "");
        }
        // 手机 or pc 返回类型
        if (StringUtils.isNullOrEmpty(mobile)) {
            String url = rjson.getString("qrCode");
            logger.info("返回链接：" + url);
            
            if ("34".equals(pay_code)) {
                return PayUtil.returnPayJson("success", "3", "支付接口请求成功!", userName, amount, order_no,
                        rjson.getString("qrCode"));
            }
            // pc qq扫码渠道
//            if ("39".equals(pay_code)) {
//           
//                return PayUtil.returnPayJson("success", "3", "支付接口请求成功!", userName, amount, order_no, url);
//            }

            return PayUtil.returnPayJson("success", "2", "支付接口请求成功!", userName, amount, order_no,
                    rjson.getString("qrCode"));
        } else {
            return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, amount, order_no,
                    rjson.getString("qrCode"));
        }
    }
}
