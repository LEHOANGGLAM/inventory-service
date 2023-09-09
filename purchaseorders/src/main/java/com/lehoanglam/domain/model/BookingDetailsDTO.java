package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yes4all.domain.BookingProformaInvoice;
import com.yes4all.domain.BookingPurchaseOrder;
import com.yes4all.domain.BookingPurchaseOrderLocation;
import com.yes4all.domain.Resource;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingDetailsDTO extends BookingMainDTO {
    private String portOfDischarge;
    private String container;
    private String invoice;
    private String portOfLoading;
    private String cds;
    private String destination;
    private LocalDate originEtd;
    private String freightMode;
    private LocalDate dischargeEta;
    private String fcrNo;
    private LocalDate estimatedDeliveryDate;
    private String poDest;
    private String freightTerms;
    private String shipToLocation;
    private String manufacturer;
    private Instant updatedAt;
    private String updatedBy;
    private Instant createdAt;
    private String vendorCode;
    private String stuffingLocation;
    @JsonProperty("products")
    private Page<BookingPurchaseOrderDTO> bookingPurchaseOrder;
    @JsonProperty("proformaInvoice")
    private Page<BookingProformaInvoiceMainDTO> bookingProformaInvoice;
    @JsonProperty("resource")
    private Page<ResourceDTO> resource;
    @JsonProperty("purchaseOrder")
    private List<BookingPurchaseOrderLocationDTO> bookingPurchaseOrderLocation;

}
