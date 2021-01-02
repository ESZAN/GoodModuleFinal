package com.ooad.good.controller;

import cn.edu.xmu.ooad.annotation.Audit;
import cn.edu.xmu.ooad.annotation.Depart;
import cn.edu.xmu.ooad.annotation.LoginUser;
import cn.edu.xmu.ooad.model.VoObject;
import cn.edu.xmu.ooad.util.Common;
import cn.edu.xmu.ooad.util.ResponseCode;
import cn.edu.xmu.ooad.util.ResponseUtil;
import cn.edu.xmu.ooad.util.ReturnObject;
import com.github.pagehelper.PageInfo;
import com.ooad.good.model.bo.*;
import com.ooad.good.model.vo.brand.BrandVo;
import com.ooad.good.model.vo.category.CategoryVo;
import com.ooad.good.model.vo.floatPrice.FloatPriceVo;
import com.ooad.good.model.vo.shop.ShopStateVo;
import com.ooad.good.model.vo.sku.SkuStateVo;
import com.ooad.good.model.vo.sku.SkuVo;
import com.ooad.good.model.vo.spu.SpuVo;
import com.ooad.good.service.BrandService;

import com.ooad.good.service.CategoryService;
import com.ooad.good.service.SkuService;
import com.ooad.good.service.SpuService;
import io.swagger.annotations.*;
import org.apache.http.impl.client.RequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Api(value="商品服务",tags="goods")
@RestController
@RequestMapping(value = "/goods", produces = "application/json;charset=UTF-8")
public class GoodController {

    private  static  final Logger logger = LoggerFactory.getLogger(GoodController.class);
    @Autowired
    private HttpServletResponse httpServletResponse;
    @Autowired
    BrandService brandService;


    /**
     * 获得所有品牌
     * @param page
     * @param pageSize
     * @return
     */
    @Audit
    @GetMapping("/brands")
    public Object getAllBrands(@RequestParam(required = false,defaultValue = "1") Integer page, @RequestParam(required = false,defaultValue = "10") Integer pageSize){

        logger.debug("getAllBrands: page = "+ page +"  pageSize ="+pageSize);

        ReturnObject<PageInfo<VoObject>> returnObject =  brandService.getAllBrands(page, pageSize);
        return Common.getPageRetObject(returnObject);
    }

    /**
     * 删除品牌
     * @param id
     * @return
     */
    @Audit
    @DeleteMapping("/shops/{shopId}/brands/{id}")
    public Object deleteBrand(@PathVariable("id")Long id){
        logger.debug("delete brand: id =" +id);
        ReturnObject returnObject=brandService.deleteBrand(id);
        return Common.decorateReturnObject(returnObject);
    }

    /**
     * 增加品牌
     * @param vo
     * @param bindingResult
     * @return
     */

    @Audit
    @PostMapping("/shops/{id}/brands")
    public Object insertBrand(@RequestBody BrandVo vo, BindingResult bindingResult){
        //校验前端数据
        Object returnObject=Common.processFieldErrors(bindingResult,httpServletResponse);
        if (null != returnObject) {
            logger.debug("validate fail");
            return returnObject;
        }
        if(vo.getName()==null||vo.getName().isBlank())
        {
            httpServletResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            return Common.getNullRetObj(new ReturnObject<>(ResponseCode.FIELD_NOTVALID),httpServletResponse);
        }
        Brand brand=vo.createBrand();
        brand.setGmtCreate(LocalDateTime.now());
        ReturnObject retObject=brandService.insertBrand(brand);
        if(retObject.getData()!=null){
            logger.info("successful");
            httpServletResponse.setStatus(HttpStatus.CREATED.value());
            return Common.getRetObject(retObject);
        }
        else{
            return Common.getNullRetObj(new ReturnObject<>(retObject.getCode(), retObject.getErrmsg()), httpServletResponse);
        }
    }

