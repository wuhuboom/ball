package com.oxo.ball.service.admin;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oxo.ball.bean.dao.BallPlayer;
import com.oxo.ball.bean.dao.BallSimCurrency;
import com.oxo.ball.bean.dto.req.player.SimCurrencyDelRequest;
import com.oxo.ball.bean.dto.req.player.SimCurrencyRequest;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;

/**
 * <p>
 * 虚拟币 服务类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
public interface IBallSimCurrencyService extends IService<BallSimCurrency> {

    BallSimCurrency findByPlayerId(Long playerId);
    BaseResponse insert(BallPlayer currPlayer, SimCurrencyRequest simCurrencyRequest);
    BaseResponse edit(BallPlayer currPlayer, SimCurrencyRequest simCurrencyRequest);
    BallSimCurrency findById(Long id);
    BaseResponse del(BallPlayer player, SimCurrencyDelRequest id);

    SearchResponse<BallSimCurrency> search(BallSimCurrency query, Integer pageNo, Integer pageSize);

    int edit(BallSimCurrency simCurrency);

    Boolean status(BallSimCurrency simCurrency);

    Boolean check(BallSimCurrency simCurrency);
}
