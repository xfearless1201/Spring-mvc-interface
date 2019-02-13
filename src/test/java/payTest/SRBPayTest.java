package payTest;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.cn.tianxia.pay.impl.SRBPayServiceImpl;
import com.cn.tianxia.pay.utils.HttpUtils;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName SRBPayTest
 * @Description 商入宝支付测试类
 * @author Hardy
 * @Date 2018年9月29日 上午10:04:32
 * @version 1.0.0
 */
public class SRBPayTest {

    public static void main(String[] args) {
        JSONObject data = new JSONObject();
        data.put("uid","5c2e91744f1493b53de7af0d");//商户uid
        data.put("notify_url","http://www.baidu.com");//通知回调网址
        data.put("goodsname","TOP-UP");//商品名称
        data.put("secret","35ea16bcfb72a533d881efcc922082a3");//秘钥
        data.put("payUrl", "https://pay.srbapi.com/");
        System.err.println(data.toString());
    }
    
    @Test
    public void callbackTest() throws Exception{
        Map<String,String> map = new HashMap<String,String>();
        map.put("uid","5c2e91744f1493b53de7af0d");//商户uid
        map.put("notify_url","http://www.baidu.com");//通知回调网址
        map.put("goodsname","TOP-UP");//商品名称
        map.put("secret","35ea16bcfb72a533d881efcc922082a3");//秘钥
        map.put("payUrl", "https://pay.srbapi.com/");
        SRBPayServiceImpl payService = new SRBPayServiceImpl(map);
        
        Map<String,String> data = new HashMap<String,String>();
        data.put("paysapi_id", "201809291949121453687");
        data.put("orderid", "SRBbl1201809291052311052311650");
        data.put("price", "100");
        data.put("realprice", "100");
        data.put("orderuid", "526829");
        data.put("token", "35ea16bcfb72a533d881efcc922082a3");
        String sign = payService.generatorSign(data);
        data.remove("token");
        data.put("key", sign);
        String tagUrl = "http://localhost:85/JJF/Notify/SRBNotify.do";
        String response = HttpUtils.post(data,tagUrl);
        System.err.println("回调返回结果:"+response);
    }
}
