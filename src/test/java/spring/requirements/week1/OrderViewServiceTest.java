package spring.requirements.week1;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import spring.requirements.week1.model.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
class OrderViewServiceTest {

    @Autowired
    private OrderViewService orderViewService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void getOrderList_N플러스1_관측() {
        var result = orderViewService.getOrderList(1L);
        result.forEach(detail -> System.out.println(detail.getOrderNumber() + " / items=" + detail.getOrderItems().size()));
    }

    @Test
    void getOrderList_2_쿼리_2회_관측() {
        Long userId = userRepository.findAll().stream()
                .filter(user -> user.getEmail().equals("minjun.kim@example.com"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("테스트 사용자를 찾을 수 없습니다."))
                .getId();

        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();
        boolean statisticsWasEnabled = statistics.isStatisticsEnabled();

        statistics.setStatisticsEnabled(true);
        statistics.clear();

        try {
            var result = orderViewService.getOrderList_2(userId);
            long queryCount = statistics.getPrepareStatementCount();

            result.forEach(detail ->
                    System.out.println(detail.getOrderNumber() + " / items=" + detail.getOrderItems().size()));
            System.out.println("실행된 SQL 수 = " + queryCount);

            assertFalse(result.isEmpty());
            assertEquals(2L, queryCount);
        } finally {
            statistics.setStatisticsEnabled(statisticsWasEnabled);
        }
    }
}
