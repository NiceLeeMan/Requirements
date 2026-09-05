package spring.requirements.week1;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.requirements.week1.exception.OrderNotFoundException;
import spring.requirements.week1.model.Order;
import spring.requirements.week1.model.OrderRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderViewService {

    private final OrderRepository orderRepository;

    /***
     * 데이터베이스에서 주문내역상세를 보여주는 메소드
     * - 주문번호
     * - 주문 날짜
     * - 주문 상태
     * - 각 주문 품목들 정보
     */
    public OrderDetails getOrderDetails(int orderId) {
        Order order = orderRepository.findOrderById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        return new OrderDetails(order);
    }

    /***
     * 데이터베이스에서 주문내역상세를 보여주는 메소드 (N+1 유발)
     * - 주문번호
     * - 주문 날짜
     * - 주문 상태
     * - 각 주문 품목들 정보
     */
    public List<OrderDetails> getOrderList(long userId){
        List<Order> orderList = orderRepository.findByUserId(userId);

        List<OrderDetails> result = new ArrayList<>();
        for(Order order : orderList){
            result.add(new OrderDetails(order)); //N+1 발생
            System.out.println(order);
        }

        return result;

    }



}
