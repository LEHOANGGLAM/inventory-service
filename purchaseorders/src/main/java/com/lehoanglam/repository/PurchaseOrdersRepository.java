package com.yes4all.repository;

import com.yes4all.domain.PurchaseOrders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the PurchaseOrders entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PurchaseOrdersRepository extends JpaRepository<PurchaseOrders, Integer> {


    @Query(value = "SELECT p.* FROM purchase_orders p where p.is_deleted=false  and ( upper(p.po_number) like '%'||:poNumber||'%'  or :poNumber ='')" +
        " and ( upper(p.vendor_id) like '%'||:vendor||'%' or length(:vendor) =0) " +
        " and ( upper(p.country) like '%'||:country||'%' or length(:country) =0) " +
        " and ( upper(p.fulfillment_center) like '%'||:fulfillmentCenter||'%' or length(:fulfillmentCenter) =0) " +
        " and ( length(:updatedDateFrom) =0 or p.updated_date >=to_timestamp(:updatedDateFrom, 'yy-mm-dd T0:MI:SS.MS')  ) " +
        " and ( length(:updatedDateTo) =0 or p.updated_date <=to_timestamp(:updatedDateTo, 'yy-mm-dd T0:MI:SS.MS')   ) " +
        " and ( p.status =cast(:status as integer) or  cast(:status as integer) =-1) "+
        " and ( upper(p.vendor_id) =:supplier or length(:supplier) =0) "
        , nativeQuery = true)
    Page<PurchaseOrders> findByCondition(@Param("poNumber") String poNumber, @Param("vendor") String vendor
        , @Param("country") String country, @Param("fulfillmentCenter") String fulfillmentCenter
        , @Param("updatedDateFrom") String updatedDateFrom, @Param("updatedDateTo") String updatedDateTo, @Param("status") String status
        , @Param("supplier") String supplier
        , Pageable pageable);

    Page<PurchaseOrders> findByIsDeleted(Boolean isDeleted, Pageable pageable);


    Optional<PurchaseOrders> findByPoNumberAndIsDeleted(String poNumber,Boolean isDeleted);

    List<PurchaseOrders> findAllByPoNumber(String poNumber);

    @Query(value=" select count(1) as row from   proforma_invoice_detail a" +
        " inner join purchase_orders_detail b on b.purchase_order_id=a.purchase_order_id" +
        " and a.sku=b.sku and a.from_so=b.from_so and b.is_deleted=false " +
        " join LATERAL  (" +
        " select sum(qty) total_used from proforma_invoice_detail c where c.purchase_order_id=a.purchase_order_id" +
        "     and a.id<>c.id  and a.sku=c.sku and a.from_so=c.from_so and c.is_deleted=false  and a.purchase_order_no =c.purchase_order_no  " +
        " ) as total" +
        " on true" +
        " where a.proforma_invoice_id=:id and a.is_deleted=false " +
        " and (  b.qty_used>b.qty_ordered or total.total_used+a.qty>b.qty_used  )",nativeQuery = true)
    Integer countRowWrongData(@Param("id") Integer id);
 }
