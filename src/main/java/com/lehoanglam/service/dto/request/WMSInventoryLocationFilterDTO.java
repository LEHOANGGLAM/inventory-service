package com.yes4all.service.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.*;

@Getter
@Builder
public class WMSInventoryLocationFilterDTO {

    @Builder.Default
    Integer pageNum = 0;

    @Builder.Default
    Integer offset = 10;

    @JsonProperty("warehouse_code")
    String warehouseCode;

    WMSInventoryLocationFilterParamDTO filter;

    @Builder
    public static class WMSInventoryLocationFilterParamDTO {

        @JsonProperty("product_sku")
        List<String> productSku;

        @JsonProperty("product_name")
        String productTitle;

        @JsonProperty("location")
        String pickupLocation;

        @JsonProperty("row")
        String pickupRow;
    }
}
