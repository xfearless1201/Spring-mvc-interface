package com.cn.tianxia.service.v2.impl;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.cn.tianxia.dao.v2.NewUserDao;
import com.cn.tianxia.datadao.GameBetInfoDao;
import com.cn.tianxia.entity.v2.GameBetInfoEntity;
import com.cn.tianxia.entity.v2.UserEntity;
import com.cn.tianxia.po.v2.JSONArrayResponse;
import com.cn.tianxia.service.v2.GameBetService;
import com.cn.tianxia.vo.v2.BetInfoVO;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 
 * @ClassName GameBetServiceImpl
 * @Description 游戏注单实现类
 * @author Hardy
 * @Date 2019年1月30日 下午8:56:09
 * @version 1.0.0
 */
@Service
public class GameBetServiceImpl implements GameBetService {
    
    //日志
    private static final Logger logger = LoggerFactory.getLogger(GameBetServiceImpl.class);

    @Autowired
    private GameBetInfoDao gameBetInfoDao;
    
    @Autowired
    private NewUserDao newUserDao;
    
    /**
     * 查询用户注单信息
     */
    public JSONArray getGameBetInfo(BetInfoVO betInfoVO){
        logger.info("调用查询用户游戏注单列表业务开始==================START================");
        JSONArray data = new JSONArray();
        try {
            //通过用户ID查询用户信息
            UserEntity user = newUserDao.selectByPrimaryKey(Integer.parseInt(betInfoVO.getUid()));
            if(user == null){
                //非法用户
                logger.info("查询用户信息失败,非法用户");
                return JSONArrayResponse.faild("查询用户信息失败,非法用户");
            }
            //获取用户信息
            String ag_username = user.getAgUsername();//ag游戏账号
            String cagent = user.getCagent().toLowerCase();//平台编码
            String type = betInfoVO.getType().toUpperCase();//游戏编码
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //日期格式化
            SimpleDateFormat ssdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String bdate = betInfoVO.getBdate();//起始时间
            String edate = betInfoVO.getEdate();//结束时间
            if(StringUtils.isBlank(bdate)){
                bdate = ssdf.format(new Date())+" 00:00:00";
            }
            
            if(StringUtils.isBlank(edate)){
                edate = ssdf.format(new Date()) + " 23:59:59";
            }
            Date btime = sdf.parse(bdate);
            Date etime = sdf.parse(edate);
            
            int pageNo = betInfoVO.getPageNo();
            int pageSize = betInfoVO.getPageSize();
            
            if("HG".equals(type)){
                ag_username = user.getHgUsername();
            }
            
            if(StringUtils.isBlank(ag_username)){
                logger.info("查询用户游戏登录账号为空");
                return JSONArrayResponse.faild("查询用户游戏登录账号为空");
            }
            
            //查询游戏注单列表
            List<GameBetInfoEntity> gameBetInfos = 
                    gameBetInfoDao.findAllByPage(ag_username, cagent,type, btime, etime, pageNo, pageSize);
            
            //查询游戏注单总数
            Integer totalCounts = gameBetInfoDao.selectBetCount(ag_username, cagent,type, btime, etime);
            if(totalCounts == null){
                totalCounts = 0;
            }
            JSONObject pagesJson = new JSONObject();
            pagesJson.put("cnt", totalCounts);
            data.add(pagesJson);
            //统计注单
            Double subPayoutSum = 0d;//统计注单派彩总金额
            Double subBetamountSum = 0d;//统计投注总金额
            Double subNetAmountSum = 0d;//统计玩家输赢总金额
            Double subValidBetAmountSum = 0d;//统计玩家有效总投注
            JSONArray betinfosArray = new JSONArray();
            if(!CollectionUtils.isEmpty(gameBetInfos)){
                for (GameBetInfoEntity gameBetInfo : gameBetInfos) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("id",gameBetInfo.getId());
                    jsonObject.put("bettime",sdf.parse(gameBetInfo.getBettime()).getTime());
                    jsonObject.put("type",gameBetInfo.getType());
                    jsonObject.put("betAmount",gameBetInfo.getBetAmount());
                    jsonObject.put("validBetAmount",gameBetInfo.getValidBetAmount());
                    jsonObject.put("Payout",gameBetInfo.getPayout());
                    jsonObject.put("netAmount",gameBetInfo.getNetAmount());
                    
                    subPayoutSum += gameBetInfo.getPayout();
                    subBetamountSum += gameBetInfo.getBetAmount();
                    subNetAmountSum += gameBetInfo.getNetAmount();
                    subValidBetAmountSum += gameBetInfo.getValidBetAmount();
                    betinfosArray.add(jsonObject);
                }
            }
            JSONObject sumJson = new JSONObject();
            DecimalFormat dcf = new DecimalFormat("0.00");
            sumJson.put("payoutSum", dcf.format(subPayoutSum));
            sumJson.put("betamountSum", dcf.format(subBetamountSum));
            sumJson.put("netAmountSum", dcf.format(subNetAmountSum));
            sumJson.put("validBetAmountSum", dcf.format(subValidBetAmountSum));
            data.add(sumJson);
            data.addAll(betinfosArray);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("调用查询用户游戏注单列表业务异常:{}",e.getMessage());
        }
        return data;
    }
}
