package com.oxo.ball.service.impl.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oxo.ball.bean.dao.BallBalanceChange;
import com.oxo.ball.bean.dao.BallPlayer;
import com.oxo.ball.bean.dao.BallProxyLogger;
import com.oxo.ball.bean.dto.req.player.BalanceChangeRequest;
import com.oxo.ball.bean.dto.req.player.DataCenterRequest;
import com.oxo.ball.bean.dto.req.report.ReportBalanceChangeRequest;
import com.oxo.ball.bean.dto.req.report.ReportDataRequest;
import com.oxo.ball.bean.dto.req.report.ReportOperateRequest;
import com.oxo.ball.bean.dto.req.report.ReportPlayerDayRequest;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.bean.dto.resp.admin.ProxyStatisDto;
import com.oxo.ball.bean.dto.resp.player.ReportFormResponse;
import com.oxo.ball.bean.dto.resp.player.ReportFormTeamResponse;
import com.oxo.ball.mapper.BallBalanceChangeMapper;
import com.oxo.ball.service.admin.IBallBalanceChangeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxo.ball.service.admin.IBallPlayerService;
import com.oxo.ball.service.impl.BasePlayerService;
import com.oxo.ball.utils.BigDecimalUtil;
import com.oxo.ball.utils.TimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.*;

