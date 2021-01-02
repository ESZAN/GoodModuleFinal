package com.ooad.good.model.bo;

import cn.edu.xmu.ooad.model.VoObject;
import com.ooad.good.model.po.FloatPricePo;
import com.ooad.good.model.vo.floatPrice.FloatPriceRetVo;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @AuthorId: 24320182203185
 * @Author: Chaoyang Deng
 * @Date: 2020/12/23 下午1:15
 */
@Data
public class FloatPrice implements VoObject {

    public enum State {
        VALID(1, "可用"),
        INVALID(0, "废弃");

        private static final Map<Integer, State> stateMap;

        static {
            stateMap = new HashMap();
            for (State enum1 : values()) {
                stateMap.put(enum1.code, enum1);
            }
        }

        private int code;
        private String description;

        State(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public static State getTypeByCode(Integer code) {
            return stateMap.get(code);
        }

        public Integer getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    private Long id;

    private Long goodsSkuId;

    private Long activityPrice;

    private LocalDateTime beginTime;

    private LocalDateTime endTime;

    private Integer quantity;

    private Long createdBy;

    private Long invalidBy;

    private State valid;

    private LocalDateTime gmtCreate;

    private LocalDateTime gmtModified;

    public  FloatPrice(){}
    public FloatPrice(FloatPricePo po)
    {
        id=po.getId();
        goodsSkuId=po.getGoodsSkuId();
        activityPrice=po.getActivityPrice();
        beginTime=po.getBeginTime();
        endTime=po.getEndTime();
        quantity=po.getQuantity();
        createdBy=po.getCreatedBy();
        invalidBy=po.getInvalidBy();
        valid= State.getTypeByCode(po.getValid().intValue());
        gmtCreate=po.getGmtCreate();
        gmtModified=po.getGmtModified();
    }

    @Override
    public Object createVo() {
        return new FloatPriceRetVo(this);
    }

    @Override
    public Object createSimpleVo() {
        return null;
    }

    public FloatPricePo getFloatPricePo()
    {
        FloatPricePo floatPricePo=new FloatPricePo();
        floatPricePo.setId(id);
        floatPricePo.setGoodsSkuId(goodsSkuId);
        floatPricePo.setActivityPrice(activityPrice);
        floatPricePo.setBeginTime(beginTime);
        floatPricePo.setEndTime(endTime);
        floatPricePo.setQuantity(quantity);
        floatPricePo.setValid(valid.getCode().byteValue());
        floatPricePo.setCreatedBy(createdBy);
        return floatPricePo;
    }
}

