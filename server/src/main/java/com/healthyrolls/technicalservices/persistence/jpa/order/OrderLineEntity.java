package com.healthyrolls.technicalservices.persistence.jpa.order;

import java.math.BigDecimal;
import java.util.UUID;

import com.healthyrolls.domain.ord.menu.MenuItem;
import com.healthyrolls.domain.ord.menu.MenuItemStatus;
import com.healthyrolls.domain.ord.order.OrderLine;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_lines")
public class OrderLineEntity {
    @Id
    private UUID lineId;

    @Column(nullable = false)
    private UUID itemId;

    @Column(nullable = false)
    private String itemName;

    @Column(nullable = false, columnDefinition = "numeric")
    private BigDecimal unitPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MenuItemStatus itemStatus;

    @Column(nullable = false)
    private int quantity;

    protected OrderLineEntity() {
    }

    public OrderLineEntity(OrderLine line) {
        this.lineId = UUID.randomUUID();
        this.itemId = line.menuItem().getItemId();
        this.itemName = line.menuItem().getName();
        this.unitPrice = line.menuItem().getPrice();
        this.itemStatus = line.menuItem().getStatus();
        this.quantity = line.quantity();
    }

    public OrderLine toDomain() {
        return new OrderLine(new MenuItem(itemId, itemName, unitPrice, itemStatus), quantity);
    }
}
