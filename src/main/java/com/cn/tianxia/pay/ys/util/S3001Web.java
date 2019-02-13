
package com.cn.tianxia.pay.ys.util;
/**
 * 即时到账支付 以及直连网银  实体bean
 * @author chang
 *
 */
public class S3001Web implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private String method;
	private String partner_id;
	private String timestamp;
	private String charset;
	private String sign_type;
	private String notify_url;
	private String return_url;
	private String version;
	private String out_trade_no;
	private String trade_no;
	private String subject;
	private Double total_amount;
	private String out_request_no;
	private String seller_id;
	private String seller_name;
	private String timeout_express;
	private String pay_mode;
	private String bank_type;
	private String bank_account_type;
	private String support_card_type;
	private String extend_params;
	private String business_code;
	private String extra_common_param;
	private String biz_content;
	

	public String getBiz_content() {
		return biz_content;
	}

	public void setBiz_content(String biz_content) {
		this.biz_content = biz_content;
	}

	public String getMethod() {

		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getPartner_id() {

		return partner_id;
	}

	public void setPartner_id(String partner_id) {
		this.partner_id = partner_id;
	}

	public String getTimestamp() {

		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getCharset() {

		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public String getSign_type() {

		return sign_type;
	}

	public void setSign_type(String sign_type) {
		this.sign_type = sign_type;
	}


	public String getNotify_url() {

		return notify_url;
	}

	public void setNotify_url(String notify_url) {
		this.notify_url = notify_url;
	}

	public String getReturn_url() {

		return return_url;
	}

	public void setReturn_url(String return_url) {
		this.return_url = return_url;
	}

	public String getVersion() {

		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getOut_trade_no() {

		return out_trade_no;
	}

	public void setOut_trade_no(String out_trade_no) {
		this.out_trade_no = out_trade_no;
	}

	public String getTrade_no() {

		return trade_no;
	}

	public void setTrade_no(String trade_no) {
		this.trade_no = trade_no;
	}

	public String getSubject() {

		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Double getTotal_amount() {

		return total_amount;
	}

	public void setTotal_amount(Double total_amount) {
		this.total_amount = total_amount;
	}

	public String getOut_request_no() {

		return out_request_no;
	}

	public void setOut_request_no(String out_request_no) {
		this.out_request_no = out_request_no;
	}

	public String getSeller_id() {

		return seller_id;
	}

	public void setSeller_id(String seller_id) {
		this.seller_id = seller_id;
	}

	public String getSeller_name() {

		return seller_name;
	}

	public void setSeller_name(String seller_name) {
		this.seller_name = seller_name;
	}

	public String getTimeout_express() {

		return timeout_express;
	}

	public void setTimeout_express(String timeout_express) {
		this.timeout_express = timeout_express;
	}

	public String getPay_mode() {

		return pay_mode;
	}

	public void setPay_mode(String pay_mode) {
		this.pay_mode = pay_mode;
	}

	public String getBank_type() {

		return bank_type;
	}

	public void setBank_type(String bank_type) {
		this.bank_type = bank_type;
	}

	public String getBank_account_type() {

		return bank_account_type;
	}

	public void setBank_account_type(String bank_account_type) {
		this.bank_account_type = bank_account_type;
	}

	public String getSupport_card_type() {

		return support_card_type;
	}

	public void setSupport_card_type(String support_card_type) {
		this.support_card_type = support_card_type;
	}

	public String getExtend_params() {

		return extend_params;
	}

	public void setExtend_params(String extend_params) {
		this.extend_params = extend_params;
	}

	public String getBusiness_code() {

		return business_code;
	}

	public void setBusiness_code(String business_code) {
		this.business_code = business_code;
	}

	public String getExtra_common_param() {

		return extra_common_param;
	}

	public void setExtra_common_param(String extra_common_param) {
		this.extra_common_param = extra_common_param;
	}

	public static long getSerialversionuid() {

		return serialVersionUID;
	}

}
