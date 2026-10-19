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

    public AbstractOrderState fromStatus(OrderStatus status) {
        AbstractOrderState state = switch (status) {
            case NONE -> throw new IllegalArgumentException("NONE is not a state an order can be switched to");
            case PLACED -> new PlacedState();
            case ACCEPTED -> new AcceptedState();
            case PREPARING -> new PreparingState();
            case READY -> new ReadyState();
            case PICKED_UP -> new PickedUpState();
            case DELIVERED -> new DeliveredState();
            case CANCELLED -> new CancelledState();
            case REJECTED -> new RejectedState();
        };
        return state;
    }
}
