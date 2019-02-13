package com.cn.tianxia.pay.impl;

import com.cn.tianxia.common.PayConstant;
import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 互联网支付
 * @author TX
 */
public class HLWPayServiceImpl implements PayService {

    private final static Logger logger = LoggerFactory.getLogger(HLWPayServiceImpl.class);

    private String merId = "100519298";   //商户号  100519298
    private String key = "iVLbd4dUS9q8";   //md5 key      iVLbd4dUS9q8
    private String bankPayUrl = "http://47.244.19.206:8080/payment/PayApply.do";   //网银支付地址
    private String scanPayUrl = "http://47.244.19.206:8080/payment/ScanPayApply.do";   //扫码支付地址
    private String queryUrl = "http://47.244.19.206:8080/payment/OrderStatusQuery.do";     //订单查询地址
    private String notifyUrl = "http://www.baidu.com";    //异步通知地址

    public HLWPayServiceImpl(Map<String, String> data) {
        if (data != null && !data.isEmpty()) {
            if (data.containsKey("merId")) {
                this.merId = data.get("merId");
            }
            if (data.containsKey("key")) {
                this.key = data.get("key");
            }
            if (data.containsKey("bankPayUrl")) {
                this.bankPayUrl = data.get("bankPayUrl");
            }
            if (data.containsKey("scanPayUrl")) {
                this.scanPayUrl = data.get("scanPayUrl");
            }
            if (data.containsKey("notifyUrl")) {
                this.notifyUrl = data.get("notifyUrl");
            }
            if (data.containsKey("queryUrl")) {
                this.queryUrl = data.get("queryUrl");
            }
        }
    }

    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        logger.info("[HLW]互联网支付网银支付开始===================START=================");
        try {
            //获取支付请求参数
            Map<String, String> data = sealRequest(payEntity, 1);
            //生成签名串
            String sign = generatorSign(data);
            data.put("signData", sign);

            logger.info("[HLWPay]互联网支付网银支付请求参数:{}", data.toString());

            String response = HttpUtils.post(data, bankPayUrl);

            logger.info("[HLWPay]互联网支付网银支付返回结果:{}",response);

            if (StringUtils.isBlank(response)) {
                logger.error("[HLWPay]互联网支付网银支付失败,请求无响应结果!");
                return PayResponse.error("[HLWPay]互联网网银支付失败,请求无响应结果");
            }

              return PayUtil.returnWYPayJson("success","jsp",response,payEntity.getPayUrl(),"payhtml");

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[HLW]互联网支付网银支付异常:" + e.getMessage());
            return PayUtil.returnWYPayJson("error", "[HLW]互联网网银支付异常!", "", "", "");
        }
    }

    @Override
    public JSONObject smPay(PayEntity payEntity) {
        logger.info("[HLWPay]互联网支付扫码支付开始---------------START---------------");
        try {
            Map<String, String> data = null;
            if (PayConstant.CHANEL_KJ.equals(payEntity.getPayType())) {
                data = sealRequest(payEntity, 2);
                data.put("signData", generatorSign(data));
                logger.info("[HLWPay]互联网支付快捷支付请求参数:" + data.toString());

                String form = HttpUtils.generatorForm(data,bankPayUrl);

                logger.info("[HLWPay]互联网支付快捷支付构造form:{}",form);

                if (StringUtils.isBlank(form)) {
                    return PayResponse.error("[HLW]互联网支付快捷支付构造form为空!");
                }

                return PayResponse.sm_form(payEntity, form, "下单成功!");

            } else {
                data = sealScanRequest(payEntity);
                data.put("signData", generatorSign(data));

                logger.info("[HLWPay]互联网支付扫码请求参数:" + data.toString());

                String response = HttpUtils.post(data, scanPayUrl);

                if (StringUtils.isBlank(response)) {
                    logger.error("[HLWPay]互联网支付失败，请求无响应结果!");
                    return PayResponse.error("[HLWPay]互联网请求失败，请求无响应结果!");
                }

                logger.info("[HLWPay]互联网支付扫码支付返回结果:{}",response);

                JSONObject jsonObject = JSONObject.fromObject(response);

                if (jsonObject.containsKey("retCode") && jsonObject.getString("retCode").equals("1")) {
                    //下单成功
                    String qrCodeURL = jsonObject.getString("qrcode");
                    return PayResponse.sm_qrcode(payEntity, qrCodeURL, "下单成功!");
                }
                //下单失败
                String respMsg = jsonObject.getString("retMsg");
                return PayResponse.error("下单失败:" + respMsg);
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[HLWPay]互联网支付扫码支付异常:" + e.getMessage());
            return PayResponse.error("[HLWPay]互联网扫码支付异常!" + e.getMessage());
        }
    }

    @Override
    public String callback(Map<String, String> data) {
        logger.info("[HLW]互联网支付回调验签开始===================START===================");
        try {
            //获取验签原签名串
            String sourceSign = data.remove("signData");
            logger.info("[HLW]互联网支付验签原签名串:{}", sourceSign);

            //生成验签签名
            String sign = generatorSign(data);
            logger.info("[HLW]互联网支付验签生成签名串:{}", sign);
            if (sourceSign.equalsIgnoreCase(sign))
                return "success";
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[HLW]互联网支付回调验签异常:{}", e.getMessage());
        }
        return "fail";
    }

    public String query(Map<String, String> data) {
        logger.info("[HLW]互联网支付查询开始===================START=====================");
        try {
            Map<String, String> queryMap = new HashMap<>();
            queryMap.put("merId", merId);
            queryMap.put("sign_type", "MD5");
            queryMap.put("prdOrdNo", data.get("order_no"));
            //生成签名串
            String sign = generatorSign(data);
            queryMap.put("sign", sign);
            String response = HttpUtils.post(queryMap, queryUrl);
            if (StringUtils.isBlank(response)) {
                logger.error("[HLW]互联网支付查询失败,请求无响应结果!");
                return "failed";
            }

            //解析响应结果
            JSONObject jsonObject = JSONObject.fromObject(response);
            if (jsonObject.containsKey("orderstatus") && jsonObject.getString("orderstatus").equals("01")) {
                //（00：未支付，01：支付成功，02：支
                //付处理中，11：订单作废，14：冻结，
                //19：待处理，20：待打款）
                //2、提现订单即 prdordtype=12 或 13
                //（00：待处理，01：已完成，02：复核
                //中，14：冻结中，20：待跑批，21：银
                //行处理中）
                return "success";
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[HLW]互联网支付查询异常:{}", e.getMessage());
        }
        return null;
    }

    /**
     * @param entity
     * @param payType 1 网银 2 快捷
     * @return
     * @throws Exception
     * @Description 封装支付请求参数
     */
    private Map<String, String> sealRequest(PayEntity entity, int payType) throws Exception {
        logger.info("[HLW]互联网支付封装请求参数开始=====================START==================");
        try {
            //创建支付请求参数存储对象
            Map<String, String> data = new HashMap<>();
            //订单时间
            Date orderTime = new Date();
            //订单金额
            String amount = new DecimalFormat("##").format(entity.getAmount() * 100);//交易金额 分为单位
            data.put("versionId", "1.0");
            data.put("orderAmount", amount);
            data.put("orderDate", new SimpleDateFormat("yyyyMMddHHmmss").format(orderTime));
            data.put("currency", "RMB");//币种
            data.put("accountType", "0");//银行卡种类 必输 0-借记卡 1-贷记卡 (部分通道必输，一事一议)
            data.put("transType", "008");//交易类别 必输 默认填写 008 4
            data.put("asynNotifyUrl", notifyUrl);//异步通知 URL
            data.put("synNotifyUrl", entity.getRefererUrl());//同步返回 URL
            data.put("signType", "MD5");
            data.put("merId", merId);//商户号
            data.put("prdOrdNo", entity.getOrderNo());
            if (payType == 1) {
                data.put("payMode", "00020"); //支付方式 必输 支付方式 00020-银行卡 00023-快捷 00024-支付宝Wap
                data.put("tranChannel", entity.getPayCode());//银行编码 必输 银行编码请参照银行统一编码列表 (支付宝 Wap 固定值103)
//                data.put("tranChannel", "104");//银行编码 必输 银行编码请参照银行统一编码列表 (支付宝 Wap 固定值103)
            } else {
                data.put("payMode", "00023"); //支付方式 必输 支付方式 00020-银行卡 00023-快捷 00024-支付宝Wap
                data.put("bankCardNo", "6216665000001788266");//银行卡号 可输 (部分通道必输，一事一议,快捷支付必输)
                data.put("tranChannel", "104");//银行编码 必输 银行编码请参照银行统一编码列表 (支付宝 Wap 固定值103)
            }
            data.put("receivableType", "D00");//到账类型 必输 D00,T01,D01: D00 为 D+0,T01 为 T+1,D01 为 D+1 10
            data.put("prdName", "TOP_UP");//商品名称 必输 50
            data.put("prdDesc", "e_goods");//商品描述 必输 500
            data.put("pnum", "1");//商品数量 必输

            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[HLW]互联网支付封装请求参数异常:" + e.getMessage());
            throw new Exception("封装支付请求参数异常!");
        }
    }

    /**
     * @param entity
     * @return
     * @throws Exception
     * @Description 封装扫码接口请求参数
     */
    private Map<String, String> sealScanRequest(PayEntity entity) throws Exception {
        logger.info("[HLW]互联网支付封装请求参数开始=====================START==================");
        try {
            //创建支付请求参数存储对象
            Map<String, String> data = new HashMap<>();
            //订单时间
            Date orderTime = new Date();
            //订单金额
            String amount = new DecimalFormat("##").format(entity.getAmount() * 100);//交易金额 分为单位
            data.put("versionId", "1.0");
            data.put("orderAmount", amount);
            data.put("orderDate", new SimpleDateFormat("yyyyMMddHHmmss").format(orderTime));
            data.put("currency", "RMB");//币种
            data.put("transType", "008");//交易类别 必输 默认填写 008
            data.put("asynNotifyUrl", notifyUrl);//异步通知 URL
            data.put("synNotifyUrl", entity.getRefererUrl());//同步返回 URL
            data.put("signType", "MD5");
            data.put("merId", merId);//商户号
            data.put("prdOrdNo", entity.getOrderNo());
            //支付方式 必输 00021-支付宝扫码 00022-微信扫码 00032-QQ 扫码00027-京东扫码 00024-支付宝 wap 0025-微信 wap 00026-银联扫码
            data.put("payMode", entity.getPayCode());
            data.put("receivableType", "D00");//到账类型 必输 D00,T01,D01: D00 为 D+0,T01 为 T+1,D01 为 D+1
            data.put("prdName", "TOP_UP");//商品名称 必输
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[HLW]互联网支付封装请求参数异常:" + e.getMessage());
            throw new Exception("封装支付请求参数异常!");
        }
    }

    /**
     * @param data
     * @return
     * @throws Exception
     * @Description 生成支付签名串
     */
    public String generatorSign(Map<String, String> data) throws Exception {
        logger.info("[HLW]互联网支付生成支付签名串开始==================START========================");
        try {
            //签名规则:
            //把除签名字段和集合字段以外的所有字段（不包括值为null的）内容按照报文字段字典顺序，
            //依次按照“字段名=字段值”的方式用“&”符号连接，最后加上机构工作密钥，使用MD5算法计算数字签名，填入签名字段。接受方应按响应步骤验证签名。
            Map<String, String> treemap = new TreeMap<>();
            treemap.putAll(data);

            //生成待签名串
            StringBuffer sb = new StringBuffer();
            Iterator<String> iterator = treemap.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                String val = treemap.get(key);

                if (StringUtils.isBlank(val) || key.equalsIgnoreCase("sign") || key.equalsIgnoreCase("signData"))
                    continue;
                if (iterator.hasNext()) {
                    sb.append(key).append("=").append(val).append("&");
                } else {
                    sb.append(key).append("=").append(val);
                }
            }

            //生成待签名串
            String signStr = sb.toString() + "&key=" + key;
            logger.info("[HLW]互联网支付生成待签名串:" + signStr);
            String sign = MD5Utils.md5toUpCase_32Bit(signStr);
            logger.info("[HLW]互联网支付生成加密签名串:" + sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[HLW]支付生成支付签名串异常:" + e.getMessage());
            throw new Exception("生成支付签名串异常!");
        }
    }

    /**
     * 截取html内容里面的form
     *
     * @param html
     * @return
     */
    private String splitHtml(String html) {
        if (StringUtils.isBlank(html)) {
            return null;
        }
        int formStart = html.indexOf("<form");
        int formEnd = html.lastIndexOf("form>");
        return html.substring(formStart,formEnd+5);
    }

    public static void main(String[] args) {
        PayEntity entity = new PayEntity();
        entity.setOrderNo("bill00000146");
//        entity.setPayCode("00026");//银联扫码
//        entity.setPayCode("102");//网银
        entity.setPayCode("00026");//银联扫码
        entity.setAmount(100);
        entity.setRefererUrl("http://google.com");
        //entity.setPayType("7");
//        Map<String,Object> extendMap = new HashMap<>();
//        extendMap.put("tranChannel","102");
//        extendMap.put("bankCardNo","6212264100011335373");
//        entity.setExtendMap(extendMap);
        HLWPayServiceImpl hlwPayService = new HLWPayServiceImpl(null);
        hlwPayService.smPay(entity);
    }
}