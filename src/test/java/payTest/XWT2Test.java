package payTest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.cn.tianxia.pay.bfb.util.HttpUtils;
import com.cn.tianxia.pay.tx.util.MD5Utils;

/**
 * 新2万通支付
 * @author TX
 */
public class XWT2Test {
	private final static Logger logger = LoggerFactory.getLogger(XWT2Test.class);
	public static void main(String[] args) {
		JSONObject json = new JSONObject(true);
		json.put("payUrl", "http://wtzf666.com/Pay_Index.html");
		json.put("notifyUrl", "http://localhost:85/JJF/Notify/XWTNotify.do");
		json.put("secret", "6qx7kjmq16kkckvs0qy7d4w53wp7d9za");
		json.put("pay_memberid", "10109");
		
		logger.info("json = {}",json);
		
		Map<String,String> map = new HashMap<String,String>();
		//map.put("", );
		
		String notifyUrl = "http://192.168.0.185:8087/JJF/Notify/XYFNotify.do";
	    //String response = HttpUtils.doPost(notifyUrl,map);
	    //System.out.println(response);
		
	    //生成MD5
	    JSONObject object = new JSONObject(true);
	    object.put("amount", "50.00");
	    object.put("datetime", "2018-12-10 18:37:06");
	    object.put("memberid", "10109");
	    object.put("orderid", "XWTbl1201812121439211439214603");
	    object.put("returncode", "00");
	    object.put("transaction_id", "1201812101737031737031123");
	    object.put("key", "6qx7kjmq16kkckvs0qy7d4w53wp7d9za");
	    StringBuilder sb = new StringBuilder();
	    for(Entry<String, Object> entry : object.entrySet()){
	    	logger.info("key:{},value:{}",entry.getKey(),entry.getValue());
	    	sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
	    }
	    String md5 = sb.substring(0,sb.length()-1);
	    logger.info("md5 = {}",md5);
	    String result = MD5Utils.md5(md5).toUpperCase();
	    
	    logger.info("result = {}",result);
	    //String md5 = MD5Utils.md5(object);
	}

}
