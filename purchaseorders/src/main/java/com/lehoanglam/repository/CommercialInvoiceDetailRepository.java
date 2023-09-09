package com.yes4all.repository;

import com.yes4all.domain.CommercialInvoice;
import com.yes4all.domain.CommercialInvoiceDetail;
import com.yes4all.domain.ProformaInvoice;
import com.yes4all.domain.ProformaInvoiceDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * Spring Data JPA repository for the PurchaseOrdersDetail entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CommercialInvoiceDetailRepository extends JpaRepository<CommercialInvoiceDetail, Long> {
    Set<CommercialInvoiceDetail> findByIsDeletedAndCommercialInvoice(Boolean isDeleted, CommercialInvoice commercialInvoice);

    @Query(value = "SELECT coalesce(max(cdc_version),0) max FROM commercial_invoice_detail p where  commercial_invoice_id=:id and is_deleted=true  ", nativeQuery = true)
    Long findMaxCdcVersion(@Param("id") Integer id);

}
