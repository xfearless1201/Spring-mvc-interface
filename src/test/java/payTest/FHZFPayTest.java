package payTest;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.pay.tx.util.MD5Utils;
import com.cn.tianxia.pay.utils.MapUtils;

import net.sf.json.JSONObject;

/**
 * 富豪支付
 * @author TX
 */
public class FHZFPayTest {
	private final static Logger logger = LoggerFactory.getLogger(FHZFPayTest.class);
	public static void main(String[] args) {
		JSONObject json = new JSONObject();
		json.put("partner", "1003446323");
		json.put("secret", "89RF5m3JbTJUOVJeWeixiuXB56kS0r6b");
		json.put("payUrl", "https://mer.woniu97.com/payCenter/aliPay2");
		json.put("notify_url", "http://localhost:85/JJF/Notify/FHNotify.do");
		
		//logger.info("json = {}",json);
		String secret = "89RF5m3JbTJUOVJeWeixiuXB56kS0r6b";
		Map<String,String> map = new HashMap<String,String>();
		map.put("sign_type", "MD5");
		map.put("request_time", "2018-12-10 18:37:06");
		map.put("memberid", "10109");
		map.put("out_trade_no", "FHZFbl1201812161047081047081793");
		map.put("amount_str", "20.00");
		map.put("trade_id", "1201812101737031737031123");
		map.put("status", "1");
		
		try {
			Map<String,String> sortMap = MapUtils.sortByKeys(map);
			StringBuilder sb = new StringBuilder();
			for(Entry<String,String> entry : sortMap.entrySet()){
				sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
			}
			sb.append(secret);
			
			String md5 = MD5Utils.md5(sb.toString());
			logger.info("md5 value = {}",md5);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
