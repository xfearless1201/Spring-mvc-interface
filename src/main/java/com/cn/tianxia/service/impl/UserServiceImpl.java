package com.cn.tianxia.service.impl;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.cn.tianxia.common.PayEntity;
import com.cn.tianxia.dao.UserDao;
import com.cn.tianxia.service.UserService;
import com.cn.tianxia.util.DESEncrypt;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 功能概要：UserService实现类
 */
@Service("userServiceImpl")
public class UserServiceImpl implements UserService {

    @Resource
    private UserDao userDao;
    
    private String deskey = "tianxia88";

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Map<String, Object> selectUserById(Map<String, Object> map) {
        return userDao.selectUserById(map);

    }

    @Override
    public List<Map<String, Object>> selectUserByUserName(String userName) {
        List<Map<String, Object>> list = userDao.selectUserByUserName(userName, "");
        return list;
    }

    @Override
    public List<Map<String, Object>> selectDisUserByUserName(String userName) {
        List<Map<String, Object>> list = userDao.selectDisUserByUserName(userName, "");
        return list;
    }

    @Override
    public List<Map<String, Object>> UserLogin(String userName, String passWord) {
        DESEncrypt d = new DESEncrypt(deskey);
        try {
            passWord = d.encrypt(passWord);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<Map<String, Object>> list = userDao.selectUserByUserName(userName, passWord);
        return list;
    }

    @Override
    public void insertUser(Map<String, Object> map) {
        DESEncrypt d = new DESEncrypt(deskey);
        try {
            String pwd = map.get("password").toString();
            String agpwd = map.get("ag_password").toString();
            pwd = d.encrypt(pwd);
            agpwd = d.encrypt(agpwd);
            map.put("password", pwd);
            map.put("ag_password", agpwd);

            pwd = map.get("qkpwd").toString();
            if (StringUtils.isEmpty(pwd)) {
                map.put("qkpwd", pwd);
            } else {
                pwd = d.encrypt(pwd);
                map.put("qkpwd", pwd);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        userDao.insertUser(map);
    }

    @Override
    public void updateMoney(Map<String, Object> map) {
        userDao.updateMoney(map);
    }

    @Override
    public void updateMgUserName(Map<String, Object> map) {
        userDao.updateMgUserName(map);
    }

    @Override
    public void updateGame(Map<String, Object> map) {
        userDao.updateGame(map);
    }

    @Override
    public void insertTransfer(Map<String, Object> map) {
        userDao.insertTransfer(map);
    }

    @Override
    public void insertLogin(Map<String, Object> map) {
        userDao.insertLogin(map);
    }

    @Override
    public List<Map<String, String>> selectTransferCount(Map<String, Object> map) {
        return userDao.selectTransferCount(map);
    }

    @Override
    public List<Map<String, String>> selectTransferInfo(Map<String, Object> map) {
        return userDao.selectTransferInfo(map);
    }

    @Override
    public void insertRechange(Map<String, Object> map) {
        userDao.insertRechange(map);
    }

    @Override
    public void insertErrorRechange(Map<String, Object> map) {
        userDao.insertErrorRechange(map);
    }

    @Override
    public List<Map<String, String>> selectReChargeCount(Map<String, Object> map) {
        return userDao.selectReChargeCount(map);
    }

    @Override
    public List<Map<String, String>> selectReChargeInfo(Map<String, Object> map) {
        return userDao.selectReChargeInfo(map);
    }

    @Override
    public void insertUserCard(Map<String, Object> map) {
        userDao.insertUserCard(map);
    }

    @Override
    public void deleteUserCard(Map<String, Object> map) {
        userDao.deleteUserCard(map);
    }

    @Override
    public List<Map<String, String>> selectUserCard(Map<String, Object> map) {
        return userDao.selectUserCard(map);
    }

    @Override
    public List<Map<String, String>> checkkpwd(Map<String, Object> map) {
        return userDao.checkkpwd(map);
    }

    @Override
    public void UpdateRechange(Map<String, Object> map) {
        userDao.UpdateRechange(map);
    }

    @Override
    public void insertUserTreasure(Map<String, Object> map) {
        userDao.insertUserTreasure(map);
    }

    @Override
    public List<Map<String, String>> selectWithDrawCount(Map<String, Object> map) {
        return userDao.selectWithDrawCount(map);
    }

    @Override
    public List<Map<String, String>> selectWithDrawInfo(Map<String, Object> map) {
        return userDao.selectWithDrawInfo(map);
    }

    @Override
    public Map<String, Object> selectUserInfo(Map<String, Object> map) {
        return userDao.selectUserInfo(map);
    }

    @Override
    public List<Map<String, String>> selectPlatFromInfo(String KEY) {
        return userDao.selectPlatFromInfo(KEY);
    }

    @Override
    public List<Map<String, String>> selectWebCom(String type, String cagent) {
        return userDao.selectWebCom(type, cagent);
    }

    @Override
    public List<Map<String, String>> selectRefererUrl(String domain, String cagent) {

        return userDao.selectRefererUrl(domain, cagent);
    }

    @Override
    public void insertLoginMap(Map<String, String> map) {
        userDao.insertLoginMap(map);
    }

    @Override
    public List<Map<String, String>> selectLoginMap(Map<String, String> map) {
        return userDao.selectLoginMap(map);
    }

    @Override
    public List<Map<String, Object>> selectReserveAccount(String userName, String cagent) {
        return userDao.selectReserveAccount(userName, cagent);
    }

    @Override
    public void insertLoginErrorMap(String username) {
        userDao.insertLoginErrorMap(username);
    }

    @Override
    public List<Map<String, Object>> selectLoginErrorMap(String username) {
        return userDao.selectLoginErrorMap(username);
    }

    @Override
    public void deleteLoginErrorMap(String username) {
        userDao.deleteLoginErrorMap(username);
    }

    @Override
    public List<Map<String, Object>> selectMessageRead(String uid, String bdate, String edate) {
        return userDao.selectMessageRead(uid, bdate, edate);
    }

    @Override
    public List<Map<String, Object>> selectMessageByStatus(String uid, String status, String bdate, String edate) {
        return userDao.selectMessageByStatus(uid, status, bdate, edate);
    }

    @Override
    public List<Map<String, Object>> selectMessageInfo(String uid, String id) {
        return userDao.selectMessageInfo(uid, id);
    }

    @Override
    public void updateMessageInfo(String uid, String id) {
        userDao.updateMessageInfo(uid, id);
    }

    @Override
    public void deleteMessage(String uid, String id) {
        userDao.deleteMessage(uid, id);
    }

    @Override
    public List<Map<String, Object>> getProxyUser(String proxyname, String cagent) {
        return userDao.getProxyUser(proxyname, cagent);
    }

    @Override
    public List<Map<String, Object>> getJuniorProxyUser(String proxyname, String cagent) {
        return userDao.getJuniorProxyUser(proxyname, cagent);
    }

    @Override
    public void deleteLoginMap(String sessionid) {
        userDao.deleteLoginMap(sessionid);
    }

    @Override
    public List<Map<String, String>> getYsepayConfig(String username) {
        return userDao.getYsepayConfig(username);
    }

    @Override
    public List<Map<String, String>> getBankPayConfig(String uid, String bid) {
        return userDao.getBankPayConfig(uid, bid);
    }

    @Override
    public void userOnlineDeposit(Map<String, Object> map) {
        userDao.userOnlineDeposit(map);
    }

    @Override
    public void updateCagentStoredvalue(Map<String, Object> map) {
        userDao.updateCagentStoredvalue(map);
    }

    @Override
    public void insertStoredvalueLog(Map<String, Object> map) {
        userDao.insertStoredvalueLog(map);
    }

    @Override
    public void insertWithdraw(Map<String, Object> map) {
        userDao.insertWithdraw(map);
    }

    @Override
    public Map<String, String> selectUserCardNum(Integer uid) {
        return userDao.selectUserCardNum(uid);
    }

    @Override
    public Map<String, String> queryTotaltimes(Integer uid) {
        return userDao.queryTotaltimes(uid);
    }

    @Override
    public Integer queryDeposittimes(Integer uid) {
        return userDao.queryDeposittimes(uid);
    }

    @Override
    public Map<String, Object> queryWithdrawConfig(String cagent) {
        return userDao.queryWithdrawConfig(cagent);
    }

    @Override
    public String sumTodayWithdraw(Map<String, Object> map) {
        return userDao.sumTodayWithdraw(map);
    }

    @Override
    public Integer insertORUpdate(Map<String, Object> map) {
        return userDao.insertORUpdate(map);
    }

    @Override
    public Map<String, Object> selectWithdrawConfig(Integer uid) {
        return userDao.selectWithdrawConfig(uid);
    }

    @Override
    public List<Map<String, String>> selectWebComConfig(Map<String, String> map) {
        return userDao.selectWebComConfig(map);
    }

    @Override
    public Map<String, String> selectWebTexttMap(Map<String, String> map) {
        return userDao.selectWebTexttMap(map);
    }

    @Override
    public List<Map<String, String>> selectUserGameStatus(Map<String, Object> map) {
        return userDao.selectUserGameStatus(map);
    }

    @Override
    public void insertUserGameStatus(Map<String, Object> map) {
        userDao.insertUserGameStatus(map);
    }

    @Override
    public JSONObject queryByTreasurePage(Map<String, Object> map) {
        JSONObject json = new JSONObject();

        List<Map<String, String>> treasure = userDao.queryByTreasurePage(map);
        Double total = userDao.queryByTreasurePageCount(map);

        // 每页小计
        double subTotal = 0.0;
        for (int i = 0; i < treasure.size(); i++) {
            Map<String, String> item = treasure.get(i);
            String t_money = String.valueOf(item.get("amount"));
            Double money = Double.valueOf(t_money);
            subTotal += money;
        }
        DecimalFormat df = new DecimalFormat("0.00");
        json.put("total", total == null ? "0" : df.format(total));
        json.put("subTotal", df.format(subTotal));

        json.put("data", JSONArray.fromObject(treasure));
        return json;
    }

    @Override
    public void updateTransferPro(Map<String, Object> map) {
        userDao.updateTransferPro(map);
    }

    @Override
    public List<Map<String, String>> selectReChargeStatus(Map<String, Object> map) {
        return userDao.selectReChargeStatus(map);
    }

    @Override
    public List<Map<String, String>> selectDemoAccount(Map<String, Object> map) {
        return userDao.selectDemoAccount(map);
    }

    @Override
    public void insertDemoAccount(Map<String, Object> map) {
        userDao.insertDemoAccount(map);
    }

    @Override
    public List<Map<String, String>> selectBGRecord(Map<String, Object> map) {
        return userDao.selectBGRecord(map);
    }

    @Override
    public Integer InserterBGRecord(Map<String, Object> map) {
        return userDao.InserterBGRecord(map);
    }

    @Override
    public Map<String, Object> selectUserByAgUserName(Map<String, Object> map) {
        return userDao.selectUserByAgUserName(map);
    }

    @Override
    public List<Map<String, String>> selectChickReCharge(Map<String, Object> map) {
        return userDao.selectChickReCharge(map);
    }

    @Override
    public void InsertCallbacklog(Map<String, Object> map) {
        userDao.InsertCallbacklog(map);
    }

    @Override
    public void ysePayCallBack(Map<String, Object> map) {
        userDao.ysePayCallBack(map);
    }

    @Override
    public void insertTransferFaild(String uid, String billno, String username, String t_type, String t_money,
            String type, String ip, String result) {
        userDao.insertTransferFaild(uid, billno, username, t_type, t_money, type, ip, result);
    }

    @Override
    public void updateTransferFaild(String uid, String billno, String username, String t_type, String t_money,
            String type, String ip, String result) {
        userDao.updateTransferFaild(uid, billno, username, t_type, t_money, type, ip, result);
    }

    @Override
    public List<Map<String, String>> selectCagentOnlineMem(String cagent) {
        return userDao.selectCagentOnlineMem(cagent);
    }

    @Override
    public List<Map<String, String>> selectPlatformCagent(String uid) {
        return userDao.selectPlatformCagent(uid);
    }

    @Override
    public List<Map<String, String>> selectCagentByUid(String uid) {
        return userDao.selectCagentByUid(uid);
    }

    @Override
    public List<Map<String, String>> selectUserTypeById(Map<String, Object> typeMap) {
        return userDao.selectUserTypeById(typeMap);
    }

    @Override
    public List<Map<String, String>> selectUserPaymentList(Map<String, Object> map) {
        return userDao.selectUserPaymentList(map);
    }

    @Override
    public List<Map<String, String>> selectYsepaybyId(String pid, String uid) {
        return userDao.selectYsepaybyId(pid, uid);
    }

    @Override
    public List<Map<String, String>> selectProxyByName(Map<String, Object> map) {
        return userDao.selectProxyByName(map);
    }

    @Override
    public Map<String, String> selectUserQuantityByid(String uid) {
        return userDao.selectUserQuantityByid(uid);
    }

    @Override
    public List<Map<String, String>> selectTcagentYsepay(String paymentName) {
        return userDao.selectTcagentYsepay(paymentName);
    }

    @Override
    public int updatePersonalInformation(Map<String, Object> map) {
        return userDao.updatePersonalInformation(map);
    }

    @Override
    public int selectWithDrawStatusCount(int uid) {
        return userDao.selectWithDrawStatusCount(uid);
    }

    @Override
    public Map<String, Object> selectUserTypeHandicap(String game, String uid) {
        return userDao.selectUserTypeHandicap(game, uid);
    }

    @Override
    public List<Map<String, String>> selectMsgconfig(String cagent) {
        return userDao.selectMsgconfig(cagent);
    }

    @Override
    public void InsertMsgLog(String cagent, String mname, String mobileno, String msg, String type, String domain) {
        userDao.InsertMsgLog(cagent, mname, mobileno, msg, type, domain);
    }

    @Override
    public List<Map<String, String>> selectMsgLog(String cagent, String mobileno, String type, String sendtime) {
        return userDao.selectMsgLog(cagent, mobileno, type, sendtime);
    }

    @Override
    public void updateMsgValue(String id, String status) {
        userDao.updateMsgValue(id, status);
    }

    @Override
    public List<Map<String, Object>> selectUserByMobileNo(String cagent, String mobileNo) {
        return userDao.selectUserByMobileNo(cagent, mobileNo, null);
    }

    @Override
    public List<Map<String, Object>> selectDisUserByMobileNo(String cagent, String mobileNo) {
        return userDao.selectDisUserByMobileNo(cagent, mobileNo, null);
    }

    @Override
    public List<Map<String, Object>> UserLoginByMobile(String cagent, String mobileNo, String password) {
        DESEncrypt d = new DESEncrypt(deskey);
        try {
            password = d.encrypt(password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userDao.selectUserByMobileNo(cagent, mobileNo, password);
    }

    @Override
    public int insertMobileLog(Map<String, Object> map) {
        return userDao.insertMobileLog(map);
    }

    @Override
    public List<Map<String, Object>> selectMobileLog(String uid) {
        return userDao.selectMobileLog(uid);
    }

    @Override
    public List<Map<String, Object>> selectQkpwdCheck(String uid) {
        return userDao.selectQkpwdCheck(uid);
    }

    @Override
    public int getOrderCount(Map<String, Object> userMap) {
        // TODO Auto-generated method stub
        return userDao.getOrderCount(userMap);
    }

    @Override
    public int UpdateRechangeMoney(String orderNo, String amount) {
        return userDao.UpdateRechangeMoney(orderNo, amount);
    }

    @Override
    public Map<String, String> selectUserPaychannel(Map<String, Object> map) {
        return userDao.selectUserPaychannel(map);
    }

    @Override
    public Map<String, String> selectCagentQuota(String cagent) {
        return userDao.selectCagentQuota(cagent);
    }

    @Override
    public int insertPSToken(String auth, int step, String uid) {
        return userDao.insertPSToken(auth, step, uid);
    }

    @Override
    public Map<String, String> selectPSByauth(String auth) {
        return userDao.selectPSByauth(auth);
    }

    @Override
    public void UpdatePSToken(String auth, int step) {
        userDao.UpdatePSToken(auth, step);
    }

    @Override
    public int selectOrderNoStatus(Map<String, Object> map) {
        return userDao.selectOrderNoStatus(map);
    }

    @Override
    public Map<String, String> getContactInfo(Map<String, Object> map) {
        return userDao.getContactInfo(map);
    }

    @Override
    public List<Map<String, Object>> getProxyUserByrefererCode(String referralCode) {
        return userDao.getProxyUserByrefererCode(referralCode);
    }

    @Override
    public List<Map<String, Object>> getJuniorProxyUserByrefererCode(String referralCode) {
        return userDao.getJuniorProxyUserByrefererCode(referralCode);
    }

    /**
     * 查询用户的基本信息、所属平台信息、平台剩余额度等信息
     */
    @Override
    public Map<String, Object> findUserInfoByUid(Integer uid) {
        return userDao.findUserInfoByUid(uid);
    }

    /**
     * 创建充值订单信息
     */
    @Override
    public int createTopUpRecharge(PayEntity payEntity) {
        return userDao.createTopUpRecharge(payEntity);
    }

}
