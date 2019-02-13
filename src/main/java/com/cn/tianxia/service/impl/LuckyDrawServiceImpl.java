package com.cn.tianxia.service.impl;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map; 
import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service; 
import com.cn.tianxia.dao.LuckyDrawDao;
import com.cn.tianxia.service.LuckyDrawService;
import com.cn.tianxia.service.UserService;

import net.sf.json.JSONObject;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * 功能概要：UserService实现类
 * 
 */
@Service
public class LuckyDrawServiceImpl implements LuckyDrawService {
	@Resource
	private LuckyDrawDao luckyDrawDao;
	@Autowired
	private UserService userService;

	@Override
	public List<Map<String, Object>> selectLuckyDrawStatus(String domain) { 
		return luckyDrawDao.selectLuckyDrawStatus(domain);
	}

	@Override
	public List<Map<String, Object>> selectLuckyDrawDetail(int lid) { 
		return luckyDrawDao.selectLuckyDrawDetail(lid);
	}

	@Override
	public List<Map<String, Object>> selectUserTimes(Map<String,Object>map) { 
		return luckyDrawDao.selectUserTimes(map);
	}

	@Override
	public List<Map<String, Object>> selectUserLuckDrawTodayTimes(String lid, String uid, String begintime, String endtime) {
		return luckyDrawDao.selectUserLuckDrawTodayTimes(lid, uid, begintime, endtime);
	}

	@Override
	public List<Map<String, Object>> selectUserLuckDrawTotalTimes(String lid, String uid) {
		return luckyDrawDao.selectUserLuckDrawTotalTimes(lid,uid);
	}

	@Override
	public void insertUserLuckrdrawLog(Map<String, Object> luckrdrawLogMap) {
		// TODO Auto-generated method stub
		luckyDrawDao.insertUserLuckrdrawLog(luckrdrawLogMap);
	}

	@Override
	public Map<String, Object> selectByPrimaryKey(String uid) {
		// TODO Auto-generated method stub
		return luckyDrawDao.selectByPrimaryKey(uid);
	}

	@Override
	public Map<String, Object> selectByCidCagentStoredvalue(String cagent) {
		// TODO Auto-generated method stub
		return luckyDrawDao.selectByCidCagentStoredvalue(cagent);
	}

	@Override
	public void insertUserTreasure(Map<String, String> userWalletLog) {
		// TODO Auto-generated method stub
		luckyDrawDao.insertUserTreasure(userWalletLog);
	}

	@Override
	public void updateByPrimaryKeySelective(Map<String, Object> updateUserWallet) {
		// TODO Auto-generated method stub
		luckyDrawDao.updateByPrimaryKeySelective(updateUserWallet);
	}

	@Override
	public Map<String, Object> getUserWalletId(String uid, String number) {
		// TODO Auto-generated method stub
		return luckyDrawDao.getUserWalletId(uid,number);
	}

	@Override
	public void insertStoredvalueLog(Map<String,String> hashMap) {
		// TODO Auto-generated method stub
		luckyDrawDao.insertStoredvalueLog(hashMap);
	}

