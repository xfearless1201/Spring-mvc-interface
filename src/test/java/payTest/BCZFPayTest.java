package payTest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONObject;

public class BCZFPayTest {
	private final static Logger logger = LoggerFactory.getLogger(BCZFPayTest.class);
	public static void main(String[] args) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("pay_memberid", "10059");
		jsonObject.put("key", "kppjam4m46alclqymi1ord6381xp5wwi");
		jsonObject.put("pay_notifyurl", "http://localhost:8080/notifyurl.jsp");
		jsonObject.put("pay_url", "http://bczf138.com/Pay_Index.html");
		logger.info("json = {}",jsonObject);
	}

}
