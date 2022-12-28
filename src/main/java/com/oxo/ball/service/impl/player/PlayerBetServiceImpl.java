package com.oxo.ball.service.impl.player;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.oxo.ball.bean.dao.*;
import com.oxo.ball.bean.dto.queue.MessageQueueBet;
import com.oxo.ball.bean.dto.queue.MessageQueueDTO;
import com.oxo.ball.bean.dto.req.player.BetPreRequest;
import com.oxo.ball.bean.dto.req.player.BetRequest;
import com.oxo.ball.bean.dto.req.player.PlayerBetRequest;
import com.oxo.ball.bean.dto.req.report.ReportStandardRequest;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.contant.LogsContant;
import com.oxo.ball.contant.RedisKeyContant;
import com.oxo.ball.mapper.BallBetMapper;
import com.oxo.ball.service.IMessageQueueService;
import com.oxo.ball.service.admin.*;
import com.oxo.ball.service.impl.BasePlayerService;
import com.oxo.ball.service.player.IPlayerBetService;
import com.oxo.ball.service.player.IPlayerGameService;
import com.oxo.ball.service.player.IPlayerService;
import com.oxo.ball.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class PlayerBetServiceImpl extends ServiceImpl<BallBetMapper, BallBet> implements IPlayerBetService {
    private Logger apiLog = LoggerFactory.getLogger(LogsContant.API_LOG);

    @Resource
    IPlayerGameService gameService;
    @Resource
    IBallGameLossPerCentService gameLossPerCentService;
    @Resource
    IPlayerService playerService;
    @Resource
    BasePlayerService basePlayerService;
    @Resource
    IBallBalanceChangeService ballBalanceChangeService;
    @Resource
    RedisUtil redisUtil;
    @Resource
    IMessageQueueService messageQueueService;
    @Autowired
    IBallSystemConfigService systemConfigService;
    @Autowired
    IBallBetService ballBetService;
    @Autowired
    IBallLoggerBackService loggerBackService;
    @Autowired
    IBallCommissionStrategyService commissionStrategyService;
    @Autowired
    IBallVipService vipService;
    @Autowired
    IBallBankCardService bankCardService;
    @Autowired
    BallBetMapper mapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse gameBet(BetRequest betRequest, BallPlayer player) throws SQLException, JsonProcessingException {
        //正式号必须绑定手机
        if(player.getAccountType()==2&&StringUtils.isBlank(player.getPhone())){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "phoneNotBind"));
        }
        //查询游戏
        BallGame game = gameService.findById(betRequest.getGameId());
        if (game == null) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "gameNotFound"));
        }
        BallGameLossPerCent odds = gameLossPerCentService.findById(betRequest.getOddsId());
        if (odds == null) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "oddsNotFound"));
        }
        //查询赔率
        //查询玩家
        Double requestMoney = Double.valueOf(betRequest.getMoney());
        Double realMoneyd = BigDecimalUtil.mul(requestMoney , BigDecimalUtil.PLAYER_MONEY_UNIT);
        Long realMoney = realMoneyd.longValue();
        //是否限额,指定赔率
        if(odds.getMinBet()>0&&requestMoney<odds.getMinBet()){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "betMoneyTooLittle"));
        }
        if(odds.getMaxBet()>0&&requestMoney>odds.getMaxBet()){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "betMoneyTooMuch"));
        }
        //是否限额
        if(game.getMinBet()>0&&requestMoney<game.getMinBet()){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "betMoneyTooLittle"));
        }
        if(game.getMaxBet()>0&&requestMoney>game.getMaxBet()){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "betMoneyTooMuch"));
        }
        //全盘最低投注
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        if(systemConfig.getPlayerBetMin()>0){
            if(requestMoney<systemConfig.getPlayerBetMin()){
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("", "betMoneyTooLittle"));
            }
        }
        //查询当期赛事总投注,缓存到redis
        String totalKey = RedisKeyContant.GAME_BET_TOTAL + game.getId() + player.getId();
        String totalOddKey = RedisKeyContant.GAME_BET_TOTAL + game.getId()+odds.getId() + player.getId();
        String globalKey = RedisKeyContant.GAME_BET_TOTAL+player.getId();
        Double totalLong = 0d;
        Object total = redisUtil.get(totalKey);
        if(total!=null){
            totalLong = Double.parseDouble(total.toString());
        }
        Double totalOddLong = 0d;
        Object totalOdd = redisUtil.get(totalOddKey);
        if(totalOdd!=null){
            totalOddLong = Double.parseDouble(totalOdd.toString());
        }
        Double totalLongGlobal = 0d;
        Object totalGlobal = redisUtil.get(globalKey);
        if(totalGlobal!=null){
            totalLongGlobal = Double.parseDouble(totalGlobal.toString());
        }

        //赔率限额是否超过,以赔率为优先
        if(odds.getTotalBet()>0){
            if(totalOddLong+realMoney>odds.getTotalBet()){
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("", "betTotalExceed"));
            }
        }else{
            //赛事限额是否超过,没有设置赔率则判定赛事
            if(game.getTotalBet()>0){
                if(totalLong+requestMoney>game.getTotalBet()){
                    return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                            ResponseMessageUtil.responseMessage("", "betTotalExceed"));
                }
            }
        }
        //是否超出全盘投注额度
        if(systemConfig.getPlayerBetMax()>0){
            if(totalLongGlobal+requestMoney>systemConfig.getPlayerBetMax()){
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("", "betGlobalTotalExceed"));
            }
        }

        //判定账户余额
        if (player.getBalance() < realMoney) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "balanceNotEnough"));
        }
        //判定赛事是否可用
        if (game.getStatus() == 2) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "gameClosed"));
        }
        if (game.getGameStatus() == 2) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "gameStarted"));
        }
        if (game.getGameStatus() == 3) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "gameFinished"));
        }
        while (true) {
            //扣除账号余额
            BallPlayer edit = BallPlayer.builder()
                    .version(player.getVersion())
                    .balance(player.getBalance() - realMoney)
                    //TODO 下注冻结
                    .frozenBet((player.getFrozenBet()==null?0:player.getFrozenBet())+realMoney)
                    .build();
            edit.setId(player.getId());
            if (systemConfig.getRegisterIfNeedVerificationCode() != null && systemConfig.getRegisterIfNeedVerificationCode() > 0) {
                //累计打码量,查询配置,是否需要*比例
                Double mul = BigDecimalUtil.mul(realMoney, BigDecimalUtil.div(systemConfig.getRechargeCodeConversionRate(), 100));
                edit.setCumulativeQr((player.getCumulativeQr() == null ? 0 : player.getCumulativeQr()) + mul.intValue());
            } else {
                edit.setCumulativeQr((player.getCumulativeQr() == null ? 0 : player.getCumulativeQr()) + realMoney);
            }
            boolean b = basePlayerService.editAndClearCache(edit, player);
            if (b) {
                break;
            } else {
                //更新失败再次判定余额是否足够,
                player = basePlayerService.findById(player.getId());
                if (player.getBalance() < realMoney) {
                    return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                            ResponseMessageUtil.responseMessage("", "balanceNotEnough"));
                }
            }
        }
        /*
            (上下半场)比分赔率
         */
        String betInfo = "("+BallGameLossPerCent.GAME_TYPE[odds.getGameType()]
                +")"+odds.getScoreHome() + "-" + odds.getScoreAway()
                +"-"+(betRequest.getType()==1?odds.getLossPerCent():odds.getAntiPerCent());
        String remark = game.getAllianceName() + ":"
                + game.getMainName()
                + ":" + game.getGuestName()
                + ":" + odds.getScoreHome() + "-" + odds.getScoreAway()
                + ":[" + (betRequest.getMoney()) + "]";
        //保存下注数据

        BallBet save = BallBet.builder()
                .gameId(betRequest.getGameId())
                .gameInfo(game.getMainName()+" VS "+game.getGuestName())
                .betMoney(realMoney)
                .playerId(player.getId())
                .gameLossPerCentId(betRequest.getOddsId())
                //TODO 下注手续费,这里不扣，结算的时候从中奖金额扣
