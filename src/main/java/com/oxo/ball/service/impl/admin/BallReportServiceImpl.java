package com.oxo.ball.service.impl.admin;

import com.oxo.ball.bean.dao.*;
import com.oxo.ball.bean.dto.req.report.*;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.bean.dto.resp.report.RechargeWithdrawalResponse;
import com.oxo.ball.bean.dto.resp.report.ReportStandardDTO;
import com.oxo.ball.bean.dto.resp.report.ReportStandardPlayerDTO;
import com.oxo.ball.service.admin.*;
import com.oxo.ball.service.impl.BasePlayerService;
import com.oxo.ball.service.player.IPlayerBetService;
import com.oxo.ball.utils.BigDecimalUtil;
import com.oxo.ball.utils.ResponseMessageUtil;
import com.oxo.ball.utils.ThreadPoolUtil;
import com.oxo.ball.utils.TimeUtil;
import io.swagger.models.auth.In;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

@Service
public class BallReportServiceImpl implements IBallReportService {
    @Autowired
    IBallLoggerHandsupService loggerHandsupService;
    @Autowired
    IBallLoggerRechargeService loggerRechargeService;
    @Autowired
    IBallLoggerWithdrawalService loggerWithdrawalService;
    @Autowired
    IBallPlayerService ballPlayerService;
    @Autowired
    IBallBetService ballBetService;
    @Autowired
    IBallLoggerBetService loggerBetService;
    @Autowired
    IBallGameService gameService;
    @Autowired
    IBallBalanceChangeService ballBalanceChangeService;
    @Autowired
    BasePlayerService basePlayerService;
    @Autowired
    IBallSystemConfigService systemConfigService;
    @Autowired
    IPlayerBetService playerBetService;

    @Override
    public BaseResponse reportDataTotal() {
        Map<String, Object> data = new HashMap<>();
        //人工上分差额
        Long upDownHandsup = loggerHandsupService.statisUpDown();
        //线上线下充值
        Long recharge = loggerRechargeService.statisTotal();
        //提现
        Long withdrawal = loggerWithdrawalService.statisTotal();
        //人工提现
        Long unWithdrawal = loggerWithdrawalService.statisTotalNot();
        //充提差金额,非人工
        data.put("upDownNotHandsup", recharge - withdrawal);
        //充提差总
        data.put("upDownTotal", upDownHandsup + recharge - withdrawal);
        //全体余额
        BallPlayer playerStatis = ballPlayerService.statisTotal();
        //盈亏,中奖-投注
//        Long win = playerStatis.getCumulativeWinning() == null ? 0L : playerStatis.getCumulativeWinning();
//        Long bets = playerStatis.getAccumulativeBet() == null ? 0L : playerStatis.getAccumulativeBet();
//        data.put("winLose", win - bets);
        //余额总
        long totalBalance = playerStatis.getBalance() == null ? 0 : playerStatis.getBalance();
        //优惠总=
        data.put("totalDiscount", playerStatis.getCumulativeDiscount() == null ? 0 : playerStatis.getCumulativeDiscount());
        //用户总
        data.put("totalPlayer", playerStatis.getId() == null ? 0 : playerStatis.getId());
        //未结算总
        BallBet notOpen = ballBetService.statisNotOpen();
        data.put("betCounts", notOpen.getId());
        data.put("betTotal", notOpen.getBetMoney());
        totalBalance += notOpen.getBetMoney() == null ? 0 : notOpen.getBetMoney();
        data.put("totalBalance", totalBalance + unWithdrawal);
        //冻结金额=提现冻结+下注冻结
        Long frozen = ballPlayerService.statisFrozen();
        data.put("frozen", frozen + (notOpen.getBetMoney() == null ? 0 : notOpen.getBetMoney()));
        return BaseResponse.successWithData(data);
    }

    @Override
    public BaseResponse reportData(ReportDataRequest reportDataRequest) {
        Map<String, Object> data = new HashMap<>();
        data.put("player", ballPlayerService.statisTotal(reportDataRequest));
        data.put("recharge", loggerRechargeService.statisTotal(reportDataRequest));
        data.put("withdrawal", loggerWithdrawalService.statisTotal(reportDataRequest));
        data.put("bets", ballBetService.statisTotal(reportDataRequest));
        return BaseResponse.successWithData(data);
    }

    @Override
    public BaseResponse reportRechargeWithdrawal(ReportDataRequest reportDataRequest) {
        List<Map<String, Object>> loggerRecharge = loggerRechargeService.statisByType(reportDataRequest);
        List<Map<String, Object>> loggerWithdrawal = loggerWithdrawalService.statisByType(reportDataRequest);
        List<BallBet> bets = ballBetService.statisByType(reportDataRequest);
        Map<String, Object> data = new HashMap<>();
        data.put("recharge", loggerRecharge);
        data.put("withdrawal", loggerWithdrawal);
        data.put("bets", bets);
        return BaseResponse.successWithData(data);
    }

