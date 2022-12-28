package com.oxo.ball.service.admin;

import com.oxo.ball.bean.dao.BallVip;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-24
 */
public interface IBallVipService extends IService<BallVip> {
    SearchResponse<BallVip> search(BallVip query, Integer pageNo, Integer pageSize);
    BallVip findById(Long id);
    List<BallVip> findByAll();
    BallVip insert(BallVip ballVip);
    Boolean delete(Long id);
    BaseResponse edit(BallVip ballVip);
    Boolean status(BallVip ballVip);

    BallVip findByLevel(Integer vipLevel);

    void checkLevel();
}
