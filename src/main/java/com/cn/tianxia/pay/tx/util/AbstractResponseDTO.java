package com.cn.tianxia.pay.tx.util;


//import com.sk.core.common.ExceptionCode;

public abstract class AbstractResponseDTO implements IResponseDTO {

	/** serialVersionUID */
	private static final long serialVersionUID = 8824712820746383054L;
	
    private String respCode = "0000";
    private String respMsg = "请求成功";
    
    public String getRespCode() {
        return respCode;
    }

    public void setRespCode(String respCode) {
        this.respCode = respCode;
    }


    public String getRespMsg() {
        return respMsg;
    }


    public void setRespMsg(String respMsg) {
        this.respMsg = respMsg;
    }


}
