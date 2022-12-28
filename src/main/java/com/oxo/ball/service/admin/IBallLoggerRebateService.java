package com.oxo.ball.service.admin;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oxo.ball.bean.dao.BallAdmin;
import com.oxo.ball.bean.dao.BallLoggerRebate;
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
public interface IBallLoggerRebateService extends IService<BallLoggerRebate> {
    SearchResponse<BallLoggerRebate> search(BallLoggerRebate queryParam, Integer pageNo, Integer pageSize, BallAdmin currentUser);
    List<BallLoggerRebate> search(BallLoggerRebate build, BallAdmin currentUser);
    SearchResponse<BallLoggerRebate> searchRecharge(BallLoggerRebate loggerRebate, Integer pageNo, Integer pageSize, BallAdmin currentUser);
    List<BallLoggerRebate> searchRecharge2(BallLoggerRebate build, BallAdmin currentUser);

    BallLoggerRebate findById(Long id);

    BallLoggerRebate insert(BallLoggerRebate loggerWithdrawal);

    Boolean edit(BallLoggerRebate loggerWithdrawal);

    BaseResponse settlement(BallLoggerRebate loggerRebate);

    int countUnDo();

    int editMult(UpdateWrapper update, BallLoggerRebate build);
    BallLoggerRebate statisDiscount(ReportDataRequest reportDataRequest);

    BallLoggerRebate findByTypeAndOrderno(Long playerId,Integer type,Long orderNo);

    void sendPlayerChat(BallLoggerRebate byId,BallPlayer player);
}
