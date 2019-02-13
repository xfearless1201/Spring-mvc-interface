package com.cn.tianxia.pay.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.qyf.util.ToolKit;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.xf.util.IoUtil;
import com.cn.tianxia.pay.xf.util.RsaUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.sf.json.JSONObject;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * @ClassName XFPayServiceImpl
 * @Description 行付支付
 * @author zw
 * @Date 2018年8月1日 下午1:56:31
 * @version 1.0.0
 */
public class XFPayServiceImpl implements PayService {

    public String publicKey_str; // 公钥
    private String pay_url; // 支付地址
    private String merId; // 请求方的合作编号
    private String signType;// 加密类型
    private String merchantCode;// 商户编号
    private String terminalCode; // 平台商户终端编号
    private String merchantName; // 收款商户名称
    private String commodityName; // 商品名称
    private String limitPay;
    private String notifyUrl;// 异步通知地址

    private final static Logger logger = LoggerFactory.getLogger(XFPayServiceImpl.class);

    public XFPayServiceImpl(Map<String, String> pmap) {
        if (pmap != null) {
            if (pmap.containsKey("publicKey_str")) {
                this.publicKey_str = pmap.get("publicKey_str");
            }
            if (pmap.containsKey("pay_url")) {
                this.pay_url = pmap.get("pay_url");
            }
            if (pmap.containsKey("merId")) {
                this.merId = pmap.get("merId");
            }
            if (pmap.containsKey("signType")) {
                this.signType = pmap.get("signType");
            }
            if (pmap.containsKey("merchantCode")) {
                this.merchantCode = pmap.get("merchantCode");
            }
            if (pmap.containsKey("terminalCode")) {
                this.terminalCode = pmap.get("terminalCode");
            }

            if (pmap.containsKey("merchantName")) {
                this.merchantName = pmap.get("merchantName");
            }
            if (pmap.containsKey("commodityName")) {
                this.commodityName = pmap.get("commodityName");
            }
            if (pmap.containsKey("limitPay")) {
                this.limitPay = pmap.get("limitPay");
            }
            if (pmap.containsKey("notifyUrl")) {
                this.notifyUrl = pmap.get("notifyUrl");
            }
        }
    }

    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JSONObject smPay(PayEntity payEntity) {
        double amount = payEntity.getAmount();// "8.02";// 订单金额
        String userName = payEntity.getUsername();
        String payCode = payEntity.getPayCode();
        String mobile = payEntity.getMobile();
        String order_no = payEntity.getOrderNo();

        JSONObject r_json = null;
        // pc端
        if (StringUtils.isBlank(mobile)) {
            r_json = scanPay(payEntity);
        } else {
            r_json = h5Pay(payEntity);
        }

        if ("success".equals(r_json.getString("status"))) {
            if (StringUtils.isBlank(mobile)) {
                return PayUtil.returnPayJson("success", "2", "支付接口请求成功!", userName, amount, payEntity.getOrderNo(),
                        r_json.getString("qrCode"));
            } else {
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
     * @param payEntity
     * @return
     */
    public JSONObject scanPay(PayEntity payEntity) {
        String orderNum = payEntity.getOrderNo(); // 商户系统订单号
        double amount = payEntity.getAmount();
        DecimalFormat df = new DecimalFormat("#########");
        String transMoney = df.format(amount * 100);
        // String returnurl = payEntity.getRefererUrl();
        String pay_id = payEntity.getPayCode();
        String payCode = payEntity.getPayCode();

        Map<String, String> reqData_map = new HashMap<String, String>();
        // 公共参数
        reqData_map.put("groupId", merId);// 请求方的合作编号
        reqData_map.put("service", payCode);// 请求的交易服务码
        reqData_map.put("signType", signType);// 签名类型（RSA）
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        reqData_map.put("datetime", sdf.format(new Date())); // 系统时间（yyyyMMddHHmmss）
        // 接口参数
        Map<String, String> reqData2_map = new HashMap<String, String>();
        reqData2_map.put("merchantCode", merchantCode);// 平台商户编号
        reqData2_map.put("merchantSubCode", "");// 平台商户子商户号，非必填
        reqData2_map.put("terminalCode", terminalCode);// 平台商户终端编号
        reqData2_map.put("orderNum", orderNum);// 合作商订单号，全局唯一
        reqData2_map.put("transMoney", transMoney);// 交易金额，单位分
        reqData2_map.put("notifyUrl", notifyUrl);// 支付结果异步通知地址
        reqData2_map.put("merchantName", merchantName);// 收款商户名称
        reqData2_map.put("commodityName", commodityName);// 商品名称（如不填使用收款商户名称） 非必填
        reqData2_map.put("merchantNum", ToolKit.randomStr(5));// 商户门店编号
        reqData2_map.put("terminalNum", ToolKit.randomStr(5));// 商户机具终端编号
        reqData2_map.put("limitPay", limitPay);// 是否可以使用信用卡支付，填写no_credit表示不能使用信用卡，不填表示可以使用信用卡，非必填
        String responseStr = "";
        try {
            responseStr = pay(reqData_map, reqData2_map);
            JSONObject resposeJson = JSONObject.fromObject(paramToMap(responseStr));
            if (resposeJson.containsKey("pl_url") && StringUtils.isNotBlank(resposeJson.getString("pl_url"))) {
                String pl_url = URLDecoder.decode(resposeJson.getString("pl_url"));
                return getReturnJson("success", pl_url, "二维码连接获取成功！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("行付扫码异常");
            return getReturnJson("error", responseStr, "二维码连接获取失败！");
        }
        return getReturnJson("error", responseStr, "二维码连接获取失败！");
    }

    /**
     * @Description 手机端支付接口
     * @param payEntity
     * @return
     */
    public JSONObject h5Pay(PayEntity payEntity) {
        String orderNum = payEntity.getOrderNo(); // 商户系统订单号
        double amount = payEntity.getAmount();
        DecimalFormat df = new DecimalFormat("#########");
        String transMoney = df.format(amount * 100);
        // String returnurl = payEntity.getRefererUrl();
        String pay_id = payEntity.getPayCode();
        String payCode = payEntity.getPayCode();

        Map<String, String> reqData_map = new HashMap<String, String>();
        // 公共参数
        reqData_map.put("groupId", merId);// 请求方的合作编号
        reqData_map.put("service", payCode);// 请求的交易服务码
        reqData_map.put("signType", signType);// 签名类型（RSA）
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        reqData_map.put("datetime", sdf.format(new Date())); // 系统时间（yyyyMMddHHmmss）
        // 接口参数
        Map<String, String> reqData2_map = new HashMap<String, String>();
        reqData2_map.put("merchantCode", merchantCode);// 平台商户编号
        reqData2_map.put("merchantSubCode", "");// 平台商户子商户号，非必填
        reqData2_map.put("terminalCode", terminalCode);// 平台商户终端编号
        reqData2_map.put("orderNum", orderNum);// 合作商订单号，全局唯一
        reqData2_map.put("transMoney", transMoney);// 交易金额，单位分
        reqData2_map.put("notifyUrl", notifyUrl);// 支付结果异步通知地址
        reqData2_map.put("merchantName", merchantName);// 收款商户名称
        reqData2_map.put("commodityName", commodityName);// 商品名称（如不填使用收款商户名称） 非必填
        reqData2_map.put("merchantNum", ToolKit.randomStr(5));// 商户门店编号
        reqData2_map.put("terminalNum", ToolKit.randomStr(5));// 商户机具终端编号
        // reqData2_map.put("limitPay", limitPay);// 是否可以使用信用卡支付，填写no_credit表示不能使用信用卡，不填表示可以使用信用卡，非必填
        reqData2_map.put("returnUrl", payEntity.getRefererUrl());// 支付成功、支付失败、取消支付后跳转地址
        String responseStr = "";
        try {
            responseStr = pay(reqData_map, reqData2_map);
            JSONObject resposeJson = JSONObject.fromObject(paramToMap(responseStr));
            if (resposeJson.containsKey("pl_url") && StringUtils.isNotBlank(resposeJson.getString("pl_url"))) {

                String pl_url = URLDecoder.decode(resposeJson.getString("pl_url"));
                return getReturnJson("success", pl_url, "二维码连接获取成功！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("手机会支付异常");
            return getReturnJson("error", responseStr, "二维码连接获取失败！");
        }
        return getReturnJson("error", responseStr, "二维码连接获取失败！");
    }

    private String pay(Map<String, String> reqData_map, Map<String, String> reqData2_map)
            throws UnsupportedEncodingException, IOException {
        String reqData2_str = "";
        for (Entry<String, String> entry : reqData2_map.entrySet()) {
            {
                reqData2_str = reqData2_str + entry.getKey() + "=" + entry.getValue() + "&";
            }
        }
        String reqData_sign_str = reqData2_str.substring(0, reqData2_str.length() - 1);
        logger.info("加密前数据：" + reqData_sign_str);

        String reqData_sign = base64Encode(RsaUtil.encrypt(publicKey_str, reqData_sign_str.getBytes("UTF-8")));
        logger.info("加密后数据：" + reqData_sign);

        // 公共参数，sign
        reqData_map.put("sign", reqData_sign); // 数据的签名字符串

        String reqData_str = "";
        for (Entry<String, String> entry : reqData_map.entrySet()) {
            reqData_str = reqData_str + entry.getKey() + "=" + entry.getValue() + "&";
        }
        logger.info("请求的参数：" + reqData_str);

        String respData_str = post(pay_url, reqData_str);
        logger.info("返回的参数：" + respData_str);

        Map<String, String> respData_map = new Gson().fromJson(respData_str, new TypeToken<Map<String, String>>() {
        }.getType());

        for (Entry<String, String> entry : respData_map.entrySet()) {
            logger.info("返回的参数明细：" + entry.getKey() + "=" + entry.getValue());
        }

        String respData_pl_sign = respData_map.get("pl_sign");
        logger.info("解密前数据：" + respData_pl_sign);

        String respData_pl_sign_str = new String(RsaUtil.verify(publicKey_str, base64Decode(respData_pl_sign)),
                "UTF-8");
        logger.info("解密后数据：" + respData_pl_sign_str);

        return respData_pl_sign_str;
    }

    public static String post(String url, String request) {
        OutputStream oos = null;
        InputStream iis = null;
        String response = null;
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setAllowUserInteraction(true);

            oos = httpURLConnection.getOutputStream();
            oos.write(request.toString().getBytes("UTF-8"));
            oos.flush();

            iis = httpURLConnection.getInputStream();
            response = IoUtil.readInputStream(iis, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } // 关闭OutputStream[END]
            if (iis != null) {
                try {
                    iis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } // 关闭InputStream[END]
        }
        return response;
    }

    public static String base64Encode(byte[] needEncode) {
        String encoded = null;
        if (needEncode != null) {
            encoded = new BASE64Encoder().encode(needEncode);
        }
        return encoded;
    }

    public static byte[] base64Decode(String needDecode) throws IOException {
        byte[] decoded = null;
        if (needDecode != null) {
            decoded = new BASE64Decoder().decodeBuffer(needDecode);
        }
        return decoded;
    }

    public static Map<String, String> paramToMap(String paramStr) {
        String[] params = paramStr.split("&");
        Map<String, String> resMap = new HashMap<String, String>();
        for (int i = 0; i < params.length; i++) {
            String[] param = params[i].split("=");
            if (param.length >= 2) {
                String key = param[0];
                String value = param[1];
                for (int j = 2; j < param.length; j++) {
                    value += "=" + param[j];
                }
                resMap.put(key, value);
            }
        }
        return resMap;
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
    public String callback(Map<String, String> data) {
        // TODO Auto-generated method stub
        return "success";
    }

//    public String queryOrder() {
//        Map<String, String> reqData_map = new HashMap<String, String>();
//        // 公共参数
//        reqData_map.put("groupId", merId);// 请求方的合作编号
//        reqData_map.put("service", "SMZF006");// 请求的交易服务码
//        reqData_map.put("signType", signType);// 签名类型（RSA）
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
//        reqData_map.put("datetime", sdf.format(new Date())); // 系统时间（yyyyMMddHHmmss）
//        // 接口参数
//        Map<String, String> reqData2_map = new HashMap<String, String>();
//        reqData2_map.put("merchantCode", merchantCode);// 平台商户编号
//        reqData2_map.put("orderNum", "XFbl1201808012135162135166795");// 平台商户子商户号，非必填
//        String responseStr = "";
//        try {
//            responseStr = pay(reqData_map, reqData2_map);
//            JSONObject resposeJson = JSONObject.fromObject(paramToMap(responseStr));
//            logger.info(resposeJson.toString());
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            logger.info("查询接口异常");
//        }
//
//        return "";
//    }


}
