package com.ooad.good.model.vo.spu;

import com.ooad.good.model.bo.Spu;
import com.ooad.good.model.vo.brand.BrandIdVo;
import com.ooad.good.model.vo.brand.BrandRetVo;
import com.ooad.good.model.vo.category.CategoryNewRetVo;
import com.ooad.good.model.vo.category.CategoryRetVo;
import com.ooad.good.model.vo.category.CategoryVo;
import lombok.Data;

/**
 * @AuthorId: 24320182203185
 * @Author: Chaoyang Deng
 * @Date: 2020/12/21 上午9:30
 */
@Data
public class SpuRetVo {
    private Long id;
    private BrandIdVo brand;
    private CategoryNewRetVo category;

    public SpuRetVo(Spu spu){
        this.id=spu.getId();
        this.brand=new BrandIdVo(spu.getBrandId());
        this.category=new CategoryNewRetVo();
        category.setId(spu.getCategoryId());
    }
}
