package com.oxo.ball.controller.admin;

import com.oxo.ball.bean.dao.*;
import com.oxo.ball.bean.dto.req.report.*;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.bean.dto.resp.report.ReportStandardDTO;
import com.oxo.ball.service.admin.*;
import com.oxo.ball.utils.BigDecimalUtil;
import com.oxo.ball.utils.PoiUtil;
import com.oxo.ball.utils.ResponseMessageUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ball/report")
public class BallReportController {
    @Autowired
    IBallReportService ballReportService;
    @Autowired
    IBallProxyLoggerService proxyLoggerService;
    @Autowired
    IBallGameReportService gameReportService;
    @Autowired
    IBallPaymentManagementService paymentManagementService;
    @Autowired
    IBallPayBehalfService payBehalfService;
    @Autowired
    PoiUtil poiUtil;
    @Autowired
    private BallAdminService ballAdminService;

    @GetMapping("data")
    public Object reportDataTotal(){
        return ballReportService.reportDataTotal();
    }
    @PostMapping("data")
    public Object reportData(@RequestBody ReportDataRequest reportDataRequest){
        return ballReportService.reportData(reportDataRequest);
    }
    @PutMapping("data")
    public Object reportRechargeWithdrawal(@RequestBody ReportDataRequest reportDataRequest){
        return ballReportService.reportRechargeWithdrawal(reportDataRequest);
    }
    @PostMapping("game")
//    public Object reportGame(@RequestBody ReportDataRequest reportDataRequest){
    public Object reportGame(@RequestBody BallGameReport ballGameReport,@RequestParam(defaultValue = "1")Integer pageNo, @RequestParam(defaultValue = "20") Integer pageSize){
//        BaseResponse data =  ballReportService.reportGame(reportDataRequest);
        SearchResponse<BallGameReport> search = gameReportService.search(ballGameReport, pageNo, pageSize);
        return BaseResponse.successWithData(search);
    }
    @PutMapping("game")
    public Object reportGameDetail(@RequestBody ReportGameRequest reportGameRequest){
        SearchResponse data =  ballReportService.reportGame(reportGameRequest);
        return BaseResponse.successWithData(data);
    }
    @RequestMapping("operate")
    public Object reportOperate(@RequestBody ReportOperateRequest reportOperateRequest){
        BaseResponse response = ballReportService.reportOperate(reportOperateRequest);
        return response;
    }
    @RequestMapping("bet")
    public Object reportBet(@RequestBody ReportDataRequest reportDataRequest){
        BaseResponse response = ballReportService.reportBet(reportDataRequest);
        return response;
    }
    @RequestMapping("player_day")
    public Object reportPlayerDay(@RequestBody ReportPlayerDayRequest reportPlayerDayRequest){
        BaseResponse response = ballReportService.reportPlayerDay(reportPlayerDayRequest);
        return response;
    }

