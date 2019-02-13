package com.cn.tianxia.game.impl;

import com.cn.tianxia.game.GameReflectService;
import com.cn.tianxia.po.v2.GameResponse;
import com.cn.tianxia.util.Encrypt;
import com.cn.tianxia.util.FileLog;
import com.cn.tianxia.util.PlatFromConfig;
import com.cn.tianxia.vo.v2.GameBalanceVO;
import com.cn.tianxia.vo.v2.GameCheckOrCreateVO;
import com.cn.tianxia.vo.v2.GameForwardVO;
import com.cn.tianxia.vo.v2.GameQueryOrderVO;
import com.cn.tianxia.vo.v2.GameTransferVO;

import net.sf.json.JSONObject;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @ClassName LYQPServiceImpl
 * @Description 乐游棋牌游戏接口实现类
 * @author Hardy
 * @Date 2019年2月9日 下午4:34:37
 * @version 1.0.0
 */
public class LYQPGameServiceImpl implements GameReflectService{
    
    private static final Logger logger = LoggerFactory.getLogger(LYQPGameServiceImpl.class);

    private String api_url;
    private String api_deskey;
    private String api_md5key;
    private String api_cagent;
    private String lineCode;

    public LYQPGameServiceImpl(Map<String, String> pmap) {
        PlatFromConfig pf = new PlatFromConfig();
        pf.InitData(pmap, "LYQP");
        JSONObject jo = JSONObject.fromObject(pf.getPlatform_config());
        api_url = jo.getString("api_url").toString();
        api_deskey = jo.getString("api_deskey").toString();
        api_md5key = jo.getString("api_md5key").toString();
        api_cagent = jo.getString("api_cagent").toString();
        lineCode = jo.getString("lineCode").toString();
    }

    public static void main(String[] args) {
        Map<String, String> pmap = new HashMap<String, String>();
        pmap.put("LYQP",
                "{'api_url':'https://api.leg668.com:189/channelHandle?'," +
                        "'pull_url':'https://record.leg668.com:190/getRecordHandle?'," +
                        "'api_deskey':'b3648fe062704394'," +
                        "'api_md5key':'38215242322443c9'," +
                        "'api_cagent':'70041'," +
                        "'KindID':'0'," +
                        "'lineCode':'100'}");
        LYQPGameServiceImpl k = new LYQPGameServiceImpl(pmap);

        //1.登录游戏
        //String url=k.checkOrCreateGameAccout("bl1huanghao93","127.0.0.1","0");
        //String msg = JSONObject.fromObject(url).getJSONObject("d").getString("url");

        //2.转入
        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Date now =new Date();
        String time=sdf.format(now);

        String msg = k.channelHandleOn("bl1huanghao93", "70041"+time+"bl1huanghao93", "200", "2");
        System.out.println(msg);
    }

    /**
     * 此接口用以验证游戏账号，如果账号不存在则创建游戏账号。并为账号上分。
     */

    public String checkOrCreateGameAccout(String loginname, String ip, String GameID) {
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String orderid = api_cagent + sf.format(new Date()) + loginname;

        Map<String, String> map = new HashMap<String, String>();
        map.put("s", "0");
        map.put("account", loginname);
        map.put("money", "0");
        map.put("orderid", orderid);
        map.put("ip", ip);
        map.put("lineCode", lineCode);
        map.put("KindID", GameID);

        String url = this.getLYQPUrl(map);
        String datastr = sendPost(url);
        if ("".equals(datastr) || datastr == null || "null".equals(datastr)) {
            return "error";
        } else {
            JSONObject js = JSONObject.fromObject(datastr);
            if (!"0".equals(js.getJSONObject("d").get("code") + "")) {
                FileLog f = new FileLog();
                Map<String, String> param = new HashMap<>();
                param.put("loginname", loginname);
                param.put("url", url);
                param.put("datastr", datastr);
                param.put("Function", "CheckOrCreateGameAccout");
                f.setLog("LYQP", param);
                return "error";
            }
        }
        return datastr;
    }

