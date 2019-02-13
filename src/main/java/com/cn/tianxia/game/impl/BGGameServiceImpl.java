package com.cn.tianxia.game.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.bg.api.util.BGHttpClientUtil;
import com.cn.tianxia.bg.api.util.HashUtil;
import com.cn.tianxia.entity.OrderQuery;
import com.cn.tianxia.game.GameReflectService;
import com.cn.tianxia.po.v2.GameResponse;
import com.cn.tianxia.util.APIResult;
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
 * @ClassName BGGameServiceImpl
 * @Description BG游戏接口
 * @author Hardy
 * @Date 2019年2月9日 下午4:24:45
 * @version 1.0.0
 */
public class BGGameServiceImpl implements GameReflectService{
	
    private static Logger logger = LoggerFactory.getLogger(BGGameServiceImpl.class);
	
	private static String apiurl;
	private static String jsonrpc;
	private static String secretKey;
	private static String sn;
	private static String agentLoginId;
	private static String nickname;
	//private static UUID random = UUID.randomUUID(); // uuid
	private static String password;

	/**
	 * 构建bg 参数列表
	 * 
	 * @param pmap
	 */
	public BGGameServiceImpl(Map<String, String> pmap) {
		PlatFromConfig pf = new PlatFromConfig();
		pf.InitData(pmap, "BG");
		JSONObject jo = JSONObject.fromObject(pf.getPlatform_config());
        apiurl = jo.getString("api_url").toString();
		jsonrpc = jo.getString("jsonrpc").toString();
		secretKey = jo.getString("secretKey").toString();
		sn = jo.getString("sn").toString();
		agentLoginId = jo.getString("agentLoginId").toString();
		nickname = jo.getString("nickname").toString();
		password = jo.getString("password").toString();
	}

	/**
	 * 创建代理(open.agent.create)
	 * 
	 * @param method
	 * @return
	 */
	public JSONObject openAgentCreate(String method) {
		JSONObject resultJson = new JSONObject();
		String responseUrl = null;
		APIResult api = new APIResult();
		// 设置BG API请求参数
		UUID random = UUID.randomUUID();
		Map<String, String> ps = new HashMap<String, String>();
		ps.put("random", random.toString());
		ps.put("sign", HashUtil.md5Hex(random.toString() + sn + agentLoginId + secretKey));
		ps.put("sn", sn);
		ps.put("loginId", agentLoginId);
//		ps.put("agentLoginId", agentLoginId);
//		ps.put("nickname", nickname);
		ps.put("password", password);

		return JSONObject.fromObject(post(ps, method));
	}

	/**
	 * 创建用户接口
	 * 
	 * @param loginId
	 * @param nickname
	 * @param method
	 * @return
	 */
	public JSONObject openUserCreate(String loginId, String nickname, String method) {
		JSONObject resultJson = new JSONObject();
		String responseUrl = null;
		APIResult api = new APIResult();
		// 设置BG API请求参数
		UUID random = UUID.randomUUID();
		Map<String, String> ps = new HashMap<String, String>();
		ps.put("random", random.toString());
		ps.put("digest", HashUtil.md5Hex(random + sn + HashUtil.sha1Base64(password)));
		ps.put("sn", sn);
		ps.put("loginId", loginId);
		ps.put("nickname", nickname);
		ps.put("agentLoginId", agentLoginId);
		return JSONObject.fromObject(post(ps, method));
	}

    /**
     *功能描述: 根据订单号确认是否转账成功
     *
     *@Author: Wilson
     *@Date: 2018年09月08日 19:03:59
     * @param loginId
    * @param method
    * @param bizId
     *@return: net.sf.json.JSONObject
     **/
	public JSONObject checkBillNo(String loginId, String method, String bizId){
		UUID random = UUID.randomUUID();
        Map<String, String> ps = new HashMap<String, String>();
        ps.put("random", random.toString());
        ps.put("sign", HashUtil.md5Hex(random + sn + secretKey));
        ps.put("sn", sn);
        ps.put("loginId", loginId);
        ps.put("bizId", bizId);
        return JSONObject.fromObject(post(ps, method));
    }

