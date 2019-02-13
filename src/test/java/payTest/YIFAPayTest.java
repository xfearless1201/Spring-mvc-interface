package payTest;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName YIFAPayTest
 * @Description 易发支付测试类
 * @author Hardy
 * @Date 2018年10月18日 下午2:13:35
 * @version 1.0.0
 */
public class YIFAPayTest {
    
    

    public static void main(String[] args) {
        JSONObject data = new JSONObject();
        data.put("shopId", "39358ec60842eb388bf1ac4f37e1f239");
        data.put("payUrl", "https://www.zhihe999.com/api/shopApi/order/createorder");
        data.put("notifyUrl", "https://www.zhihe999.com/api/shopApi/order/createorder");
        data.put("secret", "owZszn3XTe46jt4N");
        System.err.println(data.toString());
    }
    
    @Test
    public void callbackTest() throws Exception{
        Map<String,String> data = new HashMap<>();
        data.put("order_no",System.currentTimeMillis()+"");//
        data.put("user_id","526829");//
        data.put("shop_no","YIFAbl120181109143946");//
        data.put("money","99.98");//
        data.put("type","alipay");//
        data.put("date","2018-10-20 14:53:44");//
        data.put("trade_no",System.currentTimeMillis()+"");//
        data.put("status","0");//
        String sign = generatorSign(data);
        data.put("sign","dfjkldjadjaf58451fdadada");//
        String notifyUrl="http://localhost:85/JJF/Notify/TESTNotify.do";
        String response = HttpUtils.toPostForm(data, notifyUrl);
        System.err.println(response);
        
    }
    
    /**
     * 
     * @Description 生成签名串
     * @param data
     * @return
     * @throws Exception
     */
    private String generatorSign(Map<String,String> data) throws Exception{
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("39358ec60842eb388bf1ac4f37e1f239").append(data.get("user_id")).append(data.get("order_no"));
            sb.append("owZszn3XTe46jt4N").append(data.get("money")).append(data.get("type"));
            String signStr = sb.toString();
            String sign = MD5Utils.md5toUpCase_32Bit(signStr).toLowerCase();
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("生成签名串异常!");
        }
    }
}
