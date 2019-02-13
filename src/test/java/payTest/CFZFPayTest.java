package payTest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import com.cn.tianxia.pay.utils.MapUtils;

/**
 * 
 * @ClassName CFZFPayTest
 * @Description 财富支付测试类
 * @author Hardy
 * @Date 2018年10月29日 下午4:16:09
 * @version 1.0.0
 */
public class CFZFPayTest {
    
    //日志
    private static final Logger logger = LoggerFactory.getLogger(CFZFPayTest.class);

    @Test
    public void callbackTest() throws Exception{
        Map<String,String> data = new HashMap<>();
        data.put("pay_sn","流水号");
        data.put("trade_out_no", "CFZFbl1201810291603451603453802");
        data.put("amount", "1000");
        data.put("real_amount", "1000");
        data.put("error", "0");
        String sign = generatorSign(data);
        data.put("sign", sign);
        
        String notifyUrl = "http://localhost:85/JJF/Notify/CFZFNotify.do";
        
        String response = HttpUtils.toPostForm(data, notifyUrl);
        
        System.err.println(response);
    }
    
    
    private String generatorSign(Map<String,String> data) throws Exception{
        logger.info("[CFZF]财富支付生成签名串开始===========================START=========================");
        try {
            StringBuffer sb = new StringBuffer();
            //签名规则:把必填参数，连Token一起，按参数名字母升序排序。并把参数值拼接在一起。做md5-32位加密，取字符串小写。得到sign。网址类型的参数值不要urlencode。
            data.put("token", "da5dc591230fb521fe2267bc9e9e86f6");
            Map<String,String> sortmap = MapUtils.sortByKeys(data);
            Iterator<String> iterator = sortmap.keySet().iterator();
            while(iterator.hasNext()){
                String key = iterator.next();
                String val = sortmap.get(key);
                
                if(StringUtils.isBlank(val) || key.equalsIgnoreCase("sign") 
                        || key.equalsIgnoreCase("comefrom") || key.equalsIgnoreCase("create_type") 
                        || key.equalsIgnoreCase("error")) continue;
                
                sb.append("&").append(key).append("=").append(val);
            }
            String signStr = sb.toString().replaceFirst("&", "");
            logger.info("[CFZF]财富支付生成支付待签名串:"+signStr);
            String sign = MD5Utils.md5toUpCase_32Bit(signStr).toLowerCase();
            logger.info("[CFZF]财富支付生成支付签名串:"+sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[CFZF]财富支付生成签名串异常:"+e.getMessage());
            throw new Exception("生成签名串异常!");
        }
    }
}
