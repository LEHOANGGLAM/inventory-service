package com.yes4all.repository;

import com.yes4all.domain.InventoryLog;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the InventoryLog entity.
 */
@SuppressWarnings("unused")
@Repository
public interface InventoryLogRepository extends JpaRepository<InventoryLog, Long> {}
