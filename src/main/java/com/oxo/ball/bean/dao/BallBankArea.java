package com.oxo.ball.bean.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NegativeOrZero;
import java.io.Serializable;

@Data
@TableName("ball_bank_area")
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BallBankArea implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 国家
     */
    private String contry;
    /**
     * 1启用 2禁用
     */
    private Integer status;

//    /**
//     * 1 印度  2加纳
//     */
//    private Integer code;

    /**
     * 关联手机区号
     */
    private String areaCode;
}
