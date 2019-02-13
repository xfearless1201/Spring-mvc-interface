package com.cn.tianxia.service.v2.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.CollectionUtils;

import com.cn.tianxia.common.v2.DatePatternConstant;
import com.cn.tianxia.common.v2.DatePatternUtils;
import com.cn.tianxia.common.v2.KeyConstant;
import com.cn.tianxia.common.v2.PatternUtils;
import com.cn.tianxia.dao.v2.CagentDao;
import com.cn.tianxia.dao.v2.DissociateDao;
import com.cn.tianxia.dao.v2.LoginerrormapDao;
import com.cn.tianxia.dao.v2.NewUserDao;
import com.cn.tianxia.dao.v2.PlatformConfigDao;
import com.cn.tianxia.dao.v2.RefererUrlDao;
import com.cn.tianxia.dao.v2.UserLoginDao;
import com.cn.tianxia.dao.v2.UserWalletDao;
import com.cn.tianxia.entity.v2.CagentEntity;
import com.cn.tianxia.entity.v2.DissociateEntity;
import com.cn.tianxia.entity.v2.LoginerrormapEntity;
import com.cn.tianxia.entity.v2.PlatformConfigEntity;
import com.cn.tianxia.entity.v2.UserEntity;
import com.cn.tianxia.entity.v2.UserLoginEntity;
import com.cn.tianxia.po.BaseResponse;
import com.cn.tianxia.po.v2.LoginResponse;
import com.cn.tianxia.service.v2.UserLoginService;
import com.cn.tianxia.util.DESEncrypt;
import com.cn.tianxia.vo.v2.UserLoginVO;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName UserLoginServiceImpl
 * @Description 用户登录接口实现类
 * @author Hardy
 * @Date 2019年2月6日 下午2:59:23
 * @version 1.0.0
 */
@Service
public class UserLoginServiceImpl implements UserLoginService {
    
    //日志
    private static final Logger logger = LoggerFactory.getLogger(UserLoginServiceImpl.class);
    
    @Autowired
    private RefererUrlDao refererUrlDao;
    
    @Autowired
    private DissociateDao dissociateDao;
    
    @Autowired
    private NewUserDao newUserDao;
    
    @Autowired
    private LoginerrormapDao loginerrormapDao;
    
    @Autowired
    private UserLoginDao userLoginDao;
    
    @Autowired
    private PlatformConfigDao platformConfigDao;
    
    @Autowired
    private UserWalletDao userWalletDao;
    
    @Autowired
    private CagentDao cagentDao;
    
