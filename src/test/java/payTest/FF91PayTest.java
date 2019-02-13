package payTest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

public class FF91PayTest {
	private final static Logger logger = LoggerFactory.getLogger(FF91PayTest.class);
	public static void main(String[] args) {
		JSONObject object = new JSONObject();
		object.put("version", "1.0");
		object.put("payUrl", "http://www.FF91pay.com/pay/payapi/create");
		object.put("notifyUrl", "http://localhost:85/JJF/Notify/FFNotify.do");
		object.put("secret", "nyIQiVmARJBF8sX2CGT1SrFTMeyCulJC");
		
		logger.info("object = {}",object);
	}

}
