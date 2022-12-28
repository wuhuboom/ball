package com.oxo.ball.bean.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * <p>
 * 后台白名单
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Data
@TableName("ball_ip_country")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BallIpCountry extends BaseDAO {
    private static final long serialVersionUID = 1L;
    /**
     * 国家
     */
    private String country;

    /**
     * 备注
     */
    private String remark;

    /**
     * 1正常 2禁用
     */
    private Integer status;

}
