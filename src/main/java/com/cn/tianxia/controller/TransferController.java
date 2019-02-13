package com.cn.tianxia.controller;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cn.tianxia.common.v2.KeyConstant;
import com.cn.tianxia.game.OBService;
import com.cn.tianxia.game.SBService;
import com.cn.tianxia.game.impl.AGGameServiceImpl;
import com.cn.tianxia.game.impl.AGINGameServiceImpl;
import com.cn.tianxia.game.impl.BBINGameServiceImpl;
import com.cn.tianxia.game.impl.BGGameServiceImpl;
import com.cn.tianxia.game.impl.CGGameServiceImpl;
import com.cn.tianxia.game.impl.CQJServiceimpl;
import com.cn.tianxia.game.impl.DSGameServiceImpl;
import com.cn.tianxia.game.impl.ESWServiceImpl;
import com.cn.tianxia.game.impl.GGBYGameServiceImpl;
import com.cn.tianxia.game.impl.GYGameServiceImpl;
import com.cn.tianxia.game.impl.HABAGameServiceImpl;
import com.cn.tianxia.game.impl.HGGameServiceImpl;
import com.cn.tianxia.game.impl.IBCGameServiceImpl;
import com.cn.tianxia.game.impl.IGGameServiceImpl;
import com.cn.tianxia.game.impl.IGPJGameServiceImpl;
import com.cn.tianxia.game.impl.JDBGameServiceImpl;
import com.cn.tianxia.game.impl.JFGameServiceImpl;
import com.cn.tianxia.game.impl.KYQPGameServiceImpl;
import com.cn.tianxia.game.impl.LYQPGameServiceImpl;
import com.cn.tianxia.game.impl.MGGameServiceImpl;
import com.cn.tianxia.game.impl.NBGameServiceImpl;
import com.cn.tianxia.game.impl.OBGameServiceImpl;
import com.cn.tianxia.game.impl.OGGameServiceImpl;
import com.cn.tianxia.game.impl.PSGameServiceImpl;
import com.cn.tianxia.game.impl.PTGameServiceImpl;
import com.cn.tianxia.game.impl.SBGameServiceImpl;
import com.cn.tianxia.game.impl.SWGameServiceImpl;
import com.cn.tianxia.game.impl.VGGameServiceImpl;
import com.cn.tianxia.game.impl.VRGameServiceImpl;
import com.cn.tianxia.po.BaseResponse;
import com.cn.tianxia.po.v2.ResponseCode;
import com.cn.tianxia.po.v2.ResultResponse;
import com.cn.tianxia.service.UserService;
import com.cn.tianxia.service.v2.UserGameTransferService;
import com.cn.tianxia.util.DESEncrypt;
import com.cn.tianxia.util.FileLog;
import com.cn.tianxia.util.IPTools;
import com.cn.tianxia.util.PlatFromConfig;
import com.cn.tianxia.util.UserTypeHandicapUtil;
import com.cn.tianxia.vo.EswLoginVo;
import com.cn.tianxia.vo.v2.TransferVO;
import com.cn.tianxia.ws.LoginUserResponse;
import com.cn.tianxia.ws.MoneyResponse;
import com.cn.tianxia.ws.QueryPlayerResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @ClassName TransferController
 * @Description 转账接口
 * @author Hardy
 * @Date 2019年1月27日 下午10:57:20
 * @version 1.0.0
 */
@RequestMapping("User")
@Controller
public class TransferController extends BaseController {

    // 日志
    private static final Logger logger = LoggerFactory.getLogger(TransferController.class);

    @Autowired
    private UserGameTransferService userGameTransferService;

    @Autowired
    private UserService userService;

    /**
     * @Description 天下平台转向游戏平台(转出,游戏上分)
     * @param session
     * @param request
     * @param response
     * @param credit
     * @param type
     * @param uuid
     * @param imgcode
     * @return
     */
    @RequestMapping("/TransferTo")
    @ResponseBody
    public JSONObject transferIn(HttpSession session, HttpServletRequest request, HttpServletResponse response,
            int credit, String type, String uuid, String imgcode) {
        logger.info("调用天下平台向游戏平台转入金额(游戏上分)接口开始================START=====================");
        // 创建返回结果对象
        JSONObject jo = new JSONObject();
        Object uidObj = session.getAttribute("uid");
        if (!ObjectUtils.anyNotNull(uidObj)) {
            // 用户ID为空,证明用户未登录
            jo.put("msg", "03");
            jo.put("errmsg", "用户ID为空,登录已过期,请重新登录");
            return jo;
        }
        // 转换用户ID
        String uid = String.valueOf(uidObj);
        // 缓存KEY
        String key = "TRANSFER:OUT:" + String.valueOf(uid);
        if (gameMap.containsKey(key)) {
            String oldType = gameMap.get(key).split(",")[0];
            long times = Long.parseLong(gameMap.get(key).split(",")[1]);
            long nowTimes = System.currentTimeMillis();
            if (type.equals(oldType) && nowTimes - times < 3000) {
                jo.put("msg", "05");
                jo.put("errmsg", type + "平台转账处理中,请稍后再试");
                return jo;
            }
        }
        gameMap.put(key, type + "," + String.valueOf(System.currentTimeMillis()));

        try {
            //从缓存中获取用户信息
            Map<String,String> userMap = loginmaps.get(uid);
            String ag_password = userMap.get("ag_password");
            String ag_username = userMap.get("ag_username");
            String hg_username = userMap.get("hg_username");
            String username = userMap.get("userName");//用户名称
            String cid = userMap.get("cid");//平台号
            String cagent = userMap.get("cagent");//平台编码
            String suuid = formatObjectParams(session.getAttribute("uuid"));
            String simgcode = formatObjectParams(session.getAttribute("imgcode"));
            String ip = IPTools.getIp(request);
            //查询游戏配置信息
            Map<String,String> pmap = userGameTransferService.getPlatformConfig();
            if(CollectionUtils.isEmpty(pmap)){
                logger.info("查询平台配置信息为空");
                return BaseResponse.error("0", "查询平台配置信息为空");
            }
            logger.info("查询平台配置信息结果:{}",JSONObject.fromObject(pmap).toString());
            // 校验请求参数
            JSONObject verifyData = verifyRequestParams(ag_password, ag_username, suuid, simgcode, type,
                    credit, uuid, imgcode);
            type = type.trim().toUpperCase();//格式化游戏编码
            if (!"success".equalsIgnoreCase(verifyData.getString("msg"))) {
                logger.info("用户【"+username+"】,调用天下平台向游戏平台转入金额(游戏上分)接口,校验请求参数结果:{}",verifyData.toString());
                return verifyData;
            }
            // 加密密码
            DESEncrypt d = new DESEncrypt(KeyConstant.DESKEY);
            String password = d.decrypt(ag_password);
            // 检查维护状态
            PlatFromConfig pf = new PlatFromConfig();
            pf.InitData(pmap, type);
            logger.info("用户【"+username+"】,调用天下平台向游戏平台转入金额(游戏上分)接口,查询平台【"+cagent+"】游戏维护状态:{}",pf.toString());
            if ("0".equals(pf.getPlatform_status())) {
                return BaseResponse.error("0", "process");
            }
            //查询平台游戏开关
            Map<String,String> cagentGameStatus = userGameTransferService.getPlatformStatusByCid(cid);
            if(CollectionUtils.isEmpty(cagentGameStatus)){
                logger.info("查询平台游戏开关状态为空");
                return BaseResponse.error("0", "process");
            }
            logger.info("查询平台游戏开关状态结果:{}",JSONObject.fromObject(cagentGameStatus).toString());
            if(cagentGameStatus.containsKey(type)){
                String cagentStatus = String.valueOf(cagentGameStatus.get(type));
                logger.info("用户【"+username+"】,调用天下平台向游戏平台转入金额(游戏上分)接口,查询平台【"+cagent+"】游戏开关状态:{}",cagentGameStatus);
                if(!"1".equals(cagentStatus)){
                    return BaseResponse.error("0", "process");
                }
            }
            //检查用户游戏状态
            checkGameReg(uid, password, ag_username, hg_username, type, ip, pmap);

            synchronized (this) {
                // 用户钱包余额
                double balance = userGameTransferService.getUserBalance(uid);
                if (balance < credit) {
                    return transferResponse("06", "转账失败,用户余额不足");
                }
                // 生成订单号
                String billno = generatorOrderNo(type, ag_username, pmap);
                if (StringUtils.isBlank(billno)) {
                    return transferResponse("error", "创建订单号为空");
                }
                logger.info("用户【"+username+"】,调用天下平台向游戏平台转入金额(游戏上分)接口,生成订单号:{}",billno);
                JSONObject transferResult = transferInProcess(type, ag_username, hg_username, username,billno, credit, password, ip, pmap);
                logger.info("用户【"+username+"】,调用天下平台向游戏平台转入金额(游戏上分)接口,发起第三方请求结果:{}",transferResult.toString());
                // 构建写入流水对象
                Map<String, Object> data = new HashMap<>();
                data.put("uid", uid);
                data.put("billno", billno);
                data.put("ag_username", ag_username.toLowerCase());
                data.put("t_type", "OUT");
                data.put("t_money", credit);
                data.put("old_money", balance);
                data.put("new_money", balance - credit);
                data.put("type", type);
                data.put("ip", ip);
                data.put("username", username);
                if ("success".equalsIgnoreCase(transferResult.getString("msg"))) {
                    // 转账成功,扣钱,写流水
                    return saveUserTransferSuccess(data, 1);
                } else if ("faild".equalsIgnoreCase(transferResult.getString("msg"))) {
                    // 转账失败,写流水
                    return saveUserTransferFaild(data);
                } else {
                    // 扣钱,写流水
                    return saveUserTransferOutFaild(data);
                }
            }
        } catch (Exception e) {
            logger.info("调用天下平台向游戏平台转入金额(游戏上分)接口异常:{}", e.getMessage());
        } finally {
            if (gameMap.containsKey(key)) {
                logger.info("删除缓存key:{}", key);
                gameMap.remove(key);
            }
            //写入日志
            FileLog f = new FileLog();
            Map<String, String> param = new HashMap<>();
            param.put("loginname", uid);
            param.put("suuid", uuid);
            param.put("Function", "TransferTo");
            f.setLog("zhuanzhang---" + type, param);
            //清除缓存
            session.setAttribute("imgcode", "");
            session.removeAttribute("uuid");
        }
        return transferResponse("error", "调用天下平台向游戏平台转入金额(游戏上分)失败");
    }

