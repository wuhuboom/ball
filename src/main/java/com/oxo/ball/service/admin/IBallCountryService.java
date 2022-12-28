package com.oxo.ball.service.admin;


import com.baomidou.mybatisplus.extension.service.IService;
import com.oxo.ball.bean.dao.BallCountry;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;

import java.util.List;

/**
 * @author flooming
 */
public interface IBallCountryService extends IService<BallCountry> {
    BallCountry findById(Long id);
    List<BallCountry> findAll();
    SearchResponse<BallCountry> search(BallCountry keyword, Integer pageNo, Integer pageSize);
    BaseResponse insert(BallCountry ballTimezone);
    Boolean delete(Long id);
    BaseResponse edit(BallCountry editBallCountry);

    List<BallCountry> findByCode(String areaCode);
}
