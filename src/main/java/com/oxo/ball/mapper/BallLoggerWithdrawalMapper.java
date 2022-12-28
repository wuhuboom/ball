package com.oxo.ball.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oxo.ball.bean.dao.BallLoggerRecharge;
import com.oxo.ball.bean.dao.BallLoggerWithdrawal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * <p>
 * 日志表 Mapper 接口
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Mapper
public interface BallLoggerWithdrawalMapper extends BaseMapper<BallLoggerWithdrawal> {
    IPage<BallLoggerWithdrawal> listPage(@Param("page") Page<BallLoggerWithdrawal> page, @Param("query") Map<String,Object> query);
    BallLoggerWithdrawal listPageStatis(@Param("query") Map<String,Object> query);
}
