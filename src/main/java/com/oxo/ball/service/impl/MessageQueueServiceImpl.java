package com.oxo.ball.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.oxo.ball.bean.dao.*;
import com.oxo.ball.bean.dto.CommissionRules;
import com.oxo.ball.bean.dto.model.RechargeRebateDto;
import com.oxo.ball.bean.dto.queue.MessageQueueBet;
import com.oxo.ball.bean.dto.queue.MessageQueueDTO;
import com.oxo.ball.bean.dto.queue.MessageQueueLogin;
import com.oxo.ball.bean.dto.queue.MessageQueueOper;
import com.oxo.ball.bean.dto.redis.BetActivityDays;
import com.oxo.ball.contant.LogsContant;
import com.oxo.ball.contant.RedisKeyContant;
import com.oxo.ball.service.IMessageQueueService;
import com.oxo.ball.service.admin.*;
import com.oxo.ball.service.impl.admin.BallCommissionStrategyServiceImpl;
import com.oxo.ball.service.impl.admin.BallVipServiceImpl;
import com.oxo.ball.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


@Service
public class MessageQueueServiceImpl implements IMessageQueueService {
    private Logger logger = LoggerFactory.getLogger(LogsContant.TASK_LOG);

    @Resource
    IBallLoggerService loggerService;
//    BlockingQueue<MessageQueueDTO> messageQueue;
    @Resource
    private BasePlayerService basePlayerService;
    @Resource
    IBallLoggerOperService loggerOperService;
    @Resource
    IBallLoggerBetService loggerBetService;
    @Autowired
    private BallCommissionStrategyServiceImpl commissionStrategyService;
    @Autowired
    IBallBetService betService;
    @Autowired
    IBallLoggerBackService loggerBackService;
//    @Autowired
//    IBallLoggerBackRechargeService loggerBackRechargeService;
    @Autowired
    IBallBalanceChangeService ballBalanceChangeService;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    IBallLoggerRechargeService loggerRechargeService;
    @Autowired
    IBallSystemConfigService systemConfigService;
    @Autowired
    IBallVipService vipService;
    @Autowired
    IBallBonusConfigService bonusConfigService;
    @Autowired
    IBallTodoService todoService;
    @Autowired
    IBallCommissionRechargeService commissionRechargeService;
    @Autowired
    IBallLoggerRebateService loggerRebateService;
    @Autowired
    IBallPaymentManagementService paymentManagementService;
    @Autowired
    IBallPlayerActiveService playerActiveService;
    @Override
    public void putMessage(MessageQueueDTO message) {
        redisUtil.rightSet(RedisKeyContant.LOGGER_QUEUE,message.toString());
    }

