package com.cn.tianxia.pay.qft.util;

import java.util.Date;

/**
 * @author liuxingmi
 * @datetime 2017-07-27 14:23
 * @Desc 支付请求接口
 */
public class ReqPayCodeVo {

    /**
     * 商户号
     */
    private String merchantNo;

    /**
     * 商户订单号
     */
    private String orderNo;
    
    /**
     * 应用号
     */ 
    private String appNo;

    /**
     * 异步通知地址
     */
    private String noticeUrl;

    /**
     * 同步通知地址
     */
    private String rsynNoticeUrl;
    
    /**
     * 商品名称
     */
    private String goodsTitle;
    
    /**
     * 商品描述
     */
    private String goodsDesc;
    
    /**
     * 货币代码（RMB）
     */
    private String  currency;

    /**
     * 支付金额(单位：分)
     */
    private Long amount;

    /**
     * 微信开发id，微信公众账号支付专用
     */
    private String wxOpenId;

    /**
     * 支付方式
     */
    private String payType;


    /**
     * 支付时间（格式：yyyy-MM-dd HH:mm:ss）
     */
    private Date payTime;
    
    /**
     * 时间戳（yyyyMMddHHmmssSSS）
     */
    private Long timestamp;

    /**
     * 签名串
     */
    private String sign;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 随机字符串
     */
    private String randomStr;

    public String getMerchantNo() {
        return merchantNo;
    }

    public void setMerchantNo(String merchantNo) {
        this.merchantNo = merchantNo;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getWxOpenId() {
        return wxOpenId;
    }

    public void setWxOpenId(String wxOpenId) {
        this.wxOpenId = wxOpenId;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }
  

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getAppNo() {
		return appNo;
	}

	public void setAppNo(String appNo) {
		this.appNo = appNo;
	}


	public String getNoticeUrl() {
		return noticeUrl;
	}

	public void setNoticeUrl(String noticeUrl) {
		this.noticeUrl = noticeUrl;
	}

	public String getRsynNoticeUrl() {
		return rsynNoticeUrl;
	}

	public void setRsynNoticeUrl(String rsynNoticeUrl) {
		this.rsynNoticeUrl = rsynNoticeUrl;
	}

	public String getGoodsTitle() {
		return goodsTitle;
	}

	public void setGoodsTitle(String goodsTitle) {
		this.goodsTitle = goodsTitle;
	}

	public String getGoodsDesc() {
		return goodsDesc;
	}

	public void setGoodsDesc(String goodsDesc) {
		this.goodsDesc = goodsDesc;
	}

	public Long getAmount() {
		return amount;
	}

	public void setAmount(Long amount) {
		this.amount = amount;
	}

	public Date getPayTime() {
		return payTime;
	}

	public void setPayTime(Date payTime) {
		this.payTime = payTime;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

	public String getRandomStr() {
		return randomStr;
	}

	public void setRandomStr(String randomStr) {
		this.randomStr = randomStr;
	}

}
