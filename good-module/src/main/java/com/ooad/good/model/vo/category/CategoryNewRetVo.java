package com.ooad.good.model.vo.category;

import com.ooad.good.model.bo.Category;
import lombok.Data;

/**
 * @AuthorId: 24320182203185
 * @Author: Chaoyang Deng
 * @Date: 2020/12/15 下午5:15
 */
@Data
public class CategoryNewRetVo {
    private Long id;
    public CategoryNewRetVo(Category category){
        this.id=category.getId();
    }
    public CategoryNewRetVo(){}
}
