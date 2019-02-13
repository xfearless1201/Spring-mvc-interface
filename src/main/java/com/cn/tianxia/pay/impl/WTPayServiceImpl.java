package com.cn.tianxia.pay.impl;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.gst.util.MD5Encoder;
import com.cn.tianxia.pay.ly.util.HttpMethod;
import com.cn.tianxia.pay.ly.util.HttpSendModel;
import com.cn.tianxia.pay.ly.util.SimpleHttpResponse;
import com.cn.tianxia.pay.mkt.util.HttpUtil;
import com.cn.tianxia.pay.qyf.util.ToolKit;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.txkj.util.SignUtil;
import com.cn.tianxia.pay.wtzf.util.HttpUtils;

import net.sf.json.JSONObject;

/**
 * @ClassName: WTPayServiceImpl
 * @Description:万通支付
 * @author: Hardy
 * @date: 2018年7月26日 下午4:03:45
 * @Copyright: 天下科技
 */
public class WTPayServiceImpl implements PayService {

    private static final Logger log = LoggerFactory.getLogger(WTPayServiceImpl.class);

    // 商户id
    private String uid;

    // 商户secret
    private String secret;

    // 支付url
    private String payUrl;

    // 回调url
    private String notifyUrl;

    public WTPayServiceImpl(Map<String, String> pmap) {
        if (pmap != null) {
            if (pmap.containsKey("uid")) {
                uid = pmap.get("uid");
            }
            if (pmap.containsKey("secret")) {
                secret = pmap.get("secret");
            }
            if (pmap.containsKey("payUrl")) {
                payUrl = pmap.get("payUrl");
            }
            if (pmap.containsKey("notifyUrl")) {
                notifyUrl = pmap.get("notifyUrl");
            }
        }
    }

    /**
     * 网银支付
     */
    @Override
    public JSONObject wyPay(PayEntity payEntity) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * 扫码支付
     */
    @Override
    public JSONObject smPay(PayEntity payEntity) {
    	String userName = payEntity.getUsername();
        String payCode = payEntity.getPayCode();
        String mobile = payEntity.getMobile();
        String order_no = payEntity.getOrderNo();
        double amount = payEntity.getAmount();// "8.02";// 订单金额
        String price = new DecimalFormat("##").format(amount*100);
        // 组装支付参数
        Map<String, String> data = new HashMap<String, String>();
        data.put("uid", uid);
        data.put("price", price);
        data.put("paytype", payCode);
        data.put("notifyurl", notifyUrl);
        data.put("returnurl", payEntity.getRefererUrl());
        data.put("orderid", payEntity.getOrderNo());
        data.put("orderuid", ToolKit.randomStr(6));
        // 参数进行加密
        log.info("WT支付待签名参数:"+JSONObject.fromObject(data).toString());
        
        String key = null;//签名key
        
        try {
        	key = MD5Encoder.encode(FormatBizQueryParaMap(data) + secret).toLowerCase();
        	log.info("WT支付签名字符串:{"+key+"}");
		} catch (Exception e) {
			e.printStackTrace();
			log.info("WT支付签名异常:"+e.getMessage());
		}
        data.put("key", key);
        
        // 调用支付返回结果
        String result = null;
        try {
        	log.info("WT发起支付参数:{'payUrl':"+payUrl+",'data':"+data+"}");
        	result = HttpUtils.doPostJson(payUrl, data);
        	log.info("WT发起支付返回结果:"+result);
		} catch (Exception e) {
			e.printStackTrace();
			log.info("WT发起支付请求异常:"+e.getMessage());
		}
        
        if(StringUtils.isBlank(result)){
        	//支付失败
        	return PayUtil.returnPayJson("error", "4", result, userName, amount, order_no, "");
        }
        
        try {
            // 解析支付结果
            JSONObject response = JSONObject.fromObject(result);
            if ("1".equals(response.getString("code"))) {
                // 支付成功success
                JSONObject responseData = JSONObject.fromObject(response.get("data"));
                JSONObject responseResult = JSONObject.fromObject(responseData.get("result"));
                // 返回地址
                String resultUrl = "";
                String resultType = "2";
                // 判断来源
                if (StringUtils.isNoneBlank(mobile)) {
                    // 手机端解析url
                    resultUrl = responseResult.getString("url");
                    resultType = "4";
                } else {
                    // pc端
                    resultUrl = responseResult.getString("url");
                    // 微信渠道
                    if ("200".equals(payCode)) {
                        resultType = "4";
                    } else if ("100".equals(payCode)) {// 支付宝渠道
                        resultType = "2";
                    }
                }
                return PayUtil.returnPayJson("success", resultType, "支付接口请求成功!", payEntity.getUsername(),
                        payEntity.getAmount(), payEntity.getOrderNo(), resultUrl);
            } else {
                // 异常出去
                return PayUtil.returnPayJson("error", "4", result, userName, amount, order_no, "");
            }
        } catch (Exception e) {
            log.info("签名失败");
            e.printStackTrace();
            return PayUtil.returnPayJson("error", "4", result, userName, amount, order_no, "");
        }
    }

    /**
     * @Title: callback @Description:回调验签 @param: @param map @param: @return @return: String @throws
     */
    @Override
    public String callback(Map<String, String> map) {
        // 组装签名参数
        String key = map.get("key");
        map.remove("key");
        // 对签名参数进行排序
        String keyStr = SignUtil.sortData(map);
        // 进行MD5加密
        StringBuffer sb = new StringBuffer();
        sb.append(keyStr).append(secret);
        keyStr = sb.toString();
        log.info("签名原串:" + keyStr);
        String sig = MD5Encoder.encode(keyStr.toString()).toLowerCase();
        log.info("签名Key:" + sig);
        if (sig.equals(key)) {
            return "success";
        }
        return "";
    }

    /**
     * 参数排序
     * 
     * @param paraMap
     * @param urlencode
     * @return
     * @throws Exception
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    public String FormatBizQueryParaMap(Map<String, String> paraMap) throws Exception {
        String buff = "";
        try {
            List infoIds = new ArrayList(paraMap.entrySet());

            Collections.sort(infoIds, new Comparator<Map.Entry<String, String>>() {

                public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
                    return ((String) o1.getKey()).toString().compareTo((String) o2.getKey());
                }
            });
            for (int i = 0; i < infoIds.size(); i++) {
                Map.Entry item = (Map.Entry) infoIds.get(i);

                if (item.getKey() != "") {
                    String key = String.valueOf(item.getKey());
                    String val = String.valueOf(item.getValue());
                    buff = buff + key + "=" + val + "&";
                }
            }
            if (!buff.isEmpty())
                buff = buff.substring(0, buff.length() - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buff;
    }

    /**
     * HTTP post 请求
     * 
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
        log.info("【请求参数】：" + PostParms);
        HttpSendModel httpSendModel = new HttpSendModel(Url + "?" + PostParms);
        log.info("【后端请求】：" + Url + "?" + PostParms);
        httpSendModel.setMethod(HttpMethod.POST);
        SimpleHttpResponse response = null;
        try {
            response = HttpUtil.doRequest(httpSendModel, "UTF-8");
        } catch (Exception e) {
            return e.getMessage();
        }
        return response.getEntityString();
    }
}
