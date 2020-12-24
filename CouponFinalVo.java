package cn.edu.xmu.goods.model.vo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Data
@NoArgsConstructor
public class CouponFinalVo implements Serializable {

    private List<CouponSimpleRetVo> list;

    public CouponFinalVo(List<CouponSimpleRetVo> list)
    {
        this.list = new ArrayList<>();
        this.list = list;
    }
}
