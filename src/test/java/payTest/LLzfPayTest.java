package payTest;

import net.sf.json.JSONObject;

/**
 * 连连支付 测试类
 * @author TX
 */
public class LLzfPayTest {
	
	public static void main(String[] args) {
		//{info_order=, money_order=20.00, name_goods=, no_order=LLZFbl1201811230949200949208768, 
		//notify_url=http://txw.tx8899.com/XCY/Notify/LLzfNotify.do, oid_partner=201805271741040432, pay_type=48, 
		//return_url=http://localhost/, sign=042b8f0888669b5964caeeb93ffbc0e0, sign_type=MD5, time_order=20181123095032, 
		//user_id=20181123094945RIaI}
		JSONObject json = new JSONObject();
		json.put("oid_partner", "201805271741040432");
		json.put("sign_type", "MD5");
		json.put("sign", "042b8f0888669b5964caeeb93ffbc0e0");
		json.put("no_order", "LLZFbl1201811230949200949208768");
		json.put("oid_paybill", "201805271741040432");
		json.put("time_order", "20181123095032");
		json.put("money_order", "20.01");
		json.put("result_pay", "SUCCESS");
		String url = "http://localhost:85/JJF/Notify/LLzfNotify.do";
		String result = HttpUtils.doPost(url, json);
		System.out.println("result = " + result);
		
		
	}

}