    /**
     * 获得商品所有状态
     * @return
     */
    @Audit
    @GetMapping("/skus/states")
    public Object getAllShopStates() {
        logger.info("getAllSkuStates");
        Sku.State [] state= Sku.State.class.getEnumConstants();
        List<SkuStateVo> skuStateVos = new ArrayList<>();
        for (int i = 0; i < state.length; i++) {
            skuStateVos.add(new SkuStateVo(state[i]));
        }
        return ResponseUtil.ok(new ReturnObject<List>(skuStateVos).getData());
    }

    /**
     * 修改品牌信息
     * @param id
     * @param vo
     * @param bindingResult
     * @return
     */

    @Audit
    @PutMapping("/shops/{shopId}/brands/{id}")
    public Object updateBrand(@PathVariable("id")Long id,@Validated @RequestBody BrandVo vo,BindingResult bindingResult){
        logger.debug("update brand id = " + id);
        //校验前端数据
      /*  Object returnObject = Common.processFieldErrors(bindingResult, httpServletResponse);
        if (null != returnObject) {
            logger.info("error");
            return returnObject;
        }*/
        Brand brand=vo.createBrand();
        brand.setId(id);
        brand.setGmtModified(LocalDateTime.now());

        ReturnObject retObject =brandService.updateBrand(brand);
        if (retObject.getData() != null) {
            return Common.getRetObject(retObject);
        } else {
            return Common.getNullRetObj(new ReturnObject<>(retObject.getCode(), retObject.getErrmsg()), httpServletResponse);
        }

    }

    @Autowired
    private CategoryService categoryService;

    /**
     *查询商品分类关系
     * @param id
     * @return
     */
    @Audit
    @GetMapping("/categories/{id}/subcategories")
    public Object getCategories(@PathVariable Long id){
        ReturnObject returnObject =  categoryService.getCategories(id);
        return  Common.decorateReturnObject(returnObject);
    }
    /**
     * 管理员新增商品类目
     * @param vo
     * @param bindingResult
     * @param userId
     * @param departId
     * @param id
     * @return
     */
    @Audit
    @PostMapping("/shops/{shopId}/categories/{id}/subcategories")
    public Object insertCategory(@Validated @RequestBody CategoryVo vo, BindingResult bindingResult,
                                 @LoginUser @ApiIgnore @RequestParam(required = false) Long userId,
                                 @Depart @ApiIgnore @RequestParam(required = false) Long departId,
                                 @PathVariable Long id) {
        logger.debug("insert category by userId:" + userId);
        //校验前端数据
        Object returnObject = Common.processFieldErrors(bindingResult, httpServletResponse);
        if (null != returnObject) {
            logger.debug("validate fail");
            return returnObject;
        }
        Category category = vo.createCategory();
        category.setGmtCreate(LocalDateTime.now());
        if(vo.getName()==null||vo.getName().isBlank()){
            httpServletResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            return Common.getNullRetObj(new ReturnObject<>(ResponseCode.FIELD_NOTVALID),httpServletResponse);
        }
        ReturnObject retObject = categoryService.insertGoodsCategory(category,id);
        if (retObject.getData() != null) {
            httpServletResponse.setStatus(HttpStatus.CREATED.value());
            return Common.decorateReturnObject(retObject);
        } else {
            return Common.getNullRetObj(new ReturnObject<>(retObject.getCode(), retObject.getErrmsg()), httpServletResponse);
        }
    }
    /**
     * 管理员删除商品类目
     * @param id
     * @return
     */
    @Audit
    @DeleteMapping("/shops/{shopId}/categories/{id}")
    public Object deleteCategory(@PathVariable("id")Long id){
        logger.debug("delete Category: id = "+id);
        ReturnObject returnObject=categoryService.deleteCategory(id);
        return Common.decorateReturnObject(returnObject);
    }

