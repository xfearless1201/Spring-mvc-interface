package com.cn.tianxia.dao.v2;

import com.cn.tianxia.entity.v2.CagentMsgconfigEntity;
import org.springframework.data.repository.query.Param;

public interface CagentMsgconfigDao {
    int deleteByPrimaryKey(Integer id);

    int insert(CagentMsgconfigEntity record);

    int insertSelective(CagentMsgconfigEntity record);

    CagentMsgconfigEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(CagentMsgconfigEntity record);

    int updateByPrimaryKey(CagentMsgconfigEntity record);

    CagentMsgconfigEntity selectByCagent(@Param("cagent") String cagent);
}