package love.takeaway.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class OrderDtos {

    private OrderDtos() {
    }

    public record CreateOrderRequest(
            @NotBlank @Size(min = 8, max = 64) String idempotencyKey,
            @Size(max = 500) String remark,
            @NotEmpty @Valid List<OrderLineRequest> items
    ) {
    }

    public record OrderLineRequest(
            @NotNull Long productId,
            @Min(1) @Max(99) int quantity,
            List<Long> optionIds
    ) {
    }

    public record ChangeOrderStatusRequest(
            @NotNull OrderStatus status,
            @Size(max = 500) String message
    ) {
    }

    public record OrderResponse(
            Long id,
            String orderNo,
            Long customerId,
            String customerName,
            OrderStatus status,
            String statusLabel,
            BigDecimal totalVirtualPrice,
            String remark,
            Instant createdAt,
            Instant updatedAt,
            List<OrderItemResponse> items,
            List<OrderEventResponse> events
    ) {
    }

    public record OrderItemResponse(
            Long id,
            Long productId,
            String productName,
            String productImage,
            String optionSummary,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal lineTotal
    ) {
    }

    public record OrderEventResponse(
            Long id,
            String operatorName,
            OrderStatus oldStatus,
            OrderStatus newStatus,
            String newStatusLabel,
            String message,
            Instant createdAt
    ) {
    }
}

