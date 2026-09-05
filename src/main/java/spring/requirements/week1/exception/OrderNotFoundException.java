package spring.requirements.week1.exception;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(int orderId) {
        super("주문내역을 찾을 수 없습니다. orderId=" + orderId);
    }
}
