package cn.edu.xmu.goods.controller;

import cn.edu.xmu.goods.model.bo.Comment;
import cn.edu.xmu.goods.model.bo.GrouponActivity;
import cn.edu.xmu.goods.model.vo.GrouponActivityStateVo;
import cn.edu.xmu.goods.model.vo.GrouponActivityVo;
import cn.edu.xmu.goods.service.GrouponService;
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
import org.springframework.web.bind.annotation.*;
import cn.edu.xmu.ooad.model.VoObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

/**
 * 团购模块控制器
 *
 * @author 李狄翰
 * Modified at 2020/11/23 17:00
 **/
@Api(value = "团购服务", tags = "groupon")
@RestController /*Restful的Controller对象*/
@RequestMapping(value = "/groupon", produces = "application/json;charset=UTF-8")
public class GrouponController {

    private static final Logger logger = LoggerFactory.getLogger(GrouponController.class);

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private GrouponService grouponService;


    @ApiOperation(value = "获得团购活动的所有状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功")
    })
    @GetMapping("groupons/states")
    public Object getGrouponAllStates() {
        logger.debug("getGrouponAllStates:" );
        GrouponActivity.State[] states = GrouponActivity.State.class.getEnumConstants();
        List<GrouponActivityStateVo> grouponActivityStateVos = new ArrayList<GrouponActivityStateVo>();
        for (int i = 0; i < states.length; i++) {
            grouponActivityStateVos.add(new GrouponActivityStateVo(states[i]));
        }
        return ResponseUtil.ok(new ReturnObject<List>(grouponActivityStateVos).getData());
    }

    @ApiOperation(value = "查询所有有效的团购活动")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", dataType = "int", name = "shopId", value = "商店id", required = false),
            @ApiImplicitParam(paramType = "query", dataType = "int", name = "timeline", value = "时间", required = false),
            @ApiImplicitParam(paramType = "query", dataType = "int", name = "spuId", value = "商品id", required = false),
            @ApiImplicitParam(paramType = "query", dataType = "int", name = "page", value = "页码", required = false),
            @ApiImplicitParam(paramType = "query", dataType = "int", name = "page_size", value = "每页数目", required = false),
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @GetMapping("groupons")
    public Object getGrouponActivity(
            @RequestParam(required = false, defaultValue = "0") Long shopId,
            @RequestParam(required = false, defaultValue = "4") Long timeline,
            @RequestParam(required = false, defaultValue = "0") Long spuId,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize
    ) {
        logger.debug("getGrouponActivity");
        ReturnObject<PageInfo<VoObject>> returnObject = grouponService.getGrouponActivity(shopId, timeline, spuId, page, pageSize);
        return Common.getPageRetObject(returnObject);
    }

    @ApiOperation(value = "管理员查询店铺内的团购活动")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", dataType = "int", name = "shopId", value = "商店id", required = true),
            @ApiImplicitParam(paramType = "query", dataType = "int", name = "state", value = "状态", required = false),
            @ApiImplicitParam(paramType = "query", dataType = "int", name = "spuId", value = "商品id", required = false),
            @ApiImplicitParam(paramType = "query", dataType = "int", name = "page", value = "页码", required = false),
            @ApiImplicitParam(paramType = "query", dataType = "int", name = "page_size", value = "每页数目", required = false),
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @GetMapping("shops/{id}/groupons")
    public Object getShopGrouponActivity(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "3") Byte state,
            @RequestParam(required = false, defaultValue = "0") Long spuId,
            @RequestParam(required = false) String beginTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize
    ) {
        logger.debug("getShopGrouponActivity");
        LocalDateTime startTime1;
        LocalDateTime endTime1;
        if(!Objects.equals(null,beginTime)){
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            startTime1 = LocalDateTime.parse(beginTime,df);
        }
        else{
            startTime1 = null;
        }
        if(!Objects.equals(null,endTime)) {
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            endTime1 = LocalDateTime.parse(endTime,df);
        }
        else
        {
            endTime1 = null;
        }
        if(!Objects.equals(startTime1,null) && !Objects.equals(null,endTime1) && startTime1.isAfter(endTime1))
        {
            return Common.decorateReturnObject(new ReturnObject(ResponseCode.FIELD_NOTVALID));
        }
        ReturnObject<PageInfo<VoObject>> returnObject = grouponService.getShopGrouponActivity(id, state, spuId, startTime1,endTime1,page, pageSize);
        return Common.getPageRetObject(returnObject);
    }


    @ApiOperation(value = "管理员新建SPU团购活动")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "shopId", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "vo", required = true, dataType = "GrouponActivityVo", paramType = "body")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @PostMapping("shops/{shopId}/spus/{id}/groupons")
    public Object createGrouponAc(@PathVariable Long shopId, @PathVariable Long id, @RequestBody GrouponActivityVo vo, @LoginUser Long userId, @Depart Long departId) {
        logger.debug("createGrouponAc: id = " + id);
        GrouponActivity bo = new GrouponActivity(vo);
        if (!bo.isBiggerBegin()) {
            return Common.decorateReturnObject(new ReturnObject(ResponseCode.FIELD_NOTVALID));
        }
        if (!bo.beginAfterNow()) {
            return Common.decorateReturnObject(new ReturnObject(ResponseCode.FIELD_NOTVALID));
        }
        ReturnObject returnObject = grouponService.createGrouponAc(id, shopId, userId, bo);
        if(Objects.equals(returnObject.getCode(),ResponseCode.OK)) {
            return new ResponseEntity(
                    ResponseUtil.ok(returnObject.getData()),
                    HttpStatus.CREATED);
        }
        else
        {
            return Common.decorateReturnObject(returnObject);
        }
    }

    @ApiOperation(value = "管理员修改SPU团购活动")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "shopId", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "vo", required = true, dataType = "GrouponActivityVo", paramType = "body")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @PutMapping("shops/{shopId}/groupons/{id}")
    public Object changeGrouponAc(@PathVariable Long shopId, @PathVariable Long id, @RequestBody GrouponActivityVo vo, @LoginUser Long userId, @Depart Long departId) {
        logger.debug("changeGrouponAc: id = " + id);
        GrouponActivity bo = new GrouponActivity(vo);
        if (!bo.isBiggerBegin()) {
            return Common.decorateReturnObject(new ReturnObject(ResponseCode.FIELD_NOTVALID));
        }
        if (!bo.beginAfterNow()) {
            return Common.decorateReturnObject(new ReturnObject(ResponseCode.FIELD_NOTVALID));
        }
        ReturnObject returnObject = grouponService.changeGrouponAc(id, shopId, userId, bo);
        return Common.decorateReturnObject(returnObject);
    }

    @ApiOperation(value = "管理员逻辑删除SPU团购活动")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "Id", value = "活动id", required = true),
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "token", value = "authorization", required = true),
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "shopId", value = "店铺id", required = true),
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @DeleteMapping("shops/{shopId}/groupons/{id}")
    public Object cancelGrouponAc(
            @PathVariable Long id,
            @PathVariable Long shopId
    ) {
        logger.debug("cancelGrouponAc");
        ReturnObject returnObject = grouponService.cancelGrouponAc(id, shopId);
        return Common.decorateReturnObject(returnObject);
    }

    @ApiOperation(value = "管理员上线团购活动")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "Id", value = "活动id", required = true),
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "token", value = "authorization", required = true),
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "shopId", value = "店铺id", required = true),
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @PutMapping("shops/{shopId}/groupons/{id}/onshelves")
    public Object onlineGrouponAc(
            @PathVariable Long id,
            @PathVariable Long shopId
    ) {
        logger.debug("onlineGrouponAc");
        ReturnObject returnObject = grouponService.onlineGrouponAc(id, shopId);
        return Common.decorateReturnObject(returnObject);
    }

    @ApiOperation(value = "管理员下线团购活动")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "Id", value = "活动id", required = true),
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "token", value = "authorization", required = true),
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "shopId", value = "店铺id", required = true),
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @PutMapping("shops/{shopId}/groupons/{id}/offshelves")
    public Object offlineGrouponAc(
            @PathVariable Long id,
            @PathVariable Long shopId
    ) {
        logger.debug("offlineGrouponAc");
        ReturnObject returnObject = grouponService.offlineGrouponAc(id, shopId);
        return Common.decorateReturnObject(returnObject);
    }

}
