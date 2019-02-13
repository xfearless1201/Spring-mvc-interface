package payTest;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONObject;

import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.xyz.util.HttpUtil;

public class FXPayTest {
	private final static Logger logger = LoggerFactory.getLogger(FXPayTest.class);
	public static void main(String[] args) {
		
		/*JSONObject object = new JSONObject();
		object.put("fx_merchant_id", "3299801");
		object.put("secret", "zhyndkIglFfSZsVswEzWDJgrXMbSzQhA");
		object.put("payUrl", "https://www.fengxie.co/Pay");
		object.put("notify_url", "http://localhost:85/JJF/Notify/FXNotify.do");
		
		logger.info("风携支付 === {}",object);*/
		
		String url = "http://localhost:85/JJF/Notify/FXNotify.do";//回调路径
		//String url = "http://192.168.0.61:282/XPJ/Notify/XYZNotify.do";//http://192.168.0.61:282/XPJ/Notify/XYZNotify.do
		Map<String,String> object = new HashMap<>();
		object.put("fx_merchant_id", "2027477");
		object.put("fx_order_id", "FXbl1201811081448331448333545");
		object.put("fx_transaction_id", "20274772018110857505551B4C9E2FEB7");
		object.put("fx_desc", "TOP-UP");
		object.put("fx_order_amount", "1.98");
		object.put("fx_original_amount", "2.0");
		object.put("fx_attch", "");
		object.put("fx_status_code", "200");
		object.put("fx_time", "1541663523");
		object.put("fx_sign", "7308b6feadbe39dd67ce0cb35bf21429");//key cGndipusdbfVbvBvMeQgPKpintSgTNSl
		logger.info("[风携支付] 参数:{}",object);
		try {
//			JSONObject jsonObject = XMLUtils.formatXMlToMap(xml);
//			System.out.println("jsonObject = " + jsonObject);
			String result = HttpUtils.toPostForm(object,url);
			System.out.println("result = " + result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//logger.info("1:{},2:{},3:{}",1,2,3);
		
	}

}
