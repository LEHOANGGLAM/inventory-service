package com.yes4all.service.dto.request;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryQueryExportRequestDTO extends InventoryQueryRequestDTO {

    private boolean flgAll;
    private List<String> listColumn;
}
