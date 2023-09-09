package com.yes4all.service.dto.response;

import com.yes4all.domain.enumeration.ReceiptType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReceiptNoteReportResponseDTO {

    private Long id;
    private Instant createdDate;
    private String shipmentNo;
    private String containerNo;
    private String issueCode;
    private String receiptCode;
    private Long warehouseId;
    private ReceiptType receiptType;
    private Integer totalImportedQty;
    private BigDecimal totalCost;
    private String createdBy;
    private Boolean isManualCreate;
    private Set<ReceiptNoteDetailResponseDTO> receiptItemDto = new HashSet<>();
}
