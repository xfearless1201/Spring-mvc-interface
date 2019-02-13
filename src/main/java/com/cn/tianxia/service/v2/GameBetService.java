package com.cn.tianxia.service.v2;

import com.cn.tianxia.vo.v2.BetInfoVO;

import net.sf.json.JSONArray;

/**
 * 
 * @ClassName GameBetService
 * @Description 游戏注单接口
 * @author Hardy
 * @Date 2019年1月30日 下午8:55:39
 * @version 1.0.0
 */
public interface GameBetService {

    public JSONArray getGameBetInfo(BetInfoVO betInfoVO);
}
