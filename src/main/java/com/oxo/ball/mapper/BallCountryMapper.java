package com.oxo.ball.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oxo.ball.bean.dao.BallCountry;
import com.oxo.ball.bean.dao.BallTimezone;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 时区 Mapper 接口
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Mapper
public interface BallCountryMapper extends BaseMapper<BallCountry> {

}
