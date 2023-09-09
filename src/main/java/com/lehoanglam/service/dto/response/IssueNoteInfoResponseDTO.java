package com.yes4all.service.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yes4all.common.utils.CommonDataUtil;
import java.util.Set;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueNoteInfoResponseDTO {

    private Long warehouseIdFrom;
    private String department;
    private String issueDate;
    private String warehouseNameFrom;
    private Long warehouseIdTo;
    private String warehouseNameTo;
    private String receiptCode;
    private String issueCode;
    private String issueToName;
    private String adjustmentCode;
    private String issueToAddress;
    private String issueToPhone;
    private Integer totalConfirmedQty;

    @JsonIgnore
    private String totalConfirmedQtyStr;

    private Integer totalActualExportedQty;

    @JsonIgnore
    private String totalActualExportedQtyStr;

    private Integer totalRemainingQty;

    @JsonIgnore
    private String totalRemainingQtyStr;

    private Boolean isManualCreate;
    private String issueType;
    private String channel;
    private String createdBy;
    private String createdDate;
    private String status;
    private String generalNote;

    private Set<IssueNoteInfoDetailResponseDTO> details;

    public String getTotalConfirmedQtyStr() {
        totalConfirmedQtyStr = "0";
        if (CommonDataUtil.isNotNull(totalConfirmedQty)) {
            totalConfirmedQtyStr = CommonDataUtil.quantityFormatter(totalConfirmedQty);
        }
        return totalConfirmedQtyStr;
    }

    public String getTotalActualExportedQtyStr() {
        totalActualExportedQtyStr = "0";
        if (CommonDataUtil.isNotNull(totalActualExportedQty)) {
            totalActualExportedQtyStr = CommonDataUtil.quantityFormatter(totalActualExportedQty);
        }
        return totalActualExportedQtyStr;
    }

    public String getTotalRemainingQtyStr() {
        totalRemainingQtyStr = "0";
        if (CommonDataUtil.isNotNull(totalRemainingQty)) {
            totalRemainingQtyStr = CommonDataUtil.quantityFormatter(totalRemainingQty);
        }
        return totalRemainingQtyStr;
    }
}
