package com.oxo.ball.service.impl.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxo.ball.bean.dao.*;
import com.oxo.ball.bean.dto.req.admin.QueryActivePlayerRequest;
import com.oxo.ball.bean.dto.req.player.WithdrawalLogRequest;
import com.oxo.ball.bean.dto.req.report.ReportDataRequest;
import com.oxo.ball.bean.dto.req.report.ReportRewiRequest;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.bean.dto.resp.admin.ProxyStatis2Dto;
import com.oxo.ball.bean.dto.resp.admin.ProxyStatis3Dto;
import com.oxo.ball.bean.dto.resp.admin.ProxyStatisDto;
import com.oxo.ball.bean.dto.resp.report.RechargeWithdrawalResponse;
import com.oxo.ball.contant.RedisKeyContant;
import com.oxo.ball.mapper.BallLoggerWithdrawalMapper;
import com.oxo.ball.service.IBasePlayerService;
import com.oxo.ball.service.admin.*;
import com.oxo.ball.utils.RedisUtil;
import com.oxo.ball.utils.ResponseMessageUtil;
import com.oxo.ball.utils.TimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.*;

/**
 * <p>
 * 提现日志表 服务实现类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Service
public class BallLoggerWithdrawalServiceImpl extends ServiceImpl<BallLoggerWithdrawalMapper, BallLoggerWithdrawal> implements IBallLoggerWithdrawalService {

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    IBasePlayerService basePlayerService;
    @Autowired
    private BallBalanceChangeServiceImpl ballBalanceChangeService;
    @Autowired
    IBallLoggerHandsupService loggerHandsupService;
    @Autowired
    BallLoggerWithdrawalMapper mapper;
    @Autowired
    BallAdminService adminService;
    @Autowired
    IApiService apiService;
    @Autowired
    IBallSystemConfigService systemConfigService;
    @Autowired
    IBallPayBehalfService payBehalfService;
    @Autowired
    IBallBankCardService bankCardService;
    @Override
    public SearchResponse<BallLoggerWithdrawal> search(BallLoggerWithdrawal queryParam, Integer pageNo, Integer pageSize) {
        SearchResponse<BallLoggerWithdrawal> response = new SearchResponse<>();
        Page<BallLoggerWithdrawal> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallLoggerWithdrawal> query = new QueryWrapper<>();
        BallPlayer playerTree = null;

        if(queryParam.getStatus()!=null){
            query.eq("status",queryParam.getStatus());
        }
        if(queryParam.getType()!=null){
            query.eq("type",queryParam.getType());
        }
        if(!StringUtils.isBlank(queryParam.getBehalfNo())){
            query.eq("behalf_no",queryParam.getBehalfNo());
        }
        if(queryParam.getOrderNo()!=null){
            query.eq("order_no",queryParam.getOrderNo());
        }
        if(queryParam.getAccountType()!=null){
            query.eq("account_type",queryParam.getAccountType());
        }
//        if(queryParam.getPlayerId()!=null){
//            BallPlayer byId = basePlayerService.findOne(queryParam.getPlayerId());
//            if(byId!=null){
//                query.eq("player_id",byId.getId());
//            }else{
//                query.eq("player_id",queryParam.getPlayerId());
//            }
//        }
        if(queryParam.getPlayerId()!=null){
            if(queryParam.getTreeType()==null){
                query.eq("player_id",queryParam.getPlayerId());
            }else{
                playerTree = basePlayerService.findByUserId(queryParam.getPlayerId());
            }
        }
        if(!StringUtils.isBlank(queryParam.getPlayerName())){
            if(queryParam.getTreeType()==null) {
                query.eq("player_name",queryParam.getPlayerName());
            }else{
                playerTree = basePlayerService.findByUsername(queryParam.getPlayerName());
            }
        }
        if(queryParam.getTreeType()!=null){
            if(playerTree!=null){
                //要查直属下级,或者全部下级
                if(queryParam.getTreeType()==1){
                    //查直属
                    query.eq("super_tree",playerTree.getSuperTree()+playerTree.getId()+"_");
                }else if(queryParam.getTreeType()==2){
                    //查全部下级
                    query.likeRight("super_tree",playerTree.getSuperTree()+playerTree.getId()+"\\_");
                }
            }else{
                if (queryParam.getPlayerId() != null) {
                    query.eq("player_id", queryParam.getPlayerId());
                }
                if (!StringUtils.isBlank(queryParam.getPlayerName())) {
                    query.eq("player_name", queryParam.getPlayerName());
                }
            }
        }else {
            if (queryParam.getPlayerId() != null) {
                query.eq("player_id", queryParam.getPlayerId());
            }
            if (!StringUtils.isBlank(queryParam.getPlayerName())) {
                query.eq("player_name", queryParam.getPlayerName());
            }
        }
        query.orderByDesc("id");
        IPage<BallLoggerWithdrawal> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        if(!response.getResults().isEmpty()){
            BallLoggerWithdrawal sum = BallLoggerWithdrawal.builder()
                    .playerName("小计")
                    .money(0L)
                    .commission(0L)
                    .usdtMoney(0L)
                    .build();
            for(BallLoggerWithdrawal item:response.getResults()){
                sum.setMoney(sum.getMoney()+item.getMoney());
                sum.setCommission(sum.getCommission()+item.getCommission());
                sum.setUsdtMoney(sum.getUsdtMoney()+item.getUsdtMoney());
            }
            response.getResults().add(sum);
            response.getResults().add(0,sum);
        }
        return response;
    }

    @Override
    public SearchResponse<BallLoggerWithdrawal> search2(BallLoggerWithdrawal queryParam, Integer pageNo, Integer pageSize) {
        SearchResponse<BallLoggerWithdrawal> response = new SearchResponse<>();
        Page<BallLoggerWithdrawal> page = new Page<>(pageNo, pageSize);
        Map<String,Object> query = new HashMap<>();
        BallPlayer playerTree = null;
        if(queryParam.getStatus()!=null){
            query.put("status",queryParam.getStatus());
        }
        if(queryParam.getType()!=null){
            query.put("type",queryParam.getType());
        }
        if(!StringUtils.isBlank(queryParam.getBehalfNo())){
            query.put("behalf_no",queryParam.getBehalfNo());
        }
        if(queryParam.getOrderNo()!=null){
            query.put("order_no",queryParam.getOrderNo());
        }
        if(queryParam.getAccountType()!=null){
            query.put("account_type",queryParam.getAccountType());
        }
        if(!StringUtils.isBlank(queryParam.getAutoCheckTime())){
            Double aDouble = Double.valueOf(queryParam.getAutoCheckTime());
            Double time = aDouble*TimeUtil.TIME_ONE_HOUR;
            query.put("created_at",System.currentTimeMillis()-time.longValue());
        }
        if(queryParam.getPlayerId()!=null){
            if(queryParam.getTreeType()==null){
                query.put("player_id",queryParam.getPlayerId());
            }else{
                playerTree = basePlayerService.findByUserId(queryParam.getPlayerId());
            }
        }
        if(!StringUtils.isBlank(queryParam.getPlayerName())){
            if(queryParam.getTreeType()==null) {
                query.put("player_name",queryParam.getPlayerName());
            }else{
                playerTree = basePlayerService.findByUsername(queryParam.getPlayerName());
            }
        }
        if(queryParam.getTreeType()!=null){
            if(playerTree!=null){
                //要查直属下级,或者全部下级
                if(queryParam.getTreeType()==1){
                    //查直属
                    query.put("super_tree",playerTree.getSuperTree()+playerTree.getId()+"_");
                    query.put("treeType",1);
                }else if(queryParam.getTreeType()==2){
                    //查全部下级
                    query.put("super_tree",playerTree.getSuperTree()+playerTree.getId()+"\\_%");
                    query.put("treeType",2);
                }
            }else{
                if (queryParam.getPlayerId() != null) {
                    query.put("player_id", queryParam.getPlayerId());
                }
                if (!StringUtils.isBlank(queryParam.getPlayerName())) {
                    query.put("player_name", queryParam.getPlayerName());
                }
            }
        }else {
            if (queryParam.getPlayerId() != null) {
                query.put("player_id", queryParam.getPlayerId());
            }
            if (!StringUtils.isBlank(queryParam.getPlayerName())) {
                query.put("player_name", queryParam.getPlayerName());
            }
        }
        IPage<BallLoggerWithdrawal> pages = mapper.listPage(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        if(!response.getResults().isEmpty()){
            BallLoggerWithdrawal sum = BallLoggerWithdrawal.builder()
                    .playerName("小计")
                    .money(0L)
                    .commission(0L)
                    .usdtMoney(0L)
                    .topUpTimes(0)
                    .cumulativeTopUp(0L)
                    .artificialAdd(0L)
                    .cumulativeReflect(0L)
                    .build();
            Set<Long> players = new HashSet<>();
            for(BallLoggerWithdrawal item:response.getResults()){
                sum.setMoney(sum.getMoney()+item.getMoney());
                sum.setCommission(sum.getCommission()+item.getCommission());
                sum.setUsdtMoney(sum.getUsdtMoney()+item.getUsdtMoney());
                if(players.contains(item.getPlayerId())){
                    continue;
                }
                players.add(item.getPlayerId());
                sum.setTopUpTimes(sum.getTopUpTimes()+item.getTopUpTimes());
                sum.setCumulativeTopUp(sum.getCumulativeTopUp()+item.getCumulativeTopUp());
                sum.setArtificialAdd(sum.getArtificialAdd()+item.getArtificialAdd());
                sum.setCumulativeReflect(sum.getCumulativeReflect()+item.getCumulativeReflect());
            }
            response.getResults().add(sum);
            response.getResults().add(0,sum);
        }
        return response;
    }

    @Override
    public SearchResponse<BallLoggerWithdrawal> search(BallPlayer currPlayer, WithdrawalLogRequest queryParam, Integer pageNo, Integer pageSize) {
        SearchResponse<BallLoggerWithdrawal> response = new SearchResponse<>();
        Page<BallLoggerWithdrawal> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallLoggerWithdrawal> query = new QueryWrapper<>();
        if(queryParam.getStatus()!=null){
            query.eq("status",queryParam.getStatus());
        }
        query.eq("player_id",currPlayer.getId());
        searchCaseOnTime(query, queryParam.getTime());
        query.orderByDesc("id");
        IPage<BallLoggerWithdrawal> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public BallLoggerWithdrawal findById(Long id) {
        return getById(id);
    }

    static void searchCaseOnTime(QueryWrapper<BallLoggerWithdrawal> query, Integer time) {
        if(time !=null){
            switch (time){
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

    @Override
    public BallLoggerWithdrawal insert(BallLoggerWithdrawal loggerBet) {
        save(loggerBet);
        return loggerBet;
    }

    @Override
    public Boolean edit(BallLoggerWithdrawal loggerWithdrawal) {
        return updateById(loggerWithdrawal);
    }

    @Override
    public Long getDayOrderNo() {
        if (redisUtil.get(RedisKeyContant.WITHDRAWAL_ORDER_NO) == null) {
            redisUtil.set(RedisKeyContant.WITHDRAWAL_ORDER_NO, 1);
        }
        long incr = redisUtil.incr(RedisKeyContant.WITHDRAWAL_ORDER_NO, 1);
        return incr;
    }

    @Override
    public Integer todayCount(Long playerId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.select("count(1) as type");
        queryWrapper.eq("player_id",playerId);
        queryWrapper.between("created_at",TimeUtil.getDayBegin().getTime(),TimeUtil.getDayEnd().getTime());
        List<BallLoggerWithdrawal> list = list(queryWrapper);
        if(list==null||list.isEmpty()){
            return 0;
        }
        return list.get(0).getType();
    }

    @Override
    public synchronized BaseResponse check(BallLoggerWithdrawal query, BallAdmin admin) {
        BallLoggerWithdrawal withdrawal = findById(query.getId());
        //提交的修改状态和库中状态一样,直接返回已操作~
        if(withdrawal.getStatus().equals(query.getStatus())){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e29"));
        }
        BallLoggerWithdrawal edit = BallLoggerWithdrawal.builder()
                .id(query.getId())
                .status(query.getStatus())
                .build();
        if(!StringUtils.isBlank(query.getRemark())){
            edit.setRemark(query.getRemark());
        }
        //审核通过,和审核拒绝
        if(query.getStatus()==2){
            //审核通过
            edit.setChecker(admin.getUsername());
        }
        if(query.getStatus()==3){
            //审核失败
            edit.setChecker(admin.getUsername());
            edit.setOker(admin.getUsername());
            edit.setUpdatedAt(System.currentTimeMillis());
        }
        if(query.getStatus()==4){
            edit.setOker(admin.getUsername());
            edit.setUpdatedAt(System.currentTimeMillis());
        }
        Boolean succ = edit(edit);
        if(succ){
            if(query.getStatus()==3){
                //12,提现退回
                //自动发放
                BallPlayer player = basePlayerService.findById(withdrawal.getPlayerId());
                while(true) {
                    long frozen = player.getFrozenWithdrawal()-withdrawal.getMoney();
                    BallPlayer editPlayer = BallPlayer.builder()
                            .balance(player.getBalance() + withdrawal.getMoney())
                            .frozenWithdrawal(frozen<0?0:frozen)
                            .version(player.getVersion())
                            .build();
                    editPlayer.setId(player.getId());
                    boolean b = basePlayerService.editAndClearCache(editPlayer, player);
                    if (b) {
                        ballBalanceChangeService.insert(BallBalanceChange.builder()
                                .playerId(player.getId())
                                .accountType(player.getAccountType())
                                .userId(player.getUserId())
                                .parentId(player.getSuperiorId())
                                .username(player.getUsername())
                                .superTree(player.getSuperTree())
                                .balanceChangeType(12)
                                .createdAt(System.currentTimeMillis())
                                .dnedMoney(editPlayer.getBalance())
                                .initMoney(player.getBalance())
                                .changeMoney(withdrawal.getMoney())
                                .orderNo(withdrawal.getOrderNo())
                                .build());
                        break;
                    } else {
                        player = basePlayerService.findById(player.getId());
                    }
                }
            }else if(withdrawal.getStatus()==2){
                //提现成功,对应账变为真实账变,账号累计数据累加
                BallPlayer player = basePlayerService.findById(withdrawal.getPlayerId());
                BallPayBehalfServiceImpl.onWithdrawalSuccess(withdrawal, player, basePlayerService, ballBalanceChangeService,adminService,apiService, this,bankCardService);
            }
        }
        return succ?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("failed!");
    }

    @Override
    public Long statisTotal() {
        //统计加减款
        //统计充值
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.select("sum(money) as money");
        queryWrapper.eq("account_type",2);
        queryWrapper.eq("status",4);
        List<BallLoggerWithdrawal> list = list(queryWrapper);
        if(list!=null&&!list.isEmpty()&&list.get(0)!=null){
            return list.get(0).getMoney();
        }
        return 0L;
    }

    @Override
    public BallLoggerWithdrawal statisTotal(ReportDataRequest reportDataRequest) {
//        提现人数2
//                提现笔数2
//        提现金额0
//                提现手续费0
//        充提差[非人工]0
//        充提差
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.select("sum(money) as money,sum(commission) as commission,count(distinct player_id) as player_id,count(id) as id");
        //只统计正式号
        queryWrapper.eq("account_type",2);
        //只统计提款成功
        queryWrapper.eq("status", 4);
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        queryByTimeConf(queryWrapper,reportDataRequest.getTime(),reportDataRequest.getBegin(),reportDataRequest.getEnd(),systemConfig);
        BallPlayer player=null;
        if(reportDataRequest.getUserId()!=null){
            player = basePlayerService.findByUserId(reportDataRequest.getUserId());
        }else if(!StringUtils.isBlank(reportDataRequest.getUsername())){
            player = basePlayerService.findByUsername(reportDataRequest.getUsername());
        }
        if(player!=null){
            queryWrapper.likeRight("super_tree",player.getSuperTree()+player.getId()+"\\_");
        }
        List<BallLoggerWithdrawal> list = list(queryWrapper);

        List<BallLoggerHandsup> handsup = loggerHandsupService.statis(reportDataRequest);

        BallLoggerWithdrawal ballLoggerRecharge = BallLoggerWithdrawal.builder()
                .id(0L)
                .playerId(0L)
                .commission(0L)
                .money(0L)
                .build();
        if(list!=null&&!list.isEmpty()&&list.get(0)!=null){
            BallLoggerWithdrawal withdrawal = list.get(0);
            //人数
            ballLoggerRecharge.setPlayerId(withdrawal.getPlayerId());
            //笔数
            ballLoggerRecharge.setId(withdrawal.getId());
            //手续费
            ballLoggerRecharge.setCommission(withdrawal.getCommission());
            //金额
            ballLoggerRecharge.setMoney(withdrawal.getMoney());
        }
        // 0下 1上
        for(BallLoggerHandsup item:handsup){
            if(item.getType()==0){
                //人工提现金额，去掉
//                ballLoggerRecharge.setOrderNo(item.getMoney()==null?0:item.getMoney());
                //+人工人数.去掉
//                ballLoggerRecharge.setPlayerId(ballLoggerRecharge.getPlayerId()+(item.getPlayerId()==null?0:item.getPlayerId()));
                //+人工笔数,去掉
//                ballLoggerRecharge.setId(ballLoggerRecharge.getId()+(item.getId()==null?0:item.getId()));
            }else{
                ballLoggerRecharge.setCreatedAt(item.getMoney()==null?0:item.getMoney());
            }
        }
        return ballLoggerRecharge;
    }

    @Override
    public List<Map<String, Object>> statisByType(ReportDataRequest reportDataRequest) {
        List<Map<String,Object>> listData = new ArrayList<>();
        Map<String,Object> data = new HashMap<>();
        data.put("ron",0); //线上
        data.put("roff",0);//线下
        data.put("rha",0); //人工

        data.put("pon",0);//线上
        data.put("poff",0);//线下
        data.put("pha",0);//人工

        data.put("ton",0);//线上
        data.put("toff",0);//线下
        data.put("tha",0);//人工

        Long totalR = 0L;
        Long totalP = 0L;
        Long totalT = 0L;
        //非人工
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.select("count(id) as id,count(distinct player_id) as player_id,sum(money) as money,type");
        queryWrapper.eq("account_type",2);
        queryWrapper.eq("status",4);
        queryWrapper.groupBy("type");
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        queryByTimeConf(queryWrapper,reportDataRequest.getTime(),reportDataRequest.getBegin(),reportDataRequest.getEnd(),systemConfig);
        BallPlayer player=null;
        if(reportDataRequest.getUserId()!=null){
            player = basePlayerService.findByUserId(reportDataRequest.getUserId());
        }else if(!StringUtils.isBlank(reportDataRequest.getUsername())){
            player = basePlayerService.findByUsername(reportDataRequest.getUsername());
        }
        if(player!=null){
            queryWrapper.likeRight("super_tree",player.getSuperTree()+player.getId()+"\\_");
        }
        List<BallLoggerWithdrawal> list = list(queryWrapper);
        for(BallLoggerWithdrawal item:list){
            totalR+=item.getId()==null?0:item.getId();
            totalP+=item.getPlayerId()==null?0:item.getPlayerId();
            totalT+=item.getMoney()==null?0:item.getMoney();
            if(item.getType()==1){
//                银行
                data.put("roff",item.getId());
                data.put("poff",item.getPlayerId());
                data.put("toff",item.getMoney());
            }else{
//              USDT
                data.put("ron",item.getId());
                data.put("pon",item.getPlayerId());
                data.put("ton",item.getMoney());
            }
        }
        //人工
        BallLoggerHandsup loggerHandsup = loggerHandsupService.statisByType(reportDataRequest,0);
        totalR+=loggerHandsup.getId()==null?0:loggerHandsup.getId();
        totalP+=loggerHandsup.getPlayerId()==null?0:loggerHandsup.getPlayerId();
        totalT+=(loggerHandsup.getMoney()==null?0L:loggerHandsup.getMoney());
        data.put("rha",loggerHandsup.getId());
        data.put("pha",loggerHandsup.getPlayerId());
        data.put("tha",loggerHandsup.getMoney());
        listData.add(data);
        Map<String,Object> data1 = new HashMap<>();
        data1.put("ron",totalR);
        data1.put("pon",totalP);
        data1.put("ton",totalT);
        listData.add(data1);
        return listData;
    }

    @Override
    public SearchResponse<BallLoggerWithdrawal> statisReport(ReportDataRequest reportDataRequest, int pageNo, int pageSize) {
        SearchResponse<BallLoggerWithdrawal> response = new SearchResponse<>();
        Page<BallLoggerWithdrawal> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallLoggerWithdrawal> query = new QueryWrapper<>();
        query.eq("account_type",2);
        query.eq("status",5);
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        queryByTimeConf(query,reportDataRequest.getTime(),reportDataRequest.getBegin(),reportDataRequest.getEnd(),systemConfig);
        IPage<BallLoggerWithdrawal> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public List<BallLoggerWithdrawal> statisPayCount(Long id, Long begin, Long end) {
        QueryWrapper<BallLoggerWithdrawal> query = new QueryWrapper<>();
        query.eq("account_type",2);
        query.likeRight("super_tree","\\_"+id+"\\_");
        query.select("sum(money) money,type");
        query.groupBy("type");
        return list(query);
    }

    @Override
    public BallLoggerWithdrawal findByOrderNo(String order_no) {
        QueryWrapper<BallLoggerWithdrawal> query = new QueryWrapper<>();
        query.eq("order_no",order_no);
        return getOne(query);
    }

    @Override
    public Long statisTotalNot() {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.select("sum(money) as money");
        queryWrapper.eq("account_type",2);
        queryWrapper.in("status",1,2,5);
        List<BallLoggerWithdrawal> list = list(queryWrapper);
        if(list!=null&&!list.isEmpty()&&list.get(0)!=null){
            return list.get(0).getMoney();
        }
        return 0L;
    }

    @Override
    public List<BallLoggerWithdrawal> statisTotalNotForPlayer() {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.select("sum(money) as money,player_id");
        queryWrapper.eq("account_type",2);
        queryWrapper.in("status",1,2,5);
        queryWrapper.groupBy("player_id");
        List<BallLoggerWithdrawal> list = list(queryWrapper);
        return list;
    }

    @Override
    public void search(BallProxyLogger queryParam, List<Long> ids, ProxyStatisDto list1, Map<Long, ProxyStatisDto> list2Map,BallPlayer proxyUser,BallSystemConfig systemConfig) {
        QueryWrapper<BallLoggerWithdrawal> query = new QueryWrapper<>();
        //提现成功的
        query.eq("status",4);
        query.eq("account_type",2);
        String queryTime = systemConfig.getStatisTime()==0?"created_at":"behalf_time";
        if(!StringUtils.isEmpty(queryParam.getBegin())){
            try {
                query.gt(queryTime,TimeUtil.stringToTimeStamp(queryParam.getBegin(),TimeUtil.TIME_YYYY_MM_DD));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if(!StringUtils.isEmpty(queryParam.getEnd())){
            try {
                query.lt(queryTime,TimeUtil.stringToTimeStamp(queryParam.getEnd(),TimeUtil.TIME_YYYY_MM_DD)+TimeUtil.TIME_ONE_DAY);
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
            Page<BallLoggerWithdrawal> page = new Page<>(pageNo++, 500);
            IPage<BallLoggerWithdrawal> pageResult = page(page, query);
            List<BallLoggerWithdrawal> records = pageResult.getRecords();
            if(records==null||records.isEmpty()||records.get(0)==null){
                break;
            }
            for(BallLoggerWithdrawal item:records){
                if(queryParam.getProxyLine()==0){
                    ProxyStatisDto proxyStatisDto = list2Map.get(item.getPlayerId());
                    if(proxyStatisDto!=null){
                        Set<Long> withdrawalPlayerSet = list1.getWithdrawalPlayerSet();
                        withdrawalPlayerSet.add(item.getPlayerId());
                        list1.setWithdrawalMoney(list1.getWithdrawalMoney()+item.getMoney());

                        Set<Long> withdrawalPlayerSet1 = proxyStatisDto.getWithdrawalPlayerSet();
                        withdrawalPlayerSet1.add(item.getPlayerId());
                        proxyStatisDto.setWithdrawalMoney(proxyStatisDto.getWithdrawalMoney()+item.getMoney());
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
                            added=true;
                            Set<Long> withdrawalPlayerSet = list1.getWithdrawalPlayerSet();
                            withdrawalPlayerSet.add(item.getPlayerId());
                            list1.setWithdrawalMoney(list1.getWithdrawalMoney()+item.getMoney());
                        }
                        Set<Long> withdrawalPlayerSet = proxyStatisDto.getWithdrawalPlayerSet();
                        withdrawalPlayerSet.add(item.getPlayerId());
                        proxyStatisDto.setWithdrawalMoney(proxyStatisDto.getWithdrawalMoney()+item.getMoney());
                    }
                }
                if(item.getPlayerId().equals(proxyUser.getId())){
                    Set<Long> withdrawalPlayerSet = list1.getWithdrawalPlayerSet();
                    withdrawalPlayerSet.add(item.getPlayerId());
                    list1.setWithdrawalMoney(list1.getWithdrawalMoney()+item.getMoney());
                }
            }
        }
    }

    @Override
    public BallLoggerWithdrawal searchStatis(BallLoggerWithdrawal queryParam) {
        Map<String,Object> query = new HashMap<>();
        BallPlayer playerTree = null;
        if(queryParam.getStatus()!=null){
            query.put("status",queryParam.getStatus());
        }
        if(queryParam.getType()!=null){
            query.put("type",queryParam.getType());
        }
        if(!StringUtils.isBlank(queryParam.getBehalfNo())){
            query.put("behalf_no",queryParam.getBehalfNo());
        }
        if(queryParam.getOrderNo()!=null){
            query.put("order_no",queryParam.getOrderNo());
        }
        if(queryParam.getAccountType()!=null){
            query.put("account_type",queryParam.getAccountType());
        }
        if(queryParam.getPlayerId()!=null){
            if(queryParam.getTreeType()==null){
                query.put("player_id",queryParam.getPlayerId());
            }else{
                playerTree = basePlayerService.findByUserId(queryParam.getPlayerId());
            }
        }
        if(!StringUtils.isBlank(queryParam.getPlayerName())){
            if(queryParam.getTreeType()==null) {
                query.put("player_name",queryParam.getPlayerName());
            }else{
                playerTree = basePlayerService.findByUsername(queryParam.getPlayerName());
            }
        }
        if(queryParam.getTreeType()!=null){
            if(playerTree!=null){
                //要查直属下级,或者全部下级
                if(queryParam.getTreeType()==1){
                    //查直属
                    query.put("super_tree",playerTree.getSuperTree()+playerTree.getId()+"_");
                    query.put("treeType",1);
                }else if(queryParam.getTreeType()==2){
                    //查全部下级
                    query.put("super_tree",playerTree.getSuperTree()+playerTree.getId()+"\\_%");
                    query.put("treeType",2);
                }
            }else{
                if (queryParam.getPlayerId() != null) {
                    query.put("player_id", queryParam.getPlayerId());
                }
                if (!StringUtils.isBlank(queryParam.getPlayerName())) {
                    query.put("player_name", queryParam.getPlayerName());
                }
            }
        }else {
            if (queryParam.getPlayerId() != null) {
                query.put("player_id", queryParam.getPlayerId());
            }
            if (!StringUtils.isBlank(queryParam.getPlayerName())) {
                query.put("player_name", queryParam.getPlayerName());
            }
        }
        BallLoggerWithdrawal one = mapper.listPageStatis(query);
        return one;
//        QueryWrapper<BallLoggerWithdrawal> query = new QueryWrapper<>();
//        BallPlayer playerTree = null;
//        if(queryParam.getStatus()!=null){
//            query.eq("status",queryParam.getStatus());
//        }
//        if(queryParam.getType()!=null){
//            query.eq("type",queryParam.getType());
//        }
//        if(!StringUtils.isBlank(queryParam.getBehalfNo())){
//            query.eq("behalf_no",queryParam.getBehalfNo());
//        }
//        if(queryParam.getOrderNo()!=null){
//            query.eq("order_no",queryParam.getOrderNo());
//        }
//        if(queryParam.getAccountType()!=null){
//            query.eq("account_type",queryParam.getAccountType());
//        }
//        if(queryParam.getPlayerId()!=null){
//            if(queryParam.getTreeType()==null){
//                query.eq("player_id",queryParam.getPlayerId());
//            }else{
//                playerTree = basePlayerService.findByUserId(queryParam.getPlayerId());
//            }
//        }
//        if(!StringUtils.isBlank(queryParam.getPlayerName())){
//            if(queryParam.getTreeType()==null) {
//                query.eq("player_name",queryParam.getPlayerName());
//            }else{
//                playerTree = basePlayerService.findByUsername(queryParam.getPlayerName());
//            }
//        }
//        if(queryParam.getTreeType()!=null){
//            if(playerTree!=null){
//                //要查直属下级,或者全部下级
//                if(queryParam.getTreeType()==1){
//                    //查直属
//                    query.eq("super_tree",playerTree.getSuperTree()+playerTree.getId()+"_");
//                }else if(queryParam.getTreeType()==2){
//                    //查全部下级
//                    query.likeRight("super_tree",playerTree.getSuperTree()+playerTree.getId()+"\\_");
//                }
//            }else{
//                if (queryParam.getPlayerId() != null) {
//                    query.eq("player_id", queryParam.getPlayerId());
//                }
//                if (!StringUtils.isBlank(queryParam.getPlayerName())) {
//                    query.eq("player_name", queryParam.getPlayerName());
//                }
//            }
//        } else {
//            if (queryParam.getPlayerId() != null) {
//                query.eq("player_id", queryParam.getPlayerId());
//            }
//            if (!StringUtils.isBlank(queryParam.getPlayerName())) {
//                query.eq("player_name", queryParam.getPlayerName());
//            }
//        }
//        query.select("sum(money) money,sum(commission) commission,sum(usdt_money) usdt_money");
//        BallLoggerWithdrawal one = getOne(query);
//        return one;
    }

    @Override
    public List<RechargeWithdrawalResponse> rechargeStatisByPayTypeList(ReportRewiRequest reportRewiRequest) {
        SearchResponse<RechargeWithdrawalResponse> response = new SearchResponse<>();
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("account_type",2);
        queryWrapper.eq("status",4);
        if(reportRewiRequest.getBehalfId()!=null){
            queryWrapper.eq("behalf_id",reportRewiRequest.getBehalfId());
        }
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        queryByTimeConf(queryWrapper,reportRewiRequest.getTime(),reportRewiRequest.getBegin(),reportRewiRequest.getEnd(),systemConfig);
        Map<String,RechargeWithdrawalResponse> statisMap = new HashMap<>();
        RechargeWithdrawalResponse statisTotal = RechargeWithdrawalResponse.builder()
                .ymd("合计")
                .payName("")
                .resuccCount(0)
                .resuccMoney(0L)
                .resuccPlayer(new HashSet<>())
//                .refailCount(0)
//                .refailMoney(0L)
//                .refailPlayer(new HashSet<>())
//                .wifailCount(0)
//                .wifailMoney(0L)
//                .wifailPlayer(new HashSet<>())
                .wisuccCount(0)
                .wisuccMoney(0L)
                .wisuccPlayer(new HashSet<>())
//                .wisuccPer("")
                .build();
        int pageNo=1;
        int pageSize = 500;
        List<BallPayBehalf> byAll = payBehalfService.findByAllTrue();
        Map<Long,BallPayBehalf> ballPayBehalfMap = new HashMap<>();
        for(BallPayBehalf item:byAll){
            ballPayBehalfMap.put(item.getId(),item);
        }
        while (true){
            Page<BallLoggerWithdrawal> page = new Page<>(pageNo++, pageSize);
            IPage<BallLoggerWithdrawal> list = page(page,queryWrapper);
//        统计开始
            List<BallLoggerWithdrawal> records = list.getRecords();
            if(records==null||records.isEmpty()||records.get(0)==null){
                break;
            }
            for(BallLoggerWithdrawal item:records){
                String ymd = TimeUtil.dateFormat(new Date(item.getCreatedAt()), TimeUtil.TIME_YYYY_MM_DD);
                String key = ymd+(item.getBehalfId()==null?item.getType():item.getBehalfId());
                RechargeWithdrawalResponse loggerRecharge = statisMap.get(key);
                if(loggerRecharge==null){
                    BallPayBehalf behalf = ballPayBehalfMap.get(item.getBehalfId());
                    loggerRecharge = RechargeWithdrawalResponse.builder()
                            .ymd(ymd)
                            .payName(behalf==null?BallLoggerWithdrawal.getTypeString(item.getType()):behalf.getName())
                            .resuccCount(0)
                            .resuccMoney(0L)
                            .resuccPlayer(new HashSet<>())
//                        .refailCount(0)
//                        .refailMoney(0L)
//                        .refailPlayer(new HashSet<>())
//                        .resuccPer("0")
//                        .wifailCount(0)
//                        .wifailMoney(0L)
//                        .wifailPlayer(new HashSet<>())
                            .wisuccCount(0)
                            .wisuccMoney(0L)
                            .wisuccPlayer(new HashSet<>())
//                        .wisuccPer("")
                            .build();
                    statisMap.put(key,loggerRecharge);
                }
                if(item.getStatus()==4){
                    loggerRecharge.setWisuccCount(loggerRecharge.getWisuccCount()+1);
                    loggerRecharge.getWisuccPlayer().add(item.getPlayerId());
                    loggerRecharge.setWisuccMoney(loggerRecharge.getWisuccMoney()+item.getMoney());

                    statisTotal.setWisuccCount(statisTotal.getWisuccCount()+1);
                    statisTotal.getWisuccPlayer().add(item.getPlayerId());
                    statisTotal.setWisuccMoney(statisTotal.getWisuccMoney()+item.getMoney());
                }else if(item.getStatus()==3){
//                loggerRecharge.setWifailCount(loggerRecharge.getWifailCount()+1);
//                loggerRecharge.getWifailPlayer().add(item.getPlayerId());
//                loggerRecharge.setWifailMoney(loggerRecharge.getWifailMoney()+item.getMoney());
//
//                statisTotal.setWifailCount(statisTotal.getWifailCount()+1);
//                statisTotal.getWifailPlayer().add(item.getPlayerId());
//                statisTotal.setWifailMoney(statisTotal.getWifailMoney()+item.getMoney());
                }
            }
        }



        Collection<RechargeWithdrawalResponse> values = statisMap.values();
        List<RechargeWithdrawalResponse> res = new ArrayList<>(values);
        if(values!=null){
            res.add(statisTotal);
        }
//        response.setPageNo(list.getCurrent());
//        response.setPageSize(list.getSize());
//        response.setTotalCount(list.getTotal());
//        response.setTotalPage(list.getPages());
//        response.setResults(res);
        return res;
    }
//1665685800000 1665772200000 1668882600000 1668968999832
    private void queryByTimeConf(QueryWrapper query, Integer time, String begin, String end, BallSystemConfig systemConfig) {
        String queryTime = systemConfig.getStatisTime()==0?"created_at":"behalf_time";
        switch (time){
            case 0:
                query.ge(queryTime, TimeUtil.getDayBegin().getTime());
                query.le(queryTime, TimeUtil.getDayEnd().getTime());
                break;
            case 1:
                query.ge(queryTime, TimeUtil.getBeginDayOfYesterday().getTime());
                query.le(queryTime, TimeUtil.getEndDayOfYesterday().getTime());
                break;
            case 2:
                query.ge(queryTime, TimeUtil.getDayBegin().getTime()-3*TimeUtil.TIME_ONE_DAY);
                query.le(queryTime, TimeUtil.getDayEnd().getTime());
                break;
            case 3:
                query.ge(queryTime, TimeUtil.getBeginDayOfWeek().getTime());
                query.le(queryTime, TimeUtil.getDayEnd().getTime());
                break;
            case 4:
                query.ge(queryTime, TimeUtil.getBeginDayOfLastWeek().getTime());
                query.le(queryTime, TimeUtil.getEndDayOfLastWeek().getTime());
                break;
            case 5:
                query.ge(queryTime, TimeUtil.getBeginDayOfMonth().getTime());
                query.le(queryTime, TimeUtil.getDayEnd().getTime());
                break;
            case 6:
                query.ge(queryTime, TimeUtil.getBeginDayOfLastMonth().getTime());
                query.le(queryTime, TimeUtil.getEndDayOfLastMonth().getTime());
                break;
            case 7:
                if(!StringUtils.isBlank(begin)){
                    try {
                        long timeStamp = TimeUtil.stringToTimeStamp(begin, TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS);
                        query.ge(queryTime, timeStamp);
                    } catch (ParseException e) {
                        try {
                            long timeStamp = TimeUtil.stringToTimeStamp(begin, TimeUtil.TIME_YYYY_MM_DD);
                            query.ge(queryTime, timeStamp);
                        } catch (ParseException e1) {
                        }
                    }
                }
                if(!StringUtils.isBlank(end)){
                    try {
                        long timeStamp = TimeUtil.stringToTimeStamp(end, TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS);
                        query.le(queryTime, timeStamp+TimeUtil.TIME_ONE_DAY);
                    } catch (ParseException e) {
                        try {
                            long timeStamp = TimeUtil.stringToTimeStamp(end, TimeUtil.TIME_YYYY_MM_DD);
                            query.le(queryTime, timeStamp+TimeUtil.TIME_ONE_DAY);
                        } catch (ParseException ex) {
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public RechargeWithdrawalResponse rechargeStatisByPayType(ReportRewiRequest reportRewiRequest) {
        QueryWrapper<BallLoggerWithdrawal> queryWrapper = new QueryWrapper();
        queryWrapper.select("sum(money) money,status,count(id) id,count(distinct player_id) player_id");
        queryWrapper.eq("account_type",2);
//        queryWrapper.in("status",4,3);
        queryWrapper.eq("status",4);
        if(reportRewiRequest.getBehalfId()!=null){
            queryWrapper.eq("behalf_id",reportRewiRequest.getBehalfId());
        }
        queryWrapper.groupBy("status");
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        queryByTimeConf(queryWrapper,reportRewiRequest.getTime(),reportRewiRequest.getBegin(),reportRewiRequest.getEnd(),systemConfig);
        List<BallLoggerWithdrawal> list = list(queryWrapper);
        RechargeWithdrawalResponse statisTotal = RechargeWithdrawalResponse.builder()
                .ymd("合计")
                .payName("")
                .resuccCount(0)
                .resuccMoney(0L)
                .resuccPlayer(new HashSet<>())
//                .refailCount(0)
//                .refailMoney(0L)
//                .refailPlayer(new HashSet<>())
//                .wifailCount(0)
//                .wifailMoney(0L)
//                .wifailPlayer(new HashSet<>())
                .wisuccCount(0)
                .wisuccMoney(0L)
                .wisuccPlayer(new HashSet<>())
//                .wisuccPer("")
                .build();
        for(BallLoggerWithdrawal item:list){
            if(item.getStatus()==4){
                //成功
                statisTotal.setWisuccCount(item.getId().intValue());
                Set<Long> objects = new HashSet<>();
                for(long i=0;i<item.getPlayerId();i++){
                    objects.add(i);
                }
                statisTotal.setWisuccPlayer(objects);
                statisTotal.setWisuccMoney(item.getMoney());
            }else{
//                statisTotal.setWifailCount(item.getId().intValue());
//                statisTotal.setWifailMoney(item.getMoney());
//                Set<Long> objects = new HashSet<>();
//                for(long i=0;i<item.getPlayerId();i++){
//                    objects.add(i);
//                }
//                statisTotal.setWifailPlayer(objects);
            }
        }
        return statisTotal;
    }

    @Override
    public void search(QueryActivePlayerRequest queryParam, BallPlayer player, Map<Long, BallPlayer> dataMap) {
        QueryWrapper<BallLoggerWithdrawal> query = new QueryWrapper<>();
        //提现成功的
        query.eq("status",4);
        if(!StringUtils.isEmpty(queryParam.getRbegin())){
            try {
                query.gt("created_at",TimeUtil.stringToTimeStamp(queryParam.getRbegin(),TimeUtil.TIME_YYYY_MM_DD));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if(!StringUtils.isEmpty(queryParam.getRend())){
            try {
                query.lt("created_at",TimeUtil.stringToTimeStamp(queryParam.getRend(),TimeUtil.TIME_YYYY_MM_DD)+TimeUtil.TIME_ONE_DAY);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        query.likeRight("super_tree",player.getSuperTree()+player.getId()+"\\_");
//        query.in("player_id",ids);
        int pageNo = 1;
        while (true){
            Page<BallLoggerWithdrawal> page = new Page<>(pageNo++, 500);
            IPage<BallLoggerWithdrawal> pageResult = page(page, query);
            List<BallLoggerWithdrawal> records = pageResult.getRecords();
            if(records==null||records.isEmpty()||records.get(0)==null){
                break;
            }
            for(BallLoggerWithdrawal item:records){
                BallPlayer wplayer = dataMap.get(item.getPlayerId());
                if(wplayer!=null){
                    wplayer.setCumulativeReflect(wplayer.getCumulativeReflect()+item.getMoney());
                }
            }
        }
    }

    @Override
    public void searchProxy2(BallProxyLogger queryParam, Map<String, ProxyStatis2Dto> dataMap, BallPlayer playerProxy, ProxyStatis2Dto total, BallSystemConfig systemConfig) {
        QueryWrapper<BallLoggerWithdrawal> query = new QueryWrapper<>();
        //提现成功的
        query.eq("status",4);
        query.eq("account_type",2);
        String queryTime = systemConfig.getStatisTime()==0?"created_at":"behalf_time";
        if(!StringUtils.isEmpty(queryParam.getBegin())){
            try {
                query.gt(queryTime,TimeUtil.stringToTimeStamp(queryParam.getBegin(),TimeUtil.TIME_YYYY_MM_DD));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }// 1668796200000 1668882600000
        if(!StringUtils.isEmpty(queryParam.getEnd())){
            try {
                query.lt(queryTime,TimeUtil.stringToTimeStamp(queryParam.getEnd(),TimeUtil.TIME_YYYY_MM_DD)+TimeUtil.TIME_ONE_DAY);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        query.likeRight("super_tree",playerProxy.getSuperTree()+playerProxy.getId()+"\\_");
//        query.in("player_id",ids);
        int pageNo = 1;
        while (true){
            Page<BallLoggerWithdrawal> page = new Page<>(pageNo++, 500);
            IPage<BallLoggerWithdrawal> pageResult = page(page, query);
            List<BallLoggerWithdrawal> records = pageResult.getRecords();
            if(records==null||records.isEmpty()||records.get(0)==null){
                break;
            }
            for(BallLoggerWithdrawal item:records){
                String ymd = TimeUtil.longToStringYmd(systemConfig.getStatisTime()==0?item.getCreatedAt():item.getBehalfTime());
                ProxyStatis2Dto proxyStatis2Dto = dataMap.get(ymd);
                if(proxyStatis2Dto!=null){
                    Set<Long> withdrawalPlayerSet = proxyStatis2Dto.getWithdrawalPlayerSet();
                    withdrawalPlayerSet.add(item.getPlayerId());
                    proxyStatis2Dto.setWithdrawalMoney(proxyStatis2Dto.getWithdrawalMoney()+item.getMoney());

                    Set<Long> withdrawalPlayerSett = total.getWithdrawalPlayerSet();
                    withdrawalPlayerSett.add(item.getPlayerId());
                    total.setWithdrawalMoney(total.getWithdrawalMoney()+item.getMoney());
                }
            }
        }
    }

    @Override
    public void statis(BallProxyLogger queryParam, ProxyStatis3Dto total, ProxyStatis3Dto data, BallSystemConfig systemConfig) {
        QueryWrapper<BallLoggerWithdrawal> query = new QueryWrapper<>();
        //提现成功的
        query.eq("status",4);
        query.eq("account_type",2);
        String queryTime = systemConfig.getStatisTime()==0?"created_at":"behalf_time";
        if(!StringUtils.isEmpty(queryParam.getBegin())){
            try {
                query.gt(queryTime,TimeUtil.stringToTimeStamp(queryParam.getBegin(),TimeUtil.TIME_YYYY_MM_DD));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if(!StringUtils.isEmpty(queryParam.getEnd())){
            try {
                query.lt(queryTime,TimeUtil.stringToTimeStamp(queryParam.getEnd(),TimeUtil.TIME_YYYY_MM_DD)+TimeUtil.TIME_ONE_DAY);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        query.likeRight("super_tree",queryParam.getPlayerName()+queryParam.getPlayerId()+"\\_");
        query.select("count(distinct player_id) player_id,sum(money) money");
        BallLoggerWithdrawal one = getOne(query);
        data.setWithdrawalMoney(one.getMoney()==null?0:one.getMoney());
        data.setWithdrawalCount(one.getPlayerId()==null?0:one.getPlayerId().intValue());

        total.setWithdrawalMoney(total.getWithdrawalMoney()+data.getWithdrawalMoney());
        total.setWithdrawalCount(total.getWithdrawalCount()+data.getWithdrawalCount());
    }

    @Override
    public Boolean findByCardNumber(String cardNumber, Long id) {
        // 非自己提现成功数据
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("status",4);
        queryWrapper.ne("player_id",id);
        queryWrapper.eq("to_bank",cardNumber);
        int count = count(queryWrapper);
        return count>0;
    }

    @Override
    public BallLoggerWithdrawal findLast(BallLoggerWithdrawal last) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("status",4);
        queryWrapper.eq("player_id",last.getPlayerId());
        queryWrapper.lt("id",last.getId());
        queryWrapper.orderByDesc("id");
        queryWrapper.last("limit 1");
        return getOne(queryWrapper,false);
    }

    @Override
    public void autoCheck() {
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        if(systemConfig.getAutoCheck()==1){
            try {
                Double aDouble = Double.valueOf(systemConfig.getAutoCheckTime());
                Double time = aDouble*TimeUtil.TIME_ONE_HOUR;
                //未审核提现,自动审核
                UpdateWrapper updateWrapper = new UpdateWrapper();
                updateWrapper.lt("created_at",System.currentTimeMillis()-time.longValue());
                updateWrapper.eq("status",1);
                updateWrapper.set("status",2);
                update(updateWrapper);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }

}
