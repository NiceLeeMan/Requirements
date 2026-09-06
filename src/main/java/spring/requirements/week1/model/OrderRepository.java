package spring.requirements.week1.model;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findOrderById(int id);

    @EntityGraph(attributePaths = {"orderItems"})
    List<Order> findByUserId(Long userId);

    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.orderItems WHERE o.user.id = :userId")
    List<Order> findByUserIdWithItems(@Param("userId") Long userId);


}
