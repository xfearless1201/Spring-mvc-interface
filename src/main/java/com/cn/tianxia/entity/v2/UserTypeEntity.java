package com.cn.tianxia.entity.v2;

import java.io.Serializable;
import java.util.Date;

public class UserTypeEntity implements Serializable{
    
    private static final long serialVersionUID = 775398727647139775L;

    private Integer id;

    private Integer cid;

    private String typename;

    private Date updatetime;

    private String rmk;

    private String status;

    private String isdefault;

    private Float pCg;

    private Float pAg;

    private Float pShenbogame;

    private Float pShenbo;

    private Float pAb;

    private Float pDs;

    private Float pOg;

    private Float pBbin;

    private Float pBbingame;

    private Float pMg;

    private Float pPt;

    private Float pHaba;

    private Float pIg;

    private Float pHg;

    private Float pGgby;

    private Float pBgvideo;

    private Float pBglottery;

    private Integer bankcardId;

    private String alipayId;

    private String wechatId;

    private String tenpayId;

    private String onlinepayId;

    private Float integralRatio;

    private Float cIntegralRatio;

    private Float pHkig;

    private Float pVr;

    private Float pJf;

    private Float pJfcp;

    private Float pYorplay;

    private Float pKyqp;

    private Float pSpta;

    private Float pVgqp;

    private String paymentChannel;

    private Float jfAgsx;

    private Float jfAgby;

    private Float jfBbsx;

    private Float jfBbdz;

    private Float jfCgsx;

    private Float jfDssx;

    private Float jfGgby;

    private Float jfHbdz;

    private Float jfHgty;

    private Float jfIgctc;

    private Float jfMgdz;

    private Float jfObsx;

    private Float jfOgsx;

    private Float jfPtdz;

    private Float jfSbsx;

    private Float jfSbdz;

    private Float jfIgxgc;

    private Float jfIgpjctc;

    private Float jfIgpjxgc;

    private Float jfVrcp;

    private Float jfYoplaydz;

    private Float jfKyqp;

    private Float jfSpta;

    private Float jfVgqp;

    private Float pGycp;

    private Float jfGycp;

    private Float pPs;

    private Float jfPs;

    private Float pNb;

    private Float jfNb;

    private Float pLyqp;

    private Float jfLyqp;

    private Float pJdb;

    private Float jfJdb;

    private Float pSw;

    private Float jfSw;

    private Float pIbc;

    private Float jfIbc;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCid() {
        return cid;
    }

    public void setCid(Integer cid) {
        this.cid = cid;
    }

    public String getTypename() {
        return typename;
    }

    public void setTypename(String typename) {
        this.typename = typename == null ? null : typename.trim();
    }

