package com.yes4all.service.dto.request;

import javax.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptRequestParam {

    @Builder.Default
    private String searchValue = "";

    @Builder.Default
    private String searchBy = "";

    private int size;
    private int page;
    private String fromDate;
    private String toDate;

    @NotNull(message = "warehouseId is required!")
    private Long warehouseId;
}
