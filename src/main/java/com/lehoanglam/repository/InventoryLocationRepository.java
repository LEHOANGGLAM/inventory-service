package com.yes4all.repository;

import com.yes4all.domain.InventoryLocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the InventoryLocation entity.
 */
@SuppressWarnings("unused")
@Repository
public interface InventoryLocationRepository extends JpaRepository<InventoryLocation, Long> {
    Optional<InventoryLocation> findByWarehouseCodeLikeAndSkuLike(String warehouseCode, String sku);

    @Query(value = "SELECT * FROM inventory_location il WHERE ( upper( il.sku ) LIKE :sku OR il.sku IN :listSku ) \n" +
        "AND ( upper(il.warehouse_code) LIKE upper( :warehouseCode))\n" +
        "AND ( upper(il.product_title) LIKE upper( :productTitle ) OR il.product_title IN :listProductTitle)\n" +
        "AND ( upper(il.pickup_row) LIKE upper( :pickupRow ) OR il.pickup_row IN :listPickupRow OR :pickupRow ='%%')\n" +
        "AND ( upper(il.pku_location) LIKE upper( :pkuLocation ) OR il.pku_location IN :listPkuLocation OR :pkuLocation ='%%')",
        nativeQuery = true
    )
    Page<InventoryLocation> findInventoryLocationByCondition(@Param("sku") String sku,
                                                             @Param("warehouseCode") String warehouseCode,
                                                             @Param("listSku") List<String> skus,
                                                             @Param("productTitle") String productTitle,
                                                             @Param("listProductTitle") List<String> listProductTitle,
                                                             @Param("pickupRow") String pickupRow,
                                                             @Param("listPickupRow")List<String> listPickupRow,
                                                             @Param("pkuLocation") String pkuLocation,
                                                             @Param("listPkuLocation") List<String> listPkuLocation,
                                                             Pageable pageable
    );
    Page<InventoryLocation> findAllByIdIn(List<Long> listId,Pageable pageable);
}