    /**
     * @Description 游戏平台转向天下平台(转入,游戏下分)
     * @param session
     * @param request
     * @param response
     * @param credit
     * @param type
     * @param uuid
     * @param imgcode
     * @return
     */
    @RequestMapping("/TransferFrom")
    @ResponseBody
    public JSONObject transferOut(HttpSession session, HttpServletRequest request, HttpServletResponse response,
            int credit, String type, String uuid, String imgcode) {
        logger.info("调用从游戏平台转向天下平台转出金额(游戏下分)开始===================start====================");
        // 创建返回结果对象
        JSONObject jo = new JSONObject();
        Object uidObj = session.getAttribute("uid");
        if (!ObjectUtils.anyNotNull(uidObj)) {
            // 用户ID为空,证明用户未登录
            jo.put("msg", "03");
            jo.put("errmsg", "用户ID为空,登录已过期,请重新登录");
            return jo;
        }
        // 转换用户ID
        String uid = String.valueOf(uidObj);
        // 缓存KEY
        String key = "TRANSFER:OUT:" + String.valueOf(uid);
        if (gameMap.containsKey(key)) {
            String oldType = gameMap.get(key).split(",")[0];
            long times = Long.parseLong(gameMap.get(key).split(",")[1]);
            long nowTimes = System.currentTimeMillis();
            if (type.equals(oldType) && nowTimes - times < 3000) {
                jo.put("msg", "05");
                jo.put("errmsg", type + "平台转账处理中,请稍后再试");
                return jo;
            }
        }
        gameMap.put(key, type + "," + String.valueOf(System.currentTimeMillis()));

        try {
            StringBuffer url = request.getRequestURL();
            String refurl = url.delete(url.length() - request.getRequestURI().length(), url.length())
                    .append("/").toString();
            //从缓存中获取用户信息
            Map<String,String> userMap = loginmaps.get(uid);
            String ag_password = userMap.get("ag_password");
            String ag_username = userMap.get("ag_username");
            String hg_username = userMap.get("hg_username");
            String username = userMap.get("userName");//用户名称
            String cid = userMap.get("cid");//平台号
            String cagent = userMap.get("cagent");//平台编码
            String suuid = formatObjectParams(session.getAttribute("uuid"));
            String simgcode = formatObjectParams(session.getAttribute("imgcode"));
            String ip = IPTools.getIp(request);
            //查询游戏配置信息
            Map<String,String> pmap = userGameTransferService.getPlatformConfig();
            if(CollectionUtils.isEmpty(pmap)){
                logger.info("查询平台配置信息为空");
                return BaseResponse.error("0", "查询平台配置信息为空");
            }
            logger.info("查询平台配置信息结果:{}",JSONObject.fromObject(pmap).toString());
            // 校验请求参数
            JSONObject verifyData = verifyRequestParams(ag_password, ag_username, suuid, simgcode, type,
                    credit, uuid, imgcode);
            type = type.trim().toUpperCase();//格式化游戏编码
            if (!"success".equalsIgnoreCase(verifyData.getString("msg"))) {
                logger.info("用户【"+username+"】,调用游戏平台向天下平台转入金额(游戏下分)接口,校验请求参数结果:{}",verifyData.toString());
                return verifyData;
            }
            //解密
            DESEncrypt d = new DESEncrypt(KeyConstant.DESKEY);
            String password = d.decrypt(ag_password);
            // 检查维护状态
            PlatFromConfig pf = new PlatFromConfig();
            pf.InitData(pmap, type);
            logger.info("用户【"+username+"】,调用游戏平台向天下平台转入金额(游戏下分)接口,查询平台【"+cagent+"】游戏维护状态:{}",pf.toString());
            if ("0".equals(pf.getPlatform_status())) {
                return BaseResponse.error("0", "process");
            }
            //查询平台游戏开关
            Map<String,String> cagentGameStatus = userGameTransferService.getPlatformStatusByCid(cid);
            if(CollectionUtils.isEmpty(cagentGameStatus)){
                logger.info("查询平台游戏开关状态为空");
                return BaseResponse.error("0", "process");
            }
            logger.info("查询平台游戏开关状态结果:{}",JSONObject.fromObject(cagentGameStatus).toString());
            if(cagentGameStatus.containsKey(type)){
                String cagentStatus = String.valueOf(cagentGameStatus.get(type));
                logger.info("用户【"+username+"】,调用游戏平台向天下平台转入金额(游戏下分)接口,查询平台【"+cagent+"】游戏开关状态:{}",cagentGameStatus);
                if(!"1".equals(cagentStatus)){
                    return BaseResponse.error("0", "process");
                }
            }
            //检查用户游戏状态
            checkGameReg(uid, password, ag_username, hg_username, type, ip, pmap);
            
            synchronized (this) {
                //查询用户游戏余额
                double gameBalance = getBalance(uid, password, ag_username, hg_username, type, ip, refurl, pmap).getDouble("balance");
                //判断游戏余额是否足够
                if (gameBalance < credit) {
                    return transferResponse("06", "转账失败,用户游戏余额不足");
                }
                // 用户钱包余额
                double balance = userGameTransferService.getUserBalance(uid);

                // 生成订单号
                String billno = generatorOrderNo(type, ag_username, pmap);
                if (StringUtils.isBlank(billno)) {
                    return transferResponse("error", "创建订单号为空");
                }
                logger.info("用户【"+username+"】,调用游戏平台向天下平台转入金额(游戏下分)接口,生成订单号:{}",billno);
                JSONObject transferResult = transferOutProcess(type, ag_username, hg_username, username,billno, credit, password, ip, pmap);
                logger.info("用户【"+username+"】,调用游戏平台向天下平台转入金额(游戏下分)接口,发起第三方请求响应结果:{}",billno);
                // 构建写入流水对象
                Map<String, Object> data = new HashMap<>();
                data.put("uid", uid);
                data.put("billno", billno);
                data.put("ag_username", ag_username.toLowerCase());
                data.put("t_type", "IN");
                data.put("t_money", credit);
                data.put("old_money", balance);
                data.put("new_money", balance + credit);
                data.put("type", type);
                data.put("ip", ip);
                data.put("username", username);
                if ("success".equalsIgnoreCase(transferResult.getString("msg"))) {
                    // 转账成功,扣钱,写流水
                    return saveUserTransferSuccess(data, 2);
                }else {
                    // 扣钱,写流水
                    return saveUserTransferFaild(data);
                }
            }
        } catch (Exception e) {
            logger.info("调用从游戏平台转向天下平台转出金额(游戏下分)接口异常:{}", e.getMessage());
        } finally {
            if (gameMap.containsKey(key)) {
                logger.info("删除缓存key:{}", key);
                gameMap.remove(key);
            }
            
            //写入日志
            FileLog f = new FileLog();
            Map<String, String> param = new HashMap<>();
            param.put("loginname", uid);
            param.put("suuid", uuid);
            param.put("Function", "TransferFrom");
            f.setLog("zhuanzhang---" + type, param);
            //清除缓存
            session.setAttribute("imgcode", "");
            session.removeAttribute("uuid");
        }
        return transferResponse("error", "调用从游戏平台转向天下平台转出金额(游戏下分)失败");
    }

    
    /**
     * 
     * @Description 获取用户游戏余额
     * @param request
     * @param response
     * @param BType
     * @return
     */
    private JSONObject getBalance(String uid,String ag_password,String ag_username,String hg_username,
                                            String BType,String ip,String refurl,Map<String,String> pmap) throws Exception {
        JSONObject jo = new JSONObject();
        try {
             if ("JDB".equals(BType)) {
                Map<String, Object> param = new HashMap<>();
                param.put("uid", uid);
                param.put("gametype", "is_jdb");
                Map<String, String> user = userService.selectUserGameStatus(param).get(0);
                Object o = user.get("cnt");
                if ("0".equals(o.toString())) {
                    jo.put("balance", "0");
                    return jo;
                }
                // 获取JDB余额
                JDBGameServiceImpl jdb = new JDBGameServiceImpl(pmap);
                String balance = jdb.getBalance(ag_username);
                if ("error".equals(balance)) {
                    jo.put("balance", "0.00");
                    return jo;
                }
                jo.put("balance", balance);
                return jo;
            } else if ("AG".equals(BType)) {
                Map<String, Object> param = new HashMap<>();
                param.put("uid", uid);
                param.put("gametype", "is_ag");
                Map<String, String> user = userService.selectUserGameStatus(param).get(0);
                Object o = user.get("cnt");
                if ("0".equals(o.toString())) {
                    jo.put("balance", "0");
                    return jo;
                }
                // 获取AG余额
                AGGameServiceImpl agService = new AGGameServiceImpl(pmap);
                String msg = agService.CheckOrCreateGameAccout(ag_username, ag_password, "A", "CNY");
                if ("0".equals(msg)) {
                    String balance = agService.GetBalance(ag_username, ag_password, "CNY");
                    if (balance == null || balance == "") {
                        jo.put("balance", "0.00");
                        return jo;
                    } else {
                        jo.put("balance", balance);
                        return jo;
                    }
                } else {
                    jo.put("balance", "0.00");
                    return jo;
                }
            } else if ("AGIN".equals(BType)) {
                Map<String, Object> param = new HashMap<>();
                param.put("uid", uid);
                param.put("gametype", "is_agin");
                Map<String, String> user = userService.selectUserGameStatus(param).get(0);
                Object o = user.get("cnt");
                if ("0".equals(o.toString())) {
                    jo.put("balance", "0");
                    return jo;
                }
                // 获取AG余额
                AGINGameServiceImpl agService = new AGINGameServiceImpl(pmap);
                String msg = agService.CheckOrCreateGameAccout(ag_username, ag_password, "A", "CNY");
                if ("0".equals(msg)) {
                    String balance = agService.GetBalance(ag_username, ag_password, "CNY");
                    if (balance == null || balance == "") {
                        jo.put("balance", "0.00");
                        return jo;
                    } else {
                        jo.put("balance", balance);
                        return jo;
                    }
                } else {
                    jo.put("balance", "0.00");
                    return jo;
                }
            } else if ("BBIN".equals(BType)) {
                Map<String, Object> param = new HashMap<>();
                param.put("uid", uid);
                param.put("gametype", "is_bbin");
                Map<String, String> user = userService.selectUserGameStatus(param).get(0);
                Object o = user.get("cnt");
                if ("0".equals(o.toString())) {
                    jo.put("balance", "0");
                    return jo;
                }
                // 获取BBIN余额
                BBINGameServiceImpl bbinService = new BBINGameServiceImpl(pmap);
                String msg = bbinService.CheckUsrBalance(ag_username, ag_password);
                JSONObject json;
                json = JSONObject.fromObject(msg);
                if ("true".equals(json.get("result").toString())) {
                    JSONArray jsonArray = JSONArray.fromObject(json.getString("data"));
                    json = jsonArray.getJSONObject(0);
                    jo.put("balance", json.get("Balance").toString());
                } else {
                    jo.put("balance", "0.00");
                    return jo;
                }
            } else if ("DS".equals(BType)) {
                Map<String, Object> param = new HashMap<>();
                param.put("uid", uid);
                param.put("gametype", "is_ds");
                Map<String, String> user = userService.selectUserGameStatus(param).get(0);
                Object o = user.get("cnt");
                if ("0".equals(o.toString())) {
                    jo.put("balance", "0");
                    return jo;
                }
                DSGameServiceImpl ds = new DSGameServiceImpl(pmap);
                String msg = ds.getBalance(ag_username, ag_password);
                JSONObject json;
                json = JSONObject.fromObject(msg);
                if ("0".equals(json.get("errorCode").toString())) {
                    json = json.getJSONObject("params");
                    jo.put("balance", json.get("balance").toString());
                } else {
                    jo.put("balance", "0.00");
                    return jo;
                }
            } else if ("OB".equals(BType)) {
                Map<String, Object> param = new HashMap<>();
                param.put("uid", uid);
                param.put("gametype", "is_ob");
                Map<String, String> user = userService.selectUserGameStatus(param).get(0);
                Object o = user.get("cnt");
                if ("0".equals(o.toString())) {
                    jo.put("balance", "0");
                    return jo;
                }
                OBService ob = new OBGameServiceImpl(pmap);
                String msg = ob.get_balance(ag_username, ag_password);
                JSONObject json;
                json = JSONObject.fromObject(msg);
                if ("OK".equals(json.get("error_code").toString())) {
                    jo.put("balance", json.get("balance").toString());
                } else {
                    jo.put("balance", "0.00");
                    return jo;
                }
            } else if ("OG".equals(BType)) {
                Map<String, Object> param = new HashMap<>();
                param.put("uid", uid);
                param.put("gametype", "is_og");
                Map<String, String> user = userService.selectUserGameStatus(param).get(0);
                Object o = user.get("cnt");
                if ("0".equals(o.toString())) {
                    jo.put("balance", "0");
                    return jo;
                }
                OGGameServiceImpl og = new OGGameServiceImpl(pmap);
                String a = og.getBalance(ag_username, ag_password);
                try {
                    String str = a.substring(a.indexOf("<result>") + 8, a.indexOf("</result>"));
                    jo.put("balance", str);
                } catch (Exception e) {
                    jo.put("balance", "0.00");
                    return jo;
                }
            } else if ("SB".equals(BType)) {
                Map<String, Object> param = new HashMap<>();
                param.put("uid", uid);
                param.put("gametype", "is_shenbo");
                Map<String, String> user = userService.selectUserGameStatus(param).get(0);
                Object o = user.get("cnt");
                if ("0".equals(o.toString())) {
                    jo.put("balance", "0");
                    return jo;
                }
                SBService s = new SBGameServiceImpl(pmap);
                String atoken = s.getAccToken();
                JSONObject json = new JSONObject();
                json = JSONObject.fromObject(atoken);
                try {
                    atoken = json.get("access_token").toString();
                    String j = s.getBalance(ag_username, atoken);
                    json = JSONObject.fromObject(j);
                    if (json.get("bal").toString() == null || json.get("bal").toString() == "") {
                        jo.put("balance", "0.00");
                        return jo;
                    }
                    jo.put("balance", json.get("bal").toString());
                } catch (Exception e) {
                    jo.put("balance", "0.00");
                    return jo;
                }
            } else if ("MG".equals(BType)) {
                Map<String, Object> param = new HashMap<>();
                param.put("uid", uid);
                param.put("gametype", "is_mg");
                Map<String, String> user = userService.selectUserGameStatus(param).get(0);
                Object o = user.get("cnt");
                if ("0".equals(o.toString())) {
                    jo.put("balance", "0");
                    return jo;
                }
                MGGameServiceImpl m = new MGGameServiceImpl(pmap);
                Map<String, String> gmap = new HashMap<String, String>();
                gmap.put("ClientIP", ip);
                JSONObject json = m.queryBalance(ag_username, ag_password, gmap);
                try {
                    if ("success".equals(json.get("Code").toString())) {
                        jo.put("balance", json.get("Balance").toString());
                    } else {
                        jo.put("balance", "0.00");
                    }
                } catch (Exception e) {
                    jo.put("balance", "0.00");
                }

            } else if ("HABA".equals(BType)) {
                Map<String, Object> param = new HashMap<>();
                param.put("uid", uid);
                param.put("gametype", "is_haba");
                Map<String, String> user = userService.selectUserGameStatus(param).get(0);
                Object o = user.get("cnt");
                if ("0".equals(o.toString())) {
                    jo.put("balance", "0");
                    return jo;
                }
                HABAGameServiceImpl h = new HABAGameServiceImpl(pmap);
                QueryPlayerResponse qp = h.queryPlayer(ag_username, ag_password, null);
                if (qp.isFound() == true) {
                    jo.put("balance", qp.getRealBalance());
                } else {
                    jo.put("balance", "0.00");
                }
            } else if ("PT".equals(BType)) {
                Map<String, Object> param = new HashMap<>();
                param.put("uid", uid);
                param.put("gametype", "is_pt");
                Map<String, String> user = userService.selectUserGameStatus(param).get(0);
                Object o = user.get("cnt");
                if ("0".equals(o.toString())) {
                    jo.put("balance", "0");
                    return jo;
                }
                PTGameServiceImpl p = new PTGameServiceImpl(pmap);
                try {
                    JSONObject json = JSONObject.fromObject(p.GetPlayerInfo(ag_username));
                    json = json.getJSONObject("result");
                    if (json == null || "".equals(json.toString())) {
                        jo.put("balance", "0.00");
                    } else {
                        String balance = json.getString("BALANCE").toString();
                        jo.put("balance", balance);
                    }
                } catch (Exception e) {
                    jo.put("balance", "0.00");
                    return jo;
                }

            } else if ("GGBY".equals(BType)) {
                Map<String, Object> param = new HashMap<>();
                param.put("uid", uid);
                param.put("gametype", "is_ggby");
                Map<String, String> user = userService.selectUserGameStatus(param).get(0);
                Object o = user.get("cnt");
                if ("0".equals(o.toString())) {
                    jo.put("balance", "0");
                    return jo;
                }
                GGBYGameServiceImpl gg = new GGBYGameServiceImpl(pmap);
                try {
                    String msg = gg.GetBalance(ag_username, ag_password);
                    jo.put("balance", msg);
                } catch (Exception e) {
                    jo.put("balance", "0.00");
                    return jo;
                }

            } else if ("CG".equals(BType)) {
                Map<String, Object> param = new HashMap<>();
                param.put("uid", uid);
                param.put("gametype", "is_cg");
                Map<String, String> user = userService.selectUserGameStatus(param).get(0);
                Object o = user.get("cnt");
                if ("0".equals(o.toString())) {
                    jo.put("balance", "0");
                    return jo;
                }
                CGGameServiceImpl c = new CGGameServiceImpl(pmap);
                String msg = c.getBalance(ag_username, ag_password);
                JSONObject json;
                json = JSONObject.fromObject(msg);
                if ("0".equals(json.get("errorCode").toString())) {
                    json = json.getJSONObject("params");
                    jo.put("balance", json.get("balance").toString());
                } else {
                    jo.put("balance", "0.00");
                    return jo;
                }
            } else if ("IG".equals(BType)) {
                Map<String, Object> param = new HashMap<>();
                param.put("uid", uid);
                param.put("gametype", "is_ig");
                Map<String, String> user = userService.selectUserGameStatus(param).get(0);
                Object o = user.get("cnt");
                if ("0".equals(o.toString())) {
                    jo.put("balance", "0");
                    return jo;
                }
                IGGameServiceImpl c = new IGGameServiceImpl(pmap);
                String msg = c.getBalance(ag_username, ag_password);
                JSONObject json;
                json = JSONObject.fromObject(msg);
                if ("0".equals(json.get("errorCode").toString())) {
                    json = json.getJSONObject("params");
                    jo.put("balance", json.get("balance").toString());
                } else {
                    jo.put("balance", "0.00");
                    return jo;
                }
            } else if ("IGPJ".equals(BType)) {
                Map<String, Object> param = new HashMap<>();
                param.put("uid", uid);
                param.put("gametype", "is_igpj");
                Map<String, String> user = userService.selectUserGameStatus(param).get(0);
                Object o = user.get("cnt");
                if ("0".equals(o.toString())) {
                    jo.put("balance", "0");
                    return jo;
                }
                IGPJGameServiceImpl c = new IGPJGameServiceImpl(pmap);
                String msg = c.getBalance(ag_username, ag_password);
                JSONObject json;
                json = JSONObject.fromObject(msg);
                if ("0".equals(json.get("errorCode").toString())) {
                    json = json.getJSONObject("params");
                    jo.put("balance", json.get("balance").toString());
                } else {
                    jo.put("balance", "0.00");
                    return jo;
                }
            } else if ("HG".equals(BType)) {
                Map<String, Object> param = new HashMap<>();
                param.put("uid", uid);
                param.put("gametype", "is_hg");
                Map<String, String> user = userService.selectUserGameStatus(param).get(0);
                Object o = user.get("cnt");
                if ("0".equals(o.toString())) {
                    jo.put("balance", "0");
                    return jo;
                }
                HGGameServiceImpl c = new HGGameServiceImpl(pmap);
                String msg = c.getBalance(hg_username);
                if (!"error".equals(msg)) {
                    jo.put("balance", msg);
                } else {
                    jo.put("balance", "0.00");
                    return jo;
                }
            } else if ("BG".equals(BType)) {
                Map<String, Object> param = new HashMap<>();
                param.put("uid", uid);
                param.put("gametype", "is_bg");
                Map<String, String> user = userService.selectUserGameStatus(param).get(0);
                Object o = user.get("cnt");
                if ("0".equals(o.toString())) {
                    jo.put("balance", "0");
                    return jo;
                }
                BGGameServiceImpl c = new BGGameServiceImpl(pmap);
                JSONObject jsonObjec = c.openUserCommonAPI(ag_username, "open.balance.get", "", "", refurl);
                if ("success".equals(jsonObjec.get("code"))) {
                    jo.put("balance", JSONObject.fromObject(jsonObjec.get("params")).get("result"));
                } else {
                    jo.put("balance", "0.00");
                    return jo;
                }
            } else if ("VR".equals(BType)) {
                Map<String, Object> param = new HashMap<>();
                param.put("uid", uid);
                param.put("gametype", "is_vr");
                Map<String, String> user = userService.selectUserGameStatus(param).get(0);
                Object o = user.get("cnt");
                if ("0".equals(o.toString())) {
                    jo.put("balance", "0");
                    return jo;
                }
                VRGameServiceImpl v = new VRGameServiceImpl(pmap);
                String balance = v.getBalance(ag_username);
                try {
                    BigDecimal big = new BigDecimal(balance);
                    if (big.compareTo(BigDecimal.ZERO) < 0) {
                        balance = "0.0";
                    }
                } catch (Exception e) {
                    balance = "0.0";
                }
                jo.put("balance", balance);
                return jo;
            } else if ("JF".equals(BType)) {
                Map<String, Object> param = new HashMap<>();
                param.put("uid", uid);
                param.put("gametype", "is_jf");
                Map<String, String> user = userService.selectUserGameStatus(param).get(0);
                Object o = user.get("cnt");
                if ("0".equals(o.toString())) {
                    jo.put("balance", "0");
                    return jo;
                }
                JFGameServiceImpl jf = new JFGameServiceImpl(pmap);
                String balance = jf.GetBalance(ag_username, ag_password);
                jo.put("balance", balance);
                return jo;
            } else if ("KYQP".equals(BType)) {
                Map<String, Object> param = new HashMap<>();
                param.put("uid", uid);
                param.put("gametype", "is_kyqp");
                Map<String, String> user = userService.selectUserGameStatus(param).get(0);
                Object o = user.get("cnt");
                if ("0".equals(o.toString())) {
                    jo.put("balance", "0");
                    return jo;
                }
                KYQPGameServiceImpl k = new KYQPGameServiceImpl(pmap);
                String balance = k.queryUnderTheBalance(ag_username);
                if ("error".equals(balance)) {
                    jo.put("balance", "0.00");
                } else {
                    balance = JSONObject.fromObject(balance).getJSONObject("d").getString("money");
                    jo.put("balance", balance);
                }
                return jo;
            } /*
               * else if ("ESW".equals(BType)) { Map<String, Object> param = new HashMap<>(); param.put("uid", uid);
               * param.put("gametype", "is_esw"); Map<String, String> user =
               * userService.selectUserGameStatus(param).get(0); Object o = user.get("cnt"); if
               * ("0".equals(o.toString())) { jo.put("balance", "0"); return jo; } ESWServiceImpl eswService = new
               * ESWServiceImpl(pmap); String data = eswService.queryUserInfo(ag_username); JSONObject jsonObject =
               * JSONObject.fromObject(data); if (jsonObject.getInt("code") == 0) { jo.put("balance",
               * jsonObject.getString("money")); } else { if(jsonObject.getInt("code") == 1012){ jo.put("balance",
               * "userCode有误，在平台服务器找不到对应的用户"); }else { jo.put("balance", "维护中"); } } return jo; }
               */else if ("LYQP".equals(BType)) {
                Map<String, Object> param = new HashMap<>();
                param.put("uid", uid);
                param.put("gametype", "is_lyqp");
                Map<String, String> user = userService.selectUserGameStatus(param).get(0);
                Object o = user.get("cnt");
                if ("0".equals(o.toString())) {
                    jo.put("balance", "0");
                    return jo;
                }
                LYQPGameServiceImpl lyqp = new LYQPGameServiceImpl(pmap);
                String balance = lyqp.queryUnderTheBalance(ag_username);
                if ("error".equals(balance)) {
                    jo.put("balance", "0.00");
                } else {
                    balance = JSONObject.fromObject(balance).getJSONObject("d").getString("money");
                    jo.put("balance", balance);
                }
                return jo;
            } else if ("VG".equals(BType)) {
                Map<String, Object> param = new HashMap<>();
                param.put("uid", uid);
                param.put("gametype", "is_vg");
                Map<String, String> user = userService.selectUserGameStatus(param).get(0);
                Object o = user.get("cnt");
                if ("0".equals(o.toString())) {
                    jo.put("balance", "0");
                    return jo;
                }
                VGGameServiceImpl vg = new VGGameServiceImpl(pmap);
                String balance = vg.balance(ag_username);
                if ("error".equals(balance)) {
                    jo.put("balance", "0.00");
                } else {
                    jo.put("balance", balance);
                }
                return jo;

            } else if ("GY".equals(BType)) {
                Map<String, Object> param = new HashMap<>();
                param.put("uid", uid);
                param.put("gametype", "is_gy");
                Map<String, String> user = userService.selectUserGameStatus(param).get(0);
                Object o = user.get("cnt");
                if ("0".equals(o.toString())) {
                    jo.put("balance", "0");
                    return jo;
                }
                GYGameServiceImpl gy = new GYGameServiceImpl(pmap);
                String balance = gy.balance(ag_username, ag_password);
                if ("error".equals(balance)) {
                    jo.put("balance", "0.00");
                } else {
                    jo.put("balance", balance);
                }
                return jo;
            } else if ("PS".equals(BType)) {
                Map<String, Object> param = new HashMap<>();
                param.put("uid", uid);
                param.put("gametype", "is_ps");
                Map<String, String> user = userService.selectUserGameStatus(param).get(0);
                Object o = user.get("cnt");
                if ("0".equals(o.toString())) {
                    jo.put("balance", "0");
                    return jo;
                }
                PSGameServiceImpl ps = new PSGameServiceImpl(pmap);
                String balance = ps.balance(ag_username);
                if ("error".equals(balance)) {
                    jo.put("balance", "0.00");
                } else {
                    jo.put("balance", balance);
                }
                return jo;
            } else if ("NB".equals(BType)) {
                Map<String, Object> param = new HashMap<>();
                param.put("uid", uid);
                param.put("gametype", "is_nb");
                Map<String, String> user = userService.selectUserGameStatus(param).get(0);
                Object o = user.get("cnt");
                if ("0".equals(o.toString())) {
                    jo.put("balance", "0");
                    return jo;
                }
                NBGameServiceImpl nb = new NBGameServiceImpl(pmap);
                String balance = nb.balance(ag_username);
                if ("error".equals(balance)) {
                    jo.put("balance", "0.00");
                } else {
                    jo.put("balance", balance);
                }
                return jo;
            } else if ("SW".equals(BType)) {
                Map<String, Object> param = new HashMap<>();
                param.put("uid", uid);
                param.put("gametype", "is_sw");
                Map<String, String> user = userService.selectUserGameStatus(param).get(0);
                Object o = user.get("cnt");
                if ("0".equals(o.toString())) {
                    jo.put("balance", "0");
                    return jo;
                }
                SWGameServiceImpl sw = new SWGameServiceImpl(pmap);
                String balance = sw.getBalance(ag_username);
                if ("error".equals(balance)) {
                    jo.put("balance", "0.00");
                } else {
                    jo.put("balance", balance);
                }
                return jo;
            } else if ("CQJ".equals(BType)) {
                Map<String, Object> param = new HashMap<>();
                param.put("uid", uid);
                param.put("gametype", "is_cqj");
                Map<String, String> user = userService.selectUserGameStatus(param).get(0);
                Object o = user.get("cnt");
                if ("0".equals(o.toString())) {
                    jo.put("balance", "0");
                    return jo;
                }
                CQJServiceimpl cqj = new CQJServiceimpl(pmap);
                String str = cqj.findAccount(ag_username);
                if ("error".equals(str)) {
                    jo.put("balance", "维护中");
                } else {
                    jo.put("balance", str);
                }
            } else if ("ESW".equals(BType)) {
                Map<String, Object> param = new HashMap<>();
                param.put("uid", uid);
                param.put("gametype", "is_esw");
                Map<String, String> user = userService.selectUserGameStatus(param).get(0);
                Object o = user.get("cnt");
                if("0".equals(o.toString())) {
                    jo.put("balance", "0");
                    return jo;
                }
                ESWServiceImpl eswService = new ESWServiceImpl(pmap);
                String data = eswService.queryUserInfo(ag_username);
                JSONObject jsonObject = JSONObject.fromObject(data);
                if (jsonObject.getInt("code") == 0) {
                    jo.put("balance",jsonObject.getString("money"));
                } else {
                    if (jsonObject.getInt("code") == 1012) {
                        jo.put("balance","userCode有误，在平台服务器找不到对应的用户");
                    } else {
                        jo.put("balance", "维护中");
                    }
                }
                return jo;
            }else if ("IBC".equals(BType)) {
                Map<String, Object> param = new HashMap<>();
                param.put("uid", uid);
                param.put("gametype", "is_ibc");
                Map<String, String> user = userService.selectUserGameStatus(param).get(0);
                Object o = user.get("cnt");
                if("0".equals(o.toString())) {
                    jo.put("balance", "0");
                    return jo;
                }
                IBCGameServiceImpl service = new IBCGameServiceImpl(pmap);
                ResultResponse data = service.getBalance(ag_username);
                if (data.getStatus() == ResponseCode.SUCCESS_STATUS) {
                    jo.put("balance",data.getBalance());
                } else {
                    jo.put("balance", "维护中");
                }
                return jo;
            }else {
                jo.put("balance", "0.00");
                return jo;
            }
        } catch (Exception e) {
            jo.put("balance", "0.00");
            return jo;
        }
        return jo;
    }


