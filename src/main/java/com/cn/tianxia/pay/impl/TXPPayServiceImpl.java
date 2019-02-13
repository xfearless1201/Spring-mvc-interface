package com.cn.tianxia.pay.impl;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.gst.util.StringUtils;
import com.cn.tianxia.pay.service.PayService;

import net.sf.json.JSONObject;

/**
 * @ClassName TXPPayServiceImpl
 * @Description 天信支付(接入支付宝pc 支付宝h5)
 * @author zw
 * @Date 2018年7月17日 下午1:39:07
 * @version 1.0.0
 */
public class TXPPayServiceImpl implements PayService {

    private String payUrl;

    private String pay_memberid;
    private String keyValue;// MD5key
    private String pay_notifyurl;
    private String pay_productname;

    private final static Logger logger = LoggerFactory.getLogger(TXPPayServiceImpl.class);

    public TXPPayServiceImpl(Map<String, String> pmap) {
        JSONObject jo = JSONObject.fromObject(pmap);
        if (null != pmap) {
            payUrl = jo.get("payUrl").toString();
            pay_memberid = jo.get("pay_memberid").toString();
            keyValue = jo.get("keyValue").toString();
            pay_notifyurl = jo.get("pay_notifyurl").toString();
            pay_productname = jo.get("pay_productname").toString();
        }
    }

    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JSONObject smPay(PayEntity payEntity) {
        Double amount = payEntity.getAmount();
        String refereUrl = payEntity.getRefererUrl();
        String pay_code = payEntity.getPayCode();
        // String mobile = payEntity.getMobile();
        String userName = payEntity.getUsername();
        // String ip = payEntity.getIp();

        Map<String, String> map = new HashMap<>();
        DecimalFormat df = new DecimalFormat("############");
        String total_fee = df.format(amount);
        map.put("pay_bankcode", pay_code);
        String order_no = generateOrderId();
        payEntity.setOrderNo(order_no);
        map.put("pay_orderid", order_no);
        map.put("pay_callbackurl", refereUrl);
        map.put("pay_amount", total_fee);

        String form = scanPay(map);
        return PayUtil.returnPayJson("success", "1", "支付接口请求成功!", userName, amount, order_no, form);
    }

    public static String generateOrderId() {
        String keyup_prefix = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String keyup_append = String.valueOf(new Random().nextInt(899999) + 100000);
        String pay_orderid = keyup_prefix + keyup_append;// 订单号
        return pay_orderid;
    }

    public static String generateTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    /**
     * @Description Post网关支付
     * @param scanMap
     * @return
     */
    public String scanPay(Map<String, String> scanMap) {
        String pay_bankcode = scanMap.get("pay_bankcode");
        String pay_orderid = scanMap.get("pay_orderid");
        String pay_callbackurl = scanMap.get("pay_callbackurl");
        String pay_amount = scanMap.get("pay_amount");

        String pay_applydate = generateTime();

        String stringSignTemp = "pay_amount=" + pay_amount + "&pay_applydate=" + pay_applydate + "&pay_bankcode="
                + pay_bankcode + "&pay_callbackurl=" + pay_callbackurl + "&pay_memberid=" + pay_memberid
                + "&pay_notifyurl=" + pay_notifyurl + "&pay_orderid=" + pay_orderid + "&key=" + keyValue + "";

        String pay_md5sign = null;

        logger.info("待签名字符串:" + stringSignTemp);
        try {
            pay_md5sign = md5(stringSignTemp);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        Map<String, Object> params = new HashMap<>();
        params.put("pay_memberid", pay_memberid);
        params.put("pay_orderid", pay_orderid);
        params.put("pay_applydate", pay_applydate);
        params.put("pay_bankcode", pay_bankcode);
        params.put("pay_notifyurl", pay_notifyurl);
        params.put("pay_callbackurl", pay_callbackurl);
        params.put("pay_amount", pay_amount);
        params.put("pay_productname", pay_productname);
        // params.put("pay_productnum", pay_productnum);
        // params.put("pay_productdesc", pay_productdesc);
        // params.put("pay_producturl", pay_producturl);
        params.put("pay_md5sign", pay_md5sign);
        return generateForm(params);
    }

    /**
     * @Description md5
     * @param str
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String md5(String str) throws NoSuchAlgorithmException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            byte[] byteDigest = md.digest();
            int i;
            // 字符数组转换成字符串
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < byteDigest.length; offset++) {
                i = byteDigest[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            // 32位加密
            return buf.toString().toUpperCase();
            // 16位的加密
            // return buf.toString().substring(8, 24).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @Description 生成表单
     * @param params
     * @return
     */
    private String generateForm(Map<String, Object> params) {
        String FormString = "<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form  id=\"actform\" name=\"actform\" method=\"post\" action=\""
                + payUrl + "\">";
        for (String key : params.keySet()) {
            if (!StringUtils.isNullOrEmpty(params.get(key).toString()))
                FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" + params.get(key) + "'>\r\n";
        }
        FormString += "</form></body>";
        logger.info("天信支付表单:" + FormString);
        return FormString;
    }

    /**
     * @Description 回调验签方法
     * @param request
     * @return
     */
    @Override
    public String callback(Map<String, String> request) {
        String memberid = request.get("memberid");
        String orderid = request.get("orderid");
        String amount = request.get("amount");
        String datetime = request.get("datetime");
        String returncode = request.get("returncode");
        String transaction_id = request.get("transaction_id");
        // String attach = request.get("attach");
        String sign = request.get("sign");
        String SignTemp = "amount=" + amount + "&datetime=" + datetime + "&memberid=" + memberid + "&orderid=" + orderid
                + "&returncode=" + returncode + "&transaction_id=" + transaction_id + "&key=" + keyValue + "";
        String md5sign = null;
        try {
            md5sign = md5(SignTemp);
        } catch (NoSuchAlgorithmException e) {
            logger.info("md5签名异常");
            e.printStackTrace();
            return "";
        } // MD5加密

        logger.info("localSign:" + md5sign + "        serviceSign:" + sign);

        if (sign.equalsIgnoreCase(md5sign)) {
            return "success";
        }
        return "";
    }

}
