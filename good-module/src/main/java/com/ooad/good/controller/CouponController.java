package com.ooad.good.controller;

import cn.edu.xmu.ooad.annotation.Audit;
import cn.edu.xmu.ooad.annotation.Depart;
import cn.edu.xmu.ooad.annotation.LoginUser;
import cn.edu.xmu.ooad.model.VoObject;
import cn.edu.xmu.ooad.util.Common;
import cn.edu.xmu.ooad.util.ResponseCode;
import cn.edu.xmu.ooad.util.ResponseUtil;
import cn.edu.xmu.ooad.util.ReturnObject;
import com.ooad.good.model.bo.Coupon;
import com.ooad.good.model.bo.CouponActivity;
import com.ooad.good.model.bo.CouponSku;
import com.ooad.good.model.vo.couponActivity.CouponActivityVo;
import com.ooad.good.model.vo.couponActivity.CouponStateRetVo;
import com.ooad.good.model.vo.couponActivity.UpdateCouponActivityVo;
import com.ooad.good.service.CouponService;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Api(value="优惠服务",tags="coupon")
@RestController
@RequestMapping(value = "/goods", produces = "application/json;charset=UTF-8")
public class CouponController {

    private  static  final Logger logger = LoggerFactory.getLogger(CouponController.class);

    @Autowired
    private HttpServletResponse httpServletResponse;

    @Autowired
    private CouponService couponService;

    /**
     * 管理员新建己方优惠活动
     * @param vo
     * @param bindingResult
     * @return
     */
    @Audit
    @PostMapping("/shops/{shopId}/couponactivities")
    public Object insertCouponActivity(@Validated @RequestBody CouponActivityVo vo, BindingResult bindingResult){
        logger.debug("insert couponactivity :");
        //校验前端数据
        Object returnObject = Common.processFieldErrors(bindingResult, httpServletResponse);
        if (null != returnObject) {
            logger.debug("validate fail");
            return returnObject;
        }

        CouponActivity couponActivity = vo.createCouponActivity();
        couponActivity.setGmtCreate(LocalDateTime.now());
        ReturnObject retObject = couponService.insertCouponActivity(couponActivity);
        if (retObject.getData() != null) {
            httpServletResponse.setStatus(HttpStatus.CREATED.value());
            return Common.getRetObject(retObject);
        } else {
            return Common.getNullRetObj(new ReturnObject<>(retObject.getCode(), retObject.getErrmsg()), httpServletResponse);
        }
    }
    /**
     * 获得优惠卷的所有状态
     * @return Object
     */
    @GetMapping("/coupons/states")
    @ResponseBody
    public Object getcouponState()
    {
        logger.debug("getcouponState");
        Coupon.State[] states=Coupon.State.class.getEnumConstants();
        List<CouponStateRetVo> stateRetVos=new ArrayList<CouponStateRetVo>();
        for(int i=0;i<states.length;++i)
            stateRetVos.add(new CouponStateRetVo(states[i]));
        return ResponseUtil.ok(new ReturnObject<List>(stateRetVos).getData());
    }


    /**
     * 获取优惠活动
     * @param shopId
     * @param id
     * @param userId
     * @param departId
     * @return
     */
    @GetMapping("/shops/{shopId}/couponactivities/{id}")
    @ResponseBody
    public Object showCouponActivity(@PathVariable Long shopId,@PathVariable Long id,
                                     @LoginUser @ApiIgnore @RequestParam(required = false) Long userId,
                                     @Depart @ApiIgnore @RequestParam(required = false) Long departId) {
        logger.debug("get couponActivity:");

        ReturnObject<Object> couponActivity = couponService.getCouponActivity(shopId,id);
       if(couponActivity.getCode()==ResponseCode.RESOURCE_ID_OUTSCOPE){
            httpServletResponse.setStatus(HttpStatus.FORBIDDEN.value());
            return Common.getNullRetObj(couponActivity, httpServletResponse);
        }
        else return Common.decorateReturnObject(couponActivity);
    }
    /**
     * 管理员删除己方优惠活动
     * @param id
     * @return
     */
    @Audit
    @DeleteMapping("/shops/{shopId}/couponactivities/{id}")
    public Object deleteCouponActivity(@PathVariable("shopId") Long shopId,@PathVariable("id") Long id){
        logger.debug("delete couponActivity: id= "+id);

            ReturnObject returnObject = couponService.deleteCouponActivity(shopId,id);
            return Common.decorateReturnObject(returnObject);

    }

