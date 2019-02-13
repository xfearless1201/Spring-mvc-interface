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
import org.apache.http.HttpStatus;
import org.springframework.core.annotation.SynthesizingMethodParameter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @ClassName IGServiceImpl
 * @Description IG彩票
 * @author Hardy
 * @Date 2019年2月9日 下午4:32:12
 * @version 1.0.0
 */
public class IGGameServiceImpl implements GameReflectService{
	/*
	 * private static final String
	 * apiurl="http://igyfsw.iasia99.com/igapiyf/app/api.do"; private static
	 * final String hashcode="ttx_2e823371-a192-40db-b8ad-9904edd8";
	 */
	/*
	 * (1)LOGIN命令 (2)CHANGE_PASSWORD命令 (3)GET_BALANCE 命令 (4)DEPOSIT 命令
	 * (5)WITHDRAW 命令 (6)CHECK_REF命令
	 */
	DESEncrypt d = new DESEncrypt("");
	private static String apiurl;// 命令(3) (4) (5) (6)对接地址为
	private static String hashcode;
	private static String line;
	private static String lotto_url; // 香港彩 命令(1) (2) 对接地址为
	private static String lottery_url; // 时时彩 命令(1) (2) 对接地址为
	String lotteryTray=null;

	private static Logger logger =  LoggerFactory.getLogger(IGGameServiceImpl.class);
	public IGGameServiceImpl(Map<String, String> pmap) {
		PlatFromConfig pf = new PlatFromConfig();
		pf.InitData(pmap, "IG");
		JSONObject jo = new JSONObject().fromObject(pf.getPlatform_config());
		apiurl = jo.getString("apiurl").toString();
		hashcode = jo.getString("hashcode").toString();
		lotto_url = jo.getString("lotto_url").toString();
		lottery_url = jo.getString("lottery_url").toString();
		try {
			line = jo.getString("line").toString();
		} catch (Exception e) {
			line = "1";
		}
		try{
			//获取盘口
			lotteryTray=jo.getString("lotteryTray");
		}catch(Exception e){
			
		}
	}

	public String LoginGame(String username, String password, String gameType, String gameid, String type,String handicap) {
		password = d.getMd5(password);
//		String Tray="A";
//		if(lotteryTray!=null){
//			try{
//				String cagent=username.substring(0, 3).toUpperCase(); 
//				JSONObject jo=JSONObject.fromObject(lotteryTray);
//				if(jo.has(cagent)){
//					Tray=jo.getString(cagent);
//				}else{
//					Tray=jo.getString("default");
//				} 
//			}catch(Exception e){ 
//			}
//		} 
		
		String data = "{\"hashCode\":\"" + hashcode + "\",\"command\":\"LOGIN\",\"params\":{\"username\":\"" + username
				+ "\",\"password\":\"" + password + "\",";
		if ("LOTTERY".equals(gameType)) {
			data += "\"currency\":\"CNY\",\"language\":\"CN\",\"gameType\":\"" + gameType
					+ "\",\"lotteryTray\":\""+handicap+"\",\"lotteryPage\":\"" + gameid + "\",\"userCode\":\"" + username
					+ "\",\"lotteryType\":\"" + type + "\",\"line\":\"" + line + "\",\"lobby\":\"1\"}}";
		} else {
			data += "\"currency\":\"CNY\",\"language\":\"CN\",\"gameType\":\"" + gameType
					+ "\",\"lottoTray\":\""+handicap+"\",\"userCode\":\"" + username + "\",\"lottoType\":\"" + type
					+ "\",\"line\":\"" + line + "\",\"lobby\":\"1\"}}";
		}
		// 香港彩票
		if ("LOTTO".equals(gameType)) {
			apiurl = lotto_url;
		} else if ("LOTTERY".equals(gameType)) {// 时时彩
			apiurl = lottery_url;
		}
		String msg = sendPost(apiurl, data);
		JSONObject json;
		json = JSONObject.fromObject(msg);
		if (!"0".equals(json.getString("errorCode"))) {
			FileLog f = new FileLog();
			Map<String, String> map = new HashMap<>();
			map.put("apiurl", apiurl);
			map.put("data", data);
			map.put("msg", msg);
			map.put("Function", "LoginGame");
			f.setLog("IG", map);
		}
		return msg;
	}

