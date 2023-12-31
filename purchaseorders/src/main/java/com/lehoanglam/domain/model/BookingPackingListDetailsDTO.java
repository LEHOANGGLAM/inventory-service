package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yes4all.domain.BookingPackingList;
import com.yes4all.domain.BookingProformaInvoice;
import com.yes4all.domain.BookingPurchaseOrder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingPackingListDetailsDTO{
    private Integer id;
    private String poNumber;
    private String sku;
    private String title;
    private String aSin;
    private Integer quantity;
    private Integer qtyEachCarton;
    private Double totalCarton;
    private Double netWeight;
    private Double grossWeight;
    private Double cbm;
    private Integer quantityPrevious;
    private Integer qtyEachCartonPrevious;
    private Double totalCartonPrevious;
    private Double netWeightPrevious;
    private Double grossWeightPrevious;
    private Double cbmPrevious;
    private String container;
    private String proformaInvoiceNo;
}
