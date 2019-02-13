package payTest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONObject;

/**
 * 云付宝测试
 * @author TX
 *
 */
public class YFBPayTest {
	private final static Logger logger = LoggerFactory.getLogger(YFBPayTest.class);
	public static void main(String[] args) {
		/*JSONObject object = new JSONObject();
		object.put("accountId", "1810302217564TmgF9Q9");
		object.put("notifyUrl", "http://localhost:85/JJF/Notify/YFBNotify.do");
		object.put("secret", "181030221756S6KC6b7r");//密钥
		object.put("payUrl", "http://www.tjzf8888.com/gateway/Pay/pay.html");//支付地址
		
		logger.info("JSONObject:{}",object);*/
		
		//{"trade_status":"200","out_trade_no":"YFBbl1201811121114441114442281","total_amount":"2.00","gmt_payment":1541996147,"sign":"A187060DAA5F7ABA00834DE48E601371"}
		JSONObject object = new JSONObject();
		object.put("trade_status", "200");
		object.put("out_trade_no", "YFBbl1201811121114441114442281");
		object.put("total_amount", "2.00");
		object.put("gmt_payment", 1541996147);
		object.put("sign", "A187060DAA5F7ABA00834DE48E601371");
		String url = "http://localhost:85/JJF/Notify/YFBNotify.do";
		String result = HttpUtils.post(url, object.toString());
		
		logger.info("YFBPayTest 回调函数返回值:{}", result);
	}

}
