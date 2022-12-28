package com.oxo.ball.service.admin;

import com.oxo.ball.bean.dao.BallBalanceChange;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oxo.ball.bean.dao.BallPlayer;
import com.oxo.ball.bean.dao.BallProxyLogger;
import com.oxo.ball.bean.dto.req.player.BalanceChangeRequest;
import com.oxo.ball.bean.dto.req.player.DataCenterRequest;
import com.oxo.ball.bean.dto.req.report.ReportBalanceChangeRequest;
import com.oxo.ball.bean.dto.req.report.ReportDataRequest;
import com.oxo.ball.bean.dto.req.report.ReportOperateRequest;
import com.oxo.ball.bean.dto.req.report.ReportPlayerDayRequest;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.bean.dto.resp.admin.ProxyStatisDto;
import com.oxo.ball.bean.dto.resp.player.ReportFormResponse;
import com.oxo.ball.bean.dto.resp.player.ReportFormTeamResponse;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 账变表 服务类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
public interface IBallBalanceChangeService extends IService<BallBalanceChange> {

    SearchResponse<BallBalanceChange> search(BallPlayer currentUser, BalanceChangeRequest balanceChangeRequest, Integer pageNo, Integer pageSize);
    SearchResponse<BallBalanceChange> search(BallBalanceChange queryParam, Integer pageNo, Integer pageSize);
    BallBalanceChange searchTotal(BallBalanceChange queryParam, Integer pageNo, Integer pageSize);
    SearchResponse<BallBalanceChange> search(ReportPlayerDayRequest reportDataRequest, Integer pageNo, Integer pageSize);
    SearchResponse<BallBalanceChange> search(ReportBalanceChangeRequest queryRequest);
    BallBalanceChange searchTotal(ReportBalanceChangeRequest queryRequest);
    boolean insert(BallBalanceChange balanceChange);

    List<ReportFormResponse> reportForm(BallPlayer player, DataCenterRequest dataCenterRequest);

    List<ReportFormTeamResponse> reportFormTeam(BallPlayer player, DataCenterRequest dataCenterRequest);

    SearchResponse<BallBalanceChange> statisReport(ReportOperateRequest reportOperateRequest);

    Long statisSubRebate(Long id, Long begin, Long end);

    Integer statisPayCount(Long id, Long begin, Long end);

    BallBalanceChange findByOrderId(Integer type,Long orderNo);

    Boolean edit(BallBalanceChange balanceChange);

    BallBalanceChange statisDiscount(ReportDataRequest reportDataRequest);

    void search(BallProxyLogger queryParam, ProxyStatisDto statisDto1, Map<Long, ProxyStatisDto> list2Map, BallPlayer proxyUser);

    BallBalanceChange findLastByType(Long playerId,Integer type);
}
