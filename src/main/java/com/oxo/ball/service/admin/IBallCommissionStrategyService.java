package com.oxo.ball.service.admin;

import com.oxo.ball.bean.dao.BallCommissionStrategy;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oxo.ball.bean.dao.BallCommissionStrategy;
import com.oxo.ball.bean.dto.resp.SearchResponse;

import java.text.ParseException;
import java.util.List;

/**
 * <p>
 * 反佣策略 服务类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
public interface IBallCommissionStrategyService extends IService<BallCommissionStrategy> {
    SearchResponse<BallCommissionStrategy> search(BallCommissionStrategy query, Integer pageNo, Integer pageSize);
    BallCommissionStrategy insert(BallCommissionStrategy commissionStrategy) throws ParseException;
    BallCommissionStrategy findById(Long id);
    Boolean delete(BallCommissionStrategy commissionStrategy);
    Boolean edit(BallCommissionStrategy commissionStrategy);
    Boolean status(BallCommissionStrategy commissionStrategy);

    List<BallCommissionStrategy> findByType(int type);
}
