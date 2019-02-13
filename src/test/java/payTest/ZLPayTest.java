package payTest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName ZLPayTest
 * @Description 站蓝支付测试类
 * @author Hardy
 * @Date 2018年11月8日 下午8:50:39
 * @version 1.0.0
 */
public class ZLPayTest {

    private static final Logger logger = LoggerFactory.getLogger(ANZFPayTest.class);

    public static void main(String[] args) {
        JSONObject data = new JSONObject();
        data.put("userId", "10565");
        data.put("fsecret", "e93505f1dc201940411cfed88054bd04");
        data.put("bsecret", "715698a347d627d0df627704db37b59b");
        data.put("payUrl", "https://api.lemeipay.com/Pay_API.aspx");
        data.put("notifyUrl", "http://localhost:8087/JJF/Notify/LMZFNotify.do");
        System.err.println(data.toString());
    }
}
