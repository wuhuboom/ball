package com.oxo.ball.scheduled;

import com.oxo.ball.bean.dao.BallApiConfig;
import com.oxo.ball.bean.dao.BallSystemConfig;
import com.oxo.ball.bean.dto.req.player.PlayerBetRequest;
import com.oxo.ball.config.SomeConfig;
import com.oxo.ball.contant.LogsContant;
import com.oxo.ball.service.admin.*;
import com.oxo.ball.service.player.IPlayerBetService;
import com.oxo.ball.service.player.IPlayerService;
import com.oxo.ball.utils.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author jy
 */
@Component
@EnableScheduling
@Slf4j
public class JobScheduled {
    private Logger apiLog = LoggerFactory.getLogger(LogsContant.API_LOG);
    @Resource
    IPlayerBetService playerBetService;
    @Autowired
    IBallGameService gameService;
    @Autowired
    IApiService apiService;
    @Autowired
    SomeConfig someConfig;
    @Autowired
    IPlayerService playerService;
    @Autowired
    IBallLoggerRechargeService loggerRechargeService;
    @Autowired
    IBallProxyLoggerService proxyLoggerService;
    @Autowired
    IBallGameReportService gameReportService;
    @Autowired
    IBallLoggerBackService loggerBackService;
    @Autowired
    IBallSystemConfigService systemConfigService;
    @Autowired
    IBallVipService vipService;
    @Autowired
    IBallPlayerService ballPlayerService;
    @Autowired
    IBallApiConfigService apiConfigService;
    @Autowired
    IBallLoggerWithdrawalService loggerWithdrawalService;
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(7);
        return taskScheduler;
    }

    /**
     * 整点更新
     */
    @Scheduled(cron = "0 0 0 * * ?")
    private void newDayStart() {
        //清楚今日订单号
        playerBetService.clearDayOrderNo();
        //清除活跃缓存
        playerService.clearDayActitiy();
    }
    /**
     * 每分钟修改开赛的比赛
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    private void wsSenddingPing() {
        //比赛是否开始
        gameService.whenGameStart();
        //未开始的比赛前10场自动为热门
        gameService.autoSetHot();
        //超时订单
        loggerRechargeService.refreshStatus();
        //超过N小时自动审核
        loggerWithdrawalService.autoCheck();
    }

    /**
     * 每天更新比赛,每2小时更新一次
     */
//    @Scheduled(cron = "0 5 9 * * ?")
    @Scheduled(cron = "0 5 0/2 * * ?")
    private void refreshGameEveryday() {
        if(someConfig.getApiSwitch()==null){
            apiService.refreshFixturesAll(true);
        }else{
            if(someConfig.getApiSwitch()==1){
                apiService.refreshFixturesAllTest(true);
            }
        }
    }
    @Scheduled(cron = "0 5 0/3 * * ?")
    private void refreshGameEverydayTomrrow() {
        if(someConfig.getApiSwitch()==null){
            apiService.refreshFixturesAll(false);
        }else{
            if(someConfig.getApiSwitch()==1){
                apiService.refreshFixturesAllTest(false);
            }
        }
    }
    /**
     * 每分查询比赛比分
     */
//    @Scheduled(cron = "30 0 0/1 * * ?")
    @Scheduled(cron = "30 0/1 * * * ?")
    private void refreshGame() {
        if(someConfig.getApiSwitch()==null){
            apiService.refreshFixtures();
        }else{
            if(someConfig.getApiSwitch()==1){
                apiService.refreshFixtures();
            }
        }
    }

    /**
     * 每秒查询赔率
     */
    @Scheduled(cron = "0/1 * * * * ?")
    private void requestOdds() {
        apiService.refreshOdds();
        //是否有玩家群消息要发
        ballPlayerService.checkPlayerMessage();
    }

    /**
     * 统计代理数据
     */
    @Scheduled(cron = "0 33 0 * * ?")
    private void statisProxyLogger() {
//        try {
//            proxyLoggerService.statisEveryDay();
//        }catch (Exception e){
//        }
        //赛事日统计
        gameReportService.dayStatis();
    }
    public static int weeks[] = new int[]{7,1,2,3,4,5,6};
    /**
     * 每分钟检测是否要结算返利
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    private void everyWeekRebate() {
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        int nowHour = TimeUtil.getNowHour();
        int min = TimeUtil.getNowMin();
        String time = nowHour+":"+min;
        try {
            //每日VIP等级检测
            if(systemConfig.getCheckLevelTime().contains(time)){
                vipService.checkLevel();
            }
        }catch (Exception ex){
        }
        try {
            // 每日VIP奖励
            if(systemConfig.getVipRewardTime().contains(time)){
                ballPlayerService.dayReward();
            }
        }catch (Exception ex){
        }
        if(systemConfig.getSwitchRebate()!=null&&systemConfig.getSwitchRebate()==1){
            //查看时间是否到了
            int nowWeek = weeks[TimeUtil.getNowWeek()-1];
            if(systemConfig.getRebateWeek()!=null&&systemConfig.getRebateWeek()==nowWeek){
                //星期到了,时间可到?
                time = TimeUtil.dateFormat(new Date(), TimeUtil.TIME_HHMM);
                if(!StringUtils.isBlank(systemConfig.getRebateTime())&&systemConfig.getRebateTime().startsWith(time)){
                    apiLog.info("盈利返利自动结算开始");
                    loggerBackService.settlementOnWeek();
                }
            }
        }
    }
}