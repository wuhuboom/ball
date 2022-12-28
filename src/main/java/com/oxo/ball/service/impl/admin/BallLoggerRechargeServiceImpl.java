package com.oxo.ball.service.impl.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.oxo.ball.bean.dao.*;
import com.oxo.ball.bean.dto.api.PayBackDto;
import com.oxo.ball.bean.dto.api.cha.PayNoticeDtoCHA;
import com.oxo.ball.bean.dto.api.in.PayNoticeDtoIN;
import com.oxo.ball.bean.dto.req.admin.QueryActivePlayerRequest;
import com.oxo.ball.bean.dto.req.player.RechargeLogRequest;
import com.oxo.ball.bean.dto.req.report.ReportDataRequest;
import com.oxo.ball.bean.dto.req.report.ReportRewiRequest;
import com.oxo.ball.bean.dto.req.report.ReportStandardRequest;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.bean.dto.resp.admin.ProxyStatis2Dto;
import com.oxo.ball.bean.dto.resp.admin.ProxyStatis3Dto;
import com.oxo.ball.bean.dto.resp.admin.ProxyStatisDto;
import com.oxo.ball.bean.dto.resp.report.RechargeWithdrawalResponse;
import com.oxo.ball.bean.dto.resp.report.ReportStandardPlayerDTO;
import com.oxo.ball.contant.RedisKeyContant;
import com.oxo.ball.mapper.BallLoggerRechargeMapper;
import com.oxo.ball.service.admin.*;
import com.oxo.ball.service.impl.BasePlayerService;
import com.oxo.ball.service.impl.player.PlayerServiceImpl;
import com.oxo.ball.utils.*;
import io.undertow.util.StatusCodes;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
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
public class BallLoggerRechargeServiceImpl extends ServiceImpl<BallLoggerRechargeMapper, BallLoggerRecharge> implements IBallLoggerRechargeService {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    IBallLoggerBackService loggerBackService;

    @Autowired
    IBallLoggerHandsupService loggerHandsupService;
    @Autowired
    BasePlayerService basePlayerService;
    @Autowired
    IBallPlayerService ballPlayerService;
    @Autowired
    PlayerServiceImpl playerService;
    @Autowired
    IBallPaymentManagementService paymentManagementService;
    @Autowired
    IBallBalanceChangeService ballBalanceChangeService;
    @Autowired
    IBallLoggerRebateService loggerRebateService;
    @Autowired
    private BallAdminService adminService;
    @Autowired
    private IApiService apiService;
    @Autowired
    IBallSystemConfigService systemConfigService;

