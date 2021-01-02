package com.ooad.good.model.vo.floatPrice;

import com.ooad.good.model.bo.FloatPrice;
import com.ooad.good.model.vo.CreatedBy;
import com.ooad.good.model.vo.ModifiedBy;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FloatPriceRetVo {

    private Long id;

    private Long activityPrice;

    private Integer quantity;

    private LocalDateTime beginTime;

    private LocalDateTime endTime;

    private CreatedBy createdBy;

    private ModifiedBy modifiedBy;

    public FloatPriceRetVo(FloatPrice obj) {
        id = obj.getId();
        activityPrice=obj.getActivityPrice();
        quantity=obj.getQuantity();
        beginTime=obj.getBeginTime();
        endTime=obj.getEndTime();
    }

}

