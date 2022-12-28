package com.oxo.ball.bean.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 奖金配置
 * 充值额度达到
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Data
@TableName("ball_bonus_config")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BallBonusConfig extends BaseDAO {

    private static final long serialVersionUID = 1L;

    /**
     * 优惠名称
     */
    private String name;

    /**
     * 0首充，1邀请
     * 首充达到x给上级奖励n？
     * 邀请成功人数达到x给上级奖励n？
     */
    private Integer type;
    /**
     * 奖金目标
     */
    private Integer bonusAim;
    /**
     * 推荐成功需连续投注天数
     */
    private Integer activityDay;
    /**
     * 奖励金额
     */
    private Integer bonusMoney;
    /**
     * 状态 1开启 2关闭
     */
    private Integer status;

    /**
     * 操作人
     */
    private String operUser;

}
