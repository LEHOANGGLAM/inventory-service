package com.yes4all.service.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ReceiptNoteListResponseDTO {
    private Long id;
    private String createdDate;
    private String receiptDate;
    private String shipmentNo;
    private String containerNo;
    private String issueCode;
    private String receiptCode;
    private String adjustmentCode;
    private String warehouse;
    private String warehouseFrom;
    private Long warehouseId;
    private String receiptType;
    private String department;
    private Integer totalActualImportedQty;
    private Integer totalConfirmedQty;
    private Integer totalDifferenceQty;
    private String createdBy;
    private String generalNote;
    private String receiptStatus;
    private Boolean isManualCreate;
}
