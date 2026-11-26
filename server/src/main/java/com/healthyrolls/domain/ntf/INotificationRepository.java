package com.healthyrolls.domain.ntf;

import com.healthyrolls.domain.common.UserId;
import java.util.List;
import java.util.Optional;

public interface INotificationRepository {

    Notification save(Notification n);

    Optional<Notification> findById(NotificationId id);

    List<Notification> findByRecipient(UserId id);

    List<Notification> findOperatorAlerts();

    void deleteAll();
}
