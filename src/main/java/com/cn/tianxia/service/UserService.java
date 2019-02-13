package com.cn.tianxia.service;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.springframework.context.annotation.Scope;

import com.cn.tianxia.common.PayEntity;

import net.sf.json.JSONObject;

/**
 * 功能概要：UserService接口类
 * 
 */
public interface UserService {

	Map<String, Object> selectUserById(Map<String, Object> map);

	Map<String, Object> selectUserInfo(Map<String, Object> map);

	List<Map<String, Object>> selectUserByUserName(String userName);

	List<Map<String, Object>> selectDisUserByUserName(String userName);

	List<Map<String, Object>> UserLogin(String userName, String passWord);

	List<Map<String, String>> checkkpwd(Map<String, Object> map);

	void insertUser(Map<String, Object> map);

	void updateMoney(Map<String, Object> map);
	void updateMgUserName(Map<String, Object> map);
	void updateTransferPro(Map<String, Object> map);

	void updateGame(Map<String, Object> map);

	void insertTransfer(Map<String, Object> map);

	void insertLogin(Map<String, Object> map);

	List<Map<String, String>> selectTransferCount(Map<String, Object> map);

	List<Map<String, String>> selectTransferInfo(Map<String, Object> map);

	void insertRechange(Map<String, Object> map);
	
	void insertErrorRechange(Map<String, Object> map);

	List<Map<String, String>> selectReChargeCount(Map<String, Object> map);

	List<Map<String, String>> selectReChargeInfo(Map<String, Object> map);

	void insertUserCard(Map<String, Object> map);

	void deleteUserCard(Map<String, Object> map);

	List<Map<String, String>> selectUserCard(Map<String, Object> map);

	void UpdateRechange(Map<String, Object> map);

	void insertUserTreasure(Map<String, Object> map);

	List<Map<String, String>> selectWithDrawCount(Map<String, Object> map);

	List<Map<String, String>> selectWithDrawInfo(Map<String, Object> map);

	List<Map<String, String>> selectPlatFromInfo(String KEY);

	List<Map<String, String>> selectWebCom(String type, String cagent);

	List<Map<String, String>> selectRefererUrl(String domain, String cagent);

	void insertLoginMap(Map<String, String> map);

	List<Map<String, String>> selectLoginMap(Map<String, String> map);

	void deleteLoginMap(String sessionid);

	List<Map<String, Object>> selectReserveAccount(String userName, String cagent);

	void insertLoginErrorMap(String username);

	List<Map<String, Object>> selectLoginErrorMap(String username);

	void deleteLoginErrorMap(String username);

	List<Map<String, Object>> selectMessageRead(@Param("uid") String uid, @Param("bdate") String bdate,
			@Param("edate") String edate);

	List<Map<String, Object>> selectMessageByStatus(@Param("uid") String uid, @Param("status") String status,
			@Param("bdate") String bdate, @Param("edate") String edate);

	List<Map<String, Object>> selectMessageInfo(@Param("uid") String uid, @Param("id") String id);

	void updateMessageInfo(@Param("uid") String uid, @Param("id") String id);

	void deleteMessage(@Param("uid") String uid, @Param("id") String id);

	List<Map<String, Object>> getProxyUser(@Param("proxyname") String proxyname, @Param("cagent") String cagent);

	List<Map<String, Object>> getJuniorProxyUser(@Param("proxyname") String proxyname, @Param("cagent") String cagent);

	List<Map<String, String>> getYsepayConfig(@Param("username") String username);

	List<Map<String, String>> getBankPayConfig(@Param("uid") String uid, @Param("bid") String bid);

	// 用户在线存款
	void userOnlineDeposit(Map<String, Object> map);

	// 扣储值额度
	void updateCagentStoredvalue(Map<String, Object> map);

	// 扣储值额度记录
	void insertStoredvalueLog(Map<String, Object> map);

	// 用户提款
	void insertWithdraw(Map<String, Object> map);

	// 查询用户卡号
	Map<String, String> selectUserCardNum(Integer uid);

	// 查询用户提款次数
	Map<String, String> queryTotaltimes(Integer uid);

	// 查询用户存款次数
	Integer queryDeposittimes(Integer uid);

	// 查询平台彩金优惠打码比例
	Map<String, Object> queryWithdrawConfig(String cagent);

	// 汇总平台一天总存款
	String sumTodayWithdraw(Map<String, Object> map);

	// 新增或者修改当前打码量
	Integer insertORUpdate(Map<String, Object> map);

	// 查询会员打码量总游戏金额强制提款手续费
	Map<String, Object> selectWithdrawConfig(Integer uid);

	// 查询网站配置
	List<Map<String, String>> selectWebComConfig(Map<String, String> map);

	// 查询网站文档配置
	Map<String, String> selectWebTexttMap(Map<String, String> map);

	List<Map<String, String>> selectUserGameStatus(Map<String, Object> map);

	void insertUserGameStatus(Map<String, Object> map);

	// 分页查询用户资金流水
	JSONObject queryByTreasurePage(Map<String, Object> map);

