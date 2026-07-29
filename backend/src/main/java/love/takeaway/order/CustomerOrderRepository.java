package love.takeaway.order;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    @EntityGraph(attributePaths = {"customer", "items"})
    @Query("select o from CustomerOrder o where o.id = :id")
    Optional<CustomerOrder> findDetailedById(@Param("id") Long id);

    Optional<CustomerOrder> findByCustomerIdAndIdempotencyKey(Long customerId, String idempotencyKey);

    @EntityGraph(attributePaths = {"customer", "items"})
    Page<CustomerOrder> findByCustomerId(Long customerId, Pageable pageable);

    @EntityGraph(attributePaths = {"customer", "items"})
    Page<CustomerOrder> findByStatus(OrderStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"customer", "items"})
    Page<CustomerOrder> findAll(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"customer", "items"})
    @Query("select o from CustomerOrder o where o.id = :id")
    Optional<CustomerOrder> findForUpdateById(@Param("id") Long id);
}
