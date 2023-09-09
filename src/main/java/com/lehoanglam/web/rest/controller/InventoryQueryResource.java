package com.yes4all.web.rest.controller;

import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.DateUtil;
import com.yes4all.repository.InventoryQueryRepository;
import com.yes4all.service.InventoryQueryService;
import com.yes4all.service.dto.request.InventoryQueryExportRequestDTO;
import com.yes4all.service.dto.request.InventoryQueryRequestDTO;
import com.yes4all.service.dto.response.InventoryQueryResponseDTO;
import com.yes4all.web.rest.payload.RestResponse;
import java.io.IOException;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing {@link com.yes4all.domain.InventoryQuery}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class InventoryQueryResource {

    private final Logger log = LoggerFactory.getLogger(InventoryQueryResource.class);

    private static final String ENTITY_NAME = "inventoryManagementInventoryQuery";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final InventoryQueryRepository inventoryQueryRepository;

    @Autowired
    private InventoryQueryService inventoryQueryService;

    public InventoryQueryResource(InventoryQueryRepository inventoryQueryRepository) {
        this.inventoryQueryRepository = inventoryQueryRepository;
    }

    /**
     * {@code GET  /inventory-queries} : get all the inventoryQueries.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of inventoryQueries in body.
     */
    @PostMapping("/inventory-queries")
    public ResponseEntity<RestResponse<Object>> getAllInventoryQueries(@RequestBody InventoryQueryRequestDTO filterParam) {
        log.debug("REST request to get all InventoryQueries");
        Page<InventoryQueryResponseDTO> result = inventoryQueryService.getListInventoryByCondition(filterParam);
        return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
    }

    @PostMapping("/inventory-queries/export")
    public HttpEntity<byte[]> exportInventoryQueries(@RequestBody InventoryQueryExportRequestDTO filterParams) throws IOException {
        log.debug("REST request to export listing ");
        log.info("START download product template");
        String fileName = DateUtil.formatDate(new Date(), DateUtil.STANDARD_DATE_TIME_CURRENT_FORMAT) + "_" + "InventoryQuery.xlsx";

        byte[] result = inventoryQueryService.exportListingExcel(fileName, filterParams);

        if (CommonDataUtil.isNotNull(result)) {
            HttpHeaders header = new HttpHeaders();
            header.setContentType(new MediaType("application", "force-download"));
            header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName + "");
            return new ResponseEntity<>(result, header, HttpStatus.OK);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Return HTTP 400 if no data
    }
}
