package payTest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName SLJHPayTest
 * @Description 顺利聚合支付测试类
 * @author Hardy
 * @Date 2018年11月8日 下午8:49:47
 * @version 1.0.0
 */
public class SLJHPayTest {
    
    private static final Logger logger = LoggerFactory.getLogger(ANZFPayTest.class);

    public static void main(String[] args) {
        JSONObject data = new JSONObject();
        data.put("payUrl", "http://poly.igepay.com/Pay_Index.html");
        data.put("memberid", "10162");
        data.put("notifyUrl", "http://localhost:8087/JJF/Notify/SLJHNotify.do");
        data.put("secret", "ukn41g1mqnf7gvobn0kdf8i32dh5ijsl");
        System.err.println(data.toString());
    }

}
