package cn.edu.xmu.goods.model.vo;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import cn.edu.xmu.goods.model.bo.GrouponActivity;
import cn.edu.xmu.goods.model.po.GoodsCategoryPo;
import cn.edu.xmu.goods.model.po.GoodsSpuPo;
import cn.edu.xmu.goods.model.po.GrouponActivityPo;
import cn.edu.xmu.goods.model.po.ShopPo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
@Data
public class CategoryCreateVo {
    private Long id;
    private String name;
    private Long pid;
    private String gmtCreate;
    private String gmtModified;
    public CategoryCreateVo(GoodsCategoryPo po1)
    {
        this.id=po1.getId();
        this.name=po1.getName();
        this.pid=po1.getPid();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.gmtCreate=df.format(po1.getGmtCreate());
        this.gmtModified=df.format(po1.getGmtModified());
    }
}
