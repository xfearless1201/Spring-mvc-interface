package com.cn.tianxia.dao;


import com.cn.tianxia.entity.PlatFormConfig;
import org.apache.ibatis.annotations.Param;

public interface PlatFormConfigDao {
    /**
     * 根据key获取配置
     * @param key
     * @return
     */
    PlatFormConfig getConfigByKey(@Param("key") String key);
}
