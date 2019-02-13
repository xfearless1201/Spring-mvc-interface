package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.utils.MapUtils;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName HFTXPayServiceImpl
 * @Description 汇付天下支付
 * @author Hardy
 * @Date 2019年1月25日 下午9:04:25
 * @version 1.0.0
 */
public class HFTXPayServiceImpl implements PayService {

    //日志
    private static final Logger logger = LoggerFactory.getLogger(HFTXPayServiceImpl.class);
    
    private String merchId;//商户号
    
    private String payUrl;//支付地址
    
    private String notifyUrl;//回调地址
    
    private String md5Key;//签名key

    //构造器,初始化参数
    public HFTXPayServiceImpl(Map<String,String> data) {
        if(MapUtils.isNotEntity(data)){
            if(data.containsKey("merchId")){
                this.merchId = data.get("merchId");
            }
            if(data.containsKey("payUrl")){
                this.payUrl = data.get("payUrl");
            }
            if(data.containsKey("notifyUrl")){
                this.notifyUrl = data.get("notifyUrl");
            }
            if(data.containsKey("md5Key")){
                this.md5Key = data.get("md5Key");
            }
        }
    }

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
     * @return
     * @throws Exception
     */
    private Map<String,String> sealRequest(PayEntity entity) throws Exception{
        logger.info("[HFTX]汇付天下组装支付请求参数开始=============START=============");
        try {
            Map<String,String> data = new HashMap<>();
            String amount = new DecimalFormat("0.00").format(entity.getAmount());
            data.put("pay_memberid",merchId);//商户ID是Max(5)10009
            data.put("pay_orderid",entity.getOrderNo());//商户订单号是Max(50)提交的订单号必须在自身账户交易中唯一必须是数字
            data.put("pay_applydate",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));//订单时间是Max(20)格式:2017-12-610:15:15
            data.put("pay_bankcode",entity.getPayCode());//通道编码是Max(10)请在商户后台API管理理查看编码
            data.put("pay_notifyurl",notifyUrl);//服务器通知地址是Max(255)服务器点对点通知回调地址
            data.put("pay_callbackurl",entity.getRefererUrl());//页面跳转地址是Max(255)支付成功之后跳转页面地址
            data.put("pay_amount",amount);//支付金额是Max(7)支付金额单位元
            data.put("pay_md5sign","");//签名字符串是Max(64)加密字符串
//            data.put("pay_attach","");//商户自定义信息否Max(20)原样返回
//            data.put("pay_productname","");//商品名称否Max(60)商品名称
//            data.put("pay_productid","");//商品id否Max(20)商品id
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[HFTX]汇付天下组装支付请求参数异常:{}",e.getMessage());
            throw new Exception("[HFTX]汇付天下组装支付请求参数异常");
        }
    }
    

}
