package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderSplitDataDTO {
    private Long Id;
    private String sku;
    private String saleOrder;
    private String aSin;
    private String productName;
    private Long qtyOrdered;
    private Long makeToStock;
    private String vendor;
    private String fulfillmentCenter;
    private LocalDate shipDate;
    private Double unitCost;
    private Double amount;
    private Double  grossWeight;
    private Double  netWeight;
    private Double cbm;
    private Integer pcs;
    private Double totalBox;
    private String country;
}
