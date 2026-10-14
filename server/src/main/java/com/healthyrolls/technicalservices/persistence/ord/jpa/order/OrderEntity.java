package com.healthyrolls.technicalservices.persistence.ord.jpa.order;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.healthyrolls.domain.ord.order.Order;
import com.healthyrolls.domain.ord.order.OrderRecord;
import com.healthyrolls.domain.ord.order.OrderStatus;

import jakarta.persistence.*;

@Entity
@Table(name = "orders")
public class OrderEntity {
    @Id
    private UUID orderId;

    @Column(nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private UUID restaurantId;

    private UUID courierId;
    private Instant acceptedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id", nullable = false)
    @OrderColumn(name = "line_position")
    private List<OrderLineEntity> items = new ArrayList<>();

    protected OrderEntity() {
    }

    public OrderEntity(Order order) {
        this.orderId = order.getOrderId().value();
        this.customerId = order.getCustomerId();
        this.restaurantId = order.getRestaurantId();
        this.courierId = order.getCourierId();
        this.acceptedAt = order.getAcceptedAt();
        this.status = order.status();
        for (var line : order.getItems()) {
            items.add(new OrderLineEntity(line));
        }
    }

    public OrderRecord toRecord() {
        return new OrderRecord(orderId, status.name(), customerId, restaurantId,
                courierId, acceptedAt, items.stream().map(OrderLineEntity::toDomain).toList());
    }
}