    /**
     * 管理员修改商品类目信息
     * @param id
     * @return
     */
    @Audit
    @PutMapping("/shops/{shopId}/categories/{id}")
    public  Object updateCategory(@PathVariable("id")Long id, @Validated @RequestBody CategoryVo vo,BindingResult bindingResult){
        logger.debug("update Category: id = "+id);
        //校验前端数据
        Object returnObject = Common.processFieldErrors(bindingResult, httpServletResponse);
        if (null != returnObject) {
            return returnObject;
        }
        Category category=vo.createCategory();
        category.setGmtModified(LocalDateTime.now());
        category.setId(id);

        ReturnObject retObject=categoryService.updateCategory(category);
        if (retObject.getData() != null) {
            return Common.getRetObject(retObject);
        } else {
            return Common.getNullRetObj(new ReturnObject<>(retObject.getCode(), retObject.getErrmsg()), httpServletResponse);
        }
    }



    @Autowired
    private SpuService spuService;

    /**
     * 店家新建商品spu
     * @param vo
     * @param bindingResult
     * @return
     */
    @Audit
    @PostMapping("/shops/{id}/spus")
    public Object insertSpu(@Validated @RequestBody SpuVo vo,BindingResult bindingResult){
        //校验前端数据
        Object returnObject = Common.processFieldErrors(bindingResult, httpServletResponse);
        if (null != returnObject) {
            logger.debug("validate fail");
            return returnObject;
        }
        Spu spu=vo.createSpu();
        spu.setGmtCreate(LocalDateTime.now());
        ReturnObject retObject =spuService.insertSpu(spu);
        if (retObject.getData() != null) {
            httpServletResponse.setStatus(HttpStatus.CREATED.value());
            return Common.getRetObject(retObject);
        } else {
            return Common.getNullRetObj(new ReturnObject<>(retObject.getCode(), retObject.getErrmsg()), httpServletResponse);
        }

    }

    /**
     * 店家修改spu
     * @param id
     * @param vo
     * @param bindingResult
     * @return
     */
    @Audit
    @PutMapping("/shops/{shopId}/spus/{id}")
    Object updateSpu(@PathVariable("id")Long id,@Validated @RequestBody SpuVo vo,BindingResult bindingResult ){
        logger.debug("update spu: id = "+id);

        //校验前端数据
        Object returnObject = Common.processFieldErrors(bindingResult, httpServletResponse);
        if (null != returnObject) {
            logger.debug("validate fail");
            return returnObject;
        }
        Spu spu=vo.createSpu();
        spu.setId(id);
        spu.setGmtCreate(LocalDateTime.now());

        ReturnObject retObject = spuService.updateSpu(spu);
        if (retObject.getData() != null) {
            return Common.getRetObject(retObject);
        } else {
            return Common.getNullRetObj(new ReturnObject<>(retObject.getCode(), retObject.getErrmsg()), httpServletResponse);
        }
    }

    /**
     * 将spu加入品牌
     * @param spuId
     * @param brandId
     * @return
     */
    @Audit
    @PostMapping("/shops/{shopId}/spus/{spuId}/brands/{id}")
    Object insertSpuToBrand(@PathVariable("spuId")Long spuId,
                            @PathVariable("id")Long brandId){

        ReturnObject retObject=spuService.insertSpuToBrand(spuId,brandId);
        return Common.getRetObject(retObject);

    }

    /**
     * 将spu移出品牌
     * @param spuId
     * @param brandId
     * @return
     */
    @Audit
    @DeleteMapping("/shops/{shopId}/spus/{spuId}/brands/{id}")
    Object removeSpuFromBrand(@PathVariable("spuId")Long spuId,
                              @PathVariable("id")Long brandId){

        ReturnObject retObject=spuService.removeSpuFromBrand(spuId,brandId);
        return Common.getRetObject(retObject);

    }

