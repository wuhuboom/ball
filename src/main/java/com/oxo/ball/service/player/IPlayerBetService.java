package com.oxo.ball.service.player;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.oxo.ball.bean.dao.BallAdmin;
import com.oxo.ball.bean.dao.BallBet;
import com.oxo.ball.bean.dao.BallPlayer;
import com.oxo.ball.bean.dto.req.player.BetPreRequest;
import com.oxo.ball.bean.dto.req.player.BetRequest;
import com.oxo.ball.bean.dto.req.player.PlayerBetRequest;
import com.oxo.ball.bean.dto.req.report.ReportStandardRequest;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;

import java.sql.SQLException;

public interface IPlayerBetService extends IService<BallBet> {
    BaseResponse gameBet(BetRequest betRequest, BallPlayer playerId) throws SQLException, JsonProcessingException;
    SearchResponse<BallBet> search(PlayerBetRequest queryParam, BallPlayer ballPlayer, Integer pageNo, Integer pageSize) ;
    Long getDayOrderNo();
    void clearDayOrderNo();
    BaseResponse gameBetPrepare(BetPreRequest betRequest, BallPlayer currentPlayer);
    BaseResponse unbet(Long betId, BallBet bet, BallPlayer currentUser, BallAdmin ballAdmin);
    BaseResponse unbetPlayer(Long betId,BallBet bet, BallPlayer currentUser);

    BaseResponse betInfo(Long betId, BallPlayer currentUser);
    BaseResponse betToday(Integer pageNo, Integer pageSize, BallPlayer currentUser);
    boolean edit(BallBet edit);

    BaseResponse betInfoPlayer(Long betId, BallPlayer currentUser);
    int standard(ReportStandardRequest reportStandardRequest);
    int standard2(ReportStandardRequest reportStandardRequest);
}
