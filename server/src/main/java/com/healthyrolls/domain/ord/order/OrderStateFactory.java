package com.healthyrolls.domain.ord.order;

import com.healthyrolls.domain.ord.order.state.*;

public class OrderStateFactory {

    public AbstractOrderState fromStatus(OrderStatus status, Order order) {
        AbstractOrderState state = switch (status) {
            case PLACED -> new PlacedState(order);
            case ACCEPTED -> new AcceptedState();
            case PREPARING -> new PreparingState();
            case READY -> new ReadyState();
            case PICKED_UP -> new PickedUpState();
            case DELIVERED -> new DeliveredState();
            case CANCELLED -> new CancelledState();
        };
        state.order = order;
        return state;
    }
}