//                .handMoney(betRate)
                .winningAmount(0L)
                .status(1)
                .betType(betRequest.getType())
                .betScore(odds.getScoreHome() + "-" + odds.getScoreAway())
                .betOdds(betRequest.getType()==1?odds.getLossPerCent():odds.getAntiPerCent())
                .orderNo(Long.parseLong(TimeUtil.dateFormat(new Date(), TimeUtil.TIME_TAG_MM_DD_HH_MM_SS))+ getDayOrderNo())
                .remark(betInfo)
                .username(player.getUsername())
                .userId(player.getUserId())
                .accountType(player.getAccountType())
                .startTime(game.getStartTime())
                .even(game.getEven()==1?1:odds.getEven())
                .statusSettlement(0)
                .statusOpen(0)
                .superiorId(player.getSuperiorId())
                .superTree(player.getSuperTree())
                .gameType(odds.getGameType())
                .build();
        save.setCreatedAt(System.currentTimeMillis());
        boolean isSucc = save(save);
        final BallPlayer tplayer = player;
        if(isSucc){
            ThreadPoolUtil.exec(() -> {
                BallBet edit1 = BallBet.builder()
                        .build();
                edit1.setId(save.getId());
                String superTree = tplayer.getSuperTree();
                boolean hasSuper = false;
                if(superTree.equals("0")){
                }else if(superTree.equals("_")){
                }else{
                    String[] split = superTree.split("_");
                    try {
                        BallPlayer superPlayer = basePlayerService.findById(Long.parseLong(split[1]));
                        edit1.setTopUsername(superPlayer.getUsername());
                        hasSuper = true;
                        if(split.length==2){
                            edit1.setFirstUsername(superPlayer.getUsername());
                        }else if(split.length>2){
                            BallPlayer firstPlayer = basePlayerService.findById(Long.parseLong(split[2]));
                            edit1.setFirstUsername(firstPlayer.getUsername());
                        }
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
                if(hasSuper){
                    ballBetService.edit(edit1);
                }
            });
        }
        BallBalanceChange balanceChange = BallBalanceChange.builder()
                .playerId(player.getId())
                .accountType(player.getAccountType())
                .userId(player.getUserId())
                .parentId(player.getSuperiorId())
                .username(player.getUsername())
                .superTree(player.getSuperTree())
                .createdAt(System.currentTimeMillis())
                .changeMoney(-realMoney)
                .initMoney(player.getBalance())
                .dnedMoney(player.getBalance()-realMoney)
                .balanceChangeType(3)
                .frozenStatus(0)
                .orderNo(save.getOrderNo())
                .remark(remark)
                .build();
        ballBalanceChangeService.insert(balanceChange);

        //下注日志
        messageQueueService.putMessage(MessageQueueDTO.builder()
                .type(MessageQueueDTO.TYPE_LOG_BET)
                .data(JsonUtil.toJson(MessageQueueBet.builder()
                        .ballPlayer(player)
                        .betContent(balanceChange.getRemark())
                        .ip(player.getIp())
                        .orderId(save.getOrderNo())
                        .build()))
                .build());
        if (!isSucc) {
            throw new SQLException();
        }
        //缓存本期投注
        redisUtil.set(totalKey,totalLong+realMoney);
        redisUtil.set(totalOddKey,totalOddLong+realMoney);
        redisUtil.set(globalKey,totalLongGlobal+realMoney);
        return BaseResponse.successWithData(balanceChange);
    }


    @Override
    public SearchResponse<BallBet> search(PlayerBetRequest queryParam, BallPlayer ballPlayer, Integer pageNo, Integer pageSize) {
        SearchResponse<BallBet> response = new SearchResponse<>();
        Page<BallBet> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallBet> query = new QueryWrapper<>();
//        Map<String,Object> query = new HashMap<>();
        /**
         * 过滤条件
         * 一.玩家ID
         * 二.时间 今天，昨天，近7日，近10日，近30日
         * 三.类型
         */
        if (queryParam.getTime() != null) {
            switch (queryParam.getTime()) {
                case 1:
                    query.ge("created_at", TimeUtil.getDayBegin().getTime());
                    query.le("created_at", TimeUtil.getDayEnd().getTime());
//                    query.put("begin",TimeUtil.getDayBegin().getTime());
//                    query.put("end",TimeUtil.getDayEnd().getTime());
                    break;
                case 2:
                    query.ge("created_at", TimeUtil.getBeginDayOfYesterday().getTime());
                    query.le("created_at", TimeUtil.getEndDayOfYesterday().getTime());
//                    query.put("begin",TimeUtil.getBeginDayOfYesterday().getTime());
//                    query.put("end",TimeUtil.getEndDayOfYesterday().getTime());
                    break;
                case 3:
                    query.ge("created_at", TimeUtil.getDayBegin().getTime()-7*TimeUtil.TIME_ONE_DAY);
                    query.le("created_at", TimeUtil.getDayEnd().getTime());
//                    query.put("begin",TimeUtil.getDayBegin().getTime()-7*TimeUtil.TIME_ONE_DAY);
//                    query.put("end",TimeUtil.getDayEnd().getTime());
                    break;
                case 4:
                    query.ge("created_at", TimeUtil.getDayBegin().getTime()-10*TimeUtil.TIME_ONE_DAY);
                    query.le("created_at", TimeUtil.getDayEnd().getTime());
//                    query.put("begin",TimeUtil.getDayBegin().getTime()-10*TimeUtil.TIME_ONE_DAY);
//                    query.put("end",TimeUtil.getDayEnd().getTime());
                    break;
                case 5:
                    query.ge("created_at", TimeUtil.getDayBegin().getTime()-30*TimeUtil.TIME_ONE_DAY);
                    query.le("created_at", TimeUtil.getDayEnd().getTime());
//                    query.put("begin",TimeUtil.getDayBegin().getTime()-30*TimeUtil.TIME_ONE_DAY);
//                    query.put("end",TimeUtil.getDayEnd().getTime());
                    break;
                default:
                    break;
            }
        }
        if(queryParam.getType()!=null){
            if(queryParam.getType()==2){
                query.eq("bet_type",queryParam.getType());
//                query.put("type",queryParam.getType());
            }
        }
        query.eq("player_id", ballPlayer.getId());
//        query.put("playerId",ballPlayer.getId());
        //先ID降序再top升序
        query.orderByDesc("id");
        IPage<BallBet> pages = page(page, query);
//        IPage<BallBet> pages = mapper.pages(page,query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public Long getDayOrderNo() {
        if (redisUtil.get(RedisKeyContant.BET_ORDER_NO) == null) {
            redisUtil.set(RedisKeyContant.BET_ORDER_NO, 1);
        }
        long incr = redisUtil.incr(RedisKeyContant.BET_ORDER_NO, 1);
        return incr;
    }

    @Override
    public void clearDayOrderNo() {
        redisUtil.del(RedisKeyContant.BET_ORDER_NO);
        redisUtil.del(RedisKeyContant.RECHARGE_ORDER_NO);
        redisUtil.del(RedisKeyContant.WITHDRAWAL_ORDER_NO);
        redisUtil.del("bootserver");
    }

    @Override
    public BaseResponse gameBetPrepare(BetPreRequest betRequest, BallPlayer currentPlayer) {
        //账户余额
        Map<String, Object> data = new HashMap<>();
        data.put("balance", currentPlayer.getBalance());
        //手续费
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        data.put("betHandMoneyRate", BigDecimalUtil.div(systemConfig.getBetHandMoneyRate(),100,2));
        data.put("fastMoney", systemConfig.getFastMoney());
        //报酬率
        //TODO 报酬率先返回赔率了
        BallGameLossPerCent gameLossPerCent = gameLossPerCentService.findById(betRequest.getOddsId());
        if(!gameLossPerCent.getGameId().equals(betRequest.getGameId())){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "oddsUnMatchGame"));
        }
