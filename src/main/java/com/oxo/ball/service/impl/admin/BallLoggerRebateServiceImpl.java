package com.oxo.ball.service.impl.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxo.ball.bean.dao.*;
import com.oxo.ball.bean.dto.queue.MessageQueueDTO;
import com.oxo.ball.bean.dto.req.report.ReportDataRequest;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.mapper.BallLoggerBehalfMapper;
import com.oxo.ball.service.IBasePlayerService;
import com.oxo.ball.service.IMessageQueueService;
import com.oxo.ball.service.admin.IBallApiConfigService;
import com.oxo.ball.service.admin.IBallLoggerHandsupService;
import com.oxo.ball.service.admin.IBallLoggerRebateService;
import com.oxo.ball.service.admin.IBallSystemConfigService;
import com.oxo.ball.utils.*;
import com.oxo.ball.ws.WebSocketManager;
import com.oxo.ball.ws.dto.MessageResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 充值优惠 服务实现类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Service
public class BallLoggerRebateServiceImpl extends ServiceImpl<BallLoggerBehalfMapper, BallLoggerRebate> implements IBallLoggerRebateService {

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    IBasePlayerService basePlayerService;
    @Autowired
    private BallBalanceChangeServiceImpl ballBalanceChangeService;
    @Autowired
    IBallLoggerHandsupService loggerHandsupService;
    @Autowired
    WebSocketManager webSocketManager;
    @Autowired
    IBallSystemConfigService systemConfigService;
    @Autowired
    private IMessageQueueService messageQueueService;
    @Autowired
    IBallApiConfigService apiConfigService;

