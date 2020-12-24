package cn.edu.xmu.goods.controller;

import cn.edu.xmu.goods.model.bo.*;
import cn.edu.xmu.goods.model.vo.*;
import cn.edu.xmu.ooad.annotation.*;
import cn.edu.xmu.ooad.model.VoObject;
import cn.edu.xmu.ooad.util.Common;
import cn.edu.xmu.ooad.util.JwtHelper;
import cn.edu.xmu.ooad.util.ResponseCode;
import cn.edu.xmu.ooad.util.ResponseUtil;
import cn.edu.xmu.ooad.util.ReturnObject;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;
import cn.edu.xmu.goods.service.GoodsService;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * 商品模块控制器
 * @author 李狄翰
 * Modified at 2020/11/24 17:00
 **/
@Api(value = "商品服务", tags = "goods")
@RestController /*Restful的Controller对象*/
@RequestMapping(value = "/goods", produces = "application/json;charset=UTF-8")
public class GoodsController {

    private  static  final Logger logger = LoggerFactory.getLogger(GoodsController.class);

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private GoodsService goodsService;

    @ApiOperation(value = "管理员新增品牌")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="id", required = true, dataType="String", paramType="path"),
            @ApiImplicitParam(name="vo", required = true, dataType="Brandvo", paramType="body")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @PostMapping("shops/{id}/brands")
    public Object createBrand(@PathVariable Long id, @RequestBody BrandVo vo, @LoginUser Long userId, @Depart Long departId){
        logger.debug("createBrand: id = "+ id );
        Brand bo=new Brand(vo);
        if(!Objects.equals(departId,0L))
        {
            return new ResponseEntity(ResponseUtil.ok(), HttpStatus.FORBIDDEN);
        }
        if(Objects.equals(null,bo.getName()))
        {
            return Common.decorateReturnObject(new ReturnObject(ResponseCode.FIELD_NOTVALID));
        }
        if(bo.getName().isBlank())
        {
            return Common.decorateReturnObject(new ReturnObject(ResponseCode.FIELD_NOTVALID));
        }
        ReturnObject returnObject =  goodsService.createBrand(id,bo);
        if(Objects.equals(returnObject.getCode(),ResponseCode.OK))
        {
            return new ResponseEntity(ResponseUtil.ok(returnObject.getData()), HttpStatus.CREATED);
        }
        return Common.decorateReturnObject(returnObject);
    }

    @ApiOperation(value = "管理员修改品牌")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="shopid", required = true, dataType="int", paramType="path"),
            @ApiImplicitParam(name="id", required = true, dataType="int", paramType="path"),
            @ApiImplicitParam(name="vo", required = true, dataType="Brandvo", paramType="body")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @PutMapping("shops/{shopId}/brands/{id}")
    public Object changeBrand(@PathVariable Long shopId,@PathVariable Long id, @RequestBody BrandVo vo, @LoginUser Long userId, @Depart Long departId){
        logger.debug("createBrand: id = "+ id );
        if(!Objects.equals(departId,0L))
        {
            return new ResponseEntity(ResponseUtil.ok(), HttpStatus.FORBIDDEN);
        }
        ReturnObject returnObject =  goodsService.changeBrand(id,vo);
        return Common.decorateReturnObject(returnObject);
    }

    @ApiOperation(value = "管理员删除品牌")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="shopid", required = true, dataType="int", paramType="path"),
            @ApiImplicitParam(name="id", required = true, dataType="int", paramType="path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @DeleteMapping("shops/{shopId}/brands/{id}")
    public Object deleteBrand(@PathVariable Long shopId,@PathVariable Long id, @LoginUser Long userId, @Depart Long departId){
        logger.debug("deleteBrand: id = "+ id );
        if(!Objects.equals(departId,0L))
        {
            return new ResponseEntity(ResponseUtil.ok(), HttpStatus.FORBIDDEN);
        }
        ReturnObject returnObject =  goodsService.deleteBrand(id);
        return Common.decorateReturnObject(returnObject);
    }

    @ApiOperation(value = "获得商品的所有状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功")
    })
    @GetMapping("skus/states")
    public Object getSkuAllStates(@LoginUser Long userId, @Depart Long departId){
        logger.debug("getSkuAllStates: 用户 = "+ userId );
        GoodsSku.State[] states=GoodsSku.State.class.getEnumConstants();
        List<GoodsSkuStateVo> goodsSkuStateVos =new ArrayList<GoodsSkuStateVo>();
        for(int i=0;i<states.length;i++){
            goodsSkuStateVos.add(new GoodsSkuStateVo(states[i]));
        }
        return ResponseUtil.ok(new ReturnObject<List>(goodsSkuStateVos).getData());
    }

    @ApiOperation(value = "查询SKU")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", dataType = "int", name = "shopId", value = "商店id", required = false),
            @ApiImplicitParam(paramType = "query", dataType = "string", name = "skuSn", value = "规格编号", required = false),
            @ApiImplicitParam(paramType = "query", dataType = "int", name = "spuId", value = "商品id", required = false),
            @ApiImplicitParam(paramType = "query", dataType = "string", name = "spuSn", value = "商品编号", required = false),
            @ApiImplicitParam(paramType = "query", dataType = "int", name = "page", value = "页码", required = false),
            @ApiImplicitParam(paramType = "query", dataType = "int", name = "page_size", value = "每页数目", required = false)

    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @GetMapping("skus")
    public Object getSku(
            @RequestParam(required = false, defaultValue = "0") Long shopId,
            @RequestParam(required = false, defaultValue = "0") String skuSn,
            @RequestParam(required = false, defaultValue = "0") Long spuId,
            @RequestParam(required = false, defaultValue = "0") String spuSn,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize
    ){
        logger.debug("getSku");
        ReturnObject<PageInfo<VoObject>> returnObject =  goodsService.getSku(shopId,skuSn,spuId,spuSn, page, pageSize);
        return Common.getPageRetObject(returnObject);
    }

    @ApiOperation(value = "获得SKU详细信息")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "id", value = "SKUid", required = true),
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @GetMapping("skus/{id}")
    public Object getOneSku(
            @PathVariable Long id
    ){
        logger.debug("getOneSku");
        ReturnObject<VoObject> returnObject =  goodsService.getOneSku(id);
        return Common.decorateReturnObject(returnObject);
    }

    @ApiOperation(value = "管理员添加新的SKU到SPU里")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="id", required = true, dataType="Integer", paramType="path"),
            @ApiImplicitParam(name="shopId", required = true, dataType="Integer", paramType="path"),
            @ApiImplicitParam(name="vo", required = true, dataType="GoodsSkuVo", paramType="body")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @PostMapping("shops/{shopId}/spus/{id}/skus")
    public Object addSkuToSpu(@PathVariable Long shopId, @PathVariable Long id, @RequestBody GoodsSkuVo vo, @LoginUser Long userId, @Depart Long departId){
        logger.debug("addSkuToSpu: id = "+ id );
        GoodsSku bo=new GoodsSku(vo);
        ReturnObject returnObject =  goodsService.addSkuToSpu(id, shopId,userId,bo);
        if(Objects.equals(returnObject.getCode(),ResponseCode.OK))
        {
            return new ResponseEntity(ResponseUtil.ok(returnObject.getData()), HttpStatus.CREATED);
        }
        return Common.decorateReturnObject(returnObject);
    }

    @ApiOperation(value = "管理员逻辑删除SKU")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="id", required = true, dataType="Integer", paramType="path"),
            @ApiImplicitParam(name="shopId", required = true, dataType="Integer", paramType="path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @DeleteMapping("shops/{shopId}/skus/{id}")
    public Object deleteSku(@PathVariable Long shopId, @PathVariable Long id,  @LoginUser Long userId, @Depart Long departId){
        logger.debug("deleteSku: id = "+ id );
        ReturnObject returnObject =  goodsService.deleteSku(id, shopId);
        return Common.decorateReturnObject(returnObject);
    }

    @ApiOperation(value = "管理员修改SKU信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="id", required = true, dataType="Integer", paramType="path"),
            @ApiImplicitParam(name="shopId", required = true, dataType="Integer", paramType="path"),
            @ApiImplicitParam(name="vo", required = true, dataType="GoodsSkuVo", paramType="body")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @PutMapping("shops/{shopId}/skus/{id}")
    public Object changeSku(@PathVariable Long shopId, @PathVariable Long id, @RequestBody GoodsSkuVo vo, @LoginUser Long userId, @Depart Long departId){
        logger.debug("changeSku: id = "+ id );
        GoodsSku bo=new GoodsSku(vo);
        ReturnObject returnObject =  goodsService.changeSku(id, shopId,bo);
        return Common.decorateReturnObject(returnObject);
    }


    @ApiOperation(value = "查询商品分类关系")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "id", value = "种类id", required = true)
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @GetMapping("/categories/{id}/subcategories")
    public Object getCategory(
            @PathVariable Long id
    ){
        logger.debug("getCategory");
        ReturnObject<List> returnObject =  goodsService.getCategory(id);
        return Common.decorateReturnObject(returnObject);
    }

    @ApiOperation(value = "管理员新增商品类目")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "Id", value = "种类id", required = true),
            @ApiImplicitParam(name="vo", required = true, dataType="GoodsCategoryVo", paramType="body")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @PostMapping("/shops/{shopId}/categories/{id}/subcategories")
    public Object createCategory(
            @PathVariable Long shopId,
            @PathVariable Long id,
            @RequestBody GoodsCategoryVo vo,
            @Depart Long departId
    ){
        logger.debug("createCategory");
        if(!Objects.equals(departId,0L))
        {
            return new ResponseEntity(ResponseUtil.ok(), HttpStatus.FORBIDDEN);
        }
        if(Objects.equals(null,vo.getName()))
        {
            return Common.decorateReturnObject(new ReturnObject(ResponseCode.FIELD_NOTVALID));
        }
        if(vo.getName().isBlank())
        {
            return Common.decorateReturnObject(new ReturnObject(ResponseCode.FIELD_NOTVALID));
        }
        ReturnObject returnObject =  goodsService.createCategory(id,vo);
        if(Objects.equals(returnObject.getCode(),ResponseCode.OK))
        {
            return new ResponseEntity(ResponseUtil.ok(returnObject.getData()), HttpStatus.CREATED);
        }
        return Common.decorateReturnObject(returnObject);
    }

    @ApiOperation(value = "管理员修改商品类目信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "Id", value = "种类id", required = true),
            @ApiImplicitParam(name="vo", required = true, dataType="GoodsCategoryVo", paramType="body")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @PutMapping("/shops/{shopId}/categories/{id}")
    public Object changeCategory(
            @PathVariable Long shopId,
            @PathVariable Long id,
            @RequestBody GoodsCategoryVo vo,
            @Depart Long departId
    ){
        logger.debug("changeCategory");
        if(!Objects.equals(departId,0L))
        {
            return new ResponseEntity(ResponseUtil.ok(), HttpStatus.FORBIDDEN);
        }
        ReturnObject returnObject =  goodsService.changeCategory(id,vo);
        return Common.decorateReturnObject(returnObject);
    }

    @ApiOperation(value = "管理员删除商品类目信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "Id", value = "种类id", required = true)
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @DeleteMapping("/shops/{shopId}/categories/{id}")
    public Object deleteCategory(
            @PathVariable Long shopId,
            @PathVariable Long id,
            @Depart Long departId
    ){
        logger.debug("deleteCategory");
        if(!Objects.equals(departId,0L))
        {
            return new ResponseEntity(ResponseUtil.ok(), HttpStatus.FORBIDDEN);
        }
        ReturnObject returnObject= goodsService.deleteCategory(id);
        return Common.decorateReturnObject(returnObject);
    }

    @ApiOperation(value = "查看一条商品Spu的详细信息")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "id", value = "SPUid", required = true),
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @GetMapping("spus/{id}")
    public Object getOneSpu(
            @PathVariable Long id
    ){
        logger.debug("getOneSpu");
        ReturnObject<VoObject> returnObject =  goodsService.getOneSpu(id);
        return Common.decorateReturnObject(returnObject);
    }


    @ApiOperation(value = "查看一条被分享的商品Sku的详细信息")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "id", value = "SPUid", required = true),
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "sid", value = "分享id", required = true),
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @GetMapping("share/{sid}/skus/{id}")
    public Object getOneShareSku(
            @PathVariable Long id,
            @PathVariable Long sid,
            @LoginUser Long userId
    ){
        logger.debug("getOneShareSku");
        ReturnObject<VoObject> returnObject =  goodsService.getOneShareSku(id,sid,userId);
        if(Objects.equals(returnObject.getCode(),ResponseCode.RESOURCE_ID_NOTEXIST))
        {
            return new ResponseEntity(ResponseUtil.fail(ResponseCode.RESOURCE_ID_NOTEXIST),HttpStatus.FORBIDDEN);
        }
        return Common.decorateReturnObject(returnObject);
    }


    @ApiOperation(value = "查看所有品牌")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", dataType = "int", name = "page", value = "页码", required = false),
            @ApiImplicitParam(paramType = "query", dataType = "int", name = "page_size", value = "每页数目", required = false)

    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @GetMapping("brands")
    public Object getBrands(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize
    ){
        logger.debug("getBrands");
        ReturnObject<PageInfo<VoObject>> returnObject =  goodsService.getBrands( page, pageSize);
        return Common.getPageRetObject(returnObject);
    }

    @ApiOperation(value = "店家新建商品Spu")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "id", value = "店铺id", required = true),
            @ApiImplicitParam(name="vo", required = true, dataType="GoodsSpuVo", paramType="body")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @PostMapping("shops/{id}/spus")
    public Object createSpu(
            @PathVariable Long id,
            @RequestBody GoodsSpuVo vo
    ){
        logger.debug("createSpu");
        GoodsSpu bo = new GoodsSpu(vo);
        ReturnObject returnObject =  goodsService.createSpu(id,bo);
        return Common.decorateReturnObject(returnObject);
    }


    @ApiOperation(value = "店家修改商品Spu")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "shopId", value = "店铺id", required = true),
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "id", value = "SPUid", required = true),
            @ApiImplicitParam(name="vo", required = true, dataType="GoodsSpuVo", paramType="body")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @PutMapping("shops/{shopId}/spus/{id}")
    public Object changeSpu(
            @PathVariable Long shopId,
            @PathVariable Long id,
            @RequestBody GoodsSpuVo vo
    ){
        logger.debug("changeSpu");
        ReturnObject returnObject =  goodsService.changeSpu(shopId,id,vo);
        return Common.decorateReturnObject(returnObject);
    }


    @ApiOperation(value = "店家逻辑删除商品Spu")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "shopId", value = "店铺id", required = true),
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "id", value = "SPUid", required = true)
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @DeleteMapping("shops/{shopId}/spus/{id}")
    public Object deleteSpu(
            @PathVariable Long shopId,
            @PathVariable Long id
    ){
        logger.debug("deleteSpu");
        ReturnObject returnObject =  goodsService.deleteSpu(shopId,id);
        return Common.decorateReturnObject(returnObject);
    }


    @ApiOperation(value = "店家上架商品Sku")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "shopId", value = "店铺id", required = true),
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "id", value = "SPUid", required = true)
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @PutMapping("shops/{shopId}/skus/{id}/onshelves")
    public Object onlineSku(
            @PathVariable Long shopId,
            @PathVariable Long id
    ){
        logger.debug("onlineSku");
        ReturnObject returnObject =  goodsService.onlineSku(shopId,id);
        return Common.decorateReturnObject(returnObject);
    }


    @ApiOperation(value = "店家下架商品Spu")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "shopId", value = "店铺id", required = true),
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "id", value = "SPUid", required = true)
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @PutMapping("shops/{shopId}/skus/{id}/offshelves")
    public Object offlineSku(
            @PathVariable Long shopId,
            @PathVariable Long id
    ){
        logger.debug("offlineSku");
        ReturnObject returnObject =  goodsService.offlineSku(shopId,id);
        return Common.decorateReturnObject(returnObject);
    }


    @ApiOperation(value = "店家新建商品价格浮动")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "shopId", value = "店铺id", required = true),
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "id", value = "SKUid", required = true),
            @ApiImplicitParam(name="vo", required = true, dataType="FloatPriceVo", paramType="body")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @PostMapping("shops/{shopId}/skus/{id}/floatPrices")
    public Object createFloatPrice(
            @PathVariable Long shopId,
            @PathVariable Long id,
            @RequestBody FloatPriceVo vo,
            @LoginUser Long userId
    ){
        logger.debug("createFloatPrice");
        FloatPrice bo=new FloatPrice(vo);
        if (!bo.isBiggerBegin())
        {
            return Common.decorateReturnObject(new ReturnObject(ResponseCode.Log_Bigger));
        }
        if (!bo.beginAfterNow())
        {
            return Common.decorateReturnObject(new ReturnObject(ResponseCode.FIELD_NOTVALID));
        }
        if(bo.getQuantity()<0)
        {
            return Common.decorateReturnObject(new ReturnObject(ResponseCode.FIELD_NOTVALID));
        }
        ReturnObject returnObject =  goodsService.createFloatPrice(shopId,id,bo,userId);
        if(Objects.equals(returnObject.getCode(),ResponseCode.SKUPRICE_CONFLICT))
        {
            return new ResponseEntity(ResponseUtil.fail(returnObject.getCode(), returnObject.getErrmsg()),
                    HttpStatus.FORBIDDEN);
        }
        if(Objects.equals(returnObject.getCode(),ResponseCode.OK))
        {
            return new ResponseEntity(ResponseUtil.ok(returnObject.getData()), HttpStatus.CREATED);
        }
        return Common.decorateReturnObject(returnObject);
    }

    @ApiOperation(value = "店家失效商品价格浮动")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "shopId", value = "店铺id", required = true),
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "id", value = "浮动价格id", required = true)
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @DeleteMapping("shops/{shopId}/floatPrices/{id}")
    public Object invalidFloatPrice(
            @PathVariable Long shopId,
            @PathVariable Long id,
            @LoginUser Long userId
    ){
        logger.debug("invalidFloatPrice");
        ReturnObject returnObject =  goodsService.invalidFloatPrice(shopId,id,userId);
        return Common.decorateReturnObject(returnObject);
    }

    @ApiOperation(value = "将SPU加入分类")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="shopId", required = true, dataType="int", paramType="path"),
            @ApiImplicitParam(name="spuId", required = true, dataType="int", paramType="path"),
            @ApiImplicitParam(name="id", required = true, dataType="int", paramType="path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @PostMapping("shops/{shopId}/spus/{spuId}/categories/{id}")
    public Object addSpuToCategory(@PathVariable Long shopId,@PathVariable Long spuId,@PathVariable Long id,@Depart Long depart){
        logger.debug("addSpuToCategory: id = "+ id );
        if(!Objects.equals(depart,0L))
        {
            return new ResponseEntity(ResponseUtil.ok(), HttpStatus.FORBIDDEN);
        }
        ReturnObject returnObject =  goodsService.addSpuToCategory(spuId,id);
        return Common.decorateReturnObject(returnObject);
    }

    @ApiOperation(value = "将SPU移出分类")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="shopId", required = true, dataType="int", paramType="path"),
            @ApiImplicitParam(name="spuId", required = true, dataType="int", paramType="path"),
            @ApiImplicitParam(name="id", required = true, dataType="int", paramType="path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @DeleteMapping("shops/{shopId}/spus/{spuId}/categories/{id}")
    public Object removeSpuToCategory(@PathVariable Long shopId,@PathVariable Long spuId,@PathVariable Long id,@Depart Long depart){
        logger.debug("removeSpuToCategory: id = "+ id );
        if(!Objects.equals(depart,0L))
        {
            return new ResponseEntity(ResponseUtil.ok(), HttpStatus.FORBIDDEN);
        }
        ReturnObject returnObject =  goodsService.removeSpuToCategory(spuId,id);
        return Common.decorateReturnObject(returnObject);
    }

    @ApiOperation(value = "将SPU加入品牌")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="shopid", required = true, dataType="int", paramType="path"),
            @ApiImplicitParam(name="spuid", required = true, dataType="int", paramType="path"),
            @ApiImplicitParam(name="id", required = true, dataType="int", paramType="path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @PostMapping("shops/{shopId}/spus/{spuId}/brands/{id}")
    public Object addSpuToBrand(@PathVariable Long shopId,@PathVariable Long spuId,@PathVariable Long id,@Depart Long departId){
        logger.debug("addSpuToBrand: id = "+ id );
        if(!Objects.equals(departId,0L))
        {
            return new ResponseEntity(ResponseUtil.ok(), HttpStatus.FORBIDDEN);
        }
        ReturnObject returnObject =  goodsService.addSpuToBrand(spuId,id);
        return Common.decorateReturnObject(returnObject);
    }

    @ApiOperation(value = "将SPU移出品牌")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="shopid", required = true, dataType="int", paramType="path"),
            @ApiImplicitParam(name="spuid", required = true, dataType="int", paramType="path"),
            @ApiImplicitParam(name="id", required = true, dataType="int", paramType="path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @DeleteMapping("shops/{shopId}/spus/{spuId}/brands/{id}")
    public Object removeSpuToBrand(@PathVariable Long shopId,@PathVariable Long spuId,@PathVariable Long id,@Depart Long departId){
        logger.debug("removeSpuToBrand: id = "+ id );
        if(!Objects.equals(departId,0L))
        {
            return new ResponseEntity(ResponseUtil.ok(), HttpStatus.FORBIDDEN);
        }
        ReturnObject returnObject =  goodsService.removeSpuToBrand(spuId,id);
        return Common.decorateReturnObject(returnObject);
    }

    @ApiOperation(value = "sku上传图片")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "shopid", required = true, dataType = "int", paramType = "path"),
            @ApiImplicitParam(name = "id", required = true, dataType = "int", paramType = "path"),
            @ApiImplicitParam(name = "img", required = true, dataType = "file", value = "文件", paramType = "formData")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @PostMapping("shops/{shopId}/skus/{id}/uploadImg")
    public Object uploadSkuImg(@PathVariable Long shopId, @PathVariable Long id,  MultipartFile img) {
        logger.debug("uploadImg: shopId = " + shopId + " id = " + id + " img:" + img.getOriginalFilename());
        ReturnObject returnObject = goodsService.uploadSkuImg(shopId, id,img);
        return Common.decorateReturnObject(returnObject);
    }

    @ApiOperation(value = "spu上传图片")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "shopid", required = true, dataType = "int", paramType = "path"),
            @ApiImplicitParam(name = "id", required = true, dataType = "int", paramType = "path"),
            @ApiImplicitParam(name = "img", required = true, dataType = "file", value = "文件", paramType = "formData")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @PostMapping("shops/{shopId}/spus/{id}/uploadImg")
    public Object uploadSpuImg(@PathVariable Long shopId, @PathVariable Long id,  MultipartFile img) {
        logger.debug("uploadImg: shopId = " + shopId + " id = " + id + " img:" + img.getOriginalFilename());
        ReturnObject returnObject = goodsService.uploadSpuImg(shopId, id,img);
        return Common.decorateReturnObject(returnObject);
    }

    @ApiOperation(value = "品牌上传图片")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "shopid", required = true, dataType = "int", paramType = "path"),
            @ApiImplicitParam(name = "id", required = true, dataType = "int", paramType = "path"),
            @ApiImplicitParam(name = "img", required = true, dataType = "file", value = "文件", paramType = "formData")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @PostMapping("shops/{shopId}/brands/{id}/uploadImg")
    public Object uploadBrandImg(@PathVariable Long shopId, @PathVariable Long id,  MultipartFile img) {
        logger.debug("uploadImg: shopId = " + shopId + " id = " + id + " img:" + img.getOriginalFilename());
        ReturnObject returnObject = goodsService.uploadBrandImg(shopId, id,img);
        return Common.decorateReturnObject(returnObject);
    }



}
