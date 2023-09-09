package com.yes4all.service.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class AdjustmentItemResponseDTO {
    private Integer no;
    private String sku;
    private String asin;
    private String inboundCode;
    private String outboundCode;
    private String productTitle;
    private Integer wipQuantityBefore;
    private Integer pkuQuantityBefore;
    private Integer wipQuantityAfter;
    private Integer pkuQuantityAfter;
    private Integer totalQuantityBefore;
    private Integer totalQuantityAfter;
}
