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
        //???????????????????????????
        if(player.getAccountType()==2&&StringUtils.isBlank(player.getPhone())){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "phoneNotBind"));
        }
        //????????????
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
        //????????????
        //????????????
        Double requestMoney = Double.valueOf(betRequest.getMoney());
        Double realMoneyd = BigDecimalUtil.mul(requestMoney , BigDecimalUtil.PLAYER_MONEY_UNIT);
        Long realMoney = realMoneyd.longValue();
        //????????????,????????????
        if(odds.getMinBet()>0&&requestMoney<odds.getMinBet()){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "betMoneyTooLittle"));
        }
        if(odds.getMaxBet()>0&&requestMoney>odds.getMaxBet()){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "betMoneyTooMuch"));
        }
        //????????????
        if(game.getMinBet()>0&&requestMoney<game.getMinBet()){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "betMoneyTooLittle"));
        }
        if(game.getMaxBet()>0&&requestMoney>game.getMaxBet()){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "betMoneyTooMuch"));
        }
        //??????????????????
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        if(systemConfig.getPlayerBetMin()>0){
            if(requestMoney<systemConfig.getPlayerBetMin()){
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("", "betMoneyTooLittle"));
            }
        }
        //???????????????????????????,?????????redis
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

        //????????????????????????,??????????????????
        if(odds.getTotalBet()>0){
            if(totalOddLong+realMoney>odds.getTotalBet()){
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("", "betTotalExceed"));
            }
        }else{
            //????????????????????????,?????????????????????????????????
            if(game.getTotalBet()>0){
                if(totalLong+requestMoney>game.getTotalBet()){
                    return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                            ResponseMessageUtil.responseMessage("", "betTotalExceed"));
                }
            }
        }
        //??????????????????????????????
        if(systemConfig.getPlayerBetMax()>0){
            if(totalLongGlobal+requestMoney>systemConfig.getPlayerBetMax()){
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("", "betGlobalTotalExceed"));
            }
        }

        //??????????????????
        if (player.getBalance() < realMoney) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "balanceNotEnough"));
        }
        //????????????????????????
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
            //??????????????????
            BallPlayer edit = BallPlayer.builder()
                    .version(player.getVersion())
                    .balance(player.getBalance() - realMoney)
                    //TODO ????????????
                    .frozenBet((player.getFrozenBet()==null?0:player.getFrozenBet())+realMoney)
                    .build();
            edit.setId(player.getId());
            if (systemConfig.getRegisterIfNeedVerificationCode() != null && systemConfig.getRegisterIfNeedVerificationCode() > 0) {
                //???????????????,????????????,????????????*??????
                Double mul = BigDecimalUtil.mul(realMoney, BigDecimalUtil.div(systemConfig.getRechargeCodeConversionRate(), 100));
                edit.setCumulativeQr((player.getCumulativeQr() == null ? 0 : player.getCumulativeQr()) + mul.intValue());
            } else {
                edit.setCumulativeQr((player.getCumulativeQr() == null ? 0 : player.getCumulativeQr()) + realMoney);
            }
            boolean b = basePlayerService.editAndClearCache(edit, player);
            if (b) {
                break;
            } else {
                //??????????????????????????????????????????,
                player = basePlayerService.findById(player.getId());
                if (player.getBalance() < realMoney) {
                    return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                            ResponseMessageUtil.responseMessage("", "balanceNotEnough"));
                }
            }
        }
        /*
            (????????????)????????????
         */
        String betInfo = "("+BallGameLossPerCent.GAME_TYPE[odds.getGameType()]
                +")"+odds.getScoreHome() + "-" + odds.getScoreAway()
                +"-"+(betRequest.getType()==1?odds.getLossPerCent():odds.getAntiPerCent());
        String remark = game.getAllianceName() + ":"
                + game.getMainName()
                + ":" + game.getGuestName()
                + ":" + odds.getScoreHome() + "-" + odds.getScoreAway()
                + ":[" + (betRequest.getMoney()) + "]";
        //??????????????????

        BallBet save = BallBet.builder()
                .gameId(betRequest.getGameId())
                .gameInfo(game.getMainName()+" VS "+game.getGuestName())
                .betMoney(realMoney)
                .playerId(player.getId())
                .gameLossPerCentId(betRequest.getOddsId())
                //TODO ???????????????,????????????????????????????????????????????????
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

        //????????????
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
        //??????????????????
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
         * ????????????
         * ???.??????ID
         * ???.?????? ?????????????????????7?????????10?????????30???
         * ???.??????
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
        //???ID?????????top??????
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
        //????????????
        Map<String, Object> data = new HashMap<>();
        data.put("balance", currentPlayer.getBalance());
        //?????????
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        data.put("betHandMoneyRate", BigDecimalUtil.div(systemConfig.getBetHandMoneyRate(),100,2));
        data.put("fastMoney", systemConfig.getFastMoney());
        //?????????
        //TODO ???????????????????????????
        BallGameLossPerCent gameLossPerCent = gameLossPerCentService.findById(betRequest.getOddsId());
        if(!gameLossPerCent.getGameId().equals(betRequest.getGameId())){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "oddsUnMatchGame"));
        }
