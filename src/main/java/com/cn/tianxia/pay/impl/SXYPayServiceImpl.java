/****************************************************************** 
 *
 *    Powered By tianxia-online. 
 *
 *    Copyright (c) 2018-2020 Digital Telemedia 天下网络 
 *    http://www.d-telemedia.com/ 
 *
 *    Package:     com.cn.tianxia.pay.impl 
 *
 *    Filename:    SXYPayServiceImpl.java 
 *
 *    Description: TYC太阳城接入首信易支付
 *
 *    Copyright:   Copyright (c) 2018-2020 
 *
 *    Company:     天下网络科技 
 *
 *    @author: Elephone
 *
 *    @version: 1.0.0
 *
 *    Create at:   2018年08月22日 10:07 
 *
 *    Revision: 
 *
 *    2018/8/22 10:07 
 *        - first revision 
 *
 *****************************************************************/
package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.RsaUtils;
import com.cn.tianxia.pay.ys.util.DateUtil;

import net.sf.json.JSONObject;

/**
 * @ClassName SXYPayServiceImpl
 * @Description TYC太阳城接入首信易支付
 * @Author Elephone
 * @Date 2018年08月22日 10:07
 * @Version 1.0.0
 **/
public class SXYPayServiceImpl implements PayService {

    /**
     * Java密钥库(Java Key Store，JKS)KEY_STORE
     */
    public static final String KEY_STORE = "PKCS12";

    private final static Logger logger = LoggerFactory.getLogger(SXYPayServiceImpl.class);
    static String v_mid;// 商户编号
    static String priKey; // RSA商户私钥
    static String pubKey; // RSA公钥
    static String wyPayUrl;// 支付地址
    static String smPayUrl;// 支付地址
    static String v_url;// 反馈地址

