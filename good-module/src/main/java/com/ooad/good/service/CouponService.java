package com.ooad.good.service;

import cn.edu.xmu.ooad.util.ResponseCode;
import cn.edu.xmu.ooad.util.ReturnObject;
import com.ooad.good.controller.CouponController;
import com.ooad.good.dao.CouponDao;
import com.ooad.good.dao.ShopDao;
import com.ooad.good.model.bo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CouponService {
    private  static  final Logger logger = LoggerFactory.getLogger(CouponService.class);

    @Autowired
    private CouponDao couponDao;

    @Autowired
    private ShopService shopService;
    @Autowired
    private SkuService skuService;
    @Autowired
    private SpuService spuService;

    /**
     * 管理员新建己方优惠活动
     * @param couponActivity
     * @return
     */
    @Transactional
    public ReturnObject insertCouponActivity(CouponActivity couponActivity){
        ReturnObject<CouponActivity>retObj=couponDao.insertCouponActivity(couponActivity);
        return retObj;
    }

    /**
     * 管理员删除己方优惠活动
     * @param id
     * @return
     */
    @Transactional
    public ReturnObject deleteCouponActivity(Long shopId,Long id){

        return couponDao.deleteCouponActivity(shopId,id);
    }

    /**
     * 管理员修改己方优惠活动
     * @param couponActivity
     * @return
     */
    @Transactional
    public ReturnObject updateCouponActivity(CouponActivity couponActivity){
        ReturnObject<CouponActivity> retObj=couponDao.updateCouponActivity(couponActivity);
        return retObj;
    }


    /**
     * 上线优惠活动
     * @param id
     * @return
     */
    @Transactional
    public ReturnObject onlineCouponactivity(Long shopId,Long id){
        ReturnObject<ResponseCode>retObj=couponDao.onlineCouponactivity(shopId,id);
        return  retObj;
    }

    /**
     * 下线优惠活动
     * @param id
     * @return
     */
    @Transactional
    public ReturnObject offlineCouponactivity(Long shopId,Long id){
        ReturnObject<ResponseCode>retObj=couponDao.offlineCouponactivity(shopId,id);
        return  retObj;
    }

    /**
     * 查询优惠活动
     * @param shopId
     * @param id
     * @return
     */
    @Transactional
    public ReturnObject<Object> getCouponActivity(Long shopId, Long id) {
        Shop shop = shopService.getSimpleShopByShopId(shopId).getData();
        if(shop == null) {
            logger.info("shop==null");
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        ReturnObject<CouponActivity> retCouponActivity=couponDao.getCouponActivity(shopId,id);
        //不存在该活动或无权限先返回
        if(retCouponActivity.getCode().equals(ResponseCode.RESOURCE_ID_NOTEXIST)||
                retCouponActivity.getCode().equals(ResponseCode.RESOURCE_ID_OUTSCOPE))
            return new ReturnObject<>(retCouponActivity.getCode());
        ReturnObject<Object> returnObject= couponDao.showCouponActivity(shop,id);
        return returnObject;
    }

    @Transactional
    public ReturnObject<Object> createCouponSkus(Long shopId, Long id, List<CouponSku> couponSkus) {
        ReturnObject<Object> returnObject;
        logger.info("Ser");
        for(CouponSku couponSku:couponSkus)
        {
          Sku sku=skuService.getSkuBySkuId(couponSku.getSkuId()).getData();
          if(sku==null){
              logger.info("sku error");
              return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
          }
          Spu spu=spuService.getSpuBySpuId(sku.getGoodsSpuId()).getData();
          if(spu==null){
              logger.info("spu error");
              return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
          }
          Shop shop=shopService.getSimpleShopByShopId(shopId).getData();
          if(shop==null)
          {              logger.info("shop error");
              return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
          }
          if(sku.getState().intValue()==6){
              logger.info("sku state error");
              return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
          }
          if(shop.getState().intValue()==4||shop.getState().intValue()==3){
                           logger.info(shop.getId()+"shop state error"+shop.getState().intValue());
              return new ReturnObject<>(ResponseCode.SHOP_STATENOTALLOW);}
          if(spu.getShopId()!=shopId){
              logger.info("spu and shop error");
            return new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE);
          }
        }
        returnObject= couponDao.createCouponSkus(shopId,id, couponSkus);
        return returnObject;
    }

    public ReturnObject<ResponseCode> deleteCouponSku(Long shopId, Long id) {
        ReturnObject<ResponseCode> returnObject= couponDao.deleteCouponSku(shopId,id);
        return returnObject;
    }
}

