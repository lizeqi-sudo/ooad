package cn.edu.xmu.goods.dao;


import cn.edu.xmu.goods.mapper.*;
import cn.edu.xmu.goods.model.bo.*;
import cn.edu.xmu.goods.model.po.*;
import cn.edu.xmu.goods.model.vo.AuditVo;
import cn.edu.xmu.goods.model.vo.CouponActivityRetVo;
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
public class ShopDao {

    private static final Logger logger = LoggerFactory.getLogger(ShopDao.class);

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    ShopPoMapper shopPoMapper;

    @Transactional
    public ReturnObject createShop(Shop bo) {
        try{
            ShopPo po = new ShopPo();
            po.setName(bo.getName());
            po.setGmtCreate(LocalDateTime.now());
            po.setGmtModified(LocalDateTime.now());
            po.setState((byte) 0);
            shopPoMapper.insert(po);
            return new ReturnObject<>(po);
        }
        catch (DataAccessException e){
            logger.error("createShop: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    @Transactional
    public ReturnObject changeShop(Shop bo,Long id) {
        try{
            ShopPo po=shopPoMapper.selectByPrimaryKey(id);
            if(Objects.equals(null, po))
            {
                return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            if(Objects.equals((byte) 3,po.getState()) || Objects.equals((byte) 4,po.getState()))
            {
                return new ReturnObject(ResponseCode.SHOP_STATENOTALLOW);
            }
            po.setGmtModified(LocalDateTime.now());
            po.setName(bo.getName());
            shopPoMapper.updateByPrimaryKey(po);
            return new ReturnObject();
        }
        catch (DataAccessException e){
            logger.error("changeShop: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    @Transactional
    public ReturnObject closeShop(Long id) {
        try{
            ShopPo po=shopPoMapper.selectByPrimaryKey(id);
            if(Objects.equals(null, po))
            {
                return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            po.setState((byte) 3);
            shopPoMapper.updateByPrimaryKey(po);
            return new ReturnObject();
        }
        catch (DataAccessException e){
            logger.error("closeShop: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    @Transactional
    public ReturnObject auditShop(Long id, AuditVo vo) {
        try{
            ShopPo po=shopPoMapper.selectByPrimaryKey(id);
            if(Objects.equals(null, po))
            {
                return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            if(!Objects.equals(po.getState(),(byte)0))
            {
                return new ReturnObject(ResponseCode.SHOP_STATENOTALLOW);
            }
            if(vo.getConclusion())
            {
                po.setState((byte) 1);
                shopPoMapper.updateByPrimaryKey(po);
                return new ReturnObject();
            }
            else
            {
                po.setState((byte) 4);
                shopPoMapper.updateByPrimaryKey(po);
                return new ReturnObject();
            }
        }
        catch (DataAccessException e){
            logger.error("auditShop: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    @Transactional
    public ReturnObject onlineShop(Long id) {
        try{
            ShopPo po=shopPoMapper.selectByPrimaryKey(id);
            if(Objects.equals(null, po))
            {
                return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            if(!Objects.equals(po.getState(),(byte)1))
            {
                return new ReturnObject(ResponseCode.SHOP_STATENOTALLOW);
            }
            po.setState((byte) 2);
            shopPoMapper.updateByPrimaryKey(po);
            return new ReturnObject();
        }
        catch (DataAccessException e){
            logger.error("onlineShop: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    @Transactional
    public ReturnObject offlineShop(Long id) {
        try{
            ShopPo po=shopPoMapper.selectByPrimaryKey(id);
            if(Objects.equals(null, po))
            {
                return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            if(!Objects.equals(po.getState(),(byte)2))
            {
                return new ReturnObject(ResponseCode.SHOP_STATENOTALLOW);
            }
            po.setState((byte) 1);
            shopPoMapper.updateByPrimaryKey(po);
            return new ReturnObject();
        }
        catch (DataAccessException e){
            logger.error("offlineShop: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }
}
