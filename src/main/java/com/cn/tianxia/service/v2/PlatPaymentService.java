package com.cn.tianxia.service.v2;


import net.sf.json.JSONObject;

/**
 * 
 * @ClassName PlatPaymentService
 * @Description 平台支付商接口
 * @author Hardy
 * @Date 2018年12月31日 下午3:46:04
 * @version 1.0.0
 */
public interface PlatPaymentService {

    /**
     * 获取可用支付渠道
     * @param userId 用户id
     */
    JSONObject getPaymentChannel(String userId);

    /**
     * 获取支付渠道列表
     * @param userId 用户id
     * @param type 支付类型
     */
    JSONObject getPaymentList(String userId,String type);

}
