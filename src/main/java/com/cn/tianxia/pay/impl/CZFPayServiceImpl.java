package com.cn.tianxia.pay.impl;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.common.PayUtil;
import com.cn.tianxia.pay.gst.util.StringUtils;
import com.cn.tianxia.pay.jh.util.SimpleHttpUtils;
import com.cn.tianxia.pay.kjf.util.HttpUtil;
import com.cn.tianxia.pay.kjf.util.MD5;
import com.cn.tianxia.pay.service.PayService;
import com.cn.tianxia.util.SSLClient;

import net.sf.json.JSONObject;

/**
 * 
 * @Description:TODO
 * 
 * @author:zouwei
 * 
 * @time:2017年7月6日 下午7:22:39
 * 
 */
public class CZFPayServiceImpl implements PayService {
	private String sign_type;// 加密方式 加密类型，取值：md5默认：md5
	private String mch_id;// 商户id
	private String remark;// 订单内容
	private String notify_url; // 回调通知URL
	private String url;// 请求地址
	private String mch_key;// 加密key

	private final static Logger logger = LoggerFactory.getLogger(CZFPayServiceImpl.class);

	public CZFPayServiceImpl(Map<String, String> pmap) {
		net.sf.json.JSONObject jo = new net.sf.json.JSONObject().fromObject(pmap);
		if (null != pmap) {
			sign_type = jo.get("sign_type").toString();
			mch_id = jo.get("mch_id").toString();
			remark = jo.get("remark").toString();
			notify_url = jo.get("notify_url").toString();
			url = jo.get("url").toString();
			mch_key = jo.get("mch_key").toString();
		}
	}

	/**
	 * 接口返回结果
	 * 
	 * @param link
	 * @param linkType
	 *            二种形式:1.qrcode 生成二维码 2.qrcode_url 支持跳转的url
	 * @param msg
	 * @param status
	 * @return
	 */
	private JSONObject retJSON(String link, String linkType, String msg, String status) {
		JSONObject json = new JSONObject();
		json.put("status", status);
		json.put("type", linkType);
		json.put("link", link);
		json.put("msg", msg);
		return json;
	}

