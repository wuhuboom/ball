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
 * 提现记录
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Data
@TableName("ball_logger_withdrawal")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BallLoggerWithdrawal implements Serializable {

    private static final long serialVersionUID = 1L;
    private static String[] WITHDRAWAL_TYPE=new String[]{"银行卡","USDT","SIM"};
    private static String[] WITHDRAWAL_STATUS=new String[]{"待审核","已审核","失败","提现成功","代付中","代付失败"};

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long playerId;
    private Long behalfId;
    private Integer accountType;
    private String playerName;
    private String superTree;
    private Integer playerType;
    //顶级
    private String topUsername;
    //一级
    private String firstUsername;
    /**
     * 1.银行卡,2.USDT",3.SIM
     */
    private Integer type;
    /**
     *
     */
    private Long usdtId;
    /**
     * 1待审核'
     * 2已审核'
     * 3失败'
     * 4提现成功'
     * 5代付中'
     * 6代付失败
     */
    private Integer status;
    /**
     * 金额
     */
    private Long money;
    /**
     * 手续费率
     */
    private Integer rate;

    /**
     * usdt汇率
     */
    private String usdtRate;
    /**
     * usdt金额
     */
    private Long usdtMoney;
    /**
     * 手续费
     */
    private Long commission;
    /**
     * 审核人
     */
    private String checker;
    /**
     * 确认人
     */
    private String oker;
    /**
     * 备注
     */
    private String remark;
    private String remarkFail;
    /**
     * 订单号
     */
    private Long orderNo;
    /**
     * 创建时间
     */
    private Long createdAt;
    /**
     * 处理时间
     */
    private Long updatedAt;
    /**
     * 下发代付时间
     */
    private Long behalfTime;

    /**
     * 提现IP地区
     */
    private String ipAddr;

    /**
     * 平台订单号
     */
    private String behalfNo;

    /**
     * 提现银行卡
     */
    private String toBank;
    private String toBankAccount;

    @TableField(exist = false)
    private Integer treeType;

    /**
     * 充值次数
     */
    @TableField(exist = false)
    private Integer topUpTimes;
    /**
     * 充值金额
     */
    @TableField(exist = false)
    private Long cumulativeTopUp;
    /**
     * 人工加
     */
    @TableField(exist = false)
    private Long artificialAdd;
    /**
     * 提现金额
     */
    @TableField(exist = false)
    private Long cumulativeReflect;

    @TableField(exist = false)
    private Integer action;
    @TableField(exist = false)
    private Long sysId;
    @TableField(exist = false)
    private Integer autoCheck;
    @TableField(exist = false)
    private String autoCheckTime;




    public static String getTypeString(int type){
        return WITHDRAWAL_TYPE[type-1];
    }
    public static String getStatusString(int status){
        return WITHDRAWAL_STATUS[status-1];
    }
}
