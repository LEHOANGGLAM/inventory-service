package com.yes4all.repository;

import com.yes4all.domain.InventoryQuery;
import com.yes4all.service.dto.InventoryFilterResultDTO;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the InventoryQuery entity.
 */
@SuppressWarnings("unused")
@Repository
public interface InventoryQueryRepository extends JpaRepository<InventoryQuery, Long> {
    @Query(
        value = "SELECT iq.*" +
        " FROM period_log pl INNER JOIN inventory_query iq ON pl.id = iq.period_id" +
        " WHERE " +
        " (:productId is NULL OR product_id = :productId)" +
        " OR (:sku IS NULL OR sku = :sku) " +
        " AND warehouse_id = :warehouseId" +
        " ORDER BY pl.full_date DESC" +
        " LIMIT 1",
        nativeQuery = true
    )
    InventoryQuery findFirstByProductIdAndWarehouseIdOrderByFullDateDesc(
        @Param("productId") Long productId,
        @Param("sku") String sku,
        @Param("warehouseId") Long warehouseId
    );

    @Query(
        value = "WITH inventory AS \n" +
        "       (SELECT p.sku AS sku, p.product_title AS productName, iq.warehouse_id , iq.id AS id, \n" +
        "               w.warehouse_name , iq.open_qty AS openingStock, iq.close_qty AS closingStock, \n" +
        "               iq.receipt_qty AS importedQty, iq.issue_qty AS exportedQty, pl.full_date\n" +
        "       FROM inventory_query iq \n" +
        "           JOIN warehouse w ON iq.warehouse_id = w.id\n" +
        "           JOIN product p ON iq.product_id = p.id\n" +
        "           JOIN period_log pl ON iq.period_id = pl.id AND pl.full_date BETWEEN :fromDate AND :toDate\n" +
        "       WHERE( p.sku LIKE :skuValue OR p.sku IN (:sku))\n" +
        "       AND iq.warehouse_id =:warehouseId)\n" +
        "   SELECT opening.sku, closingStock, openingStock, totalQty.importedQty, totalQty.exportedQty, \n" +
        "       totalQty.warehouse_name AS warehouseName, totalQty.productName\n" +
        "FROM (SELECT iq.sku, iq.closingStock\n" +
        "       FROM inventory iq ,\n" +
        "           ( SELECT max(iq.full_date) AS closeQty , iq.sku, iq.warehouse_id\n" +
        "           FROM inventory iq\n" +
        "           GROUP BY iq.sku, iq.warehouse_id ) closeQty\n" +
        "       WHERE iq.full_date = closeQty.closeQty\n" +
        "       AND iq.sku = closeQty.sku\n" +
        "       AND iq.warehouse_id = closeQty.warehouse_id ) closing\n" +
        "   INNER JOIN \n" +
        "       ( SELECT iq.sku , openingStock\n" +
        "       FROM inventory iq ,( SELECT min(iq.full_date) AS openQty , iq.sku, iq.warehouse_id\n" +
        "       FROM inventory iq\n" +
        "       GROUP BY iq.sku, iq.warehouse_id ) openingQty\n" +
        "   WHERE iq.full_date = openingQty.openQty\n" +
        "   AND iq.sku = openingQty.sku\n" +
        "   AND iq.warehouse_id = openingQty.warehouse_id ) opening\n" +
        "   ON closing.sku = opening.sku\n" +
        "INNER JOIN \n" +
        "   (SELECT iq.sku, SUM(importedQty) AS importedQty, SUM(exportedQty) AS exportedQty, iq.warehouse_name, iq.productName\n" +
        "   FROM inventory iq\n" +
        "   GROUP BY iq.warehouse_id, iq.sku, iq.warehouse_name, iq.productName) totalQty\n" +
        "ON opening.sku = totalQty.sku",
        countQuery = "SELECT count(inventoryCount.product_id)\n" +
        "   FROM(SELECT DISTINCT iq.warehouse_id , iq.product_id\n" +
        "   FROM inventory_query iq\n" +
        "       JOIN warehouse w ON iq.warehouse_id = w.id\n" +
        "       JOIN product p ON iq.product_id = p.id\n" +
        "       JOIN period_log pl ON iq.period_id = pl.id\n" +
        "   AND pl.full_date BETWEEN :fromDate AND :toDate\n" +
        "   WHERE( p.sku LIKE :skuValue OR p.sku IN (:sku))\n" +
        "   AND iq.warehouse_id =:warehouseId) AS inventoryCount\n",
        nativeQuery = true
    )
    Page<InventoryFilterResultDTO> findListInventoryByCondition(
        @Param("skuValue") String skuSearch,
        @Param("sku") List<String> sku,
        @Param("fromDate") Instant fromDate,
        @Param("toDate") Instant toDate,
        @Param("warehouseId") Long warehouseId,
        Pageable pageable
    );
}
