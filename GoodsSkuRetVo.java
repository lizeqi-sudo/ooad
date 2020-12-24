package cn.edu.xmu.goods.model.vo;

import cn.edu.xmu.goods.model.bo.GoodsSku;
import cn.edu.xmu.goods.model.bo.GoodsSpu;
import cn.edu.xmu.goods.model.po.GoodsSkuPo;
import cn.edu.xmu.goods.model.po.GoodsSpuPo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Data
@ApiModel(description = "规格视图对象")
public class GoodsSkuRetVo {

    @ApiModelProperty(value = "规格id")
    private Long id;
    @ApiModelProperty(value = "商品id")
    private Long goodsSpuId;
    @ApiModelProperty(value = "规格编号")
    private String skuSn;
    @ApiModelProperty(value = "规格名称")
    private String name;
    @ApiModelProperty(value = "规格原价")
    private Long originalPrice;
    @ApiModelProperty(value = "规格原价")
    private Long price;
    @ApiModelProperty(value = "规格配置参数")
    private String configuration;
    @ApiModelProperty(value = "规格重量")
    private Long weight;
    @ApiModelProperty(value = "规格图片链接")
    private String imageUrl;
    @ApiModelProperty(value = "规格库存")
    private Integer inventory;
    @ApiModelProperty(value = "规格详述")
    private String detail;
    @ApiModelProperty(value = "规格禁止访问")
    private boolean disable;
    @ApiModelProperty(value = "规格创建时间")
    private String gmtCreated;
    @ApiModelProperty(value = "规格修改时间")
    private String gmtModified;

    public GoodsSkuRetVo(GoodsSku bo)
    {
        this.id=bo.getId();
        this.goodsSpuId=bo.getGoodsSpuId();
        this.skuSn=bo.getSkuSn();
        this.name=bo.getName();
        this.originalPrice=bo.getOriginalPrice();
        this.price = bo.getOriginalPrice();
        this.configuration=bo.getConfiguration();
        this.weight=bo.getWeight();
        this.imageUrl=bo.getImageUrl();
        this.inventory=bo.getInventory();
        this.detail=bo.getDetail();
        if(Objects.equals((byte) 0,bo.getDisabled()))
        {
            this.disable = false;
        }
        else
        {
            this.disable =true;
        }
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.gmtCreated=df.format(bo.getGmtCreated());
        this.gmtModified=df.format(bo.getGmtModified());
    }

    public GoodsSkuRetVo(GoodsSkuPo bo)
    {
        this.id=bo.getId();
        this.goodsSpuId=bo.getGoodsSpuId();
        this.skuSn=bo.getSkuSn();
        this.name=bo.getName();
        this.originalPrice=bo.getOriginalPrice();
        this.configuration=bo.getConfiguration();
        this.weight=bo.getWeight();
        this.imageUrl=bo.getImageUrl();
        this.inventory=bo.getInventory();
        this.detail=bo.getDetail();
        if(Objects.equals((byte) 0,bo.getDisabled()))
        {
            this.disable = false;
        }
        else
        {
            this.disable =true;
        }
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.gmtCreated=df.format(bo.getGmtCreate());
        this.gmtModified=df.format(bo.getGmtModified());
    }
}
