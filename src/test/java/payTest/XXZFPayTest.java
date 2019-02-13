package payTest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONObject;

/**
 * 新鑫支付
 * @author TX
 */
public class XXZFPayTest {
	private final static Logger logger = LoggerFactory.getLogger(XXZFPayTest.class);
	
	public static void main(String[] args) {
		JSONObject json = new JSONObject();
		json.put("seller_id", "5d78113c-8489-4251-8207-efaca2151ea0");
		json.put("notify_url", "http://localhost:85/JJF/Notify/XXZFNotify.do");
		json.put("secret", "09625F08XX6449sU5984Mv");
		json.put("payUrl", "http://47.107.88.80/api/Pay/Order");
		
		logger.info("{}",json);
	}
}
