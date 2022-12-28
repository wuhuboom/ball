package com.oxo.ball.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oxo.ball.bean.dao.BallApiConfig;
import com.oxo.ball.bean.dao.BallVersionConfig;
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
public interface BallVersionConfigMapper extends BaseMapper<BallVersionConfig> {

    @Update("CREATE TABLE `ball_version_config` (\n" +
            "  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,\n" +
            "  `version` bigint(50) unsigned DEFAULT '1',\n" +
            "  PRIMARY KEY (`id`)\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;")
    void createTable();

}
