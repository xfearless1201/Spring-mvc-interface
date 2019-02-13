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

import net.sf.json.JSONObject;

public class JFUPayTest {
    
    private static final Logger logger = LoggerFactory.getLogger(JFUPayTest.class);

    public static void main(String[] args) {
        JSONObject data = new JSONObject();
        data.put("returnType","json");//返回数据类型,必填参数json， html（详情请看，返回说明）
        data.put("apiCode","33396133");//商户号
        data.put("payUrl","http://39.109.9.11:10001/channel/Common/mail_interface");//支付类型
        data.put("notifyUrl","http://39.109.9.11:10001/channel/Common/mail_interface");//订单定价,留2位小数，不能传0
        data.put("secret","879adf264bf0fc038d427a413ede6b03");//您的自定义单号
        System.err.println(data.toString());
    }
    
    /**
     * 回调
     * @throws Exception 
     * @Description (TODO这里用一句话描述这个方法的作用)
     */
    @Test
    public void callbackTest() throws Exception{
        Map<String,String> data = new HashMap<>();
        data.put("messages","messages");//数组    否   是   返回提示
        data.put("paysapi_id","d40afac0af937a94be73b994ec1b958a");//    字符串 是   是   是此订单在Api服务器上的唯一编号
        data.put("order_id","JFUbl1201811041753291753295712");//字符串，最长50位 是   是   是您在发起支付接口传入的您的自定义订单号
        data.put("is_type","alipay");//字符串 是   是   必须，支付渠道：
        data.put("price","100.00");//float，保留2位小数 是   是   是您在发起支付接口传入的订单价格
        data.put("real_price","99.99");//    float，保留2位小数    是   是   表示用户实际支付的金额。一般会和price值一致，如果同时存在多个用户支付同一金额，就会和price存在一定差额，差额一般在1-2分钱上下，越多人同时支付，差额越大。
        data.put("mark","mark");//字符串   是   是   你提交上来的订单备注信息
        data.put("code","1");//字符串，长度1   是   是   订单状态判断标准：0 未处理 1 交易成功 2 支付失败 3 关闭交易 4 支付超时
        data.put("api_code","35363263");//字符串   是   否
        
        String sign = generatorSign(data);
        
        data.put("sign",sign);//字符串，最长32位 －   是
        
        String notifyUrl = "http://localhost:85/JJF/Notify/JFUNotify.do";
        
        String response = HttpUtils.toPostForm(data, notifyUrl);
        
        System.err.println(response);
    }
    
    public String generatorSign(Map<String,String> data) throws Exception{
        logger.info("[JFU]极付支付生成支付签名串开始=======================START=====================");
        try {
            //参数排序
            Map<String,String> sortmap = MapUtils.sortByKeys(data);
            //拼接参数，组装待签名串
            StringBuffer sb = new StringBuffer();
            Iterator<String> iterator = sortmap.keySet().iterator();
            while(iterator.hasNext()){
                String key = iterator.next();
                String val = sortmap.get(key);
                
                if(StringUtils.isBlank(val) || key.equalsIgnoreCase("sign") || 
                        key.equalsIgnoreCase("api_key") || key.equalsIgnoreCase("messages")) continue;
                sb.append(key).append("=").append(val).append("&");
            }
            sb.append("key=").append("69284eaca7b741ad0480ca83cc944c17");
            //生成待签名串
            String signStr = sb.toString();
            logger.info("[JFU]极付支付生成待签名串:"+signStr);
            //进行MD5签名，并结果转换为大写
            String sign = MD5Utils.md5toUpCase_32Bit(signStr);
            logger.info("[JFU]极付支付生成加密签名串:"+sign);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[JFU]极付支付生成支付签名串异常:"+e.getMessage());
            throw new Exception("[JFU]极付支付生成支付签名串异常!");
        }
    }
}
