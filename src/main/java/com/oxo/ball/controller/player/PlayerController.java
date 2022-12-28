package com.oxo.ball.controller.player;

import com.google.common.collect.Lists;
import com.oxo.ball.auth.PlayerDisabledException;
import com.oxo.ball.auth.TokenInvalidedException;
import com.oxo.ball.bean.dao.*;
import com.oxo.ball.bean.dto.req.AuthEditPwdRequest;
import com.oxo.ball.bean.dto.req.admin.RateConfigs;
import com.oxo.ball.bean.dto.req.player.*;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.config.SomeConfig;
import com.oxo.ball.contant.LogsContant;
import com.oxo.ball.service.IBasePlayerService;
import com.oxo.ball.service.admin.IBallBalanceChangeService;
import com.oxo.ball.service.admin.IBallLoggerBackService;
import com.oxo.ball.service.admin.IBallPaymentManagementService;
import com.oxo.ball.service.admin.IBallSystemConfigService;
import com.oxo.ball.service.player.AuthPlayerService;
import com.oxo.ball.service.player.IPlayerBetService;
import com.oxo.ball.service.player.IPlayerService;
import com.oxo.ball.utils.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.undertow.util.StatusCodes;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;


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
@Api(tags = "玩家 - 个人数据")
public class PlayerController {
    private Logger apiLog = LoggerFactory.getLogger(LogsContant.API_LOG);

    @Resource
    IPlayerService playerService;
    @Resource
    AuthPlayerService authPlayerService;
    @Resource
    IBasePlayerService basePlayerService;
    @Resource
    IBallBalanceChangeService ballBalanceChangeService;
    @Resource
    IPlayerBetService betService;
    @Autowired
    IBallLoggerBackService loggerBackService;
    @Autowired
    SomeConfig someConfig;
    @Autowired
    IBallSystemConfigService systemConfigService;
    @Autowired
    IBallPaymentManagementService paymentManagementService;
    @ApiOperation(
            value = "个人资料",
            notes = "个人资料" ,
            httpMethod = "GET")
    @GetMapping("player_info")
    public Object getPlayerInfo(HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
        BallPlayer player = playerService.getCurrentUser(request);
        BallPlayer playerDb = basePlayerService.findByIdNoCache(player.getId());
        if(!playerDb.getVersion().equals(player.getVersion())){
            apiLog.warn("redis中的玩家信息与数据库不对应");
        }
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        double div1 = BigDecimalUtil.div(playerDb.getBalance(), Double.valueOf(systemConfig.getUsdtWithdrawPer()), 2);
        playerDb.setCurrRate(""+BigDecimalUtil.div(div1,100,2));
        playerDb.setCurrencySymbol("USDT");
        playerDb.setCurrencyName("USDT");
        try {
            List<RateConfigs> rateConfigs = JsonUtil.fromJsonToList(systemConfig.getEuroRate(), RateConfigs.class);
            for(RateConfigs item:rateConfigs){
                String[] split = item.getAreaCode().split(",");
                List<String> strings = Arrays.asList(split);
                if(strings.contains(player.getAreaCode())){
                    div1 = BigDecimalUtil.div(playerDb.getBalance(), Double.valueOf(item.getRate()), 2);
                    playerDb.setCurrRate(""+BigDecimalUtil.div(div1,100,2));
                    playerDb.setCurrencySymbol(item.getSymbol());
                    playerDb.setCurrencyName(item.getName());
                    break;
                }
            }
        } catch (IOException e) {
        } catch (Exception e){
        }
        return BaseResponse.successWithData(playerDb);
    }

