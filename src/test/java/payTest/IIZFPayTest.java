package payTest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.pay.jhz.util.MD5Utils;

import net.sf.json.JSONObject;

/**
 * iipays 支付测试类
 * @author TX
 *
 */
public class IIZFPayTest {
	private final static Logger logger = LoggerFactory.getLogger(IIZFPayTest.class);
	public static void main(String[] args) {
		JSONObject object = new JSONObject();
		object.put("version", "1.0");
		object.put("notify_url", "http://localhost:85/JJF/Notify/IIZFNotify.do");
		object.put("pay_url", "http://www.bikedq.com/Bike/apisubmit");
		object.put("customerid", "11362");
		object.put("secret", "b982ad45718e92ea1a34b39484abf61c34213d2a");
		logger.info("pay_url= {}",object);
		
		String billno = "IIZFbl1201812191346271346273543";
		logger.info("billno = {}",billno.length());
		
		//"customerid="+customerid+"&status="+status+"&sdpayno="+sdpayno+"&sdorderno="+sdorderno+"&total_fee="+total_fee+"&paytype="+paytype
		StringBuilder sb = new StringBuilder();
		sb.append("customerid=11362").append("&status=1").append("&sdpayno=113622018121915542829115624")
		.append("&sdorderno=IIZFbl120181219145423145423438").append("&total_fee=100")
		.append("&paytype=AliPayPScan").append("&b982ad45718e92ea1a34b39484abf61c34213d2a");
		
		logger.info("iipyas 签名值:{}",MD5Utils.md5(sb.toString()));
		
	}

}
