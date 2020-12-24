package cn.edu.xmu.flashsale.model.vo;

import cn.edu.xmu.flashsale.model.po.GoodsSkuPo;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GoodsSkuCreateVo {
    private Long id;
    private Long goodsSpuId;
    private String skuSn;
    private String name;
    private Long originalPrice;
    private String configuration;
    private Long weight;
    private String imageUrl;
    private Integer inventory;
    private String detail;
    private Byte disabled;
    private LocalDateTime gmtCreated;
    private LocalDateTime gmtModified;

    public GoodsSkuCreateVo(GoodsSkuPo po){
        this.id=po.getId();
        this.goodsSpuId=po.getGoodsSpuId();
        this.skuSn=po.getSkuSn();
        this.name=po.getName();
        this.originalPrice=po.getOriginalPrice();
        this.configuration=po.getConfiguration();
        this.weight=po.getWeight();
        this.imageUrl=po.getImageUrl();
        this.inventory=po.getInventory();
        this.detail=po.getDetail();
        this.disabled=po.getDisabled();
    }

}
