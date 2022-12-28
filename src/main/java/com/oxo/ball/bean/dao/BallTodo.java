package com.oxo.ball.bean.dao;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 待办事项
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Data
@TableName("ball_todo")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BallTodo extends BaseDAO {

    private static final long serialVersionUID = 1L;

    /**
     * 待办事项
     * 1.充值奖金
     * 2.邀请奖金
     */
    private String name;

    private String playerName;
    private Long playerId;
    private String topUsername;
    private String firstUsername;
    private String fromName;
    private Long fromId;
    private Long bonusId;
    private String superTree;
    /**
     * 奖励金额
     */
    private Integer bonusMoney;
    /**
     * 状态 0未处理1已处理
     */
    private Integer status;
    /**
     * 操作人
     */
    private String operUser;

    @TableField(exist = false)
    private BallBonusConfig bonusConfig;
}
