package payTest;

import org.junit.Test;

import com.cn.tianxia.util.v2.HttpUtils;
import com.cn.tianxia.util.v2.MD5Utils;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName FLZFPayTest
 * @Description 富乐支付测试类
 * @author Hardy
 * @Date 2018年11月7日 下午8:43:48
 * @version 1.0.0
 */
public class FLZFPayTest {
    
    public static void main(String[] args) {
        JSONObject data = new JSONObject();
        data.put("payUrl", "http://q.101juan.com/lakalapay.php");
        data.put("uid", "1238343420");
        data.put("notifyUrl", "http://localhost:8087/JJF/Notify/FLZFNotify.do");
        data.put("secret", "jdjtGdjHjdjhGhdjjejvhjedj64jdhjrkHjdj64773jfjh");
        System.err.println(data.toString());
    }

    @Test
    public void callbackTest() throws Exception{
        String currtime = String.valueOf(System.currentTimeMillis());
        StringBuffer sb = new StringBuffer();
        sb.append("order_id=").append("FLZFbl1201811072049592049593485").append("&price=").append("100.00").append("&txnTime=").append(currtime);
        String signStr = "FLZFbl1201811072049592049593485100.00"+currtime+"jdjtGdjHjdjhGhdjjejvhjedj64jdhjrkHjdj64773jfjh";
        String sign = MD5Utils.md5toUpCase_32Bit(signStr).toLowerCase();
        sb.append("&sign=").append(sign);
        
        String repParams = sb.toString();
        repParams = "http://localhost:8087/JJF/Notify/FLZFNotify.do?"+repParams;
        
        String response = HttpUtils.get(repParams);
        
        System.err.println(response);
    }
}
