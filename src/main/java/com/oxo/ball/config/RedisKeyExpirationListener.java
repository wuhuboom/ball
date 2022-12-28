package com.oxo.ball.config;

import com.oxo.ball.bean.dao.BallPlayer;
import com.oxo.ball.contant.LogsContant;
import com.oxo.ball.service.impl.BasePlayerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisKeyExpirationListener  extends KeyExpirationEventMessageListener {

    private Logger apiLog = LoggerFactory.getLogger(LogsContant.API_LOG);


    @Autowired
    BasePlayerService basePlayerService;
    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    /**
     * 针对redis数据失效事件，进行数据处理
     * @param message
     * @param pattern
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String key = message.toString();
//        apiLog.info("redis key expired:{}",key);
        if(key.startsWith("ball_player_activity")){
            //退出登录
            BallPlayer player = basePlayerService.findById(Long.parseLong(key.split(":")[1]));
            BallPlayer edit = BallPlayer.builder()
                    .statusOnline(0)
                    .build();
            edit.setId(player.getId());
            basePlayerService.editAndClearCache(edit,player);
        }
    }
}
