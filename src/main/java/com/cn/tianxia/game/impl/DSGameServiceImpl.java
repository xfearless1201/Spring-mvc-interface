package com.cn.tianxia.game.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.http.HttpStatus;
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
 * @ClassName DSServiceImpl
 * @Description DS视讯
 * @author Hardy
 * @Date 2019年2月9日 下午4:27:08
 * @version 1.0.0
 */
public class DSGameServiceImpl implements GameReflectService{
    
    private static final Logger logger = LoggerFactory.getLogger(DSGameServiceImpl.class);
    
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");  
    /*String url = "http://dsapitest.iasia999.com:81/dsapi/app/api.do";
    String dskey="tx3_63bfceea-009d-40b6-aaa6-708dfe8e";*/
    DESEncrypt d=new DESEncrypt("");
    Calendar Cal=Calendar.getInstance();   
    private static  String url;
	private static  String dskey;
    public DSGameServiceImpl(Map<String, String> pmap) {
		PlatFromConfig pf=new PlatFromConfig();
		pf.InitData(pmap, "DS");
		JSONObject jo=new JSONObject().fromObject(pf.getPlatform_config());
		url=jo.getString("url").toString();
		dskey=jo.getString("dskey").toString();
    }
    
     
	public String LoginGame(String username,String password){ 
    	password=d.getMd5(password);
    	 //登录创建账号
        String data = "{\"hashCode\":\""+dskey+"\",\"command\":\"LOGIN\",\"params\":";
        data += "{\"username\":\""+username+"\",\"password\":\""+password+"\",\"nickname\":\""+username+"\",\"currency\":\"CNY\",\"language\":\"CN\",\"line\":1}}";
        String msg=sendPost(url,data); 
        JSONObject json;
		json = JSONObject.fromObject(msg);
		if (!"null".equals(json.getString("errorMessage")) || json.getString("errorMessage") != "null") {
			FileLog f=new FileLog(); 
			Map<String,String> map =new HashMap<>();
			map.put("apiurl", url);
			map.put("data", data); 
			map.put("msg", msg); 
			map.put("Function", "LoginGame");
			f.setLog("DS", map);
		}
		return msg;   
    }
     
	public String getBalance(String username,String password){ 
    	password=d.getMd5(password);
    	 //登录创建账号
        String data = "{\"hashCode\":\""+dskey+"\",\"command\":\"GET_BALANCE\",\"params\":";
        data += "{\"username\":\""+username+"\",\"password\":\""+password+"\"}}";
        String msg=sendPost(url,data); 
        JSONObject json;
		json = JSONObject.fromObject(msg);
		if (!"null".equals(json.getString("errorMessage")) || json.getString("errorMessage") != "null") {
			FileLog f=new FileLog(); 
			Map<String,String> map =new HashMap<>();
			map.put("apiurl", url);
			map.put("data", data); 
			map.put("msg", msg); 
			map.put("Function", "getBalance");
			f.setLog("DS", map);
		}
		return msg;   
    }
     
	public String DEPOSIT(String username,String password,String billno,String amount){ 
		try{
	    	password=d.getMd5(password);
	    	 //登录创建账号
	    	String data = "{\"hashCode\":\""+dskey+"\",\"command\":\"DEPOSIT\",\"params\":{\"username\":\""+username+"\",\"password\":\""+password+"\",";
	        data += "\"ref\":\""+billno+"\",\"desc\":\"\",\"amount\":\""+amount+"\"}}";
	        String msg=sendPost(url,data); 
	        JSONObject json;
			json = JSONObject.fromObject(msg);
			if(!"0".equals(json.getString("errorCode"))){
				FileLog f=new FileLog(); 
				Map<String,String> map =new HashMap<>();
				map.put("apiurl", url);
				map.put("data", data); 
				map.put("msg", msg); 
				map.put("Function", "DEPOSIT");
				f.setLog("DS", map);
				return "error";
			}
			return "success";   
		}catch(Exception e){
			return "error";
		}
    }
     
	public String WITHDRAW(String username,String password,String billno,String amount){ 
		try{
	    	password=d.getMd5(password);
	    	 //登录创建账号
	    	String data = "{\"hashCode\":\""+dskey+"\",\"command\":\"WITHDRAW\",\"params\":{\"username\":\""+username+"\",\"password\":\""+password+"\",";
	        data += "\"ref\":\""+billno+"\",\"desc\":\"\",\"amount\":\""+amount+"\"}}";
	        String msg=sendPost(url,data); 
	        JSONObject json;
			json = JSONObject.fromObject(msg);
			if(!"0".equals(json.getString("errorCode"))){
				FileLog f=new FileLog(); 
				Map<String,String> map =new HashMap<>();
				map.put("apiurl", url);
				map.put("data", data); 
				map.put("msg", data); 
				map.put("Function", "WITHDRAW");
				f.setLog("DS", map);
				return "error";
			}
			return "success";   
		}catch(Exception e){
			return "error";
		}
    }
	
