package com.oxo.ball.config;

import com.oxo.ball.OxoMainApplication;
import com.oxo.ball.bean.dao.BallSystemConfig;
import com.oxo.ball.bean.dao.BallVersionConfig;
import com.oxo.ball.contant.LogsContant;
import com.oxo.ball.service.admin.IBallSystemConfigService;
import com.oxo.ball.service.admin.IBallVersionConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Configuration
public class CustomizeDataSourceInitializer {
    private Logger apiLog = LoggerFactory.getLogger(LogsContant.API_LOG);

    @Autowired
    IBallVersionConfigService versionConfigService;

    @Bean
    public DataSourceInitializer dataSourceInitializer(final DataSource dataSource) {
        BallVersionConfig systemConfig = null;
        try {
            systemConfig = versionConfigService.getVersionConfig();
        }catch (Exception ex){
            versionConfigService.createTable();
            versionConfigService.init();
            systemConfig  = versionConfigService.getVersionConfig();
        }
        if(systemConfig==null){
            apiLog.info("检测更新版本失败~");
            return  null;
        }
        long from = 0;
        if(systemConfig.getVersion()!=null){
            from = systemConfig.getVersion();
        }else{
            from = OxoMainApplication.global_version;
        }
        if(systemConfig.getVersion()==null||systemConfig.getVersion()<OxoMainApplication.global_version){
            apiLog.info("onboot sql~未执行,执行sql");
            final DataSourceInitializer initializer = new DataSourceInitializer();
            // 设置数据源
            initializer.setDataSource(dataSource);
            initializer.setDatabasePopulator(databasePopulator(from));
            return initializer;
        }
        apiLog.info("onboot sql~已经执行过,不再执行");
        return null;
    }

    private DatabasePopulator databasePopulator(long fromVersion) {
        final ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        for(;fromVersion<=OxoMainApplication.global_version;){
            try {
                ClassPathResource classPathResource = new ClassPathResource("db/db" + (++fromVersion) + ".sql");
                if(classPathResource.exists()){
                    populator.addScript(classPathResource);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return populator;
    }
}

