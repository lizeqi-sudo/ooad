package cn.edu.xmu.flashsale.model.bo;


import cn.edu.xmu.flashsale.model.po.GoodsSpuPo;
import cn.edu.xmu.ooad.model.VoObject;
import lombok.Data;
import cn.edu.xmu.flashsale.model.vo.GoodsSpuVo;
import cn.edu.xmu.flashsale.model.vo.GoodsSpuRetVo;
import cn.edu.xmu.flashsale.model.vo.GoodsSpuSimpleRetVo;

import java.time.LocalDateTime;

@Data
public class GoodsSpu implements VoObject{

    private Long id;
    private String name;
    private Long brandId;
    private Long categoryId;
    private Long freightId;
    private Long shopId;
    private String goodsSn;
    private String detail;
    private String imageUrl;
    private String spec;
    private Byte disabled;
    private LocalDateTime gmtCreated;
    private LocalDateTime gmtModified;

    public GoodsSpu(GoodsSpuVo vo)
    {
        this.name=vo.getName();
        this.detail=vo.getDetail();
        this.spec=vo.getSpec();
    }

    public GoodsSpu(GoodsSpuPo po)
    {
        this.id=po.getId();
        this.name=po.getName();
        this.brandId=po.getBrandId();
        this.categoryId=po.getCategoryId();
        this.freightId=po.getFreightId();
        this.shopId=po.getShopId();
        this.goodsSn=po.getGoodsSn();
        this.detail=po.getDetail();
        this.imageUrl=po.getImageUrl();
        this.spec=po.getSpec();
        this.disabled=po.getDisabled();
        this.gmtCreated=po.getGmtCreate();
        this.gmtModified=po.getGmtModified();
    }

    @Override
    public Object createVo() {
        return new GoodsSpuRetVo(this);
    }

    @Override
    public Object createSimpleVo() {
        return new GoodsSpuSimpleRetVo(this);
    }
}