	/**
	 * BG 获取userCommonAPI,查询余额不需要写入URL
	 * 
	 * @param loginId
	 * @param method
	 * @param model 类型
	 * @param amount
	 * @return
	 */
	public JSONObject openUserCommonAPI(String loginId, String method, String model, String amount,String returnUrl) {
		Map<String, String> ps = new HashMap<String, String>();
		UUID random = UUID.randomUUID();
		ps.put("sn", sn);
		ps.put("loginId", loginId);
		// ps.put("loginId", agentLoginId);
		ps.put("agentLoginId", agentLoginId);
		ps.put("random", random.toString());

		// 视讯或者彩票 是否试玩连接地址
		if ("open.video.game.url".equals(method) || "open.lottery.game.url".equals(method)) {
			ps.put("returnUrl", returnUrl);
			// 如果是试玩在线提供试玩在线方法
			if ("fun".equals(model)) {
				method = "open.video.game.url".equals(method) ? "open.video.trial.game.url"
						: "open.lottery.trial.game.url";// 视讯或者彩票试玩游戏链接
			} else if ("MB".equals(model)) {//手机真线
				ps.put("isMobileUrl", "1");
				
			}else if("MBFUN".equals(model)){//手机试玩连接
				method = "open.video.game.url".equals(method) ? "open.video.trial.game.url"
						: "open.lottery.trial.game.url";// 视讯或者彩票试玩游戏链接
				ps.put("isMobileUrl", "1");
			}
		}

		// 适配sign或者digest参数
		// 需要sign方法列表
		String signatureMethod[] = { "open.order.query", "open.balance.transfer.query", "open.video.round.query",
				"open.video.trial.game.url", "open.lottery.trial.game.url", "open.video.user.tip.query"};
		// 特殊digest方法列表
		String digestMethod[] = { "open.order.agent.query", "open.balance.transfer.agent.query",
				"open.video.user.tip.agent.query" };
		String specialSignature[] = { "open.operator.user.balance" };

		if (signatureMethod[0].equals(method) || signatureMethod[1].equals(method) || signatureMethod[2].equals(method)
				|| signatureMethod[3].equals(method) || signatureMethod[4].equals(method)
				|| signatureMethod[5].equals(method)) {
			// sign特殊参数
			if (specialSignature[0].equals(method)) {
				ps.put("sign", HashUtil.md5Hex(random + sn + loginId + secretKey));
			} else {
				// 通用sign参数
				ps.put("sign", HashUtil.md5Hex(random + sn + secretKey));
			}

		} else {
			// 特殊digest open.order.agent.query
			if (digestMethod[0].equals(method) || digestMethod[1].equals(method) || digestMethod[2].equals(method)) {
				ps.put("digest", HashUtil.md5Hex(random + sn + HashUtil.sha1Base64(password)));
			} else {
				// 通用digest参数
				ps.put("digest", HashUtil.md5Hex(random + sn + loginId + HashUtil.sha1Base64(password)));
			}
		}

		// 会员进行余额操作(open.operator.user.transfer) 3.2.7
		// 接口：转账(open.balance.transfer)
		if ("open.operator.user.transfer".equals(method) || "open.balance.transfer".equals(method)) {
			String amountStr = "";
			if ("".equals(amount)) {
                amount = "0";
            }
			ps.put("amount", amount);
			ps.put("digest", HashUtil.md5Hex(random + sn + loginId + amount + HashUtil.sha1Base64(password)));
		}

		// ps.put("agentId",agentLoginId);
		return JSONObject.fromObject(post(ps, method));
	}

	/**
	 * // 转账
	 * 
	 * @param loginId
	 * @param method
	 * @param billno
	 * @param amount
	 * @param inout
	 * @return
	 */
	public String openBalanceTransfer(String loginId, String method, String billno, String amount, String inout,String bizId) {
		Map<String, String> ps = new HashMap<String, String>();
		UUID random = UUID.randomUUID();
		ps.put("sn", sn);
		ps.put("loginId", loginId);
		ps.put("bizId", bizId);
		ps.put("agentLoginId", agentLoginId);
		ps.put("random", random.toString());
        ps.put("checkBizId", "1");
		// 是否转出
		if ("OUT".equals(inout)) {
			amount = "-" + amount;
		}
		ps.put("amount", amount);
		ps.put("digest", HashUtil.md5Hex(random + sn + loginId + amount + HashUtil.sha1Base64(password)));

		JSONObject json = JSONObject.fromObject(post(ps, method));

		Map<String, String> map = new HashMap<>();
		System.out.println(json.toString());
		if ("success".equals(json.get("code"))) {
			return "success";
		} else {
			FileLog f = new FileLog();
			map.put("username", loginId);
			map.put("billno", billno);
			map.put("amount", amount);
			map.put("msg", json.toString());
			map.put("Function", "DEPOSIT");
			f.setLog("BG", map);
			return "error";
		}
	}

