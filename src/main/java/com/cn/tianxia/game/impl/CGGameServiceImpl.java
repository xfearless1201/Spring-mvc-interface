package com.cn.tianxia.game.impl;

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
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @ClassName CGServiceImpl
 * @Description 卡卡湾88(视讯)接口实现类
 * @author Hardy
 * @Date 2019年2月9日 下午4:25:00
 * @version 1.0.0
 */
public class CGGameServiceImpl implements GameReflectService{
        private final static Logger logger = LoggerFactory.getLogger(CGGameServiceImpl.class);
		DESEncrypt d=new DESEncrypt("");
		private static  String apiurl;
		private static  String hashcode;
		private static String currency;
		public CGGameServiceImpl(Map<String, String> pmap) {
			PlatFromConfig pf=new PlatFromConfig();
			pf.InitData(pmap, "CG");
			JSONObject jo=new JSONObject().fromObject(pf.getPlatform_config());
			apiurl=jo.getString("apiurl").toString();
			hashcode=jo.getString("hashcode").toString();
			currency = jo.getString("currency").toString();
		}
		
		public String LoginGame(String username,String password){
			password=d.getMd5(password);
			String data = "{\"hashCode\":\""+hashcode+"\",\"command\":\"LOGIN\",\"params\":{\"username\":\""+username+"\",\"password\":\""+password+"\",";
	        data += "\"nickname\":\""+username+"\",\"currency\":\""+currency+"\",\"language\":\"CN\"}}"; 
	        String msg=sendPost(apiurl, data);
	        JSONObject json;
			json = JSONObject.fromObject(msg);
			if (!"null".equals(json.getString("errorMessage")) || json.getString("errorMessage") != "null") {
				FileLog f=new FileLog(); 
				Map<String,String> map =new HashMap<>();
				map.put("apiurl", apiurl);
				map.put("data", data); 
				map.put("msg", msg); 
				map.put("Function", "LoginGame");
				f.setLog("CG", map);
			}
			return msg; 
		}
		
		public String getBalance(String username,String password){ 
	    	password=d.getMd5(password);
	    	 //登录创建账号
	        String data = "{\"hashCode\":\""+hashcode+"\",\"command\":\"GET_BALANCE\",\"params\":";
	        data += "{\"username\":\""+username+"\",\"password\":\""+password+"\"}}";
	        String msg=sendPost(apiurl,data); 
	        JSONObject json;
			json = JSONObject.fromObject(msg);
			if (!"null".equals(json.getString("errorMessage")) || json.getString("errorMessage") != "null") {
				FileLog f=new FileLog(); 
				Map<String,String> map =new HashMap<>();
				map.put("apiurl", apiurl);
				map.put("data", data); 
				map.put("msg", msg); 
				map.put("Function", "getBalance");
				f.setLog("CG", map);
			}
			return msg;   
	    }
	    
