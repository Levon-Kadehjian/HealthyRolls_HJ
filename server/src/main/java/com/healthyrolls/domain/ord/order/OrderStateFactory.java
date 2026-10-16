package com.healthyrolls.domain.ord.order;

import com.healthyrolls.domain.ord.order.state.AcceptedState;
import com.healthyrolls.domain.ord.order.state.CancelledState;
import com.healthyrolls.domain.ord.order.state.DeliveredState;
import com.healthyrolls.domain.ord.order.state.PickedUpState;
import com.healthyrolls.domain.ord.order.state.PlacedState;
import com.healthyrolls.domain.ord.order.state.PreparingState;
import com.healthyrolls.domain.ord.order.state.ReadyState;
import com.healthyrolls.domain.ord.order.state.RejectedState;

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
            case REJECTED -> new RejectedState();
        };
        state.order = order;
        return state;
    }
}