    @ApiOperation(
            value = "余额变动记录",
            notes = "余额变动记录" ,
            httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type",value = "1充值 2提现 3投注 4赢 5佣金 6人工 7撤单"),
            @ApiImplicitParam(name = "typeb",value = "1.收入,2.支出"),
            @ApiImplicitParam(name = "time",value = "1.今日,2.昨日,3.近7日"),
            @ApiImplicitParam(name = "pageNo",value = "页码"),
            @ApiImplicitParam(name = "pageSize",value = "数量")
    })
    @PostMapping("balance_change")
    public Object index(@Validated BalanceChangeRequest balanceChangeRequest,
                        @RequestParam(defaultValue = "1")Integer pageNo,
                        @RequestParam(defaultValue = "20") Integer pageSize,
                        HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
        BallPlayer currentUser = playerService.getCurrentUser(request);
        SearchResponse<BallBalanceChange> search = ballBalanceChangeService.search(currentUser,balanceChangeRequest, pageNo, pageSize);
        return BaseResponse.successWithData(search);
    }

    @ApiOperation(
            value = "订单中心",
            notes = "订单中心" ,
            httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "time",value = "1今天，2昨天，3近7日，4近10日，5近30日",required = false),
            @ApiImplicitParam(name = "type",value = "1查全部 2查反波",required = false),
            @ApiImplicitParam(name = "pageNo",value = "页码"),
            @ApiImplicitParam(name = "pageSize",value = "数量")
    })
    @PostMapping("bets")
    public Object bets(@Validated PlayerBetRequest query,
                        @RequestParam(defaultValue = "1")Integer pageNo,
                        @RequestParam(defaultValue = "20") Integer pageSize,
                        HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
        BallPlayer currentUser = playerService.getCurrentUser(request);
        SearchResponse<BallBet> search = betService.search(query,currentUser, pageNo, pageSize);
        return BaseResponse.successWithData(search);
    }
    @ApiOperation(
            value = "订单中心-今日订单",
            notes = "订单中心-今日订单" ,
            httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNo",value = "页码"),
            @ApiImplicitParam(name = "pageSize",value = "数量")
    })
    @PostMapping("bets/today")
    public Object betsDetail(@RequestParam(defaultValue = "1")Integer pageNo,
                             @RequestParam(defaultValue = "20") Integer pageSize,HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
        BallPlayer currentUser = playerService.getCurrentUser(request);
        BaseResponse search = betService.betToday(pageNo,pageSize, currentUser);
        return search;
    }
    @ApiOperation(
            value = "订单中心-撤消",
            notes = "订单中心-撤消" ,
            httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "betId",value = "订单ID",required = true)
    })
    @GetMapping("unbet")
    public Object unbet(Long betId,
                        HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
        BallPlayer currentUser = playerService.getCurrentUser(request);
        BaseResponse response = betService.unbetPlayer(betId,null,currentUser);
        return response;
    }
    @ApiOperation(
            value = "订单中心-详情",
            notes = "订单中心-详情" ,
            httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "betId",value = "订单ID",required = true)
    })
    @GetMapping("betInfo")
    public Object betInfo(Long betId,
                        HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
        BallPlayer currentUser = playerService.getCurrentUser(request);
        BaseResponse response = betService.betInfoPlayer(betId,currentUser);
        return response;
    }

    @ApiOperation(
            value = "充值准备",
            notes = "充值准备" ,
            httpMethod = "GET")
    @GetMapping("recharge_pre")
    public Object rechargePre(HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
        BallPlayer currentUser = playerService.getCurrentUser(request);
        currentUser.setLoginContry(GeoLiteUtil.getIpAddr(IpUtil.getIpAddress(request)));
        List<BallPaymentManagement> list = paymentManagementService.findByAll(currentUser);
        List<Map<String,Object>> payWays = new ArrayList<>();
//        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        for(BallPaymentManagement item:list){
            Map<String,Object> pay1 = new HashMap<>();
            pay1.put("id",item.getId());
            pay1.put("name",item.getName());
            pay1.put("img",item.getImg());
            pay1.put("type",item.getPayType());
            if(item.getPayType()!=1){
                pay1.put("fast",item.getFastMoney());
            }else{
                //TODO 汇率
//                pay1.put("rate",BigDecimalUtil.div(systemConfig.getUsdtWithdrawPer(),100,2));
            }
            pay1.put("rate",item.getRate());
            pay1.put("minMax",item.getMinMax());
            pay1.put("unhold",item.getUnhold());
            pay1.put("unholdMsg",StringUtils.isBlank(item.getUnholdMessage())?"invalid":item.getUnholdMessage());
            pay1.put("currencySymbol",item.getCurrencySymbol());
            payWays.add(pay1);
        }
//        HashMap<String, Object> pay2 = new HashMap<>();
//        pay2.put("id","2");
//        pay2.put("name","USDT");
//        pay2.put("img","/recharge/virtual.png");
//        pay2.put("type","2");
//        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
//        pay2.put("rate",BigDecimalUtil.div(systemConfig.getUsdtWithdrawPer(),100,2));
//        pay2.put("minMax","200-10000000");
//        payWays.add(pay2);
        Collections.shuffle(payWays,TimeUtil.random);
        return BaseResponse.successWithData(payWays);
    }
    @ApiOperation(
            value = "充值",
            notes = "充值" ,
            httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "money",value = "充值金额",required = true),
            @ApiImplicitParam(name = "payId",value = "充值类型ID",required = true),
    })
    @PostMapping("recharge")
    public Object recharge(Double money,Long payId,
                        HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
        BallPlayer currentUser = playerService.getCurrentUser(request);
        String ipAddress = IpUtil.getIpAddress(request);
        currentUser.setTheNewIp(ipAddress);
        currentUser.setLoginContry(GeoLiteUtil.getIpAddr(ipAddress));
        BaseResponse response = playerService.rechargePre(currentUser, money,payId);
        return response;
    }
