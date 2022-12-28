package com.oxo.ball.service.impl.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.oxo.ball.bean.dao.*;
import com.oxo.ball.bean.dto.queue.MessageQueueDTO;
import com.oxo.ball.bean.dto.req.admin.QueryActivePlayerRequest;
import com.oxo.ball.bean.dto.req.report.ReportDataRequest;
import com.oxo.ball.bean.dto.req.report.ReportStandardRequest;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.bean.dto.resp.admin.ProxyStatis2Dto;
import com.oxo.ball.bean.dto.resp.admin.ProxyStatis3Dto;
import com.oxo.ball.bean.dto.resp.admin.ProxyStatisDto;
import com.oxo.ball.bean.dto.resp.report.ReportStandardPlayerDTO;
import com.oxo.ball.contant.LogsContant;
import com.oxo.ball.contant.RedisKeyContant;
import com.oxo.ball.mapper.BallBetMapper;
import com.oxo.ball.service.IBasePlayerService;
import com.oxo.ball.service.IMessageQueueService;
import com.oxo.ball.service.admin.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxo.ball.service.player.IPlayerBetService;
import com.oxo.ball.service.player.IPlayerGameService;
import com.oxo.ball.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.*;

/**
 * <p>
 * 下注 服务实现类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Service
public class BallBetServiceImpl extends ServiceImpl<BallBetMapper, BallBet> implements IBallBetService {
    private Logger apiLog = LoggerFactory.getLogger(LogsContant.API_LOG);

    @Autowired
    IPlayerBetService playerBetService;
    @Autowired
    IBasePlayerService basePlayerService;
    @Autowired
    IBallGameService gameService;
    @Autowired
    IPlayerGameService playerGameService;
    @Autowired
    IBallGameLossPerCentService lossPerCentService;
    @Autowired
    private IBallBalanceChangeService ballBalanceChangeService;
    @Autowired
    IMessageQueueService messageQueueService;
    @Autowired
    IBallLoggerBackService loggerBackService;
    @Autowired
    IBallVipService vipService;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    IApiService apiService;
    @Autowired
    BallBetMapper mapper;
    @Override
    public SearchResponse<BallBet> search(BallBet queryParam, Integer pageNo, Integer pageSize) {
        SearchResponse<BallBet> response = new SearchResponse<>();
        Page<BallBet> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallBet> query = new QueryWrapper<>();
        BallPlayer playerTree = null;
        if (queryParam.getOrderNo() != null) {
            query.eq("order_no", queryParam.getOrderNo());
        }
        if (queryParam.getBetType() != null) {
            query.eq("bet_type", queryParam.getBetType());
        }
        if (queryParam.getEven() != null) {
            query.eq("even", queryParam.getEven());
        }
        if (queryParam.getStatus() != null) {
            query.eq("status", queryParam.getStatus());
        }
        if (queryParam.getStatusOpen() != null) {
            query.eq("status_open", queryParam.getStatusOpen());
        }
        if (queryParam.getStatusSettlement() != null) {
            query.eq("status_settlement", queryParam.getStatusSettlement());
        }
        if(queryParam.getGameId()!=null){
            query.eq("game_id",queryParam.getGameId());
        }
        if (queryParam.getUserId() != null) {
            if (queryParam.getTreeType() == null) {
                query.eq("user_id", queryParam.getUserId());
            } else {
                playerTree = basePlayerService.findByUserId(queryParam.getUserId());
            }
        }
        if (!StringUtils.isBlank(queryParam.getUsername())) {
            if (queryParam.getTreeType() == null) {
                query.eq("username", queryParam.getUsername());
            } else {
                playerTree = basePlayerService.findByUsername(queryParam.getUsername());
            }
        }

        if (queryParam.getTreeType() != null) {
            if (playerTree != null) {
                //要查直属下级,或者全部下级
                if (queryParam.getTreeType() == 1) {
                    //查直属
                    query.eq("superior_id", playerTree.getId());
                } else if (queryParam.getTreeType() == 2) {
                    //查全部下级
                    query.likeRight("super_tree", playerTree.getSuperTree() + playerTree.getId() + "\\_");
                }
            }
        }
        if(queryParam.getSettlementTime()!=null){
            query.ge("created_at",queryParam.getSettlementTime());
        }
        if (!StringUtils.isBlank(queryParam.getBetBegin())) {
            try {
                long timeStamp = TimeUtil.stringToTimeStamp(queryParam.getBetBegin(), TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS);
                query.ge("created_at", timeStamp);
            } catch (ParseException e) {
                try {
                    long timeStamp = TimeUtil.stringToTimeStamp(queryParam.getBetBegin(), TimeUtil.TIME_YYYY_MM_DD);
                    query.ge("created_at", timeStamp);
                } catch (ParseException e1) {
                }
            }
        }
        if (!StringUtils.isBlank(queryParam.getBetEnd())) {
            try {
                long timeStamp = TimeUtil.stringToTimeStamp(queryParam.getBetEnd(), TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS);
                query.le("created_at", timeStamp);
            } catch (ParseException e) {
                try {
                    long timeStamp = TimeUtil.stringToTimeStamp(queryParam.getBetEnd(), TimeUtil.TIME_YYYY_MM_DD);
                    query.le("created_at", timeStamp+TimeUtil.TIME_ONE_DAY);
                } catch (ParseException e1) {
                }
            }
        }
        if (!StringUtils.isBlank(queryParam.getStartBegin())) {
            try {
                long timeStamp = TimeUtil.stringToTimeStamp(queryParam.getStartBegin(), TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS);
                query.ge("start_time", timeStamp);
            } catch (ParseException e) {
                try {
                    long timeStamp = TimeUtil.stringToTimeStamp(queryParam.getStartBegin(), TimeUtil.TIME_YYYY_MM_DD);
                    query.ge("start_time", timeStamp);
                } catch (ParseException e1) {
                }
            }
        }
        if (!StringUtils.isBlank(queryParam.getStartEnd())) {
            try {
                long timeStamp = TimeUtil.stringToTimeStamp(queryParam.getStartEnd(), TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS);
                query.le("start_time", timeStamp);
            } catch (ParseException e) {
                try {
                    long timeStamp = TimeUtil.stringToTimeStamp(queryParam.getStartEnd(), TimeUtil.TIME_YYYY_MM_DD);
                    query.le("start_time", timeStamp+TimeUtil.TIME_ONE_DAY);
                } catch (ParseException e1) {
                }
            }
        }
        if (!StringUtils.isBlank(queryParam.getSettlementBegin())) {
            try {
                long timeStamp = TimeUtil.stringToTimeStamp(queryParam.getSettlementBegin(), TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS);
                query.ge("settlement_time", timeStamp);
            } catch (ParseException e) {
                try {
                    long timeStamp = TimeUtil.stringToTimeStamp(queryParam.getSettlementBegin(), TimeUtil.TIME_YYYY_MM_DD);
                    query.ge("settlement_time", timeStamp);
                } catch (ParseException e1) {
                }
            }
        }
        if (!StringUtils.isBlank(queryParam.getSettlementEnd())) {
            try {
                long timeStamp = TimeUtil.stringToTimeStamp(queryParam.getSettlementEnd(), TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS);
                query.le("settlement_time", timeStamp);
            } catch (ParseException e) {
                try {
                    long timeStamp = TimeUtil.stringToTimeStamp(queryParam.getSettlementEnd(), TimeUtil.TIME_YYYY_MM_DD);
                    query.le("settlement_time", timeStamp+TimeUtil.TIME_ONE_DAY);
                } catch (ParseException e1) {
                }
            }
        }

        //先ID降序再top升序
        query.orderByDesc("id");
        IPage<BallBet> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public SearchResponse<BallBet> search(ReportDataRequest reportDataRequest, Integer pageNo, Integer pageSize) {
        SearchResponse<BallBet> response = new SearchResponse<>();
        Page<BallBet> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallBet> query = new QueryWrapper<>();
        query.eq("account_type", 2);
        //已确认
        query.eq("status", 1);
        //已结算
        query.eq("status_settlement", 1);
        BallPlayerServiceImpl.queryByTime(query, reportDataRequest.getTime(), reportDataRequest.getBegin(), reportDataRequest.getEnd());
        IPage<BallBet> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public BallBet findById(Long id) {
        return getById(id);
    }

    @Override
    public List<BallBet> findByGameId(Long id, int status) {
        /**
         * 0 查询未结算
         * 1 查询已结算
         */
        QueryWrapper query = new QueryWrapper();
        query.eq("game_id", id);
        //只查询未结算,已确认的
        if (status == 0) {
            query.eq("status", 1);
            query.eq("status_settlement", 0);
        }else if(status == 2){
            //查询结算的和确认的
            query.eq("status", 1);
            query.eq("status_settlement", 1);
        }
        return list(query);
    }

    @Override
    public Boolean edit(BallBet ballBet) {
        return updateById(ballBet);
    }

    @Override
    public BaseResponse undo(BallBet query, BallAdmin ballAdmin, boolean b) {
        BallBet bet = null;
        if(!b){
            bet = findById(query.getId());
        }else{
            bet = query;
        }
        BallPlayer player = basePlayerService.findById(bet.getPlayerId());
        BaseResponse unbet = playerBetService.unbet(query.getId(), bet, player,ballAdmin);
        return unbet;
    }

    @Override
    public BaseResponse info(BallBet query) {
        BallBet bet = findById(query.getId());
        BallPlayer player = basePlayerService.findById(bet.getPlayerId());
        BaseResponse response = playerBetService.betInfo(bet.getId(), player);
//        Map<String, Object> data = response.getData();
//        data.put("player", player);
        return response;
    }

    @Override
    public void betOpen(BallGame edit,boolean ishand) {
        //结算
        BallGame game = playerGameService.findById(edit.getId());
//        if (game.getStatus() == 3) {
//            return;
//        }
        if(game.getStatus()==2){
            apiLog.info("赛事已关闭，不结算{}",game);
        }
        //不是手动结算是判定是否是保本
        if(!ishand){
            //TODO 保本锁定不结算
            if (game.getEven() == 1) {
                apiLog.info("赛事保本，不自动结算{}",game);
                apiService.tgNotice(game.getId(),MessageFormat.format("赛事:{0} 爆单!,比分:{1}-{2}，请及时处理~",
                        game.getId().toString(), game.getHomeFull(),game.getGuestFull()));
                return;
            }
            //TODO 比赛比分赔率设置为保本也不结算
            List<BallGameLossPerCent> lossPerCents = lossPerCentService.findByGameId(game.getId());
            for(BallGameLossPerCent item:lossPerCents){
                //如果赔率保本锁定
                if(item.getEven()==1){
                    //如果是本期比赛结果赔率,则是不自动结算
                    if(isLock(game,item)){
                        apiLog.info("赛事赔率保本，不自动结算~{}",game);
                        apiService.tgNotice(game.getId(),MessageFormat.format("赛事:{0} 爆单!,比分:{1}-{2}，请及时处理~",
                                game.getId().toString(), game.getHomeFull(),game.getGuestFull()));
                        return;
                    }
                }
            }
        }
        //查询当前游戏所有下注
        StringBuilder sb = new StringBuilder();
        List<BallBet> betsList = findByGameId(game.getId(), 0);
        if (betsList != null && !betsList.isEmpty()) {
            Map<Long, BallGameLossPerCent> lossPerCentMap = new HashMap<>();
            for (BallBet bet : betsList) {
                sb.append(bet.getOrderNo());
                BallGameLossPerCent lossPerCent = lossPerCentMap.get(bet.getGameLossPerCentId());
                if (lossPerCent == null) {
                    lossPerCent = lossPerCentService.findById(bet.getGameLossPerCentId());
                    lossPerCentMap.put(bet.getGameLossPerCentId(), lossPerCent);
                }
                if (lossPerCent.getStatus() != 1) {
                    //如果赔率不可用，订单无法结算
                    sb.append(":赔率不可用,");
                    continue;
                }
                sb.append(",");
                ThreadPoolUtil.exec(new OpenBetThread(lossPerCent, bet, playerBetService,
                        game, basePlayerService, ballBalanceChangeService, messageQueueService, vipService, redisUtil,ishand
                ));
            }
        }
        apiLog.info("赛事结算订单:{}",sb);
        if(ishand){
            //手动
            edit.setSettlementType(1);
            apiLog.info("赛事已手动结算{}",game);
        }else{
            //自动结算了 0
            edit.setSettlementType(0);
            edit.setSettlementTime(System.currentTimeMillis());
            apiLog.info("赛事已自动结算{}",game);
        }
        //赛事为已结束
        edit.setGameStatus(3);
        gameService.edit(edit);
    }

    private boolean isLock(BallGame game, BallGameLossPerCent lossPerCent) {
        //反波
        boolean bingo = false;
        if (lossPerCent.getGameType()==1) {
            //上半场
            //下正波,带*,只需要比较一方分数
            if (lossPerCent.getScoreHome().equals("*")) {
                //主场带*, 客场分>=4,算中
                int awayOdd = Integer.parseInt(lossPerCent.getScoreAway());
                bingo = game.getGuestHalf() >= awayOdd;
            } else if (lossPerCent.getScoreAway().equals("*")) {
                //客场带*,主场分>=4,算中
                int homeOdd = Integer.parseInt(lossPerCent.getScoreHome());
                bingo = game.getHomeHalf() >= homeOdd;
            } else {
                //不带*
                bingo = (lossPerCent.getScoreAway().equals(game.getGuestHalf().toString())
                        && lossPerCent.getScoreHome().equals(game.getHomeHalf().toString()));
            }
        } else {
            //全场
            //下正波,带*,只需要比较一方分数
            if (lossPerCent.getScoreHome().equals("*")) {
                //主场带*, 客场分>=4,算中
                int awayOdd = Integer.parseInt(lossPerCent.getScoreAway());
                bingo = game.getGuestFull() >= awayOdd;
            } else if (lossPerCent.getScoreAway().equals("*")) {
                //客场带*,主场分>=4,算中
                int homeOdd = Integer.parseInt(lossPerCent.getScoreHome());
                bingo = game.getHomeFull() >= homeOdd;
            } else {
                //不带*
                bingo = (lossPerCent.getScoreAway().equals(game.getGuestFull().toString())
                        && lossPerCent.getScoreHome().equals(game.getHomeFull().toString()));
            }
        }
//        if (lossPerCent.getGameType() == 1) {
//            //上半
//            //下反波,只要和比分任意不一样就中
//            if (lossPerCent.getScoreHome().equals("*")) {
//                //主场带*,客场只要比分<4算中
//                int awayOdd = Integer.parseInt(lossPerCent.getScoreAway());
//                bingo = !(game.getGuestHalf() >= awayOdd);
//            } else if (lossPerCent.getScoreAway().equals("*")) {
//                //客场带*,只要主场比分未下中,就中
//                int homeOdd = Integer.parseInt(lossPerCent.getScoreHome());
//                bingo = !(game.getHomeHalf() >= homeOdd);
//            } else {
//                //不带*,任意未下中,就中
//                bingo = (!lossPerCent.getScoreAway().equals(game.getGuestHalf().toString())
//                        || !lossPerCent.getScoreHome().equals(game.getHomeHalf().toString()));
//            }
//        } else {
//            //全场
//            //下反波,只要和比分任意不一样就中
//            if (lossPerCent.getScoreHome().equals("*")) {
//                //主场带*,客场只要比分<4算中
//                int awayOdd = Integer.parseInt(lossPerCent.getScoreAway());
//                bingo = !(game.getGuestFull() >= awayOdd);
//            } else if (lossPerCent.getScoreAway().equals("*")) {
//                //客场带*,只要主场比分未下中,就中
//                int homeOdd = Integer.parseInt(lossPerCent.getScoreHome());
//                bingo = !(game.getHomeFull() >= homeOdd);
//            } else {
//                //不带*,任意未下中,就中
//                bingo = (!lossPerCent.getScoreAway().equals(game.getGuestFull().toString())
//                        || !lossPerCent.getScoreHome().equals(game.getHomeFull().toString()));
//            }
//        }
        return bingo;
    }

    @Override
    public BaseResponse betRecount(Long id, BallAdmin currentUser) {
        //重算
        BallGame game = playerGameService.findById(id);
        if (game.getSettlementType() == 3) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e19"));
        }
        //查询当前游戏所有下注
        List<BallBet> betsList = findByGameId(game.getId(), 2);
        if (betsList != null && !betsList.isEmpty()) {
            Map<Long, BallGameLossPerCent> lossPerCentMap = new HashMap<>();
            for (BallBet bet : betsList) {
                BallGameLossPerCent lossPerCent = lossPerCentMap.get(bet.getGameLossPerCentId());
                if (lossPerCent == null) {
                    lossPerCent = lossPerCentService.findById(bet.getGameLossPerCentId());
                    lossPerCentMap.put(bet.getGameLossPerCentId(), lossPerCent);
                }
                if (lossPerCent == null || lossPerCent.getStatus() != 1) {
                    //如果赔率不可用，订单无法结算
                    continue;
                }
                ThreadPoolUtil.exec(new BetRecountThread(lossPerCent, bet, playerBetService,
                        game, basePlayerService, ballBalanceChangeService, currentUser));
            }
        }
        gameService.edit(BallGame.builder()
                .settlementType(3)
                .settlementTime(System.currentTimeMillis())
                .id(id)
                .build());
        return BaseResponse.successWithMsg("ok");
    }

    @Override
    public synchronized BaseResponse betRollback(Long id, BallAdmin currentUser) {
        //回滚是指本期投注全部撤消
        //余额找回,累加数据减出,增加账变数据
        BallGame game = playerGameService.findById(id);
        if (game.getSettlementType() == 2) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e26"));
        }
        //查询当前游戏所有下注
        List<BallBet> betsList = findByGameId(game.getId(), 2);
        if (betsList != null && !betsList.isEmpty()) {
            for (BallBet bet : betsList) {
                ThreadPoolUtil.exec(new BetRollbackThread(bet, playerBetService,
                        game, basePlayerService, ballBalanceChangeService,currentUser));
            }
        }
        gameService.edit(BallGame.builder()
                .settlementType(2)
                .settlementTime(System.currentTimeMillis())
                .id(id)
                .build());
        return BaseResponse.successWithMsg("ok");
    }

    @Override
    public synchronized BaseResponse betRollbackByBetId(Long id, BallAdmin admin) {
        BallBet bet = findById(id);
        if (bet.getStatus() == 4) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e20"));
        }
        BallGame game = playerGameService.findById(bet.getGameId());
        ThreadPoolUtil.exec(new BetRollbackThread(bet, playerBetService,
                game, basePlayerService, ballBalanceChangeService,admin));
        return BaseResponse.successWithMsg("ok");
    }

    @Override
    public BallBet statisNotOpen() {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.select("sum(bet_money) as bet_money,count(id) as id");
        queryWrapper.eq("account_type", 2);
        queryWrapper.eq("status", 1);
        queryWrapper.eq("status_settlement", 0);
        List<BallBet> list = list(queryWrapper);
        return list.get(0);
    }

    @Override
    public BallBet statisTotal(ReportDataRequest reportDataRequest) {
//        投注人数2
//                投注金额2
//        中奖金额0
//                返倗金额
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.select("sum(bet_money) as bet_money,count(distinct player_id) as player_id,sum(winning_amount) as winning_amount");
        //只统计正式号
        queryWrapper.eq("account_type", 2);
        //只统计已结算
        queryWrapper.eq("status_settlement", 1);

        BallPlayerServiceImpl.queryByTime(queryWrapper, reportDataRequest.getTime(), reportDataRequest.getBegin(), reportDataRequest.getEnd());
        BallPlayer player = null;
        if (reportDataRequest.getUserId() != null) {
            player = basePlayerService.findByUserId(reportDataRequest.getUserId());
        } else if (!StringUtils.isBlank(reportDataRequest.getUsername())) {
            player = basePlayerService.findByUsername(reportDataRequest.getUsername());
        }
        if (player != null) {
            queryWrapper.likeRight("super_tree", player.getSuperTree() + player.getId() + "\\_");
        }
        List<BallBet> list = list(queryWrapper);

        BallLoggerBack backBet = loggerBackService.statis(reportDataRequest, 1);
        BallLoggerBack backWin = loggerBackService.statis(reportDataRequest, 2);

        if (list != null && !list.isEmpty() && list.get(0) != null) {
            BallBet ballLoggerRecharge = list.get(0);
            //返佣
            ballLoggerRecharge.setOrderNo(backBet.getMoney() + backWin.getMoney());
            return ballLoggerRecharge;
        }
        return BallBet.builder()
                .playerId(0L)
                .betMoney(0L)
                .winningAmount(0L)
                .orderNo(backBet.getMoney() + backWin.getMoney())
                .build();
    }

    @Override
    public List<BallBet> statisByType(ReportDataRequest reportDataRequest) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.select("count(id) as id,count(distinct player_id) as player_id,sum(bet_money) as bet_money,sum(hand_money) as hand_money,sum(winning_amount) as winning_amount");
        queryWrapper.eq("account_type", 2);
        queryWrapper.eq("status_settlement", 1);

        BallPlayerServiceImpl.queryByTime(queryWrapper, reportDataRequest.getTime(), reportDataRequest.getBegin(), reportDataRequest.getEnd());
        BallPlayer player = null;
        if (reportDataRequest.getUserId() != null) {
            player = basePlayerService.findByUserId(reportDataRequest.getUserId());
        } else if (!StringUtils.isBlank(reportDataRequest.getUsername())) {
            player = basePlayerService.findByUsername(reportDataRequest.getUsername());
        }
        if (player != null) {
            queryWrapper.likeRight("super_tree", player.getSuperTree() + player.getId() + "\\_");
        }
        List<BallBet> list = list(queryWrapper);
        return list;
    }

    @Override
    public SearchResponse<BallBet> statisReport(List<Long> gameIds, int pageNo) {
        //按赛事统计下注
        SearchResponse<BallBet> response = new SearchResponse<>();
        QueryWrapper<BallBet> query = new QueryWrapper<>();
        query.eq("account_type", 2);
        query.in("game_id", gameIds);
        Page<BallBet> page = new Page<>(pageNo, 1000);
        IPage<BallBet> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public BallBet statisBetCount(Long id, Long begin, Long end) {
        QueryWrapper<BallBet> query = new QueryWrapper<>();
        query.select("count(distinct player_id) as player_id ,count(id) as id");
        query.eq("account_type", 2);
        query.between("created_at", begin, end);
        query.likeRight("super_tree", "\\_" + id + "\\_");
        return getOne(query);
    }

    @Override
    public List<BallBet> statisNotOpenForPlayer() {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.select("sum(bet_money) as bet_money,player_id");
        queryWrapper.eq("account_type", 2);
        queryWrapper.eq("status", 1);
        queryWrapper.eq("status_settlement", 0);
        queryWrapper.groupBy("player_id");
        List<BallBet> list = list(queryWrapper);
        return list;
    }

    @Override
    public int editMult(UpdateWrapper update, BallBet edit) {
        int edited = baseMapper.update(edit, update);
        return edited;
    }

    @Override
    public void search(BallProxyLogger queryParam, List<Long> ids, ProxyStatisDto list1, Map<Long, ProxyStatisDto> list2Map,BallPlayer proxyUser) {
        QueryWrapper<BallBet> query = new QueryWrapper<>();
        //正常结算订单
        query.eq("status",1);
        query.eq("status_settlement",1);
        if(!StringUtils.isEmpty(queryParam.getBegin())){
            try {
                query.gt("created_at",TimeUtil.stringToTimeStamp(queryParam.getBegin(),TimeUtil.TIME_YYYY_MM_DD));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if(!StringUtils.isEmpty(queryParam.getEnd())){
            try {
                query.lt("created_at",TimeUtil.stringToTimeStamp(queryParam.getEnd(),TimeUtil.TIME_YYYY_MM_DD)+TimeUtil.TIME_ONE_DAY);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if(queryParam.getSelf()==1){
            query.and(QueryWrapper -> QueryWrapper.eq("player_id",proxyUser.getId())
                    .or()
                    .likeRight("super_tree",proxyUser.getSuperTree()+proxyUser.getId()+"\\_"));
        }else{
            query.likeRight("super_tree",proxyUser.getSuperTree()+proxyUser.getId()+"\\_");
        }
        int pageNo = 1;
        while (true){
            Page<BallBet> page = new Page<>(pageNo++, 500);
            IPage<BallBet> pageResult = page(page, query);
            List<BallBet> records = pageResult.getRecords();
            if(records==null||records.isEmpty()||records.get(0)==null){
                break;
            }
            for(BallBet item:records){
                if(queryParam.getProxyLine()==0){
                    ProxyStatisDto proxyStatisDto = list2Map.get(item.getPlayerId());
                    if(proxyStatisDto!=null){
                        list1.setBetCount(list1.getBetCount()+1);
                        Set<Long> betPlayer = list1.getBetPlayerSet();
                        betPlayer.add(item.getPlayerId());

                        proxyStatisDto.setBetCount(proxyStatisDto.getBetCount()+1);
                        Set<Long> betPlayer1 = proxyStatisDto.getBetPlayerSet();
                        betPlayer1.add(item.getPlayerId());
                    }
                }else{
                    String superTree = item.getSuperTree();
                    String[] split = superTree.split("_");
                    List<String> strings = new ArrayList<>(Arrays.asList(split));
                    strings.add(item.getPlayerId().toString());
                    boolean added = false;
                    for(String sitem:strings){
                        if(StringUtils.isBlank(sitem)){
                            continue;
                        }
                        ProxyStatisDto proxyStatisDto = list2Map.get(Long.parseLong(sitem));
                        if(proxyStatisDto==null){
                            continue;
                        }
                        if(!added){
                            list1.setBetCount(list1.getBetCount()+1);
                            Set<Long> betPlayer = list1.getBetPlayerSet();
                            betPlayer.add(item.getPlayerId());
                            added=true;
                        }
                        proxyStatisDto.setBetCount(proxyStatisDto.getBetCount()+1);
                        Set<Long> betPlayer1 = proxyStatisDto.getBetPlayerSet();
                        betPlayer1.add(item.getPlayerId());
                    }
                }
                if(item.getPlayerId().equals(proxyUser.getId())){
                    list1.setBetCount(list1.getBetCount()+1);
                    Set<Long> betPlayer = list1.getBetPlayerSet();
                    betPlayer.add(item.getPlayerId());
                }
            }
        }
    }

    @Override
    public void search(QueryActivePlayerRequest queryParam, BallPlayer player, Map<Long, BallPlayer> dataMap) {
        QueryWrapper<BallBet> query = new QueryWrapper<>();
        //正常结算订单
        query.eq("status",1);
        query.eq("status_settlement",1);
        if(!StringUtils.isEmpty(queryParam.getBbegin())){
            try {
                query.gt("created_at",TimeUtil.stringToTimeStamp(queryParam.getBbegin(),TimeUtil.TIME_YYYY_MM_DD));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }else if(!StringUtils.isEmpty(queryParam.getRbegin())){
            try {
                query.gt("created_at",TimeUtil.stringToTimeStamp(queryParam.getRbegin(),TimeUtil.TIME_YYYY_MM_DD));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if(!StringUtils.isEmpty(queryParam.getBend())){
            try {
                query.lt("created_at",TimeUtil.stringToTimeStamp(queryParam.getBend(),TimeUtil.TIME_YYYY_MM_DD)+TimeUtil.TIME_ONE_DAY);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }else if(!StringUtils.isEmpty(queryParam.getRend())){
            try {
                query.lt("created_at",TimeUtil.stringToTimeStamp(queryParam.getRend(),TimeUtil.TIME_YYYY_MM_DD)+TimeUtil.TIME_ONE_DAY);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        query.likeRight("super_tree",player.getSuperTree()+player.getId()+"\\_");
        int pageNo = 1;
        while (true){
            Page<BallBet> page = new Page<>(pageNo++, 500);
            IPage<BallBet> pageResult = page(page, query);
            List<BallBet> records = pageResult.getRecords();
            if(records==null||records.isEmpty()||records.get(0)==null){
                break;
            }
            for(BallBet item:records){
                BallPlayer bplayer = dataMap.get(item.getPlayerId());
                if(bplayer!=null){
                    bplayer.setBcount(bplayer.getBcount()+1);
                    bplayer.setAccumulativeBet(bplayer.getAccumulativeBet()+item.getBetMoney());
                    Set<String> betPlayer = bplayer.getBetDays();
                    betPlayer.add(TimeUtil.dateFormat(new Date(item.getCreatedAt()),TimeUtil.TIME_YYYY_MM_DD));
                }
            }
        }
    }

    @Override
    public BaseResponse betOpen(Long id) {
        BallBet bet = findById(id);
        BallGame game = playerGameService.findById(bet.getGameId());
        BallGameLossPerCent lossPerCent = lossPerCentService.findById(bet.getGameLossPerCentId());
        ThreadPoolUtil.exec(new OpenBetThread(lossPerCent, bet, playerBetService,
                game, basePlayerService, ballBalanceChangeService, messageQueueService, vipService, redisUtil,true
        ));
        return BaseResponse.successWithData(MapUtil.newMap("sucmsg","e59"));
    }

    @Override
    public void searchProxy2(BallProxyLogger queryParam, Map<String, ProxyStatis2Dto> dataMap, BallPlayer playerProxy, ProxyStatis2Dto total) {
        QueryWrapper<BallBet> query = new QueryWrapper<>();
        //正常结算订单
        query.eq("status",1);
        query.eq("status_settlement",1);
        if(!StringUtils.isEmpty(queryParam.getBegin())){
            try {
                query.gt("created_at",TimeUtil.stringToTimeStamp(queryParam.getBegin(),TimeUtil.TIME_YYYY_MM_DD));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if(!StringUtils.isEmpty(queryParam.getEnd())){
            try {
                query.lt("created_at",TimeUtil.stringToTimeStamp(queryParam.getEnd(),TimeUtil.TIME_YYYY_MM_DD)+TimeUtil.TIME_ONE_DAY);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        query.likeRight("super_tree",playerProxy.getSuperTree()+playerProxy.getId()+"\\_");
        int pageNo = 1;
        while (true){
            Page<BallBet> page = new Page<>(pageNo++, 500);
            IPage<BallBet> pageResult = page(page, query);
            List<BallBet> records = pageResult.getRecords();
            if(records==null||records.isEmpty()||records.get(0)==null){
                break;
            }
            for(BallBet item:records){
                String ymd = TimeUtil.longToStringYmd(item.getCreatedAt());
                ProxyStatis2Dto proxyStatisDto = dataMap.get(ymd);
                if(proxyStatisDto!=null){
                    proxyStatisDto.setBetCount(proxyStatisDto.getBetCount()+1);
                    Set<Long> betPlayer = proxyStatisDto.getBetPlayerSet();
                    betPlayer.add(item.getPlayerId());

                    total.setBetCount(total.getBetCount()+1);
                    Set<Long> betPlayert = total.getBetPlayerSet();
                    betPlayert.add(item.getPlayerId());

                }
            }
        }
    }

    @Override
    public void statis(BallProxyLogger queryParam, ProxyStatis3Dto total, ProxyStatis3Dto data) {
        QueryWrapper<BallBet> query = new QueryWrapper<>();
        //正常结算订单
        query.eq("status",1);
        query.eq("status_settlement",1);
        if(!StringUtils.isEmpty(queryParam.getBegin())){
            try {
                query.gt("created_at",TimeUtil.stringToTimeStamp(queryParam.getBegin(),TimeUtil.TIME_YYYY_MM_DD));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if(!StringUtils.isEmpty(queryParam.getEnd())){
            try {
                query.lt("created_at",TimeUtil.stringToTimeStamp(queryParam.getEnd(),TimeUtil.TIME_YYYY_MM_DD)+TimeUtil.TIME_ONE_DAY);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        query.likeRight("super_tree",queryParam.getPlayerName()+queryParam.getPlayerId()+"\\_");

        query.select("count(distinct player_id) player_id,count(id) id");

        BallBet one = getOne(query);
        data.setBetCount(one.getId()==null?0:one.getId().intValue());
        data.setBetPlayer(one.getPlayerId()==null?0:one.getPlayerId().intValue());

        total.setBetCount(total.getBetCount()+data.getBetCount());
        total.setBetPlayer(total.getBetPlayer()+data.getBetPlayer());
    }

    @Override
    public void searchProxy3(BallProxyLogger queryParam, Map<String, ProxyStatis2Dto> dataMap, BallPlayer playerProxy, ProxyStatis2Dto total) {
        QueryWrapper<BallBet> query = new QueryWrapper<>();
        //正常结算订单
        query.eq("status",1);
        query.eq("status_settlement",1);
        query.gt("created_at",TimeUtil.getBeginDayOfYesterday().getTime());
        query.lt("created_at",TimeUtil.getEndDayOfYesterday().getTime());
        query.likeRight("super_tree",playerProxy.getSuperTree()+playerProxy.getId()+"\\_");
        int pageNo = 1;
        while (true){
            Page<BallBet> page = new Page<>(pageNo++, 500);
            IPage<BallBet> pageResult = page(page, query);
            List<BallBet> records = pageResult.getRecords();
            if(records==null||records.isEmpty()||records.get(0)==null){
                break;
            }
            for(BallBet item:records){
                for(ProxyStatis2Dto item2:dataMap.values()){
                    Set<Long> betPlayer = item2.getBetPlayerYestodaySet();
                    betPlayer.add(item.getPlayerId());

                    Set<Long> betPlayert = total.getBetPlayerYestodaySet();
                    betPlayert.add(item.getPlayerId());
                }
            }
        }
    }

    @Override
    public void statis2(BallProxyLogger queryParam, ProxyStatis3Dto total, ProxyStatis3Dto data) {
        QueryWrapper<BallBet> query = new QueryWrapper<>();
        //正常结算订单
        query.eq("status",1);
        query.eq("status_settlement",1);
        query.gt("created_at",TimeUtil.getBeginDayOfYesterday().getTime());
        query.lt("created_at",TimeUtil.getEndDayOfYesterday().getTime());
        query.likeRight("super_tree",queryParam.getPlayerName()+queryParam.getPlayerId()+"\\_");

        query.select("count(distinct player_id) player_id");

        BallBet one = getOne(query);
        data.setBetPlayerYestoday(one.getPlayerId()==null?0:one.getPlayerId().intValue());
        total.setBetPlayerYestoday(total.getBetPlayerYestoday()+data.getBetPlayerYestoday());
    }

    @Override
    public void searchStandard(ReportStandardRequest reportStandardRequest, BallPlayer playerProxy, Map<Long, ReportStandardPlayerDTO> playerMap) {
        QueryWrapper<BallBet> query = new QueryWrapper<>();
        //充值成功的
        query.eq("status",1);
        query.eq("status_settlement",1);
        query.gt("created_at",reportStandardRequest.getBegins());
        query.lt("created_at",reportStandardRequest.getEnds());
        query.likeRight("super_tree",playerProxy.getSuperTree()+playerProxy.getId()+"\\_");
        int pageNo = 1;
        while (true){
            IPage<BallBet> page = page(new Page<>(pageNo++, 500), query);
            List<BallBet> records = page.getRecords();
            if(records==null||records.isEmpty()||records.get(0)==null){
                break;
            }
            for (BallBet item:records){
                ReportStandardPlayerDTO reportStandardPlayerDTO = playerMap.get(item.getPlayerId());
                String ymd = TimeUtil.longToStringYmd(item.getCreatedAt());
                reportStandardPlayerDTO.getDaySet().add(ymd);
                reportStandardPlayerDTO.setBets(item.getBetMoney()+reportStandardPlayerDTO.getBets());
            }
        }
    }

    @Override
    public SearchResponse<BallBet> searchStandard(ReportStandardRequest reportStandardRequest, int pageNo) {
        SearchResponse<BallBet> response = new SearchResponse<>();
        Page<BallBet> page = new Page<>(pageNo, 500);
        IPage<BallBet> pages = mapper.page_s(page, reportStandardRequest);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public BaseResponse checkRebate(String playerName) {
        if(StringUtils.isBlank(playerName)){
            return BaseResponse.failedWithMsg("user not found");
        }
        BallPlayer byUsername = basePlayerService.findByUsername(playerName);
        if(byUsername==null){
            return BaseResponse.failedWithMsg("user not found");
        }
        ThreadPoolUtil.execSaki(new Runnable() {
            @Override
            public void run() {
                //补返利步骤
                //1.查询该账号下级的所有订单
                //2.订单是否中奖是否可以返利
                int pageNo = 1;
                while (true){
                    //只查已结算已中奖状态的订单
                    SearchResponse<BallBet> search = search(BallBet.builder()
                            .username(playerName)
                            .treeType(2)
                            .statusSettlement(1)
                            .statusOpen(1)
                            .settlementTime(System.currentTimeMillis()-TimeUtil.TIME_ONE_DAY*3)
                            .build(), pageNo++, 100);
                    List<BallBet> results = search.getResults();
                    if(results==null||results.isEmpty()||results.get(0)==null){
                        break;
                    }
                    for(BallBet item:results){
                        //查询本订单是否有返利数据
                        BallLoggerBack one = loggerBackService.search(BallLoggerBack.builder()
                                .playerId(byUsername.getId())
                                .orderNo(String.valueOf(item.getOrderNo()))
                                .build());
                        if(one==null){
                            //如果没有返利,则安排返利
                            //下注返佣
                            BallBet data = BallBet.builder()
                                    .playerId(byUsername.getId())
                                    .build();
                            data.setId(item.getId());
                            try {
                                messageQueueService.putMessage(MessageQueueDTO.builder()
                                        .type(MessageQueueDTO.TYPE_LOG_BET_BACK2)
                                        .data(JsonUtil.toJson(data))
                                        .build());
                            } catch (JsonProcessingException e) {
                            }
                        }
                    }
                }
            }
        });
        return BaseResponse.successWithData(MapUtil.newMap("sucmsg","e71"));
    }

    @Override
    public BaseResponse checkRebateQueue() {
        long l = redisUtil.lGetListSize(RedisKeyContant.LOGGER_QUEUE);
        return BaseResponse.successWithMsg(String.valueOf(l));
    }

    public static Long arithmeticBingo(Long betMoney, String oddsStr) {
        Double odds = Double.valueOf(oddsStr);
        double div = BigDecimalUtil.div(odds, 100);
        long bingo = Double.valueOf(BigDecimalUtil.mul(div, betMoney)).longValue();
//        下注+下注*赔率%
        return bingo + betMoney;
    }

    public static class OpenBetThread implements Runnable {
        private Logger apiLog = LoggerFactory.getLogger(LogsContant.API_LOG);

        private BallGameLossPerCent lossPerCent;
        private BallBet bet;
        private IPlayerBetService betService;
        private BallGame game;
        private IBasePlayerService playerService;
        private IBallBalanceChangeService ballBalanceChangeService;
        private IMessageQueueService messageQueueService;
        private IBallVipService vipService;
        private RedisUtil redisUtil;
        private Boolean ishand;
        public OpenBetThread(BallGameLossPerCent lossPerCent, BallBet bet,
                             IPlayerBetService betService, BallGame game,
                             IBasePlayerService playerService,
                             IBallBalanceChangeService ballBalanceChangeService,
                             IMessageQueueService messageQueueService,
                             IBallVipService vipService,
                             RedisUtil redisUtil, boolean ishand) {
            this.lossPerCent = lossPerCent;
            this.bet = bet;
            this.betService = betService;
            this.game = game;
            this.playerService = playerService;
            this.ballBalanceChangeService = ballBalanceChangeService;
            this.messageQueueService = messageQueueService;
            this.vipService = vipService;
            this.redisUtil = redisUtil;
            this.ishand = ishand;
        }


        @Override
        public void run() {
            //是否中奖,正波需要匹配，反波是只要不是比分就中
            boolean bingo = false;
            String odds = null;
            //正波
            if (bet.getBetType() == 1) {
                if (bet.getGameType().equals(1)) {
                    //上半场
                    //下正波,带*,只需要比较一方分数
                    if (lossPerCent.getScoreHome().equals("*")) {
                        //主场带*, 客场分>=4,算中
                        int awayOdd = Integer.parseInt(lossPerCent.getScoreAway());
                        bingo = game.getGuestHalf() >= awayOdd;
                    } else if (lossPerCent.getScoreAway().equals("*")) {
                        //客场带*,主场分>=4,算中
                        int homeOdd = Integer.parseInt(lossPerCent.getScoreHome());
                        bingo = game.getHomeHalf() >= homeOdd;
                    } else {
                        //不带*
                        bingo = (lossPerCent.getScoreAway().equals(game.getGuestHalf().toString())
                                && lossPerCent.getScoreHome().equals(game.getHomeHalf().toString()));
                    }
                } else {
                    //全场
                    //下正波,带*,只需要比较一方分数
                    if (lossPerCent.getScoreHome().equals("*")) {
                        //主场带*, 客场分>=4,算中
                        int awayOdd = Integer.parseInt(lossPerCent.getScoreAway());
                        bingo = game.getGuestFull() >= awayOdd;
                    } else if (lossPerCent.getScoreAway().equals("*")) {
                        //客场带*,主场分>=4,算中
                        int homeOdd = Integer.parseInt(lossPerCent.getScoreHome());
                        bingo = game.getHomeFull() >= homeOdd;
                    } else {
                        //不带*
                        bingo = (lossPerCent.getScoreAway().equals(game.getGuestFull().toString())
                                && lossPerCent.getScoreHome().equals(game.getHomeFull().toString()));
                    }
                }
                odds = lossPerCent.getLossPerCent();
            } else {
                if (bet.getGameType().equals(1)) {
                    //上半
                    //下反波,只要和比分任意不一样就中
                    if (lossPerCent.getScoreHome().equals("*")) {
                        //主场带*,客场只要比分<4算中
                        int awayOdd = Integer.parseInt(lossPerCent.getScoreAway());
                        bingo = !(game.getGuestHalf() >= awayOdd);
                    } else if (lossPerCent.getScoreAway().equals("*")) {
                        //客场带*,只要主场比分未下中,就中
                        int homeOdd = Integer.parseInt(lossPerCent.getScoreHome());
                        bingo = !(game.getHomeHalf() >= homeOdd);
                    } else {
                        //不带*,任意未下中,就中
                        bingo = (!lossPerCent.getScoreAway().equals(game.getGuestHalf().toString())
                                || !lossPerCent.getScoreHome().equals(game.getHomeHalf().toString()));
                    }
                } else {
                    //全场
                    //下反波,只要和比分任意不一样就中
                    if (lossPerCent.getScoreHome().equals("*")) {
                        //主场带*,客场只要比分<4算中
                        int awayOdd = Integer.parseInt(lossPerCent.getScoreAway());
                        bingo = !(game.getGuestFull() >= awayOdd);
                    } else if (lossPerCent.getScoreAway().equals("*")) {
                        //客场带*,只要主场比分未下中,就中
                        int homeOdd = Integer.parseInt(lossPerCent.getScoreHome());
                        bingo = !(game.getHomeFull() >= homeOdd);
                    } else {
                        //不带*,任意未下中,就中
                        bingo = (!lossPerCent.getScoreAway().equals(game.getGuestFull().toString())
                                || !lossPerCent.getScoreHome().equals(game.getHomeFull().toString()));
                    }
                }
                odds = lossPerCent.getAntiPerCent();
            }
            BallBet edit = BallBet.builder()
                    .statusSettlement(1)
                    .settlementTime(System.currentTimeMillis())
                    .settlememntPerson("sys")
                    .build();
            edit.setId(bet.getId());
            int balanceType = 4;
            BallPlayer player = playerService.findById(bet.getPlayerId());
            if (bingo) {
                //计算中奖金额 ,下注+下注*赔率%
                Long oddsLong = arithmeticBingo(bet.getBetMoney(), odds);
                //根据VIP等级计算额外收益
                if (player.getVipLevel() != null && player.getVipLevel() > 0) {
                    BallVip byLevel = vipService.findByLevel(player.getVipLevel());
                    if (!byLevel.getLevelProfit().equals("0")) {
                        Double profit = BigDecimalUtil.div(BigDecimalUtil.mul(oddsLong, Double.valueOf(byLevel.getLevelProfit())), BigDecimalUtil.PLAYER_MONEY_UNIT);
                        oddsLong += profit.longValue();
                    }
                }
                edit.setWinningAmount(oddsLong);
                edit.setStatusOpen(1);
            } else {
                if (bet.getEven() == 1) {
                    //保本
                    edit.setWinningAmount(bet.getBetMoney());
                    bingo = true;
                    balanceType = 13;
                } else {
                    edit.setWinningAmount(0L);
                }
                edit.setStatusOpen(2);
            }
            boolean isSucc = betService.edit(edit);
            if (isSucc) {
                apiLog.info("赛事订单[{}]开始结算",bet.getOrderNo());
                //更新账号余额+累计中奖金额
                while (true) {
                    BallPlayer editPlayer = BallPlayer.builder()
                            .balance(player.getBalance() + edit.getWinningAmount())
                            .version(player.getVersion())
                            //累计中奖
                            .cumulativeWinning(player.getCumulativeWinning() + edit.getWinningAmount())
                            //累计投注
                            .accumulativeBet((player.getAccumulativeBet() == null ? 0 : player.getAccumulativeBet()) + bet.getBetMoney())
                            //TODO 扣除冻结下注
                            .frozenBet(player.getFrozenBet()-bet.getBetMoney())
                            .build();
                    editPlayer.setId(player.getId());
                    boolean isSuccess = playerService.editAndClearCache(editPlayer, player);
                    if (isSuccess) {
                        apiLog.info("赛事订单[{}]金额结算完成",bet.getOrderNo());
                        if (bingo) {
                            String remark = (ishand?"手动":"自动")+"订单结算-用户:" + player.getUsername() + "-订单号:" + bet.getOrderNo() + "-";
                            if (balanceType == 13) {
                                remark += "未中奖保本退回金额:" + (bet.getBetMoney() / BigDecimalUtil.PLAYER_MONEY_UNIT);
                            } else {
                                remark += "中奖盈利:" + BigDecimalUtil.div(edit.getWinningAmount(), BigDecimalUtil.PLAYER_MONEY_UNIT, 2);
                            }
                            //账变记录
                            ballBalanceChangeService.insert(BallBalanceChange.builder()
                                    .playerId(player.getId())
                                    .accountType(player.getAccountType())
                                    .userId(player.getUserId())
                                    .parentId(player.getSuperiorId())
                                    .username(player.getUsername())
                                    .superTree(player.getSuperTree())
                                    .initMoney(player.getBalance())
                                    .changeMoney(edit.getWinningAmount())
                                    .dnedMoney(editPlayer.getBalance())
                                    //key recharge_self
                                    .createdAt(System.currentTimeMillis())
                                    .balanceChangeType(balanceType)
                                    .orderNo(bet.getOrderNo())
                                    .remark(remark)
                                    .build());
                        }
                        try {
                            messageQueueService.putMessage(MessageQueueDTO.builder()
                                    .type(MessageQueueDTO.TYPE_BET_BINGO_HANDSUP)
                                    .data(JsonUtil.toJson(bet))
                                    .build());
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                        //下注返佣
                        messageQueueService.putMessage(MessageQueueDTO.builder()
                                .type(MessageQueueDTO.TYPE_LOG_BET_BACK)
                                .data(String.valueOf(bet.getId()))
                                .build());
                        //TODO 中奖返佣,未中保本不返
                        if (balanceType != 13 && bingo) {
                            messageQueueService.putMessage(MessageQueueDTO.builder()
                                    .type(MessageQueueDTO.TYPE_LOG_BET_BINGO)
                                    .data(String.valueOf(bet.getId()))
                                    .build());
                        }
                        //账变为真实账变
                        try {
                            messageQueueService.putMessage(MessageQueueDTO.builder()
                                    .type(MessageQueueDTO.TYPE_BET_OPEN)
                                    .data(JsonUtil.toJson(bet))
                                    .build());
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                    player = playerService.findById(player.getId());
                }
            }
            if (isSucc) {
                //TODO 清除投注总额缓存
                redisUtil.del(RedisKeyContant.GAME_BET_TOTAL + bet.getGameId() + bet.getPlayerId());
                redisUtil.del(RedisKeyContant.GAME_BET_TOTAL + bet.getGameId() + bet.getGameLossPerCentId() + bet.getPlayerId());
                //全盘投注缓存减本期投注额度
                String globalKey = RedisKeyContant.GAME_BET_TOTAL + bet.getPlayerId();
                Object globalTotal = redisUtil.get(globalKey);
                if (globalTotal != null) {
                    Double globalLong = Double.parseDouble(globalTotal.toString());
                    Double hasLong = globalLong - bet.getBetMoney();
                    //全盘限额>0,保留,否则删除缓存key
                    if (hasLong > 0) {
                        redisUtil.set(globalKey, hasLong);
                    } else {
                        redisUtil.del(globalKey);
                    }
                }
            }
        }
    }

    public static class BetRecountThread implements Runnable {


        private BallGameLossPerCent lossPerCent;
        private BallBet bet;
        private IPlayerBetService betService;
        private BallGame game;
        private IBasePlayerService playerService;
        private IBallBalanceChangeService ballBalanceChangeService;
        private BallAdmin currentUser;

        public BetRecountThread(BallGameLossPerCent lossPerCent, BallBet bet,
                                IPlayerBetService betService, BallGame game,
                                IBasePlayerService playerService,
                                IBallBalanceChangeService ballBalanceChangeService, BallAdmin currentUser) {
            this.lossPerCent = lossPerCent;
            this.bet = bet;
            this.betService = betService;
            this.game = game;
            this.playerService = playerService;
            this.ballBalanceChangeService = ballBalanceChangeService;
        }


        @Override
        public void run() {
            //是否中奖,正波需要匹配，反波是只要不是比分就中
            boolean bingo = false;
            String odds = null;
            if (bet.getBetType() == 1) {
                //下正波,带*,只需要比较一方分数
                if (lossPerCent.getScoreHome().equals("*")) {
                    //主场带*
                    bingo = lossPerCent.getScoreAway().equals(game.getGuestFull().toString());
                } else if (lossPerCent.getScoreAway().equals("*")) {
                    //客场带*
                    bingo = lossPerCent.getScoreHome().equals(game.getHomeFull().toString());
                } else {
                    //不带*
                    bingo = (lossPerCent.getScoreAway().equals(game.getGuestFull().toString())
                            && lossPerCent.getScoreHome().equals(game.getHomeFull().toString()));
                }
                odds = lossPerCent.getLossPerCent();
            } else {
                //下反波,只要和比分任意不一样就中
                if (lossPerCent.getScoreHome().equals("*")) {
                    //主场带*,只要主比分未下中,就中
                    bingo = !lossPerCent.getScoreAway().equals(game.getGuestFull().toString());
                } else if (lossPerCent.getScoreAway().equals("*")) {
                    //客场带*,只要主场比分未下中,就中
                    bingo = !lossPerCent.getScoreHome().equals(game.getHomeFull().toString());
                } else {
                    //不带*,任意未下中,就中
                    bingo = (!lossPerCent.getScoreAway().equals(game.getGuestFull().toString())
                            || !lossPerCent.getScoreHome().equals(game.getHomeFull().toString()));
                }
                odds = lossPerCent.getAntiPerCent();
            }
            BallBet edit = BallBet.builder()
                    .settlementTime(System.currentTimeMillis())
                    .settlememntPerson(currentUser.getUsername())
                    .statusSettlement(1)
                    .build();
            edit.setId(bet.getId());
            int balanceType = 4;
            if (bingo) {
                //计算中奖金额 ,下注+下注*赔率%
                Long oddsLong = arithmeticBingo(bet.getBetMoney(), odds);
                edit.setWinningAmount(oddsLong);
                edit.setStatusOpen(1);
            } else {
                if (bet.getEven() == 1) {
                    //保本
                    edit.setWinningAmount(bet.getBetMoney());
                    bingo = true;
                    balanceType = 13;
                } else {
                    edit.setWinningAmount(0L);
                }
                edit.setStatusOpen(2);
            }
            //重新算的差额,新算出金额-原金额
            long recountMoney = edit.getWinningAmount() - bet.getWinningAmount();
            //重算无变化,不需要有操作
            if (recountMoney == 0) {
                return;
            }
            boolean isSucc = betService.edit(edit);
            if (isSucc) {
                //更新账号余额+中奖金额
                // 1.中奖金额与原中奖金额的对比变化
                //    - 小于,扣除余额,增加账变
                //    - 大于,增加余额,增加账变
                BallPlayer player = playerService.findById(bet.getPlayerId());
                while (true) {
                    BallPlayer editPlayer = BallPlayer.builder()
                            .balance(player.getBalance() + recountMoney)
                            .cumulativeWinning(player.getCumulativeWinning() + recountMoney)
                            .version(player.getVersion())
                            .build();
                    editPlayer.setId(player.getId());
                    boolean isSuccess = playerService.editAndClearCache(editPlayer, player);
                    if (isSuccess) {
                        String remark = "手动订单结算-用户:" + player.getUsername() + "-订单号:" + bet.getOrderNo() + "-";
                        if (balanceType == 13) {
                            remark += "未中奖保本退回金额:" + (bet.getBetMoney() / BigDecimalUtil.PLAYER_MONEY_UNIT);
                            balanceType = 13;
                        } else if (recountMoney > 0) {
                            remark += "中奖盈利:" + BigDecimalUtil.div(recountMoney, BigDecimalUtil.PLAYER_MONEY_UNIT, 2);
                            balanceType = 4;
                        } else {
                            remark += "投注返奖扣除:" + BigDecimalUtil.div(recountMoney, BigDecimalUtil.PLAYER_MONEY_UNIT, 2);
                            balanceType = 9;
                        }
                        //账变记录
                        ballBalanceChangeService.insert(BallBalanceChange.builder()
                                .playerId(player.getId())
                                .accountType(player.getAccountType())
                                .userId(player.getUserId())
                                .parentId(player.getSuperiorId())
                                .username(player.getUsername())
                                .superTree(player.getSuperTree())
                                .initMoney(player.getBalance())
                                .changeMoney(recountMoney)
                                .dnedMoney(editPlayer.getBalance())
                                //key recharge_self
                                .createdAt(System.currentTimeMillis())
                                //盈利为4,扣除为9,保本为13
                                .balanceChangeType(balanceType)
                                .remark(remark)
                                .build());
                        break;
                    }
                    player = playerService.findById(player.getId());
                }
            }
        }
    }

    public static class BetRollbackThread implements Runnable {


        private BallBet bet;
        private IPlayerBetService betService;
        private BallGame game;
        private IBasePlayerService playerService;
        private IBallBalanceChangeService ballBalanceChangeService;
        private BallAdmin ballAdmin;

        public BetRollbackThread(BallBet bet,
                                 IPlayerBetService betService, BallGame game,
                                 IBasePlayerService playerService,
                                 IBallBalanceChangeService ballBalanceChangeService,
                                 BallAdmin ballAdmin) {
            this.bet = bet;
            this.betService = betService;
            this.game = game;
            this.playerService = playerService;
            this.ballBalanceChangeService = ballBalanceChangeService;
            this.ballAdmin = ballAdmin;
        }


        @Override
        public void run() {
            BallBet edit = BallBet.builder()
                    .status(4)
                    .winningAmount(0L)
                    .settlememntPerson(ballAdmin.getUsername())
                    .build();
            edit.setId(bet.getId());
            //加回投注金额减去中奖金额
            long rollbackMoney = Math.abs(bet.getBetMoney()) - bet.getWinningAmount();
            boolean isSucc = betService.edit(edit);
            if (isSucc) {
                //更新账号余额,中奖金额
                BallPlayer player = playerService.findById(bet.getPlayerId());
                while (true) {
                    BallPlayer editPlayer = BallPlayer.builder()
                            .balance(player.getBalance() + rollbackMoney)
                            //累计中奖减回
                            .cumulativeWinning(player.getCumulativeWinning() - bet.getWinningAmount())
                            //累计投注减回
                            .accumulativeBet(player.getAccumulativeBet() - bet.getBetMoney())
                            .version(player.getVersion())
                            .build();
                    editPlayer.setId(player.getId());
                    boolean isSuccess = playerService.editAndClearCache(editPlayer, player);
                    if (isSuccess) {
                        //加回投注账变
                        ballBalanceChangeService.insert(BallBalanceChange.builder()
                                .playerId(player.getId())
                                .accountType(player.getAccountType())
                                .userId(player.getUserId())
                                .parentId(player.getSuperiorId())
                                .username(player.getUsername())
                                .superTree(player.getSuperTree())
                                .initMoney(player.getBalance())
                                .changeMoney(bet.getBetMoney())
                                .dnedMoney(player.getBalance() + bet.getBetMoney())
                                .createdAt(System.currentTimeMillis())
                                .balanceChangeType(14)
                                .build());
                        //如果中奖增加扣除账变
                        if (bet.getWinningAmount() > 0) {
                            ballBalanceChangeService.insert(BallBalanceChange.builder()
                                    .playerId(player.getId())
                                    .accountType(player.getAccountType())
                                    .userId(player.getUserId())
                                    .parentId(player.getSuperiorId())
                                    .username(player.getUsername())
                                    .superTree(player.getSuperTree())
                                    .initMoney(player.getBalance() + bet.getBetMoney())
                                    .changeMoney(-bet.getWinningAmount())
                                    .dnedMoney(player.getBalance() + bet.getBetMoney() - bet.getWinningAmount())
                                    .createdAt(System.currentTimeMillis())
                                    .balanceChangeType(9)
                                    .build());
                        }
                        break;
                    }
                    player = playerService.findById(player.getId());
                }
            }
        }
    }
}
