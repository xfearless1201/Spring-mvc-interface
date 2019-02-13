package com.cn.tianxia.pay.tjf.utils;


import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import sun.net.www.protocol.http.HttpURLConnection;

import javax.net.ssl.*;
import java.io.ByteArrayOutputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Map;

public class Merchant {

    /**
     * 发送请求到支付网关并接受回复
     *
     * @param paramsStr
     *            请求参数字符串
     * @param serverUrl
     *            支付网关地址
     * @return
     * @throws Exception
     */
    public static String transact(String paramsStr, String serverUrl)
            throws Exception {

        if (StringUtils.isBlank(serverUrl) || StringUtils.isBlank(paramsStr)) {
            throw new NullPointerException("请求地址或请求数据为空!");
        }

        myX509TrustManager xtm = new myX509TrustManager();
        myHostnameVerifier hnv = new myHostnameVerifier();
        ByteArrayOutputStream respData = new ByteArrayOutputStream();

        byte[] b = new byte[8192];
        String result = "";
        try {
            SSLContext sslContext = null;
            try {
                sslContext = SSLContext.getInstance("TLS");
                X509TrustManager[] xtmArray = new X509TrustManager[] { xtm };
                sslContext.init(null, xtmArray,
                        new java.security.SecureRandom());
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            }

            if (sslContext != null) {
                HttpsURLConnection.setDefaultSSLSocketFactory(sslContext
                        .getSocketFactory());
            }
            HttpsURLConnection.setDefaultHostnameVerifier(hnv);

            // 匹配http或者https请求
            URLConnection conn = null;
            if (serverUrl.toLowerCase().startsWith("https")) {
                HttpsURLConnection httpsUrlConn = (HttpsURLConnection) (new URL(
                        serverUrl)).openConnection();
                httpsUrlConn.setRequestMethod("POST");
                conn = httpsUrlConn;
            } else {
                HttpURLConnection httpUrlConn = (HttpURLConnection) (new URL(
                        serverUrl)).openConnection();
                httpUrlConn.setRequestMethod("POST");
                conn = httpUrlConn;
            }

            conn.setConnectTimeout(5000);
            conn.setReadTimeout(30000);
            conn
                    .setRequestProperty(
                            "User-Agent",
                            "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.8.1.1) Gecko/20061204 Firefox/2.0.0.3");
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            conn.setRequestProperty("connection", "close");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.getOutputStream().write(paramsStr.getBytes("utf-8"));
            conn.getOutputStream().flush();

            int len = 0;
            try {
                while (true) {
                    len = conn.getInputStream().read(b);
                    if (len <= 0) {
                        conn.getInputStream().close();
                        break;
                    }
                    respData.write(b, 0, len);
                }
            } catch (SocketTimeoutException ee) {
                throw new RuntimeException("读取响应数据出错！" + ee.getMessage());
            }

            // 返回给商户的数据
            result = respData.toString("utf-8");
            System.out.println("==============================返回的数据"+result);
            if (StringUtils.isBlank(result)) {
                throw new RuntimeException("返回参数错误！");
            }
        } catch (Exception e) {
            throw new RuntimeException("发送POST请求出现异常！" + e.getMessage());
        }

        // 验签返回数据后返回支付平台回复数据
        checkResult(result);
        return result;
    }
    
