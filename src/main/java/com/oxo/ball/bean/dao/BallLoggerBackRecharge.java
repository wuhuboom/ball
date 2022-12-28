//package com.oxo.ball.bean.dao;
//
//import com.baomidou.mybatisplus.annotation.IdType;
//import com.baomidou.mybatisplus.annotation.TableField;
//import com.baomidou.mybatisplus.annotation.TableId;
//import com.baomidou.mybatisplus.annotation.TableName;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.io.Serializable;
//
///**
// * <p>
// * 日志表-返佣日志
// * </p>
// *
// * @author oxo_jy
// * @since 2022-04-13
// */
//@Data
//@TableName("ball_logger_back_recharge")
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class BallLoggerBackRecharge implements Serializable {
//
//    private static final long serialVersionUID = 1L;
//
//    @TableId(value = "id", type = IdType.AUTO)
//    private Long id;
//    /**
//     * 用户ID
//     */
//    private Long playerId;
//    private Long fromId;
//    private Integer accountType;
//    private String orderNo;
//    private String playerName;
//    private String fromName;
//    private String superTree;
//    private String topUsername;
//    private String firstUsername;
//    private Integer payTypeOnff;
//    /**
//     * 金额
//     */
//    private Long money;
//    /**
//     * 状态 1未结算 2已结算 3取消
//     */
//    private Integer status;
//    /**
//     * 操作时间
//     */
//    private Long createdAt;
//    private Long updatedAt;
//    //充值金额
//    private Long moneyRecharge;
//    private String rate;
//    private String fixed;
//    private Integer vipRank;
//    private String remark;
//
//    @TableField(exist = false)
//    private Integer treeType;
//
//    @TableField(exist = false)
//    private String begin;
//
//    @TableField(exist = false)
//    private String end;
//
//    @TableField(exist = false)
//    private String moneyParam;
//}
