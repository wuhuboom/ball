package com.oxo.ball.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oxo.ball.bean.dao.BallSystemNotice;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 系统公告 Mapper 接口
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Mapper
public interface BallSystemNoticeMapper extends BaseMapper<BallSystemNotice> {
    Page<BallSystemNotice> listPage(@Param("page") Page<BallSystemNotice> page, @Param("query") BallSystemNotice query);
}
