/****************************************************************** 
 *
 *    Powered By tianxia-online. 
 *
 *    Copyright (c) 2018-2020 Digital Telemedia 天下网络 
 *    http://www.d-telemedia.com/
 *
 *    Package:     com.cn.tianxia.service 
 *
 *    Filename:    IBCServiceImpl.java 
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
 *    Create at:   2018年12月04日 21:10 
 *
 *    Revision: 
 *
 *    2018/12/4 21:10 
 *        - first revision 
 *
 *****************************************************************/
package com.cn.tianxia.game.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.cn.tianxia.exception.HttpClientException;
import com.cn.tianxia.po.v2.ResultResponse;
import com.cn.tianxia.util.PlatFromConfig;
import com.cn.tianxia.util.v2.MD5Utils;
import com.cn.tianxia.util.v2.TransferHttpClientUtils;
import com.cn.tianxia.util.v2.TransferUtils;
import com.cn.tianxia.vo.v2.TransferVO;

import net.sf.json.JSONObject;

/**
 * @ClassName IBCServiceImpl
 * @Description TODO(这里用一句话描述这个类的作用)
 * @Author Wilson
 * @Date 2018年12月04日 21:10
 * @Version 1.0.0
 **/
public class IBCGameServiceImpl {
    private static final Logger logger = LoggerFactory.getLogger(IBCGameServiceImpl.class);

    private String url;
    private String SecurityToken;
    private String OpCode;     // 代理码
    private String OddsType;  // 赔率类型
    private String MaxTransfer;
    private String MinTransfer;

    public IBCGameServiceImpl(Map<String,String> data) {
        PlatFromConfig pf = new PlatFromConfig();
        pf.InitData(data, "IBC");
        JSONObject jo = JSONObject.fromObject(pf.getPlatform_config());

        if(jo != null && !jo.isEmpty()){
            if(jo.containsKey("url")){
                this.url = jo.getString("url");
            }
            if(jo.containsKey("SecurityToken")){
                this.SecurityToken = jo.getString("SecurityToken");
            }
            if(jo.containsKey("OpCode")){
                this.OpCode = jo.getString("OpCode");
            }
            if(jo.containsKey("OddsType")){
                this.OddsType = jo.getString("OddsType");
            }
            if(jo.containsKey("MaxTransfer")){
                this.MaxTransfer = jo.getString("MaxTransfer");
            }
            if(jo.containsKey("MinTransfer")){
                this.MinTransfer = jo.getString("MinTransfer");
            }
        }
    }