    @Override
    public void startQueue() {
        while (true){
//            Object o = redisUtil.lGetIndex(RedisKeyContant.LOGGER_QUEUE, 0);
            Object o = redisUtil.lpop(RedisKeyContant.LOGGER_QUEUE);
            if(o==null){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
                continue;
            }
            MessageQueueDTO message = null;
            try {
                message = JsonUtil.fromJson(o.toString(),MessageQueueDTO.class);
            } catch (IOException e) {
                e.printStackTrace();
                logger.info("??????????????????tojson{}",message);
            }
            try {
                switch (message.getType()){
                    case MessageQueueDTO.TYPE_LOG_LOGIN:
                        loginLog(message);
                        break;
                    case MessageQueueDTO.TYPE_LOG_OPER:
                        logOper(message);
                        break;
                    case MessageQueueDTO.TYPE_LOG_BET:
                        logBet(message);
                        break;
                    case MessageQueueDTO.TYPE_LOG_BET_BACK:
                        logBetBack(message);
                        break;
                    case MessageQueueDTO.TYPE_LOG_BET_BACK2:
                        logBetBack2(message);
                        break;
                    case MessageQueueDTO.TYPE_LOG_BET_BINGO:
                        logBetBingo(message);
                        break;
                    case MessageQueueDTO.TYPE_LOG_RECHARGE:
                        MessageQueueDTO finalMessage = message;
                        ThreadPoolUtil.execSaki(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    playerRecharge(finalMessage);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        break;
                    case MessageQueueDTO.TYPE_LOG_RECHARGE_LOG:
//            2022.12.17            playerRechargeSucc(message);

//                        playerRechargeLog(message);
                        break;
                    case MessageQueueDTO.TYPE_RECHARGE_FIRST:
                        playerRechargeFirst(message);
                        break;
                    case MessageQueueDTO.TYPE_LOG_RECHARGE_UP:
                        MessageQueueDTO finalMessage1 = message;
                        ThreadPoolUtil.execSaki(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    playerRechargeUp(finalMessage1);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        break;
                    case MessageQueueDTO.TYPE_RECHARGE_PARENT_BONUS:
                        MessageQueueDTO finalMessage2 = message;
                        ThreadPoolUtil.execSaki(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    rechargeParentBonus(finalMessage2);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        break;
                    case MessageQueueDTO.TYPE_BET_OPEN:
                        betOpen(message);
                        break;
                    case MessageQueueDTO.TYPE_BET_BINGO_HANDSUP:
                        betBingoHandsup(message);
                        break;
                    case MessageQueueDTO.TYPE_PLAYER_TG_CHAT:
                        sendMessageToPlayerChat(message);
                        break;
                    default:
                        break;
                }
            } catch (Exception ex){
                ex.printStackTrace();
                logger.error("??????????????????:{}",o);
            }
        }
    }

    private void logBetBack2(MessageQueueDTO message) throws IOException {
        // ????????????????????????
        // ??????????????????
        List<BallCommissionStrategy> commissionStrategies = commissionStrategyService.findByType(2);
        if(commissionStrategies==null||commissionStrategies.isEmpty()){
            return;
        }
        BallBet msgData = JsonUtil.fromJson(message.getData(),BallBet.class);
        Long aimPlayer = msgData.getPlayerId();
        BallBet bet = betService.findById(msgData.getId());
        BallPlayer player = basePlayerService.findById(bet.getPlayerId());
        List<CommissionRules> commissionRules = null;
        BallCommissionStrategy ballCommissionStrategy = commissionStrategies.get(0);
        try {
            commissionRules = JsonUtil.fromJsonToList(ballCommissionStrategy.getRules(), CommissionRules.class);
            Collections.sort(commissionRules, new Comparator<CommissionRules>() {
                @Override
                public int compare(CommissionRules o1, CommissionRules o2) {
                    return o1.getLevel()-o2.getLevel();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        //?????????
        for(CommissionRules comm:commissionRules){
            Double perDouble = TimeUtil.isDouble(comm.getCommission());
            if(perDouble<=0){
                continue;
            }
            //??????????????????
            if(comm.getLevel()==0){
                continue;
            }
            //???????????????,???????????????,????????????????????????
            if(player.getSuperiorId()==0||player.getSuperiorId()==null){
                break;
            }
            BallPlayer parentPlayer = basePlayerService.findById(player.getSuperiorId());
            if(parentPlayer==null){
                break;
            }
            if(parentPlayer.getId().equals(aimPlayer)){
                //?????????????????????????????????,????????? ??????ID,aimPlayerId,money
                Double per = BigDecimalUtil.div(perDouble,100);
                //???????????????????????????-????????????
                Double mul = Double.valueOf(BigDecimalUtil.mul(bet.getWinningAmount()-bet.getBetMoney(), per));
                long money = mul.longValue();
                if(money==0){
                    logger.info("??????????????????0.01,[{}]",mul);
                    return;
                }
                BallLoggerBack search = loggerBackService.search(BallLoggerBack.builder()
                        .playerId(aimPlayer)
                        .money(money)
                        .gameId(bet.getGameId())
                        .build());
                if(search!=null){
                    return;
                }
                BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
                bingoTo2(ballCommissionStrategy,bet,parentPlayer,perDouble,systemConfig);
                break;
            }
            player = parentPlayer;
        }
    }

    private void bingoTo2(BallCommissionStrategy comm, BallBet bet, BallPlayer player, Double perDouble, BallSystemConfig systemConfig) {
        Double per = BigDecimalUtil.div(perDouble,100);
        //???????????????????????????-????????????
        Double mul = Double.valueOf(BigDecimalUtil.mul(bet.getWinningAmount()-bet.getBetMoney(), per));
        long money = mul.longValue();
        if(money==0){
            logger.info("??????????????????0.01,[{}]",mul);
            return;
        }
        //??????????????????????????????????????????????????????
        //????????????????????????,????????????????????????????????????
        //        //??????????????????????????????????????????
        Integer switchRebate = systemConfig.getSwitchRebate();
        long week = TimeUtil.getBeginDayOfWeek().getTime();
        long configTime = 0;
        //??????????????????
        int nowWeek = TimeUtil.getNowWeek();
        if(nowWeek==1){
            nowWeek=7;
        }else{
            nowWeek-=1;
        }
        if(switchRebate==1){
            //?????????????????????????????????
            Integer rebateWeek = systemConfig.getRebateWeek();
            String rebateTime = systemConfig.getRebateTime();
            //?????????????????????>????????????,???????????????????????????
            if(rebateWeek>nowWeek){
                week = TimeUtil.getBeginDayOfLastWeek().getTime();
            }
            if(rebateWeek>1){
                configTime +=TimeUtil.TIME_ONE_DAY*(rebateWeek-1);
                configTime +=TimeUtil.hmsToMills(rebateTime);
            }
        }
        //???????????????????????????????????????
        if(bet.getSettlementTime() < week+configTime ){
            //?????????????????????????????????
            while(true){
                BallPlayer editPlayer = BallPlayer.builder()
                        .balance((player.getBalance()==null?0:player.getBalance())+money)
                        .cumulativeActivity((player.getCumulativeActivity()==null?0:player.getCumulativeActivity())+money)
                        .version(player.getVersion())
                        .build();
                editPlayer.setId(player.getId());
                boolean b = basePlayerService.editAndClearCache(editPlayer, player);
                if(b){
                    ballBalanceChangeService.insert(BallBalanceChange.builder()
                            .playerId(player.getId())
                            .accountType(player.getAccountType())
                            .userId(player.getUserId())
                            .username(player.getUsername())
                            .parentId(player.getSuperiorId())
                            .superTree(player.getSuperTree())
                            .balanceChangeType(5)
                            .createdAt(System.currentTimeMillis())
                            .dnedMoney(editPlayer.getBalance())
                            .initMoney(player.getBalance())
                            .changeMoney(money)
                            .orderNo(bet.getOrderNo())
                            .build());
                    //????????????
                    BallLoggerBack save = BallLoggerBack.builder()
                            .createdAt(System.currentTimeMillis())
                            .money(money)
                            .type(2)
                            .gameId(bet.getGameId())
                            .orderNo(String.valueOf(bet.getOrderNo()))
                            .status(2)
                            .accountType(player.getAccountType())
                            .playerId(player.getId())
                            .playerName(player.getUsername())
                            .superTree(player.getSuperTree())
                            .ymd(TimeUtil.longToStringYmd(System.currentTimeMillis()))
                            .build();
                    String superTree = player.getSuperTree();
                    if (superTree.equals("0")) {
                    } else if (superTree.equals("_")) {
                    } else {
                        String[] split = superTree.split("_");
                        try {
                            BallPlayer superPlayer = basePlayerService.findById(Long.parseLong(split[1]));
                            save.setTopUsername(superPlayer.getUsername());
                            if (split.length == 2) {
                                save.setFirstUsername(superPlayer.getUsername());
                            } else if (split.length > 2) {
                                BallPlayer firstPlayer = basePlayerService.findById(Long.parseLong(split[2]));
                                save.setFirstUsername(firstPlayer.getUsername());
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    loggerBackService.insert(save);
                    break;
                }else{
                    player = basePlayerService.findById(player.getId());
                }
            }
        }else{
            /*if(comm.getAutomaticDistribution()==0 && systemConfig.getSwitchRebate()==1)*/
            //???????????????,?????????????????????,????????????????????????
            BallLoggerBack save = BallLoggerBack.builder()
                    .createdAt(System.currentTimeMillis())
                    .ymd(TimeUtil.longToStringYmd(System.currentTimeMillis()))
                    .money(money)
                    .type(2)
                    .gameId(bet.getGameId())
                    .orderNo(String.valueOf(bet.getOrderNo()))
                    //????????????
                    .status(3)
                    .accountType(player.getAccountType())
                    .playerId(player.getId())
                    .playerName(player.getUsername())
                    .superTree(player.getSuperTree())
                    .build();
            String superTree = player.getSuperTree();
            if (superTree.equals("0")) {
            } else if (superTree.equals("_")) {
            } else {
                String[] split = superTree.split("_");
                try {
                    BallPlayer superPlayer = basePlayerService.findById(Long.parseLong(split[1]));
                    save.setTopUsername(superPlayer.getUsername());
                    if (split.length == 2) {
                        save.setFirstUsername(superPlayer.getUsername());
                    } else if (split.length > 2) {
                        BallPlayer firstPlayer = basePlayerService.findById(Long.parseLong(split[2]));
                        save.setFirstUsername(firstPlayer.getUsername());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            loggerBackService.insert(save);
        }
    }

    @Override
    public void startMessage(MessageQueueDTO message) {
        try {
            switch (message.getType()){
                case MessageQueueDTO.TYPE_LOG_RECHARGE:
                    ThreadPoolUtil.execSaki(() -> {
                        try {
                            playerRecharge(message);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    break;
                case MessageQueueDTO.TYPE_LOG_RECHARGE_UP:
                    ThreadPoolUtil.execSaki(() -> {
                        try {
                            playerRechargeUp(message);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    break;
                case MessageQueueDTO.TYPE_RECHARGE_PARENT_BONUS:
                    ThreadPoolUtil.execSaki(() -> {
                        try {
                            rechargeParentBonus(message);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    break;
                default:
                    break;
            }
        } catch (Exception ex){
            ex.printStackTrace();
            logger.error("??????????????????:{}",message);
        }
    }

    private void sendMessageToPlayerChat(MessageQueueDTO message) {
        playerActiveService.sendMessageToPlayer(message.getData());
    }

    private void playerRechargeFirst(MessageQueueDTO message) {
        try {
            BallLoggerRecharge saveChange = JsonUtil.fromJson(message.getData(),BallLoggerRecharge.class);
        } catch (IOException e) {
        }
    }

    private void betBingoHandsup(MessageQueueDTO message) throws IOException {
        BallBet bet = JsonUtil.fromJson(message.getData(), BallBet.class);
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        //?????????????????????
        Double betHandMoneyRate = BigDecimalUtil.div(systemConfig.getBetHandMoneyRate(),100);
        //?????????
        Long betRate = 0L;
        if(betHandMoneyRate>0){
            //?????????????????????????????? = ??????*??????    1*10000/100 = 100 = 1???
            Double rate = BigDecimalUtil.div(BigDecimalUtil.mul(betHandMoneyRate, bet.getWinningAmount()-bet.getBetMoney()),100);
            betRate = rate.longValue();
        }
        if(betRate>0){
            BallBet editBet = BallBet.builder()
                    .handMoney(betRate)
                    .build();
            editBet.setId(bet.getId());
            Boolean edit1 = betService.edit(editBet);
            if(!edit1){
                return;
            }
            BallPlayer player = basePlayerService.findById(bet.getPlayerId());
            while(true){
                BallPlayer edit = BallPlayer.builder()
                        .balance(player.getBalance()-betRate)
                        .version(player.getVersion())
                        .build();
                boolean b = basePlayerService.editAndClearCache(edit, player);
                if(b){
                    //???????????????
                    BallBalanceChange balanceChangea = BallBalanceChange.builder()
                            .playerId(player.getId())
                            .accountType(player.getAccountType())
                            .userId(player.getUserId())
                            .parentId(player.getSuperiorId())
                            .username(player.getUsername())
                            .superTree(player.getSuperTree())
                            .createdAt(System.currentTimeMillis())
                            .changeMoney(-betRate)
                            .initMoney(player.getBalance()-betRate)
                            .dnedMoney(player.getBalance()-betRate)
                            .balanceChangeType(20)
                            .orderNo(bet.getOrderNo())
                            .remark("")
                            .build();
                    ballBalanceChangeService.insert(balanceChangea);
                    break;
                }else{
                    player = basePlayerService.findById(player.getId());
                }
            }

        }
    }

    private void betOpen(MessageQueueDTO message) throws IOException {
        BallBet bet = JsonUtil.fromJson(message.getData(),BallBet.class);
        BallBalanceChange balanceChange = ballBalanceChangeService.findByOrderId(3,bet.getOrderNo());
        ballBalanceChangeService.edit(BallBalanceChange.builder()
                .id(balanceChange.getId())
                .frozenStatus(1)
                .build());

    }

    private void rechargeParentBonus(MessageQueueDTO message) throws IOException {
        BallBalanceChange saveChange = JsonUtil.fromJson(message.getData(),BallBalanceChange.class);
        //??????????????????????????????
        List<BallBonusConfig> firstRecharge = bonusConfigService.findByType(0);
        //?????????????????????
        Collections.sort(firstRecharge, (o1, o2) -> o2.getBonusMoney()-o1.getBonusMoney());
        for(BallBonusConfig item:firstRecharge){
            if(saveChange.getChangeMoney()>item.getBonusMoney()){
                //??????????????????
                BallPlayer player = basePlayerService.findById(saveChange.getPlayerId());
                if(player.getSuperiorId()==0||player.getSuperiorId()==null){
                    //????????????
                    break;
                }
                BallPlayer fromPlayer = basePlayerService.findById(player.getSuperiorId());
                BallTodo save = BallTodo.builder()
                        .playerId(player.getId())
                        .playerName(player.getUsername())
                        .bonusId(item.getId())
                        .fromId(fromPlayer.getId())
                        .fromName(fromPlayer.getUsername())
                        .bonusMoney(item.getBonusMoney())
                        .name("????????????")
                        .status(0)
                        .superTree(player.getSuperTree())
                        .build();
                String superTree = player.getSuperTree();
                setTopPlayerName(save, superTree);
                todoService.insert(save);
                break;
            }
        }
    }

    private void setTopPlayerName(BallTodo save, String superTree) {
        if (superTree.equals("0")) {
        } else if (superTree.equals("_")) {
        } else {
            String[] split = superTree.split("_");
            try {
                BallPlayer superPlayer = basePlayerService.findById(Long.parseLong(split[1]));
                save.setTopUsername(superPlayer.getUsername());
                if (split.length == 2) {
                    save.setFirstUsername(superPlayer.getUsername());
                } else if (split.length > 2) {
                    BallPlayer firstPlayer = basePlayerService.findById(Long.parseLong(split[2]));
                    save.setFirstUsername(firstPlayer.getUsername());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void playerRechargeUp(MessageQueueDTO message) throws IOException {
        BallPlayer paramPlayer = JsonUtil.fromJson(message.getData(),BallPlayer.class);
        BallPlayer player = basePlayerService.findByIdNoCache(paramPlayer.getId());
        int level = 0;
        List<BallVip> byAll = vipService.findByAll();
        Long cumulativeTopUp = player.getCumulativeTopUp()+player.getArtificialAdd();
        Long cumulativeReflect = player.getCumulativeReflect()+player.getArtificialSubtract();
        level = BallVipServiceImpl.checkPlayerVipLevel(byAll, player, level, cumulativeTopUp, cumulativeReflect);
//        System.out.println(player);
//        System.out.println(player.getVipLevel()+":"+level+":"+(player.getVipLevel()!=level));
        if(player.getVipLevel()!=level){
            while(true) {
                BallPlayer editPlayer = BallPlayer.builder()
                        .vipLevel(level)
                        .version(player.getVersion())
                        .build();
                if(player.getVipLevelMax()==null||player.getVipLevelMax()<level){
                    editPlayer.setVipLevelMax(level);
                }
                editPlayer.setId(player.getId());
                boolean b = basePlayerService.editAndClearCache(editPlayer, player);
                if(b){
                    break;
                }else{
                    player = basePlayerService.findById(player.getId());
                }
            }
        }
    }

//    private void playerRechargeSucc(MessageQueueDTO message) throws IOException {
//        BallLoggerRecharge ballLoggerRecharge =JsonUtil.fromJson(message.getData(),BallLoggerRecharge.class);
//        BallLoggerRecharge edit = BallLoggerRecharge.builder()
//                .id(ballLoggerRecharge.getId())
//                .moneySys(ballLoggerRecharge.getMoneySys())
//                .moneyDiscount(ballLoggerRecharge.getMoneyDiscount())
//                .updatedAt(System.currentTimeMillis())
//                .build();
//        if(StringUtils.isBlank(ballLoggerRecharge.getOperUser())){
//            ballLoggerRecharge.setOperUser("sys");
//        }
//        loggerRechargeService.edit(edit);
//    }

//    private void playerRechargeLog(MessageQueueDTO message) throws IOException {
//        BallBalanceChange saveChange = JsonUtil.fromJson(message.getData(),BallBalanceChange.class);
//        loggerRechargeService.insert(BallLoggerRecharge.builder()
//                //???????????????
//                .money(saveChange.getChangeMoney())
//                .status(2)
//                .type(2)
//                //??????
//                .moneyDiscount(saveChange.getDiscount())
//                .playerId(saveChange.getPlayerId())
//                .accountType(saveChange.getAccountType())
//                .userId(saveChange.getUserId())
//                .username(saveChange.getUsername())
//                .createdAt(System.currentTimeMillis())
//                .orderNo(saveChange.getOrderNo())
//                .build());
//    }

    private void playerRecharge(MessageQueueDTO message) throws IOException {
        BallLoggerRecharge saveChange = JsonUtil.fromJson(message.getData(),BallLoggerRecharge.class);
        Double money = saveChange.getMoneySys()/100d;
        BallCommissionRecharge commissionRecharge = commissionRechargeService.findOne();
        if(commissionRecharge.getStatus()!=1){
            return;
        }
        String rules = commissionRecharge.getRules();
        if(StringUtils.isBlank(rules)){
            return;
        }
        List<RechargeRebateDto> rechargeRebateDtos = JsonUtil.fromJsonToList(rules, RechargeRebateDto.class);
        if(rechargeRebateDtos==null||rechargeRebateDtos.isEmpty()){
            return;
        }
        BallPlayer player = basePlayerService.findById(saveChange.getPlayerId());
        if(player.getSuperiorId()==null || player.getSuperiorId()==0 ||player.getAccountType()==1){
            return;
        }
        BallPlayer parentPlayer = basePlayerService.findById(player.getSuperiorId());
        if(parentPlayer==null){
            return;
        }
        Collections.sort(rechargeRebateDtos, (o1, o2) -> (int) (o2.getMax()-o1.getMax()));
        for(RechargeRebateDto item:rechargeRebateDtos){
            if(money>=item.getMin()&&money<=item.getMax()){
                //?????????????????????,???????????????,??????????????????????????????
                rechargeTo(commissionRecharge,item,saveChange,parentPlayer,player);
                break;
            }
        }


        //        //????????????
//        List<BallCommissionStrategy> commissionStrategies = commissionStrategyService.findByType(3);
//        if(commissionStrategies==null||commissionStrategies.isEmpty()){
//            return;
//        }
//        BallPlayer player = basePlayerService.findById(saveChange.getPlayerId());
//        //?????????
//        BallCommissionStrategy ballCommissionStrategy = commissionStrategies.get(0);
//        List<CommissionRules> commissionRules = null;
//        try {
//            commissionRules = JsonUtil.fromJsonToList(ballCommissionStrategy.getRules(), CommissionRules.class);
//            Collections.sort(commissionRules, new Comparator<CommissionRules>() {
//                @Override
//                public int compare(CommissionRules o1, CommissionRules o2) {
//                    return o1.getLevel()-o2.getLevel();
//                }
//            });
//        } catch (IOException e) {
//            e.printStackTrace();
//            return;
//        }
//        for(CommissionRules comm:commissionRules){
//            Double perDouble = TimeUtil.isDouble(comm.getCommission());
//            if(perDouble<=0){
//                continue;
//            }
//            if(comm.getLevel()==0){
//                rechargeTo(ballCommissionStrategy, saveChange, player,perDouble);
//                continue;
//            }
//            //???????????????,???????????????,????????????????????????
//            if(player.getSuperiorId()==null || player.getSuperiorId()==0){
//                break;
//            }
//            BallPlayer parentPlayer = basePlayerService.findById(player.getSuperiorId());
//            rechargeTo(ballCommissionStrategy,saveChange,parentPlayer,perDouble);
//            player = parentPlayer;
//        }
    }

//    private void rechargeTo(BallCommissionRecharge comm, RechargeRebateDto rechargeRebate, BallLoggerRecharge change, BallPlayer parentPlayer, BallPlayer playerme) {
//        //???????????????????????????
//        Double per = BigDecimalUtil.div(Double.valueOf(rechargeRebate.getRate()),100);
//        Double mul = Double.valueOf(BigDecimalUtil.mul(change.getMoneySys(), per));
//        long money = mul.longValue();
//        if(!StringUtils.isBlank(rechargeRebate.getFixed())){
//            Double fixed = BigDecimalUtil.mul(Double.valueOf(rechargeRebate.getFixed()),BigDecimalUtil.PLAYER_MONEY_UNIT);
//            money+=fixed.longValue();
//        }
//        if(money==0){
//            logger.info("??????????????????0.01,[{}]",mul);
//            return;
//        }
//        BallLoggerBackRecharge save = BallLoggerBackRecharge.builder()
//                .createdAt(System.currentTimeMillis())
//                .money(money)
//                .playerId(parentPlayer.getId())
//                .playerName(parentPlayer.getUsername())
//                .fromId(playerme.getId())
//                .fromName(playerme.getUsername())
//                .moneyRecharge(change.getMoneySys())
//                .fixed(rechargeRebate.getFixed())
//                .rate(rechargeRebate.getRate())
//                .accountType(parentPlayer.getAccountType())
//                .superTree(parentPlayer.getSuperTree())
//                .orderNo(change.getOrderNo().toString())
//                .vipRank(parentPlayer.getVipRank())
//                .build();
//        String superTree = parentPlayer.getSuperTree();
//        if (superTree.equals("0")) {
//        } else if (superTree.equals("_")) {
//        } else {
//            String[] split = superTree.split("_");
//            try {
//                BallPlayer superPlayer = basePlayerService.findById(Long.parseLong(split[1]));
//                save.setTopUsername(superPlayer.getUsername());
//                if (split.length == 2) {
//                    save.setFirstUsername(superPlayer.getUsername());
//                } else if (split.length > 2) {
//                    BallPlayer firstPlayer = basePlayerService.findById(Long.parseLong(split[2]));
//                    save.setFirstUsername(firstPlayer.getUsername());
//                }
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }
//        save.setPayTypeOnff(change.getType());
//        //????????????,????????????,????????????
//        if(comm.getAutomaticDistribution()==1){
//            //????????????
//            while(true){
//                BallPlayer editPlayer = BallPlayer.builder()
//                        .balance(parentPlayer.getBalance()+money)
//                        .version(parentPlayer.getVersion())
//                        .build();
//                editPlayer.setId(parentPlayer.getId());
////                long a = parentPlayer.getCumulativeDiscount()==null?0:parentPlayer.getCumulativeDiscount();
////                editPlayer.setCumulativeDiscount(a+money);
//                boolean b = basePlayerService.editAndClearCache(editPlayer, parentPlayer);
//                if(b){
//                    ballBalanceChangeService.insert(BallBalanceChange.builder()
//                            .playerId(parentPlayer.getId())
//                            .accountType(parentPlayer.getAccountType())
//                            .userId(parentPlayer.getUserId())
//                            .username(parentPlayer.getUsername())
//                            .parentId(parentPlayer.getSuperiorId())
//                            .superTree(parentPlayer.getSuperTree())
//                            .balanceChangeType(21)
//                            .createdAt(System.currentTimeMillis())
//                            .dnedMoney(editPlayer.getBalance())
//                            .initMoney(parentPlayer.getBalance())
//                            .changeMoney(money)
//                            .build());
//                    //????????????sd
//                    save.setStatus(2);
//                    loggerBackRechargeService.insert(save);
//                    break;
//                }else{
//                    parentPlayer = basePlayerService.findById(parentPlayer.getId());
//                }
//            }
//        }else{
//            //????????????
//            //????????????
//            save.setStatus(1);
//            loggerBackRechargeService.insert(save);
//        }
//    }
    private void rechargeTo(BallCommissionRecharge comm, RechargeRebateDto rechargeRebate, BallLoggerRecharge loggerRecharge, BallPlayer parentPlayer, BallPlayer playerme) {
        //????????? 1+1????????????(0|1)+0
        if(loggerRecharge.getMoneyMin()==0&&comm.getAutoSettleFirst()==1){
            //????????????????????????????????????
            logger.info("???????????????????????????????????????");
            return;
        }
        //???????????????????????????
        Double per = BigDecimalUtil.div(Double.valueOf(rechargeRebate.getRate()),100);
        Double mul = Double.valueOf(BigDecimalUtil.mul(loggerRecharge.getMoneySys(), per));
        long money = mul.longValue();
        if(!StringUtils.isBlank(rechargeRebate.getFixed())){
            Double fixed = BigDecimalUtil.mul(Double.valueOf(rechargeRebate.getFixed()),BigDecimalUtil.PLAYER_MONEY_UNIT);
            money+=fixed.longValue();
        }
        if(money==0){
            logger.info("??????????????????0.01,[{}]",mul);
            return;
        }
        BallPaymentManagement paymentManagement = paymentManagementService.findById(loggerRecharge.getPayId());
        BallLoggerRebate save = BallLoggerRebate.builder()
                .money(money)
                .playerId(parentPlayer.getId())
                .playerName(parentPlayer.getUsername())
                .moneyReal(loggerRecharge.getMoneySys())
                .moneyUsdt(loggerRecharge.getMoneyReal())
                .fixed(rechargeRebate.getFixed())
                .rate(rechargeRebate.getRate())
                .accountType(parentPlayer.getAccountType())
                .superTree(parentPlayer.getSuperTree())
                .orderNo(loggerRecharge.getOrderNo())
                .rateUsdt(paymentManagement.getRate())
                .type(99)
                .payTypeOnff(loggerRecharge.getType())
                .fromName(playerme.getUsername())
                .build();
        save.setCreatedAt(loggerRecharge.getUpdatedAt());
        String superTree = parentPlayer.getSuperTree();
        if (superTree.equals("0")) {
        } else if (superTree.equals("_")) {
        } else {
            String[] split = superTree.split("_");
            try {
                BallPlayer superPlayer = basePlayerService.findById(Long.parseLong(split[1]));
                save.setTopUsername(superPlayer.getUsername());
                if (split.length == 2) {
                    save.setFirstUsername(superPlayer.getUsername());
                } else if (split.length > 2) {
                    BallPlayer firstPlayer = basePlayerService.findById(Long.parseLong(split[2]));
                    save.setFirstUsername(firstPlayer.getUsername());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        save.setPayTypeOnff(loggerRecharge.getType());

//        if(loggerRecharge.getMoneyMin()==1&&comm.getAutoSettleFirst()==0){
//            //????????????????????????????????????
//            return;
//        }
        //????????????????????????????????????2?????????
        if(comm.getAutomaticDistribution()==1){
            //????????????
            while(true){
                BallPlayer editPlayer = BallPlayer.builder()
                        .balance(parentPlayer.getBalance()+money)
                        .version(parentPlayer.getVersion())
                        .build();
                editPlayer.setId(parentPlayer.getId());
//                long a = parentPlayer.getCumulativeDiscount()==null?0:parentPlayer.getCumulativeDiscount();
//                editPlayer.setCumulativeDiscount(a+money);
                boolean b = basePlayerService.editAndClearCache(editPlayer, parentPlayer);
                if(b){
                    ballBalanceChangeService.insert(BallBalanceChange.builder()
                            .playerId(parentPlayer.getId())
                            .accountType(parentPlayer.getAccountType())
                            .userId(parentPlayer.getUserId())
                            .username(parentPlayer.getUsername())
                            .parentId(parentPlayer.getSuperiorId())
                            .superTree(parentPlayer.getSuperTree())
                            .balanceChangeType(21)
                            .createdAt(System.currentTimeMillis())
                            .dnedMoney(editPlayer.getBalance())
                            .initMoney(parentPlayer.getBalance())
                            .changeMoney(money)
                            .build());
                    //????????????sd
                    save.setStatus(2);
                    loggerRebateService.insert(save);
                    break;
                }else{
                    parentPlayer = basePlayerService.findById(parentPlayer.getId());
                }
            }
        }else{
            //????????????
            //????????????
            save.setStatus(1);
            loggerRebateService.insert(save);
        }
    }

    private void logBetBingo(MessageQueueDTO message) {
        // ??????????????????
        List<BallCommissionStrategy> commissionStrategies = commissionStrategyService.findByType(2);
        if(commissionStrategies==null||commissionStrategies.isEmpty()){
            return;
        }
        Long betId = Long.parseLong(message.getData().toString());
        BallBet bet = betService.findById(betId);
        BallPlayer player = basePlayerService.findById(bet.getPlayerId());
        List<CommissionRules> commissionRules = null;
        BallCommissionStrategy ballCommissionStrategy = commissionStrategies.get(0);
        try {
            commissionRules = JsonUtil.fromJsonToList(ballCommissionStrategy.getRules(), CommissionRules.class);
            Collections.sort(commissionRules, new Comparator<CommissionRules>() {
                @Override
                public int compare(CommissionRules o1, CommissionRules o2) {
                    return o1.getLevel()-o2.getLevel();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        //?????????
        for(CommissionRules comm:commissionRules){
            Double perDouble = TimeUtil.isDouble(comm.getCommission());
            if(perDouble<=0){
                continue;
            }
            //??????????????????
            if(comm.getLevel()==0){
                bingoTo(ballCommissionStrategy, bet, player,perDouble,systemConfig);
                continue;
            }
            //???????????????,???????????????,????????????????????????
            if(player.getSuperiorId()==0||player.getSuperiorId()==null){
                break;
            }
            BallPlayer parentPlayer = basePlayerService.findById(player.getSuperiorId());
            if(parentPlayer==null){
                break;
            }
            bingoTo(ballCommissionStrategy,bet,parentPlayer,perDouble,systemConfig);
            player = parentPlayer;
        }
    }

    private void bingoTo(BallCommissionStrategy comm, BallBet bet, BallPlayer player, Double perDouble, BallSystemConfig systemConfig) {
        Double per = BigDecimalUtil.div(perDouble,100);
        //???????????????????????????-????????????
        Double mul = Double.valueOf(BigDecimalUtil.mul(bet.getWinningAmount()-bet.getBetMoney(), per));
        long money = mul.longValue();
        if(money==0){
            logger.info("??????????????????0.01,[{}]",mul);
            return;
        }
        //????????????,????????????,????????????
        if(comm.getAutomaticDistribution()==1 && systemConfig.getSwitchRebate()==0){
            //????????????,???????????????????????????????????????????????????
            while(true){
                BallPlayer editPlayer = BallPlayer.builder()
                        .balance((player.getBalance()==null?0:player.getBalance())+money)
                        .cumulativeActivity((player.getCumulativeActivity()==null?0:player.getCumulativeActivity())+money)
                        .version(player.getVersion())
                        .build();
                editPlayer.setId(player.getId());
                boolean b = basePlayerService.editAndClearCache(editPlayer, player);
                if(b){
                    ballBalanceChangeService.insert(BallBalanceChange.builder()
                            .playerId(player.getId())
                            .accountType(player.getAccountType())
                            .userId(player.getUserId())
                            .username(player.getUsername())
                            .parentId(player.getSuperiorId())
                            .superTree(player.getSuperTree())
                            .balanceChangeType(5)
                            .createdAt(System.currentTimeMillis())
                            .dnedMoney(editPlayer.getBalance())
                            .initMoney(player.getBalance())
                            .changeMoney(money)
                            .orderNo(bet.getOrderNo())
                            .build());
                    //????????????
                    BallLoggerBack save = BallLoggerBack.builder()
                            .createdAt(System.currentTimeMillis())
                            .money(money)
                            .type(2)
                            .gameId(bet.getGameId())
                            .orderNo(String.valueOf(bet.getOrderNo()))
                            .status(2)
                            .accountType(player.getAccountType())
                            .playerId(player.getId())
                            .playerName(player.getUsername())
                            .superTree(player.getSuperTree())
                            .ymd(TimeUtil.longToStringYmd(System.currentTimeMillis()))
                            .build();
                    String superTree = player.getSuperTree();
                    if (superTree.equals("0")) {
                    } else if (superTree.equals("_")) {
                    } else {
                        String[] split = superTree.split("_");
                        try {
                            BallPlayer superPlayer = basePlayerService.findById(Long.parseLong(split[1]));
                            save.setTopUsername(superPlayer.getUsername());
                            if (split.length == 2) {
                                save.setFirstUsername(superPlayer.getUsername());
                            } else if (split.length > 2) {
                                BallPlayer firstPlayer = basePlayerService.findById(Long.parseLong(split[2]));
                                save.setFirstUsername(firstPlayer.getUsername());
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    loggerBackService.insert(save);
                    break;
                }else{
                    player = basePlayerService.findById(player.getId());
                }
            }
        }else if(comm.getAutomaticDistribution()==1 && systemConfig.getSwitchRebate()==1){
            //?????????????????????,????????????,???????????????????????????
            //?????????????????????????????????1????????????????????????????????????
            BallLoggerBack save = BallLoggerBack.builder()
                    .createdAt(System.currentTimeMillis())
                    .ymd(TimeUtil.longToStringYmd(System.currentTimeMillis()))
                    .money(money)
                    .gameId(bet.getGameId())
                    .orderNo(String.valueOf(bet.getOrderNo()))
                    .type(2)
                    //???????????????
                    .status(3)
                    .accountType(player.getAccountType())
                    .playerId(player.getId())
                    .playerName(player.getUsername())
                    .superTree(player.getSuperTree())
                    .build();
            String superTree = player.getSuperTree();
            if (superTree.equals("0")) {
            } else if (superTree.equals("_")) {
            } else {
                String[] split = superTree.split("_");
                try {
                    BallPlayer superPlayer = basePlayerService.findById(Long.parseLong(split[1]));
                    save.setTopUsername(superPlayer.getUsername());
                    if (split.length == 2) {
                        save.setFirstUsername(superPlayer.getUsername());
                    } else if (split.length > 2) {
                        BallPlayer firstPlayer = basePlayerService.findById(Long.parseLong(split[2]));
                        save.setFirstUsername(firstPlayer.getUsername());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            loggerBackService.insert(save);
        }else /*if(comm.getAutomaticDistribution()==0 && systemConfig.getSwitchRebate()==1)*/{
            //???????????????,?????????????????????,????????????????????????
            BallLoggerBack save = BallLoggerBack.builder()
                    .createdAt(System.currentTimeMillis())
                    .ymd(TimeUtil.longToStringYmd(System.currentTimeMillis()))
                    .money(money)
                    .type(2)
                    .gameId(bet.getGameId())
                    .orderNo(String.valueOf(bet.getOrderNo()))
                    //????????????
                    .status(3)
                    .accountType(player.getAccountType())
                    .playerId(player.getId())
                    .playerName(player.getUsername())
                    .superTree(player.getSuperTree())
                    .build();
            String superTree = player.getSuperTree();
            if (superTree.equals("0")) {
            } else if (superTree.equals("_")) {
            } else {
                String[] split = superTree.split("_");
                try {
                    BallPlayer superPlayer = basePlayerService.findById(Long.parseLong(split[1]));
                    save.setTopUsername(superPlayer.getUsername());
                    if (split.length == 2) {
                        save.setFirstUsername(superPlayer.getUsername());
                    } else if (split.length > 2) {
                        BallPlayer firstPlayer = basePlayerService.findById(Long.parseLong(split[2]));
                        save.setFirstUsername(firstPlayer.getUsername());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            loggerBackService.insert(save);
        }
    }

    private void logBetBack(MessageQueueDTO message) {
        // ????????????????????????
        List<BallCommissionStrategy> commissionStrategies = commissionStrategyService.findByType(1);
        if(commissionStrategies==null||commissionStrategies.isEmpty()){
            return;
        }
        Long betId = Long.parseLong(message.getData().toString());
        BallBet bet = betService.findById(betId);
        BallPlayer player = basePlayerService.findById(bet.getPlayerId());
        List<CommissionRules> commissionRules = null;
        BallCommissionStrategy ballCommissionStrategy = commissionStrategies.get(0);
        try {
            commissionRules = JsonUtil.fromJsonToList(ballCommissionStrategy.getRules(), CommissionRules.class);
            Collections.sort(commissionRules, new Comparator<CommissionRules>() {
                @Override
                public int compare(CommissionRules o1, CommissionRules o2) {
                    return o1.getLevel()-o2.getLevel();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        for(CommissionRules comm:commissionRules){
            Double perDouble = TimeUtil.isDouble(comm.getCommission());
            if(perDouble<=0){
                continue;
            }
            //??????????????????
            if(comm.getLevel()==0){
                backTo(ballCommissionStrategy, bet, player,perDouble);
                continue;
            }
            //???????????????,???????????????,????????????????????????
            if(player.getSuperiorId()==null||player.getSuperiorId()==0){
                break;
            }
            BallPlayer parentPlayer = basePlayerService.findById(player.getSuperiorId());
            backTo(ballCommissionStrategy,bet,parentPlayer, perDouble);
            player = parentPlayer;
        }
    }

    private void backTo(BallCommissionStrategy comm, BallBet bet, BallPlayer player, Double perDouble) {
        Double per = BigDecimalUtil.div(perDouble,100);
        Double mul = Double.valueOf(BigDecimalUtil.mul(bet.getBetMoney(), per));
        long money = mul.longValue();
        if(money==0){
            logger.info("??????????????????0.01,[{}]",mul);
            return;
        }
        //????????????,????????????,????????????
        if(comm.getAutomaticDistribution()==1){
            //????????????
            while(true){
                BallPlayer editPlayer = BallPlayer.builder()
                        .balance(player.getBalance()+money)
                        .cumulativeActivity(player.getCumulativeActivity()+money)
                        .version(player.getVersion())
                        .build();
                editPlayer.setId(player.getId());
                boolean b = basePlayerService.editAndClearCache(editPlayer, player);
                if(b){
                    ballBalanceChangeService.insert(BallBalanceChange.builder()
                            .playerId(player.getId())
                            .accountType(player.getAccountType())
                            .userId(player.getUserId())
                            .username(player.getUsername())
                            .parentId(player.getSuperiorId())
                            .superTree(player.getSuperTree())
                            .balanceChangeType(22)
                            .createdAt(System.currentTimeMillis())
                            .dnedMoney(editPlayer.getBalance())
                            .initMoney(player.getBalance())
                            .changeMoney(money)
                            .orderNo(bet.getOrderNo())
                            .build());
                    //????????????
                    loggerBackService.insert(BallLoggerBack.builder()
                            .createdAt(System.currentTimeMillis())
                            .ymd(TimeUtil.longToStringYmd(System.currentTimeMillis()))
                            .money(money)
                            .type(1)
                            .status(2)
                            .accountType(player.getAccountType())
                            .playerId(player.getId())
                            .orderNo(String.valueOf(bet.getOrderNo()))
                            .build());
                    break;
                }else{
                    player = basePlayerService.findById(player.getId());
                }
            }
        }else{
            //????????????
            //????????????
            loggerBackService.insert(BallLoggerBack.builder()
                    .createdAt(System.currentTimeMillis())
                    .ymd(TimeUtil.longToStringYmd(System.currentTimeMillis()))
                    .money(money)
                    .type(1)
                    .status(3)
                    .accountType(player.getAccountType())
                    .playerId(player.getId())
                    .orderNo(String.valueOf(bet.getOrderNo()))
                    .build());
        }
    }

    private void logBet(MessageQueueDTO message) throws IOException {
        MessageQueueBet data = JsonUtil.fromJson(message.getData(),MessageQueueBet.class);
        String superName = "";
        BallPlayer ballPlayer = data.getBallPlayer();
        if(!StringUtils.isBlank(ballPlayer.getSuperTree())){
            //?????????
            superName = getSuperPlayerName(superName, ballPlayer);
        }
        loggerBetService.insert(BallLoggerBet.builder()
                .createdAt(System.currentTimeMillis())
                .betIp(data.getIp())
                .betContent(data.getBetContent())
                .betOrderNo(data.getOrderId())
                .playerName(ballPlayer.getUsername())
                .playerId(ballPlayer.getId())
                .accountType(ballPlayer.getAccountType())
                .superPlayerName(superName)
                .build());
        //??????????????????
        if(ballPlayer.getActived()==1){
            //??????????????????????????????????????????????????????
            return;
        }
        if(ballPlayer.getSuperiorId()==0||ballPlayer.getSuperiorId()==null){
            //????????????
            return;
        }
        String key = RedisKeyContant.PLAYER_BET_ACTIVITY_DAYS+ballPlayer.getId();
        Object o = redisUtil.get(key);
        if(o==null){
            List<BetActivityDays> bets = new ArrayList<>();
            bets.add(BetActivityDays.builder()
                    .betTime(System.currentTimeMillis())
                    .build());
            try {
                //????????????24??????,?????????24????????????????????????????????????
                redisUtil.set(key,JsonUtil.toJson(bets),TimeUtil.TIME_ONE_DAY/1000);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }else{
            //????????????????????????????????????
            try {
                List<BetActivityDays> betActivityDays = JsonUtil.fromJsonToList(o.toString(), BetActivityDays.class);
                Collections.sort(betActivityDays, (o1, o2) -> (int) (o2.getBetTime()-o1.getBetTime()));
                BetActivityDays item0 = betActivityDays.get(0);
                if(System.currentTimeMillis()-item0.getBetTime()<TimeUtil.TIME_ONE_DAY){
                    betActivityDays.add(BetActivityDays.builder()
                            .betTime(System.currentTimeMillis())
                            .build());
                    redisUtil.set(key,JsonUtil.toJson(betActivityDays),TimeUtil.TIME_ONE_DAY/1000);
                    //????????????????????????????????????
                    List<BallBonusConfig> byType = bonusConfigService.findByType(1);
                    for(BallBonusConfig item:byType){
                        if(betActivityDays.size()>=item.getActivityDay()){
                            //???????????????????????????
                            BallPlayer fromPlayer = basePlayerService.findById(ballPlayer.getId());
                            BallPlayer editFrom = BallPlayer.builder()
                                    .actived(1)
                                    .build();
                            editFrom.setId(fromPlayer.getId());
                            basePlayerService.editAndClearCache(editFrom,fromPlayer);
                            //?????????????????????+1
                            BallPlayer parentPlayer = basePlayerService.findById(fromPlayer.getSuperiorId());
                            BallPlayer editParent = BallPlayer.builder()
                                    .build();
                            while (true){
                                editParent.setId(parentPlayer.getId());
                                editParent.setVersion(parentPlayer.getVersion());
                                if(parentPlayer.getInvitationCount()!=null){
                                    editParent.setInvitationCount(parentPlayer.getInvitationCount()+1);
                                }else{
                                    editParent.setInvitationCount(1);
                                }
                                boolean b = basePlayerService.editAndClearCache(editParent, parentPlayer);
                                if(b){
                                    break;
                                }else{
                                    parentPlayer = basePlayerService.findById(parentPlayer.getId());
                                }
                            }
                            //????????????????????????
                            if(editParent.getInvitationCount()>=item.getBonusAim()){
                                BallTodo save = BallTodo.builder()
                                        .playerId(parentPlayer.getId())
                                        .playerName(parentPlayer.getUsername())
                                        .bonusId(item.getId())
                                        .fromId(fromPlayer.getId())
                                        .fromName(fromPlayer.getUsername())
                                        .bonusMoney(item.getBonusMoney())
                                        .name("????????????")
                                        .status(0)
                                        .superTree(parentPlayer.getSuperTree())
                                        .build();
                                String superTree = parentPlayer.getSuperTree();
                                setTopPlayerName(save, superTree);
                                todoService.insert(save);
                            }
                            break;
                        }
                    }
                }else{
                    //??????????????????,????????????
                    List<BetActivityDays> bets = new ArrayList<>();
                    bets.add(BetActivityDays.builder()
                            .betTime(System.currentTimeMillis())
                            .build());
                    try {
                        redisUtil.set(key,JsonUtil.toJson(bets),TimeUtil.TIME_ONE_DAY/1000);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getSuperPlayerName(String superName, BallPlayer ballPlayer) {
        if(!StringUtils.isBlank(ballPlayer.getSuperTree()) || !("_".equals(ballPlayer.getSuperTree()))){
            //?????????
            String superTree =ballPlayer.getSuperTree().startsWith("_")?ballPlayer.getSuperTree().substring(1):ballPlayer.getSuperTree();
            String[] superId = superTree.split("_");
            try {
                BallPlayer superPlayer = basePlayerService.findById(Long.parseLong(superId[0]));
                superName = superPlayer.getUsername();
            }catch (Exception ex){
            }
        }
        return superName;
    }

    private void logOper(MessageQueueDTO message) throws IOException {
        MessageQueueOper data =JsonUtil.fromJson(message.getData(),MessageQueueOper.class);
        loggerOperService.insert(BallLoggerOper.builder()
                .createdAt(System.currentTimeMillis())
                .ip(data.getIp())
                .mainFunc(data.getMainOper())
                .subFunc(data.getSubOper())
                .remark(data.getRemark())
                .username(data.getUsername())
                .build());
    }

    private void loginLog(MessageQueueDTO message) throws IOException {
        MessageQueueLogin data = JsonUtil.fromJson(message.getData(),MessageQueueLogin.class);
        BallPlayer ballPlayer = data.getBallPlayer();
        String superName = "";
        superName = getSuperPlayerName(superName, ballPlayer);
        String ipAddr = GeoLiteUtil.getIpAddr(data.getIp());
        //????????????
        try {
            loggerService.insert(BallLoggerLogin.builder()
                    .devices(data.getDevice())
                    .createdAt(System.currentTimeMillis())
                    .ip(data.getIp())
                    .ipAddr(ipAddr)
                    .playerName(ballPlayer.getUsername())
                    .superPlayerName(superName)
                    .build());
        }catch (Exception ex){
            ex.printStackTrace();
        }
//        String ips = ballPlayer.getLoginIps();
//        String contry = ballPlayer.getLoginContry();
        //????????????????????????IP
        try {
            BallPlayer edit = BallPlayer.builder()
                    //????????????IP
                    .theLastIp(ballPlayer.getTheNewIp())
                    //????????????IP
                    .theNewIp(data.getIp())
                    .theNewLoginTime(System.currentTimeMillis())
                    .build();
            edit.setId(ballPlayer.getId());
            BallLoggerLogin loginLogger = loggerService.search(ballPlayer.getUsername(), ipAddr);
            edit.setLoginContry(ipAddr+"("+loginLogger.getId()+")");
            basePlayerService.editAndClearCache(edit,ballPlayer);
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }
}
