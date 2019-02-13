package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.pay.service.PayService;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName SANSPayServiceImpl
 * @Description 三生支付
 * @author Hardy
 * @Date 2018年12月19日 上午11:33:58
 * @version 1.0.0
 */
public class SANSPayServiceImpl implements PayService {

    //日志
    private static final Logger logger = LoggerFactory.getLogger(SANSPayServiceImpl.class);
    
    private String payUrl;//支付地址
    
    private String notifyUrl;//回调地址
    
    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JSONObject smPay(PayEntity payEntity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String callback(Map<String, String> data) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * 
     * @Description 组装支付请求参数
     * @param entity
     * @param type 1 网银   其他 扫码
     * @return
     * @throws Exception
     */
    private Map<String,String> sealRequest(PayEntity entity,int type)throws Exception{
        logger.info("[SANS]三生支付组装支付请求参数开始=================START==============");
        try {
            Map<String,String> data = new HashMap<>();
            String amount = new DecimalFormat("0.00").format(entity.getAmount());//订单金额
            data.put("total_fee",amount);//订单金额
            data.put("order_sn",entity.getOrderNo());//商家订单号
            data.put("goods","TOP-UP");//商品名
            if(StringUtils.isBlank(entity.getMobile())){
                data.put("client","web");//客户端类型（web，wap）
            }else{
                data.put("client","wap");//客户端类型（web，wap）
            }
            data.put("bank_code",entity.getPayCode());//银行编码（详情见 其它说明 - 银行编号）
            data.put("client_ip",entity.getIp());//客户端ip
            data.put("notify_url",notifyUrl);//异步通知地址
            data.put("return_url",entity.getRefererUrl());//同步跳转地址
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[SANS]三生支付组装支付请求参数异常:{}",e.getMessage());
            throw new Exception("[SANS]三生支付组装支付请求参数异常");
        }
    }
}
