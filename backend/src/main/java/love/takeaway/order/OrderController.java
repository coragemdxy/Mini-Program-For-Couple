package love.takeaway.order;

import jakarta.validation.Valid;
import love.takeaway.common.PageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static love.takeaway.order.OrderDtos.*;

@RestController
@RequestMapping("/api")
public class OrderController {

    private final OrderService orders;

    public OrderController(OrderService orders) {
        this.orders = orders;
    }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest request) {
        return orders.create(request);
    }

    @GetMapping("/orders/my")
    public PageResponse<OrderResponse> myOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return orders.myOrders(page, size);
    }

    @GetMapping("/orders/{id}")
    public OrderResponse detail(@PathVariable Long id) {
        return orders.detail(id);
    }

    @PostMapping("/orders/{id}/cancel")
    public OrderResponse cancel(@PathVariable Long id) {
        return orders.cancel(id);
    }
}

