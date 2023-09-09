package com.yes4all.web.rest.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.DateUtil;
import com.yes4all.common.utils.PageRequestUtil;
import com.yes4all.domain.enumeration.Channel;
import com.yes4all.domain.enumeration.Department;
import com.yes4all.domain.enumeration.IssueType;
import com.yes4all.service.IssueNoteService;
import com.yes4all.service.dto.request.*;
import com.yes4all.service.dto.response.IssueNoteInfoResponseDTO;
import com.yes4all.service.dto.response.IssueNoteListResponseDTO;
import com.yes4all.web.rest.payload.RestResponse;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.ws.rs.QueryParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing {@link com.yes4all.domain.IssueNote}.
 */
@RestController
@RequestMapping("/api")
public class IssueNoteResource {

    private final Logger log = LoggerFactory.getLogger(IssueNoteResource.class);

    @Autowired
    private IssueNoteService issueNoteService;

    /**
     * {@code GET  /issue-type/listing} : Get list Issue Type.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the Issue Type}.
     */
    @GetMapping("/issue-type/listing")
    public ResponseEntity<RestResponse<Object>> getListIssueType() {
        log.debug("REST request: /issue-type/listing --- Get list Issue Type");

        // Search data
        Map<String, String> result = IssueType.getKeyAndValue();
        if (CommonDataUtil.isNull(result)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(null).build());
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
    }

    /**
     * {@code GET  /channel/listing} : Get list Channel.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the Channel}.
     */
    @GetMapping("/channel/listing")
    public ResponseEntity<RestResponse<Object>> getListChannel() {
        log.debug("REST request: /channel/listing --- Get list Channel");

        // Search data
        Map<String, String> result = Channel.getKeyAndValue();
        if (CommonDataUtil.isNull(result)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(null).build());
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
    }

    /**
     * {@code POST  /issue-notes/listing} : get all the issueNotes.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of issueNotes in body.
     */
    @PostMapping("/issue-notes/listing")
    public ResponseEntity<RestResponse<Object>> findAll(
        @RequestBody(required = false) @Validated IssueNoteListRequestDTO issueNoteListReqDTO
    ) throws JsonProcessingException {
        log.debug("REST request: /issue-notes/listing --- Gel list Issue Note");

        // Paging
        Pageable pageable = PageRequestUtil.genPageRequest(
            issueNoteListReqDTO.getPage(),
            issueNoteListReqDTO.getSize(),
            Sort.Direction.DESC,
            "created_date"
        );
        // Search data
        Page<IssueNoteListResponseDTO> result = issueNoteService.listIssueNote(
            CommonDataUtil.toEmpty(issueNoteListReqDTO.getSearchBy()),
            CommonDataUtil.toEmpty(issueNoteListReqDTO.getSearchValue()),
            issueNoteListReqDTO.getWarehouseId(),
            CommonDataUtil.toEmpty(issueNoteListReqDTO.getFromDate()),
            CommonDataUtil.toEmpty(issueNoteListReqDTO.getToDate()),
            true,
            null,
            pageable
        );
        if (CommonDataUtil.isNull(result)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(null).build());
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
    }

    /**
     * {@code GET  /issue-notes/completed/listing} : get all the completed issueNotes.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of issueNotes in body.
     */
    @PostMapping("/issue-notes/completed/listing")
    public ResponseEntity<RestResponse<Object>> getListCompletedIssueNote(@RequestBody CompletedNoteRequestDTO filterParam) {
        log.debug("REST request to filter Completed Issue Notes");
        Page<IssueNoteListResponseDTO> result = issueNoteService.filterCompletedIssueNote(filterParam);
        return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
    }

