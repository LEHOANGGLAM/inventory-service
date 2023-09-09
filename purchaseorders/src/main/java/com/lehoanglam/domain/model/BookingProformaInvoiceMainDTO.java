package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yes4all.domain.BookingProformaInvoice;
import com.yes4all.domain.BookingPurchaseOrder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingProformaInvoiceMainDTO {
    private Integer id;
    private String invoiceNo;
    private String poAmazon;
    private Long quantity;
    private Integer ctn;
    private Double cbm;
    private LocalDate shipDate;
    private String proformaInvoiceNo;
    private Integer bookingPackingListId;
    private Integer bookingPackingListStatus;
    private String supplier;
    @JsonProperty("packingList")
    private BookingPackingListDTO bookingPackingListDTO;
}
