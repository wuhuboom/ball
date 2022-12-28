package com.oxo.ball.service.admin;


import com.baomidou.mybatisplus.extension.service.IService;
import com.oxo.ball.bean.dao.BallTimezone;
import com.oxo.ball.bean.dao.BallTimezone;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;

import java.util.List;

/**
 * @author flooming
 */
public interface IBallTimezoneService extends IService<BallTimezone> {
    BallTimezone findById(Long id);
    BallTimezone findByStatusOn();
    SearchResponse<BallTimezone> search(BallTimezone keyword, Integer pageNo, Integer pageSize);
    BaseResponse insert(BallTimezone ballTimezone);
    Boolean delete(Long id);
    Boolean statusOn(Long id);
    BaseResponse edit(BallTimezone editBallTimezone);
}