    /**
     * 查询可下分余额
     */

    public String queryUnderTheBalance(String loginname) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("s", "1");
        map.put("account", loginname);

        String url = this.getLYQPUrl(map);
        String datastr = sendPost(url);
        System.out.println(datastr);
        if ("".equals(datastr) || datastr == null || "null".equals(datastr)) {
            return "error";
        } else {
            JSONObject js = JSONObject.fromObject(datastr);
            if (!"0".equals(js.getJSONObject("d").get("code") + "")) {
                FileLog f = new FileLog();
                Map<String, String> param = new HashMap<>();
                param.put("loginname", loginname);
                param.put("url", url);
                param.put("datastr", datastr);
                param.put("Function", "queryUnderTheBalance");
                f.setLog("LYQP", param);
                return "error";
            }
        }
        return datastr;
    }

    /**
     * 上分flag：2 下分 flag：3
     */

    public String channelHandleOn(String loginname, String orderid, String money, String flag) {

//        String res = orderQuery(orderid);
//        if (!"error".equals(res)) {
//            JSONObject orderjs = JSONObject.fromObject(res);
//            if (orderjs.getJSONObject("d").getInt("status") != -1) {
//                return "error";
//            }
//        }
        
        Map<String, String> map = new HashMap<String, String>();
        map.put("s", flag);
        map.put("account", loginname);
        map.put("money", money);
        map.put("orderid", orderid);

        String url = this.getLYQPUrl(map);
        String datastr = sendPost(url);
        System.out.println(datastr);
        if ("".equals(datastr) || datastr == null || "null".equals(datastr)) {
            return "error";
        } else {
            JSONObject js = JSONObject.fromObject(datastr);
            if ("0".equals(js.getJSONObject("d").get("code").toString())) {
                return "success";
            } else {
                FileLog f = new FileLog();
                Map<String, String> param = new HashMap<>();
                param.put("loginname", loginname);
                param.put("url", url);
                param.put("datastr", datastr);
                param.put("Function", "channelHandleOn");
                f.setLog("LYQP", param);
                return "error";
            }
        }
    }

    /**
     * 订单查询
     */

    public String orderQuery(String orderid) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("s", "4");
        map.put("orderid", orderid);

        String url = this.getLYQPUrl(map);
        String datastr = sendPost(url);
        if ("".equals(datastr) || datastr == null || "null".equals(datastr)) {
            return "error";
        } else {
            JSONObject js = JSONObject.fromObject(datastr);
            String code = js.getJSONObject("d").get("code") + "";
            String status = js.getJSONObject("d").get("status") + "";
            boolean result  = "0".equals(code)&&"0".equals(status);
            if (!result) {
                FileLog f = new FileLog();
                Map<String, String> param = new HashMap<>();
                param.put("orderid", orderid);
                param.put("url", url);
                param.put("datastr", datastr);
                param.put("Function", "orderQuery");
                f.setLog("LYQP", param);
                return "error";
            }
        }
        return datastr;
    }

    public String getLYQPUrl(Map<String, String> map) {
        long timestamp = System.currentTimeMillis();
        String param = getParam(map);
        String key = Encrypt.MD5(api_cagent + timestamp + api_md5key);
        return api_url + "agent=" + api_cagent + "&timestamp=" + timestamp + "&param=" + param + "&key=" + key;
    }

    public String getParam(Map<String, String> map) {
        String param = "";
        StringBuffer sr = new StringBuffer("");
        Set<String> set = map.keySet();
        for (String str : set) {
            sr.append(str + "=");
            sr.append(map.get(str) + "&");
        }
        try {
            param = Encrypt.AESEncrypt(sr.toString().substring(0, sr.length() - 1), api_deskey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return param;
    }

    /**
     * 发送xml请求到server端
     * 
     * @param tagUrl
     *            请求数据地址
     * @return null发送失败，否则返回响应内容
     */
    public static String sendPost(String tagUrl) {
    	 // 创建httpclient工具对象
        HttpClient client = new HttpClient();
        client.setTimeout(40*1000);
        // 创建get请求方法
        GetMethod myGet = new GetMethod(tagUrl);
        String responseString = null;
        try {
            // 设置请求头部类型
            myGet.setRequestHeader("Content-Type", "application/json");
            myGet.setRequestHeader("charset", "utf-8");
            // 设置请求体，即xml文本内容，一种是直接获取xml内容字符串，一种是读取xml文件以流的形式
            int statusCode = client.executeMethod(myGet);
            // 只有请求成功200了，才做处理
            if (statusCode == HttpStatus.SC_OK) {
                InputStream inputStream = myGet.getResponseBodyAsStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                StringBuffer stringBuffer = new StringBuffer();
                String str = "";
                while ((str = br.readLine()) != null) {
                    stringBuffer.append(str);
                }
                responseString = stringBuffer.toString();
            } else {
                FileLog f = new FileLog();
                Map<String, String> map = new HashMap<>();
                map.put("statusCode", statusCode + "");
                map.put("ResponseBody", myGet.getResponseBodyAsString());
                map.put("tagUrl", tagUrl);
                map.put("Function", "sendPost");
                f.setLog("LYQP", map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            myGet.releaseConnection();
        }
        return responseString;
    }

    
    /**
     * 游戏上分
     */
    @Override
    public JSONObject transferIn(GameTransferVO gameTransferVO) {
        String ag_username = gameTransferVO.getAg_username();
        String billno = gameTransferVO.getBillno();
        String credit = gameTransferVO.getMoney();
        String username = gameTransferVO.getUsername();
        String type = gameTransferVO.getType();
        try {
            String msg = channelHandleOn(ag_username, billno, credit + "", "2");
            if ("success".equals(msg) || msg == "success") {
                // 转账订单提交成功
                return GameResponse.success("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
            }

            // 轮询订单
            boolean isPoll = true;
            int polls = 0;
            do {
                Thread.sleep(1500);
                logger.info("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务,第{}次轮询转账订单次数", polls);
                polls++;
                String s = orderQuery(billno);
                if (StringUtils.isBlank(s)) {
                    // 请求异常
                    if (polls > 2) {
                        return GameResponse.process("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务异常,需人工审核");
                    }
                }

                // 解析响应结果
                JSONObject jsonObject = JSONObject.fromObject(s);
                if (jsonObject.containsKey("d") && "0".equals(jsonObject.getJSONObject("d").getString("status"))) {
                    // 成功
                    return GameResponse.success("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
                }

                if (polls > 2) {
                    isPoll = false;
                }
            } while (isPoll);
            return GameResponse.faild("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务失败");
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务异常:{}", e.getMessage());
            return GameResponse.process("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务失败");
        }
    }

    
    /**
     * 游戏下分
     */
    @Override
    public JSONObject transferOut(GameTransferVO gameTransferVO) {
        String ag_username = gameTransferVO.getAg_username();
        String billno = gameTransferVO.getBillno();
        String credit = gameTransferVO.getMoney();
        String username = gameTransferVO.getUsername();
        String type = gameTransferVO.getType();
        try {
            String msg = channelHandleOn(ag_username, billno, credit + "", "3");
            if ("success".equals(msg) || msg == "success") {
                return GameResponse.success("用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
            }else {
                return GameResponse.faild("用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务异常:{}",e.getMessage());
            return GameResponse.faild("用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务失败");
        }
    }

    @Override
    public JSONObject forwardGame(GameForwardVO gameForwardVO) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JSONObject getBalance(GameBalanceVO gameBalanceVO) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JSONObject checkOrCreateAccount(GameCheckOrCreateVO gameCheckOrCreateVO) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JSONObject queryTransferOrder(GameQueryOrderVO gameQueryOrderVO) {
        // TODO Auto-generated method stub
        return null;
    }
}
