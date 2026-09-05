package spring.requirements.week1;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OrderViewServiceTest {

    @Autowired
    private OrderViewService orderViewService;

    @Test
    void getOrderList_N플러스1_관측() {
        var result = orderViewService.getOrderList(1L);
        result.forEach(detail -> System.out.println(detail.getOrderNumber() + " / items=" + detail.getOrderItems().size()));
    }
}
