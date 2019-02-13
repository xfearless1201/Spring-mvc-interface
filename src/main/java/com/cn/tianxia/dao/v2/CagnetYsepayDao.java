package com.cn.tianxia.dao.v2;

import com.cn.tianxia.entity.v2.CagnetYsepayEntity;
import org.springframework.data.repository.query.Param;

public interface CagnetYsepayDao {
    int deleteByPrimaryKey(Integer id);

    int insert(CagnetYsepayEntity record);

    int insertSelective(CagnetYsepayEntity record);

    CagnetYsepayEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(CagnetYsepayEntity record);

    int updateByPrimaryKey(CagnetYsepayEntity record);

    CagnetYsepayEntity selectYsepayConfigByUsername(@Param("username") String username);
}