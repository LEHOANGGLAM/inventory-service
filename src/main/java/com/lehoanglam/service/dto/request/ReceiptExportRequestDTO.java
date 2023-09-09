package com.yes4all.service.dto.request;

import java.util.List;
import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReceiptExportRequestDTO {
    @Builder.Default
    private String searchValue = "";
    private boolean flgAll;
    private String codes;
    private String fromDate;
    private String toDate;
    private List<String> listColumn;
    @Builder.Default
    private String searchBy = "";

    @NotNull(message = "warehouseId is required!")
    private Long warehouseId;
}
