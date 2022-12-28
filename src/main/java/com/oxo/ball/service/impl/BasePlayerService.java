package com.oxo.ball.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxo.ball.bean.dao.BallPlayer;
import com.oxo.ball.contant.LogsContant;
import com.oxo.ball.mapper.BallPlayerMapper;
import com.oxo.ball.service.IBasePlayerService;
import com.oxo.ball.utils.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.List;

@Service
public class BasePlayerService extends ServiceImpl<BallPlayerMapper, BallPlayer> implements IBasePlayerService {
    @Resource
    private RedisUtil redisUtil;
    private Logger apiLog = LoggerFactory.getLogger(LogsContant.API_LOG);


    @Override
    @Cacheable(value = "ball_player_by_id", key = "#id", unless = "#result == null")
    public BallPlayer findById(Long id) {
        return getById(id);
    }

    @Override
    public BallPlayer findByIdNoCache(Long id) {
        return getById(id);
    }

    @Cacheable(value = "ball_player_by_username", key = "#username", unless = "#result == null")
    @Override
    public BallPlayer findByUsername(String username) {
        QueryWrapper query = new QueryWrapper();
        query.eq("username", username);
        BallPlayer ballPlayer = getOne(query);
        return ballPlayer;
    }

    @Override
    @Cacheable(value = "ball_player_by_phone", key = "#phone", unless = "#result == null")
    public BallPlayer findByPhone(String area,String phone) {
        QueryWrapper query = new QueryWrapper();
//        query.eq("area_code", area);
        query.eq("phone", phone);
        List<BallPlayer> ballPlayer = list(query);
        return ballPlayer.isEmpty()?null:ballPlayer.get(0);
    }

    @Cacheable(value = "ball_player_by_userid", key = "#userId", unless = "#result == null")
    @Override
    public BallPlayer findByUserId(Long userId) {
        QueryWrapper query = new QueryWrapper();
        query.eq("user_id", userId);
        BallPlayer ballPlayer = getOne(query);
        return ballPlayer;
    }

    @Override
    @Cacheable(value = "ball_player_by_invitation_code", key = "#invitationCode", unless = "#result == null")
    public BallPlayer findByInvitationCode(String invitationCode) {
        QueryWrapper query = new QueryWrapper();
        query.eq("invitation_code", invitationCode);
        BallPlayer ballPlayer = getOne(query);
        return ballPlayer;
    }

    @Override
    public Long createUserId() {
        while (true) {
            Calendar instance = Calendar.getInstance();
            int year = instance.get(Calendar.YEAR);
            int month = instance.get(Calendar.MONTH);
            int day = instance.get(Calendar.DAY_OF_MONTH);
            int hour = instance.get(Calendar.HOUR_OF_DAY);
            int min = instance.get(Calendar.MINUTE);
            int sed = instance.get(Calendar.SECOND);
            String idStr = "" + year + month + day + hour + min + sed;
            long userId = Long.parseLong(idStr);
            BallPlayer byUserId = findByUserId(userId);
            if (byUserId == null) {
                return userId;
            }
        }
    }

    @Override
    public boolean editAndClearCache(BallPlayer edit, BallPlayer db) {
        try {
            boolean b = updateById(edit);
            return b;
        } catch (Exception ex) {
            return false;
        } finally {
            //清除账号缓存
            long curr = System.currentTimeMillis();
            long del = redisUtil.del("ball_player_by_id::" + db.getId());
            if(System.currentTimeMillis()-curr>2000){
                apiLog.warn("redis 删除key超过了2s{}",System.currentTimeMillis()-curr);
            }
//            if(del==0){
//                apiLog.warn("redis!!:ball_player_by_id::"+db.getId()+"执行失败");
//            }
            del = redisUtil.del("ball_player_by_username::" + db.getUsername());
//            if(del==0){
//                apiLog.warn("redis!!:ball_player_by_username::"+db.getUsername()+"执行失败");
//            }
            del = redisUtil.del("ball_player_by_userid::" + db.getUserId());
//            if(del==0){
//                apiLog.warn("redis!!:ball_player_by_userid::"+db.getUserId()+"执行失败");
//            }
            del = redisUtil.del("ball_player_by_invitation_code::" + db.getInvitationCode());
//            if(del==0){
//                apiLog.warn("redis!!:ball_player_by_invitation_code::"+db.getInvitationCode()+"执行失败");
//            }
            del = redisUtil.del("ball_player_by_phone::" + db.getPhone());
//            if(del==0){
//                apiLog.warn("redis!!:ball_player_by_phone::"+db.getPhone()+"执行失败");
//            }
        }
    }

    @Override
    public void editMultGroupNum(String treeIds, int quantity) {
        baseMapper.updateTreeGroupNum(treeIds, quantity);
    }

    @Override
    public void clearFrozen() {
        update(BallPlayer.builder()
                .frozenWithdrawal(0L)
                .frozenBet(0L)
                .build(),new UpdateWrapper<>());
    }
}
