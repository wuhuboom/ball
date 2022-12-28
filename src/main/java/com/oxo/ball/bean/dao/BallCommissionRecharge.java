package com.oxo.ball.bean.dao;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.oxo.ball.bean.dto.model.RechargeRebateDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * <p>
 * 存款反佣策略
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Data
@TableName("ball_commission_recharge")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BallCommissionRecharge extends BaseDAO {

    private static final long serialVersionUID = 1L;

    /**
     * 优惠名称
     */
    private String name;

    /**
     * 返佣层级
     */
    private Integer commissionLevel;
    /**
     * 自动发放 1 自动 0不自动
     */
    private Integer automaticDistribution;
    /**
     * 首次自动发放 1 自动 0不自动
     */
    private Integer autoSettleFirst;

    /**
     * 状态 1开启 2关闭
     */
    private Integer status;

    /**
     * 操作人
     */
    private String operUser;

    /**
     * [
     * {
     *     begin:100,end,200,level:0,commission:0.1,fixed:10
     * }
     * ]
     */
    private String rules;

    @TableField(exist = false)
    private List<RechargeRebateDto> odds;
}
