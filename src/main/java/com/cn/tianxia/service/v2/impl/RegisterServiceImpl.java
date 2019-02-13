package com.cn.tianxia.service.v2.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.cn.tianxia.common.v2.DatePatternConstant;
import com.cn.tianxia.common.v2.DatePatternUtils;
import com.cn.tianxia.common.v2.KeyConstant;
import com.cn.tianxia.common.v2.PatternUtils;
import com.cn.tianxia.common.v2.SystemConfigLoader;
import com.cn.tianxia.dao.v2.CagentDao;
import com.cn.tianxia.dao.v2.CagentMsgDao;
import com.cn.tianxia.dao.v2.RefererUrlDao;
import com.cn.tianxia.dao.v2.ReserveAccountDao;
import com.cn.tianxia.dao.v2.UserLoginDao;
import com.cn.tianxia.dao.v2.UserMapper;
import com.cn.tianxia.dao.v2.UserQuantityDao;
import com.cn.tianxia.dao.v2.UserTypeDao;
import com.cn.tianxia.dao.v2.UserWalletDao;
import com.cn.tianxia.entity.v2.CagentEntity;
import com.cn.tianxia.entity.v2.CagentMsgEntity;
import com.cn.tianxia.entity.v2.ReserveAccountEntity;
import com.cn.tianxia.entity.v2.UserEntity;
import com.cn.tianxia.entity.v2.UserLoginEntity;
import com.cn.tianxia.entity.v2.UserQuantityEntity;
import com.cn.tianxia.entity.v2.UserWalletEntity;
import com.cn.tianxia.po.BaseResponse;
import com.cn.tianxia.po.v2.RegisterResponse;
import com.cn.tianxia.service.v2.RegisterService;
import com.cn.tianxia.util.DESEncrypt;
import com.cn.tianxia.vo.v2.RegisterVO;

import net.sf.json.JSONObject;

/**
 * @Auther: zed
 * @Date: 2019/1/23 20:01
 * @Description: 注册服务实现
 */
@Service
public class RegisterServiceImpl implements RegisterService {

    private static Logger logger = LoggerFactory.getLogger(RegisterServiceImpl.class);

    @Autowired
    private SystemConfigLoader systemConfigLoader;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ReserveAccountDao reserveAccountDao;
    @Autowired
    private RefererUrlDao refererUrlDao;
    @Autowired
    private UserLoginDao userLoginDao;
    @Autowired
    private UserWalletDao userWalletDao;
    @Autowired
    private UserQuantityDao userQuantityDao;
    @Autowired
    private CagentDao cagentDao;
    @Autowired
    private CagentMsgDao cagentMsgDao;

    @Autowired
    private UserTypeDao userTypeDao;


