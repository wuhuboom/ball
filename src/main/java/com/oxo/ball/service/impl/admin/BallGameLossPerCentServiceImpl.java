package com.oxo.ball.service.impl.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.additional.query.impl.QueryChainWrapper;
import com.oxo.ball.bean.dao.BallGameLossPerCent;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.mapper.BallGameLossPerCentMapper;
import com.oxo.ball.service.admin.IBallGameLossPerCentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxo.ball.utils.RedisUtil;
import com.oxo.ball.utils.TimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 游戏赔率 服务实现类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Service
public class BallGameLossPerCentServiceImpl extends ServiceImpl<BallGameLossPerCentMapper, BallGameLossPerCent> implements IBallGameLossPerCentService {

    @Autowired
    BallGameLossPerCentMapper mapper;
    @Autowired
    RedisUtil redisUtil;

    @Override
    @Cacheable(value = "ball_game_loss_per_cent_by_game_id", key = "'_ID_' + #gameId", unless = "#result == null")
    public List<BallGameLossPerCent> findByGameId(Long gameId) {
        QueryWrapper query = new QueryWrapper();
        query.eq("game_id", gameId);
        List<BallGameLossPerCent> list = list(query);
        if(list==null||list.size()==0){
            return null;
        }
        List<BallGameLossPerCent> returnList = list.parallelStream().filter(Objects::nonNull).collect(Collectors.toList());
        if(returnList.isEmpty()){
            return null;
        }
        Collections.sort(returnList, (o1, o2) -> {
            if(o1.getScoreHome().equals("*")||o1.getScoreAway().equals("*")){
                return 1;
            }
            if(o2.getScoreHome().equals("*")||o2.getScoreAway().equals("*")){
                return -1;
            }
            return Integer.parseInt(o1.getScoreHome())-Integer.parseInt(o2.getScoreHome());
        });
        return returnList;
    }

    @Override
    @Cacheable(value = "ball_game_loss_per_cent_by_id", key = "#oddsId", unless = "#result == null")
    public BallGameLossPerCent findById(Long oddsId) {
        return getById(oddsId);
    }

    @Override
    public SearchResponse<BallGameLossPerCent> search(BallGameLossPerCent queryParam, Integer pageNo, Integer pageSize) {
        SearchResponse<BallGameLossPerCent> response = new SearchResponse<>();
        Page<BallGameLossPerCent> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallGameLossPerCent> query = new QueryWrapper<>();
        if(queryParam.getGameId()!=null){
            query.eq("game_id",queryParam.getGameId());
        }
        if(queryParam.getId()!=null){
            query.eq("id",queryParam.getId());
        }
        if(queryParam.getEven()!=null){
            query.eq("even",queryParam.getEven());
        }
        if(queryParam.getGameType()!=null){
            query.eq("game_type",queryParam.getGameType());
        }
        if(queryParam.getStatus()!=null){
            query.eq("status",queryParam.getStatus());
        }
        if(!StringUtils.isBlank(queryParam.getScoreHome())){
            query.eq("score_home",queryParam.getScoreHome());
        }
        if(!StringUtils.isBlank(queryParam.getScoreAway())){
            query.eq("score_away",queryParam.getScoreAway());
        }
        query.orderByDesc("id");
        IPage<BallGameLossPerCent> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    @CacheEvict(value = "ball_game_loss_per_cent_by_id", key = "#gameLossPerCent.getId()")
    public BaseResponse edit(BallGameLossPerCent gameLossPerCent) {
        BallGameLossPerCent edit = BallGameLossPerCent.builder()
                .scoreHome(gameLossPerCent.getScoreHome())
                .scoreAway(gameLossPerCent.getScoreAway())
                .antiPerCent(gameLossPerCent.getAntiPerCent())
                .lossPerCent(gameLossPerCent.getLossPerCent())
                .minBet(gameLossPerCent.getMinBet())
                .maxBet(gameLossPerCent.getMaxBet())
                .totalBet(gameLossPerCent.getTotalBet())
                .build();
        edit.setId(gameLossPerCent.getId());
        edit.setUpdatedAt(TimeUtil.getNowTimeMill());
        boolean b = updateById(edit);
        if(b){
            clearCache(gameLossPerCent.getGameId());
        }
        return b?BaseResponse.successWithMsg("ok"):BaseResponse.failedWithMsg("edit failed");
    }
//    @CacheEvict(value = "ball_game_loss_per_cent_by_game_id", key = "'_ID_' + #id")
    private void clearCache(Long id){
        redisUtil.del("ball_game_loss_per_cent_by_game_id::_ID_"+id);
    }
    @Override
    @CacheEvict(value = "ball_game_loss_per_cent_by_id", key = "#gameLossPerCent.getId()")
    public Boolean editStatus(BallGameLossPerCent gameLossPerCent) {
        BallGameLossPerCent edit = BallGameLossPerCent.builder()
                .status(gameLossPerCent.getStatus()==1?2:1)
                .build();
        edit.setId(gameLossPerCent.getId());
        edit.setUpdatedAt(TimeUtil.getNowTimeMill());
        boolean b = updateById(edit);
        if(b){
            clearCache(gameLossPerCent.getGameId());
        }
        return b;
    }

    @Override
    @CacheEvict(value = "ball_game_loss_per_cent_by_id", key = "#gameLossPerCent.getId()")
    public Boolean editStatusEven(BallGameLossPerCent gameLossPerCent) {
        BallGameLossPerCent edit = BallGameLossPerCent.builder()
                .even(gameLossPerCent.getEven()==1?2:1)
                .build();
        edit.setId(gameLossPerCent.getId());
        edit.setUpdatedAt(TimeUtil.getNowTimeMill());
        boolean b = updateById(edit);
        if(b){
            clearCache(gameLossPerCent.getGameId());
        }
        return b;
    }

    @Override
    public int batchInsert(List<BallGameLossPerCent> lossPerCents) {
        return mapper.batchInsert(lossPerCents);
    }

    @Override
    public void editStatusEvenByGameId(Long id,int even) {
        UpdateWrapper edit = new UpdateWrapper();
        edit.eq("game_id", id);
        mapper.update(BallGameLossPerCent.builder()
                .even(even)
                .build(),edit);
    }

    @Override
    public SearchResponse<BallGameLossPerCent> searchLock(BallGameLossPerCent queryParam, Integer pageNo, Integer pageSize) {
        SearchResponse<BallGameLossPerCent> response = new SearchResponse<>();
        Page<BallGameLossPerCent> page = new Page<>(pageNo, pageSize);
        IPage<BallGameLossPerCent> pages = mapper.listPage(page,queryParam);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }
}
