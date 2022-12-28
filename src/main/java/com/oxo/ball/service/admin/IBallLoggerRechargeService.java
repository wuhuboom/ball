package com.oxo.ball.service.admin;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.oxo.ball.bean.dao.BallLoggerRecharge;
import com.oxo.ball.bean.dao.BallPlayer;
import com.oxo.ball.bean.dao.BallProxyLogger;
import com.oxo.ball.bean.dao.BallSystemConfig;
import com.oxo.ball.bean.dto.req.admin.QueryActivePlayerRequest;
import com.oxo.ball.bean.dto.req.player.RechargeLogRequest;
import com.oxo.ball.bean.dto.req.report.ReportDataRequest;
import com.oxo.ball.bean.dto.req.report.ReportRewiRequest;
import com.oxo.ball.bean.dto.req.report.ReportStandardRequest;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.bean.dto.resp.admin.ProxyStatis2Dto;
import com.oxo.ball.bean.dto.resp.admin.ProxyStatis3Dto;
import com.oxo.ball.bean.dto.resp.admin.ProxyStatisDto;
import com.oxo.ball.bean.dto.resp.report.RechargeWithdrawalResponse;
import com.oxo.ball.bean.dto.resp.report.ReportStandardPlayerDTO;

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
public interface IBallLoggerRechargeService extends IService<BallLoggerRecharge> {
    SearchResponse<BallLoggerRecharge> search(BallLoggerRecharge queryParam, Integer pageNo, Integer pageSize);
    SearchResponse<BallLoggerRecharge> search(BallPlayer currPlayer,RechargeLogRequest queryParam, Integer pageNo, Integer pageSize);
    BallLoggerRecharge insert(BallLoggerRecharge announcement);
    BallLoggerRecharge findById(Long id);
    boolean edit(BallLoggerRecharge loggerRecharge);
    BaseResponse editRe(BallLoggerRecharge loggerRecharge) throws JsonProcessingException;
    Long getDayOrderNo();
    void refreshStatus();

    Long statisTotal();

    BallLoggerRecharge statisTotal(ReportDataRequest reportDataRequest);

    List<Map<String,Object>> statisByType(ReportDataRequest reportDataRequest);

    SearchResponse<BallLoggerRecharge> statisReport(ReportDataRequest reportDataRequest, int pageNo, int pageSize);

    BallLoggerRecharge findByOrderNo(long orderNo);

    List<BallLoggerRecharge> statisPayCount(Long id, Long begin, Long end);

    void search(BallProxyLogger queryParam, List<Long> ids, ProxyStatisDto list1, Map<Long, ProxyStatisDto> list2Map, BallPlayer proxyUser, BallSystemConfig systemConfig);

    List<RechargeWithdrawalResponse> rechargeStatisByPayTypeList(ReportRewiRequest reportRewiRequest);

    RechargeWithdrawalResponse rechargeStatisByPayType(ReportRewiRequest reportRewiRequest);

    List<BallLoggerRecharge> search43(long timeBegin, long timeEnd);

    void search(QueryActivePlayerRequest request, BallPlayer player, Map<Long, BallPlayer> dataMap);

    void editFirst(List<Long> ids);

    SearchResponse<BallLoggerRecharge> searchFirst(BallLoggerRecharge build, int pageNo, int pageSize);

    void searchProxy2(BallProxyLogger queryParam, Map<String, ProxyStatis2Dto> dataMap, BallPlayer playerProxy, ProxyStatis2Dto total, BallSystemConfig systemConfig);

    void statis(BallProxyLogger queryParam, ProxyStatis3Dto total, ProxyStatis3Dto list, BallSystemConfig systemConfig);

    void searchStandard(ReportStandardRequest reportStandardRequest, BallPlayer playerProxy, Map<Long, ReportStandardPlayerDTO> playerMap, BallSystemConfig systemConfig);
}
