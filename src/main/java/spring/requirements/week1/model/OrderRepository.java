package spring.requirements.week1.model;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"orderItems"})
    List<Order> findByUserId(Long userId);

    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.orderItems WHERE o.user.id = :userId")
    List<Order> findByUserIdWithItems(@Param("userId") Long userId);

    /**
     * EntityGraph, FETCH JOIN 없이 orderItems를 지연로딩 상태로 두는 순수 조회.
     * N+1(getOrderList) / BatchSize(getOrderListByBatchSize) 데모에서 사용한다.
     * findByUserId는 @EntityGraph가 항상 적용되어 있어 두 데모에 재사용할 수 없다.
     * */
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId")
    List<Order> findByUserIdPlain(@Param("userId") Long userId);


}
