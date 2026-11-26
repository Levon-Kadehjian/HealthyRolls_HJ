package com.healthyrolls.domain.ntf;

import java.util.List;
import java.util.Optional;

import com.healthyrolls.domain.common.UserId;

public interface INotificationRepository {

    Notification save(Notification notification);

    Optional<Notification> findById(NotificationId id);

    List<Notification> findByRecipient(UserId id);

    void deleteAll();
}