	/**
	 * 请求BG API
	 * 
	 * @param params
	 * @param method
	 * @return
	 */
	public JSONObject post(Map<String, String> params, String method) {
		Map<String, Object> postData = new HashMap<String, Object>();
		postData.put("id", UUID.randomUUID());
		postData.put("method", method);
		postData.put("params", params);
		postData.put("jsonrpc", jsonrpc);
		String str = com.alibaba.fastjson.JSONObject.toJSONString(postData);
		// 构造返回json
		APIResult api = new APIResult();
		String responseUrl = null;
		// post 请求内容
		String content = "json=" + str;
		logger.info("content:" + content);
		// 处理响应结果 jscheck
		JSONObject jscheck = null;
		// 请求BG API
		try {
			responseUrl = BGHttpClientUtil.post(apiurl, content);
			jscheck = JSONObject.fromObject(responseUrl);
			// 是否成功创建代理
			if (jscheck.has("error") && jscheck.get("error").toString().equals("null")) {
				api.setCode("success");
				api.setMessage("BG" + method + "成功!");
				api.setParams((Map<String, Object>) jscheck);
				return JSONObject.fromObject(api);
			}
		} catch (Exception e) {
			logger.info("BG" + method + "失败!", e);
			e.printStackTrace();
			api.setCode("error");
			api.setMessage(e.getMessage());

			Map<String, String> map = new HashMap<>();
			FileLog f = new FileLog();
			map.put("params", params.toString());
			map.put("msg", jscheck == null ? "":jscheck.toString());
			map.put("Function", method);
			f.setLog("BG", map);

			return JSONObject.fromObject(api);
		}
		logger.info(responseUrl);
		api.setCode("error");
		api.setMessage(JSONObject.fromObject(jscheck.get("error")).get("message").toString());
		api.setParams((Map<String, Object>) jscheck);
		// 返回响应结果
		return JSONObject.fromObject(api);
	}

	/**
	 * 彩票开奖结果查询
	 * 
	 * @param bgtoken
	 * @param lotteryId
	 * @return
	 */
	public static JSONObject lotteryCheck(String bgtoken, String lotteryId, String method, String isMobile,
			String pageSize, String startTime, String endTime) {
		String actionName = method;
		String url = "http://am.bgvip55.com/lottery-api/api/";

		Map<String, String> map = new HashMap<String, String>();

		// 查询所有
		if ("lottery.result.list.all".equals(method)) {
			if ("".equals(isMobile) || null == isMobile || isMobile.equals("0")){
                map.put("token", bgtoken);
            }
			if (!"".equals(pageSize) && null != pageSize){
                map.put("pageSize", pageSize);
            }
			if (!"".equals(isMobile) && null != isMobile){
                map.put("isMobile", isMobile);
            }
		}
		// 查询单个
		if ("lottery.result.list".equals(method)) {
			if ("".equals(isMobile) || null == isMobile || isMobile.equals("0")) {
                map.put("token", bgtoken);
            }
			map.put("lotteryId", lotteryId);
			if (!"".equals(pageSize) && null != pageSize){
                map.put("pageSize", pageSize);
            }
			if (!"".equals(startTime) && null != startTime){
                map.put("startTime", startTime);
            }
			if (!"".equals(endTime) && null != endTime){
                map.put("endTime", endTime);
            }
			if (!"".equals(isMobile) && null != isMobile){
                map.put("isMobile", isMobile);
            }
		}
		// 彩票走势
		if ("lt.result.trend.query".equals(method)) {
			map.put("lotteryId", lotteryId);
			if (!"".equals(pageSize) && null != pageSize){
                map.put("pageSize", pageSize);
            }
		}

		JSONObject json = new JSONObject();
		json.put("id", UUID.randomUUID().toString());
		json.put("method", method);
		json.put("params", map);
		json.put("jsonrpc", "2.0");
		// 构造返回json
		APIResult api = new APIResult();
		String responseUrl = null;
		// 处理响应结果 jscheck
		JSONObject jscheck = null;
		String content = "json=" + json.toString();
		System.out.println(content);
		try {
			responseUrl = BGHttpClientUtil.post(url, content);
			System.out.println(responseUrl);
			jscheck = JSONObject.fromObject(responseUrl);
			// 是否成功创建代理
			if (jscheck.has("error") && jscheck.get("error").toString().equals("null")) {
				api.setCode("success");
				api.setMessage("BG" + method + "成功!");
				jscheck.put("error", "null");
				api.setParams((Map<String, Object>) jscheck);
				
				return JSONObject.fromObject(api);
			}
		} catch (Exception e) {
			logger.error("BG" + method + "失败!", e);
			e.printStackTrace();
			api.setCode("error");
			api.setMessage(e.getMessage());
			Map<String, String> map1 = new HashMap<>();
			FileLog f = new FileLog();
			map1.put("params", map.toString());
			map1.put("msg", jscheck.toString());
			map1.put("Function", method);
			f.setLog("BG", map1);
			return JSONObject.fromObject(api);
		}
		api.setCode("error");
		api.setMessage(JSONObject.fromObject(jscheck.get("error")).get("message").toString());
		api.setParams((Map<String, Object>) jscheck);
		return JSONObject.fromObject(api);
	}
	
