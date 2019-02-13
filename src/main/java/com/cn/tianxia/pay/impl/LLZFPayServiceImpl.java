package com.cn.tianxia.pay.impl;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.qft.util.HttpUtil;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.RandomUtils;
import com.cn.tianxia.pay.ys.util.DateUtil;

import net.sf.json.JSONObject;
/**
 * 连连支付
 * @author TX
 */
public class LLZFPayServiceImpl implements PayService {

    private final static Logger logger = LoggerFactory.getLogger(LLZFPayServiceImpl.class);
    /** 支付地址 */
    private String payUrl;

    /** 加密因子,md5_key */
    private String secret;
    /** 商户号 */
    private String oid_partner;
    /** 通知地址 */
    private String notify_url;
    /** 商品名称 */
    private String name_goods;
    /** 商品描述 */
    private String info_order;

    private String scan_url;

    public LLZFPayServiceImpl(Map<String, String> pmap) {
        if (pmap != null) {
            if (pmap.containsKey("payUrl")) {
                this.payUrl = pmap.get("payUrl");
            }
            if (pmap.containsKey("name_goods")) {
                this.name_goods = pmap.get("name_goods");
            } else {
                this.name_goods = "";
            }
            if (pmap.containsKey("info_order")) {
                this.info_order = pmap.get("info_order");
            } else {
                this.info_order = "";
            }
            if (pmap.containsKey("secret")) {
                this.secret = pmap.get("secret");
            }
            if (pmap.containsKey("oid_partner")) {
                this.oid_partner = pmap.get("oid_partner");
            }
            if (pmap.containsKey("notify_url")) {
                this.notify_url = pmap.get("notify_url");
            }
            if (pmap.containsKey("scan_url")) {
                this.scan_url = pmap.get("scan_url");
            }
        }
    }

    public static void main(String[] args) {
        // 1,初始化支持平台配置
        Map pmap = new HashMap<String, Object>();
        // returnMap是从数据查询来的，怎么配置数据库？
        pmap.put("payUrl", "http://yiapi.lianlianspc.com/gateway/bankgateway");
        pmap.put("name_goods", "TXWL");
        pmap.put("info_order", "something is wrong");
        pmap.put("md5_key", "b5ab28f0170f216e");
        pmap.put("oid_partner", "201805271741040432");
        pmap.put("notify_url", "yu.lianlianspc.com");
        pmap.put("scan_url", "http://yiapi.lianlianspc.com/gateway/yigateway");

        System.out.println("JSON配置:" + JSONObject.fromObject(pmap));
        LLZFPayServiceImpl ll = new LLZFPayServiceImpl(pmap);

        // 2,填充实体
        PayEntity payEntity = new PayEntity();
        // 此参数用于区别手机h5 和pc
        payEntity.setMobile("");
        payEntity.setIp("192.168.0.11");
        payEntity.setRefererUrl("http://baidu.com");
        payEntity.setPayCode("48");
        payEntity.setAmount(200);
        payEntity.setOrderNo("TX" + System.currentTimeMillis());

        // 3,调用支付接口
        ll.scanPay(payEntity);
    }

