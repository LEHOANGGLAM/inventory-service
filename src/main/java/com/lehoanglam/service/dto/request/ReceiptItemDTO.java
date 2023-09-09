package com.yes4all.service.dto.request;

import javax.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReceiptItemDTO {

    private Long id;

    @NotNull(message = "productId is required!")
    private Long productId;

    private String sku;
    @Builder.Default
    private Integer actualImportedQty = 0;
    private String note;
    @Builder.Default
    private Integer confirmedQty = 0;
    @Builder.Default
    private Integer differenceQty = 0;
}
