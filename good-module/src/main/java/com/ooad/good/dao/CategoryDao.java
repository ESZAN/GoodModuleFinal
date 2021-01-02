package com.ooad.good.dao;

import cn.edu.xmu.ooad.util.ResponseCode;
import cn.edu.xmu.ooad.util.ReturnObject;

import com.ooad.good.mapper.GoodsCategoryPoMapper;
import com.ooad.good.mapper.SpuPoMapper;
import com.ooad.good.model.bo.Category;
import com.ooad.good.model.po.GoodsCategoryPo;
import com.ooad.good.model.po.GoodsCategoryPoExample;
import com.ooad.good.model.po.SpuPo;
import com.ooad.good.model.po.SpuPoExample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
public class CategoryDao {
    private static final Logger logger = LoggerFactory.getLogger(CategoryDao.class);

    @Autowired
    private GoodsCategoryPoMapper categoryPoMapper;

    @Autowired
    private SpuPoMapper spuPoMapper;

    /**
     * 查询商品分类信息
     * @param id
     * @return
     */
    public ReturnObject getCategories(Long id) {
     //   GoodsCategoryPo categoryPo=null;
        List<GoodsCategoryPo> pos=null;
        try {
            //categoryPo = categoryPoMapper.selectByPrimaryKey(id);
            GoodsCategoryPoExample example = new GoodsCategoryPoExample();
            GoodsCategoryPoExample.Criteria criteria = example.createCriteria();
            criteria.andPidEqualTo(id);

            GoodsCategoryPo nowPo=categoryPoMapper.selectByPrimaryKey(id);
            if(nowPo==null)
                {
                    logger.info("商品类目不存在：id = " + id);
                    return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
                }
            pos = categoryPoMapper.selectByExample(example);
         //   if (pos == null||pos.size()==0)

        }
        catch (Exception e)
        {
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }
        return new ReturnObject<>(pos);
    }

