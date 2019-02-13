package com.cn.tianxia.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.cn.tianxia.pay.impl.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cn.tianxia.common.PayConstant;
import com.cn.tianxia.pay.impl.DFIFPayServiceImpl;
import com.cn.tianxia.pay.impl.EASYPayServiceImpl;
import com.cn.tianxia.pay.impl.FYZFPayServiceImpl;
import com.cn.tianxia.pay.impl.HPZFPayServiceImpl;
import com.cn.tianxia.pay.impl.HUIPPayServiceImpl;
import com.cn.tianxia.pay.impl.JIUPayServiceImpl;
import com.cn.tianxia.pay.impl.LOVEPayServiceImpl;
import com.cn.tianxia.pay.impl.SKPPayServiceImpl;
import com.cn.tianxia.pay.impl.SLONPayServiceImpl;
import com.cn.tianxia.pay.impl.TTZFPayServiceImpl;
import com.cn.tianxia.pay.impl.WDZFPayServiceImpl;
import com.cn.tianxia.pay.impl.XBBZFPayServiceImpl;
import com.cn.tianxia.pay.impl.XHEIPayServiceImpl;
import com.cn.tianxia.pay.impl.XHZFPayServiceImpl;
import com.cn.tianxia.pay.impl.YTBPPayServiceImpl;
import com.cn.tianxia.pay.impl.YXINPayServiceImpl;
import com.cn.tianxia.pay.impl.ZHUIPayServiceImpl;
import com.cn.tianxia.pay.utils.NotifyUtils;
import com.cn.tianxia.pay.utils.ParamsUtils;
import com.cn.tianxia.po.ResultResponse;
import com.cn.tianxia.service.NotifyService;
import com.cn.tianxia.util.IPTools;
import com.cn.tianxia.vo.CagentYespayVO;
import com.cn.tianxia.vo.RechargeOrderVO;

import net.sf.json.JSONObject;

/**
 * @ClassName: NewNotifyController
 * @Description: 新支付回调controller(新增支付回调都写在这里)
 * @Author: Zed
 * @Date: 2019-01-11 16:08
 * @Version:1.0.0
 **/
@RequestMapping("Notify")
@Controller
@Scope("prototype")
public class NewNotifyController extends BaseController{

    @Autowired
    private NotifyService notifyService;
    private String ret_str_failed = "fail";

