package com.oxo.ball.controller.player;

import com.oxo.ball.auth.PlayerDisabledException;
import com.oxo.ball.auth.TokenInvalidedException;
import com.oxo.ball.bean.dao.*;
import com.oxo.ball.bean.dto.req.AuthEditPwdRequest;
import com.oxo.ball.bean.dto.req.player.*;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.bean.dto.resp.player.ReportFormResponse;
import com.oxo.ball.bean.dto.resp.player.ReportFormTeamResponse;
import com.oxo.ball.config.SomeConfig;
import com.oxo.ball.service.IBasePlayerService;
import com.oxo.ball.service.admin.IBallBalanceChangeService;
import com.oxo.ball.service.admin.IBallLoggerBackService;
import com.oxo.ball.service.admin.IBallLoggerRechargeService;
import com.oxo.ball.service.admin.IBallLoggerWithdrawalService;
import com.oxo.ball.service.player.AuthPlayerService;
import com.oxo.ball.service.player.IPlayerBetService;
import com.oxo.ball.service.player.IPlayerService;
import com.oxo.ball.utils.PasswordUtil;
import com.oxo.ball.utils.ResponseMessageUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.undertow.util.StatusCodes;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * <p>
 * 玩家账号 前端控制器
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@RestController
@RequestMapping("/player")
@Api(tags = "玩家 - 个人报表")
public class PlayerReportFormController {

    @Resource
    IPlayerService playerService;
    @Resource
    IBallBalanceChangeService ballBalanceChangeService;
    @Autowired
    IBallLoggerRechargeService loggerRechargeService;
    @Autowired
    IBallLoggerWithdrawalService loggerWithdrawalService;
    @ApiOperation(
            value = "个人报表",
            notes = "个人报表 recharge:充值, withdrawal:提现, bet:下注, bingo:中奖, rebate:佣金, activity:活动" ,
            httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "time",value = "1.今日,2.昨日,3.近7日，4.10日,5.30日",required = true)
    })
    @PostMapping("report_form")
    public Object reportForm(@Validated DataCenterRequest dataCenterRequest,HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
        BallPlayer player = playerService.getCurrentUser(request);
        List<ReportFormResponse> response = ballBalanceChangeService.reportForm(player,dataCenterRequest);
        return BaseResponse.successWithData(response);
    }
    @ApiOperation(
            value = "团队报表",
            notes = "团队报表 bet:下注, bingo:中奖, activity:活动, recharge:充值, withdrawal:提现, winLose:输赢, betCount:下注人数" ,
            httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "time",value = "1.今日,2.昨日,3.近7日，4.10日,5.30日",required = true)
    })
    @PostMapping("report_form_team")
    public Object reportFormTeam(@Validated DataCenterRequest dataCenterRequest,HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
        BallPlayer player = playerService.getCurrentUser(request);
        List<ReportFormTeamResponse> response = ballBalanceChangeService.reportFormTeam(player,dataCenterRequest);
        return BaseResponse.successWithData(response);
    }

    @ApiOperation(
            value = "用户列表",
            notes = "用户列表" ,
            httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "time",value = "1.全部，2.3天未登录，3.7天未登录"),
            @ApiImplicitParam(name = "username",value = "用户名"),
            @ApiImplicitParam(name = "pageNo",value = "页码"),
            @ApiImplicitParam(name = "pageSize",value = "数量")
    })
    @PostMapping("sub_players")
    public Object bets(@Validated SubPlayersRequest query,
                       @RequestParam(defaultValue = "1")Integer pageNo,
                       @RequestParam(defaultValue = "20") Integer pageSize,
                       HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
        BallPlayer currentUser = playerService.getCurrentUser(request);
        SearchResponse<BallPlayer> search = playerService.searchSub(query,currentUser, pageNo, pageSize);
        return BaseResponse.successWithData(search);
    }


    @ApiOperation(
            value = "充值记录",
            notes = "充值记录" ,
            httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "time",value = "1今日 2昨日 3.7日 4.10日 5.30日"),
            @ApiImplicitParam(name = "status",value = " 1待付款/2已到账/3已上分/4支付超时"),
            @ApiImplicitParam(name = "type",value = "1.线下,2.线上"),
            @ApiImplicitParam(name = "pageNo",value = "页码"),
            @ApiImplicitParam(name = "pageSize",value = "数量")
    })
    @PostMapping("recharge_log")
    public Object index(@Validated RechargeLogRequest rechargeLogRequest,
                        @RequestParam(defaultValue = "1")Integer pageNo,
                        @RequestParam(defaultValue = "20") Integer pageSize,
                        HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
        BallPlayer currentUser = playerService.getCurrentUser(request);
        SearchResponse<BallLoggerRecharge> search = loggerRechargeService.search(currentUser,rechargeLogRequest, pageNo, pageSize);
        return BaseResponse.successWithData(search);
    }

    @ApiOperation(
            value = "提现记录",
            notes = "提现记录" ,
            httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "time",value = "1今日 2昨日 3.7日 4.10日 5.30日"),
            @ApiImplicitParam(name = "status",value = "1待审核2已审核3失败4提现成功5代付中"),
            @ApiImplicitParam(name = "pageNo",value = "页码"),
            @ApiImplicitParam(name = "pageSize",value = "数量")
    })
    @PostMapping("withdrawal_log")
    public Object index(@Validated WithdrawalLogRequest rechargeLogRequest,
                        @RequestParam(defaultValue = "1")Integer pageNo,
                        @RequestParam(defaultValue = "20") Integer pageSize,
                        HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
        BallPlayer currentUser = playerService.getCurrentUser(request);
        SearchResponse<BallLoggerWithdrawal> search = loggerWithdrawalService.search(currentUser,rechargeLogRequest, pageNo, pageSize);
        return BaseResponse.successWithData(search);
    }
}
