package com.yes4all.service.dto;

import java.math.BigDecimal;

public interface InventoryFilterResultDTO {
    String getWarehouseName();

    String getSku();

    String getProductName();

    Integer getOpeningStock();

    Integer getClosingStock();

    Integer getImportedQty();

    Integer getExportedQty();
}
