package com.yes4all.web.rest.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.service.WarehouseService;
import com.yes4all.service.dto.response.WarehouseResponseDTO;
import com.yes4all.web.rest.payload.RestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for managing {@link com.yes4all.domain.Warehouse}.
 */
@RestController
@RequestMapping("/api")
public class WarehouseResource {

    private final Logger log = LoggerFactory.getLogger(WarehouseResource.class);

    @Autowired
    WarehouseService warehouseService;

    /**
     * {@code GET  /warehouses} : get all the warehouses.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of warehouses in body.
     */
    @GetMapping("/warehouses/listing")
    public ResponseEntity<RestResponse<Object>> getAllWarehouses() throws JsonProcessingException {
        log.debug("REST request: /warehouses/list --- to get all Warehouses");

        List<WarehouseResponseDTO> result = warehouseService.findAll();
        if (CommonDataUtil.isNull(result)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(null).build());
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
    }
}
