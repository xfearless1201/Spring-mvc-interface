package com.cn.tianxia.mq.vo;

public class GameTransferVO extends MQBaseVO {

    private static final long serialVersionUID = -9032498610011640080L;

    private String biilno;// 转账订单号

    private String uid;// 用户ID

    private Integer id;// 订单号

    public String getBiilno() {
        return biilno;
    }

    public void setBiilno(String biilno) {
        this.biilno = biilno;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "GameTransferVO [biilno=" + biilno + ", uid=" + uid + ", id=" + id + "]";
    }

}
