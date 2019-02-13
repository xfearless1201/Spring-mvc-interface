package com.cn.tianxia.controller.v2;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cn.tianxia.controller.BaseController;
import com.cn.tianxia.po.BaseResponse;
import com.cn.tianxia.service.v2.PlatPaymentService;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName PayConfigController
 * @Description 支付配置接口
 * @author Hardy
 * @Date 2019年2月11日 下午12:08:56
 * @version 1.0.0
 */
@RequestMapping("PlatformPay")
@Controller
public class PayConfigController extends BaseController{
    
    @Autowired
    private PlatPaymentService platPaymentService;
    
    /**
     * 
     * @Description 获取支付渠道
     * @param request
     * @param session
     * @param response
     * @return
     */
    @RequestMapping("paymentChannel")
    @ResponseBody
    public JSONObject getPaymentChannel(HttpServletRequest request, HttpSession session, HttpServletResponse response) {
        logger.info("调用获取支付渠道接口开始=================START=================");
        try {
            Object uid = session.getAttribute("uid");
            if (!ObjectUtils.allNotNull(uid)) {
                return BaseResponse.error("1001","用户未登陆！");
            }
            
            return platPaymentService.getPaymentChannel(uid.toString());
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("调用获取支付渠道接口异常:{}",e.getMessage());
            return BaseResponse.error("0", "调用获取支付渠道接口异常");
        }
    }
    
    /**
     * 
     * @Description 获取可用支付列表
     * @param request
     * @param session
     * @param response
     * @param type
     * @return
     */
    @RequestMapping("getPaymentList")
    @ResponseBody
    public JSONObject getPaymentList(HttpServletRequest request, HttpSession session, HttpServletResponse response,
            String type) {
        logger.info("调用获取可用支付列表接口开始==================START========================");
        try {
            Object uid = session.getAttribute("uid");
            if (!ObjectUtils.allNotNull(uid)) {
                return BaseResponse.error("1001","用户未登陆！");
            }
            return platPaymentService.getPaymentList(uid.toString(),type);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("调用获取可用支付列表接口异常:{}",e.getMessage());
            return BaseResponse.error("0", "调用获取可用支付列表接口异常");
        }
    }
}
