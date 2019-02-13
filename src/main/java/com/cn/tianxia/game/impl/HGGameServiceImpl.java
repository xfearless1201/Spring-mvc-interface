package com.cn.tianxia.game.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator; 
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document; 
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.game.GameReflectService;
import com.cn.tianxia.po.v2.GameResponse;
import com.cn.tianxia.util.FileLog;
import com.cn.tianxia.util.PlatFromConfig;
import com.cn.tianxia.util.SecurityUtil;
import com.cn.tianxia.vo.v2.GameBalanceVO;
import com.cn.tianxia.vo.v2.GameCheckOrCreateVO;
import com.cn.tianxia.vo.v2.GameForwardVO;
import com.cn.tianxia.vo.v2.GameQueryOrderVO;
import com.cn.tianxia.vo.v2.GameTransferVO;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName HGServiceImpl
 * @Description 皇冠体育
 * @author Hardy
 * @Date 2019年2月9日 下午4:29:33
 * @version 1.0.0
 */
public class HGGameServiceImpl implements GameReflectService{

	private static final Logger logger = LoggerFactory.getLogger(HGGameServiceImpl.class);
	
	private String PublicKey;
	private String PrivateKey;
	private String DefaultCompanyCode;
	private String DefaultPrefixCode;
	private String PassAccessKey;
	private String onlinekey;
	private String apiurl;
	private String loginurl;
	private String mobileloginurl;

	
	public HGGameServiceImpl(Map<String, String> pmap) {
		PlatFromConfig pf=new PlatFromConfig();
		pf.InitData(pmap, "HG");
		JSONObject jo=new JSONObject().fromObject(pf.getPlatform_config());
		PublicKey=jo.getString("PublicKey").toString();
		PrivateKey=jo.getString("PrivateKey").toString();
		DefaultCompanyCode=jo.getString("DefaultCompanyCode").toString();
		DefaultPrefixCode=jo.getString("DefaultPrefixCode").toString();
		PassAccessKey=jo.getString("PassAccessKey").toString();
		onlinekey=jo.getString("onlinekey").toString();
		apiurl=jo.getString("apiurl").toString();
		loginurl=jo.getString("loginurl").toString();
		mobileloginurl=jo.getString("mobileloginurl").toString();
	}
	
	public String getLogin(String username,String model){
		username=username.toLowerCase();
		Map<String,String> map =new HashMap<>(); 
		SecurityUtil s=new SecurityUtil();
		String data="ACTIVE_KEY|"+PassAccessKey+"|"+DefaultPrefixCode+"|"+DefaultPrefixCode+username+"|RMB|"+onlinekey+"=@=";  
		map.put("data", data); 
		try {
			data=s.encrypt(data, PrivateKey+DefaultPrefixCode+username);
			data+="^"+DefaultPrefixCode+username;
			map.put("data1", data); 
			data=s.encrypt(data, PublicKey);
			data+="^"+DefaultPrefixCode+username;
			String url=apiurl+"/soccer_api_sys_v2/get-access-key.php?r=";
			map.put("data2", (url+data)); 
			data=readContentFromGet(url+data);
			Document doc =  DocumentHelper.parseText(data);
			Element root = doc.getRootElement();
			if("success".equals(root.getName())){ 
				Iterator<Element> iterator = root.elementIterator();  
		        while(iterator.hasNext()){  
		            Element e = iterator.next();  
		            if("access_key".equals(e.getName())){
		            	String loginurls="";
		            	if(!"MB".equals(model)){
		            		 loginurls=loginurl+"?activekey="+e.getTextTrim()+"&acc="+DefaultPrefixCode+username+"&langs=2";		            		
		            	}else{
		            		 loginurls=mobileloginurl+"?activekey="+e.getTextTrim()+"&acc="+DefaultPrefixCode+username+"&langs=2";		
		            	}
		            	return loginurls;
		            }
		        }  
				return "success";
			}else{
				FileLog f=new FileLog();  
				map.put("username", username); 
				map.put("msg", data); 
				map.put("Function", "getLogin");
				f.setLog("HG", map);
				return "error";
			}
		} catch (Exception e) {
			FileLog f=new FileLog(); 
			map.put("username", username);  
			map.put("msg", "HttpURLConnection Error"); 
			map.put("Function", "WITHDRAW");
			f.setLog("HG", map);
			return "error";
		}   
	}
	
