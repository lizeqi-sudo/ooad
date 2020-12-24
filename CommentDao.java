package cn.edu.xmu.goods.dao;

import cn.edu.xmu.goods.mapper.*;
import cn.edu.xmu.goods.model.bo.*;
import cn.edu.xmu.goods.model.po.*;
import cn.edu.xmu.goods.model.vo.AuditVo;
import cn.edu.xmu.goods.model.vo.CommentCreateVo;
import cn.edu.xmu.goods.model.vo.CommentVo;
import cn.edu.xmu.goods.model.vo.CouponActivityRetVo;
import cn.edu.xmu.ooad.goods.require.ICommentService;
import cn.edu.xmu.ooad.goods.require.ICustomerService;
import cn.edu.xmu.ooad.goods.require.IFreightModelService;
import cn.edu.xmu.ooad.goods.require.model.CustomerSimple;
import cn.edu.xmu.ooad.goods.require.model.OrderSimple;
import cn.edu.xmu.ooad.model.VoObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.dubbo.config.annotation.DubboReference;
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
public class CommentDao {

    private static final Logger logger = LoggerFactory.getLogger(CommentDao.class);

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    CommentPoMapper commentPoMapper;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    GoodsSkuPoMapper goodsSkuPoMapper;

    @DubboReference(check = false)
    ICommentService iCommentService;

    @DubboReference(check = false)
    ICustomerService iCustomerService;

    public ReturnObject<PageInfo<VoObject>> getSkuComment(Long Id,  Integer pageNum, Integer pageSize) {
        GoodsSkuPo goodsSkuPo = goodsSkuPoMapper.selectByPrimaryKey(Id);
        if(goodsSkuPo == null)
        {
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        CommentPoExample example=new CommentPoExample();
        CommentPoExample.Criteria criteria=example.createCriteria();
        criteria.andGoodsSkuIdEqualTo(Id);
        criteria.andStateEqualTo((byte) 1);
        PageHelper.startPage(pageNum, pageSize);
        logger.debug("page = " + pageNum + "pageSize = " + pageSize);
        List<CommentPo> commentPo = null;
        try {
            commentPo = commentPoMapper.selectByExample(example);
            List<VoObject> ret = new ArrayList<>(commentPo.size());
            for (CommentPo po : commentPo) {
                CustomerSimple customerSimple = iCustomerService.getCustomer(po.getCustomerId());
                Comment comment = new Comment(po,customerSimple);
                ret.add(comment);
            }
            PageInfo<VoObject> commentPage = PageInfo.of(ret);
            return new ReturnObject<>(commentPage);

        }
        catch (DataAccessException e){
            logger.error("getSkuComment: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    @Transactional
    public ReturnObject auditComment(Long id, AuditVo vo) {
        try{
            CommentPo po=commentPoMapper.selectByPrimaryKey(id);
            if(po == null)
            {
                return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            if(!Objects.equals(po.getState(),(byte)0))
            {
                return new ReturnObject(ResponseCode.COMMENT_CANT_AUDIT);
            }
            if(vo.getConclusion())
            {
                po.setState((byte) 1);
                commentPoMapper.updateByPrimaryKey(po);
                return new ReturnObject();
            }
            else
            {
                po.setState((byte) 2);
                commentPoMapper.updateByPrimaryKey(po);
                return new ReturnObject();
            }
        }
        catch (DataAccessException e){
            logger.error("auditComment: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    public ReturnObject<PageInfo<VoObject>> getUserComment(Long userId,  Integer pageNum, Integer pageSize) {
        CommentPoExample example=new CommentPoExample();
        CommentPoExample.Criteria criteria=example.createCriteria();
        criteria.andCustomerIdEqualTo(userId);
        PageHelper.startPage(pageNum, pageSize);
        logger.debug("page = " + pageNum + "pageSize = " + pageSize);
        List<CommentPo> commentPo = null;
        try {
            commentPo = commentPoMapper.selectByExample(example);
            List<VoObject> ret = new ArrayList<>(commentPo.size());
            for (CommentPo po : commentPo) {
                CustomerSimple customerSimple = iCustomerService.getCustomer(po.getCustomerId());
                Comment comment = new Comment(po,customerSimple);
                ret.add(comment);
            }
            PageInfo<VoObject> commentPage = PageInfo.of(ret);
            return new ReturnObject<>(commentPage);

        }
        catch (DataAccessException e){
            logger.error("getUserComment: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    public ReturnObject<PageInfo<VoObject>> getAdComment(Byte Id,  Integer pageNum, Integer pageSize) {
        CommentPoExample example=new CommentPoExample();
        CommentPoExample.Criteria criteria=example.createCriteria();
        if(Objects.equals(Id,0) || Objects.equals(Id,1) || Objects.equals(Id,2))
        {
            criteria.andStateEqualTo(Id);
        }
        PageHelper.startPage(pageNum, pageSize);
        logger.debug("page = " + pageNum + "pageSize = " + pageSize);
        List<CommentPo> commentPo = null;
        try {
            commentPo = commentPoMapper.selectByExample(example);
            List<VoObject> ret = new ArrayList<>(commentPo.size());
            for (CommentPo po : commentPo) {
                CustomerSimple customerSimple = iCustomerService.getCustomer(po.getCustomerId());
                Comment comment = new Comment(po,customerSimple);
                ret.add(comment);
            }
            PageInfo<VoObject> commentPage = PageInfo.of(ret);
            return new ReturnObject<>(commentPage);

        }
        catch (DataAccessException e){
            logger.error("getAdComment: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    @Transactional
    public ReturnObject createComment(Long id,Long userId, CommentVo vo) {
        try{
            OrderSimple orderSimple = iCommentService.getComment(id);
            if(Objects.equals(orderSimple,null))
            {
                return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            if(!Objects.equals(orderSimple.getCustomer_id(),userId))
            {
                return new ReturnObject(ResponseCode.USER_NOTBUY);
            }
            CommentPoExample example = new CommentPoExample();
            CommentPoExample.Criteria criteria = example.createCriteria();
            criteria.andOrderitemIdEqualTo(id);
            if(!Objects.equals(0,commentPoMapper.selectByExample(example).size()))
            {
                return new ReturnObject(ResponseCode.FLASH_SALE_ITEM_AND_FLASH_SALE_CONFLICT);
            }
            CommentPo po=new CommentPo();
            po.setContent(vo.getContent());
            po.setGmtCreate(LocalDateTime.now());
            po.setGmtModified(LocalDateTime.now());
            po.setType(vo.getType());
            po.setCustomerId(userId);
            po.setOrderitemId(id);
            po.setState((byte) 0);
            po.setGoodsSkuId(orderSimple.getSku_id());
            commentPoMapper.insert(po);
            CommentCreateVo vo1 = new CommentCreateVo(po);
            return new ReturnObject<>(vo1);
        }
        catch (DataAccessException e){
            logger.error("createComment: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

}