    /**
     * 
     * @Description 检查用户游戏状态
     * @param uid 用户ID
     * @param ag_password 游戏登录密码(密码为解密后的)
     * @param ag_username
     * @param hg_username
     * @param type
     * @param ip
     * @param pmap
     * @throws Exception
     */
    private void checkGameReg(String uid,String ag_password,String ag_username,String hg_username,
                                        String type,String ip,Map<String,String> pmap) throws Exception {
        if ("IGLOTTO".equals(type) || "IGLOTTERY".equals(type)) {
            type = "IG";
        }
        if ("IGPJLOTTO".equals(type) || "IGLOTTERY".equals(type)) {
            type = "IGPJ";
        }
        if ("AGIN".equals(type) || type == "AGIN" || "AGBY".equals(type) || type.equals("AGBY") || "YOPLAY".equals(type)
                || type == "YOPLAY" || type.equals("TASSPTA") || type == "TASSPTA") {
            String msg = "";
            // 查询用户是否在平台注册
            AGINGameServiceImpl agService = new AGINGameServiceImpl(pmap);
            Map<String, Object> param = new HashMap<>();
            param.put("uid", uid);
            param.put("gametype", "is_agin");
            Map<String, String> user = userService.selectUserGameStatus(param).get(0);
            Object o = user.get("cnt");
            if ("0".equals(o.toString())) {
                // 如果未注册AG平台,注册AG用户,并记录
                msg = agService.CheckOrCreateGameAccout(ag_username, ag_password, "A", "CNY");
                if ("0".equals(msg)) {
                    userService.insertUserGameStatus(param);
                }
            }
        } else if ("AG".equals(type) || type == "AG") {
            String msg = "";
            // 查询用户是否在平台注册
            AGGameServiceImpl agService = new AGGameServiceImpl(pmap);
            Map<String, Object> param = new HashMap<>();
            param.put("uid", uid);
            param.put("gametype", "is_ag");
            Map<String, String> user = userService.selectUserGameStatus(param).get(0);
            Object o = user.get("cnt");
            if ("0".equals(o.toString())) {
                // 如果未注册AG平台,注册AG用户,并记录
                msg = agService.CheckOrCreateGameAccout(ag_username, ag_password, "A", "CNY");
                if ("0".equals(msg)) {
                    userService.insertUserGameStatus(param);
                }
            }
        } else if ("BBIN".equals(type) || type == "BBIN") {
            String msg = "";
            JSONObject json = new JSONObject();
            BBINGameServiceImpl b = new BBINGameServiceImpl(pmap);

            // 查询用户是否在平台注册,如果未注册则注册用户,并记录
            Map<String, Object> param = new HashMap<>();
            param.put("uid", uid);
            param.put("gametype", "is_bbin");
            Map<String, String> user = userService.selectUserGameStatus(param).get(0);
            Object o = user.get("cnt");
            if ("0".equals(o.toString())) {
                msg = b.CreateMember(ag_username, ag_password);
                json = JSONObject.fromObject(msg);
                if ("true".equals(json.getString("result")) || msg.indexOf("The account is repeated") > -1) {
                    userService.insertUserGameStatus(param);
                }
            }
        } else if ("DS".equals(type) || type == "DS") {
            String msg = "";
            JSONObject json = new JSONObject();
            DSGameServiceImpl ds = new DSGameServiceImpl(pmap);

            // 查询用户是否在平台注册,如果未注册则注册用户,并记录
            Map<String, Object> param = new HashMap<>();
            param.put("uid", uid);
            param.put("gametype", "is_ds");
            Map<String, String> user = userService.selectUserGameStatus(param).get(0);
            Object o = user.get("cnt");
            if ("0".equals(o.toString())) {
                msg = ds.LoginGame(ag_username, ag_password);
                json = JSONObject.fromObject(msg);
                if ("0".equals(json.getString("errorCode"))) {
                    userService.insertUserGameStatus(param);
                }
            }

        } else if ("OB".equals(type) || type == "OB") {
            String msg = "";
            OBService ob = new OBGameServiceImpl(pmap);
            Map<String, Object> param = new HashMap<>();
            param.put("uid", uid);
            param.put("gametype", "is_ob");
            Map<String, String> user = userService.selectUserGameStatus(param).get(0);
            Object o = user.get("cnt");
            if ("0".equals(o.toString())) {
                msg = ob.check_or_create(ag_username, ag_password);
                if ("success".equals(msg)) {
                    userService.insertUserGameStatus(param);
                }
            }

        } else if ("OG".equals(type) || type == "OG") {
            String msg = "";
            OGGameServiceImpl og = new OGGameServiceImpl(pmap);
            Map<String, Object> param = new HashMap<>();
            param.put("uid", uid);
            param.put("gametype", "is_og");
            Map<String, String> user = userService.selectUserGameStatus(param).get(0);
            Object o = user.get("cnt");
            if ("0".equals(o.toString())) {
                msg = og.CreateMem(ag_username, ag_password);
                msg = msg.substring(msg.indexOf("<result>") + 8, msg.indexOf("</result>"));
                if ("1".equals(msg)) {
                    userService.insertUserGameStatus(param);
                }
            }
        } else if ("SB".equals(type) || type == "SB") {
            SBService s = new SBGameServiceImpl(pmap);
            Map<String, Object> param = new HashMap<>();
            param.put("uid", uid);
            param.put("gametype", "is_shenbo");
            Map<String, String> user = userService.selectUserGameStatus(param).get(0);
            Object o = user.get("cnt");
            if ("0".equals(o.toString())) {
                // 设置盘口 默认设置申博默认盘口1-5
                UserTypeHandicapUtil uth = new UserTypeHandicapUtil();
                String handicap = uth.getHandicap("SB", uid, userService);
                handicap = handicap.isEmpty() ? "4" : handicap;

                String atoken = s.getAccToken();
                JSONObject json = new JSONObject();
                json = JSONObject.fromObject(atoken);
                atoken = s.getUserToken(ip, ag_username, ag_username,
                        json.get("access_token").toString(), handicap, "");
                json = JSONObject.fromObject(atoken);
                if (!"".equals(json.getString("authtoken"))) {
                    userService.insertUserGameStatus(param);
                }
            }
        } else if ("MG".equals(type) || type == "MG") {
            JSONObject json = new JSONObject();
            MGGameServiceImpl m = new MGGameServiceImpl(pmap);
            Map<String, Object> param = new HashMap<>();
            param.put("uid", uid);
            param.put("gametype", "is_mg");
            Map<String, String> user = userService.selectUserGameStatus(param).get(0);
            Object o = user.get("cnt");
            if ("0".equals(o.toString())) {
                json = m.createAccount(ag_username, ag_password, null);
                if ("success".equals(json.getString("Code"))) {
                    userService.insertUserGameStatus(param);
                }
            }

        } else if ("PT".equals(type) || type == "PT") {
            String msg = "";
            PTGameServiceImpl p = new PTGameServiceImpl(pmap);
            Map<String, Object> param = new HashMap<>();
            param.put("uid", uid);
            param.put("gametype", "is_pt");
            Map<String, String> user = userService.selectUserGameStatus(param).get(0);
            Object o = user.get("cnt");
            if ("0".equals(o.toString())) {
                msg = p.CreatePlayer(ag_username, ag_password);
                if ("success".equals(msg)) {
                    userService.insertUserGameStatus(param);
                }
            }
        } else if ("HABA".equals(type) || type == "HABA") {
            HABAGameServiceImpl h = new HABAGameServiceImpl(pmap);
            Map<String, Object> params = new HashMap<String, Object>();
            Map<String, Object> param = new HashMap<>();
            param.put("uid", uid);
            param.put("gametype", "is_haba");
            Map<String, String> user = userService.selectUserGameStatus(param).get(0);
            Object o = user.get("cnt");
            if ("0".equals(o.toString())) {
                LoginUserResponse lu = h.loginOrCreatePlayer(ag_username, ag_password, params);
                if (lu.isPlayerCreated() || lu.getMessage() == null || "null".equals(lu.getMessage())) {
                    userService.insertUserGameStatus(param);
                }
            }
        } else if ("JDB".equals(type) || type == "JDB") {
            JDBGameServiceImpl jdb = new JDBGameServiceImpl(pmap);
            Map<String, Object> param = new HashMap<>();
            param.put("uid", uid);
            param.put("gametype", "is_jdb");
            Map<String, String> user = userService.selectUserGameStatus(param).get(0);
            Object o = user.get("cnt");
            if ("0".equals(o.toString())) {
                String msg = jdb.createUser(ag_username);
                if ("success".equals(msg)) {
                    userService.insertUserGameStatus(param);
                }
            }
        } else if ("GGBY".equals(type) || type == "GGBY") {
            String msg = "";
            GGBYGameServiceImpl gg = new GGBYGameServiceImpl(pmap);
            Map<String, Object> param = new HashMap<>();
            param.put("uid", uid);
            param.put("gametype", "is_ggby");
            Map<String, String> user = userService.selectUserGameStatus(param).get(0);
            Object o = user.get("cnt");
            if ("0".equals(o.toString())) {
                msg = gg.CheckOrCreateGameAccout(ag_username, ag_password);
                if ("0".equals(msg)) {
                    userService.insertUserGameStatus(param);
                }
            }

        } else if ("CG".equals(type) || type == "CG") {
            String msg = "";
            JSONObject json = new JSONObject();
            CGGameServiceImpl c = new CGGameServiceImpl(pmap);
            // 查询用户是否在平台注册,如果未注册则注册用户,并记录
            Map<String, Object> param = new HashMap<>();
            param.put("uid", uid);
            param.put("gametype", "is_cg");
            Map<String, String> user = userService.selectUserGameStatus(param).get(0);
            Object o = user.get("cnt");
            if ("0".equals(o.toString())) {
                msg = c.LoginGame(ag_username, ag_password);
                json = JSONObject.fromObject(msg);
                if ("0".equals(json.getString("errorCode"))) {
                    userService.insertUserGameStatus(param);
                }
            }
        } else if ("IG".equals(type) || type == "IG") {
            String msg = "";
            JSONObject json = new JSONObject();
            IGGameServiceImpl c = new IGGameServiceImpl(pmap);
            // 查询用户是否在平台注册,如果未注册则注册用户,并记录
            Map<String, Object> param = new HashMap<>();
            param.put("uid", uid);
            param.put("gametype", "is_ig");
            Map<String, String> user = userService.selectUserGameStatus(param).get(0);
            Object o = user.get("cnt");
            if ("0".equals(o.toString())) {
                // 首次登录默认设置A盘口
                msg = c.LoginGame(ag_username, ag_password, "LOTTERY", "", "PC", "A");
                if (msg != null && !"null".equals(msg)) {
                    json = JSONObject.fromObject(msg);
                    if ("0".equals(json.getString("errorCode"))) {
                        userService.insertUserGameStatus(param);
                    }
                }
            }
        } else if ("IGPJ".equals(type) || type == "IGPJ") {
            String msg = "";
            JSONObject json = new JSONObject();
            IGPJGameServiceImpl c = new IGPJGameServiceImpl(pmap);
            // 查询用户是否在平台注册,如果未注册则注册用户,并记录
            Map<String, Object> param = new HashMap<>();
            param.put("uid", uid);
            param.put("gametype", "is_igpj");
            Map<String, String> user = userService.selectUserGameStatus(param).get(0);
            Object o = user.get("cnt");
            if ("0".equals(o.toString())) {
                // 首次登录游戏默认设置 A盘口
                msg = c.LoginGame(ag_username, ag_password, "LOTTERY", "", "PC", "A");
                if (msg != null && !"null".equals(msg)) {
                    json = JSONObject.fromObject(msg);
                    if ("0".equals(json.getString("errorCode"))) {
                        userService.insertUserGameStatus(param);
                    }
                }
            }
        } else if ("HG".equals(type) || type == "HG") {
            String msg = "";
            HGGameServiceImpl c = new HGGameServiceImpl(pmap);
            // 查询用户是否在平台注册,如果未注册则注册用户,并记录
            Map<String, Object> param = new HashMap<>();
            param.put("uid", uid);
            param.put("gametype", "is_hg");
            Map<String, String> user = userService.selectUserGameStatus(param).get(0);
            Object o = user.get("cnt");
            if ("0".equals(o.toString())) {
                msg = c.getLogin(hg_username, "");
                if (!"error".equals(msg)) {
                    userService.insertUserGameStatus(param);
                }
            }
        } else if ("BG".equals(type) || type == "BG") {
            JSONObject json = new JSONObject();
            BGGameServiceImpl m = new BGGameServiceImpl(pmap);
            Map<String, Object> param = new HashMap<>();
            param.put("uid", uid);
            param.put("gametype", "is_bg");
            Map<String, String> user = userService.selectUserGameStatus(param).get(0);
            Object o = user.get("cnt");
            if ("0".equals(o.toString())) {
                json = m.openUserCreate(ag_username, ag_password, "open.user.create");
                if ("success".equals(json.getString("code")) || json.toString().indexOf("登录名(loginId)已存在") > 0) {
                    userService.insertUserGameStatus(param);
                }
            }

        } else if ("VR".equals(type) || type == "VR") {
            String msg = "";
            VRGameServiceImpl v = new VRGameServiceImpl(pmap);
            Map<String, Object> param = new HashMap<>();
            param.put("uid", uid);
            param.put("gametype", "is_vr");
            Map<String, String> user = userService.selectUserGameStatus(param).get(0);
            Object o = user.get("cnt");
            if ("0".equals(o.toString())) {
                msg = v.CreateUser(ag_username);
                if ("success".equals(msg)) {
                    userService.insertUserGameStatus(param);
                }
            }
        } else if ("JF".equals(type) || type == "JF") {
            String msg = "";
            JSONObject json = new JSONObject();
            JFGameServiceImpl jf = new JFGameServiceImpl(pmap);
            Map<String, Object> param = new HashMap<>();
            param.put("uid", uid);
            param.put("gametype", "is_jf");
            Map<String, String> user = userService.selectUserGameStatus(param).get(0);
            Object o = user.get("cnt");
            if ("0".equals(o.toString())) {
                msg = jf.CreateUser(ag_username, ag_password, ag_username);
                json = JSONObject.fromObject(msg);
                if (json.getString("Success").equals("true") && json.getString("Code").equals("1")) {
                    userService.insertUserGameStatus(param);
                }
            }
        } else if ("KYQP".equals(type) || type == "KYQP") {
            String msg = "";
            JSONObject json = new JSONObject();
            KYQPGameServiceImpl k = new KYQPGameServiceImpl(pmap);
            Map<String, Object> param = new HashMap<>();
            param.put("uid", uid);
            param.put("gametype", "is_kyqp");
            Map<String, String> user = userService.selectUserGameStatus(param).get(0);
            Object o = user.get("cnt");
            if ("0".equals(o.toString())) {
                msg = k.checkOrCreateGameAccout(ag_username,ip, "0");
                json = JSONObject.fromObject(msg);
                if ("0".equals(json.getJSONObject("d").get("code") + "")) {
                    userService.insertUserGameStatus(param);
                }
            }
        } else if ("LYQP".equals(type) || type == "LYQP") {
            String msg = "";
            JSONObject json = new JSONObject();
            LYQPGameServiceImpl lyqp = new LYQPGameServiceImpl(pmap);
            Map<String, Object> param = new HashMap<>();
            param.put("uid", uid);
            param.put("gametype", "is_lyqp");
            Map<String, String> user = userService.selectUserGameStatus(param).get(0);
            Object o = user.get("cnt");
            if ("0".equals(o.toString())) {
                msg = lyqp.checkOrCreateGameAccout(ag_username,ip, "0");
                json = JSONObject.fromObject(msg);
                if ("0".equals(json.getJSONObject("d").get("code") + "")) {
                    userService.insertUserGameStatus(param);
                }
            }
        } else if ("VG".equals(type) || type == "VG") {
            String msg = "";
            VGGameServiceImpl vg = new VGGameServiceImpl(pmap);
            Map<String, Object> param = new HashMap<>();
            param.put("uid", uid);
            param.put("gametype", "is_vg");
            Map<String, String> user = userService.selectUserGameStatus(param).get(0);
            Object o = user.get("cnt");
            if ("0".equals(o.toString())) {
                msg = vg.createUser(ag_username);
                if ("success".equals(msg)) {
                    userService.insertUserGameStatus(param);
                }
            }
        } else if ("GY".equals(type) || type == "GY") {
            String msg = "";
            GYGameServiceImpl c = new GYGameServiceImpl(pmap);
            // 查询用户是否在平台注册,如果未注册则注册用户,并记录
            Map<String, Object> param = new HashMap<>();
            param.put("uid", uid);
            param.put("gametype", "is_gy");
            Map<String, String> user = userService.selectUserGameStatus(param).get(0);
            Object o = user.get("cnt");
            if ("0".equals(o.toString())) {
                msg = c.createUser(ag_username, ag_password);
                if (!"error".equals(msg)) {
                    userService.insertUserGameStatus(param);
                }
            }
        } else if ("PS".equals(type) || type == "PS") {
            String msg = "";
            PSGameServiceImpl ps = new PSGameServiceImpl(pmap);
            Map<String, Object> param = new HashMap<>();
            param.put("uid", uid);
            param.put("gametype", "is_ps");
            Map<String, String> user = userService.selectUserGameStatus(param).get(0);
            Object o = user.get("cnt");
            if ("0".equals(o.toString())) {
                msg = ps.createUser(ag_username);
                if ("success".equals(msg)) {
                    userService.insertUserGameStatus(param);
                }
            }
        } else if ("NB".equals(type) || type == "NB") {
            String msg = "";
            NBGameServiceImpl nb = new NBGameServiceImpl(pmap);
            Map<String, Object> param = new HashMap<>();
            param.put("uid", uid);
            param.put("gametype", "is_nb");
            Map<String, String> user = userService.selectUserGameStatus(param).get(0);
            Object o = user.get("cnt");
            if ("0".equals(o.toString())) {
                msg = nb.createUser(ag_username);
                if ("success".equals(msg)) {
                    userService.insertUserGameStatus(param);
                }
            }
        } else if ("SW".equals(type) || type == "SW") {
            String msg = "";
            SWGameServiceImpl sw = new SWGameServiceImpl(pmap);
            Map<String, Object> param = new HashMap<>();
            param.put("uid", uid);
            param.put("gametype", "is_sw");
            Map<String, String> user = userService.selectUserGameStatus(param).get(0);
            Object o = user.get("cnt");
            if ("0".equals(o.toString())) {
                msg = sw.createUser(ag_username);
                if ("success".equals(msg)) {
                    userService.insertUserGameStatus(param);
                }
            }
        }else if ("IBC".equals(type) || type == "IBC") {
            IBCGameServiceImpl ibcService = new IBCGameServiceImpl(pmap);
            Map<String, Object> param = new HashMap<>();
            param.put("uid", uid);
            param.put("gametype", "is_ibc");
            Map<String, String> user = userService.selectUserGameStatus(param).get(0);
            Object o = user.get("cnt");
            TransferVO transferVO = new TransferVO();
            transferVO.setAccount(ag_username);
            if ("0".equals(o.toString())) {
                ResultResponse response = ibcService.CheckOrCreateGameAccout(transferVO);
                if (response.getStatus() == 1) {
                    userService.insertUserGameStatus(param);
                }
            }
        } else if ("CQJ".equals(type)) {
            CQJServiceimpl cqjServiceimpl = new CQJServiceimpl(pmap);
            Map<String, Object> param = new HashMap<>();
            param.put("uid", uid);
            param.put("gametype", "is_cqj");
            Map<String, String> user = userService.selectUserGameStatus(param).get(0);
            Object o = user.get("cnt");
            TransferVO transferVO = new TransferVO();
            transferVO.setAccount(ag_username);
            if ("0".equals(o.toString())) {
                boolean msgs = cqjServiceimpl.checkOrCreateGameAccout(transferVO);
                if (msgs) {
                    userService.insertUserGameStatus(param);
                }
            }

        }else if ("ESW".equals(type)) {
            ESWServiceImpl eswServiceimpl = new ESWServiceImpl(pmap);
            Map<String, Object> param = new HashMap<>();
            param.put("uid", uid);
            param.put("gametype", "is_esw");
            Map<String, String> user = userService.selectUserGameStatus(param).get(0);
            Object o = user.get("cnt");
            TransferVO transferVO = new TransferVO();
            transferVO.setAccount(ag_username);
            if ("0".equals(o.toString())) {
                EswLoginVo vo = new EswLoginVo();
                vo.setUserCode(ag_username);
                String data = eswServiceimpl.checkOrCreateGameAccout(vo);
                JSONObject jsonObject = JSONObject.fromObject(data);
                if (jsonObject.getInt("code") == 0) {
                    userService.insertUserGameStatus(param);
                }
            }
        }
    }


   
    /**
     * @Description 保存转账信息
     * @param data
     * @param type
     *            1 转出(游戏上分) 2 转入(游戏下分)
     */
    private synchronized JSONObject saveUserTransferSuccess(Map<String, Object> data, int type) {
        JSONObject jsonObject = new JSONObject();
        int result = 0;
        if (type == 1) {
            data.put("result", "天下平台向游戏平台转出金额(游戏上分)成功");
            jsonObject.put("errmsg", "天下平台向游戏平台转出金额(游戏上分)成功");
            result = userGameTransferService.insertUserTransferOut(data);
        } else {
            data.put("result", "游戏平台向天下平台转入金额(游戏下分)成功");
            jsonObject.put("errmsg", "游戏平台向天下平台转入金额(游戏下分)成功");
            result = userGameTransferService.insertUserTransferIn(data);
        }

        if (result > 0) {
            jsonObject.put("msg", "success");
        } else {
            jsonObject.put("msg", "error");
            data.put("result", "写入转账数据失败");
        }
        return jsonObject;
    }

