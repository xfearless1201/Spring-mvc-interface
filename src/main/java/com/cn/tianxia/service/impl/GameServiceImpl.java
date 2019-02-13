/****************************************************************** 
 *
 *    Powered By tianxia-online. 
 *
 *    Copyright (c) 2018-2020 Digital Telemedia 天下网络 
 *    http://www.d-telemedia.com/
 *
 *    Package:     com.cn.tianxia.service.impl 
 *
 *    Filename:    GameServiceImpl.java 
 *
 *    Description: TODO(用一句话描述该文件做什么) 
 *
 *    Copyright:   Copyright (c) 2018-2020 
 *
 *    Company:     天下网络科技 
 *
 *    @author: Wilson
 *
 *    @version: 1.0.0
 *
 *    Create at:   2018年10月26日 10:09 
 *
 *    Revision: 
 *
 *    2018/10/26 10:09 
 *        - first revision 
 *
 *****************************************************************/
package com.cn.tianxia.service.impl;

import com.cn.tianxia.dao.GameListDao;
import com.cn.tianxia.service.IGameService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @ClassName GameServiceImpl
 * @Description TODO(这里用一句话描述这个类的作用)
 * @Author Wilson
 * @Date 2018年10月26日 10:09
 * @Version 1.0.0
 **/
@Service("gameService")
public class GameServiceImpl implements IGameService {

    @Resource
    private GameListDao gameListDao;

    @Override
    public List<Map<String, String>> selectByGameType(Map<String,Object> params) {
        List<Map<String, String>> counts = gameListDao.selectByGameTypeCount(params);
        List<Map<String, String>> lists = gameListDao.selectByGameType(params);
        lists.add(0,counts.get(0));
        return lists;
    }

}