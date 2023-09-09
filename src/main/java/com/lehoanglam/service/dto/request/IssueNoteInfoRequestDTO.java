package com.yes4all.service.dto.request;

import com.yes4all.common.constants.GlobalConstant;
import com.yes4all.domain.Adjustment;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueNoteInfoRequestDTO {

    private String issueCode;
    private Long warehouseIdFrom;
    private String issueDate;
    private String issueToName;
    private String issueToAddress;
    private Long warehouseIdTo;
    private Adjustment adjustmentDto;
    private String warehouseCodeFrom;
    private String warehouseCodeTo;
    private String channel;
    private Integer totalActualExportedQty;
    private Integer totalRemainingQty;
    private Boolean isManualCreate;
    private String createdBy;
    private String createdDate;
    private Boolean isConfirmed;

    @Size(max = 255, message = "note max size 255 character!")
    private String generalNote;

    @NotNull(message = "totalConfirmedQty is required!")
    private Integer totalConfirmedQty;

    @NotBlank(message = "issueType is required!")
    private String issueType;

    @Pattern(regexp = "RECEIVING|RETURN|WHOLESALE|RETAIL", message = "please enter correct department!")
    private String department;

    @Pattern(regexp = GlobalConstant.PHONE_PATTERN_REGEX, message = "Please enter correct phone number format!")
    private String issueToPhone;

    @Valid
    private Set<IssueNoteInfoDetailRequestDTO> details;
}
