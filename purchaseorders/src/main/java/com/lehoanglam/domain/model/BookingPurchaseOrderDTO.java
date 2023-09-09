package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yes4all.domain.BookingPurchaseOrder;
import com.yes4all.domain.BookingPurchaseOrderLocation;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingPurchaseOrderDTO {
    private Integer id;
    private String sku;
    private String poNumber;
    private String aSin;
    private String title;
    private Long quantity;
    private Long quantityCtns;
    private Double fobPrice;
    private Double grossWeight;
    private Double cbm;
    private String shipToLocation;
    private String supplier;
}
