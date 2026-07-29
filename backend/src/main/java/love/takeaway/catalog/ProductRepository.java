package love.takeaway.catalog;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    @EntityGraph(attributePaths = {"category", "optionGroups"})
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findDetailedById(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"category", "optionGroups"})
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findForOrderById(@Param("id") Long id);

    boolean existsByCategoryId(Long categoryId);
}
