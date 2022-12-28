package com.oxo.ball.bean.dao;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@TableName("ball_sim_currency")
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BallSimCurrency extends BaseDAO {
    private Long playerId;
    private String username;
    /**
     */
    private String simName;
    private String sim;
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
