package payTest;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.pay.utils.MD5Utils;

import net.sf.json.JSONObject;
/**
 * 乐百支付测试类
 * @author TX
 */
public class LMZFPayTest {
	private final static Logger logger = LoggerFactory.getLogger(LMZFPayTest.class);
	public static void main(String[] args) {
		
		JSONObject object = new JSONObject();
		object.put("pay_memberid", "10203");
		object.put("notifyUrl", "http://localhost:8087/JJF/Notify/XYFNotify.do");
		object.put("secret", "zkw3tfdlfmn8sc3cmxf86nwgfcxol2ax");
		object.put("payUrl", "http://pay.kadiya66.com/Pay_Index.html");	
		logger.info("{}",object);
		
		//pay_amount=99&pay_applydate=2019-01-03 21:48:54&pay_bankcode=907&pay_callbackurl=http://localhost/&pay_memberid=10203&pay_orderid=LBZFbl1201901032148402148409155&key=zkw3tfdlfmn8sc3cmxf86nwgfcxol2ax
		Map<String,String> map = new LinkedHashMap<String,String>();
		
		map.put("amount", "99");
		map.put("datetime", "2019-01-03 21:48:54");
		map.put("memberid", "10203");
		map.put("orderid", "LBZFbl1201901032148402148409155");
		map.put("returncode", "00");
		map.put("transaction_id", "1201901032148402148409155");
		map.put("key", "zkw3tfdlfmn8sc3cmxf86nwgfcxol2ax");
		StringBuilder sb = new StringBuilder();
		for(Entry<String, String> entry : map.entrySet()){
			logger.info("key = {}, value = {}", entry.getKey(), entry.getValue());
			sb.append(entry.getKey()).append("=").append(entry.getValue());
		}
		
		try {
			String str = MD5Utils.md5toUpCase_32Bit(sb.toString());
			logger.info("str = {}", str);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
	}
		
}
