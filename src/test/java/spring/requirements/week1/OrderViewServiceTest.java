package spring.requirements.week1;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.CollectionStatistics;
import org.hibernate.stat.EntityStatistics;
import org.hibernate.stat.QueryStatistics;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import spring.requirements.week1.model.UserRepository;
import spring.requirements.week1.service.OrderViewService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * OrderViewService의 각 조회 방식(N+1 / FETCH JOIN / EntityGraph / 수동 IN절 배치 / BatchSize)이
 * 실제로 몇 번의 쿼리를 발생시키고, 각 쿼리(혹은 컬렉션 로딩)가 몇 행을 읽어오는지 관측하기 위한 테스트.
 *
 * Hibernate의 Statistics를 이용해
 * 1) 총 쿼리(Prepared Statement) 수
 * 2) JPQL로 추적되는 쿼리별 실행횟수 / 총 읽은 행수 / 실행당 평균 행수
 * 3) 엔티티별 로드된 행수(Order, OrderItem)
 * 4) orderItems 컬렉션이 지연/배치로딩된 횟수와, 로딩 1회당 평균 행수
 * 를 로그로 남긴다.
 */
@SpringBootTest
class OrderViewServiceTest {

    @Autowired
    private OrderViewService orderViewService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private Statistics statistics;
    private boolean statisticsWasEnabled;

    @BeforeEach
    void setUp() {
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        statistics = sessionFactory.getStatistics();
        statisticsWasEnabled = statistics.isStatisticsEnabled();
        statistics.setStatisticsEnabled(true);
        statistics.clear();
    }

    @AfterEach
    void tearDown() {
        statistics.setStatisticsEnabled(statisticsWasEnabled);
    }

    private Long findTestUserId() {
        return userRepository.findAll().stream()
                .filter(user -> user.getEmail().equals("minjun.kim@example.com"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("테스트 사용자를 찾을 수 없습니다."))
                .getId();
    }

    /**
     * 테스트 대상 userId를 조회하고, 그 과정에서 발생한 쿼리(users 조회)는
     * 통계에서 제외하기 위해 초기화한다. 이후 통계는 서비스 메서드 호출만 반영한다.
     */
    private Long prepareTestUserId() {
        Long userId = findTestUserId();
        statistics.clear();
        return userId;
    }

    @Test
    void getOrderList_기본_N플러스1_관측() {
        // 참고: Order.orderItems 필드에 @BatchSize(size = 50)가 항상 붙어있기 때문에,
        // 실제로는 주문 1건당 쿼리 1개가 아니라 "최대 50건씩 묶어서" 컬렉션을 로딩한다.
        // 즉 테스트 데이터가 50건을 넘지 않으면 getOrderListByBatchSize()와 쿼리 패턴이 동일하게 관측된다.
        Long userId = prepareTestUserId();
        List<OrderDetails> result = orderViewService.getOrderList(userId.longValue()); // long 오버로드
        logStatistics("getOrderList(long) - 기본 N+1 발생코드", result);
    }

    @Test
    void getOrderList_FetchJoin_관측() {
        Long userId = prepareTestUserId();
        List<OrderDetails> result = orderViewService.getOrderListFetchJoin(userId);
        logStatistics("getOrderListFetchJoin - FETCH JOIN", result);
    }

    @Test
    void getOrderList_EntityGraph_관측() {
        Long userId = prepareTestUserId();
        List<OrderDetails> result = orderViewService.getOrderListEntityGraph(userId);
        logStatistics("getOrderListEntityGraph - @EntityGraph", result);
    }

    @Test
    void getOrderList_수동IN절_배치조회_관측() {
        Long userId = prepareTestUserId();
        List<OrderDetails> result = orderViewService.getOrderList(userId); // Long 오버로드
        logStatistics("getOrderList(Long) - 수동 IN절 배치조회", result);

        assertFalse(result.isEmpty());
        assertEquals(2L, statistics.getPrepareStatementCount());
    }

    @Test
    void getOrderList_BatchSize_관측() {
        Long userId = prepareTestUserId();
        List<OrderDetails> result = orderViewService.getOrderListByBatchSize(userId);
        logStatistics("getOrderListByBatchSize - @BatchSize(50)", result);
    }

    /**
     * 결과를 검증하고, 발생한 쿼리/행수 통계를 콘솔에 로그로 남긴다.
     */
    private void logStatistics(String label, List<OrderDetails> result) {
        assertFalse(result.isEmpty());

        System.out.println("========== [" + label + "] ==========");
        System.out.println("결과 주문 수 = " + result.size());
        result.forEach(detail ->
                System.out.println("  주문번호=" + detail.getOrderNumber() + ", items=" + detail.getOrderItems().size()));

        System.out.println("총 쿼리(Prepared Statement) 수 = " + statistics.getPrepareStatementCount());

        for (String query : statistics.getQueries()) {
            QueryStatistics qs = statistics.getQueryStatistics(query);
            long execCount = qs.getExecutionCount();
            long rowCount = qs.getExecutionRowCount();
            double avgRows = execCount == 0 ? 0 : (double) rowCount / execCount;
            System.out.printf("  [쿼리] %s%n      실행횟수=%d, 총읽은행수=%d, 실행당평균행수=%.2f%n",
                    query, execCount, rowCount, avgRows);
        }

        long orderItemLoadCount = 0;
        for (String entityName : statistics.getEntityNames()) {
            EntityStatistics es = statistics.getEntityStatistics(entityName);
            if (es.getLoadCount() > 0) {
                System.out.println("  [엔티티 로드행수] " + entityName + " = " + es.getLoadCount());
                if (entityName.contains("OrderItem")) {
                    orderItemLoadCount = es.getLoadCount();
                }
            }
        }

        // 참고: 컬렉션 로드횟수는 "컬렉션 인스턴스가 초기화된 횟수"이다.
        // JOIN FETCH/EntityGraph처럼 최초 쿼리에 함께 조회된 경우에도 주문 건수만큼 집계되지만
        // 이때는 추가 SQL이 나가지 않는다 (위 총 쿼리 수와 비교해서 판단할 것).
        for (String role : statistics.getCollectionRoleNames()) {
            CollectionStatistics cs = statistics.getCollectionStatistics(role);
            long collLoadCount = cs.getLoadCount();
            if (collLoadCount > 0) {
                double avgRowsPerLoad = orderItemLoadCount > 0 ? (double) orderItemLoadCount / collLoadCount : 0;
                System.out.printf("  [컬렉션 로드] %s -> 컬렉션 초기화 횟수=%d, 초기화당 평균 행수=%.2f%n",
                        role, collLoadCount, avgRowsPerLoad);
            }
        }
    }
}
