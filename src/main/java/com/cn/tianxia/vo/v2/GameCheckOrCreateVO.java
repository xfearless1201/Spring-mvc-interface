package com.cn.tianxia.vo.v2;

import java.io.Serializable;

/**
 * @ClassName GameCheckOrCreateVO
 * @Description 游戏检查或创建用户账号VO类
 * @author Hardy
 * @Date 2019年2月9日 下午4:48:26
 * @version 1.0.0
 */
public class GameCheckOrCreateVO implements Serializable {

    private static final long serialVersionUID = 6653355225984348682L;

    private String uid;// 用户ID

    private String gamename;// 游戏登录账号

    private String password;// 游戏登录密码

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
