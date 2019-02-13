package com.cn.tianxia.dao;

import org.springframework.context.annotation.Scope;

import java.util.List;
import java.util.Map;

public interface GameListDao {
    @Scope("prototype")
    List<Map<String,String>> selectByGameType(Map<String,Object> params);

    List<Map<String,String>> selectByGameTypeCount(Map<String,Object> params);
}