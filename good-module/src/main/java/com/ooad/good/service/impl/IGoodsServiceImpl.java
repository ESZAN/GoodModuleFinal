package com.ooad.good.service.impl;

import cn.edu.xmu.ooad.util.ResponseCode;
import cn.edu.xmu.ooad.util.ReturnObject;
import cn.edu.xmu.oomall.goods.model.*;
import cn.edu.xmu.oomall.goods.service.IActivityService;
import cn.edu.xmu.oomall.goods.service.IFlashsaleService;
import cn.edu.xmu.oomall.goods.service.IGoodsService;
import com.ooad.good.dao.ShopDao;
import com.ooad.good.dao.SkuDao;
import com.ooad.good.dao.SpuDao;
import com.ooad.good.model.po.ShopPo;
import com.ooad.good.model.po.SpuPo;
import com.ooad.good.model.vo.sku.SkuDetailRetVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @AuthorId: 24320182203185
 * @Author: Chaoyang Deng
 * @Date: 2020/12/24 下午2:14
 */
@Slf4j
@DubboService
@Service
public class IGoodsServiceImpl implements IGoodsService {
    @Autowired
    SkuDao goodsDao;

    @Autowired
    SpuDao goodsSpuDao;

    @Autowired
    ShopDao shopDao;

    @DubboReference(check = false)
    private IActivityService IActivityService;

    @DubboReference(check = false)
    private IFlashsaleService iFlashsaleService;

    private static final Logger logger = LoggerFactory.getLogger(IGoodsServiceImpl.class);

    @Override
    public ReturnObject<List<Long>> getAllSkuIdByShopId(Long shopId) {
        ReturnObject<List<Long>> returnObject=goodsDao.getAllSkuIdByShopId(shopId);
        return returnObject;
    }

    @Override
    public ReturnObject<Long> getShopIdBySkuId(Long skuId) {
        return null;
    }

    @Override
    public ReturnObject<Boolean> getVaildSkuId(Long skuId) {
        return null;
    }

    @Override
    public ReturnObject<SkuInfoDTO> getSelectSkuInfoBySkuId(Long skuId)
    {
        SkuInfoDTO skuInfoDTO=goodsDao.getSelectSkuInfoBySkuId(skuId);
        if(skuInfoDTO!=null)
            skuInfoDTO.setPrice(goodsDao.getPriceBySkuId(skuId).getData());
        return new ReturnObject<>(skuInfoDTO);
    }

    @Override
    public ReturnObject<Map<Long, SkuInfoDTO>> listSelectSkuInfoById(List<Long> skuIdList) {
        return null;
    }

    @Override
    public ReturnObject<GoodsInfoDTO> getSelectGoodsInfoBySkuId(Long skuId) {
        return null;
    }

    @Override
    public List<SkuNameInfoDTO> getSelectSkuNameListBySkuIdList(List<Long> idList) {
        return null;
    }

    @Override
    public ReturnObject checkSkuUsableBySkuShop(Long skuId, Long shopId) {
        return null;
    }

    @Override
    public ReturnObject<SimpleShopDTO> getSimpleShopByShopId(Long shopId) {
        return null;
    }

    @Override
    public ReturnObject<GoodsFreightDTO> getGoodsFreightDetailBySkuId(Long skuId) {
        return null;
    }

    @Override
    public ReturnObject<GoodsSpuPoDTO> getSpuBySpuId(Long id) {
        return null;
    }

    @Override
    public ReturnObject<List<Long>> getSkuIdsBySpuId(Long spuId) {
        return null;
    }

    @Override
    public ReturnObject<SimpleGoodsSkuDTO> getSimpleSkuBySkuId(Long skuId) {
        return null;
    }

    @Override
    public List<SkuInfoDTO> getSelectSkuListBySkuIdList(List<Long> idList) {
        return null;
    }

    @Override
    public ReturnObject<ShopDetailDTO> getShopInfoBySkuId(Long skuId) {
        return null;
    }

    @Override
    public ReturnObject<ShopDetailDTO> getShopInfoByShopId(Long shopId) {
        return null;
    }

    @Override
    public ReturnObject<GoodsDetailDTO> getGoodsBySkuId(Long skuId, Byte type, Long activityId, Integer quantity) {
        return null;
    }

    @Override
    public ReturnObject<Boolean> updateSpuFreightId(Long freightModelId) {
        return null;
    }

    public ReturnObject<Long> getPriceBySkuId(Long skuId) {
        ReturnObject<GoodsDetailDTO> ret=iFlashsaleService.modifyFlashsaleItem(skuId,0);
        if(ret!=null&&ret.getCode().equals(ResponseCode.OK))
            return new ReturnObject<>(ret.getData().getPrice());
        return new ReturnObject<>(goodsDao.getPriceBySkuId(skuId).getData());
    }

    @Override
    public ReturnObject<ResponseCode> signalDecrInventory(List<Long> skuIds, List<Integer> quantity) {
        return null;
    }
}
