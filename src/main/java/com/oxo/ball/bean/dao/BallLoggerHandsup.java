package com.oxo.ball.bean.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * <p>
 * 手动上下分记录
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Data
@TableName("ball_logger_handsup")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BallLoggerHandsup implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long playerId;
    private Integer accountType;
    private String username;
    private Long userId;
    private String superTree;
    /**
     * 0.下分,1.上分"),
     */
    private Integer type;
    /**
     * 金额
     */
    private Long money;

    /**
     * 创建时间
     */
    private Long createdAt;
    /**
     * 操作人
     */
    private String operUser;
    /**
     * 打码倍数
     */
    private Integer qrmult;
    /**
     * 备注
     */
    private String remark;

    @TableField(exist = false)
    private Integer moneyMin;
    @TableField(exist = false)
    private Integer moneyMax;
}
