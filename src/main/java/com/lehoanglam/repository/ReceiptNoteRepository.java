package com.yes4all.repository;

import com.yes4all.domain.ReceiptNote;
import com.yes4all.domain.Warehouse;
import com.yes4all.domain.enumeration.ReceiptNoteStatus;
import com.yes4all.domain.enumeration.ReceiptType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the ReceiptNote entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ReceiptNoteRepository extends JpaRepository<ReceiptNote, Long> {
    @Query(
        value = "select r from ReceiptNote r where " +
        " ( UPPER(r.receiptCode) like :receiptCode or UPPER(r.receiptCode) in :listReceiptCode )" +
        " and r.warehouse = :warehouse and r.isActive = true"
    )
    Page<ReceiptNote> findAllByReceiptCodeAndWarehouse(
        @Param("receiptCode") String receiptCode,
        @Param("listReceiptCode") List<String> listReceiptCode,
        @Param("warehouse") Warehouse warehouse,
        Pageable pageable
    );

    @Query(
        value = "select r from ReceiptNote r inner join r.receiptItems ri join ri.product p" +
        " where (UPPER(p.sku) like :sku or UPPER(p.sku) in :listSku) and r.warehouse = :warehouse and r.isActive = true"
    )
    Page<ReceiptNote> getAllByReceiptItems_ProductSkuLikeAndWarehouse(
        @Param("sku") String sku,
        @Param("listSku") List<String> listSku,
        @Param("warehouse") Warehouse warehouse,
        Pageable pageable
    );

    @Query(
        value = "select r from ReceiptNote r where r.receiptType in :receiptTypes and r.warehouse = :warehouse" + " and r.isActive = true"
    )
    Page<ReceiptNote> getAllByReceiptTypeLikeAndWarehouse(
        @Param("receiptTypes") List<ReceiptType> receiptTypes,
        @Param("warehouse") Warehouse warehouse,
        Pageable pageable
    );

    @Query(value = "select r from ReceiptNote r where r.status in :statuses and r.warehouse = :warehouse" + " and r.isActive = true")
    Page<ReceiptNote> getAllByStatusAndWarehouse(
        @Param("statuses") List<ReceiptNoteStatus> statuses,
        @Param("warehouse") Warehouse warehouse,
        Pageable pageable
    );

    @Query(
        value = "select r from ReceiptNote r where ( r.createdDate between :fromDate and :toDate )" +
        " and r.warehouse = :warehouse and r.isActive = true"
    )
    Page<ReceiptNote> getAllByCreatedDateBetweenAndWarehouse(
        @Param("fromDate") Instant fromDate,
        @Param("toDate") Instant toDate,
        @Param("warehouse") Warehouse warehouse,
        Pageable pageable
    );

    @Query(
        value = "select r from ReceiptNote r where ( r.receiptDate between :fromDate and :toDate )" +
        " and r.warehouse = :warehouse and r.isActive = true"
    )
    Page<ReceiptNote> getAllByReceiptDateBetweenAndWarehouse(
        @Param("fromDate") Instant fromDate,
        @Param("toDate") Instant toDate,
        @Param("warehouse") Warehouse warehouse,
        Pageable pageable
    );

    @Query(
        value = "SELECT rn.* FROM receipt_note rn INNER JOIN \n" +
        "       ( SELECT il.reference_id \n" +
        "       FROM inventory_log il\n" +
        "       INNER JOIN inventory_query iq ON il.product_id = iq.product_id AND il.warehouse_id = iq.warehouse_id\n" +
        "       INNER JOIN period_log pl ON pl.id = iq.period_id \n" +
        "       INNER JOIN product p ON p.sku = :sku AND p.id = iq.product_id \n" +
        "       WHERE pl.full_date BETWEEN :fromDate AND :toDate AND iq.warehouse_id =:warehouseId \n" +
        "       GROUP BY il.reference_id) AS ifilter \n" +
        "    ON rn.id = iFilter.reference_id\n" +
        "    ORDER BY rn.created_date DESC",
        nativeQuery = true
    )
    Page<ReceiptNote> filterCompleteReceiptNote(
        @Param("sku") String sku,
        @Param("fromDate") Instant fromDate,
        @Param("toDate") Instant toDate,
        @Param("warehouseId") Long warehouseId,
        Pageable pageable
    );

    Page<ReceiptNote> findAllByReceiptCodeIn(List<String> receiptCodes, Pageable pageable);

    Page<ReceiptNote> findAllByWarehouseAndIsActiveTrue(Warehouse warehouse, Pageable pageable);

    Optional<ReceiptNote> findByReceiptCode(String receiptCode);

    Boolean existsByReceiptCode(String receiptCode);

    @Modifying
    @Query(value = "UPDATE receipt_note SET status = :status WHERE receipt_code = :receiptCode", nativeQuery = true)
    ReceiptNote updateReceiptStatus(@Param("status") String status, @Param("receiptCode") String receiptCode);

    @Query(value = "SELECT NEXTVAL('sequence_receipt_note')", nativeQuery = true)
    Long getNextReceiptNoteValue();
}