    /**
     * @Description 转账失败
     * @param data
     * @return
     */
    private synchronized JSONObject saveUserTransferFaild(Map<String, Object> data) {
        JSONObject jsonObject = new JSONObject();
        data.put("result", "转账失败,需人工审核");
        userGameTransferService.insertUserTransferFaild(data);
        jsonObject.put("msg", "error");
        jsonObject.put("errmsg", "转账失败,需人工审核");
        return jsonObject;
    }

    /**
     *
     * @Description (TODO这里用一句话描述这个方法的作用)
     * @param data
     * @return
     */
    private synchronized JSONObject saveUserTransferOutFaild(Map<String, Object> data) {
        JSONObject jsonObject = new JSONObject();
        data.put("result", "天下平台向游戏平台转入金额(游戏上分)失败,需人工审核");
        userGameTransferService.insertUserTransferOutFaild(data);
        jsonObject.put("msg", "error");
        jsonObject.put("errmsg", "天下平台向游戏平台转入金额(游戏上分)失败,需人工审核,需人工审核");
        return jsonObject;
    }

    /**
     * @Description 格式化obj参数
     * @param obj
     * @return
     */
    private String formatObjectParams(Object obj) {
        if (ObjectUtils.anyNotNull(obj)) {
            return String.valueOf(obj);
        }
        return null;
    }

