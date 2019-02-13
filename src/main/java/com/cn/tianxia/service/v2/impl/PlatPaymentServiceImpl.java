package com.cn.tianxia.service.v2.impl;

import com.cn.tianxia.dao.v2.CagentYsepayDao;
import com.cn.tianxia.dao.v2.UserTypeDao;
import com.cn.tianxia.entity.v2.CagentYsepayEntity;
import com.cn.tianxia.po.PaymentChannelPO;
import com.cn.tianxia.po.PaymentListPO;
import com.cn.tianxia.service.v2.PlatPaymentService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 * @ClassName PlatPaymentServiceImpl
 * @Description 平台支付商接口实现
 * @author Hardy
 * @Date 2018年12月31日 下午3:46:44
 * @version 1.0.0
 */
@Service
public class PlatPaymentServiceImpl implements PlatPaymentService {

    @Autowired
    private UserTypeDao userTypeDao;

    @Autowired
    private CagentYsepayDao cagentYsepayDao;

    @Override
    public JSONObject getPaymentChannel(String userId) {
        
        JSONArray mobileList = new JSONArray();   //手机端渠道
        JSONArray pcList = new JSONArray();       //pc端渠道
        
        if (StringUtils.isBlank(userId)) {
            return PaymentChannelPO.error(pcList,mobileList,"用户未登陆！");
        }

        String paymentChannel = userTypeDao.getPaychannelByUser(userId);

        if (StringUtils.isBlank(paymentChannel)) {
            return PaymentChannelPO.error(pcList,mobileList,"渠道数据为空！");
        }

        String[] paymentChannels = paymentChannel.split(",");

        if (paymentChannels.length == 0 || StringUtils.isBlank(paymentChannels[0])) {
            return PaymentChannelPO.error(pcList,mobileList,"未设置渠道数据！");
        }
        for (String channel:paymentChannels) {
            if (StringUtils.isBlank(channel.trim()))
                continue;
            int channelCode = Integer.parseInt(channel.trim());
            if (channelCode < 20) {
                pcList.add(channelCode);
            } else {
                mobileList.add(channelCode);
            }
        }
        return PaymentChannelPO.success(pcList,mobileList,"支付渠道数据获取成功！");
    }

    @Override
    public JSONObject getPaymentList(String userId, String payId) {
        if (StringUtils.isBlank(userId)) {
            return PaymentListPO.error("1001","参数异常：用户未登陆！");
        }
        if (StringUtils.isBlank(payId)) {
            return PaymentListPO.error("1001","参数异常：payId为空！");
        }
        List<CagentYsepayEntity> paymentList = cagentYsepayDao.selectPaymentListById(userId,payId);

        if (null == paymentList || paymentList.isEmpty()) {
            return PaymentListPO.error("1003","没有可用支付商列表！");
        }

        JSONArray payList = new JSONArray();

        for (CagentYsepayEntity payment:paymentList) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("paymentName", payment.getPaymentName()); //支付商编码
            jsonObject.put("id", payment.getId().toString());      //支付商id

            switch (payId) {                                     //限额设置
                case "1": case "5":
                    jsonObject.put("minquota", payment.getMinquota());
                    jsonObject.put("maxquota", payment.getMaxquota());
                    break;
                case "2": case "6":
                    jsonObject.put("minquota", payment.getAliMinquota());
                    jsonObject.put("maxquota", payment.getAliMaxquota());
                    break;
                case "3": case "7":
                    jsonObject.put("minquota", payment.getWxMinquota());
                    jsonObject.put("maxquota", payment.getWxMaxquota());
                    break;
                case "4": case "8":
                    jsonObject.put("minquota", payment.getQrminquota());
                    jsonObject.put("maxquota", payment.getQrmaxquota());
                    break;
                case "9": case "10":
                    jsonObject.put("minquota", payment.getYlMinquota());
                    jsonObject.put("maxquota", payment.getYlMaxquota());
                    break;
                case "11": case "12":   // 11pc京东扫码，12手机端京东扫码
                    jsonObject.put("minquota", payment.getJdMinquota());
                    jsonObject.put("maxquota", payment.getJdMaxquota());
                    break;
                case "13": case "14":   // 13pc端快捷，14手机端快捷
                    jsonObject.put("minquota", payment.getKjMinquota());
                    jsonObject.put("maxquota", payment.getKjMaxquota());
                    break;
                case "15": case "16":   // 15 PC微信条码 16 手机微信条码
                    jsonObject.put("minquota", payment.getWxtmMinquota());
                    jsonObject.put("maxquota", payment.getWxtmMaxquota());
                    break;
                case "17": case "18":   // 17 PC支付宝条码 18 手机支付宝条码
                    jsonObject.put("minquota", payment.getAlitmMinquota());
                    jsonObject.put("maxquota", payment.getAlitmMaxquota());
                    break;
            }

            payList.add(jsonObject);
        }

        return PaymentListPO.success(payList,"接口获取成功！");
    }
}
