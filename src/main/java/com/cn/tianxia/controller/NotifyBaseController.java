package com.cn.tianxia.controller;

import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.NotifyUtils;
import com.cn.tianxia.po.ResultResponse;
import com.cn.tianxia.service.NotifyService;
import com.cn.tianxia.vo.CagentYespayVO;
import com.cn.tianxia.vo.ProcessNotifyVO;
import com.cn.tianxia.vo.RechargeOrderVO;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Constructor;
import java.util.Map;

/**
 * @Auther: zed
 * @Date: 2019/1/17 17:19
 * @Description: 回调基础controller
 */
@RequestMapping("Notify")
@Controller
@Scope("prototype")
public class NotifyBaseController extends BaseController{
    @Autowired
    private NotifyService notifyService;

    public String ret_str_failed = "fail";

    public String processNotifyRequest(ProcessNotifyVO processNotifyVO){

        String order_no = processNotifyVO.getOrder_no();
        String clazz_name = processNotifyVO.getClazz_name();
        String ip = processNotifyVO.getIp();
        String ret__success = processNotifyVO.getRet__success();
        String trade_no = processNotifyVO.getTrade_no();
        String trade_status = processNotifyVO.getTrade_status();
        String t_trade_status = processNotifyVO.getT_trade_status();
        double realAmount = processNotifyVO.getRealAmount();
        String payment = processNotifyVO.getPayment();
        String serviceType = processNotifyVO.getService_type();
        Map<String,String> infoMap = JSONObject.fromObject(processNotifyVO.getInfoMap());


        if (payMap.containsKey(order_no)) {
            logger.info(clazz_name+"支付回调订单号:{}重复调用",order_no);
            return ret_str_failed;
        }
        payMap.put(order_no, "1");
        try {
            logger.info(clazz_name+"执行回调业务开始=========================START===========================");
            // 保存文件记录
            NotifyUtils.savePayFile(clazz_name, infoMap, ip);
            //通过订单号查询订单信息
            RechargeOrderVO rechargeOrderVO = notifyService.findNotifyOrderByOrderNo(order_no);
            if(rechargeOrderVO == null){
                logger.info(clazz_name+"支付回调通知订单号为非法订单号,查询订单信息失败,订单号:{}",order_no);
                return ret__success;
            }
            rechargeOrderVO.setTradeNo(trade_no);
            rechargeOrderVO.setTradeStatus(trade_status);
            rechargeOrderVO.setSuccessStatus(t_trade_status);
            rechargeOrderVO.setNotifyIp(ip);
            rechargeOrderVO.setNotifyParams(JSONObject.fromObject(infoMap).toString());
            rechargeOrderVO.setOrderAmount(realAmount);//实际支付金额
            Integer payId = rechargeOrderVO.getPayId();//支付商ID
            //查询支付商信息
            CagentYespayVO cagentYespayVO = notifyService.getCagentYespayByPayId(payId);
            if(cagentYespayVO == null){
                logger.info(clazz_name+"非法支付商ID,查询支付商信息失败,支付商ID:{}",payId);
                return ret_str_failed;
            }
            String paymentName = cagentYespayVO.getPaymentName();//支付商编码
            Map<String,String> pmapsconfig = JSONObject.fromObject(cagentYespayVO.getPaymentConfig());//支付商配置信息
            logger.info(clazz_name+"支付回调验签开始=======================START====================");
            if (paymentName.equals(payment)) {
                PayService payService = getPayService(paymentName,pmapsconfig,serviceType);
                String rmsg = payService.callback(infoMap);
                if (!"success".equalsIgnoreCase(rmsg)) {
                    logger.info(clazz_name+"支付回调验签失败!");
                    notifyService.updateNotifyOrderDescription(rechargeOrderVO);
                    return ret_str_failed;
                }
                logger.info(clazz_name+"支付回调验签成功!");
            } else {
                // 异常请求
                logger.error(clazz_name+"支付回调异常请求");
                return ret_str_failed;
            }
            logger.info(clazz_name+"支付回调验签结束=======================END====================");

            logger.info("==========================处理订单回调业务并修改订单状态==========================");
            String result = notifyService.processNotifyOrder(rechargeOrderVO);
            if(ResultResponse.SUCCESS_CODE.equals(result)){
                logger.info(clazz_name+"支付回调业务处理成功=======================SUCCESS====================");
                return ret__success;
            }
            logger.info(clazz_name+"支付回调业务处理成功=======================FAILD====================");
            return ret_str_failed;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info(clazz_name+"支付回调业务处理异常:{}",e.getMessage());
            return ret_str_failed;
        }finally {
            if (payMap.containsKey(order_no)) {
                logger.info(clazz_name+"支付回调业务处理成功,删除缓存中的订单KEY:{}",order_no);
                payMap.remove(order_no);
            }
        }
    }

    /**
     * 获取支付实现类
     * @param provider
     * @param pmapsconfig
     * @param type
     * @return
     * @throws Exception
     */
    private PayService getPayService(String provider, Map<String, String> pmapsconfig, String type) throws Exception {
        logger.info("获取支付反射接口开始====================START========================");
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("com.cn.tianxia.pay.impl").append(".");// 包名
            sb.append(provider).append("PayServiceImpl");
            logger.info("反射接口包名:{}", sb.toString());
            // 创建构造器
            PayService payService;
            if (StringUtils.isBlank(type)) {
                Constructor<?> constructor = Class.forName(sb.toString()).getConstructor(Map.class);
                payService = (PayService) constructor.newInstance(pmapsconfig);
            } else {
                Constructor<?> constructor = Class.forName(sb.toString()).getConstructor(Map.class,String.class);
                payService = (PayService) constructor.newInstance(pmapsconfig,type);
            }
            return payService;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("获取支付反射接口异常:{}", e.getMessage());
            throw new Exception("获取支付反射接口异常");
        }
    }
}