    /**
     * 登录
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED,rollbackFor=Exception.class)
    public JSONObject login(UserLoginVO userLoginVO) {
        logger.info("调用用户登录业务开始==================START==================");
        try {
            //验证来源域名是否属于代该代理平台
            boolean isDomainWhite = false;//默认为false
            //登录账号
            String username = userLoginVO.getUsername();
            //原始登录账号
            String account = username;
            //获取平台编码
            String cagent = username.substring(0, 3);
            //查询白名单域名列表
            String referUrl = userLoginVO.getRefurl().split("/")[2];
            List<String> domains = refererUrlDao.findAllByCagent(cagent);
            if(!CollectionUtils.isEmpty(domains)){
                String refurls = domains.toString();
                if(refurls.indexOf(referUrl) > 0){
                    isDomainWhite = true;
                }
            }
            if(!isDomainWhite){
                return BaseResponse.faild("0", "域名不匹配");
            }
            
            //处理不活跃的用户
            int isPhoneNo = 0;//是否为电话号码 0 不是 1 是
            String mobileUsername = username.substring(3, username.length());//手机号码
            if(PatternUtils.isMatch(mobileUsername, PatternUtils.PHONENOREGEX)){
                isPhoneNo = 1;//手机号码
                username = mobileUsername;
            }
            
            //查询用户信息
            UserEntity user = newUserDao.getUserInfoByUsername(username, cagent,isPhoneNo);
            if(user == null){
                //查询用户失败,再去游离表确认
                DissociateEntity dissociateEntity = dissociateDao.getDissociateInfoByUsername(username, cagent, isPhoneNo);
                if(dissociateEntity == null){
                    logger.info("用户登录名不正确--->>>tname:{}",userLoginVO.getUsername());
                    return LoginResponse.faild("0", "输入用户登录账号不正确");
                }else{
                    //把用户信息从游离表中写入到用户表
                    user = new UserEntity();
                    BeanUtils.copyProperties(dissociateEntity, user);
                    user.setIsStop("0");
                    user.setIsMobile(userLoginVO.getIsMobile());
                    user.setLoginIp(userLoginVO.getIp());
                    user.setUsername(account.toLowerCase());
                    user.setLoginTime(new Date());
                    newUserDao.insertSelective(user);
                    //删除游离表信息
                    dissociateDao.deleteByPrimaryKey(dissociateEntity.getUid());
                }
            }
            
            //查询用户登录错误日志
            LoginerrormapEntity loginerrormapEntity = loginerrormapDao.findAllByUsername(account);
            if(loginerrormapEntity != null){
                //获取错误次数
                Integer errorTimes = loginerrormapEntity.getTimes();
                //获取最后一次登录时间
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(loginerrormapEntity.getLogintime());
                long loginTimes = calendar.getTimeInMillis();//最后登录的时间毫秒数
                //当前系统的毫秒数
                long nowTimes = System.currentTimeMillis();
                if(nowTimes - loginTimes > 60*60*24*1000){
                    //大于一天,清零所有的登录错误次数
                    loginerrormapEntity.setTimes(0);
                    loginerrormapEntity.setLogintime(new Date());
                    loginerrormapDao.updateByPrimaryKey(loginerrormapEntity);
                }else{
                    //小于一天
                    if(nowTimes - loginTimes < 300*1000 && errorTimes >= 5){
                        //小于五分钟,并且登录错误次数大于5
                        return LoginResponse.faild("0", "登录失败：【密码错误次数过多,账号已锁定5分钟】");
                    }
                    
                    if(errorTimes >= 10){
                        return LoginResponse.faild("0", "登录失败：【密码错误次数过多,账号已锁定一天】");
                    }
                }
            }
            
            //判断用户密码
            DESEncrypt desEncrypt = new DESEncrypt(KeyConstant.DESKEY);
            String password = desEncrypt.encrypt(userLoginVO.getPassword());
            if(!password.equals(user.getPassword())){
                logger.info("登录失败:【用户输入的登录密码不正确】");
                //写入一条错误日志
                if(loginerrormapEntity != null){
                    //更新错误次数
                    int newErrorTimes = loginerrormapEntity.getTimes()+1;
                    loginerrormapEntity.setTimes(newErrorTimes);
                    loginerrormapEntity.setLogintime(new Date());
                    //更新
                    loginerrormapDao.updateByPrimaryKeySelective(loginerrormapEntity);
                }else{
                    //写入一条
                    loginerrormapEntity = new LoginerrormapEntity();
                    loginerrormapEntity.setLogintime(new Date());
                    loginerrormapEntity.setTimes(1);
                    loginerrormapEntity.setUsername(account);
                    loginerrormapDao.insertSelective(loginerrormapEntity);
                }
                
                return LoginResponse.faild("0", "登录失败:【输入登录密码错误】");
            }
            
            if("1".equals(user.getIsStop())){
                return LoginResponse.faild("0", "账户已被锁定,请联系客服");
            }
            
            //写入一条登录日志
            UserLoginEntity userLoginEntity = new UserLoginEntity();
            userLoginEntity.setUid(user.getUid());
            userLoginEntity.setIsMobile(userLoginVO.getIsMobile());
            userLoginEntity.setLoginIp(userLoginVO.getIp());
            userLoginEntity.setRefurl(userLoginVO.getRefurl());
            userLoginEntity.setAddress(userLoginVO.getAddress());
            userLoginEntity.setLoginTime(new Date());
            userLoginEntity.setIsLogin((byte)1);
            userLoginEntity.setLoginNum(1);
            userLoginEntity.setStatus("1");
            //写入登录日志
            userLoginDao.insertSelective(userLoginEntity);
            
            //查询需要存入缓存中的数据
            //1.查询所有平台的游戏配置信息,所有状态为1的已开启的游戏配置
            List<PlatformConfigEntity> platformConfigs = platformConfigDao.findAll();
            if(CollectionUtils.isEmpty(platformConfigs)){
                platformConfigs = new ArrayList<>();
            }
            //2.查询用户积分
            Double integralBalance = userWalletDao.getIntegralBalance(user.getUid());
            if(integralBalance == null){
                integralBalance = 0.00D;
            }
            //查询用户所属平台ID
            CagentEntity cagentEntity = cagentDao.selectByCagent(user.getCagent());
            //返回json对象
            //生成token
            UUID uuid = UUID.randomUUID();
            String token = uuid.toString();
            JSONObject data = new JSONObject();
            data.put("status", "ok");
            data.put("userKey", token);
            data.put("userName",account);
            data.put("balance", user.getWallet());
            data.put("integral",integralBalance);
            
            JSONObject cacheJson = new JSONObject();
            cacheJson.put("uid", user.getUid());
            cacheJson.put("userName",account);
            cacheJson.put("ag_username",user.getAgUsername());
            cacheJson.put("hg_username", user.getHgUsername());
            cacheJson.put("ag_password", user.getAgPassword());
            cacheJson.put("userkey",token);
            cacheJson.put("loginmobile",user.getLoginmobile());
            cacheJson.put("cagent", user.getCagent());
            cacheJson.put("balance", user.getWallet());
            cacheJson.put("integral",integralBalance);
            cacheJson.put("cid", cagentEntity.getId());
            cacheJson.put("typeid", user.getTypeId());//分层ID
            cacheJson.put("login_time",DatePatternUtils.dateToStr(user.getLoginTime(), DatePatternConstant.NORM_DATETIME_MINUTE_PATTERN));
            cacheJson.put("Transfer", "0");
            cacheJson.put("WithDraw", "0");
            data.put("cacheJson", cacheJson);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("调用用户登录业务异常:{}",e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return LoginResponse.faild("0", "调用用户登录业务异常");
        }
    }
}
