package com.cn.tianxia.game;

import com.cn.tianxia.vo.v2.GameBalanceVO;
import com.cn.tianxia.vo.v2.GameCheckOrCreateVO;
import com.cn.tianxia.vo.v2.GameForwardVO;
import com.cn.tianxia.vo.v2.GameQueryOrderVO;
import com.cn.tianxia.vo.v2.GameTransferVO;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName GameReflectService
 * @Description 游戏反射接口
 * @author Hardy
 * @Date 2019年2月8日 下午2:54:28
 * @version 1.0.0
 */
public interface GameReflectService {
    
    /**
     * 
     * @Description 上分接口
     * @param gameTransferVO
     * @return
     */
    public JSONObject transferIn(GameTransferVO gameTransferVO);
    
    
    /**
     * 
     * @Description 下分接口
     * @param gameTransferVO
     * @return
     */
    public JSONObject transferOut(GameTransferVO gameTransferVO);
    
    /**
     * 
     * @Description 游戏跳转
     * @return
     */
    public JSONObject forwardGame(GameForwardVO gameForwardVO);
    
    /**
     * 获取游戏余额
     * @Description 
     * @return
     */
    public JSONObject getBalance(GameBalanceVO gameBalanceVO);
    
    /**
     * 
     * @Description 检查或创建用户信息
     * @return
     */
    public JSONObject checkOrCreateAccount(GameCheckOrCreateVO gameCheckOrCreateVO);
    
    /**
     * 
     * @Description 查询订单
     * @return
     */
    public JSONObject queryTransferOrder(GameQueryOrderVO gameQueryOrderVO);

}
