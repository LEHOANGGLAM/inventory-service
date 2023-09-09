package com.yes4all.service;

import com.yes4all.service.dto.request.CompletedNoteRequestDTO;
import com.yes4all.service.dto.request.InventoryQueryExportRequestDTO;
import com.yes4all.service.dto.request.InventoryQueryRequestDTO;
import com.yes4all.service.dto.response.InventoryQueryResponseDTO;
import com.yes4all.service.dto.response.IssueNoteListResponseDTO;
import com.yes4all.service.dto.response.ReceiptNoteResponseDTO;
import org.springframework.data.domain.Page;

public interface InventoryQueryService {
    Page<InventoryQueryResponseDTO> getListInventoryByCondition(InventoryQueryRequestDTO filterParam);

    byte[] exportListingExcel(String fileName, InventoryQueryExportRequestDTO params);
}
