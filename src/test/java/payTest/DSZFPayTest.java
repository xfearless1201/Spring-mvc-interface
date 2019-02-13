package payTest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.pay.glzf.util.Md5Util;

import net.sf.json.JSONObject;

public class DSZFPayTest {
	private final static Logger logger = LoggerFactory.getLogger(DSZFPayTest.class);
	public static void main(String[] args) {
		
		JSONObject object = new JSONObject();
		object.put("fxid", "2018107");
		object.put("notifyUrl", "http://txw.tx8899.com/BLR/Notify/DSZFNotify.do");
		object.put("secret", "XMhpYbUsPIHezBivmJxHkuJgYLTGSDjd");
		object.put("payUrl", "http://dszf-api.com/pay");
		logger.info("object === {}",object);
		
		
		//amount=50.00&datetime=2018-12-10 15:59:10&memberid=10059&orderid=BCZFbl1201812101459091459094208
		//&returncode=00&transaction_id=812101459091459094208&key=kppjam4m46alclqymi1ord6381xp5wwi
		StringBuilder sb = new StringBuilder();
		sb.append("amount=50.00").append("&datetime=").append("2018-12-10 15:59:10")
		.append("&memberid=10059").append("&orderid=BCZFbl1201812101459091459094208")
		.append("&returncode=00").append("&transaction_id=812101459091459094208")
		.append("&key=kppjam4m46alclqymi1ord6381xp5wwi");
		
		logger.info("==========={}",Md5Util.md5(sb.toString(), "utf-8").toUpperCase());
		
	}

}
