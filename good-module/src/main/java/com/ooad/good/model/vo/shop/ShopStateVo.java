package com.ooad.good.model.vo.shop;

import com.ooad.good.model.bo.Shop;
import lombok.Data;

/**
 * @AuthorId: 24320182203185
 * @Author: Chaoyang Deng
 * @Date: 2020/12/23 上午6:33
 */
@Data
public class ShopStateVo {
    //@ApiModelProperty(value="店铺状态")

    //@ApiModelProperty(value="状态名称")
    private Long code;
    private String name;


    public ShopStateVo(Shop.StateType state)
    {
        this.code=Long.valueOf(state.getCode());
        this.name=state.getDescription();
    }
}
