package com.oxo.ball.service.impl.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxo.ball.bean.dao.*;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.bean.dto.resp.admin.ProxyStatis2Dto;
import com.oxo.ball.bean.dto.resp.admin.ProxyStatis3Dto;
import com.oxo.ball.bean.dto.resp.admin.ProxyStatisDto;
import com.oxo.ball.mapper.BallProxyLoggerMapper;
import com.oxo.ball.service.admin.*;
import com.oxo.ball.service.impl.BasePlayerService;
import com.oxo.ball.utils.ResponseMessageUtil;
import com.oxo.ball.utils.ThreadPoolUtil;
import com.oxo.ball.utils.TimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

@Service
public class BallProxyLoggerServiceImpl extends ServiceImpl<BallProxyLoggerMapper, BallProxyLogger> implements IBallProxyLoggerService {
    @Autowired
    IBallPlayerService ballPlayerService;
    @Autowired
    IBallBalanceChangeService ballBalanceChangeService;
    @Autowired
    IBallLoggerRechargeService loggerRechargeService;
    @Autowired
    IBallLoggerHandsupService loggerHandsupService;
    @Autowired
    IBallLoggerWithdrawalService loggerWithdrawalService;
    @Autowired
    IBallBetService ballBetService;
    @Autowired
    BasePlayerService basePlayerService;
    @Autowired
    IBallSystemConfigService systemConfigService;

