package com.oxo.ball.mapper;

import com.oxo.ball.bean.dao.BallSystemConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 系统配置 Mapper 接口
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Mapper
public interface BallSystemConfigMapper extends BaseMapper<BallSystemConfig> {
    @Update("ALTER TABLE `ball_system_config`\n" +
            "ADD COLUMN `version`  bigint NOT NULL DEFAULT 0 AFTER `open_white`;"
            )
    void createVersion();

    @Update("ALTER TABLE `ball_system_config`\n" +
            "ADD COLUMN `new_devices`  tinyint NULL DEFAULT 0 COMMENT '0关1开' AFTER `version`;"
    )
    void addConfig();

    @Update("ALTER TABLE `ball_system_config`\n" +
            "ADD COLUMN `auto_up`  tinyint NULL DEFAULT 0 COMMENT '0关1开' AFTER `new_devices`;"
    )
    void addConfig2();

    @Update("ALTER TABLE `ball_system_config`\n" +
            "ADD COLUMN `auto_up_off`  tinyint NULL DEFAULT 0 COMMENT '0关1开' AFTER `auto_up`;"
    )
    void addConfig3();


    @Update("ALTER TABLE `ball_system_config`\n" +
            "ADD COLUMN `switch_rebate`  tinyint NULL DEFAULT 0 AFTER `auto_up_off`,\n" +
            "ADD COLUMN `rebate_week`  tinyint NULL AFTER `switch_rebate`,\n" +
            "ADD COLUMN `rebate_time`  char(8) NULL AFTER `rebate_week`;\n"
    )
    void addConfig4();

    @Update("ALTER TABLE `ball_system_config`\n" +
            "ADD COLUMN `max_sms`  int NULL DEFAULT 5 AFTER `rebate_time`,\n" +
            "ADD COLUMN `switch_rebate_first` tinyint NULL DEFAULT 0 AFTER `max_sms`,\n" +
            "ADD COLUMN `switch_rebate_every` tinyint NULL DEFAULT 0 AFTER `switch_rebate_first`;\n"
    )
    void addConfig5();
}
