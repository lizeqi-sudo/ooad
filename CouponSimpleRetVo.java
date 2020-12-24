package cn.edu.xmu.goods.model.vo;

import cn.edu.xmu.goods.model.bo.Coupon;
import cn.edu.xmu.goods.model.bo.CouponActivity;
import cn.edu.xmu.goods.model.po.CouponPo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
@Data
@ApiModel(description = "优惠券简单视图对象")
public class CouponSimpleRetVo implements Serializable {
   private String couponSn;


    public CouponSimpleRetVo(Coupon po) {
        this.couponSn = po.getCouponSn();
    }

    public CouponSimpleRetVo(CouponPo po) {
        this.couponSn = po.getCouponSn();
    }

    public CouponSimpleRetVo(){}
}
