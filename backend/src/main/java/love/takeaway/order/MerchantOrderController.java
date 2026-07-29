package love.takeaway.order;

import jakarta.validation.Valid;
import love.takeaway.common.PageResponse;
import org.springframework.web.bind.annotation.*;

import static love.takeaway.order.OrderDtos.*;

@RestController
@RequestMapping("/api/merchant/orders")
public class MerchantOrderController {

    private final OrderService orders;

    public MerchantOrderController(OrderService orders) {
        this.orders = orders;
    }

    @GetMapping
    public PageResponse<OrderResponse> orders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return orders.merchantOrders(status, page, size);
    }

    @GetMapping("/{id}")
    public OrderResponse detail(@PathVariable Long id) {
        return orders.detail(id);
    }

    @PatchMapping("/{id}/status")
    public OrderResponse changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody ChangeOrderStatusRequest request
    ) {
        return orders.changeStatus(id, request);
    }
}