    /**
     * 将快捷支付申请获取请求参数组成的map组织成字符串，并对参数做合法性验证
     *
     * @param paramsMap
     * @return
     * @throws Exception
     */
    public static String generateQuickPayApplyRequest(Map<String, String> paramsMap)
            throws Exception {
        if (!paramsMap.containsKey("service")
                || StringUtils.isBlank(paramsMap.get("service"))) {
            throw new Exception("service不能为空");
        }
        if (!paramsMap.containsKey("version")
                || StringUtils.isBlank(paramsMap.get("version"))) {
            throw new Exception("version不能为空");
        }
        if (!paramsMap.containsKey("merId")
                || StringUtils.isBlank(paramsMap.get("merId"))) {
            throw new Exception("merId不能为空");
        }
        if (!paramsMap.containsKey("tradeNo")
                || StringUtils.isBlank(paramsMap.get("tradeNo"))) {
            throw new Exception("tradeNo不能为空");
        }
        if (!paramsMap.containsKey("tradeDate")
                || StringUtils.isBlank(paramsMap.get("tradeDate"))) {
            throw new Exception("tradeDate不能为空");
        }
        if (!paramsMap.containsKey("amount")
                || StringUtils.isBlank(paramsMap.get("amount"))) {
            throw new Exception("amount不能为空");
        }
        if (!paramsMap.containsKey("cardType")
                || StringUtils.isBlank(paramsMap.get("cardType"))) {
            throw new Exception("cardType不能为空");
        }
        if (!paramsMap.containsKey("cardNo")
                || StringUtils.isBlank(paramsMap.get("cardNo"))) {
            throw new Exception("cardNo不能为空");
        }
        if (!paramsMap.containsKey("cardName")
                || StringUtils.isBlank(paramsMap.get("cardName"))) {
            throw new Exception("cardName不能为空");
        }
        if (!paramsMap.containsKey("idCardNo")
                || StringUtils.isBlank(paramsMap.get("idCardNo"))) {
            throw new Exception("idCardNo不能为空");
        }
        if (!paramsMap.containsKey("mobile")
                || StringUtils.isBlank(paramsMap.get("mobile"))) {
            throw new Exception("mobile不能为空");
        }

        String resultStr = String
                .format(
                        "service=%s&version=%s&merId=%s&tradeNo=%s&tradeDate=%s&amount=%s&notifyUrl=%s&extra=%s&summary=%s&expireTime=%s&clientIp=%s&cardType=%s&cardNo=%s&cardName=%s&idCardNo=%s&mobile=%s&cvn2=%s&validDate=%s",
                        paramsMap.get("service"), paramsMap.get("version"),
                        paramsMap.get("merId"), paramsMap.get("tradeNo"), paramsMap.get("tradeDate"),
                        paramsMap.get("amount"), paramsMap.get("notifyUrl"),
                        paramsMap.get("extra"), paramsMap.get("summary"),
                        paramsMap.get("expireTime"), paramsMap.get("clientIp"),
                        paramsMap.get("cardType"), paramsMap.get("cardNo"),
                        paramsMap.get("cardName"), paramsMap.get("idCardNo"),paramsMap.get("mobile"),
                        paramsMap.get("cvn2"), paramsMap.get("validDate")
                );

        return resultStr;
    }
    
    
    /**
     * 将快捷支付确认获取请求参数组成的map组织成字符串，并对参数做合法性验证
     *
     * @param paramsMap
     * @return
     * @throws Exception
     */
    public static String generateQuickPayConfirmRequest(Map<String, String> paramsMap)
            throws Exception {
        if (!paramsMap.containsKey("service")
                || StringUtils.isBlank(paramsMap.get("service"))) {
            throw new Exception("service不能为空");
        }
        if (!paramsMap.containsKey("version")
                || StringUtils.isBlank(paramsMap.get("version"))) {
            throw new Exception("version不能为空");
        }
        if (!paramsMap.containsKey("merId")
                || StringUtils.isBlank(paramsMap.get("merId"))) {
            throw new Exception("merId不能为空");
        }
        if (!paramsMap.containsKey("opeNo")
                || StringUtils.isBlank(paramsMap.get("opeNo"))) {
            throw new Exception("opeNo不能为空");
        }
        if (!paramsMap.containsKey("opeDate")
                || StringUtils.isBlank(paramsMap.get("opeDate"))) {
            throw new Exception("opeDate不能为空");
        }
        if (!paramsMap.containsKey("sessionID")
                || StringUtils.isBlank(paramsMap.get("sessionID"))) {
            throw new Exception("sessionID不能为空");
        }
        if (!paramsMap.containsKey("dymPwd")
                || StringUtils.isBlank(paramsMap.get("dymPwd"))) {
            throw new Exception("dymPwd不能为空");
        }

        String resultStr = String
                .format(
                        "service=%s&version=%s&merId=%s&opeNo=%s&opeDate=%s&sessionID=%s&dymPwd=%s",
                        paramsMap.get("service"), paramsMap.get("version"),
                        paramsMap.get("merId"), paramsMap.get("opeNo"), paramsMap.get("opeDate"),
                        paramsMap.get("sessionID"), paramsMap.get("dymPwd")
                );

        return resultStr;
    }
    
    
    /**
     * 如果数据被篡改 则抛出异常
     *
     * @param result
     * @throws Exception
     */
    private static void checkResult(String result) throws Exception {

        if (StringUtils.isBlank(result)) {
            throw new NullPointerException("返回数据为空!");
        }

        try {
            Document resultDOM = DocumentHelper.parseText(result);
            Element root = resultDOM.getRootElement();
            String responseData = root.element("detail").asXML();
            String signMsg = root.element("sign").getStringValue();

            if (StringUtils.isBlank(responseData)
                    || StringUtils.isBlank(signMsg)) {
                throw new Exception("解析返回验签或原数据错误！");
            }

            if (!SignUtil.verifyData(signMsg, responseData)) {
                throw new Exception("系统效验返回数据失败！");
            }

        } catch (DocumentException e) {
            throw new RuntimeException("xml解析错误：" + e);
        }
    }

