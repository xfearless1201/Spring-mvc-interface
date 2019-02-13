package com.cn.tianxia.dao.v2;

import com.cn.tianxia.entity.v2.MobileLogEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MobileLogDao {
    int deleteByPrimaryKey(Integer id);

    int insert(MobileLogEntity record);

    int insertSelective(MobileLogEntity record);

    MobileLogEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(MobileLogEntity record);

    int updateByPrimaryKey(MobileLogEntity record);

    List<MobileLogEntity> selectMobileLogByUid(@Param("uid") String uid);
}