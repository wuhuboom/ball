package com.oxo.ball.service.admin;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oxo.ball.bean.dao.*;
import com.oxo.ball.bean.dto.req.admin.QueryActivePlayerRequest;
import com.oxo.ball.bean.dto.req.player.WithdrawalLogRequest;
import com.oxo.ball.bean.dto.req.report.ReportDataRequest;
import com.oxo.ball.bean.dto.req.report.ReportRewiRequest;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.bean.dto.resp.admin.ProxyStatis2Dto;
import com.oxo.ball.bean.dto.resp.admin.ProxyStatis3Dto;
import com.oxo.ball.bean.dto.resp.admin.ProxyStatisDto;
import com.oxo.ball.bean.dto.resp.report.RechargeWithdrawalResponse;

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
public interface IBallLoggerWithdrawalService extends IService<BallLoggerWithdrawal> {
    SearchResponse<BallLoggerWithdrawal> search(BallLoggerWithdrawal queryParam, Integer pageNo, Integer pageSize);
    SearchResponse<BallLoggerWithdrawal> search2(BallLoggerWithdrawal queryParam, Integer pageNo, Integer pageSize);
    SearchResponse<BallLoggerWithdrawal> search(BallPlayer currPlayer, WithdrawalLogRequest queryParam, Integer pageNo, Integer pageSize);
    BallLoggerWithdrawal findById(Long id);
    BallLoggerWithdrawal insert(BallLoggerWithdrawal loggerWithdrawal);
    Boolean edit(BallLoggerWithdrawal loggerWithdrawal);
    Long getDayOrderNo();
    Integer todayCount(Long playerId);
    BaseResponse check(BallLoggerWithdrawal query, BallAdmin admin);

    Long statisTotal();

    BallLoggerWithdrawal statisTotal(ReportDataRequest reportDataRequest);

    List<Map<String,Object>> statisByType(ReportDataRequest reportDataRequest);

    SearchResponse<BallLoggerWithdrawal> statisReport(ReportDataRequest reportDataRequest, int pageNo, int pageSize);

    List<BallLoggerWithdrawal> statisPayCount(Long id, Long begin, Long end);

    BallLoggerWithdrawal findByOrderNo(String order_no);

    Long statisTotalNot();

    List<BallLoggerWithdrawal> statisTotalNotForPlayer();

    void search(BallProxyLogger queryParam, List<Long> ids, ProxyStatisDto list1, Map<Long, ProxyStatisDto> list2Map, BallPlayer proxyUser, BallSystemConfig systemConfig);

    BallLoggerWithdrawal searchStatis(BallLoggerWithdrawal query);

    List<RechargeWithdrawalResponse> rechargeStatisByPayTypeList(ReportRewiRequest reportRewiRequest);

    RechargeWithdrawalResponse rechargeStatisByPayType(ReportRewiRequest reportRewiRequest);

    void search(QueryActivePlayerRequest request, BallPlayer player, Map<Long, BallPlayer> dataMap);

    void searchProxy2(BallProxyLogger queryParam, Map<String, ProxyStatis2Dto> dataMap, BallPlayer playerProxy, ProxyStatis2Dto total, BallSystemConfig systemConfig);

    void statis(BallProxyLogger queryParam, ProxyStatis3Dto total, ProxyStatis3Dto data, BallSystemConfig systemConfig);

    Boolean findByCardNumber(String cardNumber, Long id);

    BallLoggerWithdrawal findLast(BallLoggerWithdrawal id);

    void autoCheck();
}
