package com.cn.tianxia.vo.v2;

import java.io.Serializable;

/**
 * @ClassName GameBalanceVO
 * @Description 游戏余额VO类
 * @author Hardy
 * @Date 2019年2月9日 下午4:41:54
 * @version 1.0.0
 */
public class GameBalanceVO implements Serializable {

    private static final long serialVersionUID = 2988735306543681431L;

    private String uid;// 用户名

    private String gamename;// 游戏名称

    private String password;// 游戏密码

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getGamename() {
        return gamename;
    }

    public void setGamename(String gamename) {
        this.gamename = gamename;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