    @Override
    public SearchResponse<BallLoggerRebate> search(BallLoggerRebate queryParam, Integer pageNo, Integer pageSize, BallAdmin currentUser) {
        SearchResponse<BallLoggerRebate> response = new SearchResponse<>();
        Page<BallLoggerRebate> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallLoggerRebate> query = new QueryWrapper<>();
        if(queryParam.getStatus()!=null){
            query.eq("status",queryParam.getStatus());
        }
        if(queryParam.getOrderNo()!=null){
            query.eq("order_no",queryParam.getOrderNo());
        }
        if(queryParam.getType()!=null){
            query.eq("type",queryParam.getType());
        }
        if(queryParam.getPayTypeOnff()!=null){
            query.eq("pay_type_onff",queryParam.getPayTypeOnff());
        }
        if(!StringUtils.isBlank(queryParam.getBegin())){
            try {
                long timeStamp = TimeUtil.stringToTimeStamp(queryParam.getBegin(), TimeUtil.TIME_YYYY_MM_DD);
                query.ge("created_at",timeStamp);
            } catch (ParseException e) {
            }
        }
        if(!StringUtils.isBlank(queryParam.getEnd())){
            try {
                long timeStamp = TimeUtil.stringToTimeStamp(queryParam.getEnd(), TimeUtil.TIME_YYYY_MM_DD);
                query.le("created_at",timeStamp+TimeUtil.TIME_ONE_DAY);
            } catch (ParseException e) {
            }
        }
        BallPlayer playerTree = null;
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        playerTree = todoModel(queryParam, currentUser, query, playerTree, systemConfig);
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
            }
        }
        query.orderByDesc("id");
        IPage<BallLoggerRebate> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public List<BallLoggerRebate> search(BallLoggerRebate queryParam, BallAdmin currentUser) {
        QueryWrapper<BallLoggerRebate> query = new QueryWrapper<>();
        query.eq("status",1);
        query.eq("type",99);
        BallPlayer byUsername = basePlayerService.findByUsername(queryParam.getPlayerName());
        query.and(QueryWrapper -> QueryWrapper.eq("player_id",byUsername.getId())
                .or()
                .eq("from_name",byUsername.getUsername()));
        BallPlayer playerTree = null;
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        todoModel2(queryParam, currentUser, query, playerTree, systemConfig);
        //查全部下级
        query.orderByDesc("id");
        return list(query);
    }

    @Override
    public SearchResponse<BallLoggerRebate> searchRecharge(BallLoggerRebate queryParam, Integer pageNo, Integer pageSize, BallAdmin currentUser) {
        SearchResponse<BallLoggerRebate> response = new SearchResponse<>();
        Page<BallLoggerRebate> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallLoggerRebate> query = new QueryWrapper<>();
        if(queryParam.getStatus()!=null){
            query.eq("status",queryParam.getStatus());
        }
        if(queryParam.getOrderNo()!=null){
            query.eq("order_no",queryParam.getOrderNo());
        }
        if(queryParam.getType()!=null){
            query.eq("type",queryParam.getType());
        }else{
            query.ne("type",99);
        }
        if(queryParam.getPayTypeOnff()!=null){
            query.eq("pay_type_onff",queryParam.getPayTypeOnff());
        }
        if(!StringUtils.isBlank(queryParam.getBegin())){
            try {
                long timeStamp = TimeUtil.stringToTimeStamp(queryParam.getBegin(), TimeUtil.TIME_YYYY_MM_DD);
                query.ge("created_at",timeStamp);
            } catch (ParseException e) {
            }
        }
        if(!StringUtils.isBlank(queryParam.getEnd())){
            try {
                long timeStamp = TimeUtil.stringToTimeStamp(queryParam.getEnd(), TimeUtil.TIME_YYYY_MM_DD);
                query.le("created_at",timeStamp+TimeUtil.TIME_ONE_DAY);
            } catch (ParseException e) {
            }
        }
        BallPlayer playerTree = null;
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        playerTree = todoModel(queryParam, currentUser, query, playerTree, systemConfig);
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
            }
        }
        query.orderByDesc("id");
        IPage<BallLoggerRebate> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public List<BallLoggerRebate> searchRecharge2(BallLoggerRebate queryParam, BallAdmin currentUser) {
        QueryWrapper<BallLoggerRebate> query = new QueryWrapper<>();
        query.eq("status",1);
        query.ne("type",99);
        BallPlayer byUsername = basePlayerService.findByUsername(queryParam.getPlayerName());
//        query.in("player_id",new Long[]{byUsername.getId(),byUsername.getSuperiorId()});
        BallPlayer playerTree = null;
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        playerTree = todoModel(queryParam, currentUser, query, playerTree, systemConfig);
        query.orderByDesc("id");
        return list(query);
    }

    private BallPlayer todoModel(BallLoggerRebate queryParam, BallAdmin currentUser, QueryWrapper<BallLoggerRebate> query, BallPlayer playerTree, BallSystemConfig systemConfig) {
        if(!StringUtils.isBlank(queryParam.getPlayerName())){
            //代理模式
            if(systemConfig.getTodoModel()==1&&currentUser.getTodoAll()==0){
                if(StringUtils.isBlank(currentUser.getPlayerName())){
                    query.eq("player_id",-100);
                }else{
                    // 只能查自己下级
                    playerTree = basePlayerService.findByUsername(queryParam.getPlayerName());
                    if(playerTree==null){
                        query.eq("player_id",-100);
                    }else{
                        query.eq("player_name",queryParam.getPlayerName());
                    }
                }
            }else {
                //一般模式
                if (queryParam.getTreeType() != null) {
                    playerTree = basePlayerService.findByUsername(queryParam.getPlayerName());
                } else {
                    query.eq("player_name",queryParam.getPlayerName());
                }
            }
        }else{
            if(systemConfig.getTodoModel()==1&&currentUser.getTodoAll()==0) {
                if(!StringUtils.isBlank(currentUser.getPlayerName())){
                    //如果没有配置用户名,则查自己名下的
                    //in
                    String[] split = currentUser.getPlayerName().split(",");
                    List<String> strings = Arrays.asList(split);
                    query.in("top_username",strings);
                }else{
                    //没给代理查询权限，也不配置代理，返回空
                    query.eq("top_username","-1");
                }
            }
        }
        return playerTree;
    }

    private BallPlayer todoModel2(BallLoggerRebate queryParam, BallAdmin currentUser, QueryWrapper<BallLoggerRebate> query, BallPlayer playerTree, BallSystemConfig systemConfig) {
        if(!StringUtils.isBlank(queryParam.getPlayerName())){
            //代理模式
            if(systemConfig.getTodoModel()==1&&currentUser.getTodoAll()==0){
                if(StringUtils.isBlank(currentUser.getPlayerName())){
                    query.eq("player_id",-100);
                }else{
                    // 只能查自己下级
                    playerTree = basePlayerService.findByUsername(queryParam.getPlayerName());
                    if(playerTree==null){
                        query.eq("player_id",-100);
                    }
                }
            }else {
                //一般模式
                if (queryParam.getTreeType() != null) {
                    playerTree = basePlayerService.findByUsername(queryParam.getPlayerName());
                }
            }
        }else{
            if(systemConfig.getTodoModel()==1&&currentUser.getTodoAll()==0) {
                if(!StringUtils.isBlank(currentUser.getPlayerName())){
                    //如果没有配置用户名,则查自己名下的
                    //in
                    String[] split = currentUser.getPlayerName().split(",");
                    List<String> strings = Arrays.asList(split);
                    query.in("top_username",strings);
                }else{
                    //没给代理查询权限，也不配置代理，返回空
                    query.eq("top_username","-1");
                }
            }
        }
        return playerTree;
    }

    @Override
    public BallLoggerRebate findById(Long id) {
        return getById(id);
    }

    @Override
    public BallLoggerRebate insert(BallLoggerRebate loggerBet) {
        save(loggerBet);
        //发送消息提示
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        if(systemConfig.getTodoModel()==1){
            if(loggerBet.getType()==99){
                webSocketManager.sendMessage(loggerBet.getTopUsername(),MessageResponse.builder()
                        .type(MessageResponse.DEEP_TYPE_T_3)
                        .build());
            }else{
                webSocketManager.sendMessage(loggerBet.getTopUsername(),MessageResponse.builder()
                        .type(MessageResponse.DEEP_TYPE_T_2)
                        .build());
            }
        }else{
            webSocketManager.sendMessage(null,MessageResponse.builder()
                    .type(MessageResponse.DEEP_TYPE_T_2)
                    .build());
        }
        return loggerBet;
    }

    @Override
    public Boolean edit(BallLoggerRebate loggerWithdrawal) {
        return updateById(loggerWithdrawal);
    }

    @Override
    public synchronized  BaseResponse settlement(BallLoggerRebate loggerRebate) {
        BallLoggerRebate byId = findById(loggerRebate.getId());
        if(byId.getStatus()==2||byId.getStatus()==3){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e27"));
        }
        BallPlayer player = basePlayerService.findById(byId.getPlayerId());
        if(loggerRebate.getStatus()==2){
            Double finald = BigDecimalUtil.mul(Double.valueOf(loggerRebate.getMoneyParam()),BigDecimalUtil.PLAYER_MONEY_UNIT);
            long finalMoney = finald.longValue();
            while (true){
                BallPlayer ballPlayer = BallPlayer.builder()
                        .balance(player.getBalance()+finalMoney)
                        .version(player.getVersion())
                        .build();
                ballPlayer.setId(player.getId());
                boolean b = basePlayerService.editAndClearCache(ballPlayer, player);
                if(b){
                    int changeType = 0;
                    //TODO 充值优惠类型同步 0邀请首充 1 首充 2 活动 3次充 4固定日 99返佣
                    switch (byId.getType()){
                        case 99:
                            changeType=21;
                            break;
                        case 1:
                            changeType=24;
                            break;
                        case 2:
                            changeType=25;
                            break;
                        case 3:
                            changeType=26;
                            break;
                        case 4:
                            changeType=27;
                            break;
                        case 0:
                            changeType=28;
                            break;
                            default:
                                break;
                    }
                    //+充值优惠账变
                    BallBalanceChange saveChangeDiscount = BallBalanceChange.builder()
                            .playerId(player.getId())
                            .accountType(player.getAccountType())
                            .userId(player.getUserId())
                            .parentId(player.getSuperiorId())
                            .username(player.getUsername())
                            .superTree(player.getSuperTree())
                            .initMoney(player.getBalance())
                            .changeMoney(finalMoney)
                            .dnedMoney(player.getBalance()+finalMoney)
                            .createdAt(System.currentTimeMillis())
                            .balanceChangeType(changeType)
                            .orderNo(byId.getOrderNo())
                            .build();
                    ballBalanceChangeService.insert(saveChangeDiscount);
                    break;
                }else {
                    player = basePlayerService.findById(player.getId());
                }
            }
        }
        BallLoggerRebate edit = BallLoggerRebate.builder()
                .status(loggerRebate.getStatus())
                .remark(loggerRebate.getRemark())
                .build();
        edit.setId(byId.getId());
        edit.setUpdatedAt(System.currentTimeMillis());
        edit(edit);
        //发送会员群消息
        sendPlayerChat(byId,player);
        return BaseResponse.successWithData(MapUtil.newMap("sucmsg","e62"));
    }

    @Override
    public int countUnDo() {
        QueryWrapper query = new QueryWrapper();
        query.eq("status",1);
        return count(query);
    }

    @Override
    public int editMult(UpdateWrapper update, BallLoggerRebate build) {
        return baseMapper.update(build,update);
    }

    @Override
    public BallLoggerRebate statisDiscount(ReportDataRequest reportDataRequest) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.select("sum(money) as money");
        if(reportDataRequest.getUserId()!=null){
            queryWrapper.eq("player_id",reportDataRequest.getUserId());
        }
        queryWrapper.eq("status",2);
        queryWrapper.ne("type",99);
        BallPlayerServiceImpl.queryByTime(queryWrapper,reportDataRequest.getTime(),reportDataRequest.getBegin(),reportDataRequest.getEnd());
        List<BallLoggerRebate> list = list(queryWrapper);
        if(list==null||list.isEmpty()||list.get(0)==null){
            return BallLoggerRebate.builder()
                    .money(0L)
                    .build();
        }
        return list.get(0);
    }

    public BallLoggerRebate findByTypeAndOrderno(Long playerId, Integer type, Long orderNo){
        QueryWrapper queryWrapper  = new QueryWrapper();
        queryWrapper.eq("player_id",playerId);
        queryWrapper.eq("order_no",orderNo);
        queryWrapper.eq("type",type);
        return getOne(queryWrapper);
    }

    public void sendPlayerChat(BallLoggerRebate byId, BallPlayer player) {
        //TODO 给TG玩家群发消息
//        if(true){
//            return;
//        }
        //发送会员群消息
        //充值小于5000不发
        if(byId.getMoneyReal()<500000){
            return;
        }
        String format = null;
        BallApiConfig apiConfig = apiConfigService.getApiConfig();
        switch (byId.getType()){
            case 1:
                if(player.getSuperiorId()!=null && !StringUtils.isBlank(player.getSuperiorName())){
                    //查询本单的返佣奖励type=99，order=充值order
                    BallLoggerRebate byTypeAndOrderno = findByTypeAndOrderno(player.getSuperiorId(), 99, byId.getOrderNo());
                    /*
                    "@all\n" +
                            "恭喜会员 {0}\n" +
                            "新邀请一位新会员首次充值{1}\n" +
                            "新会员 {2} 获得{3}奖励\n" +
                            (byTypeAndOrderno!=null?"邀请人获得{4}奖励":"{4}") +
                            "\n同时获得新会员每日收益的10%佣金\n" +
                            "在此鼓励大家多多加油希望未来越来越好"
                            */
                    if(StringUtils.isBlank(apiConfig.getFirstRecharge())){
                        return;
                    }
                    if(byTypeAndOrderno!=null){
                        format = MessageFormat.format(apiConfig.getFirstRecharge(),
                                UUIDUtil.getStarString2(player.getSuperiorName(),1,1),
                                String.valueOf(byId.getMoneyReal()/100),
                                UUIDUtil.getStarString2(byId.getPlayerName(),1,1),
                                String.valueOf(byId.getMoney()/100),
                                String.valueOf(byTypeAndOrderno!=null?byTypeAndOrderno.getMoney()/100:0)
                        );
                    }else{
                        format = MessageFormat.format(apiConfig.getFirstRecharge2(),
                                UUIDUtil.getStarString2(player.getSuperiorName(),1,1),
                                String.valueOf(byId.getMoneyReal()/100),
                                UUIDUtil.getStarString2(byId.getPlayerName(),1,1),
                                String.valueOf(byId.getMoney()/100)
                        );
                    }
                }
                break;
            case 2:
                break;
            case 3:
                /**
                 "@all\n" +
                                                 "恭喜会员 {0}\n" +
                                                 "再次充值获得{1}奖励\n" +
                                                 "\n" +
                                                 "在此鼓励大家多多加油希望未来越来越好"
                 */
                if(StringUtils.isBlank(apiConfig.getSecondRecharge())){
                    return;
                }
                format = MessageFormat.format(apiConfig.getSecondRecharge(),
                        UUIDUtil.getStarString2(byId.getPlayerName(),1,1),
                        String.valueOf(byId.getMoney() / 100)
                );
                break;
            case 4:
                /*"@all\n" +
                        "恭喜会员 {0}\n" +
                        "获得充值日{1}奖励\n" +
                        "\n" +
                        "在此鼓励大家多多加油希望未来越来越好"*/
                if(StringUtils.isBlank(apiConfig.getFixedRecharge())){
                    return;
                }
                format = MessageFormat.format(apiConfig.getFixedRecharge(),
                        UUIDUtil.getStarString2(byId.getPlayerName(),1,1),
                        String.valueOf(byId.getMoney() / 100)
                );
                break;
            case 99:
                break;
            default:
                break;
        }
        if(!StringUtils.isEmpty(format)){
            messageQueueService.putMessage(MessageQueueDTO.builder()
                    .type(MessageQueueDTO.TYPE_PLAYER_TG_CHAT)
                    .data(format)
                    .build());
        }
    }
}
