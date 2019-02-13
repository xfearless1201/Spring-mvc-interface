package com.cn.tianxia.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.cn.tianxia.entity.OrderQuery;
import com.cn.tianxia.game.impl.BGGameServiceImpl;
import com.cn.tianxia.json.JSONUtils;
import com.cn.tianxia.pay.gst.util.StringUtils;
import com.cn.tianxia.service.UserService;
import com.cn.tianxia.util.PlatFromConfig;

/**
 * 
 * @author zw
 *
 */

@Controller
@Scope("prototype")
@RequestMapping("/bg")
public class BGIntefaceController extends BaseController {
	@Resource
	private UserService userService;

	private final String deskey = "tianxia88";

	/***
	 * 彩票开奖结果走势图查询
	 * 
	 * @param request
	 * @param response
	 * @param uid
	 * @param lotteryId
	 * @param method
	 * @param isMobile
	 * @param pageSize
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	@RequestMapping("/lotteryCheck")
	@ResponseBody
	public JSONObject lotteryCheck(HttpServletRequest request, HttpServletResponse response,String token, String lotteryId,
			String method, String isMobile, String pageSize, String startTime, String endTime) {
//		HttpSession session = request.getSession();
//		String bgtoken = session.getAttribute(session.getAttribute("uid").toString() + "BGToken").toString();
//		APIResult apiResult = new APIResult();
//
//		if ("".equals(bgtoken) || null == bgtoken) {
//			apiResult.setCode("001");
//			apiResult.setMessage("token失效");
//			apiResult.setParams(null);
//		}
//		logger.info("bgtoken:" + bgtoken);
//		CRequest cr = new CRequest();
//		String token = cr.URLRequest(bgtoken).get("token").toString();
		net.sf.json.JSONObject json = BGGameServiceImpl.lotteryCheck(token, lotteryId, method, isMobile, pageSize,
				startTime, endTime);
		return (JSONObject) JSONObject.parseObject(json.toString());
	}

	/**
	 * 限制只查代理的用户注单(open.order.agent.query)
	 * 
	 * @param request
	 * @param response
	 * @param orderQuery
	 * @return
	 */
	@RequestMapping("/orderAgentQuery")
	@ResponseBody
	public JSONObject orderAgentQuery(HttpServletRequest request, HttpServletResponse response, OrderQuery orderQuery) {
		PlatFromConfig pf = new PlatFromConfig();
		List<Map<String, String>> plist = userService.selectPlatFromInfo(null);
		Map<String, String> pmap = new HashMap<>();
		for (int i = 0; i < plist.size(); i++) {
			if ("1".equals(plist.get(i).get("platform_status").toString())) {
				pmap.put(plist.get(i).get("platform_key").toString(), plist.get(i).get("platform_config").toString());
			}
		}

		BGGameServiceImpl c = new BGGameServiceImpl(pmap);
		net.sf.json.JSONObject json = c.orderAgentQuery(orderQuery);

		return (JSONObject) JSONObject.parseObject(json.toString());
	}

	/**
	 * bg 试玩连接
	 * 
	 * @param request
	 * @param response
	 * @param gameID
	 * @param model
	 * @return
	 */
	@RequestMapping("/bgTrialGame")
	@ResponseBody
	public JSONObject bgTrialGame(HttpServletRequest request, HttpServletResponse response, String gameID,
			String model,String agent) {
		JSONObject jo = new JSONObject();
		
		//代理号不能为空
		if(StringUtils.isNullOrEmpty(agent) || StringUtils.isNullOrEmpty(gameID) || StringUtils.isNullOrEmpty(model)){
			jo.put("msg", "error");
			return jo;
		}
		
		// 检查维护状态
		PlatFromConfig pf = new PlatFromConfig();
		List<Map<String, String>> plist = userService.selectPlatFromInfo(null);
		Map<String, String> pmap = new HashMap<>();
		for (int i = 0; i < plist.size(); i++) {
			net.sf.json.JSONObject platform_configJson= JSONUtils.toJSONObject(plist.get(i).get("platform_config").toString());
    		String platform_config="";
    		if(platform_configJson.containsKey(agent)){
    			logger.info("试玩代理号:"+agent);
    			platform_config=platform_configJson.get(agent).toString();
    			logger.info("试玩新增线路配置信息:"+platform_config);
    		}else if(platform_configJson.containsKey("ALL")){
    			platform_config=platform_configJson.get("ALL").toString();
    			logger.info("试玩新线路默认配置信息:"+platform_config);
    		}else{
    			platform_config=plist.get(i).get("platform_config").toString();
    			logger.info("试玩默认配置信息:"+platform_config);
    		}
    		pmap.put(plist.get(i).get("platform_key").toString(), platform_config);
		}
		String method = "";

		if ("1".equals(gameID)) {
			method = "open.video.game.url";// 视讯
		} else if ("2".equals(gameID)) {
			method = "open.lottery.game.url";// 彩票
		}

		jo.put("msg", "error");

		 StringBuffer url = request.getRequestURL();  
		 String tempContextUrl = url.delete(url.length() - request.getRequestURI().length(), url.length()).append("/").toString(); 
		
		BGGameServiceImpl c = new BGGameServiceImpl(pmap);
		net.sf.json.JSONObject json = c.openUserCommonAPI("", method, model, "",tempContextUrl);
		if ("success".equals(json.get("code"))) {
			String url2 = net.sf.json.JSONObject.fromObject(json.get("params")).get("result").toString();
			jo.put("msg", url2);
			jo.put("type", "link");
		}

		return jo;
	}

}
