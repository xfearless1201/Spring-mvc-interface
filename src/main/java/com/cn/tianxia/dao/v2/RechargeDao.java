package com.cn.tianxia.dao.v2;


import com.cn.tianxia.entity.v2.RechargeEntity;

public interface RechargeDao {
    int deleteByPrimaryKey(Integer rId);

    int insert(RechargeEntity record);

    int insertSelective(RechargeEntity record);

    RechargeEntity selectByPrimaryKey(Integer rId);

    int updateByPrimaryKeySelective(RechargeEntity record);

    int updateByPrimaryKey(RechargeEntity record);
}