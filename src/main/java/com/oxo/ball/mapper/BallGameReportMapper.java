package com.oxo.ball.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oxo.ball.bean.dao.BallGame;
import com.oxo.ball.bean.dao.BallGameReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 游戏赛事 Mapper 接口
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Mapper
public interface BallGameReportMapper extends BaseMapper<BallGameReport> {
}
