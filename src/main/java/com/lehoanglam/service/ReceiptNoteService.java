package com.yes4all.service;

import com.yes4all.domain.ReceiptNote;
import com.yes4all.service.dto.request.*;
import com.yes4all.service.dto.response.ReceiptNoteListResponseDTO;
import com.yes4all.service.dto.response.ReceiptNoteResponseDTO;
import org.springframework.data.domain.Page;

/**
 * Service Interface for managing {@link ReceiptNote}.
 */
public interface ReceiptNoteService {
    ReceiptNoteResponseDTO saveReceiptNote(ReceiptNoteDTO receiptNoteDto);
    ReceiptNoteResponseDTO saveOrUpdateReceiptNote(ReceiptNoteDTO receiptNoteDTO, Boolean isConfirmed);
    ReceiptNoteResponseDTO getReceiptNoteDetail(String receiptCode, Long warehouseId);
    ReceiptNoteResponseDTO updateReceiptNote(String receiptCode, ReceiptNoteDTO receiptNoteDTO);
    ReceiptNoteResponseDTO approveReceiptNote(String receiptCode, ReceiptChangeInfoDto receiptChangeInfoDto);
    ReceiptNoteResponseDTO completeReceiptNote(String receiptCode, ReceiptChangeInfoDto receiptChangeInfoDto);
    Page<ReceiptNoteListResponseDTO> getListReceiptNote(ReceiptRequestParam receiptRequestParam);
    void deleteReceiptNote(String receiptCode, ReceiptChangeInfoDto receiptChangeInfoDto);
    byte[] exportExcelFile(String fileName, ReceiptExportRequestDTO receiptExportRequestDto);
    byte[] exportReceiptNotePdf(String receiptCode);
    Page<ReceiptNoteResponseDTO> filterCompletedReceiptNote(CompletedNoteRequestDTO requestParamDTO);
}
