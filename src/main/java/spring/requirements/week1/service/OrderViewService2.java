package spring.requirements.week1.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.requirements.week1.OrderDetails;
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
public class OrderViewService2 {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    /**
     * 수동으로 IN 절 배치 조회
     * 장점 : N+1은 방지됨. 쿼리횟수도 2회로 고정
     * 단점: 데이터 조립메서드로 인해, 메소드가 너무 커지고, 가독성이 떨어짐
     * */
    @Transactional(readOnly = true)
    public List<OrderDetails> getOrderList(Long userId){

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

    /**
     * FETCH_JOIN으로 인한 코드
     *
     * */
    @Transactional(readOnly = true)
    public List<OrderDetails> getOrderListFetchJoin(Long userId) {
        List<Order> orderList = orderRepository.findByUserIdWithItems(userId);
        return orderList.stream()
                .map(OrderDetails::new)   // 원래 getOrderList와 똑같은 생성자
                .collect(Collectors.toList());
    }

    /**
     * Entity_Graph로 N+1을 해결한 코드
     * */
    @Transactional(readOnly = true)
    public List<OrderDetails> getOrderListEntityGraph(Long userId) {

        List<Order> orderList = orderRepository.findByUserId(userId);

        return orderList.stream()
                .map(OrderDetails::new)   // 원래 getOrderList와 똑같은 생성자
                .collect(Collectors.toList());
    }



 //===================================HELPER METHOD===================================

}
