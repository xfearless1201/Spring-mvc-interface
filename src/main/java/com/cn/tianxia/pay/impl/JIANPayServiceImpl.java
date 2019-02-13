package com.cn.tianxia.pay.impl;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: JIANPayServiceImpl
 * @Description: 简付支付
 * @Author: Zed
 * @Date: 2019-01-07 10:04
 * @Version:1.0.0
 **/

public class JIANPayServiceImpl implements PayService {

    private static final Logger logger = LoggerFactory.getLogger(JIANPayServiceImpl.class);

    private String customerid = "4566";
    private String key = "654E5775E731CB9A2582328F120CB0AE";
    private String payUrl = "http://api.jfqbpay.cn";
    private String notifyUrl = "http://txw.tx8899.com/TAS/Notify/YISZFNotify.do";

    public JIANPayServiceImpl(Map<String,String> map) {
        if(map != null && !map.isEmpty()){
            if(map.containsKey("customerid")){
                this.customerid = map.get("customerid");
            }
            if(map.containsKey("notifyUrl")){
                this.notifyUrl = map.get("notifyUrl");
            }
            if(map.containsKey("key")){
                this.key = map.get("key");
            }
            if(map.containsKey("payUrl")){
                this.payUrl = map.get("payUrl");
            }
        }

    }

    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        try {
            Map<String,String> param = sealRequest(payEntity,1);

            String sign = generatorSign(param);

            param.put("sign",sign);
            logger.info("[JIAN]简付支付网银请求参数:{}",JSONObject.fromObject(param).toString());
            String formStr = HttpUtils.generatorFormGet(param,payUrl);

            if (StringUtils.isBlank(formStr)) {
                logger.error("[JIAN]简付支付下单失败：生成表单结果为空");
                PayResponse.error("[JIAN]简付支付下单失败：生成表单结果为空");
            }

            return PayResponse.wy_form(payEntity.getPayUrl(),formStr);

        } catch (Exception e) {
            e.printStackTrace();
            return PayResponse.error("[JIAN]简付支付网银支付下单失败"+e.getMessage());
        }
    }

    @Override
    public JSONObject smPay(PayEntity payEntity) {
        try {
            Map<String,String> param = sealRequest(payEntity,2);

            String sign = generatorSign(param);

            param.put("sign",sign);
            logger.info("[JIAN]简付支付扫码请求参数:{}",JSONObject.fromObject(param).toString());
            String formStr = HttpUtils.generatorFormGet(param,payUrl);

            if (StringUtils.isBlank(formStr)) {
                logger.error("[JIAN]简付支付下单失败：生成表单结果为空");
                PayResponse.error("[JIAN]简付支付下单失败：生成表单结果为空");
            }

            return PayResponse.sm_form(payEntity,formStr,"下单成功");

        } catch (Exception e) {
            e.printStackTrace();
            return PayResponse.error("[YISZF]易收支付扫码支付下单失败"+e.getMessage());
        }
    }

    @Override
    public String callback(Map<String, String> data) {
        String sourceSign = data.remove("user_sign");
        if (StringUtils.isBlank(sourceSign)) {
            logger.info("[JIAN]简付支付回调验签失败：回调签名为空！");
            return "fail";
        }
        if(verifyCallback(sourceSign,data))
            return "success";
        return "fail";
    }

    private boolean verifyCallback(String sign,Map<String,String> data) {

//  user_sign参数的构造：
//user_id="&user_id&"&user_order="&user_order&"&user_money="& user_money"&user_status="&user_status&"&user_ext="&user_ext&Ukey
//Ukey为商户的秘钥，请登录我司网站后台查看
//然后将sign 字符串进行md5加密（大写）

        StringBuffer sb = new StringBuffer();
        sb.append("user_id=").append(data.get("user_id"));
        sb.append("&user_order=").append(data.get("user_order"));
        sb.append("&user_money=").append(data.get("user_money"));
        sb.append("&user_status=").append(data.get("user_status"));
        sb.append("&user_ext=").append(data.get("user_ext"));
        sb.append(key);
        String localSign;
        try {
            localSign = MD5Utils.md5toUpCase_32Bit(sb.toString());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            logger.error("[JIAN]简付支付生成支付签名串异常:"+ e.getMessage());
            return false;
        }
        return sign.equalsIgnoreCase(localSign);
    }

    /**
     *
     * @Description 封装支付请求参数
     * @param entity
     * @return
     * @throws Exception
     */
    private Map<String,String> sealRequest(PayEntity entity,int type) throws Exception{
        logger.info("[JIAN]简付支付封装请求参数开始=====================START==================");
        try {
            //创建支付请求参数存储对象
            Map<String,String> data = new HashMap<>();
            //订单金额
            String amount = new DecimalFormat("0.00").format(entity.getAmount());

            data.put("user_id",customerid);//    商户ID
            data.put("order_id",entity.getOrderNo());//订单号
            data.put("user_rmb",amount);//支付金额
            data.put("syn_url",notifyUrl);//    异步URL
            data.put("re_url",entity.getRefererUrl());//    同步URL
            data.put("user_ip",entity.getIp());//    客户端IP
            if (type == 1) {
                data.put("pay_type", "bank");//支付方式
                data.put("bank_code", entity.getPayCode());//银行类别
            }else {
                data.put("pay_type",entity.getPayCode());
            }
            data.put("ext_info","top_up");//扩展信息
            //data.put("pay_format","");//	数据返回

            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[JIAN]简付支付封装请求参数异常:"+e.getMessage());
            throw new Exception("封装支付请求参数异常!");
        }
    }

    /**
     *
     * @Description 生成支付签名串
     * @param data
     * @return
     * @throws Exception
     */
    public String generatorSign(Map<String,String> data) throws Exception{
        logger.info("[JIAN]简付支付生成支付签名串开始==================START========================");
        try {
            //签名规则:
//            sign="user_id="&user_id&"&order_id="&order_id&"&user_rmb="&user_rmb &"&syn_url="&syn_url&"&pay_type="&pay_type&Ukey
//            Ukey为商户的秘钥，请登录我司网站后台查看
//            然后将sign 字符串进行md5加密（大写方式）
//            注意最后的Ukey和字符链接不用&符号 直接拼接上即可

            StringBuilder strBuilder = new StringBuilder();
            strBuilder.append("user_id=").append(data.get("user_id"));
            strBuilder.append("&order_id=").append(data.get("order_id"));
            strBuilder.append("&user_rmb=").append(data.get("user_rmb"));
            strBuilder.append("&syn_url=").append(data.get("syn_url"));
            strBuilder.append("&pay_type=").append(data.get("pay_type"));
            strBuilder.append(key);
            logger.info("[JIAN]简付支付生成待签名串:"+strBuilder.toString());
            String md5Value = MD5Utils.md5toUpCase_32Bit(strBuilder.toString());
            if (StringUtils.isBlank(md5Value)) {
                logger.error("[JIAN]简付支付生成签名异常：生成签名为空");
                throw new Exception("生成支付签名串异常!");
            }
            logger.info("[JIAN]简付支付生成加密签名串:"+md5Value);
            return md5Value;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[JIAN]简付支付生成支付签名串异常:"+e.getMessage());
            throw new Exception("生成支付签名串异常!");
        }
    }
}
