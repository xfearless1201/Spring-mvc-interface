package com.cn.tianxia.pay.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.pay.xq.util.CharsetTypeEnum;
import com.cn.tianxia.pay.xq.util.ClientSignature;
import com.cn.tianxia.pay.xq.util.StringUtils;
import com.cn.tianxia.pay.xq.util.XQHttpUtil;

import net.sf.json.JSONObject;

public class XQDFPayServiceImpl {

    private final static Logger logger = LoggerFactory.getLogger(XQDFPayServiceImpl.class);

    private static String key;// md5key

    private static String partnerID;// 商户ID
    // 2017年12月14日 16:06:32
    private static String dfUrl;

    public XQDFPayServiceImpl(Map<String, String> pmap) {
        JSONObject jo = new JSONObject().fromObject(pmap);
        if (null != pmap) {
            key = jo.get("key").toString();// md5key
            partnerID = jo.getString("partnerID").toString();// 商户ID
            if (jo.containsKey("dfUrl")) {
                dfUrl = jo.getString("dfUrl").toString();
            } else {
                dfUrl = "http://test.xqiangpay.net/website/api/pay2bank.htm";
            }
        }
    }

    public static void main(String[] args) {
        Map<String, String> param = new LinkedHashMap<>();
        String version = "1.0";
        String failureTime = "";
        // 获取订单详情
        String signType = "2";
        String key = "30820122300d06092a864886f70d01010105000382010f003082010a02820101008a71130ab8878ecc242b1d58c3831bc226e5b04277eb3b1f7b9670c2726f4ccdac4275c3130f79956e6cc0e3d73f0508a2086d69949f205f883ca6a1694f9d9b2e2a2423e765aa628c5da05a5f67965d1beec604fbfc3ac72544c8f951a475a3eb97bd440173f8d4cebf2534fe30b32511e74aa44b4c739ae626b945f12f9e2925b842f7dcab08c8e920f7c66e5a5fe9d6ed8383111b9aa6aafc5754c3d0dd8b48e0ac0003e5762082e2d1ebb5009ee9de38621c99c99592a0946c95801bba2eb67ea9a9fb85bcf356a2f4d41f38692cc748cdcf2831bfcf9c228fc35fd4537be3017d49979baac814bf06667e3796ec821f99abb8adccf18106f17659d99c970203010001";
        String url = "https://www.xqiangpay.net/website/pay.htm";
        String partnerID = "10006526245";
        String noticeUrl = "http://119.23.200.153/Demo/xQingPayNotifyAction";

        param.put("version", version);
        param.put("failureTime", failureTime);
        param.put("signType", signType);
        param.put("key", key);
        param.put("url", url);
        param.put("partnerID", partnerID);// 商户ID
        param.put("noticeUrl", noticeUrl);// 回调通知地址
        // logger.info("json配置:" + JSONObject.fromObject(param).toString());
        XQDFPayServiceImpl xq = new XQDFPayServiceImpl(param);
        String tradeType = "0";// 个人账户 "1"企业账户
        String payeeName = "刘建华";
        String payeeBankName = "中国银行";
        String payeeBankCode = "10003001";
        String payeeBankProvinceName = "辽宁省";
        String payeeBankCityName = "朝阳市";
        String payeeOpeningBankName = "朝阳凌源南街支行";
        String payeeBankAcctCode = "6216600500003531885";
        String requestAmount = "1";
        String payerMemberCode = "10006526245";
        String notifyUrl = "www.baidu.com";
        // String signType="2";
        Map<String, String> dfMap = new HashMap<>();
        dfMap.put("tradeType", tradeType);
        dfMap.put("payeeName", payeeName);
        dfMap.put("payeeBankName", payeeBankName);
        dfMap.put("payeeBankCode", payeeBankCode);
        dfMap.put("payeeBankProvinceName", payeeBankProvinceName);
        dfMap.put("payeeBankCityName", payeeBankCityName);
        dfMap.put("payeeOpeningBankName", payeeOpeningBankName);
        dfMap.put("payeeBankAcctCode", payeeBankAcctCode);
        dfMap.put("requestAmount", requestAmount);
        dfMap.put("payerMemberCode", payerMemberCode);
        dfMap.put("notifyUrl", notifyUrl);
        dfMap.put("signType", signType);
        try {
            System.out.println(xq.daifu(dfMap));
            // System.out.println(xq.daifuQuery(null));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 代付接口
     * 
     * @param req
     * @return
     * @throws Exception
     */
    public String daifu(Map<String, String> req) throws Exception {
        // 构建请求参数
        Map<String, String> reqMap = new LinkedHashMap<>();
        reqMap.put("method", "apiPay");
        reqMap.put("tradeType", req.get("tradeType"));
        reqMap.put("payeeName", req.get("payeeName"));
        reqMap.put("payeeBankName", req.get("payeeBankName"));
        reqMap.put("payeeBankCode", req.get("payeeBankCode"));
        reqMap.put("payeeBankProvinceName", req.get("payeeBankProvinceName"));
        reqMap.put("payeeBankCityName", req.get("payeeBankCityName"));
        reqMap.put("payeeOpeningBankName", req.get("payeeOpeningBankName"));
        reqMap.put("payeeBankAcctCode", req.get("payeeBankAcctCode"));
        reqMap.put("requestAmount", req.get("requestAmount"));
        reqMap.put("payerMemberCode", req.get("payerMemberCode"));
        reqMap.put("notifyUrl", req.get("notifyUrl"));
        reqMap.put("signType", "2");//
        // MD5签名
        String signStr = "payeeName=" + reqMap.get("payeeName") + "&payeeBankName=" + reqMap.get("payeeBankName")
                + "&payeeBankAcctCode=" + reqMap.get("payeeBankAcctCode") + "&requestAmount="
                + reqMap.get("requestAmount") + "&payerMemberCode=" + reqMap.get("payerMemberCode") + "&notifyUrl="
                + reqMap.get("notifyUrl");
        String signMsg = ClientSignature.genSignByMD5(signStr, CharsetTypeEnum.UTF8, key);
        reqMap.put("signMsg", signMsg);
        // 组装请求参数,name1=value1&name2=value2 的形式
        String reqStr = StringUtils.createRetStr(reqMap);
        System.out.println("代付请求内容：" + reqStr);
        // 请求
        String result = XQHttpUtil.sendPost(dfUrl, reqStr, "utf-8");
        return result;
    }

    /**
     * 代付查询
     * 
     * @return
     * @throws Exception
     */
    public String daifuQuery(Map<String, String> map) throws Exception {
        // 构建请求参数
        Map<String, String> reqMap = new LinkedHashMap<>();
        reqMap.put("method", "apiPayQuery");
        reqMap.put("orderId", "2001712141804394773");
        reqMap.put("memberCode", partnerID);
        // MD5签名
        String signStr = "method=" + reqMap.get("method") + "&memberCode=" + reqMap.get("memberCode") + "&orderId="
                + reqMap.get("orderId");
        String signMsg = ClientSignature.genSignByMD5(signStr, CharsetTypeEnum.UTF8, key);
        reqMap.put("signMsg", signMsg);
        // 组装请求参数,name1=value1&name2=value2 的形式
        String reqStr = StringUtils.createRetStr(reqMap);
        System.out.println("代付请求内容：" + reqStr);
        // 请求
        String result = XQHttpUtil.sendPost(dfUrl, reqStr, "utf-8");
        return result;
    }

    public String dfNotify(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // resp.setContentType("text/html;charset=utf-8");
        // req.setCharacterEncoding("utf-8");
        System.out.println("=========进入小强代付异步回调方法==========");
        String result = StringUtils.parseRequst(req);
        // result =
        // "orderID=2001709291038393256&stateCode=111&orderAmount=147540&partnerID=10000009833&msg=&signMsg=8d357717222b4b94a94c2517f7a00f63";
        System.out.println("回调内容：" + result);
        Map<String, String> resultMap = StringUtils.StrToMap(result);
        String stateCode = resultMap.get("stateCode");
        if (!StringUtils.isEmpty(stateCode) && "111".equals(stateCode)) {
            // 验签
            String signMsg = resultMap.remove("signMsg");
            String signStr = StringUtils.createRetStr(resultMap);
            try {
                String newsignMsg = ClientSignature.genSignByMD5(signStr, CharsetTypeEnum.UTF8, key);
                if (signMsg.equals(newsignMsg)) {
                    result = "success";
                } else {
                    result = "sign fail";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            result = "fail";
        }
        return result;
    }

}
