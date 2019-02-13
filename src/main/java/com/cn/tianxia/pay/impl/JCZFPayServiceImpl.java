package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.jczf.util.HttpUtil;
import com.cn.tianxia.jczf.util.Params;
import com.cn.tianxia.jczf.util.RSA;
import com.cn.tianxia.pay.gst.util.StringUtils;
import com.cn.tianxia.pay.service.PayService;

import net.sf.json.JSONObject;

/**
 * @ClassName JCZFPayServiceImpl
 * @Description 新金彩支付
 * @author zw
 * @Date 2018年7月23日 下午4:24:29
 * @version 1.0.0
 */
public class JCZFPayServiceImpl implements PayService {

    private String payUrl; // 服务器地址

    private String NOTIFY_URL;

    private int COMPANY_OID;

    private String clientPrivateKey;// 您的私钥

    private String serverPublicKey;

    private static String gatwayUrl;

    private final static Logger logger = LoggerFactory.getLogger(JCZFPayServiceImpl.class);

    public JCZFPayServiceImpl(Map<String, String> pmap) {
        if (pmap != null) {
            if (pmap.containsKey("payUrl")) {
                payUrl = pmap.get("payUrl");
            }
            if (pmap.containsKey("NOTIFY_URL")) {
                NOTIFY_URL = pmap.get("NOTIFY_URL");
            }
            if (pmap.containsKey("COMPANY_OID")) {
                COMPANY_OID = Integer.parseInt(pmap.get("COMPANY_OID"));
            }
            if (pmap.containsKey("clientPrivateKey")) {
                clientPrivateKey = pmap.get("clientPrivateKey");
            }
            if (pmap.containsKey("serverPublicKey")) {
                serverPublicKey = pmap.get("serverPublicKey");
            }
            if (pmap.containsKey("gatwayUrl")) {
                gatwayUrl = pmap.get("gatwayUrl");
            }
        }
    }

    public static void main(String[] args) {
        // JCZFPayServiceImpl jc = new JCZFPayServiceImpl();
        // jc.scanPay();
        // jc.bankPay();
    }

    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        double amount = payEntity.getAmount();// "8.02";// 订单金额
        String userName = payEntity.getUsername();
        String payCode = payEntity.getPayCode();
        String mobile = payEntity.getMobile();
        String order_no = payEntity.getOrderNo();
        String return_url = payEntity.getRefererUrl();
        String pay_url = payEntity.getPayUrl();
        DecimalFormat df = new DecimalFormat("#########");
        int price = Integer.valueOf(df.format(amount * 100));
        Map<String, Object> params = new TreeMap<>();

        // int pay_type = Integer.parseInt(payCode);

        params.put(Params.ORDER_ID, order_no);// 商户唯一订单号
        params.put(Params.AMOUNT, price); // 单位：分
        params.put("return_url", return_url);
        params.put("bank_id", payCode);

        String form = bankPay(params);

