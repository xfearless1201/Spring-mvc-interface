package com.cn.tianxia.controller.v2;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
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

import com.cn.tianxia.common.v2.KeyConstant;
import com.cn.tianxia.controller.BaseController;
import com.cn.tianxia.controller.TransferController;
import com.cn.tianxia.game.GameReflectService;
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
import com.cn.tianxia.po.v2.GameResponse;
import com.cn.tianxia.po.v2.ResultResponse;
import com.cn.tianxia.service.UserService;
import com.cn.tianxia.service.v2.UserGameTransferService;
import com.cn.tianxia.util.DESEncrypt;
import com.cn.tianxia.util.FileLog;
import com.cn.tianxia.util.IPTools;
import com.cn.tianxia.util.PlatFromConfig;
import com.cn.tianxia.util.UserTypeHandicapUtil;
import com.cn.tianxia.vo.EswLoginVo;
import com.cn.tianxia.vo.v2.GameTransferVO;
import com.cn.tianxia.vo.v2.TransferVO;
import com.cn.tianxia.ws.LoginUserResponse;
import com.cn.tianxia.ws.QueryPlayerResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 
 * @ClassName GameTransferController
 * @Description 游戏转账接口
 * @author Hardy
 * @Date 2019年2月9日 下午9:24:10
 * @version 1.0.0
 */
@Controller
@RequestMapping("User")
public class GameTransferController extends BaseController{

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
//    @RequestMapping("/TransferTo")
//    @ResponseBody
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
                    return GameResponse.error("06", "转账失败,用户余额不足");
                }
                // 生成订单号
                String billno = generatorOrderNo(type, ag_username, pmap);
                if (StringUtils.isBlank(billno)) {
                    return GameResponse.error("error", "创建订单号为空");
                }
                logger.info("用户【"+username+"】,调用天下平台向游戏平台转入金额(游戏上分)接口,生成订单号:{}",billno);
                
                GameReflectService gameService = getGameReflectService(type, pmap);
                
//                JSONObject transferResult = transferInProcess(type, ag_username, hg_username, username,billno, credit, password, ip, pmap);
                GameTransferVO gameTransferVO = new GameTransferVO();
                gameTransferVO.setUid(uid);
                gameTransferVO.setAg_username(ag_username);
                gameTransferVO.setBillno(billno);
                gameTransferVO.setHg_username(hg_username);
                gameTransferVO.setIp(ip);
                gameTransferVO.setMoney(String.valueOf(credit));
                gameTransferVO.setPassword(password);
                gameTransferVO.setType(type);
                gameTransferVO.setUsername(username);
                JSONObject transferResult = gameService.transferIn(gameTransferVO);
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
        return GameResponse.error("error", "调用天下平台向游戏平台转入金额(游戏上分)失败");
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
//    @RequestMapping("/TransferFrom")
//    @ResponseBody
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
                    return GameResponse.error("06", "转账失败,用户游戏余额不足");
                }
                // 用户钱包余额
                double balance = userGameTransferService.getUserBalance(uid);

                // 生成订单号
                String billno = generatorOrderNo(type, ag_username, pmap);
                if (StringUtils.isBlank(billno)) {
                    return GameResponse.error("error", "创建订单号为空");
                }
                logger.info("用户【"+username+"】,调用游戏平台向天下平台转入金额(游戏下分)接口,生成订单号:{}",billno);
                
                GameReflectService gameService = getGameReflectService(type, pmap);
                
//                JSONObject transferResult = transferOutProcess(type, ag_username, hg_username, username,billno, credit, password, ip, pmap);
                GameTransferVO gameTransferVO = new GameTransferVO();
                gameTransferVO.setUid(uid);
                gameTransferVO.setAg_username(ag_username);
                gameTransferVO.setBillno(billno);
                gameTransferVO.setHg_username(hg_username);
                gameTransferVO.setIp(ip);
                gameTransferVO.setMoney(String.valueOf(credit));
                gameTransferVO.setPassword(password);
                gameTransferVO.setType(type);
                gameTransferVO.setUsername(username);
                JSONObject transferResult = gameService.transferOut(gameTransferVO);
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
        return GameResponse.error("error", "调用从游戏平台转向天下平台转出金额(游戏下分)失败");
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
                    jo.put("balance", "0.00");
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
                        jo.put("balance","0.00");
                    } else {
                        jo.put("balance", "0.00");
                    }
                }
                return jo;
            } else {
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
        } else if ("IBC".equals(type) || type == "IBC") {
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
        } else if ("ESW".equals(type)) {
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
                return GameResponse.error("error","用户游戏登录账号不能为空");
            }

            if (StringUtils.isBlank(ag_password)) {
                return GameResponse.error("error","用户游戏登录密码不能为空");
            }

            if (StringUtils.isBlank(type)) {
                return GameResponse.error("01", "请求参数平台编码不能为空");
            }

            if (credit == null || credit < 1 || credit > 100000000) {
                return GameResponse.error("02", "请输入合法金额,大于【1】且小于【100000000】之间的金额");
            }

            if (StringUtils.isBlank(suuid)) {
                return GameResponse.error("error", "用户UUID不能为空");
            }

            if (StringUtils.isBlank(uuid)) {
                return GameResponse.error("error", "请求参数uuid不能为空");
            }

            if (!suuid.equalsIgnoreCase(uuid)) {
                return GameResponse.error("error", "非法用户,请重新登录");
            }

            if (StringUtils.isNotBlank(simgcode)) {
                if (StringUtils.isBlank(imgcode)) {
                    return GameResponse.error("04", "验证不能空,请输入验证码");
                }

                if (!simgcode.equalsIgnoreCase(imgcode)) { // 忽略验证码大小写
                    return GameResponse.error("04", "验证码不正确,请输入正确的验证码");
                }
            }
            return GameResponse.success("校验转账请求参数成功");
        } catch (Exception e) {
            logger.info("校验转账参数异常:{}", e.getMessage());
            return GameResponse.error("error", "校验转账请求参数异常");
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
     * 
     * @Description 通过游戏类型获取游戏接口
     * @param type
     * @return
     */
    private GameReflectService getGameReflectService(String type,Map<String,String> pmap) throws Exception{
        logger.info("获取游戏反射接口开始====================START========================");
        try {
            // 组装游戏实现类路径
            StringBuffer sb = new StringBuffer();
            sb.append("com.cn.tianxia.game.impl").append(".");// 包名
            sb.append(type).append("GameServiceImpl");
            logger.info("反射接口包名:{}", sb.toString());
            // 创建构造器
            Constructor<?> constructor = Class.forName(sb.toString()).getConstructor(Map.class);
            GameReflectService gameService = (GameReflectService) constructor.newInstance(pmap);
            return gameService;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("获取游戏反射接口异常:{}", e.getMessage());
            throw new Exception("获取游戏反射接口异常");
        }
    }
    
}
