package com.oxo.ball.bean.dao;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.oxo.ball.bean.dao.BaseDAO;
import java.io.Serializable;

import lombok.*;

/**
 * <p>
 * 银行卡
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Data
@TableName("ball_bank_card")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BallBankCard extends BaseDAO {

    private static final long serialVersionUID = 1L;

    /**
     * 玩家ID
     */
    private Long playerId;
    private Long userId;
    private String username;
//    private Long bankId;
    /**
     * 银行名字
     */
    private String bankName;
    /**
     * 银行编码
     */
    private String backEncoding;
    /**
     * 卡号
     */
    private String cardNumber;
    /**
     * 持卡人姓名
     */
    private String cardName;

    /**
     * 国际
     */
    private String country;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 支行
     */
    private String subBranch;

    /**
     * 状态 1正常 2禁用
     */
    private Integer status;
    /**
     * 0待审核1通过2拒绝
     */
    private Integer statusCheck;
    /**
     * 审核人
     */
    private String checker;
    /**
     * 审核时间
     */
    private Long checkTime;
    /**
     * 操作人
     */
    private String operUser;

    /**
     * 身份证号
     */
    private String identityCard;

    private String phone;

    /**
     * 是否成功提现
     * 0否1是
     */
    private Integer hasWithdrawal;

    @TableField(exist = false)
    private String img;
    @TableField(exist = false)
    private Integer areaType;
    /**
     * 0.审核列表
     * 1.日志列表
     */
    @TableField(exist = false)
    private Integer queryType;

}
