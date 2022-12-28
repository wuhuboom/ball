package com.oxo.ball.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oxo.ball.bean.dao.BallSimCurrency;
import com.oxo.ball.bean.dao.BallVirtualCurrency;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 银行 Mapper 接口
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Mapper
public interface BallSimCurrencyMapper extends BaseMapper<BallSimCurrency> {

}
