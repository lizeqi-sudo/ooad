package cn.edu.xmu.flashsale.model.vo;

import cn.edu.xmu.flashsale.model.bo.GoodsSku;
import lombok.Data;

/**
 * 商品状态VO
 * @author 李狄翰
 * @date 2020/11/10 18:41
 */
@Data
public class GoodsSkuStateVo {
    private Long Code;

    private String name;
    public GoodsSkuStateVo(GoodsSku.State state){
        Code=Long.valueOf(state.getCode());
        name=state.getDescription();
    }
}
