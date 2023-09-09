package com.yes4all.repository;

import com.yes4all.domain.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data SQL repository for the Photo entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ResourceRepository extends JpaRepository<Resource, Integer> {
    List<Resource> findByFileTypeAndProformaInvoiceId(String fileType, Integer ProformaInvoiceId);
    List<Resource> findByFileTypeAndCommercialInvoiceId(String fileType, Integer commercialInvoiceId);
    Page<Resource> findByFileTypeAndBookingId(String fileType, Integer bookingId, Pageable pageable);

}
