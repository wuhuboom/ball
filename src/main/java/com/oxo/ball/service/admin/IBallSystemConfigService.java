package com.oxo.ball.service.admin;

import com.oxo.ball.bean.dao.BallSystemConfig;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.cache.annotation.Cacheable;

/**
 * <p>
 * 系统配置 服务类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
public interface IBallSystemConfigService extends IService<BallSystemConfig> {
    BallSystemConfig getSystemConfig();
    BallSystemConfig getSystemConfigNoCache();
    void init();
    void createVersion();
    Boolean edit(BallSystemConfig systemConfig);
    void addConfig();

    void addConfig2();
    void addConfig3();

    void addConfig4();

    void addConfig5();
}
