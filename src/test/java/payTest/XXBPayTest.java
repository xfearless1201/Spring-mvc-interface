package payTest;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.cn.tianxia.pay.impl.SRBPayServiceImpl;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;

/**
 * 
 * @ClassName XXBPayTest
 * @Description 小熊宝回调测试类
 * @author Hardy
 * @Date 2018年9月29日 下午8:46:23
 * @version 1.0.0
 */
public class XXBPayTest {
    
    @Test
    public void callbackTest() throws Exception{
//        Map<String,String> map = new HashMap<String,String>();
//        map.put("payUrl","https://api.shaimeixiong.com/api/receive?type=form");//商户uid
//        map.put("merchantId","600101820");//通知回调网址
//        map.put("md5Key","4AF708880EEEE58DF11D1B2C4A110C56");//商品名称
//        map.put("notifyURL","http://txw.tx8899.com/YHH/Notify/XXBNotify.do");//秘钥
//        map.put("goodsName", "XXBZF");
//        map.put("type", "form");
//        SRBPayServiceImpl payService = new SRBPayServiceImpl(map);
        
        Map<String,String> data = new HashMap<String,String>();
        data.put("merchantOrderNo", "XXBBL1201809121901431901437994");
        data.put("orderNo", "2018092910523110456321784");
        data.put("money", "100");
        data.put("payAmount", "100");
        data.put("paytype", "1");
        
        String sginStr = "2018092910523110456321784&XXBBL1201809121901431901437994&100&100&4AF708880EEEE58DF11D1B2C4A110C56";
        
        String sign = MD5Utils.md5toUpCase_32Bit(sginStr);
        data.put("sign", sign);
        String tagUrl = "http://localhost:85/JJF/Notify/XXBNotify.do";
        String response = HttpUtils.post(data,tagUrl);
        System.err.println("回调返回结果:"+response);
    }

}
