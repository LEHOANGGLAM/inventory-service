package com.yes4all.repository;

import com.yes4all.domain.Adjustment;
import com.yes4all.domain.enumeration.Reason;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Adjustment entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AdjustmentRepository extends JpaRepository<Adjustment, Long> {
    Optional<Adjustment> findByAdjustmentCode(String adjustmentCode);

    @Query(
        value = "select a from Adjustment a where " +
        "a.warehouseCode = :warehouseCode and " +
        "( a.adjustmentCode like :adjustmentCode or a.adjustmentCode in :listAdjustment )"
    )
    Page<Adjustment> findAllByAdjustmentCodeAndWarehouseCode(
        @Param("warehouseCode") String warehouseCode,
        @Param("listAdjustment") List<String> listAdjustment,
        @Param("adjustmentCode") String adjustmentCode,
        Pageable pageable
    );

    @Query(value = "select a from Adjustment a where a.warehouseCode = :warehouseCode and a.reason in :listReason")
    Page<Adjustment> findAllByReasonAndWarehouse(
        @Param("warehouseCode") String warehouseCode,
        @Param("listReason") List<Reason> reasonList,
        Pageable pageable
    );

    @Query(value = "select a from Adjustment a where ( a.dateCreated between :fromDate and :toDate ) and a.warehouseCode = :warehouseCode ")
    Page<Adjustment> findAllByDateCreatedAndWarehouse(
        @Param("fromDate") Instant fromDate,
        @Param("toDate") Instant toDate,
        @Param("warehouseCode") String warehouseCode,
        Pageable pageable
    );

    Page<Adjustment> findAllByWarehouseCodeLike(String warehouseCode, Pageable pageable);
    Page<Adjustment> findAllByAdjustmentCodeIn(List<String> adjustmentCode, Pageable pageable);

    @Query(value = "SELECT NEXTVAL('sequence_adjustment')", nativeQuery = true)
    Long getNextAdjustmentValue();
}
