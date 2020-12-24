package cn.edu.xmu.flashsale.model.bo;

import cn.edu.xmu.flashsale.model.vo.FlashSaleVo;
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
public class FlashSale {

    private Long id;
    private LocalDateTime flashDate;
    private Long timeSegId;
    private LocalDateTime gmtCreated;
    private LocalDateTime gmtModified;

    public enum State {
        OFFLINE((byte)0, "已下线"),
        ONLINE((byte)1, "已上线"),
        DELETE((byte)2, "已删除");

        private Byte code;
        private String description;

        State(Byte code, String description) {
            this.code = code;
            this.description = description;
        }
        public void setCode(Byte code){
            this.code=code;
        }

        public Byte getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }
    private State state= State.OFFLINE;

    public FlashSale(FlashSaleVo vo)
    {
        this.flashDate = LocalDateTime.parse(vo.getFlashDate());
    }
}
