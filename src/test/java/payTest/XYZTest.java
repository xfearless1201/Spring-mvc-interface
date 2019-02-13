package payTest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.omg.SendingContext.RunTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.pay.impl.XYZPayServiceImpl;
import com.cn.tianxia.pay.xyz.util.XMLUtils;
import com.cn.tianxia.pay.ys.util.DateUtil;

import net.sf.json.JSONObject;

/**
 * 信誉支付测试类
 * @author TX
 */
public class XYZTest {

	private final static Logger logger = LoggerFactory.getLogger(XYZTest.class);
	
	public static void main(String[] args) throws Exception {
		/*JSONObject object = new JSONObject();
		object.put("transaction_id", "XY1810151206499868696");
		object.put("charset", "UTF-8");
		object.put("mer_id", "100290");
		object.put("notify_url", "");
		object.put("key", "35f6acbb3f4340518615deab26a9c093");
		object.put("queryUrl", "");*/
	
//		System.out.println(" ==" + object.toString());
		/*Map map = new HashMap();
		XYZPayServiceImpl xyzPay = new XYZPayServiceImpl(map);
		String xml = "<xml><transaction_id><![CDATA[XY1810151206499868696]]></transaction_id><charset><![CDATA[UTF-8]]></charset><nonce_str>1539576673600</nonce_str><sign><![CDATA[E89CBCD97988929BEE752331C04C306F]]></sign><fee_type><![CDATA[CNY]]></fee_type><mch_id><![CDATA[100000]]></mch_id><version><![CDATA[1.0]]></version><pay_result>0</pay_result><out_trade_no><![CDATA[XYZbl1201811041126151126153145]]></out_trade_no><total_fee>20000</total_fee><trade_type>ALIPAY</trade_type><result_code>0</result_code><time_end><![CDATA[2018-10-15 12:11:13]]></time_end><sign_type><![CDATA[MD5]]></sign_type><status>0</status></xml>";
		HashMap<String,String> hashMap = XMLUtils.formatXMlToMap(xml);
		String sign = xyzPay.generatorSign(hashMap);
		System.out.println("sign = " + sign);*/
		
		String xml = "<xml><transaction_id><![CDATA[XY1810151206499868696]]></transaction_id><charset><![CDATA[UTF-8]]></charset><nonce_str>1539576673600</nonce_str><sign><![CDATA[E89CBCD97988929BEE752331C04C306F]]></sign><fee_type><![CDATA[CNY]]></fee_type><mch_id><![CDATA[100000]]></mch_id><version><![CDATA[1.0]]></version><pay_result>0</pay_result><out_trade_no><![CDATA[XYZbl1201811041126151126153145]]></out_trade_no><total_fee>20000</total_fee><trade_type>ALIPAY</trade_type><result_code>0</result_code><time_end><![CDATA[2018-10-15 12:11:13]]></time_end><sign_type><![CDATA[MD5]]></sign_type><status>0</status></xml>";
		String url = "http://localhost:85/JJF/Notify/XYZNotify.do";//回调路径
		//String url = "http://192.168.0.61:282/XPJ/Notify/XYZNotify.do";//http://192.168.0.61:282/XPJ/Notify/XYZNotify.do
		try {
			//JSONObject jsonObject = XMLUtils.formatXMlToMap(xml);
			//System.out.println("jsonObject = " + jsonObject);
			//String result = HttpUtil.toPostIO(xml, url, "");
			String result = HttpUtils.post(url, xml);
			System.out.println("result = " + result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//String str = "TXZFtxk201811052118302118306380";
		//System.out.println("str -= " + str.length());
		
		
		
		
		/*Map<String,String> object = new HashMap<String,String>();
		object.put("pay_memberid", "YM201108");
		object.put("payUrl", "https://pay.yongzf.net/load");
		object.put("notify_url", "http://localhost:85/JJF/Notify/YFZNotify.do");
		object.put("secret", "eb7fc29421b60bb430d1474372a1424b");
		
		for(Entry<String,String> entry : object.entrySet()){
			logger.info("=======clear 之前 =key= {}, value = {} ", entry.getKey() , entry.getValue());
		}
		//Map<String,String> clearMap = new HashMap<String,String>();
		
		Collection<String> array = object.values();
		for(String str : array){
			logger.info("values === {}",str);
		}*/
		/*logger.info("clear================");
		for(Entry<String,String> entry : object.entrySet()){
			logger.info("=======clear之后 =key= {}, value = {} ", entry.getKey() , entry.getValue());
		}*/
		//System.out.println("date = " + DateUtil.getCurrentDate("YYYYMMDDHHMMSS"));
		
		long begin = 0;
		long end = 0;
		
		begin = Runtime.getRuntime().freeMemory();
		logger.info("空闲内存: {}",begin);
		
		String str = "20274772018110857505551B4C9E2FEB7";
		logger.info("========{}",str.length());
		
	}

}
