package cn.edu.xmu.flashsale.model.vo;

import cn.edu.xmu.flashsale.model.po.ShopPo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 权限传值对象
 *
 * @author Di Han Li
 * @date Created in 2020/11/24 9:08
 * Modified by 24320182203221 李狄翰 at 2020/11/24 8:00
 **/
@Data
@ApiModel("店铺传值对象")
public class ShopRetVo {

    @ApiModelProperty(value = "店铺id")
    private Long id;
    @ApiModelProperty(value = "店铺名称")
    private String name;
    @ApiModelProperty(value = "店铺状态")
    private Byte state;

    public ShopRetVo(ShopPo po)
    {
        this.id=po.getId();
        this.name=po.getName();
        this.state=po.getState();
    }


}
