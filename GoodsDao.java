package cn.edu.xmu.goods.dao;


import cn.edu.xmu.goods.mapper.*;
import cn.edu.xmu.goods.model.bo.*;
import cn.edu.xmu.goods.model.po.*;
import cn.edu.xmu.goods.model.vo.*;
import cn.edu.xmu.ooad.goods.require.IFreightModelService;
import cn.edu.xmu.ooad.goods.require.IShareService;
import cn.edu.xmu.ooad.goods.require.model.FreightModelSimple;
import cn.edu.xmu.ooad.model.VoObject;
import cn.edu.xmu.ooad.order.require.models.SkuInfo;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import cn.edu.xmu.ooad.util.ResponseCode;
import cn.edu.xmu.ooad.util.ReturnObject;
import cn.edu.xmu.ooad.util.ImgHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
public class GoodsDao {

    private static final Logger logger = LoggerFactory.getLogger(GoodsDao.class);

    private static final String REDIS_SKU_INFO_KEY = "skuInfo";

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    GoodsSpuPoMapper goodsSpuPoMapper;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    GoodsSkuPoMapper goodsSkuPoMapper;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    GoodsCategoryPoMapper goodsCategoryPoMapper;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    BrandPoMapper brandPoMapper;

    @DubboReference(check = false)
    IFreightModelService iFreightModelService;

    @DubboReference(check = false)
    IShareService iShareService;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;



    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    FloatPricePoMapper floatPricePoMapper;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    ShopPoMapper shopPoMapper;

    @Value("${privilegeservice.dav.username}")
    private String davUsername;

    @Value("${privilegeservice.dav.password}")
    private String davPassword;

    @Value("${privilegeservice.dav.baseUrl}")
    private String baseUrl;

