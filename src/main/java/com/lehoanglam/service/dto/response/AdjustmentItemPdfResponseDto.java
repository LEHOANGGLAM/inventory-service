package com.yes4all.service.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdjustmentItemPdfResponseDto {
    private String inboundCode;
    private String outboundCode;
    private Integer no;
    private String sku;
    private String asin;
    private String productTitle;
    private String wipQuantity;
    private String pkuQty;
    private String totalQty;
}
