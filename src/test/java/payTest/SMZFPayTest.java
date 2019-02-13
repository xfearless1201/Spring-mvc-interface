package payTest;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.pay.tx.util.MD5Utils;
import com.cn.tianxia.pay.utils.RandomUtils;

import net.sf.json.JSONObject;

/**
 * 商盟支付测试类
 * @author TX
 */
public class SMZFPayTest {
	private final static Logger logger = LoggerFactory.getLogger(SMZFPayTest.class);
	
	public static void main(String[] args) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("app_id", "80080");
		jsonObject.put("payUrl", "http://122.114.159.150:8080/payment/create");
		jsonObject.put("notifyUrl", "http://localhost:8087/JJF/Notify/SMZFNotify.do");
		jsonObject.put("secret", "7CEAF4AA3573203845B5");
		
		logger.info("jsonObject = {} ", jsonObject);
		
		Map<String,String> map = new TreeMap<String,String>();
		map.put("app_id", "80080");
		map.put("platform_order_id", "1528061528063588");
		map.put("order_id", "SMZFbl1201901071528061528063588");
		map.put("status","2");
		map.put("code", "200");
		map.put("price", "10000");
		map.put("money", "50000");
		map.put("pay_type", "5");
		map.put("pay_id", "5");
		map.put("user_id", "565274");
		map.put("ts", "1546851480017");
		map.put("pay_ts", "20190107");
		map.put("rand", "gOX2om");
		
		StringBuilder sb = new StringBuilder();
		for(Entry<String,String> entry : map.entrySet()){
			logger.info("key = {}, value = {}", entry.getKey(), entry.getValue());
			sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
		}
		
		sb.append("key=7CEAF4AA3573203845B5"); 
		String sign = MD5Utils.md5(sb.toString());
		logger.info("sign = {}",sign);
		map.put("sign", "34f2adf098d08e3efce9a011a23f0aee");
		
		
	}
	
}
