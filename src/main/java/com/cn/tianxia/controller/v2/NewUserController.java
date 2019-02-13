package com.cn.tianxia.controller.v2;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.cn.tianxia.po.v2.ResponseCode;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cn.tianxia.common.v2.KeyConstant;
import com.cn.tianxia.controller.BaseController;
import com.cn.tianxia.dao.v2.RechargeDao;
import com.cn.tianxia.entity.v2.RechargeEntity;
import com.cn.tianxia.game.OBService;
import com.cn.tianxia.game.SBService;
import com.cn.tianxia.game.impl.AGINGameServiceImpl;
import com.cn.tianxia.game.impl.AGGameServiceImpl;
import com.cn.tianxia.game.impl.BBINGameServiceImpl;
import com.cn.tianxia.game.impl.BGGameServiceImpl;
import com.cn.tianxia.game.impl.CGGameServiceImpl;
import com.cn.tianxia.game.impl.CQJServiceimpl;
import com.cn.tianxia.game.impl.DSGameServiceImpl;
import com.cn.tianxia.game.impl.ESWServiceImpl;
import com.cn.tianxia.game.impl.GGBYGameServiceImpl;
import com.cn.tianxia.game.impl.GYGameServiceImpl;
import com.cn.tianxia.game.impl.HGGameServiceImpl;
import com.cn.tianxia.game.impl.HABAGameServiceImpl;
import com.cn.tianxia.game.impl.IBCGameServiceImpl;
import com.cn.tianxia.game.impl.IGPJGameServiceImpl;
import com.cn.tianxia.game.impl.IGGameServiceImpl;
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
import com.cn.tianxia.po.v2.ResultResponse;
import com.cn.tianxia.service.UserService;
import com.cn.tianxia.service.impl.ReChangeServiceImpl;
import com.cn.tianxia.service.v2.NewUserService;
import com.cn.tianxia.service.v2.UserGameTransferService;
import com.cn.tianxia.util.DESEncrypt;
import com.cn.tianxia.util.IPTools;
import com.cn.tianxia.util.MD5Encoder;
import com.cn.tianxia.util.PTDes;
import com.cn.tianxia.util.PlatFromConfig;
import com.cn.tianxia.util.UserTypeHandicapUtil;
import com.cn.tianxia.vo.EswLoginVo;
import com.cn.tianxia.vo.v2.TransferVO;
import com.cn.tianxia.ws.LoginUserResponse;
import com.cn.tianxia.ws.QueryPlayerResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @ClassName NewUserController
 * @Description 用户接口
 * @author Hardy
 * @Date 2019年2月7日 下午3:57:48
 * @version 1.0.0
 */
