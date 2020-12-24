package cn.edu.xmu.flashsale.model.vo;


import lombok.Data;

@Data
public class GrouponActivitySpuVo {

    private Long id;
    private String name;
    private String goodsSn;
    private String imageUrl;
    private String gmtCreate;
    private String gmtModified;
    private Byte disable;

    public GrouponActivitySpuVo(){}
}
