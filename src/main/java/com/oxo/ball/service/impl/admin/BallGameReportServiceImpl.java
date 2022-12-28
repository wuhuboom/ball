package com.oxo.ball.service.impl.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxo.ball.bean.dao.*;
import com.oxo.ball.bean.dto.req.report.ReportDataRequest;
import com.oxo.ball.bean.dto.req.report.ReportGameRequest;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.mapper.BallGameMapper;
import com.oxo.ball.mapper.BallGameReportMapper;
import com.oxo.ball.service.admin.*;
import com.oxo.ball.service.player.IPlayerGameService;
import com.oxo.ball.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.util.*;

/**
 * <p>
 * 游戏赛事报表 服务实现类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Service
public class BallGameReportServiceImpl extends ServiceImpl<BallGameReportMapper, BallGameReport> implements IBallGameReportService {

    @Autowired
    IBallGameReportService gameReportService;
    @Autowired
    private IBallGameService gameService;
    @Autowired
    private IBallBetService ballBetService;

    @Override
    public SearchResponse<BallGameReport> search(BallGameReport queryParam, Integer pageNo, Integer pageSize) {
        SearchResponse<BallGameReport> response = new SearchResponse<>();
        Page<BallGameReport> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallGameReport> query = new QueryWrapper<>();
        if (queryParam.getTimeType() != null) {
            switch (queryParam.getTimeType()) {
                case 0:
                    query.ge("ymd_stamp", TimeUtil.getDayBegin().getTime()-2*TimeUtil.TIME_ONE_DAY);
                    query.le("ymd_stamp", TimeUtil.getDayEnd().getTime()-2*TimeUtil.TIME_ONE_DAY);
                    break;
                case 1:
                    query.ge("ymd_stamp", TimeUtil.getBeginDayOfYesterday().getTime());
                    query.le("ymd_stamp", TimeUtil.getEndDayOfYesterday().getTime());
                    break;
                case 2:
                    query.ge("ymd_stamp", TimeUtil.getDayBegin().getTime());
                    query.le("ymd_stamp", TimeUtil.getDayEnd().getTime());
                    break;
                case 3:
                    query.ge("ymd_stamp", TimeUtil.getBeginDayOfTomorrow().getTime());
                    query.le("ymd_stamp", TimeUtil.getEndDayOfTomorrow().getTime());
                    break;
                case 4:
                    query.ge("ymd_stamp", TimeUtil.getBeginDayOfTomorrow().getTime()+TimeUtil.TIME_ONE_DAY);
                    query.le("ymd_stamp", TimeUtil.getEndDayOfTomorrow().getTime()+TimeUtil.TIME_ONE_DAY);
                    break;
                case 5:
                    query.ge("ymd_stamp", TimeUtil.getDayBegin().getTime());
                    query.le("ymd_stamp", TimeUtil.getDayEnd().getTime()+3*TimeUtil.TIME_ONE_DAY);
                    break;
                case 6:
                    if(!StringUtils.isBlank(queryParam.getBegin())){
                        try {
                            long timeStamp = TimeUtil.stringToTimeStamp(queryParam.getBegin(), TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS);
                            query.ge("ymd_stamp", timeStamp);
                        } catch (ParseException e) {
                            try {
                                long timeStamp = TimeUtil.stringToTimeStamp(queryParam.getBegin(), TimeUtil.TIME_YYYY_MM_DD);
                                query.ge("ymd_stamp", timeStamp);
                            } catch (ParseException e1) {
                            }
                        }
                    }
                    if(!StringUtils.isBlank(queryParam.getEnd())){
                        try {
                            long timeStamp = TimeUtil.stringToTimeStamp(queryParam.getEnd(), TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS);
                            query.le("ymd_stamp", timeStamp);
                        } catch (ParseException e) {
                            try {
                                long timeStamp = TimeUtil.stringToTimeStamp(queryParam.getEnd(), TimeUtil.TIME_YYYY_MM_DD);
                                query.le("ymd_stamp", timeStamp+TimeUtil.TIME_ONE_DAY);
                            } catch (ParseException e1) {
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        query.orderByAsc("ymd_stamp");
        IPage<BallGameReport> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public Boolean insert(BallGameReport ballGameReport) {
        return save(ballGameReport);
    }

    @Override
    public void dayStatis() {
//        Map<String, Map<String, Object>> dataMap = new HashMap<>();
        //查询一共多少场次,未开赛
        List<BallGame> ballGames = gameService.statisReport(new ReportDataRequest());
        BallGameReport save = BallGameReport.builder()
                .ymd(TimeUtil.dateFormat(new Date(TimeUtil.getNowTimeMill()-TimeUtil.TIME_ONE_DAY), TimeUtil.TIME_YYYY_MM_DD))
                .build();
        try {
            save.setYmdStamp(TimeUtil.stringToTimeStamp(save.getYmd(),TimeUtil.TIME_YYYY_MM_DD));
        } catch (ParseException e) {
        }
        if(ballGames==null||ballGames.isEmpty()||ballGames.get(0)==null){
            insert(save);
            return;
        }
        for (BallGame item : ballGames) {
//            Map<String, Object> map = new HashMap<>();
            save.setGameCount(item.getId().intValue());
            save.setNotStart(item.getGameStatus());
//            map.put("gameCount", item.getId());
//            map.put("notStart", item.getGameStatus());
//            map.put("betCounts", 0);
//            map.put("bbetCounts", 0);
        }
        int pageNo = 1;
        Map<String,Object> data = new HashMap<>();
        while (true) {
            ReportDataRequest reportDataRequest = new ReportDataRequest();
            reportDataRequest.setTime(1);
            SearchResponse<BallBet> search = ballBetService.search(reportDataRequest, pageNo++, 1000);
            if (search.getResults() == null || search.getResults().isEmpty()) {
                break;
            }
            for (BallBet bet : search.getResults()) {
//                String ymd = TimeUtil.dateFormat(new Date(bet.getCreatedAt()), TimeUtil.TIME_YYYY_MM_DD);
//                Map<String, Object> data = dataMap.get(ymd);
                //不统计未结算,和非确认状态的
                if (bet.getStatusSettlement() == 0 || bet.getStatus()!=1) {
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
        //下注场次
        Object betGame = data.get("betGame");
        if (betGame != null) {
            Set<Long> set = (Set<Long>) betGame;
//            mitem.put("betGameCount", set.size());
            save.setBetGameCount(set.size());
        }
        //人均盈亏 = 盈亏/下注人数
        Object betWinLose = data.get("betWinLose");
        Object betPlayer = data.get("betPlayer");
        int betPlayerCount = 0;
        Long bwl = 0L;
        if (betPlayer != null) {
            Set<Long> set = (Set<Long>) betPlayer;
            betPlayerCount = set.size();
        }
        if (betWinLose != null) {
            bwl = (Long) betWinLose;
        }
        Double div = betPlayerCount == 0 ? 0 : BigDecimalUtil.div(bwl, betPlayerCount);
//        mitem.put("betPlayerCount", betPlayerCount);
        save.setBetPlayerCount(betPlayerCount);
//        mitem.put("betWinLosePerPlayer", div);
        save.setBetWinLosePerPlayer(String.valueOf(div.longValue()));
        Object betBingo = data.get("betBingo");
        Object betBalance = data.get("betBalance");
        Object betBingoBalance = data.get("betBingoBalance");
        int betBingoCount = 0;
        long betBingoBalanceLong = 0;
        long betBalanceLong = 0;
        if (betBingo != null) {
            Set<Long> set = (Set<Long>) betBingo;
            betBingoCount = set.size();
        }
        if(betBingoBalance!=null){
            betBingoBalanceLong = (long) betBingoBalance;
        }
        if(betBalance!=null){
            betBalanceLong = (long) betBalance;
        }
        //盈亏率 = 100- (中奖/投注*100)
        double div1 = betBalanceLong == 0 ? 0 : BigDecimalUtil.div(betBingoBalanceLong, betBalanceLong);
        double dnum = betBalanceLong==0?0:100-(BigDecimalUtil.mul(div1,100));
        save.setBetWinLosePer(BigDecimalUtil.format2(dnum));
        save.setBetBingoCount(betBingoCount);
        //保本=====================================================================
        Object bbetWinLose = data.get("bbetWinLose");
        Object bbetPlayer = data.get("bbetPlayer");
        int bbetPlayerCount = 0;
        Long bbwl = 0L;
        if (bbetPlayer != null) {
            Set<Long> set = (Set<Long>) bbetPlayer;
            bbetPlayerCount = set.size();
        }
        if (bbetWinLose != null) {
            bbwl = (Long) bbetWinLose;
        }
        Double bdiv = bbetPlayerCount == 0 ? 0 : BigDecimalUtil.div(bbwl, bbetPlayerCount);
//        mitem.put("bbetWinLosePerPlayer", bdiv);
        save.setBbetWinLosePerPlayer(String.valueOf(bdiv.longValue()));
//        mitem.put("bbetPlayerCount", bbetPlayerCount);
        save.setBbetPlayerCount(bbetPlayerCount);
        //盈亏率 = 中奖人数/下注人数
        Object bbetBingo = data.get("bbetBingo");
        Object bbetBalance = data.get("bbetBalance");
        Object bbetBingoBalance = data.get("bbetBingoBalance");
        int bbetBingoCount = 0;
        long bbetBingoBalanceLong = 0;
        long bbetBalanceLong = 0;
        if (bbetBingo != null) {
            Set<Long> set = (Set<Long>) bbetBingo;
            bbetBingoCount = set.size();
        }
        if(bbetBingoBalance!=null){
            bbetBingoBalanceLong = (long) bbetBingoBalance;
        }
        if(betBalance!=null){
            bbetBalanceLong = (long) bbetBalance;
        }
        double bdiv1 = bbetBalanceLong == 0 ? 0 : BigDecimalUtil.div(bbetBingoBalanceLong, bbetBalanceLong);
//        mitem.put("bbetWinLosePer", bdiv1);
        double bdnum = bbetBalanceLong==0?0:100-BigDecimalUtil.mul(bdiv1,100);
        save.setBbetWinLosePer(BigDecimalUtil.format2(bdnum));
//        mitem.put("bbetBingoCount", bbetBingoCount);
        save.setBbetBingoCount(bbetBingoCount);
        //
        save.setBetCounts(data.get("betCounts")==null?0:Integer.parseInt(data.get("betCounts").toString()));
        save.setBbetCounts(data.get("bbetCounts")==null?0:Integer.parseInt(data.get("bbetCounts").toString()));
        save.setBetBalance(data.get("betBalance")==null?0:Long.parseLong(data.get("betBalance").toString()));
        save.setBbetBalance(data.get("bbetBalance")==null?0:Long.parseLong(data.get("bbetBalance").toString()));
        save.setBetBingoBalance(data.get("betBingoBalance")==null?0:Long.parseLong(data.get("betBingoBalance").toString()));
        save.setBbetBingoBalance(data.get("bbetBingoBalance")==null?0:Long.parseLong(data.get("bbetBingoBalance").toString()));
        save.setBetNotBingoBalance(data.get("betNotBingoBalance")==null?0:Long.parseLong(data.get("betNotBingoBalance").toString()));
        save.setBbetNotBingoBalance(data.get("bbetNotBingoBalance")==null?0:Long.parseLong(data.get("bbetNotBingoBalance").toString()));
        save.setBetHandMoney(data.get("betHandMoney")==null?0:Long.parseLong(data.get("betHandMoney").toString()));
        save.setBbetHandMoney(data.get("bbetHandMoney")==null?0:Long.parseLong(data.get("bbetHandMoney").toString()));
        save.setBetWinLose(data.get("betWinLose")==null?0:Long.parseLong(data.get("betWinLose").toString()));
        save.setBbetWinLose(data.get("bbetWinLose")==null?0:Long.parseLong(data.get("bbetWinLose").toString()));
        insert(save);
    }
}
