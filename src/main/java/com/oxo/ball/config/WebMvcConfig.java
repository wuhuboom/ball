package com.oxo.ball.config;

import com.oxo.ball.auth.AdminAuthInterceptor;
import com.oxo.ball.auth.PlayerAuthInterceptor;
import com.oxo.ball.interceptor.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * @author flooming
 */
@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {
    private Logger logger = LoggerFactory.getLogger(WebMvcConfig.class);

    @Value("${static.file}")
    private String staticFile;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowCredentials(true)
                .allowedHeaders("*")
                .allowedOrigins("*")
                .allowedMethods("*");
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor())
                .addPathPatterns("/ball/**").excludePathPatterns("/static/**"
                , "/auth/login**"
                , "/auth/logout"
                , "/auth/userinfo"
                , "/test/**"
        );

        registry.addInterceptor(multFormRequestInterceptor())
                .addPathPatterns("/ball/**").excludePathPatterns("/static/**"
                , "/auth/login**"
                , "/auth/logout"
                , "/auth/userinfo"
                , "/test/**"
        );

        //地区限制
        registry.addInterceptor(playerIpCountrynterceptor())
                .addPathPatterns("/player/**");
        //玩家登录认证
        registry.addInterceptor(authenticationPlayInterceptor())
                .addPathPatterns("/player/**")
                .excludePathPatterns("/player/auth/login**",
                        "/player/auth/verify_code",
                        "/player/auth/verify_code_check",
                        "/player/auth/sys_config",
                        "/player/pay/callback/**",
                        "/player/home/serv_tmp",
                        "/player/home/app_url",
                        "/player/auth/regist",
                        "/player/v2/phone_change_pwd",
                        "/player/v2/phone_code/change_pwd",
                        "/player/v2/phone_code/username"
                );

        //玩家频率限制
        registry.addInterceptor(playerOperCountInterceptor())
                .addPathPatterns("/player/**")
                .excludePathPatterns("/player/pay/callback/**")
                ;
        //玩家在线状态
        registry.addInterceptor(playerOperInterceptor())
                .addPathPatterns("/player/**")
                .excludePathPatterns("/player/auth/verify_code",
                        "/player/auth/verify_code_check",
                        "/player/auth/login**",
                        "/player/auth/sys_config",
                        "/player/pay/callback/**",
                        "/player/home/serv_tmp",
                        "/player/home/app_url",
                        "/player/auth/regist",
                        "/player/v2/phone_change_pwd",
                        "/player/v2/phone_code/change_pwd",
                        "/player/v2/phone_code/username"
                );
    }


    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/", staticFile);
        //swagger-config
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
        super.addResourceHandlers(registry);
    }

    @Bean
    public AdminAuthInterceptor authenticationInterceptor() {
        return new AdminAuthInterceptor();
    }

    @Bean
    public PlayerAuthInterceptor authenticationPlayInterceptor() {
        return new PlayerAuthInterceptor();
    }

    @Bean
    public PlayerOperInterceptor playerOperInterceptor() {
        return new PlayerOperInterceptor();
    }

    @Bean
    public PlayerOperCountInterceptor playerOperCountInterceptor() {
        return new PlayerOperCountInterceptor();
    }
    @Bean
    public PlayerIpCountrynterceptor playerIpCountrynterceptor() {
        return new PlayerIpCountrynterceptor();
    }
    @Bean
    public MultFormRequestInterceptor multFormRequestInterceptor() {
        return new MultFormRequestInterceptor();
    }
}