    /**
     * 将spu加入分类
     * @param spuId
     * @param categoryId
     * @return
     */
    @Audit
    @PostMapping("/shops/{shopId}/spus/{spuId}/categories/{id}")
    Object insertSpuToCategory(@PathVariable("spuId")Long spuId,
                            @PathVariable("id")Long categoryId){

        ReturnObject retObject=spuService.insertSpuToCategory(spuId,categoryId);
        return Common.getRetObject(retObject);
    }

    /**
     * 将spu移出分类
     * @param spuId
     * @param categoryId
     * @return
     */
    @Audit
    @DeleteMapping("/shops/{shopId}/spus/{spuId}/categories/{id}")
    Object removeSpuFromCategory(@PathVariable("spuId")Long spuId,
                              @PathVariable("id")Long categoryId){

        ReturnObject retObject=spuService.removeSpuFromCategory(spuId,categoryId);
        return Common.getRetObject(retObject);

    }




    @Autowired
    private SkuService skuService;

    /**
     * 管理员添加新的sku到spu里
     * @param id
     * @param vo
     * @param bindingResult
     * @return
     */
    @Audit
    @PostMapping("/shops/{shopId}/spus/{id}/skus")
    public Object insertSku(@PathVariable("shopId")Long shopId,@PathVariable("id")Long id, @Validated @RequestBody SkuVo vo,BindingResult bindingResult){

        logger.debug("insert sku: spuId = "+id);

        //校验前端数据
       /* Object returnObject = Common.processFieldErrors(bindingResult, httpServletResponse);
        if (null != returnObject) {
            logger.info("validate fail");
            return returnObject;
        }*/

        Sku sku=vo.createSku();
        sku.setGmtCreate(LocalDateTime.now());
        sku.setGoodsSpuId(id);//spuId设为url传入的id
        ReturnObject retObject = skuService.insertSku(shopId,sku);
        if (retObject.getData() != null) {
            httpServletResponse.setStatus(HttpStatus.CREATED.value());
            return Common.getRetObject(retObject);
        } if(retObject.getCode()==ResponseCode.RESOURCE_ID_OUTSCOPE)
        {
            httpServletResponse.setStatus(HttpStatus.FORBIDDEN.value());
            return Common.getNullRetObj(retObject,httpServletResponse);
        }
        else {
            return Common.getNullRetObj(new ReturnObject<>(retObject.getCode(), retObject.getErrmsg()), httpServletResponse);
        }
    }


    /**
     * 店家商品上架
     * @param id
     * @return
     */
    @Audit
    @PutMapping("/shops/{shopId}/skus/{id}/onshelves")
    @ResponseBody
    public Object onlineSku(@PathVariable Long shopId,@PathVariable Long id,
                                @LoginUser @ApiIgnore @RequestParam(required = false) Long userId,
                                @Depart @ApiIgnore @RequestParam(required = false) Long departId){

        logger.debug("onlineSku: id ="+id);

        //校验权限
//        if(departId!=0&&shopId!=departId)
  //          return  Common.getNullRetObj(new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE), httpServletResponse);
        ReturnObject retObject = skuService.putGoodsOnSale(shopId,id);
        if (retObject.getData() != null) {
            return Common.decorateReturnObject(retObject);
        }
        else if(retObject.getCode()== ResponseCode.RESOURCE_ID_OUTSCOPE)
        {
            httpServletResponse.setStatus(HttpStatus.FORBIDDEN.value());
            return Common.getNullRetObj(retObject,httpServletResponse);
        }else {
            return Common.getNullRetObj(new ReturnObject<>(retObject.getCode(), retObject.getErrmsg()), httpServletResponse);
        }


    }

