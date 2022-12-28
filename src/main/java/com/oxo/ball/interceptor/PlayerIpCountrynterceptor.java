package com.oxo.ball.interceptor;

import com.oxo.ball.auth.CountryInvalidedException;
import com.oxo.ball.bean.dao.BallIpCountry;
import com.oxo.ball.bean.dao.BallPlayer;
import com.oxo.ball.contant.RedisKeyContant;
import com.oxo.ball.service.IBasePlayerService;
import com.oxo.ball.service.admin.IBallIpCountryService;
import com.oxo.ball.service.player.IPlayerService;
import com.oxo.ball.utils.GeoLiteUtil;
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
 * 玩家请求拦截
 * @author flooming
 */
@Order
public class PlayerIpCountrynterceptor implements HandlerInterceptor {
    @Autowired
    private IBallIpCountryService ipCountryService;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws CountryInvalidedException {
        String ipAddress = IpUtil.getIpAddress(request);
        String ipAddr = GeoLiteUtil.getIpAddr(ipAddress);
        if(!StringUtils.isBlank(ipAddr)){
            BallIpCountry byCountry = ipCountryService.findByCountry(ipAddr);
            if(byCountry==null){
                BallIpCountry build = BallIpCountry.builder()
                        .country(ipAddr)
                        .build();
                build.setCreatedAt(TimeUtil.getNowTimeMill());
                ipCountryService.insert(build);
            }else{
                if(byCountry.getStatus()==2){
                    throw new CountryInvalidedException();
                }
            }
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
