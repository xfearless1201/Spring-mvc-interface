package payTest;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONObject;

public class BXPayTest {
	private final static Logger logger = LoggerFactory.getLogger(BXPayTest.class);
	public static void main(String[] args) {
		/*JSONObject object = new JSONObject();
		
		object.put("amchid", "10002");
		object.put("secret", "853c3bbec746135743ff38561feac7c9");
		object.put("payUrl", "https://api.paybaixin.com/pay/");
		object.put("notify_url", "http://localhost:85/JJF/Notify/HXNotify.do");
		
		logger.info("object:{}",object);*/
		//String secret = "853c3bbec746135743ff38561fe9ac7c9";
		//logger.info("===={}",secret.subSequence(0,secret.length()-1));
		//logger.info("==={}",StringUtils.substringBefore(secret, "9"));
		//String str ="amchid=10000002&border=BXbl1201811191734121734129451&cpacc=TOP-UP&dmoney=10&enotifyurl=http://localhost:85/JJF/Notify/BXNotify.do&freturl=DOWN&gpaytype=8&hbcode=&iclientip=127.0.0.1853c3bbec746135743ff38561feac7c9";
		//String str2 = "amchid=10000002&border=BXbl1201811191718091718094995&cpacc=TOP-UP&dmoney=10&enotifyurl=http://localhost:85/JJF/Notify/BXNotify.do&freturl=DOWN&gpaytype=8&hbcode=&iclientip=127.0.0.1853c3bbec746135743ff38561feac7c9";
		//logger.info("BXPayTest = {}", demo.MD5(str2).toLowerCase());
		
		//String url = "https://api.paybaixin.com/pay";
		//String param = "border=BXbl1201811191734121734129451&cpacc=TOP-UP&gpaytype=8&dmoney=10&amchid=10000002&enotifyurl=http://localhost:85/JJF/Notify/BXNotify.do&sign=ab296207075aa0c087905dcfa7e6eadc&hbcode=&freturl=DOWN&iclientip=127.0.0.1";
		//HttpUtils.sendPost(url, param);
		
		/*JSONObject json = new JSONObject();
		json.put("aorder", "BXssd");
		json.put("bmoney", "300.00");*/
		String url = "http://localhost:85/JJF/Notify/BXNotify.do";
		JSONObject map = new JSONObject();
		//{sign=934790efc314c31be7ce59cc31f93eb3, bmoney=200, cpacc=TOP-UP, aorder=BXtxk201812031613381613381144, state=2}
		map.put("border", "BXtxk201812031613381613381144");
		map.put("cpacc", "TOP-UP");
		map.put("dmoney", "200");
		map.put("sign", "934790efc314c31be7ce59cc31f93eb3");
		map.put("state", "2");
		
		String result = HttpUtils.post(url, map.toString());
		logger.info("result = {}", result);
	}

}