	List<Map<String, String>> selectReChargeStatus(Map<String, Object> map);

	List<Map<String, String>> selectDemoAccount(Map<String, Object> map);

	void insertDemoAccount(Map<String, Object> map);

	// 查询BG注单
	List<Map<String, String>> selectBGRecord(Map<String, Object> map);

	Integer InserterBGRecord(Map<String, Object> map);

	Map<String, Object> selectUserByAgUserName(Map<String, Object> map);

	// 确认订单是否存在
	List<Map<String, String>> selectChickReCharge(Map<String, Object> map);

	// 保存回调记录
	void InsertCallbacklog(Map<String, Object> map);

	// 支付回调
	void ysePayCallBack(Map<String, Object> map);

	// 转账错误记录
	void insertTransferFaild(String uid, String billno, String username, String t_type, String t_money, String type,
							 String ip, String result);

	// 更新错误记录
	void updateTransferFaild(String uid, String billno, String username, String t_type, String t_money, String type,
							 String ip, String result);
	// 获取在线会员配置地址
	List<Map<String, String>> selectCagentOnlineMem(@Param("cagent") String cagent);

	List<Map<String, String>> selectPlatformCagent(@Param("uid") String uid);

	// 根据用户获取代理平台
	List<Map<String, String>> selectCagentByUid(@Param("uid") String uid);

	// 查询用户分层
	List<Map<String, String>> selectUserTypeById(Map<String, Object> map);

	// 查询用户分层支付列表
	List<Map<String, String>> selectUserPaymentList(Map<String, Object> map);

	// 根据id查询支付商
	List<Map<String, String>> selectYsepaybyId(@Param("pid") String pid,@Param("uid") String iid);

	// <!--查询t_proxy_user user_name-->
	List<Map<String, String>> selectProxyByName(Map<String, Object> map);

	// <!--查询selectUserQuantityByid-->
	Map<String, String> selectUserQuantityByid(@Param("uid") String uid);

	List<Map<String, String>> selectTcagentYsepay(String paymentName);

	int updatePersonalInformation(Map<String, Object> map);

	// 查询用户取款审核状态
	@Scope("prototype")
	int selectWithDrawStatusCount(@Param("uid") int uid);

	// 查询用户分层盘口设置
	@Scope("prototype")
	Map<String, Object> selectUserTypeHandicap(@Param("game") String game, @Param("uid") String uid);

	// 查询短信平台配置
	List<Map<String, String>> selectMsgconfig(String cagent);

	// 保存短信发送记录
	void InsertMsgLog(String cagent, String mname, String mobileno, String msg, String type, String domain);

	// 查询短信发送记录
	List<Map<String, String>> selectMsgLog(String cagent, String mobileno, String type, String sendtime);

	// 更新短信状态
	void updateMsgValue(String id, String status);

	// 检测手机号是否存在
	List<Map<String, Object>> selectUserByMobileNo(String cagent, String mobileNo);

	// 检测游离表中的手机号是否存在
	List<Map<String, Object>> selectDisUserByMobileNo(String cagent, String mobileNo);

	// 手机号登录
	List<Map<String, Object>> UserLoginByMobile(String cagent, String mobileNo, String password);

	// 手机绑定记录
	int insertMobileLog(Map<String, Object> map);

	// 当日只能更改一次绑定
	List<Map<String, Object>> selectMobileLog(@Param("uid") String uid);

	// 检查取款密码
	List<Map<String, Object>> selectQkpwdCheck(@Param("uid") String uid);

	// 获取当前用户未审批订单数
	int getOrderCount(Map<String, Object> userMap);

	// 修改订单金额
	int UpdateRechangeMoney(String orderNo, String amount);

	// 用户可用支付渠道
	Map<String, String> selectUserPaychannel(Map<String, Object> map);

	// 平台可用额度
	Map<String, String> selectCagentQuota(@Param("cagent") String cagent);
	

    //保存pstaoken
    int insertPSToken(@Param("auth") String auth,@Param("step") int step,@Param("uid") String uid);
    
    //PS游戏查询token
    Map<String, String> selectPSByauth(@Param("auth") String auth);
    
    //PS游戏token状态
    void UpdatePSToken(@Param("auth") String auth,@Param("step") int step);
    
    //查询订单是状态
  	int selectOrderNoStatus(Map<String, Object> map);

	Map<String, String> getContactInfo(Map<String, Object> map);

    List<Map<String,Object>> getProxyUserByrefererCode(String referralCode);

	List<Map<String,Object>> getJuniorProxyUserByrefererCode(String referralCode);
	
	/**
	 * 
	 * @Description 查询用户的基本信息、所属平台信息、平台剩余额度等信息
	 * @param uid
	 * @return
	 */
	Map<String,Object> findUserInfoByUid(Integer uid);
	
	/**
	 * 
	 * @Description 创建充值订单
	 * @param payEntity
	 * @return
	 */
	int createTopUpRecharge(PayEntity payEntity);
}
