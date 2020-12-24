package cn.edu.xmu.flashsale.model.vo;

import cn.edu.xmu.flashsale.model.bo.GoodsSku;
import cn.edu.xmu.flashsale.model.bo.GoodsSpu;
import cn.edu.xmu.flashsale.model.po.GoodsSkuPo;
import cn.edu.xmu.flashsale.model.po.GoodsSpuPo;
import cn.edu.xmu.flashsale.model.po.GrouponActivityPo;
import cn.edu.xmu.flashsale.model.po.ShopPo;
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


    public GoodsSpuCreateVo(GoodsSpuPo po2)
    {
        this.id=po2.getId();
        this.name=po2.getName();
        this.category=new GoodsCategoryVo();
        this.category.setName("string");
        this.category.setId(0L);
        this.brand=new BrandVo();
        this.brand.setName("string");
        this.brand.setId(0L);
        this.shop=new ShopVo();
        this.shop.setName("string");
        this.shop.setId(0L);
        this.goodsSn="String";
        this.detail="String";
        this.imageUrl="String";
        this.spec=po2.getSpec();
        this.disable=false;
    }
    public GoodsSpuCreateVo(GoodsSpu bo2,FreightModelSimple freightModelSimple)
    {
        this.id=bo2.getId();
        this.name=bo2.getName();
        this.category=new GoodsCategoryVo();
        this.category.setName("string");
        this.category.setId(0L);
        this.brand=new BrandVo();
        this.brand.setName("string");
        this.brand.setId(0L);
        this.shop=new ShopVo();
        this.shop.setName("string");
        this.shop.setId(0L);
        this.goodsSn="String";
        this.detail="String";
        this.imageUrl="String";
        this.spec=bo2.getSpec();
        this.disable=false;
        this.freightModelSimple = freightModelSimple;
    }

}
