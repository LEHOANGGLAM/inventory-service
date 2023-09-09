package com.yes4all.service;

import com.yes4all.domain.Warehouse;
import com.yes4all.service.dto.response.WarehouseResponseDTO;

import java.util.List;

/**
 * Service Interface for managing {@link Warehouse}.
 */
public interface WarehouseService {

    /**
     * Get all the warehouses.
     *
     * @return the list of entities.
     */
    List<WarehouseResponseDTO> findAll();

}
