package com.yes4all.service.dto.request;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdjustmentExportRequestDTO {
    @Builder.Default
    private String searchValue = "";
    private boolean flgAll;
    private String codes;
    private String fromDate;
    private String toDate;
    private List<String> listColumn;
    @Builder.Default
    private String searchBy = "";

    @NotNull(message = "warehouseCode is required!")
    private String warehouseCode;
}
