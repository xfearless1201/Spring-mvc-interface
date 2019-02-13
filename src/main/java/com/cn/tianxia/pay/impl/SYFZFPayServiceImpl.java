package com.cn.tianxia.pay.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.pay.syf.util.ToolKit;
import com.sun.org.apache.xml.internal.security.utils.Base64;

import net.sf.json.JSONObject;


/**
 * 闪亿付支付
 * 
 * @author
 * 
 */
public class SYFZFPayServiceImpl implements PayService{
	private final static Logger logger = LoggerFactory.getLogger(SYFZFPayServiceImpl.class);
	private String merNo ;// 商户号
	private String key ;// 签名MD5密钥,24位
	private String callBackViewUrl;
	private String goodsName;
	 /** 通知地址 */
    private String callBackUrl;

	public static void main(String[] args) throws Exception {
        // 1,初始化支持平台配置
        Map pmap = new HashMap<String, Object>();
        // returnMap是从数据查询来的，怎么配置数据库？
        pmap.put("payUrl", "http://zfb.637pay.com/api/pay");
        pmap.put("merNo", "YSF201803080000");
        pmap.put("key", "161658270F6D258D64026E25EE78A0CB");
        pmap.put("callBackUrl", "www.baidu.com");
        pmap.put("callBackViewUrl", "www.163.com");

        System.out.println("JSON配置:" + JSONObject.fromObject(pmap));
        SYFZFPayServiceImpl ll = new SYFZFPayServiceImpl(pmap);

        // 2,填充实体
        PayEntity payEntity = new PayEntity();
        // 此参数用于区别手机h5 和pc
		payEntity.setPayCode("ZFB");
		payEntity.setAmount(50);
		payEntity.setUsername("");
		payEntity.setMobile("");
        // 3,调用支付接口
        ll.smPay(payEntity);
    }

	public SYFZFPayServiceImpl(Map<String, String> pmap) {
		if (pmap != null) {
			if (pmap.containsKey("key")) {
				key = pmap.get("key");// md5key
			}
			if (pmap.containsKey("merNo")) {
				merNo = pmap.get("merNo");
			}
			if (pmap.containsKey("callBackUrl")) {
				callBackUrl = pmap.get("callBackUrl"); // 异步回调地址
			}
			if (pmap.containsKey("callBackViewUrl")) {
				callBackViewUrl = pmap.get("callBackViewUrl"); // 异步回调地址
			}
			if (pmap.containsKey("goodsName")) {
				goodsName = pmap.get("goodsName").toString();
			}
		}
	}
	
	/**
	 * 支付查询
	 * @throws IOException 
	 */
	public void payQuery() throws IOException {
		Map<String, String> metaSignMap = new TreeMap<String, String>();
		metaSignMap.put("orderNum", "20170803220058130o8i");
		metaSignMap.put("payDate", "2017-08-03");
		metaSignMap.put("merNo", this.merNo);
		metaSignMap.put("netway", "WX_WAP");// WX:微信支付,ZFB:支付宝支付
		metaSignMap.put("amount", "500");// 单位:分
		metaSignMap.put("goodsName", "笔");// 商品名称：20位

		String metaSignJsonStr = ToolKit.mapToJson(metaSignMap);
		String sign = ToolKit.MD5(metaSignJsonStr + key, ToolKit.CHARSET);// 32位
		System.out.println("sign=" + sign); // 英文字母大写
		metaSignMap.put("sign", sign);

		byte[] dataStr = ToolKit.encryptByPublicKey(ToolKit.mapToJson(metaSignMap).getBytes(ToolKit.CHARSET),
				ToolKit.PAY_PUBLIC_KEY);
		String param = Base64.encode(dataStr);
		String reqParam = "data=" + URLEncoder.encode(param, ToolKit.CHARSET) + "&merchNo=" + metaSignMap.get("merNo")
				+ "&version=V3.1.0.0";
		String resultJsonStr = ToolKit.request("http://zfb.637pay.com/api/pay", reqParam);
		// 检查状态
		JSONObject resultJsonObj = JSONObject.fromObject(resultJsonStr);
		String stateCode = resultJsonObj.getString("stateCode");
		if (!stateCode.equals("00")) {
			return;
		}
		String resultSign = resultJsonObj.getString("sign");
		resultJsonObj.remove("sign");
		String targetString = ToolKit.MD5(resultJsonObj.toString() + key, ToolKit.CHARSET);
		if (targetString.equals(resultSign)) {
			System.out.println("签名校验成功");
		}
	}