    @PostMapping("balance_change")
    public Object reportBalanceChange(@RequestBody ReportBalanceChangeRequest queryRequest){
        BaseResponse response = ballReportService.reportBalanceChange(queryRequest);
        return response;
    }
    @GetMapping("balance_change")
    public Object reportBalanceChangeTotal(ReportBalanceChangeRequest queryRequest){
        BallBalanceChange response = ballReportService.reportBalanceChangeTotal(queryRequest);
        return BaseResponse.successWithData(response);
    }
    @PostMapping("recharge")
    public Object reportRecharge(@RequestBody ReportDataRequest reportDataRequest){
        BaseResponse response = ballReportService.reportRecharge(reportDataRequest);
        return response;
    }
    @RequestMapping("proxy")
    public Object reportProxy(BallProxyLogger queryParam,@RequestParam(defaultValue = "1")Integer pageNo, @RequestParam(defaultValue = "20") Integer pageSize){
//        SearchResponse<BallProxyLogger> search = proxyLoggerService.search(queryParam, pageNo, pageSize);
        BaseResponse response = proxyLoggerService.statis(queryParam);
        return response;
    }
    @RequestMapping("proxy2")
    public Object reportProxy2(BallProxyLogger queryParam, HttpServletRequest request){
        BallAdmin admin = ballAdminService.getCurrentUser(request.getHeader("token"));
        if(!StringUtils.isBlank(admin.getPlayerName())){
            queryParam.setPlayerName(admin.getPlayerName());
        }
        BaseResponse response = null;
        try {
            //今日全部
            if(StringUtils.isBlank(queryParam.getPlayerName())&&StringUtils.isBlank(queryParam.getBegin())
                    &&StringUtils.isBlank(queryParam.getEnd())){
                response = proxyLoggerService.statis2();
            }else if(!StringUtils.isBlank(queryParam.getPlayerName())){
                response = proxyLoggerService.statis2(queryParam);
            }else{
                response = proxyLoggerService.statis2_1(queryParam);
            }
        } catch (ParseException e) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e8"));
        } catch (Exception e){
            e.printStackTrace();
        }
        return response;
    }
    @GetMapping("recharge_way")
    public Object reportRechargeWay(ReportRewiRequest reportRewiRequest,@RequestParam(defaultValue = "1")Integer pageNo, @RequestParam(defaultValue = "20") Integer pageSize){
       BaseResponse response =  ballReportService.reportRechargeWay(reportRewiRequest,pageNo,pageSize);
        return response;
    }
    @PostMapping("recharge_way")
    public Object reportRechargeWayAll(ReportRewiRequest reportRewiRequest){
       BaseResponse response =  ballReportService.reportRechargeWayAll(reportRewiRequest);
        return response;
    }
    @DeleteMapping("recharge_way")
    public Object rechargesAll(){
        List<BallPaymentManagement> byAll = paymentManagementService.findByAllTrue();
        BaseResponse response = BaseResponse.successWithData(byAll);
        return response;
    }
    @PutMapping("recharge_way")
    public Object behalfsAll(){
        BaseResponse response = BaseResponse.successWithData(payBehalfService.findByAll());
        return response;
    }
    @PostMapping("standard")
    public Object standard(ReportStandardRequest reportStandardRequest,@RequestParam(defaultValue = "1")Integer pageNo, @RequestParam(defaultValue = "10") Integer pageSize){
        if(reportStandardRequest.getTime()==7&&StringUtils.isBlank(reportStandardRequest.getBegin())) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e9"));
        }
        if(reportStandardRequest.getRecharge()==null){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e10"));
        }
        if(reportStandardRequest.getBets()==null){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e11"));
        }
        if(reportStandardRequest.getDays()==null){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e12"));
        }
        reportStandardRequest.setRecharge(reportStandardRequest.getRecharge()*BigDecimalUtil.PLAYER_MONEY_UNIT);
        reportStandardRequest.setBets(reportStandardRequest.getBets()*BigDecimalUtil.PLAYER_MONEY_UNIT);
        SearchResponse<ReportStandardDTO> response = ballReportService.standard3(reportStandardRequest,pageNo,pageSize);
        return BaseResponse.successWithData(response);
    }
    @PutMapping("standard")
    public Object standardExport(ReportStandardRequest reportStandardRequest){
        if(reportStandardRequest.getTime()==7&&StringUtils.isBlank(reportStandardRequest.getBegin())) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e9"));
        }
        if(reportStandardRequest.getRecharge()==null){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e10"));
        }
        if(reportStandardRequest.getBets()==null){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e11"));
        }
        if(StringUtils.isBlank(reportStandardRequest.getPlayerName())){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e13"));
        }
        reportStandardRequest.setRecharge(reportStandardRequest.getRecharge()*BigDecimalUtil.PLAYER_MONEY_UNIT);
        reportStandardRequest.setBets(reportStandardRequest.getBets()*BigDecimalUtil.PLAYER_MONEY_UNIT);
        BaseResponse response = poiUtil.exportStandardExport(reportStandardRequest);
        return response;
    }
}
