package com.oxo.ball.bean.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@TableName("ball_virtual_currency")
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BallVirtualCurrency extends BaseDAO {
    private Long playerId;
    private Long userId;
    private String username;
    /**
     * 地址
     */
    private String addr;
    /**
     * 协议
     */
    private String protocol;

    /**
     * 状态 1正常 2禁用
     */
    private Integer status;
    /**
     * 0待审核1通过2拒绝
     */
    private Integer statusCheck;
    /**
     * 审核人
     */
    private String checker;
    /**
     * 审核时间
     */
    private Long checkTime;
    /**
     * 操作人
     */
    private String operUser;

    @TableField(exist = false)
    private String img;
}
