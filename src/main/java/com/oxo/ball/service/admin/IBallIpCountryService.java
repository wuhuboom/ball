package com.oxo.ball.service.admin;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oxo.ball.bean.dao.BallIpCountry;
import com.oxo.ball.bean.dto.resp.SearchResponse;

import java.util.List;

/**
 * <p>
 * IP地区 服务类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
public interface IBallIpCountryService extends IService<BallIpCountry> {
    SearchResponse<BallIpCountry> search(BallIpCountry query, Integer pageNo, Integer pageSize);
    BallIpCountry insert(BallIpCountry ipCountry);
    BallIpCountry findById(Long id);
    Boolean delete(Long id);
    Boolean edit(BallIpCountry ipCountry);
    Boolean status(BallIpCountry ipCountry);
    List<BallIpCountry> findAll();
    BallIpCountry findByCountry(String country);
}
