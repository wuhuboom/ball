package com.oxo.ball.service.impl.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxo.ball.bean.dao.BallLoggerHandsup;
import com.oxo.ball.bean.dao.BallPlayer;
import com.oxo.ball.bean.dao.BallProxyLogger;
import com.oxo.ball.bean.dto.req.report.ReportDataRequest;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.bean.dto.resp.admin.ProxyStatisDto;
import com.oxo.ball.mapper.BallLoggerHandsupMapper;
import com.oxo.ball.service.admin.IBallLoggerHandsupService;
import com.oxo.ball.utils.BigDecimalUtil;
import com.oxo.ball.utils.RedisUtil;
import com.oxo.ball.utils.TimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.*;

/**
 * <p>
 * 充值日志表 服务实现类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Service
public class BallLoggerHandsupServiceImpl extends ServiceImpl<BallLoggerHandsupMapper, BallLoggerHandsup> implements IBallLoggerHandsupService {

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public SearchResponse<BallLoggerHandsup> search(BallLoggerHandsup queryParam, Integer pageNo, Integer pageSize) {
        SearchResponse<BallLoggerHandsup> response = new SearchResponse<>();
        Page<BallLoggerHandsup> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallLoggerHandsup> query = new QueryWrapper<>();
        if(queryParam.getType()!=null){
            query.eq("type",queryParam.getType());
        }
        if(queryParam.getPlayerId()!=null){
            query.eq("player_id",queryParam.getPlayerId());
        }
        if(queryParam.getUserId()!=null){
            query.eq("user_id",queryParam.getUserId());
        }
        if(!StringUtils.isBlank(queryParam.getUsername())){
            query.eq("username",queryParam.getUsername());
        }
        if(!StringUtils.isBlank(queryParam.getOperUser())){
            query.eq("oper_user",queryParam.getOperUser());
        }
        if(queryParam.getMoneyMin()!=null){
            query.ge("money",queryParam.getMoneyMin()*BigDecimalUtil.PLAYER_MONEY_UNIT);
        }
        if(queryParam.getMoneyMax()!=null){
            query.le("money",queryParam.getMoneyMax()*BigDecimalUtil.PLAYER_MONEY_UNIT);
        }
        query.orderByDesc("id");
        IPage<BallLoggerHandsup> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public SearchResponse<BallLoggerHandsup> searchFixed(BallLoggerHandsup queryParam, Integer pageNo, Integer pageSize) {
        SearchResponse<BallLoggerHandsup> response = new SearchResponse<>();
        Page<BallLoggerHandsup> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallLoggerHandsup> query = new QueryWrapper<>();
        query.select("sum(money) money,player_id");
        if(queryParam.getType()!=null){
            query.eq("type",queryParam.getType());
        }
        query.groupBy("player_id");
        IPage<BallLoggerHandsup> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public BallLoggerHandsup insert(BallLoggerHandsup loggerBet) {
        save(loggerBet);
        return loggerBet;
    }

    @Override
    public Long statisUpDown() {
        //统计加减款
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.select("sum(money) as money,type");
        queryWrapper.eq("account_type",2);
        queryWrapper.groupBy("type");
        List<BallLoggerHandsup> list = list(queryWrapper);
        Long up=0L;
        Long down = 0L;
        for(BallLoggerHandsup item:list){
            if(item.getType()==0){
                down = item.getMoney();
            }else{
                up = item.getMoney();
            }
        }
        return up-(-down);
    }

    @Override
    public List<BallLoggerHandsup> statis(ReportDataRequest reportDataRequest) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.select("type,sum(money) as money,count(id) as id,count(distinct player_id) as player_id");
        queryWrapper.eq("account_type",2);
        queryWrapper.groupBy("type");
        BallPlayerServiceImpl.queryByTime(queryWrapper,reportDataRequest.getTime(),reportDataRequest.getBegin(),reportDataRequest.getEnd());
        List<BallLoggerHandsup> list = list(queryWrapper);
        return list;
    }

    @Override
    public BallLoggerHandsup statisByType(ReportDataRequest reportDataRequest, int type) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.select("count(id) as id,count(distinct player_id) as player_id,sum(money) as money,type");
        queryWrapper.eq("account_type",2);
        queryWrapper.eq("type",type);
        BallPlayerServiceImpl.queryByTime(queryWrapper,reportDataRequest.getTime(),reportDataRequest.getBegin(),reportDataRequest.getEnd());
        List<BallLoggerHandsup> list = list(queryWrapper);
        if(list==null||list.isEmpty()||list.get(0)==null){
            return BallLoggerHandsup.builder()
                    .id(0L)
                    .playerId(0L)
                    .money(0L)
                    .build();
        }
        return list.get(0);
    }

    @Override
    public SearchResponse<BallLoggerHandsup> statisReport(ReportDataRequest reportDataRequest, int type, int pageNo, int pageSize) {
        SearchResponse<BallLoggerHandsup> response = new SearchResponse<>();
        Page<BallLoggerHandsup> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallLoggerHandsup> query = new QueryWrapper<>();
        query.eq("account_type",2);
        query.eq("type",type);
        BallPlayerServiceImpl.queryByTime(query,reportDataRequest.getTime(),reportDataRequest.getBegin(),reportDataRequest.getEnd());
        IPage<BallLoggerHandsup> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;

    }

    @Override
    public List<BallLoggerHandsup> statisPayCount(Long id, Long begin, Long end) {
        //查询指定账号今日返利
        QueryWrapper<BallLoggerHandsup> query = new QueryWrapper<>();
        query.eq("account_type",2);
        query.likeRight("super_tree",","+id+",");
        query.between("created_at",begin,end);
        query.groupBy("type");
        query.select("sum(money) as money,type");
        List<BallLoggerHandsup> list = list(query);
        return list;
    }

    @Override
    public BallLoggerHandsup statisRecharge(ReportDataRequest reportDataRequest) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.select("sum(money) as money,count(distinct player_id) as player_id,count(id) as id");
        queryWrapper.eq("type",1);
        queryWrapper.eq("account_type",2);
        BallPlayerServiceImpl.queryByTime(queryWrapper,reportDataRequest.getTime(),reportDataRequest.getBegin(),reportDataRequest.getEnd());
        BallLoggerHandsup query = getOne(queryWrapper);
        return query;
    }

    @Override
    public void search(BallProxyLogger queryParam, List<Long> ids, ProxyStatisDto list1, Map<Long, ProxyStatisDto> list2Map, BallPlayer proxyUser) {
        QueryWrapper<BallLoggerHandsup> query = new QueryWrapper<>();
        query.eq("type",1);
        if(!StringUtils.isEmpty(queryParam.getBegin())){
            try {
                query.gt("created_at",TimeUtil.stringToTimeStamp(queryParam.getBegin(),TimeUtil.TIME_YYYY_MM_DD));
            } catch (ParseException e) {
            }
        }
        if(!StringUtils.isEmpty(queryParam.getEnd())){
            try {
                query.lt("created_at",TimeUtil.stringToTimeStamp(queryParam.getBegin(),TimeUtil.TIME_YYYY_MM_DD)+TimeUtil.TIME_ONE_DAY);
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
//        query.in("player_id",ids);
        int pageNo = 1;
        while (true){
            Page<BallLoggerHandsup> page = new Page<>(pageNo++, 500);
            IPage<BallLoggerHandsup> pageResult = page(page, query);
            List<BallLoggerHandsup> records = pageResult.getRecords();
            if(records==null||records.isEmpty()||records.get(0)==null){
                break;
            }
            for(BallLoggerHandsup item:records){
                String superTree = item.getSuperTree();
                String[] split = superTree.split("_");
                List<String> strings = new ArrayList<>(Arrays.asList(split));
                strings.add(item.getPlayerId().toString());
                for(String sitem:strings){
                    if(StringUtils.isBlank(sitem)){
                        continue;
                    }
                    ProxyStatisDto proxyStatisDto = list2Map.get(Long.parseLong(sitem));
                    if(proxyStatisDto==null){
                        continue;
                    }
                    Set<Long> rechargePlayerSet = list1.getRechargePlayerSet();
                    rechargePlayerSet.add(item.getPlayerId());
                    list1.setRechargeMoney(list1.getRechargeMoney()+item.getMoney());

                    Set<Long> rechargePlayerSet1 = proxyStatisDto.getRechargePlayerSet();
                    rechargePlayerSet1.add(item.getPlayerId());
                    proxyStatisDto.setRechargeMoney(proxyStatisDto.getRechargeMoney()+item.getMoney());
                }
                if(item.getPlayerId().equals(proxyUser.getId())){
                    Set<Long> rechargePlayerSet = list1.getRechargePlayerSet();
                    rechargePlayerSet.add(item.getPlayerId());
                    list1.setRechargeMoney(list1.getRechargeMoney()+item.getMoney());
                }
            }
        }
    }

    @Override
    public void searchWithdrawal(BallProxyLogger queryParam, List<Long> ids, ProxyStatisDto statisDto1, Map<Long, ProxyStatisDto> list2Map, BallPlayer proxyUser) {
        QueryWrapper<BallLoggerHandsup> query = new QueryWrapper<>();
        query.eq("type",0);
        if(!StringUtils.isEmpty(queryParam.getBegin())){
            try {
                query.gt("created_at",TimeUtil.stringToTimeStamp(queryParam.getBegin(),TimeUtil.TIME_YYYY_MM_DD));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if(!StringUtils.isEmpty(queryParam.getEnd())){
            try {
                query.lt("created_at",TimeUtil.stringToTimeStamp(queryParam.getBegin(),TimeUtil.TIME_YYYY_MM_DD)+TimeUtil.TIME_ONE_DAY);
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
//        query.in("player_id",ids);
        int pageNo = 1;
        while (true){
            Page<BallLoggerHandsup> page = new Page<>(pageNo++, 500);
            IPage<BallLoggerHandsup> pageResult = page(page, query);
            List<BallLoggerHandsup> records = pageResult.getRecords();
            if(records==null||records.isEmpty()||records.get(0)==null){
                break;
            }
            for(BallLoggerHandsup item:records){


                String superTree = item.getSuperTree();
                String[] split = superTree.split("_");
                List<String> strings = new ArrayList<>(Arrays.asList(split));
                strings.add(item.getPlayerId().toString());
                for(String sitem:strings){
                    if(StringUtils.isBlank(sitem)){
                        continue;
                    }
                    ProxyStatisDto proxyStatisDto = list2Map.get(Long.parseLong(sitem));
                    if(proxyStatisDto==null){
                        continue;
                    }
                    Set<Long> withdrawalPlayerSet = statisDto1.getWithdrawalPlayerSet();
                    withdrawalPlayerSet.add(item.getPlayerId());
                    statisDto1.setWithdrawalMoney(statisDto1.getWithdrawalMoney()+item.getMoney());

                    Set<Long> withdrawalPlayerSet1 = proxyStatisDto.getWithdrawalPlayerSet();
                    withdrawalPlayerSet1.add(item.getPlayerId());
                    proxyStatisDto.setWithdrawalMoney(proxyStatisDto.getWithdrawalMoney()+item.getMoney());
                }
                if(item.getPlayerId().equals(proxyUser.getId())){
                    Set<Long> withdrawalPlayerSet = statisDto1.getWithdrawalPlayerSet();
                    withdrawalPlayerSet.add(item.getPlayerId());
                    statisDto1.setWithdrawalMoney(statisDto1.getWithdrawalMoney()+item.getMoney());
                }
            }
        }
    }
}
