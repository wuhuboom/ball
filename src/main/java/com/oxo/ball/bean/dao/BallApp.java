package com.oxo.ball.bean.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ball_app")
public class BallApp extends BaseDAO {

    private static final long serialVersionUID = 1L;

    /**
     * 0.ios 1.android
     */
    private Integer appType;

    /**
     * url
     */
    private String appUrl;

    /**
     * 状态0禁1启
     */
    private Integer status;

}