//    @ApiOperation(
//            value = "取消充值",
//            notes = "取消充值" ,
//            httpMethod = "GET")
//    @GetMapping("recharge/cancel")
//    public Object rechargeCancel(HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
//        BallPlayer currentUser = playerService.getCurrentUser(request);
//        BaseResponse response = playerService.rechargeCancel(currentUser);
//        return response;
//    }
    @ApiOperation(
            value = "提现准备",
            notes = "everydayWithdrawTimes:最大提现次数,everydayWithdrawFree:免手续费次数" +
                    "withdrawalToday:今日提现次数\n"+
                    "withdrawalRate:手续费," +
                    "withdrawMax:最大提现金额,withdrawMin:最小提现金额" +
                    "withdrawalRateMax:最大提现手续费,withdrawalRateMin:最小提现手续费\n"+
                    "usdtWithdrawPer:usdt汇率,usdtWithdrawalRate:usdt手续费,usdtWithdrawMax:usdt最大提现金额,usdtWithdrawMin:usdt最小提现金额" +
                    "usdtWithdrawalRateMax:usdt最大提现手续费,usdtWithdrawalRateMin:usdt最小提现手续费\n",
            httpMethod = "GET")
    @GetMapping("withdrawal_pre")
    public Object withdrawalPre(HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
        BallPlayer currentUser = playerService.getCurrentUser(request);
        currentUser.setLoginContry(GeoLiteUtil.getIpAddr(IpUtil.getIpAddress(request)));
        BaseResponse response = playerService.withdrawalPre(currentUser);
        return response;
    }
    @ApiOperation(
            value = "提现",
            notes = "提现" ,
            httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type",value = "提现方式,1.银行卡,2.USDT,3.SIM",required = true),
            @ApiImplicitParam(name = "money",value = "提现金额",required = true),
            @ApiImplicitParam(name = "payPwd",value = "支付密码",required = true),
            @ApiImplicitParam(name = "code",value = "短信验证码",required = true)
    })
    @PostMapping("withdrawal")
    public Object withdrawal(@Validated WithdrawalRequest withdrawalRequest,
                        HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
        BallPlayer currentUser = playerService.getCurrentUser(request);
        String ipAddress = IpUtil.getIpAddress(request);
        String ipAddr = GeoLiteUtil.getIpAddr(ipAddress);
        withdrawalRequest.setIpAddr(ipAddress+"|"+ipAddr);
        BaseResponse response = playerService.withdrawal(currentUser, withdrawalRequest);
        return response;
    }

    @ApiOperation(
            value = "提现密码是否设置",
            notes = "提现密码是否设置",
            httpMethod = "GET")
    @GetMapping("/getPwdPay")
    public BaseResponse getPwdPay(HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
        BallPlayer player = playerService.getCurrentUser(request);
        Map<String,Object> data = new HashMap<>();
        data.put("paySet",StringUtils.isBlank(player.getPayPassword())?2:1);
        return BaseResponse.successWithData(data);
    }

    @ApiOperation(
            value = "修改提现密码",
            notes = "修改提现密码",
            httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "origin",value = "原密码",required = true),
            @ApiImplicitParam(name = "newpwd",value = "新密码",required = true),
            @ApiImplicitParam(name = "confirmed",value = "再次密码",required = true),
            @ApiImplicitParam(name = "code",value = "短信验证码",required = true),
    })
    @PostMapping("/editPwdPay")
    public BaseResponse editPwdPay(@Validated AuthEditPwdRequest req, HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
        BallPlayer player = playerService.getCurrentUser(request);
        BaseResponse response = playerService.editPwdPay(req,player);
        return response;
    }
    @ApiOperation(
            value = "设置提现密码",
            notes = "设置提现密码",
            httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "payPwd",value = "密码",required = true),
            @ApiImplicitParam(name = "payPwdAgain",value = "再次密码",required = true),
    })
    @PostMapping("/setPwdPay")
    public BaseResponse setPwdPay(@Validated AuthSetPayPwdRequest req, HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
        BallPlayer player = playerService.getCurrentUser(request);

        if(!req.getPayPwdAgain().equals(req.getPayPwd())) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("confirmed","confirmedError"));
        }

        BallPlayer edit = BallPlayer.builder()
                .payPassword(PasswordUtil.genPasswordMd5(req.getPayPwd()))
                .build();
        edit.setId(player.getId());
        if(!basePlayerService.editAndClearCache(edit, player)) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("","updateFailed"));
        }
        return new BaseResponse(StatusCodes.OK, "edit success");
    }

    @ApiOperation(
            value = "数据中心",
            notes = "数据中心,totalBalance:团队余额, playerCount:团队人数, newPlayer:新增注册, totalWithdrawal:团队提现, totalRecharge:团队充值, totalBetPlayer:投注人数, netProfit:净利润, cumulativeActivity:活动奖励, playerOffline:未上线, cumulativeWinning:中奖总额, playerActive:活跃人数, totalBetBalance:投注金额, cumulativeDiscount:优惠金额",
            httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username",value = "用户名"),
            @ApiImplicitParam(name = "time",value = "1.今日,2昨日,3.七日,4.10日,5.30日"),
    })
    @PostMapping("/data_center")
    public BaseResponse dataCenter(@Validated DataCenterRequest dataCenterRequest, HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
        BallPlayer player = playerService.getCurrentUser(request);
        Map<String,Object> data = playerService.dataCenter(player,dataCenterRequest);
        return BaseResponse.successWithData(data);
    }
    @ApiOperation(
            value = "数据中心-下级详情",
            notes = "数据中心-下级详情,recharge:充值, levelType:层级, newPlayer:新增, cumulativeReflect:提现",
            httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "time",value = "1.今日,2昨日,3.七日,4.10日,5.30日",required = true),
    })
    @PostMapping("/data_center/detail")
    public BaseResponse dataCenterDetail(@Validated DataCenterRequest dataCenterRequest, HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
        BallPlayer player = playerService.getCurrentUser(request);
        Collection<Map<String, Object>> data = playerService.dataCenterDetail(player, dataCenterRequest);
        return BaseResponse.successWithData(data);
    }
    @ApiOperation(
            value = "返佣中心-统计",
            notes = "返佣中心-统计",
            httpMethod = "GET")
    @GetMapping("/rebate_statis")
    public BaseResponse rebateStatis( HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
        BallPlayer player = playerService.getCurrentUser(request);
        BaseResponse data = loggerBackService.statis(player);
        return data;
    }
    @ApiOperation(
            value = "返佣中心-列表",
            notes = "返佣中心-列表,type:1下注返佣 2盈利返佣 3充值返佣,status:1未领取 2已领取" ,
            httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNo",value = "页码"),
            @ApiImplicitParam(name = "pageSize",value = "数量")
    })
    @PostMapping("rebate_list")
    public Object rebateList(
                        @RequestParam(defaultValue = "1")Integer pageNo,
                        @RequestParam(defaultValue = "20") Integer pageSize,
                        HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
        BallPlayer currentUser = playerService.getCurrentUser(request);
        SearchResponse<BallLoggerBack> search = loggerBackService.search2(currentUser, pageNo, pageSize);
        return BaseResponse.successWithData(search);
    }
    @ApiOperation(
            value = "返佣中心-提取",
            notes = "返佣中心-提取" ,
            httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "数据ID",required = true)
    })
    @PostMapping("rebate_draw")
    public Object rebateList(
                        Long id,
                        HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
        BallPlayer currentUser = playerService.getCurrentUser(request);
        BaseResponse res = loggerBackService.draw(currentUser,id);
        return res;
    }

