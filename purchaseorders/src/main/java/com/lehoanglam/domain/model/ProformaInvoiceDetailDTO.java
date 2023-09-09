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
public class ProformaInvoiceDetailDTO {
    private Long Id;
    private String sku;
    private String aSin;
    private String productTitle;
    private String barcode;
    private Integer qty;
    private Double unitPrice;
    private Double amount;
    private Double ctn;
    private Integer pcs;
    private Double cbmUnit;
    @JsonProperty("isDeleted")
    @JsonDeserialize(using = BooleanDeserializer.class)
    private boolean isDeleted;
    private Double cbmTotal;
    private Double grossWeight;
    private Double netWeight;
    private String fromSo;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate shipDate;
    private Long qtyOrdered;
}
