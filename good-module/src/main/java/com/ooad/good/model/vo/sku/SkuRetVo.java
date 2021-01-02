package com.ooad.good.model.vo.sku;

import com.ooad.good.model.bo.Sku;
import lombok.Data;

@Data
public class SkuRetVo {
    private Long id;
    private String name;
    private String skuSn;
    private String imageUrl;
    private Integer inventory;
    private Long originalPrice;
    private Long price;
    private Boolean disable;


    public SkuRetVo(Sku sku){

        Boolean dis;
        if (sku.getDisabled()==null)
            dis=false;
        else if(sku.getDisabled()==(byte)0)
            dis=false;
        else dis=true;

        this.id=sku.getId();
        this.skuSn=sku.getSkuSn();
        this.name=sku.getName();
        this.originalPrice=sku.getOriginalPrice();
        this.imageUrl=sku.getImageUrl();
        this.inventory=sku.getInventory();
        this.disable=dis;
        this.price=sku.getPrice();
    }
}