    /**
     * {@code POST  /issue-notes/listing/export-excel} : export all the issueNotes for Excel.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the file.
     */
    @PostMapping("/issue-notes/listing/export-excel")
    public HttpEntity<ByteArrayResource> exportListIssueNote(
        @RequestBody(required = false) @Validated IssueNoteListExcelRequestDTO issueNoteListExcelRequestDTO
    ) {
        log.debug("REST request: /issue-notes/listing/export-excel --- Export list Issue Note");

        // Create file
        String fileName = DateUtil.formatDate(new Date(), DateUtil.STANDARD_DATE_TIME_CURRENT_FORMAT) + "_" + "OutboundList.xlsx";
        Pageable pageable = PageRequestUtil.genPageRequest(0, Integer.MAX_VALUE, Sort.Direction.DESC, "created_date");
        byte[] result = issueNoteService.listIssueNoteExport(
            fileName,
            CommonDataUtil.toEmpty(issueNoteListExcelRequestDTO.getSearchBy()),
            CommonDataUtil.toEmpty(issueNoteListExcelRequestDTO.getSearchValue()),
            issueNoteListExcelRequestDTO.getWarehouseId(),
            CommonDataUtil.toEmpty(issueNoteListExcelRequestDTO.getFromDate()),
            CommonDataUtil.toEmpty(issueNoteListExcelRequestDTO.getToDate()),
            issueNoteListExcelRequestDTO.isFlgAll(),
            issueNoteListExcelRequestDTO.getCodes(),
            issueNoteListExcelRequestDTO.getListColumn(),
            pageable
        );

        if (CommonDataUtil.isNull(result)) {
            return ResponseEntity.notFound().build();
        }
        HttpHeaders header = new HttpHeaders();
        header.setContentType(new MediaType("application", "force-download"));
        header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName + "");
        return new HttpEntity<>(new ByteArrayResource(result), header);
    }

    /**
     * {@code GET  /issue-notes/:issueCode} : get the "issueCode" issue note.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the issue note.
     */
    @GetMapping("/issue-notes/{issueCode}")
    public ResponseEntity<RestResponse<Object>> getIssueNoteByCode(
        @PathVariable String issueCode,
        @RequestParam(value = "warehouseId") Long warehouseId
    ) {
        log.debug("REST request: /issue-notes/{issueCode} --- Get info Issue Note");

        // Search data
        IssueNoteInfoResponseDTO result = issueNoteService.getIssueNoteByCode(issueCode, warehouseId);
        if (CommonDataUtil.isNull(result)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(null).build());
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
    }

    /**
     * {@code GET  issue-notes/{issueCode}/export-pdf} : export Issue Note to file pdf.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the issue note.
     */
    @GetMapping("issue-notes/{issueCode}/export-pdf")
    public HttpEntity<ByteArrayResource> exportIssueNotePdf(@PathVariable String issueCode) {
        log.debug("REST request: issue-notes/{issueCode}/export-pdf --- Export info Issue Note");

        // Create file pdf
        String fileName = DateUtil.formatDate(new Date(), DateUtil.STANDARD_DATE_TIME_CURRENT_FORMAT) + "_" + "Outbound.pdf";
        byte[] result = issueNoteService.exportIssueNotePdf(issueCode);
        if (CommonDataUtil.isNull(result)) {
            return ResponseEntity.notFound().build();
        }
        HttpHeaders header = new HttpHeaders();
        header.setContentType(new MediaType("application", "force-download"));
        header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName + "");
        return new HttpEntity<>(new ByteArrayResource(result), header);
    }

    /**
     * {@code POST  /issue-notes/{issueCode}/approved : approve the issue note.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body "Success".
     */
    @PatchMapping("/issue-notes/{issueCode}/approved")
    public ResponseEntity<RestResponse<Object>> approveIssueNote(
        @PathVariable("issueCode") String issueCode,
        @QueryParam("approveBy") String approveBy
    ) {
        log.debug("REST request: /issue-notes/{issueCode}/approved --- Approve data Issue Note");

        // Approve
        IssueNoteDTO issueNoteDTO = new IssueNoteDTO();
        issueNoteDTO.setUserName(approveBy);
        issueNoteDTO.setIssueCode(issueCode);
        boolean result = issueNoteService.approveIssueNote(issueNoteDTO);
        return result
            ? ResponseEntity.ok().body(RestResponse.builder().body("Issue Note was approved").build())
            : ResponseEntity.notFound().build();
    }

    /**
     * {@code POST  issue-notes/{issueCode} : update the IssueNote.
     *
     * @param issueCode the issueCode of the IssueNote to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("issue-notes/{issueCode}")
    public ResponseEntity<RestResponse<Object>> deleteIssueNote(
        @PathVariable("issueCode") String issueCode,
        @QueryParam("cancelBy") String cancelBy
    ) {
        log.debug("REST request: issue-notes/{issueCode} --- Delete Issue Note by issueCode");

        // Delete
        IssueNoteDTO issueNoteDTO = new IssueNoteDTO();
        issueNoteDTO.setUserName(cancelBy);
        issueNoteDTO.setIssueCode(issueCode);
        boolean result = issueNoteService.deleteIssueNote(issueNoteDTO);
        return result
            ? ResponseEntity.ok().body(RestResponse.builder().body("Receipt Note was canceled success").build())
            : ResponseEntity.notFound().build();
    }

    /**
     * {@code POST  /issue-notes} : save the entity issue note.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the issue note, or with status {@code 404 (Not Found)}.
     */
    @PostMapping("/issue-notes")
    public ResponseEntity<RestResponse<Object>> saveOrUpdateIssueNote(
        @RequestParam(required = false) Boolean isConfirmed,
        @RequestBody @Valid IssueNoteInfoRequestDTO issueNoteInfoReqDTO
    ) {
        log.debug("REST request: /issue-notes/info --- Insert Issue Note");

        if (CommonDataUtil.isNotNull(issueNoteInfoReqDTO)) {
            issueNoteInfoReqDTO.setIsConfirmed(!CommonDataUtil.isNull(isConfirmed) && isConfirmed);
            issueNoteInfoReqDTO.setIsManualCreate(true);
            IssueNoteInfoResponseDTO result = issueNoteService.saveOrUpdateIssueNote(issueNoteInfoReqDTO);
            return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * {@code POST  /system/issue-notes} : save the entity issue note.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the issue note.
     */
    @PostMapping("/system/issue-notes")
    public ResponseEntity<RestResponse<Object>> saveIssueNote(@RequestBody @Valid IssueNoteInfoRequestDTO issueNoteInfoReqDTO) {
        log.debug("REST request: /issue-notes/info --- Insert Issue Note");

        if (CommonDataUtil.isNotNull(issueNoteInfoReqDTO)) {
            issueNoteInfoReqDTO.setIsManualCreate(false);
            IssueNoteInfoResponseDTO result = issueNoteService.saveOrUpdateIssueNote(issueNoteInfoReqDTO);
            return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * {@code GET  /issue-notes/list-transfer} : get the list issueCode for transfer.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the issue note.
     */
    @GetMapping("/issue-notes/listing/code-transfer")
    public ResponseEntity<RestResponse<Object>> getListCodeTransfer() {
        log.debug("REST request: /issue-notes/list-transfer --- Get list issueCode for transfer");

        // Search data
        List<String> result = issueNoteService.getListCodeTransfer();
        if (CommonDataUtil.isNull(result)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(null).build());
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
    }

    /**
     * {@code GET  /departments} : get list Department.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body Departments.
     */
    @GetMapping("/departments")
    public ResponseEntity<RestResponse<Object>> getListDepartments() {
        log.debug("REST request: /departments --- Get list Department");

        // Search data
        Map<String, String> result = Department.getKeyAndValue();
        if (CommonDataUtil.isNull(result)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(null).build());
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
    }
}
