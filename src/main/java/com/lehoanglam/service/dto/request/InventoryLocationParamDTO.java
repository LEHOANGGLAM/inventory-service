package com.yes4all.service.dto.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryLocationParamDTO {

    @Builder.Default
    private String sku = "";

    private Integer page;
    private Integer size;
    private String warehouseCode;
    private String productTitle;
    private String pickupRow;
    private String pickupLocation;
}
