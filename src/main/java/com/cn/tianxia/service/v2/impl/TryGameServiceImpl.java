package com.cn.tianxia.service.v2.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cn.tianxia.common.v2.KeyConstant;
import com.cn.tianxia.dao.v2.DemoAccountDao;
import com.cn.tianxia.dao.v2.PlatformConfigDao;
import com.cn.tianxia.entity.v2.DemoAccountEntity;
import com.cn.tianxia.entity.v2.PlatformConfigEntity;
import com.cn.tianxia.game.impl.AGINFreeServiceImpl;
import com.cn.tianxia.game.impl.IGFunServiceImpl;
import com.cn.tianxia.po.BaseResponse;
import com.cn.tianxia.service.v2.TryGameService;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName TryGameServiceImpl
 * @Description 试玩游戏接口实现类
 * @author Hardy
 * @Date 2019年2月6日 上午11:16:24
 * @version 1.0.0
 */
@Service
public class TryGameServiceImpl implements TryGameService {
    
    //日志
    private static final Logger logger = LoggerFactory.getLogger(TryGameServiceImpl.class);
    
    @Autowired
    private DemoAccountDao demoAccountDao;
    
    @Autowired
    private PlatformConfigDao platformConfigDao;

    @Override
    public JSONObject forwardIGGame(String cagent, String gameType, String gameID, String accountCode, String model,String ip) {
        logger.info("调用获取IG游戏试玩链接业务开始==================START===================");
        try {
            boolean isnew = true;//是否为新玩家,默认为true;
            String platformConfigKey = "IGFUN";//获取游戏配置信息key
            //初次试玩默认账号和密码
            String username = cagent + System.currentTimeMillis();
            String password = KeyConstant.TRY_GAME_DESKEY;//试玩密码
            DemoAccountEntity demoAccount = new DemoAccountEntity(); 
            if(!"0".equals(accountCode)){
                //非初次试玩,获取用户账号
                demoAccount = demoAccountDao.selectByAccountcode(accountCode);
                if(demoAccount != null){
                    logger.info("IG试玩帐号已经存在-------username:" + username + "password:" + password);
                    username = demoAccount.getUsername();//试玩登录账号
                    password = demoAccount.getPassword();//试玩登录密码
                    isnew = false;
                }
            }
            
            //获取游戏配置信息
            PlatformConfigEntity platformConfig = platformConfigDao.selectByPlatformKey(platformConfigKey);
            if(platformConfig == null){
                logger.info("IG游戏正在维护中.....");
                return BaseResponse.error("0", "process");
            }
            
            Map<String, String> pmap = new HashMap<>();
            pmap.put(platformConfig.getPlatformKey(), platformConfig.getPlatformConfig());
            
            //登录IG游戏
            IGFunServiceImpl ig = new IGFunServiceImpl(pmap);
            
            //如果是新玩家就创建账号
            if(isnew){
                if(demoAccount == null){
                    demoAccount = new DemoAccountEntity();
                }
                demoAccount.setUsername(username);
                demoAccount.setPassword(password);
                demoAccount.setCagent(cagent);
                demoAccount.setAccountcode(accountCode);
                demoAccount.setAddtime(new Date());
                demoAccount.setIp(ip);
                demoAccountDao.insertSelective(demoAccount);
                String billno = cagent + System.currentTimeMillis();
                logger.info("IG试玩创建新帐号-------username:" + username + "password:" + password + "billno:" + billno);
                String res = ig.DEPOSIT(username, password, billno, "2000");
                if ("success".equals(res)) {
                    logger.info("IG试玩创建成功-------");
                } else {
                    logger.info("IG试玩创建失败-------");
                }
            }
            
            String msg = ig.LoginGame(username, password, gameType, gameID, model);
            if(StringUtils.isBlank(msg)){
                logger.info("获取IG游戏试玩链接失败:{}",msg);
                return BaseResponse.error("0", "error");
            }
            
            //解析响应结果
            JSONObject jsonObject = JSONObject.fromObject(msg);
            if ("0".equals(jsonObject.getString("errorCode"))) {
                jsonObject = jsonObject.getJSONObject("params");
                msg = jsonObject.getString("link");
                jsonObject.put("msg", msg);
                jsonObject.put("type", "link");
                return jsonObject;
            }
            logger.info("获取IG游戏试玩链接失败:{}",msg);
            return BaseResponse.error("0", "process");
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("调用获取IG游戏试玩链接业务异常:{}",e.getMessage());
            return BaseResponse.error("0", "调用获取IG游戏试玩链接业务异常");
        }
    }

    @Override
    public JSONObject forwardAGGame(String cagent, String gameType, String gameID, String accountCode, String model,String ip) {
        logger.info("调用获取AG游戏试玩链接业务开始==================START======================");
        try {
            boolean isnew = true;//是否为新玩家,默认为true;
            String platformConfigKey = "AGINFUN";//获取游戏配置信息key
            //获取游戏配置信息
            PlatformConfigEntity platformConfig = platformConfigDao.selectByPlatformKey(platformConfigKey);
            if(platformConfig == null){
                logger.info("IG游戏正在维护中.....");
                return BaseResponse.error("0", "process");
            }
            //初次试玩默认账号和密码
            String username = cagent + System.currentTimeMillis();
            String password = KeyConstant.TRY_GAME_DESKEY;//试玩密码
            DemoAccountEntity demoAccount = new DemoAccountEntity(); 
            if(!"0".equals(accountCode)){
                //非初次试玩,获取用户账号
                demoAccount = demoAccountDao.selectByAccountcode(accountCode);
                if(demoAccount != null){
                    logger.info("IG试玩帐号已经存在-------username:" + username + "password:" + password);
                    username = demoAccount.getUsername();//试玩登录账号
                    password = demoAccount.getPassword();//试玩登录密码
                    isnew = false;
                }
            }
            Map<String, String> pmap = new HashMap<>();
            pmap.put(platformConfig.getPlatformKey(), platformConfig.getPlatformConfig());
            AGINFreeServiceImpl agService = new AGINFreeServiceImpl(pmap,platformConfigKey);
            //创建用户
            if(isnew){
                if(demoAccount == null){
                    demoAccount = new DemoAccountEntity();
                }
                demoAccount.setUsername(username);
                demoAccount.setPassword(password);
                demoAccount.setCagent(cagent);
                demoAccount.setAccountcode(accountCode);
                demoAccount.setAddtime(new Date());
                demoAccount.setIp(ip);
                demoAccountDao.insertSelective(demoAccount);
                String billno = cagent + System.currentTimeMillis();
                logger.info("AG试玩创建新帐号-------username:" + username + "password:" + password + "billno:" + billno);
                // 检查并创建帐号  oddtype 表示下注金额 50-5000
                String msg = agService.CheckOrCreateGameAccout(username, password, "A", "CNY");
                if ("0".equals(msg)) {
                    logger.info("AGIN试玩创建新帐号-------username:" + username + "password:" + password );
                } else {
                    return BaseResponse.error("0", "error");
                }
            }
            
            // 获取游戏连接
            JSONObject data = new JSONObject();
            String sid = "AGIN" + System.currentTimeMillis();
            if ("mobile".equals(gameID)) {
                String url = agService.forwardMobileGame(username, password, ip, sid, "11");
                data.put("msg", url);
                data.put("type", "from");
            } else {
                String url = agService.forwardGame(username, password, ip, sid, "2");
                data.put("msg", url);
                data.put("type", "from");
            }
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("调用获取AG游戏试玩链接业务异常:{}",e.getMessage());
            return BaseResponse.error("0", "调用获取AG游戏试玩链接业务异常");
        }
    }

}