    @Override
    public BaseResponse reportGame(ReportDataRequest reportDataRequest) {
        Map<String, Map<String, Object>> dataMap = new HashMap<>();
        //查询一共多少场次,未开赛
        List<BallGame> ballGames = gameService.statisReport(reportDataRequest);
        for (BallGame item : ballGames) {
            Map<String, Object> map = new HashMap<>();
            dataMap.put(item.getYmd(), map);
            map.put("ymd", item.getYmd());
            map.put("gameCount", item.getId());
            map.put("notStart", item.getGameStatus());
            map.put("betCounts", 0);
            map.put("bbetCounts", 0);
        }
        int pageNo = 1;
        while (true) {
            SearchResponse<BallBet> search = ballBetService.search(reportDataRequest, pageNo++, 1000);
            if (search.getResults() == null || search.getResults().isEmpty()) {
                break;
            }
            for (BallBet bet : search.getResults()) {
                String ymd = TimeUtil.dateFormat(new Date(bet.getCreatedAt()), TimeUtil.TIME_YYYY_MM_DD);
                Map<String, Object> data = dataMap.get(ymd);
                if (data == null) {
                    continue;
                }
                Object betGame = data.get("betGame");
                if (betGame == null) {
                    Set<Long> set = new HashSet<>();
                    set.add(bet.getGameId());
                    data.put("betGame", set);
                } else {
                    Set<Long> set = (Set<Long>) betGame;
                    set.add(bet.getGameId());
                }
                if (bet.getEven() == 2) {
                    //下注笔数
                    Object betCounts = data.get("betCounts");
                    if (betCounts == null) {
                        data.put("betCounts", 1);
                    } else {
                        data.put("betCounts", 1 + Integer.parseInt(betCounts.toString()));
                    }
                    //下注人数
                    Object betPlayer = data.get("betPlayer");
                    if (betCounts == null) {
                        Set<Long> set = new HashSet<>();
                        set.add(bet.getPlayerId());
                        data.put("betPlayer", set);
                    } else {
                        Set<Long> set = (Set<Long>) betPlayer;
                        set.add(bet.getPlayerId());
                    }
                    //下注金额
                    Object betBalance = data.get("betBalance");
                    if (betBalance == null) {
                        data.put("betBalance", bet.getBetMoney());
                    } else {
                        data.put("betBalance", bet.getBetMoney() + Long.parseLong(betBalance.toString()));
                    }
                    //中奖人数
                    Object betBingo = data.get("betBingo");
                    //中奖金额
                    Object betBingoBalance = data.get("betBingoBalance");
                    //未中奖金额
                    Object betNotBingoBalance = data.get("betNotBingoBalance");
                    if (bet.getStatusOpen() == 1) {
                        if (betBingo == null) {
                            Set<Long> set = new HashSet<>();
                            set.add(bet.getPlayerId());
                            data.put("betBingo", set);
                        } else {
                            Set<Long> set = (Set<Long>) betBingo;
                            set.add(bet.getPlayerId());
                        }
                        if (betBingoBalance == null) {
                            data.put("betBingoBalance", bet.getWinningAmount());
                        } else {
                            data.put("betBingoBalance", bet.getWinningAmount() + Long.parseLong(betBingoBalance.toString()));
                        }
                    } else {
                        if (betNotBingoBalance == null) {
                            data.put("betNotBingoBalance", bet.getBetMoney());
                        } else {
                            data.put("betNotBingoBalance", bet.getBetMoney() + Long.parseLong(betNotBingoBalance.toString()));
                        }
                    }
                    //手续费
                    Object betHandMoney = data.get("betHandMoney");
                    if (betBalance == null) {
                        data.put("betHandMoney", bet.getHandMoney());
                    } else {
                        data.put("betHandMoney", bet.getHandMoney() + Long.parseLong(betHandMoney.toString()));
                    }
                    //盈亏金额
                    Object betWinLose = data.get("betWinLose");
                    if (betBalance == null) {
                        data.put("betWinLose", bet.getWinningAmount() - bet.getBetMoney());
                    } else {
                        data.put("betWinLose", (bet.getWinningAmount() - bet.getBetMoney()) + Long.parseLong(betWinLose.toString()));
                    }
                } else {
                    //保本
                    //下注笔数
                    Object betCounts = data.get("bbetCounts");
                    if (betCounts == null) {
                        data.put("bbetCounts", 1);
                    } else {
                        data.put("bbetCounts", 1 + Integer.parseInt(betCounts.toString()));
                    }
                    //下注人数
                    Object betPlayer = data.get("bbetPlayer");
                    if (betCounts == null) {
                        Set<Long> set = new HashSet<>();
                        set.add(bet.getPlayerId());
                        data.put("bbetPlayer", set);
                    } else {
                        Set<Long> set = (Set<Long>) betPlayer;
                        set.add(bet.getPlayerId());
                    }
                    //下注金额
                    Object betBalance = data.get("bbetBalance");
                    if (betBalance == null) {
                        data.put("bbetBalance", bet.getBetMoney());
                    } else {
                        data.put("bbetBalance", bet.getBetMoney() + Long.parseLong(betBalance.toString()));
                    }
                    //中奖人数
                    Object betBingo = data.get("bbetBingo");
                    //中奖金额
                    Object betBingoBalance = data.get("bbetBingoBalance");
                    //未中奖金额
                    Object betNotBingoBalance = data.get("bbetNotBingoBalance");
                    if (bet.getStatusOpen() == 1) {
                        if (betBingo == null) {
                            Set<Long> set = new HashSet<>();
                            set.add(bet.getPlayerId());
                            data.put("bbetBingo", set);
                        } else {
                            Set<Long> set = (Set<Long>) betBingo;
                            set.add(bet.getPlayerId());
                        }
                        if (betBingoBalance == null) {
                            data.put("bbetBingoBalance", bet.getWinningAmount());
                        } else {
                            data.put("bbetBingoBalance", bet.getWinningAmount() + Long.parseLong(betBingoBalance.toString()));
                        }
                    } else {
                        if (betNotBingoBalance == null) {
                            data.put("bbetNotBingoBalance", bet.getBetMoney());
                        } else {
                            data.put("bbetNotBingoBalance", bet.getBetMoney() + Long.parseLong(betNotBingoBalance.toString()));
                        }
                    }
                    //手续费
                    Object betHandMoney = data.get("bbetHandMoney");
                    if (betBalance == null) {
                        data.put("bbetHandMoney", bet.getHandMoney());
                    } else {
                        data.put("bbetHandMoney", bet.getHandMoney() + Long.parseLong(betHandMoney.toString()));
                    }
                    //盈亏金额
                    Object betWinLose = data.get("bbetWinLose");
                    if (betBalance == null) {
                        data.put("bbetWinLose", bet.getWinningAmount() - bet.getBetMoney());
                    } else {
                        data.put("bbetWinLose", (bet.getWinningAmount() - bet.getBetMoney()) + Long.parseLong(betWinLose.toString()));
                    }
                }
            }
        }
        for (Map<String, Object> mitem : dataMap.values()) {
            //下注场次
            Object betGame = mitem.get("betGame");
            if (betGame != null) {
                Set<Long> set = (Set<Long>) betGame;
                mitem.put("betGameCount", set.size());
            } else {
                mitem.put("betGameCount", 0);
            }
            //人均盈亏 = 盈亏/下注人数
            Object betWinLose = mitem.get("betWinLose");
            Object betPlayer = mitem.get("betPlayer");
            int betPlayerCount = 0;
            Long bwl = 0L;
            if (betPlayer != null) {
                Set<Long> set = (Set<Long>) betPlayer;
                betPlayerCount = set.size();
            }
            if (betWinLose != null) {
                bwl = (Long) betWinLose;
            }
            double div = betPlayerCount == 0 ? 0 : BigDecimalUtil.div(bwl, betPlayerCount);
            mitem.put("betPlayerCount", betPlayerCount);
            mitem.put("betWinLosePerPlayer", div);
            //盈亏率 = 中奖人数/下注人数
            Object betBingo = mitem.get("betBingo");
            int betBingoCount = 0;
            if (betBingo != null) {
                Set<Long> set = (Set<Long>) betBingo;
                betBingoCount = set.size();
            }
            double div1 = betPlayerCount == 0 ? 0 : BigDecimalUtil.div(betBingoCount, betPlayerCount);
            mitem.put("betWinLosePer", div1);
            mitem.put("betBingoCount", betBingoCount);
            //保本
            Object bbetWinLose = mitem.get("bbetWinLose");
            Object bbetPlayer = mitem.get("bbetPlayer");
            int bbetPlayerCount = 0;
            Long bbwl = 0L;
            if (bbetPlayer != null) {
                Set<Long> set = (Set<Long>) bbetPlayer;
                bbetPlayerCount = set.size();
            }
            if (bbetWinLose != null) {
                bbwl = (Long) bbetWinLose;
            }
            double bdiv = bbetPlayerCount == 0 ? 0 : BigDecimalUtil.div(bbwl, bbetPlayerCount);
            mitem.put("bbetWinLosePerPlayer", bdiv);
            mitem.put("bbetPlayerCount", bbetPlayerCount);
            //盈亏率 = 中奖人数/下注人数
            Object bbetBingo = mitem.get("bbetBingo");
            int bbetBingoCount = 0;
            if (bbetBingo != null) {
                Set<Long> set = (Set<Long>) bbetBingo;
                bbetBingoCount = set.size();
            }
            double bdiv1 = bbetPlayerCount == 0 ? 0 : BigDecimalUtil.div(bbetBingoCount, bbetPlayerCount);
            mitem.put("bbetWinLosePer", bdiv1);
            mitem.put("bbetBingoCount", bbetBingoCount);
        }
        return BaseResponse.successWithData(dataMap.values());
    }

