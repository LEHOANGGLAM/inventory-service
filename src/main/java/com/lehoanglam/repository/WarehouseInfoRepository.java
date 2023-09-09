package com.yes4all.repository;

import com.yes4all.domain.WarehouseInfo;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the WarehouseInfo entity.
 */
@SuppressWarnings("unused")
@Repository
public interface WarehouseInfoRepository extends JpaRepository<WarehouseInfo, Long> {}
