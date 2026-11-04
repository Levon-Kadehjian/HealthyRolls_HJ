package com.healthyrolls.technicalservices.persistence.dlv.jpa.courier;

import com.healthyrolls.domain.common.UserId;
import com.healthyrolls.domain.dlv.courier.Courier;
import com.healthyrolls.domain.dlv.courier.CourierStatus;
import com.healthyrolls.domain.ord.order.OrderId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "couriers")
public class CourierEntity {

    @Id
    private UUID courierId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourierStatus status;

    private UUID currentOrderId;

    protected CourierEntity() {
    }

    static CourierEntity from(Courier c) {
        CourierEntity e = new CourierEntity();
        e.courierId = c.getId().value();
        e.name = c.getName();
        e.latitude = c.getLatitude();
        e.longitude = c.getLongitude();
        e.status = c.getStatus();
        e.currentOrderId = c.getCurrentOrderId() == null ? null : c.getCurrentOrderId().value();
        return e;
    }

    Courier toDomain() {
        return new Courier(new UserId(courierId), name, latitude, longitude, status,
                currentOrderId == null ? null : new OrderId(currentOrderId));
    }
}