    public ReturnObject<PageInfo<VoObject>> getSku(Long shopId, String skuSn,Long spuId,String spuSn, Integer pageNum, Integer pageSize) {
        GoodsSkuPoExample example = new GoodsSkuPoExample();
        GoodsSkuPoExample.Criteria criteria=example.createCriteria();
        if(!Objects.equals(skuSn,"0"))
        {
            criteria.andSkuSnEqualTo(skuSn);
        }
        if(!Objects.equals(spuId,0L))
        {
            criteria.andGoodsSpuIdEqualTo(spuId);
        }
        criteria.andIdIsNotNull();

        logger.debug("page = " + pageNum + "pageSize = " + pageSize);
        List<GoodsSkuPo> goodsSkuPos = null;
        try {
            Page<GoodsSkuPo> page = PageHelper.startPage(pageNum, pageSize);
            goodsSkuPos = goodsSkuPoMapper.selectByExample(example);
            List<VoObject> ret = new ArrayList<>();
            for (GoodsSkuPo po : goodsSkuPos) {
                GoodsSpuPo goodsSpuPo=goodsSpuPoMapper.selectByPrimaryKey(po.getGoodsSpuId());
                if((Objects.equals(shopId,0L) || Objects.equals(shopId,goodsSpuPo.getShopId()) )
                        && (Objects.equals("0",spuSn) || Objects.equals(spuSn,goodsSpuPo.getGoodsSn()))) {
                    GoodsSku goodsSku = new GoodsSku(po);
                    ret.add(goodsSku);
                }
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
            logger.error("getSku: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    public ReturnObject  getOneSku(Long Id)
    {
        try {
            GoodsSkuPo goodsSkupo=goodsSkuPoMapper.selectByPrimaryKey(Id);
            if(Objects.equals(null,goodsSkupo))
            {
                return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            if(!Objects.equals((byte) 4,goodsSkupo.getState()))
            {
                return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            GoodsSkuRetVo vo = new GoodsSkuRetVo(goodsSkupo);
            return new ReturnObject<>(vo);
        }
        catch (DataAccessException e){
            logger.error("getOneSku: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    @Transactional
    public ReturnObject addSkuToSpu(Long Id, Long shopId,Long userId,GoodsSku bo) {
        GoodsSpuPo po=goodsSpuPoMapper.selectByPrimaryKey(Id);
        if(po == null)
        {
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        if(!Objects.equals(po.getShopId(),shopId))
        {
            return new ReturnObject(ResponseCode.RESOURCE_ID_OUTSCOPE);
        }
        try{
            GoodsSkuPo goodsSkuPo=new GoodsSkuPo();
            goodsSkuPo.setName(bo.getName());
            goodsSkuPo.setConfiguration(bo.getConfiguration());
            goodsSkuPo.setGoodsSpuId(Id);
            goodsSkuPo.setDetail(bo.getDetail());
            goodsSkuPo.setDisabled((byte) 0);
            goodsSkuPo.setImageUrl(bo.getImageUrl());
            goodsSkuPo.setOriginalPrice(bo.getOriginalPrice());
            goodsSkuPo.setInventory(bo.getInventory());
            goodsSkuPo.setWeight(bo.getWeight());
            goodsSkuPo.setSkuSn(bo.getSkuSn());
            goodsSkuPo.setState((byte) 0);
            goodsSkuPo.setGmtCreate(LocalDateTime.now());
            goodsSkuPo.setGmtModified(LocalDateTime.now());
            goodsSkuPoMapper.insert(goodsSkuPo);
            GoodsSkuCreateVo vo=new GoodsSkuCreateVo(goodsSkuPo);
            return new ReturnObject<>(vo);
        }
        catch (DataAccessException e){
            logger.error("addSkuToSpu: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    @Transactional
    public ReturnObject deleteSku(Long Id, Long shopId) {
        GoodsSkuPo po=goodsSkuPoMapper.selectByPrimaryKey(Id);
        if(po == null || Objects.equals(po.getDisabled(),(byte) 1))
        {
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        GoodsSpuPo po1=goodsSpuPoMapper.selectByPrimaryKey(po.getGoodsSpuId());
        if(!Objects.equals(po1.getShopId(),shopId))
        {
            return new ReturnObject(ResponseCode.RESOURCE_ID_OUTSCOPE);
        }
        try{
            po.setDisabled((byte) 1);
            po.setState((byte) 6);
            goodsSkuPoMapper.updateByPrimaryKey(po);
            return new ReturnObject();
        }
        catch (DataAccessException e){
            logger.error("deleteSku: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    @Transactional
    public ReturnObject changeSku(Long Id, Long shopId,GoodsSku bo) {
        String skuInfoKey = REDIS_SKU_INFO_KEY + Id ;
        SkuInfo skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuInfoKey);
        GoodsSkuPo po=goodsSkuPoMapper.selectByPrimaryKey(Id);
        if(po == null)
        {
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        GoodsSpuPo po1=goodsSpuPoMapper.selectByPrimaryKey(po.getGoodsSpuId());
        if(!Objects.equals(po1.getShopId(),shopId))
        {
            return new ReturnObject(ResponseCode.RESOURCE_ID_OUTSCOPE);
        }
//        GoodsSkuPoExample example = new GoodsSkuPoExample();
//        GoodsSkuPoExample.Criteria criteria = example.createCriteria();
//        criteria.andSkuSnEqualTo()
//        List<GoodsSkuPo> goodsSkuPos = goodsSkuPoMapper.selectByExample(example);
//        for(GoodsSkuPo po4:goodsSkuPos)
//        {
//            if(!Objects.equals(po4.getId(),Id))
//            {
//                return new ReturnObject(ResponseCode.SKUSN_SAME);
//            }
//        }
        try{
            po.setState((byte) 0);
            po.setOriginalPrice(bo.getOriginalPrice());
            po.setConfiguration(bo.getConfiguration());
            po.setName(bo.getName());
            po.setInventory(bo.getInventory());
            po.setWeight(bo.getWeight());
            po.setDetail(bo.getDetail());
            goodsSkuPoMapper.updateByPrimaryKey(po);
            if(!Objects.equals(skuInfo,null))
            {
                redisTemplate.delete(skuInfoKey);
            }
            return new ReturnObject();
        }
        catch (DataAccessException e){
            logger.error("changeSku: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }


    public ReturnObject getCategory(Long Id) {
        try{
            GoodsCategoryPo po1 = goodsCategoryPoMapper.selectByPrimaryKey(Id);
            if(po1 == null && !Objects.equals(Id,0L))
            {
                return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            GoodsCategoryPoExample example=new GoodsCategoryPoExample();
            GoodsCategoryPoExample.Criteria criteria=example.createCriteria();
            criteria.andPidEqualTo(Id);
            List<GoodsCategoryPo> goodsCategoryPos=goodsCategoryPoMapper.selectByExample(example);
//            if(Objects.equals(goodsCategoryPos.size(),0))
//            {
//                return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
//            }
            List<GoodsCategoryRetVo> goodsCategoryRetVos = new ArrayList<>();
            for(GoodsCategoryPo po:goodsCategoryPos)
            {
                GoodsCategoryRetVo vo = new GoodsCategoryRetVo(po);
                goodsCategoryRetVos.add(vo);
            }
            return new ReturnObject<>(goodsCategoryRetVos);
        }
        catch (DataAccessException e){
            logger.error("getCategory: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    @Transactional
    public ReturnObject createCategory(Long Id, GoodsCategoryVo vo) {
        try{
            GoodsCategoryPo po1 = goodsCategoryPoMapper.selectByPrimaryKey(Id);
            if(po1 == null && !Objects.equals(Id,0L))
            {
                return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            GoodsCategoryPoExample example = new GoodsCategoryPoExample();
            GoodsCategoryPoExample.Criteria criteria = example.createCriteria();
            criteria.andNameEqualTo(vo.getName());
            if(!Objects.equals(goodsCategoryPoMapper.selectByExample(example).size(),0))
            {
                return new ReturnObject(ResponseCode.CATEGORY_NAME_SAME);
            }
            GoodsCategoryPo po=new GoodsCategoryPo();
            po.setName(vo.getName());
            po.setPid(Id);
            po.setGmtCreate(LocalDateTime.now());
            po.setGmtModified(LocalDateTime.now());
            goodsCategoryPoMapper.insert(po);
            CategoryCreateVo vo1=new CategoryCreateVo(po);
            return new ReturnObject<>(vo1);
        }
        catch (DataAccessException e){
            logger.error("createCategory: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    @Transactional
    public ReturnObject changeCategory(Long Id, GoodsCategoryVo vo) {
        try{
            GoodsCategoryPo po=goodsCategoryPoMapper.selectByPrimaryKey(Id);
            if(Objects.equals(po,null))
            {
                return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            GoodsCategoryPoExample example = new GoodsCategoryPoExample();
            GoodsCategoryPoExample.Criteria criteria = example.createCriteria();
            criteria.andNameEqualTo(vo.getName());
            if(!Objects.equals(0,goodsCategoryPoMapper.selectByExample(example).size()))
            {
                return new ReturnObject(ResponseCode.CATEGORY_NAME_SAME);
            }
            po.setName(vo.getName());
            goodsCategoryPoMapper.updateByPrimaryKey(po);
            return new ReturnObject();
        }
        catch (DataAccessException e){
            logger.error("changeCategory: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    @Transactional
    public ReturnObject deleteCategory(Long Id) {
        try{
            GoodsCategoryPo po=goodsCategoryPoMapper.selectByPrimaryKey(Id);
            if(Objects.equals(po,null))
            {
                return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            GoodsCategoryPoExample example1 = new GoodsCategoryPoExample();
            GoodsCategoryPoExample.Criteria criteria1= example1.createCriteria();
            criteria1.andPidEqualTo(Id);
            for(GoodsCategoryPo po1: goodsCategoryPoMapper.selectByExample(example1))
            {
                goodsCategoryPoMapper.deleteByPrimaryKey(po1.getId());
                GoodsSpuPoExample example = new GoodsSpuPoExample();
                GoodsSpuPoExample.Criteria criteria = example.createCriteria();
                criteria.andCategoryIdEqualTo(po1.getId());
                for(GoodsSpuPo po2:goodsSpuPoMapper.selectByExample(example))
                {
                    po2.setCategoryId(0L);
                    goodsSpuPoMapper.updateByPrimaryKey(po2);
                }
            }
            goodsCategoryPoMapper.deleteByPrimaryKey(Id);
            GoodsSpuPoExample example = new GoodsSpuPoExample();
            GoodsSpuPoExample.Criteria criteria = example.createCriteria();
            criteria.andCategoryIdEqualTo(Id);
            for(GoodsSpuPo po1:goodsSpuPoMapper.selectByExample(example))
            {
                po1.setCategoryId(0L);
                goodsSpuPoMapper.updateByPrimaryKey(po1);
            }
            return new ReturnObject();
        }
        catch (DataAccessException e){
            logger.error("deleteCategory: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    public ReturnObject<PageInfo<VoObject>> getBrands(Integer pageNum, Integer pageSize) {
        BrandPoExample example = new BrandPoExample();
        BrandPoExample.Criteria criteria=example.createCriteria();
        criteria.andNameIsNotNull();
        logger.debug("page = " + pageNum + "pageSize = " + pageSize);
        List<BrandPo> brandPos = null;
        try {
            Page<BrandPo> page = PageHelper.startPage(pageNum, pageSize);
            brandPos = brandPoMapper.selectByExample(example);
//            System.out.print(brandPos.size());
            List<VoObject> ret = new ArrayList<>();
            for (BrandPo po : brandPos)
            {
                Brand brand = new Brand(po);
                ret.add(brand);
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
            logger.error("getSku: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    public ReturnObject  getOneSpu(Long Id)
    {
        try {
            GoodsSpuPo goodsSpupo=goodsSpuPoMapper.selectByPrimaryKey(Id);
            if(Objects.equals(goodsSpupo,null) || Objects.equals(goodsSpupo.getDisabled(), (byte) 1))
            {
                return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
//            FreightModelSimple freightModelSimple = iFreightModelService.getFreightModel(goodsSpupo.getFreightId());
//            GoodsSpu bo=new GoodsSpu(goodsSpupo);
            GoodsSpuCreateVo vo = new GoodsSpuCreateVo(goodsSpupo);
            return new ReturnObject<>(vo);
        }
        catch (DataAccessException e){
            logger.error("getOneSpu: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }


    public ReturnObject  getOneShareSku(Long Id,Long sid,Long userId)
    {
        try {
            if(!iShareService.insertBeShared(Id,sid,userId))
            {
                return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            GoodsSkuPo goodsSkupo=goodsSkuPoMapper.selectByPrimaryKey(Id);
            GoodsSkuCreateVo vo=new GoodsSkuCreateVo(goodsSkupo);
            return new ReturnObject<>(vo);
        }
        catch (DataAccessException e){
            logger.error("getOneShareSpu: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    @Transactional
    public ReturnObject createSpu(Long Id, GoodsSpu bo) {
        try{
            GoodsSpuPo po=new GoodsSpuPo();
            po.setShopId(Id);
            po.setName(bo.getName());
            po.setDetail(bo.getDetail());
            po.setSpec(bo.getSpec());
            po.setGmtCreate(LocalDateTime.now());
            po.setGmtModified(LocalDateTime.now());
            po.setDisabled((byte) 0);
            po.setGoodsSn(bo.getName()+Id+bo.getDetail());
            goodsSpuPoMapper.insert(po);
            GoodsSpuCreateVo vo=new GoodsSpuCreateVo(po);
            return new ReturnObject<>(vo);
        }
        catch (DataAccessException e){
            logger.error("createSpu: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }


    @Transactional
    public ReturnObject changeSpu(Long shopId,Long Id, GoodsSpuVo vo) {
        GoodsSpuPo po=goodsSpuPoMapper.selectByPrimaryKey(Id);
        if(Objects.equals(null,po))
        {
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        if(!Objects.equals(po.getShopId(),shopId))
        {
            return new ReturnObject(ResponseCode.RESOURCE_ID_OUTSCOPE);
        }
        try{
            po.setName(vo.getName());
            po.setDetail(vo.getDetail());
            po.setSpec(vo.getSpec());
            po.setGmtModified(LocalDateTime.now());
            goodsSpuPoMapper.updateByPrimaryKey(po);
            return new ReturnObject();
        }
        catch (DataAccessException e){
            logger.error("changeSpu: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    @Transactional
    public ReturnObject deleteSpu(Long shopId,Long Id) {
        GoodsSpuPo po=goodsSpuPoMapper.selectByPrimaryKey(Id);
        if(Objects.equals(null,po))
        {
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        if(!Objects.equals(po.getShopId(),shopId))
        {
            return new ReturnObject(ResponseCode.RESOURCE_ID_OUTSCOPE);
        }
        try{
            po.setDisabled((byte) 1);
            po.setGmtModified(LocalDateTime.now());
            goodsSpuPoMapper.updateByPrimaryKey(po);
            return new ReturnObject();
        }
        catch (DataAccessException e){
            logger.error("deleteSpu: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }


    @Transactional
    public ReturnObject onlineSku(Long shopId,Long Id) {
        GoodsSkuPo po=goodsSkuPoMapper.selectByPrimaryKey(Id);
        if(Objects.equals(null,po))
        {
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        GoodsSpuPo po1=goodsSpuPoMapper.selectByPrimaryKey(po.getGoodsSpuId());
        if(!Objects.equals(po1.getShopId(),shopId))
        {
            return new ReturnObject(ResponseCode.RESOURCE_ID_OUTSCOPE);
        }
        if(!Objects.equals(po.getState(),(byte) 0))
        {
            return new ReturnObject(ResponseCode.CANT_ADD_SPU_TO_ACTIVITY);
        }
        try{
            po.setGmtModified(LocalDateTime.now());
            po.setState((byte)4);
            goodsSkuPoMapper.updateByPrimaryKey(po);
            return new ReturnObject();
        }
        catch (DataAccessException e){
            logger.error("onlineSpu: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }


    public ReturnObject offlineSku(Long shopId,Long Id) {
        GoodsSkuPo po=goodsSkuPoMapper.selectByPrimaryKey(Id);
        if(Objects.equals(null,po))
        {
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        GoodsSpuPo po1=goodsSpuPoMapper.selectByPrimaryKey(po.getGoodsSpuId());
        if(!Objects.equals(po1.getShopId(),shopId))
        {
            return new ReturnObject(ResponseCode.RESOURCE_ID_OUTSCOPE);
        }
        if(!Objects.equals(po.getState(),(byte) 4))
        {
            return new ReturnObject(ResponseCode.CANT_ADD_SPU_TO_ACTIVITY);
        }
        try{
            po.setGmtModified(LocalDateTime.now());
            po.setState((byte)0);
            goodsSkuPoMapper.updateByPrimaryKey(po);
            return new ReturnObject();
        }
        catch (DataAccessException e){
            logger.error("offlineSpu: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }


    @Transactional
    public ReturnObject createFloatPrice(Long shopId, Long id,FloatPrice bo,Long userId) {
        GoodsSkuPo po=goodsSkuPoMapper.selectByPrimaryKey(id);
        if(Objects.equals(null,po))
        {
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        GoodsSpuPo po5=goodsSpuPoMapper.selectByPrimaryKey(po.getGoodsSpuId());
        if(!Objects.equals(po5.getShopId(),shopId))
        {
            return new ReturnObject(ResponseCode.RESOURCE_ID_OUTSCOPE);
        }
        if(bo.getQuantity()>po.getInventory())
        {
            return new ReturnObject(ResponseCode.SKU_NOTENOUGH);
        }
        FloatPricePoExample example = new FloatPricePoExample();
        FloatPricePoExample.Criteria criteria = example.createCriteria();
        criteria.andBeginTimeBetween(bo.getBeginTime(),bo.getEndTime());
        criteria.andGoodsSkuIdEqualTo(id);
        List<FloatPricePo> floatPricePos = floatPricePoMapper.selectByExample(example);
        FloatPricePoExample example1 = new FloatPricePoExample();
        FloatPricePoExample.Criteria criteria1 = example1.createCriteria();
        criteria1.andEndTimeBetween(bo.getBeginTime(),bo.getEndTime());
        criteria1.andGoodsSkuIdEqualTo(id);
        List<FloatPricePo> floatPricePos1 = floatPricePoMapper.selectByExample(example1);
        boolean f = Objects.equals(floatPricePos.size(),0);
        boolean f1 = Objects.equals(floatPricePos1.size(),0);
        if(!(f&&f1))
        {
            return new ReturnObject(ResponseCode.SKUPRICE_CONFLICT);
        }
        try{
            FloatPricePo po1=new FloatPricePo();
            po1.setActivityPrice(bo.getActivityPrice());
            po1.setBeginTime(bo.getBeginTime());
            po1.setEndTime(bo.getEndTime());
            po1.setQuantity(bo.getQuantity());
            po1.setCreatedBy(userId);
            po1.setGmtCreate(LocalDateTime.now());
            po1.setGmtModified(LocalDateTime.now());
            po1.setValid((byte) 1);
            po1.setGoodsSkuId(id);
            floatPricePoMapper.insert(po1);
            FloatPriceCreateVo vo=new FloatPriceCreateVo(po1,userId);
            return new ReturnObject<>(vo);
        }
        catch (DataAccessException e){
            logger.error("createFloatPrice: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    @Transactional
    public ReturnObject invalidFloatPrice(Long shopId, Long id,Long userId) {
        FloatPricePo po=floatPricePoMapper.selectByPrimaryKey(id);
        if(po==null || Objects.equals(po.getValid(),(byte) 0) )
        {
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        GoodsSkuPo po1=goodsSkuPoMapper.selectByPrimaryKey(po.getGoodsSkuId());
        GoodsSpuPo po2=goodsSpuPoMapper.selectByPrimaryKey(po1.getGoodsSpuId());
        if(po1== null )
        {
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        if(!Objects.equals(po2.getShopId(),shopId))
        {
            return new ReturnObject(ResponseCode.RESOURCE_ID_OUTSCOPE);
        }
        try{
            po.setValid((byte) 0);
            po.setInvalidBy(userId);
            floatPricePoMapper.updateByPrimaryKey(po);
            return new ReturnObject();
        }
        catch (DataAccessException e){
            logger.error("invalidFloatPrice: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    @Transactional
    public ReturnObject createBrand(Long Id, Brand bo) {
        try{
            BrandPoExample example = new BrandPoExample();
            BrandPoExample.Criteria criteria = example.createCriteria();
            criteria.andNameEqualTo(bo.getName());
            if(!Objects.equals(brandPoMapper.selectByExample(example).size(),0))
            {
                return new ReturnObject(ResponseCode.BRAND_NAME_SAME);
            }
            BrandPo po = new BrandPo();
            po.setDetail(bo.getDetail());
            po.setImageUrl(bo.getImageUrl());
            po.setName(bo.getName());
            po.setGmtCreate(LocalDateTime.now());
            po.setGmtModified(LocalDateTime.now());
            brandPoMapper.insert(po);
            BrandCreateVo vo=new BrandCreateVo(po);
            return new ReturnObject<>(vo);
        }
        catch (DataAccessException e){
            logger.error("createBrand: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    @Transactional
    public ReturnObject changeBrand(Long Id, BrandVo vo) {
        try{
            BrandPo po = brandPoMapper.selectByPrimaryKey(Id);
            if(po == null)
            {
                return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            po.setGmtModified(LocalDateTime.now());
            BrandPoExample example = new BrandPoExample();
            BrandPoExample.Criteria criteria = example.createCriteria();
            criteria.andNameEqualTo(vo.getName());
            if(!Objects.equals(0,brandPoMapper.selectByExample(example).size()))
            {
                return new ReturnObject(ResponseCode.BRAND_NAME_SAME);
            }
            po.setName(vo.getName());
            po.setDetail(vo.getDetail());
            brandPoMapper.updateByPrimaryKey(po);
            return new ReturnObject();
        }
        catch (DataAccessException e){
            logger.error("changeBrand: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    @Transactional
    public ReturnObject deleteBrand(Long Id) {
        try{
            BrandPo po = brandPoMapper.selectByPrimaryKey(Id);
            if(po == null)
            {
                return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            brandPoMapper.deleteByPrimaryKey(Id);
            GoodsSpuPoExample example = new GoodsSpuPoExample();
            GoodsSpuPoExample.Criteria criteria = example.createCriteria();
            criteria.andBrandIdEqualTo(Id);
            for(GoodsSpuPo po1:goodsSpuPoMapper.selectByExample(example))
            {
                po1.setBrandId(0L);
                goodsSpuPoMapper.updateByPrimaryKey(po1);
            }
            return new ReturnObject();
        }
        catch (DataAccessException e){
            logger.error("deleteBrand: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    @Transactional
    public ReturnObject addSpuToCategory(Long spuId,Long id) {
        try{
            GoodsCategoryPo po=goodsCategoryPoMapper.selectByPrimaryKey(id);
            if(po == null)
            {
                return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            if(Objects.equals(po.getPid(),null))
            {
                return new ReturnObject(ResponseCode.SPU_CANT_ADD_TO_FIRST_CATEGORY);
            }
            GoodsSpuPo po1=goodsSpuPoMapper.selectByPrimaryKey(spuId);
            if(po1 == null)
            {
                return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            po1.setCategoryId(id);
            goodsSpuPoMapper.updateByPrimaryKey(po1);
            return new ReturnObject();
        }
        catch (DataAccessException e){
            logger.error("addSpuToCategory: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    @Transactional
    public ReturnObject removeSpuToCategory(Long spuId,Long id) {
        try{
            GoodsSpuPo po=goodsSpuPoMapper.selectByPrimaryKey(spuId);
            GoodsCategoryPo po1 = goodsCategoryPoMapper.selectByPrimaryKey(id);
            if(po1 == null || po == null)
            {
                return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            if(!Objects.equals(po.getCategoryId(),id))
            {
                return new ReturnObject(ResponseCode.SPU_NO_BELONG_TO_CATEGORY);
            }
            po.setCategoryId(null);
            goodsSpuPoMapper.updateByPrimaryKey(po);
            return new ReturnObject();
        }
        catch (DataAccessException e){
            logger.error("removeSpuToCategory: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    @Transactional
    public ReturnObject addSpuToBrand(Long spuId,Long id) {
        try{
            GoodsSpuPo po1=goodsSpuPoMapper.selectByPrimaryKey(spuId);
            BrandPo po2= brandPoMapper.selectByPrimaryKey(id);
            if(po1 == null || po2 == null)
            {
                return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            po1.setBrandId(id);
            goodsSpuPoMapper.updateByPrimaryKey(po1);
            return new ReturnObject();
        }
        catch (DataAccessException e){
            logger.error("addSpuToBrand: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    @Transactional
    public ReturnObject removeSpuToBrand(Long spuId,Long id) {
        try{
            GoodsSpuPo po1=goodsSpuPoMapper.selectByPrimaryKey(spuId);
            BrandPo po2= brandPoMapper.selectByPrimaryKey(id);
            if(po1 == null || po2 == null)
            {
                return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            if(!Objects.equals(po1.getBrandId(),id))
            {
                return new ReturnObject(ResponseCode.SPU_NO_BELONG_TO_BRAND);
            }
            po1.setBrandId(null);
            goodsSpuPoMapper.updateByPrimaryKey(po1);
            return new ReturnObject();
        }
        catch (DataAccessException e){
            logger.error("removeSpuToBrand: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    public ReturnObject uploadSkuImg(Long shopId, Long id, MultipartFile multipartFile) {
        ShopPo shopPo = shopPoMapper.selectByPrimaryKey(shopId);

        if (null == shopPo) {
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }

        ReturnObject returnObject = new ReturnObject();
        try {
            returnObject = ImgHelper.remoteSaveImg(multipartFile, 2, davUsername, davPassword, baseUrl);

            //文件上传错误
            if (returnObject.getCode() != ResponseCode.OK) {
                logger.debug(returnObject.getErrmsg());
                return returnObject;
            }

            GoodsSkuPo goodsSkuPo = goodsSkuPoMapper.selectByPrimaryKey(id);
            String oldImageUrl = goodsSkuPo.getImageUrl();
            goodsSkuPo.setImageUrl(returnObject.getData().toString());
            int count = goodsSkuPoMapper.updateByPrimaryKey(goodsSkuPo);

            //数据库更新失败，需删除新增的图片
            if (count == 0) {
                ImgHelper.deleteRemoteImg(returnObject.getData().toString(), davUsername, davPassword, baseUrl);
                return new ReturnObject(ResponseCode.IMAGE_FAIL);
            }

            //数据库更新成功需删除旧图片，未设置则不删除
            if (oldImageUrl != null) {
                ImgHelper.deleteRemoteImg(oldImageUrl, davUsername, davPassword, baseUrl);
            }
        } catch (IOException e) {
            logger.debug("uploadImg: I/O Error:" + baseUrl);
            return new ReturnObject(ResponseCode.FILE_NO_WRITE_PERMISSION);
        }
        return returnObject;
    }

    public ReturnObject uploadSpuImg(Long shopId, Long id, MultipartFile multipartFile) {
        ShopPo shopPo = shopPoMapper.selectByPrimaryKey(shopId);

        if (null == shopPo) {
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }

        ReturnObject returnObject = new ReturnObject();
        try {
            returnObject = ImgHelper.remoteSaveImg(multipartFile, 2, davUsername, davPassword, baseUrl);

            //文件上传错误
            if (returnObject.getCode() != ResponseCode.OK) {
                logger.debug(returnObject.getErrmsg());
                return returnObject;
            }

            GoodsSpuPo goodsSpuPo = goodsSpuPoMapper.selectByPrimaryKey(id);
            String oldImageUrl = goodsSpuPo.getImageUrl();
            goodsSpuPo.setImageUrl(returnObject.getData().toString());
            int count = goodsSpuPoMapper.updateByPrimaryKey(goodsSpuPo);

            //数据库更新失败，需删除新增的图片
            if (count == 0) {
                ImgHelper.deleteRemoteImg(returnObject.getData().toString(), davUsername, davPassword, baseUrl);
                return new ReturnObject(ResponseCode.IMAGE_FAIL);
            }

            //数据库更新成功需删除旧图片，未设置则不删除
            if (oldImageUrl != null) {
                ImgHelper.deleteRemoteImg(oldImageUrl, davUsername, davPassword, baseUrl);
            }
        } catch (IOException e) {
            logger.debug("uploadImg: I/O Error:" + baseUrl);
            return new ReturnObject(ResponseCode.FILE_NO_WRITE_PERMISSION);
        }
        return returnObject;
    }

    public ReturnObject uploadBrandImg(Long shopId, Long id, MultipartFile multipartFile) {
        ShopPo shopPo = shopPoMapper.selectByPrimaryKey(shopId);

        if (null == shopPo) {
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        ReturnObject returnObject = new ReturnObject();
        try {
            returnObject = ImgHelper.remoteSaveImg(multipartFile, 2, davUsername, davPassword, baseUrl);

            //文件上传错误
            if (returnObject.getCode() != ResponseCode.OK) {
                logger.debug(returnObject.getErrmsg());
                return returnObject;
            }

            BrandPo brandPo = brandPoMapper.selectByPrimaryKey(id);
            String oldImageUrl = brandPo.getImageUrl();
            brandPo.setImageUrl(returnObject.getData().toString());
            int count = brandPoMapper.updateByPrimaryKey(brandPo);

            //数据库更新失败，需删除新增的图片
            if (count == 0) {
                ImgHelper.deleteRemoteImg(returnObject.getData().toString(), davUsername, davPassword, baseUrl);
                return new ReturnObject(ResponseCode.IMAGE_FAIL);
            }

            //数据库更新成功需删除旧图片，未设置则不删除
            if (oldImageUrl != null) {
                ImgHelper.deleteRemoteImg(oldImageUrl, davUsername, davPassword, baseUrl);
            }
        } catch (IOException e) {
            logger.debug("uploadImg: I/O Error:" + baseUrl);
            return new ReturnObject(ResponseCode.FILE_NO_WRITE_PERMISSION);
        }
        return returnObject;
    }


}