	public String getBalance(String username){
		username=username.toLowerCase();
		SecurityUtil s=new SecurityUtil();
		Map<String,String> map =new HashMap<>(); 
		String data="GET_CCL|"+PassAccessKey+"|"+DefaultPrefixCode+username+"|"+onlinekey+"=@=";  
		//System.out.println(data);
		map.put("data", data); 
		try {
			data=s.encrypt(data, PrivateKey+DefaultPrefixCode+username);
			data+="^"+DefaultPrefixCode+username;
			//System.out.println(data);
			map.put("data1", data); 
			data=s.encrypt(data, PublicKey);
			data+="^"+DefaultPrefixCode+username;
			String url=apiurl+"/soccer_api_sys_v2/get-wallet-ccl.php?r=";
			map.put("data2", url+data); 
			//System.out.println(data);
			data=readContentFromGet(url+data);
			//System.out.println(data);
			Document doc =  DocumentHelper.parseText(data);
			Element root = doc.getRootElement();
			if("success".equals(root.getName())){ 
				Iterator<Element> iterator = root.elementIterator();  
		        while(iterator.hasNext()){  
		            Element e = iterator.next();  
		            if("credit_left".equals(e.getName())){ 
		            	return e.getTextTrim();
		            }
		        }  
				return "success";
			}else{
				FileLog f=new FileLog();  
				map.put("username", username); 
				map.put("msg", data); 
				map.put("Function", "getBalance");
				f.setLog("HG", map);
				return "error";
			}
		} catch (Exception e) {
			FileLog f=new FileLog();  
			map.put("username", username);  
			map.put("msg", "HttpURLConnection Error"); 
			map.put("Function", "WITHDRAW");
			f.setLog("HG", map);
			return "error";
		}  
	}
	
	public String DEPOSIT(String username,String billno,String amount){
		username=username.toLowerCase();
		SecurityUtil s=new SecurityUtil();
		Map<String,String> map =new HashMap<>(); 
		String data="GET_DEPOSIT|"+PassAccessKey+"|"+billno+"|"+DefaultPrefixCode+username+"|RMB|"+amount+"|"+onlinekey+"=@=";  
		map.put("data", data); 
		try {
			data=s.encrypt(data, PrivateKey+DefaultPrefixCode+username);
			data+="^"+DefaultPrefixCode+username+"^"+username+PublicKey;
			map.put("data1", data); 
			data=s.encrypt(data, PublicKey);
			String url=apiurl+"/soccer_api_sys_v2/get_wallet_deposit.php?r=";
			map.put("data2", url+data); 
			data=readContentFromGet(url+data);
			Document doc =  DocumentHelper.parseText(data);
			Element root = doc.getRootElement();
			if("success".equals(root.getName())){  
				return "success";
			}else{
				FileLog f=new FileLog();  
				map.put("username", username); 
				map.put("billno", billno); 
				map.put("amount", amount); 
				map.put("msg", data); 
				map.put("Function", "DEPOSIT");
				f.setLog("HG", map);
				return "error";
			}
		} catch (Exception e) {
			FileLog f=new FileLog();  
			map.put("username", username);  
			map.put("msg", "HttpURLConnection Error"); 
			map.put("Function", "WITHDRAW");
			f.setLog("HG", map);
			return "error";
		}  
	}
	
	public String WITHDRAW(String username,String billno,String amount){
		username=username.toLowerCase();
		SecurityUtil s=new SecurityUtil();
		Map<String,String> map =new HashMap<>(); 
		String data="GET_WITHDRAW|"+PassAccessKey+"|"+billno+"|"+DefaultPrefixCode+username+"|RMB|"+amount+"|"+onlinekey+"=@=";  
		map.put("data", data); 
		try {
			data=s.encrypt(data, PrivateKey+DefaultPrefixCode+username);
			data+="^"+DefaultPrefixCode+username+"^"+username+PublicKey;
			map.put("data1", data); 
			data=s.encrypt(data,PublicKey);
			//data+="^"+DefaultPrefixCode+username;
			String url=apiurl+"/soccer_api_sys_v2/get_wallet_withdraw.php?r=";
			map.put("data2", url+data); 
			data=readContentFromGet(url+data);
			Document doc =  DocumentHelper.parseText(data);
			Element root = doc.getRootElement();
			if("success".equals(root.getName())){  
				return "success";
			}else{
				FileLog f=new FileLog(); 
				
				map.put("username", username); 
				map.put("billno", billno); 
				map.put("amount", amount); 
				map.put("msg", data); 
				map.put("Function", "WITHDRAW");
				f.setLog("HG", map);
				return "error";
			}
		} catch (Exception e) {
			FileLog f=new FileLog();  
			map.put("username", username);  
			map.put("msg", "HttpURLConnection Error"); 
			map.put("Function", "WITHDRAW");
			f.setLog("HG", map);
			return "error";
		}  
	}
	
