package com.cn.tianxia.pay.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.gst.util.StringUtils;
import com.cn.tianxia.pay.qyf.util.ToolKit;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.txkj.util.HttpClientUtil;
import com.cn.tianxia.pay.txkj.util.SignUtil;

import net.sf.json.JSONObject;

/**
 * @ClassName TXKJPayServiceImpl
 * @Description 天下科技支付
 * @author zw
 * @Date 2018年5月21日 下午7:07:02
 * @version 1.0.0
 */
public class TXKJPayServiceImpl implements PayService {

    private String mer_no;
    private String pay_web_url;
    private String scan_Pay_url;
    private String pay_key;
    private String back_url;
    private String sign_type;
    private String service_type;
    private final static Logger logger = LoggerFactory.getLogger(TXKJPayServiceImpl.class);

    public TXKJPayServiceImpl(Map<String, String> pmap) {
        JSONObject jo = JSONObject.fromObject(pmap);
        if (null != pmap) {
            mer_no = jo.get("mer_no").toString();
            pay_web_url = jo.get("pay_web_url").toString();
            pay_key = jo.get("pay_key").toString();
            scan_Pay_url = jo.get("scan_Pay_url").toString();
            back_url = jo.get("back_url").toString();
            sign_type = jo.get("sign_type").toString();// MD5
            service_type = jo.get("service_type").toString();// b2c
        }
    }

    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        Double amount = payEntity.getAmount();
        String order_no = payEntity.getOrderNo();
        String refereUrl = payEntity.getRefererUrl();
        String pay_code = payEntity.getPayCode();
        String pay_url = payEntity.getPayUrl();

        Map<String, String> map = new HashMap<String, String>();
        map.put("service_type", service_type);// 网银为固定值
        map.put("trade_amount", String.valueOf(amount));
        map.put("mer_order_no", order_no);
        map.put("channel_code", pay_code);
        map.put("page_url", refereUrl);

