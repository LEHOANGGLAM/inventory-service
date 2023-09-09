package com.yes4all.web.rest.controller;

import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.DateUtil;
import com.yes4all.service.InventoryLocationService;
import com.yes4all.service.dto.request.InventoryLocationExportRequestDTO;
import com.yes4all.service.dto.request.InventoryLocationParamDTO;
import com.yes4all.service.dto.response.InventoryLocationResponseDTO;
import com.yes4all.web.rest.payload.RestResponse;
import java.net.URISyntaxException;
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
 * REST controller for managing {@link com.yes4all.domain.InventoryLocation}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class InventoryLocationResource {

    private final Logger log = LoggerFactory.getLogger(InventoryLocationResource.class);

    private static final String ENTITY_NAME = "inventoryManagementInventoryLocation";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    @Autowired
    InventoryLocationService inventoryLocationService;

    /**
     * {@code POST  /inventory-locations} : Get listing Inventory Location .
     *
     * @param filterParams the inventoryLocation to listing.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new inventoryLocation, or with status {@code 400 (Bad Request)} if the inventoryLocation has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/inventory-locations/listing")
    public ResponseEntity<RestResponse<Object>> createInventoryLocation(@RequestBody InventoryLocationParamDTO filterParams)
        throws URISyntaxException {
        log.debug("REST request to save InventoryLocation : {}", filterParams);
        Page<InventoryLocationResponseDTO> result = inventoryLocationService.getListInventoryLocationByCondition(filterParams);
        return ResponseEntity.status(HttpStatus.OK).body(RestResponse.builder().body(result).build());
    }

    /**
     * {@code POST  /inventory-locations} : Create a export excel file
     *
     * @param filterParams the inventoryLocation to listing to excel.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new inventoryLocation, or with status {@code 400 (Bad Request)} if the inventoryLocation has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/inventory-locations/listing/export-excel")
    public HttpEntity<byte[]> exportListInventoryLocationToExcel(@RequestBody InventoryLocationExportRequestDTO filterParams)
        throws URISyntaxException {
        log.debug("REST request to export List ReceiptNotes");
        log.info("START download product template");
        String fileName = DateUtil.formatDate(new Date(), DateUtil.STANDARD_DATE_TIME_CURRENT_FORMAT) + "_" + "InventoryLocation.xlsx";

        byte[] result = inventoryLocationService.exportListInventoryLocationToExcelFile(fileName, filterParams);

        if (CommonDataUtil.isNotNull(result)) {
            HttpHeaders header = new HttpHeaders();
            header.setContentType(new MediaType("application", "force-download"));
            header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName + "");
            return new ResponseEntity<byte[]>(result, header, HttpStatus.OK);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Return HTTP 400 if no data
    }
}
