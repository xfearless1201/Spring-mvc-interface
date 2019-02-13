package com.cn.tianxia.dao.v2;

import com.cn.tianxia.entity.v2.CagentMsgEntity;
import org.apache.ibatis.annotations.Param;

public interface CagentMsgDao {
    int deleteByPrimaryKey(Integer id);

    int insert(CagentMsgEntity record);

    int insertSelective(CagentMsgEntity record);

    CagentMsgEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(CagentMsgEntity record);

    int updateByPrimaryKey(CagentMsgEntity record);

    CagentMsgEntity selectMsgLog(@Param("cagent") String cagent, @Param("mobileNo") String mobileNo, @Param("type") String type, @Param("sendTime") String sendTime);
}