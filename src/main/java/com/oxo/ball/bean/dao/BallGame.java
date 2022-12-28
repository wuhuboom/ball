package com.oxo.ball.bean.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.oxo.ball.bean.dao.BaseDAO;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.oxo.ball.utils.TimeUtil;
import lombok.*;

/**
 * <p>
 * 游戏赛事
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("ball_game")
@JsonIgnoreProperties(ignoreUnknown = true)
public class BallGame implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.INPUT)
    private Long id;
    private Long createdAt;
    private Long updatedAt;
    /**
     * 联盟logo
     */
    private String allianceLogo;

    /**
     * 联盟名字
     */
    private String allianceName;

    /**
     * 主队logo
     */
    private String mainLogo;

    /**
     * 主队名字
     */
    private String mainName;

    /**
     * 客队logo
     */
    private String guestLogo;

    /**
     * 客队名字
     */
    private String guestName;

    /**
     * 开赛时间
     */
    private Long startTime;
    private Long finishTime;

//    /**
//     * 比分 0:0/(0-0)  括号库里面是上半场
//     */
//    private String score;
    /**
     *  主半场
     */
    private Integer homeHalf;
    /**
     * 客半场
     */
    private Integer guestHalf;
    /**
     *  主全场
     */
    private Integer homeFull;
    /**
     * 客全场
     */
    private Integer guestFull;
    /**
     *  主加时
     */
    private Integer homeOvertime;
    /**
     * 客加时
     */
    private Integer guestOvertime;
    /**
     *  主点球
     */
    private Integer homePenalty;
    /**
     * 客点球
     */
    private Integer guestPenalty;

    /**
     * 比赛状态 1 没有开始 2正常进行  3结束 4.比赛取消
     */
    private Integer gameStatus;
    /**
     * 比赛状态说明
     */
    private String gameStatusRemark;
    /**
     * 结算类型
     * 0 自动结算
     * 1 手动结算
     * 2 回滚
     * 3 重算
     * 4 处理中
     * 5 撤消
     */
    private Integer settlementType;
    private String settlementRemark;
    private Long settlementTime;

    /**
     * 置顶  1置顶 2 不指定
     */
    private Integer top;

    /**
     * 是否热门 1 热门 2非热门
     */
    private Integer hot;

    /**
     * 是否保本 1保本 2不保本(默认为2)
     */
    private Integer even;

    /**
     * 状态 1开启  2关闭
     */
    private Integer status;

    /**
     * 来源 0 接口采集 1手动添加
     */
    private Integer fromTo;
    /**
     * 开赛YMD
     */
    private String ymd;

    private Integer minBet;
    private Integer maxBet;
    private Integer totalBet;

    @TableField(exist = false)
    private Long betPlayers;
    @TableField(exist = false)
    private Long betMoney;
    @TableField(exist = false)
    private Long betCount;

    @TableField(exist = false)
    private Integer timeType;
    @TableField(exist = false)
    private String begin;
    @TableField(exist = false)
    private String end;
    @TableField(exist = false)
    private String logoKey;
    @TableField(exist = false)
    private List<BallGameLossPerCent> odds;

    @TableField(exist = false)
    private String finishTimeStr;
    public String getFinishTimeStr() {
        if(this.getFinishTime()==0){
            return "";
        }
        return TimeUtil.dateFormat(new Date(this.getFinishTime()),TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS);
    }
    public String getFinishTimeStr2(){
        return this.finishTimeStr;
    }
    public Long getRemainingTime(){
        return startTime - System.currentTimeMillis();
    }

    public String getScore(){
        // 0:0/(0-0)  全场/(上半场)/(加时)/(点球)
        if(getHomeFull()==null){
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(homeFull);
        sb.append(":");
        sb.append(guestFull);
        sb.append("/(");
        sb.append(homeHalf);
        sb.append(":");
        sb.append(guestHalf);
        sb.append(")");
        //如果有加时
        if(homeOvertime==null){
            return sb.toString();
        }
        sb.append("/(");
        sb.append(homeOvertime);
        sb.append(":");
        sb.append(guestOvertime);
        sb.append(")");
        //如果有点球
        if(homePenalty==null){
            return sb.toString();
        }
        sb.append("/(");
        sb.append(homePenalty);
        sb.append(":");
        sb.append(guestPenalty);
        sb.append(")");
        return sb.toString();
    }

}
