package love.takeaway.order;

import org.springframework.stereotype.Component;

import static love.takeaway.order.OrderDtos.*;

@Component
public class OrderMapper {

    public OrderResponse toResponse(CustomerOrder order, boolean includeEvents) {
        return new OrderResponse(
                order.getId(),
                order.getOrderNo(),
                order.getCustomer().getId(),
                order.getCustomer().getNickname(),
                order.getStatus(),
                order.getStatus().getLabel(),
                order.getTotalVirtualPrice(),
                order.getRemark(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getItems().stream()
                        .map(item -> new OrderItemResponse(
                                item.getId(),
                                item.getProductId(),
                                item.getProductNameSnapshot(),
                                item.getProductImageSnapshot(),
                                item.getOptionSnapshot(),
                                item.getUnitPriceSnapshot(),
                                item.getQuantity(),
                                item.getLineTotal()
                        ))
                        .toList(),
                includeEvents
                        ? order.getEvents().stream()
                        .map(event -> new OrderEventResponse(
                                event.getId(),
                                event.getOperator().getNickname(),
                                event.getOldStatus(),
                                event.getNewStatus(),
                                event.getNewStatus().getLabel(),
                                event.getMessage(),
                                event.getCreatedAt()
                        ))
                        .toList()
                        : null
        );
    }
}

