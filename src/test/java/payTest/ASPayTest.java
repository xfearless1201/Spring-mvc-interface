package payTest;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 安盛支付测试类
 * @author TX
 */
public class ASPayTest {
	private final static Logger logger = LoggerFactory.getLogger(ASPayTest.class);
	public static void main(String[] args) {
		
		/*JSONObject object = new JSONObject();
		object.put("mchid", "00030011");
		object.put("submchid", "00030011000000000001");
		object.put("notify_url", "http://localhost:85/JJF/Notify/HXNotify.do");
		object.put("secret", "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDLkcyscl9HMcW3IfG55OA21n1+2VkmqA7xvC09x6J1cu+io68vT/GBCeOWEAUGLTVN8M6VK7J80uJw2iqaM8XQbLNAfVBKgIfICsLPmPzJlhKqqj7DffOj5EKWNCMY+GKONMtMcyWjMVaLthpZQUmUFk/nJDAThZ982AM6VJedYwIDAQAB");
		object.put("payUrl", "http://47.97.105.36/api-v1-order/qrcode");
		
		logger.info("安盛支付 = {}",object);*/
		//double money = 99.99;
		//logger.info("money = " + String.valueOf(money));
		String time = null;
		Map<String,String> map = new HashMap<String,String>();
		map.put("time", time);
		String result = map.get("time");
		logger.info(result);
	}

	private String doGet(String url){
		
		
		
		return null;
	}
	
}
