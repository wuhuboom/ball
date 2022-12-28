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
@TableName("ball_ip_white")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BallIpWhite extends BaseDAO {

    private static final long serialVersionUID = 1L;

    /**
     * IP
     */
    private String ip;

    /**
     * 备注
     */
    private String remark;

    /**
     * 1正常 2禁用
     */
    private Integer status;


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BallIpWhite that = (BallIpWhite) o;
        return Objects.equals(ip, that.ip);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), ip);
    }
}