	/**
	 * 获取即时注单
	 * @param latesttid : 0 or latest_tid value
	 * @return
	 */
	public String GET_INSTANT_DATA(String latesttid){
		SecurityUtil s=new SecurityUtil();
		String data="GET_INSTANT_DATA|"+PassAccessKey+"|1|"+latesttid+"|"+onlinekey+"=@=";  
		//System.out.println(data);
		try {
			data=s.encrypt(data, PrivateKey+PassAccessKey);
			//System.out.println(data);
			data+="^"+PassAccessKey+"^"+PassAccessKey+PublicKey;
			//System.out.println(data);
			data=s.encrypt(data, PublicKey); 
			//System.out.println(data);
			String url=apiurl+"/soccer_api_sys_v2/get-instant-data.php?r=";
			//System.out.println(url);
			data=readContentFromGet(url+data);
			//System.out.println(data);
			Document doc =  DocumentHelper.parseText(data);
			Element root = doc.getRootElement(); 
			return data; 
		} catch (Exception e) {
		}
		return data;  
	}
	
	public static void main(String[] args) {
		HGGameServiceImpl h=new HGGameServiceImpl(null);
		Document doc = null;
		String username="tx00001018";
		String data="";
		data=h.GET_INSTANT_DATA("1");
		//System.out.println(data);  
		//data=h.WITHDRAW(username, "1234567890008", "1"); 
		//data=h.DEPOSIT(username, "1234567890007", "1"); 
		//String data=h(username, "1234567890001", "1");
		//data=h.getBalance(username);
		////System.out.println(data);  
	} 
	
	public String readContentFromGet(String getURL) throws IOException {
		// 拼凑get请求的URL字串，使用URLEncoder.encode对特殊和不可见字符进行编码 
		URL getUrl = new URL(getURL);
		// 根据拼凑的URL，打开连接，URL.openConnection函数会根据URL的类型，
		// 返回不同的URLConnection子类的对象，这里URL是一个http，因此实际返回的是HttpURLConnection
		HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection();
		// 进行连接，但是实际上get request要在下一句的connection.getInputStream()函数中才会真正发到
		connection.setRequestProperty("Accept-encoding", "gzip");
		// 服务器
		connection.connect();
		// 取得输入流，并使用Reader读取
		GZIPInputStream in = new GZIPInputStream(connection.getInputStream());
		BufferedReader reader = new BufferedReader(new InputStreamReader(in, "utf-8"));// 设置编码,否则中文乱码
		StringBuffer str= new StringBuffer();
		String lines;
		while ((lines = reader.readLine()) != null) { 
			str.append(lines); 
		}
		reader.close();
		// 断开连接
		connection.disconnect(); 
		
		return str.toString();
	}

	/**
	 * 游戏上分
	 */
    @Override
    public JSONObject transferIn(GameTransferVO gameTransferVO) {
        String hg_username = gameTransferVO.getHg_username();
        String billno = gameTransferVO.getBillno();
        String credit = gameTransferVO.getMoney();
        String username = gameTransferVO.getUsername();
        String type = gameTransferVO.getType();
        try {
            if (StringUtils.isBlank(hg_username)) {
                return GameResponse.faild("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务失败,获取登录账号失败");
            }
            String msg = DEPOSIT(hg_username, billno, credit + "");
            if ("success".equalsIgnoreCase(msg)) {
                // 转账订单提交成功
                return GameResponse.success("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
            } else {
                return GameResponse.faild("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务失败");
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
        String hg_username = gameTransferVO.getHg_username();
        String billno = gameTransferVO.getBillno();
        String credit = gameTransferVO.getMoney();
        String username = gameTransferVO.getUsername();
        String type = gameTransferVO.getType();
        try {
            String msg = WITHDRAW(hg_username, billno, credit + "");
            if ("success".equals(msg) || msg == "success") {
                return GameResponse.success("用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
            } else {
                return GameResponse.faild("用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务失败");
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
     * 查询游戏转账
     */
    @Override
    public JSONObject queryTransferOrder(GameQueryOrderVO gameQueryOrderVO) {
        // TODO Auto-generated method stub
        return null;
    } 
}
