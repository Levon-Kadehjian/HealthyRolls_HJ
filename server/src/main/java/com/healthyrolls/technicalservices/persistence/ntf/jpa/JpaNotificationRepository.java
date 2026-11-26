package com.healthyrolls.technicalservices.persistence.ntf.jpa;

import com.healthyrolls.domain.common.UserId;
import com.healthyrolls.domain.ntf.INotificationRepository;
import com.healthyrolls.domain.ntf.Notification;
import com.healthyrolls.domain.ntf.NotificationId;
import com.healthyrolls.domain.ntf.NotificationType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaNotificationRepository implements INotificationRepository {

    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "createdAt");

    private final SpringDataNotificationRepository jpa;

    public JpaNotificationRepository(SpringDataNotificationRepository jpa) {
        this.jpa = jpa;
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
        return findByRecipient(id.value(), 50);
    }

    @Transactional(readOnly = true)
    public List<Notification> findByRecipient(UUID id, int limit) {
        return jpa.findByRecipientId(id, PageRequest.of(0, limit, NEWEST_FIRST))
                .stream()
                .map(NotificationEntity::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void deleteAll() {
        clear();
    }

    @Transactional
    public boolean insertIfAbsent(Notification n) {
        if (jpa.existsBySourceId(n.getId().value())) {
            return false;
        }
        jpa.save(NotificationEntity.from(n));
        return true;
    }

    @Transactional(readOnly = true)
    public List<Notification> findOperatorAlerts(int limit) {
        return jpa.findByType(NotificationType.DISPATCH_ALERT.name(), PageRequest.of(0, limit, NEWEST_FIRST))
                .stream()
                .map(NotificationEntity::toDomain)
                .toList();
    }

    @Transactional
    public void clear() {
        jpa.deleteAll();
    }
}