	/**
	 * 支付结果处理
	 * 
	 * @throws Throwable
	 */
	public void result(HttpServletRequest request, HttpServletResponse response) throws Throwable {
		String data = request.getParameter("data");
		byte[] result = ToolKit.decryptByPrivateKey(Base64.decode(data), ToolKit.PRIVATE_KEY);
		String resultData = new String(result, ToolKit.CHARSET);// 解密数据

		JSONObject jsonObj = JSONObject.fromObject(resultData);
		Map<String, String> metaSignMap = new TreeMap<String, String>();
		metaSignMap.put("merNo", jsonObj.getString("merNo"));
		metaSignMap.put("netway", jsonObj.getString("netway"));
		metaSignMap.put("orderNum", jsonObj.getString("orderNum"));
		metaSignMap.put("amount", jsonObj.getString("amount"));
		metaSignMap.put("goodsName", jsonObj.getString("goodsName"));
		metaSignMap.put("payResult", jsonObj.getString("payResult"));// 支付状态
		metaSignMap.put("payDate", jsonObj.getString("payDate"));// yyyyMMddHHmmss
		String jsonStr = ToolKit.mapToJson(metaSignMap);
		String sign = ToolKit.MD5(jsonStr.toString() + key, ToolKit.CHARSET);
		if (!sign.equals(jsonObj.getString("sign"))) {
			return;
		}
		System.out.println("签名校验成功");
		response.getOutputStream().write("0".getBytes());
	}

	
	@Override
	public JSONObject smPay(PayEntity payEntity) {
		String payCode = payEntity.getPayCode();
		double amount = payEntity.getAmount();// "8.02";// 订单金额
		String userName = payEntity.getUsername();
		String mobile = payEntity.getMobile();
		
		// 原始的
		Map<String, String> metaSignMap = new TreeMap<String, String>();
		String orderNum = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()); // 20位
		orderNum += ToolKit.randomStr(3);
		metaSignMap.put("orderNum", orderNum);
		metaSignMap.put("version", "V4.0.0.0");// 现在版本为4.0没有升级不需要修改
		metaSignMap.put("charset", ToolKit.CHARSET);// 编码
		metaSignMap.put("random", ToolKit.randomStr(4));// 4位随机数

		// TODO 需要经常修改的参数
		//metaSignMap.put("bankCode",bankCode);// 银行代码 参考对照表
		metaSignMap.put("netway",payCode);// WX:微信支付,ZFB:支付宝支付
		metaSignMap.put("merNo", merNo); // 商户号
		metaSignMap.put("subMerNo", merNo);
		metaSignMap.put("goodsName", goodsName);// 商品名称：20位
		metaSignMap.put("amount", String.valueOf(amount));// 金额 单位:分
		metaSignMap.put("callBackUrl", callBackUrl);// 支付结果通知地址
		metaSignMap.put("callBackViewUrl", "http://localhost/view");// 回显地址
		metaSignMap.put("charset", "UTF-8");
		
