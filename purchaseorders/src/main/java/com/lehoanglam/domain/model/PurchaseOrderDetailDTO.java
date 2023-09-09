package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.yes4all.common.annotation.BooleanDeserializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderDetailDTO {
    private Integer Id;
    private String sku;
    private String fromSo;
    private String aSin;
    private String productName;
    private Long qtyPIUsed;
    private Long qtyCIUsed;
    private Long qtyOrdered;
    private Long qtyAvailable;
    private Long makeToStock;
    private Double unitCost;
    private Double totalCost;
    private String bookingNumber;
    @JsonProperty("isDeleted")
    private boolean isDeleted;
    List<ListPICIUsedPODTO> listPICIUsedPODTO;
    private Integer purchaseOrderId;
    private Double amount;
    private Integer pcs;
    private Double totalVolume;
    private Double totalBox;
    private Double totalBoxCrossWeight;
    private Double unitCostPrevious;
    private Long qtyOrderedPrevious;
    private Double amountPrevious;
    private Integer pcsPrevious;
    private Double totalVolumePrevious;
    private Double totalBoxPrevious;
    private Double totalBoxCrossWeightPrevious;
    private LocalDate ship_date;
    private Integer onboardQty;
    private Double totalBoxNetWeight;
    private Double totalBoxNetWeightPrevious;

}
