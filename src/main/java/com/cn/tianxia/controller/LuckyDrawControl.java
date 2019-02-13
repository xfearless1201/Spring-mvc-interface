package com.cn.tianxia.controller;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cn.tianxia.common.LuckyCallBack;
import com.cn.tianxia.dao.LuckyDrawDao;
import com.cn.tianxia.service.LuckyDrawService;
import com.cn.tianxia.service.UserService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("LuckyDraw")
public class LuckyDrawControl extends BaseController {


    @Resource
    private LuckyDrawService luckyDrawService;

    @Resource
    private UserService userService;
    @Autowired
    private LuckyDrawDao luckyDrawDao;

    
    private static ThreadPoolTaskExecutor taskExecutor;
    
    private static ConcurrentHashMap<String, String> userName = new ConcurrentHashMap<String, String>();

    static{
    	taskExecutor = new ThreadPoolTaskExecutor();
    	taskExecutor.setCorePoolSize(10);
    	taskExecutor.setMaxPoolSize(100);
    	taskExecutor.setKeepAliveSeconds(1);
    	taskExecutor.setQueueCapacity(5);
    	taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    	taskExecutor.initialize();
    }

    /**
     * 获取当前网站活动状态
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "getPrize.do")
    @ResponseBody
    @Transactional
    public JSONObject getPrize(HttpServletRequest request, HttpServletResponse response, String username) throws Exception {
    	JSONObject jo = new JSONObject();
    	try{
    		put(username);
    		String refurl = request.getHeader("referer").split("/")[2];
    		FutureTask<JSONObject> dbtask = new FutureTask<JSONObject>(new LuckyCallBack(luckyDrawService, username, refurl));
			taskExecutor.submit(dbtask);
			jo =   dbtask.get();
			userName.remove(username);
        } catch (Exception e) {
        	jo.put("status", "faild");
        	jo.put("msg", "抽奖失败");
        	return jo;
        }
    	return jo;
        
    }

    /**
     * 获取当前网站活动状态
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "getStatus.do")
    @ResponseBody
    public Object LuckyDrawStatus(HttpServletRequest request, HttpServletResponse response) throws Exception {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String refurl = request.getHeader("referer").split("/")[2];
        JSONObject jo = new JSONObject();
        List<Map<String, Object>> list = luckyDrawService.selectLuckyDrawStatus(refurl);
        logger.info("来源域名是【{}】", refurl);
        if (list.size() < 1) {
            jo.put("status", "faild");
            jo.put("msg", "暂无活动");
            return jo;
        }
        Map<String, Object> map = list.get(0);
        String now = df.format(new Date());
        String day = now.substring(0, 10);
        String begintime = day + " " + map.get("begintime").toString();
        String endtime = day + " " + map.get("endtime").toString();
        String name = map.get("luckyname").toString();
        String type = map.get("type").toString();
        String minamount = map.get("minamount").toString();
        String maxamount = map.get("maxamount").toString();
        //未到当日开奖时间
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
        //未到隔日开奖时间
        diff = df.parse(now).getTime() - df.parse(endtime).getTime();
        if (diff > 0) {
        	Calendar c = Calendar.getInstance();
        	c.setTime(df.parse(begintime));
        	c.add(Calendar.DAY_OF_MONTH, 1);
        	long dd = c.getTimeInMillis() -df.parse(now).getTime();
        	
        	jo.put("status", "waiting");
            jo.put("now", now);
            jo.put("begintime", begintime);
            jo.put("endtime", endtime);
            jo.put("diff", dd / 1000);
            jo.put("msg", "未到活动时间");
            return jo;
        }
        //活动正常开启
        jo.put("status", "success");
        jo.put("now", now);
        jo.put("begintime", begintime);
        jo.put("endtime", endtime);
        jo.put("diff", -diff / 1000);
        jo.put("msg", "正常");
        jo.put("name", name);
        jo.put("type", type);
        jo.put("minamount", minamount);
        jo.put("maxamount", maxamount);

        JSONArray data = new JSONArray();
        for (int i = 0; i < list.size(); i++) {
            JSONObject json = new JSONObject();
            json.put("balance", list.get(i).get("balance").toString());
            json.put("times", list.get(i).get("times").toString());
            data.add(json);
        }
        jo.put("data", data);
        return jo;
    }
    
    /**
     * 阻止重复抽奖
     * @param name
     */
    private void put(String name){
    	if(!userName.contains(name)){
    		userName.put(name, name);
    	}else{
    		Assert.notNull(null, "未中奖");
    	}
    }
}  