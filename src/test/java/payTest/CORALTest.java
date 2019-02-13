/****************************************************************** 
 *
 * Powered By tianxia-online. 
 *
 * Copyright (c) 2018-2020 Digital Telemedia 天下网络 
 * http://www.d-telemedia.com/ 
 *
 * Package: payTest 
 *
 * Filename: CORALTest.java 
 *
 * Description: TODO(用一句话描述该文件做什么) 
 *
 * Copyright: Copyright (c) 2018-2020 
 *
 * Company: 天下网络科技 
 *
 * @author: Elephone
 *
 * @version: 1.0.0
 *
 * Create at: 2018年08月31日 20:37 
 *
 * Revision: 
 *
 * 2018/8/31 20:37 
 * - first revision 
 *
 *****************************************************************/
package payTest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cn.tianxia.pay.mqzf.util.MD5;
import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

/**
 * @ClassName CORALTest
 * @Description TODO(这里用一句话描述这个类的作用)
 * @Author Elephone
 * @Date 2018年08月31日 20:37
 * @Version 1.0.0
 **/
public class CORALTest {

    @Test
    public  void testPayCallBack(){
        String key = "o5hm6wnx17kgu1jtyvsbsor4p1b3nq29";
        String memberid = "22";
        String orderid = "20180831174431868206";
        String amount = "100";
        String datetime = "23425234";
        String returncode = "00";
        String transaction_id = "2525";

        HashMap<String,String> m =new HashMap<String, String>();
        m.put("memberid",memberid);
        m.put("orderid",orderid);
        m.put("amount",amount);   // 1 - 支付完成；
        m.put("datetime",datetime);   // 支付成功（当 v_pstatus=1 时）
        m.put("returncode",returncode);
        m.put("transaction_id",transaction_id);

        String SignTemp="amount="+amount+"+datetime="+datetime+"+memberid="+memberid+"+orderid="+orderid+"+returncode="+returncode+"+transaction_id="+transaction_id+"+key="+key+"";

        String sign = "";
        try {
            sign = MD5.md5(SignTemp);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        m.put("sign",sign);
//        HttpUtils.doPost("http://localhost:8087/JJF/Notify/CORALNotify.do",m);

        String s = JSON.toJSONString(m);
        byte[] bytes = null;
        try {
            bytes = s.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            HttpUtils.post("http://localhost:8087/JJF/Notify/CORALNotify.do",bytes,"application/json");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