    @Override
    public JSONObject verifyAccount(String cagent, String userName) {

        if (StringUtils.isBlank(cagent)) {
            return BaseResponse.error(BaseResponse.ERROR_CODE, "error");
        }

        String systemAgent = systemConfigLoader.getProperty("cagent");
        if (StringUtils.isNotBlank(systemAgent)) {
            cagent = systemAgent;
        }

        if (!cagent.matches(PatternUtils.CAGENTREGEX)) {
            return BaseResponse.error(BaseResponse.ERROR_CODE, "error");
        }

        if (StringUtils.isBlank(userName) || !userName.matches(PatternUtils.USERNAMEREGEX)) {
            return BaseResponse.error(BaseResponse.ERROR_CODE, "001");
        }

        // 检测用户是否存在
        UserEntity userEntity = userMapper.selectByUsername(cagent + userName);
        if (null != userEntity) {
            return BaseResponse.error(BaseResponse.ERROR_CODE, "009");
        }
        // 检测是否为系统保留账户
        ReserveAccountEntity reserveAccountEntity = reserveAccountDao.selectReserveAccount(userName, cagent);
        if (null != reserveAccountEntity) {
            return BaseResponse.error(BaseResponse.ERROR_CODE, "009");
        }
        return BaseResponse.success("000");
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public JSONObject register(RegisterVO registerVO) {
        logger.info("------------用户注册服务开始---------------------");
        try {
            String cagent = registerVO.getCagent();
            String userName = registerVO.getUserName();
            String refererUrl = registerVO.getRefererUrl();
            registerVO.setRefererUrl(refererUrl.split("/")[2]);
            refererUrl = registerVO.getRefererUrl();

            String systemAgent = systemConfigLoader.getProperty("cagent");  //配置文件中获取平台号
            if (StringUtils.isNotBlank(systemAgent)) {
                cagent = systemAgent;
                registerVO.setCagent(systemAgent);
            }
            JSONObject verifyParam = verifyParams(registerVO);  //验证参数
            if (null != verifyParam) {
                return verifyParam;
            }
            String isMobile = registerVO.getIsMobile();

            //构造新用户实体类
            UserEntity newUser = sealUserEntity(registerVO);

            //生成agpwd
            String agpwd = cagent + RandomUtils.nextInt(100000, 999999);
            newUser.setAgPassword(agpwd);
            newUser.setRegurl(refererUrl);

            String remark = registerVO.getRemark();
            if (StringUtils.isBlank(remark)) {
                newUser.setRmk("");
            } else {
                if (PatternUtils.isMatch(remark, PatternUtils.COMMONREGEX)) {
                    return BaseResponse.error(BaseResponse.ERROR_CODE, "请输入合法字符");
                }
                String referralCode = registerVO.getReferralCode();
                if (StringUtils.isNotBlank(referralCode)) {
                    newUser.setRmk(remark + "\n 推介码=" + referralCode);
                } else {
                    newUser.setRmk(remark);
                }
            }
            newUser.setLoginmobile(registerVO.getMobileNo());
            newUser.setTopUid(0);
            newUser.setJuniorUid(0);
            //通过平台编码查询用户的分层ID
            Integer typeId = userTypeDao.getUserTypeId(cagent);
            if (typeId == null) {
                typeId = 0;
            }
            // 根据来源域名更新代理商
            String proxyname = registerVO.getProxyname();
            if (StringUtils.isBlank(proxyname)) {

                proxyname = "";
                String domain = refererUrl;// cdsr.com

                logger.info("来源域名更新代理商_first：" + userName + "--------" + "代理商:" + cagent + "-------" + "域名" + domain);

                if (domain.indexOf(":") > 0) {   //去掉":"后面的":"和端口号
                    domain = domain.substring(0, domain.indexOf(":"));
                }

                logger.info("来源域名更新代理商_second：" + userName + "--------" + "代理商:" + cagent + "-------" + "域名" + domain);

                if (!domain.matches(PatternUtils.IPREGEX) && !"localhost".equals(domain)) {

                    Pattern p = Pattern.compile(PatternUtils.DOMAINREGEX);
                    Matcher m = p.matcher(domain);

                    List<String> strList = new ArrayList<>();
                    while (m.find()) {
                        strList.add(m.group());
                    }
                    domain = strList.toString();
                    domain = domain.substring(1, domain.length() - 1);

                }

                logger.info("来源域名更新代理商_third：" + userName + "--------" + "代理商:" + cagent + "-------" + "域名" + domain);

                //根据平台号查出所有代理商账号域名
                List<Map<String, String>> ProxyList = userMapper.selectProxyByCagent(registerVO.getCagent());

                if (CollectionUtils.isNotEmpty(ProxyList)) {

                    loop:
                    for (Map<String, String> proxy : ProxyList) {  //遍历代理商账户对应域名列表

                        if (null != proxy && proxy.containsKey("domain") && StringUtils.isNotBlank(proxy.get("domain"))) {
                            // 域名不为空 获取用分号";"分隔的域名字符串
                            String[] domains = proxy.get("domain").split(";");

                            for (String proxyDomain : domains) {
                                //遍历域名字符串，如果来源域名和某个代理商账户域名字符串数组匹配上，跳出外层循环
                                if (domain.equals(proxyDomain)) {
                                    proxyname = proxy.get("user_name");
                                    logger.info("查询代理名称,proxyname值:{}", proxyname);
                                    break loop;
                                }
                            }
                        }
                    }
                }
            }

            // 根据推荐账号,更新代理商
            if (StringUtils.isNotBlank(proxyname)) {
                if (PatternUtils.isMatch(proxyname, PatternUtils.COMMONREGEX)) {
                    return BaseResponse.error(BaseResponse.ERROR_CODE, "请输入合法的代理商账号");
                }

                Map<String, String> proxyUser = userMapper.getProxyUser(proxyname, registerVO.getCagent());
                if (MapUtils.isNotEmpty(proxyUser)) {
                    //代理商用户id
                    newUser.setTopUid(Integer.parseInt(proxyUser.get("pid")));
                    if (!"0".equals(proxyUser.get("dUserType"))) {
                        typeId = Integer.parseInt(proxyUser.get("dUserType"));
                    }
                    newUser.setJuniorUid(0);

                } else {

                    proxyUser = userMapper.getJuniorProxyUser(proxyname, registerVO.getCagent());

                    if (MapUtils.isNotEmpty(proxyUser)) {

                        newUser.setTopUid(Integer.valueOf(proxyUser.get("upId")));
                        if (!"0".equals(proxyUser.get("dUserType"))) {
                            typeId = Integer.valueOf(proxyUser.get("dUserType"));
                        }
                        newUser.setJuniorUid(Integer.valueOf(proxyUser.get("pid")));

                    }
                }
            }

            //根据推介码来查询
            String referralCode = registerVO.getReferralCode();
            if (StringUtils.isNotBlank(referralCode)) {

                int count = StringUtils.countMatches(referralCode.toUpperCase(), "A");
                //一级代理商
                if (count == 2) {

                    Map<String, String> proxyUser = userMapper.getProxyUserByrefererCode(referralCode);

                    if (MapUtils.isNotEmpty(proxyUser)) {

                        newUser.setTopUid(Integer.valueOf(proxyUser.get("pid")));
                        if (!"0".equals(proxyUser.get("dUserType"))) {
                            typeId = Integer.valueOf(proxyUser.get("dUserType"));
                        }
                        newUser.setJuniorUid(0);

                    }
                    //二级代理商
                } else if (count == 3) {

                    Map<String, String> proxyUser = userMapper.getJuniorProxyUserByrefererCode(referralCode);

                    if (MapUtils.isNotEmpty(proxyUser)) {

                        newUser.setTopUid(Integer.valueOf(proxyUser.get("upId")));
                        if (!"0".equals(proxyUser.get("dUserType"))) {
                            typeId = Integer.valueOf(proxyUser.get("dUserType"));
                        }
                        newUser.setJuniorUid(Integer.valueOf(proxyUser.get("pid")));

                    }
                }
            }

            //设置用户的分层ID
            newUser.setTypeId(typeId);

            DESEncrypt d = new DESEncrypt(KeyConstant.DESKEY);

            //密码加密
            newUser.setPassword(d.encrypt(newUser.getPassword()));
            if (StringUtils.isNotBlank(newUser.getQkPwd())) {
                newUser.setQkPwd(d.encrypt(newUser.getQkPwd()));
            }
            if (StringUtils.isNotBlank(newUser.getAgPassword())) {
                newUser.setAgPassword(d.encrypt(newUser.getAgPassword()));
            }

            //插入用户表
            userMapper.insertSelective(newUser);

            CagentEntity cagentEntity = cagentDao.selectByCagent(cagent);

            UserEntity userEntity = userMapper.selectByUsername(registerVO.getCagent() + registerVO.getUserName());
            if (userEntity != null) {
                //插入用户打码量表\
                UserQuantityEntity userQuantityEntity = new UserQuantityEntity();
                userQuantityEntity.setCid(cagentEntity.getId());
                userQuantityEntity.setUid(userEntity.getUid());
                userQuantityEntity.setMarkingQuantity(0.0);
                userQuantityEntity.setUserQuantity(0.0);
                userQuantityEntity.setUserQuantityHistory(0.0);
                userQuantityEntity.setUserWinamount(0.0);
                userQuantityEntity.setWinamount(0.0);

                userQuantityDao.insertSelective(userQuantityEntity);

                //插入用户钱包表
                UserWalletEntity userWalletEntity = new UserWalletEntity();
                userWalletEntity.setUid(userEntity.getUid());
                userWalletEntity.setType("1");
                userWalletEntity.setBalance(0.0);
                userWalletEntity.setFrozenBalance(0.0);
                userWalletEntity.setUptime(new Date());

                userWalletDao.insertSelective(userWalletEntity);

                String address = registerVO.getAddress();
                // 登录日志
                UserLoginEntity userLoginEntity = new UserLoginEntity();
                userLoginEntity.setUid(userEntity.getUid());
                userLoginEntity.setLoginIp(registerVO.getLoginIp());
                userLoginEntity.setIsMobile(isMobile);
                userLoginEntity.setIsLogin((byte) 1);
                userLoginEntity.setLoginNum(1);
                userLoginEntity.setStatus("1");
                userLoginEntity.setRefurl(refererUrl);
                userLoginEntity.setAddress(address);
                userLoginEntity.setLoginTime(new Date());

                userLoginDao.insertSelective(userLoginEntity);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("uid", userEntity.getUid());
                jsonObject.put("username", userEntity.getUsername());
                jsonObject.put("realname", userEntity.getRealname());
                jsonObject.put("ag_username", userEntity.getAgUsername());
                jsonObject.put("hg_username", userEntity.getHgUsername());
                jsonObject.put("ag_password", userEntity.getAgPassword());

                jsonObject.put("loginmobile", userEntity.getLoginmobile());
                jsonObject.put("cagent", userEntity.getCagent());
                jsonObject.put("balance", userEntity.getWallet());
                jsonObject.put("isMobile", isMobile);

                Double integralBalance = userWalletDao.getIntegralBalance(userEntity.getUid());
                if (integralBalance == null) {
                    integralBalance = 0.00D;
                }
                jsonObject.put("integral", integralBalance);

                jsonObject.put("cid", cagentEntity.getId());
                jsonObject.put("typeid", userEntity.getTypeId());//分层ID
                jsonObject.put("login_time", DatePatternUtils.dateToStr(userEntity.getLoginTime(), DatePatternConstant.NORM_DATETIME_MINUTE_PATTERN));

                return RegisterResponse.success(jsonObject);
            }
            return RegisterResponse.error(RegisterResponse.ERROR_CODE, "用户注册异常：插入用户失败，查询返回为空");

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("用户注册异常：" + e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return BaseResponse.error(BaseResponse.ERROR_CODE, "用户注册异常：" + e.getMessage());
        }

    }

    /**
     * 验证参数
     *
     * @param registerVO
     * @return
     */
    private JSONObject verifyParams(RegisterVO registerVO) {
        String isMobile = registerVO.getIsMobile();  //isMobile未传，设置为0
        if (StringUtils.isBlank(isMobile)) {
            registerVO.setIsMobile("0");
            isMobile = "0";
        }
        //推荐码
        String referralCode = registerVO.getReferralCode();
        if (StringUtils.isNotBlank(referralCode) && referralCode.matches(PatternUtils.COMMONREGEX)) {
            return BaseResponse.error(BaseResponse.ERROR_CODE, "请输入合法推荐码");
        }
        //代理平台
        String cagent = registerVO.getCagent();
        if (StringUtils.isBlank(cagent)) {
            logger.error("注册用户错误:代理平台号不能为空！");
            return BaseResponse.error(BaseResponse.ERROR_CODE, "error");
        }
        // 验证来源域名是否属于该代理平台
        String refererUrl = registerVO.getRefererUrl();
        if (StringUtils.isBlank(refererUrl)) {
            logger.error("注册用户错误:来源域名为空！");
        }
        boolean isWhiteDomain = false;
        List<String> domainList = refererUrlDao.findAllByCagent(cagent);
        if(!CollectionUtils.isEmpty(domainList)){
            String refurls = domainList.toString();
            if(refurls.indexOf(refererUrl) > 0){
                isWhiteDomain = true;
            }
        }
        if (!isWhiteDomain) {
            logger.error("注册用户错误:来源域名与代理平台不匹配！");
            return BaseResponse.error(BaseResponse.ERROR_CODE, "域名不匹配");
        }

        //手机号
        String mobileNo = registerVO.getMobileNo();
        if (StringUtils.isBlank(mobileNo)) {
            return isMobile.equals("0") ? BaseResponse.error(BaseResponse.ERROR_CODE, "003") : RegisterResponse.error(RegisterResponse.ERROR_CODE, "手机号不能为空");
        }
        if (!mobileNo.matches(PatternUtils.PHONENOREGEX)) {
            return isMobile.equals("0") ? BaseResponse.error(BaseResponse.ERROR_CODE, "018") : RegisterResponse.error(RegisterResponse.ERROR_CODE, "手机号格式不正确");
        }

        //用户名
        String userName = registerVO.getUserName();
        if (StringUtils.isNotBlank(userName) && !userName.matches(PatternUtils.USERNAMEREGEX)) {
            return isMobile.equals("0") ? BaseResponse.error(BaseResponse.ERROR_CODE, "002") : RegisterResponse.error(RegisterResponse.ERROR_CODE, "用户名格式不正确");
        } else if (StringUtils.isBlank(userName)) {
            registerVO.setUserName(mobileNo);
            userName = mobileNo;
        }
        //真实姓名
        String realName = registerVO.getRealName();
        if (StringUtils.isNotBlank(realName) && !realName.matches(PatternUtils.REALNAMEREGEX)) {
            return BaseResponse.error(BaseResponse.ERROR_CODE, "真实姓名不合法");
        } else if (StringUtils.isBlank(realName)) {
            registerVO.setRealName("会员");
        }
        if (isMobile.equals("0")) {
            //登录密码
            String passWord = registerVO.getPassWord();
            if (StringUtils.isBlank(passWord)) {
                return BaseResponse.error(BaseResponse.ERROR_CODE, "005");
            }
            //确认登录密码
            String rePassWord = registerVO.getRepassWord();
            if (StringUtils.isBlank(rePassWord)) {
                return BaseResponse.error(BaseResponse.ERROR_CODE, "006");
            }
            //登录密码和确认登录密码不同
            if (!passWord.equals(rePassWord)) {
                return BaseResponse.error(BaseResponse.ERROR_CODE, "007");
            }
            //登录密码长度小于5或大于20
            if (passWord.length() < 5 || passWord.length() > 20) {
                return BaseResponse.error(BaseResponse.ERROR_CODE, "008");
            }
            //取款密码
            String qkpwd = registerVO.getQkpwd();
            String reqkpwd = registerVO.getReqkpwd();
            if (StringUtils.isNotBlank(qkpwd)) {
                if (StringUtils.isBlank(reqkpwd)) {
                    return BaseResponse.error(BaseResponse.ERROR_CODE, "014");
                }
                if (!qkpwd.equals(reqkpwd)) {
                    return BaseResponse.error(BaseResponse.ERROR_CODE, "015");
                }
                if (qkpwd.length() < 4) {
                    return BaseResponse.error(BaseResponse.ERROR_CODE, "016");
                }

                if (qkpwd.equals(passWord)) {
                    return BaseResponse.error(BaseResponse.ERROR_CODE, "017");
                }
            } else {
                registerVO.setQkpwd("");  //如果取款密码为空，设置取款密码为空
            }
        } else {
            String passWord = registerVO.getPassWord();  //手机端如果密码为空，默认123456
            if (StringUtils.isBlank(passWord)) {
//                registerVO.setPassWord(cagent + RandomUtils.nextInt(100000,999999));
                registerVO.setPassWord("123456");
            }
            registerVO.setQkpwd("");
        }
        //验证用户名是否已存在
        // 检测用户是否存在
        UserEntity userEntity = userMapper.selectByUsername(cagent + userName);
        if (null != userEntity) {
            return BaseResponse.error(BaseResponse.ERROR_CODE, "009");
        }

        // 检测游离表的用户是否存在
        UserEntity disUserEntity = userMapper.selectDisUserByUserName(cagent + userName);
        if (null != disUserEntity) {
            return BaseResponse.error(BaseResponse.ERROR_CODE, "009");
        }

        // 检测用户是否为系统保留账户
        ReserveAccountEntity reserveAccountEntity = reserveAccountDao.selectReserveAccount(userName, cagent);
        if (null != reserveAccountEntity) {
            return BaseResponse.error(BaseResponse.ERROR_CODE, "009");
        }

        // 检测手机号是否已绑定
        UserEntity userEntity1 = userMapper.selectUserByMobile(cagent, mobileNo);
        if (null != userEntity1) {
            return BaseResponse.error(BaseResponse.ERROR_CODE, "019");
        }
        //验证码
        if ("0".equals(isMobile)) {   //pc 端验证图形验证码
            String isImgCode = registerVO.getIsImgCode();
            String imgcode = registerVO.getImgcode();
            if (!"0".equals(isImgCode)) {   //需要验证码
                // 图形验证码为空
                if (StringUtils.isBlank(imgcode)) {
                    return BaseResponse.error(BaseResponse.ERROR_CODE, "011");
                }
                String simgcode = registerVO.getSimgcode();
                logger.info(registerVO.getLoginIp() + "---register获取验证码" + "-------" + simgcode);
                if (StringUtils.isBlank(simgcode)) {
                    return BaseResponse.error(BaseResponse.ERROR_CODE, "011");
                }
                simgcode = simgcode.toLowerCase();
                imgcode = imgcode.toLowerCase();
                if (!simgcode.equals(imgcode)) { // 忽略验证码大小写
                    return BaseResponse.error(BaseResponse.ERROR_CODE, "012");
                }
            }
        } else {    //手机端验证短信验证码
            String msgCode = registerVO.getMsgCode();
            if (StringUtils.isBlank(msgCode)) {
                return RegisterResponse.error(RegisterResponse.ERROR_CODE, "短信验证码为空，请输入短信验证码");
            }
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MINUTE, -5);
            Date now = cal.getTime();
            String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now);
            // 验证短信验证码
            CagentMsgEntity msgEntity = cagentMsgDao.selectMsgLog(cagent, mobileNo, "1", nowTime);
            if (null == msgEntity) {
                return RegisterResponse.error(RegisterResponse.ERROR_CODE, "短信验证码失效,请重新发送");
            } else {
                if (msgCode.equals(msgEntity.getMsg())) {
                    msgEntity.setStatus("1");
                    cagentMsgDao.updateByPrimaryKey(msgEntity);
                } else {
                    return RegisterResponse.error(RegisterResponse.ERROR_CODE, "验证码错误");
                }
            }
        }

        return null;
    }

    private UserEntity sealUserEntity(RegisterVO registerVO) {
        UserEntity entity = new UserEntity();

        String userName = registerVO.getCagent().toLowerCase() + registerVO.getUserName().toLowerCase();
        entity.setUsername(userName);
        entity.setPassword(registerVO.getPassWord());
        entity.setRegIp(registerVO.getLoginIp());
        entity.setLoginIp(registerVO.getLoginIp());
        entity.setHgUsername(userName);
        entity.setAgUsername(userName);
        entity.setEmail("");
        entity.setVipLevel("1");
        entity.setMobile(registerVO.getMobileNo());
        entity.setIsDaili("0");
        entity.setTopUid(0);
        entity.setIsMobile(registerVO.getIsMobile());
        entity.setCagent(registerVO.getCagent());
        entity.setQkPwd(registerVO.getQkpwd());
        entity.setRealname(registerVO.getRealName());
        entity.setAgPassword(registerVO.getAgpassword());

        return entity;
    }
}
