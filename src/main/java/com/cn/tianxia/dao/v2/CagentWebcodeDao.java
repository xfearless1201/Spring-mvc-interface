package com.cn.tianxia.dao.v2;

import org.apache.ibatis.annotations.Param;

import com.cn.tianxia.entity.v2.CagentWebcodeEntity;

public interface CagentWebcodeDao {
    int deleteByPrimaryKey(Integer id);

    int insert(CagentWebcodeEntity record);

    int insertSelective(CagentWebcodeEntity record);

    CagentWebcodeEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(CagentWebcodeEntity record);

    int updateByPrimaryKey(CagentWebcodeEntity record);
    
    CagentWebcodeEntity getWebcomConfig(@Param("type") Integer type,@Param("cid") Integer cid);
}