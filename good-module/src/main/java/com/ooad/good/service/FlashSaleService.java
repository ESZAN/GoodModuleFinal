package com.ooad.good.service;

import cn.edu.xmu.ooad.util.ResponseCode;
import cn.edu.xmu.ooad.util.ReturnObject;
import cn.edu.xmu.oomall.goods.model.SkuInfoDTO;
import cn.edu.xmu.oomall.goods.service.IGoodsService;
import cn.edu.xmu.oomall.other.model.TimeDTO;
import cn.edu.xmu.oomall.other.service.ITimeService;
import com.ooad.good.dao.FlashSaleDao;
import com.ooad.good.model.bo.FlashSale;
import com.ooad.good.model.bo.FlashSaleItem;
import com.ooad.good.model.po.FlashSaleItemPo;
import com.ooad.good.model.po.FlashSalePo;
import com.ooad.good.model.vo.flashSale.FlashSaleItemRetVo;
import com.ooad.good.model.vo.flashSale.FlashsaleNewRetVo;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @AuthorId: 24320182203185
 * @Author: Chaoyang Deng
 * @Date: 2020/12/15 下午9:46
 */
@Service
public class FlashSaleService {
    private static final Logger logger = LoggerFactory.getLogger(FlashSaleDao.class);
    @Autowired
    FlashSaleDao flashsaleDao;
    @Autowired
    SkuService skuService;

    @DubboReference(check = false)
    private IGoodsService goodsServiceImpl;

    @Autowired
    private ReactiveRedisTemplate<String, Serializable>reactiveRedisTemplate;

    @Autowired
    private RedisTemplate<String, Serializable> redisTemplate;

    @DubboReference(check = false)
    private ITimeService timeService;

    /**
     * 向秒杀活动添加商品
     * @param flashSaleItem
     * @return
     */
    @Transactional
    public ReturnObject<FlashSaleItemRetVo> insertSkuToFlash(FlashSaleItem flashSaleItem) {
        ReturnObject<FlashSalePo> flashSalePoReturnObject=flashsaleDao.selectByFlashsaleId(flashSaleItem.getSaleId());
        if(flashSalePoReturnObject.getCode()!=ResponseCode.OK)
            return new ReturnObject<>(flashSalePoReturnObject.getCode());
        else {
            if(flashSalePoReturnObject.getData().getFlashDate().isBefore(LocalDateTime.now().plusDays(1))) {
                return new ReturnObject<>(ResponseCode.FIELD_NOTVALID);
            }
        }
        ReturnObject<SkuInfoDTO> returnObject = goodsServiceImpl.getSelectSkuInfoBySkuId(flashSaleItem.getGoodsSku().getId());
        logger.error("goods Service返回给flashsale："+returnObject.getData().toString());
        if(returnObject.getCode()!= ResponseCode.OK) {//错误
            return new ReturnObject(returnObject.getCode());
        } else if(returnObject.getData() == null) {//不存在，返回资源不存在
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }

        ReturnObject<FlashSaleItem> retObj = flashsaleDao.insertSku(flashSaleItem);
        if(retObj.getCode()!=ResponseCode.OK)
        {
            return new ReturnObject<>(retObj.getCode());
        }
        FlashSaleItemRetVo retVo = new FlashSaleItemRetVo(retObj.getData());
        retVo.setGoodsSku(returnObject.getData());
        return new ReturnObject<>(retVo);
    }

