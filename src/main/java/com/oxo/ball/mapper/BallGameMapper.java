package com.oxo.ball.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oxo.ball.bean.dao.BallGame;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.Map;

/**
 * <p>
 * 游戏赛事 Mapper 接口
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Mapper
public interface BallGameMapper extends BaseMapper<BallGame> {
    @Update("update ball_game set hot = 1 where id in (select t.id from (select id from ball_game where game_status = 1 order by start_time asc limit 10) t)")
    int autoSetHot();

    IPage<BallGame> page(@Param("page")Page<BallGame> page, @Param("query") Map<String, Object> query);
}
