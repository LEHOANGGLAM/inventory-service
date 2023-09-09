package com.yes4all.repository;

import com.yes4all.domain.AdjustmentItem;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the AdjustmentItem entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AdjustmentItemRepository extends JpaRepository<AdjustmentItem, Long> {}
