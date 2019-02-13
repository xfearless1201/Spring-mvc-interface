package com.cn.tianxia.dao.v2;

import com.cn.tianxia.entity.v2.UserEntity;
import org.apache.ibatis.annotations.Param;

public interface NewUserDao {
    int deleteByPrimaryKey(Integer uid);

    int insert(UserEntity record);

    int insertSelective(UserEntity record);

    UserEntity selectByPrimaryKey(Integer uid);

    int updateByPrimaryKeySelective(UserEntity record);

    int updateByPrimaryKey(UserEntity record);

    double selectAgentRechargeQuotaByUid(@Param("uid") String uid);
    
    double queryUserBalance(@Param("uid") String uid);
    
    int subtractUserBalance(@Param("uid") Integer uid,@Param("money") Double money);
    
    int plusUserBalance(@Param("uid") Integer uid,@Param("money") Double money);
    
    UserEntity getUserInfoByUsername(@Param("username") String username,@Param("cagent") String cagent,@Param("flag") Integer flag);
}