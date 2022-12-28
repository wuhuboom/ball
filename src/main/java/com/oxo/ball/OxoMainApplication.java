package com.oxo.ball;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * @author jy
 */
@SpringBootApplication
@EnableCaching()
public class OxoMainApplication {
    public static final long global_version = 173;
    public static final long html_version = 108;

    public static void main(String[] args) {
        //start main12
        SpringApplication.run(OxoMainApplication.class, args);
    }
}