    public ResultResponse loginGame(TransferVO transferVO) {
        try {
            Map<String, String> paramsMap =new HashMap<>();
            paramsMap.put("OpCode",OpCode);
            paramsMap.put("PlayerName",transferVO.getAccount());

            String concatStr = concatStr(paramsMap);
            String md5Str = MD5Utils.md5toUpCase_32Bit(SecurityToken+"/api/Login?"+concatStr);
            paramsMap.put("concatStr",concatStr);
            paramsMap.put("md5Str",md5Str);
            logger.info("沙巴体育登录请求参数："+paramsMap.toString());

            String res = com.cn.tianxia.pay.tx.util.HttpClientUtil.doGet(url+"/api/Login?"+concatStr+"&SecurityToken="+md5Str);
            logger.info("沙巴体育登录响应："+res);
            JSONObject jsonObject = JSONObject.fromObject(res);
            if ("0".equalsIgnoreCase(jsonObject.getString("error_code"))){

                String sessionToken = jsonObject.getString("sessionToken");
                String url = "https://mkt.gsoft-ib.com/Deposit_ProcessLogin.aspx?lang=cs&g="+sessionToken;
                if ("mobile".equalsIgnoreCase(transferVO.getTerminal())){
                    url = "http://ismart.ib.gsoft88.net/Deposit_ProcessLogin.aspx?lang=cs&st="+sessionToken;
                }
                return ResultResponse.success("沙巴体育登录成功!", url);
            }
            return ResultResponse.faild("沙巴体育登录失败!", jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultResponse.faild("沙巴体育登录失败!", "");
        }
    }

    public ResultResponse CheckOrCreateGameAccout(TransferVO transferVO){
        try {
            Map<String, String> paramsMap =new HashMap<>();
            paramsMap.put("OpCode",OpCode);
            paramsMap.put("PlayerName",transferVO.getAccount());
            paramsMap.put("OddsType",OddsType);
            paramsMap.put("MaxTransfer",MaxTransfer);
            paramsMap.put("MinTransfer",MinTransfer);
            String concatStr = concatStr(paramsMap);
            String md5Str = MD5Utils.md5toUpCase_32Bit(SecurityToken+"/api/CreateMember?"+concatStr);
            paramsMap.put("concatStr",concatStr);
            paramsMap.put("md5Str",md5Str);
            logger.info("沙巴体育创建用户请求参数："+paramsMap.toString());
            String res = com.cn.tianxia.pay.tx.util.HttpClientUtil.doGet(url+"/api/CreateMember?"+concatStr+"&SecurityToken="+md5Str);
            logger.info("沙巴体育创建用户响应："+res);
            JSONObject jsonObject = JSONObject.fromObject(res);
            if ("0".equalsIgnoreCase(jsonObject.getString("error_code"))){
                return  ResultResponse.success("沙巴体育创建用户ok",jsonObject);
            }else if("22005".equalsIgnoreCase(jsonObject.getString("error_code"))){
                return  ResultResponse.success("沙巴体育创建用户ok",jsonObject);
            }
            return  ResultResponse.faild("沙巴体育创建用户失败",jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
            return  ResultResponse.faild("沙巴体育创建用户失败","");
        }
    }

    private String concatStr(Map<String,? extends Object> paramsMap) {
        StringBuffer sb = new StringBuffer();

        for (Map.Entry m : paramsMap.entrySet()){
            sb.append(m.getKey()).append("=").append(m.getValue()).append("&");
        }
        String substring = sb.substring(0, sb.length() - 1);
        return substring;

    }

    public ResultResponse getBalance(String playerName) throws Exception {
        Map<String, Object> paramsMap =new HashMap<>();
        paramsMap.put("OpCode",OpCode);
        paramsMap.put("PlayerName",playerName);

        String concatStr = concatStr(paramsMap);
        String md5Str = MD5Utils.md5toUpCase_32Bit(SecurityToken+"/api/CheckUserBalance?"+concatStr);
        paramsMap.put("concatStr",concatStr);
        paramsMap.put("md5Str",md5Str);
        logger.info("沙巴体育查询余额请求参数："+paramsMap);
        String fullUrl = url+"/api/CheckUserBalance?"+concatStr+"&SecurityToken="+md5Str;
        return getResultResponse(playerName,null,true,TransferUtils.GET_BALANCE,fullUrl);
    }

    /**
     * 转入(游戏上分)
     */
    public ResultResponse transferIn(TransferVO vo) throws Exception {
        return transfer(vo,TransferUtils.TRANSFER_IN,"1");
    }

    private ResultResponse transfer(TransferVO vo,String action,String direction) throws Exception {
        Map<String, Object> paramsMap =new HashMap<>();
        paramsMap.put("OpCode",OpCode);
        paramsMap.put("PlayerName",vo.getAccount());

        paramsMap.put("OpTransId",vo.getOrderNo());
        paramsMap.put("amount",vo.getMoney());
        // 0 提款 ,1 存款
        paramsMap.put("Direction",direction);

        String concatStr = concatStr(paramsMap);
        String md5Str = MD5Utils.md5toUpCase_32Bit(SecurityToken+"/api/FundTransfer?"+concatStr).toLowerCase();
        paramsMap.put("concatStr",concatStr);
        paramsMap.put("md5Str",md5Str);
        logger.info("沙巴体育转账请求参数："+paramsMap);
        String fullUrl = url+"/api/FundTransfer?"+concatStr+"&SecurityToken="+md5Str;
        return getResultResponse(vo.getAccount(),vo.getOrderNo(),false,action,fullUrl);
    }

    /**
     * 转出(游戏下分)
     */
    public ResultResponse transferOut(TransferVO vo) throws Exception {
        return transfer(vo,TransferUtils.TRANSFER_OUT,"0");
    }

    private static final String PLATFORM_KEY = "IBC";
    private static final int SUCCESS_CODE = 0;

    private ResultResponse getResultResponse(String account,String orderNo, boolean isGetBalance, String action, String url) throws Exception {
        String result = null;
        try {
            result = TransferHttpClientUtils.doGet(url,null);
            logger.info(PLATFORM_KEY+"-方法{}-Http请求结果返回为{}",action,result);
        } catch (HttpClientException e) {//处理三种超时
            TransferUtils.setFileLog(PLATFORM_KEY,account,url,action,result);
            return ResultResponse.error("["+PLATFORM_KEY+"]"+action+"请求ERROR!",null,orderNo,result);
        }
        if(StringUtils.isNotEmpty(result)){
            com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(result);
            if(jsonObject != null && jsonObject.containsKey("error_code")){
                int errorCode = jsonObject.getInteger("error_code");
                String message = jsonObject.getString("message");
                if(SUCCESS_CODE == errorCode){
                    String balance = null;
                    if(isGetBalance){
                        JSONArray data = jsonObject.getJSONArray("Data");
                        com.alibaba.fastjson.JSONObject st = (com.alibaba.fastjson.JSONObject)data.get(0);
                        balance = st.getString("balance");
                    }
                    if(TransferUtils.TRANSFER_IN.equals(action) || TransferUtils.TRANSFER_OUT.equals(action)){
                        balance = jsonObject.getJSONObject("Data").getString("after_amount");
                    }
                    return ResultResponse.success("[IBC]请求成功",balance, orderNo,result);
                }else {
                    if(TransferUtils.CHECK_OR_CREATE_GAME_ACCOUT.equals(action) && 22005 == errorCode){
                        return ResultResponse.success(message,null, null,result);
                    }
                    return ResultResponse.faild(message,null, orderNo,result);
                }
            }
        }
        return ResultResponse.faild("["+PLATFORM_KEY+"]"+action+"请求结果为异常结果!",null, orderNo,result);
    }
}