package com.ooad.good.model.vo.floatPrice;

import com.ooad.good.model.bo.FloatPrice;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Future;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;

/**
 * @AuthorId: 24320182203185
 * @Author: Chaoyang Deng
 * @Date: 2020/12/26 上午12:59
 */
@Data
public class FloatPriceVo {
    private Long activityPrice;

    private LocalDateTime beginTime;

    private LocalDateTime endTime;

    private Integer quantity;

    public FloatPrice createFloatPrice()
    {
        FloatPrice floatPrice=new FloatPrice();
        floatPrice.setActivityPrice(activityPrice);
        floatPrice.setBeginTime(beginTime);
        floatPrice.setEndTime(endTime);
        floatPrice.setQuantity(quantity);
        return floatPrice;
    }
}