    private JSONObject verifyRequestParams(String ag_password, String ag_username, String suuid, String simgcode,
                        String type, Integer credit, String uuid, String imgcode) throws Exception {
        try {
            if (StringUtils.isBlank(ag_username)) {
                return transferResponse("error", "用户游戏登录账号不能为空");
            }

            if (StringUtils.isBlank(ag_password)) {
                return transferResponse("error", "用户游戏登录密码不能为空");
            }

            if (StringUtils.isBlank(type)) {
                return transferResponse("01", "请求参数平台编码不能为空");
            }

            if (credit == null || credit < 1 || credit > 100000000) {
                return transferResponse("02", "请输入合法金额,大于【1】且小于【100000000】之间的金额");
            }

            if (StringUtils.isBlank(suuid)) {
                return transferResponse("error", "用户UUID不能为空");
            }

            if (StringUtils.isBlank(uuid)) {
                return transferResponse("error", "请求参数uuid不能为空");
            }

            if (!suuid.equalsIgnoreCase(uuid)) {
                return transferResponse("error", "非法用户,请重新登录");
            }

            if (StringUtils.isNotBlank(simgcode)) {
                if (StringUtils.isBlank(imgcode)) {
                    return transferResponse("04", "验证不能空,请输入验证码");
                }

                if (!simgcode.equalsIgnoreCase(imgcode)) { // 忽略验证码大小写
                    return transferResponse("04", "验证码不正确,请输入正确的验证码");
                }
            }
            return transferResponse("success", "校验转账请求参数成功");
        } catch (Exception e) {
            logger.info("校验转账参数异常:{}", e.getMessage());
            return transferResponse("error", "校验转账请求参数异常");
        }
    }

    /**
     * @Description 生成订单号
     * @param type
     * @return
     * @throws Exception
     */
    private String generatorOrderNo(String type, String ag_username, Map<String, String> pmap) throws Exception {
        try {
            String billno = null;
            if ("AGIN".equals(type) || type == "AGIN") {
                String cagent = "AGIN";
                billno = cagent + System.currentTimeMillis();
            } else if ("AG".equals(type) || type == "AG") {
                String cagent = "AG";
                billno = cagent + System.currentTimeMillis();
            } else if ("JDB".equals(type) || type == "JDB") {
                billno = "TX" + System.currentTimeMillis();
            } else if ("BBIN".equals(type) || type == "BBIN") {
                billno = System.currentTimeMillis() + "";
            } else if ("DS".equals(type) || type == "DS") {
                billno = "TX" + System.currentTimeMillis();
            } else if ("OB".equals(type) || type == "OB") {
                billno = System.currentTimeMillis() + "";
            } else if ("OG".equals(type) || type == "OG") {
                billno = "TX" + System.currentTimeMillis();
            } else if ("SB".equals(type) || type == "SB") {
                billno = "TX" + System.currentTimeMillis();
            } else if ("MG".equals(type) || type == "MG") {
                billno = "TX" + System.currentTimeMillis();
            } else if ("PT".equals(type) || type == "PT") {
                billno = "TX" + System.currentTimeMillis();
            } else if ("HABA".equals(type) || type == "HABA") {
                billno = "TX" + System.currentTimeMillis();
            } else if ("GGBY".equals(type) || type == "GGBY") {
                billno = "" + System.currentTimeMillis();
            } else if ("CG".equals(type) || type == "CG") {
                billno = "TX" + System.currentTimeMillis();
            } else if ("IG".equals(type) || type == "IG") {
                billno = ag_username + System.currentTimeMillis();
            } else if ("IGPJ".equals(type) || type == "IGPJ") {
                billno = ag_username + System.currentTimeMillis();
            } else if ("HG".equals(type) || type == "HG") {
                billno = "TX" + System.currentTimeMillis();
            } else if ("BG".equals(type) || type == "BG") {
                billno = String.valueOf(System.currentTimeMillis());
            } else if ("VR".equals(type) || type == "VR") {
                billno = "TX" + System.currentTimeMillis();
            } else if ("JF".equals(type) || type == "JF") {
                billno = "00" + System.currentTimeMillis();
            } else if ("KYQP".equals(type) || type == "KYQP") {
                PlatFromConfig pf = new PlatFromConfig();
                pf.InitData(pmap, "KYQP");
                JSONObject json = JSONObject.fromObject(pf.getPlatform_config());
                String api_cagent = json.getString("api_cagent").toString();
                SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                billno = api_cagent + sf.format(new Date()) + ag_username;
            } else if ("LYQP".equals(type) || type == "LYQP") {
                PlatFromConfig pf = new PlatFromConfig();
                pf.InitData(pmap, "LYQP");
                JSONObject json = JSONObject.fromObject(pf.getPlatform_config());
                String api_cagent = json.getString("api_cagent").toString();
                SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                billno = api_cagent + sf.format(new Date()) + ag_username;
            } else if ("VG".equals(type) || type == "VG") {
                billno = "VG" + System.currentTimeMillis();
            } else if ("GY".equals(type) || type == "GY") {
                billno = "GY" + System.currentTimeMillis();
            } else if ("PS".equals(type) || type == "PS") {
                billno = "PS" + System.currentTimeMillis();
            } else if ("NB".equals(type) || type == "NB") {
                billno = "NB" + System.currentTimeMillis();
            } else if ("SW".equals(type) || type == "SW") {
                billno = "TX" + System.currentTimeMillis();
            }else if("CQJ".equals(type) || type =="CQJ"){
                billno = "CQJ" + System.currentTimeMillis();
            }else if("ESW".equals(type) || type =="ESW"){
                PlatFromConfig pf = new PlatFromConfig();
                pf.InitData(pmap, type);
                JSONObject jo = JSONObject.fromObject(pf.getPlatform_config());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                billno = jo.getString("agentId") + sdf.format(new Date()) + ag_username;
            }else if("IBC".equalsIgnoreCase(type)){
                billno = "IBC" + System.currentTimeMillis();
            }
            return billno;
        } catch (Exception e) {
            logger.info("生成转账订单号异常:{}", e.getMessage());
            throw new Exception("生成转账订单号异常");
        }
    }

