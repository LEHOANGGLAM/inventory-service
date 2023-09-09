package com.yes4all.service.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReceiptNoteDetailResponseDTO {

    private Long id;
    private Long productId;
    private Integer no;
    private String sku;
    private String asin;
    private String productTitle;
    private Integer confirmedQty;
    private Integer actualImportedQty;
    private Integer differenceQty;
    private String note;
}