    /**
     * 店家商品下架
     * @param id
     * @return
     */
    @Audit
    @PutMapping("/shops/{shopId}/skus/{id}/offshelves")
    public Object offlineSku(@PathVariable Long shopId,@PathVariable Long id,
                                 @LoginUser @ApiIgnore @RequestParam(required = false) Long userId,
                                 @Depart @ApiIgnore @RequestParam(required = false) Long departId){

        logger.debug("offlineSku: id ="+id);

        //校验是否为该商铺管理员
//        if(departId!=0&&departId!=shopId)
  //          return Common.getRetObject(new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE));
        ReturnObject retObject = skuService.putOffGoodsOnSale(shopId,id);
        if (retObject.getData() != null) {
            return Common.decorateReturnObject(retObject);
        }else if(retObject.getCode()== ResponseCode.RESOURCE_ID_OUTSCOPE)
        {
            httpServletResponse.setStatus(HttpStatus.FORBIDDEN.value());
            return Common.getNullRetObj(retObject,httpServletResponse);
        }
        else {
            return Common.getNullRetObj(new ReturnObject<>(retObject.getCode(), retObject.getErrmsg()), httpServletResponse);
        }

    }
    /**
     * 管理员或店家修改SKU信息
     * @param shopId
     * @param id
     * @param vo
     * @return Object
     */
    @Audit
    @PutMapping("/shops/{shopId}/skus/{id}")
    public Object modifySKU(@PathVariable Long shopId,@PathVariable Long id,
                            @Validated @RequestBody SkuVo vo,BindingResult bindingResult,
                            @LoginUser @ApiIgnore @RequestParam(required = false) Long userId,
                            @Depart @ApiIgnore @RequestParam(required = false) Long departId)
    {
        logger.debug("modifySKU: id = "+ id+" shopId="+shopId+" vo="+vo);
       /* Object returnObject = Common.processFieldErrors(bindingResult, httpServletResponse);
        if (null != returnObject) {
            return returnObject;
        }*/
  //      if(departId!=0&&departId!=shopId)
    //        return Common.getRetObject(new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE));
        Sku sku=vo.createSku();
        sku.setId(id);
        ReturnObject retObject=skuService.modifySku(shopId,sku);
        if (retObject.getData() != null) {
            return Common.decorateReturnObject(retObject);
        }if(retObject.getCode()==ResponseCode.RESOURCE_ID_OUTSCOPE)
    {
        httpServletResponse.setStatus(HttpStatus.FORBIDDEN.value());
        return Common.getNullRetObj(retObject,httpServletResponse);
    }
        else {
            return Common.getNullRetObj(new ReturnObject<>(retObject.getCode(), retObject.getErrmsg()), httpServletResponse);
        }
    }

    /**
     *查询SKU
     * @param shopId
     * @param skuSn
     * @param spuId
     * @param spuSn
     * @param page
     * @param pageSize
     * @return Object
     */
    @GetMapping("/skus")
    @ResponseBody
    public Object getSkuList(
            @RequestParam(required = false)Long shopId,
            @RequestParam(required = false)String skuSn,
            @RequestParam(required = false)Long spuId,
            @RequestParam(required = false)String spuSn,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize)
    {
        logger.debug("getSku:");
        ReturnObject returnObject=skuService.getSkuList(shopId,skuSn,spuId,spuSn,page,pageSize);
        return Common.getPageRetObject(returnObject);
    }

    /**
     * 查询一条sku
     * @param id
     * @return
     */
    @Audit
    @GetMapping("/skus/{id}")
    @ResponseBody
    public Object getSku(@PathVariable Long id )
    {
        logger.debug("getSku:id= "+id);
        ReturnObject returnObject=skuService.getSku(id);
        return Common.getRetObject(returnObject);

    }

