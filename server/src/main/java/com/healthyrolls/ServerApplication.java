package com.healthyrolls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import com.healthyrolls.domain.reg.seed.SeedUserLoader;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ServerApplication {

    private static final Logger log = LoggerFactory.getLogger(ServerApplication.class);

    public static void main(String[] args) {
        var context = SpringApplication.run(ServerApplication.class, args);

        try {
            context.getBean(SeedUserLoader.class).run(args);
        } catch (RuntimeException e) {
            log.error("User seeding failed, shutting down", e);
            System.exit(SpringApplication.exit(context, () -> 1));
        }
    }

}
