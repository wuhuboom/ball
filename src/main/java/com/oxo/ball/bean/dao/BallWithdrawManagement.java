package com.oxo.ball.bean.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oxo.ball.bean.dao.BaseDAO;
import java.io.Serializable;

import lombok.*;

/**
 * <p>
 * 提现方式
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("ball_withdraw_management")
public class BallWithdrawManagement extends BaseDAO {

    private static final long serialVersionUID = 1L;

    /**
     * 提现名称
     */
    private String name;
    private String country;
    /**
     * 图片地址
     */
    private String iamgeUrl;
    /**
     * 语言编码
     */
//    private String language;
    /**
     * 汇率
     */
    private String rate;
    private String currencySymbol;
    /**
     * 1启用 2关闭
     */
    private Integer status;
    /**
     *
     */
    private Integer sort;
    private String operUser;
    /**
     * 类型 1.银行 2.ustd 3.sim
     */
    private Integer type;

}