    /**
     * 查询秒杀活动
     * @param id
     * @return
     */
    @Transactional
    public Flux<FlashSaleItemRetVo> getAllFlashsale(Long id) {
        //ReturnObject<List> returnId=flashsaleDao.getflashSaleId(id);
        LocalDateTime dateTime = LocalDateTime.now();
        logger.error("FlashSaleItem:" + dateTime.toLocalDate().toString() + id.toString());
        return reactiveRedisTemplate.opsForSet().members("FlashSaleItem:" + dateTime.toLocalDate().toString() + id.toString()).map(x->
        {
            SkuInfoDTO skuInfoDTO = new SkuInfoDTO();
            skuInfoDTO.setId(((FlashSaleItemPo)x).getGoodsSkuId());
            logger.error(x.toString());
            logger.error(((FlashSaleItemPo)x).toString());
            FlashSaleItemRetVo retVo = new FlashSaleItemRetVo((FlashSaleItemPo)x);
            retVo.setGoodsSku(skuInfoDTO);
            logger.error("retVo:"+ retVo.toString());
            return retVo;
        });
    }
    /**
     * 新建秒杀活动
     * @param flashSale
     * @return
     */
    @Transactional
    public ReturnObject<FlashsaleNewRetVo> insertFlashsale(FlashSale flashSale) {
        ReturnObject<FlashsaleNewRetVo> retObj=new ReturnObject<>();
        try {
            Byte timeType = 1;//0代表广告，1代表秒杀
            ReturnObject<TimeDTO> timeDTOReturnObject = timeService.getTimeSegmentId(timeType, flashSale.getTimeSeq());
            if (timeDTOReturnObject.getData() == null) {
                //若不存在返回资源不存在错误
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            ReturnObject<FlashSalePo> flashSalePoReturnObject = flashsaleDao.selectByFlashDateAndSegId(flashSale.getFlashDate(), flashSale.getTimeSeq());

            if (flashSalePoReturnObject.getData() != null) {
                return new ReturnObject<>(ResponseCode.TIMESEG_CONFLICT);
            }

            retObj = flashsaleDao.insertFlashsale(flashSale);
        }
        catch (Exception e) {
            logger.error(e.toString());
    }
        return retObj;
    }
    /**
     * 删除秒杀活动中商品
     * @param id
     * @return
     */
    @Transactional
    public ReturnObject deleteFlashsaleItem(Long fid, Long id) {
        ReturnObject<FlashSalePo> returnFid=flashsaleDao.getFlashSaleById(fid);
        if(returnFid.getCode()==ResponseCode.OK){
        String key = "FlashSaleItem:" + returnFid.getData().getFlashDate().toLocalDate().toString() + returnFid.getData().getTimeSegId().toString();
        FlashSaleItemPo itemPo;
        if(returnFid.getCode()!=ResponseCode.OK)
            return new ReturnObject<>(returnFid.getCode());

        ReturnObject<FlashSaleItemPo> itemsRetObj = flashsaleDao.getItemsByPrimaryKey(id);
        if(itemsRetObj.getCode()!=ResponseCode.OK) {
            return itemsRetObj;
        } else {
            itemPo = itemsRetObj.getData();
        }
        ReturnObject retObj= flashsaleDao.deleteFlashsaleItem(fid,id);
        if(retObj.getCode()==ResponseCode.OK && returnFid.getData().getFlashDate().isBefore(LocalDateTime.now().plusDays(1))) {
            redisTemplate.boundSetOps(key).remove(itemPo);
        }
        return retObj;
        }return returnFid;
    }
    /**
     * 上架秒杀活动
     * @param id
     * @return
     */
    @Transactional
    public ReturnObject onshelvesflashSale(Long id) {
        ReturnObject retObj=flashsaleDao.onshelvesflashSale(id);
        return retObj;
    }
    /**
     * 下架秒杀活动
     * @param id
     * @return
     */
    @Transactional
    public ReturnObject offshelvesflashSale(Long id) {
        ReturnObject retObj=flashsaleDao.offshelvesflashSale(id);
        return retObj;
    }
    /**
     * 删除秒杀活动
     * @param id
     * @return
     */
    @Transactional
    public ReturnObject deleteflashSale(Long id) {
        ReturnObject retObj=flashsaleDao.deleteflashSale(id);
        return retObj;
    }

    /**
     * 管理员修改秒杀活动
     * @param flashSale
     * @return
     */
    @Transactional
    public ReturnObject modifyFlashsale(FlashSale flashSale) {
        ReturnObject retObj=flashsaleDao.modifyFlashSale(flashSale);
        return retObj;
    }

    /**
     * 获取当前时段秒杀活动
     * @return
     */
    public ReturnObject<List> getCurrentFlashsale() {
        Long timeseqId=getIdBytimeseq();
        ReturnObject<List> returnId=flashsaleDao.getflashSaleId(timeseqId);

        if(returnId.getCode().equals("RESOURCE_ID_NOTEXIST"))return returnId;
        List<Long> flashId=returnId.getData();
        ReturnObject<List> returnObject=flashsaleDao.getflashSaleById(flashId);
        return returnObject;
    }
/**临时创建接口**/
    private Long getIdBytimeseq() {
        int a=0;
        Long i=new Long(a);
        return i;
    }

    /**
     * 获取当前秒杀活动所有商品
     * 响应式返回
     * @param id
     * @return
     */
    public Flux<FlashSaleItemRetVo> queryTopicsByTime(Long id) {
        LocalDateTime dateTime = LocalDateTime.now();
        logger.error("FlashSaleItem:" + dateTime.toLocalDate().toString() + id.toString());
        return reactiveRedisTemplate.opsForSet().members("FlashSaleItem:" + dateTime.toLocalDate().toString() + id.toString()).map(x->
        {
            SkuInfoDTO skuInfoDTO = new SkuInfoDTO();
            skuInfoDTO.setId(((FlashSaleItemPo)x).getGoodsSkuId());
            logger.error(x.toString());
            logger.error(((FlashSaleItemPo)x).toString());
            FlashSaleItemRetVo retVo = new FlashSaleItemRetVo((FlashSaleItemPo)x);
            retVo.setGoodsSku(skuInfoDTO);
            logger.error("retVo:"+ retVo.toString());
            return retVo;
        });
    }
}
