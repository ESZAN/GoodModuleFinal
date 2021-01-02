package com.ooad.good.service;

import cn.edu.xmu.ooad.util.ResponseCode;
import cn.edu.xmu.ooad.util.ReturnObject;
import cn.edu.xmu.oomall.goods.model.GoodsDetailDTO;
import cn.edu.xmu.privilegeservice.client.IUserService;
import com.github.pagehelper.PageInfo;
import com.ooad.good.dao.SkuDao;
import com.ooad.good.model.bo.FloatPrice;
import com.ooad.good.model.bo.Groupon;
import com.ooad.good.model.bo.Sku;
import com.ooad.good.model.bo.Spu;
import com.ooad.good.model.po.FlashSaleItemPo;
import com.ooad.good.model.po.SkuPo;
import com.ooad.good.model.po.SpuPo;
import com.ooad.good.model.vo.CreatedBy;
import com.ooad.good.model.vo.ModifiedBy;
import com.ooad.good.model.vo.floatPrice.FloatPriceRetVo;
import com.ooad.good.service.impl.FlashsaleServiceImpl;
import com.ooad.good.service.impl.IGoodsServiceImpl;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.color.ICC_Profile;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class SkuService {
    private  static  final Logger logger = LoggerFactory.getLogger(SkuService.class);

    @Autowired
    private SkuDao skuDao;

    @DubboReference(check = false)
    private IUserService iUserService;



    /**
     * 修改sku信息
     * @param shopId
     * @param bo
     * @return
     */
    @Transactional
    public ReturnObject<ResponseCode> modifySku(Long shopId, Sku bo)
    {
        return skuDao.modifySku(shopId,bo);
    }

    /**
     * 管理员添加新的sku到spu里
     * @param sku
     * @return
     */
    @Transactional
    public ReturnObject insertSku(Long shopid,Sku sku){
        ReturnObject<Sku> retObj=skuDao.insertSku(shopid,sku);
        return retObj;
    }

    /**
     * 店家上架商品
     * @param id
     * @return
     */
    @Transactional
    public ReturnObject<ResponseCode> putGoodsOnSale(Long shopId, Long id) {
        ReturnObject<ResponseCode> returnObject=skuDao.putGoodsOnSale(shopId,id);
        return returnObject;
    }


    /**
     * 店家下架商品
     * @param id
     * @return
     */
    public ReturnObject<ResponseCode> putOffGoodsOnSale(Long shopId,Long id)
    {
        ReturnObject<ResponseCode> returnObject=skuDao.putOffGoodsOnSale(shopId,id);
        return returnObject;
    }


    /**
     * 查询所有sku
     * @param shopId
     * @param skuSn
     * @param spuId
     * @param spuSn
     * @param page
     * @param pageSize
     * @return
     */
    public ReturnObject<PageInfo<Sku>> getSkuList(Long shopId, String skuSn, Long spuId, String spuSn, Integer page, Integer pageSize) {
        PageInfo<Sku> skuRetVos=skuDao.getSkuList(shopId,skuSn,spuId,spuSn,page,pageSize);
        if(skuRetVos!=null&&skuRetVos.getList().size()>0)
            for(Sku sku:skuRetVos.getList())
                sku.setPrice(sku.getOriginalPrice());
          /*  for(Sku sku:skuRetVos.getList()){
                sku.setPrice(getPriceBySkuId(sku.getId()).getData());
            }*/
        return new ReturnObject<>(skuRetVos);
    }




    /**
     * 店家删除sku
     * @param shopId
     * @param id
     * @return
     */
    @Transactional
    public ReturnObject<ResponseCode> deleteSku(Long shopId, Long id)
    {
        return skuDao.logicalDelete(shopId,id);
    }

    @Transactional
    public ReturnObject<Sku> getSkuBySkuId(Long id) {
        SkuPo po=skuDao.getSkuBySkuId(id);
        Sku bo=null;
        if(po!=null)
            bo=new Sku(po);
        return new ReturnObject<>(bo);
    }

    /**
     * 获取sku
     * @param id
     * @return
     */
    @Transactional
    public ReturnObject<Sku> getSku(Long id) {
        return  skuDao.getSku(id);
    }

    /**
     * 新增价格浮动
     * @param shopId
     * @param floatPrice
     * @param userId
     * @return
     */
    public ReturnObject addFloatPrice(Long shopId, FloatPrice floatPrice, Long userId) {
        ReturnObject<FloatPriceRetVo> returnObject= skuDao.addFloatPrice(shopId,floatPrice,userId);
        if(returnObject.getData()!=null)
        {
            CreatedBy createdBy=new CreatedBy();
            ModifiedBy modifiedBy=new ModifiedBy();
            //调用权限模块接口获取用户
            String userName=iUserService.getUserName(userId);
            createdBy.set(userId,userName);
            modifiedBy.set(userId,userName);
            returnObject.getData().setCreatedBy(createdBy);
            returnObject.getData().setModifiedBy(modifiedBy);
        }
        return returnObject;
    }

    /**
     * 删除价格浮动
     * @param shopId
     * @param id
     * @return
     */
    public ReturnObject deleteFloatPrice(Long shopId, Long id) {
        ReturnObject returnObject=skuDao.deleteFloatPrice(shopId,id);
        return returnObject;
    }
}
