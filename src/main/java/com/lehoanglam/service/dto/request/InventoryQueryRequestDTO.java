package com.yes4all.service.dto.request;

import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryQueryRequestDTO {

    private Integer page;
    private Integer size;
    private String codes;

    @NotNull(message = "warehouseId is required!")
    private Long warehouseId;

    private String fromDate;
    private String toDate;
}
