package com.healthyrolls.technicalservices.persistence.ntf;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notification")
public class NotificationProperties {

    private Duration dispatchWaitThreshold;
    private Duration scanInterval;
    private int maxNotificationsPerUser;

    public Duration getDispatchWaitThreshold() {
        return dispatchWaitThreshold;
    }

    public void setDispatchWaitThreshold(Duration dispatchWaitThreshold) {
        this.dispatchWaitThreshold = dispatchWaitThreshold;
    }

    public Duration getScanInterval() {
        return scanInterval;
    }

    public void setScanInterval(Duration scanInterval) {
        this.scanInterval = scanInterval;
    }

    public int getMaxNotificationsPerUser() {
        return maxNotificationsPerUser;
    }

    public void setMaxNotificationsPerUser(int maxNotificationsPerUser) {
        this.maxNotificationsPerUser = maxNotificationsPerUser;
    }
}