@Controller
@RequestMapping("User")
public class NewUserController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserGameTransferService userGameTransferService;
    
    @Autowired
    private NewUserService newUserService;

    @Autowired
    private RechargeDao rechargeDao;

    @RequestMapping("/getBalance")
    @ResponseBody
    public JSONObject getBalance(HttpServletRequest request, HttpServletResponse response, String BType) {
        logger.info("调用查询用户钱包余额接口开始====================START=======================");
        JSONObject jo = new JSONObject();
        try {
            // 创建返回结果对象
            Object uidObj = request.getSession().getAttribute("uid");
            if (!ObjectUtils.anyNotNull(uidObj)) {
                // 用户ID为空,证明用户未登录
                jo.put("balance", "用户ID为空,登录已过期,请重新登录");
                return jo;
            }
            // 转换用户ID
            String uid = String.valueOf(uidObj);
            // 判断请求参数
            if (StringUtils.isBlank(BType)) {
                jo.put("balance", "请求参数异常:查询余额类型不能为空");
                return jo;
            }
            // 缓存中获取用户信息
            Map<String, String> cacheMap = loginmaps.get(uid);
            // 获取转账请求参数
            String ag_password = cacheMap.get("ag_password");// 游戏密码
            String ag_username = cacheMap.get("ag_username");
            String hg_username = cacheMap.get("hg_username");
            String username = cacheMap.get("userName");
            // 解密密码
            DESEncrypt d = new DESEncrypt(KeyConstant.DESKEY);
            ag_password = d.decrypt(ag_password);
            // 获取平台游戏配置信息
            Map<String, String> pmap = userGameTransferService.getPlatformConfig();
            // 检查维护状态
            if (!"WALLET".equals(BType)) {
                PlatFromConfig pf = new PlatFromConfig();
                pf.InitData(pmap, BType);
                if ("0".equals(pf.getPlatform_status())) {
                    jo.put("balance", "维护中");
                    return jo;
                }
            }
            if ("WALLET".equals(BType)) {
                // 获取数据库用户余额信息
                Map<String, Object> param = new HashMap<>();
                param.put("uid", uid);
                Map<String, Object> balanceMap = userService.selectUserById(param);
                jo.put("balance", balanceMap.get("wallet").toString());
                // 账号异常锁定账号
                double balance = Double.parseDouble(balanceMap.get("wallet").toString());
                if (balance < 0) {
                    Map<String, Object> umap = new HashMap<>();
                    umap.put("userName", username);
                    umap.put("is_stop", "1");
                    userService.updateGame(umap);
                }
                return jo;
            } else if ("JDB".equals(BType)) {
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
                    jo.put("balance", "维护中");
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
                        jo.put("balance", "维护中");
                        return jo;
                    } else {
                        jo.put("balance", balance);
                        return jo;
                    }
                } else {
                    jo.put("balance", "维护中");
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
                        jo.put("balance", "维护中");
                        return jo;
                    } else {
                        jo.put("balance", balance);
                        return jo;
                    }
                } else {
                    jo.put("balance", "维护中");
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
                    jo.put("balance", "维护中");
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
                    jo.put("balance", "维护中");
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
                    jo.put("balance", "维护中");
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
                    jo.put("balance", "维护中");
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
                        jo.put("balance", "维护中");
                        return jo;
                    }
                    jo.put("balance", json.get("bal").toString());
                } catch (Exception e) {
                    jo.put("balance", "维护中");
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
                gmap.put("ClientIP", IPTools.getIp(request));
                JSONObject json = m.queryBalance(ag_username, ag_password, gmap);
                try {
                    if ("success".equals(json.get("Code").toString())) {
                        jo.put("balance", json.get("Balance").toString());
                    } else {
                        jo.put("balance", "维护中");
                    }
                } catch (Exception e) {
                    jo.put("balance", "维护中");
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
                    jo.put("balance", "维护中");
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
                        jo.put("balance", "维护中");
                    } else {
                        String balance = json.getString("BALANCE").toString();
                        jo.put("balance", balance);
                    }
                } catch (Exception e) {
                    jo.put("balance", "维护中");
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
                    jo.put("balance", "维护中");
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
                    jo.put("balance", "维护中");
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
                    jo.put("balance", "维护中");
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
                    jo.put("balance", "维护中");
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
                    jo.put("balance", "维护中");
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
                StringBuffer url = request.getRequestURL();
                String tempContextUrl = url.delete(url.length() - request.getRequestURI().length(), url.length())
                        .append("/").toString();
                BGGameServiceImpl c = new BGGameServiceImpl(pmap);
                JSONObject jsonObjec = c.openUserCommonAPI(ag_username, "open.balance.get", "", "", tempContextUrl);
                if ("success".equals(jsonObjec.get("code"))) {
                    jo.put("balance", JSONObject.fromObject(jsonObjec.get("params")).get("result"));
                } else {
                    jo.put("balance", "维护中");
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
                    jo.put("balance", "维护中");
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
                    jo.put("balance", "维护中");
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
                    jo.put("balance", "维护中");
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
                    jo.put("balance", "维护中");
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
                    jo.put("balance", "维护中");
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
                    jo.put("balance", "维护中");
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
                    jo.put("balance", "维护中");
                } else {
                    jo.put("balance", balance);
                }
                return jo;
            }else if ("CQJ".equals(BType)) {
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
            }else if ("ESW".equals(BType)) {
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
                    jo.put("balance", "维护中");
                }
                return jo;
            } else if ("IBC".equals(BType)) {
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
                jo.put("balance", "维护中");
                return jo;
            }
            return jo;
        } catch (Exception e) {
            jo.put("balance", "0.00");
            return jo;
        }
    }

    /**
     * @Description 跳转游戏
     * @param request
     * @param response
     * @param gameType
     * @param gameID
     * @param model
     * @return
     */
    @RequestMapping("/forwardGame")
    @ResponseBody
    public JSONObject forwardGame(HttpServletRequest request, HttpServletResponse response, String gameType,
            String gameID, String model) {
        logger.info("调用获取游戏跳转链接接口开始==================START===================");
        JSONObject jo = new JSONObject();
        try {
            Object uidObj = request.getSession().getAttribute("uid");
            if (!ObjectUtils.anyNotNull(uidObj)) {
                // 用户ID为空,证明用户未登录
                return BaseResponse.error("0", "error");
            }
            // 转换用户ID
            String uid = String.valueOf(uidObj);
            // 缓存中获取用户信息
            Map<String, String> cacheMap = loginmaps.get(uid);
            // 获取转账请求参数
            String ag_password = cacheMap.get("ag_password");// 游戏密码
            String ag_username = cacheMap.get("ag_username");
            String hg_username = cacheMap.get("hg_username");
            String cid = cacheMap.get("cid");
            String ip = IPTools.getIp(request);
            // 解密密码
            DESEncrypt d = new DESEncrypt(KeyConstant.DESKEY);
            ag_password = d.decrypt(ag_password);
            // 获取平台游戏配置信息
            Map<String, String> pmap = userGameTransferService.getPlatformConfig();
            // 检查维护状态
            PlatFromConfig pf = new PlatFromConfig();
            pf.InitData(pmap, gameType);
            if ("0".equals(pf.getPlatform_status())) {
                return BaseResponse.faild("0", "process");
            }

            String gd = StringUtils.isBlank(gameID) ? "" : gameID.length() > 1 ? "" : gameID;
            String gameKey = gameType.trim() + gd;
            // ig彩票的特殊判断
            if (gameKey.indexOf("IGLOTTERY") != -1) {
                gameKey = "IGLOTTERY";
            } else if (gameKey.indexOf("IGPJ") != -1) {
                gameKey = "IGPJ";
            } else if (gameKey.indexOf("VR") != -1) {
                gameKey = "VR";
            } else if (gameKey.indexOf("KYQP") != -1) {
                gameKey = "KYQP";
            } else if (gameKey.indexOf("LYQP") != -1) {
                gameKey = "LYQP";
            }
            // 检查平台游戏开关状态
            Map<String, String> platformStatus = userGameTransferService.getPlatformStatusByCid(cid);
            if (CollectionUtils.isEmpty(platformStatus)) {
                logger.info("查询平台游戏开关状态失败");
                return BaseResponse.faild("0", "查询平台游戏开关状态失败");
            }
            Iterator<String> iterator = platformStatus.keySet().iterator();
            while (iterator.hasNext()) {
                String gametype = iterator.next();
                String val = String.valueOf(platformStatus.get(gametype));
                if (gameKey.equalsIgnoreCase(gametype) && !"1".equals(val)) {
                    return BaseResponse.faild("0", "process");
                }
            }
            checkGameReg(request,uid, ag_username, hg_username, ag_password, gameType, ip, pmap);
            if (gameType == "YOPLAY" || "YOPLAY".equals(gameType)) {
                String sid = "AGIN" + System.currentTimeMillis();
                AGINGameServiceImpl agService = new AGINGameServiceImpl(pmap);
                // 设置盘口 默认设置IG默认盘口A
                UserTypeHandicapUtil uth = new UserTypeHandicapUtil();
                String handicap = uth.getHandicap(gameType, uid, userService);
                handicap = handicap.isEmpty() ? "A" : handicap;

                if (StringUtils.isBlank(gameID)) {
                    gameID = "YP800";
                }
                // 游戏id前缀必须YP
                if (!"YP".equals(gameID.substring(0, 2))) {
                    jo.put("msg", "error");
                    return jo;
                }
                // 截取游戏id必须在800-850范围值之内
                int number = Integer.parseInt(gameID.substring(2, 5));
                if (!(number >= 800 && number < 850)) {
                    gameID = "YP800";
                }
                if ("mobile".equals(model)) {
                    String url = agService.forwardMobileGame(ag_username, ag_password, ip, sid, gameID, handicap);
                    jo.put("msg", url);
                    jo.put("type", "from");
                } else {
                    String url = agService.forwardGame(ag_username, ag_password, ip, sid, gameID, handicap);
                    jo.put("msg", url);
                    jo.put("type", "from");
                }
            } else if (gameType == "TASSPTA" || "TASSPTA".equals(gameType)) {
                String sid = "AGIN" + System.currentTimeMillis();
                AGINGameServiceImpl agService = new AGINGameServiceImpl(pmap);
                // 设置盘口 默认设置IG默认盘口A
                UserTypeHandicapUtil uth = new UserTypeHandicapUtil();
                String handicap = uth.getHandicap(gameType, uid, userService);
                handicap = handicap.isEmpty() ? "A" : handicap;

                if ("mobile".equals(model)) {
                    String url = agService.forwardMobileGame(ag_username, ag_password, ip, sid, gameType, handicap);
                    jo.put("msg", url);
                    jo.put("type", "from");
                } else {
                    String url = agService.forwardGame(ag_username, ag_password, ip, sid, gameType, handicap);
                    jo.put("msg", url);
                    jo.put("type", "from");
                }
            } else if (gameType == "AGIN" || "AGIN".equals(gameType)) {
                String sid = "AGIN" + System.currentTimeMillis();
                AGINGameServiceImpl agService = new AGINGameServiceImpl(pmap);
                // 设置盘口 默认设置IG默认盘口A
                UserTypeHandicapUtil uth = new UserTypeHandicapUtil();
                String handicap = uth.getHandicap(gameType, uid, userService);
                handicap = handicap.isEmpty() ? "A" : handicap;

                if ("mobile".equals(gameID)) {
                    String url = agService.forwardMobileGame(ag_username, ag_password, ip, sid, "11", handicap);
                    jo.put("msg", url);
                    jo.put("type", "from");
                } else {
                    String url = agService.forwardGame(ag_username, ag_password, ip, sid, "0", handicap);
                    jo.put("msg", url);
                    jo.put("type", "from");
                }
            } else if (gameType == "AG" || "AG".equals(gameType)) {
                String sid = "AG" + System.currentTimeMillis();
                AGGameServiceImpl agService = new AGGameServiceImpl(pmap);

                // 设置盘口 默认设置IG默认盘口A
                UserTypeHandicapUtil uth = new UserTypeHandicapUtil();
                String handicap = uth.getHandicap(gameType, uid, userService);
                handicap = handicap.isEmpty() ? "A" : handicap;

                if ("mobile".equals(gameID)) {
                    String url = agService.forwardMobileGame(ag_username, ag_password, ip, sid, "11", handicap);
                    jo.put("msg", url);
                    jo.put("type", "from");
                } else {
                    String url = agService.forwardGame(ag_username, ag_password, ip, sid, "0", handicap);
                    jo.put("msg", url);
                    jo.put("type", "from");
                }

            } else if (gameType == "BBIN" || "BBIN".equals(gameType)) {
                int gameno = 0;
                try {
                    gameno = Integer.parseInt(gameID);
                } catch (Exception e) {

                }
                // 1为真人,2为电子游艺
                if ("1".equals(gameID) || "2".equals(gameID) || "3".equals(gameID)) {
                    BBINGameServiceImpl b = new BBINGameServiceImpl(pmap);
                    if ("3".equals(gameID)) {
                        b.Logout(ag_username, ag_password);
                        String msg = b.Login(ag_username, ag_password, "Ltlottery");
                        jo.put("msg", msg);
                        jo.put("type", "link");
                    } else if ("2".equals(gameID)) {
                        b.Logout(ag_username, ag_password);
                        String msg = b.Login(ag_username, ag_password, "game");
                        // System.out.println(msg);
                        jo.put("msg", msg);
                        jo.put("type", "link");
                    } else {
                        b.Logout(ag_username, ag_password);
                        String msg = b.Login(ag_username, ag_password, "live");
                        // System.out.println(msg);
                        jo.put("msg", msg);
                        jo.put("type", "link");
                    }
                } else if (gameno > 5000 && gameno < 6000) {
                    BBINGameServiceImpl b = new BBINGameServiceImpl(pmap);
                    String link1 = b.Login2(ag_username, ag_password);
                    String link2 = b.PlayGame(ag_username, ag_password, gameID);
                    jo.put("link1", link1);
                    jo.put("link2", link2);
                    jo.put("type", "login");
                } else {
                    jo.put("msg", "error");
                }
            } else if (gameType == "DS" || "DS".equals(gameType)) {
                DSGameServiceImpl ds = new DSGameServiceImpl(pmap);
                String msg = ds.LoginGame(ag_username, ag_password);
                // System.out.println(msg);
                JSONObject json;
                json = JSONObject.fromObject(msg);
                json = json.getJSONObject("params");
                msg = json.getString("link");
                jo.put("msg", msg);
                jo.put("type", "link");
            } else if (gameType == "OB" || "OB".equals(gameType)) {
                OBService a = new OBGameServiceImpl(pmap);
                // a.check_or_create(ag_username, ag_password);
                String msg = a.forward_game(ag_username, ag_password, model);
                JSONObject json;
                json = JSONObject.fromObject(msg);
                msg = json.getString("gameLoginUrl");

                jo.put("msg", msg);
                jo.put("type", "link");
            } else if (gameType == "OG" || "OG".equals(gameType)) {
                OGGameServiceImpl o = new OGGameServiceImpl(pmap);
                o.CreateMem(ag_username, ag_password);
                String msg = o.Logingame(ag_username, ag_password, gameID);
                jo.put("msg", msg);
                jo.put("type", "link");
            } else if (gameType == "SB" || "SB".equals(gameType)) {
                // 1为真人视讯,2为电子游戏
                if ("1".equals(gameID) || "2".equals(gameID) || "3".equals(gameID) || "4".equals(gameID)) {

                    SBService s = new SBGameServiceImpl(pmap);
                    // 获取平台授权token
                    String atoken = s.getAccToken();
                    JSONObject json = new JSONObject();
                    json = JSONObject.fromObject(atoken);

                    // 设置盘口 默认设置申博默认盘口1-5
                    UserTypeHandicapUtil uth = new UserTypeHandicapUtil();
                    String handicap = uth.getHandicap("SB", uid, userService);
                    handicap = handicap.isEmpty() ? "4" : handicap;

                    // 获取用户token
                    String utoken = s.getUserToken(IPTools.getIp(request), ag_username, ag_username,
                            json.getString("access_token"), handicap, model);
                    json = JSONObject.fromObject(utoken);
                    // 获取游戏连接
                    String url = s.getGameUrl(json.getString("authtoken"), gameID);

                    jo.put("msg", url);
                    jo.put("type", "link");
                } else {
                    jo.put("msg", "error");
                }
            } else if (gameType == "MG" || "MG".equals(gameType)) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("ClientIP", IPTools.getIp(request));
                map.put("language", "zh");
                map.put("gameId", gameID);
                if ("fun".equals(model)) {
                    map.put("demoMode", "true");
                } else {
                    map.put("demoMode", "false");
                }
                MGGameServiceImpl m = new MGGameServiceImpl(pmap);
                JSONObject json = m.loginGame(ag_username, ag_password, map);
                if ("success".equals(json.get("Code").toString())) {
                    jo.put("msg", json.get("LaunchUrl").toString());
                    jo.put("type", "link");
                } else {
                    jo.put("msg", "error");
                }

            } else if (gameType == "HABA" || "HABA".equals(gameType)) {
                if (gameID == null || "".equals(gameID)) {
                    jo.put("msg", "error");
                }
                HABAGameServiceImpl h = new HABAGameServiceImpl(pmap);
                h.loginOrCreatePlayer(ag_username, ag_password, null);

                // 查询玩家信息
                QueryPlayerResponse qp = h.queryPlayer(ag_username, ag_password, null);
                if (qp.isFound() == true) {
                    if (!"fun".equals(model) && !"real".equals(model)) {
                        model = "real";
                    }
                    JSONObject joo = JSONObject.fromObject(pf.getPlatform_config());
                    String gameurl = joo.getString("gameurl").toString();
                    String refurl = request.getHeader("referer");
                    String[] urls = refurl.split("/");
                    String str = gameurl + "/play?brandid=" + qp.getBrandId() + "&keyname=" + gameID + "&token="
                            + qp.getToken() + "&mode=" + model + "&locale=zh-CN&lobbyurl=" + urls[2];
                    jo.put("msg", str);
                    jo.put("type", "link");
                } else {
                    jo.put("msg", "error");
                }
            } else if (gameType == "JDB" || "JDB".equals(gameType)) {
                String msg = "";
                JDBGameServiceImpl jdb = new JDBGameServiceImpl(pmap);
                if ("mobile".equals(model)) {
                    msg = jdb.login(ag_username, gameID, "MB");
                } else {
                    msg = jdb.login(ag_username, gameID, "");
                }
                jo.put("msg", msg);
            } else if (gameType == "PT" || "PT".equals(gameType)) {
                if (gameID == null || "".equals(gameID)) {
                    jo.put("msg", "error");
                }

                PTGameServiceImpl p = new PTGameServiceImpl(pmap);
                String msg = p.CreatePlayer(ag_username, ag_password);
                if (msg == null || "".equals(msg)) {
                    jo.put("msg", "error");
                } else {
                    if (model == null) {
                        model = "pc";
                    }
                    String ptsrc = "";
                    if ("mobile".equals(model)) {
                        ptsrc = "http://m.theworldpt.com/LoginM.aspx?uid=";
                    } else {
                        ptsrc = "http://pc.theworldpt.com/login.aspx?uid=";
                    }
                    String ptparam = "?use=" + ag_username + "&pwd=" + ag_password + "&code=" + gameID;
                    String key = "tX9UiO3b";

                    try {
                        ptparam = PTDes.toHexString(PTDes.encrypt(ptparam, key)).toUpperCase();
                    } catch (Exception e) {

                    }
                    jo.put("msg", ptsrc + ptparam);
                    jo.put("type", "link");
                }
            } else if (gameType == "GGBY" || "GGBY".equals(gameType)) {
                GGBYGameServiceImpl gg = new GGBYGameServiceImpl(pmap);
                String sid = "TE179" + System.currentTimeMillis();
                gg.CheckOrCreateGameAccout(ag_username, ag_password);
                String url = gg.forwardGame(ag_username, ag_password, sid, ip);
                jo.put("msg", url);
                jo.put("type", "link");

            } else if (gameType == "AGBY" || "AGBY".equals(gameType)) {
                String sid = "AGIN" + System.currentTimeMillis();
                String url = "";
                AGINGameServiceImpl agService = new AGINGameServiceImpl(pmap);
                // 默认设置a盘口 新增AGBY H5支持 2018-08-18
                if ("mobile".equals(model)) {
                    url = agService.forwardMobileGame(ag_username, ag_password, ip, sid, "6", "A");
                } else {
                    url = agService.forwardGame(ag_username, ag_password, ip, sid, "6", "A");
                }
                jo.put("msg", url);
                jo.put("type", "from");
            } else if (gameType == "CG" || "CG".equals(gameType)) {
                CGGameServiceImpl c = new CGGameServiceImpl(pmap);
                String msg = c.LoginGame(ag_username, ag_password);
                JSONObject json;
                json = JSONObject.fromObject(msg);
                json = json.getJSONObject("params");
                msg = json.getString("link");
                jo.put("msg", msg);
                jo.put("type", "link");
            } else if (gameType == "IGLOTTO" || "IGLOTTO".equals(gameType) || gameType == "IGLOTTERY"
                    || "IGLOTTERY".equals(gameType)) {
                // 设置盘口 默认设置IG默认盘口A
                UserTypeHandicapUtil uth = new UserTypeHandicapUtil();
                String handicap = uth.getHandicap(gameType, uid, userService);
                handicap = handicap.isEmpty() ? "A" : handicap;

                if (model == null || (!"PC".equals(model) && !"MB".equals(model))) {
                    model = "PC";
                }
                if (gameID == null) {
                    jo.put("msg", "error");
                    return jo;
                } else if (gameType == "IGLOTTO" || "IGLOTTO".equals(gameType)) {
                    gameType = "LOTTO";
                } else if (gameType == "IGLOTTERY" || "IGLOTTERY".equals(gameType)) {
                    gameType = "LOTTERY";
                    if (gameID == null || "".equals(gameID)) {
                        jo.put("msg", "error");
                        return jo;
                    }
                } else {
                    jo.put("msg", "error");
                    return jo;
                }
                IGGameServiceImpl c = new IGGameServiceImpl(pmap);
                String msg = c.LoginGame(ag_username, ag_password, gameType, gameID, model, handicap);
                JSONObject json;
                json = JSONObject.fromObject(msg);
                json = json.getJSONObject("params");
                msg = json.getString("link");
                jo.put("msg", msg);
                jo.put("type", "link");
            } else if (gameType == "IGPJLOTTO" || "IGPJLOTTO".equals(gameType) || gameType == "IGPJLOTTERY"
                    || "IGPJLOTTERY".equals(gameType)) {

                // 设置盘口 默认设置IG默认盘口A
                UserTypeHandicapUtil uth = new UserTypeHandicapUtil();
                String handicap = uth.getHandicap(gameType, uid, userService);
                handicap = handicap.isEmpty() ? "A" : handicap;

                if (model == null || (!"PC".equals(model) && !"MB".equals(model))) {
                    model = "PC";
                }
                if (gameID == null) {
                    jo.put("msg", "error");
                    return jo;
                } else if (gameType == "IGPJLOTTO" || "IGPJLOTTO".equals(gameType)) {
                    gameType = "LOTTO";
                } else if (gameType == "IGPJLOTTERY" || "IGPJLOTTERY".equals(gameType)) {
                    gameType = "LOTTERY";
                    if (gameID == null || "".equals(gameID)) {
                        jo.put("msg", "error");
                        return jo;
                    }
                } else {
                    jo.put("msg", "error");
                    return jo;
                }
                IGPJGameServiceImpl c = new IGPJGameServiceImpl(pmap);
                String msg = c.LoginGame(ag_username, ag_password, gameType, gameID, model, handicap);
                JSONObject json;
                json = JSONObject.fromObject(msg);
                json = json.getJSONObject("params");
                msg = json.getString("link");
                jo.put("msg", msg);
                jo.put("type", "link");
            } else if (gameType == "HG" || "HG".equals(gameType)) {
                HGGameServiceImpl c = new HGGameServiceImpl(pmap);
                if (model == null || (!"MB".equals(model) && !"PC".equals(model))) {
                    model = "PC";
                }
                // if (hg_username.length() < 10) {
                // jo.put("msg", "error");
                // }
                String msg = c.getLogin(hg_username, model);
                jo.put("msg", msg);
                jo.put("type", "link");
            } else if (gameType == "BG" || "BG".equals(gameType)) {
                String method = "";
                if ("1".equals(gameID)) {
                    method = "open.video.game.url";// 视讯
                } else if ("2".equals(gameID)) {
                    method = "open.lottery.game.url";// 彩票
                }
                StringBuffer url1 = request.getRequestURL();
                String tempContextUrl = url1.delete(url1.length() - request.getRequestURI().length(), url1.length())
                        .append("/").toString();
                BGGameServiceImpl c = new BGGameServiceImpl(pmap);
                JSONObject json = c.openUserCommonAPI(ag_username, method, model, "", tempContextUrl);
                if ("success".equals(json.get("code"))) {
                    String url = JSONObject.fromObject(json.get("params")).get("result").toString();
                    request.getSession().setAttribute(uid + "BGToken", url);
                    jo.put("msg", url);
                    jo.put("type", "link");
                } else {
                    jo.put("msg", "error");
                }
            } else if (gameType == "VR" || "VR".equals(gameType)) {
                if (gameID == null || "".equals(gameID)) {
                    gameID = "0";// 视讯
                }

                // 默认设置vr彩票盘口为空则使用后台配置
                UserTypeHandicapUtil uth = new UserTypeHandicapUtil();
                String handicap = uth.getHandicap("VR", uid, userService);
                handicap = handicap.isEmpty() ? "" : handicap;

                VRGameServiceImpl v = new VRGameServiceImpl(pmap);
                String url = v.LoginGame(ag_username, gameID, handicap);
                jo.put("msg", url);
                jo.put("type", "link");
            } else if (gameType == "JF" || "JF".equals(gameType)) {
                // String Model="";
                // if (gameID==null||"".equals(gameID)) {
                // Model = "";// 牛牛
                // }else if(gameID=="0"){
                // Model = "";// 牛牛
                // }else{
                // Model=gameID;//彩票
                // }
                JFGameServiceImpl jf = new JFGameServiceImpl(pmap);
                // {"Success":"回传结果(true/false)" , "Code":"返回值" ,
                // "Message":"消息",Username:”进游戏时的用户名”}
                // String result= jf.CreateUser(ag_username, ag_password,
                // ag_username);
                // JSONObject json = JSONObject.fromObject(result);
                // String loginName="";
                /* 登录游戏带代理前缀的用户名 */
                // if (json.getString("Code").equals("200")) {// 用户已经存在
                // loginName = json.getString("Username");
                // }else {
                // if(pmap.containsKey("agent")){
                // loginName=pmap.get("agent").toString()+ag_username;
                // }else{
                // loginName=ag_username;
                // }
                // }
                String url = jf.loginGame(ag_username, ag_password, gameID);
                jo.put("msg", url);
                jo.put("type", "link");
            } else if (gameType == "KYQP" || "KYQP".equals(gameType)) {
                if (gameID == null || "".equals(gameID)) {
                    gameID = "0";// 大厅
                }
                KYQPGameServiceImpl k = new KYQPGameServiceImpl(pmap);
                String url = k.checkOrCreateGameAccout(ag_username, ip, gameID);
                if ("error".equals(url)) {
                    jo.put("msg", "error");
                } else {
                    jo.put("msg", JSONObject.fromObject(url).getJSONObject("d").getString("url"));
                    jo.put("type", "link");
                }
            } /*
               * else if ("ESW".equals(gameType)) { if (StringUtils.isNullOrEmpty(gameID)) { gameID = "0";// 大厅 }
               * ESWServiceImpl eswService = new ESWServiceImpl(pmap); EswLoginVo eswLoginVo = new
               * EswLoginVo(ag_username,ip,Integer.parseInt(gameID)); String data =
               * eswService.checkOrCreateGameAccout(eswLoginVo); JSONObject jsonObject = JSONObject.fromObject(data); if
               * (jsonObject.getInt("code") == 0) { jo.put("msg", jsonObject.getString("fullUrl")); jo.put("type",
               * "link"); } else { jo.put("msg", jsonObject.getInt("code")); } }
               */ else if (gameType == "LYQP" || "LYQP".equals(gameType)) {
                if (gameID == null || "".equals(gameID)) {
                    gameID = "0";// 大厅
                }
                LYQPGameServiceImpl lyqp = new LYQPGameServiceImpl(pmap);
                String url = lyqp.checkOrCreateGameAccout(ag_username, ip, gameID);
                if ("error".equals(url)) {
                    jo.put("msg", "error");
                } else {
                    jo.put("msg", JSONObject.fromObject(url).getJSONObject("d").getString("url"));
                    jo.put("type", "link");
                }
            } else if (gameType == "VG" || "VG".equals(gameType)) {
                if (gameID == null || "".equals(gameID)) {
                    gameID = "1000";// 大厅
                }
                VGGameServiceImpl vg = new VGGameServiceImpl(pmap);
                String url = vg.loginWithChannel(ag_username, gameID, model, "");
                if ("error".equals(url)) {
                    jo.put("msg", "error");
                } else {
                    jo.put("msg", url);
                    jo.put("type", "link");
                }
            } else if (gameType == "GY" || "GY".equals(gameType)) {
                GYGameServiceImpl gy = new GYGameServiceImpl(pmap);
                String msg = gy.login(ag_username, ag_password, model);
                if ("error".equals(msg)) {
                    jo.put("msg", "error");
                } else {
                    jo.put("msg", msg);
                    jo.put("type", "link");
                }
            } else if (gameType == "PS" || "PS".equals(gameType)) {

                StringBuffer url = request.getRequestURL();
                String return_url = url.delete(url.length() - request.getRequestURI().length(), url.length())
                        .append("/").toString();
                String subgame_id = "";

                UUID uuid = UUID.randomUUID();
                String access_token = uuid.toString();
                int step = 3; // 表示预生成token状态
                // 生成登录验证token
                userService.insertPSToken(access_token, step, uid);

                PSGameServiceImpl ps = new PSGameServiceImpl(pmap);
                String forwardUrl = ps.login(ag_username, subgame_id, gameID, access_token, return_url);
                if ("error".equals(forwardUrl)) {
                    jo.put("msg", "error");
                } else {
                    jo.put("msg", forwardUrl);
                    jo.put("type", "link");
                }
            } else if (gameType == "NB" || "NB".equals(gameType)) {
                NBGameServiceImpl nb = new NBGameServiceImpl(pmap);
                String forwardUrl = nb.login(ag_username);
                if ("error".equals(forwardUrl)) {
                    jo.put("msg", "error");
                } else {
                    jo.put("msg", forwardUrl);
                    jo.put("type", "link");
                }
            } else if (gameType == "SW" || "SW".equals(gameType)) {
                SWGameServiceImpl sw = new SWGameServiceImpl(pmap);
                String forwardUrl = sw.loginGame(gameID, ag_username);
                if ("error".equals(forwardUrl)) {
                    jo.put("msg", "error");
                } else {
                    jo.put("msg", forwardUrl);
                    jo.put("type", "link");
                }
            }else if (gameType.equalsIgnoreCase("IBC")) {
                IBCGameServiceImpl ibcService = new IBCGameServiceImpl(pmap);
                TransferVO transferVO = new TransferVO();
                transferVO.setGameId(gameID);
                transferVO.setAccount(ag_username);
                transferVO.setTerminal(model);
                ResultResponse resultResponse = ibcService.loginGame(transferVO);
                String forwardUrl = resultResponse.getData().toString();
                if (resultResponse.getStatus() == 0) {
                    jo.put("msg", "error");
                } else {
                    jo.put("msg", forwardUrl);
                    jo.put("type", "link");
                }
            } else if ("CQJ".equals(gameType)) {
                logger.info("ag_username,ag_password" + ag_username + ag_password);
                CQJServiceimpl cqjServiceimpl = new CQJServiceimpl(pmap);
                String cqjUrl = cqjServiceimpl.getLobbylink(ag_username, ag_password);
                if ("error".equals(cqjUrl)) {
                    jo.put("msg", "error");
                } else {
                    jo.put("msg", cqjUrl);
                    jo.put("type", "link");
                }
            } else if ("ESW".equals(gameType)) {
                ESWServiceImpl eswService = new ESWServiceImpl(pmap);
                EswLoginVo eswLoginVo = new EswLoginVo(ag_username,ip,Integer.parseInt(gameID));
                String data = eswService.checkOrCreateGameAccout(eswLoginVo);
                JSONObject jsonObject = JSONObject.fromObject(data);
                if (jsonObject.getInt("code") == 0) {
                    jo.put("msg", jsonObject.getString("fullUrl"));
                    jo.put("type", "link");
                } else {
                    jo.put("msg", jsonObject.getInt("code"));
                }
            }else {
                jo.put("msg", "error");
            }
            return jo;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("调用获取游戏跳转链接接口异常:{}", e.getMessage());
            return BaseResponse.error("0", "调用获取游戏跳转链接接口异常");
        }
    }

    
   
    /**
     * 
     * @Description 获取用户详情
     * @param request
     * @return
     */
    @RequestMapping("/getUserInfo")
    @ResponseBody
    public JSONObject getUserInfo(HttpServletRequest request,HttpServletResponse response) {
        logger.info("调用获取用户详情接口开始=============START================");
        try {
            Object uidObj = request.getSession().getAttribute("uid");
            if (!ObjectUtils.anyNotNull(uidObj)) {
                // 用户ID为空,证明用户未登录
                return BaseResponse.error("0", "用户ID为空,登录已过期,请重新登录");
            }
            // 转换用户ID
            String uid = String.valueOf(uidObj);
            return newUserService.getUserInfo(uid);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("调用获取用户详情接口异常:{}",e.getMessage());
            return BaseResponse.error("0", "调用获取用户详情接口异常");
        }
    }
    
    
    
    
    
    
    
    
    /**
     * 
     * @Description 检查用户游戏开关状态
     * @param request
     * @param uid
     * @param ag_username
     * @param hg_username
     * @param ag_password
     * @param type
     * @param ip
     * @param pmap
     */
    public void checkGameReg(HttpServletRequest request,String uid, String ag_username, String hg_username, String ag_password, String type,
            String ip, Map<String, String> pmap) throws Exception{
        // 检查维护状态
        PlatFromConfig pf = new PlatFromConfig();
        pf.InitData(pmap, type);
        if ("0".equals(pf.getPlatform_status())) {
            return;
        }

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
                atoken = s.getUserToken(ip, ag_username, ag_username, json.get("access_token").toString(), handicap,
                        "");
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
                msg = k.checkOrCreateGameAccout(ag_username, ip, "0");
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
                msg = lyqp.checkOrCreateGameAccout(ag_username, ip, "0");
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
        } else if ("IBC".equalsIgnoreCase(type)) {
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
     *
     * @Description 获取存款链接
     *
     * @param payType 平台 1：网银 2：微信 3：支付宝
     * @param bankCode 银行编码 payType为1时必填
     * @param amount 金额
     */
    @RequestMapping("/ReChangePay")
    @ResponseBody
    public JSONObject ReChangPay(HttpServletRequest request, String payType,String bankCode,String amount) {
        logger.info("获取存款链接接口--------------------开始-----------------");
        try {
            HttpSession session = request.getSession();
            Object uidAttr = session.getAttribute("uid");
            if (!ObjectUtils.allNotNull(uidAttr)) {
                return BaseResponse.error( BaseResponse.ERROR_CODE , "用户未登录");
            }

            String uid = uidAttr.toString();
            String ip = IPTools.getIp(request);
            String billNo = String.valueOf(System.currentTimeMillis());
            double amt = Double.parseDouble(amount);

            if ("1".equals(payType) && StringUtils.isBlank(bankCode)) {
                return BaseResponse.error( BaseResponse.ERROR_CODE, "error");
            }

            if (amt < 0) {
                return BaseResponse.error( BaseResponse.ERROR_CODE, "error");
            }

            if (StringUtils.isBlank(bankCode)) {
                bankCode = "";
            }

            DESEncrypt d = new DESEncrypt(KeyConstant.DESKEY);

            String requestParam = d.encrypt(billNo);

            ReChangeServiceImpl r = new ReChangeServiceImpl();
            String url = r.ReChange_Pay(payType, bankCode, billNo, amount + "", ip, requestParam);
            if (!"error".equals(url)) {
                RechargeEntity entity = new RechargeEntity();
                entity.setUid(Integer.parseInt(uid));
                entity.setBankCode(bankCode);
                entity.setOrderNo(billNo);
                entity.setPayAmount(amt);
                entity.setOrderAmount(amt);
                entity.setOrderTime(new Date());
                entity.setMerchant("");
                entity.setTradeStatus("paying");
                entity.setTradeNo("");
                entity.setIp(""); //ip
                entity.setPayId(1);
                rechargeDao.insertSelective(entity);

            }
            return BaseResponse.success(url);

        } catch (Exception e) {
            logger.error("获取存款链接接口异常：" + e.getMessage());
            return BaseResponse.error(BaseResponse.ERROR_CODE,"error");
        }
    }
    /**
     * 存款回调函数
     *
     * @param request
     * @return
     */
    @RequestMapping("/ReChangePayCallBack")
    @ResponseBody
    public String ReChangePayCallBack(HttpServletRequest request, String merchant_code, String sign, String order_no,
                                      String order_amount, String order_time, String trade_status, String trade_no, String return_params) {
        DESEncrypt d = new DESEncrypt(KeyConstant.DESKEY);
        String key = "";
        try {
            // 验证加密参数
            return_params = d.decrypt(return_params);
            if (!return_params.equals(order_no)) {
                return "error";
            }
            // 验证商户号以及sign签名
            if ("1326".equals(merchant_code)) {
                key = "dc127891953a13be6c1c1aa9d7b0d895";
            } else if ("1333".equals(merchant_code)) {
                key = "20dfee2dcf1a16f825bef3b1281c45e7";
            } else if ("1335".equals(merchant_code)) {
                key = "4700859b79f93de7deffc3e5a67ffccc";
            } else {
                return "error";
            }
            String params = "merchant_code=" + merchant_code + "&order_amount=" + order_amount + "&order_no=" + order_no
                    + "&order_time=" + order_time + "&return_params=" + return_params + "&trade_no=" + trade_no
                    + "&trade_status=" + trade_status + "&key=" + key;
            params = MD5Encoder.encode(params, "UTF-8");
            if (!params.equals(sign)) {
                return "error";
            }

            // 验证单据号
            Map<String, Object> map = new HashMap<>();
            map.put("orderno", order_no);
            List<Map<String, String>> lm = userService.selectReChargeInfo(map);
            if (lm.size() < 1) {
                return "error";
            }
            // 获取用户信息
            Map<String, String> order = lm.get(0);
            String status = order.get("trade_status");
            // 如果单据状态不是交易中则跳过后续步骤
            if (!"交易中".equals(status)) {
                return "success";
            }

            // 获取用户信息
            Object ouid = order.get("uid");
            String uid = ouid.toString();
            Map<String, Object> param = new HashMap<>();
            param.put("uid", uid);
            map = userService.selectUserById(param);
            double wallet = Double.parseDouble(map.get("wallet").toString());
            double amt = Double.parseDouble(order_amount);

            if ("success".equals(trade_status)) {
                // 更新用户钱包余额
                map.clear();
                map.put("wallet", order_amount);
                map.put("uid", uid);
                userService.updateMoney(map);

                // 更新转账信息
                map.clear();
                map.put("orderno", order_no);
                map.put("tradestatus", trade_status);
                map.put("tradeno", trade_no);
                userService.UpdateRechange(map);

                // 插入流水记录
                map.clear();
                map.put("uid", uid);
                map.put("amount", amt);
                map.put("oldmoney", wallet);
                map.put("newmoney", wallet + amt);
                map.put("number", order_no);
                map.put("type", "IN");
                map.put("ttype", "存款");
                userService.insertUserTreasure(map);
            } else if ("failed".equals(trade_status)) {
                // 更新转账信息
                map.clear();
                map.put("orderno", order_no);
                map.put("tradestatus", trade_status);
                map.put("tradeno", trade_no);
                userService.UpdateRechange(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }

        return "success";
    }


}
