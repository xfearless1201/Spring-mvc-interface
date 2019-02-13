package com.cn.tianxia.game.impl;
 
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.http.HttpStatus;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * 功能概要：AGService实现类
 *  
 */ 
public class AGGameServiceImpl implements GameReflectService{ 
	/*private static String api_url = "http://gi.tianxgame.com:81/doBusiness.do?";
	private static String api_url_game = "http://gci.tianxgame.com:81/forwardGame.do?";
	private static String api_deskey="uR7R44Ni";
	private static String api_md5key="8XSW0SVZPp0X";
	private static String api_cagent="S76_AGIN" */
	
	private static String api_url ;
	private static String api_url_game ;
	private static String api_deskey;
	private static String api_md5key;
	private static String api_cagent;
	private static String actype;

    private final static Logger logger = LoggerFactory.getLogger(AGGameServiceImpl.class);
	
	public AGGameServiceImpl(Map<String, String> pmap) {
		PlatFromConfig pf=new PlatFromConfig();
		pf.InitData(pmap, "AG");
		JSONObject jo=new JSONObject().fromObject(pf.getPlatform_config());
		api_url=jo.getString("api_url").toString();
		api_url_game=jo.getString("api_url_game").toString();
		api_deskey=jo.getString("api_deskey").toString();
		api_md5key=jo.getString("api_md5key").toString();
		api_cagent=jo.getString("api_cagent").toString();
		actype=jo.getString("actype").toString();
	}
	
	
	/**
	 * 检测并创建游戏账号
	 */
	
	public String CheckOrCreateGameAccout(String loginname, String password,
			String oddtype, String cur) {  
		String xmlString=""; 
		Document doc = null;
		xmlString="cagent="+api_cagent+"/\\\\/loginname="+loginname+"/\\\\/method=lg/\\\\/actype="+actype
				+"/\\\\/password="+password+"/\\\\/oddtype="+oddtype+"/\\\\/cur="+cur;
		String tagUrl=getAGUrl(api_url,xmlString);
        logger.info("AG【检测并创建游戏账号】请求参数==========>"+tagUrl);
		xmlString=sendPost(api_cagent, tagUrl);
		String info="";
		String msg="";
		try {
			doc = DocumentHelper.parseText(xmlString);
			Element root = doc.getRootElement();
			info=root.attributeValue("info"); 
			msg=root.attributeValue("msg"); 
		} catch (DocumentException e) { 
			e.printStackTrace();
		}
        logger.info("AG【检测并创建游戏账号】响应参数<=========="+info);
		if("error".equals(info)){
			FileLog f=new FileLog(); 
			Map<String,String> map =new HashMap<>();
			map.put("tagUrl", "tagUrl"); 
			map.put("loginname", loginname);
			map.put("actype", actype);
			map.put("oddtype", oddtype);
			map.put("msg", xmlString);
			map.put("Function", "CheckOrCreateGameAccout");
			f.setLog(api_cagent, map);
			return msg;
		}else{
			return info;			
		}
	}
	/**
	 * 查询余额
	 */
	
	public String GetBalance(String loginname, String password,String cur) { 
		String xmlString=""; 
		Document doc = null;
		xmlString="cagent="+api_cagent+"/\\\\/loginname="+loginname+"/\\\\/method=gb/\\\\/actype="+actype
				+"/\\\\/password="+password+"/\\\\/cur="+cur;
		String tagUrl=getAGUrl(api_url,xmlString);
        logger.info("AG【查询余额】请求参数==========>"+tagUrl);
		xmlString=sendPost(api_cagent, tagUrl);
		String info="";
		String msg="";
		try {
			doc = DocumentHelper.parseText(xmlString);
			Element root = doc.getRootElement();
			info=root.attributeValue("info"); 
			msg=root.attributeValue("msg"); 
		} catch (DocumentException e) { 
			e.printStackTrace();
		}
        logger.info("AG【查询余额】响应参数<=========="+info);
		if("error".equals(info)){
			FileLog f=new FileLog(); 
			Map<String,String> map =new HashMap<>();
			map.put("tagUrl", "tagUrl"); 
			map.put("loginname", loginname);
			map.put("actype", actype);
			map.put("msg", msg);
			map.put("Function", "GetBalance");
			f.setLog(api_cagent, map);
			return msg;
		}else{
			return info;			
		}
	}
	/**
	 * 预备转账
	 */
	
