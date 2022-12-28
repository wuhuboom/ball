package com.oxo.ball.bean.dao;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 银行卡绑上日志
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Data
@TableName("ball_logger_bind_card")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BallLoggerBindCard extends BaseDAO {

    private static final long serialVersionUID = 1L;

    /**
     * 玩家ID
     */
    private Long playerId;
    private String username;
    private Long bankId;
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
     * 身份证号
     */
    private String identityCard;

    private String phone;

}
