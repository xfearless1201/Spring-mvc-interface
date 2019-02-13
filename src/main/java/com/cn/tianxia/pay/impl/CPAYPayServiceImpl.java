package com.cn.tianxia.pay.impl;


import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.ly.util.HttpMethod;
import com.cn.tianxia.pay.ly.util.HttpSendModel;
import com.cn.tianxia.pay.ly.util.SimpleHttpResponse;
import com.cn.tianxia.pay.mkt.util.HttpUtil;
import com.cn.tianxia.pay.qyf.util.ToolKit;
import com.cn.tianxia.pay.service.PayService;

import net.sf.json.JSONObject;

/**
 * @ClassName CPAYPayServiceImpl
 * @Description CPAY支付
 * @author zw
 * @Date 2018年6月14日 下午1:52:59
 * @version 1.0.0
 */
public class CPAYPayServiceImpl implements PayService {

    private String scan_url ;

    private String usercode ;

    private String md5_key ;

    private String productname;

    private String notifyurl;

    private final static Logger logger = LoggerFactory.getLogger(CPAYPayServiceImpl.class);

    public CPAYPayServiceImpl(Map<String, String> pmap) {
        net.sf.json.JSONObject jo = JSONObject.fromObject(pmap);
        if (null != pmap) {
            scan_url = jo.get("scan_url").toString();
            usercode = jo.get("usercode").toString();
            md5_key = jo.get("md5_key").toString();
            productname = jo.get("productname").toString();
            notifyurl = jo.get("notifyurl").toString();
        }
    }


    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JSONObject smPay(PayEntity payEntity) {
        Double amount = payEntity.getAmount();
        String order_no = payEntity.getOrderNo();
//        String refereUrl = payEntity.getRefererUrl();
        String pay_code = payEntity.getPayCode();
        String mobile = payEntity.getMobile();
        String userName = payEntity.getUsername();
        String ip = payEntity.getIp();

        Map<String, String> scanMap = new HashMap<>();
        
        double min = 0.01;//最小值  
        double max = 0.99;//总和  
        int scl =  2;//小数最大位数  
        int pow = (int) Math.pow(10, scl);//指定小数位  
        double one = Math.floor((Math.random() * (max - min) + min) * pow) / pow; 
        
        scanMap.put("customno", order_no);
        payEntity.setAmount(amount+one);
        scanMap.put("money", String.valueOf(amount+one));
        scanMap.put("scantype", pay_code);
        scanMap.put("buyerip", ip);

        JSONObject r_json = scanPay(scanMap);

        if ("success".equals(r_json.getString("status"))) {
            // pc端
            if (StringUtils.isBlank(mobile)) {
                return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, amount, order_no,
                        r_json.getString("qrCode"));
            } else {
                // 手机端
                return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, amount, order_no,
                        r_json.getString("qrCode"));
            }
        } else {
            return PayUtil.returnPayJson("error", "2", r_json.getString("msg"), userName, amount, order_no, r_json.getString("qrCode"));
        }
    }

    /**
     * @Description 扫码接口
     * @param scanMap
     * @return
     */
    public JSONObject scanPay(Map<String, String> scanMap) {

        Map<String, String> pramarMap = new HashMap<>();

        String customno = scanMap.get("customno");//

        String money = scanMap.get("money");//

        String scantype = scanMap.get("scantype");//

        String buyerip = scanMap.get("buyerip");// "110.164.197.124"; "112.97.59.221";

        String sendtime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

        String sign = "";

        pramarMap.put("usercode", usercode);

        pramarMap.put("customno", customno);

        pramarMap.put("productname", productname);

        pramarMap.put("money", money);

        pramarMap.put("scantype", scantype);

        pramarMap.put("sendtime", sendtime);

        pramarMap.put("notifyurl", notifyurl);

        pramarMap.put("buyerip", buyerip);

        String origin = usercode + "|" + customno + "|" + scantype + "|" + notifyurl + "|" + money + "|" + sendtime
                + "|" + buyerip + "|" + md5_key;

        logger.info("待签名字符原串:" + origin);

        sign = ToolKit.MD5(origin, "utf-8");

        pramarMap.put("sign", sign);
        String responseStr = "";
        try {
            responseStr = RequestForm(scan_url, pramarMap);
            logger.info("CPAY扫码响应:" + responseStr);

            JSONObject json = JSONObject.fromObject(responseStr);

            if (json.containsKey("success") && json.getBoolean("success")) {
                String url = JSONObject.fromObject(json.get("data")).getString("scanurl");
                return getReturnJson("success", url, "二维码连接获取成功！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return getReturnJson("error", responseStr, "二维码连接获取失败！");
        }

        return getReturnJson("error", responseStr, "二维码连接获取失败！");
    }

    /**
     * @Description RequestForm
     * @param Url
     * @param Parms
     * @return
     */
    public String RequestForm(String Url, Map<String, String> Parms) {
        if (Parms.isEmpty()) {
            return "参数不能为空！";
        }
        String PostParms = "";
        int PostItemTotal = Parms.keySet().size();
        int Itemp = 0;
        for (String key : Parms.keySet()) {
            PostParms += key + "=" + Parms.get(key);
            Itemp++;
            if (Itemp < PostItemTotal) {
                PostParms += "&";
            }
        }
        logger.info("【请求参数】：" + PostParms);
        HttpSendModel httpSendModel = new HttpSendModel(Url + "?" + PostParms);
        logger.info("【后端请求】：" + Url + "?" + PostParms);
        httpSendModel.setMethod(HttpMethod.POST);
        SimpleHttpResponse response = null;
        try {
            response = HttpUtil.doRequest(httpSendModel, "utf-8");
        } catch (Exception e) {
            return e.getMessage();
        }
        return response.getEntityString();
    }

    /**
     * 结果返回
     * 
     * @param status
     * @param qrCode
     * @param msg
     * @return
     */
    private JSONObject getReturnJson(String status, String qrCode, String msg) {
        JSONObject json = new JSONObject();
        json.put("status", status);
        json.put("qrCode", qrCode);
        json.put("msg", msg);
        return json;
    }

    /**
     * @Description 回调验签
     * @param map
     * @return
     */
    @Override
    public String callback(Map<String, String> map) {
        String serviSign = map.get("sign");

        String origin = usercode + "|" + map.get("orderno") + "|" + map.get("customno") + "|" + map.get("type") + "|"
                + map.get("bankcode") + "|" + map.get("tjmoney") + "|" + map.get("money") + "|" + map.get("status")
                + "|" + map.get("refundstatus") + "|" + map.get("currency") + "|" + md5_key;

        logger.info("待签名字符原串:" + origin);

        String localSign = ToolKit.MD5(origin, "utf-8");

        logger.info("本地签名:" + localSign + "      服务器签名:" + serviSign);

        if (serviSign.equalsIgnoreCase(localSign)) {
            logger.info("签名成功！");
            return "success";
        }
        logger.info("签名失败!");
        return "";
    }

}
