package com.yes4all.repository;

import com.yes4all.domain.InventoryLock;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the InventoryLock entity.
 */
@SuppressWarnings("unused")
@Repository
public interface InventoryLockRepository extends JpaRepository<InventoryLock, Long> {
    @Query(
        value = "select iq.close_qty from inventory_query iq \n" +
        "join product p on iq.product_id = p.id \n" +
        "join period_log pl on iq.period_id = pl.id\n" +
        "where product_id = :product_id and warehouse_id = :warehouse_id\n" +
        "order by pl.full_date DESC LIMIT 1",
        nativeQuery = true
    )
    Integer getCloseQuantityByProductAndWarehouse(@Param("product_id") Long productId, @Param("warehouse_id") Long warehouseId);

    @Query(
        value = "select sum(lock) as lock_qty\n" +
        "from inventory_lock il \n" +
        "where product_id = :product_id and warehouse_id = :warehouse_id " +
        "and (:issue_code IS NULL OR issue_code != :issue_code)",
        nativeQuery = true
    )
    Integer getLockQtyByProductIdAndWarehouse(
        @Param("product_id") Long productId,
        @Param("warehouse_id") Long warehouseId,
        @Param("issue_code") String issueCode
    );

    void deleteAllByIssueCode(String issueCode);
    void deleteByProductIdAndWarehouseId(Long productId, Long warehouseId);
}
