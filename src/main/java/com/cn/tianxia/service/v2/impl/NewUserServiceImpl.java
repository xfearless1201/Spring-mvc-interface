package com.cn.tianxia.service.v2.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cn.tianxia.dao.v2.NewUserDao;
import com.cn.tianxia.dao.v2.UserWalletDao;
import com.cn.tianxia.entity.v2.UserEntity;
import com.cn.tianxia.po.BaseResponse;
import com.cn.tianxia.service.v2.NewUserService;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName NewUserServiceImpl
 * @Description 重构用户接口实现类
 * @author Hardy
 * @Date 2019年2月7日 下午4:29:33
 * @version 1.0.0
 */
@Service
public class NewUserServiceImpl implements NewUserService {
    
    //日志
    private static final Logger logger = LoggerFactory.getLogger(NewUserServiceImpl.class);

    @Autowired
    private NewUserDao newUserDao;
    
    @Autowired
    private UserWalletDao userWalletDao;
    
    /**
     * 获取用户详情
     */
    @Override
    public JSONObject getUserInfo(String uid) {
        logger.info("调用获取用户详情业务开始==================START=================");
        JSONObject data = new JSONObject();
        try {
            
            //通过用户ID查询用户详情
            UserEntity user = newUserDao.selectByPrimaryKey(Integer.parseInt(uid));
            if(user == null){
                return BaseResponse.error("0", "非法用户ID,查询用户信息失败");
            }
            
            //查询用户积分余额
            Double integralBalance = userWalletDao.getIntegralBalance(Integer.parseInt(uid));
            if(integralBalance == null){
                integralBalance = 0.00D;
            }
            data.put("username",user.getUsername());
            data.put("realname",user.getRealname());
            data.put("email",user.getEmail());
            data.put("vip_level",user.getVipLevel());
            data.put("mobile",user.getMobile());
            data.put("reg_date",user.getRegDate());
//            data.put("login_time",DatePatternUtils.dateToStr(user.getLoginTime(), DatePatternConstant.NORM_DATETIME_PATTERN));
            data.put("login_time", user.getLoginTime());
            data.put("wallet",user.getWallet());
            data.put("integral",integralBalance);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("调用获取用户详情业务异常:{}",e.getMessage());
        }
        return data;
    }

}
