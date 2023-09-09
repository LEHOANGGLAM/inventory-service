package com.yes4all.service.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class AdjustmentDetailResponseDTO {
    private String warehouse;
    private Integer totalSku;
    private String status;
    private String createdBy;
    private String adjustmentCode;
    private String createdDate;
    private String reason;
    private String otherNote;
    private List<AdjustmentItemResponseDTO> details;
}