    @Override
    public SearchResponse reportGame(ReportGameRequest reportGameRequest) {
        //分页查询指定日期内的赛事,然后按赛事统计订单
        SearchResponse<BallGame> ballGames = gameService.statisReport(reportGameRequest);
        List<BallGame> results = ballGames.getResults();
        SearchResponse searchResponse = new SearchResponse();
        if (results.isEmpty()) {
            return searchResponse;
        }
        Map<Long, Map<String, Object>> dataMap = new HashMap<>();
        List<Long> gameIds = new ArrayList<>();
        for (BallGame item : results) {
            gameIds.add(item.getId());
            Map<String, Object> map = new HashMap<>();
            dataMap.put(item.getId(), map);
            map.put("id", item.getId());
            map.put("ymd", item.getYmd());
            map.put("info", item.getMainName() + " VS " + item.getGuestName());
            map.put("betCounts", 0);
            map.put("bbetCounts", 0);
        }
        int pageNo = 1;
        while (true) {
            SearchResponse<BallBet> response = ballBetService.statisReport(gameIds, pageNo++);
            List<BallBet> results1 = response.getResults();
            if (results1 == null || results1.isEmpty()) {
                break;
            }
            for (BallBet bet : results1) {
                Map<String, Object> data = dataMap.get(bet.getGameId());
                if (data == null) {
                    continue;
                }
                if (bet.getEven() == 2) {
                    //下注笔数
                    Object betCounts = data.get("betCounts");
                    if (betCounts == null) {
                        data.put("betCounts", 1);
                    } else {
                        data.put("betCounts", 1 + Integer.parseInt(betCounts.toString()));
                    }
                    //下注人数
                    Object betPlayer = data.get("betPlayer");
                    if (betPlayer == null) {
                        Set<Long> set = new HashSet<>();
                        set.add(bet.getPlayerId());
                        data.put("betPlayer", set);
                    } else {
                        Set<Long> set = (Set<Long>) betPlayer;
                        set.add(bet.getPlayerId());
                    }
                    //下注金额
                    Object betBalance = data.get("betBalance");
                    if (betBalance == null) {
                        data.put("betBalance", bet.getBetMoney());
                    } else {
                        data.put("betBalance", bet.getBetMoney() + Long.parseLong(betBalance.toString()));
                    }
                    //中奖人数
                    Object betBingo = data.get("betBingo");
                    //中奖金额
                    Object betBingoBalance = data.get("betBingoBalance");
                    if (bet.getStatusOpen() == 1) {
                        if (betBingo == null) {
                            Set<Long> set = new HashSet<>();
                            set.add(bet.getPlayerId());
                            data.put("betBingo", set);
                        } else {
                            Set<Long> set = (Set<Long>) betBingo;
                            set.add(bet.getPlayerId());
                        }
                        if (betBingoBalance == null) {
                            data.put("betBingoBalance", bet.getWinningAmount());
                        } else {
                            data.put("betBingoBalance", bet.getWinningAmount() + Long.parseLong(betBingoBalance.toString()));
                        }
                    }
                    //手续费
                    Object betHandMoney = data.get("betHandMoney");
                    if (betHandMoney == null) {
                        data.put("betHandMoney", bet.getHandMoney());
                    } else {
                        data.put("betHandMoney", bet.getHandMoney() + Long.parseLong(betHandMoney.toString()));
                    }
                    //盈亏金额
                    Object betWinLose = data.get("betWinLose");
                    if (betWinLose == null) {
                        data.put("betWinLose", bet.getWinningAmount() - bet.getBetMoney());
                    } else {
                        data.put("betWinLose", (bet.getWinningAmount() - bet.getBetMoney()) + Long.parseLong(betWinLose.toString()));
                    }
                } else {
                    //保本
                    //下注笔数
                    Object betCounts = data.get("bbetCounts");
                    if (betCounts == null) {
                        data.put("bbetCounts", 1);
                    } else {
                        data.put("bbetCounts", 1 + Integer.parseInt(betCounts.toString()));
                    }
                    //下注人数
                    Object betPlayer = data.get("bbetPlayer");
                    if (betPlayer == null) {
                        Set<Long> set = new HashSet<>();
                        set.add(bet.getPlayerId());
                        data.put("bbetPlayer", set);
                    } else {
                        Set<Long> set = (Set<Long>) betPlayer;
                        set.add(bet.getPlayerId());
                    }
                    //下注金额
                    Object betBalance = data.get("bbetBalance");
                    if (betBalance == null) {
                        data.put("bbetBalance", bet.getBetMoney());
                    } else {
                        data.put("bbetBalance", bet.getBetMoney() + Long.parseLong(betBalance.toString()));
                    }
                    //中奖人数
                    Object betBingo = data.get("bbetBingo");
                    //中奖金额
                    Object betBingoBalance = data.get("bbetBingoBalance");
                    if (bet.getStatusOpen() == 1) {
                        if (betBingo == null) {
                            Set<Long> set = new HashSet<>();
                            set.add(bet.getPlayerId());
                            data.put("bbetBingo", set);
                        } else {
                            Set<Long> set = (Set<Long>) betBingo;
                            set.add(bet.getPlayerId());
                        }
                        if (betBingoBalance == null) {
                            data.put("bbetBingoBalance", bet.getWinningAmount());
                        } else {
                            data.put("bbetBingoBalance", bet.getWinningAmount() + Long.parseLong(betBingoBalance.toString()));
                        }
                    }
                    //手续费
                    Object betHandMoney = data.get("bbetHandMoney");
                    if (betHandMoney == null) {
                        data.put("bbetHandMoney", bet.getHandMoney());
                    } else {
                        data.put("bbetHandMoney", bet.getHandMoney() + Long.parseLong(betHandMoney.toString()));
                    }
                    //盈亏金额
                    Object betWinLose = data.get("bbetWinLose");
                    if (betWinLose == null) {
                        data.put("bbetWinLose", bet.getWinningAmount() - bet.getBetMoney());
                    } else {
                        data.put("bbetWinLose", (bet.getWinningAmount() - bet.getBetMoney()) + Long.parseLong(betWinLose.toString()));
                    }
                }
            }
        }
        for (Map<String, Object> mitem : dataMap.values()) {
            //人均盈亏 = 盈亏/下注人数
            Object betPlayer = mitem.get("betPlayer");
            int betPlayerCount = 0;
            if (betPlayer != null) {
                Set<Long> set = (Set<Long>) betPlayer;
                betPlayerCount = set.size();
            }
            mitem.put("betPlayerCount", betPlayerCount);
            //盈亏率 = 中奖人数/下注人数
            Object betBingo = mitem.get("betBingo");
            int betBingoCount = 0;
            if (betBingo != null) {
                Set<Long> set = (Set<Long>) betBingo;
                betBingoCount = set.size();
            }
            mitem.put("betBingoCount", betBingoCount);
            //保本
            Object bbetPlayer = mitem.get("bbetPlayer");
            int bbetPlayerCount = 0;
            if (bbetPlayer != null) {
                Set<Long> set = (Set<Long>) bbetPlayer;
                bbetPlayerCount = set.size();
            }
            mitem.put("bbetPlayerCount", bbetPlayerCount);
            //盈亏率 = 中奖人数/下注人数
            Object bbetBingo = mitem.get("bbetBingo");
            int bbetBingoCount = 0;
            if (bbetBingo != null) {
                Set<Long> set = (Set<Long>) bbetBingo;
                bbetBingoCount = set.size();
            }
            mitem.put("bbetBingoCount", bbetBingoCount);
        }
        searchResponse.setPageNo(ballGames.getPageNo());
        searchResponse.setPageSize(ballGames.getPageSize());
        searchResponse.setTotalCount(ballGames.getTotalCount());
        searchResponse.setTotalPage(ballGames.getTotalPage());
        searchResponse.setResults(new ArrayList(dataMap.values()));
        return searchResponse;
    }

    @Override
    public BaseResponse reportOperate(ReportOperateRequest reportOperateRequest) {
        //统计运营
        SearchResponse<BallBalanceChange> ballBalanceChangeSearchResponse = ballBalanceChangeService.statisReport(reportOperateRequest);
        List<BallBalanceChange> results = ballBalanceChangeSearchResponse.getResults();
        for (BallBalanceChange item : results) {
            BallPlayer player = basePlayerService.findById(item.getPlayerId());
            item.setPlayerName(player.getUsername());
            item.setId(player.getUserId());
            item.setPlayerParent(player.getSuperiorName());
            item.setBalance(player.getBalance());
            Long bingoMoney = item.getBingoMoney() == null ? 0 : Long.parseLong(item.getBingoMoney().toString());
            Long betMoney = item.getBetMoney() == null ? 0 : Long.parseLong(item.getBetMoney().toString());
            item.setWin(bingoMoney - Math.abs(betMoney));
        }
        return BaseResponse.successWithData(ballBalanceChangeSearchResponse);
    }

