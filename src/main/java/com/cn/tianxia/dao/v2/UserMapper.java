package com.cn.tianxia.dao.v2;

import com.cn.tianxia.entity.v2.UserEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.context.annotation.Scope;

import java.util.List;
import java.util.Map;

public interface UserMapper {
    int deleteByPrimaryKey(Integer uid);

    int insert(UserEntity record);

    int insertSelective(UserEntity record);

    UserEntity selectByPrimaryKey(Integer uid);

    int updateByPrimaryKeySelective(UserEntity record);

    int updateByPrimaryKey(UserEntity record);

    double selectAgentRechargeQuotaByUid(@Param("uid") String uid);
    
    /**
     * 
     * @Description 根据用户登录账号查询用户信息 
     * @param username
     * @param flag
     * @param cagent
     * @return
     */
    UserEntity getUserByUsername(@Param("username") String username,@Param("flag") int flag,@Param("cagent") String cagent);
    
    /**
     *
     * @Description 通过登录账号查询用户信息
     * @param username
     * @return
     */
    UserEntity selectByUsername(@Param("username") String username);

    /**
     *
     * @Description 验证用户名是否在游离表中
     * @param userName
     * @return
     */
    UserEntity selectDisUserByUserName(@Param("userName") String userName);

    /**
     *
     * @Description 验证手机号是否被注册
     */
    UserEntity selectUserByMobile(@Param("cagent") String cagent,@Param("mobile") String mobile);

    List<Map<String,String>> selectProxyByCagent(@Param("cagent") String cagent);

    /**
     * 根据推荐码查询代理用户
     * @param referralCode
     * @return
     */
    Map<String,String> getProxyUserByrefererCode(@Param("referralCode") String referralCode);

    /**
     * 根据推荐码查询二级代理用户
     * @param referralCode
     * @return
     */
    Map<String,String> getJuniorProxyUserByrefererCode(@Param("referralCode") String referralCode);

    /**
     * 根据代理账号用户名和平台号查询代理商用户
     * @param proxyname 一级代理账号用户名
     * @param cagent 平台号
     * @return
     */
    Map<String, String> getProxyUser(@Param("proxyname") String proxyname, @Param("cagent") String cagent);

    /**
     * 根据代理账户用户名和平台号查询二级代理用户
     * @param proxyname 二级代理账号用户名
     * @param cagent 平台号
     * @return
     */
    Map<String, String> getJuniorProxyUser(@Param("proxyname") String proxyname, @Param("cagent") String cagent);

}