//        data.put("rateOfReturn", gameLossPerCent.getLossPerCent());
        //赛事
        data.put("game", gameService.findById(betRequest.getGameId()));
        //赔率
        data.put("lossPerCent", gameLossPerCent);
        //额外收益
        data.put("bonus",Double.valueOf(vipService.findByLevel(currentPlayer.getVipLevel()).getLevelProfit()));
//        data.put("explain", "字段说明:[betHandMoneyRate:手续费,需要除100?1000?],[rateOfReturn:报酬率,不懂先返回赔率了],[game:游戏],[lossPerCent]赔率");
        return BaseResponse.successWithData(data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse unbet(Long betId, BallBet bet, BallPlayer currentUser, BallAdmin ballAdmin) {
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        //撤消订单,账户加余额,减打码量
        if(bet==null){
            bet = ballBetService.findById(betId);
            if(bet==null){
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("", "orderNotFound"));
            }
        }
        if(!bet.getPlayerId().equals(currentUser.getId())){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "orderNotFound"));
        }
        //只能撤消未结算和已确认的订单
        if(bet.getStatusSettlement()==1 || bet.getStatus()!=1){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "orderCatUnbet"));
        }
        //设置为撤消
        BallBet edit = BallBet.builder()
                .status(3)
                .settlememntPerson(ballAdmin.getUsername())
                .build();
        edit.setId(bet.getId());
        Boolean editRes = ballBetService.edit(edit);
        if(!editRes){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "orderUnbetFail"));
        }
        apiLog.info("订单已撤消:{}",bet);
        //距离下次打码量加回
