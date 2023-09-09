package com.yes4all.service.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class WarehouseAdjustmentUpdateQtyDTO {

    private String note;
    private String adjType;

    @JsonProperty("warehouse_code")
    private String warehouseCode;

    private List<WarehouseAdjustmentItemDTO> details;
}
