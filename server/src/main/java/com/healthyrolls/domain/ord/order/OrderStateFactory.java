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
            case PLACED -> PlacedState.INSTANCE;
            case ACCEPTED -> AcceptedState.INSTANCE;
            case PREPARING -> PreparingState.INSTANCE;
            case READY -> ReadyState.INSTANCE;
            case PICKED_UP -> PickedUpState.INSTANCE;
            case DELIVERED -> DeliveredState.INSTANCE;
            case CANCELLED -> CancelledState.INSTANCE;
            case REJECTED -> RejectedState.INSTANCE;
        };
        return state;
    }
}