    public Date getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }

    public String getRmk() {
        return rmk;
    }

    public void setRmk(String rmk) {
        this.rmk = rmk == null ? null : rmk.trim();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public String getIsdefault() {
        return isdefault;
    }

    public void setIsdefault(String isdefault) {
        this.isdefault = isdefault == null ? null : isdefault.trim();
    }

    public Float getpCg() {
        return pCg;
    }

    public void setpCg(Float pCg) {
        this.pCg = pCg;
    }

    public Float getpAg() {
        return pAg;
    }

    public void setpAg(Float pAg) {
        this.pAg = pAg;
    }

    public Float getpShenbogame() {
        return pShenbogame;
    }

    public void setpShenbogame(Float pShenbogame) {
        this.pShenbogame = pShenbogame;
    }

    public Float getpShenbo() {
        return pShenbo;
    }

    public void setpShenbo(Float pShenbo) {
        this.pShenbo = pShenbo;
    }

    public Float getpAb() {
        return pAb;
    }

    public void setpAb(Float pAb) {
        this.pAb = pAb;
    }

    public Float getpDs() {
        return pDs;
    }

    public void setpDs(Float pDs) {
        this.pDs = pDs;
    }

    public Float getpOg() {
        return pOg;
    }

    public void setpOg(Float pOg) {
        this.pOg = pOg;
    }

    public Float getpBbin() {
        return pBbin;
    }

    public void setpBbin(Float pBbin) {
        this.pBbin = pBbin;
    }

    public Float getpBbingame() {
        return pBbingame;
    }

    public void setpBbingame(Float pBbingame) {
        this.pBbingame = pBbingame;
    }

    public Float getpMg() {
        return pMg;
    }

    public void setpMg(Float pMg) {
        this.pMg = pMg;
    }

    public Float getpPt() {
        return pPt;
    }

    public void setpPt(Float pPt) {
        this.pPt = pPt;
    }

    public Float getpHaba() {
        return pHaba;
    }

    public void setpHaba(Float pHaba) {
        this.pHaba = pHaba;
    }

    public Float getpIg() {
        return pIg;
    }

    public void setpIg(Float pIg) {
        this.pIg = pIg;
    }

    public Float getpHg() {
        return pHg;
    }

    public void setpHg(Float pHg) {
        this.pHg = pHg;
    }

    public Float getpGgby() {
        return pGgby;
    }

    public void setpGgby(Float pGgby) {
        this.pGgby = pGgby;
    }

    public Float getpBgvideo() {
        return pBgvideo;
    }

    public void setpBgvideo(Float pBgvideo) {
        this.pBgvideo = pBgvideo;
    }

    public Float getpBglottery() {
        return pBglottery;
    }

    public void setpBglottery(Float pBglottery) {
        this.pBglottery = pBglottery;
    }

    public Integer getBankcardId() {
        return bankcardId;
    }

    public void setBankcardId(Integer bankcardId) {
        this.bankcardId = bankcardId;
    }

    public String getAlipayId() {
        return alipayId;
    }

    public void setAlipayId(String alipayId) {
        this.alipayId = alipayId == null ? null : alipayId.trim();
    }

    public String getWechatId() {
        return wechatId;
    }

    public void setWechatId(String wechatId) {
        this.wechatId = wechatId == null ? null : wechatId.trim();
    }

    public String getTenpayId() {
        return tenpayId;
    }

    public void setTenpayId(String tenpayId) {
        this.tenpayId = tenpayId == null ? null : tenpayId.trim();
    }

    public String getOnlinepayId() {
        return onlinepayId;
    }

    public void setOnlinepayId(String onlinepayId) {
        this.onlinepayId = onlinepayId == null ? null : onlinepayId.trim();
    }

    public Float getIntegralRatio() {
        return integralRatio;
    }

    public void setIntegralRatio(Float integralRatio) {
        this.integralRatio = integralRatio;
    }

    public Float getcIntegralRatio() {
        return cIntegralRatio;
    }

    public void setcIntegralRatio(Float cIntegralRatio) {
        this.cIntegralRatio = cIntegralRatio;
    }

    public Float getpHkig() {
        return pHkig;
    }

    public void setpHkig(Float pHkig) {
        this.pHkig = pHkig;
    }

    public Float getpVr() {
        return pVr;
    }

    public void setpVr(Float pVr) {
        this.pVr = pVr;
    }

    public Float getpJf() {
        return pJf;
    }

    public void setpJf(Float pJf) {
        this.pJf = pJf;
    }

    public Float getpJfcp() {
        return pJfcp;
    }

    public void setpJfcp(Float pJfcp) {
        this.pJfcp = pJfcp;
    }

    public Float getpYorplay() {
        return pYorplay;
    }

    public void setpYorplay(Float pYorplay) {
        this.pYorplay = pYorplay;
    }

    public Float getpKyqp() {
        return pKyqp;
    }

    public void setpKyqp(Float pKyqp) {
        this.pKyqp = pKyqp;
    }

    public Float getpSpta() {
        return pSpta;
    }

    public void setpSpta(Float pSpta) {
        this.pSpta = pSpta;
    }

    public Float getpVgqp() {
        return pVgqp;
    }

    public void setpVgqp(Float pVgqp) {
        this.pVgqp = pVgqp;
    }

    public String getPaymentChannel() {
        return paymentChannel;
    }

    public void setPaymentChannel(String paymentChannel) {
        this.paymentChannel = paymentChannel == null ? null : paymentChannel.trim();
    }

    public Float getJfAgsx() {
        return jfAgsx;
    }

    public void setJfAgsx(Float jfAgsx) {
        this.jfAgsx = jfAgsx;
    }

    public Float getJfAgby() {
        return jfAgby;
    }

    public void setJfAgby(Float jfAgby) {
        this.jfAgby = jfAgby;
    }

    public Float getJfBbsx() {
        return jfBbsx;
    }

    public void setJfBbsx(Float jfBbsx) {
        this.jfBbsx = jfBbsx;
    }

    public Float getJfBbdz() {
        return jfBbdz;
    }

    public void setJfBbdz(Float jfBbdz) {
        this.jfBbdz = jfBbdz;
    }

    public Float getJfCgsx() {
        return jfCgsx;
    }

    public void setJfCgsx(Float jfCgsx) {
        this.jfCgsx = jfCgsx;
    }

    public Float getJfDssx() {
        return jfDssx;
    }

    public void setJfDssx(Float jfDssx) {
        this.jfDssx = jfDssx;
    }

    public Float getJfGgby() {
        return jfGgby;
    }

    public void setJfGgby(Float jfGgby) {
        this.jfGgby = jfGgby;
    }

    public Float getJfHbdz() {
        return jfHbdz;
    }

    public void setJfHbdz(Float jfHbdz) {
        this.jfHbdz = jfHbdz;
    }

    public Float getJfHgty() {
        return jfHgty;
    }

    public void setJfHgty(Float jfHgty) {
        this.jfHgty = jfHgty;
    }

    public Float getJfIgctc() {
        return jfIgctc;
    }

    public void setJfIgctc(Float jfIgctc) {
        this.jfIgctc = jfIgctc;
    }

    public Float getJfMgdz() {
        return jfMgdz;
    }

    public void setJfMgdz(Float jfMgdz) {
        this.jfMgdz = jfMgdz;
    }

    public Float getJfObsx() {
        return jfObsx;
    }

    public void setJfObsx(Float jfObsx) {
        this.jfObsx = jfObsx;
    }

    public Float getJfOgsx() {
        return jfOgsx;
    }

    public void setJfOgsx(Float jfOgsx) {
        this.jfOgsx = jfOgsx;
    }

    public Float getJfPtdz() {
        return jfPtdz;
    }

    public void setJfPtdz(Float jfPtdz) {
        this.jfPtdz = jfPtdz;
    }

    public Float getJfSbsx() {
        return jfSbsx;
    }

    public void setJfSbsx(Float jfSbsx) {
        this.jfSbsx = jfSbsx;
    }

    public Float getJfSbdz() {
        return jfSbdz;
    }

    public void setJfSbdz(Float jfSbdz) {
        this.jfSbdz = jfSbdz;
    }

    public Float getJfIgxgc() {
        return jfIgxgc;
    }

    public void setJfIgxgc(Float jfIgxgc) {
        this.jfIgxgc = jfIgxgc;
    }

    public Float getJfIgpjctc() {
        return jfIgpjctc;
    }

    public void setJfIgpjctc(Float jfIgpjctc) {
        this.jfIgpjctc = jfIgpjctc;
    }

    public Float getJfIgpjxgc() {
        return jfIgpjxgc;
    }

    public void setJfIgpjxgc(Float jfIgpjxgc) {
        this.jfIgpjxgc = jfIgpjxgc;
    }

    public Float getJfVrcp() {
        return jfVrcp;
    }

    public void setJfVrcp(Float jfVrcp) {
        this.jfVrcp = jfVrcp;
    }

    public Float getJfYoplaydz() {
        return jfYoplaydz;
    }

    public void setJfYoplaydz(Float jfYoplaydz) {
        this.jfYoplaydz = jfYoplaydz;
    }

    public Float getJfKyqp() {
        return jfKyqp;
    }

    public void setJfKyqp(Float jfKyqp) {
        this.jfKyqp = jfKyqp;
    }

    public Float getJfSpta() {
        return jfSpta;
    }

    public void setJfSpta(Float jfSpta) {
        this.jfSpta = jfSpta;
    }

    public Float getJfVgqp() {
        return jfVgqp;
    }

    public void setJfVgqp(Float jfVgqp) {
        this.jfVgqp = jfVgqp;
    }

    public Float getpGycp() {
        return pGycp;
    }

    public void setpGycp(Float pGycp) {
        this.pGycp = pGycp;
    }

    public Float getJfGycp() {
        return jfGycp;
    }

    public void setJfGycp(Float jfGycp) {
        this.jfGycp = jfGycp;
    }

    public Float getpPs() {
        return pPs;
    }

    public void setpPs(Float pPs) {
        this.pPs = pPs;
    }

    public Float getJfPs() {
        return jfPs;
    }

    public void setJfPs(Float jfPs) {
        this.jfPs = jfPs;
    }

    public Float getpNb() {
        return pNb;
    }

    public void setpNb(Float pNb) {
        this.pNb = pNb;
    }

    public Float getJfNb() {
        return jfNb;
    }

    public void setJfNb(Float jfNb) {
        this.jfNb = jfNb;
    }

    public Float getpLyqp() {
        return pLyqp;
    }

    public void setpLyqp(Float pLyqp) {
        this.pLyqp = pLyqp;
    }

    public Float getJfLyqp() {
        return jfLyqp;
    }

    public void setJfLyqp(Float jfLyqp) {
        this.jfLyqp = jfLyqp;
    }

    public Float getpJdb() {
        return pJdb;
    }

    public void setpJdb(Float pJdb) {
        this.pJdb = pJdb;
    }

    public Float getJfJdb() {
        return jfJdb;
    }

    public void setJfJdb(Float jfJdb) {
        this.jfJdb = jfJdb;
    }

    public Float getpSw() {
        return pSw;
    }

    public void setpSw(Float pSw) {
        this.pSw = pSw;
    }

    public Float getJfSw() {
        return jfSw;
    }

    public void setJfSw(Float jfSw) {
        this.jfSw = jfSw;
    }

    public Float getpIbc() {
        return pIbc;
    }

    public void setpIbc(Float pIbc) {
        this.pIbc = pIbc;
    }

    public Float getJfIbc() {
        return jfIbc;
    }

    public void setJfIbc(Float jfIbc) {
        this.jfIbc = jfIbc;
    }
}