    @Override
    public SearchResponse<BallLoggerRecharge> search(BallLoggerRecharge queryParam, Integer pageNo, Integer pageSize) {
        SearchResponse<BallLoggerRecharge> response = new SearchResponse<>();
        Page<BallLoggerRecharge> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallLoggerRecharge> query = new QueryWrapper<>();

        if(queryParam.getUserId()!=null){
            query.eq("user_id",queryParam.getUserId());
        }
        if(queryParam.getFirst()!=null){
            query.eq("first",queryParam.getFirst());
        }
        if(queryParam.getType()!=null){
            query.eq("type",queryParam.getType());
        }
        if(queryParam.getAccountType()!=null){
            query.eq("account_type",queryParam.getAccountType());
        }
        if(queryParam.getStatus()!=null){
            query.eq("status",queryParam.getStatus());
        }
        if(!StringUtils.isBlank(queryParam.getUsername())){
            query.eq("username",queryParam.getUsername());
        }
        if(queryParam.getOrderNo()!=null){
            query.eq("order_no",queryParam.getOrderNo());
        }
        if(queryParam.getMoneyMin()!=null){
            query.ge("money_sys",queryParam.getMoneyMin()*BigDecimalUtil.PLAYER_MONEY_UNIT);
        }
        if(queryParam.getMoneyMax()!=null){
            query.le("money_sys",queryParam.getMoneyMax()*BigDecimalUtil.PLAYER_MONEY_UNIT);
        }

        query.orderByDesc("id");
        IPage<BallLoggerRecharge> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public SearchResponse<BallLoggerRecharge> search(BallPlayer currPlayer, RechargeLogRequest queryParam, Integer pageNo, Integer pageSize) {
        SearchResponse<BallLoggerRecharge> response = new SearchResponse<>();
        Page<BallLoggerRecharge> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallLoggerRecharge> query = new QueryWrapper<>();
        if(queryParam.getType()!=null){
            query.eq("type",queryParam.getType());
        }
        if(queryParam.getStatus()!=null){
            query.eq("status",queryParam.getStatus());
        }
        query.eq("player_id",currPlayer.getId());
//        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        searchCaseOnTime(query, queryParam.getTime(),null);
        query.orderByDesc("id");
        IPage<BallLoggerRecharge> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    static void searchCaseOnTime(QueryWrapper<BallLoggerRecharge> query, Integer time, BallSystemConfig systemConfig) {
        String statisTime = "created_at";
        if(time !=null){
            switch (time){
                case 1:
                    query.ge(statisTime, TimeUtil.getDayBegin().getTime());
                    query.le(statisTime, TimeUtil.getDayEnd().getTime());
                    break;
                case 2:
                    query.ge(statisTime, TimeUtil.getBeginDayOfYesterday().getTime());
                    query.le(statisTime, TimeUtil.getEndDayOfYesterday().getTime());
                    break;
                case 3:
                    query.ge(statisTime, TimeUtil.getDayBegin().getTime() - 7 * TimeUtil.TIME_ONE_DAY);
                    query.le(statisTime, TimeUtil.getDayEnd().getTime());
                    break;
                case 4:
                    query.ge(statisTime, TimeUtil.getDayBegin().getTime() - 10 * TimeUtil.TIME_ONE_DAY);
                    query.le(statisTime, TimeUtil.getDayEnd().getTime());
                    break;
                case 5:
                    query.ge(statisTime, TimeUtil.getDayBegin().getTime() - 30 * TimeUtil.TIME_ONE_DAY);
                    query.le(statisTime, TimeUtil.getDayEnd().getTime());
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public BallLoggerRecharge insert(BallLoggerRecharge loggerBet) {
        save(loggerBet);
        return loggerBet;
    }

    @Override
    public BallLoggerRecharge findById(Long id) {
        return getById(id);
    }

    @Override
    public boolean edit(BallLoggerRecharge loggerRecharge) {
        return updateById(loggerRecharge);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized BaseResponse editRe(BallLoggerRecharge aloggerRecharge) throws JsonProcessingException {
        String moneyParam = aloggerRecharge.getMoneyParam();
        BallLoggerRecharge ballLoggerRecharge = findById(aloggerRecharge.getId());
        if(ballLoggerRecharge.getStatus()==3){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e28"));
        }
        try {
            double moneyParamd = Double.parseDouble(moneyParam);
            Double mul = BigDecimalUtil.mul(moneyParamd, 100);
            aloggerRecharge.setMoneyReal(mul.longValue());
        }catch (Exception ex){

        }
        if(aloggerRecharge.getMoneyReal()==null||aloggerRecharge.getMoneyReal()==0){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e54"));
        }
        //拉回,主要是修改状态,和增加余额和日志
        BallLoggerRecharge edit = BallLoggerRecharge.builder()
                .id(ballLoggerRecharge.getId())
                .moneyReal(aloggerRecharge.getMoneyReal())
                .remark(aloggerRecharge.getRemark())
                .status(3)
                .operUser(aloggerRecharge.getOperUser())
                .createdAt(ballLoggerRecharge.getCreatedAt())
                .build();
        //已付款则添加余额和日志
        BallPlayer player = basePlayerService.findById(ballLoggerRecharge.getPlayerId());
        final BallPaymentManagement paymentManagement = paymentManagementService.findById(ballLoggerRecharge.getPayId());
        BaseResponse res=null;
        if(paymentManagement.getPayType()==1){
            //usdt
            res = playerService.recharge(player, edit, PayBackDto.builder()
                    .accountPractical(Long.valueOf(aloggerRecharge.getMoneyReal()).floatValue())
                    .build(),paymentManagement);
        }else if(paymentManagement.getPayType()==2){
            //印度
            res = playerService.recharge(player, edit, PayNoticeDtoIN.builder()
                    .pay_amount(aloggerRecharge.getMoneyReal().intValue())
                    .build(),paymentManagement);
        }else{
            //加纳
            //印度
            res = playerService.recharge(player, edit, PayNoticeDtoCHA.builder()
                    .amount(aloggerRecharge.getMoneyReal().toString())
                    .build(),paymentManagement);
        }
        if(!res.getCode().equals(StatusCodes.OK)){
            return res;
        }
        ThreadPoolUtil.exec(new Runnable() {
            @Override
            public void run() {
                //TG提醒 @对应的TG账号,查询全代理然后配置了tg账号的号先
//                List<BallAdmin> byTg = adminService.findByTg();
//                if(!byTg.isEmpty()){
//                    for(BallAdmin item:byTg){
//                        if(names.contains(item.getTgName())){
//                            continue;
//                        }
//                        names.add(item.getTgName());
//                        String message = MessageFormat.format("@{0} 用户 {1}  顶级代理 {2} 充值金额 {3} 充值货币 {4} 充值渠道 {5} 系统金额 {6}",
//                                item.getTgName(),
//                                ballLoggerRecharge.getUsername(),
//                                ballLoggerRecharge.getTopUsername(),
//                                aloggerRecharge.getMoneyParam(),
//                                paymentManagement.getCurrencySymbol(),
//                                paymentManagement.getName(),
//                                BigDecimalUtil.div(edit.getMoneySys(),BigDecimalUtil.PLAYER_MONEY_UNIT));
//                        apiService.tgNotice(message);
//                    }
//                }
                //查数据关联账号，playerName like topname
                if(player.getAccountType()==1){
                    return;
                }
                if(StringUtils.isBlank(ballLoggerRecharge.getTopUsername())){
                    //总充值 总提现 人工加款 存提差
                    String message = MessageFormat.format("用户 {0} 顶级代理 {1} 充值金额 {2} 充值货币 {3} 充值渠道 {4} 系统金额 {5}",
                            ballLoggerRecharge.getUsername(),
                            "无",
                            aloggerRecharge.getMoneyParam(),
                            paymentManagement.getCurrencySymbol(),
                            paymentManagement.getName(),
                            ""+BigDecimalUtil.div(edit.getMoneySys(),BigDecimalUtil.PLAYER_MONEY_UNIT)
                            );
                    apiService.tgNotice(message);
                }else{
                    Set<String> names = new HashSet<>();
                    List<BallAdmin> byPlayername = adminService.findByPlayername(ballLoggerRecharge.getTopUsername());
                    if(!byPlayername.isEmpty()){
                        for(BallAdmin item:byPlayername){
                            if(names.contains(item.getTgName())){
                                continue;
                            }
                            if(StringUtils.isBlank(item.getTgName())){
                                // 没有配置TGname不发
                                continue;
                            }
                            names.add(item.getTgName());
                            String message = MessageFormat.format("{0} 用户 {1}  顶级代理 {2} 充值金额 {3} 充值货币 {4} 充值渠道 {5} 系统金额 {6}",
                                    "@"+item.getTgName(),
                                    ballLoggerRecharge.getUsername(),
                                    ballLoggerRecharge.getTopUsername(),
                                    aloggerRecharge.getMoneyParam(),
                                    paymentManagement.getCurrencySymbol(),
                                    paymentManagement.getName(),
                                    ""+BigDecimalUtil.div(edit.getMoneySys(),BigDecimalUtil.PLAYER_MONEY_UNIT));
                            apiService.tgNotice(message);
                        }
                    }else{
                        String message = MessageFormat.format("用户 {0}  顶级代理 {1} 充值金额 {2} 充值货币 {3} 充值渠道 {4} 系统金额 {5}",
                                ballLoggerRecharge.getUsername(),
                                ballLoggerRecharge.getTopUsername(),
                                aloggerRecharge.getMoneyParam(),
                                paymentManagement.getCurrencySymbol(),
                                paymentManagement.getName(),
                                ""+BigDecimalUtil.div(edit.getMoneySys(),BigDecimalUtil.PLAYER_MONEY_UNIT));
                        apiService.tgNotice(message);
                    }
                }
            }
        });
        return res;
    }

    @Override
    public Long getDayOrderNo() {
        if (redisUtil.get(RedisKeyContant.RECHARGE_ORDER_NO) == null) {
            redisUtil.set(RedisKeyContant.RECHARGE_ORDER_NO, 1);
        }
        long incr = redisUtil.incr(RedisKeyContant.RECHARGE_ORDER_NO, 1);
        return incr;
    }

    @Override
    public void refreshStatus() {
        //超时账单设置为超时状态
        UpdateWrapper updateWrapper = new UpdateWrapper();
        updateWrapper.set("status",4);
        //未支付订单
        updateWrapper.eq("status",1);
        updateWrapper.le("created_at",System.currentTimeMillis()-TimeUtil.TIME_ONE_MIN*30);
        update(updateWrapper);
    }

    @Override
    public Long statisTotal() {
        //统计充值
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.select("sum(money_sys) as money_sys");
        queryWrapper.eq("account_type",2);
        queryWrapper.eq("status",3);
        List<BallLoggerRecharge> list = list(queryWrapper);
        if(list!=null&&!list.isEmpty()&&list.get(0)!=null){
            return list.get(0).getMoneySys();
        }
        return 0L;
    }

    @Override
    public BallLoggerRecharge statisTotal(ReportDataRequest reportDataRequest) {
        //充值人数 充值笔数 充值金额 充值优惠 活动优惠 统计充值
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.select("sum(money_sys) as money_sys,count(distinct player_id) as player_id,count(id) as id");
        queryWrapper.eq("account_type",2);
        queryWrapper.eq("status",3);
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        BallPlayerServiceImpl.queryByTimeConf(queryWrapper,reportDataRequest.getTime(),reportDataRequest.getBegin(),reportDataRequest.getEnd(),systemConfig);
        BallPlayer player=null;
        if(reportDataRequest.getUserId()!=null){
            player = basePlayerService.findByUserId(reportDataRequest.getUserId());
        }else if(!StringUtils.isBlank(reportDataRequest.getUsername())){
            player = basePlayerService.findByUsername(reportDataRequest.getUsername());
        }
        if(player!=null){
            queryWrapper.likeRight("super_tree",player.getSuperTree()+player.getId()+"\\_");
        }
        List<BallLoggerRecharge> list = list(queryWrapper);

        //盈利返利
        BallLoggerBack statis = loggerBackService.statis(reportDataRequest,2);
        //充值返佣
//        BallLoggerBack statisRechargeBack = loggerBackService.statis(reportDataRequest,3);
        //充值优惠
//        BallBalanceChange balanceDiscount = ballBalanceChangeService.statisDiscount(reportDataRequest);
        BallLoggerRebate statisRebate = loggerRebateService.statisDiscount(reportDataRequest);
        BallLoggerRecharge loggerRecharge = null;
        if(list!=null&&!list.isEmpty()&&list.get(0)!=null){
            loggerRecharge = list.get(0);
            loggerRecharge.setOrderNo(statis.getMoney());
//            long a = statisRechargeBack.getMoney();
            loggerRecharge.setMoneyDiscount(statisRebate.getMoney());
        }
        if(loggerRecharge==null){
            loggerRecharge = BallLoggerRecharge.builder()
                    .id(0L)
                    .playerId(0L)
                    .moneyDiscount(statisRebate.getMoney())
                    .money(0L)
                    .orderNo(statis.getMoney())
                    .build();
        }
        //统计人工充值
//        BallLoggerHandsup statisHands = loggerHandsupService.statisRecharge(reportDataRequest);
//        loggerRecharge.setId((loggerRecharge.getId()==null?0:loggerRecharge.getId())+(statisHands.getId()==null?0:statisHands.getId()));
//        loggerRecharge.setPlayerId((loggerRecharge.getPlayerId()==null?0:loggerRecharge.getPlayerId())+loggerRecharge.getPlayerId()+(statisHands.getPlayerId()==null?0:statisHands.getPlayerId()));
//        loggerRecharge.setMoney((loggerRecharge.getMoney()==null?0:loggerRecharge.getMoney())+(statisHands.getMoney()==null?0:statisHands.getMoney()));
        return  loggerRecharge;
    }

    @Override
    public List<Map<String,Object>> statisByType(ReportDataRequest reportDataRequest) {
        List<Map<String,Object>> listData = new ArrayList<>();
        Map<String,Object> data = new HashMap<>();
        data.put("ron",0);
        data.put("roff",0);
        data.put("rha",0);

        data.put("pon",0);
        data.put("poff",0);
        data.put("pha",0);

        data.put("ton",0);
        data.put("toff",0);
        data.put("tha",0);

        Long totalR = 0L;
        Long totalP = 0L;
        Long totalT = 0L;
        //非人工
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.select("count(id) as id,count(distinct player_id) as player_id,sum(money_sys) as money_sys,type");
        queryWrapper.eq("account_type",2);
        queryWrapper.groupBy("type");
        queryWrapper.eq("status",3);
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        BallPlayerServiceImpl.queryByTimeConf(queryWrapper,reportDataRequest.getTime(),reportDataRequest.getBegin(),reportDataRequest.getEnd(),systemConfig);
        BallPlayer player=null;
        if(reportDataRequest.getUserId()!=null){
            player = basePlayerService.findByUserId(reportDataRequest.getUserId());
        }else if(!StringUtils.isBlank(reportDataRequest.getUsername())){
            player = basePlayerService.findByUsername(reportDataRequest.getUsername());
        }
        if(player!=null){
            queryWrapper.likeRight("super_tree",player.getSuperTree()+player.getId()+"\\_");
        }
        List<BallLoggerRecharge> list = list(queryWrapper);
        for(BallLoggerRecharge item:list){
            totalR+=item.getId()==null?0:item.getId();
            totalP+=item.getPlayerId()==null?0:item.getPlayerId();
            totalT+=item.getMoneySys()==null?0:item.getMoneySys();
            if(item.getType()==2){
                //线下
                data.put("roff",item.getId());
                data.put("poff",item.getPlayerId());
                data.put("toff",item.getMoneySys());
            }else{
                //线上
                data.put("ron",item.getId());
                data.put("pon",item.getPlayerId());
                data.put("ton",item.getMoneySys());
            }
        }
        //人工
        BallLoggerHandsup loggerHandsup = loggerHandsupService.statisByType(reportDataRequest,1);
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
    public SearchResponse<BallLoggerRecharge> statisReport(ReportDataRequest reportDataRequest, int pageNo, int pageSize) {
        SearchResponse<BallLoggerRecharge> response = new SearchResponse<>();
        Page<BallLoggerRecharge> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallLoggerRecharge> query = new QueryWrapper<>();
        query.eq("account_type",2);
        query.eq("status",2);
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        BallPlayerServiceImpl.queryByTimeConf(query,reportDataRequest.getTime(),reportDataRequest.getBegin(),reportDataRequest.getEnd(),systemConfig);
        IPage<BallLoggerRecharge> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public BallLoggerRecharge findByOrderNo(long orderNo) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("order_no",orderNo);
        List<BallLoggerRecharge> list = list(queryWrapper);
        if(list==null || list.isEmpty() || list.get(0)==null){
            return null;
        }
        return list.get(0);
    }

    @Override
    public List<BallLoggerRecharge> statisPayCount(Long id, Long begin, Long end) {
        QueryWrapper<BallLoggerRecharge> query = new QueryWrapper<>();
        query.eq("account_type",2);
        query.likeRight("super_tree","\\_"+id+"\\_");
        query.select("sum(money_sys) money_sys,type");
        query.groupBy("type");
        return list(query);
    }

    @Override
    public void search(BallProxyLogger queryParam, List<Long> ids, ProxyStatisDto list1, Map<Long,ProxyStatisDto> list2Map,BallPlayer proxyUser,BallSystemConfig systemConfig) {
        QueryWrapper<BallLoggerRecharge> query = new QueryWrapper<>();
        //充值成功的
        query.eq("status",3);
        String queryTime = systemConfig.getStatisTime()==0?"created_at":"updated_at";
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
        //查代理线
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
            Page<BallLoggerRecharge> page = new Page<>(pageNo++, 500);
            IPage<BallLoggerRecharge> pageResult = page(page, query);
            List<BallLoggerRecharge> records = pageResult.getRecords();
            if(records==null||records.isEmpty()||records.get(0)==null){
                break;
            }
            for(BallLoggerRecharge item:records){
                if(queryParam.getProxyLine()==0){
                    ProxyStatisDto proxyStatisDto = list2Map.get(item.getPlayerId());
                    if(proxyStatisDto!=null){
                        Set<Long> rechargePlayerSet = list1.getRechargePlayerSet();
                        rechargePlayerSet.add(item.getPlayerId());
                        list1.setRechargeMoney(list1.getRechargeMoney()+item.getMoneySys());

                        Set<Long> rechargePlayerSet1 = proxyStatisDto.getRechargePlayerSet();
                        rechargePlayerSet1.add(item.getPlayerId());
                        proxyStatisDto.setRechargeMoney(proxyStatisDto.getRechargeMoney()+item.getMoneySys());
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
                            Set<Long> rechargePlayerSet = list1.getRechargePlayerSet();
                            rechargePlayerSet.add(item.getPlayerId());
                            list1.setRechargeMoney(list1.getRechargeMoney()+item.getMoneySys());
                            added=true;
                        }

                        Set<Long> rechargePlayerSet = proxyStatisDto.getRechargePlayerSet();
                        rechargePlayerSet.add(item.getPlayerId());
                        proxyStatisDto.setRechargeMoney(proxyStatisDto.getRechargeMoney()+item.getMoneySys());
                    }
                }
                if(item.getPlayerId().equals(proxyUser.getId())){
                    Set<Long> rechargePlayerSet = list1.getRechargePlayerSet();
                    rechargePlayerSet.add(item.getPlayerId());
                    list1.setRechargeMoney(list1.getRechargeMoney()+item.getMoneySys());
                }
            }
        }
    }

    @Override
    public List<RechargeWithdrawalResponse> rechargeStatisByPayTypeList(ReportRewiRequest reportRewiRequest) {
        //充值人数 充值笔数 充值金额 充值优惠 活动优惠 统计充值
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("account_type",2);
//        queryWrapper.in("status",4,3);
        queryWrapper.eq("status",3);
        if(reportRewiRequest.getPayId()!=null){
            queryWrapper.eq("pay_id",reportRewiRequest.getPayId());
        }
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        BallPlayerServiceImpl.queryByTimeConf(queryWrapper,reportRewiRequest.getTime(),reportRewiRequest.getBegin(),reportRewiRequest.getEnd(),systemConfig);
        int pageNo = 1;
        int pageSize = 500;
        Map<String,RechargeWithdrawalResponse> statisMap = new HashMap<>();
        RechargeWithdrawalResponse statisTotal = RechargeWithdrawalResponse.builder()
                .ymd("合计")
                .payName("")
                .resuccCount(0)
                .resuccMoney(0L)
                .resuccPlayer(new HashSet<>())
                .wisuccCount(0)
                .wisuccMoney(0L)
                .wisuccPlayer(new HashSet<>())
                .build();
        while (true){
            Page<BallLoggerRecharge> page = new Page<>(pageNo++, pageSize);
            IPage<BallLoggerRecharge> list = page(page,queryWrapper);
            List<BallLoggerRecharge> records = list.getRecords();
            if(records==null||records.isEmpty()||records.get(0)==null){
                break;
            }
//        统计开始
            for(BallLoggerRecharge item:records){
                String ymd = TimeUtil.dateFormat(new Date(item.getCreatedAt()), TimeUtil.TIME_YYYY_MM_DD);
                String key = ymd+item.getPayId();
                RechargeWithdrawalResponse loggerRecharge = statisMap.get(key);
                if(loggerRecharge==null){
                    loggerRecharge = RechargeWithdrawalResponse.builder()
                            .ymd(ymd)
                            .payName(item.getPayName())
                            .resuccCount(0)
                            .resuccMoney(0L)
                            .resuccPlayer(new HashSet<>())
                            .wisuccCount(0)
                            .wisuccMoney(0L)
                            .wisuccPlayer(new HashSet<>())
//                        .wisuccPer("")
                            .build();
                    statisMap.put(key,loggerRecharge);
                }
                if(item.getStatus()==3){
                    loggerRecharge.setResuccCount(loggerRecharge.getResuccCount()+1);
                    loggerRecharge.getResuccPlayer().add(item.getPlayerId());
                    loggerRecharge.setResuccMoney(loggerRecharge.getResuccMoney()+item.getMoneySys());

                    statisTotal.setResuccCount(statisTotal.getResuccCount()+1);
                    statisTotal.getResuccPlayer().add(item.getPlayerId());
                    statisTotal.setResuccMoney(statisTotal.getResuccMoney()+item.getMoneySys());
                }else if(item.getStatus()==4){
//                loggerRecharge.setRefailCount(loggerRecharge.getRefailCount()+1);
//                loggerRecharge.getRefailPlayer().add(item.getPlayerId());
//                loggerRecharge.setRefailMoney(loggerRecharge.getRefailMoney()+item.getMoney());
//
//                statisTotal.setRefailCount(statisTotal.getRefailCount()+1);
//                statisTotal.getRefailPlayer().add(item.getPlayerId());
//                statisTotal.setRefailMoney(statisTotal.getRefailMoney()+item.getMoney());
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

    @Override
    public RechargeWithdrawalResponse rechargeStatisByPayType(ReportRewiRequest reportRewiRequest) {
        QueryWrapper<BallLoggerRecharge> queryWrapper = new QueryWrapper();
        queryWrapper.select("sum(money_sys) money_sys,sum(money) money,status,count(id) id,count(distinct player_id) player_id");
        queryWrapper.eq("account_type",2);
//        queryWrapper.in("status",4,3);
        queryWrapper.eq("status",3);
        queryWrapper.groupBy("status");
        if(reportRewiRequest.getPayId()!=null){
            queryWrapper.eq("pay_id",reportRewiRequest.getPayId());
        }
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        BallPlayerServiceImpl.queryByTimeConf(queryWrapper,reportRewiRequest.getTime(),reportRewiRequest.getBegin(),reportRewiRequest.getEnd(),systemConfig);
        List<BallLoggerRecharge> list = list(queryWrapper);
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
        for(BallLoggerRecharge item:list){
            if(item.getStatus()==3){
                //成功
                statisTotal.setResuccCount(item.getId().intValue());
                Set<Long> objects = new HashSet<>();
                for(long i=0;i<item.getPlayerId();i++){
                    objects.add(i);
                }
                statisTotal.setResuccPlayer(objects);
                statisTotal.setResuccMoney(item.getMoneySys());
            }else{
//                statisTotal.setRefailCount(item.getId().intValue());
//                statisTotal.setRefailMoney(item.getMoney());
//                Set<Long> objects = new HashSet<>();
//                for(long i=0;i<item.getPlayerId();i++){
//                    objects.add(i);
//                }
//                statisTotal.setRefailPlayer(objects);
            }
        }
        return statisTotal;
    }

    @Override
    public List<BallLoggerRecharge> search43(long timeBegin, long timeEnd) {
        QueryWrapper query = new QueryWrapper();
        query.eq("status",3);
        query.ge("created_at",timeBegin);
        query.le("created_at",timeEnd);
        return list(query);
    }

    @Override
    public void search(QueryActivePlayerRequest queryParam, BallPlayer player, Map<Long, BallPlayer> dataMap) {
        QueryWrapper<BallLoggerRecharge> query = new QueryWrapper<>();
        //充值成功的
        query.eq("status",3);
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
        //查代理线
        query.likeRight("super_tree",player.getSuperTree()+player.getId()+"\\_");
        int pageNo = 1;
        while (true){
            Page<BallLoggerRecharge> page = new Page<>(pageNo++, 500);
            IPage<BallLoggerRecharge> pageResult = page(page, query);
            List<BallLoggerRecharge> records = pageResult.getRecords();
            if(records==null||records.isEmpty()||records.get(0)==null){
                break;
            }
            for(BallLoggerRecharge item:records){
                BallPlayer qplayer = dataMap.get(item.getPlayerId());
                if(qplayer!=null){
                    qplayer.setCumulativeTopUp(qplayer.getCumulativeTopUp()+item.getMoneySys());
                }
            }
        }
    }

    @Override
    public void editFirst(List<Long> ids) {
        UpdateWrapper updateWrapper = new UpdateWrapper();
        updateWrapper.in("id",ids);
        update(BallLoggerRecharge.builder()
                .first(1)
                .build(),updateWrapper);
    }

    @Override
    public SearchResponse<BallLoggerRecharge> searchFirst(BallLoggerRecharge build, int pageNo, int pageSize) {
        SearchResponse<BallLoggerRecharge> response = new SearchResponse<>();
        Page<BallLoggerRecharge> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallLoggerRecharge> query = new QueryWrapper<>();
        query.select("id");
        query.eq("first",0);
        query.eq("status",3);
        query.exists("select 1 from ball_player p where p.id = ball_logger_recharge.player_id" +
                " and p.first_top_up>0");
        query.groupBy("player_id");
        query.orderByAsc("id");
        IPage<BallLoggerRecharge> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public void searchProxy2(BallProxyLogger queryParam, Map<String, ProxyStatis2Dto> dataMap, BallPlayer playerProxy, ProxyStatis2Dto total, BallSystemConfig systemConfig) {
        QueryWrapper<BallLoggerRecharge> query = new QueryWrapper<>();
        //充值成功的
        query.eq("status",3);
        String queryTime = systemConfig.getStatisTime()==0?"created_at":"updated_at";
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
        //查代理线
        query.likeRight("super_tree",playerProxy.getSuperTree()+playerProxy.getId()+"\\_");
        int pageNo = 1;
        while (true){
            Page<BallLoggerRecharge> page = new Page<>(pageNo++, 500);
            IPage<BallLoggerRecharge> pageResult = page(page, query);
            List<BallLoggerRecharge> records = pageResult.getRecords();
            if(records==null||records.isEmpty()||records.get(0)==null){
                break;
            }
            for(BallLoggerRecharge item:records){
                Long time = systemConfig.getStatisTime()==0?item.getCreatedAt():item.getUpdatedAt();
                String ymd = TimeUtil.longToStringYmd(time);
                ProxyStatis2Dto proxyStatis2Dto = dataMap.get(ymd);
                if(proxyStatis2Dto!=null){
                    Set<Long> rechargePlayerSet = proxyStatis2Dto.getRechargePlayerSet();
                    rechargePlayerSet.add(item.getPlayerId());
                    proxyStatis2Dto.setRechargeMoney(proxyStatis2Dto.getRechargeMoney()+item.getMoneySys());

                    //总计
                    Set<Long> rechargePlayerSett = total.getRechargePlayerSet();
                    rechargePlayerSett.add(item.getPlayerId());
                    total.setRechargeMoney(total.getRechargeMoney()+item.getMoneySys());
                }
            }
        }
    }

    @Override
    public void statis(BallProxyLogger queryParam, ProxyStatis3Dto total, ProxyStatis3Dto data, BallSystemConfig systemConfig) {
        QueryWrapper<BallLoggerRecharge> query = new QueryWrapper<>();
        //充值成功的
        query.eq("status",3);
        String queryTime = systemConfig.getStatisTime()==0?"created_at":"updated_at";
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
        //查代理线
        query.likeRight("super_tree",queryParam.getPlayerName()+queryParam.getPlayerId()+"\\_");
        query.select("count(distinct player_id) player_id,sum(money_sys) money_sys");
        BallLoggerRecharge one = getOne(query);
        data.setRechargeCount(one.getPlayerId()==null?0:one.getPlayerId().intValue());
        data.setRechargeMoney(one.getMoneySys()==null?0:one.getMoneySys());

        total.setRechargeCount(total.getRechargeCount()+data.getRechargeCount());
        total.setRechargeMoney(total.getRechargeMoney()+data.getRechargeMoney());
    }

    @Override
    public void searchStandard(ReportStandardRequest reportStandardRequest, BallPlayer playerProxy, Map<Long, ReportStandardPlayerDTO> playerMap, BallSystemConfig systemConfig) {
        QueryWrapper<BallLoggerRecharge> query = new QueryWrapper<>();
        //充值成功的
        query.eq("status",3);
        String queryTime = systemConfig.getStatisTime()==0?"created_at":"updated_at";
        query.gt(queryTime,reportStandardRequest.getBegins());
        query.lt(queryTime,reportStandardRequest.getEnds());
        query.likeRight("super_tree",playerProxy.getSuperTree()+playerProxy.getId()+"\\_");
        int pageNo = 1;
        while (true){
            IPage<BallLoggerRecharge> page = page(new Page<>(pageNo++, 500), query);
            List<BallLoggerRecharge> records = page.getRecords();
            if(records==null||records.isEmpty()||records.get(0)==null){
                break;
            }
            for (BallLoggerRecharge item:records){
                ReportStandardPlayerDTO reportStandardPlayerDTO = playerMap.get(item.getPlayerId());
                reportStandardPlayerDTO.setRecharge(reportStandardPlayerDTO.getRecharge()+item.getMoneySys());
            }
        }
    }
}
