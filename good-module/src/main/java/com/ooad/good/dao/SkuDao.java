package com.ooad.good.dao;

import cn.edu.xmu.ooad.util.ResponseCode;
import cn.edu.xmu.ooad.util.ReturnObject;
import cn.edu.xmu.oomall.goods.model.SkuInfoDTO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ooad.good.mapper.FloatPricePoMapper;
import com.ooad.good.mapper.SkuPoMapper;
import com.ooad.good.mapper.SpuPoMapper;
import com.ooad.good.model.bo.FloatPrice;
import com.ooad.good.model.bo.Groupon;
import com.ooad.good.model.bo.Sku;
import com.ooad.good.model.po.*;
import com.ooad.good.model.vo.floatPrice.FloatPriceRetVo;
import com.ooad.good.service.PresaleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.awt.color.ICC_Profile;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
public class SkuDao {
    private  static  final Logger logger = LoggerFactory.getLogger(SkuDao.class);

    @Autowired
    SkuPoMapper skuPoMapper;
    @Autowired
    SpuPoMapper spuPoMapper;
    @Autowired
    FloatPricePoMapper floatPricePoMapper;

    @Autowired
    RedisTemplate redisTemplate;


    /**
     * 管理员添加新的sku到spu里
     * @param sku
     * @return
     */
    public ReturnObject<Sku>insertSku(Long shopId,Sku sku){
        SkuPo skuPo=sku.gotSkuPo();
        ReturnObject<Sku> retObj=null;
        SpuPo spPo;
        logger.info("1");
        try{
            spPo=spuPoMapper.selectByPrimaryKey(sku.getGoodsSpuId());
            logger.info("2");
            if(spPo==null)
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
            if(spPo.getShopId()!=shopId)
            {
                logger.info("shop id error");
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE);
            }
            int ret = skuPoMapper.insertSelective(skuPo);
            logger.info("3");
            if (ret == 0) {
                //插入失败
                logger.info("insertRole: insert sku fail " + skuPo.toString());
                retObj = new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST, String.format("新增失败：" + skuPo.getName()));
            } else {
                //插入成功
                logger.info("insertRole: insert sku = " + skuPo.toString());
                sku.setId(skuPo.getId());
                sku.setDisabled(skuPo.getDisabled());
                logger.info("dis:"+sku.getDisabled()+" sn:"+sku.getSkuSn());
                retObj = new ReturnObject<>(sku);
            }
        }
        catch (DataAccessException e) {
            if (Objects.requireNonNull(e.getMessage()).contains("auth_role.auth_role_name_uindex")) {
                //若有重复的角色名则新增失败
                logger.info("updateRole: have same sku name = " + skuPo.getName());
                retObj = new ReturnObject<>(ResponseCode.ROLE_REGISTERED, String.format("sku名重复：" + skuPo.getName()));
            } else {
                // 其他数据库错误
                logger.info("other sql exception : " + e.getMessage());
                retObj = new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
            }
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.info("other exception : " + e.getMessage());
            retObj = new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
        return retObj;
    }

    /**
     * 店家商品上架
     * @param id
     * @return
     */
    public ReturnObject<ResponseCode> putGoodsOnSale(Long shopId, Long id) {
        //SKU存在
        SkuPo skuPo=skuPoMapper.selectByPrimaryKey(id);
        if(skuPo==null||Sku.State.getTypeByCode(skuPo.getState().intValue()).equals(Sku.State.DELETED))
            return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);

        Sku.State state=Sku.State.getTypeByCode(skuPo.getState().intValue());
        //shopId能对上号
        SpuPo spuPo= spuPoMapper.selectByPrimaryKey(skuPo.getGoodsSpuId());
        if(!spuPo.getShopId().equals(shopId))return new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE);

        //SKU非已上架
        if(state.equals(Sku.State.ONSHELF))return new ReturnObject<>(ResponseCode.STATE_NOCHANGE);

        skuPo.setState(Sku.State.ONSHELF.getCode().byteValue());
        try{
            int ret=skuPoMapper.updateByPrimaryKeySelective(skuPo);
            if(ret==0)
            {
                logger.debug("putGoodsOnSale fail:skuPo="+skuPo);
                return new ReturnObject<>(ResponseCode.FIELD_NOTVALID, "sku字段不合法：" + skuPo.toString());
            }
            else return new ReturnObject<>();
        }
        catch (DataAccessException e)
        {
            // 其他数据库错误
            logger.debug("other sql exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    /**
     * 店家商品下架
     * @param id
     * @return
     */
    public ReturnObject<ResponseCode> putOffGoodsOnSale(Long shopId, Long id) {
        //SKU存在
        SkuPo skuPo=skuPoMapper.selectByPrimaryKey(id);
        if(skuPo==null||Sku.State.getTypeByCode(skuPo.getState().intValue()).equals(Sku.State.DELETED))
            return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);

        Sku.State state=Sku.State.getTypeByCode(skuPo.getState().intValue());
        //shopId能对上号
        SpuPo spuPo= spuPoMapper.selectByPrimaryKey(skuPo.getGoodsSpuId());
        if(!spuPo.getShopId().equals(shopId))return new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE);

        //SKU非已下架
        if(state.equals(Sku.State.OFFSHELF))return new ReturnObject<>(ResponseCode.STATE_NOCHANGE);

        skuPo.setState(Sku.State.OFFSHELF.getCode().byteValue());
        try{
            int ret=skuPoMapper.updateByPrimaryKeySelective(skuPo);
            if(ret==0)
            {
                logger.debug("putGoodsOnSale fail:skuPo="+skuPo);
                return new ReturnObject<>(ResponseCode.FIELD_NOTVALID, "sku字段不合法：" + skuPo.toString());
            }
            else  return new ReturnObject<>();
        }
        catch (DataAccessException e)
        {
            // 其他数据库错误
            logger.debug("other sql exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    /**
     * 查询sku
     * @param shopId
     * @param skuSn
     * @param spuId
     * @param spuSn
     * @param page
     * @param pageSize
     * @return
     */
    public PageInfo<Sku> getSkuList(Long shopId, String skuSn, Long spuId, String spuSn, Integer page, Integer pageSize) {

        SkuPoExample skuExample=new SkuPoExample();
        SkuPoExample.Criteria skuCriteria=skuExample.createCriteria();
        if(skuSn!=null&&!skuSn.isBlank())skuCriteria.andSkuSnEqualTo(skuSn);
        if(spuId!=null)skuCriteria.andGoodsSpuIdEqualTo(spuId);
        List<SkuPo> skuPos=new ArrayList<>();
        PageHelper.startPage(page,pageSize);
        logger.debug("page="+page+" pageSize="+pageSize);
        if((spuSn!=null&&!spuSn.isBlank())||shopId!=null)
        {
            logger.info("get By spuSn:"+spuSn+"or shopId:"+shopId);
            SpuPoExample spuExample=new SpuPoExample();
            SpuPoExample.Criteria spuCriteria= spuExample.createCriteria();
            if(spuSn!=null&&!spuSn.isBlank())spuCriteria.andGoodsSnEqualTo(spuSn);
            if(shopId!=null)spuCriteria.andShopIdEqualTo(shopId);
            List<SpuPo> spuPos=spuPoMapper.selectByExample(spuExample);
            for (SpuPo spuPo:spuPos)
            {

                skuCriteria.andGoodsSpuIdEqualTo(spuPo.getId());
                skuPos.addAll(skuPoMapper.selectByExample(skuExample));

            }
            logger.info("get: "+skuPos.size());
        }
        else {
            logger.info("get By other:");
            skuPos.addAll(skuPoMapper.selectByExample(skuExample));
            logger.info("get: "+skuPos.size());
        }
      /* List<Sku>skus=new ArrayList<>(skuPos.size());
        for(SkuPo skuPo: skuPos)
        {
            Sku skutmp=new Sku(skuPo);
            skus.add(skutmp);
        }*/
        List<Sku>skus=skuPos.stream().map(Sku::new).collect(Collectors.toList());
        //logger.info("get: "+skus.size());
        PageInfo<Sku> returns=PageInfo.of(skus);
        returns.setPageNum(page);
        returns.setPageSize(pageSize);
        return returns;
    }

    public ReturnObject<Long> getPriceBySkuId(Long skuId) {
        SkuPo sku = null;
        try {
            sku = skuPoMapper.selectByPrimaryKey(skuId);
        } catch (Exception e) {
            StringBuilder message = new StringBuilder().append("getPriceBySkuId: ").append(e.getMessage());
            logger.error(message.toString());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }
        if(sku==null|| Sku.State.getTypeByCode(sku.getState().intValue()).equals(Sku.State.DELETED))
            return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
        FloatPricePoExample example = new FloatPricePoExample();
        FloatPricePoExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsSkuIdEqualTo(skuId);
        criteria.andBeginTimeLessThanOrEqualTo(LocalDateTime.now());
        criteria.andEndTimeGreaterThan(LocalDateTime.now());
        Byte state=1;
        criteria.andValidEqualTo(state);

        /*查询浮动表*/
        List<FloatPricePo> floatPricePo = null;
        try {
            floatPricePo = floatPricePoMapper.selectByExample(example);
        } catch (Exception e) {
            StringBuilder message = new StringBuilder().append("getPriceBySkuId: ").append(e.getMessage());
            logger.error(message.toString());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }
        if(!(floatPricePo==null||floatPricePo.size()==0||floatPricePo.get(0).getQuantity().equals(0))) {
            return new ReturnObject<>(floatPricePo.get(0).getActivityPrice());
        }else {
            return new ReturnObject<>(sku.getOriginalPrice());
        }
    }

    public ReturnObject<List<Long>> getAllSkuIdByShopId(Long shopId)
    {
        SpuPoExample spuExample=new SpuPoExample();
        SpuPoExample.Criteria spuCriteria=spuExample.createCriteria();
        spuCriteria.andShopIdEqualTo(shopId);
        List<SpuPo>spuPos=spuPoMapper.selectByExample(spuExample);

        //查不到shop下的spu
        if(spuPos==null||spuPos.size()==0)return new ReturnObject<>(null);

        List<Long>skuIds=new ArrayList<>();
        for(SpuPo spuPo:spuPos)
        {
            if(spuPo.getDisabled().equals((byte)0)) {
                SkuPoExample skuExample = new SkuPoExample();
                SkuPoExample.Criteria skuCriteria = skuExample.createCriteria();
                skuCriteria.andGoodsSpuIdEqualTo(spuPo.getId());
                List<SkuPo> skuPos = skuPoMapper.selectByExample(skuExample);

                //查得到spu下的sku
                if (skuPos != null && skuPos.size() > 0)
                    skuIds.addAll(skuPos.stream().map(SkuPo::getId).collect(Collectors.toList()));
            }
        }
        return new ReturnObject<>(skuIds);
    }

    public ReturnObject<Long> getShopIdBySkuId(Long skuId)
    {
        SkuPo skuPo= null;
        try {
            skuPo = skuPoMapper.selectByPrimaryKey(skuId);
        } catch (Exception e) {
            StringBuilder message = new StringBuilder().append("getShopIdBySkuId: ").append(e.getMessage());
            logger.error(message.toString());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }

        //查不到sku
        if(skuPo==null|| Sku.State.getTypeByCode(skuPo.getState().intValue()).equals(Sku.State.DELETED))
            return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);

        SpuPo spuPo= null;
        try {
            spuPo = spuPoMapper.selectByPrimaryKey(skuPo.getGoodsSpuId());
        } catch (Exception e) {
            StringBuilder message = new StringBuilder().append("getShopIdBySkuId: ").append(e.getMessage());
            logger.error(message.toString());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }

        //查不到spu
        if(spuPo==null)
            return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);

        return new ReturnObject<>(spuPo.getShopId());
    }

    public SkuInfoDTO getSelectSkuInfoBySkuId(Long skuId)
    {
        SkuPo skuPo=skuPoMapper.selectByPrimaryKey(skuId);

        //查不到sku
        if(skuPo==null|| Sku.State.getTypeByCode(skuPo.getState().intValue()).equals(Sku.State.DELETED))return null;

        SkuInfoDTO skuInfoDTO=new SkuInfoDTO();
        skuInfoDTO.setDisable(skuPo.getDisabled());
        skuInfoDTO.setImageUrl(skuPo.getImageUrl());
        skuInfoDTO.setInventory(skuPo.getInventory());
        skuInfoDTO.setName(skuPo.getName());
        skuInfoDTO.setOriginalPrice(skuPo.getOriginalPrice());
        skuInfoDTO.setId(skuPo.getId());
        skuInfoDTO.setSkuSn(skuPo.getSkuSn());
        return skuInfoDTO;
    }

    public ReturnObject<ResponseCode> modifySku(Long shopId, Sku sku) {
        SkuPo selectSkuPo=skuPoMapper.selectByPrimaryKey(sku.getId());
        if(selectSkuPo==null)// Sku.State.getTypeByCode(selectSkuPo.getState().intValue())== Sku.State.DELETED)
            return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);

        //shopId和spuId匹配
        SpuPoExample spuPoExample=new SpuPoExample();
        SpuPoExample.Criteria criteria1=spuPoExample.createCriteria();
        criteria1.andShopIdEqualTo(shopId);
        criteria1.andIdEqualTo(selectSkuPo.getGoodsSpuId());
        List<SpuPo> spuPos=spuPoMapper.selectByExample(spuPoExample);
        if(spuPos.size()==0)return new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE);

        //同SPU下SKU不重名
        SkuPoExample skuExample=new SkuPoExample();
        SkuPoExample.Criteria criteria=skuExample.createCriteria();
        criteria.andGoodsSpuIdEqualTo(selectSkuPo.getGoodsSpuId());
        List<SkuPo>skuPos=skuPoMapper.selectByExample(skuExample);
        for(SkuPo po:skuPos)
            if(po.getName().equals(sku.getName())&&!po.getId().equals(sku.getId()))return new ReturnObject<>(ResponseCode.SKUSN_SAME, String.format("SKU名重复：" + selectSkuPo.getName()));


        //更新redis
        String key="sku_"+sku.getId();
        redisTemplate.opsForHash().delete(key,"quantity");
        redisTemplate.opsForHash().delete(key,"price");
        redisTemplate.opsForHash().put(key,"quantity",sku.getInventory());
        redisTemplate.opsForHash().put(key,"price",sku.getOriginalPrice());
        //尝试修改
        SkuPo skuPo=sku.gotSkuPo();
        skuPo.setGmtModified(LocalDateTime.now());
        try{
            int ret = skuPoMapper.updateByPrimaryKeySelective(skuPo);
            if (ret == 0)
            {
                //修改失败
                logger.debug("modifySku: update sku fail : " + skuPo.toString());
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST, "skuId不存在：" + skuPo.getId());
            }
            else {
                //修改成功
                logger.debug("modifySku: update sku = " + skuPo.toString());
                return new ReturnObject<>();
            }
        }
        catch (DataAccessException e)
        {
            if (Objects.requireNonNull(e.getMessage()).contains("goods_sku.goods_sku_name_uindex"))
            {
                logger.debug("modifySku: have same sku name = " + skuPo.getName());
                return new ReturnObject<>(ResponseCode.FIELD_NOTVALID, "SKU名重复：" + skuPo.getName());
            }
            else {
                // 其他数据库错误
                logger.debug("other sql exception : " + e.getMessage());
                return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
            }
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    /**
     * 店家删除sku
     * @param shopId
     * @param id
     * @return
     */
    public ReturnObject<ResponseCode> logicalDelete(Long shopId, Long id)
    {
        SkuPo skuPo=skuPoMapper.selectByPrimaryKey(id);
        if(skuPo==null)//|| Sku.State.getTypeByCode(skuPo.getState().intValue())== Sku.State.DELETED)
            return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
      //  if(Sku.State.getTypeByCode(skuPo.getState().intValue())== Sku.State.DELETED)
        //    return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);

        SpuPo spuPo=spuPoMapper.selectByPrimaryKey(skuPo.getGoodsSpuId());
        if(spuPo.getShopId().equals(shopId))
        {
            skuPo.setState(Sku.State.DELETED.getCode().byteValue());
            skuPo.setGmtModified(LocalDateTime.now());
            String key="sku_"+id;
            if(redisTemplate.opsForHash().hasKey(key,"quantity"))
            {
                redisTemplate.opsForHash().delete(key,"quantity");
                redisTemplate.opsForHash().delete(key,"price");
            }
            int ret=skuPoMapper.updateByPrimaryKey(skuPo);
            if(ret==0)
            {
                logger.debug("logicalDelete:update fail.sku id="+id);
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            FloatPricePo floatPo=new FloatPricePo();
            floatPo.setGoodsSkuId(skuPo.getId());
            floatPo.setValid(FloatPrice.State.INVALID.getCode().byteValue());
            floatPricePoMapper.updateByPrimaryKeySelective(floatPo);
            return new ReturnObject<>();
        }
        else return new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE);
    }

    /**
     * 通过id查询sku
     * @param id
     * @return
     */
    public ReturnObject<Sku> getSku(Long id)
    {
        SkuPo po=skuPoMapper.selectByPrimaryKey(id);

        if(po==null||Sku.State.getTypeByCode(po.getState().intValue())== Sku.State.DELETED)
        {

            return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        Sku sku=new Sku(po);
        logger.info("getSku:dao");
        return new ReturnObject<>(sku);
    }

    public SkuPo getSkuBySkuId(Long id) {
        SkuPo po=new SkuPo();
        ReturnObject<Object> retObj = null;
        SkuPoExample skuPo = new  SkuPoExample();
        SkuPoExample.Criteria criteria = skuPo.createCriteria();
        criteria.andIdEqualTo(id);
        try {
            po = skuPoMapper.selectByPrimaryKey(id);
        } catch (Exception e) {
            logger.error("getSku id= ",id," : DataAccessException:" + e.getMessage());
            return null;
        }
        if(po==null)
        {
            logger.info("sku不存在或已被删除：id = " + id);
            return null;
        }
        return po;
    }

    /**
     * 新增价格浮动
     * @param shopId
     * @param floatPrice
     * @param userId
     * @return
     */
    public ReturnObject<FloatPriceRetVo> addFloatPrice(Long shopId, FloatPrice floatPrice, Long userId) {
        SkuPo skuPo;
        try{
            logger.info("1");
        skuPo=skuPoMapper.selectByPrimaryKey(floatPrice.getGoodsSkuId());
        }
        catch (Exception e)
        {
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }logger.info("2");
        if(skuPo==null|| Sku.State.getTypeByCode(skuPo.getState().intValue())== Sku.State.DELETED)
            return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
        logger.info("3");
        if(skuPo.getInventory()<floatPrice.getQuantity())
            return new ReturnObject<>(ResponseCode.SKU_NOTENOUGH, "库存不足：" + floatPrice.getGoodsSkuId());
        //shopId是否能和skuId匹配
        logger.info("4");
        SpuPoExample spuPoExample=new SpuPoExample();
        SpuPoExample.Criteria criteria=spuPoExample.createCriteria();
        criteria.andShopIdEqualTo(shopId);
        criteria.andIdEqualTo(skuPo.getGoodsSpuId());
        List<SpuPo> spuPos=spuPoMapper.selectByExample(spuPoExample);
        if(spuPos.size()==0)
            return new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE);

        //时间不冲突
        FloatPricePoExample nowExample=new FloatPricePoExample();
        FloatPricePoExample.Criteria nowCriteria=nowExample.createCriteria();
        nowCriteria.andGoodsSkuIdEqualTo(floatPrice.getGoodsSkuId());
        nowCriteria.andBeginTimeLessThanOrEqualTo(floatPrice.getEndTime());
        nowCriteria.andEndTimeGreaterThanOrEqualTo(floatPrice.getBeginTime());
        List<FloatPricePo> nowFloatPos=floatPricePoMapper.selectByExample(nowExample);
        if(nowFloatPos.size()>0)
            return new ReturnObject<>(ResponseCode.SKUPRICE_CONFLICT, "floatPrice时间冲突：已有" + nowFloatPos.toString());
        logger.info("5");
        //尝试插入
        FloatPricePo floatPricePo=floatPrice.getFloatPricePo();
        floatPricePo.setGmtCreate(LocalDateTime.now());
        floatPricePo.setGmtModified(LocalDateTime.now());
        try{
            int ret = floatPricePoMapper.insertSelective(floatPricePo);
            logger.info("6");
            if (ret == 0)
            {
                //修改失败
                logger.debug("addFloatPrice: insert floatPrice fail : " + floatPricePo.toString());
                return new ReturnObject<>(ResponseCode.FIELD_NOTVALID, "floatPrice字段不合法：" + floatPricePo.toString());
            }
            else {
                //修改成功
                logger.debug("addFloatPrice: insert floatPrice = " + floatPricePo.toString());
                //检验
                FloatPricePoExample floatExample=new FloatPricePoExample();
                FloatPricePoExample.Criteria criteria1=floatExample.createCriteria();
                criteria1.andGoodsSkuIdEqualTo(floatPrice.getGoodsSkuId());
                criteria1.andBeginTimeEqualTo(floatPrice.getBeginTime());
                criteria1.andEndTimeEqualTo(floatPrice.getEndTime());
                criteria1.andActivityPriceEqualTo(floatPrice.getActivityPrice());
                criteria1.andQuantityEqualTo(floatPrice.getQuantity());
                List<FloatPricePo> checkFloatPo=floatPricePoMapper.selectByExample(floatExample);
                if(checkFloatPo.size()==0)
                    return new ReturnObject<>(ResponseCode.FIELD_NOTVALID, "floatPrice字段不合法：" + floatPricePo.toString());
                else {//构造FloatPriceRetVo
                    FloatPriceRetVo retVo=new FloatPriceRetVo(new FloatPrice(checkFloatPo.get(0)));
                    return new ReturnObject<>(retVo);
                }
            }
        }
        catch (DataAccessException e)
        {
            // 其他数据库错误
            logger.debug("other sql exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    /**
     * 删除价格浮动
     * @param shopId
     * @param id
     * @return
     */
    public ReturnObject deleteFloatPrice(Long shopId, Long id) {

        FloatPricePo fpPo;
        SkuPo skuPo;
        try{
            fpPo=floatPricePoMapper.selectByPrimaryKey(id);
            if(fpPo==null||fpPo.getValid()==FloatPrice.State.INVALID.getCode().byteValue())
            {
                logger.info("价格浮动不存在");
                return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            skuPo=skuPoMapper.selectByPrimaryKey(fpPo.getGoodsSkuId());
            if(skuPo==null|| Sku.State.getTypeByCode(skuPo.getState().intValue())== Sku.State.DELETED)
            {
                logger.info("sku不存在");
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            SpuPoExample spuPoExample=new SpuPoExample();
            SpuPoExample.Criteria criteria=spuPoExample.createCriteria();
            criteria.andShopIdEqualTo(shopId);
            criteria.andIdEqualTo(skuPo.getGoodsSpuId());
            List<SpuPo> spuPos=spuPoMapper.selectByExample(spuPoExample);
            if(spuPos.size()==0){
                logger.info("shop id error");
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE);
            }
            logger.info("can be delete");
            //进行修改状态进行删除
            FloatPricePo newPo=fpPo;
            newPo.setValid(FloatPrice.State.INVALID.getCode().byteValue());
            int ret=floatPricePoMapper.updateByPrimaryKeySelective(newPo);
            if(ret==0)
                return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            else
                return new ReturnObject(ResponseCode.OK);
        }
        catch (Exception e)
        {
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }
    }

}