    /**
     * @Description pc扫码
     * @param payEntity
     * @return
     */
    public JSONObject scanPay(PayEntity payEntity) {
    	logger.info("[连连支付] pc扫码支付开始.................");
        String outTradeNo = payEntity.getOrderNo();// new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());//
        // 商户系统订单号
        double amount = payEntity.getAmount();// "8.02";// 订单金额
        DecimalFormat dcf = new DecimalFormat("#######.00");
        //String returnurl = payEntity.getRefererUrl();// "http://www.baidu.com";
        String bankCode = payEntity.getPayCode();

        // 14位日期
        String currTime = DateUtil.getCurrentDate("yyyyMMddHHmmss");
        // 四位随机数
        String strRandom = RandomUtils.generateString(4);
        // 18位序列号,可以自行调整。
        String user_id = currTime + strRandom;

        Map<String, String> params = new TreeMap<>();
        params.put("oid_partner", this.oid_partner);
        params.put("notify_url", this.notify_url);
        //params.put("return_url", returnurl);

        params.put("user_id", user_id);
        params.put("sign_type", "MD5");

        params.put("no_order", outTradeNo);
        params.put("time_order", DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"));
        params.put("money_order", dcf.format(amount));
        params.put("name_goods", this.name_goods);
        params.put("info_order", this.info_order);
        // params.put("bank_code", bankCode);
        params.put("pay_type", "52");
        params.put("sign", generateSign(params));
        logger.info("[连连支付]pc 扫码支付请求参数:{}",params);
        String responseStr = null;
        try {
            responseStr = HttpUtil.post(scan_url, params, "utf-8");
            logger.info("连连支付扫码响应：" + responseStr);
            
            if(StringUtils.isBlank(responseStr)){
            	return getReturnJson("error","","连连支付请求无响应");
            }
            ////连连支付手机有问题
            return PayResponse.sm_form(payEntity, responseStr, "连连支付手机支付成功!");
           /* JSONObject resJson = JSONObject.fromObject(responseStr);
            if (resJson.containsKey("ret_code") && "0000".equals(resJson.getString("ret_code"))) {
                return getReturnJson("success", resJson.getString("dimension_url"), "二维码连接获取成功！");
            }*/
            /*if(resJson.containsKey("status") && "success".equals(resJson.getString("status"))){
            	if("52".equals(payEntity.getPayCode()) && resJson.containsKey("qrcode")){//手机扫码支付H5
            		return PayResponse.sm_link(payEntity, resJson.getString("qrcode"), "连连支付手机支付成功!");
            	}
            	return PayResponse.sm_form(payEntity, resJson.toString(), "连连支付pc支付成功!");
            }*/

        } catch (IOException e) {
            e.printStackTrace();
            return getReturnJson("error", responseStr, "二维码连接获取失败！");
        }

        //return getReturnJson("error", responseStr, "二维码连接获取失败！");
    }

    /**
     * @Description 手机接口渠道
     * @param payEntity
     * @return
     */
    public String wapPay(PayEntity payEntity) {
    	logger.info("[连连支付] 手机 wap 支付.......");
        // 商户系统订单号
        double amount = payEntity.getAmount();
        DecimalFormat dcf = new DecimalFormat("#######.00");
        String returnurl = payEntity.getRefererUrl();

        // 14位日期
        String currTime = DateUtil.getCurrentDate("yyyyMMddHHmmss");
        // 四位随机数
        String strRandom = RandomUtils.generateString(4);
        // 18位序列号,可以自行调整。
        String user_id = currTime + strRandom;

        Map<String, String> params = new TreeMap<>();
        params.put("oid_partner", this.oid_partner);
        params.put("notify_url", this.notify_url);
        params.put("return_url", returnurl);

        params.put("user_id", user_id);
        params.put("sign_type", "MD5");

        params.put("no_order", payEntity.getOrderNo());
        params.put("time_order", DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"));
        params.put("money_order", dcf.format(amount));
        params.put("name_goods", this.name_goods);
        params.put("info_order", this.info_order);
        params.put("pay_type", payEntity.getPayCode());
        params.put("sign", generateSign(params));

        String formStr = buildForm(params, this.payUrl);
        logger.info("连连支付wap表单:{}",formStr);
        return formStr;
    }

    @Override
    public JSONObject smPay(PayEntity payEntity) {
    	logger.info("[连连支付] 手机smPay支付.......");
        Double amount = payEntity.getAmount();
        String order_no = payEntity.getOrderNo();
        String userName = payEntity.getUsername();
        String mobile = payEntity.getMobile();

        // pc端
        if (StringUtils.isBlank(mobile)) {
            JSONObject r_json = scanPay(payEntity);
            if ("success".equals(r_json.getString("status"))) {
                return PayUtil.returnPayJson("success", "2", "支付接口请求成功!", userName, amount, order_no,
                        r_json.getString("qrCode"));
            } else {
                return PayUtil.returnPayJson("error", "2", r_json.getString("msg"), userName, amount, order_no, "");
            }
        } else {
            // 手机端
            String html = wapPay(payEntity);
            return PayUtil.returnPayJson("success", "1", "支付接口请求成功!", userName, amount, order_no, html);
        }

    }

    @Override
    public JSONObject wyPay(PayEntity payEntity) {
    	logger.info("[连连支付] 银联支付....start...");
    	
        double amount = payEntity.getAmount();// "8.02";// 订单金额
        DecimalFormat dcf = new DecimalFormat("0.00");
        String returnurl = payEntity.getRefererUrl();
        // 14位日期
        String currTime = DateUtil.getCurrentDate("yyyyMMddHHmmss");
        // 四位随机数
        String strRandom = RandomUtils.generateString(4);
        // 18位序列号,可以自行调整。
        String user_id = currTime + strRandom;

        Map<String, String> params = new TreeMap<>();
        params.put("oid_partner", this.oid_partner);
        params.put("notify_url", this.notify_url);
        params.put("return_url", returnurl);

        params.put("user_id", user_id);
        params.put("sign_type", "MD5");

        params.put("no_order", payEntity.getOrderNo());
        params.put("time_order", DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"));
        params.put("money_order", dcf.format(amount));
        params.put("name_goods", this.name_goods);
        params.put("info_order", this.info_order);
        params.put("bank_code", payEntity.getPayCode());
        params.put("pay_type", "11");
        params.put("sign", generateSign(params));
        logger.info("[连连支付]请求的地址:{},请求的参数:{}",payUrl,params);
        
        String formStr = buildForm(params, this.payUrl);// HttpUtil.RequestForm(payUrl, params);
        logger.info("[连连支付] form表单：" + formStr);

        return PayUtil.returnWYPayJson("success", "jsp", formStr, this.payUrl, "payhtml");
    }

    public String buildForm(Map<String, String> paramMap, String payUrl) {
    	logger.info("[连连支付] 开始调用 buildForm 方法....");
        // 待请求参数数组
        String FormString = "<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form  id=\"actform\" name=\"actform\" method=\"post\" action=\""
                + payUrl + "\">";
        for (String key : paramMap.keySet()) {
            FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" + paramMap.get(key) + "'>\r\n";
        }
        FormString += "</form></body>";
        logger.info("[连连支付] buildFrom 返回参数:{}",FormString);
        return FormString;
    }

    /**
     * 签名
     * @param params
     * @return
     */
    private String generateSign(Map<String, String> params) {
    	logger.info("[连连支付]开始签名..........");
        StringBuilder buf = new StringBuilder();

        for (String key : params.keySet()) {
            String value = params.get(key);
            if (value == null || value.trim().length() == 0) {
                continue;
            }
            buf.append(key).append("=").append(value).append("&");
        }
        String signatureStr = buf.substring(0, buf.length() - 1);
        signatureStr = signatureStr + this.secret;
        logger.info("[连连支付]待签名字符串:{}",signatureStr);
        String signature = null;
        try {
            signature = cryptMD5(signatureStr);
            logger.info("[连连支付]生成签名串:{}",signature);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[连连支付]签名生成失败");
        }
        return signature;
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
     * MD5加密
     */
    public String cryptMD5(String str) {
        if (str == null || str.length() == 0) {
            throw new IllegalArgumentException("String to encript cannot be null or zero length");
        }
        StringBuffer hexString = new StringBuffer();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            byte[] hash = md.digest();
            for (int i = 0; i < hash.length; i++) {
                if ((0xff & hash[i]) < 0x10) {
                    hexString.append("0" + Integer.toHexString((0xFF & hash[i])));
                } else {
                    hexString.append(Integer.toHexString(0xFF & hash[i]));
                }
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hexString.toString();
    }

    /**
     * 回调验签
     * 
     * @param infoMap
     * @return
     */
    @Override
    public String callback(Map<String, String> infoMap) {
        StringBuilder sb = new StringBuilder();
        for (String key : infoMap.keySet()) {
            if ("sign".equalsIgnoreCase(key)) {
                continue;
            }
            String value = String.valueOf(infoMap.get(key));
            if (StringUtils.isBlank(value)) {
                continue;
            }
            sb.append(key + "=" + value + "&");
        }
        String signatureStr = sb.substring(0, sb.length() - 1);
        signatureStr = signatureStr + this.secret;
        logger.info("验签内容signatureStr = " + signatureStr);
        String result = "";
        try {
            result = cryptMD5(signatureStr);
            logger.info("生成签名串：" + result);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("签名生成失败");
        }

        String sign = infoMap.get("sign");
        if (sign.equals(result)) {
            logger.info("验签成功");
            return "success";
        }
        logger.info("验签失败");
        return "fail";
    }
}
