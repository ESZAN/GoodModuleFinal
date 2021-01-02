package com.ooad.good.dao;

import cn.edu.xmu.ooad.util.ResponseCode;
import cn.edu.xmu.ooad.util.ReturnObject;
import cn.edu.xmu.oomall.goods.model.CouponInfoDTO;
import com.ooad.good.controller.CouponController;
import com.ooad.good.mapper.CouponActivityPoMapper;
import com.ooad.good.mapper.CouponSkuPoMapper;
import com.ooad.good.model.bo.*;
import com.ooad.good.model.po.*;
import com.ooad.good.model.vo.couponActivity.CouponActivityDetailVo;
import com.ooad.good.model.vo.couponActivity.CouponActivityVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
public class CouponDao {
    private  static  final Logger logger = LoggerFactory.getLogger(CouponDao.class);

    @Autowired
    private CouponActivityPoMapper couponActivityPoMapper;
    @Autowired
    private CouponSkuPoMapper couponSkuMapper;
    /**
     * 管理员新建己方优惠活动
     * @param couponActivity
     * @return
     */
    public ReturnObject<CouponActivity>insertCouponActivity(CouponActivity couponActivity){

        CouponActivityPo couponActivityPo=couponActivity.gotCouponActivityPo();
        ReturnObject<CouponActivity>retObj=null;
        try{
            int ret = couponActivityPoMapper.insertSelective(couponActivityPo);
            if (ret == 0) {
                //插入失败
                logger.debug("insertCouponActivity: insert couponActivity fail " + couponActivityPo.toString());
                retObj = new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST, String.format("新增失败：" + couponActivityPo.getName()));
            } else {
                //插入成功
                logger.debug("insertRole: insert couponActivity = " + couponActivityPo.toString());
                couponActivity.setId(couponActivityPo.getId());
                retObj = new ReturnObject<>(couponActivity);
            }
        }
        catch (DataAccessException e) {
            if (Objects.requireNonNull(e.getMessage()).contains("auth_role.auth_role_name_uindex")) {
                //若有重复的couponActivity名则新增失败
                logger.debug("insert CouponActivity: have same role name = " + couponActivityPo.getName());
                retObj = new ReturnObject<>(ResponseCode.ROLE_REGISTERED, String.format("couponActivity名重复：" + couponActivityPo.getName()));
            } else {
                // 其他数据库错误
                logger.debug("sql exception : " + e.getMessage());
                retObj = new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
            }
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("exception : " + e.getMessage());
            retObj = new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
        return retObj;
    }


