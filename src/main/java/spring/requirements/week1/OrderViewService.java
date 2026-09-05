package spring.requirements.week1;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.requirements.week1.exception.OrderNotFoundException;
import spring.requirements.week1.model.Order;
import spring.requirements.week1.model.OrderItem;
import spring.requirements.week1.model.OrderItemRepository;
import spring.requirements.week1.model.OrderRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderViewService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

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
    @Transactional(readOnly = true)
    public List<OrderDetails> getOrderList(long userId){
        List<Order> orderList = orderRepository.findByUserId(userId);

        List<OrderDetails> result = new ArrayList<>();
        for(Order order : orderList){
            result.add(new OrderDetails(order)); //N+1 발생
            System.out.println(order);
        }

        return result;
    }

    /**
     * 수동으로 IN 절 배치 조죄 후곃ㄴ
     *
     * */
    @Transactional(readOnly = true)
    public List<OrderDetails> getOrderList_2(Long userId){

        List<Order> orderList = orderRepository.findByUserId(userId);

        List<Long> orderIds = orderList.stream().
                map(Order::getId).toList();

        List<OrderItem> items = orderItemRepository.findAllByOrderIdIn(orderIds);

        Map<Long, List<OrderItem>> orderItemsById = items.stream().
                collect(Collectors.groupingBy(item -> item.getOrder().getId()));

        List<OrderDetails> result = new ArrayList<>();

        for(Order order : orderList){
            List<OrderItem> orderItems = orderItemsById.getOrDefault(order.getId(), Collections.emptyList());
            result.add(new OrderDetails(order, orderItems));
        }

        return result;



    }



}