    public SXYPayServiceImpl(Map<String, String> pmap) {
        if (pmap != null) {
            if (pmap.containsKey("v_mid")) {
                v_mid = pmap.get("v_mid");
            }
            if (pmap.containsKey("priKey")) {
                priKey = pmap.get("priKey");
            }
            if (pmap.containsKey("pubKey")) {
                pubKey = pmap.get("pubKey");
            }
            if (pmap.containsKey("wyPayUrl")) {
                wyPayUrl = pmap.get("wyPayUrl");
            }
            if (pmap.containsKey("smPayUrl")) {
                smPayUrl = pmap.get("smPayUrl");
            }
            if (pmap.containsKey("v_url")) {
                v_url = pmap.get("v_url");
            }
        }
    }

    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        logger.info("[SXY]首信易网银支付开始==================start====================");
        try {
            // 获取支付请求参数
            Map<String, String> data = sealRequest(payEntity, 1);
            // 生产签名
            String sign = generatorSign(data);
            data.put("v_md5info", sign); // 签名
            logger.info("[SXY]首信易支付最终支付请求参数:" + JSONObject.fromObject(data).toString());
            // 发起支付请求
            String formStr = HttpUtils.generatorForm(data, wyPayUrl);
            logger.info("支付form表单：" + formStr);
            return PayUtil.returnWYPayJson("success", "form", formStr, payEntity.getPayUrl(), "");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[SXY]首信易网银支付异常:" + e.getMessage());
            return PayUtil.returnWYPayJson("error", "", "", "", "");
        }

    }

    @Override
    public JSONObject smPay(PayEntity payEntity) {
        logger.info("[SXY]首信易扫码支付开始===================START==================");
        try {
            double amount = payEntity.getAmount();// "8.02";// 订单金额
            String userName = payEntity.getUsername();
            // 获取支付请求参数
            Map<String, String> data = sealRequest(payEntity, 0);
            // 获取签名
            String sign = generatorSign(data);
            data.put("v_md5info", sign);
            logger.info("[SXY]首信易支付最终支付请求参数:" + JSONObject.fromObject(data).toString());
            // 发起支付请求
            String response = HttpUtils.generatorForm(data, smPayUrl);
            logger.info("组装支付表单：" + response);
            return PayUtil.returnPayJson("success", "1", "支付接口请求成功!", userName, amount, "", response);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[SXY]首信易扫码支付异常:" + e.getMessage());
            return PayUtil.returnPayJson("error", "", "", "", 0, "", "");
        }
    }

    @Override
    public String callback(Map<String, String> data) {
        logger.info("[SXY]首信易支付回调验签开始=================START===============");
        try {
            // 验签规则:v_oid+v_pstatus+v_amount+v_moneytype。
            // 获取回调传递回来的原签名
            String sourceSign = data.get("v_sign");
            logger.info("[SXY]首信易回调传递的原签名串:" + sourceSign);
            // 待签名串
            String v_moneytype = data.get("v_moneytype");// 币种
            String v_amount = data.get("v_amount");// 订单金额
            String v_oid = data.get("v_oid");// 订单号
            String v_pstatus = data.get("v_pstatus");// 支付状态组
            StringBuffer sb = new StringBuffer();
            sb.append(v_oid).append(v_pstatus).append(v_amount).append(v_moneytype);
            String signStr = sb.toString();
            logger.info("[SXY]首信易回调待签名串:" + signStr);
            boolean verifyReuslt = RsaUtils.verify(signStr, sourceSign, pubKey);
            logger.info("[SXY]首信易回调验签结果:" + verifyReuslt);
            if (verifyReuslt)
                return "success";
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[SXY]首信易支付回调验签异常:" + e.getMessage());
        }
        return "";
    }

    
    /**
     * 封装支付请求参数
     * 
     * @param payEntity
     * @param type
     *            1 网银 0 扫码
     * @return
     * @throws Exception
     */
    private Map<String, String> sealRequest(PayEntity payEntity, Integer type) throws Exception {
        try {
            // 创建存储参数对象
            Map<String, String> params = new HashMap<>();
            double amount = payEntity.getAmount();// "8.02";// 订单金额
            DecimalFormat dcf = new DecimalFormat("0.00");
            String price = dcf.format(amount);
            String v_moneytype = "0"; // 币种
            String v_ymd = DateUtil.getCurrentDate("yyyyMMdd"); // 日期
            params.put("v_mid", v_mid); // 商户编号

            // ---------------生成支付订单号 开始------------------------
            // 当前时间 yyyyMMddHHmmss
            String currTime = DateUtil.getCurrentDate("yyyyMMddHHmmss");
            // 8位 当前时间 yyyyMMddHHmmss
            String prefixTime = v_ymd;
            // 6位日期
            String suffixTime = currTime.substring(8, currTime.length());
            // 四位随机数
            String strRandom = DateUtil.getRandom(4) + "";

            // 订单号，此处用时间加随机数生成，商户根据自己情况调整，只要保持全局唯一就行
            String order_no = prefixTime + "-" + v_mid + "-" + suffixTime + strRandom;
            // ---------------生成支付订单号 结束------------------------
            payEntity.setOrderNo(order_no);
            params.put("v_oid", order_no); // 订单编号20180822-7606-221117
            params.put("v_rcvname", v_mid);// 接收人姓名
            params.put("v_rcvaddr", v_mid); // 接收人地址
            params.put("v_rcvtel", v_mid); // 接收人电话
            params.put("v_rcvpost", v_mid); // 接收人邮编
            params.put("v_amount", price); // 金额
            params.put("v_pmode", payEntity.getPayCode()); // 银行编码
            params.put("v_ymd", v_ymd); // 日期
            params.put("v_orderstatus", "1"); // 订单状态
            params.put("v_ordername", v_mid); // 订单名
            params.put("v_moneytype", v_moneytype); // 币种
            params.put("v_url", v_url); // 反馈地址
            return params;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[SXY]首信易支付组装支付请求参数异常:" + e.getMessage());
            throw new Exception("组装支付请求参数异常!");
        }
    }

    /**
     * @Description 生产签名串
     * @param data
     * @return
     * @throws Exception
     */
    private String generatorSign(Map<String, String> data) throws Exception {
        logger.info("[SXY]首信易支付生产签名串开始=====================START====================");
        try {
            // 签名规则:支付时:v_moneytype+v_ymd+v_amount+ v_rcvname+v_oid+v_mid+v_url
            // 签名规则:验签时:v_oid+v_pstatus+v_amount+v_moneytype+v_count
            // CFCA加密
            String v_moneytype = data.get("v_moneytype");// 币种
            String v_amount = data.get("v_amount");// 订单金额
            String v_oid = data.get("v_oid");// 订单号
            String v_ymd = data.get("v_ymd");// 日期
            String v_rcvname = data.get("v_rcvname");// 接受人姓名
            StringBuffer sb = new StringBuffer();
            sb.append(v_moneytype).append(v_ymd).append(v_amount);
            sb.append(v_rcvname).append(v_oid).append(v_mid).append(v_url);
            String signStr = sb.toString();
            logger.info("[SXY]首信易生产待签名串:" + signStr);
            String sign = RsaUtils.sign(signStr, priKey);
            logger.info("[SXY]首信易生产加密签名串:" + sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[SXY]首信易支付生产签名串异常:" + e.getMessage());
            throw new Exception("生产签名串异常!");
        }
    }
}
