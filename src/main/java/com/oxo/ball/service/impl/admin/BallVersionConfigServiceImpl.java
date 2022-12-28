package com.oxo.ball.service.impl.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxo.ball.bean.dao.BallVersionConfig;
import com.oxo.ball.mapper.BallVersionConfigMapper;
import com.oxo.ball.service.admin.IBallVersionConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 系统配置 服务实现类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Service
public class BallVersionConfigServiceImpl extends ServiceImpl<BallVersionConfigMapper, BallVersionConfig> implements IBallVersionConfigService {

    @Autowired
    BallVersionConfigMapper mapper;

    @Override
    @Cacheable(value = "ball_version_config", key = "'one'", unless = "#result == null")
    public BallVersionConfig getVersionConfig() {
        BallVersionConfig one = getOne(new QueryWrapper<>());
        return one;
    }

    @Override
    public void init() {
        BallVersionConfig systemConfig = getVersionConfig();
        if(systemConfig==null){
            BallVersionConfig save = BallVersionConfig.builder()
                    .id(1L)
                    .version(37L)
                    .build();
            save(save);
        }
    }

    @Override
    public Boolean edit(BallVersionConfig systemConfig) {
        return updateById(systemConfig);
    }

    @Override
    public void createTable() {
        mapper.createTable();
    }

}