    @Override
    public BaseResponse reportBet(ReportDataRequest reportDataRequest) {
        Map<String, Map<String, Object>> dataMap = new HashMap<>();
        int pageNo = 1;
        while (true) {
            SearchResponse<BallBet> search = ballBetService.search(reportDataRequest, pageNo++, 1000);
            if (search.getResults() == null || search.getResults().isEmpty()) {
                break;
            }
            for (BallBet bet : search.getResults()) {
                String ymd = TimeUtil.dateFormat(new Date(bet.getCreatedAt()), TimeUtil.TIME_YYYY_MM_DD);
                Map<String, Object> data = dataMap.get(ymd);
                if (data == null) {
                    data = new HashMap<>();
                    data.put("ymd", ymd);
                    data.put("betCounts", 0);
                    data.put("bbetCounts", 0);
                    dataMap.put(ymd, data);
                }
                if (bet.getEven() == 2) {
                    //下注笔数
                    Object betCounts = data.get("betCounts");
                    if (betCounts == null) {
                        data.put("betCounts", 1);
                    } else {
                        data.put("betCounts", 1 + Integer.parseInt(betCounts.toString()));
                    }
                    //下注人数
                    Object betPlayer = data.get("betPlayer");
                    if (betPlayer == null) {
                        Set<Long> set = new HashSet<>();
                        set.add(bet.getPlayerId());
                        data.put("betPlayer", set);
                    } else {
                        Set<Long> set = (Set<Long>) betPlayer;
                        set.add(bet.getPlayerId());
                    }
                    //下注金额
                    Object betBalance = data.get("betBalance");
                    if (betBalance == null) {
                        data.put("betBalance", bet.getBetMoney());
                    } else {
                        data.put("betBalance", bet.getBetMoney() + Long.parseLong(betBalance.toString()));
                    }
                    //中奖人数
                    Object betBingo = data.get("betBingo");
                    //中奖金额
                    Object betBingoBalance = data.get("betBingoBalance");
                    //未中奖金额
                    Object betNotBingoBalance = data.get("betNotBingoBalance");
                    if (bet.getStatusOpen() == 1) {
                        if (betBingo == null) {
                            Set<Long> set = new HashSet<>();
                            set.add(bet.getPlayerId());
                            data.put("betBingo", set);
                        } else {
                            Set<Long> set = (Set<Long>) betBingo;
                            set.add(bet.getPlayerId());
                        }
                        if (betBingoBalance == null) {
                            data.put("betBingoBalance", bet.getWinningAmount());
                        } else {
                            data.put("betBingoBalance", bet.getWinningAmount() + Long.parseLong(betBingoBalance.toString()));
                        }
                    } else {
                        if (betNotBingoBalance == null) {
                            data.put("betNotBingoBalance", bet.getBetMoney());
                        } else {
                            data.put("betNotBingoBalance", bet.getBetMoney() + Long.parseLong(betNotBingoBalance.toString()));
                        }
                    }
                    //手续费
                    Object betHandMoney = data.get("betHandMoney");
                    if (betHandMoney == null) {
                        data.put("betHandMoney", bet.getHandMoney());
                    } else {
                        data.put("betHandMoney", bet.getHandMoney() + Long.parseLong(betHandMoney.toString()));
                    }
                    //盈亏金额
                    Object betWinLose = data.get("betWinLose");
                    if (betWinLose == null) {
                        data.put("betWinLose", bet.getWinningAmount() - bet.getBetMoney());
                    } else {
                        data.put("betWinLose", (bet.getWinningAmount() - bet.getBetMoney()) + Long.parseLong(betWinLose.toString()));
                    }
                } else {
                    //保本
                    //下注笔数
                    Object betCounts = data.get("bbetCounts");
                    if (betCounts == null) {
                        data.put("bbetCounts", 1);
                    } else {
                        data.put("bbetCounts", 1 + Integer.parseInt(betCounts.toString()));
                    }
                    //下注人数
                    Object betPlayer = data.get("bbetPlayer");
                    if (betPlayer == null) {
                        Set<Long> set = new HashSet<>();
                        set.add(bet.getPlayerId());
                        data.put("bbetPlayer", set);
                    } else {
                        Set<Long> set = (Set<Long>) betPlayer;
                        set.add(bet.getPlayerId());
                    }
                    //下注金额
                    Object betBalance = data.get("bbetBalance");
                    if (betBalance == null) {
                        data.put("bbetBalance", bet.getBetMoney());
                    } else {
                        data.put("bbetBalance", bet.getBetMoney() + Long.parseLong(betBalance.toString()));
                    }
                    //中奖人数
                    Object betBingo = data.get("bbetBingo");
                    //中奖金额
                    Object betBingoBalance = data.get("bbetBingoBalance");
                    //未中奖金额
                    Object betNotBingoBalance = data.get("bbetNotBingoBalance");
                    if (bet.getStatusOpen() == 1) {
                        if (betBingo == null) {
                            Set<Long> set = new HashSet<>();
                            set.add(bet.getPlayerId());
                            data.put("bbetBingo", set);
                        } else {
                            Set<Long> set = (Set<Long>) betBingo;
                            set.add(bet.getPlayerId());
                        }
                        if (betBingoBalance == null) {
                            data.put("bbetBingoBalance", bet.getWinningAmount());
                        } else {
                            data.put("bbetBingoBalance", bet.getWinningAmount() + Long.parseLong(betBingoBalance.toString()));
                        }
                    } else {
                        if (betNotBingoBalance == null) {
                            data.put("bbetNotBingoBalance", bet.getBetMoney());
                        } else {
                            data.put("bbetNotBingoBalance", bet.getBetMoney() + Long.parseLong(betNotBingoBalance.toString()));
                        }
                    }
                    //手续费
                    Object betHandMoney = data.get("bbetHandMoney");
                    if (betHandMoney == null) {
                        data.put("bbetHandMoney", bet.getHandMoney());
                    } else {
                        data.put("bbetHandMoney", bet.getHandMoney() + Long.parseLong(betHandMoney.toString()));
                    }
                    //盈亏金额
                    Object betWinLose = data.get("bbetWinLose");
                    if (betWinLose == null) {
                        data.put("bbetWinLose", bet.getWinningAmount() - bet.getBetMoney());
                    } else {
                        data.put("bbetWinLose", (bet.getWinningAmount() - bet.getBetMoney()) + Long.parseLong(betWinLose.toString()));
                    }
                }
            }
        }
        for (Map<String, Object> mitem : dataMap.values()) {
            //人均盈亏 = 盈亏/下注人数
            Object betBingoBalance = mitem.get("betBingoBalance");
            Object betWinLose = mitem.get("betWinLose");
            Object betPlayer = mitem.get("betPlayer");
            int betPlayerCount = 0;
            Object betBalance = mitem.get("betBalance");
            long betBalanceLong = 0L;
            long betBingoBalanceLong = 0L;
            long betWinLoseLong = 0L;
            if (betWinLose != null) {
                betWinLoseLong = (long) betWinLose;
            }
            if (betPlayer != null) {
                Set<Long> set = (Set<Long>) betPlayer;
                betPlayerCount = set.size();
            }
            if (betBingoBalance != null) {
                betBingoBalanceLong = (Long) betBingoBalance;
            }
            if (betBalance != null) {
                betBalanceLong = (long) betBalance;
            }
            double div = betPlayerCount == 0 ? 0 : BigDecimalUtil.div(betWinLoseLong, betPlayerCount);
            mitem.put("betPlayerCount", betPlayerCount);
            mitem.put("betWinLosePerPlayer", div);
            //盈亏率 = 中奖金额/投注金额
            Object betBingo = mitem.get("betBingo");
            int betBingoCount = 0;
            if (betBingo != null) {
                Set<Long> set = (Set<Long>) betBingo;
                betBingoCount = set.size();
            }
            double div1 = betBalanceLong == 0 ? 0 : BigDecimalUtil.div(betBingoBalanceLong, Math.abs(betBalanceLong));
            mitem.put("betWinLosePer", betBalanceLong == 0 ? 0 : 100 - BigDecimalUtil.mul(div1, 100));
            mitem.put("betBingoCount", betBingoCount);

            //保本-----------------------------------------------
            Object bbetWinLose = mitem.get("bbetWinLose");
            Object bbetBingoBalance = mitem.get("bbetBingoBalance");
            Object bbetPlayer = mitem.get("bbetPlayer");
            Object bbetBalance = mitem.get("bbetBalance");
            int bbetPlayerCount = 0;
            long bbetWinLoseLong = 0L;
            long bbetBalanceLong = 0L;
            long bbetBingoBalanceLong = 0L;
            if (bbetPlayer != null) {
                Set<Long> set = (Set<Long>) bbetPlayer;
                bbetPlayerCount = set.size();
            }
            if (bbetWinLose != null) {
                bbetWinLoseLong = (Long) bbetWinLose;
            }
            if (bbetBalance != null) {
                bbetBalanceLong = (long) bbetBalance;
            }
            if (bbetBingoBalance != null) {
                bbetBingoBalanceLong = (long) bbetBingoBalance;
            }
            double bdiv = bbetPlayerCount == 0 ? 0 : BigDecimalUtil.div(bbetWinLoseLong, bbetPlayerCount);
            mitem.put("bbetWinLosePerPlayer", bdiv);
            mitem.put("bbetPlayerCount", bbetPlayerCount);
            //盈亏率 = 中奖金额/下注金额
            Object bbetBingo = mitem.get("bbetBingo");
            int bbetBingoCount = 0;
            if (bbetBingo != null) {
                Set<Long> set = (Set<Long>) bbetBingo;
                bbetBingoCount = set.size();
            }
            double bdiv1 = bbetBalanceLong == 0 ? 0 : BigDecimalUtil.div(bbetBingoBalanceLong, Math.abs(bbetBalanceLong));
            mitem.put("bbetWinLosePer", bbetBalanceLong == 0 ? 0 : 100 - BigDecimalUtil.mul(bdiv1, 100));
            mitem.put("bbetBingoCount", bbetBingoCount);
        }
        return BaseResponse.successWithData(dataMap.values());
    }

    @Override
    public BaseResponse reportPlayerDay(ReportPlayerDayRequest reportDataRequest) {
        Map<String, Map<String, Object>> dataMap = new HashMap<>();
        int pageNo = 1;
        while (true) {
            SearchResponse<BallBalanceChange> search = ballBalanceChangeService.search(reportDataRequest, pageNo++, 1000);
            if (search.getResults() == null || search.getResults().isEmpty()) {
                break;
            }
            for (BallBalanceChange item : search.getResults()) {
                String ymd = TimeUtil.dateFormat(new Date(item.getCreatedAt()), TimeUtil.TIME_YYYY_MM_DD);
                Map<String, Object> data = dataMap.get(ymd + item.getPlayerId());
                if (data == null) {
                    data = new HashMap<>();
                    BallPlayer player = basePlayerService.findById(item.getPlayerId());
                    data.put("ymd", ymd);
                    data.put("username", player.getUsername());
                    data.put("userId", player.getUserId());
                    dataMap.put(ymd + item.getPlayerId(), data);
                }
                String key = "a" + item.getBalanceChangeType();
                Object citem = data.get(key);
                if (citem == null) {
                    citem = item.getChangeMoney();
                } else {
                    citem = item.getChangeMoney() + Long.parseLong(citem.toString());
                }
                data.put(key, citem);

                if (item.getQr() > 0) {
                    Object totalQr = data.get("totalQr");
                    if (totalQr == null) {
                        data.put("totalQr", item.getQr());
                    } else {
                        data.put("totalQr", item.getQr() + Long.parseLong(totalQr.toString()));
                    }
                }
            }
        }
        return BaseResponse.successWithData(dataMap.values());
    }

    @Override
    public BaseResponse reportBalanceChange(ReportBalanceChangeRequest queryRequest) {
        SearchResponse<BallBalanceChange> search = ballBalanceChangeService.search(queryRequest);
        return BaseResponse.successWithData(search);
    }

    @Override
    public BallBalanceChange reportBalanceChangeTotal(ReportBalanceChangeRequest queryRequest) {
        return ballBalanceChangeService.searchTotal(queryRequest);
    }