	public String CHECK_REF(String billno){  
   	 //登录创建账号
   	String data = "{\"hashCode\":\""+dskey+"\",\"command\":\"CHECK_REF\",\"params\":{\"ref\":\""+billno+"\"}}";
       String msg=sendPost(url,data); 
       JSONObject json;
		json = JSONObject.fromObject(msg);
		String errorcode=json.getString("errorCode");  
		Map<String,String> map =new HashMap<>();
		FileLog f=new FileLog(); 
		if("0".equals(errorcode)||"6601".equals(errorcode)||"6617".equals(errorcode)){
			map.put("apiurl", url);
			map.put("data", data); 
			map.put("errorcode", errorcode);
			map.put("msg", msg); 
			map.put("Function", "CHECK_REF");
		}
		else
		{
		    map.put("apiurl", url);
            map.put("data", data); 
            map.put("errorcode", errorcode);
            map.put("msg", msg); 
            map.put("Function", "CHECK_REF");
        }
		f.setLog("DS", map);
		return errorcode;   
   }
    
    /**   
     * 发送xml请求到server端   
     * @param url xml请求数据地址   
     * @param xmlString 发送的xml数据流   
     * @return null发送失败，否则返回响应内容   
     */      
    public static String sendPost(String tagUrl,String Data){         
    	//System.out.println(Data);
        //创建httpclient工具对象     
        HttpClient client = new HttpClient();      
        //创建post请求方法     
        PostMethod myPost = new PostMethod(tagUrl);      
        String responseString = null;      
        try{      
            //设置请求头部类型     
            myPost.setRequestHeader("Content-Type","application/json");    
            myPost.setRequestHeader("charset","utf-8");    
            myPost.setRequestBody(Data);
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
    			f.setLog("DS", map);
            }      
        }catch (Exception e) {   
            e.printStackTrace();      
        }finally{  
             myPost.releaseConnection();   
        }   
        return responseString;      
    }

    /**
     * 游戏上分
     */
    @Override
    public JSONObject transferIn(GameTransferVO gameTransferVO) {
        String ag_username = gameTransferVO.getAg_username();
        String ag_password = gameTransferVO.getPassword();
        String billno = gameTransferVO.getBillno();
        String credit = gameTransferVO.getMoney();
        String username = gameTransferVO.getUsername();
        String type = gameTransferVO.getType();
        try {
            String msg = DEPOSIT(ag_username, ag_password, billno, credit + "");
            if ("success".equalsIgnoreCase(msg)) {
                // 转账成功
                return GameResponse.success("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
            }

            // 轮询订单
            boolean isPoll = true;
            int polls = 0;
            do {
                // 休眠2秒
                Thread.sleep(2000);
                logger.info("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务,第{}次轮询转账订单次数", polls);
                polls++;
                // 查询订单
                msg = CHECK_REF(billno);
                // 6601为该单据已成功,6617为处理中,2秒后再次查询该订单状态
                if ("6601".endsWith(msg)) {
                    return GameResponse.success("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
                } else if ("6617".endsWith(msg)) {
                    if (polls > 2) {
                        // 异常订单,需要人工审核
                        return GameResponse.process("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务异常");
                    }
                } else {
                    if (polls > 2) {
                        isPoll = false;
                    }
                }
            } while (isPoll);
            // 转账失败
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
        String ag_password = gameTransferVO.getPassword();
        String billno = gameTransferVO.getBillno();
        String credit = gameTransferVO.getMoney();
        String username = gameTransferVO.getUsername();
        String type = gameTransferVO.getType();
        try {
            String msg = WITHDRAW(ag_username, ag_password, billno, credit + "");
            if ("success".equals(msg)) {
                return GameResponse.success("用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
            } else {
                // 轮询订单
                boolean isPoll = true;
                int polls = 0;
                do {
                    // 休眠2秒
                    Thread.sleep(2000);
                    logger.info("用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务,第{}次轮询转账订单次数", polls);
                    polls++;
                    msg = CHECK_REF(billno);
                    // 6601为该单据已成功,6607为处理中,2秒后再次查询该订单状态
                    if ("6601".endsWith(msg)) {
                        // 单据成功
                        return GameResponse.success("用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
                    }else {
                        if (polls > 2) {
                            isPoll = false;
                        }
                    }
                } while (isPoll);
            }
            // 订单处理失败
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