    /**
     * 将由支付请求参数构成的map转换成支付串，并对参数做合法验证
     *
     * @param paramsMap
     *            由支付请求参数构成的map
     * @return
     * @throws Exception
     */
    public static String generatePayRequest(Map<String, String> paramsMap)
            throws Exception {

        // 验证输入数据合法性
        if (!paramsMap.containsKey("service")
                || StringUtils.isBlank(paramsMap.get("service"))) {
            throw new Exception("service不能为空");
        }
        if (!paramsMap.containsKey("version")
                || StringUtils.isBlank(paramsMap.get("version"))) {
            throw new Exception("version不能为空");
        }
        if (!paramsMap.containsKey("merId") || StringUtils.isBlank("merId")) {
            throw new Exception("merId不能为空");
        }
        if (!paramsMap.containsKey("tradeNo") || StringUtils.isBlank("tradeNo")) {
            throw new Exception("tradeNo不能为空");
        }
        if (!paramsMap.containsKey("tradeDate")
                || StringUtils.isBlank("tradeDate")) {
            throw new Exception("tradeDate不能为空");
        }
        if (!paramsMap.containsKey("amount") || StringUtils.isBlank("amount")) {
            throw new Exception("amount不能为空");
        }
        if (!paramsMap.containsKey("notifyUrl")
                || StringUtils.isBlank("notifyUrl")) {
            throw new Exception("notifyUrl不能为空");
        }
        if (!paramsMap.containsKey("extra")) {
            throw new Exception("extra可以为空，但必须存在！");
        }
        if (!paramsMap.containsKey("summary") || StringUtils.isBlank("summary")) {
            throw new Exception("summary不能为空");
        }

        // 输入数据组织成字符串
        String paramsStr = String
                .format(
                        "service=%s&version=%s&merId=%s&tradeNo=%s&tradeDate=%s&amount=%s&notifyUrl=%s&extra=%s&summary=%s&expireTime=%s&clientIp=%s&bankId=%s",
                        paramsMap.get("service"), paramsMap.get("version"),
                        paramsMap.get("merId"), paramsMap.get("tradeNo"),
                        paramsMap.get("tradeDate"), paramsMap.get("amount"),
                        paramsMap.get("notifyUrl"), paramsMap.get("extra"),
                        paramsMap.get("summary"),
                        paramsMap.get("expireTime") == null ? "" : paramsMap
                                .get("expireTime"),
                        paramsMap.get("clientIp") == null ? "" : paramsMap
                                .get("clientIp"),
                        paramsMap.get("bankId") == null ? "" : paramsMap
                                .get("bankId"));

        return paramsStr;
    }