    /**
     * 新增商品类目
     * @param goodsCategory
     * @param pid
     * @return
     */
    public ReturnObject<Category> insertGoodsCategory(Category goodsCategory, long pid) {

        if(pid!=0) {
            //寻找id = pid 的分类
            GoodsCategoryPo orig = categoryPoMapper.selectByPrimaryKey(pid);
            //只能在已有分类下新建子分类
            if (orig == null) {
                logger.info("父级类目不存在：id = " + pid);
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
        }
        GoodsCategoryPoExample example = new GoodsCategoryPoExample();
        GoodsCategoryPoExample.Criteria criteria = example.createCriteria();
        criteria.andNameEqualTo(goodsCategory.getName());

        ReturnObject<Category> retObj = null;
        goodsCategory.setPid(pid);
        GoodsCategoryPo goodsCategoryPo = goodsCategory.gotCategoryPo();
        List<GoodsCategoryPo> pos=null;

        try{
            pos = categoryPoMapper.selectByExample(example);
            if (pos != null&&pos.size()!=0) {
                logger.info("商品名称已经存在：id = " +pos.get(0).getId());
                return new ReturnObject<>(ResponseCode.CATEGORY_NAME_SAME);
            }

            int ret = categoryPoMapper.insertSelective(goodsCategoryPo);
            if (ret == 0) {
                //插入失败
                logger.info("insertGoodsCategory: insert goodsCategory fail " + goodsCategoryPo.toString());
                retObj = new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST, String.format("新增失败：" + goodsCategoryPo.getName()));
            } else {
                //插入成功
                logger.info("insertGoodsCategory: insert goodsCategory = " + goodsCategoryPo.getId());
                goodsCategory.setId(goodsCategoryPo.getId());
                retObj = new ReturnObject<>(goodsCategory);
            }
        }
        catch (DataAccessException e) {
            if (Objects.requireNonNull(e.getMessage()).contains("goods_category_name_uindex")) {
                //若有重复的分类名则新增失败
                logger.info("updateGoodsCategory: have same goodsCategory name = " + goodsCategoryPo.getName());
                //retObj = new ReturnObject<>(ResponseCode.CATEGORY_NAME_SAME, String.format("类目名称已存在：" + goodsCategoryPo.getName()));
                retObj = new ReturnObject<>(ResponseCode.CATEGORY_NAME_SAME, String.format("类目名称已存在"));
            } else {
                // 其他数据库错误
                logger.debug("other sql exception : " + e.getMessage());
                retObj = new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
            }
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            retObj = new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
        return retObj;
    }
    /**
     * 管理员删除商品类目
     *
     * @param id
     * @return
     */
    public ReturnObject<Object> deleteCategory(Long id) {
        ReturnObject<Object> retObj = null;
        //获取该分类
        GoodsCategoryPo orig = categoryPoMapper.selectByPrimaryKey(id);

        if (orig == null ) {
            logger.info("类目不存在或已被删除：id = " + id);
            return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
        }

        //为一级分类
        else if(orig.getPid()==0)
        {
            //删除一级类目
            categoryPoMapper.deleteByPrimaryKey(id);

            //一并删除二级类目
            GoodsCategoryPoExample example1 = new GoodsCategoryPoExample();
            GoodsCategoryPoExample.Criteria criteria = example1.createCriteria();
            criteria.andPidEqualTo(id);

            List<GoodsCategoryPo> goodsCategoryPos = categoryPoMapper.selectByExample(example1);
            //先将每一个二级分类下的商品变为没有分类的商品
            for (GoodsCategoryPo po : goodsCategoryPos) {
                SpuPoExample example2 = new SpuPoExample();
                SpuPoExample.Criteria criteria2 = example2.createCriteria();
                criteria2.andCategoryIdEqualTo(po.getId());
                List<SpuPo> goodsSpuPos = spuPoMapper.selectByExample(example2);
                for (SpuPo spupo : goodsSpuPos) {
                    spupo.setCategoryId((long)0);
                    spuPoMapper.updateByPrimaryKeySelective(spupo);
                }
            }

            //删除所有二级类目
           categoryPoMapper.deleteByExample(example1);
            logger.info("category id = " + id + " 已被永久删除");
            retObj = new ReturnObject<>();

        }

        //为二级分类
        else{

            //删除二级类目
            int ret = categoryPoMapper.deleteByPrimaryKey(id);

            //将二级分类下的商品变为没有分类的商品
            SpuPoExample example2 = new SpuPoExample();
            SpuPoExample.Criteria criteria2 = example2.createCriteria();
            criteria2.andCategoryIdEqualTo(id);

            List<SpuPo> goodsSpuPos = spuPoMapper.selectByExample(example2);
            for (SpuPo spupo : goodsSpuPos) {
                spupo.setCategoryId((long)0);
                spuPoMapper.updateByPrimaryKeySelective(spupo);
            }

            retObj = new ReturnObject<>();
        }

        return retObj;
    }


    /**
     * 管理员修改商品类目信息
     * @param category
     * @return
     */
    public ReturnObject<Category> updateCategory(Category category) {
        if(category.getId()!=0) {
            //寻找id = pid 的分类
            GoodsCategoryPo orig = categoryPoMapper.selectByPrimaryKey(category.getId());
            //只能在已有分类下新建子分类
            if (orig == null) {
                logger.info("父级类目不存在：id = " + category.getId());
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
        }

        GoodsCategoryPo categoryPo = category.gotCategoryPo();
        ReturnObject<Category> retObj = null;


        GoodsCategoryPoExample exampleName = new GoodsCategoryPoExample();
        GoodsCategoryPoExample.Criteria criterian = exampleName.createCriteria();
        criterian.andNameEqualTo(category.getName());
        List<GoodsCategoryPo> pos=null;
        try{
            pos = categoryPoMapper.selectByExample(exampleName);
            if (pos != null&&pos.size()!=0) {
                logger.info("商品名称已经存在：id = " + pos.get(0).getId());
                return new ReturnObject<>(ResponseCode.CATEGORY_NAME_SAME);

            }
        }catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            retObj = new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }

        GoodsCategoryPoExample example = new GoodsCategoryPoExample();
        GoodsCategoryPoExample.Criteria criteria = example.createCriteria();
        criteria.andIdEqualTo(category.getId());


        try {
            int ret = categoryPoMapper.updateByExampleSelective(categoryPo, example);
//            int ret = roleMapper.updateByPrimaryKeySelective(categoryPo);
            if (ret == 0) {
                //修改失败
                logger.debug("updateCategory: update category fail : " + categoryPo.toString());
                retObj = new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST, String.format("商品类目id不存在：" + categoryPo.getId()));
            } else {
                //修改成功
                logger.debug("updateRole: update category = " + categoryPo.toString());
                retObj = new ReturnObject<>();
            }
        } catch (DataAccessException e) {
            if (Objects.requireNonNull(e.getMessage()).contains("auth_role.auth_role_name_uindex")) {
                //若有重复的角色名则修改失败
                logger.debug("updateCategory: have same category name = " + categoryPo.getName());
                retObj = new ReturnObject<>(ResponseCode.ROLE_REGISTERED, String.format("商品类目名重复：" + categoryPo.getName()));
            } else {
                // 其他数据库错误
                logger.debug("other sql exception : " + e.getMessage());
                retObj = new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
            }
        } catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            retObj = new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
        return retObj;
    }
}