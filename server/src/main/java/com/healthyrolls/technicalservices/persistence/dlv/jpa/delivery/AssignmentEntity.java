package com.healthyrolls.technicalservices.persistence.dlv.jpa.delivery;

import java.time.Instant;
import java.util.UUID;

import com.healthyrolls.domain.common.UserId;
import com.healthyrolls.domain.dlv.delivery.Assignment;
import com.healthyrolls.domain.ord.order.OrderId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "assignments")
public class AssignmentEntity {

    @Id
    private UUID orderId;

    @Column(nullable = false)
    private UUID courierId;

    @Column(nullable = false)
    private Instant assignedAt;

    private Instant acceptedAt;
    private Instant pickedUpAt;
    private Instant deliveredAt;

    @Column(nullable = false)
    private boolean active;

    protected AssignmentEntity() {
    }

    static AssignmentEntity from(Assignment assignment) {
        AssignmentEntity entity = new AssignmentEntity();
        entity.orderId = assignment.getOrderId().value();
        entity.courierId = assignment.getCourierId().value();
        entity.assignedAt = assignment.getAssignedAt();
        entity.acceptedAt = assignment.getAcceptedAt();
        entity.pickedUpAt = assignment.getPickedUpAt();
        entity.deliveredAt = assignment.getDeliveredAt();
        entity.active = assignment.isActive();
        return entity;
    }

    Assignment toDomain() {
        return new Assignment(
                new OrderId(orderId),
                new UserId(courierId),
                assignedAt,
                acceptedAt,
                pickedUpAt,
                deliveredAt,
                active);
    }
}