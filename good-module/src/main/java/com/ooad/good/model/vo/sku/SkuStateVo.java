package com.ooad.good.model.vo.sku;

import com.ooad.good.model.bo.Shop;
import com.ooad.good.model.bo.Sku;
import lombok.Data;

/**
 * @AuthorId: 24320182203185
 * @Author: Chaoyang Deng
 * @Date: 2020/12/25 上午9:40
 */
@Data
public class SkuStateVo {
    private Long code;
    private String name;


    public SkuStateVo(Sku.State state)
    {
        this.code=Long.valueOf(state.getCode());
        this.name=state.getDescription();
    }
}