    /**
     * 店家删除sku
     * @param shopId
     * @param id
     * @param userId
     * @param departId
     * @return
     */
    @Audit
    @DeleteMapping("/shops/{shopId}/skus/{id}")
    public Object deleteSku(@PathVariable Long shopId, @PathVariable Long id,
                            @LoginUser @ApiIgnore @RequestParam(required = false) Long userId,
                            @Depart @ApiIgnore @RequestParam(required = false) Long departId)
    {
        logger.debug("deleteSku: Skuid = "+ id+" shopId="+shopId);
        ReturnObject retObject=skuService.deleteSku(shopId,id);
        if (retObject.getData() != null) {
            return Common.decorateReturnObject(retObject);
        } else if(retObject.getCode()== ResponseCode.RESOURCE_ID_OUTSCOPE)
        {
            httpServletResponse.setStatus(HttpStatus.FORBIDDEN.value());
            return Common.getNullRetObj(retObject,httpServletResponse);
        }
        else {
            return Common.getNullRetObj(new ReturnObject<>(retObject.getCode(), retObject.getErrmsg()), httpServletResponse);
        }
    }

    /**
     * 新增价格浮动
     * @param shopId
     * @param id
     * @param vo
     * @param bindingResult
     * @param userId
     * @param departId
     * @return
     */
    @Audit
    @PostMapping("/shops/{shopId}/skus/{id}/floatPrices")
    public Object addFloatPrice(@PathVariable Long shopId, @PathVariable Long id,
                                     @Validated @RequestBody FloatPriceVo vo, BindingResult bindingResult,
                                     @LoginUser @ApiIgnore @RequestParam(required = false) Long userId,
                                     @Depart @ApiIgnore @RequestParam(required = false) Long departId)
    {
        //判断时间是否存在问题
        if(vo.getBeginTime().isAfter(vo.getEndTime()))
            return Common.getRetObject(new ReturnObject<>(ResponseCode.Log_Bigger));

        Object returnObject = Common.processFieldErrors(bindingResult, httpServletResponse);
        if (null != returnObject) {
            logger.error("error");
            return returnObject;
        }
        FloatPrice floatPrice=vo.createFloatPrice();
        floatPrice.setGoodsSkuId(id);
        floatPrice.setValid(FloatPrice.State.VALID);
        floatPrice.setCreatedBy(userId);
        floatPrice.setInvalidBy(userId);
        ReturnObject retObject=skuService.addFloatPrice(shopId,floatPrice,userId);
        if (retObject.getData() != null) {
            httpServletResponse.setStatus(HttpStatus.CREATED.value());
            return Common.decorateReturnObject(retObject);
        } else if(retObject.getCode()== ResponseCode.RESOURCE_ID_OUTSCOPE)
        {
            httpServletResponse.setStatus(HttpStatus.FORBIDDEN.value());
            return Common.getNullRetObj(retObject,httpServletResponse);
        }
        else {
            return Common.getNullRetObj(new ReturnObject<>(retObject.getCode(), retObject.getErrmsg()), httpServletResponse);
        }
    }
    @Audit
    @DeleteMapping("/shops/{shopId}/floatPrices/{id}")
    public Object addFloatPrice(@PathVariable Long shopId, @PathVariable Long id,
                                @LoginUser @ApiIgnore @RequestParam(required = false) Long userId,
                                @Depart @ApiIgnore @RequestParam(required = false) Long departId)
    {
        ReturnObject retObject=skuService.deleteFloatPrice(shopId,id);
        if (retObject.getData() != null) {
            return Common.decorateReturnObject(retObject);
        } else if(retObject.getCode()== ResponseCode.RESOURCE_ID_OUTSCOPE)
        {
            httpServletResponse.setStatus(HttpStatus.FORBIDDEN.value());
            return Common.getNullRetObj(retObject,httpServletResponse);
        }
        else {
            return Common.getNullRetObj(new ReturnObject<>(retObject.getCode(), retObject.getErrmsg()), httpServletResponse);
        }
    }


    /**
     * 查询一条spu
     * @param id
     * @return
     */
    @Audit
    @GetMapping("/spus/{id}")
    @ResponseBody
    public Object getSpu(@PathVariable Long id )
    {
        logger.info("getSpu:id= "+id);
        ReturnObject returnObject=spuService.getSpu(id);
        return Common.getRetObject(returnObject);

    }
}

