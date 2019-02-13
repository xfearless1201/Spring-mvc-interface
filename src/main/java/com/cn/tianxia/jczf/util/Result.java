package com.cn.tianxia.jczf.util;

/**
 * Created on 2018/1/19.
 */
public class Result {

  public static final String STATUS_OK = "1"; // 请求成功或订单成功
  public static final String STATUS_PRE = "2"; // 订单为正在处理中或预处理
  public static final String STATUS_FAIL = "0"; // 请求失败或订单失败

  private String status;

  private String message;

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

}
