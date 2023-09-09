package com.yes4all.service.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class WarehouseAdjustmentRequestDTO {

    @JsonProperty("warehouse_code")
    private String warehouseCode;

    private String type;

    private String reference;

    private List<AdjustmentItemDTO> items;

    @Getter
    @Setter
    @Builder
    public static class AdjustmentItemDTO {

        private String sku;

        private String quantity;
    }
}
