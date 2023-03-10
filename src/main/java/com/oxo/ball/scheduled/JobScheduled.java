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
     * ????????????
     */
    @Scheduled(cron = "0 0 0 * * ?")
    private void newDayStart() {
        //?????????????????????
        playerBetService.clearDayOrderNo();
        //??????????????????
        playerService.clearDayActitiy();
    }
    /**
     * ??????????????????????????????
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    private void wsSenddingPing() {
        //??????????????????
        gameService.whenGameStart();
        //?????????????????????10??????????????????
        gameService.autoSetHot();
        //????????????
        loggerRechargeService.refreshStatus();
        //??????N??????????????????
        loggerWithdrawalService.autoCheck();
    }

    /**
     * ??????????????????,???2??????????????????
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
     * ????????????????????????
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
     * ??????????????????
     */
    @Scheduled(cron = "0/1 * * * * ?")
    private void requestOdds() {
        apiService.refreshOdds();
        //??????????????????????????????
        ballPlayerService.checkPlayerMessage();
    }

    /**
     * ??????????????????
     */
    @Scheduled(cron = "0 33 0 * * ?")
    private void statisProxyLogger() {
//        try {
//            proxyLoggerService.statisEveryDay();
//        }catch (Exception e){
//        }
        //???????????????
        gameReportService.dayStatis();
    }
    public static int weeks[] = new int[]{7,1,2,3,4,5,6};
    /**
     * ????????????????????????????????????
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    private void everyWeekRebate() {
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        int nowHour = TimeUtil.getNowHour();
        int min = TimeUtil.getNowMin();
        String time = nowHour+":"+min;
        try {
            //??????VIP????????????
            if(systemConfig.getCheckLevelTime().contains(time)){
                vipService.checkLevel();
            }
        }catch (Exception ex){
        }
        try {
            // ??????VIP??????
            if(systemConfig.getVipRewardTime().contains(time)){
                ballPlayerService.dayReward();
            }
        }catch (Exception ex){
        }
        if(systemConfig.getSwitchRebate()!=null&&systemConfig.getSwitchRebate()==1){
            //????????????????????????
            int nowWeek = weeks[TimeUtil.getNowWeek()-1];
            if(systemConfig.getRebateWeek()!=null&&systemConfig.getRebateWeek()==nowWeek){
                //????????????,?????????????
                time = TimeUtil.dateFormat(new Date(), TimeUtil.TIME_HHMM);
                if(!StringUtils.isBlank(systemConfig.getRebateTime())&&systemConfig.getRebateTime().startsWith(time)){
                    apiLog.info("??????????????????????????????");
                    loggerBackService.settlementOnWeek();
                }
            }
        }
    }
}