	public String PrepareTransferCredit(String loginname, String billno, String type,
			String credit,  String password, String cur) { 
		String xmlString=""; 
		Document doc = null;
		xmlString="cagent="+api_cagent+"/\\\\/method=tc/\\\\/loginname="+loginname+"/\\\\/billno="+billno
				+ "/\\\\/type="+type+"/\\\\/credit="+credit+"/\\\\/actype="+actype+"/\\\\/password="+password+"/\\\\/cur="+cur;
		String tagUrl=getAGUrl(api_url,xmlString);
        logger.info("AG【预备转账】请求参数==========>"+tagUrl);
		xmlString=sendPost(api_cagent, tagUrl);
		String info="";
		String msg="";
		try {
			doc = DocumentHelper.parseText(xmlString);
			Element root = doc.getRootElement();
			info=root.attributeValue("info"); 
			msg=root.attributeValue("msg"); 
		} catch (DocumentException e) { 
			//e.printStackTrace();
			return "error";
		}
        logger.info("AG【预备转账】响应参数<=========="+info);
		if(!"0".equals(info)){
			FileLog f=new FileLog(); 
			Map<String,String> map =new HashMap<>();
			map.put("tagUrl", "tagUrl"); 
			map.put("loginname", loginname);
			map.put("actype", actype);
			map.put("billno", billno);
			map.put("type", type);
			map.put("credit", credit);
			map.put("msg", xmlString);
			map.put("Function", "PrepareTransferCredit");
			f.setLog(api_cagent, map);
			return msg;
		}else{
			return info;			
		}
	}
	/**
	 * 确认转账
	 */
	
	public String TransferCreditConfirm(String loginname, String billno, String type,
			String credit,  String flag, String password, String cur) { 
		String xmlString=""; 
		Document doc = null;
		xmlString="cagent="+api_cagent+"/\\\\/loginname="+loginname+"/\\\\/method=tcc/\\\\/billno="+billno
				+ "/\\\\/type="+type+"/\\\\/credit="+credit+"/\\\\/actype="+actype+"/\\\\/flag="+flag+"/\\\\/password="+password+"/\\\\/cur="+cur;
		String tagUrl=getAGUrl(api_url,xmlString);
        logger.info("AG【确认转账】请求参数==========>"+tagUrl);
		xmlString=sendPost(api_cagent, tagUrl);
		String info="";
		String msg="";
		try {
			doc = DocumentHelper.parseText(xmlString);
			Element root = doc.getRootElement();
			info=root.attributeValue("info"); 
			msg=root.attributeValue("msg"); 
		} catch (DocumentException e) { 
			//e.printStackTrace();
			return "error";
		}
        logger.info("AG【确认转账】响应参数<=========="+info);
		if(!"0".equals(info)){
			FileLog f=new FileLog(); 
			Map<String,String> map =new HashMap<>();
			map.put("tagUrl", "tagUrl"); 
			map.put("loginname", loginname);
			map.put("actype", actype);
			map.put("billno", billno);
			map.put("type", type);
			map.put("credit", credit);
			map.put("msg", xmlString);
			map.put("Function", "TransferCreditConfirm");
			f.setLog(api_cagent, map);
			return msg;
		}else{
			return info;			
		}
	}
	/**
	 * 检查订单状态
	 */
	
	public String QueryOrderStatus(String billno,  String cur) {
		String xmlString=""; 
		Document doc = null;
		xmlString="cagent="+api_cagent+"/\\\\/billno="+billno+"/\\\\/method=qos"+
				 "/\\\\/actype="+actype+"/\\\\/cur="+cur;
		String tagUrl=getAGUrl(api_url,xmlString);
        logger.info("AG【检查订单状态】请求参数==========>"+tagUrl);
		xmlString=sendPost(api_cagent, tagUrl);
		String info="";
		String msg="";
		try {
			doc = DocumentHelper.parseText(xmlString);
			Element root = doc.getRootElement();
			info=root.attributeValue("info"); 
			msg=root.attributeValue("msg"); 
		} catch (DocumentException e) { 
			e.printStackTrace();
			return "1";
		}
        logger.info("AG【检查订单状态】响应参数<=========="+info);
		if("error".equals(info)){
			FileLog f=new FileLog(); 
			Map<String,String> map =new HashMap<>(); 
			map.put("actype", actype);
			map.put("billno", billno); 
			map.put("msg", msg);
			map.put("Function", "QueryOrderStatus");
			f.setLog(api_cagent, map);
			return msg; 
		}else{
			return info;			
		}
	}
	/**
	 * 获取游戏跳转连接
	 */
	
