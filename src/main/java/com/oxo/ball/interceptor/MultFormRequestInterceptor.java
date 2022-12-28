package com.oxo.ball.interceptor;

import com.oxo.ball.auth.CountryInvalidedException;
import com.oxo.ball.auth.MultiFormSubmitException;
import com.oxo.ball.contant.LogsContant;
import com.oxo.ball.utils.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 表单重复提交处理
 * @author flooming
 */
@Order
public class MultFormRequestInterceptor implements HandlerInterceptor {
    private Logger apiLog = LoggerFactory.getLogger(LogsContant.API_LOG);

    @Autowired
    RedisUtil redisUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws CountryInvalidedException, MultiFormSubmitException {
        if(request.getServletPath().contains("report")){
            return true;
        }
//        if(request.getServletPath().contains("pay/add")||request.getServletPath().contains("pay/edit")){
//            apiLog.info("finance/pay/add:{}",request.getContentType());
//        }
        String key = request.getHeader("token")+request.getServletPath()+request.getMethod();
        if(redisUtil.get(key)!=null){
            redisUtil.set(key,0,1);
            throw new MultiFormSubmitException();
        }else{
            redisUtil.set(key,0,1);
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
//        String key = request.getHeader("token")+request.getServletPath();
//        cacheForm.remove(key);
//        System.out.println(key);
    }
}
