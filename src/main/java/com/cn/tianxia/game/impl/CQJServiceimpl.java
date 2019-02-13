package com.cn.tianxia.game.impl;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.tianxia.game.GameReflectService;
import com.cn.tianxia.pay.ys.util.StringUtils;
import com.cn.tianxia.util.PlatFromConfig;
import com.cn.tianxia.vo.v2.GameBalanceVO;
import com.cn.tianxia.vo.v2.GameCheckOrCreateVO;
import com.cn.tianxia.vo.v2.GameForwardVO;
import com.cn.tianxia.vo.v2.GameQueryOrderVO;
import com.cn.tianxia.vo.v2.GameTransferVO;
import com.cn.tianxia.vo.v2.TransferVO;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 
 * @ClassName CQJServiceimpl
 * @Description CQJ棋牌
 * @author Hardy
 * @Date 2019年2月9日 下午4:25:55
 * @version 1.0.0
 */
public class CQJServiceimpl implements GameReflectService{

    private static final Logger logger = LoggerFactory.getLogger(CQJServiceimpl.class);

    private static final String  ACCOUNT = "account";  // 账号

    private static final String  PASSWORD = "password"; // 密码

    private static final String  CONTENT_TYPE = "Content-Type";

    private static final String  CHARSET = "charset";

    private static final String  USER_TOKEN  = "usertoken";

    private static final String  LANG  = "lang";

    private static final String  DEPOSIT  = "deposit";

    private static final String  WITHDRAW = "withdraw";

    private static final String  FIND_ACCOUNT = "findAccount"; //查询玩家余额

    private static final String  LOBBYLINK  = "lobbylink";//获取游戏大厅链接

    private static final String  MEMBER_LOGIN  = "memberLogin"; //登录

    private static final String  IS_ACCOUNT   = "isAccount";//获取玩家是否已注册

    private static final String  CREATE_ACCOUNT = "createAccount"; //创建游戏玩家

    private static final String  GET_TOKEN = "getToken";//获取玩家token

    private static final String  DATA = "data";

    private static final String  CODE = "code";

    private static final String  STATUS = "status";

    private static final String  UTF = "utf-8";

    private static final String  APPLICATION  = "application/x-www-form-urlencoded";

    private static final String  AUTHORIZATION = "Authorization";

    private static final String   BALANCE = "balance";// 账号余额

    private static final String  AMOUNT = "amount"; //金额

    private static final String  MTCODE = "mtcode"; //交易代码

    private static final String CURRENCY = "currency";

    private static final String  QUERY_ORDER = "queryOrder"; //订单交易状态

    private static String RECORD_URL;//查询订单状态

    private  static String WITHDRAW_URL /*= "http://api.cqgame.games/gameboy/player/withdraw"*/;//取款 /gameboy/player

    private static String DEPOSIT_URL /*= "http://api.cqgame.games/gameboy/player/deposit"*/;//存款

    private static   String FIND_ACCOUNT_URL /*= "http://api.cqgame.games/gameboy/player/balance"*/;//获取余额 链接直接拼接参数 /gameboy/player:account

    private static   String LOBBYLINK_URL  /*= "http://api.cqgame.games/gameboy/player/lobbylink"*/;//获取大厅链接/gameboy/player

    private static String TOKEN_URL/* = "http://api.cqgame.games/gameboy/player/token"*/;//获取用户token /gameboy/player/token/:account

    private static String CREATE_URL  /*=  "http://api.cqgame.games/gameboy/player"*/;//创建用户
    private static String IS_USER_URL /*= "http://api.cqgame.games/gameboy/player/check"*/ ; //查询用户是否存在*/

    private static String LOGIN /* = " http://api.cqgame.games/gameboy/player/login"*/;//登录 ==

    private static String TOKEN /* ="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyaWQiOiI1YzMzMTBkYjJkMjRmNzAwMDExZTliNTY" +
            "iLCJhY2NvdW50Ijoid24iLCJvd25lciI6IjVjMzMxMGRiMmQyNGY3MDAwMTFlOWI1NiIsInBhcmVudCI6InNlbGYiLCJjdXJyZW5jeSI6IkNOW" +
            "SIsImp0aSI6Ijk2ODIyNDI2MiIsImlhdCI6MTU0Njg1MDUyMywiaXNzIjoiQ3lwcmVzcyIsInN1YiI6IlNTVG9rZW4ifQ.yYNN-zU3z7MMCEhCC9ke" +
            "aZHAv5QbG8dH8vm__wtWGIE"*/;

