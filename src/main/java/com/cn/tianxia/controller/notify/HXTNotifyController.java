package com.cn.tianxia.controller.notify;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cn.tianxia.controller.NotifyBaseController;
import com.cn.tianxia.pay.utils.ParamsUtils;
import com.cn.tianxia.util.IPTools;
import com.cn.tianxia.vo.ProcessNotifyVO;

import net.sf.json.JSONObject;

/**
 * @Auther: zed
 * @Date: 2019/1/17 17:50
 * @Description: 华夏通回调类
 */
@Controller
public class HXTNotifyController extends NotifyBaseController {

    @RequestMapping("/HXTNotify.do")
    @ResponseBody
    public String HXTNotify(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        String ret__success = "success";// 成功返回SUCCESS
        String clazz_name = "HXTNotify";
        logger.info(clazz_name+"支付回调开始-----------------------------START------------------------------");
        Map<String,String> infoMap = ParamsUtils.getNotifyParams(request);
        if(infoMap == null || infoMap.isEmpty()){
            logger.info(clazz_name+"支付回调获取请求参数为空!");
            return ret_str_failed;
        }
        logger.info(clazz_name+"支付回调请求参数:{}", JSONObject.fromObject(infoMap).toString());
        //=================================获取回调基本参数结果--START===========================//
        String order_no = infoMap.get("user_order_no");// 平台订单号
        String trade_no = infoMap.get("orderno");// 第三方订单号
        String trade_status = "success";//订单状态
        String t_trade_status = "success";// 表示成功状态
        String order_amount = infoMap.get("realprice");
        if(StringUtils.isBlank(order_amount)){
            logger.info(clazz_name+"获取实际支付金额为空!");
            return ret_str_failed;
        }
        double realAmount = Double.parseDouble(order_amount);
        String ip = StringUtils.isBlank(IPTools.getIp(request))?"127.0.0.1":IPTools.getIp(request);
        ProcessNotifyVO processNotifyVO = new ProcessNotifyVO();
        processNotifyVO.setClazz_name(clazz_name);
        processNotifyVO.setRet__success(ret__success);
        processNotifyVO.setIp(ip);
        processNotifyVO.setOrder_no(order_no);
        processNotifyVO.setTrade_no(trade_no);
        processNotifyVO.setTrade_status(trade_status);
        processNotifyVO.setT_trade_status(t_trade_status);
        processNotifyVO.setRealAmount(realAmount);
        processNotifyVO.setInfoMap(JSONObject.fromObject(infoMap).toString());
        processNotifyVO.setPayment("HXT");
        processNotifyVO.setService_type("");
        return processNotifyRequest(processNotifyVO);
    }
}
