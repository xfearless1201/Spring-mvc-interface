package com.cn.tianxia.pay.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayConstant;
import com.cn.tianxia.pay.ys.util.DateUtil;

/**
 * 
 * @ClassName GeneratorOrderUtils
 * @Description 生成订单工具类
 * @author Hardy
 * @Date 2018年11月2日 下午4:06:42
 * @version 1.0.0
 */
public class GeneratorOrderUtils {
    
    //日志
    private static final Logger logger = LoggerFactory.getLogger(GeneratorOrderUtils.class);
    
    /**
     * 
     * @Description 获取订单号
     * @return
     */
    public static String getOrderNo(String paymentName,String cagenCode,String sellerId){
        logger.info("---------------生成支付订单号 开始------------------------");
        //订单号组成部分:平台标识(3位)+支付标识(3位)+时间戳+随机数(8)
        // 14位 当前时间 yyyyMMddHHmmss
        String currTime = DateUtil.getCurrentDate("yyyyMMddHHmmss");
        // 8位日期
        String strTime = currTime.substring(8, currTime.length());
        // 四位随机数
        String strRandom = DateUtil.getRandom(4) + "";
        // 10位序列号,可以自行调整。
        String strReq = strTime + strRandom;
        // 订单号，此处用时间加随机数生成，商户根据自己情况调整，只要保持全局唯一就行
        String order_no = paymentName + cagenCode + currTime + strReq;
        logger.info("---------------生成支付订单号 结束------------------------");
        if(PayConstant.CONSTANT_WT.equals(paymentName) || PayConstant.CONSTANT_DAF.equals(paymentName) 
                || PayConstant.CONSTANT_NWT.equals(paymentName) || PayConstant.CONSTANT_NOMQ.equalsIgnoreCase(paymentName)
                || PayConstant.CONSTANT_BJYX.equals(paymentName) || PayConstant.CONSTANT_ABH.equals(paymentName) 
                || PayConstant.CONSTANT_YIFA.equals(paymentName) || PayConstant.CONSTANT_YBT.equals(paymentName)
                || PayConstant.CONSTANT_SYB.equals(paymentName) || PayConstant.CONSTANT_QIANYING.equals(paymentName)
                || PayConstant.CONSTANT_XFB.equals(paymentName)){
            
            //长度为20位的订单号
            order_no.subSequence(0, 21);
        }else if(PayConstant.CONSTANT_SXY.equals(paymentName)){
            //首信易支付订单,该参数格式为：订单生成日期-商户编号-商户流水号。例如：
            //20170101-888-12345。商户流水号
            //为数字，每日内不可重复，并且不能包括除数字、英文字母和“-”外以其它字符。流水号可为一组也可以用“-”间
            //隔成几组。
            currTime = new SimpleDateFormat("yyyyMMdd").format(new Date());
            strRandom = RandomUtils.generateNumberStr(5);
            order_no = currTime+"-"+sellerId+"-"+strRandom;
        }
        return order_no;
    }

}
