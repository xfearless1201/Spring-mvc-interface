package com.cn.tianxia.game.impl;
 
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.cn.tianxia.game.GameReflectService;
import com.cn.tianxia.po.v2.GameResponse;
import com.cn.tianxia.util.DESEncrypt;
import com.cn.tianxia.util.FileLog;
import com.cn.tianxia.util.PlatFromConfig;
import com.cn.tianxia.vo.v2.GameBalanceVO;
import com.cn.tianxia.vo.v2.GameCheckOrCreateVO;
import com.cn.tianxia.vo.v2.GameForwardVO;
import com.cn.tianxia.vo.v2.GameQueryOrderVO;
import com.cn.tianxia.vo.v2.GameTransferVO;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName GGBYServiceImpl
 * @Description GGBY游戏接口
 * @author Hardy
 * @Date 2019年2月9日 下午4:27:50
 * @version 1.0.0
 */
public class GGBYGameServiceImpl implements GameReflectService{
    //日志
	private static final Logger logger = LoggerFactory.getLogger(GGBYGameServiceImpl.class);
	
	private static String api_url ;
	private static String api_deskey;
	private static String api_md5key;
	private static String cagent;
	private static String actype;
	private static String ishttps="1";//正式环境
	
	public GGBYGameServiceImpl(Map<String, String> pmap) {
		PlatFromConfig pf=new PlatFromConfig();
		pf.InitData(pmap, "GGBY");
		JSONObject jo=new JSONObject().fromObject(pf.getPlatform_config());
		api_url=jo.getString("api_url").toString();
		api_deskey=jo.getString("api_deskey").toString();
		api_md5key=jo.getString("api_md5key").toString();
		cagent=jo.getString("cagent").toString();
		actype=jo.getString("actype").toString();
		
		if(jo.containsKey("ishttps")){
			ishttps="0";//测试环境
		}
	}
	/**
	 * 检测并创建游戏账号
	 */
	
	public String CheckOrCreateGameAccout(String loginname,String password) {  
		String xmlString=""; 
		Document doc = null;
		xmlString="cagent="+cagent+"/\\\\/loginname="+loginname+"/\\\\/method=ca/\\\\/actype="+actype
				+"/\\\\/password="+password+"/\\\\/cur=CNY";
		String tagUrl=getAGUrl(api_url,xmlString);
		xmlString=sendGet( tagUrl);
		String info="";
		String msg="";
		JSONObject json=new JSONObject().fromObject(xmlString); 
			info=json.getString("code");
			msg=json.getString("msg");
		if(!"0".equals(info)){
			FileLog f=new FileLog(); 
			Map<String,String> map =new HashMap<>();
			map.put("tagUrl", tagUrl); 
			map.put("msg", json.toString());
			map.put("Function", "CheckOrCreateGameAccout");
			f.setLog("GGBY", map);
			return msg;
		}else{
			return info;			
		}
	}
	/**
	 * 查询余额
	 */
	
	public String GetBalance(String loginname, String password) { 
		String xmlString=""; 
		Document doc = null;
		xmlString="cagent="+cagent+"/\\\\/loginname="+loginname+"/\\\\/method=gb/\\\\/password="+password+"/\\\\/cur=CNY";
		String tagUrl=getAGUrl(api_url,xmlString);
		xmlString=sendGet(tagUrl);
		String info="";
		String msg="";
		String balance=""; 
		JSONObject json=new JSONObject().fromObject(xmlString); 
		info=json.getString("code");
		msg=json.getString("msg");
		if(!"0".equals(info)){
			FileLog f=new FileLog(); 
			Map<String,String> map =new HashMap<>();
			map.put("tagUrl", tagUrl); 
			map.put("msg", json.toString());
			map.put("Function", "GetBalance");
			f.setLog("GGBY", map);
			return "维护中";
		}else{
			balance=json.getString("dbalance");
			return balance;			
		}
	}
	/**
	 * 转账
	 */
	
