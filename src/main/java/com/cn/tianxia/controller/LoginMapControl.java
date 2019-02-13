package com.cn.tianxia.controller;  
  
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.cn.tianxia.pay.ys.util.DateUtil;
import com.cn.tianxia.pay.ys.util.S3001Web;
import com.cn.tianxia.pay.ys.util.SignUtils;
import com.cn.tianxia.pay.ys.util.YspayConfig;
import com.cn.tianxia.service.UserService;
import com.cn.tianxia.service.impl.UserServiceImpl;
import com.cn.tianxia.util.Https;
import com.cn.tianxia.util.IPTools;

import net.sf.json.JSONObject;
 
  
@Controller
@RequestMapping("LoginMap") 
@Scope("prototype")
public class LoginMapControl  extends BaseController  {   
	@Resource
	private UserServiceImpl userService;
	
	@RequestMapping("/getUserList.do")  
	@ResponseBody
	public JSONObject getUserList(String key) {  
		 Properties pro = new Properties();
			InputStream in; 
			String acckey ="";
		try {
			in = this.getClass().getResourceAsStream("/file.properties");
			pro.load(in);
			acckey =pro.getProperty("key");   
		} catch (Exception e) { 
			
		} 
		if(key==null||"".equals(key)||!key.equals(acckey)){
			return null;
		}
		JSONObject jo=new JSONObject().fromObject(loginmaps); 
		return jo;
	}
	
	@RequestMapping("/shotOff.do")  
	@ResponseBody
	public String shotOff(String uid,String key) {  
		 Properties pro = new Properties();
			InputStream in; 
			String acckey ="";
		try {
			in = this.getClass().getResourceAsStream("/file.properties");
			pro.load(in);
			acckey =pro.getProperty("key");   
		} catch (Exception e) { 
			return "faild";
		} 
		if(key==null||"".equals(key)||!key.equals(acckey)){
			return "faild";
		}
		if(loginmaps.containsKey(uid)){
			Map<String, String> loginmap=loginmaps.get(uid);
			loginmap.put("uid", uid);
	        loginmap.put("sessionid", "FFFFFFFFFF");
			loginmaps.put(uid, loginmap); 
		}
		return "success";
	} 
	
	@RequestMapping("/update.do")  
	@ResponseBody 
	public String test(String uid,String key,String sid)  {   
		 Properties pro = new Properties();
			InputStream in; 
			String acckey ="";
		try {
			in = this.getClass().getResourceAsStream("/file.properties");
			pro.load(in);
			acckey =pro.getProperty("key");   
		} catch (Exception e) { 
			return "faild";
		} 
		if(key==null||"".equals(key)||!key.equals(acckey)){
			return "faild";
		}
		if(loginmaps.containsKey(uid)){
			Map<String, String> loginmap=loginmaps.get(uid);
			loginmap.put("uid", uid);
			loginmap.put("sessionid", sid);
			loginmaps.put(uid, loginmap);
		} 
		return "success";
	} 
	 
}  