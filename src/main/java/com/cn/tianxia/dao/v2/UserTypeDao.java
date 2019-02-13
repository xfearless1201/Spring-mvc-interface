package com.cn.tianxia.dao.v2;

import org.apache.ibatis.annotations.Param;

import com.cn.tianxia.entity.v2.UserTypeEntity;

public interface UserTypeDao {
    int deleteByPrimaryKey(Integer id);

    int insert(UserTypeEntity record);

    int insertSelective(UserTypeEntity record);

    UserTypeEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(UserTypeEntity record);

    int updateByPrimaryKey(UserTypeEntity record);

    String getPaychannelByUser(@Param("uid") String uId);
    
    UserTypeEntity getOfflineQrCodeByUser(@Param("uid") String uid);
    
    /**
     * 
     * @Description 获取会员的分层ID
     * @param cid
     * @return
     */
    Integer getUserTypeId(@Param("cagent") String cagent);
}