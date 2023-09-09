package com.yes4all.service.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryQueryResponseDTO {

    String warehouseName;

    String sku;

    String productName;

    Integer openingStock;

    Integer closingStock;

    Integer importedQty;

    Integer exportedQty;
}
