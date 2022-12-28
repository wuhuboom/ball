package com.oxo.ball.service.impl.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxo.ball.bean.dao.*;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.bean.dto.resp.admin.UndoTaskDto;
import com.oxo.ball.mapper.BallTodoMapper;
import com.oxo.ball.service.admin.*;
import com.oxo.ball.service.impl.BasePlayerService;
import com.oxo.ball.utils.BigDecimalUtil;
import com.oxo.ball.utils.MapUtil;
import com.oxo.ball.utils.ResponseMessageUtil;
import com.oxo.ball.ws.WebSocketManager;
import com.oxo.ball.ws.dto.MessageResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 反佣策略 服务实现类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Service
public class BallTodoServiceImpl extends ServiceImpl<BallTodoMapper, BallTodo> implements IBallTodoService {
    @Autowired
    IBallBonusConfigService bonusConfigService;
    @Autowired
    BasePlayerService basePlayerService;
    @Autowired
    IBallBalanceChangeService balanceChangeService;
    @Autowired
    IBallLoggerRebateService loggerRebateService;
    @Autowired
    WebSocketManager webSocketManager;
    @Autowired
    IBallSystemConfigService systemConfigService;

    @Override
    public SearchResponse<BallTodo> search(BallTodo queryParam, Integer pageNo, Integer pageSize, BallAdmin currentUser) {
        SearchResponse<BallTodo> response = new SearchResponse<>();
        Page<BallTodo> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallTodo> query = new QueryWrapper<>();
        if(queryParam.getStatus()!=null){
            query.eq("status",queryParam.getStatus());
        }
        if(!StringUtils.isBlank(queryParam.getName())){
            query.eq("name",queryParam.getName());
        }
        //查询模式 0 权限 1代理
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        if(systemConfig.getTodoModel()==1&&currentUser.getTodoAll()==0){
            if(StringUtils.isBlank(currentUser.getPlayerName())){
                query.eq("player_id",-100);
            }else{
                //in
                String[] split = currentUser.getPlayerName().split(",");
                List<String> strings = Arrays.asList(split);
                query.in("top_username",strings);
            }
        }
        IPage<BallTodo> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public List<BallTodo> search(BallTodo queryParam, BallAdmin currentUser) {
        QueryWrapper<BallTodo> query = new QueryWrapper<>();
        query.eq("status",0);
        BallPlayer byUsername = basePlayerService.findByUsername(queryParam.getPlayerName());
        query.eq("player_id",byUsername.getId());
//        query.in("player_id",new Long[]{byUsername.getId(),byUsername.getSuperiorId()});
        //查询模式 0 权限 1代理
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        if(systemConfig.getTodoModel()==1&&currentUser.getTodoAll()==0){
            if(StringUtils.isBlank(currentUser.getPlayerName())){
                query.eq("player_id",-100);
            }else{
                //in
                String[] split = currentUser.getPlayerName().split(",");
                List<String> strings = Arrays.asList(split);
                query.in("top_username",strings);
            }
        }
        return list(query);
    }

    @Override
    public BallTodo insert(BallTodo todo) {
        todo.setCreatedAt(System.currentTimeMillis());
        boolean save = save(todo);
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        if(systemConfig.getTodoModel()==1){
            String superTree = todo.getSuperTree();
            if(superTree.equals("0")){
            }else if(superTree.equals("_")){
            }else {
                String[] split = superTree.split("_");
                BallPlayer superPlayer = basePlayerService.findById(Long.parseLong(split[1]));
                webSocketManager.sendMessage(superPlayer.getUsername(),MessageResponse.builder()
                        .type(MessageResponse.DEEP_TYPE_T_5)
                        .build());
            }

        }else{
            webSocketManager.sendMessage(null,MessageResponse.builder()
                    .type(MessageResponse.DEEP_TYPE_T_5)
                    .build());
        }
        return todo;
    }

    @Override
    public Boolean delete(Long id) {
        return removeById(id);
    }

    @Override
    public synchronized BaseResponse edit(BallTodo todo, BallAdmin currentUser) {
        todo.setUpdatedAt(System.currentTimeMillis());
        todo.setOperUser(currentUser.getUsername());
        BallTodo info = info(todo.getId());
        if(info.getStatus()==1){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e47"));
        }
        if(todo.getStatus()==1){
            BallPlayer dbPlayer = basePlayerService.findById(todo.getPlayerId());
            Long realMoney = todo.getBonusMoney()*BigDecimalUtil.PLAYER_MONEY_UNIT;
            while (true){
                BallPlayer edit = BallPlayer.builder()
                        .version(dbPlayer.getVersion())
                        .balance(dbPlayer.getBalance()+realMoney)
                        .build();
                edit.setId(dbPlayer.getId());
                boolean b = basePlayerService.editAndClearCache(edit, dbPlayer);
                if(b){
                    balanceChangeService.insert(BallBalanceChange.builder()
                            .playerId(dbPlayer.getId())
                            .accountType(dbPlayer.getAccountType())
                            .userId(dbPlayer.getUserId())
                            .parentId(dbPlayer.getSuperiorId())
                            .username(dbPlayer.getUsername())
                            .superTree(dbPlayer.getSuperTree())
                            .initMoney(dbPlayer.getBalance())
                            .changeMoney(realMoney)
                            .dnedMoney(edit.getBalance())
                            .createdAt(System.currentTimeMillis())
                            //奖金
                            .balanceChangeType(16)
                            .build());
                    break;
                }else{
                    dbPlayer = basePlayerService.findById(dbPlayer.getId());
                }
            }
        }
        boolean b = updateById(todo);
        return b?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("failed");
    }

    @Override
    public BallTodo info(Long id) {
        BallTodo byId = getById(id);
        BallBonusConfig byId1 = bonusConfigService.findById(byId.getBonusId());
        byId.setBonusConfig(byId1);
        return byId;
    }

    @Override
    public BaseResponse unCheck() {
        //奖金待办
        QueryWrapper query = new QueryWrapper();
        query.eq("status",0);
        int count = count(query);

        //充值待办优惠
        count+=loggerRebateService.countUnDo();
        return BaseResponse.successWithData(MapUtil.newMap("count",count));
    }

    @Override
    public List<UndoTaskDto> currentUnDo() {
        List<UndoTaskDto> list = new ArrayList<>();
        int i = loggerRebateService.countUnDo();
        if(i>0){
            list.add(UndoTaskDto.builder()
                    .type(1)
                    .count(i)
                    .name("充值优惠待办")
                    .build());
        }
        QueryWrapper query = new QueryWrapper();
        query.eq("status",0);
        i = count(query);
        if(i>0){
            list.add(UndoTaskDto.builder()
                    .type(2)
                    .count(i)
                    .name("奖金待办")
                    .build());
        }
        return list;
    }

}
