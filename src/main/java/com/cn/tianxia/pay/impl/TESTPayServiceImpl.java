package com.cn.tianxia.pay.impl;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.pay.po.PayResponse;
import com.cn.tianxia.pay.service.PayService;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @ClassName: TESTPayServiceImpl
 * @Description: 测试支付
 * @Author: Zed
 * @Date: 2019-01-10 11:08
 * @Version:1.0.0
 **/
public class TESTPayServiceImpl implements PayService {

    private final static Logger logger = LoggerFactory.getLogger(TESTPayServiceImpl.class);

    public TESTPayServiceImpl(Map<String, String>  map) {
    }
    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        logger.info("[TEST]测试网银支付成功");
        return PayResponse.wy_link("http://www.google.com");
    }

    @Override
    public JSONObject smPay(PayEntity payEntity) {
        logger.info("[TEST]测试扫码支付成功");
        return PayResponse.sm_link(payEntity,"http://www.google.com","下单成功");
    }

    @Override
    public String callback(Map<String, String> data) {
        logger.info("[TEST]测试支付验签成功");
        return "success";
    }
}
