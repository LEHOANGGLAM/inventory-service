package com.yes4all.service.dto.request;

import javax.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueNoteInfoDetailRequestDTO {

    private Long id;
    private String sku;
    private Long productId;

    private String saleOrderNumber;

    @NotNull(message = "confirmedQty is required!")
    private Integer confirmedQty;

    private Integer actualExportedQty;
    private Integer remainingQty;
    private String note;
}
