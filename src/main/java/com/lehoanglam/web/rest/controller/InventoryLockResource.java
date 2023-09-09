package com.yes4all.web.rest.controller;

import com.yes4all.domain.InventoryLock;
import com.yes4all.repository.InventoryLockRepository;
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
 * REST controller for managing {@link com.yes4all.domain.InventoryLock}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class InventoryLockResource {

    private final Logger log = LoggerFactory.getLogger(InventoryLockResource.class);

    private static final String ENTITY_NAME = "inventoryManagementInventoryLock";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final InventoryLockRepository inventoryLockRepository;

    public InventoryLockResource(InventoryLockRepository inventoryLockRepository) {
        this.inventoryLockRepository = inventoryLockRepository;
    }

    /**
     * {@code POST  /inventory-locks} : Create a new inventoryLock.
     *
     * @param inventoryLock the inventoryLock to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new inventoryLock, or with status {@code 400 (Bad Request)} if the inventoryLock has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/inventory-locks")
    public ResponseEntity<InventoryLock> createInventoryLock(@RequestBody InventoryLock inventoryLock) throws URISyntaxException {
        log.debug("REST request to save InventoryLock : {}", inventoryLock);
        if (inventoryLock.getId() != null) {
            throw new BadRequestAlertException("A new inventoryLock cannot already have an ID", ENTITY_NAME, "idexists");
        }
        InventoryLock result = inventoryLockRepository.save(inventoryLock);
        return ResponseEntity
            .created(new URI("/api/inventory-locks/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /inventory-locks/:id} : Updates an existing inventoryLock.
     *
     * @param id the id of the inventoryLock to save.
     * @param inventoryLock the inventoryLock to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated inventoryLock,
     * or with status {@code 400 (Bad Request)} if the inventoryLock is not valid,
     * or with status {@code 500 (Internal Server Error)} if the inventoryLock couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/inventory-locks/{id}")
    public ResponseEntity<InventoryLock> updateInventoryLock(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody InventoryLock inventoryLock
    ) throws URISyntaxException {
        log.debug("REST request to update InventoryLock : {}, {}", id, inventoryLock);
        if (inventoryLock.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, inventoryLock.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!inventoryLockRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        InventoryLock result = inventoryLockRepository.save(inventoryLock);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, inventoryLock.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /inventory-locks/:id} : Partial updates given fields of an existing inventoryLock, field will ignore if it is null
     *
     * @param id the id of the inventoryLock to save.
     * @param inventoryLock the inventoryLock to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated inventoryLock,
     * or with status {@code 400 (Bad Request)} if the inventoryLock is not valid,
     * or with status {@code 404 (Not Found)} if the inventoryLock is not found,
     * or with status {@code 500 (Internal Server Error)} if the inventoryLock couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/inventory-locks/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<InventoryLock> partialUpdateInventoryLock(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody InventoryLock inventoryLock
    ) throws URISyntaxException {
        log.debug("REST request to partial update InventoryLock partially : {}, {}", id, inventoryLock);
        if (inventoryLock.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, inventoryLock.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!inventoryLockRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<InventoryLock> result = inventoryLockRepository
            .findById(inventoryLock.getId())
            .map(existingInventoryLock -> {
                if (inventoryLock.getLock() != null) {
                    existingInventoryLock.setLock(inventoryLock.getLock());
                }
                if (inventoryLock.getProductId() != null) {
                    existingInventoryLock.setProductId(inventoryLock.getProductId());
                }
                if (inventoryLock.getWarehouseId() != null) {
                    existingInventoryLock.setWarehouseId(inventoryLock.getWarehouseId());
                }
                if (inventoryLock.getIssueCode() != null) {
                    existingInventoryLock.setIssueCode(inventoryLock.getIssueCode());
                }

                return existingInventoryLock;
            })
            .map(inventoryLockRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, inventoryLock.getId().toString())
        );
    }

    /**
     * {@code GET  /inventory-locks} : get all the inventoryLocks.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of inventoryLocks in body.
     */
    @GetMapping("/inventory-locks")
    public List<InventoryLock> getAllInventoryLocks() {
        log.debug("REST request to get all InventoryLocks");
        return inventoryLockRepository.findAll();
    }

    /**
     * {@code GET  /inventory-locks/:id} : get the "id" inventoryLock.
     *
     * @param id the id of the inventoryLock to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the inventoryLock, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/inventory-locks/{id}")
    public ResponseEntity<InventoryLock> getInventoryLock(@PathVariable Long id) {
        log.debug("REST request to get InventoryLock : {}", id);
        Optional<InventoryLock> inventoryLock = inventoryLockRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(inventoryLock);
    }

    /**
     * {@code DELETE  /inventory-locks/:id} : delete the "id" inventoryLock.
     *
     * @param id the id of the inventoryLock to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/inventory-locks/{id}")
    public ResponseEntity<Void> deleteInventoryLock(@PathVariable Long id) {
        log.debug("REST request to delete InventoryLock : {}", id);
        inventoryLockRepository.deleteById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
