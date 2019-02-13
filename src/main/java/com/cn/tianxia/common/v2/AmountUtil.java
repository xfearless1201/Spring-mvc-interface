package com.cn.tianxia.common.v2;

import com.cn.tianxia.common.PayConstant;
import com.cn.tianxia.enums.v2.PayTypeEnum;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;

/**
 * @ClassName: AmountUtil
 * @Description: 金额工具类
 * @Author: Zed
 * @Date: 2019-01-07 21:26
 * @Version:1.0.0
 **/

public class AmountUtil {
    public static double definedAmount(double originalAmount,String cagent,String topay,String payType,String mobile){
        double finalAmount = originalAmount;
        //临时添加根据平台自定义订单金额,一定要注意
        if("TYC".equals(cagent) && PayConstant.CONSTANT_WK.equals(topay)){
            //太阳城平台的悟空支付
            if(PayTypeEnum.ali.getType().equals(payType)) {
                finalAmount = new BigDecimal(originalAmount).subtract(new BigDecimal(0.01)).doubleValue();
            }
        }
        ///乐百支付 支付金额 减一
        if(PayConstant.CONSTANT_LBZF.equals(topay)){
            if(originalAmount%100 == 0){
                finalAmount = new BigDecimal(originalAmount).subtract(new BigDecimal(1)).doubleValue();
            }
        }

        //阿里宝盒支付银联H5支付自动扣减一分钱
        if(PayConstant.CONSTANT_ABH.equals(topay)){
            if(PayTypeEnum.yl.getType().equals(payType) && StringUtils.isNotBlank(mobile)){
                finalAmount = new BigDecimal(originalAmount).subtract(new BigDecimal(0.01)).doubleValue();
            }
        }
        return finalAmount;
    }
}