		public String DEPOSIT(String username,String password,String billno,String amount){ 
			try{
		    	password=d.getMd5(password);
		    	 //登录创建账号
		    	String data = "{\"hashCode\":\""+hashcode+"\",\"command\":\"DEPOSIT\",\"params\":{\"username\":\""+username+"\",\"password\":\""+password+"\",";
		        data += "\"ref\":\""+billno+"\",\"desc\":\"DEPOSIT\",\"amount\":\""+amount+"\"}}";
		        String msg=sendPost(apiurl,data); 
		        JSONObject json;
				json = JSONObject.fromObject(msg);
				if(!"0".equals(json.getString("errorCode"))){
					FileLog f=new FileLog(); 
					Map<String,String> map =new HashMap<>();
					map.put("apiurl", apiurl);
					map.put("data", data); 
					map.put("msg", msg); 
					map.put("Function", "DEPOSIT");
					f.setLog("CG", map);
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
		    	String data = "{\"hashCode\":\""+hashcode+"\",\"command\":\"WITHDRAW\",\"params\":{\"username\":\""+username+"\",\"password\":\""+password+"\",";
		        data += "\"ref\":\""+billno+"\",\"desc\":\"withdra "+amount+"\",\"amount\":\""+amount+"\"}}";
		        String msg=sendPost(apiurl,data); 
		        JSONObject json;
				json = JSONObject.fromObject(msg);
				if(!"0".equals(json.getString("errorCode"))){
					FileLog f=new FileLog(); 
					Map<String,String> map =new HashMap<>();
					map.put("apiurl", apiurl);
					map.put("data", data); 
					map.put("msg", msg); 
					map.put("Function", "WITHDRAW");
					f.setLog("CG", map);
					return "error";
				}
				return "success";   
			}catch(Exception e){
				return "error";
			}
	    }
		
		public String CHECK_REF(String billno){  
	    	 //登录创建账号
	    	String data = "{\"hashCode\":\""+hashcode+"\",\"command\":\"CHECK_REF\",\"params\":{\"ref\":\""+billno+"\"}}";
	        String msg=sendPost(apiurl,data); 
	        if(StringUtils.isBlank(msg)){
	            logger.info("发起第三方查询订单详情无响应结果");
	            return "6617";
	        }
	        JSONObject json = JSONObject.fromObject(msg);
			String errorcode=json.getString("errorCode");   
			if("0".equals(errorcode)||"6601".equals(errorcode)||"6617".equals(errorcode)){
				FileLog f=new FileLog(); 
				Map<String,String> map =new HashMap<>();
				map.put("apiurl", apiurl);
				map.put("data", data); 
				map.put("msg", msg); 
				map.put("Function", "CHECK_REF");
				f.setLog("CG", map);
			}
			return errorcode;   
	    }
		
		/**   
	     * 发送请求到server端   
	     * @param url 请求数据地址   
	     * @param  发送的数据流   
	     * @return null发送失败，否则返回响应内容   
	     */      
		public static String sendPost(String tagUrl,String Data){        
			//System.out.println(tagUrl+Data);
	        //创建httpclient工具对象     
	        HttpClient client = new HttpClient();      
	        //创建post请求方法     
	        PostMethod myPost = new PostMethod(tagUrl);      
	        String responseString = null;      
	        try{
	        	logger.info("CG请求:"+tagUrl+JSONObject.fromObject(Data));
	            //设置请求头部类型     
	            myPost.setRequestHeader("Content-Type","application/json");    
	            myPost.setRequestHeader("charset","utf-8");    
	            myPost.setRequestBody(Data);
	            //设置请求体，即xml文本内容，一种是直接获取xml内容字符串，一种是读取xml文件以流的形式      
	            int statusCode = client.executeMethod(myPost);     
	            //只有请求成功200了，才做处理  
	            //System.out.println(statusCode);
	            if(statusCode == HttpStatus.SC_OK){       
	            	InputStream inputStream = myPost.getResponseBodyAsStream();  
	            	BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));  
	            	StringBuffer stringBuffer = new StringBuffer();  
	            	String str= "";  
	            	while((str = br.readLine()) != null){  
	            		stringBuffer.append(str );  
	            	}   
	                responseString = stringBuffer.toString();
	                logger.info("CG响应:"+responseString);   
	            }else{
	            	FileLog f=new FileLog(); 
	    			Map<String,String> map =new HashMap<>(); 
	    			map.put("statusCode", statusCode+"");
	    			map.put("ResponseBody", myPost.getResponseBodyAsString()); 
	    			map.put("tagUrl", tagUrl+Data);
	    			map.put("Function", "sendPost");
	    			f.setLog("CG", map);
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
                    return GameResponse.faild("用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
                } else {
                    //轮询
                    boolean isPoll = true;
                    int polls = 0;
                    do {
                        Thread.sleep(2000);
                        logger.info("用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务,第{}次轮询转账订单次数", polls);
                        polls++;
                        msg = CHECK_REF(billno);
                        //6601为该单据已成功,6607为处理中,2秒后再次查询该订单状态
                        if ("6601".endsWith(msg)) {
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
         * 创建或检查用户游戏账号
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
