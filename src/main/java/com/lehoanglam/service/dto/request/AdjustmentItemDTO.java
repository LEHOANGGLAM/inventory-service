package com.yes4all.service.dto.request;

import com.poiji.annotation.ExcelCellName;
import com.poiji.annotation.ExcelRow;
import com.poiji.annotation.ExcelUnknownCells;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.Map;

@Getter
@Setter
public class AdjustmentItemDTO {
    @ExcelRow
    private int rowIndex;
    @ExcelCellName("SKU")
    private String sku ;
    @ExcelCellName("WIP")
    private String wipQuantity;
    @ExcelCellName("PKU")
    private String pkuQuantity;
    @ExcelUnknownCells
    private Map<String, String> unknownCells;
}
