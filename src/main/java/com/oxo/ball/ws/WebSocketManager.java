package com.oxo.ball.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.oxo.ball.contant.LogsContant;
import com.oxo.ball.utils.JsonUtil;
import com.oxo.ball.ws.dto.MessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ws管理
 * @author ljy
 */
@Component
public class WebSocketManager {
    private static Logger wslog = LoggerFactory.getLogger(LogsContant.WS_LOG);

    private Map<String,WebSocketServer> clientsMap = new ConcurrentHashMap<>();

    public synchronized void joinServer(String sessionId, WebSocketServer client){
        clientsMap.put(sessionId,client);
    }

    public void leaveServer(WebSocketServer client){
        if(client!=null){
            if(null!=client.getSessionId()){
                clientsMap.remove(client.getSessionId());
            }
        }
    }

    public boolean sendMessage(String sessionId,MessageResponse messageResponse){
        if(sessionId==null){
            for(WebSocketServer wss:clientsMap.values()){
                try {
                    wss.sendMessageToPlugin(JsonUtil.toJson(messageResponse));
                } catch (JsonProcessingException e) {
                }
            }
        }else{
            //超级管理员必定发
            //查询superTree的key
            Set<String> keys = clientsMap.keySet();
            for(String key:keys){
                if(key.startsWith(WebSocketServer.MUST_RECEIVE)||sessionId.equals(key)){
                    WebSocketServer webSocketServer = clientsMap.get(key);
                    if(webSocketServer!=null){
                        try {
                            webSocketServer.sendMessageToPlugin(JsonUtil.toJson(messageResponse));
                        } catch (JsonProcessingException e) {
                        }
                    }
                }
            }
        }
        return true;
    }
}
