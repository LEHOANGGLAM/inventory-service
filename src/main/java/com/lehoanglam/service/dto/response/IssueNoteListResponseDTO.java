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
public class IssueNoteListResponseDTO {

    private Long warehouseIdFrom;
    private String warehouseNameFrom;
    private String issueDate;
    private Long warehouseIdTo;
    private String warehouseNameTo;
    private String receiptCode;

    private String issueCode;
    private Integer totalConfirmedQty;
    private Integer totalActualExportedQty;

    @JsonIgnore
    private String totalConfirmedQtyStr;

    private Integer totalRemainingQty;
    private Boolean isManualCreate;
    private String issueType;
    private String channel;
    private String department;
    private String createdBy;
    private String createdDate;
    private String status;
    private String generalNote;
    private String issueToName;
    private String issueToAddress;
    private String issueToPhone;
    private String adjustmentCode;

    public String getTotalConfirmedQtyStr() {
        totalConfirmedQtyStr = "0";
        if (CommonDataUtil.isNotNull(totalConfirmedQty)) {
            totalConfirmedQtyStr = CommonDataUtil.quantityFormatter(totalConfirmedQty);
        }
        return totalConfirmedQtyStr;
    }
}
