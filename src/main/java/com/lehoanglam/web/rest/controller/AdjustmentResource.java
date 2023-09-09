package com.yes4all.web.rest.controller;

import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.DateUtil;
import com.yes4all.domain.Adjustment;
import com.yes4all.domain.enumeration.Reason;
import com.yes4all.repository.AdjustmentRepository;
import com.yes4all.service.AdjustmentService;
import com.yes4all.service.dto.request.AdjustmentDTO;
import com.yes4all.service.dto.request.AdjustmentExportRequestDTO;
import com.yes4all.service.dto.request.AdjustmentParamDTO;
import com.yes4all.service.dto.request.ReceiptExportRequestDTO;
import com.yes4all.service.dto.response.AdjustmentDetailResponseDTO;
import com.yes4all.service.dto.response.AdjustmentResponseDTO;
import com.yes4all.service.dto.response.ReceiptNoteResponseDTO;
import com.yes4all.web.rest.errors.BadRequestAlertException;
import com.yes4all.web.rest.payload.RestResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.yes4all.domain.Adjustment}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class AdjustmentResource {

    private final Logger log = LoggerFactory.getLogger(AdjustmentResource.class);

    private static final String ENTITY_NAME = "inventoryManagementAdjustment";

    @Autowired
    AdjustmentService adjustmentService;

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AdjustmentRepository adjustmentRepository;

    public AdjustmentResource(AdjustmentRepository adjustmentRepository) {
        this.adjustmentRepository = adjustmentRepository;
    }

    /**
     * {@code POST  /adjustments} : Create a new adjustment.
     *
     * @param file the adjustment to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new adjustment, or with status {@code 400 (Bad Request)} if the adjustment has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping(value = "/adjustments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RestResponse<Object>> createAdjustment(
        @RequestParam("file") MultipartFile file,
        @RequestParam("warehouseCode") String warehouseCode,
        @RequestParam("createdBy") String createdBy,
        @RequestParam("generalNote") String generalNote,
        @RequestParam("reason") String reason
    ) throws URISyntaxException, IOException {
        log.debug("REST request to save Adjustment : {}", file);
        AdjustmentDTO adjustmentDTO = AdjustmentDTO
            .builder()
            .createdBy(createdBy)
            .generalNote(generalNote)
            .warehouseCode(warehouseCode)
            .reason(reason)
            .build();
        AdjustmentDetailResponseDTO result = adjustmentService.createAdjustment(file, adjustmentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(RestResponse.builder().body(result).build());
    }

    /**
     * {@code POST  /adjustments/validation} : Validate a adjustment file upload.
     *
     * @param file the adjustment to validate for create.
     * @return the {@link ResponseEntity} with status {@code 201 (Validated)} and with body the new adjustment, or with status {@code 400 (Bad Request)} if the adjustment has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping(value = "/adjustments/validation", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RestResponse<Object>> doValidateAdjustmentFileUpload(
        @RequestPart("file") MultipartFile file,
        @RequestPart("warehouseCode") String warehouseCode
    ) throws URISyntaxException, IOException {
        log.debug("REST request to validation Adjustment : {}", file);

        boolean result = adjustmentService.doValidateAdjustmentFileUpload(file, warehouseCode);
        return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
    }

    /**
     * {@code GET  /adjustments} : get all the adjustments.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of adjustments in body.
     */
    @PostMapping("/adjustments/listing")
    public ResponseEntity<RestResponse<Object>> getAllAdjustments(@RequestBody AdjustmentParamDTO filterParam) {
        log.debug("REST request to filter Adjustment by condition.");
        Page<AdjustmentResponseDTO> result = adjustmentService.getListAdjustmentByCondition(filterParam);
        return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
    }

    /**
     * {@code GET  /adjustments/:adjustmentCode} : get the "adjustmentCode" adjustment.
     *
     * @param adjustmentCode the adjustmentCode of the adjustment to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the adjustment, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/adjustments/{adjustmentCode}")
    public ResponseEntity<RestResponse<Object>> getAdjustment(
        @PathVariable String adjustmentCode,
        @RequestParam("warehouseCode") String warehouseCode
    ) {
        log.debug("REST request to get Adjustment : {}", adjustmentCode);
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(
                RestResponse.builder().body(adjustmentService.getAdjustmentDetailByAdjustmentCode(adjustmentCode, warehouseCode)).build()
            );
    }

    /**
     * {@code GET  /adjustments/reasons} : get the list of reason.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the Reason, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/adjustments/reasons")
    public ResponseEntity<RestResponse<Object>> getListReason() {
        log.debug("REST request to get Reasons ");
        return ResponseEntity.status(HttpStatus.OK).body(RestResponse.builder().body(Reason.getKeyAndValue()).build());
    }

    @PostMapping("/adjustments/listing/export-excel")
    public HttpEntity<byte[]> exportReceiptNoteFile(@RequestBody AdjustmentExportRequestDTO filterParams) throws IOException {
        log.debug("REST request to export List Adjustment");
        log.info("START download product template");
        String fileName = DateUtil.formatDate(new Date(), DateUtil.STANDARD_DATE_TIME_CURRENT_FORMAT) + "_" + "AdjustmentList.xlsx";

        byte[] result = adjustmentService.exportAdjustmentExcelFile(fileName, filterParams);

        if (CommonDataUtil.isNotNull(result)) {
            HttpHeaders header = new HttpHeaders();
            header.setContentType(new MediaType("application", "force-download"));
            header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName + "");
            return new ResponseEntity<byte[]>(result, header, HttpStatus.OK);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Return HTTP 400 if no data
    }

    @GetMapping("/adjustments/download-templates")
    public HttpEntity<byte[]> downloadExcelTemplates() throws IOException {
        log.debug("REST request to download excel templates");
        log.info("START download template");

        byte[] result = adjustmentService.getExcelTemplate();

        if (CommonDataUtil.isNotNull(result)) {
            HttpHeaders header = new HttpHeaders();
            header.setContentType(new MediaType("application", "force-download"));
            header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + "Import_internal_template.xlsx" + "");
            return new ResponseEntity<byte[]>(result, header, HttpStatus.OK);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Return HTTP 400 if no data
    }

    /**
     * {@code GET  /adjustments/info/export/:adjustmentCode} : export Adjustment to file pdf.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the Adjustment.
     */
    @GetMapping("/adjustments/{adjustmentCode}/export-pdf")
    public ResponseEntity<byte[]> exportAdjustmentPdf(@PathVariable String adjustmentCode) {
        log.debug("REST request: adjustments/info/export/{adjustmentCode} --- Export info Adjustment");

        // Create file pdf
        String fileName = DateUtil.formatDate(new Date(), DateUtil.STANDARD_DATE_TIME_CURRENT_FORMAT) + "_" + "Adjustment.pdf";
        byte[] result = adjustmentService.exportAdjustmentPdf(adjustmentCode);
        if (CommonDataUtil.isNotNull(result)) {
            //            HttpHeaders header = new HttpHeaders();
            //            header.setContentType(new MediaType("application", "force-download"));
            //            header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName + "");
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Return HTTP 400 if no data
    }
}
