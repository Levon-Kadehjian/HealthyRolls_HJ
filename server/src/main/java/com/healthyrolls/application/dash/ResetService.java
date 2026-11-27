package com.healthyrolls.application.dash;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.healthyrolls.application.ntf.DispatchWaitMonitor;
import com.healthyrolls.domain.common.AuthenticatedUser;
import com.healthyrolls.domain.dlv.delivery.DispatchQueue;
import com.healthyrolls.domain.ord.exceptions.ActionNotPermittedException;

@Service
public class ResetService {
    private static final Logger log = LoggerFactory.getLogger(ResetService.class);

    private final ISystemReset systemReset;
    private final DispatchQueue dispatchQueue;
    private final DispatchWaitMonitor waitMonitor;

    public ResetService(ISystemReset systemReset, DispatchQueue dispatchQueue, DispatchWaitMonitor waitMonitor) {
        this.systemReset = systemReset;
        this.dispatchQueue = dispatchQueue;
        this.waitMonitor = waitMonitor;
    }

    public void reset(AuthenticatedUser user) {
        if (user == null || !user.isOperator()) {
            throw new ActionNotPermittedException(
                    "Only operators can reset the system");
        }

        systemReset.restoreInitialData();
        dispatchQueue.clear();
        waitMonitor.clear();

        log.info("System reset by operator {}", user.userId());
    }

}