	public String TransferCredit( String loginname, String billno, String credit, String type, String password,String ip) { 
		try{
			String xmlString=""; 
			Document doc = null;
			xmlString="cagent="+cagent+"/\\\\/method=tc/\\\\/loginname="+loginname+"/\\\\/billno="+cagent+billno
					+ "/\\\\/type="+type+"/\\\\/credit="+credit+"/\\\\/password="+password+"/\\\\/cur=CNY/\\\\/ip=="+ip;
			String tagUrl=getAGUrl(api_url,xmlString);
			xmlString=sendGet(tagUrl);
			String info="";
			String msg="";
			JSONObject json=new JSONObject().fromObject(xmlString); 
				info=json.getString("code");
				msg=json.getString("msg");
			if(!"0".equals(info)){
				FileLog f=new FileLog(); 
				Map<String,String> map =new HashMap<>();
				map.put("tagUrl", tagUrl); 
				map.put("msg", json.toString());
				map.put("Function", "TransferCredit");
				f.setLog("GGBY", map);
				return "error";
			}
			return "success";   
		}catch(Exception e){
			return "error";
		}
	}
	/**
	 * 检查订单状态
	 */
	
	public String QueryOrderStatus(String billno) {
		try{ 
			String xmlString=""; 
			Document doc = null;
			xmlString="cagent="+cagent+"/\\\\/method=qx/\\\\/billno="+billno;
			String tagUrl=getAGUrl(api_url,xmlString);
			xmlString=sendGet( tagUrl);
			String info="";
			String msg="";
			JSONObject json=new JSONObject().fromObject(xmlString); 
				info=json.getString("code");
				msg=json.getString("msg");
			if(!"0".equals(info)){
				FileLog f=new FileLog(); 
				Map<String,String> map =new HashMap<>();  
				map.put("tagUrl", tagUrl); 
				map.put("msg", json.toString());
				map.put("Function", "QueryOrderStatus");
				f.setLog("GGBY", map); 
			}
			return info;			
		}catch(Exception e){
			return "-1";
		}
		
	}
	/**
	 * 获取游戏跳转连接
	 */
	