	public String getBalance(String username, String password) {
		password = d.getMd5(password);
		// 登录创建账号
		String data = "{\"hashCode\":\"" + hashcode + "\",\"command\":\"GET_BALANCE\",\"params\":";
		data += "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}}";
		String msg = sendPost(apiurl, data);
		JSONObject json;
		json = JSONObject.fromObject(msg);
		if (!"0".equals(json.getString("errorCode"))) {
			FileLog f = new FileLog();
			Map<String, String> map = new HashMap<>();
			map.put("apiurl", apiurl);
			map.put("data", data);
			map.put("msg", msg);
			map.put("Function", "getBalance");
			f.setLog("IG", map);
		}
		return msg;
	}

	public synchronized  String DEPOSIT(String username, String password, String billno, String amount) {
		try {
			password = d.getMd5(password);
			// 登录创建账号
			String data = "{\"hashCode\":\"" + hashcode + "\",\"command\":\"DEPOSIT\",\"params\":{\"username\":\""
					+ username + "\",\"password\":\"" + password + "\",";
			data += "\"ref\":\"" + billno + "\",\"desc\":\"\",\"amount\":\"" + amount + "\"}}";
            logger.info("IG转账转出,post请求url"+apiurl+"参数"+data);
			String msg = sendPost(apiurl, data);
			JSONObject json;
			json = JSONObject.fromObject(msg);

			FileLog f = new FileLog();
			Map<String, String> map = new HashMap<>();
			map.put("apiurl", apiurl);
			map.put("data", data);
			map.put("billno", billno);
			map.put("msg", msg);
			map.put("Function", "DEPOSIT");
			f.setLog("IG", map);
			if (!"0".equals(json.getString("errorCode"))) {
				return "error";
			}
			return "success";
		} catch (Exception e) {
			return "error";
		}

	}

	public String WITHDRAW(String username, String password, String billno, String amount) {
		try {
			password = d.getMd5(password);
			// 登录创建账号
			String data = "{\"hashCode\":\"" + hashcode + "\",\"command\":\"WITHDRAW\",\"params\":{\"username\":\""
					+ username + "\",\"password\":\"" + password + "\",";
			data += "\"ref\":\"" + billno + "\",\"desc\":\"\",\"amount\":\"" + amount + "\"}}";
			String msg = sendPost(apiurl, data);
			JSONObject json;
			json = JSONObject.fromObject(msg);
			if (!"0".equals(json.getString("errorCode"))) {
				FileLog f = new FileLog();
				Map<String, String> map = new HashMap<>();
				map.put("apiurl", apiurl);
				map.put("data", data);
				map.put("msg", msg);
				map.put("Function", "WITHDRAW");
				f.setLog("IG", map);
				return "error";
			}
			return "success";
		} catch (Exception e) {
			return "error";
		}
	}

	public synchronized String CHECK_REF(String billno) {

		// 登录创建账号
		String data = "{\"hashCode\":\"" + hashcode + "\",\"command\":\"CHECK_REF\",\"params\":{\"ref\":\"" + billno
				+ "\"}}";
		try {
			String msg = sendPost(apiurl, data);
			JSONObject json;
			json = JSONObject.fromObject(msg);
			String errorcode = json.getString("errorCode");
			if ("0".equals(errorcode) || "6601".equals(errorcode) || "6617".equals(errorcode)) {
				FileLog f = new FileLog();
				Map<String, String> map = new HashMap<>();
				map.put("apiurl", apiurl);
				map.put("data", data);
				map.put("msg", msg);
				map.put("Function", "CHECK_REF");
				f.setLog("IG", map);
			}
			return errorcode;
		} catch (Exception e) {
			return "error";
		}
	}