        // return PayUtil.returnWYPayJson("success", "form", form, pay_url, "");
        return PayUtil.returnWYPayJson("success", "jsp", form, pay_url, "paytest");
    }

    @Override
    public JSONObject smPay(PayEntity payEntity) {
        double amount = payEntity.getAmount();// "8.02";// 订单金额
        String userName = payEntity.getUsername();
        String payCode = payEntity.getPayCode();
        String mobile = payEntity.getMobile();
        String order_no = payEntity.getOrderNo();

        DecimalFormat df = new DecimalFormat("#########");
        int price = Integer.valueOf(df.format(amount * 100));
        Map<String, Object> params = new TreeMap<>();

        int pay_type = Integer.parseInt(payCode);

        params.put(Params.ORDER_ID, order_no);// 商户唯一订单号
        params.put(Params.AMOUNT, price); // 单位：分
        params.put(Params.PAY_TYPE, pay_type);

        JSONObject r_json = scanPay(params);

        if ("success".equals(r_json.getString("status"))) {
            // pc端
            if (org.apache.commons.lang3.StringUtils.isBlank(mobile)) {
                return PayUtil.returnPayJson("success", "2", "支付接口请求成功!", userName, amount, payEntity.getOrderNo(),
                        r_json.getString("qrCode"));
            } else {
                // 手机端
                return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, amount, payEntity.getOrderNo(),
                        r_json.getString("qrCode"));
            }
        } else {
            return PayUtil.returnPayJson("error", "2", r_json.getString("msg"), userName, amount,
                    payEntity.getOrderNo(), "");
        }
    }

    /**
     * @Description 扫码接口
     * @param payMap
     * @return
     */
    public JSONObject scanPay(Map<String, Object> payMap) {
        int payType = 86;
        Map<String, Object> paramMap = new HashMap();
        paramMap.put(Params.COMPANY_OID, COMPANY_OID);
        paramMap.put(Params.ORDER_ID, payMap.get("order_id"));// 商户唯一订单号
        paramMap.put(Params.ORDER_NAME, "test");
        paramMap.put(Params.ORDER_DESC, "test");
        paramMap.put(Params.AMOUNT, payMap.get("amount")); // 单位：分
        paramMap.put(Params.NOTIFY_URL, NOTIFY_URL);
        paramMap.put(Params.PAY_TYPE, payMap.get("pay_type").toString());
        Map<String, Object> rMap = null;
        JSONObject json = null;
        try {
            paramMap.put(Params.SIGN, RSA.sign(paramMap, clientPrivateKey));
            logger.info("request:" + JSON.toJSONString(paramMap));
            rMap = executeAndCheckSign(paramMap, "jcPayMobile");

            json = JSONObject.fromObject(rMap);

            if (json.containsKey("status")
                    && ("1".equals(json.getString("status")) || "2".equals(json.getString("status")))) {
                if (json.containsKey("content") && !StringUtils.isNullOrEmpty(json.getString("content"))) {
                    return getReturnJson("success", json.getString("content"), "二维码连接获取成功！");
                }
            }
            return getReturnJson("error", json.get("message").toString(), "二维码连接获取失败");

        } catch (Exception e) {
            e.printStackTrace();
            return getReturnJson("error", json.toString(), "二维码连接获取失败");
        }
    }

    /**
     * @Description 网银支付
     * @param payMap
     * @return
     */
    public String bankPay(Map<String, Object> payMap) {
        int payType = 86;
        Map<String, Object> paramMap = new HashMap();
        paramMap.put(Params.COMPANY_OID, COMPANY_OID);
        paramMap.put(Params.ORDER_ID, payMap.get("order_id"));// 商户唯一订单号
        paramMap.put(Params.ORDER_NAME, "test");
        paramMap.put(Params.ORDER_DESC, "test");
        paramMap.put(Params.AMOUNT, payMap.get("amount")); // 单位：分
        paramMap.put(Params.NOTIFY_URL, NOTIFY_URL);
        paramMap.put("return_url", payMap.get("return_url"));
        paramMap.put("card_type", 0);
        paramMap.put("tran_type", "B2C");

        paramMap.put("bank_id", payMap.get("bank_id"));
        paramMap.put(Params.PAY_TYPE, 10);
        String html = null;
        try {
            paramMap.put(Params.SIGN, RSA.sign(paramMap, clientPrivateKey));
            html = generateForm(paramMap);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return html;
    }

    /**
     * @Description 生成表单
     * @param params
     * @return
     */
    private String generateForm(Map<String, Object> params) {
        String FormString = "<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form  id=\"actform\" name=\"actform\" method=\"post\" action=\""
                + gatwayUrl + "\">";
        for (String key : params.keySet()) {
            if (!StringUtils.isNullOrEmpty(params.get(key).toString()))
                FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" + params.get(key) + "'>\r\n";
        }
        FormString += "</form></body>";
        logger.info("新金彩支付表单:" + FormString);
        return FormString;
    }

    public Map<String, Object> executeAndCheckSign(Map<String, Object> paramMap, String action) throws Exception {
        Map<String, Object> resMap = execute(paramMap, action);
        RSA.checkSign(resMap, serverPublicKey);
        logger.info("验签成功。");
        logger.info("新金彩支付响应:" + JSON.toJSONString(resMap));
        return resMap;
    }

    public Map<String, Object> execute(Map<String, Object> paramMap, String action) throws Exception {
        return HttpUtil.doPost(payUrl + action, paramMap);
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
    
    @Override
    public String callback(Map<String, String> paramMap) {
        
        Map<String, Object> data = new HashMap<>();
        Iterator<String> keys = paramMap.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            Object obj = paramMap.get(key);
            data.put(key, obj);
        }
        System.out.println(JSON.toJSONString(data));
        try {
            RSA.checkSign(data, serverPublicKey);
            return "success";
        } catch (Exception e) {
            return "";
        }
    }
}
