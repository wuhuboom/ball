package com.oxo.ball.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.oxo.ball.bean.dao.*;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.contant.LogsContant;
import com.oxo.ball.service.IBallVersionUpdateService;
import com.oxo.ball.service.IBasePlayerService;
import com.oxo.ball.service.admin.*;
import com.oxo.ball.utils.TimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
/**
 * 版本更新
 */
public class BallVersionUpdateImpl implements IBallVersionUpdateService {
    private Logger apiLog = LoggerFactory.getLogger(LogsContant.API_LOG);

    @Autowired
    IBallLoggerRechargeService loggerRechargeService;
    @Autowired
    IBasePlayerService basePlayerService;
    @Autowired
    IBallLoggerWithdrawalService loggerWithdrawalService;
    @Autowired
    IBallBetService ballBetService;
    @Autowired
    IBallLoggerBackService loggerBackService;
    @Autowired
    IBallLoggerRebateService loggerRebateService;
    @Autowired
    IBallLoggerHandsupService loggerHandsupService;
    @Override
    public void updateOn11() {
        apiLog.info("版本11更新数据开始");
        //提现和充值列表增加了 顶级总代和一级代理，需要对老数据进行更新
        int pageNo = 1;
        int count=0;
        try {
            while (true){
                SearchResponse<BallLoggerRecharge> search = loggerRechargeService.search(BallLoggerRecharge.builder()
                        .build(), pageNo++, 50);
                List<BallLoggerRecharge> results = search.getResults();
                if(results==null||results.isEmpty()||results.get(0)==null){
                    break;
                }
                for(BallLoggerRecharge item:results){
                    if(!StringUtils.isBlank(item.getTopUsername())||!StringUtils.isBlank(item.getFirstUsername())){
                        continue;
                    }
                    BallPlayer player = basePlayerService.findById(item.getPlayerId());
                    if(player==null){
                        continue;
                    }
                    String superTree = player.getSuperTree();
                    if(superTree.equals("0")){
                        continue;
                    }else if(superTree.equals("_")){
                        continue;
                    }else{
                        String[] split = superTree.split("_");
                        BallPlayer superPlayer = basePlayerService.findById(Long.parseLong(split[1]));
                        BallLoggerRecharge edit = BallLoggerRecharge.builder()
                                .id(item.getId())
                                .build();
                        edit.setTopUsername(superPlayer.getUsername());
                        if(split.length==2){
                            edit.setFirstUsername(superPlayer.getUsername());
                        }else if(split.length>2){
                            BallPlayer firstPlayer = basePlayerService.findById(Long.parseLong(split[2]));
                            edit.setFirstUsername(firstPlayer.getUsername());
                        }
                        boolean edit1 = loggerRechargeService.edit(edit);
                        if(edit1){
                            count++;
                        }
                    }
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }

        apiLog.info("充值订单更新了{}条数据",count);
        pageNo = 1;
        count = 0;
        try {
            while (true){
                SearchResponse<BallLoggerWithdrawal> search = loggerWithdrawalService.search(BallLoggerWithdrawal.builder().build(), pageNo++, 50);
                List<BallLoggerWithdrawal> results = search.getResults();
                if(results==null||results.isEmpty()||results.get(0)==null){
                    break;
                }
                for(BallLoggerWithdrawal item:results){
                    if(!StringUtils.isBlank(item.getTopUsername())||!StringUtils.isBlank(item.getFirstUsername())){
                        continue;
                    }
                    BallPlayer player = basePlayerService.findById(item.getPlayerId());
                    if(player==null){
                        continue;
                    }
                    String superTree = player.getSuperTree();
                    if(superTree.equals("0")){
                        continue;
                    }else if(superTree.equals("_")){
                        continue;
                    }else{
                        String[] split = superTree.split("_");
                        BallPlayer superPlayer = basePlayerService.findById(Long.parseLong(split[1]));
                        BallLoggerWithdrawal edit = BallLoggerWithdrawal.builder()
                                .id(item.getId())
                                .build();
                        edit.setTopUsername(superPlayer.getUsername());
                        if(split.length==2){
                            edit.setFirstUsername(superPlayer.getUsername());
                        }else if(split.length>2){
                            BallPlayer firstPlayer = basePlayerService.findById(Long.parseLong(split[2]));
                            edit.setFirstUsername(firstPlayer.getUsername());
                        }
                        loggerWithdrawalService.edit(edit);
                    }
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        apiLog.info("提现订单更新了{}条数据",count);
        apiLog.info("版本11更新数据结束");
    }

    @Override
    public void updateOn16() {
        apiLog.info("版本16更新数据开始");
        //玩家投注冻结
        //先清算冻结为0
        basePlayerService.clearFrozen();
        List<BallBet> ballBets = ballBetService.statisNotOpenForPlayer();
        int count = 0;
        for(BallBet item:ballBets){
            BallPlayer player = basePlayerService.findById(item.getPlayerId());
            while(true){
                BallPlayer edit = BallPlayer.builder()
                        .version(player.getVersion())
                        .frozenBet(item.getBetMoney())
                        .build();
                edit.setId(player.getId());
                boolean b = basePlayerService.editAndClearCache(edit, player);
                if(b){
                    count++;
                    break;
                }else{
                    player = basePlayerService.findById(item.getPlayerId());
                }
            }
        }
        apiLog.info("版本16更新冻结下注结束,更新数:{}",count);
        count=0;
        //玩家提现冻结
        List<BallLoggerWithdrawal> ballLoggerWithdrawals = loggerWithdrawalService.statisTotalNotForPlayer();
        for(BallLoggerWithdrawal item:ballLoggerWithdrawals){
            BallPlayer player = basePlayerService.findById(item.getPlayerId());
            while(true){
                BallPlayer edit = BallPlayer.builder()
                        .version(player.getVersion())
                        .frozenWithdrawal(item.getMoney())
                        .build();
                edit.setId(player.getId());
                boolean b = basePlayerService.editAndClearCache(edit, player);
                if(b){
                    count++;
                    break;
                }else{
                    player = basePlayerService.findById(item.getPlayerId());
                }
            }
        }
        apiLog.info("版本16更新提现冻结结束,更新数:{}",count);
    }

    @Override
    public void updateOn21() {
        apiLog.info("版本21更新数据开始");
        //下注表增加 顶级总代和一级代理，需要对老数据进行更新
        int pageNo = 1;
        int count=0;
        try {
            //订单太多,需要来个批量更新
            Map<Long,BallBet> editMap = new HashMap<>();
            while (true){
                SearchResponse<BallBet> search = ballBetService.search(BallBet.builder()
                        .build(), pageNo++, 500);
                List<BallBet> results = search.getResults();
                if(results==null||results.isEmpty()||results.get(0)==null){
                    break;
                }
                for(BallBet item:results){
                    if(!StringUtils.isBlank(item.getTopUsername())||!StringUtils.isBlank(item.getFirstUsername())){
                        continue;
                    }
                    BallPlayer player = basePlayerService.findById(item.getPlayerId());
                    if(player==null){
                        continue;
                    }
                    BallBet bet = editMap.get(item.getPlayerId());
                    if(bet!=null){
                        continue;
                    }
                    String superTree = player.getSuperTree();
                    if(superTree.equals("0")){
                        continue;
                    }else if(superTree.equals("_")){
                        continue;
                    }else{
                        String[] split = superTree.split("_");
                        BallPlayer superPlayer = basePlayerService.findById(Long.parseLong(split[1]));
                        BallBet edit = BallBet.builder()
                                .build();
                        edit.setTopUsername(superPlayer.getUsername());
                        boolean edited = false;
                        if(split.length==2){
                            edit.setFirstUsername(superPlayer.getUsername());
                            edited = true;
                        }else if(split.length>2){
                            BallPlayer firstPlayer = basePlayerService.findById(Long.parseLong(split[2]));
                            edited = true;
                            edit.setFirstUsername(firstPlayer.getUsername());
                        }
                        if(edited){
                            editMap.put(item.getPlayerId(),edit);
                        }
                    }
                }
            }
            for(Map.Entry<Long, BallBet> entity:editMap.entrySet()){
                UpdateWrapper update = new UpdateWrapper();
                update.eq("player_id",entity.getKey());
                count += ballBetService.editMult(update,BallBet.builder()
                        .firstUsername(entity.getValue().getFirstUsername())
                        .topUsername(entity.getValue().getTopUsername())
                        .build());
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }

        apiLog.info("下注订单更新了{}条数据",count);
    }

    @Override
    public void updateOn34() {
        //返利增加玩家账号,树
        int pageNo = 1;
        int count=0;
        try {
            //订单太多,需要来个批量更新
            Map<Long,BallLoggerBack> editMap = new HashMap<>();
            while (true){
                SearchResponse<BallLoggerBack> search = loggerBackService.search(BallLoggerBack.builder()
                        .status(3)
                        .build(), pageNo++, 500);
                List<BallLoggerBack> results = search.getResults();
                if(results==null||results.isEmpty()||results.get(0)==null){
                    break;
                }
                for(BallLoggerBack item:results){
                    if(!StringUtils.isBlank(item.getPlayerName())){
                        continue;
                    }
                    BallPlayer player = basePlayerService.findById(item.getPlayerId());
                    if(player==null){
                        continue;
                    }
                    BallLoggerBack loggerBack = editMap.get(item.getPlayerId());
                    if(loggerBack!=null){
                        continue;
                    }
                    BallLoggerBack edit = BallLoggerBack.builder()
                                .build();
                        edit.setPlayerName(player.getUsername());
                        edit.setSuperTree(player.getSuperTree());
                        editMap.put(item.getPlayerId(),edit);
                }
            }
            for(Map.Entry<Long, BallLoggerBack> entity:editMap.entrySet()){
                UpdateWrapper update = new UpdateWrapper();
                update.eq("player_id",entity.getKey());
                count += loggerBackService.editMult(update,BallLoggerBack.builder()
                        .playerName(entity.getValue().getPlayerName())
                        .superTree(entity.getValue().getSuperTree())
                        .build());
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }

        apiLog.info("盈利返利更新了{}条数据",count);
    }

    @Override
    public void updateOn41() {
        apiLog.info("版本41更新数据开始");
        //下注表增加 顶级总代和一级代理，需要对老数据进行更新
        int pageNo = 1;
        int count=0;
        try {
            //订单太多,需要来个批量更新
            Map<Long,BallLoggerRebate> editMap = new HashMap<>();
            while (true){
                SearchResponse<BallLoggerRebate> search = loggerRebateService.search(BallLoggerRebate.builder()
                        .build(), pageNo++, 500, null);
                List<BallLoggerRebate> results = search.getResults();
                if(results==null||results.isEmpty()||results.get(0)==null){
                    break;
                }
                for(BallLoggerRebate item:results){
                    if(!StringUtils.isBlank(item.getTopUsername())||!StringUtils.isBlank(item.getFirstUsername())){
                        continue;
                    }
                    BallPlayer player = basePlayerService.findById(item.getPlayerId());
                    if(player==null){
                        continue;
                    }
                    BallLoggerRebate bet = editMap.get(item.getPlayerId());
                    if(bet!=null){
                        continue;
                    }
                    String superTree = player.getSuperTree();
                    if(superTree.equals("0")){
                        continue;
                    }else if(superTree.equals("_")){
                        continue;
                    }else{
                        String[] split = superTree.split("_");
                        BallPlayer superPlayer = basePlayerService.findById(Long.parseLong(split[1]));
                        BallLoggerRebate edit = BallLoggerRebate.builder()
                                .build();
                        edit.setTopUsername(superPlayer.getUsername());
                        boolean edited = false;
                        if(split.length==2){
                            edit.setFirstUsername(superPlayer.getUsername());
                            edited = true;
                        }else if(split.length>2){
                            BallPlayer firstPlayer = basePlayerService.findById(Long.parseLong(split[2]));
                            edited = true;
                            edit.setFirstUsername(firstPlayer.getUsername());
                        }
                        if(edited){
                            editMap.put(item.getPlayerId(),edit);
                        }
                    }
                }
            }
            for(Map.Entry<Long, BallLoggerRebate> entity:editMap.entrySet()){
                UpdateWrapper update = new UpdateWrapper();
                update.eq("player_id",entity.getKey());
                count += loggerRebateService.editMult(update,BallLoggerRebate.builder()
                        .firstUsername(entity.getValue().getFirstUsername())
                        .topUsername(entity.getValue().getTopUsername())
                        .build());
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }

        apiLog.info("下注订单更新了{}条数据",count);
    }

    @Override
    public void updateOn43() throws ParseException {
        apiLog.info("版本43更新数据开始");
        long timeBegin = TimeUtil.stringToTimeStamp("2022-09-02",TimeUtil.TIME_YYYY_MM_DD);
        long timeEnd = TimeUtil.stringToTimeStamp("2022-09-03",TimeUtil.TIME_YYYY_MM_DD);
        List<BallLoggerRecharge> ballLoggerRecharges = loggerRechargeService.search43(timeBegin, timeEnd);
        int count=0;
        for(BallLoggerRecharge item:ballLoggerRecharges){
            BallPlayer player = basePlayerService.findById(item.getPlayerId());
            if(player.getFirstTopUpTime()==0){
                BallPlayer edit = BallPlayer.builder()
                        .firstTopUpTime(item.getCreatedAt())
                        .build();
                edit.setId(player.getId());
                if(basePlayerService.editAndClearCache(edit, player)){
                    count++;
                }
            }
        }
        apiLog.info("版本43更新数据结束,修正了{}条数据",count);
    }

    @Override
    public void updateOn69() {
        //修正玩家的累计手动上分
        int pageNo = 1;
        int count=0;
        try {
            while (true){
                SearchResponse<BallLoggerHandsup> search = loggerHandsupService.searchFixed(BallLoggerHandsup.builder()
                        .type(1)
                        .build(), pageNo++, 500);
                List<BallLoggerHandsup> results = search.getResults();
                if(results==null||results.isEmpty()||results.get(0)==null){
                    break;
                }
                for(BallLoggerHandsup item:results){
                    BallPlayer player = basePlayerService.findById(item.getPlayerId());
                    if(player.getArtificialAdd()>0){
                        continue;
                    }
                    if(player==null){
                        continue;
                    }
                    BallPlayer edit = BallPlayer.builder()
                            .artificialAdd(item.getMoney())
                            .build();
                    edit.setId(player.getId());
                    basePlayerService.editAndClearCache(edit,player);
                    count++;
                }
            }
            while (true){
                SearchResponse<BallLoggerHandsup> search = loggerHandsupService.searchFixed(BallLoggerHandsup.builder()
                        .type(0)
                        .build(), pageNo++, 500);
                List<BallLoggerHandsup> results = search.getResults();
                if(results==null||results.isEmpty()||results.get(0)==null){
                    break;
                }
                for(BallLoggerHandsup item:results){
                    BallPlayer player = basePlayerService.findById(item.getPlayerId());
                    if(player==null){
                        continue;
                    }
                    if(player.getArtificialSubtract()<0){
                        continue;
                    }
                    BallPlayer edit = BallPlayer.builder()
                            .artificialSubtract(item.getMoney())
                            .build();
                    edit.setId(player.getId());
                    basePlayerService.editAndClearCache(edit,player);
                    count++;
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }

        apiLog.info("更新了{}条玩家数据,累计手动上分",count);
    }

    @Override
    public void updateOn84() {
        //修正玩家的累计手动上分
        int pageNo = 1;
        int count=0;
        try {
            while (true){
                SearchResponse<BallLoggerRecharge> search = loggerRechargeService.searchFirst(BallLoggerRecharge.builder()
                        .build(), pageNo++, 100);
                List<BallLoggerRecharge> results = search.getResults();
                if(results==null||results.isEmpty()||results.get(0)==null){
                    break;
                }
                List<Long> ids = new ArrayList<>();
                for(BallLoggerRecharge item:results){
                    ids.add(item.getId());
                    count++;
                }
                loggerRechargeService.editFirst(ids);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }

        apiLog.info("更新了{}条玩家首充数据",count);
    }
}
