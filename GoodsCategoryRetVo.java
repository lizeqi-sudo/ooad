package cn.edu.xmu.goods.model.vo;

import cn.edu.xmu.goods.model.po.GoodsCategoryPo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.format.DateTimeFormatter;

/**
 * 权限传值对象
 *
 * @author Di Han Li
 * @date Created in 2020/11/24 9:08
 * Modified by 24320182203221 李狄翰 at 2020/11/24 8:00
 **/
@Data
@ApiModel("商品类别传值对象")
public class GoodsCategoryRetVo {
    private Long id;
    private Long pid;
    private String name;
    private String gmtCreate;
    private String gmtModified;

    public GoodsCategoryRetVo(GoodsCategoryPo po)
    {
        this.id = po.getId();
        this.pid = po.getPid();
        this.name = po.getName();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        this.gmtCreate = df.format(po.getGmtCreate());
        this.gmtModified = df.format(po.getGmtModified());
    }
}
