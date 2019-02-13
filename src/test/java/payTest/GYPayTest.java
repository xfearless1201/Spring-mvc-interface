package payTest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONObject;
/**
 * 
 * @author TX
 */
public class GYPayTest {
	private final static Logger log = LoggerFactory.getLogger(GYPayTest.class);
	public static void main(String[] args) {
		JSONObject json = new JSONObject();
		json.put("uid", "4bfc23f0");
		json.put("notify_url", "http://localhost:85/JJF/Notify/FXNotify.do");
		json.put("secret", "cea37f77b67f3ebea312d16496f1e5ba");
		
		log.info("JSON = {}", json);
	}

}
