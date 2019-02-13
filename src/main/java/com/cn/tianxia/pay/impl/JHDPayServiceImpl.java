package com.cn.tianxia.pay.impl;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Auther: zed
 * @Date: 2019/1/22 10:25
 * @Description: 钜汇达支付
 */
public class JHDPayServiceImpl implements PayService {
    // 日志
    private static final Logger logger = LoggerFactory.getLogger(JHDPayServiceImpl.class);

    private String memberid;// 商户编号

    private String notifyUrl;// 回调URL

    private String sercet;// 加密key

    private String payUrl;// 支付地址

    public JHDPayServiceImpl(Map<String, String> data) {
        if (data != null) {
            if (data.containsKey("memberid")) {
                this.memberid = data.get("memberid");
            }
            if (data.containsKey("notifyUrl")) {
                this.notifyUrl = data.get("notifyUrl");
            }
            if (data.containsKey("sercet")) {
                this.sercet = data.get("sercet");
            }
            if (data.containsKey("payUrl")) {
                this.payUrl = data.get("payUrl");
            }
        }
    }

    /**
     * 网银支付
     */
    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * 扫码支付
     */
    @Override
    public JSONObject smPay(PayEntity payEntity) {
        logger.info("[JHD]钜汇达支付扫码支付开始====================START========================");
        String username = payEntity.getUsername();
        double amount = payEntity.getAmount();
        String orderNo = payEntity.getOrderNo();
        try {
            // 获取支付请求参数
            Map<String, String> data = sealRequest(payEntity);
            // 发起支付请求
            String response = HttpUtils.generatorForm(data, payUrl);
            // 解析响应结果
            return PayUtil.returnPayJson("success", "1", "下单成功", username, amount, orderNo, response);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[JHD]钜汇达支付扫码支付异常:" + e.getMessage());
            return PayUtil.returnPayJson("error", "", "扫码支付异常", payEntity.getUsername(), payEntity.getAmount(),
                    payEntity.getOrderNo(), e.getMessage());
        }
    }

    /**
     * @param map
     * @return
     * @Description 回调验签
     */
    @Override
    public String callback(Map<String, String> map) {
        try {
            //从回调参数中取出签名
            String sourceSign = map.get("sign");
            //对参数对象进行排序

            Map<String, String> treemap = new TreeMap<>(map);
            //生成待签名串
            StringBuffer sb = new StringBuffer();
            for (String key : treemap.keySet()) {
                String val = treemap.get(key);
                //排序不参与签名的参数
                if (key.equals("sign") || key.equals("attach"))
                    continue;

                sb.append(key).append("=").append(val).append("&");
            }
            //加上签名秘钥
            sb.append("key=").append(sercet);
            //生成待签名串
            String signStr = sb.toString();
            //进行MD5加密
            String sign = MD5Utils.md5toUpCase_32Bit(signStr);
            if (StringUtils.isBlank(sign)) {
                logger.error("[JHD]钜汇达支付回调验签失败:生成签名为空");
                return "fail";
            }
            //进行验签
            if (sign.equals(sourceSign))
                return "success";

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[JHD]钜汇达支付回调验签异常:" + e.getMessage());
        }
        return "";
    }

    /**
     * @param entity
     * @return
     * @throws Exception
     * @Description 封装支付请求参数
     */
    private Map<String, String> sealRequest(PayEntity entity) throws Exception {
        logger.info("[JHD]钜汇达支付封装支付请求参数开始===========================START=================");
        try {
            // 创建存储参数对象
            Map<String, String> data = new HashMap<>();
            String orderNo = entity.getOrderNo();// 订单号
            String amount = new DecimalFormat("0.00").format(entity.getAmount());// 订单金额
            String orderTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());// 订单时间
            data.put("pay_memberid", memberid);// 平台分配商户号
            data.put("pay_orderid", orderNo);// 订单号, 字符长度20
            data.put("pay_applydate", orderTime);// 提交时间,时间格式：2016-12-26 18:18:18
            data.put("pay_bankcode", entity.getPayCode());// 银行编码
            data.put("pay_notifyurl", notifyUrl);// 服务端通知
            data.put("pay_callbackurl", entity.getRefererUrl());// 页面跳转通知
            data.put("pay_amount", amount);// 订单金额
            logger.info("[JHD]钜汇达支付封装签名参数:" + JSONObject.fromObject(data).toString());
            // 以上字段参与签名,生成待签名串
            String sign = generatorSign(data);
            data.put("pay_md5sign", sign);
            data.put("pay_attach", "");// 附加字段
            data.put("pay_productname", "top_Up");// 商品名称
            data.put("pay_productnum", "");// 商户品数量
            data.put("pay_productdesc", "");// 商品描述
            data.put("pay_producturl", "");// 商户链接地址
            logger.info("[JHD]钜汇达支付封装支付请求参数:" + JSONObject.fromObject(data).toString());
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[JHD]钜汇达支付封装请求参数异常:" + e.getMessage());
            throw new Exception("[JUH]钜汇达支付封装支付请求参数异常!");
        }
    }

    /**
     * @param data
     * @return
     * @throws Exception
     * @Description 生成签名
     */
    private String generatorSign(Map<String, String> data) throws Exception {
        try {
            // 排序
            Map<String, String> treemap = new TreeMap<>(data);
            StringBuffer sb = new StringBuffer();
            for (String key : treemap.keySet()) {
                String val = treemap.get(key);
                sb.append(key).append("=").append(val).append("&");
            }
            // 加上签名秘钥
            sb.append("key=").append(sercet);
            String signStr = sb.toString();
            logger.info("[JHD]钜汇达支付生成待加密签名串：" + signStr);
            // 进行MD5加密，32位大写
            String sign = MD5Utils.md5toUpCase_32Bit(signStr);
            logger.info("[JHD]钜汇达支付生成MD5加密签名串:" + sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[JHD]钜汇达支付生成签名异常:" + e.getMessage());
            throw new Exception("生成签名串异常!");
        }
    }
}
