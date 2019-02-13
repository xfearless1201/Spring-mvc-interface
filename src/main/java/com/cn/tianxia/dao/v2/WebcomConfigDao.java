package com.cn.tianxia.dao.v2;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.cn.tianxia.entity.v2.WebcomConfigEntity;

public interface WebcomConfigDao {
    int deleteByPrimaryKey(Integer id);

    int insert(WebcomConfigEntity record);

    int insertSelective(WebcomConfigEntity record);

    WebcomConfigEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(WebcomConfigEntity record);

    int updateByPrimaryKey(WebcomConfigEntity record);
    
    List<WebcomConfigEntity> findAllByCagent(@Param("cagent") String cagent);
    
    List<WebcomConfigEntity> getNoticesByCagent(@Param("cagent") String cagent);
    
    List<WebcomConfigEntity> findAllByMobileType(@Param("cagent") String cagent,@Param("type") String type);
}