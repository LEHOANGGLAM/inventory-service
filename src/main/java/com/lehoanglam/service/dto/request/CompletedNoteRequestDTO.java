package com.yes4all.service.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompletedNoteRequestDTO {

    private Integer page;
    private Integer size;
    private String sku;
    private Long warehouseId;
    private String fromDate;
    private String toDate;
}
