package payTest;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.cn.tianxia.pay.utils.HttpUtils;

/**
 * @Description: TODO
 * @ClassName:SXYPayTest
 * @Auther: Wilson
 * @Date: 2018/8/22 21:23
 * @Version:1.0.0
 */
public class SXYPayTest {
    @Test
    public void payTest() throws Exception{
        Map<String,String> data = new HashMap<String,String>();
        data.put("v_ur","");//
        data.put("v_oid","20181003-19041001-2216018055");//
        data.put("v_pmode","");//
        data.put("v_pstatus","20");//
        data.put("v_pstring","");//
        data.put("v_md5info", "27b447ebadc2dbf57208253b59adf1da");//
        data.put("v_amount","100.00");//
        data.put("v_moneytype","0");//
        data.put("v_md5money","d7daee0933dbd0e1407898eb0ac48b96");//
        data.put("v_sign","4c2dba08783057c7c36c4b43cbbf5ac7261f9287ab08af662174f63cae30c5d642ef0dcd425958dae15eb4650025e834fd00d3d5b72db57635475c21c6c65b6fb28fae1bec16c59273b8e405f764932f4bd96769f9b44a235bcd7422a2957a43f79e5d45ee35e83a043280eb69a82d92b5d040b94eea888da2861688a6877c900801ee8ed16bf359ca02c63a74351a0080e686e4cfa3691638e2ebf892c22d8243039a688ffaf383f1ad5d95ddbacbf0a9a1da7c3d5f8d6a3452c2b31a21e994418314d0c7c0ae38d0b55c4c76c80ece07a937471a091652e11f89190c29d819d3bb1fc9a7a1757fdfc482ddb1aa1f6fc3d6cc429c7a83fd4076a05864aa4b36");//
        String topUrl = "http://localhost:8087/JJF/Notify/SXYNotify.do";
        String response = HttpUtils.toPostForm(data, topUrl);
        System.err.println("回调结果:"+response);
    }
}
