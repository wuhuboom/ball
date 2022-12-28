package com.oxo.ball.interceptor;

import com.oxo.ball.auth.PlayerApiTooFastException;
import com.oxo.ball.auth.PlayerDisabledException;
import com.oxo.ball.auth.TokenInvalidedException;
import com.oxo.ball.bean.dao.BallPlayer;
import com.oxo.ball.contant.RedisKeyContant;
import com.oxo.ball.service.IBasePlayerService;
import com.oxo.ball.service.player.IPlayerService;
import com.oxo.ball.utils.IpUtil;
import com.oxo.ball.utils.RedisUtil;
import com.oxo.ball.utils.TimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 玩家请求频率拦截
 * @author flooming
 */
@Order
public class PlayerOperCountInterceptor implements HandlerInterceptor {
    @Autowired
    private IPlayerService playerService;
    @Autowired
    IBasePlayerService basePlayerService;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws PlayerApiTooFastException {
        //每个号每个接口频率每秒3次为上限
        BallPlayer currentUser = null;
        String key = null;
        try {
            currentUser = playerService.getCurrentUser(request);
            //key = method+id+url
            key = request.getMethod()+currentUser.getId()+request.getServletPath();
            if("/player/game".equals(request.getServletPath())||"/player/game/finished".equals(request.getServletPath())){
                String startTime = request.getParameter("startTime");
                String status  = request.getParameter("status");
                String teamName  = request.getParameter("teamName");
                key = key + startTime+status+teamName;
            }
        } catch (TokenInvalidedException e) {
        } catch (PlayerDisabledException e) {
        }
        if(StringUtils.isBlank(key)){
            key = IpUtil.getIpAddress(request)+request.getServletPath();
        }
        long count = redisUtil.incr(key,1);
        if(count==1){
            redisUtil.expire(key,1);
        }
        //大于3次频繁
        if(count>3){
            throw new PlayerApiTooFastException();
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
