package com.oxo.ball.bean.dao;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.*;

/**
 * <p>
 * 下注
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Data
@TableName("ball_bet")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BallBet extends BaseDAO {

    private static String[] BET_TYPE = new String[]{"正波","反波"};
    //0.未确认 1已确认 2已取消 3 已撤消 4已回滚
    private static String[] BET_STATUS = new String[]{"未确认", "已确认", "已取消", "已撤消", "已回滚"};
    private static String[] EVEN_STATUS = new String[]{"保本", "弃保"};
    //0未开奖 1已中奖 2未中奖
    private static String[] OPEN_STATUS = new String[]{"未开奖","已中奖", "未中奖"};
    //1上半场 2全场
    private static String[] GAME_TYPE = new String[]{"上半场", "全场"};
    //0未结算 1已结算
    private static String[] STATUS_SETTLEMENT = new String[]{"未结算", "已结算"};
    private static final long serialVersionUID = 1L;

    /**
     * 游戏id 
     */
    private Long gameId;
    /**
     * 玩家id
     */
    private Long playerId;

    /**
     * 游戏赔率 id(索引)
     */
    private Long gameLossPerCentId;

    /**
     * 下住金额
     */
    private Long betMoney;
    /**
     * 比分
     */
    private String betScore;
    /**
     * 赔率
     */
    private String betOdds;

    /**
     * 手续费
     */
    private Long handMoney;

    /**
     * 中奖金额
     */
    private Long winningAmount;
    /**
     * 0.未确认 1已确认 2已取消 3 已撤消 4已回滚
     */
    private Integer status;
    /**
     * 结算状态 0未结算 1已结算
     */
    private Integer statusSettlement;

    /**
     * 订单号-年-月-日+1000001
     * yyyyMMdd
     *
     */
    private Long orderNo;

    /**
     * 结算时间
     */
    private Long settlementTime;
    /**
     * 结算人 默认系统 ，否则操作人
     */
    private String settlememntPerson;
    /**
     * 下注类型
     * 1正波
     * 2反波
     */
    private Integer betType;

    /**
     * 备注
     */
    private String remark;
    // new columns
    /**
     * 用户名
     */
    private String username;
    //顶级
    private String topUsername;
    //一级
    private String firstUsername;
    private Long userId;
    /**
     * 账号类型
     */
    private Integer accountType;
    /**
     * 赛事信息 主-客
     */
    private String gameInfo;
    /**
     * 开赛时间
     */
    private Long startTime;
    /**
     * 保本状态 1保本 2弃保
     */
    private Integer even;
    /**
     * 0未开奖 1已中奖 2未中奖
     */
    private Integer statusOpen;
    /**
     * 比赛类型 1上半场 2全场
     */
    private Integer gameType;

    private Long superiorId;
    private String superTree;

    @TableField(exist = false)
    private String betBegin;
    @TableField(exist = false)
    private String betEnd;

    @TableField(exist = false)
    private String startBegin;
    @TableField(exist = false)
    private String startEnd;

    @TableField(exist = false)
    private String settlementBegin;
    @TableField(exist = false)
    private String settlementEnd;

    @TableField(exist = false)
    private Integer treeType;
    @TableField(exist = false)
    private String allianceName;
    @TableField(exist = false)
    private String mainLogo;
    @TableField(exist = false)
    private String mainName;
    @TableField(exist = false)
    private String guestLogo;
    @TableField(exist = false)
    private String guestName;
    @TableField(exist = false)
    private Double bonus;
    @TableField(exist = false)
    private String chargeRate;
//    allianceName,g.mainLogo,g.mainName,
//    g.guestLogo,g.guestName,g.startTime

    public static String getBetTypeString(int type){
        return BET_TYPE[type-1];
    }
    public static String getBetStatusString(int status){
        return BET_STATUS[status];
    }
    public static String getEvenString(int status){
        return EVEN_STATUS[status-1];
    }
    public static String getOpenStatusString(int type){
        return OPEN_STATUS[type];
    }
    public static String getGameTypeString(int type){
        return GAME_TYPE[type-1];
    }
    public static String getSettlementString(int type){
        return STATUS_SETTLEMENT[type];
    }
}
