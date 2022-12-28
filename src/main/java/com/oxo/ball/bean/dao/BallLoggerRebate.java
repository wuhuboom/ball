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
 * 充值优惠订单
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Data
@TableName("ball_logger_rebate")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BallLoggerRebate extends BaseDAO implements Serializable {

    private static final long serialVersionUID = 1L;
    private static String TYPES_STRING[] = new String[]{"邀请首充奖励", "首冲奖励", "活动", "次充奖励", "固定日"};
    private static String STATUS_STRING[] = new String[]{"","未结算", "已结算", "已取消"};

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 用户ID
     */
    private Long playerId;
    private Integer accountType;
    private String playerName;
    private String topUsername;
    private String firstUsername;
    private String fromName;
    private Long orderNo;
    private String superTree;
    /**
     * 0邀请首充 1 首充 2 活动 3次充 4固定日 99充值返佣
     */
    private Integer type;
    /**
     * 1 usdt 2 印度 3加纳
     */
    private Integer payType;
    private Long payId;
    /**
     * 1.线上
     * 2.线下
     */
    private Integer payTypeOnff;

    /**
     * 优惠金额=充值金额*优惠百分比+固定
     */
    private Long money;

    /**
     * 充值金额
     */
    private Long moneyReal;
    /**
     * usdt金额
     */
    private Long moneyUsdt;
    /**
     * usdt汇率
     */
    private String rateUsdt;

    /**
     * 优惠百分比
     */
    private String rate;
    /**
     * 固定金额
     */
    private String fixed;
    /**
     * 状态 1未发放 2已发放 3取消
     */
    private Integer status;
    /**
     *
     */
    private String remark;

    @TableField(exist = false)
    private Integer treeType;

    @TableField(exist = false)
    private String begin;

    @TableField(exist = false)
    private String end;

    @TableField(exist = false)
    private String moneyParam;

    public static String getTypeString(int type){
        if(type<5){
            return TYPES_STRING[type];
        }
        return "";
    }
    public static String getStatusString(int status){
        return STATUS_STRING[status];
    }
}
