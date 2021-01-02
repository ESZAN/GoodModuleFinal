package com.ooad.good.model.bo;

import com.ooad.good.model.po.CouponActivityPo;
import com.ooad.good.model.po.SpuPo;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
public class CouponActivity {
    private Long id;
    private String name;
    private LocalDateTime beginTime;
    private LocalDateTime endTime;
    private LocalDateTime couponTime;
    private Byte state;
    private Long shopId;
    private Integer quantity;
    private Byte validTerm;
    private String imageUrl;
    private String strategy;
    private Long createdBy;
    private Long modiBy;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;
    private Byte quantitiyType;

    public enum DatabaseState {
        OFFLINE(0, "已下线"),
        ONLINE(1, "已上线"),
        DELETED(2,"已删除");

        private static final Map<Integer, DatabaseState> stateMap;

        static { //由类加载机制，静态块初始加载对应的枚举属性到map中，而不用每次取属性时，遍历一次所有枚举值
            stateMap = new HashMap();
            for (CouponActivity.DatabaseState enum1 : values()) {
                stateMap.put(enum1.code, enum1);
            }
        }

        private int code;
        private String description;

        DatabaseState(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public static CouponActivity.DatabaseState getTypeByCode(Integer code) {
            return stateMap.get(code);
        }

        public Integer getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 构造函数
     */
    public CouponActivity(){

    }

    /**
     * po对象构建bo对象
     * @param po
     */
    public CouponActivity(CouponActivityPo po) {
        this.id=po.getId();
        this.name=po.getName();
        this.beginTime=po.getBeginTime();
        this.endTime=po.getEndTime();
        this.couponTime=po.getCouponTime();
        this.state=po.getState();
        this.shopId=po.getShopId();
        this.quantity=po.getQuantity();
        this.validTerm=po.getValidTerm();
        this.imageUrl=po.getImageUrl();
        this.strategy=po.getStrategy();
        this.createdBy=po.getCreatedBy();
        this.modiBy=po.getModiBy();
        this.gmtModified=po.getGmtModified();
        this.gmtCreate=po.getGmtCreate();
        this.quantitiyType=po.getQuantitiyType();
    }

    /**
     * 用bo对象创建更新po对象
     * @return
     */
    public CouponActivityPo gotCouponActivityPo(){

        CouponActivityPo po=new CouponActivityPo();
        po.setId(this.getId());
        po.setName(this.getName());
        po.setBeginTime(this.getBeginTime());
        po.setEndTime(this.getEndTime());
        po.setCouponTime(this.getCouponTime());
        po.setState(this.getState());
        po.setShopId(this.getShopId());
        po.setQuantity(this.getQuantity());
        po.setValidTerm(this.getValidTerm());
        po.setImageUrl(this.getImageUrl());
        po.setStrategy(this.getStrategy());
        po.setCreatedBy(this.getCreatedBy());
        po.setModiBy(this.getModiBy());
        po.setGmtCreate(this.getGmtCreate());
        po.setGmtModified(this.getGmtModified());
        po.setQuantitiyType(this.getQuantitiyType());
        return po;
    }

}