		String metaSignJsonStr = ToolKit.mapToJson(metaSignMap);
		String sign = ToolKit.MD5(metaSignJsonStr + key, ToolKit.CHARSET);// 32位
		metaSignMap.put("sign", sign);
		byte[] dataStr = null;
		try {
			dataStr = ToolKit.encryptByPublicKey(ToolKit.mapToJson(metaSignMap).getBytes(ToolKit.CHARSET),
					ToolKit.PAY_PUBLIC_KEY);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String param = Base64.encode(dataStr);
		String reqParam = null;
		try {
			reqParam = "data=" + URLEncoder.encode(param, ToolKit.CHARSET) + "&merchNo=" + merNo
					+ "&version=" + metaSignMap.get("version");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		// scanPayUrl需要根据pay_code来选择
		String scanPayUrl = getPayUrl(payCode);
		// 检查状态
//		{"merNo":"SYF201803080000","msg":"提交成功
//			","orderNum":"20170812104118797WlN","qrcodeUrl":"https://qr.alipay.com/bax02093k8wsax23spmv4089","sign":"
//			947682D7579E2073DF243952702D5A14","stateCode":"00"}
		
		
		// pc端
        if (StringUtils.isBlank(mobile)) {
        	String resultJsonStr = ToolKit.request(scanPayUrl, reqParam);
    		JSONObject resultJsonObj = JSONObject.fromObject(resultJsonStr);
    		String stateCode = resultJsonObj.getString("stateCode");
            if ("00".equals(stateCode)) {
                return PayUtil.returnPayJson("success", "2", "支付接口请求成功!", userName, amount, orderNum,
                		resultJsonObj.getString("qrcodeUrl"));
            } else {
                return PayUtil.returnPayJson("error", "2", resultJsonObj.getString("msg"), userName, amount, orderNum, "");
            }
        } else {
            // 手机端
        	 String html = buildForm(metaSignMap, scanPayUrl);
             logger.info("闪亿付支付wap表单：" + html);
            return PayUtil.returnPayJson("success", "1", "支付接口请求成功!", userName, amount, orderNum, html);
        }
		
	}

	private String getPayUrl(String payCode) {
		HashMap<String, String> payMap = new HashMap<>();
		payMap.put("ZFB", "http://zfb.637pay.com/api/pay");
		payMap.put("ZFB_WAP", "http://zfbwap.637pay.com/api/pay");
		payMap.put("WX", "http://wx.637pay.com/api/pay");
		payMap.put("WX_H5", "http://wx.637pay.com/api/pay");
		payMap.put("WX_WAP", "http://wxwap.637pay.com/api/pay");
		payMap.put("QQ", "http://qq.637pay.com/api/pay");
		payMap.put("QQ_WAP", "http://qqwap.637pay.com/api/pay");
		
		payMap.put("JD", "http://jd.637pay.com/api/pay");
		payMap.put("BAIDU", "http://baidu.637pay.com/api/pay");
		payMap.put("UNION_WALLET", "http://unionpay.637pay.com/api/pay");
		payMap.put("MBANK", "http://mbank.637pay.com/api/pay");
		
		return payMap.get(payCode);
	}

	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		return null;
	}

	public String buildForm(Map<String, String> paramMap, String payUrl) {
        // 待请求参数数组
        String FormString = "<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form  id=\"actform\" name=\"actform\" method=\"post\" action=\""
                + payUrl + "\">";
        for (String key : paramMap.keySet()) {
            FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" + paramMap.get(key) + "'>\r\n";
        }
        FormString += "</form></body>";

        return FormString;
    }
    
    private JSONObject getReturnJson(String status, String qrCode, String msg) {
        JSONObject json = new JSONObject();
        json.put("status", status);
        json.put("qrCode", qrCode);
        json.put("msg", msg);
        return json;
    }
    /**
     * 回调验签
     * 
     * @param infoMap
     * @return
     */
    @Override
    public String callback(Map<String, String> infoMap) {
    	
		JSONObject jsonObj=JSONObject.fromObject(infoMap);
		Map<String, String> metaSignMap = new TreeMap<String, String>();
		metaSignMap.put("merNo", jsonObj.getString("merNo"));
		metaSignMap.put("netway", jsonObj.getString("netway"));
		metaSignMap.put("orderNum", jsonObj.getString("orderNum"));
		metaSignMap.put("amount", String.valueOf(jsonObj.getString("amount")));
		metaSignMap.put("goodsName", jsonObj.getString("goodsName"));
		metaSignMap.put("payResult", jsonObj.getString("payResult"));// 支付状态
		metaSignMap.put("payDate", jsonObj.getString("payDate"));// yyyyMMddHHmmss
		String jsonStr = ToolKit.mapToJson(metaSignMap);
		String sign = ToolKit.MD5(jsonStr.toString() + key, ToolKit.CHARSET);
		if (!sign.equals(jsonObj.getString("sign"))) {
			logger.info("验签失败");
	        return "fail";
		}
		logger.info("验签成功");
        return "success";
    }
}