	public String forwardGame(String loginname, String password, String sid, String ip) { 
		String xmlString=""; 
		Document doc = null;
		xmlString="cagent="+cagent+"/\\\\/loginname="+loginname+"/\\\\/password="+password
			+"/\\\\/method=fw/\\\\/sid="+sid+"/\\\\/lang=zh-CN/\\\\/gametype=0/\\\\/ip="+ip+"/\\\\/ishttps="+ishttps; 
		String tagUrl=getAGUrl(api_url,xmlString); 
		xmlString=sendGet( tagUrl); 
		String info="";
		String msg="";
		String url="";
		JSONObject json=new JSONObject().fromObject(xmlString); 
			info=json.getString("code");
			msg=json.getString("msg");
		if(!"0".equals(info)){
			FileLog f=new FileLog(); 
			Map<String,String> map =new HashMap<>(); 
			map.put("tagUrl", tagUrl); 
			map.put("msg", json.toString());
			map.put("Function", "forwardGame");
			f.setLog("GGBY", map);
			return msg;
		}else{
			url=json.getString("url");
			return url;			
		}
	}
	
	
	/**   
     * 发送xml请求到server端   
     * @param url xml请求数据地址   
     * @param xmlString 发送的xml数据流   
     * @return null发送失败，否则返回响应内容   
     */      
	public static String sendGet(String tagUrl){        
		URL url = null;
		HttpURLConnection httpConn = null;
		InputStream in = null;
		String responseString ="";
		try {
			url = new URL(tagUrl);
			httpConn = (HttpURLConnection) url.openConnection();
			HttpURLConnection.setFollowRedirects(true);
			httpConn.setConnectTimeout(30000);
			httpConn.setReadTimeout(30000);
			httpConn.setRequestMethod("GET");
			httpConn.setRequestProperty("GGaming", "WEB_GG_GI_" + cagent);// cagent请参考上线说明,文件头为必传
			in = httpConn.getInputStream();
			
			BufferedReader br = new BufferedReader(new InputStreamReader(in));  
        	StringBuffer stringBuffer = new StringBuffer();  
        	String str= "";  
        	while((str = br.readLine()) != null){  
        	stringBuffer.append(str );  
        	}   
            responseString = stringBuffer.toString();
			return responseString;
		} catch (Exception e) {
			FileLog f=new FileLog(); 
			Map<String,String> map =new HashMap<>(); 
			map.put("tagUrl", tagUrl); 
			map.put("msg", e.getMessage());
			map.put("Function", "sendGet");
			f.setLog("GGBY", map);  
			e.printStackTrace();
		} finally {
			try {
				httpConn.disconnect();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return responseString;
		}
    }
	
	public static String getAGUrl(String url,String xmlString){
		String param = "";
		String tagUrl = "";
		String key = "";
		//System.out.println(url + "params="+xmlString);
		DESEncrypt d = new DESEncrypt(api_deskey);
		try {
			param=d.encrypt(xmlString);
			key=d.getMd5(param+api_md5key);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		tagUrl=url + "params=" + param + "&key=" + key;
		//System.out.println("-----------GetURL------------"); 
		return tagUrl;
	}
	
	
	
	/**
	 * 游戏上分
	 */
    @Override
    public JSONObject transferIn(GameTransferVO gameTransferVO) {
        String ag_username = gameTransferVO.getAg_username();
        String billno = gameTransferVO.getBillno();
        String credit = gameTransferVO.getMoney();
        String ag_password = gameTransferVO.getPassword();
        String ip = gameTransferVO.getIp();
        String username = gameTransferVO.getUsername();
        String type = gameTransferVO.getType();
        try {
            String msg = TransferCredit(ag_username, billno, credit + "", "IN", ag_password, ip);
            if ("success".equalsIgnoreCase(msg)) {
                // 成功
                return GameResponse.success("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
            }
            // 轮询订单
            boolean isPoll = true;
            int polls = 0;
            do {
                Thread.sleep(2000);
                logger.info("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务,第{}次轮询转账订单次数", polls);
                polls++;
                msg = QueryOrderStatus(billno);
                if ("0".equals(msg)) {
                    // 0为该单据已成功
                    return GameResponse.success("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
                } else if ("-1".equals(msg)) {
                    // 异常订单
                    if (polls > 2) {
                        return GameResponse.process("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务异常,需人工审核");
                    }
                } else {
                    if (polls > 2) {
                        isPoll = false;
                    }
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
        String ag_password = gameTransferVO.getPassword();
        String ip = gameTransferVO.getIp();
        String username = gameTransferVO.getUsername();
        String type = gameTransferVO.getType();
        try {
            String msg = TransferCredit(ag_username, billno, credit + "", "OUT", ag_password, ip);
            if ("success".equals(msg)) {
                return GameResponse.success("用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
            } else {

                //轮询
                boolean isPoll = true;
                int polls = 0;
                do {
                    Thread.sleep(2000);
                    logger.info("用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务,第{}次轮询转账订单次数", polls);
                    polls++;
                    msg = QueryOrderStatus(billno);
                    if ("0".endsWith(msg)) {
                        return GameResponse.success("用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
                    } else {
                        if(polls > 2){
                            isPoll = false;
                        }
                    }
                } while (isPoll);
            }
            return GameResponse.faild("用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务失败");
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务异常:{}",e.getMessage());
            return GameResponse.faild("用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务失败");
        }
    }
    
    /**
     * 跳转游戏
     */
    @Override
    public JSONObject forwardGame(GameForwardVO gameForwardVO) {
        // TODO Auto-generated method stub
        return null;
    }
    
    /**
     * 获取游戏余额
     */
    @Override
    public JSONObject getBalance(GameBalanceVO gameBalanceVO) {
        // TODO Auto-generated method stub
        return null;
    }
    
    /**
     * 检查或创建游戏账号
     */
    @Override
    public JSONObject checkOrCreateAccount(GameCheckOrCreateVO gameCheckOrCreateVO) {
        // TODO Auto-generated method stub
        return null;
    }
    
    /**
     * 查询游戏转账订单
     */
    @Override
    public JSONObject queryTransferOrder(GameQueryOrderVO gameQueryOrderVO) {
        // TODO Auto-generated method stub
        return null;
    }
 

}
