package payTest;

import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.XTUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: QFTZFPayTest
 * @Description: 全付通支付回调测试类
 * @Author: Zed
 * @Date: 2019-01-09 19:28
 * @Version:1.0.0
 **/

public class QFTZFPayTest {
    @Test
    public void testCallback() throws Exception{

        String keyValue   = "746e34cf8e2d41c7";   // 商家密钥
        String r0_Cmd 	  = "Buy"; // 业务类型
        String p1_MerId   = "21053";   // 商户编号
        String r1_Code    = "1";// 支付结果
        String r2_TrxId   = "woshizhendedashabi123";// API支付交易流水号
        String r3_Amt     = "197";// 支付金额
        String r4_Cur     = "CNY";// 交易币种
        String r5_Pid     = "top_up";// 商品名称
        String r6_Order   = "QFT1547035469473";// 商户订单号
        String r7_Uid     = "bl1zed1994";// API支付会员ID
        String r8_MP      = "";// 商户扩展信息
        String r9_BType   = "2";// 交易结果返回类型

        HashMap<String, String> params = new HashMap<>();
        params.put("r0_Cmd",r0_Cmd);
        params.put("p1_MerId",p1_MerId);
        params.put("r1_Code",r1_Code);
        params.put("r2_TrxId",r2_TrxId);
        params.put("r3_Amt",r3_Amt);
        params.put("r4_Cur",r4_Cur);
        params.put("r5_Pid",r5_Pid);
        params.put("r6_Order",r6_Order);
        params.put("r7_Uid",r7_Uid);
        params.put("r8_MP",r8_MP);
        params.put("r9_BType",r9_BType);
        String hmac       = verifyCallback(params,keyValue);
        params.put("hmac",hmac);

        String notifyUrl = "http://localhost:85/JJF/Notify/QFTZFNotify.do";

        String response = HttpUtils.toPostForm(params, notifyUrl);

        System.err.println(response);
    }

    private static String verifyCallback(Map data, String keyValue) {
        StringBuffer sValue = new StringBuffer();
        // 商户编号
        sValue.append(data.get("p1_MerId")).append("^|^");
        // 业务类型
        sValue.append(data.get("r0_Cmd")).append("^|^");
        // 支付结果
        sValue.append(data.get("r1_Code")).append("^|^");
        // 易宝支付交易流水号
        sValue.append(data.get("r2_TrxId")).append("^|^");
        // 支付金额
        sValue.append(data.get("r3_Amt")).append("^|^");
        // 交易币种
        sValue.append(data.get("r4_Cur")).append("^|^");
        // 商品名称
        sValue.append(data.get("r5_Pid")).append("^|^");
        // 商户订单号
        sValue.append(data.get("r6_Order")).append("^|^");
        // 易宝支付会员ID
        sValue.append(data.get("r7_Uid")).append("^|^");
        // 商户扩展信息
        sValue.append(data.get("r8_MP")).append("^|^");
        // 交易结果返回类型
        sValue.append(data.get("r9_BType")).append("^|^");
        String sNewString = null;
        sNewString = XTUtils.hmacSign(sValue.toString(), keyValue);

        return sNewString;
    }

}
