package com.oxo.ball.service.impl.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oxo.ball.bean.dao.BallPlayer;
import com.oxo.ball.bean.dao.BallVip;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.mapper.BallVipMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxo.ball.service.admin.IBallPlayerService;
import com.oxo.ball.service.admin.IBallVipService;
import com.oxo.ball.service.impl.BasePlayerService;
import com.oxo.ball.utils.BigDecimalUtil;
import com.oxo.ball.utils.RedisUtil;
import com.oxo.ball.utils.ResponseMessageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-24
 */
@Service
public class BallVipServiceImpl extends ServiceImpl<BallVipMapper, BallVip> implements IBallVipService {

    @Autowired
    RedisUtil redisUtil;
    @Autowired
    IBallPlayerService ballPlayerService;
    @Autowired
    BasePlayerService basePlayerService;
    @Override
    public SearchResponse<BallVip> search(BallVip queryParam, Integer pageNo, Integer pageSize) {
        SearchResponse<BallVip> response = new SearchResponse<>();
        Page<BallVip> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallVip> query = new QueryWrapper<>();
        if(queryParam.getStatus()!=null){
            query.eq("status",queryParam.getStatus());
        }
        IPage<BallVip> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public BallVip findById(Long id) {
        return getById(id);
    }

    @Override
    public List<BallVip> findByAll() {
        QueryWrapper query = new QueryWrapper();
        query.orderByAsc("level");
        return list(query);
    }

    @Override
    public BallVip insert(BallVip ballVip) {
        ballVip.setStatus(1);
        boolean save = save(ballVip);
        return ballVip;
    }

    @Override
    public Boolean delete(Long id) {
        return removeById(id);
    }

    @Override
    public BaseResponse edit(BallVip ballVip) {
        //判定升级设定是否大于前一个等级，或者小于后一个等级
        if(ballVip.getLevel()!=0){
            BallVip byLevela = findByLevel(ballVip.getLevel() - 1);
            BallVip byLevelc = findByLevel(ballVip.getLevel() + 1);

            if((byLevela.getUpTotal()!=0 && ballVip.getUpTotal()<byLevela.getUpTotal())
                    || (byLevelc!=null && byLevelc.getUpTotal()!=0 && ballVip.getUpTotal() > byLevelc.getUpTotal())){
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("", "e48"));
            }
            if((byLevela.getUpRw()!=0 && ballVip.getUpRw()<byLevela.getUpRw())
                    || (byLevelc!=null && byLevelc.getUpRw()!=0 && ballVip.getUpRw() > byLevelc.getUpRw())){
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("", "e48"));
            }
            double levelProfit = Double.valueOf(ballVip.getLevelProfit());
            double levelProfita = Double.valueOf(byLevela.getLevelProfit());
            double levelProfitc = byLevelc==null?0:Double.valueOf(byLevelc.getLevelProfit());
            if((levelProfita!=0 && levelProfit<levelProfita)
                    || (levelProfitc!=0 && levelProfit > levelProfitc)){
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("", "e49"));
            }
        }
        boolean b = updateById(ballVip);
        if(b){
            redisUtil.delKeys("ball_vip_level*");
        }
        return b?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("update failed~");
    }

    @Override
    public Boolean status(BallVip notice) {
        BallVip edit = BallVip.builder()
                .status(notice.getStatus())
                .build();
        edit.setId(notice.getId());
        redisUtil.delKeys("ball_vip_level*");
        return updateById(edit);
    }

    @Override
    @Cacheable(value = "ball_vip_level", key = "#vipLevel", unless = "#result == null")
    public BallVip findByLevel(Integer vipLevel) {
        QueryWrapper query = new QueryWrapper();
        query.eq("level",vipLevel);
        return getOne(query);
    }

    @Override
    public void checkLevel() {
        //等级检测,检测玩家余额是否小于等级配置
        List<BallVip> byAll = findByAll();
        Map<Integer,BallVip> vipMap = new HashMap<>();
        for(BallVip item:byAll){
            vipMap.put(item.getLevel(),item);
        }
        int pageNo = 1;
        while(true){
            SearchResponse<BallPlayer> search = ballPlayerService.search(BallPlayer.builder()
                    .accountType(2)
                    .build(), pageNo++, 500);
            if(search.getResults()==null||search.getResults().isEmpty()||search.getResults().get(0)==null){
                break;
            }
            for (BallPlayer player:search.getResults()){
                //VIP满足等级几？
                int level = 0;
                Long cumulativeTopUp = player.getCumulativeTopUp()+player.getArtificialAdd();
                Long cumulativeReflect = player.getCumulativeReflect()+player.getArtificialSubtract();
                level = checkPlayerVipLevel(byAll, player, level, cumulativeTopUp, cumulativeReflect);
                if(player.getVipLevel()!=level){
                    while(true) {
                        BallPlayer editPlayer = BallPlayer.builder()
                                .vipLevel(level)
                                .version(player.getVersion())
                                .build();
                        editPlayer.setId(player.getId());
                        boolean b = basePlayerService.editAndClearCache(editPlayer, player);
                        if(b){
                            break;
                        }else{
                            player = basePlayerService.findById(player.getId());
                        }
                    }
                }
//                //余额是否小于设定值
//                BallVip vip = vipMap.get(item.getVipLevel());
//                if(vip==null||vip.getBalance()==0){
//                    //没有设置条件,跳过
//                    break;
//                }
//                int level = 0;
//                if(item.getBalance()+item.getFrozenBet()<vip.getBalance()*BigDecimalUtil.PLAYER_MONEY_UNIT){
//                    //降级,符合几级的就降为几级
//                    while(true){
//                        BallVip ballVip = vipMap.get(vip.getLevel() - 1);
//                        if(item.getBalance()+item.getFrozenBet()<vip.getBalance()*BigDecimalUtil.PLAYER_MONEY_UNIT){
//                            vip = vipMap.get(ballVip.getLevel()-1);
//                            if(vip==null){
//                                level=0;
//                            }
//                            //继续降
//                            continue;
//                        }
//                        level = ballVip.getLevel();
//                        break;
//                    }
//                }else{
//                    //不用降级
//                    continue;
//                }

//                BallPlayer edit = BallPlayer.builder()
//                        .vipLevel(level)
//                        .build();
//                edit.setId(item.getId());
//                basePlayerService.editAndClearCache(edit,basePlayerService.findById(item.getId()));
            }
        }
    }

    public static int checkPlayerVipLevel(List<BallVip> byAll, BallPlayer player, int level, Long cumulativeTopUp, Long cumulativeReflect) {
        boolean isUpTotal;
        boolean isUpRw;
        boolean isBlance;
        for(BallVip item:byAll){
            //累计充值=人工+非人工
            if(item.getUpTotal()==0||cumulativeTopUp>=item.getUpTotal()*BigDecimalUtil.PLAYER_MONEY_UNIT){
                isUpTotal = true;
            }else{
                isUpTotal = false;
            }
            //充提差
            if(item.getUpRw()==0||cumulativeTopUp-cumulativeReflect>=item.getUpRw()*BigDecimalUtil.PLAYER_MONEY_UNIT){
                isUpRw = true;
            }else{
                isUpRw = false;
            }
            //余额是否满足
            if(item.getBalance()==0||(player.getBalance()+player.getFrozenBet())>=(item.getBalance()*BigDecimalUtil.PLAYER_MONEY_UNIT)){
                isBlance = true;
            }else{
                isBlance = false;
            }
            if(isUpRw&&isUpTotal&&isBlance) {
                level = item.getLevel();
                continue;
            }else{
                break;
            }
        }
        return level;
    }
}
