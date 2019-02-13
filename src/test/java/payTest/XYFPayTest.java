/****************************************************************** 
 *
 *    Powered By tianxia-online. 
 *
 *    Copyright (c) 2018-2020 Digital Telemedia 天下网络 
 *    http://www.d-telemedia.com/ 
 *
 *    Package:     payTest 
 *
 *    Filename:    XYFPayTest.java 
 *
 *    Description: TODO(用一句话描述该文件做什么) 
 *
 *    Copyright:   Copyright (c) 2018-2020 
 *
 *    Company:     天下网络科技 
 *
 *    @author:     Tammy 
 *
 *    @version:    1.0.0 
 *
 *    Create at:   2018年09月02日 19:50 
 *
 *    Revision: 
 *
 *    2018/9/2 19:50 
 *        - first revision 
 *
 *****************************************************************/
package payTest;

import com.cn.tianxia.pay.bfb.util.HttpUtils;

import net.sf.json.JSONObject;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 *  * @ClassName XYFPayTest
 *  * @Description TODO(这里用一句话描述这个类的作用)
 *  * @Author Tammy
 *  * @Date 2018年09月02日 19:50
 *  * @Version 1.0.0
 *  
 **/
public class XYFPayTest {

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

//        String notifyUrl = "http://192.168.0.185:8087/JJF/Notify/XYFNotify.do";
//
//        String response = HttpUtils.doPost(notifyUrl,data);
//
//        System.out.println(response);
        System.err.println(data.toString());
        System.err.println(JSONObject.fromObject(data).toString());
        System.err.println(data.get("partner"));



    }
}
