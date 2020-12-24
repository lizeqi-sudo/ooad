package cn.edu.xmu.flashsale.controller;

import cn.edu.xmu.flashsale.model.bo.FlashSale;
import cn.edu.xmu.flashsale.model.vo.*;
import cn.edu.xmu.flashsale.service.FlashSaleService;
import cn.edu.xmu.ooad.annotation.*;
import cn.edu.xmu.ooad.util.ResponseCode;
import cn.edu.xmu.ooad.util.Common;
import cn.edu.xmu.ooad.util.ResponseUtil;
import cn.edu.xmu.ooad.util.ReturnObject;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import cn.edu.xmu.ooad.model.VoObject;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;


/**
 * 秒杀模块控制器
 * @author 李狄翰
 * Modified at 2020/11/23 17:00
 **/
@Api(value = "秒杀服务", tags = "flashsale")
@RestController /*Restful的Controller对象*/
@RequestMapping(value = "/flashsale", produces = "application/json;charset=UTF-8")
public class FlashSaleController {

    private  static  final Logger logger = LoggerFactory.getLogger(FlashSaleController.class);

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private FlashSaleService flashSaleService;

    @ApiOperation(value = "平台管理员在某个时段下新建秒杀")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="id", required = true, dataType="int", paramType="path"),
            @ApiImplicitParam(name="vo", required = true, dataType="FlashSaleVo", paramType="body")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @PostMapping("shops/{did}/timesegments/{id}/flashsales")
    public Object createFlashSale(@PathVariable Long id,
                                  @PathVariable Long did,
                                  @RequestBody FlashSaleVo vo,
                                  @LoginUser Long userId,
                                  @Depart Long departId){
        logger.debug("createFlashSale");
        if(!Objects.equals(departId,0L))
        {
            return new ResponseEntity(ResponseUtil.ok(), HttpStatus.FORBIDDEN);
        }
        FlashSale flashSale=new FlashSale(vo);
        if(Objects.equals(flashSale.getFlashDate().toLocalDate(),LocalDateTime.now().toLocalDate()) || flashSale.getFlashDate().isBefore(LocalDateTime.now()))
        {
            return Common.decorateReturnObject(new ReturnObject(ResponseCode.FIELD_NOTVALID));
        }
        ReturnObject returnObject=flashSaleService.createFlashSale(id,flashSale);
        return Common.decorateReturnObject(returnObject);
    }

    @ApiOperation(value = "平台管理员删除某个时段秒杀")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="id", required = true, dataType="int", paramType="path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @DeleteMapping("shops/{did}/flashsales/{id}")
    public Object deleteFlashSale(@PathVariable Long id,@PathVariable Long did, @LoginUser Long userId, @Depart Long departId){
        logger.debug("deleteFlashSale");
        if(!Objects.equals(departId,0L))
        {
            return new ResponseEntity(ResponseUtil.ok(), HttpStatus.FORBIDDEN);
        }
        ReturnObject returnObject=flashSaleService.deleteFlashSale(id);
        return Common.decorateReturnObject(returnObject);
    }

    @ApiOperation(value = "平台管理员修改秒杀活动")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="id", required = true, dataType="int", paramType="path"),
            @ApiImplicitParam(name="vo", required = true, dataType="FlashSaleVo", paramType="body")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @PutMapping("shops/{did}/flashsales/{id}")
    public Object changeFlashSale(@PathVariable Long id, @PathVariable Long did,@RequestBody FlashSaleVo vo, @LoginUser Long userId, @Depart Long departId){
        logger.debug("changeFlashSale");
        if(!Objects.equals(departId,0L))
        {
            return new ResponseEntity(ResponseUtil.ok(), HttpStatus.FORBIDDEN);
        }
        FlashSale flashSale=new FlashSale(vo);
        if(flashSale.getFlashDate().isBefore(LocalDateTime.now()))
        {
            return ResponseUtil.fail(ResponseCode.FIELD_NOTVALID);
        }
        ReturnObject returnObject=flashSaleService.changeFlashSale(id,flashSale);
        return Common.decorateReturnObject(returnObject);
    }

    @ApiOperation(value = "平台管理员上线秒杀活动")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="id", required = true, dataType="int", paramType="path"),
            @ApiImplicitParam(name="did", required = true, dataType="int", paramType="path"),
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @PutMapping("shops/{did}/flashsales/{id}/onshelves")
    public Object onlineFlashSale(@PathVariable Long id, @PathVariable Long did, @LoginUser Long userId, @Depart Long departId){
        logger.debug("onlineFlashSale");
        if(!Objects.equals(departId,0L))
        {
            return new ResponseEntity(ResponseUtil.ok(), HttpStatus.FORBIDDEN);
        }
        ReturnObject returnObject=flashSaleService.onlineFlashSale(id);
        return Common.decorateReturnObject(returnObject);
    }

    @ApiOperation(value = "平台管理员下线秒杀活动")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="id", required = true, dataType="int", paramType="path"),
            @ApiImplicitParam(name="did", required = true, dataType="int", paramType="path"),
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @PutMapping("shops/{did}/flashsales/{id}/offshelves")
    public Object offlineFlashSale(@PathVariable Long id, @PathVariable Long did, @LoginUser Long userId, @Depart Long departId){
        logger.debug("offlineFlashSale");
        if(!Objects.equals(departId,0L))
        {
            return new ResponseEntity(ResponseUtil.ok(), HttpStatus.FORBIDDEN);
        }
        ReturnObject returnObject=flashSaleService.offlineFlashSale(id);
        return Common.decorateReturnObject(returnObject);
    }

    @ApiOperation(value = "平台管理员向秒杀活动添加商品SKU")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="id", required = true, dataType="int", paramType="path"),
            @ApiImplicitParam(name="vo", required = true, dataType="FlashSaleItemVo", paramType="body")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @PostMapping("shops/{did}/flashsales/{id}/flashitems")
    public Object createFlashSaleItem(@PathVariable Long id, @PathVariable Long did,@RequestBody FlashSaleItemVo vo, @LoginUser Long userId, @Depart Long departId){
        logger.debug("createFlashSaleItem");
        if(!Objects.equals(departId,0L))
        {
            return new ResponseEntity(ResponseUtil.ok(), HttpStatus.FORBIDDEN);
        }
        ReturnObject returnObject=flashSaleService.createFlashSaleItem(id,vo);
        return Common.decorateReturnObject(returnObject);
    }

    @ApiOperation(value = "平台管理员在秒杀活动中删除某个SKU")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="id", required = true, dataType="int", paramType="path"),
            @ApiImplicitParam(name="fid", required = true, dataType="int", paramType="path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @DeleteMapping("shops/{did}/flashsales/{fid}/flashitems/{id}")
    public Object deleteFlashSaleItem(@PathVariable Long fid,@PathVariable Long did,@PathVariable Long id, @LoginUser Long userId, @Depart Long departId){
        logger.debug("deleteFlashSaleItem");
        if(!Objects.equals(departId,0L))
        {
            return new ResponseEntity(ResponseUtil.ok(), HttpStatus.FORBIDDEN);
        }
        ReturnObject returnObject=flashSaleService.deleteFlashSaleItem(fid,id);
        return Common.decorateReturnObject(returnObject);
    }

    @ApiOperation(value = "查询某一时段秒杀活动详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "id", required = true, dataType = "int", paramType = "path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @GetMapping("timesegments/{id}/flashsales")
    public Flux<FlashSaleItemRetVo> getFlashDetail(@PathVariable Long id) {
        logger.debug("getFlashDetail: id = " + id);
        return flashSaleService.getFlashDetail(id);
    }

    @ApiOperation(value = "查询当前时段秒杀活动详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "id", required = true, dataType = "int", paramType = "path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @GetMapping("flashsales/current")
    public Flux<FlashSaleItemRetVo> getCurrentFlashDetail() {
        logger.debug("getFlashDetail: id = ");
        return flashSaleService.getCurrentFlashDetail();
    }

    @ApiOperation(value = "删除某个秒杀")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", required = true, dataType = "int", paramType = "path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @GetMapping("flashsales/{id}")
    public Object removeFlashSale(@PathVariable Long id) {
        return flashSaleService.removeFlashSale(id);
    }


}