	/**
	 * 限制只查代理的用户注单(open.order.agent.query)
	 * @param orderQuery
	 * @return
	 */
	public JSONObject orderAgentQuery(OrderQuery orderQuery) {
		Map<String, Object> ps = new HashMap<String, Object>();
		UUID random = UUID.randomUUID();
		ps.put("sn", sn);
		ps.put("agentLoginId", agentLoginId);
		ps.put("random", random.toString());
		ps.put("digest", HashUtil.md5Hex(random + sn + HashUtil.sha1Base64(password)));

		if (null != orderQuery) {
			if (!"".equals(orderQuery.getStartTime()) && null != orderQuery.getStartTime()){
                ps.put("startTime", orderQuery.getStartTime());
            }
			if (!"".equals(orderQuery.getEndTime()) && null != orderQuery.getEndTime()){
                ps.put("endTime", orderQuery.getEndTime());
            }
			if (!"".equals(orderQuery.getModuleId()) && null != orderQuery.getModuleId()){
                ps.put("moduleId", orderQuery.getModuleId());
            }
			if (!"".equals(orderQuery.getGameId()) && null != orderQuery.getGameId()){
                ps.put("gameId", orderQuery.getGameId());
            }
			if (!"".equals(orderQuery.getIssueId()) && null != orderQuery.getIssueId()){
                ps.put("issueId", orderQuery.getIssueId());
            }
			if (!"".equals(orderQuery.getPlayerId()) && null != orderQuery.getPlayerId()){
                ps.put("playerId", orderQuery.getPlayerId());
            }
			if (!"".equals(orderQuery.getPageIndex()) && null != orderQuery.getPageIndex()){
                ps.put("pageIndex", orderQuery.getPageIndex());
            }
			if (!"".equals(orderQuery.getPageSize()) && null != orderQuery.getPageSize()){
                ps.put("pageSize", orderQuery.getPageSize());
            }
			if (!"".equals(orderQuery.getEtag()) && null != orderQuery.getEtag()){
                ps.put("etag", orderQuery.getEtag());
            }
			if (orderQuery.getUserIds() != null && !orderQuery.getUserIds().isEmpty()){
                ps.put("userIds", orderQuery.getUserIds());
            }
			if (orderQuery.getLoginIds() != null && !orderQuery.getLoginIds().isEmpty()){
                ps.put("loginIds", orderQuery.getLoginIds());
            }
		}
		logger.debug("posts:" + ps.toString());
		Map<String, Object> postData = new HashMap<String, Object>();
		postData.put("id", UUID.randomUUID());
		postData.put("method", "open.order.agent.query");
		postData.put("params", ps);
		postData.put("jsonrpc", jsonrpc);
		String str = com.alibaba.fastjson.JSONObject.toJSONString(postData);

		// 构造返回json
		APIResult api = new APIResult();
		String responseUrl = null;
		// post 请求内容
		String content = "json=" + str;
		logger.debug("content:" + content);
		// 处理响应结果 jscheck
		JSONObject jscheck = null;
		try {
			responseUrl = BGHttpClientUtil.post(apiurl, content);
			jscheck = JSONObject.fromObject(responseUrl);
			// 是否成功创建代理
			if (jscheck.has("error") && jscheck.get("error").toString().equals("null")) {
				api.setCode("success");
				api.setMessage("BG" + "open.order.agent.query" + "成功!");
				api.setParams((Map<String, Object>) jscheck);
				return JSONObject.fromObject(api);
			}
		} catch (Exception e) {
			logger.error("BG" + "open.order.agent.query" + "失败!", e);
			e.printStackTrace();
			api.setCode("error");
			api.setMessage(e.getMessage());

			Map<String, String> map = new HashMap<>();
			FileLog f = new FileLog();
			map.put("params", ps.toString());
			map.put("msg", jscheck.toString());
			map.put("Function", "open.order.agent.query");
			f.setLog("BG", map);
		}

		return JSONObject.fromObject(api);
	}

