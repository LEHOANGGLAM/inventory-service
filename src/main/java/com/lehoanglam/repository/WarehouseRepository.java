package com.yes4all.repository;

import com.yes4all.domain.Warehouse;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Warehouse entity.
 */
@SuppressWarnings("unused")
@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    Optional<Warehouse> findFirstByWarehouseCode(String warehouseCode);
}
