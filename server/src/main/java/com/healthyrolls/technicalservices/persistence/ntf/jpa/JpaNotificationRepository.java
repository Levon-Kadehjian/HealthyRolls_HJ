package com.healthyrolls.technicalservices.persistence.ntf.jpa;

import com.healthyrolls.domain.common.UserId;
import com.healthyrolls.domain.ntf.INotificationRepository;
import com.healthyrolls.domain.ntf.Notification;
import com.healthyrolls.domain.ntf.NotificationId;
import com.healthyrolls.domain.ntf.NotificationType;
import com.healthyrolls.technicalservices.persistence.ntf.NotificationProperties;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaNotificationRepository implements INotificationRepository {

    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "createdAt");

    private final SpringDataNotificationRepository jpa;
    private final NotificationProperties properties;

    public JpaNotificationRepository(SpringDataNotificationRepository jpa, NotificationProperties properties) {
        this.jpa = jpa;
        this.properties = properties;
    }

    @Override
    @Transactional
    public Notification save(Notification n) {
        jpa.save(NotificationEntity.from(n));
        return n;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Notification> findById(NotificationId id) {
        return jpa.findById(id.value()).map(NotificationEntity::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> findByRecipient(UserId id) {
        return jpa.findByRecipientId(id.value(), latest())
                .stream()
                .map(NotificationEntity::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> findOperatorAlerts() {
        return jpa.findByType(NotificationType.DISPATCH_ALERT.name(), latest())
                .stream()
                .map(NotificationEntity::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void deleteAll() {
        jpa.deleteAll();
    }

    private PageRequest latest() {
        return PageRequest.of(0, properties.getMaxNotificationsPerUser(), NEWEST_FIRST);
    }
}
