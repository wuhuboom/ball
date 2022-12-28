package com.oxo.ball.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oxo.ball.bean.dao.BallBet;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oxo.ball.bean.dao.BallLoggerBet;
import com.oxo.ball.bean.dto.req.report.ReportStandardRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * <p>
 * 下注 Mapper 接口
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Mapper
public interface BallBetMapper extends BaseMapper<BallBet> {
    //主场球队名字及图片，客场球队名字及图片,联盟名,开赛时间
    IPage<BallBet> pages(@Param("page") Page<BallBet> page, @Param("query") Map<String, Object> query);
    int standard(@Param("query") ReportStandardRequest query);
    int standard2(@Param("query") ReportStandardRequest query);

    IPage<BallBet> page_s(Page<BallBet> page, @Param("query") ReportStandardRequest reportStandardRequest);
}