//    @ApiOperation(
//            value = "邀请中心",
//            notes = "邀请中心-邀请链接",
//            httpMethod = "GET")
//    @GetMapping("/invitation_link")
//    public BaseResponse invitationLink( HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
//        BallPlayer player = playerService.getCurrentUser(request);
//        Map<String,Object> data = new HashMap<>();
//        data.put("link",someConfig.getInvitationUrl()+player.getInvitationCode());
//        return BaseResponse.successWithData(data);
//    }

//    @ApiOperation(
//            value = "银行卡",
//            notes = "余额变动记录" ,
//            httpMethod = "POST")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "type",value = "1充值 2提现 3投注 4赢 5佣金 6人工"),
//            @ApiImplicitParam(name = "pageNo",value = "页码"),
//            @ApiImplicitParam(name = "pageSize",value = "数量")
//    })
//    @PostMapping("balance_change")
//    public Object index(BalanceChangeRequest balanceChangeRequest,
//                        @RequestParam(defaultValue = "1")Integer pageNo,
//                        @RequestParam(defaultValue = "20") Integer pageSize,
//                        HttpServletRequest request) throws TokenInvalidedException {
//        BallPlayer currentUser = playerService.getCurrentUser(request);
//        SearchResponse<BallBalanceChange> search = ballBalanceChangeService.search(currentUser,balanceChangeRequest, pageNo, pageSize);
//        return BaseResponse.successWithData(search);
//    }
}
