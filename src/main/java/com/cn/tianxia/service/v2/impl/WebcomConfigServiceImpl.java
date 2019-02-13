package com.cn.tianxia.service.v2.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.cn.tianxia.common.v2.DatePatternConstant;
import com.cn.tianxia.common.v2.DatePatternUtils;
import com.cn.tianxia.dao.v2.CagentDao;
import com.cn.tianxia.dao.v2.CagentWebcodeDao;
import com.cn.tianxia.dao.v2.WebcomConfigDao;
import com.cn.tianxia.entity.v2.CagentEntity;
import com.cn.tianxia.entity.v2.CagentWebcodeEntity;
import com.cn.tianxia.entity.v2.WebcomConfigEntity;
import com.cn.tianxia.po.BaseResponse;
import com.cn.tianxia.po.v2.JSONArrayResponse;
import com.cn.tianxia.service.v2.WebcomConfigService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 
 * @ClassName WebcomConfigServiceImpl
 * @Description 网站配置信息接口实现类
 * @author Hardy
 * @Date 2019年2月5日 下午6:55:24
 * @version 1.0.0
 */
@Service
public class WebcomConfigServiceImpl implements WebcomConfigService {

    //日志
    private static final Logger logger = LoggerFactory.getLogger(WebcomConfigServiceImpl.class);
    
    @Autowired
    private WebcomConfigDao webcomConfigDao;
    
    @Autowired
    private CagentDao cagentDao;
    
    @Autowired
    private CagentWebcodeDao cagentWebcodeDao;
    
    
    @Override
    public JSONArray getBanner(String cagent) {
        logger.info("调用获取网站广告图列表业务开始================START================");
        try {
            JSONArray data = new JSONArray();
            //查询平台banner图
            List<WebcomConfigEntity> list = webcomConfigDao.findAllByCagent(cagent);
            if(!CollectionUtils.isEmpty(list)){
                Map<String,List<WebcomConfigEntity>> arrays = list.stream().collect(Collectors.groupingBy(WebcomConfigEntity::getType));
                if(arrays.containsKey("1")){
                    data.add(JSONArray.fromObject(arrays.get("1")));
                }
                
                if(arrays.containsKey("2")){
                    data.add(JSONArray.fromObject(arrays.get("2")));
                }
                
                if(arrays.containsKey("3")){
                    data.add(JSONArray.fromObject(arrays.get("3")));
                }
                
                if(arrays.containsKey("4")){
                    data.add(JSONArray.fromObject(arrays.get("4")));
                }
                
                if(arrays.containsKey("5")){
                    data.add(JSONArray.fromObject(arrays.get("5")));
                }
            }
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("调用获取网站广告图列表业务异常:{}",e.getMessage());
            return JSONArrayResponse.faild("调用获取网站广告图列表业务异常");
        }
    }

    @Override
    public JSONArray getNoticeInfo(String cagent) {
        logger.info("调用获取网站公告信息业务开始===================START=============");
        try {
            JSONArray data = new JSONArray();
            //查询玩网站公告信息
            List<WebcomConfigEntity> webcomConfigs = webcomConfigDao.getNoticesByCagent(cagent);
            if(!CollectionUtils.isEmpty(webcomConfigs)){
                for (WebcomConfigEntity webcomConfigEntity : webcomConfigs) {
                    JSONObject jsonObject = new JSONObject();
                    // 修改特殊符号被转义的问题
                    String rmk = StringEscapeUtils.unescapeHtml(webcomConfigEntity.getRmk());
                    jsonObject.put("rmk", rmk);
                    jsonObject.put("src1",webcomConfigEntity.getSrc1());
                    data.add(jsonObject);
                }
            }
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("调用获取网站公告信息业务异常:{}",e.getMessage());
            return JSONArrayResponse.faild("调用获取网站公告信息业务异常");
        }
    }

    @Override
    public JSONObject getWebcomConfig(String cagent, Integer type) {
        logger.info("调用查询平台网站设置业务开始==================START===============");
        try {
            
            //通过平台编码查询平台ID
            CagentEntity cagentEntity = cagentDao.selectByCagent(cagent);
            if(cagentEntity == null){
                return BaseResponse.faild("0", "非法平台编码,查询平台信息失败");
            }
            CagentWebcodeEntity cagentWebcodeEntity = cagentWebcodeDao.getWebcomConfig(type, cagentEntity.getId());
            if(cagentWebcodeEntity == null){
                return BaseResponse.faild("0", "查询平台网站设置失败");
            }
            
            JSONObject data = new JSONObject();
            data.put("id",cagentWebcodeEntity.getId());
            data.put("cid",cagentWebcodeEntity.getCid());
            data.put("type",cagentWebcodeEntity.getType());
            data.put("code",cagentWebcodeEntity.getCode());
            data.put("utime",cagentWebcodeEntity.getUtime());
            data.put("uid",cagentWebcodeEntity.getUid());
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("调用查询平台网站设置业务异常:{}",e.getMessage());
            return BaseResponse.faild("0", "调用查询平台网站设置业务异常");
        }
    }

    @Override
    public JSONArray getMobileWebcomConfig(String cagent, Integer type) {
        logger.info("调用查询平台移动端网站设置业务开始=================START==================");
        try {
            JSONArray data = new JSONArray();
            //查询手机端网站设置
            List<WebcomConfigEntity> webcomConfigEntities = webcomConfigDao.findAllByMobileType(cagent, String.valueOf(type));
            List<Map<String,String>> mapList = new ArrayList<>();
            if(!CollectionUtils.isEmpty(webcomConfigEntities)){
                for (WebcomConfigEntity webcomConfigEntity : webcomConfigEntities) {
                    Map<String,String> jsonObject = new HashMap<>();
                    jsonObject.put("type",webcomConfigEntity.getType());
                    jsonObject.put("title",webcomConfigEntity.getTitle());
                    jsonObject.put("img1",webcomConfigEntity.getImg1());
                    jsonObject.put("img2",webcomConfigEntity.getImg2());
                    jsonObject.put("img3",webcomConfigEntity.getImg3());
                    jsonObject.put("src1",webcomConfigEntity.getSrc1());
                    jsonObject.put("weight",String.valueOf(webcomConfigEntity.getWeight()));
                    jsonObject.put("rmk",webcomConfigEntity.getRmk());
                    jsonObject.put("updatetime",
                            DatePatternUtils.dateToStr(webcomConfigEntity.getUpdatetime(), DatePatternConstant.NORM_DATETIME_MINUTE_PATTERN));
                    jsonObject.put("id",String.valueOf(webcomConfigEntity.getId()));
                    mapList.add(jsonObject);
                }
            }
            data.add(mapList);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("调用查询平台移动端网站设置业务异常:{}",e.getMessage());
            return JSONArrayResponse.faild("调用查询平台移动端网站设置业务异常");
        }
    }

}
