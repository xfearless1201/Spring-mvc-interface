package payTest;

import org.junit.Test;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName SZPayTest
 * @Description 山竹支付测试类
 * @author Hardy
 * @Date 2018年10月10日 下午5:41:40
 * @version 1.0.0
 */
public class SZPayTest {
    
    public static void main(String[] args) {
        JSONObject data = new JSONObject();
        data.put("payUrl", "https://域名/pay/unifiedorder");
        data.put("notifyUrl", "http://localhost:8087/JJF/Notify/SZNotify.do");
        data.put("secret", "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDTdFOyCdGtNQuz/0k3rCrCJwB44TSmOBEQOGfDMDy1o0QX6ZRWlq2M2mZ6ULxWicaXTrD1ks4evo9q9qoffYLqbR6IAlsBIXA/rLUxMP58KnB7MJwNkDwfJBuD6kn0Xvdq7MVsfatJd8JiCAXoz7NoqphIr+iXt5KAWM96iOrOmQIDAQAB");
        data.put("mchId", "100000008119");
        System.err.println(data.toString());
    }

    @Test
    public void callbakTest(){
        String str = "SZbl1201810101737171737175868";
        System.err.println(str.length());
    }
}
