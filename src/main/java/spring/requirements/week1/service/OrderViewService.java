package spring.requirements.week1.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.requirements.week1.OrderDetails;
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

    // 기본 N+1 발생코드
    @Transactional(readOnly = true)
    public List<OrderDetails> getOrderList(long userId){
        List<Order> orderList = orderRepository.findByUserIdPlain(userId);

        return orderList.stream()
                .map(OrderDetails::new)
                .collect(Collectors.toList());
    }

    /**
     * FETCH_JOIN으로 인한 코드
     * 메서드 구조자체는 N+1유발과 동일. 연관 데이터 조회 시점을 루프 이전에 앞당김
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
     * N+1, FETCH_JOIN등과 구조는 동일. FETCH_JOIN과 다른점은 쿼리 제어도 차이일뿐, 나머지는 거의 동일
     * */
    @Transactional(readOnly = true)
    public List<OrderDetails> getOrderListEntityGraph(Long userId) {
        List<Order> orderList = orderRepository.findByUserId(userId);

        return orderList.stream()
                .map(OrderDetails::new)   // 원래 getOrderList와 똑같은 생성자
                .collect(Collectors.toList());
    }

    /**
     * 수동으로 IN 절 배치 조회 코드
     * 조인자체가 없다는게 위의 2가지와 다름. 단 데이터 조립으로인한 애플리케이션 코드 복잡도 증가
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
     * 엔티티 필드 혹은 설정파일 수준에서 배치사이즈 설정
     * 조인비용X, 단건씩 읽어오던 방식을 Size크기 단위로 읽기진행.
     * */
    @Transactional(readOnly = true)
    public List<OrderDetails> getOrderListByBatchSize(long userId){
        List<Order> orderList = orderRepository.findByUserIdPlain(userId);

        return orderList.stream()
                .map(OrderDetails::new)
                .collect(Collectors.toList());
    }


}
