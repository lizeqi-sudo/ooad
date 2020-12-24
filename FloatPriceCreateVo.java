package cn.edu.xmu.goods.model.vo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import cn.edu.xmu.goods.model.bo.GrouponActivity;
import cn.edu.xmu.goods.model.po.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class FloatPriceCreateVo {

    private Long id;
    private Long goodsSkuId;
    private Long activityPrice;
    private LocalDateTime beginTime;
    private LocalDateTime endTime;
    private Integer quantity;
    private CustomerVo createdBy;
    private CustomerVo modifiedBy;
    private Long invalidBy;
    private Byte valid;
    private LocalDateTime gmtCreated;
    private LocalDateTime gmtModified;

    public FloatPriceCreateVo(FloatPricePo Po,Long userId){
        this.id=Po.getId();
        this.goodsSkuId=Po.getGoodsSkuId();
        this.activityPrice=Po.getActivityPrice();
        this.beginTime=Po.getBeginTime();
        this.endTime=Po.getEndTime();
        this.quantity=Po.getQuantity();
        this.createdBy=new CustomerVo(userId);
        this.modifiedBy=new CustomerVo(userId);
        this.invalidBy=Po.getInvalidBy();
    }

}
