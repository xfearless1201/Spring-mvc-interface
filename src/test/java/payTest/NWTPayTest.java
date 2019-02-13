package payTest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.cn.tianxia.pay.impl.NWTPayServiceImpl;
import com.cn.tianxia.pay.qyf.util.ToolKit;

/**
 * 
 * @ClassName NWTPayTest
 * @Description 新万通测试类
 * @author Hardy
 * @Date 2018年9月22日 上午9:58:52
 * @version 1.0.0
 */
public class NWTPayTest {

    @Test
    public void callbackTest() throws Exception{
        
        Map<String,String> map = new HashMap<String,String>();
        map.put("MerId", "10437");
        map.put("NotifyUrl", "http://txw.tx8899.com/TYC/Notify/NWTNotify.do");
        map.put("sercet", "ba828a940ce75468d5cce004a12611f9b4c8e30e96eee2bdf7f1b66d6cbbba0a");
        map.put("payUrl", "http://www.wtzfpay.com/Payapi_Index_Pay.html");
        
        NWTPayServiceImpl payService = new NWTPayServiceImpl(map);
        
        Map<String,String> data = new HashMap<String,String>();
        data.put("Sjt_factMoney", "100");
        data.put("Sjt_Username", "526829");
        data.put("Sjt_TransID", "20180929194912195666");
        data.put("Sjt_BType", "2");
        data.put("Sjt_Error", "");
        data.put("Sjt_OderId", "NWTbl1201809291946451");
        data.put("Sjt_SuccTime", "20180921185039");
        data.put("Sjt_Return", "1");
        data.put("Sjt_MerchantID", "437");
        String sign = payService.generatorSign(data, 0);
        data.put("Sjt_Sign", sign);
//        String reqParams = sb.toString().replaceFirst("&", "");
        String tagUrl = "http://localhost:85/JJF/Notify/NWTNotify.do";
        String response = com.cn.tianxia.pay.mqzf.util.HttpUtil.post(tagUrl, data);
        System.err.println(response);
    }
}
