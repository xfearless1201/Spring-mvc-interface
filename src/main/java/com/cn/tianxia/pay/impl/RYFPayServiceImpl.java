package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.cn.tianxia.common.PayConstant;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.utils.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.util.DESEncrypt;
import com.cn.tianxia.util.v2.MapUtils;

import net.sf.json.JSONObject;

/**
 * @ClassName: RYFPayServiceImpl
 * @Description:TODO(荣亿付)
 * @author: seven
 * @date: 2018年12月4日 下午7:29:18
 */
public class RYFPayServiceImpl implements PayService {
    private static final Logger logger = LoggerFactory.getLogger(RYFPayServiceImpl.class);
    //商户号
    private String payUrl;
    private String notifyUrl;
    private String queryUrl;
    private String wxH5Merchno;
    private String wxH5key;
    private String wxQRMerchno;
    private String wxQRkey;
    private String aliH5Merchno;
    private String aliH5key;
    private String wxH5BDMerchno;
    private String wxH5BDkey;


    public RYFPayServiceImpl(Map<String, String> data) {
        if (!CollectionUtils.isEmpty(data)) {
            payUrl = StringUtils.isEmpty(data.get("payUrl")) ? "" : data.get("payUrl");
            notifyUrl = StringUtils.isEmpty(data.get("notifyUrl")) ? "" : data.get("notifyUrl");
            queryUrl = StringUtils.isEmpty(data.get("queryUrl")) ? "" : data.get("queryUrl");
            wxH5Merchno = StringUtils.isEmpty(data.get("wxH5Merchno")) ? "" : data.get("wxH5Merchno");
            wxH5key = StringUtils.isEmpty(data.get("wxH5key")) ? "" : data.get("wxH5key");
            wxQRMerchno = StringUtils.isEmpty(data.get("wxQRMerchno")) ? "" : data.get("wxQRMerchno");
            wxQRkey = StringUtils.isEmpty(data.get("wxQRkey")) ? "" : data.get("wxQRkey");
            aliH5Merchno = StringUtils.isEmpty(data.get("aliH5Merchno")) ? "" : data.get("aliH5Merchno");
            aliH5key = StringUtils.isEmpty(data.get("aliH5key")) ? "" : data.get("aliH5key");
            wxH5BDMerchno = StringUtils.isEmpty(data.get("wxH5BDMerchno")) ? "" : data.get("wxH5BDMerchno");
            wxH5BDkey = StringUtils.isEmpty(data.get("wxH5BDkey")) ? "" : data.get("wxH5BDkey");
        }
    }

    /**
     * 网银支付
     *
     * @param payEntity
     * @return
     */
    public JSONObject wyPay(PayEntity payEntity) {
        return null;
    }

    /**
     * 扫码支付
     *
     * @param payEntity
     * @return
     */
    public JSONObject smPay(PayEntity payEntity) {
        try {
            logger.info("[RYF]荣亿付支付扫码支付开始===================START=================");
            String result = HttpUtils.toPostForm(getParam(payEntity), payUrl);
            logger.info("[RYF]荣亿付支付扫码支付请求响应结果:" + result);
            if (StringUtils.isEmpty(result)) {
                return PayResponse.error("下单失败:HTTP请求异常");
            }
            JSONObject res = JSONObject.fromObject(result);
            if (!ObjectUtils.isEmpty(res) && res.getString("respCode").equals("00")) {
                if (StringUtils.isEmpty(payEntity.getMobile())) {
                    return PayResponse.sm_qrcode(payEntity, res.getString("barCode"), "下单成功!");
                } else {
                    return PayResponse.sm_link(payEntity,res.getString("barCode"),"下单成功");
                }
            }
            String respMsg = res.getString("message");
            return PayResponse.error(respMsg);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[RYF]荣亿付支付扫码支付异常:" + e.getMessage());
            return PayResponse.error("下单失败:" + e.getMessage());
        }
    }

    /**
     * @param data
     * @return
     * @Description 验签
     */
    public String callback(Map<String, String> data) {
        logger.info("[RYF]荣亿付支付回调验签开始======================START=======================");
        try {
            //获取回调签名原串
            String signature = data.get("signature");
            String key = null;
            String merchno = data.get("merchno");
            if (StringUtils.isEmpty(merchno)) {
                logger.error("[RYF]荣亿付支付回调验签参数异常:merchno为空!");
                return "faild";
            }
            if (merchno.equals(aliH5key)) {
                key = aliH5key;
            } else if (merchno.equals(wxH5Merchno)) {
                key = wxH5key;
            } else if (merchno.equals(wxH5BDMerchno)) {
                key = wxH5BDkey;
            } else if (merchno.equals(wxQRMerchno)) {
                key = wxQRkey;
            } else {
                logger.error("[RYF]荣亿付支付回调验签参数异常:merchno不匹配!");
                return "faild";
            }
            SortedMap<String, String> dataMapIn = new TreeMap<>();
            dataMapIn.putAll(data);
            String param = MapUtils.mapToString(dataMapIn) + "&" + key;
            //生成回调签名
            String sign = DESEncrypt.getMd5(param);
            if (signature.equalsIgnoreCase(sign))
                return "success";

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[RYF]荣亿付支付回调验签异常:" + e.getMessage());
        }
        return "faild";
    }

    /**
     * @throws
     * @Title: getParam
     * @Description: TODO(获取参数)
     * @param: @param payEntity
     * @param: @return
     * @return: Map<String   ,   String>
     */
    private Map<String, String> getParam(PayEntity payEntity) {
        String key = null;    //不同支付方式，商户号和key不同
        String amount = new DecimalFormat("#.##").format(payEntity.getAmount());
        SortedMap<String, String> map = new TreeMap<>();
        if (payEntity.getPayType().equals(PayConstant.CHANEL_ALI)) {
            map.put("merchno", aliH5Merchno);
            key = aliH5key;
        } else if (payEntity.getPayType().equals(PayConstant.CHANEL_WX)) {
            if (StringUtils.isEmpty(payEntity.getMobile())) {
//				map.put("merchno",wxH5Merchno);
//				key = wxH5key;
                map.put("merchno",wxQRMerchno);
                key = wxQRkey;
            } else {
                map.put("merchno", wxH5Merchno);
                key = wxH5key;
            }
        }
        map.put("traceno", payEntity.getOrderNo());
        map.put("amount", amount);
        map.put("payType", payEntity.getPayCode());
        //map.put("payType", "AlipayH5");
        map.put("notifyUrl", notifyUrl);
        map.put("returnUrl", payEntity.getRefererUrl());
        map.put("goodsName", "top_up");
        String sign = MapUtils.mapToString(map) + "&" + key;
        map.put("signature", DESEncrypt.getMd5(sign));
        logger.info("[RYF]荣亿付支付扫码支付请求参数:" + JSONObject.fromObject(map));
        return map;
    }


    public static void main(String[] args) {
        PayEntity payEntity = new PayEntity();
        payEntity.setOrderNo("NO123456794687");
        payEntity.setPayCode("WechatH5");
        payEntity.setAmount(0.01);
        RYFPayServiceImpl impl = new RYFPayServiceImpl(null);
        impl.smPay(payEntity);
    }
}
