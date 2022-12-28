package com.oxo.ball.bean.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oxo.ball.bean.dao.BaseDAO;
import java.io.Serializable;

import lombok.*;

/**
 * <p>
 * 
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ball_vip")
public class BallVip extends BaseDAO {

    private static final long serialVersionUID = 1L;

    /**
     * 会员等级
     */
    private Integer level;
    /**
     * 余额
     */
    private Long balance;
    /**
     * 升级条件
     * 0.累计充值
     * 1.充提差额
     */
    private Integer upTotal;
    private Integer upRw;

    /**
     * 额外收益
     */
    private String levelProfit;
    /**
     * 名称
     */
    private String levelName;

    /**
     * 最小提现
     */
    private Long minPull;

    /**
     * 最大提现
     */
    private Long maxPull;

    /**
     * 返水（%）
     */
    private Integer backWater;

    /**
     * 礼金
     */
    private Integer cashGift;

    /**
     * 发放周期
     */
    private Integer cashGiftInterval;

    /**
     * 状态0禁1启
     */
    private Integer status;

    /**
     * VIP日奖励比率
     */
    private String dayReward;
}
