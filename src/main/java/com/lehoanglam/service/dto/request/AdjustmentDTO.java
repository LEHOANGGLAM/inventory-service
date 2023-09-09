package com.yes4all.service.dto.request;

import com.yes4all.common.constants.GlobalConstant;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Pattern;
import java.util.List;

@Getter
@Setter
@Builder
public class AdjustmentDTO {
    private String warehouseCode;
    private String createdBy;
    private String generalNote;
    @Pattern(regexp = GlobalConstant.REASON_PATTERN_REGEX, message = "please enter correct 'Reason'!")
    private String reason;
    private List<AdjustmentItemDTO> details;
}