//        if (systemConfig.getCaptchaThreshold() != null && systemConfig.getCaptchaThreshold() > 0) {
//            //离下次提现所需captchaThreshold要打码量,如果当前打码量>设置的量,则为0,否则为相差数
//            if (currentUser.getCumulativeQr() > systemConfig.getCaptchaThreshold()) {
//                currentUser.setNeedQr(0L);
//            } else {
//                currentUser.setNeedQr(systemConfig.getCaptchaThreshold() - currentUser.getCumulativeQr());
//            }
//        }
        while(true){
            BallPlayer editPlayer = BallPlayer.builder()
                    .version(currentUser.getVersion())
                    .balance(currentUser.getBalance()+bet.getBetMoney())
                    //冻结减去投注金额
                    .frozenBet(currentUser.getFrozenBet()-bet.getBetMoney())
                    .build();
            editPlayer.setId(currentUser.getId());
            //打码量撤回
            if(systemConfig.getRegisterIfNeedVerificationCode()!=null&&systemConfig.getRegisterIfNeedVerificationCode()>0){
                //累计打码量,查询配置,是否需要*比例
                Double mul = BigDecimalUtil.mul(bet.getBetMoney(), BigDecimalUtil.div(systemConfig.getRechargeCodeConversionRate(), 100));
                editPlayer.setCumulativeQr(currentUser.getCumulativeQr()-mul.intValue());
            }else{
                editPlayer.setCumulativeQr(currentUser.getCumulativeQr()-bet.getBetMoney());
            }
            boolean b = basePlayerService.editAndClearCache(editPlayer, currentUser);
            if(b){
                //下注账变改为假账变
//                BallBalanceChange betChange = ballBalanceChangeService.findByOrderId(3, bet.getOrderNo());
//                ballBalanceChangeService.edit(BallBalanceChange.builder()
//                        .id(betChange.getId())
//                        .frozenStatus(0)
//                        .build());
                //插入账变
                ballBalanceChangeService.insert(BallBalanceChange.builder()
                        .playerId(currentUser.getId())
                        .accountType(currentUser.getAccountType())
                        .userId(currentUser.getUserId())
                        .parentId(currentUser.getSuperiorId())
                        .username(currentUser.getUsername())
                        .superTree(currentUser.getSuperTree())
                        .initMoney(currentUser.getBalance())
                        .changeMoney(bet.getBetMoney())
                        .dnedMoney(editPlayer.getBalance())
                        .createdAt(System.currentTimeMillis())
                        //撤单状态
                        .balanceChangeType(7)
                        .remark(MessageFormat.format("game order:{0},canceled",bet.getOrderNo()))
                        .build());
                //撤消总投注
                String totalKey = RedisKeyContant.GAME_BET_TOTAL + bet.getGameId() + bet.getPlayerId();
                String totalOddKey = RedisKeyContant.GAME_BET_TOTAL + bet.getGameId() +bet.getGameLossPerCentId()+ bet.getPlayerId();
                String globalKey = RedisKeyContant.GAME_BET_TOTAL + bet.getPlayerId();
                Double totalLong = 0D;
                Double totalOddLong = 0D;
                Double totalGlobalLong = 0D;
                Object total = redisUtil.get(totalKey);
                Object totalOdd = redisUtil.get(totalOddKey);
                Object totalGlobal = redisUtil.get(globalKey);
                if(total!=null) {
                    totalLong = Double.parseDouble(total.toString());
                    redisUtil.set(totalKey,totalLong-bet.getBetMoney());
                }
                if(totalOdd!=null) {
                    totalOddLong = Double.parseDouble(totalOdd.toString());
                    redisUtil.set(totalOddKey,totalOddLong-bet.getBetMoney());
                }
                if(totalGlobal!=null) {
                    totalGlobalLong = Double.parseDouble(totalGlobal.toString());
                    redisUtil.set(globalKey,totalGlobalLong-bet.getBetMoney());
                }
                return BaseResponse.successWithMsg("ok");
            }else{
                //更新失败再次更新
                currentUser = basePlayerService.findById(currentUser.getId());
            }
        }
    }

    @Override
    public BaseResponse unbetPlayer(Long betId, BallBet bet, BallPlayer currentUser) {
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        //撤消订单,账户加余额,减打码量
        if(bet==null){
            bet = ballBetService.findById(betId);
            if(bet==null){
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("", "orderNotFound"));
            }
        }
        if(!bet.getPlayerId().equals(currentUser.getId())){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "orderNotFound"));
        }
        //只能撤消未结算和已确认的订单
        if(bet.getStatusSettlement()==1 || bet.getStatus()!=1){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "orderCatUnbet"));
        }
        //比赛开始后的订单不能撤消
        BallGame game = gameService.findById(bet.getGameId());
        if(game.getGameStatus()!=1){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "orderCatUnbet"));
        }
        //设置为撤消
        BallBet edit = BallBet.builder()
                .status(3)
                // 玩家撤消
                .settlememntPerson(currentUser.getUsername())
                .build();
        edit.setId(bet.getId());
        Boolean editRes = ballBetService.edit(edit);
        if(!editRes){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "orderUnbetFail"));
        }
        //距离下次打码量加回
