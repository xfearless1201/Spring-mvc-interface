package com.cn.tianxia.pay.impl;

import com.cn.tianxia.common.MD5Util;
import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.gst.util.StringUtils;
import com.cn.tianxia.pay.mjf.util.MJFToolKit;
import com.cn.tianxia.pay.qyf.util.ToolKit;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.ys.util.DateUtil;
import net.sf.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

public class HYFZFPayServiceImpl implements PayService {

    private String payUrl;
    private String merchantCode;
    private String notifyUrl;
    private String key;

    private final static Logger logger = LoggerFactory.getLogger(HYFZFPayServiceImpl.class);

    public HYFZFPayServiceImpl(Map<String, String> pmap) {
        JSONObject jo = JSONObject.fromObject(pmap);
        if (null != pmap) {
            if (jo.containsKey("payUrl")) {
                payUrl = jo.get("payUrl").toString();
            }
            if (jo.containsKey("merchant_code")) {
                merchantCode = jo.get("merchant_code").toString();
            }
            if (jo.containsKey("notify_url")) {
                notifyUrl = jo.get("notify_url").toString();
            }
            if (jo.containsKey("key")) {
                key = jo.get("key").toString();
            }
        }
    }

    /**
     * 网银支付
     *
     * @param payEntity
     * @return
     */
    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        return null;
    }

    /**
     * 扫码支付
     *
     * @param payEntity
     * @return
     */
    @Override
    public JSONObject smPay(PayEntity payEntity) {
        String notify_url = this.notifyUrl;// 支付Url
        String pay_type = payEntity.getPayCode();// 支付渠道号
        String merchant_code = this.merchantCode;// 商户号
//        String order_no = payEntity.getOrderNo();// 订单号
        //修改订单长度20位
        String order_no = DateUtil.getCurrentDate("yyyyMMddHHmmss")+DateUtil.getRandom(6) + "";// 订单号
        payEntity.setOrderNo(order_no);

        /**
         * 金额必须是整数
         */
        DecimalFormat decimalFormat = new DecimalFormat("#");
        String order_amount = decimalFormat.format(payEntity.getAmount());
        String order_time = DateUtil.getCurrentDate("yyyy-MM-dd HH:mm:ss");
        String customer_ip = payEntity.getIp();
        String random_char = MJFToolKit.getRandomStr(10);
        String mobile = payEntity.getMobile();
        String userName = payEntity.getUsername();
        String sign = "";

        Map<String, String> scanMap = new HashMap<String, String>();
        scanMap.put("notify_url", notify_url);
        scanMap.put("pay_type", pay_type);
        scanMap.put("merchant_code", merchant_code);
        scanMap.put("order_no", order_no);
        scanMap.put("order_amount", String.valueOf(order_amount));
        scanMap.put("order_time", order_time);
        scanMap.put("customer_ip", customer_ip);
        scanMap.put("random_char", random_char);

        List<String> keys = new ArrayList<String>(scanMap.keySet());
        Collections.sort(keys);

        StringBuilder sb = new StringBuilder();

        String key, value;
        for (int i = 0; i < keys.size(); i++) {
            key = keys.get(i);
            value = scanMap.get(key);

            if (sb.length() == 0) {
                sb.append(key + "=" + value);
            } else {
                sb.append("&" + key + "=" + value);
            }
        }

        if (sb.length() > 0) {
            sb.append("&");
        }
        sb.append("key=");
        sb.append(this.key);

        logger.info("汇银付支付待签名原串：" + sb.toString());
        sign = ToolKit.MD5(sb.toString(), "UTF-8");

        scanMap.put("sign", sign);

        logger.info("汇银付支付请求Json格式数据：" + JSONObject.fromObject(scanMap));

        String resultstr = postJson(payUrl, JSONObject.fromObject(scanMap).toString());
        logger.info("汇银付支付服务端返回的数据：" + resultstr);

        JSONObject r_json = JSONObject.fromObject(resultstr);

        if ("00".equals(r_json.getString("flag"))) {
            // pc端
            if (StringUtils.isNullOrEmpty(mobile)) {
                return PayUtil.returnPayJson("success", "2", "汇银付支付PC端支付接口请求成功!", userName,
                        Double.valueOf(order_amount), order_no, r_json.getString("qrCodeUrl"));
            } else {
                // 手机端
                return PayUtil.returnPayJson("success", "4", "汇银付支付手机端支付接口请求成功!", userName,
                        Double.valueOf(order_amount), order_no, r_json.getString("qrCodeUrl").toLowerCase());
            }
        } else {
            return PayUtil.returnPayJson("error", "2", r_json.getString("msg"), userName, Double.valueOf(order_amount),
                    order_no, "");
        }
    }

    @Override
    public String callback(Map<String, String> paramMap){
        String serviceSign = paramMap.remove("sign");
        // 排序
        TreeMap<String, String> treeMap = new TreeMap<>();
        treeMap.putAll(paramMap);
        StringBuffer sb = new StringBuffer();
        for (String key : treeMap.keySet()) {
            if ("sign".equalsIgnoreCase(key)) {
                continue;
            }
            String value = String.valueOf(paramMap.get(key));
            if (org.apache.commons.lang.StringUtils.isBlank(value)) {
                continue;
            }
            sb.append(key + "=" + value + "&");
        }
        sb.append("key=" + this.key);
        logger.info("汇银付支付回调待签名字符:" + sb.toString());

        String signStr = ToolKit.MD5(sb.toString(), "UTF-8");
        logger.info("本地sign" + signStr + "        服务器sign" + serviceSign);
        if (serviceSign.equalsIgnoreCase(signStr)) {
            logger.info("汇银付支付签名成功");
            return "success";
        }

        logger.info("汇银付支付签名失败");
        return "fail";
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
     * 判断是否是json结构
     */
    public static boolean isJson(String value) {
        try {
            JSONObject.fromObject(value);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * postJson
     */
    public static String postJson(String url, String param) {
        String resp = "";
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost post = new HttpPost(url);
        post.setHeader("Connection", "Close");// 服务端发送完数据，即关闭连接
        try {
            StringEntity s = new StringEntity(param);
            s.setContentEncoding("UTF-8");
            s.setContentType("application/json");// 发送json数据需要设置contentType
            post.setEntity(s);
            HttpResponse res = httpclient.execute(post);
            if (Integer.valueOf(res.getStatusLine().getStatusCode()).equals(HttpStatus.SC_OK)) {
                HttpEntity entity = res.getEntity();
                resp = EntityUtils.toString(res.getEntity());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // 关闭连接,释放资源
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resp;
    }

}
