package com.yes4all.repository;

import com.yes4all.domain.Booking;
import com.yes4all.domain.BookingProformaInvoice;
import com.yes4all.domain.CommercialInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the PurchaseOrders entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BookingProformaInvoiceRepository extends JpaRepository<BookingProformaInvoice, Integer> {
    Page<BookingProformaInvoice> findAllByBooking(Booking booking, Pageable pageable );
 }
