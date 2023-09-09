package com.yes4all.repository;

import com.yes4all.domain.ProformaInvoice;
import com.yes4all.domain.ProformaInvoiceDetail;
import com.yes4all.domain.PurchaseOrders;
import com.yes4all.domain.PurchaseOrdersDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * Spring Data JPA repository for the PurchaseOrdersDetail entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ProformaInvoiceDetailRepository extends JpaRepository<ProformaInvoiceDetail, Long> {
    Set<ProformaInvoiceDetail> findByIsDeletedAndProformaInvoice(Boolean isDeleted, ProformaInvoice proformaInvoice);

    @Query(value = "SELECT coalesce(max(cdc_version),0) max FROM proforma_invoice_detail p where  proforma_invoice_id=:id and is_deleted=true  ", nativeQuery = true)
    Long findMaxCdcVersion(@Param("id") Integer id);


}
