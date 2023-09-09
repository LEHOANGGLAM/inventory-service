package com.yes4all.service.dto.request;

import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdjustmentParamDTO {
    @Builder.Default
    private String searchValue = "";

    @Builder.Default
    private String searchBy = "";

    private int size;
    private int page;
    private String fromDate;
    private String toDate;

    @NotNull(message = "warehouseCode is required!")
    private String warehouseCode;
}
