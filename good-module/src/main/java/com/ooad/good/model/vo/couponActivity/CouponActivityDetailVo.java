package com.ooad.good.model.vo.couponActivity;

import cn.edu.xmu.oomall.goods.model.SimpleShopDTO;
import com.ooad.good.model.bo.CouponActivity;
import lombok.Data;

/**
 * @AuthorId: 24320182203185
 * @Author: Chaoyang Deng
 * @Date: 2020/12/22 上午6:07
 */
@Data
public class  CouponActivityDetailVo {

    private Long id;

    private String name;

    private Byte state;

    private SimpleShopDTO shopVo;

    private Integer quantity;

    private Byte quantitiyType;

    private Byte validTerm;

    private String imageUrl;

    private String beginTime;

    private String endTime;

    private String couponTime;

    private String strategy;

    private String createdBy;

    private String  modifiedBy;

    private String gmtCreate;//到底写成string类型还是LocalDateTime类型？

    private String gmtModified;


    /**
     * 构造函数
     * @param bo Bo对象
     */

    public CouponActivityDetailVo(CouponActivity bo){
        this.id=bo.getId();
        this.name=bo.getName();
        this.state=bo.getState().byteValue();
        this.quantity=bo.getQuantity();
        this.quantitiyType=bo.getQuantitiyType().byteValue();
        this.validTerm=bo.getValidTerm();
        this.imageUrl=bo.getImageUrl();
        this.beginTime=bo.getBeginTime().toString();
        this.endTime=bo.getEndTime().toString();
        this.couponTime=bo.getCouponTime().toString();
        this.strategy=bo.getStrategy();
        this.state=bo.getState().byteValue();
        if(this.gmtCreate!=null)
            this.gmtCreate=bo.getGmtCreate().toString();
        if(this.gmtModified!=null)
            this.gmtModified=bo.getGmtModified().toString();
    }
}