    @Override
    public BaseResponse reportRecharge(ReportDataRequest reportDataRequest) {
        Map<String, Map<String, Object>> dataMap = new HashMap<>();
        //查充提日志，查人工日志
        //充值日志
        rechargeStatis(reportDataRequest, dataMap);
        //人工充值日志
        rechargeStatisHandsup(reportDataRequest, dataMap);
        //提现日志
        withdrawalStatis(reportDataRequest, dataMap);
        //人工提现日志
        withdrawalStatisHandsup(reportDataRequest, dataMap);

        for (Map<String, Object> item : dataMap.values()) {
            int rechargeTotalPlayer = 0;
            int rechargeTotalCount = 0;
            long rechargeTotalBalance = 0L;
            int withdrawalTotalPlayer = 0;
            int withdrawalTotalCount = 0;
            long withdrawalTotalBalance = 0L;
            Object crechargePlayer = item.get("crechargePlayer");
            if (crechargePlayer != null) {
                Set<Long> set = (Set<Long>) crechargePlayer;
                item.put("crechargePlayerCount", set.size());
            } else {
                item.put("crechargePlayerCount", 0);
            }
            Object brechargePlayer = item.get("brechargePlayer");
            if (brechargePlayer != null) {
                Set<Long> set = (Set<Long>) brechargePlayer;
                item.put("brechargePlayerCount", set.size());
            } else {
                item.put("brechargePlayerCount", 0);
            }
            Object rechargePlayer = item.get("rechargePlayer");
            if (rechargePlayer != null) {
                Set<Long> set = (Set<Long>) rechargePlayer;
                item.put("rechargePlayerCount", set.size());
            } else {
                item.put("rechargePlayerCount", 0);
            }

            Object cwithdrawalPlayer = item.get("cwithdrawalPlayer");
            if (cwithdrawalPlayer != null) {
                Set<Long> set = (Set<Long>) cwithdrawalPlayer;
                item.put("cwithdrawalPlayerCount", set.size());
            } else {
                item.put("cwithdrawalPlayerCount", 0);
            }
            Object bwithdrawalPlayer = item.get("bwithdrawalPlayer");
            if (bwithdrawalPlayer != null) {
                Set<Long> set = (Set<Long>) bwithdrawalPlayer;
                item.put("bwithdrawalPlayerCount", set.size());
            } else {
                item.put("bwithdrawalPlayerCount", 0);
            }
            Object withdrawalPlayer = item.get("withdrawalPlayer");
            if (withdrawalPlayer != null) {
                Set<Long> set = (Set<Long>) withdrawalPlayer;
                item.put("withdrawalPlayerCount", set.size());
            } else {
                item.put("withdrawalPlayerCount", 0);
            }
            rechargeTotalPlayer += (BigDecimalUtil.objToInt(item.get("crechargePlayerCount"))
                    + BigDecimalUtil.objToInt(item.get("brechargePlayerCount"))
                    + BigDecimalUtil.objToInt(item.get("rechargePlayerCount")));
            rechargeTotalCount += (
                    BigDecimalUtil.objToInt(item.get("rechargeCount"))
                            + BigDecimalUtil.objToInt(item.get("brechargeCount"))
                            + BigDecimalUtil.objToInt(item.get("crechargeCount")));
            rechargeTotalBalance += (
                    BigDecimalUtil.objToInt(item.get("rechargeMoney"))
                            + BigDecimalUtil.objToInt(item.get("brechargeMoney"))
                            + BigDecimalUtil.objToInt(item.get("crechargeMoney")));

            withdrawalTotalPlayer += (BigDecimalUtil.objToInt(item.get("rechargePlaywithdrawal"))
                    + BigDecimalUtil.objToInt(item.get("bwithdrawalPlayerCount"))
                    + BigDecimalUtil.objToInt(item.get("withdrawalPlayerCount")));
            withdrawalTotalCount += (
                    BigDecimalUtil.objToInt(item.get("withdrawalCount"))
                            + BigDecimalUtil.objToInt(item.get("bwithdrawalCount"))
                            + BigDecimalUtil.objToInt(item.get("cwithdrawalCount")));
            withdrawalTotalBalance += (
                    BigDecimalUtil.objToInt(item.get("withdrawalMoney"))
                            + BigDecimalUtil.objToInt(item.get("bwithdrawalMoney"))
                            + BigDecimalUtil.objToInt(item.get("cwithdrawalMoney")));
            item.put("rechargeTotalPlayer", rechargeTotalPlayer);
            item.put("rechargeTotalCount", rechargeTotalCount);
            item.put("rechargeTotalBalance", rechargeTotalBalance);
            item.put("withdrawalTotalPlayer", withdrawalTotalPlayer);
            item.put("withdrawalTotalCount", withdrawalTotalCount);
            item.put("withdrawalTotalBalance", withdrawalTotalBalance);
        }

        return BaseResponse.successWithData(dataMap.values());
    }

    private void withdrawalStatisHandsup(ReportDataRequest reportDataRequest, Map<String, Map<String, Object>> dataMap) {
        int pageNo = 1;
        while (true) {
            SearchResponse<BallLoggerHandsup> searchReponse = loggerHandsupService.statisReport(reportDataRequest, 0, pageNo++, 1000);
            List<BallLoggerHandsup> results = searchReponse.getResults();
            if (results == null || results.isEmpty()) {

                break;
            }
            for (BallLoggerHandsup item : results) {
                String ymd = TimeUtil.dateFormat(new Date(item.getCreatedAt()), TimeUtil.TIME_YYYY_MM_DD);
                Map<String, Object> cdata = dataMap.get(ymd);
                cdata = initCdata(dataMap, ymd, cdata);
                //充值人数
                Object withdrawalPlayer = cdata.get("cwithdrawalPlayer");
                if (withdrawalPlayer == null) {
                    HashSet<Long> set = new HashSet<>();
                    set.add(item.getPlayerId());
                    cdata.put("cwithdrawalPlayer", set);
                } else {
                    HashSet<Long> set = (HashSet<Long>) withdrawalPlayer;
                    set.add(item.getPlayerId());
                }
                //充值笔数
                Object withdrawalCount = cdata.get("cwithdrawalCount");
                if (withdrawalCount == null) {
                    cdata.put("cwithdrawalCount", 1);
                } else {
                    cdata.put("cwithdrawalCount", 1 + Integer.parseInt(withdrawalCount.toString()));
                }
                //充值金额
                Object withdrawalMoney = cdata.get("cwithdrawalMoney");
                if (withdrawalMoney == null) {
                    cdata.put("cwithdrawalMoney", item.getMoney());
                } else {
                    cdata.put("cwithdrawalMoney", item.getMoney() + Long.parseLong(withdrawalMoney.toString()));
                }
            }
        }
    }

    private void withdrawalStatis(ReportDataRequest reportDataRequest, Map<String, Map<String, Object>> dataMap) {
        int pageNo = 1;
        while (true) {
            SearchResponse<BallLoggerWithdrawal> searchReponse = loggerWithdrawalService.statisReport(reportDataRequest, pageNo++, 1000);
            List<BallLoggerWithdrawal> results = searchReponse.getResults();
            if (results == null || results.isEmpty()) {
                break;
            }
            for (BallLoggerWithdrawal item : results) {
                String ymd = TimeUtil.dateFormat(new Date(item.getCreatedAt()), TimeUtil.TIME_YYYY_MM_DD);
                Map<String, Object> cdata = dataMap.get(ymd);
                cdata = initCdata(dataMap, ymd, cdata);
                if (item.getType() == 2) {
                    //USDT
                    //充值人数
                    Object withdrawalPlayer = cdata.get("withdrawalPlayer");
                    if (withdrawalPlayer == null) {
                        HashSet<Long> set = new HashSet<>();
                        set.add(item.getPlayerId());
                        cdata.put("withdrawalPlayer", set);
                    } else {
                        HashSet<Long> set = (HashSet<Long>) withdrawalPlayer;
                        set.add(item.getPlayerId());
                    }
                    //充值笔数
                    Object withdrawalCount = cdata.get("withdrawalCount");
                    if (withdrawalCount == null) {
                        cdata.put("withdrawalCount", 1);
                    } else {
                        cdata.put("withdrawalCount", 1 + Integer.parseInt(withdrawalCount.toString()));
                    }
                    //充值金额
                    Object withdrawalMoney = cdata.get("withdrawalMoney");
                    if (withdrawalMoney == null) {
                        cdata.put("withdrawalMoney", item.getMoney());
                    } else {
                        cdata.put("withdrawalMoney", item.getMoney() + Long.parseLong(withdrawalMoney.toString()));
                    }
                } else {
                    //充值人数
                    Object bwithdrawalPlayer = cdata.get("bwithdrawalPlayer");
                    if (bwithdrawalPlayer == null) {
                        HashSet<Long> set = new HashSet<>();
                        set.add(item.getPlayerId());
                        cdata.put("bwithdrawalPlayer", set);
                    } else {
                        HashSet<Long> set = (HashSet<Long>) bwithdrawalPlayer;
                        set.add(item.getPlayerId());
                    }
                    //充值笔数
                    Object bwithdrawalCount = cdata.get("bwithdrawalCount");
                    if (bwithdrawalCount == null) {
                        cdata.put("bwithdrawalCount", 1);
                    } else {
                        cdata.put("bwithdrawalCount", 1 + Integer.parseInt(bwithdrawalCount.toString()));
                    }
                    //充值金额
                    Object bwithdrawalMoney = cdata.get("bwithdrawalMoney");
                    if (bwithdrawalMoney == null) {
                        cdata.put("bwithdrawalMoney", item.getMoney());
                    } else {
                        cdata.put("bwithdrawalMoney", item.getMoney() + Long.parseLong(bwithdrawalMoney.toString()));
                    }
                }
            }
        }
    }

