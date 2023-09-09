package com.yes4all.service.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AdjustmentResponseDTO {
    private String warehouse;
    private String status;
    private String adjustmentCode;
    private Integer totalSku;
    private String dateCreated;
    private String createdBy;
    private String reason;
    private String otherNote;
}
