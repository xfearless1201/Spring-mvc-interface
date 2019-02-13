package com.cn.tianxia.dao.v2;

import com.cn.tianxia.entity.v2.CagentEntity;

public interface CagentDao {
    int deleteByPrimaryKey(Integer id);

    int insert(CagentEntity record);

    int insertSelective(CagentEntity record);

    CagentEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(CagentEntity record);

    int updateByPrimaryKey(CagentEntity record);

    CagentEntity selectByCagent(String cagent);
}