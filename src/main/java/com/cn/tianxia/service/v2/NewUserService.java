package com.cn.tianxia.service.v2;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName NewUserService
 * @Description 重构用户接口
 * @author Hardy
 * @Date 2019年2月7日 下午4:29:07
 * @version 1.0.0
 */
public interface NewUserService {

    /**
     * 
     * @Description 获取用户详情
     * @param uid
     * @return
     */
    public JSONObject getUserInfo(String uid);
}
