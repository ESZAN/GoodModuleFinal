package com.ooad.good.model.vo.brand;

import lombok.Data;

/**
 * @AuthorId: 24320182203185
 * @Author: Chaoyang Deng
 * @Date: 2020/12/20 上午9:33
 */
@Data
public class BrandIdVo {
    private Long id;
    public BrandIdVo(Long id)
    {
        this.id=id;
    }
}
