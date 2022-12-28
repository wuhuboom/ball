package com.oxo.ball.service.admin;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oxo.ball.bean.dao.BallVersionConfig;

/**
 * <p>
 * 系统配置 服务类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
public interface IBallVersionConfigService extends IService<BallVersionConfig> {
    BallVersionConfig getVersionConfig();
    void init();
    Boolean edit(BallVersionConfig systemConfig);

    void createTable();

}
