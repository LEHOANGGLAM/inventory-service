package com.yes4all.service.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
public class InventoryLocationResponseDTO {
    private Long id;
    private String warehouse;
    private String sku;
    private String productTitle;
    private String pickupRow;
    private String pkuLocation;
    private Integer pkuQuantity;
    private Integer ovsQuantity;
    private Integer wipQuantity;
    private Integer total;
}
