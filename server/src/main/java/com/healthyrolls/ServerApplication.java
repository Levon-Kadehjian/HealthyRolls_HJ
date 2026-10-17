package com.healthyrolls;

import com.healthyrolls.domain.reg.seed.SeedUserLoader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ServerApplication {

    public static void main(String[] args) {
        var context = SpringApplication.run(ServerApplication.class, args);
        context.getBean(SeedUserLoader.class).run(args);
    }

}
