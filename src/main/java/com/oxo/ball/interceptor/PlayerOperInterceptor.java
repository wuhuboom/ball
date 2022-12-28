package com.oxo.ball.interceptor;

import com.oxo.ball.auth.AuthException;
import com.oxo.ball.auth.PlayerEnabledException;
import com.oxo.ball.auth.TokenInvalidedException;
import com.oxo.ball.bean.dao.BallPlayer;
import com.oxo.ball.contant.RedisKeyContant;
import com.oxo.ball.service.IBasePlayerService;
import com.oxo.ball.service.player.AuthPlayerService;
import com.oxo.ball.service.player.IPlayerService;
import com.oxo.ball.utils.RedisUtil;
import com.oxo.ball.utils.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.oxo.ball.service.admin.AuthService.HAVE_NO_AUTH;
import static com.oxo.ball.service.admin.AuthService.TOKEN_INVALID;
import static com.oxo.ball.service.player.AuthPlayerService.PLAYER_INVALID;

/**
 * 玩家请求拦截
 * @author flooming
 */
@Order
public class PlayerOperInterceptor implements HandlerInterceptor {
    @Autowired
    private IPlayerService playerService;
    @Autowired
    IBasePlayerService basePlayerService;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        BallPlayer currentUser = playerService.getCurrentUser(request);
        //只要活跃了就记录一次theNewLoginTime,并且在线,离线时间设置为30分钟吧
        Object o = redisUtil.get(RedisKeyContant.PLAYER_ACTIVITY + currentUser.getId());
        redisUtil.set(RedisKeyContant.PLAYER_ACTIVITY + currentUser.getId(),0,30*TimeUtil.TIME_ONE_MIN/1000);
        if(o==null){
            BallPlayer edit = BallPlayer.builder()
                    .statusOnline(1)
                    .build();
            edit.setId(currentUser.getId());
            basePlayerService.editAndClearCache(edit,currentUser);
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }
}