//        data.put("rateOfReturn", gameLossPerCent.getLossPerCent());
        //??????
        data.put("game", gameService.findById(betRequest.getGameId()));
        //??????
        data.put("lossPerCent", gameLossPerCent);
        //????????????
        data.put("bonus",Double.valueOf(vipService.findByLevel(currentPlayer.getVipLevel()).getLevelProfit()));
//        data.put("explain", "????????????:[betHandMoneyRate:?????????,?????????100?1000?],[rateOfReturn:?????????,????????????????????????],[game:??????],[lossPerCent]??????");
        return BaseResponse.successWithData(data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse unbet(Long betId, BallBet bet, BallPlayer currentUser, BallAdmin ballAdmin) {
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        //????????????,???????????????,????????????
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
        //??????????????????????????????????????????
        if(bet.getStatusSettlement()==1 || bet.getStatus()!=1){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "orderCatUnbet"));
        }
        //???????????????
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
        apiLog.info("???????????????:{}",bet);
        //???????????????????????????
//        if (systemConfig.getCaptchaThreshold() != null && systemConfig.getCaptchaThreshold() > 0) {
//            //?????????????????????captchaThreshold????????????,?????????????????????>????????????,??????0,??????????????????
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
                    //????????????????????????
                    .frozenBet(currentUser.getFrozenBet()-bet.getBetMoney())
                    .build();
            editPlayer.setId(currentUser.getId());
            //???????????????
            if(systemConfig.getRegisterIfNeedVerificationCode()!=null&&systemConfig.getRegisterIfNeedVerificationCode()>0){
                //???????????????,????????????,????????????*??????
                Double mul = BigDecimalUtil.mul(bet.getBetMoney(), BigDecimalUtil.div(systemConfig.getRechargeCodeConversionRate(), 100));
                editPlayer.setCumulativeQr(currentUser.getCumulativeQr()-mul.intValue());
            }else{
                editPlayer.setCumulativeQr(currentUser.getCumulativeQr()-bet.getBetMoney());
            }
            boolean b = basePlayerService.editAndClearCache(editPlayer, currentUser);
            if(b){
                //???????????????????????????
//                BallBalanceChange betChange = ballBalanceChangeService.findByOrderId(3, bet.getOrderNo());
//                ballBalanceChangeService.edit(BallBalanceChange.builder()
//                        .id(betChange.getId())
//                        .frozenStatus(0)
//                        .build());
                //????????????
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
                        //????????????
                        .balanceChangeType(7)
                        .remark(MessageFormat.format("game order:{0},canceled",bet.getOrderNo()))
                        .build());
                //???????????????
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
                //????????????????????????
                currentUser = basePlayerService.findById(currentUser.getId());
            }
        }
    }

    @Override
    public BaseResponse unbetPlayer(Long betId, BallBet bet, BallPlayer currentUser) {
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        //????????????,???????????????,????????????
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
        //??????????????????????????????????????????
        if(bet.getStatusSettlement()==1 || bet.getStatus()!=1){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "orderCatUnbet"));
        }
        //????????????????????????????????????
        BallGame game = gameService.findById(bet.getGameId());
        if(game.getGameStatus()!=1){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "orderCatUnbet"));
        }
        //???????????????
        BallBet edit = BallBet.builder()
                .status(3)
                // ????????????
                .settlememntPerson(currentUser.getUsername())
                .build();
        edit.setId(bet.getId());
        Boolean editRes = ballBetService.edit(edit);
        if(!editRes){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "orderUnbetFail"));
        }
        //???????????????????????????
//        if (systemConfig.getCaptchaThreshold() != null && systemConfig.getCaptchaThreshold() > 0) {
//            //?????????????????????captchaThreshold????????????,?????????????????????>????????????,??????0,??????????????????
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
                    //????????????????????????
                    .frozenBet(currentUser.getFrozenBet()-bet.getBetMoney())
                    .build();
            editPlayer.setId(currentUser.getId());
            //???????????????
            if(systemConfig.getRegisterIfNeedVerificationCode()!=null&&systemConfig.getRegisterIfNeedVerificationCode()>0){
                //???????????????,????????????,????????????*??????
                Double mul = BigDecimalUtil.mul(bet.getBetMoney(), BigDecimalUtil.div(systemConfig.getRechargeCodeConversionRate(), 100));
                editPlayer.setCumulativeQr(currentUser.getCumulativeQr()-mul.intValue());
            }else{
                editPlayer.setCumulativeQr(currentUser.getCumulativeQr()-bet.getBetMoney());
            }
            boolean b = basePlayerService.editAndClearCache(editPlayer, currentUser);
            if(b){
                //???????????????????????????
//                BallBalanceChange betChange = ballBalanceChangeService.findByOrderId(3, bet.getOrderNo());
//                ballBalanceChangeService.edit(BallBalanceChange.builder()
//                        .id(betChange.getId())
//                        .frozenStatus(0)
//                        .build());
                //????????????
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
                        //????????????
                        .balanceChangeType(7)
                        .remark(MessageFormat.format("????????????:{0},?????????",bet.getOrderNo()))
                        .build());
                //???????????????
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
                //????????????????????????
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
        //TODO ??????????????????????????????
//        query.put("status",1);
        query.put("playerId",ballPlayer.getId());
        query.put("begin",TimeUtil.getDayBegin().getTime());
        query.put("end",TimeUtil.getDayEnd().getTime());
        //???ID?????????top??????
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
        //?????????
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
