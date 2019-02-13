package payTest;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName JHZFPayTest
 * @Description 聚合支付测试类
 * @author Hardy
 * @Date 2018年11月27日 下午12:03:40
 * @version 1.0.0
 */
public class JHZFPayTest {

    public static void main(String[] args) {
        JSONObject data = new JSONObject();
        data.put("merId", "100519280");
        data.put("notifyUrl", "http://localhost:8087/JJF/Notify/ZYNotify.do");
        data.put("smPayUrl", " http://47.75.173.161:8080/payment/ScanPayApply.do");
        data.put("wyPayUrl", "http://47.75.173.161:8080/payment/PayApply.do");
        data.put("sercet", "6VeXZeLpCsCm");
        data.put("receivableType", "D00");
        
        System.err.println(data.toString());
    }
}
