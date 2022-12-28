package com.oxo.ball.bean.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.oxo.ball.bean.dao.BaseDAO;
import java.io.Serializable;

import lombok.*;

/**
 * <p>
 * 账变表
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ball_balance_change")
public class BallBalanceChange implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long createdAt;
    private Long orderNo;
    /**
     * 玩家id
     */
    private Long playerId;
    /**
     * 账号类型 1测试
     */
    private Integer accountType;
    private Long userId;
    private String username;
    private String superTree;
    private Long parentId;
    /**
     * 变化金额
     */
    private Long changeMoney;

    /**
     * 初始金额
     */
    private Long initMoney;

    /**
     * 变化后的金额
     */
    private Long dnedMoney;

    /**
     * 1线上充值 2提现 3投注 4投注盈利 5下级盈利返利
     * 6人工加款 7投注撤消
     * 8人工减款 9投注返奖扣除 10 充值撤消 11线下充值
     * 12提现退回 13投注退回 14投注回滚 15注册赠送
     * 16奖金 17 VIP礼金 18 活动礼金 19充值赠送
     * 20手续费 21下级充值返利 22下级投注返利
     * 23邀请奖励 24首充奖励 25活动 26次充奖励 27固定日
     * 28邀请首充奖励
     */
    private Integer balanceChangeType;

    /**
     * 备注
     */
    private String remark;
    /**
     * 打码量
     */
    private Long qr;

    /**
     * 账变状态 0 未账变  1账变
     * -0指下注,提现,账号类余额已扣,但是还不能计算到报表中,撤单后变为假账变
     * -1 已真实账变
     */
    private Integer frozenStatus;


    @TableField(exist = false)
    private Integer timeType;
    @TableField(exist = false)
    private String begin;
    @TableField(exist = false)
    private String end;
    @TableField(exist = false)
    private Long discount;

    @TableField(exist = false)
    private Object recharge;
    @TableField(exist = false)
    private Object rechargeCount;
    @TableField(exist = false)
    private Object withdrawal;
    @TableField(exist = false)
    private Object withdrawalCount;
    @TableField(exist = false)
    private Object betMoney;
    @TableField(exist = false)
    private Object bingoMoney;
    @TableField(exist = false)
    private Object rechargeMore;
    @TableField(exist = false)
    private Object activity;
    @TableField(exist = false)
    private Object playerName;
    @TableField(exist = false)
    private Object playerParent;
    @TableField(exist = false)
    private Object balance;
    @TableField(exist = false)
    private Object win;

}