    /**
     * @Description 封装返回结果
     * @param code
     * @param message
     * @return
     */
    private JSONObject transferResponse(String code, String message) {
        JSONObject data = new JSONObject();
        if (StringUtils.isBlank(code) || StringUtils.isBlank(message)) {
            data.put("msg", "error");
            data.put("errmsg", "转账异常");
        } else {
            data.put("msg", code);
            data.put("errmsg", message);
        }
        return data;
    }

    /**
     * @Description 操作天下平台向游戏平台转入金额业务(游戏上分)
     * @param type
     *            平台编码
     * @throws Exception
     */
    private JSONObject transferInProcess(String type, String ag_username, String hg_username, String username,String billno, int credit,
            String ag_password, String ip, Map<String, String> data) throws Exception {
        logger.info("用户【"+username+"】操作天下平台向游戏平台【"+type+"】转入金额业务(游戏上分)业务开始=============START===============");
        try {

            if ("AG".equalsIgnoreCase(type)) {
                AGGameServiceImpl gameService = new AGGameServiceImpl(data);
                // 调用预转账
                String msg = gameService.PrepareTransferCredit(ag_username, billno, "IN", credit + "", ag_password,
                        "CNY");
                if (!"0".equals(msg)) {
                    return transferResponse("faild", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务失败");
                }
                // 预转账失败
                boolean tflag = true;
                boolean bflag = false;
                do {
                    // 调用确认转账
                    msg = gameService.TransferCreditConfirm(ag_username, billno, "IN", credit + "", "1", ag_password,
                            "CNY");
                    if (!"0".equals(msg)) {
                        int counts = 0;
                        do {
                            Thread.sleep(3000);
                            logger.info("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务,第{}次轮询转账订单次数", counts);
                            counts++;
                            // 查询转账订单
                            msg = gameService.QueryOrderStatus(billno, "CNY");
                            if ("0".equals(msg)) {
                                // 转账订单处理成功
                                return transferResponse("success", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
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
                                    return transferResponse("process",
                                            "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务异常,需人工审核");
                                }
                            }
                        } while (bflag);
                    }
                    // 订单处理成功
                    tflag = false;
                } while (tflag);
                // 处理转账订单业务
                if (bflag) {
                    return transferResponse("faild", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务失败");
                } else {
                    // 转账成功
                    return transferResponse("success", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
                }
            } else if ("AGIN".equalsIgnoreCase(type)) {
                AGINGameServiceImpl gameService = new AGINGameServiceImpl(data);
                // 调用预转账
                String msg = gameService.PrepareTransferCredit(ag_username, billno, "IN", credit + "", ag_password,
                        "CNY");
                if (!"0".equals(msg)) {
                    return transferResponse("faild", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务失败");
                }
                // 预转账失败
                boolean tflag = true;
                boolean bflag = false;
                do {
                    // 调用确认转账
                    msg = gameService.TransferCreditConfirm(ag_username, billno, "IN", credit + "", "1", ag_password,
                            "CNY");
                    if (!"0".equals(msg)) {
                        int counts = 0;
                        do {
                            Thread.sleep(3000);
                            logger.info("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务,第{}次轮询转账订单次数", counts);
                            counts++;
                            // 查询转账订单
                            msg = gameService.QueryOrderStatus(billno, "CNY");
                            if ("0".equals(msg)) {
                                // 转账订单处理成功
                                return transferResponse("success", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
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
                                    return transferResponse("process",
                                            "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务异常,需人工审核");
                                }
                            }
                        } while (bflag);
                    }
                    // 订单处理成功
                    tflag = false;
                } while (tflag);
                // 处理转账订单业务
                if (bflag) {
                    return transferResponse("faild", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务失败");
                } else {
                    // 转账成功
                    return transferResponse("success", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
                }
            } else if ("BBIN".equalsIgnoreCase(type)) {
                BBINGameServiceImpl gameService = new BBINGameServiceImpl(data);
                String msg = gameService.Transfer(ag_username, ag_password, billno, "IN", credit + "");
                // 解析响应结果
                msg = JSONObject.fromObject(msg).getString("result");
                if ("true".equalsIgnoreCase(msg)) {
                    // 转账订单提交成功
                    return transferResponse("success", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
                }
                // 失败订单延时10秒查询订单状态
                boolean isPoll = true;
                int polls = 0;
                do {
                    Thread.sleep(3000);
                    logger.info("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务,第{}次轮询转账订单次数", polls);
                    polls++;
                    boolean isCheck = gameService.checkTransfer(billno, ag_username);
                    if (isCheck) {
                        // 成功
                        return transferResponse("success", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
                    } else {
                        if (polls > 2) {
                            // 转账订单处理失败
                            return transferResponse("faild", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务失败");
                        }
                    }

                } while (isPoll);
            } else if ("DS".equalsIgnoreCase(type)) {
                DSGameServiceImpl gameService = new DSGameServiceImpl(data);
                String msg = gameService.DEPOSIT(ag_username, ag_password, billno, credit + "");
                if ("success".equalsIgnoreCase(msg)) {
                    // 转账订单处理成功
                    return transferResponse("success", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
                }
                // 轮询订单
                boolean isPoll = true;
                int polls = 0;
                do {
                    // 休眠2秒
                    Thread.sleep(2000);
                    logger.info("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务,第{}次轮询转账订单次数", polls);
                    polls++;
                    msg = gameService.CHECK_REF(billno);
                    // 6601为该单据已成功,6607为处理中,2秒后再次查询该订单状态
                    if ("6601".endsWith(msg)) {
                        // 单据成功
                        return transferResponse("success", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
                    } else if ("6617".endsWith(msg)) {
                        if (polls > 2) {
                            // 订单为处理中,无法判断订单是否成功或失败,需人工审核
                            return transferResponse("process", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务异常,需人工审核");
                        }
                    } else {
                        if (polls > 2) {
                            // 订单处理失败
                            return transferResponse("faild", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务失败");
                        }
                    }
                } while (isPoll);
            } else if ("OB".equalsIgnoreCase(type)) {
                OBService ob = new OBGameServiceImpl(data);
                String msg = ob.agent_client_transfer(ag_username, billno, "1", credit + "");
                if (StringUtils.isBlank(msg)) {
                    // 查询订单
                    msg = ob.queryOrder(new SecureRandom().nextLong(), billno);
                    if (StringUtils.isBlank(msg)) {
                        // 查询无响应结果
                        return transferResponse("process", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务异常,需人工审核");
                    }

                    if ("1".equals(JSONObject.fromObject(msg).getString("transferState"))) {
                        // 转账订单处理成功
                        return transferResponse("success", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
                    }

                } else if ("Ok".equalsIgnoreCase(JSONObject.fromObject(msg).getString("error_code"))) {
                    // 转账订单处理成功
                    return transferResponse("success", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
                }

                // 转账订单处理失败
                return transferResponse("faild", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务失败");
            } else if ("OG".equalsIgnoreCase(type)) {
                OGGameServiceImpl o = new OGGameServiceImpl(data);
                String msg = o.DEPOSIT(ag_username, ag_password, billno, credit + "");
                logger.info("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务响应结果:{}",msg);
                if ("success".equalsIgnoreCase(msg)) {
                    // 转账订单处理成功
                    return transferResponse("success", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
                }

                // 转账订单处理失败
                return transferResponse("faild", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务失败");
            } else if ("SB".equalsIgnoreCase(type)) {
                SBService s = new SBGameServiceImpl(data);
                // 获取平台授权token
                String atoken = s.getAccToken();
                // 获取token
                String access_token = JSONObject.fromObject(atoken).getString("access_token");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+00:00");
                Date now = new Date();
                Calendar Cal = Calendar.getInstance();
                Cal.setTime(now);
                Cal.add(Calendar.HOUR_OF_DAY, -7);
                String todayStr = sdf.format(Cal.getTime());
                String msg = s.WalletCredit(ag_username, billno, credit + "", todayStr, access_token);
                if (StringUtils.isBlank(msg)) {
                    return transferResponse("process", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务异常,需人工审核");
                } else if ("false".equalsIgnoreCase(JSONObject.fromObject(msg).getString("dup"))) {
                    // 转账成功
                    return transferResponse("success", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
                } else {
                    return transferResponse("faild", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务失败");
                }
            } else if ("MG".equalsIgnoreCase(type)) {
                MGGameServiceImpl m = new MGGameServiceImpl(data);
                Map<String, String> mgmap = new HashMap<String, String>();
                mgmap.put("ClientIP", ip);
                mgmap.put("OrderId", billno);
                JSONObject result = m.deposit(ag_username, ag_password, credit + "", mgmap);
                if (result.containsKey("Code") && "success".equalsIgnoreCase(result.getString("Code"))) {
                    // 转账处理成功
                    return transferResponse("success", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
                } else if ("error".equalsIgnoreCase(result.getString("Code"))) {
                    // 异常订单
                    return transferResponse("process", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务异常,需人工审核");
                } else {
                    // 失败订单
                    return transferResponse("faild", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务失败");
                }
            } else if ("PT".equalsIgnoreCase(type)) {
                PTGameServiceImpl p = new PTGameServiceImpl(data);
                String msg = p.Deposit(ag_username, credit + "", billno);
                if (StringUtils.isBlank(msg)) {
                    // 异常订单
                    return transferResponse("process", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务异常,需人工审核");
                }
                // 解析
                JSONObject json = JSONObject.fromObject(msg);
                if (json.containsKey("result") && json.getString("result").indexOf("errorcode") < 0) {
                    // 成功
                    return transferResponse("success", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
                }

                return transferResponse("faild", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务失败");

            } else if ("HABA".equalsIgnoreCase(type)) {
                HABAGameServiceImpl h = new HABAGameServiceImpl(data);
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("amount", credit);
                params.put("requestId", billno);
                MoneyResponse mr = h.depositPlayerMoney(ag_username, ag_password, params);
                if (mr == null) {
                    // 异常订单
                    return transferResponse("process", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务异常,需人工审核");
                }
                if (mr.isSuccess()) {
                    // 转账成功
                    return transferResponse("success", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
                }
                return transferResponse("faild", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务失败");
            } else if ("JDB".equalsIgnoreCase(type)) {
                JDBGameServiceImpl jdb = new JDBGameServiceImpl(data);
                String msg = jdb.transIn(ag_username, billno, credit);
                if ("success".equalsIgnoreCase(msg)) {
                    // 成功
                    return transferResponse("success", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
                }

                if ("error".equalsIgnoreCase(msg)) {
                    // 异常订单
                    return transferResponse("process", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务异常,需人工审核");
                }
                return transferResponse("faild", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务失败");
            } else if ("GGBY".equalsIgnoreCase(type)) {
                GGBYGameServiceImpl gg = new GGBYGameServiceImpl(data);
                String msg = gg.TransferCredit(ag_username, billno, credit + "", "IN", ag_password, ip);
                if ("success".equalsIgnoreCase(msg)) {
                    // 成功
                    return transferResponse("success", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
                }

                // 轮询订单
                boolean isPoll = true;
                int polls = 0;
                do {
                    Thread.sleep(2000);
                    logger.info("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务,第{}次轮询转账订单次数", polls);
                    polls++;
                    msg = gg.QueryOrderStatus(billno);
                    if ("0".equals(msg)) {
                        // 0为该单据已成功
                        return transferResponse("success", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
                    } else if ("-1".equals(msg)) {
                        // 异常订单
                        if (polls > 2) {
                            return transferResponse("process", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务异常,需人工审核");
                        }
                    } else {
                        if (polls > 2) {
                            return transferResponse("faild", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务失败");
                        }
                    }
                } while (isPoll);
            } else if ("CG".equalsIgnoreCase(type)) {
                CGGameServiceImpl c = new CGGameServiceImpl(data);
                String msg = c.DEPOSIT(ag_username, ag_password, billno, credit + "");
                if ("success".equalsIgnoreCase(msg)) {
                    // 转账成功
                    return transferResponse("success", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
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
                    msg = c.CHECK_REF(billno);
                    // 6601为该单据已成功,6617为处理中,2秒后再次查询该订单状态
                    if ("6601".endsWith(msg)) {
                        return transferResponse("success", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
                    } else if ("6617".endsWith(msg)) {
                        if (polls > 2) {
                            // 异常订单,需要人工审核
                            return transferResponse("process", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务异常");
                        }
                    } else {
                        if (polls > 2) {
                            // 转账失败
                            return transferResponse("faild", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务失败");
                        }
                    }
                } while (isPoll);
            } else if ("IG".equalsIgnoreCase(type)) {
                IGGameServiceImpl c = new IGGameServiceImpl(data);
                String msg = c.DEPOSIT(ag_username, ag_password, billno, credit + "");
                if ("success".equalsIgnoreCase(msg)) {
                    return transferResponse("success", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
                }

                // 轮询
                boolean isPoll = true;
                int polls = 0;
                do {
                    Thread.sleep(1500);
                    logger.info("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务,第{}次轮询转账订单次数", polls);
                    polls++;
                    String ckeckMsg = c.CHECK_REF(billno);
                    if ("6601".endsWith(ckeckMsg)) {
                        // 转账成功
                        return transferResponse("success", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
                    } else if ("6617".endsWith(ckeckMsg)) {
                        if (polls > 2) {
                            return transferResponse("process", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务异常,需人工审核");
                        }
                    } else {
                        if (polls > 2) {
                            return transferResponse("faild", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务失败");
                        }
                    }
                } while (isPoll);
            } else if ("IGPJ".equalsIgnoreCase(type)) {
                IGPJGameServiceImpl c = new IGPJGameServiceImpl(data);
                String msg = c.DEPOSIT(ag_username, ag_password, billno, credit + "");
                if ("success".equalsIgnoreCase(msg)) {
                    return transferResponse("success", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
                }

                // 轮询
                boolean isPoll = true;
                int polls = 0;
                do {
                    Thread.sleep(1500);
                    logger.info("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务,第{}次轮询转账订单次数", polls);
                    polls++;
                    String ckeckMsg = c.CHECK_REF(billno);
                    if ("6601".endsWith(ckeckMsg)) {
                        // 转账成功
                        return transferResponse("success", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
                    } else if ("6617".endsWith(ckeckMsg)) {
                        if (polls > 2) {
                            return transferResponse("process", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务异常,需人工审核");
                        }
                    } else {
                        if (polls > 2) {
                            return transferResponse("faild", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务失败");
                        }
                    }
                } while (isPoll);
            } else if ("HG".equalsIgnoreCase(type)) {
                HGGameServiceImpl c = new HGGameServiceImpl(data);
                if (StringUtils.isBlank(hg_username)) {
                    return transferResponse("faild", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务失败,获取登录账号失败");
                }
                String msg = c.DEPOSIT(hg_username, billno, credit + "");
                if ("success".equalsIgnoreCase(msg)) {
                    // 转账订单提交成功
                    return transferResponse("success", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
                } else {
                    return transferResponse("faild", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务失败");
                }
            } else if ("BG".equalsIgnoreCase(type)) {
                BGGameServiceImpl c = new BGGameServiceImpl(data);
                String msg = c.openBalanceTransfer(ag_username, "open.balance.transfer", billno, credit + "", "IN",
                        billno);
                if ("success".equalsIgnoreCase(msg)) {
                    // 转账订单提交成功
                    return transferResponse("success", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
                } else {
                    return transferResponse("faild", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务失败");
                }
            } else if ("VR".equalsIgnoreCase(type)) {
                VRGameServiceImpl v = new VRGameServiceImpl(data);
                String msg = v.Deposit(billno, ag_username, credit);
                if ("success".equalsIgnoreCase(msg)) {
                    // 转账订单提交成功
                    return transferResponse("success", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
                } else {
                    return transferResponse("faild", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务失败");
                }
            } else if ("JF".equalsIgnoreCase(type)) {
                JFGameServiceImpl jf = new JFGameServiceImpl(data);
                String msg = jf.Transfer(ag_username, ag_password, billno, "IN", credit + "");
                if ("success".equalsIgnoreCase(msg)) {
                    // 转账订单提交成功
                    return transferResponse("success", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
                } else {
                    return transferResponse("faild", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务失败");
                }
            } else if ("KYQP".equalsIgnoreCase(type)) {
                KYQPGameServiceImpl k = new KYQPGameServiceImpl(data);
                String msg = k.channelHandleOn(ag_username, billno, credit + "", "2");
                if ("success".equals(msg) || msg == "success") {
                    // 转账订单提交成功
                    return transferResponse("success", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
                }

                // 轮询订单
                boolean isPoll = true;
                int polls = 0;
                do {
                    Thread.sleep(1500);
                    logger.info("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务,第{}次轮询转账订单次数", polls);
                    polls++;
                    String s = k.orderQuery(billno);
                    if (StringUtils.isBlank(s)) {
                        // 请求异常
                        if (polls > 2) {
                            return transferResponse("process", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务异常,需人工审核");
                        }
                    }

                    // 解析响应结果
                    JSONObject jsonObject = JSONObject.fromObject(s);
                    if (jsonObject.containsKey("d") && "0".equals(jsonObject.getJSONObject("d").getString("status"))) {
                        // 成功
                        return transferResponse("success", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
                    }

                    if (polls > 2) {
                        return transferResponse("faild", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务失败");
                    }
                } while (isPoll);
            } else if ("LYQP".equalsIgnoreCase(type)) {
                LYQPGameServiceImpl lyqp = new LYQPGameServiceImpl(data);
                String msg = lyqp.channelHandleOn(ag_username, billno, credit + "", "2");
                if ("success".equals(msg) || msg == "success") {
                    // 转账订单提交成功
                    return transferResponse("success", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
                }

                // 轮询订单
                boolean isPoll = true;
                int polls = 0;
                do {
                    Thread.sleep(1500);
                    logger.info("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务,第{}次轮询转账订单次数", polls);
                    polls++;
                    String s = lyqp.orderQuery(billno);
                    if (StringUtils.isBlank(s)) {
                        // 请求异常
                        if (polls > 2) {
                            return transferResponse("process", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务异常,需人工审核");
                        }
                    }

                    // 解析响应结果
                    JSONObject jsonObject = JSONObject.fromObject(s);
                    if (jsonObject.containsKey("d") && "0".equals(jsonObject.getJSONObject("d").getString("status"))) {
                        // 成功
                        return transferResponse("success", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
                    }

                    if (polls > 2) {
                        return transferResponse("faild", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务失败");
                    }
                } while (isPoll);
            } else if ("VG".equalsIgnoreCase(type)) {
                VGGameServiceImpl vg = new VGGameServiceImpl(data);
                String msg = vg.deposit(ag_username, credit, billno);
                if ("success".equalsIgnoreCase(msg)) {
                    // 转账订单提交成功
                    return transferResponse("success", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
                } else {
                    return transferResponse("faild", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务失败");
                }
            } else if ("GY".equalsIgnoreCase(type)) {
                GYGameServiceImpl vg = new GYGameServiceImpl(data);
                String msg = vg.transIn(ag_username, ag_password, billno, credit + "");
                if ("success".equalsIgnoreCase(msg)) {
                    // 转账订单提交成功
                    return transferResponse("success", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
                } else {
                    return transferResponse("faild", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务失败");
                }
            } else if ("PS".equalsIgnoreCase(type)) {
                PSGameServiceImpl ps = new PSGameServiceImpl(data);
                String msg = ps.transIn(ag_username, billno, credit + "");
                if ("success".equalsIgnoreCase(msg)) {
                    // 转账订单提交成功
                    return transferResponse("success", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
                } else {
                    return transferResponse("faild", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务失败");
                }
            } else if ("NB".equalsIgnoreCase(type)) {
                NBGameServiceImpl nb = new NBGameServiceImpl(data);
                String msg = nb.transfer(ag_username, 1, 1, credit + "");
                if ("success".equalsIgnoreCase(msg)) {
                    // 转账订单提交成功
                    return transferResponse("success", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
                } else {
                    return transferResponse("faild", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务失败");
                }
            } else if ("SW".equalsIgnoreCase(type)) {
                SWGameServiceImpl sw = new SWGameServiceImpl(data);
                String msg = sw.transferTo(ag_username, Double.valueOf(credit), billno);
                if ("success".equalsIgnoreCase(msg)) {
                    // 转账订单提交成功
                    return transferResponse("success", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
                } else {
                    return transferResponse("faild", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务失败");
                }
            }else if("CQJ".equalsIgnoreCase(type)){
                CQJServiceimpl cqjServiceimpl = new CQJServiceimpl(data);
                TransferVO transferVO = new TransferVO();
                transferVO.setPassword(ag_password);
                transferVO.setAccount(ag_username);
                transferVO.setOrderNo(billno);
                transferVO.setMoney(Double.valueOf(credit));
                String returnMsg = cqjServiceimpl.transferIn(transferVO);
               if ("success".equalsIgnoreCase(returnMsg)) {
                   // 转账订单提交成功
                   return transferResponse("success", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
               } else {
                   return transferResponse("faild", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务失败");
               }
           }else if("ESW".equalsIgnoreCase(type)){
               ESWServiceImpl eSWServiceImpl = new ESWServiceImpl(data);
               String returnMsg = eSWServiceImpl.transferIn(ag_username,String.valueOf(credit),billno);
               if ("success".equalsIgnoreCase(returnMsg)) {
                   // 转账订单提交成功
                   return transferResponse("success", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
               } else {
                   return transferResponse("faild", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务失败");
               }
           }else if("IBC".equalsIgnoreCase(type)){
               IBCGameServiceImpl service = new IBCGameServiceImpl(data);
               TransferVO vo = new TransferVO();
               vo.setAccount(ag_username);
               vo.setMoney(Double.parseDouble(String.valueOf(credit)));
               vo.setOrderNo(billno);
               ResultResponse resultResponse = service.transferIn(vo);
               if(ResponseCode.SUCCESS_STATUS == resultResponse.getStatus()){
                   return transferResponse("success", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务成功");
               }else {
                   return transferResponse("faild", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务失败:"+resultResponse.getMessage());
               }
           }
            return transferResponse("error", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务失败,游戏平台不存在");
        } catch (Exception e) {
            logger.info("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务异常:{}", e.getMessage());
            return transferResponse("process", "用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务失败");
        }
    }



    /**
     *
     * @Description 操作游戏平台向天下平台转入金额业务(游戏下分)
     * @param type
     * @param ag_username
     * @param hg_username
     * @param billno
     * @return
     */
    private JSONObject transferOutProcess(String type, String ag_username, String hg_username, String username, String billno, int credit,
                                                       String ag_password, String ip, Map<String, String> data)throws Exception{

        logger.info("用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务开始=============START===============");
        try {
            if ("AG".equalsIgnoreCase(type)) {
                AGGameServiceImpl gameService = new AGGameServiceImpl(data);
                // 调用预转账
                String msg = gameService.PrepareTransferCredit(ag_username, billno, "OUT", credit + "", ag_password,
                        "CNY");
                if (!"0".equals(msg)) {
                    return transferResponse("faild", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务失败");
                }
                // 预转账失败
                boolean tflag = true;
                boolean bflag = false;
                do {
                    // 调用确认转账
                    msg = gameService.TransferCreditConfirm(ag_username, billno, "OUT", credit + "", "1", ag_password,
                            "CNY");
                    if (!"0".equals(msg)) {
                        int counts = 0;
                        do {
                            Thread.sleep(1500);
                            logger.info("用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务,第{}次轮询转账订单次数", counts);
                            counts++;
                            // 查询转账订单
                            msg = gameService.QueryOrderStatus(billno, "CNY");
                            if ("0".equals(msg)) {
                                // 转账订单处理成功
                                return transferResponse("success", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
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
                                    return transferResponse("process",
                                            "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务异常,需人工审核");
                                }
                            }
                        } while (bflag);
                    }
                    // 订单处理成功
                    tflag = false;
                } while (tflag);
                // 处理转账订单业务
                if (bflag) {
                    return transferResponse("faild", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务失败");
                } else {
                    // 转账成功
                    return transferResponse("success", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
                }
            } else if ("AGIN".equalsIgnoreCase(type)) {
                AGINGameServiceImpl gameService = new AGINGameServiceImpl(data);
                // 调用预转账
                String msg = gameService.PrepareTransferCredit(ag_username, billno, "OUT", credit + "", ag_password,
                        "CNY");
                if (!"0".equals(msg)) {
                    return transferResponse("faild", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务失败");
                }
                // 预转账失败
                boolean tflag = true;
                boolean bflag = false;
                do {
                    // 调用确认转账
                    msg = gameService.TransferCreditConfirm(ag_username, billno, "OUT", credit + "", "1", ag_password,
                            "CNY");
                    if (!"0".equals(msg)) {
                        int counts = 0;
                        do {
                            Thread.sleep(1500);
                            logger.info("用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务,第{}次轮询转账订单次数", counts);
                            counts++;
                            // 查询转账订单
                            msg = gameService.QueryOrderStatus(billno, "CNY");
                            if ("0".equals(msg)) {
                                // 转账订单处理成功
                                return transferResponse("success", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
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
                                    return transferResponse("process",
                                            "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务异常,需人工审核");
                                }
                            }
                        } while (bflag);
                    }
                    // 订单处理成功
                    tflag = false;
                } while (tflag);
                // 处理转账订单业务
                if (bflag) {
                    return transferResponse("faild", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务失败");
                } else {
                    // 转账成功
                    return transferResponse("success", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
                }
            } else if ("BBIN".equalsIgnoreCase(type)) {
                String msg = "";
                JSONObject json = new JSONObject();
                BBINGameServiceImpl b = new BBINGameServiceImpl(data);
                msg = b.Transfer(ag_username, ag_password, billno, "OUT", credit + "");
                json = JSONObject.fromObject(msg);
                msg = json.getString("result");
                if ("true".equals(msg.toLowerCase())) {
                    return transferResponse("success", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
                } else {
                 // 失败订单延时10秒查询订单状态
                    boolean isPoll = true;
                    int polls = 0;
                    do {
                        Thread.sleep(1500);
                        logger.info("用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务,第{}次轮询转账订单次数", polls);
                        polls++;
                        boolean isCheck = b.checkTransfer(billno, ag_username);
                        if (isCheck) {
                            // 成功
                            return transferResponse("success", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
                        } else {
                            if (polls > 2) {
                                // 转账订单处理失败
                                return transferResponse("faild", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务失败");
                            }
                        }

                    } while (isPoll);
                }

            } else if ("JDB".equalsIgnoreCase(type)) {
                String msg = "";
                JDBGameServiceImpl jdb = new JDBGameServiceImpl(data);
                msg = jdb.transOut(ag_username, billno, credit);
                if ("success".equals(msg)) {
                    return transferResponse("success", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
                } else {
                    return transferResponse("faild", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务失败");
                }
            } else if ("DS".equalsIgnoreCase(type)) {
                String msg = "";
                DSGameServiceImpl ds = new DSGameServiceImpl(data);
                msg = ds.WITHDRAW(ag_username, ag_password, billno, credit + "");
                if ("success".equals(msg)) {
                    return transferResponse("success", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
                } else {
                    // 轮询订单
                    boolean isPoll = true;
                    int polls = 0;
                    do {
                        // 休眠2秒
                        Thread.sleep(2000);
                        logger.info("用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务,第{}次轮询转账订单次数", polls);
                        polls++;
                        msg = ds.CHECK_REF(billno);
                        // 6601为该单据已成功,6607为处理中,2秒后再次查询该订单状态
                        if ("6601".endsWith(msg)) {
                            // 单据成功
                            return transferResponse("success", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
                        }else {
                            if (polls > 2) {
                                // 订单处理失败
                                return transferResponse("faild", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务失败");
                            }
                        }
                    } while (isPoll);
                }

            } else if ("OB".equalsIgnoreCase(type)) {
                String msg = "";
                JSONObject json = new JSONObject();
                OBService ob = new OBGameServiceImpl(data);
                msg = ob.agent_client_transfer(ag_username, billno, "0", credit + "");
                json = JSONObject.fromObject(msg);
                if ("OK".equals(json.getString("error_code")) || json.getString("error_code") == "OK") {
                    return transferResponse("success", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
                } else {
                    return transferResponse("faild", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务失败");
                }
            } else if ("OG".equalsIgnoreCase(type)) {
                String msg = "";
                OGGameServiceImpl o = new OGGameServiceImpl(data);
                msg = o.WITHDRAW(ag_username, ag_password, billno, credit + "");
                logger.info("用户【"+username+"】操作天下平台向游戏平台【" + type + "】转入金额业务(游戏上分)业务响应结果:{}",msg);
                if ("success".equals(msg) || msg == "success") {
                    return transferResponse("success", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
                } else {
                    return transferResponse("faild", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务失败");
                }

            } else if ("SB".equals(type) || type == "SB") {
                String msg = "";
                SBService s = new SBGameServiceImpl(data);
                // 获取平台授权token
                String atoken = s.getAccToken();
                JSONObject json = new JSONObject();
                json = JSONObject.fromObject(atoken);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+00:00");
                Date now = new Date();
                Calendar Cal = Calendar.getInstance();
                Cal.setTime(now);
                Cal.add(Calendar.HOUR_OF_DAY, -7);
                String todayStr = sdf.format(Cal.getTime());
                msg = s.WalletDebit(ag_username, billno, credit + "", todayStr, json.get("access_token").toString());
                json = JSONObject.fromObject(msg);
                // false代表提交成功
                if ("false".equals(json.getString("dup").toString())) {
                    return transferResponse("success", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
                } else {
                    return transferResponse("faild", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务失败");
                }
            } else if ("MG".equalsIgnoreCase(type)) {
                String msg = "";
                Map<String, String> mgmap = new HashMap<String, String>();
                mgmap.put("ClientIP", ip);
                mgmap.put("OrderId", billno);
                MGGameServiceImpl m = new MGGameServiceImpl(data);
                JSONObject jsonObject = m.withdrawal(ag_username, ag_password, credit + "", mgmap);
                msg = jsonObject.getString("Code");
                if ("success".equals(msg) || msg == "success") {
                    return transferResponse("success", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
                } else {
                    return transferResponse("faild", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务失败");
                }
            } else if ("PT".equalsIgnoreCase(type)) {
                String msg = "";
                PTGameServiceImpl p = new PTGameServiceImpl(data);
                msg = p.Withdraw(ag_username, credit + "", billno);
                JSONObject jsonObject = JSONObject.fromObject(msg);
                msg = jsonObject.getJSONObject("result").getString("result");
                if (msg.indexOf("errorcode") < 0) {
                    return transferResponse("success", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
                } else {
                    return transferResponse("faild", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务失败");
                }
            } else if ("HABA".equalsIgnoreCase(type)) {
                HABAGameServiceImpl h = new HABAGameServiceImpl(data);
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("amount", -credit);
                params.put("requestId", billno);
                MoneyResponse mr = h.withdrawPlayerMoney(ag_username, ag_password, params);
                if (mr.isSuccess()) {
                    return transferResponse("success", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
                } else {
                    return transferResponse("faild", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务失败");
                }
            } else if ("GGBY".equalsIgnoreCase(type)) {
                String msg = "";
                GGBYGameServiceImpl gg = new GGBYGameServiceImpl(data);
                msg = gg.TransferCredit(ag_username, billno, credit + "", "OUT", ag_password, ip);
                if ("success".equals(msg)) {
                    return transferResponse("success", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
                } else {

                    //轮询
                    boolean isPoll = true;
                    int polls = 0;
                    do {
                        Thread.sleep(2000);
                        logger.info("用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务,第{}次轮询转账订单次数", polls);
                        polls++;
                        msg = gg.QueryOrderStatus(billno);
                        if ("0".endsWith(msg)) {
                            return transferResponse("success", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
                        } else {
                            if(polls > 2){
                                return transferResponse("faild", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务失败");
                            }
                        }
                    } while (isPoll);
                }
            } else if ("CG".equalsIgnoreCase(type)) {
                String msg = "";
                CGGameServiceImpl c = new CGGameServiceImpl(data);
                msg = c.WITHDRAW(ag_username, ag_password, billno, credit + "");
                if ("success".equals(msg)) {
                    return transferResponse("success", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
                } else {
                    //轮询
                    boolean isPoll = true;
                    int polls = 0;
                    do {
                        Thread.sleep(2000);
                        logger.info("用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务,第{}次轮询转账订单次数", polls);
                        polls++;
                        msg = c.CHECK_REF(billno);
                        //6601为该单据已成功,6607为处理中,2秒后再次查询该订单状态
                        if ("6601".endsWith(msg)) {
                            return transferResponse("success", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
                        } else {
                            if(polls > 2){
                                return transferResponse("faild", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务失败");
                            }
                        }
                    } while (isPoll);
                }
            } else if ("IG".equalsIgnoreCase(type)) {
                String msg = "";
                IGGameServiceImpl c = new IGGameServiceImpl(data);
                msg = c.WITHDRAW(ag_username, ag_password, billno, credit + "");
                if ("success".equals(msg)) {
                    return transferResponse("success", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
                } else {
                    //轮询
                    boolean isPoll = true;
                    int polls = 0;
                    do {
                        Thread.sleep(2000);
                        logger.info("用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务,第{}次轮询转账订单次数", polls);
                        polls++;
                        msg = c.CHECK_REF(billno);
                        //6601为该单据已成功,6607为处理中,2秒后再次查询该订单状态
                        if ("6601".endsWith(msg)) {
                            return transferResponse("success", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
                        } else {
                            if(polls > 2){
                                return transferResponse("faild", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务失败");
                            }
                        }
                    } while (isPoll);
                }
            } else if ("IGPJ".equalsIgnoreCase(type)) {
                String msg = "";
                IGPJGameServiceImpl c = new IGPJGameServiceImpl(data);
                msg = c.WITHDRAW(ag_username, ag_password, billno, credit + "");
                if ("success".equals(msg)) {
                    return transferResponse("success", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
                } else {
                    //轮询
                    boolean isPoll = true;
                    int polls = 0;
                    do {
                        Thread.sleep(2000);
                        logger.info("用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务,第{}次轮询转账订单次数", polls);
                        polls++;
                        msg = c.CHECK_REF(billno);
                        //6601为该单据已成功,6607为处理中,2秒后再次查询该订单状态
                        if ("6601".endsWith(msg)) {
                            return transferResponse("success", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
                        } else {
                            if(polls > 2){
                                return transferResponse("faild", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务失败");
                            }
                        }
                    } while (isPoll);
                }
            } else if ("HG".equalsIgnoreCase(type)) {
                String msg = "";
                HGGameServiceImpl c = new HGGameServiceImpl(data);
                msg = c.WITHDRAW(hg_username, billno, credit + "");
                if ("success".equals(msg) || msg == "success") {
                    return transferResponse("success", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
                } else {
                    return transferResponse("faild", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务失败");
                }
            } else if ("BG".equalsIgnoreCase(type)) {
                String msg = "";
                BGGameServiceImpl c = new BGGameServiceImpl(data);
                msg = c.openBalanceTransfer(ag_username, "open.balance.transfer", billno, credit + "", "OUT", billno);
                if ("success".equals(msg) || msg == "success") {
                    return transferResponse("success", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
                } else {
                    return transferResponse("faild", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务失败");
                }

            } else if ("VR".equalsIgnoreCase(type)) {
                String msg = "";
                VRGameServiceImpl v = new VRGameServiceImpl(data);
                msg = v.Withdraw(billno, ag_username, credit);
                if ("success".equals(msg) || msg == "success") {
                    return transferResponse("success", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
                } else {
                    return transferResponse("faild", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务失败");
                }
            } else if ("JF".equalsIgnoreCase(type)) {
                String msg = "";
                JFGameServiceImpl jf = new JFGameServiceImpl(data);
                msg = jf.Transfer(ag_username, ag_password, billno, "OUT", credit + "");
                if ("success".equals(msg) || msg == "success") {
                    return transferResponse("success", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
                } else {
                    return transferResponse("faild", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务失败");
                }
            } else if ("KYQP".equalsIgnoreCase(type)) {
                String msg = "";
                KYQPGameServiceImpl k = new KYQPGameServiceImpl(data);
                msg = k.channelHandleOn(ag_username, billno, credit + "", "3");
                if ("success".equals(msg) || msg == "success") {
                    return transferResponse("success", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
                }else {
                    return transferResponse("faild", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务失败");
                }
            } else if ("LYQP".equalsIgnoreCase(type)) {
                String msg = "";
                LYQPGameServiceImpl lyqp = new LYQPGameServiceImpl(data);
                msg = lyqp.channelHandleOn(ag_username, billno, credit + "", "3");
                if ("success".equals(msg) || msg == "success") {
                    return transferResponse("success", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
                }else {
                    return transferResponse("faild", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务失败");
                }
            } else if ("VG".equalsIgnoreCase(type)) {
                String msg = "";
                VGGameServiceImpl vg = new VGGameServiceImpl(data);
                msg = vg.withdraw(ag_username, credit, billno);
                if ("success".equals(msg) || msg == "success") {
                    return transferResponse("success", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
                } else {
                    return transferResponse("faild", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务失败");
                }
            } else if ("GY".equalsIgnoreCase(type)) {
                String msg = "";
                GYGameServiceImpl c = new GYGameServiceImpl(data);
                msg = c.transOut(ag_username, ag_password, billno, credit + "");
                if ("success".equals(msg) || msg == "success") {
                    return transferResponse("success", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
                } else {
                    return transferResponse("faild", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务失败");
                }
            } else if ("PS".equalsIgnoreCase(type)) {
                String msg = "";
                PSGameServiceImpl ps = new PSGameServiceImpl(data);
                msg = ps.transOut(ag_username, billno, credit + "");
                if ("success".equals(msg) || msg == "success") {
                    return transferResponse("success", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
                } else {
                    return transferResponse("faild", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务失败");
                }
            } else if ("NB".equalsIgnoreCase(type)) {
                String msg = "";
                NBGameServiceImpl nb = new NBGameServiceImpl(data);
                msg = nb.transfer(ag_username, 0, 1, credit + "");
                if ("success".equals(msg) || msg == "success") {
                    return transferResponse("success", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
                } else {
                    return transferResponse("faild", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务失败");
                }
            } else if ("SW".equalsIgnoreCase(type)) {
                String msg = "";
                SWGameServiceImpl sw = new SWGameServiceImpl(data);
                msg = sw.transferFrom(ag_username, Double.valueOf(credit), billno);
                if ("success".equals(msg) || msg == "success") {
                    return transferResponse("success", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
                } else {
                    return transferResponse("faild", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务失败");
                }
            }else if("CQJ".equalsIgnoreCase(type)){
                CQJServiceimpl cqjServiceimpl = new CQJServiceimpl(data);
                TransferVO transferVO = new TransferVO();
                transferVO.setPassword(ag_password);
                transferVO.setAccount(ag_username);
                transferVO.setOrderNo(billno);
                transferVO.setMoney(Double.valueOf(credit));
                String returnMsg = cqjServiceimpl.transferOut(transferVO);
                if ("success".equalsIgnoreCase(returnMsg)) {
                    // 转账订单提交成功
                    return transferResponse("success", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
                } else {
                    return transferResponse("faild", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务失败");
                }
            }else if("ESW".equalsIgnoreCase(type)){
                ESWServiceImpl eSWServiceImpl = new ESWServiceImpl(data);
                String returnMsg = eSWServiceImpl.transferOut(ag_username,String.valueOf(credit),billno);
                if ("success".equalsIgnoreCase(returnMsg)) {
                    // 转账订单提交成功
                    return transferResponse("success", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
                } else {
                    return transferResponse("faild", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务失败");
                }
            }else if("IBC".equalsIgnoreCase(type)){
                IBCGameServiceImpl service = new IBCGameServiceImpl(data);
                TransferVO vo = new TransferVO();
                vo.setAccount(ag_username);
                vo.setMoney(Double.parseDouble(String.valueOf(credit)));
                vo.setOrderNo(billno);
                ResultResponse resultResponse = service.transferOut(vo);
                if(ResponseCode.SUCCESS_STATUS == resultResponse.getStatus()){
                    return transferResponse("success", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务成功");
                }else {
                    return transferResponse("faild", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务失败"+resultResponse.getMessage());
                }
            }
            return transferResponse("error", "操作游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务失败,游戏平台不存在");
        } catch (Exception e) {
            logger.info("用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务异常:{}",e.getMessage());
            return transferResponse("faild", "用户【"+username+"】游戏平台【" + type + "】向天下平台转入金额业务(游戏下分)业务失败");
        }
    }
}
