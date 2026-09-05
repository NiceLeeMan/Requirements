package spring.requirements.week1.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT o1 FROM  OrderItem o1 WHERE o1.order.id IN :orderIds")
    List<OrderItem> findAllByOrderIdIn(@Param("orderIds") List<Long> orderIds);

}