/**
 * <p>
 * 账变表 服务实现类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Service
public class BallBalanceChangeServiceImpl extends ServiceImpl<BallBalanceChangeMapper, BallBalanceChange> implements IBallBalanceChangeService {

    @Autowired
    BasePlayerService basePlayerService;
    @Autowired
    IBallPlayerService ballPlayerService;

    @Override
    public SearchResponse<BallBalanceChange> search(BallPlayer currentUser, BalanceChangeRequest queryParam, Integer pageNo, Integer pageSize) {
        SearchResponse<BallBalanceChange> response = new SearchResponse<>();

        Page<BallBalanceChange> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallBalanceChange> query = new QueryWrapper<>();
        if (currentUser != null) {
            query.eq("player_id", currentUser.getId());
        }
        if (queryParam.getType() != null) {
            query.eq("balance_change_type", queryParam.getType());
        }
        if (queryParam.getTime() != null) {
            switch (queryParam.getTime()) {
                case 1:
                    query.ge("created_at", TimeUtil.getDayBegin().getTime());
                    query.le("created_at", TimeUtil.getDayEnd().getTime());
                    break;
                case 2:
                    query.ge("created_at", TimeUtil.getBeginDayOfYesterday().getTime());
                    query.le("created_at", TimeUtil.getEndDayOfYesterday().getTime());
                    break;
                case 3:
                    query.ge("created_at", TimeUtil.getDayBegin().getTime() - 7 * TimeUtil.TIME_ONE_DAY);
                    query.le("created_at", TimeUtil.getDayEnd().getTime());
                    break;
                default:
                    break;
            }
        }

        query.orderByDesc("id");
        IPage<BallBalanceChange> pages = page(page, query);
        List<BallBalanceChange> records = pages.getRecords();
        for(BallBalanceChange item:records){
            item.setRemark("");
        }
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public SearchResponse<BallBalanceChange> search(BallBalanceChange queryParam, Integer pageNo, Integer pageSize) {
        SearchResponse<BallBalanceChange> response = new SearchResponse<>();
        Page<BallBalanceChange> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallBalanceChange> query = new QueryWrapper<>();
        query.eq("player_id", queryParam.getPlayerId());
        if (queryParam.getBalanceChangeType() != null) {
            query.eq("balance_change_type", queryParam.getBalanceChangeType());
        }
        if(queryParam.getOrderNo()!=null){
            query.eq("order_no",queryParam.getOrderNo());
        }
        if (queryParam.getTimeType() != null) {
            switch (queryParam.getTimeType()) {
                case 0:
                    query.ge("created_at", TimeUtil.getDayBegin().getTime());
                    query.le("created_at", TimeUtil.getDayEnd().getTime());
                    break;
                case 1:
                    query.ge("created_at", TimeUtil.getBeginDayOfYesterday().getTime());
                    query.le("created_at", TimeUtil.getEndDayOfYesterday().getTime());
                    break;
                case 2:
                    query.ge("created_at", TimeUtil.getDayBegin().getTime() - 3 * TimeUtil.TIME_ONE_DAY);
                    query.le("created_at", TimeUtil.getDayEnd().getTime());
                    break;
                case 3:
                    query.ge("created_at", TimeUtil.getBeginDayOfWeek().getTime());
                    query.le("created_at", TimeUtil.getDayEnd().getTime());
                    break;
                case 4:
                    query.ge("created_at", TimeUtil.getBeginDayOfLastWeek().getTime());
                    query.le("created_at", TimeUtil.getEndDayOfLastWeek().getTime());
                    break;
                case 5:
                    query.ge("created_at", TimeUtil.getBeginDayOfMonth().getTime());
                    query.le("created_at", TimeUtil.getDayEnd().getTime());
                    break;
                case 6:
                    query.ge("created_at", TimeUtil.getBeginDayOfLastMonth().getTime());
                    query.le("created_at", TimeUtil.getEndDayOfLastMonth().getTime());
                    break;
                case 7:
                    if(!StringUtils.isBlank(queryParam.getBegin())){
                        try {
                            long timeStamp = TimeUtil.stringToTimeStamp(queryParam.getBegin(), TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS);
                            query.ge("created_at", timeStamp);
                        } catch (ParseException e) {
                            try {
                                long timeStamp = TimeUtil.stringToTimeStamp(queryParam.getBegin(), TimeUtil.TIME_YYYY_MM_DD);
                                query.ge("created_at", timeStamp);
                            } catch (ParseException e1) {
                            }
                        }
                    }
                    if(!StringUtils.isBlank(queryParam.getEnd())){
                        try {
                            long timeStamp = TimeUtil.stringToTimeStamp(queryParam.getEnd(), TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS);
                            query.le("created_at", timeStamp);
                        } catch (ParseException e) {
                            try {
                                long timeStamp = TimeUtil.stringToTimeStamp(queryParam.getEnd(), TimeUtil.TIME_YYYY_MM_DD);
                                query.le("created_at", timeStamp+TimeUtil.TIME_ONE_DAY);
                            } catch (ParseException e1) {
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }

        query.orderByDesc("id");
        IPage<BallBalanceChange> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        BallBalanceChange sum = BallBalanceChange.builder()
                .username("小计")
                .initMoney(0L)
                .changeMoney(0L)
                .dnedMoney(0L)
                .build();
        for(BallBalanceChange item:pages.getRecords()){
            sum.setInitMoney(item.getInitMoney()+sum.getInitMoney());
            sum.setChangeMoney(item.getChangeMoney()+sum.getChangeMoney());
            sum.setDnedMoney(item.getDnedMoney()+sum.getDnedMoney());
        }
        if(sum.getDnedMoney()>0){
            pages.getRecords().add(0,sum);
            pages.getRecords().add(sum);
        }
        return response;
    }

    @Override
    public BallBalanceChange searchTotal(BallBalanceChange queryParam, Integer pageNo, Integer pageSize) {
        QueryWrapper<BallBalanceChange> query = new QueryWrapper<>();
        query.eq("player_id", queryParam.getPlayerId());
        if (queryParam.getBalanceChangeType() != null) {
            query.eq("balance_change_type", queryParam.getBalanceChangeType());
        }
        if(queryParam.getOrderNo()!=null){
            query.eq("order_no",queryParam.getOrderNo());
        }
        if (queryParam.getTimeType() != null) {
            switch (queryParam.getTimeType()) {
                case 0:
                    query.ge("created_at", TimeUtil.getDayBegin().getTime());
                    query.le("created_at", TimeUtil.getDayEnd().getTime());
                    break;
                case 1:
                    query.ge("created_at", TimeUtil.getBeginDayOfYesterday().getTime());
                    query.le("created_at", TimeUtil.getEndDayOfYesterday().getTime());
                    break;
                case 2:
                    query.ge("created_at", TimeUtil.getDayBegin().getTime() - 3 * TimeUtil.TIME_ONE_DAY);
                    query.le("created_at", TimeUtil.getDayEnd().getTime());
                    break;
                case 3:
                    query.ge("created_at", TimeUtil.getBeginDayOfWeek().getTime());
                    query.le("created_at", TimeUtil.getDayEnd().getTime());
                    break;
                case 4:
                    query.ge("created_at", TimeUtil.getBeginDayOfLastWeek().getTime());
                    query.le("created_at", TimeUtil.getEndDayOfLastWeek().getTime());
                    break;
                case 5:
                    query.ge("created_at", TimeUtil.getBeginDayOfMonth().getTime());
                    query.le("created_at", TimeUtil.getDayEnd().getTime());
                    break;
                case 6:
                    query.ge("created_at", TimeUtil.getBeginDayOfLastMonth().getTime());
                    query.le("created_at", TimeUtil.getEndDayOfLastMonth().getTime());
                    break;
                case 7:
                    if(!StringUtils.isBlank(queryParam.getBegin())){
                        try {
                            long timeStamp = TimeUtil.stringToTimeStamp(queryParam.getBegin(), TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS);
                            query.ge("created_at", timeStamp);
                        } catch (ParseException e) {
                            try {
                                long timeStamp = TimeUtil.stringToTimeStamp(queryParam.getBegin(), TimeUtil.TIME_YYYY_MM_DD);
                                query.ge("created_at", timeStamp);
                            } catch (ParseException e1) {
                            }
                        }
                    }
                    if(!StringUtils.isBlank(queryParam.getEnd())){
                        try {
                            long timeStamp = TimeUtil.stringToTimeStamp(queryParam.getEnd(), TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS);
                            query.le("created_at", timeStamp);
                        } catch (ParseException e) {
                            try {
                                long timeStamp = TimeUtil.stringToTimeStamp(queryParam.getEnd(), TimeUtil.TIME_YYYY_MM_DD);
                                query.le("created_at", timeStamp+TimeUtil.TIME_ONE_DAY);
                            } catch (ParseException e1) {
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        query.select("sum(init_money) init_money,sum(change_money) change_money,sum(dned_money) dned_money");
        BallBalanceChange one = getOne(query);
        if(one!=null){
            one.setUsername("总计");
        }
        return one;
    }

    @Override
    public SearchResponse<BallBalanceChange> search(ReportPlayerDayRequest reportDataRequest, Integer pageNo, Integer pageSize) {
        SearchResponse<BallBalanceChange> response = new SearchResponse<>();
        Page<BallBalanceChange> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallBalanceChange> query = new QueryWrapper<>();
        query.eq("account_type",2);
        query.eq("frozen_status",1);
        if(reportDataRequest.getUserId()!=null){
            BallPlayer player = basePlayerService.findByUserId(reportDataRequest.getUserId());
            if(player!=null){
                query.eq("player_id",player.getId());
            }
        } else if(!StringUtils.isBlank(reportDataRequest.getUsername())){
            BallPlayer player = basePlayerService.findByUsername(reportDataRequest.getUsername());
            if(player!=null){
                query.eq("player_id",player.getId());
            }
        }
        BallPlayerServiceImpl.queryByTime(query,reportDataRequest.getTime(),reportDataRequest.getBegin(),reportDataRequest.getEnd());
        IPage<BallBalanceChange> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public SearchResponse<BallBalanceChange> search(ReportBalanceChangeRequest queryRequest) {
        SearchResponse<BallBalanceChange> response = new SearchResponse<>();
        Page<BallBalanceChange> page = new Page<>(queryRequest.getPageNo(), queryRequest.getPageSize());
        QueryWrapper<BallBalanceChange> query = new QueryWrapper<>();
        if(queryRequest.getAccountType()!=null){
            query.eq("account_type",queryRequest.getAccountType());
        }
        BallPlayer playerTree = null;
        //仅自己
        if(queryRequest.getUserId()!=null){
            playerTree = basePlayerService.findByUserId(queryRequest.getUserId());
            if(playerTree==null) {
                query.eq("player_id",-100);
            }
        }else{
            if(!StringUtils.isBlank(queryRequest.getUsername())){
                playerTree = basePlayerService.findByUsername(queryRequest.getUsername());
                if(playerTree==null) {
                    query.eq("username",queryRequest.getUsername());
                }
            }
        }
        if(queryRequest.getTreeType()!=null){
            if(playerTree!=null){
                //要查直属下级,或者全部下级
                if(queryRequest.getTreeType()==1){
                    //查直属
                    query.eq("parent_id",playerTree.getId());
                }else if(queryRequest.getTreeType()==2){
                    //查全部下级
                    query.likeRight("super_tree",playerTree.getSuperTree()+playerTree.getId()+"\\_");
                }
            }else{
                query.eq("player_id",-100);
            }
        }else{
            if(playerTree!=null){
                if(queryRequest.getUserId()!=null){
                    query.eq("player_id",playerTree.getId());
                }else{
                    if(!StringUtils.isBlank(queryRequest.getUsername())){
                        query.like("username",queryRequest.getUsername());
                    }
                }
            }
        }
        if(queryRequest.getType()!=null){
            query.eq("balance_change_type",queryRequest.getType());
        }
        if(queryRequest.getOrderNo()!=null){
            query.eq("order_no",queryRequest.getOrderNo());
        }
        query.orderByDesc("id");
        BallPlayerServiceImpl.queryByTime(query,queryRequest.getTime(),queryRequest.getBegin(),queryRequest.getEnd());
        IPage<BallBalanceChange> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        BallBalanceChange sum = BallBalanceChange.builder()
                .username("小计")
                .initMoney(0L)
                .changeMoney(0L)
                .dnedMoney(0L)
                .build();
        for(BallBalanceChange item:pages.getRecords()){
            sum.setInitMoney(item.getInitMoney()+sum.getInitMoney());
            sum.setChangeMoney(item.getChangeMoney()+sum.getChangeMoney());
            sum.setDnedMoney(item.getDnedMoney()+sum.getDnedMoney());
        }
        if(sum.getDnedMoney()>0){
            pages.getRecords().add(0,sum);
            pages.getRecords().add(sum);
        }
        return response;
    }

    @Override
    public BallBalanceChange searchTotal(ReportBalanceChangeRequest queryRequest) {
        QueryWrapper<BallBalanceChange> query = new QueryWrapper<>();
        if(queryRequest.getAccountType()!=null){
            query.eq("account_type",queryRequest.getAccountType());
        }
        BallPlayer playerTree = null;
        //仅自己
        //仅自己
        if(queryRequest.getUserId()!=null){
            playerTree = basePlayerService.findByUserId(queryRequest.getUserId());
            if(playerTree==null) {
                query.eq("player_id",-100);
            }
        }else{
            if(!StringUtils.isBlank(queryRequest.getUsername())){
                playerTree = basePlayerService.findByUsername(queryRequest.getUsername());
            }
        }
        if(queryRequest.getTreeType()!=null){
            if(playerTree!=null){
                //要查直属下级,或者全部下级
                if(queryRequest.getTreeType()==1){
                    //查直属
                    query.eq("parent_id",playerTree.getId());
                }else if(queryRequest.getTreeType()==2){
                    //查全部下级
                    query.likeRight("super_tree",playerTree.getSuperTree()+playerTree.getId()+"\\_");
                }
            }else{
                query.eq("player_id",-100);
            }
        }else{
            if(playerTree!=null){
                if(queryRequest.getUserId()!=null){
                    query.eq("player_id",playerTree.getId());
                }else{
                    if(!StringUtils.isBlank(queryRequest.getUsername())){
                        query.eq("username",queryRequest.getUsername());
                    }
                }
            }
        }
        if(queryRequest.getType()!=null){
            query.eq("balance_change_type",queryRequest.getType());
        }
        if(queryRequest.getOrderNo()!=null){
            query.eq("order_no",queryRequest.getOrderNo());
        }
        query.select("sum(init_money) init_money,sum(change_money) change_money,sum(dned_money) dned_money");
        BallPlayerServiceImpl.queryByTime(query,queryRequest.getTime(),queryRequest.getBegin(),queryRequest.getEnd());
        BallBalanceChange one = getOne(query);
        if(one!=null){
            one.setUsername("总计");
        }
        return one;
    }

    @Override
    public boolean insert(BallBalanceChange balanceChange) {
        return save(balanceChange);
    }

    @Override
    public List<ReportFormResponse> reportForm(BallPlayer player, DataCenterRequest dataCenterRequest) {
        QueryWrapper<BallBalanceChange> query = new QueryWrapper();
        query.eq("player_id", player.getId());
        queryCaseTime(dataCenterRequest, query);
        Map<String, ReportFormResponse> dataMap = createDataMapForDate(dataCenterRequest.getTime());
        List<BallBalanceChange> list = list(query);
        for (BallBalanceChange item : list) {
            String date = TimeUtil.dateFormat(new Date(item.getCreatedAt()), TimeUtil.TIME_YYYY_MM_DD);
            ReportFormResponse res = dataMap.get(date);
            switch (item.getBalanceChangeType()) {
                case 1:
                case 11:
                    res.setRecharge(BigDecimalUtil.objToLong(res.getRecharge()) + item.getChangeMoney());
                    break;
                case 2:
                    res.setWithdrawal(BigDecimalUtil.objToLong(res.getWithdrawal()) + item.getChangeMoney());
                    break;
                case 4:
                    res.setBingo(BigDecimalUtil.objToLong(res.getBingo()) + item.getChangeMoney());
                    break;
                case 3:
                    res.setBet(BigDecimalUtil.objToLong(res.getBet()) + item.getChangeMoney());
                    break;
                case 5:
                    res.setRebate(BigDecimalUtil.objToLong(res.getRebate()) + item.getChangeMoney());
                    break;
                case 18:
                    res.setActivity(BigDecimalUtil.objToLong(res.getActivity()) + item.getChangeMoney());
                    break;
                default:
                    break;
            }
        }
        ArrayList<ReportFormResponse> reportFormResponses = new ArrayList<>(dataMap.values());
        Collections.sort(reportFormResponses, (o1, o2) -> o2.getDate().compareTo(o1.getDate()));
        return reportFormResponses;
    }

    @Override
    public List<ReportFormTeamResponse> reportFormTeam(BallPlayer player, DataCenterRequest dataCenterRequest) {
        BallPlayer superPlayer = null;
        if(player.getSuperiorId()!=null&&player.getSuperiorId()!=0){
            superPlayer = basePlayerService.findById(player.getSuperiorId());
        }
        Map<String, ReportFormTeamResponse> dataMap = createDataMapForDateTeam(dataCenterRequest.getTime(),superPlayer);
        //团队还要加上自己的
        QueryWrapper<BallBalanceChange> querySelf = new QueryWrapper();
        querySelf.eq("frozen_status",1);
        queryCaseTime(dataCenterRequest, querySelf);
        querySelf.in("balance_change_type",1,2,3,4,11);
        querySelf.and(QueryWrapper -> QueryWrapper.eq("player_id",player.getId())
                .or()
                .likeRight("super_tree",player.getSuperTree() + player.getId()+"\\_"));
        int pageNo = 1;
        while(true){
            Page<BallBalanceChange> page = new Page<>(pageNo++, 500);
            IPage<BallBalanceChange> listBalanceChanges = page(page,querySelf);
            List<BallBalanceChange> records = listBalanceChanges.getRecords();
            if(records==null||records.isEmpty()||records.get(0)==null){
                break;
            }
            for (BallBalanceChange item : records) {
                String date = TimeUtil.dateFormat(new Date(item.getCreatedAt()), TimeUtil.TIME_YYYY_MM_DD);
                ReportFormTeamResponse res = dataMap.get(date);
                switch (item.getBalanceChangeType()) {
                    case 1:
                    case 11:
                        res.setRecharge(res.getRecharge() + item.getChangeMoney());
                        break;
                    case 2:
                        res.setWithdrawal(res.getWithdrawal() + (-item.getChangeMoney()));
                        break;
                    case 4:
                        res.setBingo(res.getBingo() + item.getChangeMoney());
                        break;
                    case 3:
                        res.setBet(res.getBet() + (-item.getChangeMoney()));
                        res.getBetCountSet().add(item.getPlayerId());
                        break;
                    case 18:
                        res.setActivity(res.getActivity() + item.getChangeMoney());
                        break;
                    default:
                        break;
                }
            }
        }


        ArrayList<ReportFormTeamResponse> reportFormResponses = new ArrayList<>(dataMap.values());
        Collections.sort(reportFormResponses, (o1, o2) -> o2.getDate().compareTo(o1.getDate()));
        return reportFormResponses;
    }

    @Override
    public SearchResponse<BallBalanceChange> statisReport(ReportOperateRequest reportOperateRequest) {
        SearchResponse<BallBalanceChange> response = new SearchResponse<>();

        Page<BallBalanceChange> page = new Page<>(reportOperateRequest.getPageNo(), reportOperateRequest.getPageSize());
        QueryWrapper<BallBalanceChange> query = new QueryWrapper<>();
        query.select("created_at,sum(case when balance_change_type=1 or balance_change_type=6 or balance_change_type=11 then change_money end) as recharge" +
                ",count(case when balance_change_type=1 or balance_change_type=6 or balance_change_type=11 then 1 end) as recharge_count" +
                ",sum(case when balance_change_type=2 or balance_change_type=8 then change_money end) as withdrawal" +
                ",count(case when balance_change_type=2 or balance_change_type=8 then 1 end) as withdrawal_count" +
                ",sum(case when balance_change_type=3 then change_money end) as bet_money" +
                ",sum(case when balance_change_type=4 then change_money end) as bingo_money" +
                ",sum(case when balance_change_type=20 then change_money end) as discount" +
                ",sum(case when balance_change_type=19 then change_money end) as recharge_more" +
                ",sum(case when balance_change_type=5 then change_money end) as activity" +
                ",player_id");
        query.eq("account_type",2);
        query.eq("frozen_status", 1);
        BallPlayer playerTree = null;
        if(reportOperateRequest.getUserId()!=null){
            //如果没指定查下级就查自己，
            if(reportOperateRequest.getTreeType()==null){
                query.eq("user_id",reportOperateRequest.getUserId());
            }else{
                playerTree = basePlayerService.findByUserId(reportOperateRequest.getUserId());
            }
        }
        if(!StringUtils.isBlank(reportOperateRequest.getUsername())){
            //如果没指定查下级就查自己，
            if(reportOperateRequest.getTreeType()==null) {
                query.eq("username",reportOperateRequest.getUsername());
            }else{
                playerTree = basePlayerService.findByUsername(reportOperateRequest.getUsername());
            }
        }
        if(reportOperateRequest.getTreeType()!=null){
            if(playerTree!=null){
                //要查直属下级,或者全部下级
                if(reportOperateRequest.getTreeType()==1){
                    //查直属
                    query.eq("parent_id",playerTree.getId());
                }else if(reportOperateRequest.getTreeType()==2){
                    //查全部下级
                    query.likeRight("super_tree",playerTree.getSuperTree()+playerTree.getId()+"\\_");
                }
            }
        }
        if (reportOperateRequest.getTime() != null) {
            BallPlayerServiceImpl.queryByTime(query,reportOperateRequest.getTime(),reportOperateRequest.getBegin(),reportOperateRequest.getEnd());
        }
        query.groupBy("player_id");
        IPage<BallBalanceChange> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public Long statisSubRebate(Long id, Long begin, Long end) {
        //查询指定账号今日返利
        QueryWrapper<BallBalanceChange> query = new QueryWrapper<>();
        query.eq("account_type",2);
        query.eq("player_id",id);
        query.between("created_at",begin,end);
        //返利
        query.eq("balance_change_type",5);
        query.select("sum(change_money) as change_money");
        BallBalanceChange one = getOne(query);
        if(one==null){
            return 0L;
        }
        return one.getChangeMoney();
    }

    @Override
    public Integer statisPayCount(Long id, Long begin, Long end) {
        //查询指定账号今日返利
        QueryWrapper<BallBalanceChange> query = new QueryWrapper<>();
        query.eq("account_type",2);
        query.likeRight("super_tree",","+id+",");
        query.between("created_at",begin,end);
        //返利
        query.in("balance_change_type",1,6,11);
        query.select("DISTINCT player_id");
        List<BallBalanceChange> list = list(query);
        if(list==null||list.isEmpty()||list.get(0).getPlayerId()==0){
            return 0;
        }
        return list.size();
    }

    @Override
    public BallBalanceChange findByOrderId(Integer type,Long orderNo) {
        QueryWrapper<BallBalanceChange> query = new QueryWrapper<>();
        query.eq("balance_change_type", type);
        query.eq("order_no",orderNo);
        return getOne(query);
    }

    @Override
    public Boolean edit(BallBalanceChange balanceChange) {
        return updateById(balanceChange);
    }

    @Override
    public BallBalanceChange statisDiscount(ReportDataRequest reportDataRequest) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.select("sum(change_money) as change_money");
        queryWrapper.eq("account_type",2);
        queryWrapper.eq("balance_change_type",19);
        BallPlayerServiceImpl.queryByTime(queryWrapper,reportDataRequest.getTime(),reportDataRequest.getBegin(),reportDataRequest.getEnd());
        List<BallBalanceChange> list = list(queryWrapper);
        if(list==null||list.isEmpty()||list.get(0)==null){
            return BallBalanceChange.builder()
                    .changeMoney(0L)
                    .build();
        }
        return list.get(0);
    }

    @Override
    public void search(BallProxyLogger queryParam, ProxyStatisDto list1, Map<Long, ProxyStatisDto> list2Map, BallPlayer proxyUser) {
        QueryWrapper<BallBalanceChange> query = new QueryWrapper<>();
        //正常结算订单
        query.in("balance_change_type",5,21,22);
        if(!StringUtils.isEmpty(queryParam.getBegin())){
            try {
                query.gt("created_at",TimeUtil.stringToTimeStamp(queryParam.getBegin(),TimeUtil.TIME_YYYY_MM_DD));
            } catch (ParseException e) {
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
            Page<BallBalanceChange> page = new Page<>(pageNo++, 500);
            IPage<BallBalanceChange> pageResult = page(page, query);
            List<BallBalanceChange> records = pageResult.getRecords();
            if(records==null||records.isEmpty()||records.get(0)==null){
                break;
            }
            for(BallBalanceChange item:records){
                if(queryParam.getProxyLine()==1){
                    String superTree = item.getSuperTree();
                    if(StringUtils.isEmpty(superTree)){
                        list1.setSubRebate(item.getChangeMoney()+list1.getSubRebate());
                        continue;
                    }
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
                            list1.setSubRebate(item.getChangeMoney()+list1.getSubRebate());
                            added = true;
                        }
                        proxyStatisDto.setSubRebate(proxyStatisDto.getSubRebate()+item.getChangeMoney());
                    }
                }else{
                    ProxyStatisDto proxyStatisDto = list2Map.get(item.getPlayerId());
                    if(proxyStatisDto!=null){
                        list1.setSubRebate(item.getChangeMoney()+list1.getSubRebate());
                        proxyStatisDto.setSubRebate(proxyStatisDto.getSubRebate()+item.getChangeMoney());
                    }
                }
                if(item.getPlayerId().equals(proxyUser.getId())){
                    list1.setSubRebate(item.getChangeMoney()+list1.getSubRebate());
                }
            }
        }
    }

    @Override
    public BallBalanceChange findLastByType(Long playerId, Integer type) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("balance_change_type",type);
        queryWrapper.eq("player_id",playerId);
        queryWrapper.orderByDesc("id");
        queryWrapper.last("limit 1");
        BallBalanceChange one = getOne(queryWrapper, false);
        return one;
    }

    private Map<String,ReportFormTeamResponse> createDataMapForDateTeam(Integer time, BallPlayer superPlayer) {
        Map<String, ReportFormTeamResponse> dataMap = new HashMap<>();
        long timeBegin = 0L;
        long timeEnd = 0L;
        String superName = superPlayer!=null?superPlayer.getUsername():"admin";
        switch (time) {
            case 1:
                timeBegin = TimeUtil.getDayBegin().getTime();
                timeEnd = TimeUtil.getDayEnd().getTime();
                break;
            case 2:
                timeBegin = TimeUtil.getBeginDayOfYesterday().getTime();
                timeEnd = TimeUtil.getEndDayOfYesterday().getTime();
                break;
            case 3:
                timeBegin = TimeUtil.getDayBegin().getTime() - 7 * TimeUtil.TIME_ONE_DAY;
                timeEnd = TimeUtil.getDayEnd().getTime();
                break;
            case 4:
                timeBegin = TimeUtil.getDayBegin().getTime() - 10 * TimeUtil.TIME_ONE_DAY;
                timeEnd = TimeUtil.getDayEnd().getTime();
                break;
            case 5:
                timeBegin = TimeUtil.getDayBegin().getTime() - 30 * TimeUtil.TIME_ONE_DAY;
                timeEnd = TimeUtil.getDayEnd().getTime();
                break;
            default:
                break;
        }
        for (long i = timeBegin; i <= timeEnd; i += TimeUtil.TIME_ONE_DAY) {
            String date = TimeUtil.dateFormat(new Date(i), TimeUtil.TIME_YYYY_MM_DD);
            dataMap.put(date,
                    ReportFormTeamResponse.builder()
                            .date(date)
                            .bet(0L)
                            .bingo(0L)
                            .activity(0L)
                            .recharge(0L)
                            .withdrawal(0L)
                            .superPlayer(superName)
                            .betCountSet(new HashSet<>())
                            .build());
        }
        return dataMap;
    }

    private Map<String, ReportFormResponse> createDataMapForDate(int time) {
        Map<String, ReportFormResponse> dataMap = new HashMap<>();
        long timeBegin = 0L;
        long timeEnd = 0L;
        switch (time) {
            case 1:
                timeBegin = TimeUtil.getDayBegin().getTime();
                timeEnd = TimeUtil.getDayEnd().getTime();
                break;
            case 2:
                timeBegin = TimeUtil.getBeginDayOfYesterday().getTime();
                timeEnd = TimeUtil.getEndDayOfYesterday().getTime();
                break;
            case 3:
                timeBegin = TimeUtil.getDayBegin().getTime() - 7 * TimeUtil.TIME_ONE_DAY;
                timeEnd = TimeUtil.getDayEnd().getTime();
                break;
            case 4:
                timeBegin = TimeUtil.getDayBegin().getTime() - 10 * TimeUtil.TIME_ONE_DAY;
                timeEnd = TimeUtil.getDayEnd().getTime();
                break;
            case 5:
                timeBegin = TimeUtil.getDayBegin().getTime() - 30 * TimeUtil.TIME_ONE_DAY;
                timeEnd = TimeUtil.getDayEnd().getTime();
                break;
            default:
                break;
        }
        for (long i = timeBegin; i <= timeEnd; i += TimeUtil.TIME_ONE_DAY) {
            String date = TimeUtil.dateFormat(new Date(i), TimeUtil.TIME_YYYY_MM_DD);
            dataMap.put(date,
                    ReportFormResponse.builder()
                            .date(date)
                            .activity(0L)
                            .bet(0L)
                            .bingo(0L)
                            .rebate(0L)
                            .recharge(0L)
                            .withdrawal(0L)
                            .build());
        }
        return dataMap;
    }

    public static void queryCaseTime(DataCenterRequest dataCenterRequest, QueryWrapper query) {
        if(dataCenterRequest.getTime() !=null){
            switch (dataCenterRequest.getTime()){
                case 1:
                    query.ge("created_at", TimeUtil.getDayBegin().getTime());
                    query.le("created_at", TimeUtil.getDayEnd().getTime());
                    break;
                case 2:
                    query.ge("created_at", TimeUtil.getBeginDayOfYesterday().getTime());
                    query.le("created_at", TimeUtil.getEndDayOfYesterday().getTime());
                    break;
                case 3:
                    query.ge("created_at", TimeUtil.getDayBegin().getTime() - 7 * TimeUtil.TIME_ONE_DAY);
                    query.le("created_at", TimeUtil.getDayEnd().getTime());
                    break;
                case 4:
                    query.ge("created_at", TimeUtil.getDayBegin().getTime() - 10 * TimeUtil.TIME_ONE_DAY);
                    query.le("created_at", TimeUtil.getDayEnd().getTime());
                    break;
                case 5:
                    query.ge("created_at", TimeUtil.getDayBegin().getTime() - 30 * TimeUtil.TIME_ONE_DAY);
                    query.le("created_at", TimeUtil.getDayEnd().getTime());
                    break;
                default:
                    break;
            }
        }
    }
}