    private void rechargeStatisHandsup(ReportDataRequest reportDataRequest, Map<String, Map<String, Object>> dataMap) {
        int pageNo = 1;
        while (true) {
            SearchResponse<BallLoggerHandsup> searchReponse = loggerHandsupService.statisReport(reportDataRequest, 1, pageNo++, 1000);
            List<BallLoggerHandsup> results = searchReponse.getResults();
            if (results == null || results.isEmpty()) {

                break;
            }
            for (BallLoggerHandsup item : results) {
                String ymd = TimeUtil.dateFormat(new Date(item.getCreatedAt()), TimeUtil.TIME_YYYY_MM_DD);
                Map<String, Object> cdata = dataMap.get(ymd);
                cdata = initCdata(dataMap, ymd, cdata);
                //充值人数
                Object rechargePlayer = cdata.get("crechargePlayer");
                if (rechargePlayer == null) {
                    HashSet<Long> set = new HashSet<>();
                    set.add(item.getPlayerId());
                    cdata.put("crechargePlayer", set);
                } else {
                    HashSet<Long> set = (HashSet<Long>) rechargePlayer;
                    set.add(item.getPlayerId());
                }
                //充值笔数
                Object rechargeCount = cdata.get("crechargeCount");
                if (rechargeCount == null) {
                    cdata.put("crechargeCount", 1);
                } else {
                    cdata.put("crechargeCount", 1 + Integer.parseInt(rechargeCount.toString()));
                }
                //充值金额
                Object rechargeMoney = cdata.get("crechargeMoney");
                if (rechargeMoney == null) {
                    cdata.put("crechargeMoney", item.getMoney());
                } else {
                    cdata.put("crechargeMoney", item.getMoney() + Long.parseLong(rechargeMoney.toString()));
                }
            }
        }
    }

    private Map<String, Object> initCdata(Map<String, Map<String, Object>> dataMap, String ymd, Map<String, Object> cdata) {
        if (cdata == null) {
            cdata = new HashMap<>();
            cdata.put("rechargePlayerCount", 0);
            cdata.put("brechargePlayerCount", 0);
            cdata.put("crechargePlayerCount", 0);
            cdata.put("rechargeCount", 0);
            cdata.put("brechargeCount", 0);
            cdata.put("crechargeCount", 0);

            cdata.put("withdrawalPlayerCount", 0);
            cdata.put("bwithdrawalPlayerCount", 0);
            cdata.put("cwithdrawalPlayerCount", 0);
            cdata.put("withdrawalCount", 0);
            cdata.put("bwithdrawalCount", 0);
            cdata.put("cwithdrawalCount", 0);
            cdata.put("ymd", ymd);
            dataMap.put(ymd, cdata);
        }
        return cdata;
    }

    private void rechargeStatis(ReportDataRequest reportDataRequest, Map<String, Map<String, Object>> dataMap) {
        int pageNo = 1;
        while (true) {
            SearchResponse<BallLoggerRecharge> searchReponse = loggerRechargeService.statisReport(reportDataRequest, pageNo++, 1000);
            List<BallLoggerRecharge> results = searchReponse.getResults();
            if (results == null || results.isEmpty()) {
                break;
            }
            for (BallLoggerRecharge item : results) {
                String ymd = TimeUtil.dateFormat(new Date(item.getCreatedAt()), TimeUtil.TIME_YYYY_MM_DD);
                Map<String, Object> cdata = dataMap.get(ymd);
                cdata = initCdata(dataMap, ymd, cdata);
                if (item.getType() == 2) {
                    //充值人数
                    Object rechargePlayer = cdata.get("rechargePlayer");
                    if (rechargePlayer == null) {
                        HashSet<Long> set = new HashSet<>();
                        set.add(item.getPlayerId());
                        cdata.put("rechargePlayer", set);
                    } else {
                        HashSet<Long> set = (HashSet<Long>) rechargePlayer;
                        set.add(item.getPlayerId());
                    }
                    //充值笔数
                    Object rechargeCount = cdata.get("rechargeCount");
                    if (rechargeCount == null) {
                        cdata.put("rechargeCount", 1);
                    } else {
                        cdata.put("rechargeCount", 1 + Integer.parseInt(rechargeCount.toString()));
                    }
                    //充值金额
                    Object rechargeMoney = cdata.get("rechargeMoney");
                    if (rechargeMoney == null) {
                        cdata.put("rechargeMoney", item.getMoney());
                    } else {
                        cdata.put("rechargeMoney", item.getMoney() + Long.parseLong(rechargeMoney.toString()));
                    }
                } else {
                    //充值人数
                    Object brechargePlayer = cdata.get("brechargePlayer");
                    if (brechargePlayer == null) {
                        HashSet<Long> set = new HashSet<>();
                        set.add(item.getPlayerId());
                        cdata.put("brechargePlayer", set);
                    } else {
                        HashSet<Long> set = (HashSet<Long>) brechargePlayer;
                        set.add(item.getPlayerId());
                    }
                    //充值笔数
                    Object brechargeCount = cdata.get("brechargeCount");
                    if (brechargeCount == null) {
                        cdata.put("brechargeCount", 1);
                    } else {
                        cdata.put("brechargeCount", 1 + Integer.parseInt(brechargeCount.toString()));
                    }
                    //充值金额
                    Object brechargeMoney = cdata.get("brechargeMoney");
                    if (brechargeMoney == null) {
                        cdata.put("brechargeMoney", item.getMoney());
                    } else {
                        cdata.put("brechargeMoney", item.getMoney() + Long.parseLong(brechargeMoney.toString()));
                    }
                }
            }
        }
    }

    @Override
    public BaseResponse reportProxy() {
        return null;
    }

    @Override
    public BaseResponse reportRechargeWay(ReportRewiRequest reportRewiRequest, Integer pageNo, Integer pageSize) {
        boolean s1 = false;
        boolean s2 = false;
        if (reportRewiRequest.getBehalfId() == null && reportRewiRequest.getPayId() == null) {
            s1 = true;
            s2 = true;
        }
        if (reportRewiRequest.getBehalfId() != null && reportRewiRequest.getPayId() != null) {
            s1 = true;
            s2 = true;
        }
        if (reportRewiRequest.getBehalfId() != null && reportRewiRequest.getPayId() == null) {
            s1 = false;
            s2 = true;
        }
        if (reportRewiRequest.getBehalfId() == null && reportRewiRequest.getPayId() != null) {
            s1 = true;
            s2 = false;
        }
        //统计充值报表
        List<RechargeWithdrawalResponse> ballLoggerRecharges = new ArrayList<>();
        if (s1) {
            ballLoggerRecharges = loggerRechargeService.rechargeStatisByPayTypeList(reportRewiRequest);
        }
        //统计提现报表
        List<RechargeWithdrawalResponse> ballLoggerWithdrawal = new ArrayList<>();
        if (s2) {
            ballLoggerWithdrawal = loggerWithdrawalService.rechargeStatisByPayTypeList(reportRewiRequest);
        }
        //拿 到2边小计
        RechargeWithdrawalResponse total1 = null;
        RechargeWithdrawalResponse total2 = null;
        if (!ballLoggerRecharges.isEmpty() && s1) {
            total1 = ballLoggerRecharges.remove(ballLoggerRecharges.size() - 1);
        }
        if (!ballLoggerWithdrawal.isEmpty() && s2) {
            total2 = ballLoggerWithdrawal.remove(ballLoggerWithdrawal.size() - 1);
        }
        if (total1 == null && total2 == null) {
            return BaseResponse.SUCCESS;
        }
        boolean useRecharge = true;
        if (ballLoggerRecharges.size() > ballLoggerWithdrawal.size()) {
            ballLoggerRecharges.addAll(ballLoggerWithdrawal);
        } else {
            useRecharge = false;
            ballLoggerWithdrawal.addAll(ballLoggerRecharges);
        }

        if (total1 != null) {
            if (total2 != null) {
//                total1.setWifailCount(total2.getWifailCount());
//                total1.setWifailMoney(total2.getWifailMoney());
//                total1.setWifailPlayer(total2.getWifailPlayer());
                total1.setWisuccCount(total2.getWisuccCount());
                total1.setWisuccMoney(total2.getWisuccMoney());
                total1.setWisuccPlayer(total2.getWisuccPlayer());
                if (useRecharge) {
                    ballLoggerRecharges.add(total1);
                } else {
                    ballLoggerWithdrawal.add(total1);
                }
            } else {
                if (useRecharge) {
                    ballLoggerRecharges.add(total1);
                } else {
                    ballLoggerWithdrawal.add(total1);
                }
            }
        } else {
            if (useRecharge) {
                ballLoggerRecharges.add(total2);
            } else {
                ballLoggerWithdrawal.add(total2);
            }
        }
        if (useRecharge) {
            Collections.sort(ballLoggerRecharges, new Comparator<RechargeWithdrawalResponse>() {
                @Override
                public int compare(RechargeWithdrawalResponse o1, RechargeWithdrawalResponse o2) {
                    return o1.getYmd().compareTo(o2.getYmd());
                }
            });
        } else {
            Collections.sort(ballLoggerWithdrawal, new Comparator<RechargeWithdrawalResponse>() {
                @Override
                public int compare(RechargeWithdrawalResponse o1, RechargeWithdrawalResponse o2) {
                    return o1.getYmd().compareTo(o2.getYmd());
                }
            });
        }
        if (useRecharge) {
            return BaseResponse.successWithData(ballLoggerRecharges);

        } else {
            return BaseResponse.successWithData(ballLoggerWithdrawal);
        }
    }

