package com.cn.tianxia.controller.v2;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cn.tianxia.controller.BaseController;
import com.cn.tianxia.po.OfflineScanReponse;
import com.cn.tianxia.service.v2.OfflineScanPayService;
import com.cn.tianxia.vo.OfflineScanQrCodeVO;

/**
 * 
 * @ClassName OfflineScanController
 * @Description 线下扫码接口
 * @author Hardy
 * @Date 2019年2月4日 下午2:59:07
 * @version 1.0.0
 */
@Controller
@RequestMapping("/alipayPaymentScanCode")
public class OfflineScanController extends BaseController{

    @Autowired
    private OfflineScanPayService offlineScanPayService;
    
    /**
     * 获取二维码及限额
     *
     * @param page
     * @param rows
     * @param sort
     * @param order
     * @return
     */
    @RequestMapping("/getQRCode")
    @ResponseBody
    public Object getQRCode(HttpServletRequest request, HttpSession session, String type) {
        logger.info("调用获取平台线下二维码图片开始=================START==================");
        try {
            //获取用户ID
            Object obj = session.getAttribute("uid");
            if(!ObjectUtils.allNotNull(obj)){
                logger.info("线下二维码扫码支付创建订单,获取用户ID:{}",obj);
                return OfflineScanReponse.error("登录超时,请求重新登录");
            }
            String uid = String.valueOf(obj);
            
            return offlineScanPayService.getOfflineScanQrCode(uid, type);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("调用获取平台线下二维码图片异常:{}",e.getMessage());
            return OfflineScanReponse.error("调用获取平台线下二维码图片异常");
        }
    }

    @RequestMapping("/getOrder")
    @ResponseBody
    public Object addOrder(HttpServletRequest request, HttpSession session) {
        logger.info("线下二维码扫码支付创建订单开始=================START==================");
        //获取用户ID
        Object obj = session.getAttribute("uid");
        if(!ObjectUtils.allNotNull(obj)){
            logger.info("线下二维码扫码支付创建订单,获取用户ID:{}",obj);
            return OfflineScanReponse.error("登录超时,请求重新登录");
        }
        String uid = String.valueOf(obj);
        logger.info("线下二维码扫码支付创建订单,获取用户ID:{}",uid);
        //创建幂等key
        String key = "scan:pay:"+uid;
        try {
            if(scanPayMap.contains(key)){
                return OfflineScanReponse.error("订单正在处理,请勿重复操作.....");
            }
            scanPayMap.put(key, uid);
            String id = request.getParameter("id");//二维码图片ID
            String orderNum = request.getParameter("orderNum");//订单号
            String amount = request.getParameter("amount");//订单金额
            String type = request.getParameter("type");//支付类型
            if(StringUtils.isBlank(id)){
                return OfflineScanReponse.error("请求参数异常:二维码图片ID不能为空");
            }
            if(StringUtils.isBlank(orderNum)){
                return OfflineScanReponse.error("请求参数异常:订单号不能为空");
            }
            if(StringUtils.isBlank(amount)){
                return OfflineScanReponse.error("请求参数异常:订单金额不能为空");
            }
            if(StringUtils.isBlank(type)){
                return OfflineScanReponse.error("请求参数异常:扫码二维码类型不能为空");
            }
            OfflineScanQrCodeVO offlineScanQrCodeVO = new OfflineScanQrCodeVO();
            offlineScanQrCodeVO.setId(id);
            offlineScanQrCodeVO.setAmount(amount);
            offlineScanQrCodeVO.setOrderNum(orderNum);
            offlineScanQrCodeVO.setType(type);
            offlineScanQrCodeVO.setUid(uid);
            return offlineScanPayService.addOfflineQrCodeOrderRecord(offlineScanQrCodeVO);
        } catch (Exception e) {
            logger.info("线下二维码扫码支付创建订单异常:{}",e.getMessage());
            return OfflineScanReponse.error("线下二维码扫码支付创建订单异常");
        }finally {
            if(scanPayMap.containsKey(key)){
                scanPayMap.remove(key);
            }
        }
    }
}
