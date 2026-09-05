package spring.requirements.week1;

import lombok.Getter;
import spring.requirements.week1.model.Order;
import spring.requirements.week1.model.OrderItem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 주문내역을 보여주는 내부 객체
 * OrderViewService에서, Order 엔티티를 화면에 필요한 형태로 변환할 때 사용한다.
 * */
@Getter
public class OrderDetails {

    private final String orderNumber;
    private final LocalDateTime orderDate;
    private final Order.OrderStatus status;
    private final List<OrderItemDetail> orderItems;

    public OrderDetails(Order order) {
        this.orderNumber = order.getOrderNumber();
        this.orderDate = order.getOrderDate();
        this.status = order.getStatus();
        this.orderItems = order.getOrderItems().stream()
                .map(OrderItemDetail::new)
                .collect(Collectors.toList());
    }

    @Getter
    public static class OrderItemDetail {
        private final String productName;
        private final int price;
        private final int quantity;

        public OrderItemDetail(OrderItem orderItem) {
            this.productName = orderItem.getProductName();
            this.price = orderItem.getPrice();
            this.quantity = orderItem.getQuantity();
        }
    }
}
