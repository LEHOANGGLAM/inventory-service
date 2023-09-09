package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yes4all.domain.BookingProformaInvoice;
import com.yes4all.domain.BookingPurchaseOrder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import javax.persistence.Column;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingPackingListDTO {
    private Integer id;
    private String fromCompany;
    private String soldToCompany;
    private String fromAddress;
    private String soldToAddress;
    private String fromFax;
    private String soldToFax;
    private String invoice;
    private LocalDate date;
    private String poNumber;
    private Instant createdAt;
    private String createdBy;
    private Instant updatedAt;
    private String updatedBy;
    private Integer status;
    private String soldToTelephone;
    private String fromTelephone;
    private LocalDateTime cds;
    private String supplier;
    @JsonProperty("details")
    private Set<BookingPackingListDetailsDTO> bookingPackingListDetailsDTO;


}
