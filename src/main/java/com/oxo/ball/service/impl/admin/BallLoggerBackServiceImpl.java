package com.oxo.ball.service.impl.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxo.ball.bean.dao.*;
import com.oxo.ball.bean.dto.req.report.ReportDataRequest;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.mapper.BallLoggerBackMapper;
import com.oxo.ball.service.IBasePlayerService;
import com.oxo.ball.service.admin.IBallBalanceChangeService;
import com.oxo.ball.service.admin.IBallLoggerBackService;
import com.oxo.ball.service.admin.IBallSystemConfigService;
import com.oxo.ball.utils.MapUtil;
import com.oxo.ball.utils.ResponseMessageUtil;
import com.oxo.ball.utils.TimeUtil;
import com.oxo.ball.ws.WebSocketManager;
import com.oxo.ball.ws.dto.MessageResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.querydsl.QuerydslUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.*;

/**
 * <p>
 * 日志表 服务实现类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Service
public class BallLoggerBackServiceImpl extends ServiceImpl<BallLoggerBackMapper, BallLoggerBack> implements IBallLoggerBackService {

    @Resource
    BallLoggerBackMapper mapper;
    @Autowired
    IBasePlayerService basePlayerService;
    @Autowired
    private IBallBalanceChangeService ballBalanceChangeService;
    @Autowired
    private IBallSystemConfigService systemConfigService;
    @Autowired
    WebSocketManager webSocketManager;
    @Override
    public SearchResponse<BallLoggerBack> search(BallPlayer currPlayer, Integer pageNo, Integer pageSize) {
        SearchResponse<BallLoggerBack> response = new SearchResponse<>();
        Page<BallLoggerBack> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallLoggerBack> query = new QueryWrapper<>();
        query.eq("player_id",currPlayer.getId());
        query.orderByDesc("id");
        IPage<BallLoggerBack> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public SearchResponse<BallLoggerBack> search(BallLoggerBack queryParam, Integer pageNo, Integer pageSize) {
        SearchResponse<BallLoggerBack> response = new SearchResponse<>();
        Page<BallLoggerBack> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallLoggerBack> query = new QueryWrapper<>();
        if(queryParam.getStatus()!=null){
            query.eq("status",queryParam.getStatus());
        }
        query.orderByDesc("id");
        IPage<BallLoggerBack> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public BallLoggerBack search(BallLoggerBack loggerBack) {
        QueryWrapper query = new QueryWrapper();
        if(loggerBack.getPlayerId()!=null){
            query.eq("player_id",loggerBack.getPlayerId());
        }
        if(!StringUtils.isBlank(loggerBack.getOrderNo())){
            query.eq("order_no",loggerBack.getOrderNo());
        }
        if(loggerBack.getMoney()!=null){
            query.eq("money",loggerBack.getMoney());
        }
        if(loggerBack.getGameId()!=null){
            query.eq("game_id",loggerBack.getGameId());
        }
        query.last("limit 1");
        return getOne(query,false);
    }

    @Override
    public SearchResponse<BallLoggerBack> search2(BallPlayer currPlayer, Integer pageNo, Integer pageSize) {
        SearchResponse<BallLoggerBack> response = new SearchResponse<>();
        Page<BallLoggerBack> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallLoggerBack> query = new QueryWrapper<>();
        query.select("ymd,type,sum(money) money");
        query.eq("player_id",currPlayer.getId());
        query.groupBy("ymd");
        query.orderByDesc("ymd");
        IPage<BallLoggerBack> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        List<BallLoggerBack> records = pages.getRecords();
        if(records!=null&&!records.isEmpty()&&records.get(0)!=null){
            BallLoggerBack loggerBack = records.get(0);
            if(loggerBack.getYmd().equals(TimeUtil.dateFormat(new Date(),TimeUtil.TIME_YYYY_MM_DD))){
                //今日不返回
                records.remove(0);
            }
        }
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public BaseResponse statis(BallPlayer currPlayer) {
//        今日 昨日 上周
//                - 列表 日期 返佣类型 返佣金额 操作
        //今日，本周，上周，累计
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();

        Map<String,Object> data = new HashMap<>();
        Map<String, Object> statis = statis(currPlayer.getId(), TimeUtil.getDayBegin().getTime(), TimeUtil.getDayEnd().getTime());
        data.put("today",statis==null?0:statis.get("money"));

//        statis = statis(currPlayer.getId(), TimeUtil.getBeginDayOfYesterday().getTime(), TimeUtil.getEndDayOfYesterday().getTime());
//        data.put("yesterday",statis==null?0:statis.get("money"));
//        statis = statis2(currPlayer.getId());
//        data.put("lastWeek",statis==null?0:statis.get("money"));

        //本周从配置的时间开始算到现在
        Integer switchRebate = systemConfig.getSwitchRebate();
        long week = TimeUtil.getBeginDayOfWeek().getTime();
        long configTime = 0;
        //获取今日星期
        int nowWeek = TimeUtil.getNowWeek();
        if(nowWeek==1){
            nowWeek=7;
        }else{
            nowWeek-=1;
        }
        Integer rebateWeek = systemConfig.getRebateWeek();
        if(switchRebate==1){
            //为开才使用配置的周时间
            String rebateTime = systemConfig.getRebateTime();
            //如果配置的周期>今日星期,获取上周的开始时间
            if(rebateWeek>nowWeek){
                week = TimeUtil.getBeginDayOfLastWeek().getTime();
            }
            if(rebateWeek>1){
                configTime +=TimeUtil.TIME_ONE_DAY*(rebateWeek-1);
                configTime +=TimeUtil.hmsToMills(rebateTime);
            }
        }

        statis = statis(currPlayer.getId(), week+configTime, System.currentTimeMillis());
        data.put("week",statis==null?0:statis.get("money"));
//        long lastWeekEnd = 0;
//        if(rebateWeek>nowWeek){
//            week = TimeUtil.getBeginDayOfLastWeek().getTime()-7*TimeUtil.TIME_ONE_DAY;
//            lastWeekEnd = TimeUtil.getEndDayOfLastWeek().getTime()-7*TimeUtil.TIME_ONE_DAY;
//        }else{
//            week = TimeUtil.getBeginDayOfLastWeek().getTime();
//            lastWeekEnd = TimeUtil.getEndDayOfLastWeek().getTime();
//        }
//
//        statis = statis(currPlayer.getId(), week+configTime, lastWeekEnd+configTime);
//        data.put("lastWeek",statis==null?0:statis.get("money"));
        BallBalanceChange lastByType = ballBalanceChangeService.findLastByType(currPlayer.getId(), 5);
        if(lastByType!=null){
            data.put("lastWeek",lastByType.getChangeMoney());
        }else{
            data.put("lastWeek",0);
        }
        statis = statis2(currPlayer.getId());
        data.put("total",statis==null?0:statis.get("money"));
        return BaseResponse.successWithData(data);
    }

    private Map<String,Object> statis(Long pid,Long begin,Long end){
        return mapper.statis(pid,begin,end);
    }
    private Map<String,Object> statis2(Long pid){
        return mapper.statis2(pid);
    }

    @Override
    public BallLoggerBack insert(BallLoggerBack loggerBet) {
        save(loggerBet);
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        if(systemConfig.getTodoModel()==1){
            //提示WS
            String superTree = loggerBet.getSuperTree();
            if(superTree.equals("0")){
            }else if(superTree.equals("_")){
            }else{
                String[] split = superTree.split("_");
                BallPlayer superPlayer = basePlayerService.findById(Long.parseLong(split[1]));
                webSocketManager.sendMessage(superPlayer.getUsername(),MessageResponse.builder()
                        .data("")
                        .type(MessageResponse.DEEP_TYPE_T_4)
                        .build());
            }
        }else{
            webSocketManager.sendMessage(null,MessageResponse.builder()
                    .data("")
                    .type(MessageResponse.DEEP_TYPE_T_4)
                    .build());
        }
        return loggerBet;
    }

    @Override
    public boolean edit(BallLoggerBack ballLoggerBack) {
        return updateById(ballLoggerBack);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse draw(BallPlayer currentUser, Long id) {
        synchronized (id){
            //提取佣金
            BallLoggerBack ballLoggerBack = mapper.selectById(id);
            if(ballLoggerBack.getStatus()!=1){
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("", "alreadyExtracted"));
            }
            while(true){
                //修改余额
                BallPlayer editPlayer = BallPlayer.builder()
                        .balance(currentUser.getBalance()+ballLoggerBack.getMoney())
                        .version(currentUser.getVersion())
                        .build();
                int balanceChangeType=0;
                if(ballLoggerBack.getType()==2){
                    //盈利返
                    balanceChangeType = 5;
                }else if(ballLoggerBack.getType()==3){
                    //充值返
                    balanceChangeType = 21;
                    long a = currentUser.getCumulativeDiscount()==null?0:currentUser.getCumulativeDiscount();
                    editPlayer.setCumulativeDiscount(a+ballLoggerBack.getMoney());
                }
                editPlayer.setId(currentUser.getId());
                boolean b = basePlayerService.editAndClearCache(editPlayer, currentUser);
                if(b){
                    //账变
                    ballBalanceChangeService.insert(BallBalanceChange.builder()
                            .playerId(currentUser.getId())
                            .accountType(currentUser.getAccountType())
                            .balanceChangeType(balanceChangeType)
                            .createdAt(System.currentTimeMillis())
                            .dnedMoney(editPlayer.getBalance())
                            .initMoney(currentUser.getBalance())
                            .changeMoney(ballLoggerBack.getMoney())
                            .build());
                    //修改状态为已提取
                    edit(BallLoggerBack.builder()
                            .id(ballLoggerBack.getId())
                            .status(2)
                            .build());
                    break;
                }else{
                    currentUser = basePlayerService.findById(currentUser.getId());
                }
            }
            return BaseResponse.successWithMsg("ok");
        }
    }

    @Override
    public BallLoggerBack statis(ReportDataRequest reportDataRequest,int type) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.select("sum(money) as money");
        queryWrapper.eq("account_type",2);
        queryWrapper.eq("type",type);
        BallPlayerServiceImpl.queryByTime(queryWrapper,reportDataRequest.getTime(),reportDataRequest.getBegin(),reportDataRequest.getEnd());
        List<BallLoggerBack> list = list(queryWrapper);
        if(list==null||list.isEmpty()||list.get(0)==null){
            return BallLoggerBack.builder()
                    .money(0L)
                    .build();
        }
        return list.get(0);
    }

    @Override
    public void settlementOnWeek() {
        //查询所有未结算佣金,自动结算的
        QueryWrapper<BallLoggerBack> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("sum(money) money,player_id");
        queryWrapper.eq("type",2);
        queryWrapper.eq("status",3);
        queryWrapper.groupBy("player_id");
        List<BallLoggerBack> list = list(queryWrapper);
        for(BallLoggerBack item:list){
            //提取佣金
            BallPlayer currentUser = basePlayerService.findById(item.getPlayerId());
            while(true){
                //修改余额
                BallPlayer editPlayer = BallPlayer.builder()
                        .balance(currentUser.getBalance()+item.getMoney())
                        .version(currentUser.getVersion())
                        .build();
                    long a = currentUser.getCumulativeActivity()==null?0:currentUser.getCumulativeActivity();
                editPlayer.setCumulativeActivity(a+item.getMoney());
                editPlayer.setId(currentUser.getId());
                boolean b = basePlayerService.editAndClearCache(editPlayer, currentUser);
                if(b){
                    //账变
                    ballBalanceChangeService.insert(BallBalanceChange.builder()
                            .playerId(currentUser.getId())
                            .accountType(currentUser.getAccountType())
                            .userId(currentUser.getUserId())
                            .username(currentUser.getUsername())
                            .parentId(currentUser.getSuperiorId())
                            .superTree(currentUser.getSuperTree())
                            .balanceChangeType(5)
                            .createdAt(System.currentTimeMillis())
                            .dnedMoney(editPlayer.getBalance())
                            .initMoney(currentUser.getBalance())
                            .changeMoney(item.getMoney())
                            .build());
                    //修改状态为已提取
                    UpdateWrapper updateWrapper = new UpdateWrapper();
                    updateWrapper.eq("player_id",currentUser.getId());
                    updateWrapper.eq("status",3);
                    edit(updateWrapper,BallLoggerBack.builder()
                            .status(2)
                            .build());
                    break;
                }else{
                    currentUser = basePlayerService.findById(currentUser.getId());
                }
            }
        }
    }

    @Override
    public SearchResponse<BallLoggerBack> searchUnSettlement(BallLoggerBack queryParam, Integer pageNo, Integer pageSize, BallAdmin currentUser) {
        QueryWrapper<BallLoggerBack> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("game_id,sum(money) money,count(id) id,count(distinct player_id) player_id,created_at");
        queryWrapper.eq("status",3);
        queryWrapper.eq("type",2);
        queryWrapper.groupBy("game_id");
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        if(systemConfig.getTodoModel()==1&&currentUser.getTodoAll()==0){
            //指定代理模式，只查代理下的
            if(StringUtils.isBlank(currentUser.getPlayerName())){
                queryWrapper.eq("player_id",-100);
            }else{
                //in
                String[] split = currentUser.getPlayerName().split(",");
                List<String> strings = Arrays.asList(split);
                queryWrapper.in("top_username",strings);
//                String playerName = currentUser.getPlayerName();
//                BallPlayer byUsername = basePlayerService.findByUsername(playerName);
//                if(byUsername!=null){
//                    queryWrapper.likeRight("super_tree",byUsername.getSuperTree()+byUsername.getId()+"\\_");
//                }else{
//                    queryWrapper.eq("player_id",-100);
//                }
            }
        }
        SearchResponse<BallLoggerBack> response = new SearchResponse<>();
        Page<BallLoggerBack> page = new Page<>(pageNo, pageSize);
        IPage<BallLoggerBack> pages = page(page, queryWrapper);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public List<BallLoggerBack> searchUnSettlement(BallLoggerBack queryParam, BallAdmin currentUser) {
        QueryWrapper<BallLoggerBack> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("game_id,sum(money) money,count(id) id,count(distinct player_id) player_id,created_at");
        queryWrapper.eq("status",3);
        queryWrapper.eq("type",2);
        BallPlayer byUsername = basePlayerService.findByUsername(queryParam.getPlayerName());
        queryWrapper.eq("player_id",byUsername.getId());
//        queryWrapper.in("player_id",new Long[]{byUsername.getId(),byUsername.getSuperiorId()});
        queryWrapper.groupBy("game_id");
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        if(systemConfig.getTodoModel()==1&&currentUser.getTodoAll()==0){
            //指定代理模式，只查代理下的
            if(StringUtils.isBlank(currentUser.getPlayerName())){
                queryWrapper.eq("player_id",-100);
            }else{
                //in
                String[] split = currentUser.getPlayerName().split(",");
                List<String> strings = Arrays.asList(split);
                queryWrapper.in("top_username",strings);
            }
        }
        return list(queryWrapper);
    }

    @Override
    public SearchResponse<BallLoggerBack> searchUnSettlementByPlayer(BallLoggerBack queryParam, Integer pageNo, Integer pageSize, BallAdmin currentUser) {
        QueryWrapper<BallLoggerBack> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("game_id,sum(money) money,count(id) id,player_name");
        queryWrapper.eq("status",3);
        queryWrapper.eq("type",2);
        queryWrapper.eq("game_id",queryParam.getGameId());
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        if(systemConfig.getTodoModel()==1&&currentUser.getTodoAll()==0){
            //指定代理模式，只查代理下的
            if(StringUtils.isBlank(currentUser.getPlayerName())){
                queryWrapper.eq("player_id",-100);
            }else{
                String playerName = currentUser.getPlayerName();
                BallPlayer byUsername = basePlayerService.findByUsername(playerName);
                if(byUsername!=null){
                    queryWrapper.likeRight("super_tree",byUsername.getSuperTree()+byUsername.getId()+"\\_");
                }else{
                    queryWrapper.eq("player_id",-100);
                }
            }
        }
        queryWrapper.groupBy("player_id");
        SearchResponse<BallLoggerBack> response = new SearchResponse<>();
        Page<BallLoggerBack> page = new Page<>(pageNo, pageSize);
        IPage<BallLoggerBack> pages = page(page, queryWrapper);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public synchronized BaseResponse settlementOnWeek(Long gameId) {
        //查询所有未结算佣金,自动结算的
        QueryWrapper<BallLoggerBack> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("sum(money) money,player_id");
        queryWrapper.eq("type",2);
        queryWrapper.eq("status",3);
        queryWrapper.eq("game_id",gameId);
        queryWrapper.groupBy("player_id");
        List<BallLoggerBack> list = list(queryWrapper);
        for(BallLoggerBack item:list){
            //提取佣金
            BallPlayer currentUser = basePlayerService.findById(item.getPlayerId());
            while(true){
                //修改余额
                BallPlayer editPlayer = BallPlayer.builder()
                        .balance(currentUser.getBalance()+item.getMoney())
                        .version(currentUser.getVersion())
                        .build();
                long a = currentUser.getCumulativeActivity()==null?0:currentUser.getCumulativeActivity();
                editPlayer.setCumulativeActivity(a+item.getMoney());
                editPlayer.setId(currentUser.getId());
                boolean b = basePlayerService.editAndClearCache(editPlayer, currentUser);
                if(b){
                    //账变
                    ballBalanceChangeService.insert(BallBalanceChange.builder()
                            .playerId(currentUser.getId())
                            .accountType(currentUser.getAccountType())
                            .userId(currentUser.getUserId())
                            .username(currentUser.getUsername())
                            .parentId(currentUser.getSuperiorId())
                            .superTree(currentUser.getSuperTree())
                            .balanceChangeType(5)
                            .createdAt(System.currentTimeMillis())
                            .dnedMoney(editPlayer.getBalance())
                            .initMoney(currentUser.getBalance())
                            .changeMoney(item.getMoney())
                            .build());
                    break;
                }else{
                    currentUser = basePlayerService.findById(currentUser.getId());
                }
            }
        }
        //修改状态为已提取
        UpdateWrapper updateWrapper = new UpdateWrapper();
        updateWrapper.eq("game_id",gameId);
        updateWrapper.eq("status",3);
        edit(updateWrapper,BallLoggerBack.builder()
                .status(2)
                .build());
        return BaseResponse.successWithData(MapUtil.newMap("sucmsg","e61"));
    }

    @Override
    public int editMult(UpdateWrapper update, BallLoggerBack build) {
        return baseMapper.update(build,update);
    }

    private void edit(UpdateWrapper updateWrapper,BallLoggerBack edit){
        update(edit,updateWrapper);
    }
}
