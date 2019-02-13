package com.cn.tianxia.vo.v2;

import java.io.Serializable;

/**
 * @ClassName GameTransferVO
 * @Description 游戏转账VO类
 * @author Hardy
 * @Date 2019年2月9日 下午4:20:29
 * @version 1.0.0
 */
public class GameTransferVO implements Serializable {

    private static final long serialVersionUID = -823355629857507468L;

    private String uid;// 用户ID

    private String username;// 用户名

    private String ag_username;// 游戏登录账号

    private String hg_username;// 皇冠体育

    private String password;// 游戏登录密码

    private String money;// 订单金额

    private String billno;// 订单号

    private String type;// 游戏平台编码
    
    private String ip;//ip地址

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAg_username() {
        return ag_username;
    }

    public void setAg_username(String ag_username) {
        this.ag_username = ag_username;
    }

    public String getHg_username() {
        return hg_username;
    }

    public void setHg_username(String hg_username) {
        this.hg_username = hg_username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public String getBillno() {
        return billno;
    }

    public void setBillno(String billno) {
        this.billno = billno;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    
    public String getIp() {
        return ip;
    }

    
    public void setIp(String ip) {
        this.ip = ip;
    }

    
}
