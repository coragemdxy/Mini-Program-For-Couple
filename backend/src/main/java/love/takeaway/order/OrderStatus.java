package love.takeaway.order;

import love.takeaway.common.ApiException;

import java.util.EnumSet;
import java.util.Set;

public enum OrderStatus {
    SUBMITTED("已提交"),
    ACCEPTED("已接受"),
    PREPARING("准备中"),
    COMPLETED("已完成"),
    REJECTED("已拒绝"),
    CANCELLED("已取消");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean canTransitionTo(OrderStatus target) {
        return allowedTargets().contains(target);
    }

    private Set<OrderStatus> allowedTargets() {
        return switch (this) {
            case SUBMITTED -> EnumSet.of(ACCEPTED, REJECTED, CANCELLED);
            case ACCEPTED -> EnumSet.of(PREPARING, CANCELLED);
            case PREPARING -> EnumSet.of(COMPLETED, CANCELLED);
            case COMPLETED, REJECTED, CANCELLED -> EnumSet.noneOf(OrderStatus.class);
        };
    }

    public void requireTransitionTo(OrderStatus target) {
        if (!canTransitionTo(target)) {
            throw ApiException.conflict(
                    "ORDER_STATUS_TRANSITION_INVALID",
                    "订单不能从“" + label + "”变为“" + target.label + "”"
            );
        }
    }
}

