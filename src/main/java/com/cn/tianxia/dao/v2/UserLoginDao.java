package com.cn.tianxia.dao.v2;

import com.cn.tianxia.entity.v2.UserLoginEntity;

/**
 * 
 * @ClassName UserLoginDao
 * @Description 用户登录表dao
 * @author Hardy
 * @Date 2019年2月6日 下午5:34:21
 * @version 1.0.0
 */
public interface UserLoginDao {
    int deleteByPrimaryKey(Integer id);

    int insert(UserLoginEntity record);

    int insertSelective(UserLoginEntity record);

    UserLoginEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(UserLoginEntity record);

    int updateByPrimaryKey(UserLoginEntity record);
}