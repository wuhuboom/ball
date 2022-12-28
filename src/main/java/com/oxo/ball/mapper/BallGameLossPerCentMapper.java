package com.oxo.ball.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oxo.ball.bean.dao.BallGameLossPerCent;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 游戏赔率 Mapper 接口
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Mapper
public interface BallGameLossPerCentMapper extends BaseMapper<BallGameLossPerCent> {

    int batchInsert(@Param("lossPerCents") List<BallGameLossPerCent> lossPerCents);

    IPage<BallGameLossPerCent> listPage(@Param("page") Page<BallGameLossPerCent> page,@Param("query")BallGameLossPerCent query);
}
