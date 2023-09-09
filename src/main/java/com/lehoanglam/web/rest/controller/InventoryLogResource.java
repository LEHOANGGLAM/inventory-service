package com.yes4all.web.rest.controller;

import com.yes4all.domain.InventoryLog;
import com.yes4all.repository.InventoryLogRepository;
import com.yes4all.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.yes4all.domain.InventoryLog}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class InventoryLogResource {

    private final Logger log = LoggerFactory.getLogger(InventoryLogResource.class);

    private static final String ENTITY_NAME = "inventoryManagementInventoryLog";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final InventoryLogRepository inventoryLogRepository;

    public InventoryLogResource(InventoryLogRepository inventoryLogRepository) {
        this.inventoryLogRepository = inventoryLogRepository;
    }

    /**
     * {@code POST  /inventory-logs} : Create a new inventoryLog.
     *
     * @param inventoryLog the inventoryLog to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new inventoryLog, or with status {@code 400 (Bad Request)} if the inventoryLog has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/inventory-logs")
    public ResponseEntity<InventoryLog> createInventoryLog(@RequestBody InventoryLog inventoryLog) throws URISyntaxException {
        log.debug("REST request to save InventoryLog : {}", inventoryLog);
        if (inventoryLog.getId() != null) {
            throw new BadRequestAlertException("A new inventoryLog cannot already have an ID", ENTITY_NAME, "idexists");
        }
        InventoryLog result = inventoryLogRepository.save(inventoryLog);
        return ResponseEntity
            .created(new URI("/api/inventory-logs/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /inventory-logs/:id} : Updates an existing inventoryLog.
     *
     * @param id the id of the inventoryLog to save.
     * @param inventoryLog the inventoryLog to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated inventoryLog,
     * or with status {@code 400 (Bad Request)} if the inventoryLog is not valid,
     * or with status {@code 500 (Internal Server Error)} if the inventoryLog couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/inventory-logs/{id}")
    public ResponseEntity<InventoryLog> updateInventoryLog(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody InventoryLog inventoryLog
    ) throws URISyntaxException {
        log.debug("REST request to update InventoryLog : {}, {}", id, inventoryLog);
        if (inventoryLog.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, inventoryLog.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!inventoryLogRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        InventoryLog result = inventoryLogRepository.save(inventoryLog);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, inventoryLog.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /inventory-logs/:id} : Partial updates given fields of an existing inventoryLog, field will ignore if it is null
     *
     * @param id the id of the inventoryLog to save.
     * @param inventoryLog the inventoryLog to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated inventoryLog,
     * or with status {@code 400 (Bad Request)} if the inventoryLog is not valid,
     * or with status {@code 404 (Not Found)} if the inventoryLog is not found,
     * or with status {@code 500 (Internal Server Error)} if the inventoryLog couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/inventory-logs/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<InventoryLog> partialUpdateInventoryLog(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody InventoryLog inventoryLog
    ) throws URISyntaxException {
        log.debug("REST request to partial update InventoryLog partially : {}, {}", id, inventoryLog);
        if (inventoryLog.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, inventoryLog.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!inventoryLogRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<InventoryLog> result = inventoryLogRepository
            .findById(inventoryLog.getId())
            .map(existingInventoryLog -> {
                if (inventoryLog.getProductId() != null) {
                    existingInventoryLog.setProductId(inventoryLog.getProductId());
                }
                if (inventoryLog.getWarehouseId() != null) {
                    existingInventoryLog.setWarehouseId(inventoryLog.getWarehouseId());
                }
                if (inventoryLog.getUserId() != null) {
                    existingInventoryLog.setUserId(inventoryLog.getUserId());
                }
                if (inventoryLog.getQuantityBefore() != null) {
                    existingInventoryLog.setQuantityBefore(inventoryLog.getQuantityBefore());
                }
                if (inventoryLog.getQuantityAfter() != null) {
                    existingInventoryLog.setQuantityAfter(inventoryLog.getQuantityAfter());
                }
                if (inventoryLog.getType() != null) {
                    existingInventoryLog.setType(inventoryLog.getType());
                }
                if (inventoryLog.getReferenceId() != null) {
                    existingInventoryLog.setReferenceId(inventoryLog.getReferenceId());
                }
                if (inventoryLog.getNote() != null) {
                    existingInventoryLog.setNote(inventoryLog.getNote());
                }
                if (inventoryLog.getUpdatedAt() != null) {
                    existingInventoryLog.setUpdatedAt(inventoryLog.getUpdatedAt());
                }

                return existingInventoryLog;
            })
            .map(inventoryLogRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, inventoryLog.getId().toString())
        );
    }

    /**
     * {@code GET  /inventory-logs} : get all the inventoryLogs.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of inventoryLogs in body.
     */
    @GetMapping("/inventory-logs")
    public List<InventoryLog> getAllInventoryLogs() {
        log.debug("REST request to get all InventoryLogs");
        return inventoryLogRepository.findAll();
    }

    /**
     * {@code GET  /inventory-logs/:id} : get the "id" inventoryLog.
     *
     * @param id the id of the inventoryLog to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the inventoryLog, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/inventory-logs/{id}")
    public ResponseEntity<InventoryLog> getInventoryLog(@PathVariable Long id) {
        log.debug("REST request to get InventoryLog : {}", id);
        Optional<InventoryLog> inventoryLog = inventoryLogRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(inventoryLog);
    }

    /**
     * {@code DELETE  /inventory-logs/:id} : delete the "id" inventoryLog.
     *
     * @param id the id of the inventoryLog to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/inventory-logs/{id}")
    public ResponseEntity<Void> deleteInventoryLog(@PathVariable Long id) {
        log.debug("REST request to delete InventoryLog : {}", id);
        inventoryLogRepository.deleteById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
