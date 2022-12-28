package com.oxo.ball.bean.dao;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.oxo.ball.bean.dao.BaseDAO;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.oxo.ball.bean.dto.model.RechargeRebateDto;
import com.oxo.ball.utils.TimeUtil;
import lombok.*;

/**
 * <p>
 * 存款策略
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Data
@TableName("ball_deposit_policy")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BallDepositPolicy extends BaseDAO {

    private static final long serialVersionUID = 1L;

    public static final int DEPOSITPOLICY_TYPE_FIRST = 1;
    public static final int DEPOSITPOLICY_TYPE_EVERY = 2;

    /**
     * 优惠名称
     */
    private String name;

    /**
     * 优惠类型 1首冲 2活动 3次充 4固定日 5.返佣 6邀请首充
     */
    private Integer depositPolicyType;

    /**
     * 1.usdt
     * 2.bank
     */
//    private Integer payType;
    /**
     * 充值ID
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long payId;

    private String country;
    /**
     * 自动结算
     */
    private Integer autoSettlement;

    private Integer week;
    /**
     * 开始时间
     */
    private Long startTime;

    /**
     * 结束时间
     */
    private Long endTime;

    /**
     * 规则
     * min,max,rate,
     */
    private String rules;

    /**
     * 状态 1开启 2关闭
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    //
    public String getStartStr() {
        if(getStartTime()==0){
            return "";
        }
        try {
            return TimeUtil.dateFormat(new Date(getStartTime()), TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS);
        } catch (Exception ex) {
            return "";
        }
    }

    public String getEndStr() {
        if(getEndTime()==0){
            return "";
        }
        try {
            return TimeUtil.dateFormat(new Date(getEndTime()), TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS);
        }catch (Exception e){
            return "";
        }
    }

    @TableField(exist = false)
    private String start;
    @TableField(exist = false)
    private String end;

    @TableField(exist = false)
    private List<RechargeRebateDto> odds;
}
