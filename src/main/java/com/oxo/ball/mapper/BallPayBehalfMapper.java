package com.oxo.ball.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oxo.ball.bean.dao.BallPayBehalf;
import com.oxo.ball.bean.dao.BallPaymentManagement;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 支付管理 Mapper 接口
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Mapper
public interface BallPayBehalfMapper extends BaseMapper<BallPayBehalf> {

}