	/**
	 * 发送请求到server端
	 * 
	 * @param
	 *            
	 * @param
	 * @return null发送失败，否则返回响应内容
	 */
	public static String sendPost(String tagUrl, String Data) {
		// System.out.println(tagUrl);
		// System.out.println(Data);
		// 创建httpclient工具对象
		HttpClient client = new HttpClient();
		// 创建post请求方法
		PostMethod myPost = new PostMethod(tagUrl);
		String responseString = null;
		int statusCode=0; 
		try {
			// 设置请求头部类型
			myPost.setRequestHeader("Content-Type", "application/json");
			myPost.setRequestHeader("charset", "utf-8");
			myPost.setRequestBody(Data);
			//这里的超时单位是毫秒。这里的http.socket.timeout相当于SO_TIMEOUT  
			client.getParams().setIntParameter("http.socket.timeout",10000); 
			
			// 设置请求体，即xml文本内容，一种是直接获取xml内容字符串，一种是读取xml文件以流的形式
			 statusCode = client.executeMethod(myPost);
			// 只有请求成功200了，才做处理
			if (statusCode == HttpStatus.SC_OK) {
				InputStream inputStream = myPost.getResponseBodyAsStream();
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
				map.put("ResponseBody", responseString);
				map.put("tagUrl", tagUrl);
				map.put("Data", Data);
				map.put("Function", "sendPost");
				f.setLog("IG", map);
			}
		} catch (Exception e) {
			FileLog f = new FileLog();
			Map<String, String> map = new HashMap<>();
			map.put("statusCode", statusCode + "");
			map.put("ResponseBody", responseString);
			map.put("tagUrl", tagUrl);
			map.put("Data", Data);
			map.put("Function", "sendPost");
			f.setLog("IG", map);
			e.printStackTrace();
		} finally {
			myPost.releaseConnection();
		}
		// System.out.println(responseString);
		return responseString;
	}

	/**
	 * 游戏下分
	 */
    @Override
    public JSONObject transferIn(GameTransferVO gameTransferVO) {
        String ag_username = gameTransferVO.getAg_username();
        String ag_password = gameTransferVO.getPassword();
        String billno = gameTransferVO.getBillno();
        String credit = gameTransferVO.getMoney();//订单金额
        String username = gameTransferVO.getUsername();
        String type = gameTransferVO.getType();
        try {
            String msg = DEPOSIT(ag_username, ag_password, billno, credit + "");
            if ("success".equalsIgnoreCase(msg)) {
                return GameResponse.success("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
            }

            // 轮询
            boolean isPoll = true;
            int polls = 0;
            do {
                Thread.sleep(1500);
                logger.info("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务,第{}次轮询转账订单次数", polls);
                polls++;
                String ckeckMsg = CHECK_REF(billno);
                if ("6601".endsWith(ckeckMsg)) {
                    // 转账成功
                    return GameResponse.success("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
                } else if ("6617".endsWith(ckeckMsg)) {
                    if (polls > 2) {
                        return GameResponse.process("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务异常,需人工审核");
                    }
                } else {
                    if (polls > 2) {
                        isPoll = false;
                    }
                }
                return GameResponse.faild("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务失败");
            } while (isPoll);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务异常:{}", e.getMessage());
            return GameResponse.process("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务失败");
        }
    }

    /**
     * 游戏上分
     */
    @Override
    public JSONObject transferOut(GameTransferVO gameTransferVO) {
        String ag_username = gameTransferVO.getAg_username();
        String ag_password = gameTransferVO.getPassword();
        String billno = gameTransferVO.getBillno();
        String credit = gameTransferVO.getMoney();//订单金额
        String username = gameTransferVO.getUsername();
        String type = gameTransferVO.getType();
        try {
            String msg = WITHDRAW(ag_username, ag_password, billno, credit + "");
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
