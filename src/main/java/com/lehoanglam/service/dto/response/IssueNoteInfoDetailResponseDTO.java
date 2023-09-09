package com.yes4all.service.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yes4all.common.utils.CommonDataUtil;
import java.math.BigDecimal;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueNoteInfoDetailResponseDTO {

    private Integer no;
    private Long id;
    private String sku;
    private String asin;
    private String productTitle;
    private String saleOrderNumber;
    private Integer confirmedQty;
    @JsonIgnore
    private String confirmedQtyStr;
    private Integer actualExportedQty;
    @JsonIgnore
    private String actualExportedQtyStr;
    private Integer remainingQty;
    @JsonIgnore
    private String remainingQtyStr;
    private String note;
    private Long productId;

    public String getConfirmedQtyStr() {
        confirmedQtyStr = "0";
        if (CommonDataUtil.isNotNull(confirmedQty)) {
            confirmedQtyStr = CommonDataUtil.quantityFormatter(confirmedQty);
        }
        return confirmedQtyStr;
    }

    public String getActualExportedQtyStr() {
        actualExportedQtyStr = "0";
        if (CommonDataUtil.isNotNull(actualExportedQty)) {
            actualExportedQtyStr = CommonDataUtil.quantityFormatter(actualExportedQty);
        }
        return actualExportedQtyStr;
    }

    public String getRemainingQtyStr() {
        remainingQtyStr = "0";
        if (CommonDataUtil.isNotNull(remainingQty)) {
            remainingQtyStr = CommonDataUtil.quantityFormatter(remainingQty);
        }
        return remainingQtyStr;
    }
}
