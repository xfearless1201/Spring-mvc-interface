package payTest;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.cn.tianxia.controller.BaseController;
import com.cn.tianxia.util.DESEncrypt;
import org.junit.Test;

import net.sf.json.JSONObject;
import com.cn.tianxia.pay.bfb.util.HttpUtils;

public class JYPayTest {

	
	@Test
	public void getJson(){
		JSONObject jo = new JSONObject();
		jo.put("memberid", "180891761");
		jo.put("key", "0qmvg38n9n8ic3l6cleglbyksrj31t9p");
		jo.put("payUrl", "https://www.9-epay.com/Pay_Index.html");
		jo.put("notifyUrl", "http://localhost:85/JJF/Notify/JYNotify.do");
		System.out.print(jo.toString());
		ConcurrentHashMap map = BaseController.payMap;
		System.out.print(map);
//		String deskey = "tianxia88";
//		DESEncrypt d = new DESEncrypt(deskey);
//		String passWord = "1234";
//		try {
//			passWord = d.encrypt(passWord);
//			System.out.println(passWord);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
	
	@Test
	public void callbackTest(){
		Map<String,String> data = new HashMap<>();
        data.put("partner","100047");
        data.put("UserNumber","XYFbl1201809021640031640031443");
        data.put("OrderStatus","1");
        data.put("PayMoney","10");
        data.put("Sign","A3A204D54CA09E5A5DAF5CC24EA828E178D7A091739C14714B761244F62E4D6F9C4B6B57DC426DAE716BB81513433C77");

//        Map<String,String> map = new HashMap();
//        map.put("ID","100047");
//        map.put("sercet","A3A204D54CA09E5A5DAF5CC24EA828E178D7A091739C14714B761244F62E4D6F9C4B6B57DC426DAE716BB81513433C77");
//        map.put("payUrl","http://aukao.cn/pay.aspx");
//        map.put("callbackurl","http://www.baidu.com");

        String notifyUrl = "http://127.0.0.1:8087/JJF/Notify/JYNotify.do";

        String response = HttpUtils.doPost(notifyUrl,data);

        System.out.println(response);

	}
}
