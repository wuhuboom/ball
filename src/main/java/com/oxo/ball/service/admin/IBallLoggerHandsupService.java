package com.oxo.ball.service.admin;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oxo.ball.bean.dao.BallLoggerHandsup;
import com.oxo.ball.bean.dao.BallPlayer;
import com.oxo.ball.bean.dao.BallProxyLogger;
import com.oxo.ball.bean.dto.req.report.ReportDataRequest;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.bean.dto.resp.admin.ProxyStatisDto;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 日志表 服务类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
public interface IBallLoggerHandsupService extends IService<BallLoggerHandsup> {
    SearchResponse<BallLoggerHandsup> search(BallLoggerHandsup queryParam, Integer pageNo, Integer pageSize);
    SearchResponse<BallLoggerHandsup> searchFixed(BallLoggerHandsup queryParam, Integer pageNo, Integer pageSize);
    BallLoggerHandsup insert(BallLoggerHandsup announcement);

    Long statisUpDown();

    List<BallLoggerHandsup> statis(ReportDataRequest reportDataRequest);

    BallLoggerHandsup statisByType(ReportDataRequest reportDataRequest, int type);

    SearchResponse<BallLoggerHandsup> statisReport(ReportDataRequest reportDataRequest, int type, int pageNo, int pageSize);

    List<BallLoggerHandsup> statisPayCount(Long id, Long begin, Long end);

    BallLoggerHandsup statisRecharge(ReportDataRequest reportDataRequest);

    void search(BallProxyLogger queryParam, List<Long> ids, ProxyStatisDto list1, Map<Long, ProxyStatisDto> list2Map, BallPlayer proxyUser);

    void searchWithdrawal(BallProxyLogger queryParam, List<Long> ids, ProxyStatisDto statisDto1, Map<Long, ProxyStatisDto> list2Map, BallPlayer proxyUser);
}
