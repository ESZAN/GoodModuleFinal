package com.ooad.good.service;

import cn.edu.xmu.ooad.util.ReturnObject;
import com.ooad.good.dao.BrandDao;
import com.ooad.good.dao.CategoryDao;
import com.ooad.good.model.bo.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {
    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);

    @Autowired
    private CategoryDao categoryDao;

    /**
     *查询商品分类关系
     * @param id
     * @return
     */
    public ReturnObject getCategories(Long id) {
        return categoryDao.getCategories(id);
    }
    /**
     * 管理员新增商品类目
     * @param 
     * @param pid
     * @return
     */
    @Transactional
    public ReturnObject insertGoodsCategory(Category category,Long pid) {
        ReturnObject<Category> retObj = categoryDao.insertGoodsCategory(category,pid);
        return retObj;
    }
    /**
     * 管理员删除商品类目
     * @param id
     * @return
     */
    @Transactional
    public  ReturnObject<Object>deleteCategory(Long id){
        return categoryDao.deleteCategory(id);
    }

    /**
     * 管理员修改商品类目
     * @param category
     * @return
     */
    @Transactional
    public ReturnObject updateCategory(Category category){
        ReturnObject<Category>retObj=categoryDao.updateCategory(category);
        return retObj;
    }
}
