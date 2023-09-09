package com.yes4all.service.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yes4all.domain.Adjustment;
import com.yes4all.domain.enumeration.ReceiptNoteStatus;
import com.yes4all.domain.enumeration.ReceiptType;
import java.util.HashSet;
import java.util.Set;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReceiptNoteDTO {

    private String receiptCode;
    private Integer totalConfirmedQty;
    private Integer totalActualImportedQty;
    private Integer totalDifferenceQty;
    private String shipmentNo;
    private String containerNo;
    private String issueCode;
    private String warehouseCode;
    private String createdBy;
    private String modifiedBy;
    private String generalNote;
    private Boolean isConfirmed;
    private Adjustment adjustmentDto;
    private Boolean isManualCreate;
    private Set<ReceiptItemDTO> details = new HashSet<>();
    private String createdDate;
    private String receiptDate;

    @Pattern(regexp = "RECEIVING|RETURN|WHOLESALE|RETAIL", message = "please enter correct department!")
    private String department;

    @JsonProperty("warehouseId")
    @NotNull(message = "warehouseId is required!")
    private Long warehouse;

    @NotNull(message = "receiptType is required!")
    private ReceiptType receiptType;

    @NotNull(message = "receiptStatus is required!")
    private ReceiptNoteStatus receiptStatus;
}
