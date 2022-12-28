package com.oxo.ball.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oxo.ball.bean.dao.BallBank;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 银行 Mapper 接口
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Mapper
public interface BallBankMapper extends BaseMapper<BallBank> {
    int insertBatch(@Param("list") List<BallBank> list);
}
