package com.yes4all.service;

import com.yes4all.domain.IssueNote;
import com.yes4all.domain.ReceiptNote;
import com.yes4all.service.dto.request.CompletedNoteRequestDTO;
import com.yes4all.service.dto.request.IssueNoteDTO;
import com.yes4all.service.dto.request.IssueNoteInfoRequestDTO;
import com.yes4all.service.dto.response.IssueNoteInfoResponseDTO;
import com.yes4all.service.dto.response.IssueNoteListResponseDTO;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link IssueNote}.
 */
public interface IssueNoteService {
    /**
     * Get issueNotes with paging.
     *
     * @return the list of entities.
     */
    Page<IssueNoteListResponseDTO> listIssueNote(
        String searchBy,
        String searchValue,
        Long warehouseId,
        String fromDateStr,
        String toDateStr,
        boolean flgAll,
        String codes,
        Pageable pageable
    );

    Page<IssueNoteListResponseDTO> filterCompletedIssueNote(CompletedNoteRequestDTO requestParamDTO);

    /**
     * Get all the issueNotes.
     *
     * @return the list of entities.
     */
    byte[] listIssueNoteExport(
        String fileName,
        String searchBy,
        String searchValue,
        Long warehouseId,
        String fromDateStr,
        String toDateStr,
        boolean flgAll,
        String codes,
        List<String> listColumn,
        Pageable pageable
    );

    /**
     * Get issueNotes and detail with paging.
     *
     * @return the entities.
     */
    IssueNoteInfoResponseDTO getIssueNoteByCode(String issueCode, Long currentWarehouseId);

    /**
     * Approve data Issue Note
     *
     * @return the boolean.
     */
    boolean approveIssueNote(IssueNoteDTO issueNoteDTO);

    /**
     * Approve data Issue Note
     *
     * @return the boolean.
     */
    boolean completeIssueNote(IssueNoteDTO issueNoteDTO, ReceiptNote receiptNote);

    /**
     * Delete data Issue Note
     *
     * @return the boolean.
     */
    boolean deleteIssueNote(IssueNoteDTO issueNoteDTO);

    /**
     * Export IssueNote to pdf.
     *
     */
    byte[] exportIssueNotePdf(String issueCode);

    /**
     * Save issueNote.
     *
     * @return the entities.
     */
    IssueNoteInfoResponseDTO saveOrUpdateIssueNote(IssueNoteInfoRequestDTO issueNoteInfoReqDTO);

    void resetSequence();

    List<String> getListCodeTransfer();
}