	@Override
	public void updateStoredvalue(Map<String, String> storedvalueMap) {
		// TODO Auto-generated method stub
		luckyDrawDao.updateStoredvalue(storedvalueMap);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
	public int updateLuckydraw(Map<String, String> storedvalueMap) {
		// TODO Auto-generated method stub
		return luckyDrawDao.updateLuckydraw(storedvalueMap);
	}

	@Override
	public String selectUserDetail(Map<String, Object> userTimesMap) {
		// TODO Auto-generated method stub
		return luckyDrawDao.selectUserDetail(userTimesMap);
	}

	@Override
	public List<Map<String, Object>> selectUserValidBetTimes(Map<String, Object> userTimesMap) {
		// TODO Auto-generated method stub
		return luckyDrawDao.selectUserValidBetTimes(userTimesMap);
	}
	
	/**
	 * 抽取红包
	 */
	@Transactional(rollbackFor = Exception.class)
	public synchronized JSONObject luckyDraw(String userName,String refurl)throws Exception{
		JSONObject jo = new JSONObject();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        String dayBegin = "00:00:00";
        String dayEnd = "23:59:59";
        // 获取来源域名
        if (userName == null || userName.length() < 6) {
            jo.put("status", "faild");
            jo.put("msg", "该会员不存在");
            return jo;
        }

        List<Map<String, Object>> userlist = userService.selectUserByUserName(userName);
        if (userlist.size() < 1) {
            jo.put("status", "faild");
            jo.put("msg", "该会员不存在");
            return jo;
        }

        Map<String, Object> user = userlist.get(0);

        String cagent = userName.substring(0, 3);
        //查询网站活动
        List<Map<String, Object>> list = selectLuckyDrawStatus(refurl);
        if (list.size() < 1) {
            jo.put("status", "faild");
            jo.put("msg", "暂无活动");
            return jo;
        }

        Map<String, Object> map = list.get(0);
        String now = df.format(new Date());
        String day = now.substring(0, 10);

        
        String timesBegin = day + " "+dayBegin;
        String timesEnd = day + " "+dayEnd;
        String begintime = day + " " + map.get("begintime").toString();
        String endtime = day + " " + map.get("endtime").toString();
        //未到开奖时间
        long diff = df.parse(now).getTime() - df.parse(begintime).getTime();
        if (diff < 0) {
            jo.put("status", "waiting");
            jo.put("now", now);
            jo.put("begintime", begintime);
            jo.put("endtime", endtime);
            jo.put("diff", -diff / 1000);
            jo.put("msg", "未到活动时间");
            return jo;
        }
        //已超过活动结束时间
        diff = df.parse(now).getTime() - df.parse(endtime).getTime();
        if (diff > 0) {
            jo.put("status", "end");
            jo.put("now", now);
            jo.put("begintime", begintime);
            jo.put("endtime", endtime);
            jo.put("msg", "今日活动已结束");
            return jo;
        }
        //验证会员信息
        if (!cagent.toLowerCase().equals(map.get("cagent").toString().toLowerCase())) {
            jo.put("status", "faild");
            jo.put("msg", "会员帐号错误");
            return jo;
        }

        //计算抽奖金额
        float Min = (float) map.get("minamount"); //单次抽奖最大金额
        float Max = Float.valueOf(map.get("maxamount").toString());//单次抽奖最小金额maxamount
        if (Min >= Max) {
        	luckyDrawDao.updateStatusByAmount(Integer.parseInt(map.get("id").toString()), "1");
            jo.put("status", "faild");
            jo.put("msg", "活动已结束");
            return jo;
        }

        float result = (float) (Min + Math.random() * (Max - Min));//随机金额
        DecimalFormat fnum = new DecimalFormat("##0.00");
        result = Float.parseFloat(fnum.format(result));

        //抽奖次数计算方式,
        String type = map.get("type").toString();
        Date d = sf.parse(day);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);
        if(type.equals("1")){
        	calendar.add(Calendar.DATE, -1);
        }
        day = sf.format(calendar.getTime());
        String begin = day + " "+dayBegin;
        String end = day + " "+dayEnd;
        
        Map<String, Object> userTimesMap = new HashMap<String, Object>();
        userTimesMap.put("lid", map.get("id").toString());
        userTimesMap.put("uid", user.get("uid").toString());
        userTimesMap.put("begintime", begin);
        userTimesMap.put("endtime", end);
        List<Map<String, Object>> timeslist = null;

        //设置该会员的可抽奖次数
        if ("1".equals(map.get("typesOf"))) {
            userTimesMap.put("typesOf", 1);
            timeslist = selectUserTimes(userTimesMap);
            if (timeslist.size() <= 0) {
                jo.put("status", "faild");
                jo.put("msg", "充值金额未达标");
                return jo;
            }
        } else if ("2".equals(map.get("typesOf"))) {
            String validBetAmount = selectUserDetail(userTimesMap);
            userTimesMap.put("typesOf", 2);
            userTimesMap.put("validBetAmount", validBetAmount);
            timeslist = selectUserTimes(userTimesMap);
            if (timeslist.size() <= 0) {
                jo.put("status", "faild");
                jo.put("msg", "注单金额未达标");
                return jo;
            }
        }
       
        Map<String, Object> cagentStoredvalue = selectByCidCagentStoredvalue(cagent.toLowerCase());
        if (Float.parseFloat(cagentStoredvalue.get("remainvalue").toString()) < result) {
            jo.put("status", "faild");
            jo.put("msg", "平台额度已不足");
            return jo;
        }

        Map<String, Object> luckrdrawLogMap = new HashMap<String, Object>();
        //设置会员的总的抽奖次数
        List<Map<String, Object>> luckDrawTodayTimeslist = selectUserLuckDrawTodayTimes(map.get("id").toString(), user.get("uid").toString(), timesBegin,timesEnd);
        List<Map<String, Object>> luckDrawTotalTimeslist = selectUserLuckDrawTotalTimes(map.get("cid").toString(), user.get("uid").toString());
        int todaytimes = CollectionUtils.isEmpty(luckDrawTodayTimeslist)?0:Integer.parseInt(luckDrawTodayTimeslist.get(0).get("todaytimes").toString());
        int  totaltimes= CollectionUtils.isEmpty(luckDrawTotalTimeslist)?0:Integer.valueOf(luckDrawTotalTimeslist.get(0).get("totaltimes").toString());
        //总抽奖次数
        int allTimes =CollectionUtils.isEmpty(timeslist)?0:Integer.parseInt(timeslist.get(0).get("times").toString());
        if (luckDrawTodayTimeslist.size() > 0) {
            luckrdrawLogMap.put("todaytimes", todaytimes+1);
        }else{
            luckrdrawLogMap.put("todaytimes", 1);
        }

        if(luckDrawTotalTimeslist.size()>0){
        	luckrdrawLogMap.put("totaltimes",totaltimes+1);
        }else {
            luckrdrawLogMap.put("totaltimes", 1);
        }
        
        if (todaytimes >= allTimes) {
        	 jo.put("status", "faild");
             jo.put("msg", "已无抽奖次数");
             return jo;
        }
        float amountUsed = Float.parseFloat(map.get("amountUsed").toString());//获取奖池已用金额
        float amountLimit = Float.parseFloat(map.get("amountLimit").toString());//获取奖池最大金额
        if (result + amountUsed > amountLimit) {
            result=Math.abs(amountLimit-amountUsed);
        }
        if (result + amountUsed >= amountLimit) {
        	luckyDrawDao.updateStatusByAmount(Integer.parseInt(map.get("id").toString()), "1");
            jo.put("status", "faild");
            jo.put("msg", "活动已结束");
            return jo;
        }

        Map<String, String> hashMap = new HashMap<String, String>();
        //更新抽奖已用金额
        hashMap.clear();
        hashMap.put("id", list.get(0).get("id") + "");
        hashMap.put("amountUsed", (amountUsed + result)+"");
        updateLuckydraw(hashMap);
        
        //添加抽奖日志
        luckrdrawLogMap.put("addtime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        luckrdrawLogMap.put("amount", result);
        luckrdrawLogMap.put("uid", user.get("uid").toString());
        luckrdrawLogMap.put("lid", map.get("id").toString());
        luckrdrawLogMap.put("cid", cagentStoredvalue.get("cid").toString());
        luckrdrawLogMap.put("orderid", "HB" + System.currentTimeMillis());
        luckrdrawLogMap.put("ip", refurl);
        insertUserLuckrdrawLog(luckrdrawLogMap);

        jo.put("status", "success");
        jo.put("result", result);
        jo.put("msg", "正常");
        return jo;
	}
}
