package com.oxo.ball.service.admin;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oxo.ball.bean.dao.BallAdmin;
import com.oxo.ball.bean.dao.BallBet;
import com.oxo.ball.bean.dao.BallLoggerBack;
import com.oxo.ball.bean.dao.BallPlayer;
import com.oxo.ball.bean.dto.req.report.ReportDataRequest;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;

import java.util.List;

/**
 * <p>
 * 日志表 服务类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
public interface IBallLoggerBackService extends IService<BallLoggerBack> {
    SearchResponse<BallLoggerBack> search(BallPlayer currPlayer, Integer pageNo, Integer pageSize);
    SearchResponse<BallLoggerBack> search(BallLoggerBack queryParam, Integer pageNo, Integer pageSize);
    BallLoggerBack search(BallLoggerBack loggerBack);
    SearchResponse<BallLoggerBack> search2(BallPlayer currentUser, Integer pageNo, Integer pageSize);
    BaseResponse statis(BallPlayer currPlayer);
    BallLoggerBack insert(BallLoggerBack ballLoggerBack);
    boolean edit(BallLoggerBack ballLoggerBack);
    BaseResponse draw(BallPlayer currentUser, Long id);

    BallLoggerBack statis(ReportDataRequest reportDataRequest,int type);

    void settlementOnWeek();
    SearchResponse<BallLoggerBack> searchUnSettlement(BallLoggerBack queryParam, Integer pageNo, Integer pageSize, BallAdmin currentUser);

    List<BallLoggerBack> searchUnSettlement(BallLoggerBack build, BallAdmin currentUser);

    SearchResponse<BallLoggerBack> searchUnSettlementByPlayer(BallLoggerBack queryParam, Integer pageNo, Integer pageSize, BallAdmin currentUser);

    BaseResponse settlementOnWeek(Long gameId);

    int editMult(UpdateWrapper update, BallLoggerBack build);

}