    private  String URL;//url

    public CQJServiceimpl(Map<String, String> pmap) {
        PlatFromConfig pf = new PlatFromConfig();
        pf.InitData(pmap, "CQJ");
        JSONObject jo = JSONObject.fromObject(pf.getPlatform_config());
        URL = jo.getString("URL");
        RECORD_URL = URL + jo.getString("RECORD_URL");
        WITHDRAW_URL = URL+ jo.getString("WITHDRAW_URL");
        DEPOSIT_URL = URL + jo.getString("DEPOSIT_URL");
        FIND_ACCOUNT_URL = URL + jo.getString("FIND_ACCOUNT_URL");
        LOBBYLINK_URL = URL + jo.getString("LOBBYLINK_URL");
        TOKEN_URL =URL +  jo.getString("TOKEN_URL");
        CREATE_URL = URL + jo.getString("CREATE_URL");
        LOGIN =URL +  jo.getString("LOGIN");
        IS_USER_URL= URL + jo.getString("IS_USER_URL");
        TOKEN = jo.getString("TOKEN");
    }



    /**
     * 获取订单交易状态
     * @param  mtcode 交易编码
     * @return
     * @throws Exception
     */
    public boolean queryOrder(String mtcode)throws  Exception{
        logger.debug("queryOrder(String mtcode {}  -start" + mtcode);
        if(StringUtils.isEmpty(mtcode)){
            throw  new Exception("交易代码不能为空！");//TODO
        }
        boolean status = false;
        try{
            HttpMethodParams  params = new HttpMethodParams();
            params.setParameter(MTCODE,mtcode);
            JSONObject returnJson = sendGet(params,RECORD_URL,QUERY_ORDER);
            JSONObject json = null;
            if(returnJson.size()>0){
                json = JSONObject.fromObject(returnJson.get(DATA));
                if(json != null){
                    //发起交易请求时没有回应或者其他状态，查询该交易的订单状态，若状态为failed:失敗 pending:尚未完成交易，
                    //pending:尚未完成交易 视为失败！----- 暂定(第三方系统会在一个小时内处理，并不知道具体时间，若失败则会走人工通道进行处理)
                    status = json.get("status").toString() =="success"? true : false;

                }else{
                    json = JSONObject.fromObject(returnJson.get(STATUS ));
                    logger.error("查询订单交易状态失败！ 状态码："+ json.get("code") +"message:"+ json.get("message"));
                    status = false;
                }
            }

        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
        return  status;

    }

    /**
     * 查询玩家是否已注册过账号
     * 异常排查通过json返回的code码对应接口文档的异常信息，或询问第三方
     * @param transferVO
     * @return
     */
    public boolean checkOrCreateGameAccout(TransferVO transferVO) throws  Exception{
        logger.debug(" CheckOrCreateGameAccouts(TransferVO transferVO = {}" + transferVO);
        if(StringUtils.isEmpty(transferVO.getAccount())){
            throw  new  Exception("玩家账号不能为空！");
        }
        boolean b = false;
        HttpMethodParams params = new HttpMethodParams();
        params.setParameter(ACCOUNT,transferVO.getAccount());
        JSONObject returnJson =  sendGet(params,IS_USER_URL,IS_ACCOUNT);
        Boolean returnParam = Boolean.FALSE;
        JSONObject statusParam = null;
        if (returnJson != null){
            statusParam =  returnJson.getJSONObject("status");
            if (statusParam.get("code").toString().equals("0")){
                returnParam = (boolean) returnJson.get("data");
                if(!returnParam){
                    if(createAccount(transferVO.getAccount(),transferVO.getPassword())){
                        logger.info("CQJ注册用户成功！ 用户账号为"+ transferVO.getAccount());
                        b = true;
                    }else{
                        logger.error("CQJ注册用户失败! 用户账号为"+transferVO.getAccount());
                        b = false;
                    }
                }else{
                    logger.info("检查用户注册成功返回，用户已在第三方验证！");
                    b = true;
                }
            }else {
                logger.error("CQJ请求第三方接口出现异常，异常原因 CODE：" + statusParam.get("code").toString());
                b = false;
            }

        }else{
            logger.error("CQJ请求第三方接口异常！  异常请求报文：Params ="+ params + "URL = "+IS_USER_URL + "Type ="+ IS_ACCOUNT);
            b = false;
        }
        logger.debug("CheckOrCreateGameAccouts() end   return = "+ returnParam);

        return  b;
    }

/*
    public static  void main(String[] arg) throws  Exception{
        CQJServiceimpl cqjServiceimpl = new CQJServiceimpl(new HashMap<String, String>());
        TransferVO transferVO = new TransferVO();
        transferVO.setOrderNo(UUID.randomUUID().toString());
        transferVO.setMoney(50.0);
        transferVO.setAccount("jacky123test033");
        transferVO.setPassword("123456");
        cqjServiceimpl.TransferOut(transferVO);
    }
*/

    public String transferIn(TransferVO transferVO) throws Exception {
        logger.debug("TransferIn(TransferVO transferVO  = {}  -start" + transferVO);
        boolean resultResponse = this.checkOrCreateGameAccout(transferVO);
        String response = "error";
        if (resultResponse) {
            response = this.Transfer(transferVO, this.DEPOSIT_URL, "deposit");
        }
        return response;
    }
    /**
     *转出(游戏下分)
     */
    public String transferOut(TransferVO transferVO) throws Exception {
        return Transfer(transferVO,WITHDRAW_URL,"withdraw");
    }

    private  String Transfer(TransferVO transferVO ,String url,String type) throws Exception {
        logger.debug("CQJ  - TransferIn(TransferVO transferVO = {}  -start" + transferVO);
        if(transferVO.getAccount()== null|| transferVO.getMoney()== null){
            logger.error("玩家账号或金额不能为空！");
        }
        HttpMethodParams params = new HttpMethodParams();
        String res = "error";
        try{
            params.setParameter(ACCOUNT,transferVO.getAccount());
            params.setParameter(AMOUNT,transferVO.getMoney());
            params.setParameter(MTCODE,transferVO.getOrderNo());
            logger.debug("deposit -- params:"+ params);
            JSONObject returnJson = sendPost(params,url,type);
            JSONObject statusJson = JSONObject.fromObject(returnJson.get(STATUS));
            if(!statusJson.get("code").toString().equals("0")){
                //游戏转账失败！
                logger.error("CQJ电子游戏转账失败   失败用户："+transferVO.getAccount()+"失败状态码："+ statusJson.get("code"));
            }else{
                /**
                 * 游戏上下分成功判断条件为code = 0,与data返回值数据不为空
                 */
                JSONObject json = JSONObject.fromObject(returnJson.get(DATA));
                if (json != null){
                    res = "success";
                }else{
                    logger.error("第三方出现转账异常  订单号："+ transferVO.getOrderNo()+"游戏玩家："+ transferVO.getAccount());
                }
            }

        }catch (Exception e){
            logger.error(e.getMessage(),e);
            throw e ;
        }
        logger.debug("Transfer(TransferVO transferVO ,String url) {}   - return" + res);
        return  res;
    }




    /**
     * 查询玩家余额
     * @param account  玩家账号
     * @return  链接带参数
     * @throws Exception
     */
    public  String findAccount(String account) throws Exception{
        logger.debug("findAccount(String account = {} -start" + account );
        if (StringUtils.isEmpty(account)){
            throw  new Exception("玩家账号不能为空！");
        }
        HttpMethodParams params = new HttpMethodParams();
        String balance = "error";
        try{
            params.setParameter(ACCOUNT,account);
            JSONObject returnJson =  sendGet(params,FIND_ACCOUNT_URL,FIND_ACCOUNT);
            JSONObject json = JSONObject.fromObject(returnJson.get(DATA));
            if(json != null && json.get("balance")!= null ){
                balance = json.get("balance").toString();
            }else{
                balance = "error";
                JSONObject status = JSONObject.fromObject(returnJson.get(STATUS));
                logger.debug("请求查询玩家余额异常--异常状态码："+ status.get(CODE));
                //请求异常
            }
        }catch (Exception e){
            logger.debug(e.getMessage(),e);
            throw  e;
        }
        return  balance;
    }


    /**
     * 登录后获取游戏链接与游戏token
     * @param account
     * @param password
     * @return
     */
    public String getLobbylink(String account ,String password)throws  Exception{
        logger.info("getLobbylink(String account ,String password = {} -start"+account + password );
        String lobbylink = null;
        if (StringUtils.isEmpty(account) || StringUtils.isEmpty(password)){
            lobbylink = "error";
        }
        String userToken = null;
        if(isAccount(account)){
             userToken = memberLogin(account,password);
            if(userToken != null){
                Map<String,String> map  =  lobbylink(userToken);
                lobbylink = map.get("url");
            }else{
                lobbylink = "error";
            }
        }else{
            /**
             * 用户账号没有在第三方游戏平台存在，注册玩家账号获取游戏链接
             */
          boolean createStatus=  createAccount(account,password);
          if(createStatus){
              userToken = memberLogin(account,password);
              if(userToken != null) {
                  Map<String, String> map = lobbylink(userToken);
                  lobbylink = map.get("url");
              }else {
                  logger.debug("获取用户token异常--->  获取方法 getToken()");
                  lobbylink = "error";
              }
          }else{
              logger.info("CQJ创建第三方玩家失败,  玩家获取游戏链接失败！");
              lobbylink = "error";
          }

        }
        return  lobbylink;
    }

    /**
     * 玩家登录
     * @param account   账号
     * @param password  密码
     * @return
     * @throws Exception
     */
    public String memberLogin(String account , String password) throws  Exception{
        logger.debug(" memberLogin(String account , String password = {}  -start " + account + password );
        if(StringUtils.isEmpty(account) || StringUtils.isEmpty(password)){
            throw  new Exception("玩家登录参数数据不足！");  //TODO
        }
        HttpMethodParams params = new HttpMethodParams();
        params.setParameter(ACCOUNT,account);
        params.setParameter(PASSWORD,password);
        JSONObject returnJson =  sendPost(params,LOGIN,MEMBER_LOGIN);
        String token = null;
        if(returnJson.size() > 0){
            JSONObject json = JSONObject.fromObject(returnJson.get(DATA));
            if (json !=null && json.get(USER_TOKEN)!= null){
                token = json.get(USER_TOKEN).toString();
            }
        }
        return  token;

    }


    /**
     * 测试获取游戏列表
     * /gameboy/game/list/:gamehall
     * @param id   游戏代码
     * @return
     * @throws Exception
     */
    private  static  final String GAMEURL= "http://api.cqgame.games/gameboy/game/list";
    public void getGamelist(String id)throws  Exception{
        HttpMethodParams params = new HttpMethodParams();
        params.setParameter("gamehall",id);
        JSONObject json =  sendGet(params,GAMEURL,"getGamelist");
        JSONArray returnJson = JSONArray.fromObject(json.get(DATA));
        for (int i =0;i<returnJson.size();i++){
            System.out.println(returnJson.get(i));
        }


    }



    /**
     * 获取游戏大厅链接
     * @param userToken 玩家Token
     * @return
     * @throws Exception
     */
    public Map<String,String> lobbylink(String userToken) throws Exception{
        logger.debug("lobbylink(String userToken = {}"+ userToken);
        if(StringUtils.isEmpty(userToken)){
            throw  new Exception("玩家ToKen不能为空！");
        }
        HttpMethodParams params  = new HttpMethodParams();
        Map<String,String>  returnMap = new HashMap<>();
        try{
            params.setParameter(USER_TOKEN,userToken);
            params.setParameter(LANG,"zh-cn");
            JSONObject returnJson =  sendPost(params,LOBBYLINK_URL,LOBBYLINK);
            logger.debug("sendPostReturn : "+ returnJson );
            if (returnJson.size()>0){
                JSONObject json = JSONObject.fromObject(returnJson.get(DATA));
                if (json != null && json.get("url")!= null){
                    returnMap.put("url",json.get("url").toString());
                    returnMap.put("token",json.get("token").toString());//游戏大厅token
                }
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            throw  e;
        }

        return  returnMap;


    }



    /**
     * 创建游戏玩家
     * @param account 用户账号  必传
     * @param psw     用户密码  必传
     * @return
     * @throws Exception
     */
    public Boolean createAccount(String account,String psw ) throws Exception {
        logger.debug("createAccount(String userName,String psw ,String nickName = {} -start"+ account + psw);
        if(StringUtils.isEmpty(account) || StringUtils.isEmpty(psw)){
            throw  new  Exception("用户参数数据空缺！");
        }
        boolean returnParam = false;
        HttpMethodParams params  = new HttpMethodParams();
        params.setParameter(ACCOUNT,account);
        params.setParameter(PASSWORD,psw);
        try{
            JSONObject returnJson =  sendPost(params,CREATE_URL,CREATE_ACCOUNT);
            logger.debug("returnJson:"+returnJson);
            if(returnJson.size() > 0){
                JSONObject json = JSONObject.fromObject(returnJson.get(STATUS));
                System.out.println(json.get(CODE));
                if (json.get(CODE).equals("0")){
                    returnParam = true;
                }
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            throw  e;
        }
        return  returnParam;
    }



    /**
     * 查询玩家是否已注册过账号
     * @param account 玩家账号
     * @return
     */
    public Boolean isAccount(String account) throws  Exception{
        logger.debug(" isAccount(String userName = {}  -start" + account);
        if(StringUtils.isEmpty(account)){
            throw  new  Exception("玩家账号不能为空！");
        }
        HttpMethodParams params = new HttpMethodParams();
        params.setParameter(ACCOUNT,account);
        JSONObject returnJson =  sendGet(params,IS_USER_URL,IS_ACCOUNT);
        Boolean returnParam = Boolean.FALSE;
        if (returnJson != null){
            if (returnJson.size()>0){
                returnParam = (boolean) returnJson.get("data");
            }
        }else{
            logger.debug("sendGet请求第三方接口出现异常！");
        }

        logger.debug("isAccount() end   return = "+ returnParam);
        return returnParam;
    }




    /**
     * 获取玩家Token
     * @param account 玩家账号
     * @return
     * @throws Exception
     */
    public  String getToken(String account,String password)throws  Exception{
        logger.debug("getToken(String account, String password = {}  -start" + account + password);
        if(StringUtils.isEmpty(account)){
            throw  new Exception("玩家账号不能为空！");
        }
        String toKen = queryToken(account);
        logger.debug("getToken   return:---"+ toKen);
        return  toKen;
    }

    private  String queryToken(String account) throws  Exception{
        HttpMethodParams params = new HttpMethodParams();
        params.setParameter(ACCOUNT,account);
        JSONObject returnJson = new JSONObject();
        try {
            returnJson = sendGet(params,TOKEN_URL,GET_TOKEN);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
        String toKen = null;
        if(returnJson.size()> 0){
            JSONObject json = JSONObject.fromObject(returnJson.get(DATA));
            toKen =json.get(USER_TOKEN).toString();
        }
        return  toKen;
    }
    /**
     *  发送get请求
     * @return
     * @throws Exception
     */
    public JSONObject sendGet(HttpMethodParams params,String url,String type) throws  Exception{
        logger.debug(" senGet(HttpMethodParams params = {} -start " + params + url + type);
        HttpClient client = new HttpClient();
        if(type.equals(FIND_ACCOUNT)||type.equals(IS_ACCOUNT)||type.equals(GET_TOKEN)){
            StringBuilder builderUrl = new StringBuilder(url).append("/"+params.getParameter(ACCOUNT));
            url = builderUrl.toString();
        }
        if (type.equals(QUERY_ORDER)){
            StringBuilder builderUrl = new StringBuilder(url).append("/"+params.getParameter(MTCODE));
            url = builderUrl.toString();
        }
        if(type.equals("getGamelist")){
            StringBuilder builderUrl = new StringBuilder(url).append("/"+params.getParameter("gamehall"));
            url = builderUrl.toString();
        }
        GetMethod get = new GetMethod(url);
        JSONObject retrunJson = new  JSONObject();
        try {
            get.setRequestHeader(CONTENT_TYPE, APPLICATION);
            get.setRequestHeader(CHARSET, UTF);
            get.setRequestHeader(AUTHORIZATION, TOKEN);

            get.setParams(params);
            int statusCode = client.executeMethod(get);
            if (statusCode == 200) {
                InputStream inputStream = get.getResponseBodyAsStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                String readStr;
                while ((readStr = br.readLine()) != null) {
                    retrunJson = JSONObject.fromObject(readStr);
                    // JSONObject json = JSONObject.fromObject(r_json.get("data"));
                }
            }else {
                logger.error("异常状态码："+ statusCode);
                //TODO 返回错误处理
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            throw  e;
        }finally {
            get.releaseConnection();
        }
        return  retrunJson;


    }


    /**
     * 发送post请求
     *
     * @return
     * @throws
     */
    public JSONObject sendPost(HttpMethodParams params ,String url,String type) throws Exception {
        logger.debug("sendPost(HttpMethodParams params,String url = {} -start "+ params + url + type);
        PostMethod postMethod = new PostMethod(url);
        HttpClient client = new HttpClient();
        JSONObject returnJson = new JSONObject();
        try {
            postMethod.setRequestHeader(AUTHORIZATION, TOKEN);
            postMethod.setRequestHeader(CONTENT_TYPE, APPLICATION);
            setPostParams(params,postMethod,type);
            int status = client.executeMethod(postMethod);
            if (status == 200) {
                InputStream inputStream = postMethod.getResponseBodyAsStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                String readStr;
                while ((readStr = br.readLine()) != null) {
                    InputStream InputStream = postMethod.getResponseBodyAsStream();
                    BufferedReader duff = new BufferedReader(new InputStreamReader(inputStream));
                    returnJson = JSONObject.fromObject(readStr);
                    // JSONObject json = JSONObject.fromObject(json.get("data"));
                }
            }else{
                logger.error("异常状态码："+ status);

                //TODO  异常处理
            }
        } catch(Exception e){
            logger.error(e.getMessage(),e);
            throw e;
        } finally{
            postMethod.releaseConnection();
        }
        return  returnJson;
    }


    private void  setPostParams(HttpMethodParams params,PostMethod  postMethod, String type){
        if(type.equals(DEPOSIT) || type.equals(WITHDRAW)){//转账
            postMethod.setParameter(ACCOUNT,params.getParameter(ACCOUNT).toString());
            postMethod.setParameter(MTCODE,params.getParameter(MTCODE).toString());
            postMethod.setParameter(AMOUNT,params.getParameter(AMOUNT).toString());
        }else if (type.equals(LOBBYLINK)){
            postMethod.setParameter(USER_TOKEN,params.getParameter(USER_TOKEN).toString());
            postMethod.setParameter(LANG,params.getParameter(LANG).toString());
        }else if(type.equals(CREATE_ACCOUNT) || type.equals(MEMBER_LOGIN)){
            postMethod.setParameter(ACCOUNT,params.getParameter(ACCOUNT).toString());
            postMethod.setParameter(PASSWORD,params.getParameter(PASSWORD).toString());
        }
    }


    /**
     * 游戏上分
     */
    @Override
    public JSONObject transferIn(GameTransferVO gameTransferVO) {
        // TODO Auto-generated method stub
        return null;
    }


    /**
     * 游戏下分
     */
    @Override
    public JSONObject transferOut(GameTransferVO gameTransferVO) {
        // TODO Auto-generated method stub
        return null;
    }


    /**
     * 跳转游戏
     */
    @Override
    public JSONObject forwardGame(GameForwardVO gameForwardVO) {
        // TODO Auto-generated method stub
        return null;
    }


    /**
     * 获取游戏余额
     */
    @Override
    public JSONObject getBalance(GameBalanceVO gameBalanceVO) {
        // TODO Auto-generated method stub
        return null;
    }


    /**
     * 检查或创建游戏账号
     */
    @Override
    public JSONObject checkOrCreateAccount(GameCheckOrCreateVO gameCheckOrCreateVO) {
        // TODO Auto-generated method stub
        return null;
    }


    /**
     * 查询转账订单
     */
    @Override
    public JSONObject queryTransferOrder(GameQueryOrderVO gameQueryOrderVO) {
        // TODO Auto-generated method stub
        return null;
    }


}
