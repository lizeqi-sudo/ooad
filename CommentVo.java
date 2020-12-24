package cn.edu.xmu.goods.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 权限传值对象
 *
 * @author Di Han Li
 * @date Created in 2020/11/24 9:08
 * Modified by 24320182203221 李狄翰 at 2020/11/24 8:00
 **/
@Data
@ApiModel("评论传值对象")
public class CommentVo {

    @ApiModelProperty(name = "评论类型", value = "type", required = true)
    private Byte type;

    @ApiModelProperty(name = "评论内容", value = "content", required = true)
    private String content;

}
