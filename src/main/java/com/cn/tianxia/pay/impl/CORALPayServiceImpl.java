/****************************************************************** 
 *
 * Powered By tianxia-online. 
 *
 * Copyright (c) 2018-2020 Digital Telemedia 天下网络 
 * http://www.d-telemedia.com/ 
 *
 * Package: com.cn.tianxia.pay.impl 
 *
 * Filename: CORALPayServiceImpl.java
 *
 * Description: yhh澳门银河对接-珊瑚支付
 *
 * Copyright: Copyright (c) 2018-2020 
 *
 * Company: 天下网络科技 
 *
 * @author: Elephone
 *
 * @version: 1.0.0
 *
 * Create at: 2018年08月31日 14:51 
 *
 * Revision: 
 *
 * 2018/8/31 14:51 
 * - first revision 
 *
 *****************************************************************/
package com.cn.tianxia.pay.impl;

import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.mqzf.util.MD5;
import com.cn.tianxia.pay.service.PayService;

import net.sf.json.JSONObject;

/**
 * @ClassName CORALPayServiceImpl
 * @Description yhh澳门银河对接-珊瑚支付
 * @Author Elephone
 * @Date 2018年08月31日 14:51
 * @Version 1.0.0
 **/
public class CORALPayServiceImpl implements PayService {
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(CORALPayServiceImpl.class);
    static String pay_memberid ;    // 商户编号
    static String pay_notifyurl ;   // 服务端通知
    static String key ;   // 密钥
    static String pay_url ;   // 支付url
    public CORALPayServiceImpl(Map<String, String> pmap) {
        if (pmap != null) {
            if (pmap.containsKey("pay_memberid")) {
                pay_memberid = pmap.get("pay_memberid");
            }
            if (pmap.containsKey("pay_notifyurl")) {
                pay_notifyurl = pmap.get("pay_notifyurl");
            }
            if (pmap.containsKey("key")) {
                key = pmap.get("key");
            }
            if (pmap.containsKey("pay_url")) {
                pay_url = pmap.get("pay_url");
            }
        }
    }

    public static void main(String[] args) {
        // 1,初始化支持平台配置
        Map pmap = new HashMap<String, Object>();
        pmap.put("pay_memberid", "10768");
        pmap.put("pay_url", "https://pay.hnshunqi.com/Pay_Index.html");
        pmap.put("pay_notifyurl", "https://txw.tx8899.com/YHH/Notify/CORALNotify.do");
        pmap.put("key", "o5hm6wnx17kgu1jtyvsbsor4p1b3nq29");

        System.out.println("JSON配置:" + JSONObject.fromObject(pmap));
    }
    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        return null;
    }

    @Override
    public JSONObject smPay(PayEntity payEntity) {
        String topay = payEntity.getTopay();
        double amount = payEntity.getAmount();// 订单金额
        DecimalFormat dcf = new DecimalFormat("0.00");
        String pay_amount = dcf.format(amount);
        String pay_bankcode = payEntity.getPayCode();
        String pay_callbackurl = payEntity.getRefererUrl();
        String mobile = payEntity.getMobile();

        Map<String, String> params = new TreeMap<>();
        // 平台分配商户号
        params.put("pay_memberid", pay_memberid);
        // 订单号唯一, 字符长度20
        String pay_orderid=payEntity.getOrderNo();
        params.put("pay_orderid", pay_orderid);
        // 时间格式：2016-12-26 18:18:18
        String pay_applydate=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        params.put("pay_applydate", pay_applydate);
        // 银行编码
        params.put("pay_bankcode", pay_bankcode);
        // 服务端通知
        params.put("pay_notifyurl", pay_notifyurl);
        // 页面跳转通知
        params.put("pay_callbackurl", pay_callbackurl);
        // 订单金额
        params.put("pay_amount", pay_amount);

        String stringSignTemp="pay_amount="+pay_amount+"&pay_applydate="+pay_applydate+"&pay_bankcode="+pay_bankcode+"&pay_callbackurl="+pay_callbackurl+"&pay_memberid="+pay_memberid+"&pay_notifyurl="+pay_notifyurl+"&pay_orderid="+pay_orderid+"&key="+key+"";
        params.put("pay_productname", "game_recharge");
        params.put("pay_productnum", "");
        params.put("pay_productdesc", "");
        params.put("pay_producturl", "");
        try {
            String sign = MD5.md5(stringSignTemp);
            params.put("pay_md5sign", sign);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        // pc端
        if (StringUtils.isBlank(mobile)) {
            String formStr = buildForm(params, pay_url);// HttpUtil.RequestForm(payUrl, params);
            logger.info("支付form表单：" + formStr);
            return PayUtil.returnPayJson("success", "1", "支付接口请求成功!", "", amount, pay_orderid,
                    formStr);
        } else {
            // 手机端
            String formStr = buildForm(params, pay_url);// HttpUtil.RequestForm(payUrl, params);
            logger.info("支付form表单：" + formStr);
            return PayUtil.returnPayJson("success", "1", "支付接口请求成功!", "", amount, pay_orderid,
                    formStr);
        }
    }

    public String buildForm(Map<String, String> paramMap, String payUrl) {
        // 待请求参数数组
        String FormString = "<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form  id=\"actform\" name=\"actform\" method=\"post\" action=\""
                + payUrl + "\">";
        for (String key : paramMap.keySet()) {
            FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" + paramMap.get(key) + "'>\r\n";
        }
        FormString += "</form></body>";

        return FormString;
    }

    @Override
    public String callback(Map<String,String> infoMap) {
        try {
            String sourceSign =  infoMap.get("sign").toLowerCase();
            logger.info("[CORAL]删除支付回调请求原签名:"+sourceSign);
            //生成待签名串
            StringBuffer sb = new StringBuffer();
            sb.append("amount="+infoMap.get("amount")+"&").append("datetime="+infoMap.get("datetime")+"&");
            sb.append("memberid="+infoMap.get("memberid")+"&").append("orderid="+infoMap.get("orderid")+"&");
            sb.append("returncode="+infoMap.get("returncode")+"&").append("transaction_id="+infoMap.get("transaction_id")+"&");
            sb.append("key="+key);
            String SignTemp=sb.toString();
            logger.info("[CORAL]删除支付回调待签名串: " + SignTemp);
            String sign = MD5.md5(SignTemp).toLowerCase();
            logger.info("[CORAL]删除支付回调生成签名串:"+sign);
            if (sign.equals(sourceSign)) return "success";
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            logger.error("[CORAL]珊瑚支付回调验签异常:"+e.getMessage());
        }
        return "fail";
    }
}
