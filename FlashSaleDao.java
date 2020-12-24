package cn.edu.xmu.flashsale.dao;

import cn.edu.xmu.flashsale.mapper.FlashSaleItemPoMapper;
import cn.edu.xmu.flashsale.mapper.FlashSalePoMapper;
import cn.edu.xmu.flashsale.mapper.GoodsSkuPoMapper;
import cn.edu.xmu.flashsale.model.bo.FlashSale;
import cn.edu.xmu.flashsale.model.bo.FlashSaleItem;
import cn.edu.xmu.flashsale.model.po.*;
import cn.edu.xmu.flashsale.model.vo.FlashSaleItemRetVo;
import cn.edu.xmu.flashsale.model.vo.FlashSaleItemVo;
import cn.edu.xmu.flashsale.model.vo.FlashSaleRetVo;
import cn.edu.xmu.ooad.goods.require.IFreightModelService;
import cn.edu.xmu.ooad.goods.require.ITimeSegmentService;
import cn.edu.xmu.ooad.model.VoObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.dao.DataAccessException;
import cn.edu.xmu.ooad.util.ResponseCode;
import cn.edu.xmu.ooad.util.ReturnObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
public class FlashSaleDao {

    private static final Logger logger = LoggerFactory.getLogger(FlashSaleDao.class);

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    FlashSalePoMapper flashSalePoMapper;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    FlashSaleItemPoMapper flashSaleItemPoMapper;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    GoodsSkuPoMapper goodsSkuPoMapper;

    private static final String REDIS_FLASH_SALE_KEY = "flashsale";

    @Autowired
    private ReactiveRedisTemplate reactiveRedisTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @DubboReference(check = false)
    ITimeSegmentService iTimeSegmentService;


