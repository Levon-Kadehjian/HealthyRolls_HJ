package com.healthyrolls.ui.ord;

import static org.springframework.http.HttpStatus.CREATED;

import com.healthyrolls.application.ord.OrderService;
import com.healthyrolls.domain.common.AuthenticatedUser;
import com.healthyrolls.domain.ord.menu.MenuItem;
import com.healthyrolls.domain.ord.menu.MenuItemStatus;
import com.healthyrolls.domain.ord.order.Order;
import com.healthyrolls.domain.ord.order.OrderId;
import com.healthyrolls.domain.ord.order.OrderLine;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
            @RequestAttribute(AuthenticatedUser.REQUEST_ATTRIBUTE) AuthenticatedUser user,
            @RequestBody PlaceOrderRequest req) {

        List<OrderLine> items = req.lines().stream()
                .map(line -> new OrderLine(
                        new MenuItem(line.menuItemId(), "pending", BigDecimal.ZERO, MenuItemStatus.AVAILABLE),
                        line.quantity()))
                .toList();

        OrderId orderId = orderService.placeOrder(req.restaurantId(), items, user);
        Order order = orderService.getOrder(orderId, user);

        return ResponseEntity.status(CREATED).body(OrderResponse.from(order));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> listOrders(
            @RequestAttribute(AuthenticatedUser.REQUEST_ATTRIBUTE) AuthenticatedUser user) {

        List<OrderResponse> orders = orderService.listOrders(user).stream()
                .map(OrderResponse::from)
                .toList();

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(
            @RequestAttribute(AuthenticatedUser.REQUEST_ATTRIBUTE) AuthenticatedUser user,
            @PathVariable UUID orderId) {

        Order order = orderService.getOrder(new OrderId(orderId), user);

        return ResponseEntity.ok(OrderResponse.from(order));
    }

    @PostMapping("/{orderId}/actions")
    public ResponseEntity<OrderResponse> applyAction(
            @RequestAttribute(AuthenticatedUser.REQUEST_ATTRIBUTE) AuthenticatedUser user,
            @PathVariable UUID orderId,
            @RequestBody OrderActionRequest req) {

        OrderId id = new OrderId(orderId);
        orderService.changeOrder(id, req.action(), req.reason(), user);
        Order order = orderService.getOrder(id, user);

        return ResponseEntity.ok(OrderResponse.from(order));
    }
}