	/**
	 * post 方法
	 * 
	 * @param url
	 * @param map
	 * @param charset
	 * @return
	 */
	public static String doPost(String url, Map<String, String> map, String charset) {
		HttpClient httpClient = null;
		HttpPost httpPost = null;
		String result = null;
		try {
			httpClient = new SSLClient();
			httpPost = new HttpPost(url);
			// ���ò���
			List<NameValuePair> list = new ArrayList<NameValuePair>();
			Iterator iterator = map.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, String> elem = (Entry<String, String>) iterator.next();
				list.add(new BasicNameValuePair(elem.getKey(), elem.getValue()));
			}
			if (list.size() > 0) {
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list, charset);
				httpPost.setEntity(entity);
			}
			HttpResponse response = httpClient.execute(httpPost);
			if (response != null) {
				HttpEntity resEntity = response.getEntity();
				if (resEntity != null) {
					result = EntityUtils.toString(resEntity, charset);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}

	public static String md5(String strSrc) {
		String result = "";
		try {
			MessageDigest md5;
			try {
				md5 = MessageDigest.getInstance("MD5");
				md5.update((strSrc).getBytes("UTF-8"));
				byte b[] = md5.digest();
				int i;
				StringBuffer buf = new StringBuffer("");
				for (int offset = 0; offset < b.length; offset++) {
					i = b[offset];
					if (i < 0) {
						i += 256;
					}
					if (i < 16) {
						buf.append("0");
					}
					buf.append(Integer.toHexString(i));
				}
				result = buf.toString();
				return result;
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	 /*
     * unicode编码转中文
     */
    public static String decodeUnicode(final String dataStr) {
        int start = 0;
        int end = 0;
        final StringBuffer buffer = new StringBuffer();
        while (start > -1) {
            end = dataStr.indexOf("\\u", start + 2);
            String charStr = "";
            if (end == -1) {
                charStr = dataStr.substring(start + 2, dataStr.length());
            } else {
                charStr = dataStr.substring(start + 2, end);
            }
            char letter = (char) Integer.parseInt(charStr, 16); // 16进制parse整形字符串。
            buffer.append(new Character(letter).toString());
            start = end;
        }
        return buffer.toString();
    }
    public static String gbEncoding(final String gbString) {
        char[] utfBytes = gbString.toCharArray();
        String unicodeBytes = "";
        for (int i = 0; i < utfBytes.length; i++) {
            String hexB = Integer.toHexString(utfBytes[i]);
            if (hexB.length() <= 2) {
                hexB = "00" + hexB;
            }
            unicodeBytes = unicodeBytes + "\\u" + hexB;
        }
        return unicodeBytes;
    }

	private JSONObject scanPay(Map<String, String> scanMap) {
		// TODO Auto-generated method stub
		if(!StringUtils.isNullOrEmpty(scanMap.get("mobile")) && scanMap.get("payType").equals("union_qrcode")){
			scanMap.put("mobile", null);
		}
		Map<String,String> hashMap = new HashMap<String,String>();
		hashMap.put("amt", scanMap.get("amt"));
		hashMap.put("client_ip", scanMap.get("client_ip"));
		hashMap.put("created_at", scanMap.get("created_at"));
		hashMap.put("mch_id", mch_id);
		hashMap.put("mch_order", scanMap.get("mch_order"));
		hashMap.put("notify_url", notify_url);
		hashMap.put("remark", remark);
		hashMap.put("sign_type", sign_type);
		hashMap.put("mch_key", mch_key);
		String queryStr = getSign(hashMap);
		String sign = md5(queryStr);
		hashMap.put("sign", sign);
		hashMap.remove("mch_key");
		System.out.println("请求地址"+url+scanMap.get("payType")+".api"+"请求参数："+hashMap.toString());
		String rusult = doPost(url+scanMap.get("payType")+".api", hashMap, "UTF-8");
		JSONObject json = JSONObject.fromObject(rusult);
		System.out.println("返回结果："+rusult);
		if("1".equals(json.get("code").toString())){
			JSONObject js = JSONObject.fromObject(json.get("data"));
			String link ="";
			if (StringUtils.isNullOrEmpty(scanMap.get("mobile"))) {
//				if(js.containsKey("redirect_pay_url") && (scanMap.get("payType").equals("ali_qrcode")||
//					scanMap.get("payType").equals("qq_qrcode")) ){
//					link = js.get("redirect_pay_url").toString().replace("\\\\","");
//				}else{
//				}
				//快捷支付
				if(scanMap.get("payType").equals("quick_page")){
					link = js.get("pay_info").toString().replace("\\\\","");
					return retJSON(link, "qrcode_url", "二维码图片生成", "success");
				}
				
				link = js.get("code_url").toString().replace("\\\\","");
				return retJSON(link, "qrcode", "二维码图片生成", "success");
			} else {
			    if(scanMap.get("payType").equals("jd_wap")){
                    link = js.get("pay_url").toString().replace("\\\\","");
                    return retJSON(link, "qrcode_url", "二维码图片生成", "success");
                }
			    
				 link = js.get("pay_info").toString().replace("\\\\","");
				return retJSON(js.get("pay_info").toString(), "qrcode_url", "二维码图片连接", "success");
			}
			
		}else{
			String msg = decodeUnicode(gbEncoding(json.get("msg").toString()));
			return retJSON("", "",msg, "error");
		}
	}

	public static String getSign(Map<String, String> map) {
		Set<String> set = new TreeSet<String>();
		StringBuffer sb = new StringBuffer();
		for (String s : map.keySet()) {
			set.add(s);
		}
		int i = 0;
		for (String s : set) {
			if (i < 1) {
				sb.append(s + "=" + map.get(s));
			} else {
				sb.append("&" + s + "=" + map.get(s));
			}
			i++;
		}
		System.out.println(sb.toString());
		return sb.toString();
	}

	@Override
	public String callback(Map<String, String> request) {
		List<String> keys = new ArrayList<String>(request.keySet());
		for (int i = 0; i < keys.size(); i++) {
			String name = (String) keys.get(i);
			String value = (String) request.get(name);
			if (StringUtils.isNullOrEmpty(value)) {
				
				logger.info("删除:" + name);
				request.remove(name);
			}
		}
		String sign = request.get("sign");
		request.remove("sign");
		request.put("mch_key", mch_key);
		String queryStr = getSign(request);
		String newSign = md5(queryStr);
		if(sign.equals(newSign)){
			return "success";
		}
		return null;
	}
	private static void callback() { 
	      Map<String, String> paramMap = new HashMap<String, String>(); 
	      String a  = System.currentTimeMillis()+"";
	      paramMap.put("mch_id", "viwgonhmfk"); 
	      paramMap.put("service", "10"); 
	      paramMap.put("mch_order", "CZFtxk201803182048512048513345"); 
	      paramMap.put("amt", "20000"); 
	      paramMap.put("mch_amt", "20000"); 
	      paramMap.put("sign_type", "md5"); 
	      paramMap.put("amt_type", "cny"); 
	      paramMap.put("status", "2"); 
	      paramMap.put("created_at", a); 
	      String b  = System.currentTimeMillis()+"";
	      paramMap.put("success_at", b); 
	      paramMap.put("mch_key", "d023d0e37451aa53ce44cd7fe570d6a9"); 
	      String signStr1 = getSign(paramMap); 
	      String  sign = md5(signStr1);
	      paramMap.put("sign", sign); 
	      paramMap.remove("mch_key");

	      String payResult = SimpleHttpUtils.httpPost("http://localhost:81/JJF/Notify/CZFNotify.do", paramMap);// 发送请求,POST请求，文档get请求是演示参数 
	      System.out.println(payResult); 
	   }

	@Override
	public JSONObject wyPay(PayEntity payEntity) {
		// TODO Auto-generated method stub
		Double amount = payEntity.getAmount();
		String order_no = payEntity.getOrderNo();
		String refereUrl = payEntity.getRefererUrl();
		String pay_code = payEntity.getPayCode();
		String userName = payEntity.getUsername();
		String pay_url = payEntity.getPayUrl();
		
		Map<String, String> scanMap = new HashMap<>();
		scanMap.put("mch_order", payEntity.getOrderNo());// 订单号
		scanMap.put("created_at", System.currentTimeMillis() + "");
		String amt = String.valueOf((int)(payEntity.getAmount() * 1000));
		scanMap.put("amt", amt);// 金额 .substring(0,amt.indexOf("."))
		scanMap.put("client_ip", payEntity.getIp());// "110.164.197.124"
		JSONObject retJson;
		scanMap.put("bank_code", payEntity.getPayCode());

		String payInfo = bankPay(scanMap);
		return PayUtil.returnWYPayJson("success", "link", payInfo, null, "");
	}

	private String bankPay(Map<String, String> scanMap) {
		Map<String,String> hashMap = new TreeMap<String,String>();
		hashMap.put("sign_type", sign_type);
		hashMap.put("mch_id", mch_id);
		hashMap.put("mch_order", scanMap.get("mch_order"));
		hashMap.put("amt", scanMap.get("amt"));
		hashMap.put("remark", remark);
		hashMap.put("created_at", scanMap.get("created_at"));
		hashMap.put("client_ip", scanMap.get("client_ip"));
		hashMap.put("notify_url", notify_url);
		hashMap.put("bank_code", scanMap.get("bank_code"));//scanMap.get("bank_code")
		hashMap.put("bank_card_type", "1");
		hashMap.put("mch_key", mch_key);
		
		String queryStr = getSign(hashMap);
		String sign = md5(queryStr);
		hashMap.put("sign", sign);
		hashMap.remove("mch_key");
		
		String responseStr = HttpUtil.RequestForm(url+"union.api", hashMap);
		JSONObject  responseJson= JSONObject.fromObject(responseStr);
		logger.info("畅支付支付请求:" + responseJson);
		String res_code = responseJson.getString("code");
		
		String pay_info = null;//网银收银台地址
		if("1".equals(res_code)) {//成功
			pay_info = responseJson.getJSONObject("data").getString("pay_info");
		}
		return pay_info;
	}

	@Override
	public JSONObject smPay(PayEntity payEntity) {
		// TODO Auto-generated method stub
		Double amount = payEntity.getAmount();
		String order_no = payEntity.getOrderNo();
		String userName = payEntity.getUsername();
		Map<String, String> scanMap = new HashMap<String, String>();
		Map<String, Object> json = new HashMap<String, Object>();
		scanMap.put("mch_order", payEntity.getOrderNo());// 订单号
		scanMap.put("created_at", System.currentTimeMillis() + "");
        // String amt = String.valueOf(payEntity.getAmount() * 1000);
		 DecimalFormat df = new DecimalFormat("############");
	        String amt = df.format(payEntity.getAmount()* 1000);
		
		scanMap.put("amt", amt);// 金额 .substring(0,amt.indexOf("."))
		scanMap.put("client_ip",payEntity.getIp());// payEntity.getIp());//"58.64.40.26"
		JSONObject retJson;
		scanMap.put("payType", payEntity.getPayCode());
		scanMap.put("mobile", payEntity.getMobile());
		retJson = scanPay(scanMap);

		if (retJson.getString("status").equals("success")) {
			if (retJson.getString("type").equals("qrcode")) {
				if(!StringUtils.isNullOrEmpty(payEntity.getMobile()) && payEntity.getPayCode().equals("union_qrcode")){
					return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, amount, order_no, retJson.getString("link"));
				}
				return PayUtil.returnPayJson("success", "2", "支付接口请求成功!", userName, amount, order_no, retJson.getString("link"));
			} else if (retJson.getString("type").equals("qrcode_url")) {
				return PayUtil.returnPayJson("success", "4", "支付接口请求成功!", userName, amount, order_no, retJson.getString("link"));
			} else if (payEntity.getPayCode().equals("wx_h5")){
			    return PayUtil.returnPayJson("error", "4", "支付接口请求失败!", userName, amount, order_no, retJson.getString("msg"));
			}else{
			    return PayUtil.returnPayJson("error", "1", "支付接口请求失败!", userName, amount, order_no, retJson.getString("msg"));
			}
		}
		return PayUtil.returnPayJson("error", "1", "支付接口请求失败!", userName, amount, order_no, retJson.getString("msg"));
	}
	
	public static void main(String[] args) {
 
		String s = "amt=123000&bank_code =1001&client_ip=127.0.0.1&created_at=1529564886503&mch_id=elpwifdmsc&mch_key=248254578cc92fabea3c3f7fac2aa67b&mch_order=CZFbl1201806211508031508037721&notify_url=http://txw.tx8899.com/YHH/Notify/CZFNotify.do&remark=tianxiazhifu&sign_type=md5";
		
		/*
		Map<String,String> hashMap = new HashMap<String,String>();
		hashMap.put("amt", "100000");
		hashMap.put("client_ip", "110.164.197.124");
		hashMap.put("created_at", System.currentTimeMillis() + "");
		hashMap.put("mch_id", "tmygnxasqh");
		hashMap.put("mch_order", "CZF"+System.currentTimeMillis());
		hashMap.put("notify_url", "https://www.6hghg.com/HG1/PlatformPay/QFTNotify.do");
		hashMap.put("remark", "xianxiazhifu");
		hashMap.put("sign_type", "md5");
		hashMap.put("mch_key", "284bdd34ae8e91156c3682ee0f26bad0");
//		hashMap.put("user_bank_id ", "1");
//		hashMap.put("bank_card_type ", "11");
//		hashMap.put("bank_code ", "1026");
//		hashMap.put("callback_url ", "https://www.6hghg.com");
		String queryStr = getSign(hashMap);
		String sign = md5(queryStr);
		hashMap.put("sign", sign);
		hashMap.remove("mch_key");
		String rusult = doPost("https://sdk.consucredit.com/api/v1/union_qrcode.api", hashMap, "UTF-8");
		
		System.out.println(rusult);
//		String FormString = "<body onLoad=\"document.actform.submit()\">正在处理请稍候.....................<form  id=\"actform\" name=\"actform\" method=\"post\" action=\""
//				+ "https://sdk.consucredit.com/api/v1/quick_page.api" + "\">";
//		for (String key : hashMap.keySet()) {
//			FormString += "<input name=\"" + key + "\" type=\"hidden\" value='" + hashMap.get(key) + "'>\r\n";
//		}
//		FormString += "</form></body>";
//
//		String html = FormString;
		// String html = HttpUtil.HtmlFrom(url, resquestMap);
//		System.out.println(html);	
		
		
//		Map<String,String> hashMap = new HashMap<String,String>();
//		hashMap.put("amt", "100000");
//		hashMap.put("client_ip", "110.164.197.124");
//		hashMap.put("created_at", System.currentTimeMillis() + "");
//		hashMap.put("mch_id", "elpwifdmsc");
//		hashMap.put("mch_order", "CZF"+System.currentTimeMillis());
//		hashMap.put("notify_url", "https://www.6hghg.com/HG1/PlatformPay/QFTNotify.do");
//		hashMap.put("remark", "xianxiazhifu");
//		hashMap.put("sign_type", "md5");
//		hashMap.put("mch_key", "248254578cc92fabea3c3f7fac2aa67b");
//		String queryStr = getSign(hashMap);
//		String sign = md5(queryStr);
//		hashMap.put("sign", sign);
//		hashMap.remove("mch_key");
//		String rusult = doPost("https://sdk.consucredit.com/api/v1/ali_qrcode.api", hashMap, "UTF-8");
//		JSONObject json = JSONObject.fromObject(rusult);
//		System.out.println("返回结果："+rusult);
	*/}
}
