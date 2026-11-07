package com.healthyrolls.application.dlv.controllers;

import java.util.List;
import java.util.UUID;

import com.healthyrolls.domain.ord.order.Order;

public record CourierOrderResponse(
        UUID orderId,
        List<Item> items) {

    public record Item(String name, int quantity) {}

    public static CourierOrderResponse from(Order order) {
        List<Item> items = order.getItems().stream()
                .map(line -> new Item(
                        line.menuItem().getName(),
                        line.quantity()))
                .toList();

        return new CourierOrderResponse(
                order.getOrderId().value(),
                items);
    }
}