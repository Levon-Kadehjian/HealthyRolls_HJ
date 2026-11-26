package com.healthyrolls.technicalservices.persistence.ntf.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataNotificationRepository extends JpaRepository<NotificationRecord, UUID> {

    boolean existsBySourceId(UUID sourceId);

    List<NotificationRecord> findByRecipientId(UUID recipientId, Pageable page);

    List<NotificationRecord> findByType(String type, Pageable page);
}
