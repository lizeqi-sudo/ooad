package cn.edu.xmu.goods.model.bo;

import cn.edu.xmu.goods.model.po.CommentPo;
import cn.edu.xmu.goods.model.vo.*;
import cn.edu.xmu.ooad.goods.require.model.CustomerSimple;
import cn.edu.xmu.ooad.model.VoObject;
import cn.edu.xmu.ooad.util.Common;
import cn.edu.xmu.ooad.util.encript.AES;
import cn.edu.xmu.ooad.util.encript.SHA256;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Data
public class Comment implements VoObject{

    private Long id;
    private CustomerVo customer;
    private Long customerId;
    private Long goodsSkuId;
    private Long orderItemId;
    private Byte type;
    private String content;
    private LocalDateTime gmtCreated;
    private LocalDateTime gmtModified;

    public enum State {
        UNAUDITED((byte)0, "未审核"),
        SUCCESS((byte)1, "评论成功"),
        DENY((byte)2, "未通过");


        private Byte code;
        private String description;

        State(Byte code, String description) {
            this.code = code;
            this.description = description;
        }

        public void setCode(Byte code) {
            this.code = code;
        }

        public Byte getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    private State state=State.UNAUDITED;

    public Comment(CommentVo vo)
    {
        this.type=vo.getType();
        this.content=vo.getContent();
    }

    public Comment(CommentPo po, CustomerSimple customerSimple)
    {
        this.id=po.getId();
        this.customerId=po.getCustomerId();
        this.content=po.getContent();
        this.goodsSkuId=po.getGoodsSkuId();
        this.orderItemId=po.getOrderitemId();
        this.type=po.getType();
        this.state.setCode(po.getState());
        this.gmtCreated=po.getGmtCreate();
        this.gmtModified=po.getGmtModified();
        this.customer = new CustomerVo();
        this.customer.setId(customerSimple.getId());
        this.customer.setUserName(customerSimple.getUser_name());
        this.customer.setName(customerSimple.getReal_name());
    }

    @Override
    public Object createVo() {
        return new CommentRetVo(this);
    }

    @Override
    public Object createSimpleVo() {
        return new CommentSimpleRetVo(this);
    }
}
