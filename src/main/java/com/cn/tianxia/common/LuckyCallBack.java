/**
 * 
 */
package com.cn.tianxia.common;

import java.util.concurrent.Callable;

import com.cn.tianxia.service.LuckyDrawService;

import net.sf.json.JSONObject;

/**
 * @author seven
 *
 */
public class LuckyCallBack implements Callable<JSONObject>{

	private LuckyDrawService luckyDrawService;
	private String userName;
	private String refurl;
	
	public LuckyCallBack(LuckyDrawService luckyDrawService,String userName,String refurl){
		this.luckyDrawService = luckyDrawService;
		this.userName = userName;
		this.refurl = refurl;
	}
	
	@Override
	public JSONObject call() throws Exception {
		return luckyDrawService.luckyDraw(userName, refurl);
	}

}