    @Override
    public BaseResponse reportRechargeWayAll(ReportRewiRequest reportRewiRequest) {
        boolean s1 = false;
        boolean s2 = false;
        if (reportRewiRequest.getBehalfId() == null && reportRewiRequest.getPayId() == null) {
            s1 = true;
            s2 = true;
        }
        if (reportRewiRequest.getBehalfId() != null && reportRewiRequest.getPayId() != null) {
            s1 = true;
            s2 = true;
        }
        if (reportRewiRequest.getBehalfId() != null && reportRewiRequest.getPayId() == null) {
            s1 = false;
            s2 = true;
        }
        if (reportRewiRequest.getBehalfId() == null && reportRewiRequest.getPayId() != null) {
            s1 = true;
            s2 = false;
        }
        //统计充值报表
        RechargeWithdrawalResponse ballLoggerRecharges = RechargeWithdrawalResponse.builder()
                .ymd("合计")
                .payName("")
                .resuccCount(0)
                .resuccMoney(0L)
                .resuccPlayer(new HashSet<>())
                .wisuccCount(0)
                .wisuccMoney(0L)
                .wisuccPlayer(new HashSet<>())
                .build();
        if (s1) {
            ballLoggerRecharges = loggerRechargeService.rechargeStatisByPayType(reportRewiRequest);
        }
        //统计提现报表
        RechargeWithdrawalResponse ballLoggerWithdrawal = RechargeWithdrawalResponse.builder()
                .ymd("合计")
                .payName("")
                .resuccCount(0)
                .resuccMoney(0L)
                .resuccPlayer(new HashSet<>())
                .wisuccCount(0)
                .wisuccMoney(0L)
                .wisuccPlayer(new HashSet<>())
                .build();
        if (s2) {
            ballLoggerWithdrawal = loggerWithdrawalService.rechargeStatisByPayType(reportRewiRequest);
        }
//        ballLoggerRecharges.setWifailCount(ballLoggerWithdrawal.getWifailCount());
//        ballLoggerRecharges.setWifailMoney(ballLoggerWithdrawal.getWifailMoney());
//        ballLoggerRecharges.setWifailPlayer(ballLoggerWithdrawal.getWifailPlayer());
        ballLoggerRecharges.setWisuccCount(ballLoggerWithdrawal.getWisuccCount());
        ballLoggerRecharges.setWisuccMoney(ballLoggerWithdrawal.getWisuccMoney());
        ballLoggerRecharges.setWisuccPlayer(ballLoggerWithdrawal.getWisuccPlayer());
        return BaseResponse.successWithData(ballLoggerRecharges);
    }

