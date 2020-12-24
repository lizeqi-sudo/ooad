package cn.edu.xmu.flashsale.model.bo;

import cn.edu.xmu.flashsale.model.po.FlashSaleItemPo;
import cn.edu.xmu.flashsale.model.vo.FlashSaleItemRetVo;
import cn.edu.xmu.flashsale.model.po.GoodsSkuPo;
import cn.edu.xmu.flashsale.model.vo.GoodsSkuRetVo;
import cn.edu.xmu.ooad.model.VoObject;
import cn.edu.xmu.ooad.util.Common;
import cn.edu.xmu.ooad.util.encript.AES;
import cn.edu.xmu.ooad.util.encript.SHA256;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Data
public class FlashSaleItem implements VoObject, Serializable {

    private Long id;
    private Long saleId;
    private Long goodsSkuId;
    private Long price;
    private Integer quantity;
    private LocalDateTime gmtCreated;
    private LocalDateTime gmtModified;

    private GoodsSku goodsSku;

    public FlashSaleItem() {
    }

    public FlashSaleItem(FlashSaleItemPo po, GoodsSkuPo goodsSkuPo) {
        this.id = po.getId();
        this.quantity = po.getQuantity();
        this.price = po.getPrice();
        this.gmtCreated = po.getGmtCreate();
        this.gmtModified = po.getGmtModified();
        this.goodsSku = new GoodsSku(goodsSkuPo);
    }

    @Override
    public Object createVo() {
        FlashSaleItemRetVo retVo = new FlashSaleItemRetVo();
        retVo.setId(this.id);
        retVo.setPrice(this.price);
        retVo.setQuantity(this.quantity);
        GoodsSkuRetVo goodsSkuRetVo = (GoodsSkuRetVo) goodsSku.createVo();
        retVo.setGoodsSkuRetVo(goodsSkuRetVo);
        return retVo;
    }

    @Override
    public Object createSimpleVo() {
        return null;
    }
}
