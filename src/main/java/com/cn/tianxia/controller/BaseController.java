package com.cn.tianxia.controller;
 
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.common.HttpConstants;
import com.cn.tianxia.json.JsonDateValueProcessor;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

/**
 * Controller基类
 */
public class BaseController { 

    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    
    protected final static SimpleDateFormat SDF =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    //在线用户
    public static ConcurrentHashMap<String,Map<String, String>> loginmaps=new ConcurrentHashMap<String,Map<String, String>>();
    // 注册验证码
    public static final ConcurrentHashMap<String, Long> regist = new ConcurrentHashMap<String, Long>();
    //支付回调
    public static ConcurrentHashMap<String,String> payMap=new ConcurrentHashMap<String,String>();
    //短信发送
    public static ConcurrentHashMap<String,String> msgMap=new ConcurrentHashMap<String,String>();
    //短信登录,累计密码错误次数记录
    public static ConcurrentHashMap<String,Integer> errorMap=new ConcurrentHashMap<String,Integer>();
    //游戏转账锁
    public static ConcurrentHashMap<String,String> TransferMap=new ConcurrentHashMap<String,String>();
    //提现订单锁
    public static ConcurrentHashMap<String,String> withDrawMap=new ConcurrentHashMap<String,String>();
    
    /**
     * 游戏转账新锁
     */
    public static ConcurrentHashMap<String,String> gameMap=new ConcurrentHashMap<String,String>();
    
    public static String addUserCard="0";
    //支付
    public static ConcurrentHashMap<String,String> userPayMap=new ConcurrentHashMap<String,String>();
    //抽奖
    public static ConcurrentHashMap<String,String> luckDrawMap=new ConcurrentHashMap<String,String>();
    //线下扫码
    public static ConcurrentHashMap<String,String> scanPayMap=new ConcurrentHashMap<String,String>();

//	@Autowired
//    public RedisClientManager redisService;
//
//	//用户登录存入redis的key前缀
//	public final static String LOGIN_KEY=RedisKeysPrefix.GROUP_USER_LOGIN_INFO.getKey();
//
//	//登录过期时间30分钟
//	public final static int LOGIN_EXPIRES=7200;

    /**
     * 返回服务端处理结果
     * @param obj 服务端输出对象
     * @return 输出处理结果给前段JSON格式数据 
     */
	public String responseResult(Object obj){
		JSONObject jsonObj = null;
		if(obj != null){
		    logger.info("后端返回对象：{}", obj);
		    JsonConfig jsonConfig = new JsonConfig(); 
		    jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor());
		    jsonObj = JSONObject.fromObject(obj, jsonConfig);
		    logger.info("后端返回数据：" + jsonObj);
		    if(HttpConstants.SERVICE_RESPONSE_SUCCESS_CODE.equals(jsonObj.getString(HttpConstants.SERVICE_RESPONSE_RESULT_FLAG))){
		    	jsonObj.element(HttpConstants.RESPONSE_RESULT_FLAG_ISERROR, false);
		    	jsonObj.element(HttpConstants.SERVICE_RESPONSE_RESULT_MSG, "");
		    }else{
		    	jsonObj.element(HttpConstants.RESPONSE_RESULT_FLAG_ISERROR, true);
		    	String errMsg = jsonObj.getString(HttpConstants.SERVICE_RESPONSE_RESULT_MSG);
		    	jsonObj.element(HttpConstants.SERVICE_RESPONSE_RESULT_MSG, errMsg==null?HttpConstants.SERVICE_RESPONSE_NULL:errMsg);
		    }
		}
		logger.info("输出结果：{}", jsonObj.toString());
		return jsonObj.toString();
	}
	
	/**
     * 返回成功
     * @param obj 输出对象
     * @return 输出成功的JSON格式数据
     */
	public String responseSuccess(Object obj){
		JSONObject jsonObj = null;
		if(obj != null){
		    logger.info("后端返回对象：{}", obj);
		    JsonConfig jsonConfig = new JsonConfig(); 
		    jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor());
		    jsonObj = JSONObject.fromObject(obj, jsonConfig);
		    logger.info("后端返回数据：" + jsonObj);
		    jsonObj.element(HttpConstants.RESPONSE_RESULT_FLAG_ISERROR, false);
		    jsonObj.element(HttpConstants.SERVICE_RESPONSE_RESULT_MSG, "");
		}
		logger.info("输出结果：{}", jsonObj.toString());
		return jsonObj.toString();
	}

	/**
	 * 返回成功
	 * @param obj 输出对象
	 * @return 输出成功的JSON格式数据
	 */
	public String responseArraySuccess(Object obj){
		JSONArray jsonObj = null;
		if(obj != null){
			logger.info("后端返回对象：{}", obj);
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor());
			jsonObj = JSONArray.fromObject(obj, jsonConfig);
			logger.info("后端返回数据：" + jsonObj);
		}
		logger.info("输出结果：{}", jsonObj.toString());
		return jsonObj.toString();
	}
	
	/**
     * 返回成功
     * @param obj 输出对象
     * @return 输出成功的JSON格式数据
     */
	public String responseSuccess(Object obj, String msg){
		JSONObject jsonObj = null;
		if(obj != null){
		    logger.info("后端返回对象：{}", obj);
		    JsonConfig jsonConfig = new JsonConfig(); 
		    jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor());
		    jsonObj = JSONObject.fromObject(obj, jsonConfig);
		    logger.info("后端返回数据：" + jsonObj);
		    jsonObj.element(HttpConstants.RESPONSE_RESULT_FLAG_ISERROR, false);
		    jsonObj.element(HttpConstants.SERVICE_RESPONSE_RESULT_MSG, msg);
		}
		logger.info("输出结果：{}", jsonObj.toString());
		return jsonObj.toString();
	}
	
	/**
     * 返回失败
     * @param errorMsg 错误信息
     * @return 输出失败的JSON格式数据
     */
    public String responseFail(String errorMsg){
    	JSONObject jsonObj = new JSONObject();
    	jsonObj.put(HttpConstants.RESPONSE_RESULT_FLAG_ISERROR, true);
    	jsonObj.put(HttpConstants.SERVICE_RESPONSE_RESULT_MSG, errorMsg);
        logger.info("输出结果：{}", jsonObj.toString());
        return jsonObj.toString();
    } 
}
