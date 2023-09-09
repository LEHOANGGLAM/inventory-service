package com.yes4all.repository;

import com.yes4all.domain.ReceiptItem;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the ReceiptItem entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ReceiptItemRepository extends JpaRepository<ReceiptItem, Long> {}