        String html = bankPay(map);
        return PayUtil.returnWYPayJson("success", "form", html, pay_url, "");

    }

    @Override
    public JSONObject smPay(PayEntity payEntity) {
        Double amount = payEntity.getAmount();
        String order_no = payEntity.getOrderNo();
        String refereUrl = payEntity.getRefererUrl();
        String pay_code = payEntity.getPayCode();
        // String pay_url = payEntity.getPayUrl();
        String userName = payEntity.getUsername();
        String mobile = payEntity.getMobile();

        Map<String, String> map = new HashMap<String, String>();
        map.put("service_type", pay_code);// 支付类型
        map.put("trade_amount", String.valueOf(amount));
        map.put("mer_order_no", order_no);
        map.put("page_url", refereUrl);

        String html = "";
        // 快捷支付
        if ("quick-web".equals(pay_code)) {
            html = bankPay(map);
            return PayUtil.returnPayJson("success", "1", "支付接口请求成功!", userName, amount, order_no, html);
        }

        // 手机 or pc 返回类型
        if (!StringUtils.isNullOrEmpty(mobile)) {
            html = bankPay(map);
            return PayUtil.returnPayJson("success", "1", "支付接口请求成功!", userName, amount, order_no, html);
        } else {

            JSONObject rjson = scanPay(map);
            if (!"success".equals(rjson.getString("status"))) {
                return PayUtil.returnPayJson("error", "2", rjson.getString("msg"), userName, amount, order_no, "");
            }
            String qrcode = rjson.getString("qrCode");
            if (rjson.containsKey("qrCode") && !"null".equals(qrcode) && !StringUtils.isNullOrEmpty(qrcode)) {
                return PayUtil.returnPayJson("success", "2", "支付接口请求成功!", userName, amount, order_no, qrcode);
            } else {
                return PayUtil.returnPayJson("error", "2", rjson.toString(), userName, amount, order_no, "");
            }

        }
    }

    /***
     * @Description 表单方式支持业务类型 quick-web(快捷H5) b2c weixin_wap(微信H5) alipay_wap(支付宝H5)
     * @param bankMap
     * @return
     */
    public String bankPay(Map<String, String> bankMap) {
        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("mer_no", mer_no);
        paramMap.put("mer_order_no", bankMap.get("mer_order_no"));
        paramMap.put("channel_code", bankMap.get("channel_code"));
        // paramMap.put("card_no", bankMap.get("card_no"));//快捷支付时，用户需要输入银行卡号
        paramMap.put("trade_amount", bankMap.get("trade_amount"));
        paramMap.put("service_type", bankMap.get("service_type"));

        paramMap.put("order_date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        paramMap.put("page_url", bankMap.get("page_url"));
        paramMap.put("back_url", back_url);
        String signStr = SignUtil.sortData(paramMap);
        // String sign = com.mpay.signutils.Md5Util.MD5Encode(signStr, pay_key);
        signStr += "&key=" + pay_key;
        logger.info("签名原串:" + signStr);
        String sign = ToolKit.MD5(signStr, "UTF-8");
        paramMap.put("sign_type", sign_type);// MD5
        paramMap.put("sign", sign);
        logger.info("signStr:" + signStr);
        logger.info("sign:" + sign);
        String FormString = "<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form  id=\"actform\" name=\"actform\" method=\"post\" action=\""
                + pay_web_url + "\">";
        for (String key : paramMap.keySet()) {
            if (!StringUtils.isNullOrEmpty(paramMap.get(key)))
                FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" + paramMap.get(key) + "'>\r\n";
        }
        FormString += "</form></body>";
        logger.info("天下科技支付表单:" + FormString);
        return FormString;
    }

    /**
     * @Description 扫码接口支持类型 weixin_scan(微信)，qq_scan(QQ)，alipay_scan(支付宝),jd_scan（京东）
     * @param scanMap
     * @return
     */
    public JSONObject scanPay(Map<String, String> scanMap) {
        String result = "";
        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("mer_no", mer_no);
        paramMap.put("mer_order_no", scanMap.get("mer_order_no"));
        paramMap.put("trade_amount", scanMap.get("trade_amount"));
        paramMap.put("service_type", scanMap.get("service_type"));
        paramMap.put("page_url", scanMap.get("page_url"));
        paramMap.put("order_date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        paramMap.put("back_url", back_url);
        String signStr = SignUtil.sortData(paramMap);
        // String sign = com.mpay.signutils.Md5Util.MD5Encode(signStr, pay_key);
        signStr += "&key=" + pay_key;
        logger.info("签名原串:" + signStr);
        String sign = ToolKit.MD5(signStr, "UTF-8");

        paramMap.put("sign_type", sign_type);
        paramMap.put("sign", sign);
        logger.info("signStr:" + signStr);
        logger.info("sign:" + sign);
        result = HttpClientUtil.post(scan_Pay_url, paramMap);
        logger.info("result:" + result);
        try {
            JSONObject rjson = JSONObject.fromObject(result);
            // 请求参数成功
            if (rjson.containsKey("auth_result") && "success".equals(rjson.getString("auth_result"))
                    && rjson.containsKey("trade_result") && "3".equals(rjson.getString("trade_result"))) {
                return getReturnJson("success", rjson.getString("trade_return_msg"), "获取二维码地址成功！");
            } else {
                return getReturnJson("error", "", result);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return getReturnJson("error", "", result);
        }
    }

    /**
     * @Description 解析响应参数
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
     * @Description 回调验签方法
     * @param infoMap
     * @return
     */
    @Override
    public String callback(Map<String, String> infoMap) {
        String sign = infoMap.get("sign");
        infoMap.remove("sign");
        infoMap.remove("sign_type");

        String signStr = SignUtil.sortData(infoMap);
        signStr += "&key=" + pay_key;
        logger.info("签名原串:" + signStr);
        String sig = ToolKit.MD5(signStr, "UTF-8");

        if (sign.equals(sig)) {
            return "success";
        }
        return "";
    }
}
