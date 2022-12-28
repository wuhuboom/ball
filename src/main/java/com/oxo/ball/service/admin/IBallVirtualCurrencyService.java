package com.oxo.ball.service.admin;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oxo.ball.bean.dao.BallPlayer;
import com.oxo.ball.bean.dao.BallVirtualCurrency;
import com.oxo.ball.bean.dto.req.player.VirtualCurrencyDelRequest;
import com.oxo.ball.bean.dto.req.player.VirtualCurrencyEditRequest;
import com.oxo.ball.bean.dto.req.player.VirtualCurrencyRequest;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;

import java.util.List;

/**
 * <p>
 * 虚拟币 服务类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
public interface IBallVirtualCurrencyService extends IService<BallVirtualCurrency> {

    List<BallVirtualCurrency> findByPlayerId(Long playerId);
    BaseResponse insert(BallPlayer currPlayer, VirtualCurrencyRequest virtualCurrency);
    BaseResponse edit(BallPlayer currPlayer, VirtualCurrencyEditRequest virtualCurrency);
    BallVirtualCurrency findById(Long id);
    BaseResponse del(BallPlayer player, VirtualCurrencyDelRequest id);

    SearchResponse<BallVirtualCurrency> search(BallVirtualCurrency query, Integer pageNo, Integer pageSize);

    int edit(BallVirtualCurrency ballBankCard);

    Boolean status(BallVirtualCurrency ballBankCard);

    Boolean check(BallVirtualCurrency ballBankCard);
}