    /**
     *
     * @Description  D15支付回调通知
     * @param request
     * @param response
     * @param session
     * @return
     */
    @RequestMapping("/DFIFNotify.do")
    @ResponseBody
    public String DFIFNotify(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        String ret__success = "success";//收到通知后请回复  success
        String clazz_name = "DFIFNotify";
        logger.info(clazz_name+"支付回调开始-----------------------------START------------------------------");
        String order_no = request.getParameter("orderId"); // 平台订单号
        if (StringUtils.isBlank(order_no)) {
            logger.info(clazz_name+"支付回调获取请求参数orderId为空!");
            return ret_str_failed;
        }
        //=================================获取回调基本参数结果--END===========================//
        if (payMap.containsKey(order_no)) {
            logger.info(clazz_name+"支付回调订单号:{}重复调用",order_no);
            return ret_str_failed;
        }
        payMap.put(order_no, "1");
        try {
            //通过订单号查询订单信息
            RechargeOrderVO rechargeOrderVO = notifyService.findNotifyOrderByOrderNo(order_no);
            if(rechargeOrderVO == null){
                logger.info(clazz_name+"支付回调通知订单号为非法订单号,查询订单信息失败,订单号:{}",order_no);
                return ret__success;
            }
            Integer payId = rechargeOrderVO.getPayId();//支付商ID
            //查询支付商信息
            CagentYespayVO cagentYespayVO = notifyService.getCagentYespayByPayId(payId);
            if(cagentYespayVO == null){
                logger.info(clazz_name+"非法支付商ID,查询支付商信息失败,支付商ID:{}",payId);
                return ret_str_failed;
            }
            Map<String,String> pmapsconfig = JSONObject.fromObject(cagentYespayVO.getPaymentConfig());//支付商配置信息
            Map<String,String> infoMap = ParamsUtils.getDFIFNotifyParams(request,pmapsconfig.get("PAY_PRIVATE_KEY"));
            if(infoMap == null || infoMap.isEmpty()){
                logger.info(clazz_name+"支付回调获取请求参数为空!");
                return ret_str_failed;
            }
            logger.info(clazz_name+"支付回调请求参数:{}",JSONObject.fromObject(infoMap).toString());
            //=================================获取回调基本参数结果--START===========================//
            String trade_no = infoMap.get("no");//流水号
            String trade_status = infoMap.get("result");//0:成功，其他失败
            String t_trade_status = "00";// 00:成功
            String order_amount = infoMap.get("amount");
            if(StringUtils.isBlank(order_amount)){
                logger.info(clazz_name+"获取实际支付金额为空!");
                return ret_str_failed;
            }
            String ip = StringUtils.isBlank(IPTools.getIp(request))?"127.0.0.1":IPTools.getIp(request);
            logger.info(clazz_name+"执行回调业务开始=========================START===========================");
            // 保存文件记录
            NotifyUtils.savePayFile(clazz_name, infoMap, IPTools.getIp(request));
            //更新支付订单信息
            rechargeOrderVO.setTradeNo(trade_no);
            rechargeOrderVO.setTradeStatus(trade_status);
            rechargeOrderVO.setSuccessStatus(t_trade_status);
            rechargeOrderVO.setNotifyIp(ip);
            rechargeOrderVO.setNotifyParams(JSONObject.fromObject(infoMap).toString());
            rechargeOrderVO.setOrderAmount(Double.parseDouble(order_amount));

            logger.info(clazz_name+"支付回调验签开始=======================START====================");

            String paymentName = cagentYespayVO.getPaymentName();//支付商编码
            if (paymentName.equals(PayConstant.CONSTANT_DFIF)) {
                DFIFPayServiceImpl qft = new DFIFPayServiceImpl(pmapsconfig);
                String rmsg = qft.callback(infoMap);
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
     * 
     * @Description 爱付支付回调通知
     * @param request
     * @param response
     * @param session
     * @return
     */
    @RequestMapping("/LOVENotify.do")
    @ResponseBody
    public String LOVENotify(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        String ret__success = "SUCCESS";// 成功返回SUCCESS
        String clazz_name = "LOVENotify";
        logger.info(clazz_name+"支付回调开始-----------------------------START------------------------------");
        Map<String,String> infoMap = ParamsUtils.getNotifyParams(request);
        if(infoMap == null || infoMap.isEmpty()){
          logger.info(clazz_name+"支付回调获取请求参数为空!");
          return ret_str_failed;
        }
        logger.info(clazz_name+"支付回调请求参数:{}",JSONObject.fromObject(infoMap).toString());
        //=================================获取回调基本参数结果--START===========================//
        String order_no = infoMap.get("merchant_billno");// 平台订单号
        String trade_no = infoMap.get("billno");// 平台订单号
        String trade_status = infoMap.get("status");//订单状态 100待支付 200已完成 300已取消
        String t_trade_status = "200";// 表示成功状态
        String order_amount = infoMap.get("paid_amount");
        if(StringUtils.isBlank(order_amount)){
            logger.info(clazz_name+"获取实际支付金额为空!");
            return ret_str_failed;
        }
        String ip = StringUtils.isBlank(IPTools.getIp(request))?"127.0.0.1":IPTools.getIp(request);
        //=================================获取回调基本参数结果--END===========================//
        if (payMap.containsKey(order_no)) {
            logger.info(clazz_name+"支付回调订单号:{}重复调用",order_no);
            return ret_str_failed;
        }
        payMap.put(order_no, "1");
        try {
            logger.info(clazz_name+"执行回调业务开始=========================START===========================");
            // 保存文件记录
            NotifyUtils.savePayFile(clazz_name, infoMap, IPTools.getIp(request));
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
            rechargeOrderVO.setOrderAmount(Double.parseDouble(order_amount));//实际支付金额
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
            if (paymentName.equals(PayConstant.CONSTANT_LOVE)) {
                LOVEPayServiceImpl xxb = new LOVEPayServiceImpl(pmapsconfig);
                String rmsg = xxb.callback(infoMap);
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
     * 
     * @Description 爽快支付回调通知
     * @param request
     * @param response
     * @param session
     * @return
     */
    @RequestMapping("/SKPNotify.do")
    @ResponseBody
    public String SKPNotify(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        String ret__success = "success";// 成功返回SUCCESS
        String clazz_name = "SKPNotify";
        logger.info(clazz_name+"支付回调开始-----------------------------START------------------------------");
        Map<String,String> infoMap = ParamsUtils.getNotifyParams(request);
        if(infoMap == null || infoMap.isEmpty()){
          logger.info(clazz_name+"支付回调获取请求参数为空!");
          return ret_str_failed;
        }
        logger.info(clazz_name+"支付回调请求参数:{}",JSONObject.fromObject(infoMap).toString());
        //=================================获取回调基本参数结果--START===========================//
        String order_no = infoMap.get("out_trade_no");// 平台订单号
        String trade_no = infoMap.get("trade_no");// 平台订单号
        String trade_status = infoMap.get("code");//success表示业务成功
        String t_trade_status = "success";// 表示成功状态
        String order_amount = infoMap.get("total_amount");
        if(StringUtils.isBlank(order_amount)){
            logger.info(clazz_name+"获取实际支付金额为空!");
            return ret_str_failed;
        }
        String ip = StringUtils.isBlank(IPTools.getIp(request))?"127.0.0.1":IPTools.getIp(request);
        //=================================获取回调基本参数结果--END===========================//
        if (payMap.containsKey(order_no)) {
            logger.info(clazz_name+"支付回调订单号:{}重复调用",order_no);
            return ret_str_failed;
        }
        payMap.put(order_no, "1");
        try {
            logger.info(clazz_name+"执行回调业务开始=========================START===========================");
            // 保存文件记录
            NotifyUtils.savePayFile(clazz_name, infoMap, IPTools.getIp(request));
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
            rechargeOrderVO.setOrderAmount(Double.parseDouble(order_amount));//实际支付金额
            Integer payId = rechargeOrderVO.getPayId();//支付商ID
            //查询支付商信息
            CagentYespayVO cagentYespayVO = notifyService.getCagentYespayByPayId(payId);
            if(cagentYespayVO == null){
                logger.info(clazz_name+"非法支付商ID,查询支付商信息失败,支付商ID:{}",payId);
                return ret_str_failed;
            }
            String paymentName = cagentYespayVO.getPaymentName();//支付商编码
            Map<String,String> pmapsconfig = JSONObject.fromObject(cagentYespayVO.getPaymentConfig());//支付商配置信息
            //获取支付类型
            String type = getPayConfigType(String.valueOf(rechargeOrderVO.getPayType()));
            if(StringUtils.isBlank(type)){
                logger.info(clazz_name+"回调验签获取支付配置文件类型为空");
                return ret_str_failed;
            }
            logger.info(clazz_name+"支付回调验签开始=======================START====================");
            if (paymentName.equals(PayConstant.CONSTANT_SKP)) {
                SKPPayServiceImpl xxb = new SKPPayServiceImpl(pmapsconfig,type);
                String rmsg = xxb.callback(infoMap);
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
    *
    * @Description  HUIP汇付支付回调通知
    * @param request
    * @param response
    * @param session
    * @return
    */
    @RequestMapping("/HUIPNotify.do")
    @ResponseBody
    public String HUIPNotify(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
       String ret__success = "OK";//收到通知后请回复  OK
       String clazz_name = "HUIPNotify";
       logger.info(clazz_name+"支付回调开始-----------------------------START------------------------------");
       Map<String,String> infoMap = ParamsUtils.getNotifyParams(request);
       if(infoMap == null || infoMap.isEmpty()){
           logger.info(clazz_name+"支付回调获取请求参数为空!");
           return ret_str_failed;
       }
       logger.info(clazz_name+"支付回调请求参数:{}",JSONObject.fromObject(infoMap).toString());
       //=================================获取回调基本参数结果--START===========================//
       String order_amount = infoMap.get("amount");//实际支付金额
       if(StringUtils.isBlank(order_amount)){
           logger.info(clazz_name+"获取实际支付金额为空!");
           return ret_str_failed;
       }
       String order_no = infoMap.get("orderid");// 平台订单号
       String trade_no = infoMap.get("transaction_id");//交易流水号
       String trade_status = infoMap.get("returncode");//00代表支付成功
       String t_trade_status = "00";//1:成功
       String ip = StringUtils.isBlank(IPTools.getIp(request))?"127.0.0.1":IPTools.getIp(request);
       //=================================获取回调基本参数结果--END===========================//
       if (payMap.containsKey(order_no)) {
           logger.info(clazz_name+"支付回调订单号:{}重复调用",order_no);
           return ret_str_failed;
       }
       payMap.put(order_no, "1");
       try {
           logger.info(clazz_name+"执行回调业务开始=========================START===========================");
           // 保存文件记录
           // 保存文件记录
           NotifyUtils.savePayFile(clazz_name, infoMap, IPTools.getIp(request));
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
           rechargeOrderVO.setOrderAmount(Double.parseDouble(order_amount));
           rechargeOrderVO.setNotifyParams(JSONObject.fromObject(infoMap).toString());
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
           if (paymentName.equals(PayConstant.CONSTANT_HUIP)) {
        	   HUIPPayServiceImpl xxb = new HUIPPayServiceImpl(pmapsconfig);
               String rmsg = xxb.callback(infoMap);
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
     *
     * @Description 众惠支付回调通知
     * @param request
     * @param response
     * @param session
     * @return
     */
    @RequestMapping("/ZHUINotify.do")
    @ResponseBody
    public String ZHUINotify(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        String ret__success = "SUCCESS";// 成功返回SUCCESS
        String clazz_name = "ZHUINotify";
        logger.info(clazz_name+"支付回调开始-----------------------------START------------------------------");
        Map<String,String> infoMap = ParamsUtils.getNotifyParams(request);
        if(infoMap == null || infoMap.isEmpty()){
          logger.info(clazz_name+"支付回调获取请求参数为空!");
          return ret_str_failed;
        }
        logger.info(clazz_name+"支付回调请求参数:{}",JSONObject.fromObject(infoMap).toString());
        //=================================获取回调基本参数结果--START===========================//
        String order_no = infoMap.get("out_trade_no");// 平台订单号
        String trade_no = clazz_name + infoMap.get("cas_time_stamp");// 第三方流水号
        String trade_status = infoMap.get("status");//PAID表示业务成功
        String t_trade_status = "00";// 00表示支付成功，非00表示失败
        String order_amount = infoMap.get("total_fee"); // 分为单位
        if(StringUtils.isBlank(order_amount)){
            logger.info(clazz_name+"获取实际支付金额为空!");
            return ret_str_failed;
        }
        String ip = StringUtils.isBlank(IPTools.getIp(request))?"127.0.0.1":IPTools.getIp(request);
        //=================================获取回调基本参数结果--END===========================//
        if (payMap.containsKey(order_no)) {
            logger.info(clazz_name+"支付回调订单号:{}重复调用",order_no);
            return ret_str_failed;
        }
        payMap.put(order_no, "1");
        try {
            logger.info(clazz_name+"执行回调业务开始=========================START===========================");
            // 保存文件记录
            NotifyUtils.savePayFile(clazz_name, infoMap, IPTools.getIp(request));
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
            rechargeOrderVO.setOrderAmount(Double.parseDouble(order_amount)/100);//实际支付金额，分为单位
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
            if (paymentName.equals(PayConstant.CONSTANT_ZHUI)) {
                ZHUIPayServiceImpl zhui = new ZHUIPayServiceImpl(pmapsconfig);
                String rmsg = zhui.callback(infoMap);
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
     *
     * @Description 新币宝支付回调通知
     * @param request
     * @param response
     * @param session
     * @return
     */
    @RequestMapping("/XBBZFNotify.do")
    @ResponseBody
    public String XBBZFNotify(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        String ret__success = "{\"Success\":true,\"Code\":1,\"Message\":\"SUCCESS\" }";// 成功返回
        String clazz_name = "XBBZFNotify";
        logger.info(clazz_name+"支付回调开始-----------------------------START------------------------------");
        Map<String,String> infoMap = ParamsUtils.getNotifyParams(request);
        if(infoMap == null || infoMap.isEmpty()){
          logger.info(clazz_name+"支付回调获取请求参数为空!");
          return ret_str_failed;
        }
        logger.info(clazz_name+"支付回调请求参数:{}",JSONObject.fromObject(infoMap).toString());
        //=================================获取回调基本参数结果--START===========================//
        String order_no = infoMap.get("OrderNum");// 平台订单号
        String trade_no = clazz_name + infoMap.get("OrderId");// 第三方流水号
        String State1 = infoMap.get("State1"); //订单状态
        String State2 = infoMap.get("State2"); //支付状态
        String trade_status =  State1.equals("2") && State2.equals("2")?"success":"fail";
        String t_trade_status = "success";// 两个状态同时为2时，才给会员上分
        String order_amount = infoMap.get("LegalAmount"); // 实际充值金额，单位元
        if(StringUtils.isBlank(order_amount)){
            logger.info(clazz_name+"获取实际支付金额为空!");
            return ret_str_failed;
        }
        double amount = Double.parseDouble(order_amount);
        logger.info(clazz_name + "实际充值金额为：{}",amount);
        String ip = StringUtils.isBlank(IPTools.getIp(request))?"127.0.0.1":IPTools.getIp(request);
        //=================================获取回调基本参数结果--END===========================//
        if (payMap.containsKey(order_no)) {
            logger.info(clazz_name+"支付回调订单号:{}重复调用",order_no);
            return ret_str_failed;
        }
        payMap.put(order_no, "1");
        try {
            logger.info(clazz_name+"执行回调业务开始=========================START===========================");
            // 保存文件记录
            NotifyUtils.savePayFile(clazz_name, infoMap, IPTools.getIp(request));
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
            rechargeOrderVO.setOrderAmount(amount);//实际支付金额，单位元
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
            if (paymentName.equals(PayConstant.CONSTANT_XBBZF)) {
                XBBZFPayServiceImpl xbbzfPayService = new XBBZFPayServiceImpl(pmapsconfig);
                String rmsg = xbbzfPayService.callback(infoMap);
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
     *
     * @Description 九久支付回调通知
     * @param request
     * @param response
     * @param session
     * @return
     */
    @RequestMapping("/JIUNotify.do")
    @ResponseBody
    public String JIUNotify(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        String ret__success = "success";// 成功返回SUCCESS
        String clazz_name = "JIUNotify";
        logger.info(clazz_name+"支付回调开始-----------------------------START------------------------------");
        Map<String,String> infoMap = ParamsUtils.getNotifyParams(request);
        if(infoMap == null || infoMap.isEmpty()){
            logger.info(clazz_name+"支付回调获取请求参数为空!");
            return ret_str_failed;
        }
        logger.info(clazz_name+"支付回调请求参数:{}",JSONObject.fromObject(infoMap).toString());
        //=================================获取回调基本参数结果--START===========================//
        String order_no = infoMap.get("mchno");// 平台订单号
        String trade_no = infoMap.get("transactionid");// 第三方流水号
        String trade_status = infoMap.get("resultcode");//PAID表示业务成功
        String t_trade_status = "1";// 1 成功 0 失败
        String order_amount = infoMap.get("totalfee"); // 分为单位
        if(StringUtils.isBlank(order_amount)){
            logger.info(clazz_name+"获取实际支付金额为空!");
            return ret_str_failed;
        }
        double amount = Double.parseDouble(order_amount);
        logger.info(clazz_name + "实际充值金额为：{}",amount);
        String ip = StringUtils.isBlank(IPTools.getIp(request))?"127.0.0.1":IPTools.getIp(request);
        //=================================获取回调基本参数结果--END===========================//
        if (payMap.containsKey(order_no)) {
            logger.info(clazz_name+"支付回调订单号:{}重复调用",order_no);
            return ret_str_failed;
        }
        payMap.put(order_no, "1");
        try {
            logger.info(clazz_name+"执行回调业务开始=========================START===========================");
            // 保存文件记录
            NotifyUtils.savePayFile(clazz_name, infoMap, IPTools.getIp(request));
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
            rechargeOrderVO.setOrderAmount(amount);//实际支付金额，分为单位
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
            if (paymentName.equals(PayConstant.CONSTANT_JIU)) {
                JIUPayServiceImpl jiu = new JIUPayServiceImpl(pmapsconfig);
                String rmsg = jiu.callback(infoMap);
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
     *
     * @Description 万达支付回调通知
     * @param request
     * @param response
     * @param session
     * @return
     */
    @RequestMapping("/WDZFNotify.do")
    @ResponseBody
    public String WDZFNotify(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        String ret__success = "success";// 成功返回SUCCESS
        String clazz_name = "WDZFNotify";
        logger.info(clazz_name+"支付回调开始-----------------------------START------------------------------");
        Map<String,String> infoMap = ParamsUtils.getNotifyParams(request);
        if(infoMap == null || infoMap.isEmpty()){
            logger.info(clazz_name+"支付回调获取请求参数为空!");
            return ret_str_failed;
        }
        logger.info(clazz_name+"支付回调请求参数:{}",JSONObject.fromObject(infoMap).toString());
        //=================================获取回调基本参数结果--START===========================//
        String order_no = infoMap.get("customerId");// 平台订单号
        String trade_no = infoMap.get("orderId");// 第三方流水号
        String trade_status = infoMap.get("status");//PAID表示业务成功
        String t_trade_status = "1";// 1 成功 0 失败
        String order_amount = infoMap.get("money"); // 分为单位
        if(StringUtils.isBlank(order_amount)){
            logger.info(clazz_name+"获取实际支付金额为空!");
            return ret_str_failed;
        }
        double amount = Double.parseDouble(order_amount);
        logger.info(clazz_name + "实际充值金额为：{}",amount);
        String ip = StringUtils.isBlank(IPTools.getIp(request))?"127.0.0.1":IPTools.getIp(request);
        //=================================获取回调基本参数结果--END===========================//
        if (payMap.containsKey(order_no)) {
            logger.info(clazz_name+"支付回调订单号:{}重复调用",order_no);
            return ret_str_failed;
        }
        payMap.put(order_no, "1");
        try {
            logger.info(clazz_name+"执行回调业务开始=========================START===========================");
            // 保存文件记录
            NotifyUtils.savePayFile(clazz_name, infoMap, IPTools.getIp(request));
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
            rechargeOrderVO.setOrderAmount(amount);//实际支付金额，单位元
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
            if (paymentName.equals(PayConstant.CONSTANT_WDZF)) {
                WDZFPayServiceImpl jiu = new WDZFPayServiceImpl(pmapsconfig);
                String rmsg = jiu.callback(infoMap);
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
     * 
     * @Description 飞鹰支付回调通知
     * @param request
     * @param response
     * @param session
     * @return
     */
    @RequestMapping("/FYZFNotify.do")
    @ResponseBody
    public String FYZFNotify(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        String ret__success = "SUCCESS";// 成功返回SUCCESS
        String clazz_name = "FYZFNotify";
        logger.info(clazz_name+"支付回调开始-----------------------------START------------------------------");
        Map<String,String> infoMap = ParamsUtils.getNotifyParams(request);
        if(infoMap == null || infoMap.isEmpty()){
            logger.info(clazz_name+"支付回调获取请求参数为空!");
            return ret_str_failed;
        }
        logger.info(clazz_name+"支付回调请求参数:{}",JSONObject.fromObject(infoMap).toString());
        //=================================获取回调基本参数结果--START===========================//
        String order_no = infoMap.get("outTradeNo");// 平台订单号
        String trade_no = infoMap.get("trxNo");// 第三方流水号
        String trade_status = infoMap.get("tradeStatus");//订单状态，SUCCESS（支付成功）FAILED（支付失败）WAITING_PAYMENT（等待支付）
        String t_trade_status = "SUCCESS";// 订单状态，SUCCESS（支付成功）FAILED（支付失败）WAITING_PAYMENT（等待支付）
        String order_amount = infoMap.get("orderAmount"); //单位为元，小数两位
        if(StringUtils.isBlank(order_amount)){
            logger.info(clazz_name+"获取实际支付金额为空!");
            return ret_str_failed;
        }
        double amount = Double.parseDouble(order_amount);
        logger.info(clazz_name + "实际充值金额为：{}",amount);
        String ip = StringUtils.isBlank(IPTools.getIp(request))?"127.0.0.1":IPTools.getIp(request);
        //=================================获取回调基本参数结果--END===========================//
        if (payMap.containsKey(order_no)) {
            logger.info(clazz_name+"支付回调订单号:{}重复调用",order_no);
            return ret_str_failed;
        }
        payMap.put(order_no, "1");
        try {
            logger.info(clazz_name+"执行回调业务开始=========================START===========================");
            // 保存文件记录
            NotifyUtils.savePayFile(clazz_name, infoMap, IPTools.getIp(request));
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
            rechargeOrderVO.setOrderAmount(amount);//实际支付金额，单位元
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
            if (paymentName.equals(PayConstant.CONSTANT_FYZF)) {
                FYZFPayServiceImpl jiu = new FYZFPayServiceImpl(pmapsconfig);
                String rmsg = jiu.callback(infoMap);
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
     * 
     * @Description (废弃)
     * @param request
     * @param response
     * @param session
     * @return
     */
    @RequestMapping("/TTZFNotify.do")
    @ResponseBody
    public String TTZFNotify(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        String ret__success = "SUCCESS";// 成功返回SUCCESS
        String clazz_name = "TTZFNotify";
        logger.info(clazz_name+"支付回调开始-----------------------------START------------------------------");
        Map<String,String> infoMap = ParamsUtils.getNotifyParams(request);
        if(infoMap == null || infoMap.isEmpty()){
            logger.info(clazz_name+"支付回调获取请求参数为空!");
            return ret_str_failed;
        }
        logger.info(clazz_name+"支付回调请求参数:{}",JSONObject.fromObject(infoMap).toString());
        //=================================获取回调基本参数结果--START===========================//
        String order_no = infoMap.get("outTradeNo");// 平台订单号
        String trade_no = infoMap.get("outChannelNo");// 第三方流水号
        String trade_status = infoMap.get("status");//订单状态，01：未支付 02：已支付
        String t_trade_status = "02";// 订单状态，01：未支付 02：已支付
        String order_amount = infoMap.get("amount"); //单位为元，小数两位
        if(StringUtils.isBlank(order_amount)){
            logger.info(clazz_name+"获取实际支付金额为空!");
            return ret_str_failed;
        }
        double amount = Double.parseDouble(order_amount);
        logger.info(clazz_name + "实际充值金额为：{}",amount);
        String ip = StringUtils.isBlank(IPTools.getIp(request))?"127.0.0.1":IPTools.getIp(request);
        //=================================获取回调基本参数结果--END===========================//
        if (payMap.containsKey(order_no)) {
            logger.info(clazz_name+"支付回调订单号:{}重复调用",order_no);
            return ret_str_failed;
        }
        payMap.put(order_no, "1");
        try {
            logger.info(clazz_name+"执行回调业务开始=========================START===========================");
            // 保存文件记录
            NotifyUtils.savePayFile(clazz_name, infoMap, IPTools.getIp(request));
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
            rechargeOrderVO.setOrderAmount(amount);//实际支付金额，单位元
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
            if (paymentName.equals(PayConstant.CONSTANT_TTZF)) {
                TTZFPayServiceImpl jiu = new TTZFPayServiceImpl(pmapsconfig);
                String rmsg = jiu.callback(infoMap);
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
     * 
     * @Description 恒付支付回调通知
     * @param request
     * @param response
     * @param session
     * @return
     */
    @RequestMapping("/HPZFNotify.do")
    @ResponseBody
    public String HPZFNotify(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        String ret__success = "200";// 成功返回SUCCESS
        String clazz_name = "HPZFNotify";
        logger.info(clazz_name+"支付回调开始-----------------------------START------------------------------");
        Map<String,String> infoMap = ParamsUtils.getNotifyParams(request);
        if(infoMap == null || infoMap.isEmpty()){
            logger.info(clazz_name+"支付回调获取请求参数为空!");
            return ret_str_failed;
        }
        logger.info(clazz_name+"支付回调请求参数:{}",JSONObject.fromObject(infoMap).toString());
        //=================================获取回调基本参数结果--START===========================//
        String order_no = infoMap.get("userReamrk");// 平台订单号
        String trade_no = infoMap.get("depositNumber");// 第三方流水号
        String trade_status = "0000";//订单状态，01：未支付 02：已支付
        String t_trade_status = "0000";// 订单状态，01：未支付 02：已支付
        String order_amount = infoMap.get("amount"); //单位为元，小数两位
        if(StringUtils.isBlank(order_amount)){
            logger.info(clazz_name+"获取实际支付金额为空!");
            return ret_str_failed;
        }
        double amount = Double.parseDouble(order_amount);
        logger.info(clazz_name + "实际充值金额为：{}",amount);
        String ip = StringUtils.isBlank(IPTools.getIp(request))?"127.0.0.1":IPTools.getIp(request);
        //=================================获取回调基本参数结果--END===========================//
        if (payMap.containsKey(order_no)) {
            logger.info(clazz_name+"支付回调订单号:{}重复调用",order_no);
            return ret_str_failed;
        }
        payMap.put(order_no, "1");
        try {
            logger.info(clazz_name+"执行回调业务开始=========================START===========================");
            // 保存文件记录
            NotifyUtils.savePayFile(clazz_name, infoMap, IPTools.getIp(request));
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
            rechargeOrderVO.setOrderAmount(amount);//实际支付金额，单位元
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
            if (paymentName.equals(PayConstant.CONSTANT_HPZF)) {
                HPZFPayServiceImpl jiu = new HPZFPayServiceImpl(pmapsconfig);
                String rmsg = jiu.callback(infoMap);
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
     * 
     * @Description 易通宝支付回调通知
     * @param request
     * @param response
     * @param session
     * @return
     */
    @RequestMapping("/YTBPNotify.do")
    @ResponseBody
    public String YTBPNotify(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        String ret__success = "OK";// 成功返回SUCCESS
        String clazz_name = "YTBPNotify";
        logger.info(clazz_name+"支付回调开始-----------------------------START------------------------------");
        Map<String,String> infoMap = ParamsUtils.getNotifyParams(request);
        if(infoMap == null || infoMap.isEmpty()){
            logger.info(clazz_name+"支付回调获取请求参数为空!");
            return ret_str_failed;
        }
        logger.info(clazz_name+"支付回调请求参数:{}",JSONObject.fromObject(infoMap).toString());
        //=================================获取回调基本参数结果--START===========================//
        String order_no = infoMap.get("orderid");// 平台订单号
        String trade_no = infoMap.get("transaction_id");// 第三方流水号
        String trade_status = infoMap.get("returncode");//订单状态，“00” 为成功
        String t_trade_status = "00";// 订单状态，“00” 为成功
        String order_amount = infoMap.get("amount"); //单位为元，小数两位
        if(StringUtils.isBlank(order_amount)){
            logger.info(clazz_name+"获取实际支付金额为空!");
            return ret_str_failed;
        }
        double amount = Double.parseDouble(order_amount);
        logger.info(clazz_name + "实际充值金额为：{}",amount);
        String ip = StringUtils.isBlank(IPTools.getIp(request))?"127.0.0.1":IPTools.getIp(request);
        //=================================获取回调基本参数结果--END===========================//
        if (payMap.containsKey(order_no)) {
            logger.info(clazz_name+"支付回调订单号:{}重复调用",order_no);
            return ret_str_failed;
        }
        payMap.put(order_no, "1");
        try {
            logger.info(clazz_name+"执行回调业务开始=========================START===========================");
            // 保存文件记录
            NotifyUtils.savePayFile(clazz_name, infoMap, IPTools.getIp(request));
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
            rechargeOrderVO.setOrderAmount(amount);//实际支付金额，单位元
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
            if (paymentName.equals(PayConstant.CONSTANT_YTBP)) {
                YTBPPayServiceImpl jiu = new YTBPPayServiceImpl(pmapsconfig);
                String rmsg = jiu.callback(infoMap);
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
     * 
     * @Description 银鑫支付回调通知
     * @param request
     * @param response
     * @param session
     * @return
     */
    @RequestMapping("/YXINNotify.do")
    @ResponseBody
    public String YXINNotify(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        String ret__success = "success";// 成功返回SUCCESS
        String clazz_name = "YXINNotify";
        logger.info(clazz_name+"支付回调开始-----------------------------START------------------------------");
        Map<String,String> infoMap = ParamsUtils.getNotifyParams(request);
        if(infoMap == null || infoMap.isEmpty()){
            logger.info(clazz_name+"支付回调获取请求参数为空!");
            return ret_str_failed;
        }
        logger.info(clazz_name+"支付回调请求参数:{}",JSONObject.fromObject(infoMap).toString());
        //=================================获取回调基本参数结果--START===========================//
        String order_no = infoMap.get("orderId");// 平台订单号
        String trade_no = "YXIN"+System.currentTimeMillis();//infoMap.get("transaction_id");// 第三方流水号
        String trade_status = "收款成功".equals(infoMap.get("state"))?"0000":"1111";//订单状态，“0000” 为成功
        String t_trade_status = "0000";// 订单状态，“00” 为成功
        String order_amount = infoMap.get("amount"); //单位为元，小数两位
        if(StringUtils.isBlank(order_amount)){
            logger.info(clazz_name+"获取实际支付金额为空!");
            return ret_str_failed;
        }
        double amount = Double.parseDouble(order_amount);
        logger.info(clazz_name + "实际充值金额为：{}",amount);
        String ip = StringUtils.isBlank(IPTools.getIp(request))?"127.0.0.1":IPTools.getIp(request);
        //=================================获取回调基本参数结果--END===========================//
        if (payMap.containsKey(order_no)) {
            logger.info(clazz_name+"支付回调订单号:{}重复调用",order_no);
            return ret_str_failed;
        }
        payMap.put(order_no, "1");
        try {
            logger.info(clazz_name+"执行回调业务开始=========================START===========================");
            // 保存文件记录
            NotifyUtils.savePayFile(clazz_name, infoMap, IPTools.getIp(request));
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
            rechargeOrderVO.setOrderAmount(amount);//实际支付金额，单位元
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
            if (paymentName.equals(PayConstant.CONSTANT_YXIN)) {
                YXINPayServiceImpl jiu = new YXINPayServiceImpl(pmapsconfig);
                String rmsg = jiu.callback(infoMap);
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
     * 
     * @Description 通支付2回调通知
     * @param request
     * @param response
     * @param session
     * @return
     */
    @RequestMapping("/EASYNotify.do")
    @ResponseBody
    public String EASYNotify(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        String ret__success = "success";// 成功返回SUCCESS
        String clazz_name = "EASYNotify";
        logger.info(clazz_name+"支付回调开始-----------------------------START------------------------------");
        Map<String,String> infoMap = ParamsUtils.getNotifyParams(request);
        if(infoMap == null || infoMap.isEmpty()){
            logger.info(clazz_name+"支付回调获取请求参数为空!");
            return ret_str_failed;
        }
        logger.info(clazz_name+"支付回调请求参数:{}",JSONObject.fromObject(infoMap).toString());
        //=================================获取回调基本参数结果--START===========================//
        String order_no = infoMap.get("fxddh");// 平台订单号
        String trade_no = infoMap.get("fxorder");// 第三方流水号
        String trade_status = infoMap.get("fxstatus");//【1代表支付成功
        String t_trade_status = "1";// 订单状态，“1” 为成功
        String order_amount = infoMap.get("fxfee"); //单位为元，小数两位
        if(StringUtils.isBlank(order_amount)){
            logger.info(clazz_name+"获取实际支付金额为空!");
            return ret_str_failed;
        }
        double amount = Double.parseDouble(order_amount);
        logger.info(clazz_name + "实际充值金额为：{}",amount);
        String ip = StringUtils.isBlank(IPTools.getIp(request))?"127.0.0.1":IPTools.getIp(request);
        //=================================获取回调基本参数结果--END===========================//
        if (payMap.containsKey(order_no)) {
            logger.info(clazz_name+"支付回调订单号:{}重复调用",order_no);
            return ret_str_failed;
        }
        payMap.put(order_no, "1");
        try {
            logger.info(clazz_name+"执行回调业务开始=========================START===========================");
            // 保存文件记录
            NotifyUtils.savePayFile(clazz_name, infoMap, IPTools.getIp(request));
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
            rechargeOrderVO.setOrderAmount(amount);//实际支付金额，单位元
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
            if (paymentName.equals(PayConstant.CONSTANT_EASY)) {
                EASYPayServiceImpl jiu = new EASYPayServiceImpl(pmapsconfig);
                String rmsg = jiu.callback(infoMap);
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
     * 
     * @Description 联盛支付回调通知
     * @param request
     * @param response
     * @param session
     * @return
     */
    @RequestMapping("/LSZFNotify.do")
    @ResponseBody
    public String LSZFNotify(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        logger.info(" LSZFNotify(HttpServletRequest request = {}  -start"+ request);
        String ret__success = "success";// 成功返回SUCCESS
        String clazz_name = "LSZFNotify";
        logger.info(clazz_name+"支付回调开始-----------------------------START------------------------------");
        Map<String,String> infoMap = ParamsUtils.getNotifyParams(request);
        if(infoMap == null || infoMap.isEmpty()){
            logger.info(clazz_name+"支付回调获取请求参数为空!");
            return ret_str_failed;
        }
        logger.info(clazz_name+"支付回调请求参数:{}",JSONObject.fromObject(infoMap).toString());
        //=================================获取回调基本参数结果--START===========================//
        String order_no = infoMap.get("orderId");// 平台订单号
        String trade_no = infoMap.get("sn");// 第三方流水号
        String trade_status = "success";//定义为成功状态，第三方没有返回状态码判断
        String t_trade_status = "success";// 订单状态，“1” 为成功
        String order_amount = infoMap.get("amount"); //单位为元，小数两位
        if(StringUtils.isBlank(order_amount)){
            logger.info(clazz_name+"获取实际支付金额为空!");
            return ret_str_failed;
        }
        double amount = Double.parseDouble(order_amount);
        logger.info(clazz_name + "实际充值金额为：{}",amount);
        String ip = StringUtils.isBlank(IPTools.getIp(request))?"127.0.0.1":IPTools.getIp(request);
        //=================================获取回调基本参数结果--END===========================//
        if (payMap.containsKey(order_no)) {
            logger.info(clazz_name+"支付回调订单号:{}重复调用",order_no);
            return ret_str_failed;
        }
        payMap.put(order_no, "1");
        try {
            logger.info(clazz_name+"执行回调业务开始=========================START===========================");
            // 保存文件记录
            NotifyUtils.savePayFile(clazz_name, infoMap, IPTools.getIp(request));
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
            rechargeOrderVO.setOrderAmount(amount);//实际支付金额，单位元
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
            if (paymentName.equals(PayConstant.CONSTANT_EASY)) {
                EASYPayServiceImpl jiu = new EASYPayServiceImpl(pmapsconfig);
                String rmsg = jiu.callback(infoMap);
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
            logger.error("联盛支付回调失败，系统业务异常，异常订单号:{}"+ order_no );
            e.printStackTrace();
            logger.info(clazz_name+"支付回调业务处理异常:{}"+ e.getMessage(),e);
            return ret_str_failed;
        }finally {
            if (payMap.containsKey(order_no)) {
                logger.info(clazz_name+"支付回调业务处理成功,删除缓存中的订单KEY:{}",order_no);
                payMap.remove(order_no);
            }
        }
    }
    
    
    
    /**
     * 
     * @Description 顺隆支付回调通知
     * @param request
     * @param response
     * @param session
     * @return
     */
    @RequestMapping("/SLONNotify.do")
    @ResponseBody
    public String SLONNotify(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        String ret__success = "OK";// 成功返回SUCCESS
        String clazz_name = "SLONNotify";
        logger.info(clazz_name+"支付回调开始-----------------------------START------------------------------");
        Map<String,String> infoMap = ParamsUtils.getNotifyParams(request);
        if(infoMap == null || infoMap.isEmpty()){
            logger.info(clazz_name+"支付回调获取请求参数为空!");
            return ret_str_failed;
        }
        logger.info(clazz_name+"支付回调请求参数:{}",JSONObject.fromObject(infoMap).toString());
        //=================================获取回调基本参数结果--START===========================//
        String order_no = infoMap.get("orderid");// 平台订单号
        String trade_no = infoMap.get("transaction_id");// 第三方流水号
        String trade_status = infoMap.get("returncode");//订单状态，“00” 为成功
        String t_trade_status = "00";// 订单状态，“00” 为成功
        String order_amount = infoMap.get("amount"); //单位为元，小数两位
        if(StringUtils.isBlank(order_amount)){
            logger.info(clazz_name+"获取实际支付金额为空!");
            return ret_str_failed;
        }
        double amount = Double.parseDouble(order_amount);
        logger.info(clazz_name + "实际充值金额为：{}",amount);
        String ip = StringUtils.isBlank(IPTools.getIp(request))?"127.0.0.1":IPTools.getIp(request);
        //=================================获取回调基本参数结果--END===========================//
        if (payMap.containsKey(order_no)) {
            logger.info(clazz_name+"支付回调订单号:{}重复调用",order_no);
            return ret_str_failed;
        }
        payMap.put(order_no, "1");
        try {
            logger.info(clazz_name+"执行回调业务开始=========================START===========================");
            // 保存文件记录
            NotifyUtils.savePayFile(clazz_name, infoMap, IPTools.getIp(request));
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
            rechargeOrderVO.setOrderAmount(amount);//实际支付金额，单位元
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
            if (paymentName.equals(PayConstant.CONSTANT_SLON)) {
                SLONPayServiceImpl jiu = new SLONPayServiceImpl(pmapsconfig);
                String rmsg = jiu.callback(infoMap);
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
     *
     * @Description 大宝天下支付回调通知
     * @param request
     * @param response
     * @param session
     * @return
     */
    @RequestMapping("/DBTXNotify.do")
    @ResponseBody
    public String DBTXNotify(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        String ret__success = "OK";// 成功返回SUCCESS
        String clazz_name = "SLONNotify";
        logger.info(clazz_name+"支付回调开始-----------------------------START------------------------------");
        Map<String,String> infoMap = ParamsUtils.getNotifyParams(request);
        if(infoMap == null || infoMap.isEmpty()){
            logger.info(clazz_name+"支付回调获取请求参数为空!");
            return ret_str_failed;
        }
        logger.info(clazz_name+"支付回调请求参数:{}",JSONObject.fromObject(infoMap).toString());
        //=================================获取回调基本参数结果--START===========================//
        String order_no = infoMap.get("orderid");// 平台订单号
        String trade_no = infoMap.get("transaction_id");// 第三方流水号
        String trade_status = infoMap.get("returncode");//订单状态，“00” 为成功
        String t_trade_status = "00";// 订单状态，“00” 为成功
        String order_amount = infoMap.get("amount"); //单位为元，小数两位
        if(StringUtils.isBlank(order_amount)){
            logger.info(clazz_name+"获取实际支付金额为空!");
            return ret_str_failed;
        }
        double amount = Double.parseDouble(order_amount);
        logger.info(clazz_name + "实际充值金额为：{}",amount);
        String ip = StringUtils.isBlank(IPTools.getIp(request))?"127.0.0.1":IPTools.getIp(request);
        //=================================获取回调基本参数结果--END===========================//
        if (payMap.containsKey(order_no)) {
            logger.info(clazz_name+"支付回调订单号:{}重复调用",order_no);
            return ret_str_failed;
        }
        payMap.put(order_no, "1");
        try {
            logger.info(clazz_name+"执行回调业务开始=========================START===========================");
            // 保存文件记录
            NotifyUtils.savePayFile(clazz_name, infoMap, IPTools.getIp(request));
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
            rechargeOrderVO.setOrderAmount(amount);//实际支付金额，单位元
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
            if (paymentName.equals("DBTX")) {
                DBTXPayServiceImpl dbtx = new DBTXPayServiceImpl(pmapsconfig);
                String rmsg = dbtx.callback(infoMap);
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
     *
     * @Description 聚合银码支付回调通知
     * @param request
     * @param response
     * @param session
     * @return
     */
    @RequestMapping("/JHYMNotify.do")
    @ResponseBody
    public String JHYMNotify(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        String ret__success = "OK";// 成功返回SUCCESS
        String clazz_name = "JHYMNotify";
        logger.info(clazz_name+"支付回调开始-----------------------------START------------------------------");
        Map<String,String> infoMap = ParamsUtils.getNotifyParams(request);
        if(infoMap == null || infoMap.isEmpty()){
            logger.info(clazz_name+"支付回调获取请求参数为空!");
            return ret_str_failed;
        }
        logger.info(clazz_name+"支付回调请求参数:{}",JSONObject.fromObject(infoMap).toString());
        //=================================获取回调基本参数结果--START===========================//
        String order_no = infoMap.get("out_trade_no");// 平台订单号
        String trade_no = infoMap.get("trade_no");// 第三方流水号
        String trade_status = infoMap.get("trade_status");//订单状态，“00” 为成功
        String t_trade_status = "TRADE_SUCCESS";// TRADE_SUCCESS
        String order_amount = infoMap.get("money"); //单位为元，小数两位
        if(StringUtils.isBlank(order_amount)){
            logger.info(clazz_name+"获取实际支付金额为空!");
            return ret_str_failed;
        }
        double amount = Double.parseDouble(order_amount);
        logger.info(clazz_name + "实际充值金额为：{}",amount);
        String ip = StringUtils.isBlank(IPTools.getIp(request))?"127.0.0.1":IPTools.getIp(request);
        //=================================获取回调基本参数结果--END===========================//
        if (payMap.containsKey(order_no)) {
            logger.info(clazz_name+"支付回调订单号:{}重复调用",order_no);
            return ret_str_failed;
        }
        payMap.put(order_no, "1");
        try {
            logger.info(clazz_name+"执行回调业务开始=========================START===========================");
            // 保存文件记录
            NotifyUtils.savePayFile(clazz_name, infoMap, IPTools.getIp(request));
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
            rechargeOrderVO.setOrderAmount(amount);//实际支付金额，单位元
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
            if (paymentName.equals("JHYM")) {
                JHYMPayServiceImpl jhymPayService = new JHYMPayServiceImpl(pmapsconfig);
                String rmsg = jhymPayService.callback(infoMap);
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
     *
     * @Description 城市互联支付回调通知
     * @param request
     * @param response
     * @param session
     * @return
     */
    @RequestMapping("/CSHLNotify.do")
    @ResponseBody
    public String CSHLNotify(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        String ret__success = "success";// success
        String clazz_name = "CSHLNotify";
        logger.info(clazz_name+"支付回调开始-----------------------------START------------------------------");
        Map<String,String> infoMap = ParamsUtils.getCSHLNotifyParams(request);
        if(infoMap == null || infoMap.isEmpty()){
            logger.info(clazz_name+"支付回调获取请求参数为空!");
            return ret_str_failed;
        }
        logger.info(clazz_name+"支付回调请求参数:{}",JSONObject.fromObject(infoMap).toString());
        //=================================获取回调基本参数结果--START===========================//
        String order_no = infoMap.get("orderNo");// 平台订单号
        String trade_no = infoMap.get("trxorderNo");// 第三方流水号
        String trade_status = infoMap.get("status");//0=失败；1=成功
        String t_trade_status = "1"; //成功状态
        String order_amount = infoMap.get("amount"); //单位 分
        if(StringUtils.isBlank(order_amount)){
            logger.info(clazz_name+"获取实际支付金额为空!");
            return ret_str_failed;
        }
        double amount = Double.parseDouble(order_amount)/100;
        logger.info(clazz_name + "实际充值金额为：{}",amount);
        String ip = StringUtils.isBlank(IPTools.getIp(request))?"127.0.0.1":IPTools.getIp(request);
        //=================================获取回调基本参数结果--END===========================//
        if (payMap.containsKey(order_no)) {
            logger.info(clazz_name+"支付回调订单号:{}重复调用",order_no);
            return ret_str_failed;
        }
        payMap.put(order_no, "1");
        try {
            logger.info(clazz_name+"执行回调业务开始=========================START===========================");
            // 保存文件记录
            NotifyUtils.savePayFile(clazz_name, infoMap, IPTools.getIp(request));
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
            rechargeOrderVO.setOrderAmount(amount);//实际支付金额，单位 分
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
            if (paymentName.equals("CSHL")) {
                CSHLPayServiceImpl cshlPayService = new CSHLPayServiceImpl(pmapsconfig);
                String rmsg = cshlPayService.callback(infoMap);
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
     *
     * @Description 资海支付回调通知
     * @param request
     * @param response
     * @param session
     * @return
     */
    @RequestMapping("/ZIHAINotify.do")
    @ResponseBody
    public String ZIHAINotify(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        String ret__success = "OK";// 成功返回SUCCESS
        String clazz_name = "ZIHAINotify";
        logger.info(clazz_name+"支付回调开始-----------------------------START------------------------------");
        Map<String,String> infoMap = ParamsUtils.getNotifyParams(request);
        if(infoMap == null || infoMap.isEmpty()){
            logger.info(clazz_name+"支付回调获取请求参数为空!");
            return ret_str_failed;
        }
        logger.info(clazz_name+"支付回调请求参数:{}",JSONObject.fromObject(infoMap).toString());
        //=================================获取回调基本参数结果--START===========================//
        String order_no = infoMap.get("orderid");// 平台订单号
        String trade_no = infoMap.get("transaction_id");// 第三方流水号
        String trade_status = infoMap.get("returncode");//订单状态，“00” 为成功
        String t_trade_status = "00";// 订单状态，“00” 为成功
        String order_amount = infoMap.get("amount"); //单位为元，小数两位
        if(StringUtils.isBlank(order_amount)){
            logger.info(clazz_name+"获取实际支付金额为空!");
            return ret_str_failed;
        }
        double amount = Double.parseDouble(order_amount);
        logger.info(clazz_name + "实际充值金额为：{}",amount);
        String ip = StringUtils.isBlank(IPTools.getIp(request))?"127.0.0.1":IPTools.getIp(request);
        //=================================获取回调基本参数结果--END===========================//
        if (payMap.containsKey(order_no)) {
            logger.info(clazz_name+"支付回调订单号:{}重复调用",order_no);
            return ret_str_failed;
        }
        payMap.put(order_no, "1");
        try {
            logger.info(clazz_name+"执行回调业务开始=========================START===========================");
            // 保存文件记录
            NotifyUtils.savePayFile(clazz_name, infoMap, IPTools.getIp(request));
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
            rechargeOrderVO.setOrderAmount(amount);//实际支付金额，单位元
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
            if (paymentName.equals("ZIHAI")) {
                ZIHAIPayServiceImpl zihaiPayService = new ZIHAIPayServiceImpl(pmapsconfig);
                String rmsg = zihaiPayService.callback(infoMap);
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
    *
    * @Description  XHZF新汇付1代回调通知
    * @param request
    * @param response
    * @param session
    * @return
    */
   /* @RequestMapping("/XHZFNotify.do")
    @ResponseBody
    public String XHZFNotify(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
       JSONObject successJson = new JSONObject();
       successJson.put("status", true);
       successJson.put("msg", "支付成功");
       JSONObject failJson = new JSONObject();
       failJson.put("status", false);
       failJson.put("msg", "错误描述！");
       String ret__success = successJson.toString();//收到通知后请回复  OK
       ret_str_failed = failJson.toString();
       String clazz_name = "XHZFNotify";
       logger.info(clazz_name+"支付回调开始-----------------------------START------------------------------");
       Map<String,String> infoMap = ParamsUtils.getNotifyParams(request);
       if(infoMap == null || infoMap.isEmpty()){
           logger.info(clazz_name+"支付回调获取请求参数为空!");
           return ret_str_failed;
       }
       logger.info(clazz_name+"支付回调请求参数:{}",JSONObject.fromObject(infoMap).toString());
       //=================================获取回调基本参数结果--START===========================//
       String order_amount = infoMap.get("total_amount");//实际支付金额
       if(StringUtils.isBlank(order_amount)){
           logger.info(clazz_name+"获取实际支付金额为空!");
           return ret_str_failed;
       }
       String order_no = infoMap.get("order_id");// 平台订单号
       String trade_no = infoMap.get("order_no");//交易流水号
       String trade_status = "00";//00代表支付成功
       String t_trade_status = "00";//00:成功
       String ip = StringUtils.isBlank(IPTools.getIp(request))?"127.0.0.1":IPTools.getIp(request);
       //=================================获取回调基本参数结果--END===========================//
       if (payMap.containsKey(order_no)) {
           logger.info(clazz_name+"支付回调订单号:{}重复调用",order_no);
           return ret_str_failed;
       }
       payMap.put(order_no, "1");
       try {
           logger.info(clazz_name+"执行回调业务开始=========================START===========================");
           // 保存文件记录
           // 保存文件记录
           NotifyUtils.savePayFile(clazz_name, infoMap, IPTools.getIp(request));
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
           rechargeOrderVO.setOrderAmount(Double.parseDouble(order_amount));
           rechargeOrderVO.setNotifyParams(JSONObject.fromObject(infoMap).toString());
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
           if (paymentName.equals(PayConstant.CONSTANT_XHZF)) {
        	   XHZFPayServiceImpl xxb = new XHZFPayServiceImpl(pmapsconfig);
               String rmsg = xxb.callback(infoMap);
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
    }*/
    /**
    *
    * @Description  XHEI回调通知
    * @param request
    * @param response
    * @param session
    * @return
    */
    @RequestMapping("/XHEINotify.do")
    @ResponseBody
    public String XHEINotify(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
       String ret__success = "SUCCESS";//收到通知后请回复  SUCCESS
       String clazz_name = "XHEINotify";
       logger.info(clazz_name+"支付回调开始-----------------------------START------------------------------");
       Map<String,String> infoMap = ParamsUtils.getNotifyParams(request);
       if(infoMap == null || infoMap.isEmpty()){
           logger.info(clazz_name+"支付回调获取请求参数为空!");
           return ret_str_failed;
       }
       logger.info(clazz_name+"支付回调请求参数:{}",JSONObject.fromObject(infoMap).toString());
       //=================================获取回调基本参数结果--START===========================//
       String order_amount = infoMap.get("amount");//实际支付金额
       if(StringUtils.isBlank(order_amount)){
           logger.info(clazz_name+"获取实际支付金额为空!");
           return ret_str_failed;
       }
       String order_no = infoMap.get("outOrderNo");// 平台订单号
       String trade_no = infoMap.get("orderNo");//交易流水号
       String trade_status = "success";//success代表支付成功
       String t_trade_status = "success";//success:成功
       String ip = StringUtils.isBlank(IPTools.getIp(request))?"127.0.0.1":IPTools.getIp(request);
       //=================================获取回调基本参数结果--END===========================//
       if (payMap.containsKey(order_no)) {
           logger.info(clazz_name+"支付回调订单号:{}重复调用",order_no);
           return ret_str_failed;
       }
       payMap.put(order_no, "1");
       try {
           logger.info(clazz_name+"执行回调业务开始=========================START===========================");
           // 保存文件记录
           // 保存文件记录
           NotifyUtils.savePayFile(clazz_name, infoMap, IPTools.getIp(request));
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
           rechargeOrderVO.setOrderAmount(Double.parseDouble(order_amount)/100);
           rechargeOrderVO.setNotifyParams(JSONObject.fromObject(infoMap).toString());
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
           if (paymentName.equals(PayConstant.CONSTANT_XHEI)) {
        	   XHEIPayServiceImpl xxb = new XHEIPayServiceImpl(pmapsconfig);
               String rmsg = xxb.callback(infoMap);
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
     * 
     * @Description 获取支付类型
     * @param payType
     * @return
     */
    private String getPayConfigType(String payType){
        if("1".equals(payType) || "21".equals(payType)){
            return "bank";
        }else if("2".equals(payType) || "22".equals(payType)){
            return "wx";
        }else if("3".equals(payType) || "23".equals(payType)){
            return "ali";
        }else if("4".equals(payType) || "24".equals(payType)){
            return "cft";
        }else if("5".equals(payType) || "25".equals(payType)){
            return "jd";
        }else if("6".equals(payType) || "26".equals(payType)){
            return "yl";
        }else if("7".equals(payType) || "27".equals(payType)){
            return "kj";
        }else if("8".equals(payType) || "28".equals(payType)){
            return "wxtm";
        }else if("9".equals(payType) || "29".equals(payType)){
            return "alitm";
        }
        return null;
    }

}

