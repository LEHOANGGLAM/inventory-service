package com.yes4all.repository;

import com.yes4all.domain.Booking;
import com.yes4all.domain.PurchaseOrders;
import com.yes4all.domain.PurchaseOrdersDetail;
import com.yes4all.domain.PurchaseOrdersSplit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the PurchaseOrders entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    @Query(value = "SELECT p.* FROM booking p where " +
        " ( upper(p.booking_confirmation) like '%'||:booking||'%' or :booking='') " +
        " and( upper(p.invoice) like '%'||:masterPO||'%' or :masterPO='') "+
        " and ( (p.id in (select booking_id from booking_purchase_order a where upper(a.po_number) like '%'||:poAmazon||'%' )) or :poAmazon='') " +
        " and (     length(:supplier) =0 or ((p.id in (select booking_id from booking_purchase_order a where upper(a.supplier) like '%'||:supplier||'%' )) ))  "
        ,nativeQuery = true)
    Page<Booking> findByCondition(@Param("booking") String booking,
                                  @Param("poAmazon") String poAmazon,
                                  @Param("masterPO") String masterPO,
                                  @Param("supplier") String supplier,
                                  Pageable pageable);

}
