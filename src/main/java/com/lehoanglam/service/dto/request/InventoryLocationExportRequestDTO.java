package com.yes4all.service.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
public class InventoryLocationExportRequestDTO {
    @Builder.Default
    private String sku = "";
    private boolean flgAll;
    private String codes;
    private List<String> listColumn;
    @Builder.Default
    private String searchBy = "";
    @NotNull(message = "warehouseCode is required!")
    private String warehouseCode;
    @Builder.Default
    private String productTitle = "";
    @Builder.Default
    private String pickupRow = "";
    @Builder.Default
    private String pickupLocation = "";
}
