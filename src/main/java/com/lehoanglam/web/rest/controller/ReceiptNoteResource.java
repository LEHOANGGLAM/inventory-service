package com.yes4all.web.rest.controller;

import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.DateUtil;
import com.yes4all.domain.enumeration.ReceiptType;
import com.yes4all.repository.ReceiptNoteRepository;
import com.yes4all.service.ReceiptNoteService;
import com.yes4all.service.dto.request.*;
import com.yes4all.service.dto.response.ReceiptNoteListResponseDTO;
import com.yes4all.service.dto.response.ReceiptNoteResponseDTO;
import com.yes4all.web.rest.payload.RestResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import javax.validation.Valid;
import javax.ws.rs.QueryParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing {@link com.yes4all.domain.ReceiptNote}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class ReceiptNoteResource {

    private final Logger log = LoggerFactory.getLogger(ReceiptNoteResource.class);

    private static final String ENTITY_NAME = "inventoryManagementReceiptNote";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ReceiptNoteRepository receiptNoteRepository;

    @Autowired
    private ReceiptNoteService receiptNoteService;

    public ReceiptNoteResource(ReceiptNoteRepository receiptNoteRepository) {
        this.receiptNoteRepository = receiptNoteRepository;
    }

    /**
     * {@code POST  /receipt-notes} : Create a new receiptNote.
     *
     * @param "receiptNote" the receiptNote to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new receiptNote, or with status {@code 400 (Bad Request)} if the receiptNote has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/receipt-notes")
    public ResponseEntity<RestResponse<Object>> createReceiptNote(
        @Valid @RequestBody ReceiptNoteDTO receiptNoteDto,
        @RequestParam("isConfirmed") Boolean isConfirmed
    ) throws URISyntaxException {
        log.debug("REST request to save ReceiptNote : {}", receiptNoteDto);
        receiptNoteDto.setIsManualCreate(true);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(RestResponse.builder().body(receiptNoteService.saveOrUpdateReceiptNote(receiptNoteDto, isConfirmed)).build());
    }

    /**
     * {@code POST  /system/receipt-notes} : Create a new receiptNote.
     *
     * @param "receiptNote" the receiptNote to create by System.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new receiptNote, or with status {@code 400 (Bad Request)} if the receiptNote has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/system/receipt-notes")
    public ResponseEntity<RestResponse<Object>> createReceiptNoteBySystem(@RequestBody ReceiptNoteDTO receiptNoteDto) {
        log.debug("REST request to save ReceiptNote : {}", receiptNoteDto);
        receiptNoteDto.setIsManualCreate(false);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(RestResponse.builder().body(receiptNoteService.saveOrUpdateReceiptNote(receiptNoteDto, true)).build());
    }

    /**
     * {@code GET  /receipt-notes/info/export/:receiptCode} : export Receipt Note to file pdf.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the Receipt note.
     */
    @GetMapping("/receipt-notes/{receiptCode}/export-pdf")
    public ResponseEntity<byte[]> exportReceiptNotePdf(@PathVariable String receiptCode) {
        log.debug("REST request: receipt-notes/info/export/{receiptCode} --- Export info Receipt Note");

        // Create file pdf
        String fileName = DateUtil.formatDate(new Date(), DateUtil.STANDARD_DATE_TIME_CURRENT_FORMAT) + "_" + "Inbound.pdf";
        byte[] result = receiptNoteService.exportReceiptNotePdf(receiptCode);
        if (CommonDataUtil.isNotNull(result)) {
            HttpHeaders header = new HttpHeaders();
            header.setContentType(new MediaType("application", "force-download"));
            header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName + "");
            return new ResponseEntity<byte[]>(result, header, HttpStatus.OK);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Return HTTP 400 if no data
    }

    /**
     * {@code GET  /receipt-notes/:id} : get the "id" receiptNote.
     *
     * @param receiptCode the id of the receiptNote to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the receiptNote, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/receipt-notes/{receiptCode}")
    public ResponseEntity<RestResponse<Object>> getReceiptNoteByReceiptCode(
        @PathVariable String receiptCode,
        @RequestParam(value = "warehouseId") Long warehouseId
    ) {
        log.debug("REST request to get ReceiptNote : {}", receiptCode);
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(RestResponse.builder().body(receiptNoteService.getReceiptNoteDetail(receiptCode, warehouseId)).build());
    }

    /**
     * {@code GET  /receipt-notes} : get all the receiptNotes.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of receiptNotes in body.
     */
    @PostMapping("/receipt-notes/listing")
    public ResponseEntity<RestResponse<Object>> getAllReceiptNotes(@RequestBody ReceiptRequestParam filterParams) {
        log.debug("REST request to get all ReceiptNotes");
        Page<ReceiptNoteListResponseDTO> result = receiptNoteService.getListReceiptNote(filterParams);
        return ResponseEntity.status(HttpStatus.OK).body(RestResponse.builder().body(result).build());
    }

    /**
     * {@code GET  /receipt-notes/completed/listing} : get all the completed receiptNotes.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of receiptNotes in body.
     */
    @PostMapping("/receipt-notes/completed/listing")
    public ResponseEntity<RestResponse<Object>> getListCompletedReceiptNote(@RequestBody CompletedNoteRequestDTO filterParam) {
        log.debug("REST request to filter Completed Receipt Notes");
        Page<ReceiptNoteResponseDTO> result = receiptNoteService.filterCompletedReceiptNote(filterParam);
        return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
    }

    /**
     * {@code PATCH  /receipt-notes/approve/{receiptCode} : approve the receiptNotes status by receiptCode.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of receiptNotes in body.
     */
    @PatchMapping("/receipt-notes/{receiptCode}/approved")
    public ResponseEntity<RestResponse<Object>> approveReceiptNoteStatus(
        @PathVariable("receiptCode") String receiptCode,
        @QueryParam("approveBy") String approveBy
    ) {
        log.debug("REST request to approve ReceiptNote : {}", receiptCode);
        ReceiptChangeInfoDto receiptChangeInfoDto = new ReceiptChangeInfoDto();
        receiptChangeInfoDto.setUsername(approveBy);
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(RestResponse.builder().body(receiptNoteService.approveReceiptNote(receiptCode, receiptChangeInfoDto)).build());
    }

    /**
     * {@code PATCH  /receipt-notes/{receiptCode}/completed} : complete the receiptNotes status by receiptCode.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of receiptNotes in body.
     */
    @PatchMapping("/receipt-notes/{receiptCode}/completed")
    public ResponseEntity<RestResponse<Object>> completeReceiptNoteStatus(
        @PathVariable("receiptCode") String receiptCode,
        @QueryParam("completeBy") String completeBy
    ) {
        log.debug("REST request to complete ReceiptNote : {}", receiptCode);
        ReceiptChangeInfoDto receiptChangeInfoDto = new ReceiptChangeInfoDto();
        receiptChangeInfoDto.setUsername(completeBy);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(RestResponse.builder().body(receiptNoteService.completeReceiptNote(receiptCode, receiptChangeInfoDto)).build());
    }

    /**
     * {@code GET  /receipt-type : Get all receipt type.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of receiptNotes in body.
     */
    @GetMapping("/receipt-type/listing")
    public ResponseEntity<RestResponse<Object>> getReceiptType() {
        log.debug("REST request to get all receipt type ReceiptNote ");
        return ResponseEntity.status(HttpStatus.CREATED).body(RestResponse.builder().body(ReceiptType.getKeyAndValue()).build());
    }

    @PostMapping("/receipt-notes/listing/export-excel")
    public HttpEntity<byte[]> exportReceiptNoteFile(@RequestBody ReceiptExportRequestDTO filterParams) throws IOException {
        log.debug("REST request to export List ReceiptNotes");
        log.info("START download product template");
        String fileName = DateUtil.formatDate(new Date(), DateUtil.STANDARD_DATE_TIME_CURRENT_FORMAT) + "_" + "InboundList.xlsx";

        byte[] result = receiptNoteService.exportExcelFile(fileName, filterParams);

        if (CommonDataUtil.isNotNull(result)) {
            HttpHeaders header = new HttpHeaders();
            header.setContentType(new MediaType("application", "force-download"));
            header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName + "");
            return new ResponseEntity<byte[]>(result, header, HttpStatus.OK);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Return HTTP 400 if no data
    }

    /**
     * {@code DELETE  /receipt-notes/:receiptCode} : delete the "receiptCode" receiptNote.
     *
     * @param receiptCode the id of the receiptNote to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/receipt-notes/{receiptCode}")
    public ResponseEntity<RestResponse<Object>> deleteReceiptNote(
        @PathVariable String receiptCode,
        @QueryParam("cancelBy") String cancelBy
    ) {
        log.debug("REST request to delete ReceiptNote : {}", receiptCode);
        ReceiptChangeInfoDto receiptChangeInfoDto = new ReceiptChangeInfoDto();
        receiptChangeInfoDto.setUsername(cancelBy);
        receiptNoteService.deleteReceiptNote(receiptCode, receiptChangeInfoDto);
        return ResponseEntity.ok().body(RestResponse.builder().body("Receipt Note was canceled success").build());
    }
}
