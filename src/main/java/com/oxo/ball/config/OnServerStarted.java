package com.oxo.ball.config;

import com.oxo.ball.OxoMainApplication;
import com.oxo.ball.bean.dao.BallPlayer;
import com.oxo.ball.bean.dao.BallTimezone;
import com.oxo.ball.bean.dao.BallVersionConfig;
import com.oxo.ball.contant.RedisKeyContant;
import com.oxo.ball.controller.admin.BallAdminController;
import com.oxo.ball.service.IBallVersionUpdateService;
import com.oxo.ball.service.IMessageQueueService;
import com.oxo.ball.service.admin.*;
import com.oxo.ball.utils.RedisUtil;
import com.oxo.ball.utils.ThreadPoolUtil;
import com.oxo.ball.utils.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.TimeZone;

/**
 * @author jy
 */
@Component
@Slf4j
public class OnServerStarted  implements ApplicationListener<ContextRefreshedEvent>  {

    @Resource
    SomeConfig someConfig;

    @Resource
    IBallSystemConfigService systemConfigService;
    @Resource
    IMessageQueueService messageQueueService;
    @Autowired
    IApiService apiService;
    @Autowired
    BallAdminService ballAdminService;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    IBallVersionUpdateService versionUpdateService;
    @Autowired
    IBallApiConfigService apiConfigService;
    @Autowired
    IBallTimezoneService timezoneService;
    @Value("${server.port}")
    private int serverPort;
    @Autowired
    private IBallVersionConfigService versionConfigService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        log.info("on server start~version:{}:{}",OxoMainApplication.global_version,serverPort);
        BallVersionConfig systemConfig = versionConfigService.getVersionConfig();
        versionConfigService.edit(BallVersionConfig.builder()
                .id(systemConfig.getId())
                .version(OxoMainApplication.global_version)
                .build());

        if(StringUtils.isBlank(someConfig.getServerUrl())){
            throw new RuntimeException();
        }
        BallTimezone byStatusOn = timezoneService.findByStatusOn();
        if(byStatusOn==null){
            TimeUtil.TIME_ZONE = TimeZone.getDefault();
        }else{
            TimeUtil.TIME_ZONE = TimeZone.getTimeZone(byStatusOn.getTimeId());
        }

        systemConfigService.init();
        ThreadPoolUtil.exec(() -> {
            messageQueueService.startQueue();
        });
        ballAdminService.initAdmin();
        if(someConfig.getApiSwitch()==null){
            ThreadPoolUtil.exec(() -> {
                apiService.refreshFixturesAll(true);
                apiService.refreshFixturesAll(false);
            });
        }
        //
        clearInitData();
        apiConfigService.init();
//        //版本更新是否执行-提现,充值增加一级代理
//        if(OxoMainApplication.global_version==11){
//            ThreadPoolUtil.execSaki(()->{
//                versionUpdateService.updateOn11();
//            });
//        }
//        //版本16,22更新 - 提现冻结重算
//        if(OxoMainApplication.global_version==22){
//            versionUpdateService.updateOn16();
//        }
        //版本34更新- 下注增加一级代理
//        if(OxoMainApplication.global_version==35){
//            versionUpdateService.updateOn21();
//            versionUpdateService.updateOn34();
//        }
//        if(OxoMainApplication.global_version==41){
//            versionUpdateService.updateOn41();
//        }
//        if(OxoMainApplication.global_version==43){
//            try {
//                versionUpdateService.updateOn43();
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//        }
//        if(OxoMainApplication.global_version==49){
//            versionUpdateService.updateOn34();
//        }
//        if(OxoMainApplication.global_version==69){
////            versionUpdateService.updateOn69();
//        }

//        if(OxoMainApplication.global_version==84){
//            //首充标记
//            versionUpdateService.updateOn84();
//        }
    }
    private void clearInitData(){
        redisUtil.delKeys("ball_ip_white_list*");
        redisUtil.delKeys("ball_role*");
        redisUtil.delKeys("ball_menu*");
        redisUtil.delKeys("ball_sys_config*");
        redisUtil.delKeys("ball_vip_level*");
        redisUtil.delKeys("ball_bank_list*");
        redisUtil.delKeys("ball_auth_user_rec*");
        redisUtil.delKeys("ball_api_config*");
        //清空待发送的消息，免得出问题
        redisUtil.del(RedisKeyContant.PLAYER_CHAT_MESSAGE);
    }
}
