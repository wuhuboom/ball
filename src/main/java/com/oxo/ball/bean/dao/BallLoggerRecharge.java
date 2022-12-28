package com.oxo.ball.bean.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.oxo.ball.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

/**
 * <p>
 * 充值记录
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Data
@TableName("ball_logger_recharge")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BallLoggerRecharge implements Serializable {

    private static String[] STATUS = new String[]{"待付款","已到账","已上分","支付超时"};

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long playerId;
    private Integer accountType;
    private String username;
    private Long userId;
    private String superTree;
    //顶级
    private String topUsername;
    //一级
    private String firstUsername;
    /**
     * 充值渠道
     */
    private String payName;
    private Long payId;
    /**
     .* 1线上,2.线下"),
     */
    private Integer type;
    /**
     * 1待付款/2已到账/3已上分/4支付超时
     */
    private Integer status;
    /**
     * 金额,拉起
     */
    private Long money;
    /**
     * 金额-优惠
     */
    private Long moneyDiscount;
    /**
     * 实际支付
     */
    private Long moneyReal;
    /**
     * 转系统金额
     */
    private Long moneySys;

    /**
     * 订单号
     */
    private Long orderNo;
    /**
     * 创建时间
     */
    private Long createdAt;
    private Long updatedAt;
    /**
     * 备注
     */
    private String remark;
    /*充值手机*/
    private String phone;
    /*ip国家*/
    private String ip;
    private String country;
    /**
     * 0否1是
     */
    private Integer first;
    /**
     * 操作人
     */
    private String operUser;

    @TableField(exist = false)
    private Object payUrl;
    @TableField(exist = false)
    private Integer moneyMin;
    @TableField(exist = false)
    private Integer moneyMax;

    @TableField(exist = false)
    private String moneyParam;



    @Override
    public String toString() {
        try {
            return JsonUtil.toJson(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "{}";
    }

    public static String getStatusString(int status){
        return STATUS[status-1];
    }
}