//        if (systemConfig.getCaptchaThreshold() != null && systemConfig.getCaptchaThreshold() > 0) {
//            //离下次提现所需captchaThreshold要打码量,如果当前打码量>设置的量,则为0,否则为相差数
//            if (currentUser.getCumulativeQr() > systemConfig.getCaptchaThreshold()) {
//                currentUser.setNeedQr(0L);
//            } else {
//                currentUser.setNeedQr(systemConfig.getCaptchaThreshold() - currentUser.getCumulativeQr());
//            }
//        }
        while(true){
            BallPlayer editPlayer = BallPlayer.builder()
                    .version(currentUser.getVersion())
                    .balance(currentUser.getBalance()+bet.getBetMoney())
                    //冻结减去投注金额
                    .frozenBet(currentUser.getFrozenBet()-bet.getBetMoney())
                    .build();
            editPlayer.setId(currentUser.getId());
            //打码量撤回
            if(systemConfig.getRegisterIfNeedVerificationCode()!=null&&systemConfig.getRegisterIfNeedVerificationCode()>0){
                //累计打码量,查询配置,是否需要*比例
                Double mul = BigDecimalUtil.mul(bet.getBetMoney(), BigDecimalUtil.div(systemConfig.getRechargeCodeConversionRate(), 100));
                editPlayer.setCumulativeQr(currentUser.getCumulativeQr()-mul.intValue());
            }else{
                editPlayer.setCumulativeQr(currentUser.getCumulativeQr()-bet.getBetMoney());
            }
            boolean b = basePlayerService.editAndClearCache(editPlayer, currentUser);
            if(b){
                //下注账变改为假账变
//                BallBalanceChange betChange = ballBalanceChangeService.findByOrderId(3, bet.getOrderNo());
//                ballBalanceChangeService.edit(BallBalanceChange.builder()
//                        .id(betChange.getId())
//                        .frozenStatus(0)
//                        .build());
                //插入账变
                ballBalanceChangeService.insert(BallBalanceChange.builder()
                        .playerId(currentUser.getId())
                        .accountType(currentUser.getAccountType())
                        .userId(currentUser.getUserId())
                        .parentId(currentUser.getSuperiorId())
                        .username(currentUser.getUsername())
                        .superTree(currentUser.getSuperTree())
                        .initMoney(currentUser.getBalance())
                        .changeMoney(bet.getBetMoney())
                        .dnedMoney(editPlayer.getBalance())
                        .createdAt(System.currentTimeMillis())
                        //撤单状态
                        .balanceChangeType(7)
                        .remark(MessageFormat.format("赛事订单:{0},已撤消",bet.getOrderNo()))
                        .build());
                //撤消总投注
                String totalKey = RedisKeyContant.GAME_BET_TOTAL + bet.getGameId() + bet.getPlayerId();
                String totalOddKey = RedisKeyContant.GAME_BET_TOTAL + bet.getGameId() +bet.getGameLossPerCentId()+ bet.getPlayerId();
                String globalKey = RedisKeyContant.GAME_BET_TOTAL + bet.getPlayerId();
                Double totalLong = 0D;
                Double totalOddLong = 0D;
                Double totalGlobalLong = 0D;
                Object total = redisUtil.get(totalKey);
                Object totalOdd = redisUtil.get(totalOddKey);
                Object totalGlobal = redisUtil.get(globalKey);
                if(total!=null) {
                    totalLong = Double.parseDouble(total.toString());
                    redisUtil.set(totalKey,totalLong-bet.getBetMoney());
                }
                if(totalOdd!=null) {
                    totalOddLong = Double.parseDouble(totalOdd.toString());
                    redisUtil.set(totalOddKey,totalOddLong-bet.getBetMoney());
                }
                if(totalGlobal!=null) {
                    totalGlobalLong = Double.parseDouble(totalGlobal.toString());
                    redisUtil.set(globalKey,totalGlobalLong-bet.getBetMoney());
                }
                return BaseResponse.successWithMsg("ok");
            }else{
                //更新失败再次更新
                currentUser = basePlayerService.findById(currentUser.getId());
            }
        }
    }

    @Override
    public BaseResponse betInfo(Long betId, BallPlayer currentUser) {
        BallBet byId = ballBetService.findById(betId);
        if(byId==null){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "orderNotFound"));
        }
        return BaseResponse.successWithData(byId);
    }

    @Override
    public BaseResponse betToday(Integer pageNo, Integer pageSize, BallPlayer ballPlayer) {
        SearchResponse<BallBet> response = new SearchResponse<>();
        Page<BallBet> page = new Page<>(pageNo, pageSize);
        Map<String,Object> query = new HashMap<>();
        //TODO 是否需要只要正常订单
//        query.put("status",1);
        query.put("playerId",ballPlayer.getId());
        query.put("begin",TimeUtil.getDayBegin().getTime());
        query.put("end",TimeUtil.getDayEnd().getTime());
        //先ID降序再top升序
        IPage<BallBet> pages = mapper.pages(page,query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());

        Double aDouble = Double.valueOf(vipService.findByLevel(ballPlayer.getVipLevel()).getLevelProfit());
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        for(BallBet bet:pages.getRecords()){
//            Long oddsLong = BallBetServiceImpl.arithmeticBingo(bet.getBetMoney(), bet.getBetOdds());
//            Double profit = BigDecimalUtil.div(BigDecimalUtil.mul(oddsLong, aDouble), BigDecimalUtil.PLAYER_MONEY_UNIT);
            bet.setBonus(aDouble);
            bet.setChargeRate(""+BigDecimalUtil.div(systemConfig.getBetHandMoneyRate(),100));
        }
        return BaseResponse.successWithData(response);
    }

    @Override
    public boolean edit(BallBet edit) {
        return updateById(edit);
    }

    @Override
    public BaseResponse betInfoPlayer(Long betId, BallPlayer currentUser) {
        BallBet byId = ballBetService.findById(betId);
        if(byId==null){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "orderNotFound"));
        }
        if(!byId.getPlayerId().equals(currentUser.getId())){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "orderNotFound"));
        }
//        return BaseResponse.successWithData(byId);
        Map<String,Object> data = new HashMap<>();
        data.put("betinfo",byId);
        //查赔率
        BallGameLossPerCent gameLossPerCent = gameLossPerCentService.findById(byId.getGameLossPerCentId());
        data.put("odds",gameLossPerCent);
        if(gameLossPerCent!=null){
            BallGame game = gameService.findById(gameLossPerCent.getGameId());
            data.put("game",game);
        }
        return BaseResponse.successWithData(data);
    }

    @Override
    public int standard(ReportStandardRequest reportStandardRequest) {
        return mapper.standard(reportStandardRequest);
    }
    @Override
    public int standard2(ReportStandardRequest reportStandardRequest) {
        return mapper.standard2(reportStandardRequest);
    }
}
