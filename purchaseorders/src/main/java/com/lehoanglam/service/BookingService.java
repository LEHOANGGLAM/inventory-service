package com.yes4all.service;

import com.yes4all.domain.Booking;
import com.yes4all.domain.BookingPackingList;
import com.yes4all.domain.PurchaseOrders;
import com.yes4all.domain.model.*;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Service Interface for managing {@link PurchaseOrders}.
 */
public interface BookingService {
    BookingDTO createBooking(BookingDTO bookingDTO) ;

    BookingDTO completedBooking(Integer id) ;

    Booking save(MultipartFile file,String userId);
    BookingPackingListDTO submitPackingList( BookingPackingListDTO bookingPackingListDTO  ) ;
    Integer confirmPackingList( BookingPackingListDTO bookingPackingListDTO  ) ;

    BookingDetailsDTO getBookingDetailsDTO(BookingPageGetDetailDTO id);

    Integer updateBooking(BookingPackingListDTO request);


    BookingPackingListDTO getPackingListDetailsDTO(Integer id);

    Page<BookingMainDTO> listingBookingWithCondition(Integer page, Integer limit, Map<String, String> filterParams);
    void export(String filename,Integer id) throws IOException;




}
