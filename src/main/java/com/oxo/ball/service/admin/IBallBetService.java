package com.oxo.ball.service.admin;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.oxo.ball.bean.dao.*;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oxo.ball.bean.dto.req.admin.QueryActivePlayerRequest;
import com.oxo.ball.bean.dto.req.report.ReportDataRequest;
import com.oxo.ball.bean.dto.req.report.ReportStandardRequest;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.bean.dto.resp.admin.ProxyStatis2Dto;
import com.oxo.ball.bean.dto.resp.admin.ProxyStatis3Dto;
import com.oxo.ball.bean.dto.resp.admin.ProxyStatisDto;
import com.oxo.ball.bean.dto.resp.report.ReportStandardPlayerDTO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 下注 服务类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
public interface IBallBetService extends IService<BallBet> {
    SearchResponse<BallBet> search(BallBet query, Integer pageNo, Integer pageSize);
    SearchResponse<BallBet> search(ReportDataRequest reportDataRequest, Integer pageNo, Integer pageSize);
    BallBet findById(Long id);
    List<BallBet> findByGameId(Long id,int status);
    Boolean edit(BallBet ballBet);

    BaseResponse undo(BallBet query, BallAdmin admin, boolean b);

    BaseResponse info(BallBet query);

    void betOpen(BallGame edit,boolean ishand);

    BaseResponse betRecount(Long id, BallAdmin currentUser);

    BaseResponse betRollback(Long id, BallAdmin currentUser);

    BaseResponse betRollbackByBetId(Long id, BallAdmin admin);

    BallBet statisNotOpen();

    BallBet statisTotal(ReportDataRequest reportDataRequest);

    List<BallBet> statisByType(ReportDataRequest reportDataRequest);

    SearchResponse<BallBet> statisReport(List<Long> gameIds,int pageNo);

    BallBet statisBetCount(Long id, Long begin, Long end);

    List<BallBet> statisNotOpenForPlayer();

    int editMult(UpdateWrapper update, BallBet build);

    void search(BallProxyLogger queryParam, List<Long> ids, ProxyStatisDto list1, Map<Long, ProxyStatisDto> list2Map, BallPlayer proxyUser);

    void search(QueryActivePlayerRequest request, BallPlayer player, Map<Long, BallPlayer> dataMap);

    BaseResponse betOpen(Long id);

    void searchProxy2(BallProxyLogger queryParam, Map<String, ProxyStatis2Dto> dataMap, BallPlayer playerProxy, ProxyStatis2Dto total);

    void statis(BallProxyLogger queryParam, ProxyStatis3Dto total, ProxyStatis3Dto data);

    void searchProxy3(BallProxyLogger queryParam, Map<String, ProxyStatis2Dto> dataMap, BallPlayer playerProxy, ProxyStatis2Dto total);

    void statis2(BallProxyLogger queryParam, ProxyStatis3Dto total, ProxyStatis3Dto data);

    void searchStandard(ReportStandardRequest reportStandardRequest, BallPlayer playerProxy, Map<Long, ReportStandardPlayerDTO> playerMap);

    SearchResponse<BallBet> searchStandard(ReportStandardRequest reportStandardRequest, int pageNo);

    BaseResponse checkRebate(String playerName);

    BaseResponse checkRebateQueue();

}
