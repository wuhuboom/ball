package com.oxo.ball.service.impl.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxo.ball.bean.dao.BallApiConfig;
import com.oxo.ball.mapper.BallApiConfigMapper;
import com.oxo.ball.service.admin.IBallApiConfigService;
import com.oxo.ball.utils.HttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;

/**
 * <p>
 * 系统配置 服务实现类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Service
public class BallApiConfigServiceImpl extends ServiceImpl<BallApiConfigMapper, BallApiConfig> implements IBallApiConfigService {


    @Override
    @Cacheable(value = "ball_api_config", key = "'one'", unless = "#result == null")
    public BallApiConfig getApiConfig() {
        BallApiConfig one = getOne(new QueryWrapper<>());
        return one;
    }

    @Override
    public void init() {
        BallApiConfig systemConfig = getApiConfig();
        if(systemConfig==null){
            BallApiConfig save = BallApiConfig.builder()
                    .ballApiKey("")
                    .smsAppKey("")
                    .smsMessage("Your verification code is {0}, please ignore this message unless you operate by yourself")
                    .smsSecretKey("")
                    .smsServer("")
                    .build();
            save(save);
        }
    }

    @Override
    @CacheEvict(value = "ball_api_config", key = "'one'")
    public Boolean edit(BallApiConfig systemConfig) {
        updateById(systemConfig);
        return true;
    }

}
