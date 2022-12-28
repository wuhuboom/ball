package com.oxo.ball.ws;

import com.oxo.ball.bean.dao.BallAdmin;
import com.oxo.ball.bean.dao.BallPlayer;
import com.oxo.ball.contant.LogsContant;
import com.oxo.ball.service.IBasePlayerService;
import com.oxo.ball.service.admin.AuthService;
import com.oxo.ball.utils.JsonUtil;
import com.oxo.ball.utils.PasswordUtil;
import com.oxo.ball.ws.dto.MessageResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import static com.oxo.ball.service.admin.AuthService.HAVE_NO_AUTH;
import static com.oxo.ball.service.admin.AuthService.TOKEN_INVALID;

/**
 * ws服务端
 *
 * @author none
 */
@ServerEndpoint(value = "/ball/ws/{token}")
@Component
public class WebSocketServer {
    private static Logger logger = LoggerFactory.getLogger(LogsContant.WS_LOG);

    public static String MUST_RECEIVE = "must_receive";

    static AuthService authService;
    static WebSocketManager webSocketManager;
    static IBasePlayerService basePlayerService;
    private String token;
    private Session session;
    private String sessionId;

    @Resource
    public void setBasePlayerService(IBasePlayerService basePlayerService) {
        WebSocketServer.basePlayerService = basePlayerService;
    }
    @Resource
    public void setAuthService(AuthService authService) {
        WebSocketServer.authService = authService;
    }
    @Resource
    public void setWebSocketManager(WebSocketManager webSocketManager) {
        WebSocketServer.webSocketManager = webSocketManager;
    }
    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) throws IOException {
//        logger.info("[web]有新监听:" + token );
        int res = authService.checkAuth(token,"noauth");
        boolean isRightClient = false;
        switch (res) {
            case 1:
                isRightClient = true;
                break;
            case TOKEN_INVALID:
                session.close();
                break;
            case HAVE_NO_AUTH:
                session.close();
                break;
            default:
                session.close();
                break;
        }
        if (!isRightClient) {
//            logger.info("[web]ws连接不合法:[{}]", token);
            return;
        }
        BallAdmin userFromToken = authService.getUserFromToken(token);
        this.session = session;
        //账号是否有全接收提醒权限
        if(userFromToken.getTodoAll()==1){
            //必然会收到提示
            this.sessionId = MUST_RECEIVE+userFromToken.getUsername();
        }else{
            //没有绑定代理,则普通连接
            if(StringUtils.isBlank(userFromToken.getPlayerName())){
                this.sessionId = PasswordUtil.genMd5(session.getId());
            }else{
                //包含顶级用户名才会提示
                this.sessionId = userFromToken.getPlayerName();
            }
        }
        webSocketManager.joinServer(sessionId, this);
        this.token = token;
//        //发送sessionId
//        MessageResponse<Object> messageResponse = MessageResponse.builder()
//                .type(99)
//                .data(sessionId)
//                .build();
//        sendMessageToPlugin(JsonUtil.toJson(messageResponse));
    }

    public String getSessionId(){
        return this.sessionId;
    }

    @OnClose
    public void onClose() {
        unconnected();

    }

    private boolean isClose = false;

    private void unconnected() {
        if (isClose) {
            return;
        }
        isClose = true;
        webSocketManager.leaveServer(this);
//        logger.info("[web]连接关闭！");
    }

    public InetSocketAddress getRemoteAddress(Session session) {
        if (session == null) {
            return null;
        }
        RemoteEndpoint.Async async = session.getAsyncRemote();

        //在Tomcat 8.0.x版本有效
//		InetSocketAddress addr = (InetSocketAddress) getFieldInstance(async,"base#sos#socketWrapper#socket#sc#remoteAddress");
        //在Tomcat 8.5以上版本有效
        InetSocketAddress addr = (InetSocketAddress) getFieldInstance(async, "base#socketWrapper#socket#sc#remoteAddress");
        return addr;
    }

    private Object getFieldInstance(Object obj, String fieldPath) {
        String fields[] = fieldPath.split("#");
        for (String field : fields) {
            obj = getField(obj, obj.getClass(), field);
            if (obj == null) {
                return null;
            }
        }
        return obj;
    }

    private Object getField(Object obj, Class<?> clazz, String fieldName) {
        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                Field field;
                field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(obj);
            } catch (Exception e) {
            }
        }
        return null;
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        logger.info("[web]收到信息:" + message);
        try {
            processMessage(message);
        } catch (IOException e) {
            logger.info("[web]数据结构无法解析~");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        unconnected();
//        logger.error("[web]发生错误");
        error.printStackTrace();
    }

    private void processMessage(String msg) throws IOException {
    }

    public void sendMessageToPlugin(String requestData) {
        this.session.getAsyncRemote().sendText(requestData);
    }

}
