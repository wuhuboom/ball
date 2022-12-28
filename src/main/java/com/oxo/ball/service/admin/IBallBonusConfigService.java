package com.oxo.ball.service.admin;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oxo.ball.bean.dao.BallBonusConfig;
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
public interface IBallBonusConfigService extends IService<BallBonusConfig> {
    SearchResponse<BallBonusConfig> search(BallBonusConfig query, Integer pageNo, Integer pageSize);
    BallBonusConfig insert(BallBonusConfig bonusConfig) throws ParseException;
    BallBonusConfig findById(Long id);
    Boolean delete(BallBonusConfig bonusConfig);
    Boolean edit(BallBonusConfig bonusConfig);
    Boolean status(BallBonusConfig bonusConfig);

    List<BallBonusConfig> findByType(int type);
}
