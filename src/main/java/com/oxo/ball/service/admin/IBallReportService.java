package com.oxo.ball.service.admin;

import com.oxo.ball.bean.dao.BallBalanceChange;
import com.oxo.ball.bean.dto.req.report.*;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.bean.dto.resp.report.ReportStandardDTO;

public interface IBallReportService {
    BaseResponse reportDataTotal();
    BaseResponse reportData(ReportDataRequest reportDataRequest);
    BaseResponse reportRechargeWithdrawal(ReportDataRequest reportDataRequest);
    BaseResponse reportGame(ReportDataRequest reportDataRequest);
    SearchResponse reportGame(ReportGameRequest reportGameRequest);
    BaseResponse reportOperate(ReportOperateRequest reportOperateRequest);
    BaseResponse reportBet(ReportDataRequest reportDataRequest);
    BaseResponse reportPlayerDay(ReportPlayerDayRequest reportDataRequest);
    BaseResponse reportBalanceChange(ReportBalanceChangeRequest queryRequest);
    BallBalanceChange reportBalanceChangeTotal(ReportBalanceChangeRequest queryRequest);
    BaseResponse reportRecharge(ReportDataRequest reportDataRequest);
    BaseResponse reportProxy();
    BaseResponse reportRechargeWay(ReportRewiRequest reportRewiRequest, Integer pageNo, Integer pageSize);

    BaseResponse reportRechargeWayAll(ReportRewiRequest reportRewiRequest);

    BaseResponse standard(ReportStandardRequest reportStandardRequest);

    SearchResponse<ReportStandardDTO> standard2(ReportStandardRequest reportStandardRequest, Integer pageNo, Integer pageSize);

    SearchResponse<ReportStandardDTO> standard3(ReportStandardRequest reportStandardRequest, Integer pageNo, Integer pageSize);
}
