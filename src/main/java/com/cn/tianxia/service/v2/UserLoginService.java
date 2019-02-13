package com.cn.tianxia.service.v2;

import com.cn.tianxia.vo.v2.UserLoginVO;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName UserLoginService
 * @Description 用户登录接口
 * @author Hardy
 * @Date 2019年2月6日 下午2:59:04
 * @version 1.0.0
 */
public interface UserLoginService {

    /**
     * 
     * @Description 登录接口
     * @param userLoginVO
     * @return
     */
    public JSONObject login(UserLoginVO userLoginVO);
}