	public static void main(String[] args) throws ParseException {
		// lottery.result.list.all
		// token String 32 是 SESSION ID
		// isMobile int 32 否 0或不传需要验证token、1手机端不验证token
		// pageSize int 32 否 每个彩种读取记录数默认1
//		String token = "am00011654B110DE4725532DFDC69db7";
//		String method = "lottery.result.list.all";
//		String startTime = "2017-05-01 13:23:20";
//		String endTime = "2017-06-24 13:23:20";
//		String pageSize = "1";
//		String lotteryId = "1";
//		String isMobile = "0";
//		JSONObject json = BGGameServiceImpl.lotteryCheck(token, null, method, null, null, null, null);
//		System.out.println(json.get("code").toString());
//		System.out.println(json.get("params").toString());

        Map<String,String> pmap=new TreeMap<>();
        pmap.put("api_url","http://am.bgvip55.com/cloud/api/");
        pmap.put("jsonrpc","2.0");
        pmap.put("secretKey","8153503006031672EF300005E5EF6AEF");
        pmap.put("sn","am00");
        pmap.put("agentLoginId","demoAgentTXWL12");
        pmap.put("nickname","demoAgentTXWL12");
        pmap.put("password","TXWLAGEBT12345678");
        pmap.put("KEY","TXWLAGEBT12345678");

        Map<String, String> pmap1 = new HashMap<>();
        pmap1.put("BG", JSONObject.fromObject(pmap).toString());

        String loginId="bl1wilson";

        BGGameServiceImpl bgGameService=new BGGameServiceImpl(pmap1);
        //创建账户
        //JSONObject jsonObject = bgGameService.openUserCreate(loginId, nickname, "open.user.create");
        //登录游戏
        //JSONObject jsonObject=bgGameService.openUserCommonAPI(loginId,"open.video.game.url","1","0","");
        //转入游戏
        //JSONObject jsonObject=bgGameService.openUserCommonAPI(loginId,"open.balance.transfer","1","125","");
        //转出游戏
        //JSONObject jsonObject=bgGameService.openUserCommonAPI(loginId,"open.balance.transfer","1","-15","");
        //查询余额
        //JSONObject jsonObject=bgGameService.openUserCommonAPI(loginId,"open.balance.get","1","0","");
        //查询转账记录
        //JSONObject jsonObject=bgGameService.openUserCommonAPI(loginId,"open.balance.transfer.query","1","0","");
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startTime=sdf.parse(sdf.format(new Date())+" 00:00:00");
        Date entTime=sdf.parse(sdf.format(new Date())+" 23:59:59");
        //根据订单号查单
        JSONObject jsonObject=bgGameService.checkBillNo(loginId,"open.balance.transfer.query","1536408101139");

        System.out.println(jsonObject.toString());
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
            String msg = openBalanceTransfer(ag_username, "open.balance.transfer", billno, credit + "", "IN",billno);
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
        String ag_username = gameTransferVO.getAg_username();
        String billno = gameTransferVO.getBillno();
        String credit = gameTransferVO.getMoney();
        String username = gameTransferVO.getUsername();
        String type = gameTransferVO.getType();
        try {
            String msg = openBalanceTransfer(ag_username, "open.balance.transfer", billno, credit + "", "OUT", billno);
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
     * 查询游戏转账订单
     */
    @Override
    public JSONObject queryTransferOrder(GameQueryOrderVO gameQueryOrderVO) {
        // TODO Auto-generated method stub
        return null;
    }

}
