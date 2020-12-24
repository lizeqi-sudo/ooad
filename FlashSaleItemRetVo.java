package cn.edu.xmu.flashsale.model.vo;


import cn.edu.xmu.flashsale.model.po.FlashSaleItemPo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class FlashSaleItemRetVo {

    private Long id;
    private Long saleId;
    private Long goodsSkuId;
    private Long price;
    private Integer quantity;
    private GoodsSkuRetVo goodsSkuRetVo;

    public FlashSaleItemRetVo(){}

    public FlashSaleItemRetVo(FlashSaleItemPo po)
    {
        this.id = po.getId();
        this.saleId = po.getSaleId();
        this.goodsSkuId = po.getGoodsSkuId();
        this.price = po.getPrice();
        this.quantity = po.getQuantity();
    }
}
