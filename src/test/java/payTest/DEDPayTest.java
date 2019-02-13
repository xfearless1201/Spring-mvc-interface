package payTest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.MD5Utils;
import com.cn.tianxia.pay.utils.MapUtils;

import net.sf.json.JSONObject;

/**
 * @ClassName DEDPayTest
 * @Description 得到支付
 * @author Hardy
 * @Date 2018年10月18日 上午11:13:11
 * @version 1.0.0
 */
public class DEDPayTest {

    public static void main(String[] args) {
        JSONObject data = new JSONObject();
        data.put("mchId", "15608864553");
        data.put("payUrl", "http://pay.pcfpay.cn/pay/unifiedorder");
        data.put("notifyUrl", "http://pay.pcfpay.cn/pay/unifiedorder");
        data.put("secret", "0b35ee60d45e57033630cd7e6892b2ce");
        System.err.println(data.toString());
    }

    @Test
    public void callbackTest() throws Exception {
        Map<String, String> data = new HashMap<>();
        data.put("result_code", "SUCCESS");//
        data.put("mch_id", "15608864553");//
        data.put("trade_type", "ALISCAN");//
        data.put("nonce", "7VS264I5K850SI8ZNMTM6LTKCH16CQ2");//
        data.put("timestamp", "1524822584");//
        data.put("out_trade_no", "DEDbl1201810191746401746403808");//
        data.put("total_fee", "100");//
        data.put("trade_no", "20150806125346");//
        data.put("platform_trade_ no", "20181019091010");//
        data.put("pay_time", "20181019091010");//

        String sign = generatorSign(data);
        data.put("sign", sign);

        String nitifyUrl = "http://localhost:85/JJF/Notify/DEDNotify.do";

        String response = HttpUtils.toPostForm(data, nitifyUrl);
        System.err.println(response);

    }

    private String generatorSign(Map<String, String> data) throws Exception {
        try {
            // 第一步，设所有发送或者接收到的数据为集合 M，将集合 M 内非空参数值的参数按照参数名 ASCII 码从小到大排序（字典序），一定要转换为大写
            // 使用 URL 键值对的格式（即key1=value1&key2=value2…）拼接成字符串 stringA。特别注意以下重要规则：
            // 1.参数名 ASCII 码从小到大排序（字典序）;2.如果参数的值为空不参与签名;3.参数名区分大小写;
            // 4.验证调用返回或主动通知签名时，传送的 sign 参数不参与签名，将生成的签名与该sign 值作校验。
            // 第二步，在 stringA 最后拼接上 key 得到 stringSignTemp 字符串，并对 stringSignTemp 进行 MD5 运算，
            // 再将得到的字符串所有字符转换为大写，得到 sign 值 signValue。
            Map<String, String> treemap = MapUtils.sortByKeys(data);
            StringBuffer sb = new StringBuffer();
            Iterator<String> iterator = treemap.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                String val = treemap.get(key);

                if (StringUtils.isBlank(val) || key.equalsIgnoreCase("sign"))
                    continue;

                sb.append(key).append("=").append(val).append("&");
            }
            sb.append("key=").append("0b35ee60d45e57033630cd7e6892b2ce");
            // 获取待签名串
            String signStr = sb.toString();
            // 进行MD签名,大写
            String sign = MD5Utils.md5toUpCase_32Bit(signStr);
            return sign;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("生成支付签名串异常!");
        }
    }
}