    @Override
    public BaseResponse standard(ReportStandardRequest reportStandardRequest) {
        if (StringUtils.isBlank(reportStandardRequest.getPlayerName())) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e40"));
        }
        if (reportStandardRequest.getDays() == null) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e41"));
        }
        if (reportStandardRequest.getBets() == null) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e42"));
        }
        if (reportStandardRequest.getRecharge() == null) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e43"));
        }
        BallPlayer playerProxy = basePlayerService.findByUsername(reportStandardRequest.getPlayerName());
        if (playerProxy == null || playerProxy.getProxyPlayer() == 0) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e44"));
        }
        long begin = 0L;
        long end = 0L;
        //如果不传时间，默认查本月
        if (StringUtils.isBlank(reportStandardRequest.getBegin())) {
            begin = TimeUtil.getBeginDayOfMonth().getTime();
        } else {
            try {
                begin = TimeUtil.stringToTimeStamp(reportStandardRequest.getBegin(), TimeUtil.TIME_YYYY_MM_DD);
            } catch (ParseException e) {
                begin = TimeUtil.getBeginDayOfMonth().getTime();
            }
        }
        if (StringUtils.isBlank(reportStandardRequest.getEnd())) {
            end = System.currentTimeMillis();
        } else {
            try {
                end = TimeUtil.stringToTimeStamp(reportStandardRequest.getEnd(), TimeUtil.TIME_YYYY_MM_DD) + TimeUtil.TIME_ONE_DAY;
            } catch (ParseException e) {
                end = System.currentTimeMillis();
            }
        }
        reportStandardRequest.setBegins(begin);
        reportStandardRequest.setEnds(end);
        //查询下3级所有账号
        List<BallPlayer> subThree = ballPlayerService.findSubThree(reportStandardRequest, playerProxy);
        if (subThree == null || subThree.isEmpty() || subThree.get(0) == null) {
            return BaseResponse.successWithData(new ArrayList<>());
        }
        //所有账号数据map
        Map<Long, ReportStandardPlayerDTO> playerMap = new HashMap<>();
        //展示账号数据map
        Map<Long, ReportStandardDTO> threeMap = new HashMap<>();
        ReportStandardDTO total = ReportStandardDTO.builder()
                .playerName("总计")
                .aimCount(0)
                .aimBet(0)
                .aimRecharge(0)
                .groupCount(0)
                .build();
        Set<Long> groupSet = new HashSet<>();
        Set<Long> aimSet = new HashSet<>();
        Set<Long> rechargeSet = new HashSet<>();
        Set<Long> betSet = new HashSet<>();

        for (BallPlayer item : subThree) {
            playerMap.put(item.getId(), ReportStandardPlayerDTO.builder()
                    .daySet(new HashSet<>())
                    .bets(0L)
                    .recharge(0L)
                    .playerId(item.getId())
                    .playerName(item.getUsername())
                    .superTree(item.getSuperTree())
                    .build());
            if (item.getVipRank() - playerProxy.getVipRank() < 4) {
                threeMap.put(item.getId(), ReportStandardDTO.builder()
                        .aimCount(0)
                        .aimBet(0)
                        .aimRecharge(0)
                        .groupCount(item.getGroupSize())
                        .playerId(item.getId())
                        .playerName(item.getUsername())
                        .build());
            }
        }
        final CountDownLatch countDownLatch = new CountDownLatch(2);
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        ThreadPoolUtil.execSaki(() -> {
            //统计下级充值,线上线下
            try {
                loggerRechargeService.searchStandard(reportStandardRequest, playerProxy, playerMap, systemConfig);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                countDownLatch.countDown();
            }

        });
        ThreadPoolUtil.execSaki(() -> {
            //统计下级投注
            try {
                ballBetService.searchStandard(reportStandardRequest, playerProxy, playerMap);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                countDownLatch.countDown();
            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
        }
        //汇总每层账号达标数据
        for (ReportStandardPlayerDTO item : playerMap.values()) {
            String[] split = item.getSuperTree().split("_");
            for (String itema : split) {
                if (StringUtils.isBlank(itema)) {
                    continue;
                }
                ReportStandardDTO reportStandardDTO = threeMap.get(Long.parseLong(itema));
                if (reportStandardDTO != null) {
                    boolean aim = true;
                    //投注天数和投注金额达标
                    if (item.getDaySet().size() >= reportStandardRequest.getDays()
                            && item.getBets() >= reportStandardRequest.getBets() * BigDecimalUtil.PLAYER_MONEY_UNIT) {
                        reportStandardDTO.setAimBet(reportStandardDTO.getAimBet() + 1);
                        betSet.add(reportStandardDTO.getPlayerId());
                    } else {
                        aim = false;
                    }
                    //充值金额达标
                    if (item.getRecharge() >= reportStandardRequest.getRecharge() * BigDecimalUtil.PLAYER_MONEY_UNIT) {
                        reportStandardDTO.setAimRecharge(reportStandardDTO.getAimRecharge() + 1);
                        rechargeSet.add(reportStandardDTO.getPlayerId());
                    } else {
                        aim = false;
                    }
                    if (aim) {
                        reportStandardDTO.setAimCount(reportStandardDTO.getAimCount() + 1);
                        aimSet.add(reportStandardDTO.getPlayerId());
                    }
                }
            }
            groupSet.add(item.getPlayerId());
        }
        List<ReportStandardDTO> backList = new ArrayList<>(threeMap.values());
        Collections.sort(backList, new Comparator<ReportStandardDTO>() {
            @Override
            public int compare(ReportStandardDTO o1, ReportStandardDTO o2) {
                return o2.getAimCount() - o1.getAimCount();
            }
        });
        Map<String, Object> data = new HashMap<>();
        total.setGroupCount(groupSet.size());
        total.setAimCount(aimSet.size());
        total.setAimRecharge(rechargeSet.size());
        total.setAimBet(betSet.size());
        data.put("total", total);
        data.put("list", backList);
        return BaseResponse.successWithData(data);
    }

    @Override
    public SearchResponse<ReportStandardDTO> standard2(ReportStandardRequest reportStandardRequest, Integer pageNo, Integer pageSize) {
        SearchResponse<BallPlayer> search = ballPlayerService.searchStandard(BallPlayer.builder()
                .treeType(reportStandardRequest.getTreeType())
                .username(reportStandardRequest.getPlayerName())
                .build(), pageNo, pageSize);
        long begin = 0L;
        long end = 0L;
        switch (reportStandardRequest.getTime()) {
            case 0:
                //近七天
                begin = TimeUtil.getDayBegin().getTime() - 7 * TimeUtil.TIME_ONE_DAY;
                end = System.currentTimeMillis();
                break;
            case 1:
                //近15天
                begin = TimeUtil.getDayBegin().getTime() - 15 * TimeUtil.TIME_ONE_DAY;
                end = System.currentTimeMillis();
                break;
            case 2:
                //近30天
                begin = TimeUtil.getDayBegin().getTime() - 30 * TimeUtil.TIME_ONE_DAY;
                end = System.currentTimeMillis();
                break;
            case 3:
                //本周
                begin = TimeUtil.getBeginDayOfWeek().getTime();
                end = System.currentTimeMillis();
                break;
            case 4:
                //上周
                begin = TimeUtil.getBeginDayOfLastWeek().getTime();
                end = TimeUtil.getEndDayOfLastWeek().getTime();
                break;
            case 5:
                //本月
                begin = TimeUtil.getBeginDayOfMonth().getTime();
                end = TimeUtil.getEndDayOfMonth().getTime();
                break;
            case 6:
                //上月
                begin = TimeUtil.getBeginDayOfLastMonth().getTime();
                end = TimeUtil.getEndDayOfLastMonth().getTime();
                break;
            case 7:
                try {
                    begin = TimeUtil.stringToTimeStamp(reportStandardRequest.getBegin(), TimeUtil.TIME_YYYY_MM_DD);
                    if (StringUtils.isBlank(reportStandardRequest.getEnd())) {
                        end = System.currentTimeMillis();
                    } else {
                        end = TimeUtil.stringToTimeStamp(reportStandardRequest.getEnd(), TimeUtil.TIME_YYYY_MM_DD) + TimeUtil.TIME_ONE_DAY;
                    }
                } catch (ParseException e) {
                    return new SearchResponse<>();
                }
                break;
        }
        reportStandardRequest.setBegins(begin);
        reportStandardRequest.setEnds(end);
        List<ReportStandardDTO> backList = new ArrayList<>();
        for (BallPlayer item : search.getResults()) {
            reportStandardRequest.setRegx("^" + (item.getSuperTree() + item.getId()).replace("_", "\\_") + "\\_([0-9]+\\_){0,2}$");
            int standardAim = playerBetService.standard(reportStandardRequest);
            int standardBet = playerBetService.standard2(reportStandardRequest);
            int standardRe = ballPlayerService.standard(reportStandardRequest);
            int groupSize = ballPlayerService.standardGrouop(reportStandardRequest);
            ReportStandardDTO build = ReportStandardDTO.builder()
                    .playerName(item.getUsername())
                    .groupCount(groupSize)
                    .aimBet(standardBet)
                    .aimCount(standardAim)
                    .aimRecharge(standardRe)
                    .parentUser(item.getSuperiorName())
                    .topUser("")
                    .build();
            String superTree = item.getSuperTree();
            if (superTree.equals("0")) {
            } else if (superTree.equals("_")) {
            } else {
                String[] split = superTree.split("_");
                BallPlayer superPlayer = basePlayerService.findById(Long.parseLong(split[1]));
                build.setTopUser(superPlayer.getUsername());
            }
            backList.add(build);
        }
        SearchResponse<ReportStandardDTO> searchResponse = new SearchResponse<>();
        searchResponse.setResults(backList);
        searchResponse.setPageNo(search.getPageNo());
        searchResponse.setPageSize(search.getPageSize());
        searchResponse.setTotalPage(search.getTotalPage());
        searchResponse.setTotalCount(search.getTotalCount());
        return searchResponse;
    }

    @Override
    public SearchResponse<ReportStandardDTO> standard3(ReportStandardRequest reportStandardRequest, Integer pageNo, Integer pageSize) {
//        1.累计充值达到N
//        2.指定时间内，下注达到N天，累计下注达到N
//        3.账号只查下3级
        SearchResponse<BallPlayer> search = ballPlayerService.searchStandard(BallPlayer.builder()
                .treeType(reportStandardRequest.getTreeType())
                .username(reportStandardRequest.getPlayerName())
                .build(), pageNo, pageSize);
        long begin = 0L;
        long end = 0L;
        switch (reportStandardRequest.getTime()) {
            case 0:
                //近七天
                begin = TimeUtil.getDayBegin().getTime() - 7 * TimeUtil.TIME_ONE_DAY;
                end = System.currentTimeMillis();
                break;
            case 1:
                //近15天
                begin = TimeUtil.getDayBegin().getTime() - 15 * TimeUtil.TIME_ONE_DAY;
                end = System.currentTimeMillis();
                break;
            case 2:
                //近30天
                begin = TimeUtil.getDayBegin().getTime() - 30 * TimeUtil.TIME_ONE_DAY;
                end = System.currentTimeMillis();
                break;
            case 3:
                //本周
                begin = TimeUtil.getBeginDayOfWeek().getTime();
                end = System.currentTimeMillis();
                break;
            case 4:
                //上周
                begin = TimeUtil.getBeginDayOfLastWeek().getTime();
                end = TimeUtil.getEndDayOfLastWeek().getTime();
                break;
            case 5:
                //本月
                begin = TimeUtil.getBeginDayOfMonth().getTime();
                end = TimeUtil.getEndDayOfMonth().getTime();
                break;
            case 6:
                //上月
                begin = TimeUtil.getBeginDayOfLastMonth().getTime();
                end = TimeUtil.getEndDayOfLastMonth().getTime();
                break;
            case 7:
                try {
                    begin = TimeUtil.stringToTimeStamp(reportStandardRequest.getBegin(), TimeUtil.TIME_YYYY_MM_DD);
                    if (StringUtils.isBlank(reportStandardRequest.getEnd())) {
                        end = System.currentTimeMillis();
                    } else {
                        end = TimeUtil.stringToTimeStamp(reportStandardRequest.getEnd(), TimeUtil.TIME_YYYY_MM_DD) + TimeUtil.TIME_ONE_DAY;
                    }
                } catch (ParseException e) {
                    return new SearchResponse<>();
                }
                break;
        }
        reportStandardRequest.setBegins(begin);
        reportStandardRequest.setEnds(end);
        List<ReportStandardDTO> backList = new ArrayList<>();
        for (BallPlayer item : search.getResults()) {
            //
            reportStandardRequest.setRegx("^" + (item.getSuperTree() + item.getId()).replace("_", "\\_") + "\\_([0-9]+\\_){0,2}$");
            //充值达标
            int standardRe = ballPlayerService.standard(reportStandardRequest);
            int groupSize = ballPlayerService.standardGrouop(reportStandardRequest);
            List<BallPlayer> subThree = ballPlayerService.findSubThree(reportStandardRequest, item);
            Map<Long,BallPlayer> playerMap = new HashMap<>();
            for(BallPlayer player:subThree){
                playerMap.put(player.getId(),player);
            }
            Map<String, Integer> statisMap = standardAimBet(reportStandardRequest,playerMap);
            ReportStandardDTO build = ReportStandardDTO.builder()
                    .playerName(item.getUsername())
                    .groupCount(groupSize)
                    .aimBet(statisMap.get("aimBet"))
                    .aimCount(statisMap.get("aimCount"))
                    .aimRecharge(standardRe)
                    .parentUser(item.getSuperiorName())
                    .topUser("")
                    .build();
            String superTree = item.getSuperTree();
            if (superTree.equals("0")) {
            } else if (superTree.equals("_")) {
            } else {
                String[] split = superTree.split("_");
                BallPlayer superPlayer = basePlayerService.findById(Long.parseLong(split[1]));
                build.setTopUser(superPlayer.getUsername());
            }
            backList.add(build);
        }
        SearchResponse<ReportStandardDTO> searchResponse = new SearchResponse<>();
        searchResponse.setResults(backList);
        searchResponse.setPageNo(search.getPageNo());
        searchResponse.setPageSize(search.getPageSize());
        searchResponse.setTotalPage(search.getTotalPage());
        searchResponse.setTotalCount(search.getTotalCount());
        return searchResponse;
    }

    /**
     * @param reportStandardRequest
     * @param playerMap
     * @return
     */
    private Map<String, Integer> standardAimBet(ReportStandardRequest reportStandardRequest, Map<Long, BallPlayer> playerMap) {
        int pageNo = 1;
        int aimBet = 0;
        int aimCount = 0;
        Map<Long, ReportStandardPlayerDTO> dataMap = new HashMap<>();
        while (true) {
            SearchResponse<BallBet> searchResponse = ballBetService.searchStandard(reportStandardRequest, pageNo++);
            List<BallBet> results = searchResponse.getResults();
            if (results == null || results.isEmpty() || results.get(0) == null) {
                break;
            }
            for (BallBet item : results) {
                ReportStandardPlayerDTO mdata = dataMap.get(item.getPlayerId());
                if (mdata == null) {
                    mdata = ReportStandardPlayerDTO.builder()
                            .daySet(new HashSet<>())
                            .bets(0L)
                            .recharge(0L)
                            .playerId(item.getPlayerId())
                            .playerName(item.getUsername())
                            .build();
                    dataMap.put(item.getPlayerId(), mdata);
                }
                mdata.getDaySet().add(TimeUtil.longToStringYmd(item.getCreatedAt()));
                mdata.setBets(mdata.getBets() + item.getBetMoney());
            }
        }
        for (ReportStandardPlayerDTO item : dataMap.values()) {
            boolean betOk = item.getBets() >= reportStandardRequest.getBets() && item.getDaySet().size() >= reportStandardRequest.getDays();
            if (betOk) {
                aimBet++;
            }
            BallPlayer player = playerMap.get(item.getPlayerId());
            if (betOk && player.getCumulativeTopUp() >= reportStandardRequest.getRecharge()) {
                aimCount++;
            }
        }
        Map<String, Integer> backData = new HashMap<>();
        backData.put("aimBet", aimBet);
        backData.put("aimCount", aimCount);
        return backData;
    }
}