    @Transactional
    public ReturnObject createFlashSale(Long id, FlashSale bo) {
        try{
            if(!iTimeSegmentService.isTimeSegment(id))
            {
                return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            FlashSalePoExample example = new FlashSalePoExample();
            FlashSalePoExample.Criteria criteria=example.createCriteria();
            criteria.andTimeSegIdEqualTo(id);
            List<FlashSalePo> flashSalePos=flashSalePoMapper.selectByExample(example);
            for(FlashSalePo po1:flashSalePos)
            {
                if(!Objects.equals(po1.getState(),(byte) 2) && Objects.equals(po1.getFlashDate().toLocalDate(),bo.getFlashDate().toLocalDate()))
                {
                    return new ReturnObject(ResponseCode.TIMESEG_CONFLICT);
                }
            }
            FlashSalePo po=new FlashSalePo();
            po.setFlashDate(bo.getFlashDate());
            po.setGmtCreate(LocalDateTime.now());
            po.setGmtModified(LocalDateTime.now());
            po.setTimeSegId(id);
            po.setState((byte) 0);
            flashSalePoMapper.insert(po);
            FlashSaleRetVo vo = new FlashSaleRetVo(po);
            return new ReturnObject<>(vo);
        }
        catch (DataAccessException e){
            logger.error("createFlashSale: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }


    @Transactional
    public ReturnObject deleteFlashSale(Long id) {
        try{
            FlashSalePo po=flashSalePoMapper.selectByPrimaryKey(id);
            if(po == null || Objects.equals(po.getState(),(byte) 2))
            {
                return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            if(!Objects.equals((byte) 0,po.getState()))
            {
                return new ReturnObject(ResponseCode.NO_SUFFICIENT_GOODS);
            }
            po.setState((byte) 2);
            flashSalePoMapper.updateByPrimaryKey(po);
            return new ReturnObject();
        }
        catch (DataAccessException e){
            logger.error("createFlashSale: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    @Transactional
    public ReturnObject changeFlashSale(Long id, FlashSale bo) {
        try{
            FlashSalePo po=flashSalePoMapper.selectByPrimaryKey(id);
            if(po == null)
            {
                return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            if(!Objects.equals(po.getState(),(byte) 0))
            {
                return new ReturnObject(ResponseCode.NO_SUFFICIENT_GOODS);
            }
            if(po.getFlashDate().isBefore(LocalDateTime.now()))
            {
                return new ReturnObject(ResponseCode.NO_SUFFICIENT_GOODS);
            }
            FlashSalePoExample example=new FlashSalePoExample();
            FlashSalePoExample.Criteria criteria=example.createCriteria();
            criteria.andTimeSegIdEqualTo(po.getTimeSegId());
            List<FlashSalePo> flashSalePos=flashSalePoMapper.selectByExample(example);
            for(FlashSalePo po1:flashSalePos)
            {
                if(Objects.equals(po1.getFlashDate().toLocalDate(),bo.getFlashDate().toLocalDate()))
                {
                    return new ReturnObject(ResponseCode.FLASH_SALE_DATE_CONFLICT);
                }
            }
            po.setGmtModified(LocalDateTime.now());
            po.setFlashDate(bo.getFlashDate());
            flashSalePoMapper.updateByPrimaryKey(po);
            return new ReturnObject();
        }
        catch (DataAccessException e){
            logger.error("changeFlashSale: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    @Transactional
    public ReturnObject onlineFlashSale(Long id) {
        try{
            FlashSalePo po=flashSalePoMapper.selectByPrimaryKey(id);
            if(po == null || Objects.equals(po.getState(),(byte) 2))
            {
                return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            po.setGmtModified(LocalDateTime.now());
            po.setState((byte) 1);
            flashSalePoMapper.updateByPrimaryKey(po);
            return new ReturnObject();
        }
        catch (DataAccessException e){
            logger.error("changeFlashSale: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    @Transactional
    public ReturnObject offlineFlashSale(Long id) {
        try{
            FlashSalePo po=flashSalePoMapper.selectByPrimaryKey(id);
            if(po == null || Objects.equals(po.getState(),(byte) 2))
            {
                return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            po.setGmtModified(LocalDateTime.now());
            po.setState((byte) 0);
            flashSalePoMapper.updateByPrimaryKey(po);
            return new ReturnObject();
        }
        catch (DataAccessException e){
            logger.error("changeFlashSale: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    @Transactional
    public ReturnObject createFlashSaleItem(Long id, FlashSaleItemVo vo) {
        try{
            FlashSaleItemPo po=new FlashSaleItemPo();
            po.setGmtCreate(LocalDateTime.now());
            po.setGmtModified(LocalDateTime.now());
            po.setGoodsSkuId(vo.getSkuId());
            po.setPrice(vo.getPrice());
            po.setQuantity(vo.getQuantity());
            po.setSaleId(id);
            flashSaleItemPoMapper.insert(po);
            FlashSaleItemRetVo vo1=new FlashSaleItemRetVo(po);
            return new ReturnObject<>(vo1);
        }
        catch (DataAccessException e){
            logger.error("createFlashSaleItem: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    @Transactional
    public ReturnObject deleteFlashSaleItem(Long fid,Long id) {
        try{
            FlashSaleItemPo po=flashSaleItemPoMapper.selectByPrimaryKey(id);
            if(!Objects.equals(fid,po.getSaleId()))
            {
                return new ReturnObject(ResponseCode.FLASH_SALE_ITEM_AND_FLASH_SALE_CONFLICT);
            }
            flashSaleItemPoMapper.deleteByPrimaryKey(id);
            return new ReturnObject();
        }
        catch (DataAccessException e){
            logger.error("deleteFlashSaleItem: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }



    public Flux<FlashSaleItemRetVo> getFlashDetail(Long id) {
        String activityKey = REDIS_FLASH_SALE_KEY + id;
        if (!redisTemplate.hasKey(activityKey)) {
            List<FlashSaleItemRetVo> flashSaleItems = new ArrayList<>();
            FlashSalePoExample example = new FlashSalePoExample();
            FlashSalePoExample.Criteria criteria = example.createCriteria();
            criteria.andTimeSegIdEqualTo(id);
            List<FlashSalePo> flashSalePos = flashSalePoMapper.selectByExample(example);
            for(FlashSalePo po1:flashSalePos)
            {
                if(!Objects.equals(po1.getFlashDate().toLocalDate(),LocalDate.now()))
                {
                    flashSalePos.remove(po1);
                }
            }
            if (!CollectionUtils.isEmpty(flashSalePos)) {
                for (FlashSalePo po : flashSalePos) {
                    FlashSaleItemPoExample itemPoExample = new FlashSaleItemPoExample();
                    FlashSaleItemPoExample.Criteria itemPoCriteria = itemPoExample.createCriteria();
                    itemPoCriteria.andSaleIdEqualTo(po.getId());
                    List<FlashSaleItemPo> flashSaleItemPos = flashSaleItemPoMapper.selectByExample(itemPoExample);
                    if (!CollectionUtils.isEmpty(flashSaleItemPos)) {
                        for (FlashSaleItemPo itemPo : flashSaleItemPos) {
                            GoodsSkuPo goodsSkuPo = goodsSkuPoMapper.selectByPrimaryKey(itemPo.getGoodsSkuId());
                            FlashSaleItem flashSaleItem = new FlashSaleItem(itemPo, goodsSkuPo);
                            flashSaleItems.add((FlashSaleItemRetVo) flashSaleItem.createVo());
                        }
                    }
                }
                redisTemplate.opsForList().leftPushAll(activityKey, flashSaleItems);
            }
        }
        Mono<Long> size = reactiveRedisTemplate.opsForList().size(activityKey);
        return reactiveRedisTemplate.opsForList().range(activityKey,0,size.block()-1);
    }

    public Flux<FlashSaleItemRetVo> getCurrentFlashDetail() {
//        Long id=iTimeSegmentService.getTimeSegmentByNow(LocalDateTime.now());
        Long id = 1L;
        String activityKey = REDIS_FLASH_SALE_KEY + id;
        if (!redisTemplate.hasKey(activityKey)) {
            List<FlashSaleItemRetVo> flashSaleItems = new ArrayList<>();
            FlashSalePoExample example = new FlashSalePoExample();
            FlashSalePoExample.Criteria criteria = example.createCriteria();
            criteria.andTimeSegIdEqualTo(id);
            List<FlashSalePo> flashSalePos = flashSalePoMapper.selectByExample(example);
            for(FlashSalePo po1:flashSalePos)
            {
                if(!Objects.equals(po1.getFlashDate().toLocalDate(),LocalDate.now()))
                {
                    flashSalePos.remove(po1);
                }
            }
            if (!CollectionUtils.isEmpty(flashSalePos)) {
                for (FlashSalePo po : flashSalePos) {
                    FlashSaleItemPoExample itemPoExample = new FlashSaleItemPoExample();
                    FlashSaleItemPoExample.Criteria itemPoCriteria = itemPoExample.createCriteria();
                    itemPoCriteria.andSaleIdEqualTo(po.getId());
                    List<FlashSaleItemPo> flashSaleItemPos = flashSaleItemPoMapper.selectByExample(itemPoExample);
                    if (!CollectionUtils.isEmpty(flashSaleItemPos)) {
                        for (FlashSaleItemPo itemPo : flashSaleItemPos) {

                            GoodsSkuPo goodsSkuPo = goodsSkuPoMapper.selectByPrimaryKey(itemPo.getGoodsSkuId());
                            FlashSaleItem flashSaleItem = new FlashSaleItem(itemPo, goodsSkuPo);
                            flashSaleItems.add((FlashSaleItemRetVo) flashSaleItem.createVo());
                        }
                    }
                }
                redisTemplate.opsForList().leftPushAll(activityKey, flashSaleItems);
            }
        }
        Mono<Long> size = reactiveRedisTemplate.opsForList().size(activityKey);
        return reactiveRedisTemplate.opsForList().range(activityKey,0,size.block()-1);
    }

    @Transactional
    public ReturnObject removeFlashSale(Long id) {
        try{
            flashSaleItemPoMapper.deleteByPrimaryKey(id);
            return new ReturnObject();
        }
        catch (DataAccessException e){
            logger.error("deleteFlashSaleItem: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }


}
