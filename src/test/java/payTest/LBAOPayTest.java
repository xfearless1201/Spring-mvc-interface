package payTest;

import com.cn.tianxia.pay.impl.LAOMPayServiceImpl;
import com.cn.tianxia.pay.impl.LBAOPayServiceImpl;
import com.cn.tianxia.pay.utils.HttpUtils;
import com.cn.tianxia.pay.utils.XTUtils;
import org.junit.Test;

import java.util.HashMap;

/**
 * @ClassName: LBAOPayTest
 * @Description: 龙宝支付测试类
 * @Author: Zed
 * @Date: 2018-12-29 13:56
 * @Version:1.0.0
 **/

public class LBAOPayTest {
    @Test
    public void testCallback() throws Exception{

        String keyValue   = "gprxfpnBCsQFSjcnASXmfu3ulu2pRaCa";   // 商家密钥
        String r0_Cmd 	  = "Buy"; // 业务类型
        String p1_MerId   = "1693";   // 商户编号
        String r1_Code    = "1";// 支付结果
        String r2_TrxId   = "abcdefg1234000";// API支付交易流水号
        String r3_Amt     = "100";// 支付金额
        String r4_Cur     = "CNY";// 交易币种
        String r5_Pid     = "top_up";// 商品名称
        String r6_Order   = "LBAObl1201812291117501117507752";// 商户订单号
        String r7_Uid     = "bl1zed1994";// API支付会员ID
        String r8_MP      = "";// 商户扩展信息
        String r9_BType   = "2";// 交易结果返回类型
        String hmac       = verifyCallback(p1_MerId,
                 r0_Cmd,  r1_Code,  r2_TrxId,  r3_Amt,
                 r4_Cur,  r5_Pid,  r6_Order,  r7_Uid,
                 r8_MP,  r9_BType,  keyValue);

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
        params.put("hmac",hmac);

        String notifyUrl = "http://localhost:85/JJF/Notify/LBAONotify.do";

        String response = HttpUtils.toPostForm(params, notifyUrl);

        System.err.println(response);
    }

    private static String verifyCallback(String p1_MerId,
                                         String r0_Cmd, String r1_Code, String r2_TrxId, String r3_Amt,
                                         String r4_Cur, String r5_Pid, String r6_Order, String r7_Uid,
                                         String r8_MP, String r9_BType, String keyValue) {
        StringBuffer sValue = new StringBuffer();
        // 商户编号
        sValue.append(p1_MerId);
        // 业务类型
        sValue.append(r0_Cmd);
        // 支付结果
        sValue.append(r1_Code);
        // 易宝支付交易流水号
        sValue.append(r2_TrxId);
        // 支付金额
        sValue.append(r3_Amt);
        // 交易币种
        sValue.append(r4_Cur);
        // 商品名称
        sValue.append(r5_Pid);
        // 商户订单号
        sValue.append(r6_Order);
        // 易宝支付会员ID
        sValue.append(r7_Uid);
        // 商户扩展信息
        sValue.append(r8_MP);
        // 交易结果返回类型
        sValue.append(r9_BType);
        String sNewString = null;
        sNewString = XTUtils.hmacSign(sValue.toString(), keyValue);

        return sNewString;
    }

}
