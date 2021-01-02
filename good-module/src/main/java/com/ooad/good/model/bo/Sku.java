package com.ooad.good.model.bo;

import cn.edu.xmu.ooad.model.VoObject;
import com.ooad.good.model.po.SkuPo;
import com.ooad.good.model.vo.sku.SkuRetVo;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
public class Sku implements VoObject {
    @Override
    public Object createVo() {
        return new SkuRetVo(this);
    }

    @Override
    public Object createSimpleVo() {
        return new SkuRetVo(this);
    }

    public enum State {
        OFFSHELF(0,"未上架"),
        ONSHELF(4,"上架"),
        DELETED(6,"已删除");

        private static final Map<Integer, State> stateMap;

        static { //由类加载机制，静态块初始加载对应的枚举属性到map中，而不用每次取属性时，遍历一次所有枚举值
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

        public static Sku.State getTypeByCode(Integer code) {
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
    private Long goodsSpuId;
    private String skuSn;
    private String name;
    private Long originalPrice;
    private Long price;
    private String configuration;
    private Long weight;
    private String imageUrl;
    private Integer inventory;
    private String detail;
    private Byte disabled;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;
    private Byte state;

    public Sku(){

    }

    /**
     * po创建bo
     * @param po
     */
    public Sku(SkuPo po){
        this.id=po.getId();
        this.goodsSpuId=po.getGoodsSpuId();
        this.skuSn=po.getSkuSn();
        this.name=po.getName();
        this.originalPrice=po.getOriginalPrice();
        this.configuration=po.getConfiguration();
        this.weight=po.getWeight();
        this.imageUrl=po.getImageUrl();
        this.inventory=po.getInventory();
        this.detail=po.getDetail();
        this.disabled=po.getDisabled();
        this.gmtCreate=po.getGmtCreate();
        this.gmtModified=po.getGmtModified();
        this.state=po.getState();
    }

    /**
     * bo对象构建po对象
     * * @return
     */
    public SkuPo gotSkuPo(){

        SkuPo po=new SkuPo();
        po.setId(this.getId());
        po.setGoodsSpuId(this.getGoodsSpuId());
        po.setSkuSn(this.getSkuSn());
        po.setName(this.getName());
        po.setOriginalPrice(this.getOriginalPrice());
        po.setConfiguration(this.getConfiguration());
        po.setWeight(this.getWeight());
        po.setImageUrl(this.getImageUrl());
        po.setInventory(this.getInventory());
        po.setDetail(this.getDetail());
        po.setDisabled(this.getDisabled());
        po.setGmtCreate(this.getGmtCreate());
        po.setGmtModified(this.getGmtModified());
        po.setState(this.getState());

        return po;
    }
}
