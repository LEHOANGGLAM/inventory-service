package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.yes4all.common.annotation.BooleanDeserializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommercialInvoiceDetailDTO {
    private Long Id;
    private String sku;
    private String productTitle;
    private Integer qty;
    private Double unitPrice;
    private Double amount;
    private String fromSo;
    private String aSin;
    @JsonDeserialize(using = BooleanDeserializer.class)
    private Boolean isDeleted;
    private String proformaInvoiceNo;
}
