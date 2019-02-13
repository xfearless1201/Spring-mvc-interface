package com.cn.tianxia.service.v2.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.CollectionUtils;

import com.cn.tianxia.common.v2.DatePatternConstant;
import com.cn.tianxia.common.v2.DatePatternUtils;
import com.cn.tianxia.dao.v2.PluCateDao;
import com.cn.tianxia.dao.v2.PluInfoDao;
import com.cn.tianxia.dao.v2.PluOrderDao;
import com.cn.tianxia.dao.v2.UserWalletDao;
import com.cn.tianxia.entity.v2.PluCateEntity;
import com.cn.tianxia.entity.v2.PluInfoEntity;
import com.cn.tianxia.entity.v2.PluOrderEntity;
import com.cn.tianxia.po.BaseResponse;
import com.cn.tianxia.service.v2.IntegralMallService;
import com.cn.tianxia.vo.PluCateVO;
import com.cn.tianxia.vo.PluInfoVO;
import com.cn.tianxia.vo.PluOrderVO;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class IntegralMallServiceImpl implements IntegralMallService {
	// 日志
    private static final Logger logger = LoggerFactory.getLogger(IntegralMallServiceImpl.class);
    //商品类别映射
	@Autowired
	private PluCateDao pluCateDao;
	//商品信息映射
	@Autowired
	private PluInfoDao pluInfoDao;
	//商品订单映射
	@Autowired
	private PluOrderDao pluOrderDao;
	//用户钱包映射
	@Autowired
	private UserWalletDao userWalletDao;
	
	/**
	 * 根据平台商名称获取商品类型
	 * @param cagentName
	 * @return
	 */
	@Override
	public JSONArray getTypeByCagentName(String cagentName) {
		List<PluCateEntity> resultList = pluCateDao.selectTypeByCagentName(cagentName);
		//key-pid value-子节点的集合
		Map<Integer,List<PluCateEntity>> showDataMap=new HashMap<Integer,List<PluCateEntity>>();
		Map<Integer,String> goodsName=new HashMap<Integer,String>();//key-id,value-name
		for(PluCateEntity pluCateEntity:resultList){
			if("0".equals(pluCateEntity.getPid()+"")){
				//父节点，直接添加
				List<PluCateEntity> dataList=new ArrayList<PluCateEntity>();
				showDataMap.put(pluCateEntity.getId(), dataList);
			}else{
				//子节点，添加到相对应父节点下子节点的集合
				showDataMap.get(pluCateEntity.getPid()).add(pluCateEntity);
			}
			goodsName.put(pluCateEntity.getId(), pluCateEntity.getCatename());
		}

		List<JSONObject> list = new ArrayList<>();
		for(Entry<Integer,List<PluCateEntity>> entry:showDataMap.entrySet()){
			JSONObject jsonObject =new JSONObject();
			jsonObject.put("typeName",goodsName.get(entry.getKey()));
			jsonObject.put("data",entry.getValue());
			list.add(jsonObject);
		}

		return JSONArray.fromObject(list);
	}
	
	/**
     * 获取所有的商品信息
     * @param paramMap
     * @return
     */
	@Override
	public JSONObject getGoodsList(PluInfoVO pluInfoVO) {
		JSONObject jsonObject=new JSONObject();
		jsonObject.put("total", pluInfoDao.countGoods(pluInfoVO));
		jsonObject.put("result", pluInfoDao.getAllGoods(pluInfoVO));
		return  jsonObject;
	}

	/**
	 * 获取商品类别
	 * @param paramMap
	 * @return
	 */
	@Override
	public JSONObject getGoodsType(PluCateVO pluCateVO) {
		List<PluCateEntity> typeList = pluCateDao.getGoodsType(pluCateVO);
		return JSONObject.fromObject(typeList);
	}
    /**
     * 获取单个商品信息
     */
	@Override
	public JSONObject getGoodsInfo(Integer id) {
		PluInfoVO goodsInfo = pluInfoDao.getGoodsInfo(id);
		return JSONObject.fromObject(goodsInfo);
	}
	
	
    /**
     * 创建订单
     */
	@Override
	@Transactional(propagation=Propagation.REQUIRED,rollbackFor=Exception.class)
	public JSONObject createOrder(PluOrderVO pluOrderVO) {
	    logger.info("调用创建积分订单业务开始=================START================");
		try {
		    Integer uid = Integer.parseInt(pluOrderVO.getUid());
	        //根据商品id查询商品兑换需要的积分
	        PluInfoVO goodsInfo = pluInfoDao.getGoodsInfo(pluOrderVO.getId());
	        if(null==goodsInfo){
	            return BaseResponse.faild("0","不存在该商品！！！");
	        }
	        logger.info("积分商品ID查询商品信息:{}",goodsInfo.toString());
	        //判断库存
	        Integer inventNum = goodsInfo.getNum();//库存量
	        if(goodsInfo.getNum() > inventNum){
	            return BaseResponse.faild("0","商品库存不足！！！");
	        }
	        //查询用户可用积分
            Double integralBalance = userWalletDao.getIntegralBalance(uid);
	        //计算兑换积分*兑换数量是否大于用户剩余积分
	        double price = goodsInfo.getPrice();//兑换积分
	        // 兑换积分不能为负数
	        if (price<1) {
	            return BaseResponse.faild("0","兑换积分不能为空和负数！！！");
	        }
	        
	        //计算兑换积分金额
	        Double totalIntegral = pluOrderVO.getNum() * price;
	        
	        if(totalIntegral > integralBalance){
	            return BaseResponse.faild("0","积分不足！！！");
	        }
	        
	        //写入积分订单
	        PluOrderEntity pluOrderEntity = new PluOrderEntity();
	        pluOrderEntity.setCid(Integer.parseInt(pluOrderVO.getCid()));
	        pluOrderEntity.setUid(uid);
	        pluOrderEntity.setPluId(pluOrderVO.getId());
	        pluOrderEntity.setPluNumber(pluOrderVO.getNum());
	        pluOrderEntity.setOrderTime(new Date());
	        pluOrderEntity.setDeliverAddress(pluOrderVO.getDeliverAddress());
	        pluOrderEntity.setDeliverPhone(pluOrderVO.getDeliverPhone());
	        pluOrderEntity.setDeliverName(pluOrderVO.getDeliverName());
	        pluOrderEntity.setDeliverRmk(StringUtils.isBlank(pluOrderVO.getDeliverRmk())?"积分订单":pluOrderVO.getDeliverRmk());
	        pluOrderEntity.setRmk(StringUtils.isBlank(pluOrderVO.getDeliverRmk())?"积分订单":pluOrderVO.getDeliverRmk());
	        pluOrderEntity.setOrderState((byte)0);
	        pluOrderEntity.setAuditTime(new Date());
	        pluOrderEntity.setAuditId(0);
	        pluOrderEntity.setDeliverStatus((byte)0);
	        //写入积分订单
	        pluOrderDao.insertSelective(pluOrderEntity);
	        //修改用户积分金额
	        userWalletDao.deductUserIntegralBalance(uid, totalIntegral);
	        return BaseResponse.success("兑换成功！！！");
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("调用创建积分订单业务异常");
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return BaseResponse.error("2","系统异常！！！");
        }
	}
    /**
     * 获取历史订单
     */
	@Override
	public JSONObject getOrderHistory(String uid,String btime,String etime,Integer pageNo,Integer pageSize) {
	    logger.info("调用查询用户历史积分订单业务开始===================START=========================");
	    JSONObject data = new JSONObject();
	    try {
	        Date bdate = null;
	        Date edate = null;
	        if(StringUtils.isNotBlank(btime)){
	            bdate = DatePatternUtils.strToDate(btime,DatePatternConstant.NORM_DATETIME_PATTERN);
	        }
	        
	        if(StringUtils.isNotBlank(etime)){
	            edate = DatePatternUtils.strToDate(etime,DatePatternConstant.NORM_DATETIME_PATTERN);
	        }
	        //查询订单列表
	        List<PluOrderEntity> pluOrders = pluOrderDao.getHistoryOrder(uid, bdate, edate, pageNo, pageSize);
	        JSONArray array = new JSONArray();
	        int total = 0;
	        if(!CollectionUtils.isEmpty(pluOrders)){
	            total = pluOrderDao.countHistoryOrder(uid, bdate, edate);
	            pluOrders.stream().forEach(item->{
	                JSONObject jsonObject = new JSONObject();
	                jsonObject.put("price", item.getPrice());
	                jsonObject.put("pluname",item.getPluname());
	                jsonObject.put("plu_number",item.getPluNumber());
	                jsonObject.put("order_time", item.getOrderTime());
	                jsonObject.put("deliver_address", item.getDeliverAddress());
	                jsonObject.put("deliver_phone",item.getDeliverPhone());
	                jsonObject.put("deliver_name", item.getDeliverName());
	                jsonObject.put("deliver_status",item.getDeliverStatus());
	                jsonObject.put("order_state",item.getOrderState());
	                jsonObject.put("rmk",item.getRmk());
	                jsonObject.put("deliver_rmk", item.getDeliverRmk());
	                array.add(jsonObject);
	            });
	        }
	        data.put("result", array);
            data.put("total", total);
	        return data;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("调用查询用户历史积分订单业务异常:{}",e.getMessage());
            return BaseResponse.error("2", "调用查询用户历史积分订单业务异常");
        }
	}

	
	
	/**
     * 获取兑换排行列表
     */
	@Override
	public JSONArray getExchangeRankList(PluInfoVO pluInfoVO) {
		List<PluInfoEntity> rankList = pluInfoDao.getExchangeRankList(pluInfoVO);
		return JSONArray.fromObject(rankList);
	}

}