	public String forwardGame(String loginname, String password, String dm, String sid, String gameType,String handicap) { 
		String xmlString="";  
		xmlString="cagent="+api_cagent+"/\\\\/loginname="+loginname+"/\\\\/actype="+actype+"/\\\\/password="+password
			+"/\\\\/dm="+dm+"/\\\\/sid="+sid+"/\\\\/lang=1/\\\\/gameType="+gameType+"/\\\\/oddtype="+handicap+"/\\\\/cur=CNY";
        logger.info("AG【获取游戏跳转连接】请求参数==========>"+xmlString);
		xmlString=getAGUrl(api_url_game, xmlString);
        logger.info("AG【获取游戏跳转连接】响应参数<=========="+xmlString);
		return xmlString;
	}
	
	/**
	 * 获取游戏跳转连接
	 */
	
	public String forwardMobileGame(String loginname, String password, String dm, String sid, String gameType,String handicap) { 
		String xmlString="";  
		UUID uuid = UUID.randomUUID();
		xmlString="cagent="+api_cagent+"/\\\\/loginname="+loginname+"/\\\\/actype="+actype+"/\\\\/password="+password
			+"/\\\\/dm="+dm+"/\\\\/sid="+sid+"/\\\\/lang=1/\\\\/gameType="+gameType+"/\\\\/oddtype="+handicap+"/\\\\/cur=CNY/\\\\/mh5=y/\\\\/session_token="+uuid.toString();
        logger.info("AG【获取游戏跳转连接】请求参数==========>"+xmlString);
		xmlString=getAGUrl(api_url_game, xmlString);
        logger.info("AG【获取游戏跳转连接】响应参数<=========="+xmlString);
		return xmlString;
	}
	
	
	/**   
     * 发送xml请求到server端   
     * @param url xml请求数据地址   
     * @param xmlString 发送的xml数据流   
     * @return null发送失败，否则返回响应内容   
     */      
	public static String sendPost(String gtype,String tagUrl){        
        //创建httpclient工具对象     
        HttpClient client = new HttpClient();      
        //创建post请求方法     
        PostMethod myPost = new PostMethod(tagUrl);      
        myPost.addRequestHeader("User-Agent", "WEB_LIB_GI_"+gtype); 
        //设置请求超时时间     
        client.setConnectionTimeout(40*1000);    
        client.setTimeout(40*1000);
        String responseString = null;      
        try{      
            //设置请求头部类型     
            myPost.setRequestHeader("Content-Type","text/xml");    
            myPost.setRequestHeader("charset","utf-8");    
            //设置请求体，即xml文本内容，一种是直接获取xml内容字符串，一种是读取xml文件以流的形式      
            int statusCode = client.executeMethod(myPost);     
            //只有请求成功200了，才做处理  
            if(statusCode == HttpStatus.SC_OK){       
            	InputStream inputStream = myPost.getResponseBodyAsStream();  
            	BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));  
            	StringBuffer stringBuffer = new StringBuffer();  
            	String str= "";  
            	while((str = br.readLine()) != null){  
            	stringBuffer.append(str );  
            	}   
                responseString = stringBuffer.toString();
            }else{
            	FileLog f=new FileLog(); 
    			Map<String,String> map =new HashMap<>(); 
    			map.put("statusCode", statusCode+"");
    			map.put("ResponseBody", myPost.getResponseBodyAsString()); 
    			map.put("tagUrl", tagUrl);
    			map.put("Function", "sendPost");
    			f.setLog(gtype, map);
            }
        }catch (Exception e) {   
            e.printStackTrace();      
        }finally{  
             myPost.releaseConnection();   
        }  
        return responseString;      
    }
	
	public static String getAGUrl(String url,String xmlString){
		String param = "";
		String tagUrl = "";
		String key = "";
		DESEncrypt d = new DESEncrypt(api_deskey);
		try {
			param=d.encrypt(xmlString);
			key=d.getMd5(param+api_md5key);
		} catch (Exception e1) { 
			e1.printStackTrace();
		}
		tagUrl=url + "params=" + param + "&key=" + key; 
		return tagUrl;
	}

	
	/**
	 * 游戏上分
	 */
    @Override
    public JSONObject transferIn(GameTransferVO gameTransferVO) {
        String username = gameTransferVO.getUsername();// 用户名称
        String ag_username = gameTransferVO.getAg_username();// 游戏登录账号
        String billno = gameTransferVO.getBillno();// 订单号
        String credit = gameTransferVO.getMoney();// 订单金额
        String ag_password = gameTransferVO.getPassword();// 登录密码
        String type = gameTransferVO.getType();// 游戏平台编码
        try {
            // 调用预转账
            String msg = PrepareTransferCredit(ag_username, billno, "IN", credit + "", ag_password,
                    "CNY");
            if (!"0".equals(msg)) {
                return GameResponse.faild("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务失败");
            }
            // 预转账失败
            boolean tflag = true;
            boolean bflag = false;
            do {
                // 调用确认转账
                msg = TransferCreditConfirm(ag_username, billno, "IN", credit + "", "1", ag_password,
                        "CNY");
                if (!"0".equals(msg)) {
                    int counts = 0;
                    do {
                        Thread.sleep(3000);
                        logger.info("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务,第{}次轮询转账订单次数", counts);
                        counts++;
                        // 查询转账订单
                        msg = QueryOrderStatus(billno, "CNY");
                        if ("0".equals(msg)) {
                            // 转账订单处理成功
                            return GameResponse.success("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
                        } else if ("1".equals(msg)) {
                            // 查询成功订单待处理,需再次调用确认转账
                            if (counts > 2) {
                                tflag = false;
                            }
                        } else {
                            bflag = true;
                            if (counts > 2) {
                                bflag = false;
                                // 异常订单,未收到任何回馈的
                                return GameResponse.process("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务异常,需人工审核");
                            }
                        }
                    } while (bflag);
                }
                // 订单处理成功
                tflag = false;
            } while (tflag);
            // 处理转账订单业务
            if (bflag) {
                return GameResponse.faild("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务失败");
            } else {
                // 转账成功
                return GameResponse.faild("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
            }
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
        String username = gameTransferVO.getUsername();// 用户名称
        String ag_username = gameTransferVO.getAg_username();// 游戏登录账号
        String billno = gameTransferVO.getBillno();// 订单号
        String credit = gameTransferVO.getMoney();// 订单金额
        String ag_password = gameTransferVO.getPassword();// 登录密码
        String type = gameTransferVO.getType();// 游戏平台编码
        try {
            // 调用预转账
            String msg = PrepareTransferCredit(ag_username, billno, "OUT", credit + "", ag_password,
                    "CNY");
            if (!"0".equals(msg)) {
                return GameResponse.faild("用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务失败");
            }
            // 预转账失败
            boolean tflag = true;
            boolean bflag = false;
            do {
                // 调用确认转账
                msg = TransferCreditConfirm(ag_username, billno, "OUT", credit + "", "1", ag_password,
                        "CNY");
                if (!"0".equals(msg)) {
                    int counts = 0;
                    do {
                        Thread.sleep(1500);
                        logger.info("用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务,第{}次轮询转账订单次数", counts);
                        counts++;
                        // 查询转账订单
                        msg = QueryOrderStatus(billno, "CNY");
                        if ("0".equals(msg)) {
                            // 转账订单处理成功
                            return GameResponse.success("用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
                        } else if ("1".equals(msg)) {
                            // 查询成功订单待处理,需再次调用确认转账
                            if (counts > 2) {
                                tflag = false;
                            }
                        } else {
                            bflag = true;
                            if (counts > 2) {
                                bflag = false;
                                // 异常订单,未收到任何回馈的
                                return GameResponse.process("用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务异常,需人工审核");
                            }
                        }
                    } while (bflag);
                }
                // 订单处理成功
                tflag = false;
            } while (tflag);
            // 处理转账订单业务
            if (bflag) {
                return GameResponse.faild("用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务失败");
            } else {
                // 转账成功
                return GameResponse.success("用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
            }
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
        String ag_username = gameForwardVO.getAg_username();
        String ag_password = gameForwardVO.getPassword();
        String gameID = gameForwardVO.getGameId();
        String ip = gameForwardVO.getIp();
        String handicap = gameForwardVO.getHandicap();
        String sid = gameForwardVO.getSid();
        String model = gameForwardVO.getModel();
        try {
            if ("mobile".equals(model)) {
                String url = forwardMobileGame(ag_username, ag_password, ip, sid, "11", handicap);
                return GameResponse.form(url);
            } else {
                String url = forwardGame(ag_username, ag_password, ip, sid, "0", handicap);
                return GameResponse.form(url);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("获取游戏跳转链接异常:{}",e.getMessage());
            return GameResponse.faild("获取游戏跳转链接失败");
        }
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