    @Override
    public SearchResponse<BallProxyLogger> search(BallProxyLogger queryParam, Integer pageNo, Integer pageSize) {
        SearchResponse<BallProxyLogger> response = new SearchResponse<>();

        Page<BallProxyLogger> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallProxyLogger> query = new QueryWrapper<>();
        if(!StringUtils.isBlank(queryParam.getPlayerName())){
            query.eq("player_name",queryParam.getPlayerName());
        }
        if(!StringUtils.isBlank(queryParam.getBegin())){
            try {
                long timeStamp = TimeUtil.stringToTimeStamp(queryParam.getBegin(), TimeUtil.TIME_YYYY_MM_DD);
                query.ge("ymd_stamp",timeStamp);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if(!StringUtils.isBlank(queryParam.getEnd())){
            try {
                long timeStamp = TimeUtil.stringToTimeStamp(queryParam.getEnd(), TimeUtil.TIME_YYYY_MM_DD);
                query.le("ymd_stamp",timeStamp+TimeUtil.TIME_ONE_DAY);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        IPage<BallProxyLogger> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public BallProxyLogger insert(BallProxyLogger proxyLogger) {
        save(proxyLogger);
        return proxyLogger;
    }

    @Override
    public void statisEveryDay() {
        Long begin = TimeUtil.getBeginDayOfYesterday().getTime();
        Long end = TimeUtil.getEndDayOfYesterday().getTime();
        String ymd = TimeUtil.dateFormat(new Date(begin), TimeUtil.TIME_YYYY_MM_DD);

        //每日统计顶级代理和一级代理的情况
        //1.查询所有顶级代理
        //2.查询顶级代理的直属下级代理
        int pageNo = 1;
        while (true) {
            SearchResponse<BallPlayer> search = ballPlayerService.search(BallPlayer.builder()
                    .superiorId(0L)
                    .accountType(2)
                    .build(), pageNo++, 20);
            List<BallPlayer> results = search.getResults();
            for (BallPlayer item : results) {
                BallProxyLogger insert = BallProxyLogger.builder()
                        .playerId(item.getId())
                        .playerName(item.getUsername())
                        .playerType(item.getAccountType())
                        .ymd(ymd)
                        .ymdStamp(begin)
                        .level(item.getVipRank())
                        .subCount(item.getDirectlySubordinateNum())
                        .subAllCount(item.getGroupSize())
                        .inOut(0L)
                        .inOutAll(0L)
                        .betCount(0)
                        .betCountPlayer(0)
                        .withdrawalCountHands(0L)
                        .withdrawalCountOffline(0L)
                        .withdrawalCountOnline(0L)
                        .payCountHands(0L)
                        .payCountOffline(0L)
                        .payCountOnline(0L)
                        .withdrawalCount(0)
                        .firstPayCount(0)
                        .payCount(0)
                        .rebateCount(0L)
                        .registCount(0)
                        .build();
                //新增注册查询
                insert.setRegistCount(ballPlayerService.statisTotalRegist(item.getId(), begin, end));
                //下级返利
                insert.setRebateCount(ballBalanceChangeService.statisSubRebate(item.getId(), begin, end));
                //充值人数
                insert.setPayCount(ballBalanceChangeService.statisPayCount(item.getId(), begin, end));
                // 首充人数
                insert.setFirstPayCount(ballPlayerService.statisFirstPayCount(item.getId(), begin, end));
                //线上下充值
                List<BallLoggerRecharge> ballLoggerRecharges = loggerRechargeService.statisPayCount(item.getId(), begin, end);
                for (BallLoggerRecharge sitem : ballLoggerRecharges) {
                    //线上
                    if (sitem.getType() == 1) {
                        insert.setPayCountOnline(sitem.getMoneySys());
                    } else {
                        insert.setPayCountOffline(sitem.getMoneySys());
                    }
                }
                //人工充值/提现
                List<BallLoggerHandsup> ballLoggerHandsups = loggerHandsupService.statisPayCount(item.getId(), begin, end);
                for (BallLoggerHandsup sitem : ballLoggerHandsups) {
                    if (sitem.getType() == 0) {
                        insert.setWithdrawalCountHands(sitem.getMoney());
                    } else {
                        insert.setPayCountHands(sitem.getMoney());
                    }
                }
                //线上下提现
                List<BallLoggerWithdrawal> ballLoggerWithdrawals = loggerWithdrawalService.statisPayCount(item.getId(), begin, end);
                for (BallLoggerWithdrawal sitem : ballLoggerWithdrawals) {
                    //线上=USDT
                    if (sitem.getType() == 2) {
                        insert.setWithdrawalCountOnline(sitem.getMoney());
                    } else {
                        //线下=银行卡
                        insert.setWithdrawalCountOffline(sitem.getMoney());
                    }
                }
                //下注人数,下注次数
                BallBet bet = ballBetService.statisBetCount(item.getId(), begin, end);
                insert.setBetCountPlayer(bet.getPlayerId().intValue());
                insert.setBetCount(bet.getId().intValue());
                //充提差,不包人工
                insert.setInOut(insert.getPayCountOnline() + insert.getPayCountOffline() - insert.getWithdrawalCountOnline() - insert.getWithdrawalCountOffline());
                //包人工
                insert.setInOutAll(insert.getPayCountOnline() + insert.getPayCountOffline() + insert.getPayCountHands() - insert.getWithdrawalCountOnline() - insert.getWithdrawalCountOffline() - insert.getWithdrawalCountHands());
                insert(insert);
            }
            if (results == null || results.isEmpty()) {
                break;
            }
        }

    }

    @Override
    public BaseResponse statis(BallProxyLogger queryParam) {
        Map<String,List<ProxyStatisDto>> responseMap = new HashMap<>();
        responseMap.put("proxy",new ArrayList<>());
        responseMap.put("proxySub",new ArrayList<>());
        if(StringUtils.isBlank(queryParam.getPlayerName())){
            return BaseResponse.successWithData(new ArrayList<>());
        }
        BallPlayer proxyUser = basePlayerService.findByUsername(queryParam.getPlayerName());
        if(proxyUser==null){
            return BaseResponse.successWithData(new ArrayList<>());
        }
        //        用户名，下级层级（1查直属，类推），时间段，包含自身 0否 1是
        //        1>用户ID，用户名，直属下级，总下级数，同IP人数，
        // 下级返利，充值人数，充值金额，首充人数，提现人数，提现金额，
        // 下注人数，下注次数，充提差，总余额
        //        2>用户ID,用户名,层级,充值人数,充值金额,首充人数,
        // 提现人数,提现金额,下注次数,充提差,总余额
        //如果查询层级为2，那么 vipRank<4,=level+2
        List<BallPlayer> ballPlayers = ballPlayerService.searchProxy(queryParam,proxyUser);
        if(ballPlayers.isEmpty()){
            return BaseResponse.successWithData(new ArrayList<>());
        }
        ProxyStatisDto statisDto1 = ProxyStatisDto.builder()
                .userId(proxyUser.getUserId())
                .username(proxyUser.getUsername())
                .subCount(0)
                .subCountAll(0)
                .subRebate(0L)
                .level(0)
                .totalMoney(0L)
                .rechargeFirst(0)
                .rechargePlayerSet(new HashSet<>())
                .rechargeMoney(0L)
                .withdrawalPlayerSet(new HashSet<>())
                .withdrawalMoney(0L)
                .betCount(0)
                .betPlayerSet(new HashSet<>())
                .sameIp(0)
                .build();
        ArrayList<ProxyStatisDto> list1 = new ArrayList<>();
        list1.add(statisDto1);
        responseMap.put("proxy",list1);

        Map<Long,ProxyStatisDto> list2Map = new HashMap<>();
        List<Long> ids = new ArrayList<>();
        //同IP判定：将IP分解到set中，如果存在则+1
        Set<String> ipSet = new HashSet<>();
        for(BallPlayer item:ballPlayers){
            ids.add(item.getId());
            //list1首充数
            checkFirstRecharge(queryParam, statisDto1, item);
            //总余额
            statisDto1.setTotalMoney(statisDto1.getTotalMoney()+item.getBalance());
            //同ip
            isSameIp(item, statisDto1, ipSet);
            //只统计指定层数,查层级2，如果本号为1，那么查2，3也就是 item=3+2 > 1 3-2<=1
            if(queryParam.getLevel()==null||item.getVipRank()-proxyUser.getVipRank()<=queryParam.getLevel()){
                //总下级
                statisDto1.setSubCountAll(statisDto1.getSubCountAll()+1);
                //直属下级=
                if(item.getVipRank()-proxyUser.getVipRank()==1){
                    statisDto1.setSubCount(statisDto1.getSubCount()+1);
                }
                list2Map.put(item.getId(),ProxyStatisDto.builder()
                        .userId(item.getUserId())
                        .username(item.getUsername())
                        .subCount(0)
                        .subCountAll(0)
                        .subRebate(0L)
                        .level(item.getVipRank()-proxyUser.getVipRank())
                        .vipRank(item.getVipRank())
                        .totalMoney(item.getBalance())
                        .rechargeFirst(0)
                        .rechargeMoney(0L)
                        .withdrawalPlayerSet(new HashSet<>())
                        .withdrawalMoney(0L)
                        .betCount(0)
                        .betPlayerSet(new HashSet<>())
                        .sameIp(0)
                        .rechargePlayerSet(new HashSet<>())
                        .subIp(new HashSet<>())
                        .build());
            }
        }
        if(queryParam.getSelf()==1){
            //list1首充数
            checkFirstRecharge(queryParam, statisDto1, proxyUser);
            //总余额
            statisDto1.setTotalMoney(statisDto1.getTotalMoney()+proxyUser.getBalance());
            //同ip
            isSameIp(proxyUser, statisDto1, ipSet);
        }
        //list2的总余额
        for(BallPlayer item:ballPlayers){
            if(queryParam.getProxyLine()==1){
                String superTree = item.getSuperTree();
                String[] split = superTree.split("_");
                for(String sitem:split){
                    if(StringUtils.isBlank(sitem)){
                        continue;
                    }
                    ProxyStatisDto proxyStatisDto = list2Map.get(Long.parseLong(sitem));
                    if(proxyStatisDto==null){
                        continue;
                    }
                    //下级总余额
                    proxyStatisDto.setTotalMoney(proxyStatisDto.getTotalMoney()+item.getBalance());
                    //首充
                    if(item.getFirstTopUpTime()!=0){
                        //如果有日期查询,首充时间位置查询时间之间
                        boolean isFirst = true;
                        isFirst = isFirstRechargeBegin(queryParam, item, isFirst);
                        isFirst = isFirstRechargeEnd(queryParam, item, isFirst);
                        if(isFirst){
                            proxyStatisDto.setRechargeFirst(proxyStatisDto.getRechargeFirst()+1);
                        }
                    }
                    //同IP
                    Set<String> subIp = proxyStatisDto.getSubIp();
                    isSameIp(item, proxyStatisDto, subIp);
                    //总下级
                    proxyStatisDto.setSubCountAll(proxyStatisDto.getSubCountAll()+1);
                    //直属下级=
                    if(item.getVipRank()-proxyStatisDto.getVipRank()==1){
                        proxyStatisDto.setSubCount(proxyStatisDto.getSubCount()+1);
                    }
                }
            }
        }

        final CountDownLatch countDownLatch = new CountDownLatch(4);
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        ThreadPoolUtil.execSaki(() -> {
            //统计下级充值,线上线下+人工
            loggerRechargeService.search(queryParam,ids,statisDto1,list2Map,proxyUser,systemConfig);
            //        loggerHandsupService.search(queryParam,ids,statisDto1,list2Map,proxyUser);
            countDownLatch.countDown();
        });
        ThreadPoolUtil.execSaki(() -> {
            //统计下级提现+人工
            loggerWithdrawalService.search(queryParam,ids,statisDto1,list2Map,proxyUser,systemConfig);
            //        loggerHandsupService.searchWithdrawal(queryParam,ids,statisDto1,list2Map,proxyUser);
            countDownLatch.countDown();
        });
        ThreadPoolUtil.execSaki(() -> {
            //统计下级投注
            ballBetService.search(queryParam,ids,statisDto1,list2Map,proxyUser);
            countDownLatch.countDown();
        });
        ThreadPoolUtil.execSaki(() -> {
            //统计下级返利
            ballBalanceChangeService.search(queryParam,statisDto1,list2Map,proxyUser);
            countDownLatch.countDown();
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
        }
        ArrayList<ProxyStatisDto> proxyStatisDtos = new ArrayList<>(list2Map.values());
        Collections.sort(proxyStatisDtos, (o1, o2) -> Math.toIntExact(o2.getTotalMoney() - o1.getTotalMoney()));
        responseMap.put("proxySub",proxyStatisDtos);
        return BaseResponse.successWithData(responseMap);
    }

    @Override
    public BaseResponse statis2(BallProxyLogger queryParam) throws ParseException {
//!--日期 代理账号 直属下级 总下级 首充人数 充值人数 提现人数 充值金额 提现金额 下注次数 下注人数 充提差-->
        BallPlayer playerProxy = basePlayerService.findByUsername(queryParam.getPlayerName());
        if(playerProxy==null||playerProxy.getProxyPlayer()==0){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e3"));
        }
        if(StringUtils.isBlank(queryParam.getBegin())){
            queryParam.setBegin(TimeUtil.longToStringYmd(playerProxy.getCreatedAt()));
        }
        if(StringUtils.isBlank(queryParam.getEnd())){
            queryParam.setEnd(TimeUtil.longToStringYmd(System.currentTimeMillis()));
        }
        long start = TimeUtil.stringToTimeStamp(queryParam.getBegin(),TimeUtil.TIME_YYYY_MM_DD);
        long end = TimeUtil.stringToTimeStamp(queryParam.getEnd(),TimeUtil.TIME_YYYY_MM_DD);
        long endTrue = end+TimeUtil.TIME_ONE_DAY;
        Map<String,ProxyStatis2Dto> dataMap = new HashMap<>();
        for(long i=start;i<=end;i+=TimeUtil.TIME_ONE_DAY){
            dataMap.put(TimeUtil.longToStringYmd(i),ProxyStatis2Dto.builder()
                    .ymd(TimeUtil.longToStringYmd(i))
                    .betCount(0)
                    .betPlayerSet(new HashSet<>())
                    .rechargeFirst(0)
                    .rechargeMoney(0L)
                    .rechargePlayerSet(new HashSet<>())
                    .subCount(0)
                    .subCountAll(0)
                    .username(playerProxy.getUsername())
                    .withdrawalMoney(0L)
                    .withdrawalPlayerSet(new HashSet<>())
                    .rechargeTotal(0L)
                    .withdrawalTotal(0L)
                    .betPlayerYestodaySet(new HashSet<>())
                    .build());
        }
        ProxyStatis2Dto total = ProxyStatis2Dto.builder()
                .ymd("总计")
                .betCount(0)
                .betPlayerSet(new HashSet<>())
                .rechargeFirst(0)
                .rechargeMoney(0L)
                .rechargePlayerSet(new HashSet<>())
                .subCount(0)
                .subCountAll(0)
                .username(playerProxy.getUsername())
                .withdrawalMoney(0L)
                .withdrawalPlayerSet(new HashSet<>())
                .rechargeTotal(0L)
                .withdrawalTotal(0L)
                .betPlayerYestodaySet(new HashSet<>())
                .build();
        //查询所有下级
        List<BallPlayer> ballPlayers = ballPlayerService.searchProxy(queryParam, playerProxy);
        for(BallPlayer player:ballPlayers){
            for(ProxyStatis2Dto item:dataMap.values()){
                item.setWithdrawalTotal(item.getWithdrawalTotal()+player.getCumulativeReflect());
                item.setRechargeTotal(item.getRechargeTotal()+player.getCumulativeTopUp());
            }

            total.setWithdrawalTotal(total.getWithdrawalTotal()+player.getCumulativeReflect());
            total.setRechargeTotal(total.getRechargeTotal()+player.getCumulativeTopUp());
            //首充
            Long firstTopUpTime = player.getFirstTopUpTime();
            String first = TimeUtil.longToStringYmd(firstTopUpTime);
            ProxyStatis2Dto proxyStatis2Dto = dataMap.get(first);
            if(proxyStatis2Dto!=null){
                if(firstTopUpTime>=start&&firstTopUpTime<=endTrue){
                    proxyStatis2Dto.setRechargeFirst(proxyStatis2Dto.getRechargeFirst()+1);
                    total.setRechargeFirst(total.getRechargeFirst()+1);
                }
            }
            //下级统计
            Long createdAt = player.getCreatedAt();
            String create = TimeUtil.longToStringYmd(createdAt);
            ProxyStatis2Dto proxyStatis2Dto1 = dataMap.get(create);
            if(proxyStatis2Dto1!=null){
                if(createdAt>=start&&createdAt<=endTrue) {
                    if(player.getSuperiorId().equals(playerProxy.getId())){
                        proxyStatis2Dto1.setSubCount(proxyStatis2Dto1.getSubCount()+1);
                        total.setSubCount(total.getSubCount()+1);
                    }
                    proxyStatis2Dto1.setSubCountAll(proxyStatis2Dto1.getSubCountAll()+1);
                    total.setSubCountAll(total.getSubCountAll()+1);
                }
            }
        }
        final CountDownLatch countDownLatch = new CountDownLatch(3);
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        ThreadPoolUtil.execSaki(() -> {
            //统计下级充值,线上线下
            loggerRechargeService.searchProxy2(queryParam,dataMap,playerProxy,total,systemConfig);
            countDownLatch.countDown();
        });
        ThreadPoolUtil.execSaki(() -> {
            //统计下级提现
            loggerWithdrawalService.searchProxy2(queryParam,dataMap,playerProxy,total,systemConfig);
            countDownLatch.countDown();
        });
        ThreadPoolUtil.execSaki(() -> {
            //统计下级投注
            ballBetService.searchProxy2(queryParam,dataMap,playerProxy,total);
            //统计昨日投注
            ballBetService.searchProxy3(queryParam,dataMap,playerProxy,total);
            countDownLatch.countDown();
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
        }
        ArrayList<ProxyStatis2Dto> proxyStatisDtos = new ArrayList<>(dataMap.values());
        Collections.sort(proxyStatisDtos, (o1, o2) -> Math.toIntExact(o2.getYmd().compareTo(o1.getYmd())));
        proxyStatisDtos.add(0,total);
        proxyStatisDtos.add(total);
        return BaseResponse.successWithData(proxyStatisDtos);
    }

    @Override
    public BaseResponse statis2() throws ParseException {
        List<BallPlayer> proxys = ballPlayerService.findProxys();
        if(proxys.isEmpty()){
            return BaseResponse.successWithData(new ArrayList<>());
        }
        String ymd = TimeUtil.dateFormat(new Date(),TimeUtil.TIME_YYYY_MM_DD);
        ProxyStatis2Dto total = ProxyStatis2Dto.builder()
                .ymd("总计")
                .betCount(0)
                .betPlayerSet(new HashSet<>())
                .rechargeFirst(0)
                .rechargeMoney(0L)
                .rechargePlayerSet(new HashSet<>())
                .subCount(0)
                .subCountAll(0)
                .withdrawalMoney(0L)
                .withdrawalPlayerSet(new HashSet<>())
                .rechargeTotal(0L)
                .withdrawalTotal(0L)
                .betPlayerYestodaySet(new HashSet<>())
                .build();
        List<ProxyStatis2Dto> list = new ArrayList<>();
        for(BallPlayer item:proxys){
            BaseResponse<List<ProxyStatis2Dto>> response = statis2(BallProxyLogger.builder()
                    .playerName(item.getUsername())
                    .begin(ymd)
                    .end(ymd)
                    .build());
            List<ProxyStatis2Dto> data = response.getData();
            ProxyStatis2Dto proxyStatis2Dto = data.get(1);
            list.add(proxyStatis2Dto);
            total.setBetCount(total.getBetCount()+proxyStatis2Dto.getBetCount());
            total.getBetPlayerSet().addAll(proxyStatis2Dto.getBetPlayerSet());
            total.setRechargeFirst(total.getRechargeFirst()+proxyStatis2Dto.getRechargeFirst());
            total.setRechargeMoney(total.getRechargeMoney()+proxyStatis2Dto.getRechargeMoney());
            total.getRechargePlayerSet().addAll(proxyStatis2Dto.getRechargePlayerSet());
            total.setSubCount(total.getSubCount()+proxyStatis2Dto.getSubCount());
            total.setSubCountAll(total.getSubCountAll()+proxyStatis2Dto.getSubCountAll());
            total.setWithdrawalMoney(total.getWithdrawalMoney()+proxyStatis2Dto.getWithdrawalMoney());
            total.getWithdrawalPlayerSet().addAll(proxyStatis2Dto.getWithdrawalPlayerSet());
            total.setRechargeTotal(total.getRechargeTotal()+proxyStatis2Dto.getRechargeTotal());
            total.setWithdrawalTotal(total.getWithdrawalTotal()+proxyStatis2Dto.getWithdrawalTotal());
            total.getBetPlayerYestodaySet().addAll(proxyStatis2Dto.getBetPlayerYestodaySet());
        }
        list.add(0,total);
        list.add(total);
        return BaseResponse.successWithData(list);
    }

    @Override
    public BaseResponse statis2_1(BallProxyLogger queryParam) throws ParseException {
        //按日期汇总各代理数据
        List<BallPlayer> proxys = ballPlayerService.findProxys(queryParam);
        if(proxys.isEmpty()){
            return BaseResponse.successWithData(new ArrayList<>());
        }
        ProxyStatis3Dto total = ProxyStatis3Dto.builder()
                .ymd("总计")
                .betCount(0)
                .betPlayer(0)
                .rechargeFirst(0)
                .rechargeMoney(0L)
                .rechargeCount(0)
                .subCount(0)
                .subCountAll(0)
                .withdrawalMoney(0L)
                .withdrawalCount(0)
                .rechargeTotal(0L)
                .withdrawalTotal(0L)
                .betPlayerYestoday(0)
                .build();
        List<ProxyStatis3Dto> list = new ArrayList<>();
        for(BallPlayer item:proxys){
            ProxyStatis3Dto data = ProxyStatis3Dto.builder()
                    .ymd("小计")
                    .betCount(0)
                    .betPlayer(0)
                    .rechargeFirst(0)
                    .rechargeMoney(0L)
                    .rechargeCount(0)
                    .subCount(0)
                    .subCountAll(0)
                    .username(item.getUsername())
                    .withdrawalMoney(0L)
                    .withdrawalCount(0)
                    .rechargeTotal(0L)
                    .withdrawalTotal(0L)
                    .betPlayerYestoday(0)
                    .build();
            list.add(data);
            queryParam.setPlayerName(item.getSuperTree());
            queryParam.setPlayerId(item.getId());
            final CountDownLatch countDownLatch = new CountDownLatch(5);
            BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
            ThreadPoolUtil.execSaki(() -> {
                //统计下级充值,线上线下
                try {
                    loggerRechargeService.statis(queryParam,total,data,systemConfig);
                    //累计提现和充值
                    ballPlayerService.statis(queryParam,total,data,systemConfig);
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    countDownLatch.countDown();
                }
            });
            ThreadPoolUtil.execSaki(() -> {
                //统计下级提现
                try {
                    loggerWithdrawalService.statis(queryParam,total,data,systemConfig);
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    countDownLatch.countDown();
                }
            });
            ThreadPoolUtil.execSaki(() -> {
                //统计下级投注
                try {
                    ballBetService.statis(queryParam,total,data);
                    //昨日投注
                    ballBetService.statis2(queryParam,total,data);
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    countDownLatch.countDown();
                }
            });
            ThreadPoolUtil.execSaki(() -> {
                //统计下级首充
                try {
                    ballPlayerService.statisFirst(queryParam,total,data);
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    countDownLatch.countDown();
                }
            });
            ThreadPoolUtil.execSaki(() -> {
                //统计下级人数
                try {
                    ballPlayerService.statisSubs(queryParam,total,data);
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    countDownLatch.countDown();
                }
            });
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
            }
        }
        list.add(0,total);
        list.add(total);
        return BaseResponse.successWithData(list);
    }

    private void checkFirstRecharge(BallProxyLogger queryParam, ProxyStatisDto statisDto1, BallPlayer item) {
        if(item.getFirstTopUpTime()!=0){
            //如果有日期查询
            boolean isFirst = true;
            isFirst = isFirstRechargeBegin(queryParam, item, isFirst);
            isFirst = isFirstRechargeEnd(queryParam, item, isFirst);
            if(isFirst){
                int first = statisDto1.getRechargeFirst()==null?0:statisDto1.getRechargeFirst();
                statisDto1.setRechargeFirst(first+1);
            }
        }
    }

    private boolean isFirstRechargeEnd(BallProxyLogger queryParam, BallPlayer item, boolean isFirst) {
        if(!StringUtils.isBlank(queryParam.getEnd())){
            try {
                long end = TimeUtil.stringToTimeStamp(queryParam.getEnd(),TimeUtil.TIME_YYYY_MM_DD)+TimeUtil.TIME_ONE_DAY;
                if(item.getFirstTopUpTime()>end){
                    isFirst = false;
                }
            } catch (ParseException e) {
            }
        }
        return isFirst;
    }

    private boolean isFirstRechargeBegin(BallProxyLogger queryParam, BallPlayer item, boolean isFirst) {
        if(!StringUtils.isBlank(queryParam.getBegin())){
            try {
                long begin = TimeUtil.stringToTimeStamp(queryParam.getBegin(),TimeUtil.TIME_YYYY_MM_DD);
                if(item.getFirstTopUpTime()<begin){
                    isFirst = false;
                }
            } catch (ParseException e) {
            }
        }
        return isFirst;
    }

    private void isSameIp(BallPlayer item, ProxyStatisDto proxyStatisDto, Set<String> subIp) {
        if(!StringUtils.isBlank(item.getIp())){
            String[] split1 = item.getIp().split(",");
            for(String ip:split1){
                if(ip.contains(":")){
                    boolean add = subIp.add(ip);
                    if(!add){
                        proxyStatisDto.setSameIp(proxyStatisDto.getSameIp()+1);
                    }
                }
            }
        }
    }
}
