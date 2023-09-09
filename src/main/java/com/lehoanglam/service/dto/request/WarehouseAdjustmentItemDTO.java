package com.yes4all.service.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class WarehouseAdjustmentItemDTO {

    private String sku;
    private Integer wip;
    private Integer pku;
}