    /**
     * 将由扫码支付获取请求参数组成的map组织成字符串，并对参数做合法性验证
     *
     * @param paramsMap
     * @return
     * @throws Exception
     */
    public static String generateAlspQueryRequest(Map<String, String> paramsMap)
            throws Exception {
        if (!paramsMap.containsKey("service")
                || StringUtils.isBlank(paramsMap.get("service"))) {
            throw new Exception("service不能为空");
        }
        if (!paramsMap.containsKey("version")
                || StringUtils.isBlank(paramsMap.get("version"))) {
            throw new Exception("version不能为空");
        }
        if (!paramsMap.containsKey("merId")
                || StringUtils.isBlank(paramsMap.get("merId"))) {
            throw new Exception("merId不能为空");
        }
        if (!paramsMap.containsKey("typeId")
                || StringUtils.isBlank(paramsMap.get("typeId"))) {
            throw new Exception("typeId不能为空");
        }
        if (!paramsMap.containsKey("tradeNo")
                || StringUtils.isBlank(paramsMap.get("tradeNo"))) {
            throw new Exception("tradeNo不能为空");
        }
        if (!paramsMap.containsKey("tradeDate")
                || StringUtils.isBlank(paramsMap.get("tradeDate"))) {
            throw new Exception("tradeDate不能为空");
        }
        if (!paramsMap.containsKey("amount")
                || StringUtils.isBlank(paramsMap.get("amount"))) {
            throw new Exception("amount不能为空");
        }

        String resultStr = String
                .format(
                        "service=%s&version=%s&merId=%s&typeId=%s&tradeNo=%s&tradeDate=%s&amount=%s&notifyUrl=%s&extra=%s&summary=%s&expireTime=%s&clientIp=%s",
                        paramsMap.get("service"), paramsMap.get("version"),
                        paramsMap.get("merId"), paramsMap.get("typeId"),
                        paramsMap.get("tradeNo"), paramsMap.get("tradeDate"),
                        paramsMap.get("amount"), paramsMap.get("notifyUrl"),
                        paramsMap.get("extra"), paramsMap.get("summary"),
                        paramsMap.get("expireTime"), paramsMap.get("clientIp"));

        return resultStr;
    }
    
    
    public static String generateAlspQueryRequestH5(Map<String, String> paramsMap)
            throws Exception {
        if (!paramsMap.containsKey("service")
                || StringUtils.isBlank(paramsMap.get("service"))) {
            throw new Exception("service不能为空");
        }
        if (!paramsMap.containsKey("version")
                || StringUtils.isBlank(paramsMap.get("version"))) {
            throw new Exception("version不能为空");
        }
        if (!paramsMap.containsKey("merId")
                || StringUtils.isBlank(paramsMap.get("merId"))) {
            throw new Exception("merId不能为空");
        }
        if (!paramsMap.containsKey("typeId")
                || StringUtils.isBlank(paramsMap.get("typeId"))) {
            throw new Exception("typeId不能为空");
        }
        if (!paramsMap.containsKey("tradeNo")
                || StringUtils.isBlank(paramsMap.get("tradeNo"))) {
            throw new Exception("tradeNo不能为空");
        }
        if (!paramsMap.containsKey("tradeDate")
                || StringUtils.isBlank(paramsMap.get("tradeDate"))) {
            throw new Exception("tradeDate不能为空");
        }
        if (!paramsMap.containsKey("amount")
                || StringUtils.isBlank(paramsMap.get("amount"))) {
            throw new Exception("amount不能为空");
        }

        String resultStr = String
                .format(
                        "service=%s&version=%s&merId=%s&typeId=%s&tradeNo=%s&tradeDate=%s&amount=%s&notifyUrl=%s&extra=%s&summary=%s&expireTime=%s&clientIp=%s",
                        paramsMap.get("service"), paramsMap.get("version"),
                        paramsMap.get("merId"), paramsMap.get("typeId"),
                        paramsMap.get("tradeNo"), paramsMap.get("tradeDate"),
                        paramsMap.get("amount"), paramsMap.get("notifyUrl"),
                        paramsMap.get("extra"), paramsMap.get("summary"),
                        paramsMap.get("expireTime"), paramsMap.get("clientIp"));

        return resultStr;
    }
    

