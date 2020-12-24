package cn.edu.xmu.goods.model.vo;

import cn.edu.xmu.goods.model.bo.GoodsSku;
import cn.edu.xmu.goods.model.bo.GoodsSpu;
import cn.edu.xmu.goods.model.po.*;
import cn.edu.xmu.ooad.goods.require.model.FreightModelSimple;
import lombok.Data;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Data
public class GoodsSpuCreateVo {
    private Long id;
    private String name;
    private GoodsCategoryVo category;
    private BrandVo brand;
    private ShopVo shop;
    private String goodsSn;
    private String detail;
    private String imageUrl;
    private boolean disable;
    private String spec;
    private List<GoodsSkuRetVo> skuList;
    private FreightModelSimple freightModelSimple;


    public GoodsSpuCreateVo (GoodsSpuPo po, BrandPo po1, GoodsCategoryPo po2, List<GoodsSkuRetVo> goodsSkuRetVos)
    {
        this.id = po.getId();
        this.category = new GoodsCategoryVo();
        this.category.setId(po.getCategoryId());
        this.category.setName(po.getName());
        this.brand = new BrandVo();
        this.brand.setId(po.getBrandId());
    }

    public GoodsSpuCreateVo (GoodsSpuPo po)
    {
        this.id = po.getId();
        this.category = new GoodsCategoryVo();
        this.category.setId(po.getCategoryId());
        this.category.setName(po.getName());
        this.brand = new BrandVo();
        this.brand.setId(po.getBrandId());
    }

}
