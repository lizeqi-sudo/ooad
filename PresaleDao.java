package cn.edu.xmu.goods.dao;

import cn.edu.xmu.goods.mapper.*;
import cn.edu.xmu.goods.model.bo.*;
import cn.edu.xmu.goods.model.po.*;
import cn.edu.xmu.goods.model.vo.CouponActivityRetVo;
import cn.edu.xmu.goods.model.vo.PresaleActivityRetVo;
import cn.edu.xmu.ooad.model.VoObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.dao.DataAccessException;
import cn.edu.xmu.ooad.util.ResponseCode;
import cn.edu.xmu.ooad.util.ReturnObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
public class PresaleDao {

    private static final Logger logger = LoggerFactory.getLogger(PresaleDao.class);

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    PresaleActivityPoMapper presaleActivityPoMapper;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    GoodsSpuPoMapper goodsSpuPoMapper;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    GoodsSkuPoMapper goodsSkuPoMapper;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    ShopPoMapper shopPoMapper;


    public ReturnObject<PageInfo<VoObject>> getPresaleActivity(Long shopId, Long timeline,Long skuId, Integer pageNum, Integer pageSize) {
        PresaleActivityPoExample example = new PresaleActivityPoExample();
        PresaleActivityPoExample.Criteria criteria = example.createCriteria();
        criteria.andStateEqualTo((byte) 1);
        if(!Objects.equals(shopId,0L))
        {
            criteria.andShopIdEqualTo(shopId);
        }
        if(!Objects.equals(skuId,0L))
        {
            criteria.andGoodsSkuIdEqualTo(skuId);
        }
        if(timeline==0L)
        {
            criteria.andBeginTimeGreaterThan(LocalDateTime.now());
        }
        else if(timeline==1L)
        {
            criteria.andBeginTimeBetween(LocalDateTime.now(),LocalDateTime.now().plusDays(1));
        }
        else if(timeline==2L)
        {
            criteria.andBeginTimeLessThan(LocalDateTime.now());
            criteria.andEndTimeGreaterThan(LocalDateTime.now());
        }
        else if(timeline==3L)
        {
            criteria.andEndTimeLessThan(LocalDateTime.now());
        }
        logger.debug("page = " + pageNum + "pageSize = " + pageSize);
        List<PresaleActivityPo> presaleAcPo = null;
        try {
            Page<PresaleActivityPo> page = PageHelper.startPage(pageNum, pageSize);
            presaleAcPo = presaleActivityPoMapper.selectByExample(example);
            List<VoObject> ret = new ArrayList<>();
            for (PresaleActivityPo po : presaleAcPo) {
                PresaleActivity presaleActivity = new PresaleActivity(po);
                ret.add(presaleActivity);
            }
            PageInfo<VoObject> goodsSkuPage = PageInfo.of(ret);
            goodsSkuPage.setTotal(page.getTotal());
            goodsSkuPage.setPageSize(pageSize);
            double a = (double) page.getTotal();
            double b = pageSize.doubleValue();
            double c = a/b;
            goodsSkuPage.setPages((int) Math.ceil(c));
            goodsSkuPage.setPageNum(pageNum);
            return new ReturnObject<>(goodsSkuPage);
        }
        catch (DataAccessException e){
            logger.error("getPresaleActivity: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    public ReturnObject getOnePresaleAc(Long Id, Long shopId,Byte state) {
        try{
            PresaleActivityPoExample example=new PresaleActivityPoExample();
            PresaleActivityPoExample.Criteria criteria=example.createCriteria();
            if(!Objects.equals(Id,0L)) {
                criteria.andGoodsSkuIdEqualTo(Id);
            }
            if(!Objects.equals(state,(byte)3))
            {
                criteria.andStateEqualTo(state);
            }
            List<PresaleActivityPo> presaleActivityPos=presaleActivityPoMapper.selectByExample(example);
            return new ReturnObject<>(presaleActivityPos);
        }
        catch (DataAccessException e){
            logger.error("getOnePresaleAc: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    @Transactional
    public ReturnObject createPresaleAc(Long Id, Long shopId,Long userId,PresaleActivity bo) {
        GoodsSkuPo po=goodsSkuPoMapper.selectByPrimaryKey(Id);
        if(po == null)
        {
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        if(!Objects.equals(goodsSpuPoMapper.selectByPrimaryKey(po.getGoodsSpuId()).getShopId(),shopId))
        {
            return new ReturnObject(ResponseCode.RESOURCE_ID_OUTSCOPE);
        }
        PresaleActivityPoExample example = new PresaleActivityPoExample();
        PresaleActivityPoExample.Criteria criteria = example.createCriteria();
        criteria.andBeginTimeBetween(bo.getBeginTime(),bo.getEndTime());
        criteria.andGoodsSkuIdEqualTo(Id);
        List<PresaleActivityPo> presaleActivityPos = presaleActivityPoMapper.selectByExample(example);
        PresaleActivityPoExample example1 = new PresaleActivityPoExample();
        PresaleActivityPoExample.Criteria criteria1 = example1.createCriteria();
        criteria1.andEndTimeBetween(bo.getBeginTime(),bo.getEndTime());
        criteria1.andGoodsSkuIdEqualTo(Id);
        List<PresaleActivityPo> presaleActivityPos1 = presaleActivityPoMapper.selectByExample(example1);
        if(!Objects.equals(presaleActivityPos,null) || !Objects.equals(presaleActivityPos1,null))
        {
            return new ReturnObject(ResponseCode.PRESALE_STATENOTALLOW);
        }
        if(po.getInventory() < bo.getQuantity())
        {
            return new ReturnObject(ResponseCode.NO_SUFFICIENT_GOODS);
        }
        try{
            PresaleActivityPo presaleActivityPo=new PresaleActivityPo();
            presaleActivityPo.setAdvancePayPrice(bo.getAdvancePayPrice());
            presaleActivityPo.setBeginTime(bo.getBeginTime());
            presaleActivityPo.setEndTime(bo.getEndTime());
            presaleActivityPo.setPayTime(bo.getPayTime());
            presaleActivityPo.setGoodsSkuId(Id);
            presaleActivityPo.setQuantity(bo.getQuantity());
            presaleActivityPo.setName(bo.getName());
            presaleActivityPo.setRestPayPrice(bo.getRestPayPrice());
            presaleActivityPo.setShopId(shopId);
            presaleActivityPo.setGmtCreate(LocalDateTime.now());
            presaleActivityPo.setGmtModified(LocalDateTime.now());
            presaleActivityPo.setState((byte) 1);
            presaleActivityPoMapper.insert(presaleActivityPo);
            PresaleActivityRetVo vo=new PresaleActivityRetVo(presaleActivityPo);
            return new ReturnObject<>(vo);
        }
        catch (DataAccessException e){
            logger.error("createPresaleAc: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    @Transactional
    public ReturnObject changePresaleAc(Long Id, Long shopId,Long userId,PresaleActivity bo) {
        PresaleActivityPo po=presaleActivityPoMapper.selectByPrimaryKey(Id);
        if(!Objects.equals(po.getState(),(byte) 0))
        {
            return new ReturnObject(ResponseCode.PRESALE_STATENOTALLOW);
        }
        if(!Objects.equals(po.getShopId(),shopId))
        {
            return new ReturnObject(ResponseCode.RESOURCE_ID_OUTSCOPE);
        }
        if(!Objects.equals(bo.getQuantity(),null)) {
            Integer f = goodsSkuPoMapper.selectByPrimaryKey(po.getGoodsSkuId()).getInventory();
            if (bo.getQuantity() > f) {
                return new ReturnObject(ResponseCode.NO_SUFFICIENT_GOODS);
            }
        }
        try{
            po.setAdvancePayPrice(bo.getAdvancePayPrice());
            po.setBeginTime(bo.getBeginTime());
            po.setEndTime(bo.getEndTime());
            po.setPayTime(bo.getPayTime());
            po.setQuantity(bo.getQuantity());
            po.setName(bo.getName());
            po.setRestPayPrice(bo.getRestPayPrice());
            po.setGmtModified(LocalDateTime.now());
            presaleActivityPoMapper.updateByPrimaryKey(po);
            return new ReturnObject();
        }
        catch (DataAccessException e){
            logger.error("changePresaleAc: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    @Transactional
    public ReturnObject cancelPresaleAc(Long Id, Long shopId) {
        PresaleActivityPo po=presaleActivityPoMapper.selectByPrimaryKey(Id);
        if(!Objects.equals(po.getShopId(),shopId))
        {
            return new ReturnObject(ResponseCode.RESOURCE_ID_OUTSCOPE);
        }
        if(!Objects.equals(po.getState(),(byte) 0))
        {
            return new ReturnObject(ResponseCode.PRESALE_STATENOTALLOW);
        }
        try{
            po.setState((byte) 2);
            po.setGmtModified(LocalDateTime.now());
            presaleActivityPoMapper.updateByPrimaryKey(po);
            return new ReturnObject();
        }
        catch (DataAccessException e){
            logger.error("changePresaleAc: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    @Transactional
    public ReturnObject onlinePresaleAc(Long Id, Long shopId) {
        PresaleActivityPo po = presaleActivityPoMapper.selectByPrimaryKey(Id);
        ShopPo po1=shopPoMapper.selectByPrimaryKey(shopId);
        if(Objects.equals(null,po))
        {
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        if(Objects.equals(null,po1))
        {
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        if (!Objects.equals(po.getShopId(), shopId)) {
            return new ReturnObject(ResponseCode.RESOURCE_ID_OUTSCOPE);
        }
        if(!Objects.equals(po.getState(),(byte) 0))
        {
            return new ReturnObject(ResponseCode.PRESALE_STATENOTALLOW);
        }
        try {
            po.setState((byte) 1);
            po.setGmtModified(LocalDateTime.now());
            presaleActivityPoMapper.updateByPrimaryKey(po);
            return new ReturnObject();
        } catch (DataAccessException e) {
            logger.error("cancelGrouponAc: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        } catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    @Transactional
    public ReturnObject offlinePresaleAc(Long Id, Long shopId) {
        PresaleActivityPo po = presaleActivityPoMapper.selectByPrimaryKey(Id);
        ShopPo po1=shopPoMapper.selectByPrimaryKey(shopId);
        if(Objects.equals(null,po))
        {
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        if(Objects.equals(null,po1))
        {
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        if (!Objects.equals(po.getShopId(), shopId)) {
            return new ReturnObject(ResponseCode.RESOURCE_ID_OUTSCOPE);
        }
        if(!Objects.equals(po.getState(),(byte) 1))
        {
            return new ReturnObject(ResponseCode.PRESALE_STATENOTALLOW);
        }
        try {
            po.setState((byte) 0);
            po.setGmtModified(LocalDateTime.now());
            presaleActivityPoMapper.updateByPrimaryKey(po);
            return new ReturnObject();
        } catch (DataAccessException e) {
            logger.error("cancelGrouponAc: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        } catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }
}
