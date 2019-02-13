package payTest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONObject;

/**
 * 汇鑫支付测试类
 * @author TX
 *
 */
public class HXPayTest {
	private final static Logger logger = LoggerFactory.getLogger(HXPayTest.class);
	
	public static void main(String[] args) {
		/*JSONObject object = new JSONObject();
		object.put("mch_id", "710510001");
		object.put("secret", "oqbv57bas8he8jp92f0mqer3zr02gs0z");
		object.put("notify_url", "http://localhost:85/JJF/Notify/FXNotify.do");
		
		logger.info("汇鑫支付:::{}",object);*/
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("mch_id", "710510001");
		jsonObject.put("order_id", "1113112353047360");
		jsonObject.put("out_trade_no", "HXbl1201811131023481023482147");
		jsonObject.put("pay_type", "302");
		jsonObject.put("sign", "419BD8ED1AD5B7650E17F236C568AFF2");
		jsonObject.put("status", "1");
		jsonObject.put("total_fee", "2000");
		jsonObject.put("traid", "1113112353047360");
		String url = "http://localhost:85/JJF/Notify/HXNotify.do";
		
		String result = HttpUtils.post(url, jsonObject.toString());
		logger.info("[汇鑫支付] 返回值:{}",result);
	}

}