    /**
     * 将由查询请求参数组成的map组织成字符串，并对参数做合法性验证
     *
     * @param paramsMap
     * @return
     * @throws Exception
     */
    public static String generateQueryRequest(Map<String, String> paramsMap)
            throws Exception {
        if (!paramsMap.containsKey("service")
                || StringUtils.isBlank(paramsMap.get("service"))) {
            throw new Exception("service不能为空");
        }
        if (!paramsMap.containsKey("version")
                || StringUtils.isBlank(paramsMap.get("version"))) {
            throw new Exception("version不能为空");
        }
        if (!paramsMap.containsKey("merId")
                || StringUtils.isBlank(paramsMap.get("merId"))) {
            throw new Exception("merId不能为空");
        }
        if (!paramsMap.containsKey("tradeNo")
                || StringUtils.isBlank(paramsMap.get("tradeNo"))) {
            throw new Exception("tradeNo不能为空");
        }
        if (!paramsMap.containsKey("tradeDate")
                || StringUtils.isBlank(paramsMap.get("tradeDate"))) {
            throw new Exception("tradeDate不能为空");
        }
        if (!paramsMap.containsKey("amount")
                || StringUtils.isBlank(paramsMap.get("amount"))) {
            throw new Exception("amount不能为空");
        }

        String resultStr = String
                .format(
                        "service=%s&version=%s&merId=%s&tradeNo=%s&tradeDate=%s&amount=%s",
                        paramsMap.get("service"), paramsMap.get("version"),
                        paramsMap.get("merId"), paramsMap.get("tradeNo"),
                        paramsMap.get("tradeDate"), paramsMap.get("amount"));

        return resultStr;
    }

    public static String generateRefundRequest(Map<String, String> paramsMap)
            throws Exception {
        if (!paramsMap.containsKey("service")
                || StringUtils.isBlank(paramsMap.get("service"))) {
            throw new Exception("service不能为空");
        }
        if (!paramsMap.containsKey("version")
                || StringUtils.isBlank(paramsMap.get("version"))) {
            throw new Exception("version不能为空");
        }
        if (!paramsMap.containsKey("merId")
                || StringUtils.isBlank(paramsMap.get("merId"))) {
            throw new Exception("merId不能为空");
        }
        if (!paramsMap.containsKey("tradeNo")
                || StringUtils.isBlank(paramsMap.get("tradeNo"))) {
            throw new Exception("tradeNo不能为空");
        }
        if (!paramsMap.containsKey("tradeDate")
                || StringUtils.isBlank(paramsMap.get("tradeDate"))) {
            throw new Exception("tradeDate不能为空");
        }
        if (!paramsMap.containsKey("amount")
                || StringUtils.isBlank(paramsMap.get("amount"))) {
            throw new Exception("amount不能为空");
        }
        if (!paramsMap.containsKey("summary")
                || StringUtils.isBlank(paramsMap.get("summary"))) {
            throw new Exception("summary不能为空");
        }

        String resultStr = String
                .format(
                        "service=%s&version=%s&merId=%s&tradeNo=%s&tradeDate=%s&amount=%s&summary=%s",
                        paramsMap.get("service"), paramsMap.get("version"),
                        paramsMap.get("merId"), paramsMap.get("tradeNo"),
                        paramsMap.get("tradeDate"), paramsMap.get("amount"),
                        paramsMap.get("summary"));
        return resultStr;
    }
     
