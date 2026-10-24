package com.healthyrolls.technicalservices.configuration;

import com.healthyrolls.domain.reg.seed.SeedUser;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "reg.user.seed")
public class SeedUserProperties {

    private List<SeedUser> users = new ArrayList<>();

    public List<SeedUser> getUsers() {
        return users;
    }

    public void setUsers(List<SeedUser> users) {
        this.users = users;
    }
}
