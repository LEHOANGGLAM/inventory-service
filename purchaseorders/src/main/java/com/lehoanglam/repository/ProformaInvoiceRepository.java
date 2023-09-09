package com.yes4all.repository;

import com.yes4all.domain.ProformaInvoice;
import com.yes4all.domain.ProformaInvoiceDetail;
import com.yes4all.domain.PurchaseOrders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the PurchaseOrders entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ProformaInvoiceRepository extends JpaRepository<ProformaInvoice, Integer> {

    @Query(value = "SELECT p.* FROM proforma_invoice p " +
        " LEFT JOIN PURCHASE_ORDERS PO ON PO.ID=p.purchase_order_id " +
        " where " +
        " p.is_deleted=false " +
        " and ( length(:updatedBy) =0  or upper(p.updated_by) like '%'||:updatedBy||'%' )" +
        " and ( length(:orderNo) =0  or upper(p.order_no) like '%'||:orderNo||'%' )" +
        " and ( length(:fromSO) =0 or exists (select 1 from proforma_invoice_detail b" +
        " where p.id=b.proforma_invoice_id and upper(b.from_so) like '%'||:fromSO||'%') ) " +
        " and ( length(:poNumber) =0  or upper(PO.po_number) like '%'||:poNumber||'%' ) " +
        " and ( upper(p.term) like '%'||:term||'%' or length(:term) =0) " +
        " and ( length(:shipDateFrom) =0 or p.ship_date >=TO_DATE(:shipDateFrom, 'yyyy-mm-dd')  ) " +
        " and ( length(:shipDateTo) =0 or p.ship_date <=TO_DATE(:shipDateTo, 'yyyy-mm-dd')  ) " +
        " and ( p.status =cast(:status as integer) or  cast(:status as integer) =-1) " +
        " and ( p.amount >=cast(:amountFrom as double precision) or cast(:amountFrom as double precision)=0) " +
        " and ( p.amount <=cast(:amountTo as double precision) or cast(:amountTo as double precision)=0) " +
        " and ( length(:updatedDateFrom) =0 or p.updated_date >=to_timestamp(:updatedDateFrom, 'yy-mm-dd T0:MI:SS.MS')  ) " +
        " and ( length(:updatedDateTo) =0 or p.updated_date <=to_timestamp(:updatedDateTo, 'yy-mm-dd T0:MI:SS.MS')   )  " +
        " and ( upper(p.supplier)=:supplier or length(:supplier) =0)"
        , nativeQuery = true)
    Page<ProformaInvoice> findByCondition(@Param("updatedBy") String updatedBy, @Param("orderNo") String orderNo
        , @Param("fromSO") String fromSO
        , @Param("poNumber") String poNumber, @Param("term") String term
        , @Param("shipDateFrom") String shipDateFrom, @Param("shipDateTo") String shipDateTo
        , @Param("amountFrom") String amountFrom, @Param("amountTo") String amountTo
        , @Param("status") String status, @Param("updatedDateFrom") String updatedDateFrom
        , @Param("updatedDateTo") String updatedDateTo
        , @Param("supplier") String supplier
        , Pageable pageable);

    @Query(value = "select a.* from proforma_invoice a where a.is_deleted=false and a.id in (" +
        " select proforma_invoice_id from proforma_invoice_detail where purchase_order_id=:purchaseOrderId and  is_deleted=false  " +
        ")", nativeQuery = true)
    List<ProformaInvoice> findAllByPurchaseOrderId(@Param("purchaseOrderId") Integer purchaseOrderId);

    @Query(value = "select a.* from proforma_invoice a where a.is_deleted=false" +
        " and a.id in (" +
        " select proforma_invoice_id from proforma_invoice_detail " +
        " where purchase_order_id=(select id from purchase_orders where po_number=:orderNo and is_deleted=false    ) and  is_deleted=false  " +
        ")", nativeQuery = true)
    List<ProformaInvoice> findAllByOrderNo(@Param("orderNo") String orderNo);


    @Query(value = " select count(1) as row from   commercial_invoice_detail a" +
        " inner join proforma_invoice_detail b on b.proforma_invoice_id=a.proforma_invoice_id" +
        " and a.sku=b.sku and a.from_so=b.from_so and b.is_deleted=false " +
        " join LATERAL  (" +
        " select sum(qty) total_used from commercial_invoice_detail c where c.proforma_invoice_id=a.proforma_invoice_id" +
        "     and a.id<>c.id  and a.sku=c.sku and a.from_so=c.from_so and c.is_deleted=false and a.purchase_order_no=c.purchase_order_no " +
        " ) as total" +
        " on true" +
        " where a.commercial_invoice_id=:id and a.is_deleted=false " +
        " and (  b.qty_used>b.qty or total.total_used+a.qty>b.qty_used  )", nativeQuery = true)
    Integer countRowWrongData(@Param("id") Integer id);

    Optional<ProformaInvoice> findByOrderNoAndIsDeleted(String orderNo,Boolean isDeleted);
}
