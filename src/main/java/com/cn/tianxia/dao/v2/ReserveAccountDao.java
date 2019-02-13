package com.cn.tianxia.dao.v2;


import com.cn.tianxia.entity.v2.ReserveAccountEntity;
import org.apache.ibatis.annotations.Param;


public interface ReserveAccountDao {
    int deleteByPrimaryKey(Integer id);

    int insert(ReserveAccountEntity record);

    int insertSelective(ReserveAccountEntity record);

    ReserveAccountEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ReserveAccountEntity record);

    int updateByPrimaryKey(ReserveAccountEntity record);

    ReserveAccountEntity selectReserveAccount(@Param("userName") String userName, @Param("cagent") String cagent);
}