package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.json.JSONUtils;
import com.cn.tianxia.pay.jh.util.MerchantApiUtil;
import com.cn.tianxia.pay.ly.util.HttpMethod;
import com.cn.tianxia.pay.ly.util.HttpSendModel;
import com.cn.tianxia.pay.ly.util.SimpleHttpResponse;
import com.cn.tianxia.pay.mkt.util.HttpUtil;
import com.cn.tianxia.pay.qyf.util.ToolKit;
import com.cn.tianxia.pay.service.PayService;

import net.sf.json.JSONObject;

/**
 * @ClassName DFPayServiceImpl
 * @Description 豆付
 * @author zw
 * @Date 2018年8月5日 下午3:08:53
 * @version 1.0.0
 */
public class DFPayServiceImpl implements PayService {

    private String scanPayUrl;
    private String KEY;// md5密钥
    private String merchantCode;// 商户号
    private String model;// 模块名
    private String noticeUrl;

    private final static Logger logger = LoggerFactory.getLogger(DFPayServiceImpl.class);

    public DFPayServiceImpl(Map<String, String> pmap) {
        if (pmap != null) {
            if (pmap.containsKey("scanPayUrl")) {
                this.scanPayUrl = pmap.get("scanPayUrl");
            }
            if (pmap.containsKey("KEY")) {
                this.KEY = pmap.get("KEY");
            }
            if (pmap.containsKey("merchantCode")) {
                this.merchantCode = pmap.get("merchantCode");
            }
            if (pmap.containsKey("model")) {
                this.model = pmap.get("model");
            }
            if (pmap.containsKey("noticeUrl")) {
                this.noticeUrl = pmap.get("noticeUrl");
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
        String mobile = payEntity.getMobile();
        JSONObject r_json = null;
        r_json = scanPay(payEntity);

        if ("success".equals(r_json.getString("status"))) {
            if (StringUtils.isBlank(mobile)) {
                return PayUtil.returnPayJson("success", "2", "支付接口请求成功!", payEntity.getUsername(),
                        payEntity.getAmount(), payEntity.getOrderNo(), r_json.getString("qrCode"));
            } else {
                return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", payEntity.getUsername(),
                        payEntity.getAmount(), payEntity.getOrderNo(), r_json.getString("qrCode"));
            }
        } else {
            return PayUtil.returnPayJson("error", "2", r_json.getString("msg"), payEntity.getUsername(),
                    payEntity.getAmount(), payEntity.getOrderNo(), "");
        }
    }

    /**
     * @Description 豆付扫码接口
     * @param payEntity
     * @return
     */
    public JSONObject scanPay(PayEntity payEntity) {
        String model = this.model;
        String merchantCode = this.merchantCode;
        String outOrderId = payEntity.getOrderNo();// 商户订单号;
        // String deviceNo; //设备号
        double transMoney = payEntity.getAmount();
        DecimalFormat df = new DecimalFormat("#########");
        // 单位分 只能为正整数，最小为1
        String amount = df.format(transMoney * 100);
        // String goodsName;// 商品名称
        // String goodsExplain;// 商品描述
        // String ext;// 扩展字段
        String orderCreateTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String noticeUrl = this.noticeUrl;
        // String goodsMark;//商品标记
        String payChannel = payEntity.getPayCode();
        String ip = payEntity.getIp();
        Map<String, Object> map = new HashMap<>();

        map.put("merchantCode", merchantCode);
        map.put("outOrderId", outOrderId);
        map.put("amount", amount);
        map.put("orderCreateTime", orderCreateTime);
        map.put("noticeUrl", noticeUrl);
        map.put("payChannel", payChannel);
        String responseStr = null;
        try {
            String sign = getSgin(map);
            map.put("ip", ip);
            map.put("model", model);
            map.put("sign", sign);

            responseStr = RequestForm(scanPayUrl, map);
            logger.info("豆付响应:" + responseStr);

            JSONObject resposeJson = JSONObject.fromObject(responseStr);
            if (resposeJson.containsKey("code") && "00".equals(resposeJson.getString("code"))) {
                JSONObject dataJson = resposeJson.getJSONObject("data");
                if (dataJson.containsKey("url") && StringUtils.isNotBlank(dataJson.getString("url"))) {
                    String qrUrl = dataJson.getString("url");
                    return getReturnJson("success", qrUrl, "二维码连接获取成功！");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return getReturnJson("error", responseStr, "二维码连接获取失败！");
        }

        return getReturnJson("error", responseStr, "二维码连接获取失败！");
    }

    public String getSgin(Map<String, Object> map) {
        StringBuilder buf = new StringBuilder((map.size() + 1) * 10);
        buildPrePayParams(buf, map);
        // 添加key值
        buf.append("&KEY=" + KEY);
        logger.info("豆付待签名字符:" + buf.toString());
        return ToolKit.MD5(buf.toString(), "UTF-8").toUpperCase();
    }

    @Override
    public String callback(Map<String, String> infoMap) {
        String serviSign = infoMap.remove("sign");
        Map<String, Object> params = JSONUtils.toHashMap(infoMap);
        // 制作签名
        String localSign = getSgin(params);

        logger.info("本地签名:" + localSign + "      服务器签名:" + serviSign);
        if (serviSign.equalsIgnoreCase(localSign)) {
            return "success";
        }

        return "";
    }

    /**
     * HTTP post 请求
     * 
     * @param Url
     * @param Parms
     * @return
     */
    public String RequestForm(String Url, Map<String, Object> Parms) {
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
        logger.info("【豆付请求参数】：" + PostParms);
        HttpSendModel httpSendModel = new HttpSendModel(Url + "?" + PostParms);
        logger.info("【豆付后端请求】：" + Url + "?" + PostParms);
        httpSendModel.setMethod(HttpMethod.POST);
        SimpleHttpResponse response = null;
        try {
            response = HttpUtil.doRequest(httpSendModel, "UTF-8");
        } catch (Exception e) {
            return e.getMessage();
        }
        return response.getEntityString();
    }

    /**
     * @Description 参数签名排序
     * @param sb
     * @param payParams
     */
    public static void buildPrePayParams(StringBuilder sb, Map<String, Object> payParams) {
        List<String> keys = new ArrayList<String>(payParams.keySet());
        Collections.sort(keys);
        for (String key : keys) {
            String str = String.valueOf(payParams.get(key));
            if (str == null || str.length() == 0) {
                // 空串不参与sign计算
                continue;
            }
            sb.append(key).append("=");
            sb.append(str);
            sb.append("&");
        }
        sb.setLength(sb.length() - 1);
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

}
