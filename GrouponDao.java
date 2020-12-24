package cn.edu.xmu.goods.dao;

import cn.edu.xmu.goods.mapper.*;
import cn.edu.xmu.goods.model.bo.*;
import cn.edu.xmu.goods.model.po.*;
import cn.edu.xmu.goods.model.vo.CouponActivityRetVo;
import cn.edu.xmu.goods.model.vo.GrouponActivityCreateVo;
import cn.edu.xmu.ooad.model.VoObject;
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
public class GrouponDao {

    private static final Logger logger = LoggerFactory.getLogger(GrouponDao.class);

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    GrouponActivityPoMapper grouponActivityPoMapper;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    GoodsSpuPoMapper goodsSpuPoMapper;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    ShopPoMapper shopPoMapper;

    public ReturnObject<PageInfo<VoObject>> getGrouponActivity(Long shopId, Long timeline, Long spuId, Integer pageNum, Integer pageSize) {
        GrouponActivityPoExample example = new GrouponActivityPoExample();
        GrouponActivityPoExample.Criteria criteria = example.createCriteria();
        if (!Objects.equals(shopId, 0)) {
            criteria.andShopIdEqualTo(shopId);
        }
        if (!Objects.equals(spuId, 0)) {
            criteria.andGoodsSpuIdEqualTo(spuId);
        }
        if (timeline == 0L) {
            criteria.andBeginTimeGreaterThan(LocalDateTime.now());
        } else if (timeline == 1L) {
            criteria.andBeginTimeBetween(LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        } else if (timeline == 2L) {
            criteria.andBeginTimeLessThan(LocalDateTime.now());
            criteria.andEndTimeGreaterThan(LocalDateTime.now());
        } else if (timeline == 3L) {
            criteria.andEndTimeLessThan(LocalDateTime.now());
        }
        criteria.andStateEqualTo((byte) 1);
        try {
            PageHelper.startPage(pageNum, pageSize);
            List<GrouponActivityPo> grouponAcPo = grouponActivityPoMapper.selectByExample(example);
            List<VoObject> ret = new ArrayList<>(grouponAcPo.size());
            for (GrouponActivityPo po : grouponAcPo) {
                GrouponActivity grouponActivity = new GrouponActivity(po);
                ret.add(grouponActivity);
            }
            PageInfo<VoObject> grouponActivityPage = PageInfo.of(ret);
            grouponActivityPage.setPageSize(pageSize);
            return new ReturnObject<>(grouponActivityPage);
        } catch (DataAccessException e) {
            logger.error("getGrouponActivity: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        } catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    public ReturnObject<PageInfo<VoObject>> getShopGrouponActivity(Long shopId, Byte state, Long spuId,LocalDateTime startTime, LocalDateTime endTime, Integer pageNum, Integer pageSize) {
        GrouponActivityPoExample example = new GrouponActivityPoExample();
        GrouponActivityPoExample.Criteria criteria = example.createCriteria();
        if(Objects.equals(shopPoMapper.selectByPrimaryKey(shopId),null))
        {
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        criteria.andShopIdEqualTo(shopId);
        if(!Objects.equals(spuId, 0L))
        {
            GoodsSpuPo po = goodsSpuPoMapper.selectByPrimaryKey(spuId);
            if(Objects.equals(null,po))
            {
                return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            if(!Objects.equals(po.getShopId(),shopId))
            {
                return new ReturnObject(ResponseCode.RESOURCE_ID_OUTSCOPE);
            }
            criteria.andGoodsSpuIdEqualTo(spuId);
        }
        if (Objects.equals(state, (byte)0) || Objects.equals(state, (byte)1) || Objects.equals(state, (byte)2)) {
            criteria.andStateEqualTo(state);
        }
        if(!Objects.equals(null,startTime) && !Objects.equals(null , endTime)) {
            criteria.andBeginTimeBetween(startTime, endTime);
            criteria.andEndTimeBetween(startTime, endTime);
        }
        if(Objects.equals(null,startTime) && !Objects.equals(null , endTime))
        {
            criteria.andBeginTimeLessThan(endTime);
            criteria.andEndTimeLessThan(endTime);
        }
        if(Objects.equals(null,endTime) && !Objects.equals(null,startTime))
        {
            criteria.andBeginTimeGreaterThan(startTime);
            criteria.andEndTimeGreaterThan(startTime);
        }
        PageHelper.startPage(pageNum, pageSize);
        logger.debug("page = " + pageNum + "pageSize = " + pageSize);
        List<GrouponActivityPo> grouponAcPo = null;
        try {
            grouponAcPo = grouponActivityPoMapper.selectByExample(example);
            List<VoObject> ret = new ArrayList<>(grouponAcPo.size());
            for (GrouponActivityPo po : grouponAcPo) {
                GrouponActivity grouponActivity = new GrouponActivity(po);
                ret.add(grouponActivity);
            }
            PageInfo<VoObject> grouponActivityPage = PageInfo.of(ret);
            grouponActivityPage.setPageSize(pageSize);
            return new ReturnObject<>(grouponActivityPage);

        } catch (DataAccessException e) {
            logger.error("getShopGrouponActivity: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        } catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    @Transactional
    public ReturnObject createGrouponAc(Long Id, Long shopId, Long userId, GrouponActivity bo) {
        GoodsSpuPo po = goodsSpuPoMapper.selectByPrimaryKey(Id);
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
        try {
            GrouponActivityPo grouponActivityPo = new GrouponActivityPo();
            grouponActivityPo.setBeginTime(bo.getBeginTime());
            grouponActivityPo.setEndTime(bo.getEndTime());
            grouponActivityPo.setGoodsSpuId(Id);
            grouponActivityPo.setName(bo.getName());
            grouponActivityPo.setShopId(shopId);
            grouponActivityPo.setGmtCreate(LocalDateTime.now());
            grouponActivityPo.setGmtModified(LocalDateTime.now());
            grouponActivityPo.setState((byte) 0);
            grouponActivityPo.setStrategy(bo.getStrategy());
            grouponActivityPoMapper.insert(grouponActivityPo);
            GrouponActivityCreateVo vo=new GrouponActivityCreateVo(grouponActivityPo,po,po1);
            return new ReturnObject<>(vo);
        } catch (DataAccessException e) {
            logger.error("createGrouponAc: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        } catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    @Transactional
    public ReturnObject changeGrouponAc(Long Id, Long shopId, Long userId, GrouponActivity bo) {
        GrouponActivityPo po = grouponActivityPoMapper.selectByPrimaryKey(Id);
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
        try {
            po.setBeginTime(bo.getBeginTime());
            po.setEndTime(bo.getEndTime());
            po.setStrategy(bo.getStrategy());
            po.setGmtModified(LocalDateTime.now());
            grouponActivityPoMapper.updateByPrimaryKey(po);
            return new ReturnObject();
        } catch (DataAccessException e) {
            logger.error("changeGrouponAc: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        } catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    @Transactional
    public ReturnObject cancelGrouponAc(Long Id, Long shopId) {
        GrouponActivityPo po = grouponActivityPoMapper.selectByPrimaryKey(Id);
        ShopPo po1=shopPoMapper.selectByPrimaryKey(shopId);
        if(Objects.equals(null,po))
        {
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        if(Objects.equals(null,po1))
        {
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        if(Objects.equals((byte) 1, po.getState()))
        {
            return new ReturnObject(ResponseCode.GROUPON_STATENOTALLOW);
        }
        if (!Objects.equals(po.getShopId(), shopId)) {
            return new ReturnObject(ResponseCode.RESOURCE_ID_OUTSCOPE);
        }
        try {
            po.setState((byte) 2);
            po.setGmtModified(LocalDateTime.now());
            grouponActivityPoMapper.updateByPrimaryKey(po);
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
    public ReturnObject onlineGrouponAc(Long Id, Long shopId) {
        GrouponActivityPo po = grouponActivityPoMapper.selectByPrimaryKey(Id);
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
        if(Objects.equals(po.getState(),(byte) 2))
        {
            return new ReturnObject(ResponseCode.ACTIVITY_CANT_ONLINE);
        }
        try {
            po.setState((byte) 1);
            po.setGmtModified(LocalDateTime.now());
            grouponActivityPoMapper.updateByPrimaryKey(po);
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
    public ReturnObject offlineGrouponAc(Long Id, Long shopId) {
        GrouponActivityPo po = grouponActivityPoMapper.selectByPrimaryKey(Id);
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
        if(Objects.equals(po.getState(),(byte) 2))
        {
            return new ReturnObject(ResponseCode.ACTIVITY_CANT_OFFLINE);
        }
        try {
            po.setState((byte) 0);
            po.setGmtModified(LocalDateTime.now());
            grouponActivityPoMapper.updateByPrimaryKey(po);
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
