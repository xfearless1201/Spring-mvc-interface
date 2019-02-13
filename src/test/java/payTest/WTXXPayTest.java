package payTest;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.pay.utils.MD5Utils;

import net.sf.json.JSONObject;

/**
 * 万通XX支付 测试类
 * @author TX
 *
 */
public class WTXXPayTest {
	private final static Logger logger = LoggerFactory.getLogger(WTXXPayTest.class);
	public static void main(String[] args) {
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("uid", "038370136135662203");
		jsonObject.put("payUrl", "http://api.171yun.com/payapi/v1/orders_2");
		jsonObject.put("notifyUrl", "http://localhost:85/JJF/Notify/WTXXNotify.do");
		jsonObject.put("secret", "qq953597vgwepjedeyxpitaponuxloqf");
		logger.info("JSONObject = {} ", jsonObject);
		
		String secret = "qq953597vgwepjedeyxpitaponuxloqf";
		Map<String,String> json = new HashMap<String,String>();
		json.put("uid", "038370136135662203");
		json.put("orderid", "WTXXbl12019010618173");
		json.put("transid", "12019010618173");
		json.put("transtime", "9010618173");
		json.put("price", "10400");
		json.put("paytype", "100");
		json.put("status", "2");
		
		StringBuilder str = new StringBuilder();
		Iterator<String> iter = json.keySet().iterator();
		while(iter.hasNext()){
			String key = iter.next();
			String value = json.get(key);
			str.append(key).append("=").append(value).append("&");
		}
		String param = str.substring(0, str.length()-1);
		
		try {
			String md5 = MD5Utils.md5toUpCase_32Bit(param+secret);
			logger.info("md5 = {} ",md5);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
	}
}
