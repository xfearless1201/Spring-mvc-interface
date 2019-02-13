package com.cn.tianxia.pay.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.pay.gst.util.StringUtils;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.yj.util.PayUtil;
import com.cn.tianxia.pay.yj.util.XmlUtils;

import net.sf.json.JSONObject;

/**
 * @ClassName YJPayServiceImpl
 * @Description 银聚支付
 * @author zw
 * @Date 2018年5月12日 下午3:38:44
 * @version 1.0.0
 */
public class YJPayServiceImpl implements PayService {

    /** 版本号 **/
    private String version;

    private String spid;

    /** 银行卡类型 **/
    private String cardType;

    private String userType;

    private String notifyUrl;

    private String productName;

    private String productDesc;

    private String gatewayUrl;

    private String payKey;

    private final static Logger logger = LoggerFactory.getLogger(YJPayServiceImpl.class);

    public YJPayServiceImpl(Map<String, String> pmap) {
        JSONObject jo = JSONObject.fromObject(pmap);
        if (null != pmap) {
            version = jo.get("version").toString();
            spid = jo.get("spid").toString();
            cardType = jo.get("cardType").toString();
            userType = jo.get("userType").toString();
            notifyUrl = jo.get("notifyUrl").toString();
            productName = jo.get("productName").toString();
            productDesc = jo.get("productDesc").toString();
            gatewayUrl = jo.get("gatewayUrl").toString();
            payKey = jo.get("payKey").toString();
        }
    }

    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        Double amount = payEntity.getAmount();
        String order_no = payEntity.getOrderNo();
        String refereUrl = payEntity.getRefererUrl();
        String pay_code = payEntity.getPayCode();
        String userName = payEntity.getUsername();
        String pay_url = payEntity.getPayUrl();
        String mobile = payEntity.getMobile();
        String ip = payEntity.getIp();
        String topay = payEntity.getTopay();

        Map<String, String> params = new HashMap<String, String>();

        int int_amount = (int) (amount * 100);
        params.put("spbillno", order_no);
        params.put("tranAmt", String.valueOf(int_amount));
        params.put("bankSegment", pay_code);
        params.put("backUrl", refereUrl);

        // pc端
        if (StringUtils.isNullOrEmpty(mobile)) {
            params.put("channel", "1");
        } else {
            // 手机端
            params.put("channel", "2");
        }

        String html = bankPay(params);
        return com.cn.tianxia.common.PayUtil.returnWYPayJson("success", "form", html, pay_url, "");
    }

    @Override
    public JSONObject smPay(PayEntity payEntity) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @Description 网银支付接口
     * @param bankMap
     * @return
     */
    public String bankPay(Map<String, String> bankMap) {

        // String payKey = "19caf3599c854d7689a975d130573623";// 签名秘钥
        // String gatewayUrl = gatewayUrl;
        String bankSegment = bankMap.get("bankSegment");
        String spbillno = bankMap.get("spbillno");
        String tranAmt = bankMap.get("tranAmt");
        String channel = bankMap.get("channel");
        String backUrl = bankMap.get("backUrl");

        Map<String, String> map = new HashMap<>();
        map.put("version", version);
        map.put("spid", spid);
        map.put("spbillno", spbillno);
        map.put("tranAmt", tranAmt);
        map.put("cardType", cardType);
        map.put("channel", channel);
        map.put("backUrl", backUrl);
        map.put("notifyUrl", notifyUrl);
        map.put("userType", userType);
        map.put("bankSegment", bankSegment);
        map.put("productName", productName);
        map.put("productDesc", productDesc);

        map.put("sign", PayUtil.sign(payKey, map));

        String requestXML = XmlUtils.toXml(map, false);

        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("req_data", requestXML);

        String FormString = "<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form  id=\"actform\" name=\"actform\" method=\"post\" action=\""
                + gatewayUrl + "\">";
        for (String key : paramsMap.keySet()) {
            FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" + paramsMap.get(key) + "'>\r\n";
        }
        FormString += "</form></body>";

        logger.info("银聚支付网银表单:" + FormString);
        return FormString;
    }

    /**
     * @Description 回调验签
     * @param map
     * @return
     */
    @Override
    public String callback(Map<String, String> map) {
        String serverSign = map.remove("sign");

        String localSgin = PayUtil.sign(payKey, map);

        logger.info("银聚支付本地Sign:" + localSgin + "             " + "验证sign:" + serverSign);

        if (localSgin.equals(serverSign)) {
            logger.info("银聚支付验签成功!");
            return "success";
        }

        logger.info("银聚支付验签失败");
        return "";
    }
}
