package com.oxo.ball.service.impl.admin;

import com.oxo.ball.bean.dao.BallApiConfig;
import com.oxo.ball.bean.dao.BallPlayer;
import com.oxo.ball.bean.dto.queue.MessageQueueDTO;
import com.oxo.ball.bean.dto.req.admin.QueryActivePlayerRequest;
import com.oxo.ball.config.SomeConfig;
import com.oxo.ball.service.IMessageQueueService;
import com.oxo.ball.service.admin.IBallApiConfigService;
import com.oxo.ball.service.admin.IBallPlayerActiveService;
import com.oxo.ball.service.admin.IBallPlayerService;
import com.oxo.ball.service.impl.BasePlayerService;
import com.oxo.ball.utils.HttpUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BallPlayerActiveServiceImpl implements IBallPlayerActiveService {
    @Autowired
    BasePlayerService basePlayerService;
    @Autowired
    IBallPlayerService ballPlayerService;
    @Autowired
    IBallApiConfigService apiConfigService;
    @Autowired
    private SomeConfig someConfig;
    @Autowired
    IMessageQueueService messageQueueService;
    @Override
    public List<BallPlayer> queryActivePlayer(QueryActivePlayerRequest request) {
        BallPlayer player = basePlayerService.findByUsername(request.getUsername());
        Map<String,Object> data = new HashMap<>();
        if(player==null){
            return new ArrayList<>();
        }
        data.put("player",player);
        //并列充值+活跃
        List<BallPlayer> ballPlayerSearchResponse = queryActivePlayerAll(request, player);
        return ballPlayerSearchResponse;

    }

    @Override
    public void sendMessageToPlayer(String message) {
//        messageQueueService.putMessage(MessageQueueDTO.builder()
//                .type(MessageQueueDTO.TYPE_PLAYER_TG_CHAT)
//                .data(message)
//                .build());
        BallApiConfig apiConfig = apiConfigService.getApiConfig();
        if(StringUtils.isBlank(apiConfig.getPlayerToken())||StringUtils.isBlank(apiConfig.getPlayerChat())){
            return;
        }
        try {
            message = URLEncoder.encode(message, "utf-8");
            String url = MessageFormat.format("https://api.telegram.org/bot{0}/sendMessage?chat_id={1}&text={2}",
                    apiConfig.getPlayerToken(),
                    apiConfig.getPlayerChat(),
                    message);
            //{"ok":true,"result":{"message_id":3,"from":{"id":5762353581,"is_bot":true,"first_name":"botboya","username":"lboya_bot"},"chat":{"id":-1001511925511,"title":"b_bot_test","username":"our_b_bots","type":"supergroup"},"date":1663206257,"text":"hello"}}
            if (someConfig.getApiSwitch() == null) {
                HttpUtil.doGet(url, null);
            } else {
                HttpUtil.doGetProxy(url, null);
            }
        } catch (UnsupportedEncodingException e) {
        }catch (Exception e){}
    }

    private List<BallPlayer> queryActivePlayerAll(QueryActivePlayerRequest request, BallPlayer player) {
        return ballPlayerService.queryActivePlayerAll(request,player);
    }

//    private SearchResponse<BallPlayer> queryActivePlayerByBets(QueryActivePlayerRequest request, BallPlayer player) {
//        //未指定时间,只要有充值过就算
//        return ballPlayerService.queryActivePlayerByBet(request,player);
//    }
//
//    private SearchResponse<BallPlayer> queryActivePlayerByRecharge(QueryActivePlayerRequest request,BallPlayer player) {
//        //未指定时间,只要有充值过就算
//        return ballPlayerService.queryActivePlayer(request,player);
//    }
}
