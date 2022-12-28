package com.oxo.ball.bean.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 提现失败配置
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ball_config_wfail")
public class BallConfigWfail extends BaseDAO {

    private static final long serialVersionUID = 1L;

    /**
     */
    private String title;

    /**
     * url
     */
    private String content;

    /**
     * 状态0禁1启
     */
    private Integer status;

}
