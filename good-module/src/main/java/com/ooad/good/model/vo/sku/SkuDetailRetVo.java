package com.ooad.good.model.vo.sku;

import com.ooad.good.model.bo.Sku;
import com.ooad.good.model.vo.spu.SpuVo;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SkuDetailRetVo {
    private Long id;

    private String name;

    private String skuSn;

    private String detail;

    private String imageUrl;

    private Long originalPrice;

    private Long price;

    private Integer inventory;

    private String configuration;

    private Long weight;

    private LocalDateTime gmtCreated;

    private LocalDateTime gmtModified;

    private SpuVo spu;

    private Boolean disable;

    private Boolean shareable;

    public void set(Sku obj)
    {
        id=obj.getId();
        name=obj.getName();
        skuSn=obj.getSkuSn();
        detail = obj.getDetail();
        imageUrl=obj.getImageUrl();
        inventory=obj.getInventory();
        originalPrice=obj.getOriginalPrice();
        configuration = obj.getConfiguration();
        weight = obj.getWeight();
        gmtCreated = obj.getGmtCreate();
        gmtModified = obj.getGmtModified();
        disable= obj.getDisabled().equals(1);
    }
}