    /**
     * 管理员删除己方优惠活动
     * @param id
     * @return
     */
    public ReturnObject<Object>deleteCouponActivity(Long shopId,Long id){
        CouponActivityPo couponActivityPo=new CouponActivityPo();
        CouponActivityPoExample activityPoExample=new CouponActivityPoExample();
        CouponActivityPoExample.Criteria criteria=activityPoExample.createCriteria();
        criteria.andIdEqualTo(id);
        criteria.andShopIdEqualTo(shopId);

        try {
            couponActivityPo = couponActivityPoMapper.selectByPrimaryKey(id);
            if(couponActivityPo==null)
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
            else if(!couponActivityPo.getShopId().equals(shopId))
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE);
        }
        catch (Exception e)
        {
            logger.error("exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }
        ReturnObject<CouponActivity>retObj=null;
        if(couponActivityPo==null||couponActivityPo.getState()==2){
            //优惠活动不存在或已被删除
            logger.info("coupon activity do not exist");
            return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        try {
            List<CouponActivityPo> activityPo=couponActivityPoMapper.selectByExample(activityPoExample);
            if(activityPo==null||activityPo.get(0).getState().equals((byte)2))
            {
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            if(activityPo.get(0).getState().equals((byte)1))
            {
                logger.info("优惠活动处于上架中无法被删除");
                return new ReturnObject<>(ResponseCode.COUPONACT_STATENOTALLOW);
            }
            activityPo.get(0).setState((byte)2);
            int ret =  couponActivityPoMapper.updateByExampleSelective(activityPo.get(0),activityPoExample);
            if(ret==0){//修改失败
                logger.info("update CouponActivity fail:"+activityPo.toString());
                return new ReturnObject<>(ResponseCode.COUPONACT_STATENOTALLOW);
            }
            else{//修改活动状态成功
                logger.info("update CouponActivity success:"+activityPo.toString());
                return new ReturnObject<>();
            }
        }
        catch (Exception e)
        {
            logger.error("exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }
    }


    /**
     * 管理员修改己方优惠活动
     * @param couponActivity
     * @return
     */
    public ReturnObject<CouponActivity>updateCouponActivity(CouponActivity couponActivity){
        CouponActivityPo oldPo=new CouponActivityPo();
        try {
            oldPo = couponActivityPoMapper.selectByPrimaryKey(couponActivity.getId());
        } catch (Exception e) {
            StringBuilder message = new StringBuilder().append("modifyGrouponofSPU:select: ").append(e.getMessage());
            logger.error(message.toString());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }
        if(oldPo == null)
            return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
        if(oldPo.getState()== ActivityStatus.DELETED.getCode().byteValue())
            return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
        //3.若状态不为下线，则禁止
        if(oldPo.getState()!= ActivityStatus.OFF_SHELVES.getCode().byteValue())
            return new ReturnObject<>(ResponseCode.COUPONACT_STATENOTALLOW);

        CouponActivityPo couponActivityPo=couponActivity.gotCouponActivityPo();
        ReturnObject<CouponActivity>retObj=null;
        CouponActivityPoExample example=new CouponActivityPoExample();
        CouponActivityPoExample.Criteria criteria=example.createCriteria();
        criteria.andIdEqualTo(couponActivity.getId());

        try{
            int ret = couponActivityPoMapper.updateByExampleSelective(couponActivityPo, example);
//            int ret = roleMapper.updateByPrimaryKeySelective(rolePo);
            if (ret == 0) {
                //修改失败
                logger.debug("updateCouponActivity: update CouponActivity fail : " + couponActivityPo.toString());
                retObj = new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST, String.format("优惠活动id不存在：" + couponActivityPo.getId()));
            } else {
                //修改成功
                logger.debug("updateCouponActivity: update CouponActivity = " + couponActivityPo.toString());
                retObj = new ReturnObject<>();
            }
        }
        catch (DataAccessException e) {
            if (Objects.requireNonNull(e.getMessage()).contains("auth_role.auth_role_name_uindex")) {
                //若有重复的角色名则修改失败
                logger.debug("updateCouponActivity: have same CouponActivity name = " + couponActivityPo.getName());
                retObj = new ReturnObject<>(ResponseCode.ROLE_REGISTERED, String.format("优惠活动名重复：" + couponActivityPo.getName()));
            } else {
                // 其他数据库错误
                logger.debug("other sql exception : " + e.getMessage());
                retObj = new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
            }
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            retObj = new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
        return retObj;
    }


    /**
     * 上线优惠活动
     * @param id
     * @return
     */
    public ReturnObject<ResponseCode>onlineCouponactivity(Long shopId,Long id){
        CouponActivityPo couponActivityPo=new CouponActivityPo();
        CouponActivityPoExample activityPoExample=new CouponActivityPoExample();
        CouponActivityPoExample.Criteria criteria=activityPoExample.createCriteria();
        criteria.andIdEqualTo(id);
        criteria.andShopIdEqualTo(shopId);

        try {
            couponActivityPo = couponActivityPoMapper.selectByPrimaryKey(id);
            if(couponActivityPo==null)
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
            else if(!couponActivityPo.getShopId().equals(shopId))
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE);
        }
        catch (Exception e)
        {
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }
        ReturnObject<CouponActivity>retObj=null;
        if(couponActivityPo==null||couponActivityPo.getState()==2){
            //优惠活动不存在或已被删除
            logger.info("couponactivity not exist");
            return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        logger.info("onlineCouponactivity: successful: couponactivity name = " + couponActivityPo.getName());
        try {
            List<CouponActivityPo> activityPo=couponActivityPoMapper.selectByExample(activityPoExample);
            if(activityPo==null)//未找到符合条件的优惠活动
            {
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            if(activityPo.get(0).getState().equals((byte)1)){
                return new ReturnObject<>(ResponseCode.STATE_NOCHANGE);
            }
            if(activityPo.get(0).getState().equals((byte)2)){
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            activityPo.get(0).setState((byte)1);//活动状态修改为1
            int ret =  couponActivityPoMapper.updateByExampleSelective(activityPo.get(0),activityPoExample);
            if(ret==0){//修改失败
                logger.info("updateCouponActivity fail:"+activityPo.toString());
                return new ReturnObject<>(ResponseCode.COUPONACT_STATENOTALLOW);
            }
            else{//修改活动状态成功
                logger.info("updateCouponActivity success:"+activityPo.toString());
                return new ReturnObject<>();
            }
        }
        catch (Exception e)
        {
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }
    }

    /**
     * 下线优惠活动
     * @param id
     * @return
     */
    public ReturnObject<ResponseCode> offlineCouponactivity(Long shopId,Long id){
        CouponActivityPo couponActivityPo=new CouponActivityPo();
        CouponActivityPoExample activityPoExample=new CouponActivityPoExample();
        CouponActivityPoExample.Criteria criteria=activityPoExample.createCriteria();
        criteria.andIdEqualTo(id);
        criteria.andShopIdEqualTo(shopId);

        try {
            couponActivityPo = couponActivityPoMapper.selectByPrimaryKey(id);
            if(couponActivityPo==null)
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
            else if(!couponActivityPo.getShopId().equals(shopId))
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE);
        }
        catch (Exception e)
        {
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }
        ReturnObject<CouponActivity>retObj=null;
        if(couponActivityPo==null||couponActivityPo.getState()==2){
            //优惠活动不存在或已被删除
            logger.info("couponactivity not exist");
            return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        logger.info("onlineCouponactivity: successful: couponactivity name = " + couponActivityPo.getName());
        try {
            List<CouponActivityPo> activityPo=couponActivityPoMapper.selectByExample(activityPoExample);
            if(activityPo==null)//未找到符合条件的优惠活动
            {
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            if(activityPo.get(0).getState().equals((byte)0)){
                return new ReturnObject<>(ResponseCode.STATE_NOCHANGE);
            }
            if(activityPo.get(0).getState().equals((byte)2)){
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            activityPo.get(0).setState((byte)0);
            int ret =  couponActivityPoMapper.updateByExampleSelective(activityPo.get(0),activityPoExample);
            if(ret==0){//修改失败
                logger.info("updateCouponActivity fail:"+activityPo.toString());
                return new ReturnObject<>(ResponseCode.COUPONACT_STATENOTALLOW);
            }
            else{//修改活动状态成功
                logger.info("updateCouponActivity success:"+activityPo.toString());
                return new ReturnObject<>();
            }
        }
        catch (Exception e)
        {
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }
    }

    public List<CouponInfoDTO> getCouponInfoBySkuId(Long skuId)
    {
        CouponActivityPo activityPo;
        CouponSkuPoExample couponSkuExample=new CouponSkuPoExample();
        CouponSkuPoExample.Criteria couponSkuCriteria=couponSkuExample.createCriteria();
        couponSkuCriteria.andSkuIdEqualTo(skuId);
        List<CouponSkuPo> couponSkuPos=couponSkuMapper.selectByExample(couponSkuExample);
        List<CouponInfoDTO> couponInfoDTOs=new ArrayList<>();
        for(CouponSkuPo couponSkuPo:couponSkuPos)
        {
            activityPo=couponActivityPoMapper.selectByPrimaryKey(couponSkuPo.getActivityId());
            LocalDateTime beginTime=activityPo.getBeginTime();
            LocalDateTime endTime=activityPo.getEndTime();
            if(CouponActivity.DatabaseState.getTypeByCode(activityPo.getState().intValue()).equals(CouponActivity.DatabaseState.ONLINE)&&
                    !beginTime.isAfter(LocalDateTime.now())&&endTime.isAfter(LocalDateTime.now()))
            {
                CouponInfoDTO couponInfoDTO=new CouponInfoDTO();
                couponInfoDTO.setBeginTime(beginTime);
                couponInfoDTO.setEndTime(endTime);
                couponInfoDTO.setId(activityPo.getId());
                couponInfoDTO.setName(activityPo.getName());
                couponInfoDTOs.add(couponInfoDTO);
            }
        }
        return couponInfoDTOs;
    }

    public ReturnObject<CouponActivity> getCouponActivity(Long shopId, Long id) {
        CouponActivityPo activityPo=couponActivityPoMapper.selectByPrimaryKey(id);
        if(activityPo==null)return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
        if(!activityPo.getShopId().equals(shopId))return new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE);
        CouponActivity couponActivity= new CouponActivity(activityPo);
        return new ReturnObject<CouponActivity>(couponActivity);
    }

    public ReturnObject<Object> showCouponActivity(Shop shop, Long id) {
       try{ CouponActivityPo activityPo= couponActivityPoMapper.selectByPrimaryKey(id);
        if(activityPo==null)
            return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
        if(!activityPo.getShopId().equals(shop.getId()))
            return new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE);
           CouponActivity couponActivity=new CouponActivity(activityPo);
           CouponActivityDetailVo couponActivityVo=new CouponActivityDetailVo(couponActivity);
           return new ReturnObject<Object>(couponActivityVo);
    }
       catch (Exception e)
       {
           logger.info("exception");
           return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
       }

    }

    public ReturnObject<Object> createCouponSkus(Long shopId, Long id, List<CouponSku> couponSkus) {
        CouponActivityPo activityPo=null;
        logger.info("s");
        try{
        activityPo= couponActivityPoMapper.selectByPrimaryKey(id);
            if (activityPo == null) {
                logger.info("not a valid coupon activity");
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
        }catch (Exception e)
        {
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }

        logger.info("shopid: "+activityPo.getShopId()+":"+shopId);

        if(CouponActivity.DatabaseState.getTypeByCode(activityPo.getState().intValue()).equals(CouponActivity.DatabaseState.DELETED))
            return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);

        //【已上线】
        if(CouponActivity.DatabaseState.getTypeByCode(activityPo.getState().intValue()).equals(CouponActivity.DatabaseState.ONLINE))
            return new ReturnObject<>(ResponseCode.COUPONACT_STATENOTALLOW);

        //活动和shopId匹配
        if(activityPo.getShopId()!= shopId)return new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE);

        List<CouponSkuPo>couponSkuPos=new ArrayList<>();
        //对每个SKU进行判断、添加
        for(CouponSku couponSku:couponSkus)
        {
            //【已下线】
            //之前没有添加过该SKU
            CouponSkuPoExample alreadyExample=new CouponSkuPoExample();
            CouponSkuPoExample.Criteria alreadyCriteria=alreadyExample.createCriteria();
            alreadyCriteria.andSkuIdEqualTo(couponSku.getSkuId());
            alreadyCriteria.andActivityIdEqualTo(id);
            List<CouponSkuPo> alreadyPos=couponSkuMapper.selectByExample(alreadyExample);
            if(alreadyPos!=null&&alreadyPos.size()>0)return new ReturnObject<>(ResponseCode.ACTIVITYALTER_INVALID);

            //设置CouponSkuPo
            CouponSkuPo couponSkuPo = couponSku.getCouponSkuPo();
            couponSkuPo.setActivityId(id);
            couponSkuPo.setGmtCreate(LocalDateTime.now());
            couponSkuPo.setGmtModified(LocalDateTime.now());
            couponSkuPos.add(couponSkuPo);
        }

        //尝试插入
        try {
            for(CouponSkuPo po:couponSkuPos) {
                int ret = couponSkuMapper.insert(po);
                if (ret == 0) {
                    //插入失败
                    logger.debug("createCouponSpu: insert couponSkus fail : " + couponSkuPos.toString());
                    return new ReturnObject<>(ResponseCode.FIELD_NOTVALID, "couponSpu字段不合法：" + couponSkuPos.toString());
                }
            }
            return new ReturnObject<>();
        } catch (DataAccessException e) {
            // 其他数据库错误
            logger.debug("other sql exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        } catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    public ReturnObject<ResponseCode> deleteCouponSku(Long shopId, Long id) {
        //判断CouponSku是否存在
        CouponSkuPo couponSkuPo=couponSkuMapper.selectByPrimaryKey(id);
        if(couponSkuPo==null)return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
        //查看shopid
        CouponActivityPo activityPo= couponActivityPoMapper.selectByPrimaryKey(couponSkuPo.getActivityId());
        if(activityPo.getShopId()==shopId)return new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE);

        if(CouponActivity.DatabaseState.getTypeByCode(activityPo.getState().intValue()).equals(CouponActivity.DatabaseState.DELETED))
            return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
        if(CouponActivity.DatabaseState.getTypeByCode(activityPo.getState().intValue()).equals(CouponActivity.DatabaseState.DELETED))
            return new ReturnObject<>(ResponseCode.COUPONACT_STATENOTALLOW);
        try{
            int ret=couponSkuMapper.deleteByPrimaryKey(id);
            if(ret==0){
                //删除失败
                logger.debug("deleteCouponSpu: delete couponSpu fail : " + couponSkuPo.toString());
                return new ReturnObject<>(ResponseCode.FIELD_NOTVALID, "couponSpu字段不合法：" + couponSkuPo.toString());
            }
            else {
                //删除成功
                logger.debug("deleteCouponSpu: delete couponSpu = " + couponSkuPo.toString());
                return new ReturnObject<>();
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
}