    /**
     * 管理员修改己方优惠活动
     * @param id
     * @param vo
     * @param bindingResult
     * @return
     */
    @Audit
    @PutMapping("/shops/{shopId}/couponactivities/{id}")
    public Object updateCouponActivity(@PathVariable("id") Long id, @Validated @RequestBody UpdateCouponActivityVo vo,BindingResult bindingResult){
        logger.debug("update couponActivity: id= "+id);
        //校验前端数据
     //   Object returnObject = Common.processFieldErrors(bindingResult, httpServletResponse);
       // if (null != returnObject) {
         //   return returnObject;
        //}
        CouponActivity couponActivity=vo.createCouponActivity();
        couponActivity.setId(id);
        couponActivity.setGmtModified(LocalDateTime.now());
        ReturnObject retObject=couponService.updateCouponActivity(couponActivity);
        if (retObject.getData() != null) {
            return Common.getRetObject(retObject);
        } else {
            return Common.decorateReturnObject(retObject);
        }
    }


    /**
     * 上线优惠活动
     * @param id
     * @return
     */
    @Audit
    @PutMapping("shops/{shopId}/couponactivities/{id}/onshelves")
    public Object onlineCoupon(@PathVariable("shopId")Long shopId,@PathVariable("id")Long id){

        logger.info("onlineCouponactivities: id ="+id);

        ReturnObject retObject=couponService.onlineCouponactivity(shopId,id);
        return Common.decorateReturnObject(retObject);

    }

    /**
     * 管理员为己方某优惠券活动新增sku
     * @param shopId
     * @param id
     * @param body
     * @param bindingResult
     * @param userId
     * @param departId
     * @return Object
     **/
    @Audit

    @PostMapping("/shops/{shopId}/couponactivities/{id}/skus")
    @ResponseBody
    public Object createCouponSkus(@PathVariable Long shopId, @PathVariable Long id,
                                   @Validated @RequestBody Long[] body, BindingResult bindingResult,
                                   @LoginUser @ApiIgnore @RequestParam(required = false) Long userId,
                                   @Depart @ApiIgnore @RequestParam(required = false) Long departId)
    {
        logger.debug("createCouponSku: id = "+ id+" shopId="+shopId+" vos="+ Arrays.toString(body));
        Object returnObject = Common.processFieldErrors(bindingResult, httpServletResponse);
        if (null != returnObject) {
            logger.info("error");
            return returnObject;
        }
//        if(departId!=0&&!Objects.equals(departId, shopId))
  //          return Common.decorateReturnObject(new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE));
        List<CouponSku> couponSkus=new ArrayList<>();

        for(Long vo:body)
        {
            CouponSku couponSku=new CouponSku();
            couponSku.setSkuId(vo);
            couponSkus.add(couponSku);
        }
        logger.info("con");

        ReturnObject retObject=couponService.createCouponSkus(shopId,id, couponSkus);
        if (retObject.getCode() == ResponseCode.OK) {
            httpServletResponse.setStatus(HttpStatus.CREATED.value());
            return Common.decorateReturnObject(retObject);
        } else {
            if(retObject.getCode().equals(ResponseCode.RESOURCE_ID_OUTSCOPE))
                httpServletResponse.setStatus(HttpStatus.FORBIDDEN.value());
            else if(retObject.getCode().equals(ResponseCode.RESOURCE_ID_NOTEXIST))
                httpServletResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return Common.getNullRetObj(new ReturnObject<>(retObject.getCode(), retObject.getErrmsg()), httpServletResponse);
        }
    }
    /**
     * 店家删除己方某优惠券活动的coupon_sku
     * @param shopId
     * @param id
     * @param userId
     * @param departId
     * @return Object
     */
    @Audit
    @DeleteMapping("/shops/{shopId}/couponskus/{id}")
    @ResponseBody
    public Object deleteCouponSku(@PathVariable Long shopId, @PathVariable Long id,
                                  @LoginUser @ApiIgnore @RequestParam(required = false) Long userId,
                                  @Depart @ApiIgnore @RequestParam(required = false) Long departId)
    {
        logger.debug("deleteCouponSpu: id = "+ id+" shopId="+shopId);
     //   if(departId!=0&&!Objects.equals(departId, shopId)&&!departId.equals((long)0))
       //     return Common.decorateReturnObject(new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE));
        ReturnObject<ResponseCode> returnObject=couponService.deleteCouponSku(shopId,id);
        return Common.decorateReturnObject(returnObject);
    }

    /**
     * 下线优惠活动
     * @param id
     * @return
     */
    @Audit
    @PutMapping("/shops/{shopId}/couponactivities/{id}/offshelves")
    public Object offlineCoupon(@PathVariable("shopId")Long shopId,@PathVariable("id")Long id){

        logger.debug("offlineCouponactivities: id ="+id);

        ReturnObject retObject=couponService.offlineCouponactivity(shopId,id);
        return Common.decorateReturnObject(retObject);

    }

}