    //余额查询
    public static String generateRefundRequestYue(Map<String, String> paramsMap)
            throws Exception {
        if (!paramsMap.containsKey("service")
                || StringUtils.isBlank(paramsMap.get("service"))) {
            throw new Exception("service不能为空");
        }
        if (!paramsMap.containsKey("version")
                || StringUtils.isBlank(paramsMap.get("version"))) {
            throw new Exception("version不能为空");
        }
        if (!paramsMap.containsKey("merId")
                || StringUtils.isBlank(paramsMap.get("merId"))) {
            throw new Exception("merId不能为空");
        }
        

        String resultStr = String
                .format(
                        "service=%s&version=%s&merId=%s",
                        paramsMap.get("service"), paramsMap.get("version"),
                        paramsMap.get("merId"));
        return resultStr;
    }
    
    
    
    
    
    public static String generateSingleSettRequest(Map<String, String> paramsMap)
            throws Exception {
        if (!paramsMap.containsKey("service")
                || StringUtils.isBlank(paramsMap.get("service"))) {
            throw new Exception("service不能为空");
        }
        if (!paramsMap.containsKey("version")
                || StringUtils.isBlank(paramsMap.get("version"))) {
            throw new Exception("version不能为空");
        }
        if (!paramsMap.containsKey("merId")
                || StringUtils.isBlank(paramsMap.get("merId"))) {
            throw new Exception("merId不能为空");
        }
        if (!paramsMap.containsKey("tradeNo")
                || StringUtils.isBlank(paramsMap.get("tradeNo"))) {
            throw new Exception("tradeNo不能为空");
        }
        if (!paramsMap.containsKey("tradeDate")
                || StringUtils.isBlank(paramsMap.get("tradeDate"))) {
            throw new Exception("tradeDate不能为空");
        }
        if (!paramsMap.containsKey("amount")
                || StringUtils.isBlank(paramsMap.get("amount"))) {
            throw new Exception("amount不能为空");
        }

        String resultStr = String
                .format(
                        "service=%s&version=%s&merId=%s&tradeNo=%s&tradeDate=%s&amount=%s"
                                + "&notifyUrl=%s&extra=%s&summary=%s&bankCardNo=%s&bankCardName=%s"
                                + "&bankId=%s&bankName=%s&purpose=%s",
                        paramsMap.get("service"), paramsMap.get("version"),
                        paramsMap.get("merId"), paramsMap.get("tradeNo"),
                        paramsMap.get("tradeDate"),paramsMap.get("amount"), paramsMap.get("notifyUrl"),
                        paramsMap.get("extra"),paramsMap.get("summary"), paramsMap.get("bankCardNo"),
                        paramsMap.get("bankCardName"), paramsMap.get("bankId"),
                        paramsMap.get("bankName"), paramsMap.get("purpose"));
        return resultStr;
    }

    public static String generateSingleSettQueryRequest(
            Map<String, String> paramsMap) throws Exception {

        if (!paramsMap.containsKey("service")
                || StringUtils.isBlank(paramsMap.get("service"))) {
            throw new Exception("service不能为空");
        }
        if (!paramsMap.containsKey("version")
                || StringUtils.isBlank(paramsMap.get("version"))) {
            throw new Exception("version不能为空");
        }
        if (!paramsMap.containsKey("merId")
                || StringUtils.isBlank(paramsMap.get("merId"))) {
            throw new Exception("merId不能为空");
        }
        if (!paramsMap.containsKey("tradeNo")
                || StringUtils.isBlank(paramsMap.get("tradeNo"))) {
            throw new Exception("tradeNo不能为空");
        }
        if (!paramsMap.containsKey("tradeDate")
                || StringUtils.isBlank(paramsMap.get("tradeDate"))) {
            throw new Exception("tradeDate不能为空");
        }

        String resultStr = String.format(
                "service=%s&version=%s&merId=%s&tradeNo=%s&tradeDate=%s",
                paramsMap.get("service"), paramsMap.get("version"), paramsMap
                        .get("merId"), paramsMap.get("tradeNo"), paramsMap
                        .get("tradeDate"));
        return resultStr;
    }

}

class myX509TrustManager implements X509TrustManager {

    public void checkClientTrusted(X509Certificate[] chain, String authType) {
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType) {
    }

    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }
}

class myHostnameVerifier implements HostnameVerifier {

    public boolean verify(String hostname, SSLSession session) {
        return true;
    }

}
